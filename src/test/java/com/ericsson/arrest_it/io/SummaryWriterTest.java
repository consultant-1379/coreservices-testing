package com.ericsson.arrest_it.io;

import static com.ericsson.arrest_it.common.Constants.*;
import static org.junit.Assert.*;

import java.util.*;

import junit.extensions.PA;

import org.junit.BeforeClass;
import org.junit.Test;

import com.ericsson.arrest_it.common.TestCase;
import com.ericsson.arrest_it.results.*;

public class SummaryWriterTest {
    private static SummaryWriter SUMMARY_WRITER;
    private static final String TIME_ZONE_OFFSET = "+0800";
    private static final int[] TIMES = { 30, 60 };
    private static final boolean SHOULD_VALIDATE_CSV = true;
    private static final String FILENAME = "dummyPath/coreStuff.xml";
    private static final int EXCESS_TESTS_COUNT = 7;

    @BeforeClass
    public static void setup_class() {
        SUMMARY_WRITER = new SummaryWriter(true, TIMES, TIME_ZONE_OFFSET);
    }

    @Test
    public void test_classSetup() throws NoSuchFieldException {

        SUMMARY_WRITER = new SummaryWriter(SHOULD_VALIDATE_CSV, TIMES, TIME_ZONE_OFFSET);
        String actualTimeZone = (String) PA.getValue(SUMMARY_WRITER, "tzOffset");
        String actualTimes = (String) PA.getValue(SUMMARY_WRITER, "times");
        String expectedTimes = "30,60";
        boolean actualShouldValidateCsv = (Boolean) PA.getValue(SUMMARY_WRITER, "csvValidationOn");
        Map<String, Double> actualResultMap = (Map<String, Double>) PA.getValue(SUMMARY_WRITER, "results");
        int expectedResultsSize = 21;

        assertEquals(expectedResultsSize, actualResultMap.size());
        assertEquals(actualShouldValidateCsv, SHOULD_VALIDATE_CSV);
        assertEquals(actualTimeZone, TIME_ZONE_OFFSET);
        assertEquals(actualTimes, expectedTimes);
    }

    @Test
    public void test_addResultsToSummary() {
        final int noOfFrameWorkPasses = 3, noOfFrameWorkFails = 1, noOfTotalPasses = 69, noOfTotalFails = 21, noOfEmptyGrids = 3;
        List<TestResultHolder> testResultHolders = setupTestList(noOfFrameWorkPasses, noOfFrameWorkFails, noOfTotalPasses, noOfTotalFails,
                noOfEmptyGrids);

        SUMMARY_WRITER.addResultsToSummary(testResultHolders, FILENAME);

        Map<String, Double> actualResults = (Map<String, Double>) PA.getValue(SUMMARY_WRITER, "results");

        int actualPasses = (int) (double) actualResults.get(JSON_VALIDATION + PASSED_IDENTIFIER);

        assertEquals(actualPasses, noOfTotalPasses);

        System.out.println(PA.getValue(SUMMARY_WRITER, "results"));
    }

    private List<TestResultHolder> setupTestList(final int noOfFrameWorkPasses, final int noOfFrameWorkFails, final int noOfTotalPasses,
                                                 final int noOfTotalFails, final int noOfEmptyGrids) {

        List<TestResultHolder> testResultHolders = new ArrayList<TestResultHolder>();

        testResultHolders.add(createFrameWorkOnlyResults(noOfFrameWorkPasses, noOfFrameWorkFails, noOfTotalPasses, noOfTotalFails, noOfEmptyGrids));

        testResultHolders.addAll(createSimulatedTestResults(noOfTotalPasses, noOfTotalFails));
        return testResultHolders;
    }

    private List<TestResultHolder> createSimulatedTestResults(final int noOfTotalPasses, final int noOfTotalFails) {
        List<TestResultHolder> resultsToReturn = new ArrayList<TestResultHolder>();
        final int noOfPassesPerTest = noOfTotalPasses / 3;
        final int noOfFailsPerTest = noOfTotalFails / 3;

        for (int i = 1; i < 4; i++) {
            TestResultHolder testResultHolder = new TestResultHolder(setupTestCaseForResults(String.valueOf(i), TIMES[0]));
            testResultHolder = createResultsPerTestCase(noOfPassesPerTest, noOfFailsPerTest, testResultHolder);
            resultsToReturn.add(testResultHolder);
        }

        return resultsToReturn;
    }

    private TestResultHolder createResultsPerTestCase(final int noOfPasses, final int noOfFails, final TestResultHolder testResultHolder) {
        for (int i = 0; i < noOfPasses; i++) {
            testResultHolder.addTestResult(new JsonValidationResult("Test Passed", true));
        }
        for (int i = 0; i < noOfFails; i++) {
            testResultHolder.addTestResult(new JsonValidationResult("Test Failed", false));
        }

        return testResultHolder;
    }

    private TestCase setupTestCaseForResults(final String testId, final int time) {

        TestCase testCase = new TestCase();
        testCase.setTitle(testId + ":" + time);
        testCase.setTime(time);

        return testCase;
    }

    private TestResultHolder createFrameWorkOnlyResults(final int noOfFrameWorkPasses, final int noOfFrameWorkFails, final int noOfTotalPasses,
                                                        final int noOfTotalFails, final int noOfEmptyGrids) {
        List<TestResult> frameWorkResults = new ArrayList<TestResult>();
        int noOfTestsRun = 5;

        for (int i = 0; i < noOfFrameWorkPasses; i++) {
            frameWorkResults.add(new FrameWorkResult("Logged Into Ui", true, FILENAME));
        }

        for (int i = 0; i < noOfFrameWorkFails; i++) {
            frameWorkResults.add(new FrameWorkResult("Could not log into ui", false, FILENAME));
        }

        frameWorkResults.add(new FrameWorkResult(FILENAME, noOfTestsRun));

        frameWorkResults.add(new FrameWorkResult(initializeTestTotals(noOfTotalPasses, noOfTotalFails, noOfEmptyGrids)));

        return new TestResultHolder(FILENAME, frameWorkResults);

    }

    private Map<String, Integer> initializeTestTotals(final int passedTests, final int failedTests, final int noOfEmptyGrids) {
        Map<String, Integer> testTotals = new HashMap<String, Integer>();
        int totalTests = passedTests + failedTests;
        testTotals.put(TOTAL_TESTS, totalTests);
        testTotals.put(PASSED_TESTS, passedTests);
        testTotals.put(FAILED_TESTS, failedTests);
        testTotals.put(EXCESS_TESTS, EXCESS_TESTS_COUNT);
        testTotals.put(DRILLS_ATTEMPTED, totalTests * 2);
        testTotals.put(DRILLS_NOT_COMPLETED, totalTests);
        testTotals.put(NO_OF_EMPTY_GRIDS, noOfEmptyGrids);

        return testTotals;
    }

}
