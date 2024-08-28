package com.ericsson.arrest_it.manager;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;

import com.ericsson.arrest_it.common.*;
import com.ericsson.arrest_it.io.MasterValuesReader;
import com.ericsson.arrest_it.utils.SQLUtils;

public class DBValuesManager implements Manager {
    final HashMap<String, String> dbValues;
    final DBManager db;
    final String uiUsername;
    final TimeManager timeManager;

    public DBValuesManager(final DBManager db, final String uiUsername, final TimeManager timeManager) {
        this.db = db;
        this.dbValues = new HashMap<String, String>();
        this.uiUsername = uiUsername;
        this.timeManager = timeManager;
    }

    public HashMap<String, String> getDBValues() {
        return this.dbValues;
    }

    public void addDBValue(final String key, final String value) {
        this.dbValues.put(key, value);
    }

    public String getSingleDbValue(final TestCase testCase, final String key) throws ArrestItException {
        if (StringUtils.containsIgnoreCase(key, "username")) {
            return uiUsername;
        }

        final String keyWithTime = getKeyWithTime(testCase, key);

        if (dbValues.containsKey(keyWithTime)) {
            return dbValues.get(keyWithTime);
        }

        String value = getValueFromPropertyFile(key);

        if (value == null) {
            value = getDefaultValue(testCase, key);
        } else if (SQLUtils.isSQL(value)) {
            if (StringUtils.containsIgnoreCase(value, "datetime_id")) {
                value = timeManager.updateSqlWithRelevantTestTimes(value, testCase.getTime());
            }
            value = db.executeFixedQuery(value);
        }

        dbValues.put(keyWithTime, value);

        return value;
    }

    protected String getValueFromPropertyFile(String key) throws ArrestItException {
        final MasterValuesReader masterValuesReader = new MasterValuesReader(Constants.getMasterValuesFile());
        key = removeDashsAndMasterFromKey(key);
        return masterValuesReader.getValue(key);
    }

    String removeDashsAndMasterFromKey(String key) {
        final String lastCharacter = getLastCharacter(key);
        if (lastCharacter.equals("-")) {
            key = key.substring(0, key.length() - 1);
        }
        key = key.replaceAll("-master-", "");
        return key;
    }

    String getLastCharacter(final String key) {
        return key.substring(key.length() - 1, key.length());
    }

    private String getDefaultValue(final TestCase tc, final String key) throws ArrestItException {
        String sql = null;
        String result;
        final SQLManager sqlManager = (SQLManager) ManagerFactory.getInstance().getManager(Constants.SQL_MANAGER);

        if (isGroup(key)) {
            sql = sqlManager.getGroupSql(tc, key);
        } else {
            sql = sqlManager.getIndividualSql(tc, key);
        }

        result = db.executeFixedQuery(sql);

        if (!result.isEmpty()) {
            result = storeValuesIfNode(tc, key, result);
        }
        result = result.trim();
        result = result.replace(" ", "%20");

        final String keyWithTime = getKeyWithTime(tc, key);
        dbValues.put(keyWithTime, result);

        return result;
    }

    /**
     * @param tc
     * @param key
     * @param result
     */
    private String storeValuesIfNode(final TestCase tc, final String key, String result) throws ArrestItException {
        try {
            if (StringUtils.containsIgnoreCase(key, "-ControllerNode")) {
                result = SQLUtils.createControllerNodeSql(result);
                result = db.executeFixedQuery(result);
                final String[] nodeSplit = result.split(",");
                addDBValue("-master-controllerNodeIndexZero:" + tc.getTime(), nodeSplit[0]);
                addDBValue("-master-controllerNodeIndexOne:" + tc.getTime(), nodeSplit[1]);
                addDBValue("-master-controllerNodeIndexTwo:" + tc.getTime(), nodeSplit[2]);

            } else if (StringUtils.containsIgnoreCase(key, "-AccessAreaNode")) {
                result = SQLUtils.createAccessAreaNodeSql(result);
                result = db.executeFixedQuery(result);
                final String[] nodeSplit = result.split(",");
                addDBValue("-master-accessAreaNodeIndexZero:" + tc.getTime(), nodeSplit[0]);
                addDBValue("-master-accessAreaNodeIndexOne:" + tc.getTime(), nodeSplit[1]);
                addDBValue("-master-accessAreaNodeIndexTwo:" + tc.getTime(), nodeSplit[2]);
                addDBValue("-master-accessAreaNodeIndexThree:" + tc.getTime(), nodeSplit[3]);
                addDBValue("-master-accessAreaNodeIndexFour:" + tc.getTime(), nodeSplit[4]);
            } else if (StringUtils.containsIgnoreCase(key, "-TerminalNode")) {
                result = SQLUtils.createTerminalNodeSql(result);
                result = db.executeFixedQuery(result);
            }
        } catch (final ArrayIndexOutOfBoundsException aiException) {
            throw new ArrestItException("Could not obtain master node values");
        }

        return result;
    }

    /**
     * @param key
     * @return
     */
    public boolean isGroup(final String key) {
        return StringUtils.containsIgnoreCase(key, "group");
    }

    String getKeyWithTime(final TestCase tc, final String key) {
        return key + ":" + tc.getTime();
    }

}