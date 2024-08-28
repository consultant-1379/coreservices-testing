package com.ericsson.arrest_it.main;

import static com.ericsson.arrest_it.common.Constants.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.ParseException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ericsson.arrest_it.common.*;
import com.ericsson.arrest_it.manager.*;

public class TestCaseExpanderTest {
    private static List<TestCase> templateTests;
    private static TestCase parentTest;

    @BeforeClass
    public static void setupClass() {
        templateTests = new ArrayList<TestCase>();

        for (int i = 1; i < 5; i++) {
            final TestCase testCase = new TestCase();
            testCase.setTitle(String.valueOf(i) + COLON + "30");
            testCase.setUrl("NETWORK/RANKING_ANALYSIS?time=30&display=grid&type=BSC&tzOffset=+0100&maxRows=50");
            templateTests.add(testCase);
        }

        parentTest = templateTests.get(0);
        final JSONArray data = new JSONArray();

        for (int i = 0; i < 40; i++) {
            data.put(new JSONObject().put("1", String.valueOf(i)));
        }

        parentTest.setData(data);
        parentTest.addJsonVariable("failures", "1");

        final List<String> drillsToPerform = new ArrayList<String>();
        drillsToPerform.add("2");
        drillsToPerform.add("3");
        drillsToPerform.add("4");

        parentTest.setRepeatDrillDown(drillsToPerform);
    }

    @Test
    public void test_createChildTestsWithPrecondition() throws ParseException {
        final TestCaseExpander testCaseExpander = setupTestCaseExpander(2);
        setTestPreconditions("1.failures > ");
        final List<Integer> drillIndices = new ArrayList<Integer>();
        drillIndices.add(2);
        drillIndices.add(4);
        List<TestCase> testsToRun = new ArrayList<TestCase>();

        testsToRun = testCaseExpander.attemptToCreateChildTests(templateTests, testsToRun, parentTest, drillIndices);
        final int expectedValue = 6;
        final int actualValue = testsToRun.size();

        assertEquals("TestCaseExpander not creating correct no of child test cases", expectedValue, actualValue);
    }

    @Test
    public void test_createChildTestWithoutPrecondition() throws ParseException {
        final TestCaseExpander testCaseExpander = setupTestCaseExpander(3);
        setTestPreconditions(EMPTY_STRING);

        final List<Integer> drillIndices = new ArrayList<Integer>();
        drillIndices.add(0);
        drillIndices.add(1);
        drillIndices.add(2);

        List<TestCase> testsToRun = new ArrayList<TestCase>();
        testsToRun = testCaseExpander.attemptToCreateChildTests(templateTests, testsToRun, parentTest, drillIndices);

        final int expectedValue = 9;
        final int actualValue = testsToRun.size();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void test_createChildTestWithEqualsPrecondition() throws ParseException {
        final TestCaseExpander testCaseExpander = setupTestCaseExpander(2);
        setTestPreconditions("1.failures = ");

        final List<Integer> drillIndices = new ArrayList<Integer>();
        drillIndices.add(0);
        drillIndices.add(1);

        List<TestCase> testsToRun = new ArrayList<TestCase>();
        testsToRun = testCaseExpander.attemptToCreateChildTests(templateTests, testsToRun, parentTest, drillIndices);

        final int expectedValue = 3;
        final int actualValue = testsToRun.size();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void test_parentValuesBeingSet() throws ParseException {
        final TestCaseExpander testCaseExpander = setupTestCaseExpander(2);
        setTestPreconditions(EMPTY_STRING);

        final List<Integer> drillIndices = new ArrayList<Integer>();
        drillIndices.add(0);
        drillIndices.add(1);

        final ArrayList<String> valuesToPass = new ArrayList<String>();
        valuesToPass.add("9.failures");
        valuesToPass.add("failures");

        parentTest.addParentValue("9.failures", "6");
        parentTest.addPassToDrill("2", valuesToPass);
        parentTest.addPassToDrill("4", valuesToPass);

        List<TestCase> testsToRun = new ArrayList<TestCase>();
        testsToRun = testCaseExpander.attemptToCreateChildTests(templateTests, testsToRun, parentTest, drillIndices);
        final String drillFromFirstRowExpectedVal = "0";
        final String drillFromSecondRowExpectedVal = "1";
        String actualValue = "";

        for (final TestCase testCase : testsToRun) {
            if (testCase.getTitle().startsWith("2") || testCase.getTitle().startsWith("4")) {
                actualValue = testCase.getParentValues().get("1.failures");

                if (testCase.getTitle().endsWith("0")) {
                    assertEquals(actualValue, drillFromFirstRowExpectedVal);
                }
                if (testCase.getTitle().endsWith("1")) {
                    assertEquals(actualValue, drillFromSecondRowExpectedVal);
                }
            }
        }
    }

    @Test
    public void test_parentValuesNotBeingSet() throws ParseException {
        final TestCaseExpander testCaseExpander = setupTestCaseExpander(2);
        setTestPreconditions(EMPTY_STRING);

        final List<Integer> drillIndices = new ArrayList<Integer>();
        drillIndices.add(0);
        drillIndices.add(1);

        final ArrayList<String> valuesToPass = new ArrayList<String>();
        valuesToPass.add("9.failures");
        valuesToPass.add("failures");

        parentTest.addParentValue("9.failures", "6");
        parentTest.addPassToDrill("2", valuesToPass);
        parentTest.addPassToDrill("4", valuesToPass);

        List<TestCase> testsToRun = new ArrayList<TestCase>();
        testsToRun = testCaseExpander.attemptToCreateChildTests(templateTests, testsToRun, parentTest, drillIndices);
        final String expectedValue = null;
        String actualValue = "";

        for (final TestCase testCase : testsToRun) {
            if (testCase.getTitle().startsWith("3")) {
                actualValue = testCase.getParentValues().get("1.failures");
                assertEquals(actualValue, expectedValue);
                actualValue = testCase.getParentValues().get("9.failures");
                assertEquals(actualValue, expectedValue);
            }
        }
    }

    @Test
    public void test_inheritedValuesBeingSet() throws ParseException {
        final TestCaseExpander testCaseExpander = setupTestCaseExpander(2);
        setTestPreconditions(EMPTY_STRING);

        final List<Integer> drillIndices = new ArrayList<Integer>();
        drillIndices.add(0);
        drillIndices.add(1);

        final ArrayList<String> valuesToPass = new ArrayList<String>();
        valuesToPass.add("9.failures");
        valuesToPass.add("failures");

        parentTest.addParentValue("9.failures", "6");
        parentTest.addPassToDrill("2", valuesToPass);
        parentTest.addPassToDrill("4", valuesToPass);

        List<TestCase> testsToRun = new ArrayList<TestCase>();
        testsToRun = testCaseExpander.attemptToCreateChildTests(templateTests, testsToRun, parentTest, drillIndices);
        final String expectedValue = "6";
        String actualValue = "";

        for (final TestCase testCase : testsToRun) {
            if (testCase.getTitle().startsWith("2") || testCase.getTitle().startsWith("4")) {
                actualValue = testCase.getParentValues().get("9.failures");

                assertEquals(expectedValue, actualValue);
            }
        }
    }

    @Test
    public void test_createChildTestWithLessThanPrecondition() throws ParseException {
        final TestCaseExpander testCaseExpander = setupTestCaseExpander(2);
        setTestPreconditions("1.failures < ");
        final List<Integer> drillIndices = new ArrayList<Integer>();
        drillIndices.add(5);
        drillIndices.add(6);
        List<TestCase> testsToRun = new ArrayList<TestCase>();

        testsToRun = testCaseExpander.attemptToCreateChildTests(templateTests, testsToRun, parentTest, drillIndices);

        final int expectedValue = 3;
        final int actualValue = testsToRun.size();

        assertEquals("TestCaseExpander not creating correct no of child test cases", expectedValue, actualValue);
    }

    @Test
    public void test_createChildTestWithLessThanEqualsPrecondition() throws ParseException {

        final TestCaseExpander testCaseExpander = setupTestCaseExpander(2);
        setTestPreconditions("1.failures <= ");
        final List<Integer> drillIndices = new ArrayList<Integer>();
        drillIndices.add(5);
        drillIndices.add(6);
        List<TestCase> testsToRun = new ArrayList<TestCase>();

        testsToRun = testCaseExpander.attemptToCreateChildTests(templateTests, testsToRun, parentTest, drillIndices);

        final int expectedValue = 5;
        final int actualValue = testsToRun.size();

        assertEquals("TestCaseExpander not creating correct no of child test cases", expectedValue, actualValue);
    }

    @Test
    public void test_getRepeatDrillIndicesWithAllInstruction() throws ParseException {

        final TestCaseExpander testCaseExpander = setupTestCaseExpander(3);
        parentTest.setRepeatDrillDownInstruction("all");
        final List<Integer> actualValues = testCaseExpander.getRepeatRowNumbers(parentTest, true);

        final List<Integer> expectedValues = new ArrayList<Integer>();
        expectedValues.add(0);
        expectedValues.add(1);
        expectedValues.add(2);

        assertEquals("TestCaseExpander not creating correct drill indices", expectedValues, actualValues);
    }

    @Test
    public void test_getRepeatDrillIndicesWithRandomInstruction() throws ParseException {
        final TestCaseExpander testCaseExpander = setupTestCaseExpander(4);
        parentTest.setRepeatDrillDownInstruction("random3");
        final List<Integer> drillIndices = testCaseExpander.getRepeatRowNumbers(parentTest, true);

        final int actualValue = drillIndices.size();

        final int expectedValue = 4;

        assertEquals("TestCaseExpander not creating enough random drill indices", expectedValue, actualValue);

    }

    private void setTestPreconditions(final String newPrecondition) {
        if (newPrecondition.equals(EMPTY_STRING)) {
            for (final TestCase testCase : templateTests) {
                testCase.setPrecondition(new Precondition(EMPTY_STRING));
            }
        } else {
            for (int i = 0; i < templateTests.size(); i++) {
                templateTests.get(i).setPrecondition(new Precondition(newPrecondition + (i - 1)));
            }
        }

    }

    private TestCaseExpander setupTestCaseExpander(final int maxNoOfValidations) throws ParseException {
        final DBManager dbManager = new DBManager(TestConstants.TEST_SERVER_ADDRESS, "dc", "dc", "2640");
        final TimeManager timeManager = new TimeManager();
        final DBValuesManager dbValuesManager = new DBValuesManager(dbManager, EMPTY_STRING, timeManager);
        final QueryManager queryManager = new QueryManager(dbManager, dbValuesManager, timeManager);
        final TestDriver testDriver = new TestDriver(new File(EMPTY_STRING));

        final TestCaseExpander testCaseExpander = new TestCaseExpander(queryManager, testDriver);
        testCaseExpander.setMaxNoOfValidationsPerUrl(maxNoOfValidations);
        return testCaseExpander;
    }
}
