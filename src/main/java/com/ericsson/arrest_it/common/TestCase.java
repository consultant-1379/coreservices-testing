package com.ericsson.arrest_it.common;

import java.util.*;

import org.json.JSONArray;

import com.ericsson.arrest_it.results.TestResultHolder;

/**
 * This TestCase object contains the information required for each testcase to run. The Class also contains the cloning method which allows testcases
 * to be used as templates. Each testcase maps to a <TestCase></TestCase> in the test xml
 */
public class TestCase {
    private String direction, title, url, csvUrl, directionWithOutRowInfo;
    private Map<String, String> jsonVariables, sqlVariables, parentValues;
    private List<DBQuery> dbQueries;
    private List<String> repeatDrillDown;
    private List<Validation> validations;
    private List<Integer> ignoredInCsv;
    private String repeatDrillDownInstruction, repeatValidationInstruction, testType;
    private Precondition precondition;
    private TestResultHolder testResultHolder;

    private Map<String, List<String>> passToDrill;
    private JSONArray data, csvData;
    private int time, maxVals, maxRowCount, sizeOfCsvFile;

    public TestCase() {
        this.jsonVariables = new HashMap<String, String>();
        this.sqlVariables = new HashMap<String, String>();
        this.dbQueries = new ArrayList<DBQuery>();
        this.validations = new ArrayList<Validation>();
        this.repeatDrillDown = new ArrayList<String>();
        this.passToDrill = new HashMap<String, List<String>>();
        this.parentValues = new HashMap<String, String>();
        this.ignoredInCsv = new ArrayList<Integer>();
        this.maxVals = 0;
        this.maxRowCount = 0;
        this.sizeOfCsvFile = 0;
    }

    /**
     * @param another
     *        The source test case from which the clone will be created
     */
    public TestCase(final TestCase another) {
        this.direction = another.getDirection();
        this.directionWithOutRowInfo = another.getDirectionWithOutRowInfo();
        this.title = another.getTitle();
        this.repeatDrillDownInstruction = another.getRepeatDrillDownInstruction();
        this.repeatValidationInstruction = another.getRepeatValidationInstruction();
        this.precondition = another.getPrecondition();
        this.url = another.getUrl();
        this.csvUrl = another.getCsvUrl();
        this.time = another.getTime();
        this.maxVals = another.getMaxVals();
        this.maxRowCount = another.getMaxRowCount();
        this.sizeOfCsvFile = another.getSizeOfCsvFile();
        this.testType = another.getTestType();

        final List<Validation> newValidations = new ArrayList<Validation>();
        for (final Validation oldValidation : another.getValidations()) {
            final Validation newValidation = new Validation(oldValidation);
            newValidations.add(newValidation);
        }

        this.setValidations(newValidations);

        final Map<String, String> newParentValues = new HashMap<String, String>();
        for (final String oldKey : another.getParentValues().keySet()) {
            newParentValues.put(oldKey, another.getParentValues().get(oldKey));
        }
        this.parentValues = newParentValues;

        final List<String> newDrills = new ArrayList<String>();
        for (final String oldDrill : another.getRepeatDrillDown()) {
            newDrills.add(oldDrill);
        }
        this.repeatDrillDown = newDrills;

        final Map<String, String> newJsonVariables = new HashMap<String, String>();
        for (final String oldKey : another.getJSONVariables().keySet()) {
            newJsonVariables.put(oldKey, another.getJSONVariables().get(oldKey));
        }
        this.jsonVariables = newJsonVariables;

        final List<DBQuery> newDbQueries = new ArrayList<DBQuery>();
        for (final DBQuery oldDbQuery : another.getDbQueries()) {
            newDbQueries.add(new DBQuery(oldDbQuery));
        }
        this.dbQueries = newDbQueries;

        this.sqlVariables = new HashMap<String, String>();

        final Map<String, List<String>> newPassToDrill = new HashMap<String, List<String>>();
        for (final String key : another.getPassToDrill().keySet()) {
            final List<String> newList = new ArrayList<String>();
            for (final String drillValue : another.getPassToDrill().get(key)) {
                newList.add(drillValue);
            }
            newPassToDrill.put(key, newList);
        }

        this.passToDrill = newPassToDrill;

        final List<Integer> newIgnoredInCsv = new ArrayList<Integer>();
        for (final int oldValue : another.getIgnoredInCsv()) {
            newIgnoredInCsv.add(oldValue);
        }
        this.ignoredInCsv = newIgnoredInCsv;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getTestType() {
        return testType;
    }

    public void setTestType(final String testType) {
        this.testType = testType;
    }

    public Map<String, String> getJSONVariables() {
        return jsonVariables;
    }

    public void setJSONVariables(final Map<String, String> jsonVariables) {
        this.jsonVariables = jsonVariables;
    }

    public List<DBQuery> getDbQueries() {
        return dbQueries;
    }

    public void setDbQueries(final List<DBQuery> dbQueries) {
        this.dbQueries = dbQueries;
    }

    public Map<String, String> getSqlVariables() {
        return sqlVariables;
    }

    public void setSqlVariables(final Map<String, String> sqlVariables) {
        this.sqlVariables = sqlVariables;
    }

    public List<Validation> getValidations() {
        return validations;
    }

    public void setValidations(final List<Validation> validations) {
        this.validations = validations;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(final String direction) {
        this.direction = direction;
    }

    public Precondition getPrecondition() {
        return precondition;
    }

    public void setPrecondition(final Precondition precondition) {
        this.precondition = precondition;
    }

    public String getRepeatValidationInstruction() {
        return repeatValidationInstruction;
    }

    public void setRepeatValidationInstruction(final String repeatValidationInstruction) {
        this.repeatValidationInstruction = repeatValidationInstruction;
    }

    public List<String> getRepeatDrillDown() {
        return repeatDrillDown;
    }

    public void setRepeatDrillDown(final List<String> repeatDrillDown) {
        this.repeatDrillDown = repeatDrillDown;
    }

    public void addRepeatDrillDown(final String drillDown) {
        this.repeatDrillDown.add(drillDown);
    }

    public void addDbQuery(final DBQuery dbQuery) {
        this.dbQueries.add(dbQuery);
    }

    public void addValidation(final Validation validation) {
        this.validations.add(validation);
    }

    public void addJsonVariable(final String key, final String value) {
        this.jsonVariables.put(key, value);
    }

    public void addSQLVariable(final String key, final String value) {
        this.sqlVariables.put(key, value);
    }

    public String getRepeatDrillDownInstruction() {
        return repeatDrillDownInstruction;
    }

    public void setRepeatDrillDownInstruction(final String repeatDrillDownInstruction) {
        this.repeatDrillDownInstruction = repeatDrillDownInstruction;
    }

    public Map<String, List<String>> getPassToDrill() {
        return passToDrill;
    }

    public void setPassToDrill(final Map<String, List<String>> passToDrill) {
        this.passToDrill = passToDrill;
    }

    public void addPassToDrill(final String key, final List<String> values) {
        this.passToDrill.put(key, values);
    }

    public Map<String, String> getParentValues() {
        return parentValues;
    }

    public void setParentValues(final Map<String, String> parentValues) {
        this.parentValues = parentValues;
    }

    public void addParentValue(final String key, final String value) {
        this.parentValues.put(key, value);
    }

    public JSONArray getData() {
        return data;
    }

    public void setData(final JSONArray data) {
        this.data = data;
    }

    public int getTime() {
        return time;
    }

    public void setTime(final int time) {
        this.time = time;
    }

    public int getMaxVals() {
        return maxVals;
    }

    public void setMaxVals(final int maxVals) {
        this.maxVals = maxVals;
    }

    public int getMaxRowCount() {
        return maxRowCount;
    }

    public void setMaxRowCount(final int maxRowCount) {
        this.maxRowCount = maxRowCount;
    }

    public String getCsvUrl() {
        return csvUrl;
    }

    public void setCsvUrl(final String csvUrl) {
        this.csvUrl = csvUrl;
    }

    public JSONArray getCsvData() {
        return csvData;
    }

    public void setCsvData(final JSONArray csvData) {
        this.csvData = csvData;
    }

    public int getSizeOfCsvFile() {
        return sizeOfCsvFile;
    }

    public void setSizeOfCsvFile(final int sizeOfCsvFile) {
        this.sizeOfCsvFile = sizeOfCsvFile;
    }

    public List<Integer> getIgnoredInCsv() {
        return ignoredInCsv;
    }

    public void setIgnoredInCsv(final List<Integer> ignoredInCsv) {
        this.ignoredInCsv = ignoredInCsv;
    }

    public TestResultHolder getTestResultHolder() {
        return testResultHolder;
    }

    public void setTestResultHolder(final TestResultHolder testResultHolder) {
        this.testResultHolder = testResultHolder;
    }

    public String getDirectionWithOutRowInfo() {
        return directionWithOutRowInfo;
    }

    public void setDirectionWithOutRowInfo(final String directionWithOutRowInfo) {
        this.directionWithOutRowInfo = directionWithOutRowInfo;
    }

    public String getId() {
        if (title.contains(Constants.COLON)) {
            return title.substring(0, title.indexOf(Constants.COLON));
        }

        return title;
    }
}
