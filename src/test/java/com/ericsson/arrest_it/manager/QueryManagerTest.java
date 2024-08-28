package com.ericsson.arrest_it.manager;

import static com.ericsson.arrest_it.common.Constants.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import junit.extensions.PA;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.arrest_it.common.ArrestItException;
import com.ericsson.arrest_it.common.TestCase;
import com.ericsson.arrest_it.testUtils.TestUtils;


@RunWith(MockitoJUnitRunner.class)
public class QueryManagerTest {

    private static DBManager dbManager;
    private static DBValuesManager dbValuesManager;
    private static QueryManager queryManager;
    private static TimeManager timeManager;
    private static TestCase testCase;

    private static final String TEST_END_TIME = "2015-04-07T19:30";
    private static final String TEST_POSITIVE_TZ = "+0100";
    private static final String PARENT_IMSI = "12345";
    private static final String MASTER_EVENT_ID = "6";
    private static final int TEST_TIME = 30;
    private static final String MASTER_EVENT_ID_PLACEHOLDER = "-master-eventId";
    private static final String JSON_IMSI_PLACEHOLDER = "imsi";
    private static final String JSON_IMSI_ROW_FOUR = "4";
    private static final String PARENT_IMSI_PLACEHOLDER = "1.imsi";
    private static final String TEST_URL = "NETWORK/RANKING_ANALYSIS?time=30&display=grid&type=BSC&tzOffset=+0100&maxRows=50&imsi={"
            + PARENT_IMSI_PLACEHOLDER + "}&eventId={" + MASTER_EVENT_ID_PLACEHOLDER + "}";
    private static final String TEST_URL_ENRICHED = "NETWORK/RANKING_ANALYSIS?time=30&display=grid&type=BSC&tzOffset=+0100&maxRows=50&imsi="
            + PARENT_IMSI + "&eventId=" + MASTER_EVENT_ID;

    private static final String TEST_CSV_URL = "EniqEventsCSV.jsp?imsi={"
            + PARENT_IMSI_PLACEHOLDER
            + "}&eventId={"
            + MASTER_EVENT_ID_PLACEHOLDER
            + "}&type=MANUFACTURER&display=grid&key=SUM&tzOffset=+0100&dataTimeFrom=1414972800000&dataTimeTo=1415577600000&userName=admin&url=TERMINAL/EVENT_ANALYSIS&maxRows=0";

    private static final String TEST_CSV_URL_ENRICHED = "EniqEventsCSV.jsp?imsi="
            + PARENT_IMSI
            + "&eventId="
            + MASTER_EVENT_ID
            + "&type=MANUFACTURER&display=grid&key=SUM&tzOffset=+0100&dataTimeFrom=1414972800000&dataTimeTo=1415577600000&userName=admin&url=TERMINAL/EVENT_ANALYSIS&maxRows=0";

    private static final String SQL_VARIABLE_FAILURES = "sqlFailures";
    private static final String SQL_VARIABLE_IMSI = "sqlImsi";
    private static final String TEST_STATEMENT = "select count(*) as '" + SQL_VARIABLE_FAILURES + "', imsi AS '" + SQL_VARIABLE_IMSI
            + "' from event_e_lte_err_raw";

    private static final String SQL_STMT_WITH_MASTER = "select count(*) from event_e_lte_imsi_suc_raw where event_id = {"
            + MASTER_EVENT_ID_PLACEHOLDER + "}";

    private static final String SQL_STMT_WITH_MASTER_ENRICHED = "select count(*) from event_e_lte_imsi_suc_raw where event_id = " + MASTER_EVENT_ID;

    private static final String SQL_STMT_WITH_PARENT = "select count(*) from event_e_lte_imsi_suc_raw where imsi = {" + PARENT_IMSI_PLACEHOLDER + "}";
    private static final String SQL_STMT_WITH_PARENT_ENRICHED = "select count(*) from event_e_lte_imsi_suc_raw where imsi = " + PARENT_IMSI;

    private static final String SQL_STMT_WITH_JSON = "select count(*) from event_e_lte_err_raw where imsi = {" + JSON_IMSI_PLACEHOLDER + "}";
    private static final String SQL_STMT_WITH_JSON_ENRICHED = "select count(*) from event_e_lte_err_raw where imsi = " + JSON_IMSI_ROW_FOUR;

    @BeforeClass
    public static void setupClass() throws ArrestItException {
        testCase = new TestCase();
        mockDbManager();
        mockDbValuesManager();
        setUpQueryManager();
    }

    @Test
    public void test_urlEnrichment() throws ArrestItException {
        setupTestCase();
        queryManager.urlEnrichment(testCase);
        assertEquals("TestCase url not enriched correctly", TEST_URL_ENRICHED, testCase.getUrl());
    }

    @Test
    public void test_csvUrlEnrichment() throws ArrestItException {
        setupTestCase();
        queryManager.urlEnrichment(testCase);
        assertEquals("TestCase csv url not enriched correctly", TEST_CSV_URL_ENRICHED, testCase.getCsvUrl());
    }

    @Test
    public void test_getSqlVariables() {
        Map<String, String> actualVariables = QueryManager.getSqlVariables(TEST_STATEMENT);

        Map<String, String> expectedVariables = new HashMap<String, String>();
        expectedVariables.put(SQL_VARIABLE_FAILURES, null);
        expectedVariables.put(SQL_VARIABLE_IMSI, null);

        assertEquals("getSqlVariables method not returning correct variables", expectedVariables, actualVariables);
    }

    @Test
    public void test_addSqlVariables() {
        Map<String, String> resultsA = new HashMap<String, String>();
        Map<String, String> resultsB = new HashMap<String, String>();

        resultsA.put(SQL_VARIABLE_FAILURES, "10");
        resultsB.put(SQL_VARIABLE_FAILURES, "5");

        Map<String, String> expectedResult = new HashMap<String, String>();
        expectedResult.put(SQL_VARIABLE_FAILURES, "15.0");

        Map<String, String> actualResults = queryManager.addDbResults(resultsA, resultsB);

        assertEquals(expectedResult, actualResults);
    }

    @Test
    public void test_enrichSqlWithMasterValues() throws ArrestItException {
        setupTestCase();
        String actualValue = queryManager.enrichSqlWithMasterValues(SQL_STMT_WITH_MASTER, testCase);

        assertEquals("Sql statements not enriched with master values as expected", SQL_STMT_WITH_MASTER_ENRICHED, actualValue);
    }

    @Test
    public void test_enrichSqlWithParentValues() throws ArrestItException {
        setupTestCase();
        String actualValue = queryManager.enrichSqlWithParentValues(SQL_STMT_WITH_PARENT, testCase);

        assertEquals("Sql statements not enriched with parent values as expected", SQL_STMT_WITH_PARENT_ENRICHED, actualValue);
    }

    @Test
    public void test_enrichSqlWithJsonValues() throws ArrestItException {
        setupTestCase();
        int testRowNumberFour = 4;
        String actualValue = queryManager.enrichSqlWithJsonValues(SQL_STMT_WITH_JSON, testRowNumberFour, testCase);

        assertEquals("Sql statements not enriched with Json values as expected", SQL_STMT_WITH_JSON_ENRICHED, actualValue);
    }
       @Test
    public void test_insertDirectionToSql() throws ArrestItException{
    	String testSql = "select count(*) from event_e_lte_err_raw where imsi = 12345";
    	testSql = queryManager.insertDirectionToSql(testSql, "0", "ratId");
        assertTrue(StringUtils.containsIgnoreCase(testSql,"event_e_sgeh_err_raw"));
    }
    
    @Test
    public void test_insertDirectionToSqlWCDMA() throws ArrestItException{
    	String testSql = "select IMSI_MCC as 'sqlMcc',IMSI_MNC  as 'sqlMnc' from EVENT_E_RAN_HFA_HSDSCH_ERR_RAW tmpTable where HIER3_CELL_ID = (select HIER3_CELL_ID from  DIM_E_SGEH_HIER321_CELL where cell_id='234F' and HIERARCHY_3='rnc:4325:Ericsson') and imsi=1234  and DATETIME_ID >= '2015-03-31 05:00' and DATETIME_ID < '2015-03-31 06:00' and ISNULL(tmpTable.TAC,-1) not in (select TAC from GROUP_TYPE_E_TAC where GROUP_NAME = 'EXCLUSIVE_TAC') and event_time = '2015-23-14 10:24:54.234'";
    	testSql = queryManager.insertDirectionToSql(testSql, "2", "categoryId");
    	assertTrue(StringUtils.containsIgnoreCase(testSql,"event_e_ran_hfa_ifho_err_raw"));
    }
    
    @Test
    public void test_insertDirectionToSqlMultipleTables(){
    	String testSql = "SELECT top 5 hierarchy_3 AS 'hierarchy_3', rat AS 'rat', vendor AS 'vendor', hier3_id AS 'hier3_id' FROM ( SELECT rawV.hierarchy_3 AS 'hierarchy_3', rawV.rat AS 'rat', rawV.vendor AS 'vendor', dimV.hier3_id AS 'hier3_id', qci_err_1 + qci_err_2 + qci_err_3 + qci_err_4 + qci_err_5 + qci_err_6 + qci_err_7 + qci_err_8 + qci_err_9 + qci_err_10 AS 'qcisum' FROM event_e_lte_err_raw rawV, dim_e_lte_hier321 dimV WHERE rawV.hierarchy_3 = dimV.hierarchy_3 AND rawV.hierarchy_3 IS NOT NULL and {datetime_id} ) as sub group by hierarchy_3, rat, vendor, hier3_id ORDER BY SUM(qcisum) DESC";
    	testSql = queryManager.insertDirectionToSql(testSql, "1", "ratId");
        assertTrue(StringUtils.containsIgnoreCase(testSql,"event_e_sgeh_err_raw rawv, dim_e_sgeh_hier321"));
    }
    
    private static void setupTestCase() {

        testCase.setTitle(2 + COLON + "30");
        testCase.setUrl(TEST_URL);
        testCase.setCsvUrl(TEST_CSV_URL);
        final JSONArray data = new JSONArray();

        for (int i = 0; i < 40; i++) {
            data.put(new JSONObject().put("1", String.valueOf(i)));
        }

        testCase.setTime(TEST_TIME);

        testCase.setData(data);
        testCase.addJsonVariable("imsi", "1");

        Map<String, String> parentValues = new HashMap<String, String>();
        parentValues.put("1.imsi", PARENT_IMSI);
        testCase.setParentValues(parentValues);
    }

    private static void mockDbManager() {
        dbManager = Mockito.mock(DBManager.class);
    }

    private static void mockDbValuesManager() throws ArrestItException {
        dbValuesManager = Mockito.mock(DBValuesManager.class);
        Mockito.when(dbValuesManager.getSingleDbValue(testCase, MASTER_EVENT_ID_PLACEHOLDER)).thenReturn(MASTER_EVENT_ID);
    }

    private static void setUpQueryManager() throws ArrestItException {
        timeManager = TestUtils.initializeTestTimeManager(TEST_POSITIVE_TZ, TEST_END_TIME);
        queryManager = new QueryManager(dbManager, dbValuesManager, timeManager);
    }

}
