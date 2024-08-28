/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.arrest_it.manager;

import static com.ericsson.arrest_it.common.Constants.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import junit.extensions.PA;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.arrest_it.common.*;

@RunWith(MockitoJUnitRunner.class)
public class PreconditionManagerTest {

    private static PreconditionManager preconditionManager;
    private static DBManager dbManager;

    private static final String TEST_PRECONDITION = "3 = 1.imsi";
    private static final String TEST_PRECONDITION_WITH_SQL = "sqlErrors > 0";
    private static final String TEST_SQL = "select count(*) as 'sqlErrors' from event_e_lte_err_raw where imsi = {1.imsi}";
    private static final String TEST_SQL_ENRICHED = "select count(*) as 'sqlErrors' from event_e_lte_err_raw where imsi = 1";
    private static final TestCase testCase = new TestCase();

    @BeforeClass
    public static void initializeTestClass() throws ArrestItException {
        createTestCase();
        mockDBManager();
        createPreconditionManager();
    }

    @Test
    public void test_getUpdatedSqlPrecondition() {
        final Precondition precondition = new Precondition(TEST_SQL, TEST_PRECONDITION_WITH_SQL);

        final Precondition actual = (Precondition) PA.invokeMethod(preconditionManager,
                "getUpdatedSqlPrecondition(com.ericsson.arrest_it.common.TestCase, com.ericsson.arrest_it.common.Precondition, int)", testCase,
                precondition, 1);
        assertEquals("4 > 0", actual.getCondition());
    }

    @Test
    public void test_getUpdatedNonSqlPrecondition() {
        final Precondition precondition = new Precondition(TEST_PRECONDITION);

        final Precondition actual = (Precondition) PA.invokeMethod(preconditionManager,
                "getUpdatedNonSqlPrecondition(com.ericsson.arrest_it.common.TestCase, com.ericsson.arrest_it.common.Precondition, int)", testCase,
                precondition, 1);
        assertEquals("3 = 1", actual.getCondition());
    }

    @Test
    public void test_isEnrichedPreconditionPassed_true() {
        final Precondition precondition = new Precondition("1 = 1");

        final boolean actual = (Boolean) PA.invokeMethod(preconditionManager,
                "isEnrichedPreconditionPassed(com.ericsson.arrest_it.common.TestCase, com.ericsson.arrest_it.common.Precondition)", testCase,
                precondition);
        assertTrue(actual);
    }

    @Test
    public void test_isEnrichedPreconditionPassed_false() {
        final Precondition precondition = new Precondition("1 > 2");

        final boolean actual = (Boolean) PA.invokeMethod(preconditionManager,
                "isEnrichedPreconditionPassed(com.ericsson.arrest_it.common.TestCase, com.ericsson.arrest_it.common.Precondition)", testCase,
                precondition);
        assertFalse(actual);
    }

    @Test
    public void test_isEnrichedPreconditionPassed_emptyPrecondition() {
        final Precondition precondition = new Precondition("");

        final boolean actual = (Boolean) PA.invokeMethod(preconditionManager,
                "isEnrichedPreconditionPassed(com.ericsson.arrest_it.common.TestCase, com.ericsson.arrest_it.common.Precondition)", testCase,
                precondition);
        assertTrue(actual);
    }

    @Test
    public void test_isPreconditionPassed_withSql_true() {
        final Precondition precondition = new Precondition(TEST_SQL, TEST_PRECONDITION_WITH_SQL);

        final boolean actual = (Boolean) PA.invokeMethod(preconditionManager,
                "isPreconditionPassed(com.ericsson.arrest_it.common.TestCase, com.ericsson.arrest_it.common.Precondition, int)", testCase,
                precondition, 1);
        assertTrue(actual);
    }

    @Test
    public void test_isPreconditionPassed_false() {
        final Precondition precondition = new Precondition(TEST_PRECONDITION);

        final boolean actual = (Boolean) PA.invokeMethod(preconditionManager,
                "isPreconditionPassed(com.ericsson.arrest_it.common.TestCase, com.ericsson.arrest_it.common.Precondition, int)", testCase,
                precondition, 1);
        assertFalse(actual);
    }

    private static void createTestCase() {
        testCase.setTitle(1 + COLON + "30");
        testCase.setUrl("NETWORK/RANKING_ANALYSIS?time=30&display=grid&type=BSC&tzOffset=+0100&maxRows=50");
        final JSONArray data = new JSONArray();

        for (int i = 0; i < 40; i++) {
            data.put(new JSONObject().put("1", String.valueOf(i)));
        }

        testCase.setData(data);
        testCase.addJsonVariable("imsi", "1");
    }

    private static void mockDBManager() throws ArrestItException {
        final Map<String, String> sqlVars = new HashMap<String, String>();
        sqlVars.put("sqlErrors", "4");

        dbManager = Mockito.mock(DBManager.class);
        Mockito.when(dbManager.executeFixedQuery(TEST_SQL_ENRICHED)).thenReturn("4");
        Mockito.when(dbManager.executeQuery(TEST_SQL_ENRICHED, QueryManager.getSqlVariables(TEST_SQL_ENRICHED), "1", testCase)).thenReturn(sqlVars);
    }

    private static void createPreconditionManager() throws ArrestItException {
        final ValidationManager validationManager = (ValidationManager) ManagerFactory.getInstance().getManager(Constants.VALIDATION_MANAGER);
        final TimeManager timeManager = new TimeManager();
        final DBValuesManager dbValuesManager = new DBValuesManager(dbManager, "", timeManager);
        final QueryManager queryManager = new QueryManager(dbManager, dbValuesManager, timeManager);
        preconditionManager = new PreconditionManager(queryManager, validationManager, timeManager);
    }

}