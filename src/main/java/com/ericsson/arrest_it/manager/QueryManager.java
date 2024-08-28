package com.ericsson.arrest_it.manager;

import static com.ericsson.arrest_it.common.Constants.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.arrest_it.common.*;
import com.ericsson.arrest_it.logging.LogbackFileUtils;
import com.ericsson.arrest_it.results.FrameWorkResult;

public class QueryManager implements Manager {
    private static final String EVENT_TIME_FLAG_A = "where event_time =";
    private static final String EVENT_TIME_FLAG_B = "and event_time =";
    private static final Logger slf4jLogger = LoggerFactory.getLogger(LogbackFileUtils.ARREST_IT_LOGGER);

    private final DBManager dbManager;
    private final TimeManager timeManager;
    private final DBValuesManager dbValuesManager;

    public QueryManager(final DBManager dbManager, final DBValuesManager dbValuesManager, final TimeManager timeManager) {
        this.dbManager = dbManager;
        this.dbValuesManager = dbValuesManager;
        this.timeManager = timeManager;
    }

    public void urlEnrichment(final TestCase testCase) throws ArrestItException {

        final Map<String, String> parentValues = testCase.getParentValues();

        final Map<String, String> urls = new HashMap<String, String>();
        urls.put("gridUrl", testCase.getUrl());
        if (testCase.getCsvUrl() != null) {
            urls.put("csvUrl", testCase.getCsvUrl());
        }

        for (final String urlKey : urls.keySet()) {
            String url = urls.get(urlKey);
            final List<String> parentWords = getWordsOfInterest(url, "\\{([0-9]+[.][A-Za-z]+)\\}");
            final List<String> masterWords = getWordsOfInterest(url, "\\{-master-([A-Za-z-]+)\\}");

            for (final String masterWord : masterWords) {
                final String searchKey = "\\{" + masterWord + "\\}";
                url = url.replaceAll(searchKey, dbValuesManager.getSingleDbValue(testCase, masterWord));

            }
            for (final String parentWord : parentWords) {
                final String urlWithMarkers = "\\{" + parentWord + "\\}";

                if (parentValues.containsKey(parentWord)) {
                    final String updatedParentWord = checkParentWordSpaces(parentValues.get(parentWord));
                    url = url.replaceAll(urlWithMarkers, updatedParentWord);
                } else {

                    slf4jLogger.warn("Url from Xml contains references to parent values not passed to test case, " + url + "\t" + parentWords);
                    throw new ArrestItException("Url from Xml contains references to parent values not passed to test case");
                }
            }
            url = url.replaceAll("\\{", "");
            url = url.replaceAll("\\}", "");
            urls.put(urlKey, url);
        }

        testCase.setUrl(urls.get("gridUrl"));
        testCase.setCsvUrl(urls.get("csvUrl"));
    }

    public List<String> getWordsOfInterest(final String subject, final String pattern) {
        final List<String> listOfWords = new ArrayList<String>();
        final Pattern p = Pattern.compile(pattern);
        final Matcher m = p.matcher(subject);
        while (m.find()) {
            String wordOfInterest = m.group();
            wordOfInterest = wordOfInterest.replaceAll("\\{", "");
            wordOfInterest = wordOfInterest.replaceAll("\\}", "");
            listOfWords.add(wordOfInterest);
        }

        return listOfWords;
    }

    /**
     * 
     * @param parentWord
     * @return the parent word updated with %20 (ascii equivalent of a space) so that the url is executed correctly
     */
    String checkParentWordSpaces(final String parentWord) {

        if (parentWord.contains(" ")) {

            return parentWord.replaceAll(" ", "%20");
        } else {
            return parentWord;
        }

    }

    public void expandAndRunSql(TestCase testCase, final List<Integer> validationRowNumbers) {

        for (final Integer validationRowNumber : validationRowNumbers) {
            for (DBQuery dbQuery : testCase.getDbQueries()) {
                if (dbQuery.isRepeating()) {
                    try {
                        dbQuery = enrichRepeatingDbQuery(dbQuery, validationRowNumber, testCase);
                        testCase = runSql(testCase, dbQuery, String.valueOf(validationRowNumber));
                    } catch (final ArrestItException ae) {
                        testCase.getTestResultHolder().addTestResult(
                                new FrameWorkResult("Could not handle sql: " + dbQuery.getStatement(), String.valueOf(validationRowNumber), false));
                    }

                }
            }
        }
        for (DBQuery dbQuery : testCase.getDbQueries()) {
            if (!dbQuery.isRepeating()) {
                try {
                    dbQuery = enrichNonRepeatingDbQuery(dbQuery, testCase);
                    testCase = runSql(testCase, dbQuery, NON_REPEAT_VALIDATION_FLAG);
                } catch (final ArrestItException ae) {
                    testCase.getTestResultHolder().addTestResult(
                            new FrameWorkResult("Could not handle sql: " + dbQuery.getStatement(), NON_REPEAT_VALIDATION_FLAG, false));
                }

            }
        }
    }

    public DBQuery enrichRepeatingDbQuery(final DBQuery dbQuery, final int validationRowNumber, final TestCase testCase) throws ArrestItException {
        String sql = dbQuery.getTemplateStatement();

        sql = enrichSqlWithMasterValues(sql, testCase);
        sql = enrichSqlWithParentValues(sql, testCase);
        sql = enrichSqlWithJsonValues(sql, validationRowNumber, testCase);

        dbQuery.setStatement(sql);
        return dbQuery;
    }

    public DBQuery enrichNonRepeatingDbQuery(final DBQuery dbQuery, final TestCase testCase) throws ArrestItException {
        String sql = dbQuery.getTemplateStatement();
        sql = enrichSqlWithMasterValues(sql, testCase);
        sql = enrichSqlWithParentValues(sql, testCase);

        dbQuery.setStatement(sql);
        return dbQuery;
    }

    public String enrichSqlWithMasterValues(String sql, final TestCase testCase) throws ArrestItException {
        List<String> masterKeys;
        masterKeys = getWordsOfInterest(sql, "\\{-master-([A-Za-z-]+)\\}");

        for (final String masterKey : masterKeys) {
            final String searchKey = "\\{" + masterKey + "\\}";
            sql = sql.replaceAll(searchKey, dbValuesManager.getSingleDbValue(testCase, masterKey));
        }
        return sql;
    }

    public String enrichSqlWithParentValues(String sql, final TestCase testCase) {
        List<String> parentKeys;
        parentKeys = getWordsOfInterest(sql, "\\{([0-9]+[.][A-Za-z]+)\\}");
        for (final String parentKey : parentKeys) {
            final String parentKeyWithMarkers = "\\{" + parentKey + "\\}";
            sql = sql.replaceAll(parentKeyWithMarkers, testCase.getParentValues().get(parentKey));
        }
        return sql;
    }

    public String enrichSqlWithJsonValues(String sql, final int validationRowNumber, final TestCase testCase) {
        List<String> jsonKeys;
        jsonKeys = getWordsOfInterest(sql, "\\{([A-Za-z]+)\\}");
        for (final String jsonKey : jsonKeys) {
            final String searchKeyWithQuotes = "'\\{" + jsonKey + "\\}'";
            final String searchKey = "\\{" + jsonKey + "\\}";
            String jsonValue = getJSONValue(testCase, String.valueOf(validationRowNumber), jsonKey);
            if (jsonValue.isEmpty()) {
                if (!sql.contains(searchKeyWithQuotes)) {
                    jsonValue = "null";
                } else {
                    jsonValue = "''";
                }
            }
            sql = sql.replaceAll(searchKey, jsonValue);
        }
        return sql;
    }

    public TestCase runSql(TestCase tc, DBQuery dbQuery, final String rowIndex) throws ArrestItException {

        dbQuery = updateSqlWithEventTimeIfRelevent(dbQuery);

        Map<String, String> sqlVars = QueryManager.getSqlVariables(dbQuery.getStatement());

        if (dbQuery.isInstructionPresent()) {
            sqlVars = performDbQueryWithInstruction(dbQuery, sqlVars, tc, rowIndex);
        } else {
            sqlVars = executeQueryWithNullHandling(dbQuery.getStatement(), sqlVars, rowIndex, tc);
        }

        tc = addSqlValuesToTestCase(sqlVars, rowIndex, tc, dbQuery);

        return tc;
    }

    private TestCase addSqlValuesToTestCase(final Map<String, String> sqlVars, final String rowIndex, final TestCase testCase, final DBQuery dbQuery) {
        String rowMarker = "";

        if (dbQuery.isRepeating()) {
            rowMarker = rowIndex + ":";
        }

        for (final String oldKey : sqlVars.keySet()) {
            final String newKey = rowMarker + oldKey;
            testCase.addSQLVariable(newKey, sqlVars.get(oldKey));
        }

        return testCase;
    }

    private DBQuery updateSqlWithEventTimeIfRelevent(final DBQuery dbQuery) {
        String sql = dbQuery.getStatement();

        if (StringUtils.containsIgnoreCase(sql, EVENT_TIME_FLAG_A) || StringUtils.containsIgnoreCase(sql, EVENT_TIME_FLAG_B)) {
            sql = timeManager.manageEventTime(sql);
            dbQuery.setStatement(sql);
        }

        return dbQuery;
    }

    private Map<String, String> performDbQueryWithInstruction(final DBQuery dbQuery, Map<String, String> sqlVars, final TestCase testCase,
                                                              final String rowIndex) throws ArrestItException {

        String instruction = dbQuery.getInstruction();
        String sql = dbQuery.getStatement();

        if (StringUtils.containsIgnoreCase(instruction, "use:")) {
            sql = enrichSqlWithID(sql, instruction, testCase, rowIndex);
            sqlVars = executeQueryWithNullHandling(sql, sqlVars, rowIndex, testCase);
        } else if (StringUtils.containsIgnoreCase(instruction, "Plus:") && !StringUtils.containsIgnoreCase(sql, "distinct")) {
            final String queryB = getSecondSql(sql, instruction);
            Map<String, String> sqlVarsB = new HashMap<String, String>(sqlVars);
            sqlVars = executeQueryWithNullHandling(sql, sqlVars, rowIndex, testCase);
            sqlVarsB = executeQueryWithNullHandling(queryB, sqlVarsB, rowIndex, testCase);

            sqlVars = addDbResults(sqlVars, sqlVarsB);
        } else if (StringUtils.containsIgnoreCase(instruction, "Or:")) {
            sqlVars = executeQueryWithNullHandling(sql, sqlVars, rowIndex, testCase);
            boolean allNull = true;

            for (final String k : sqlVars.keySet()) {
                if (StringUtils.containsIgnoreCase(sql, "sum")) {
                    if (!sqlVars.get(k).equals("0")) {
                        allNull = false;
                    }
                } else if (sqlVars.get(k) != null) {
                    allNull = false;
                }
            }

            if (!allNull) {
                sql = getSecondSql(sql, instruction);
                sqlVars = executeQueryWithNullHandling(sql, sqlVars, rowIndex, testCase);
            }
        } else if (StringUtils.containsIgnoreCase(instruction, "distinctPlus")) {
            sql = splitSqlToDistinct(sql, instruction);
            sqlVars = executeQueryWithNullHandling(sql, sqlVars, rowIndex, testCase);
        }

        return sqlVars;
    }

    public String splitSqlToDistinct(String sql, final String instruction) {
        sql = sql.toLowerCase();

        String sqlDeclarations = "";

        if (sql.contains("declare @")) {
            final String regEx = "select [a-z]{1}";
            final Pattern pattern = Pattern.compile(regEx);
            final Matcher matcher = pattern.matcher(sql);
            matcher.find();
            final int beginningOfRealQuery = matcher.start();
            sqlDeclarations = sql.substring(0, beginningOfRealQuery);
            sql = sql.substring(beginningOfRealQuery);
        }

        final String otherTable = instruction.substring(instruction.indexOf(":") + 1);
        int fromIndex = sql.indexOf("from");
        fromIndex = fromIndex + "from".length() + 1;
        final String beginningOfQuery = sql.substring(0, fromIndex);

        String originalTableSql = beginningOfQuery.replaceAll("\\(", "");
        originalTableSql = originalTableSql.replaceAll("\\)", "");
        originalTableSql = originalTableSql.replaceAll("count", "");
        originalTableSql = originalTableSql.replaceAll("distinct", "");
        final int asIndex = StringUtils.indexOfIgnoreCase(originalTableSql, "as");
        originalTableSql = originalTableSql.substring(0, asIndex);

        originalTableSql = originalTableSql + " from " + sql.substring(fromIndex);

        final String secondTableSql = deprecatedSwapTableName(originalTableSql, otherTable);

        final String newSql = sqlDeclarations + beginningOfQuery + " ( " + originalTableSql + " union " + secondTableSql + " ) tempTable";
        return newSql;
    }

    static Map<String, String> getSqlVariables(final String stmt) {
        final Map<String, String> s = new HashMap<String, String>();
        String sqlVar;
        final Pattern p = Pattern.compile("[as|AS|As|aS] \\'(.*?)\\'");
        final Matcher m = p.matcher(stmt);
        while (m.find()) {
            sqlVar = m.group();
            sqlVar = sqlVar.substring(sqlVar.indexOf('\'')).replaceAll("'", "");
            if (StringUtils.isAlphanumeric(sqlVar)) {
                s.put(sqlVar, null);
            }

        }
        return s;
    }

    Map<String, String> addDbResults(final Map<String, String> sqlVarsA, final Map<String, String> sqlVarsB) {

        final Map<String, String> sqlVars = new HashMap<String, String>();
        Double firstVal, secondVal;
        for (final String k : sqlVarsA.keySet()) {
            if (NumberUtils.isNumber(sqlVarsA.get(k)) && NumberUtils.isNumber(sqlVarsB.get(k))) {
                firstVal = Double.parseDouble(sqlVarsA.get(k));
                secondVal = Double.parseDouble(sqlVarsB.get(k));
                sqlVars.put(k, String.valueOf(firstVal + secondVal));
            } else if (!NumberUtils.isNumber(sqlVarsA.get(k)) && NumberUtils.isNumber(sqlVarsB.get(k))) {
                sqlVars.put(k, sqlVarsB.get(k));
            } else if (NumberUtils.isNumber(sqlVarsA.get(k)) && !NumberUtils.isNumber(sqlVarsB.get(k))) {
                sqlVars.put(k, sqlVarsA.get(k));
            } else {
                sqlVars.put(k, "Invalid Test Operation, attempting to add two non-numerical values");
            }
        }
        return sqlVars;
    }

    String getSecondSql(String sql, final String instruction) {
        final String newTableName = instruction.substring(instruction.indexOf(":") + 1);
        sql = deprecatedSwapTableName(sql, newTableName.toLowerCase());
        return sql;
    }

    String enrichSqlWithID(String sql, final String instruction, final TestCase tc, final String rowIndex) {

        final Map<String, String> instructionsWithValues = splitandEnrichSqlInstructions(instruction, tc, rowIndex);

        for (final String key : instructionsWithValues.keySet()) {
            sql = insertDirectionToSql(sql, instructionsWithValues.get(key), key);
        }

        return sql;
    }

    public Map<String, String> splitandEnrichSqlInstructions(final String instruction, final TestCase tc, final String rowIndex) {
        final Map<String, String> instructions = new HashMap<String, String>();
        String value = "";
        final String directionKey = instruction.substring(instruction.indexOf(":") + 1);
        final String[] keys = directionKey.split(",");

        for (final String k : keys) {
            if (k.contains(".")) {
                value = tc.getParentValues().get(k);
            } else if (StringUtils.isAlpha(k)) {
                value = getJSONValue(tc, rowIndex, k);
            }
            instructions.put(k, value);
        }
        return instructions;
    }

    String insertDirectionToSql(String sql, final String jsonValue, final String jsonKey) {

        if (StringUtils.containsIgnoreCase(jsonKey, "ratId")) {
            sql = updateCoreQueriesWithRatId(sql, jsonValue);
        } else if (StringUtils.containsIgnoreCase(jsonKey, "categoryId")) {
            sql = updateWcdmaQueriesWithCategoryId(sql, jsonValue);
        } else if (StringUtils.containsIgnoreCase(jsonKey, "eventId")) {
            sql = updateCoreQueriesWithEventId(sql, jsonValue);
        } else if (StringUtils.containsIgnoreCase(jsonKey, "eventResId")) {
            sql = updateCoreQueriesWithEventResultId(sql, jsonValue);
        }
        return sql;
    }

    String updateCoreQueriesWithRatId(final String sql, final String ratId) {

        String newTableName = "";
        String[] tablePrefixes = { "event_e_", " dim_e_" };
        String[] valuesToReplace = { "sgeh", "lte" };

        if (ratId.equals("0") || ratId.equals("1")) {
            newTableName = "sgeh";
        } else if (ratId.equals("2")) {
            newTableName = "lte";
        }
        return swapTableNames(sql, newTableName, tablePrefixes, valuesToReplace);
    }

    String updateWcdmaQueriesWithCategoryId(final String sql, final String categoryId) {

        String newTableName = "";
        String[] tablePrefixes = { "event_e_ran_hfa_" };
        String[] valuesToReplace = { "soho", "hsdsch", "irat", "ifho" };

        if (categoryId.equals("0")) {
            newTableName = "soho";
        } else if (categoryId.equals("1")) {
            newTableName = "hsdsch";
        } else if (categoryId.equals("2")) {
            newTableName = "ifho";
        } else if (categoryId.equals("3")) {
            newTableName = "irat";
        }
        return swapTableNames(sql, newTableName, tablePrefixes, valuesToReplace);
    }

    String updateCoreQueriesWithEventId(final String sql, final String eventId) {
        String newTableName = "";
        String[] tablePrefixes = { "event_e_", " dim_e_" };
        String[] valuesToReplace = { "lte", "sgeh" };
        if (eventId.equals("0") || eventId.equals("1") || eventId.equals("2") || eventId.equals("3") || eventId.equals("4") || eventId.equals("14")
                || eventId.equals("15")) {
            newTableName = "sgeh";
        } else if (eventId.equals("5") || eventId.equals("6") || eventId.equals("7") || eventId.equals("8") || eventId.equals("9")
                || eventId.equals("10") || eventId.equals("11") || eventId.equals("12") || eventId.equals("13") || eventId.equals("16")) {
            newTableName = "lte";
        }
        return swapTableNames(sql, newTableName, tablePrefixes, valuesToReplace);
    }

    String updateCoreQueriesWithEventResultId(final String sql, final String eventResultId) {

        String newTableName = "";
        String[] tablePrefixes = { "event_e_" };
        String[] valuesToReplace = { "err", "suc" };

        if (eventResultId.equals("1")) {
            newTableName = "err";
        } else if (eventResultId.equals("0") || eventResultId.equals("2") || eventResultId.equals("3")) {
            newTableName = "suc";
        }
        return swapTableNames(sql, newTableName, tablePrefixes, valuesToReplace);
    }

    String swapTableNames(String sql, final String newTableName, final String[] tablePrefixes, final String[] valuesToReplace) {

        Pattern spaceCommaPattern = Pattern.compile(("[\\s|,]+"));
        String newTable = "";

        for (String tablePrefix : tablePrefixes) {
            if (StringUtils.containsIgnoreCase(sql, tablePrefix)) {

                int firstIndexOfTableName = StringUtils.indexOfIgnoreCase(sql, tablePrefix);

                while (firstIndexOfTableName != -1) {
                    Matcher commaSpaceMatcher = spaceCommaPattern.matcher(sql);
                    commaSpaceMatcher.find(firstIndexOfTableName + 1);
                    int lastIndexOfTableName = commaSpaceMatcher.start();

                    String oldTable = sql.substring(firstIndexOfTableName, lastIndexOfTableName);
                    String oldTableLower = oldTable.toLowerCase();

                    for (String valueToReplace : valuesToReplace) {
                        if (oldTableLower.contains(valueToReplace)) {
                            newTable = oldTableLower.replace(valueToReplace, newTableName);
                        }
                    }

                    sql = sql.replace(oldTable, newTable);
                    firstIndexOfTableName = StringUtils.indexOfIgnoreCase(sql, tablePrefix, lastIndexOfTableName);
                }
            }
        }

        return sql;
    }

    String addRatDependentHier321Search(String sql) {
        if (StringUtils.containsIgnoreCase(sql, "event_e_lte_imsi_suc_raw")) {
            final String hierarchy3QueryElement = getHier3QueryElement(sql);

            sql = sql + "and hier3_id = convert(int," + hierarchy3QueryElement + ")";
        }

        return sql;
    }

    String getHier3QueryElement(final String sql) {
        final int firstIndex = StringUtils.indexOfIgnoreCase(sql, "hierarchy_3");
        final int secondIndex = sql.indexOf("'", firstIndex);
        final int thirdIndex = sql.indexOf("'", secondIndex);

        return sql.substring(secondIndex, thirdIndex) + "'";
    }

    public String getJSONValue(final TestCase tc, final String rowIndex, final String jsonKey) {
        String jsonValue = "";
        jsonValue = updateJSONValueIfKeyIsJSONObject(tc, rowIndex, jsonKey);
        jsonValue = updateJSONValueIfKeyIsSpecial(tc, jsonKey, jsonValue);

        return jsonValue;
    }

    /**
     * @param tc
     * @param jsonKey
     * @param valueToReturn
     * @return
     */
    private String updateJSONValueIfKeyIsSpecial(final TestCase tc, final String jsonKey, String jsonValue) {
        if (jsonKey.equalsIgnoreCase("eventIdFromEventIdNode")) {
            final int commaIndex = jsonValue.indexOf(",");
            jsonValue = jsonValue.substring(commaIndex + 1);
        } else if (jsonKey.equalsIgnoreCase("accessAreaFromCellNode")) {
            final String[] items = jsonValue.split(",");
            jsonValue = items[0];
        } else if (jsonKey.equalsIgnoreCase("controllerFromCellNode")) {
            final String[] items = jsonValue.split(",");
            jsonValue = items[2];
        } else if (jsonKey.equalsIgnoreCase("vendorFromCellNode")) {
            final String[] items = jsonValue.split(",");
            jsonValue = items[3];
        } else if (jsonKey.equalsIgnoreCase("ratIdFromCellNode")) {
            final String[] items = jsonValue.split(",");
            jsonValue = items[4];
        } else if (jsonKey.equalsIgnoreCase("tracFromTauNode")) {
            final String[] items = jsonValue.split(",");
            jsonValue = items[0];
        } else if (jsonKey.equalsIgnoreCase("tauDescFromTauNode")) {
            final String[] items = jsonValue.split(",");
            jsonValue = items[1];
        } else if (jsonKey.equalsIgnoreCase("accessAreaFromHandoverNode")) {
            final String[] items = jsonValue.split(",");
            jsonValue = items[0];
        } else if (jsonKey.equalsIgnoreCase("vendorFromHandoverNode")) {
            final String[] items = jsonValue.split(",");
            jsonValue = items[1];
        } else if (jsonKey.equalsIgnoreCase("busyhour")) {
            jsonValue = timeManager.getBusyHourString(jsonValue);
        } else if (jsonKey.equalsIgnoreCase("busyDay")) {
            jsonValue = timeManager.getBusyDayString(jsonValue);
        } else if (jsonKey.equalsIgnoreCase("kpiTime")) {
            jsonValue = timeManager.getKpiTime(jsonValue, tc.getTime());
        } else if (jsonKey.equalsIgnoreCase("fiveMinAggTime")) {
            jsonValue = timeManager.getFiveMinAggTime(jsonValue);
        } else if (jsonKey.equalsIgnoreCase("fiveMinDateTime")) {
            jsonValue = timeManager.getFiveMinDateTime(jsonValue);
        } else if (jsonKey.equalsIgnoreCase("eventVolumeTime")) {
            jsonValue = timeManager.getEventVolumeTime(jsonValue, tc.getTime());
        }
        return jsonValue;
    }

    /**
     * @param tc
     * @param rowIndex
     * @param jsonKey
     * @return
     * @throws JSONException
     * @throws NumberFormatException
     */
    private String updateJSONValueIfKeyIsJSONObject(final TestCase tc, final String rowIndex, final String jsonKey) {
        String jsonValue = "";
        int i = 0;
        try {
            String columnIndex = tc.getJSONVariables().get(jsonKey);
            JSONObject row = tc.getData().getJSONObject(Integer.parseInt(rowIndex));
            final String parentKey = (String) row.keySet().toArray()[0];

            if (row.get(parentKey) instanceof JSONObject) {
                row = row.getJSONObject(parentKey);
                if (columnIndex != null && columnIndex.contains(".")) {
                    final String[] keys = columnIndex.split("\\.");
                    for (i = 0; i < keys.length - 1; i++) {
                        columnIndex = keys[i];
                        row = row.getJSONObject(columnIndex);
                    }
                    jsonValue = String.valueOf((row.get(keys[i])));
                } else {
                    jsonValue = String.valueOf((row.get(columnIndex)));
                }
            } else {
                jsonValue = String.valueOf((row.get(columnIndex)));
            }

        } catch (final JSONException e) {
            e.printStackTrace();
        }
        return jsonValue;
    }

    Map<String, String> executeQueryWithNullHandling(String statement, final Map<String, String> sqlVars, final String rowIndex, final TestCase tc)
            throws ArrestItException {
        statement = removeEqualsNull(statement);
        return dbManager.executeQuery(statement, sqlVars, rowIndex, tc);
    }

    String removeEqualsNull(String statement) {
        statement = statement.replaceAll("= null", " is null");
        statement = statement.replaceAll("=null", " is null");
        return statement;
    }

    //This method should be removed and any requirements sent to swapTableNames in the correct format
    String deprecatedSwapTableName(String sql, final String tableTextVal) {

        List<String> tableNamePrefixes = null;
        sql = sql.toLowerCase();
        final Map<String, String> replacements = new HashMap<String, String>();

        replacements.put("lte", "sgeh");
        replacements.put("sgeh", "lte");
        replacements.put("suc", "err");
        replacements.put("err", "suc");

        if (tableTextVal.equalsIgnoreCase("lte") || tableTextVal.equalsIgnoreCase("sgeh")) {
            tableNamePrefixes = new ArrayList<String>(Arrays.asList("event_e_", " dim_e_"));
        } else if (tableTextVal.equalsIgnoreCase("err") || tableTextVal.equalsIgnoreCase("suc")) {
            tableNamePrefixes = new ArrayList<String>(Arrays.asList("event_e_"));
        } else if (tableTextVal.equalsIgnoreCase("soho") || tableTextVal.equalsIgnoreCase("hsdsch") || tableTextVal.equalsIgnoreCase("irat")
                || tableTextVal.equalsIgnoreCase("ifho")) {
            tableNamePrefixes = new ArrayList<String>(Arrays.asList("event_e_ran_hfa_"));
        }

        for (final String tableNamePrefix : tableNamePrefixes) {
            String newTableName = "";

            if (sql.contains(tableNamePrefix)) {
                int firstIndOfTableName = sql.indexOf(tableNamePrefix);

                while (firstIndOfTableName != -1) {

                    final int lastInd = sql.indexOf(" ", firstIndOfTableName + tableNamePrefix.length());
                    final String oldTableName = sql.substring(firstIndOfTableName, lastInd);

                    if (tableTextVal.equalsIgnoreCase("soho") || tableTextVal.equalsIgnoreCase("hsdsch") || tableTextVal.equalsIgnoreCase("irat")
                            || tableTextVal.equalsIgnoreCase("ifho")) {
                        final String[] oldTableName1 = oldTableName.split("_");
                        final String oldTableName2 = oldTableName1[4];
                        newTableName = oldTableName.replace(oldTableName2, tableTextVal);
                    } else {
                        newTableName = oldTableName.replace(replacements.get(tableTextVal), tableTextVal);
                    }
                    sql = sql.replace(oldTableName, newTableName);
                    firstIndOfTableName = sql.indexOf(tableNamePrefix, firstIndOfTableName + 1);
                }
            }
        }
        return sql;
    }

}
