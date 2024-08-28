package com.ericsson.arrest_it.utils;

import static com.ericsson.arrest_it.common.Constants.*;

import java.io.*;
import java.util.*;

import com.ericsson.arrest_it.common.*;
import com.ericsson.arrest_it.io.PropertyReader;
import com.ericsson.arrest_it.manager.*;

public class GroupUtils {

    private final static Map<String, List<Row>> tableColumnValues = new HashMap<String, List<Row>>();
    private final static Map<String, String> groupNameTableName = new HashMap<String, String>();

    public static Map<String, List<Row>> getTableColumnValues() {
        return tableColumnValues;
    }

    public static void insertGroupsIntoTables(final File config, final DBManager dbManager) throws ArrestItException {
        final Map<Integer, String> dateTimeValues = getDateTimeValues();
        readConfigFile(config, dbManager, dateTimeValues);

        deleteGroupsFromTables(config, dbManager);

        for (final String tableName : tableColumnValues.keySet()) {
            for (final Row row : tableColumnValues.get(tableName)) {
                final String sql = createInsertSQL(dbManager, tableName, row);
                dbManager.executeUpdate(sql);
            }
        }
    }

    public static void deleteGroupsFromTables(final File config, final DBManager dbManager) throws ArrestItException {
        final Map<String, String> groupNameTableName = getGroupNameTableNameFromConfigFile(config);

        for (final String groupName : groupNameTableName.keySet()) {
            final String tableName = groupNameTableName.get(groupName);
            final String sql = createDeleteGroupSQL(tableName, groupName);
            dbManager.executeUpdate(sql);
        }

    }

    protected static void readConfigFile(final File config, final DBManager dbManager, final Map<Integer, String> dateTimeValues)
            throws ArrestItException {
        BufferedReader bufferedReader = null;

        try {
            bufferedReader = new BufferedReader(new FileReader(config));
            String line = bufferedReader.readLine();

            while (line != null) {
                final String[] splitLine = line.split(";");
                final String groupName = splitLine[0].trim();
                final String tableName = splitLine[1].trim();
                final String sql = splitLine[2];
                final List<Row> columnValues = getColumnValues(dbManager, groupName, sql, dateTimeValues);
                updateTableColumnValues(tableName, columnValues);
                groupNameTableName.put(groupName, tableName);

                line = bufferedReader.readLine();
            }

        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

    }

    protected static Map<String, String> getGroupNameTableNameFromConfigFile(final File config) {
        final Map<String, String> groupNameTableName = new HashMap<String, String>();
        BufferedReader bufferedReader = null;

        try {
            bufferedReader = new BufferedReader(new FileReader(config));
            String line = bufferedReader.readLine();

            while (line != null) {
                final String[] splitLine = line.split(";");
                final String groupName = splitLine[0].trim();
                final String tableName = splitLine[1].trim();
                groupNameTableName.put(groupName, tableName);

                line = bufferedReader.readLine();
            }

        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        return groupNameTableName;
    }

    protected static Map<Integer, String> getDateTimeValues() throws ArrestItException {
        final int[] times = PropertyReader.getInstance().getTimes();
        final TimeManager timeManager = new TimeManager();
        timeManager.setMainTime();

        return timeManager.getSqlDateTimes(times);
    }

    private static List<Row> getColumnValues(final DBManager dbManager, final String groupName, final String sql,
                                             final Map<Integer, String> dateTimeValues) throws ArrestItException {
        final List<Row> columnValues = new ArrayList<Row>();
        for (final Integer key : dateTimeValues.keySet()) {
            final String enrichedSql = sql.replaceAll("\\{datetime_id\\}", dateTimeValues.get(key));
            for (final Row row : dbManager.executeQuery(enrichedSql)) {
                row.addColumnValue(GROUP_NAME, groupName + "_" + key);
                columnValues.add(row);
            }
        }
        return columnValues;
    }

    private static void updateTableColumnValues(final String tableName, final List<Row> newColumnValues) {
        if (tableColumnValues.containsKey(tableName)) {
            final List<Row> oldColumnValues = tableColumnValues.get(tableName);
            newColumnValues.addAll(oldColumnValues);
        }
        tableColumnValues.put(tableName, newColumnValues);
    }

    protected static String createInsertSQL(final DBManager dbManager, final String tableName, final Row row) throws ArrestItException {
        final StringBuilder sql = new StringBuilder();
        final List<String> values = new ArrayList<String>();

        sql.append("INSERT INTO ");
        sql.append(tableName);
        sql.append(" (");

        for (final String column : row.getColumnValue().keySet()) {
            sql.append(column);
            sql.append(",");
            if (!isColumnNumeric(dbManager, column, tableName)) {
                values.add("'" + row.getColumnValue().get(column) + "'");
            } else {
                values.add(row.getColumnValue().get(column));
            }
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(") VALUES (");

        for (final String value : values) {
            sql.append(value);
            sql.append(",");
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(");");

        return sql.toString();
    }

    protected static String createDeleteGroupSQL(final String tableName, final String groupName) {
        final StringBuilder sql = new StringBuilder();

        sql.append("DELETE FROM ");
        sql.append(tableName);
        sql.append(" WHERE GROUP_NAME LIKE '");
        sql.append(groupName);
        sql.append("%';");

        return sql.toString();
    }

    protected static boolean isColumnNumeric(final DBManager dbManager, final String column, final String tableName) throws ArrestItException {
        final String sql = createDataTypeQuery(column, tableName);
        final List<Row> rows = dbManager.executeQuery(sql);
        final String dataType = rows.get(0).getColumnValue().get("DATA_TYPE").trim();

        if (dataType.contains("varchar")) {
            return false;
        }

        return true;
    }

    protected static String createDataTypeQuery(final String columnName, final String tableName) {
        final StringBuilder sql = new StringBuilder();
        sql.append("select distinct datatype.domain_name as 'DATA_TYPE'");
        sql.append(" from sys.sysdomain datatype, sys.systabcol col, sys.systab tab");
        sql.append(" where tab.table_id = col.table_id");
        sql.append(" and col.domain_id = datatype.domain_id");
        sql.append(" and col.column_name = '");
        sql.append(columnName);
        sql.append("' ");
        sql.append("and tab.table_name = '");
        sql.append(tableName);
        sql.append("'");

        return sql.toString();
    }
}
