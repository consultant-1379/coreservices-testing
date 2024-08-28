package com.ericsson.arrest_it.manager;

import static com.ericsson.arrest_it.common.Constants.*;
import static org.junit.Assert.*;

import org.json.JSONArray;
import org.junit.*;

import com.ericsson.arrest_it.common.TestCase;
import com.ericsson.arrest_it.results.TestResult;
import com.ericsson.arrest_it.results.TestResultHolder;

public class JsonValidationManagerTest {

    private static TestCase TESTCASE;
    private static JsonValidationManager JSON_VALIDATION_MANAGER;

    private static final String TEST_CSV_URL = "EniqEventsCSV.jsp?type=CELL&groupname={-master-accessareagroup}&display=grid&tzOffset=+0000&dataTimeFrom=1415272440000&dataTimeTo=1415274240000&userName=admin&url=NETWORK/CAUSE_CODE_ANALYSIS&maxRows=0";
    private static final String TEST_DATA = "[{\"1\":\"1\",\"2\":\"694688583726723\",\"3\":\"2972\",\"4\":\"3\"},{\"1\":\"2\",\"2\":\"454065815023387\",\"3\":\"701\",\"4\":\"701\"},{\"1\":\"3\",\"2\":\"454065300891185\",\"3\":\"649\",\"4\":\"0\"}]";
    private static final String TEST_DATA_NOT_MATCHING = "[{\"1\":\"1\",\"2\":\"694688583726723\",\"3\":\"2972\",\"4\":\"3\"},{\"1\":\"2\",\"2\":\"454065815023387\",\"3\":\"701\",\"4\":\"701\"},{\"1\":\"3\",\"2\":\"454065300891185\",\"3\":\"649\",\"4\":\"2\"}]";
    private static final String TEST_DATA_WITH_TIME = "[{\"1\":\"2015-03-31 11:14:38.266\",\"2\":\"3525346058\",\"3\":\"35375804\",\"4\":\"Nokia\"},{\"1\":\"2015-03-31 11:14:02.103\",\"2\":\"3525346058\",\"3\":\"35375805\",\"4\":\"Nokia\"}]";
    private static final String TEST_DATA_WITH_TIME_NOT_MATCHING = "[{\"1\":\"2015-03-31 11:14:38.266\",\"2\":\"3525346058\",\"3\":\"35375804\",\"4\":\"Nokia\"},{\"1\":\"2015-03-31 11:14:02.103\",\"2\":\"3525346058\",\"3\":\"35375805\",\"4\":\"Samsung\"}]";

    @BeforeClass
    public static void setupClass() {
        TESTCASE = new TestCase();
        JSON_VALIDATION_MANAGER = new JsonValidationManager();
    }

    @Before
    public void setupTest() {
        TESTCASE.setTestResultHolder(new TestResultHolder(TESTCASE));
    }

    @Test
    public void test_emptyGridData() {
        setupUiData(EMPTY_STRING);
        TESTCASE.setCsvUrl(null);
        JSON_VALIDATION_MANAGER.setValidateCsv(false);

        JSON_VALIDATION_MANAGER.compareCsvAndGrid(TESTCASE);
        TestResultHolder testResults = TESTCASE.getTestResultHolder();
        TestResult expectedFailedTest = testResults.getJsonValidationResults().get(0);

        assertFalse(expectedFailedTest.isPassed());
    }

    @Test
    public void test_emptyGridDataWithCSVCheckOn() {

        setupUiData(EMPTY_STRING);
        setupCsvData(TEST_DATA);
        TESTCASE.setCsvUrl(TEST_CSV_URL);
        JSON_VALIDATION_MANAGER.setValidateCsv(true);

        JSON_VALIDATION_MANAGER.compareCsvAndGrid(TESTCASE);

        TestResultHolder testResults = TESTCASE.getTestResultHolder();
        TestResult expectedFailedTest = testResults.getJsonValidationResults().get(0);

        assertFalse(expectedFailedTest.isPassed());
    }

    @Test
    public void test_gridDataWithErrorMessage() {
        setupUiData("[{errorDescription: \"Could not parse Json\"}]");
        setupCsvData(TEST_DATA);

        JSON_VALIDATION_MANAGER.setValidateCsv(true);
        TESTCASE.setCsvUrl(TEST_CSV_URL);

        JSON_VALIDATION_MANAGER.compareCsvAndGrid(TESTCASE);

        TestResultHolder testResults = TESTCASE.getTestResultHolder();

        TestResult expectedFailedTest = testResults.getJsonValidationResults().get(0);

        assertFalse(expectedFailedTest.isPassed());
    }

    @Test
    public void test_csvDataWithErrorMessage() {
        setupCsvData("[{errorDescription: \"Socket exception while reading csv stream\"}]");
        setupUiData(TEST_DATA);

        JSON_VALIDATION_MANAGER.setValidateCsv(true);
        TESTCASE.setCsvUrl(TEST_CSV_URL);

        JSON_VALIDATION_MANAGER.compareCsvAndGrid(TESTCASE);

        TestResultHolder testResults = TESTCASE.getTestResultHolder();

        TestResult expectedFailedTest = testResults.getCsvValidationResults().get(0);

        assertFalse(expectedFailedTest.isPassed());
    }

    @Test
    public void test_csvDataMatchingUI() {
        setupCsvData(TEST_DATA);
        setupUiData(TEST_DATA);

        JSON_VALIDATION_MANAGER.setValidateCsv(true);
        TESTCASE.setCsvUrl(TEST_CSV_URL);

        JSON_VALIDATION_MANAGER.compareCsvAndGrid(TESTCASE);

        TestResultHolder testResults = TESTCASE.getTestResultHolder();

        assertTrue(testResults.haveAllTestsPassed());
    }

    @Test
    public void test_csvDataNotMatchingUi() {
        setupCsvData(TEST_DATA);
        setupUiData(TEST_DATA_NOT_MATCHING);

        JSON_VALIDATION_MANAGER.setValidateCsv(true);
        TESTCASE.setCsvUrl(TEST_CSV_URL);

        JSON_VALIDATION_MANAGER.compareCsvAndGrid(TESTCASE);

        TestResultHolder testResults = TESTCASE.getTestResultHolder();

        assertFalse(testResults.haveAllTestsPassed());
    }

    @Test
    public void test_csvDataMatchingUiWithTime() {
        setupCsvData(TEST_DATA_WITH_TIME);
        setupUiData(TEST_DATA_WITH_TIME);

        JSON_VALIDATION_MANAGER.setValidateCsv(true);
        TESTCASE.setCsvUrl(TEST_CSV_URL);

        JSON_VALIDATION_MANAGER.compareCsvAndGrid(TESTCASE);

        TestResultHolder testResults = TESTCASE.getTestResultHolder();

        assertTrue(testResults.haveAllTestsPassed());
    }

    @Test
    public void test_csvDataNotMatchingUiWithTime() {
        setupCsvData(TEST_DATA_WITH_TIME);
        setupUiData(TEST_DATA_WITH_TIME_NOT_MATCHING);

        JSON_VALIDATION_MANAGER.setValidateCsv(true);
        TESTCASE.setCsvUrl(TEST_CSV_URL);

        JSON_VALIDATION_MANAGER.compareCsvAndGrid(TESTCASE);

        TestResultHolder testResults = TESTCASE.getTestResultHolder();

        assertFalse(testResults.haveAllTestsPassed());
    }

    private void setupUiData(final String jsonArrayString) {
        if (jsonArrayString.length() == 0) {
            TESTCASE.setData(new JSONArray());
        } else {
            TESTCASE.setData(new JSONArray(jsonArrayString));
        }
    }

    private void setupCsvData(final String csvArrayString) {
        if (csvArrayString.length() == 0) {
            TESTCASE.setCsvData(new JSONArray());
        } else {
            TESTCASE.setCsvData(new JSONArray(csvArrayString));
        }
    }
}
