package com.ericsson.arrest_it.main;

import static com.ericsson.arrest_it.common.Constants.*;

import java.io.File;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.arrest_it.common.*;
import com.ericsson.arrest_it.io.*;
import com.ericsson.arrest_it.logging.LogbackFileUtils;
import com.ericsson.arrest_it.manager.*;
import com.ericsson.arrest_it.results.*;

public class TestDriver {

    private static final Logger slf4jLogger = LoggerFactory.getLogger(LogbackFileUtils.ARREST_IT_LOGGER);
    public static Map<String, String> MASTERVALUES;

    public static boolean SHOULDVALIDATECSV = PropertyReader.getInstance().isValidateCsv();
    private final String uiServerAddress = PropertyReader.getInstance().getUiServerURL();
    private final String uiUsername = PropertyReader.getInstance().getUIUserName();
    private final String uiPassword = PropertyReader.getInstance().getUIPassword();
    private final int[] times = PropertyReader.getInstance().getTimes();
    private final boolean sucRaw = PropertyReader.getInstance().isSucRaw();
    private final int excessTestCaseLimit = PropertyReader.getInstance().getUniqueTests();

    private ResultSerializer resultSerializer;
    private DBManager dbManager;
    private HttpEngine httpEngine;
    private TestCaseExpander testCaseExpander;
    private List<Integer> repeatDrillRowNumbers;
    private QueryManager queryManager;
    private ValidationManager validationManager;
    private ResultTextWriter resultTextWriter;
    private TimeManager timeManager;
    private JsonValidationManager jsonValidatonManager;

    private final List<TestCase> busyHourTests = new ArrayList<TestCase>();
    private final List<TestCase> busyDayTests = new ArrayList<TestCase>();
    private final String filePath;
    private Map<String, Boolean> metTests;
    private int countUniqueTests = 0;
    private Map<String, Integer> testTotals;

    public TestDriver(final File file) {
        filePath = file.getAbsolutePath();
        initializeTestCounter();
    }

    public void run() throws ArrestItException {
        LogbackFileUtils.start(filePath);
        final List<TestResult> resultsToWrite = new ArrayList<TestResult>();

        slf4jLogger.info("Success Raw is set to: " + sucRaw);
        slf4jLogger.info("CSV Validate is set to: " + SHOULDVALIDATECSV);
        List<TestCase> suiteWithTime;
        metTests = new HashMap<String, Boolean>();

        setUpModules(filePath);

        TestResult tempResult = httpEngine.start(filePath);
        resultsToWrite.add(tempResult);

        CopyOnWriteArrayList<TestCase> allTestsFromXml = null;

        if (tempResult.isPassed()) {

            final XMLParser xmlParser = new XMLParser(filePath, sucRaw);

            try {
                allTestsFromXml = xmlParser.parse();
                tempResult = new FrameWorkResult("Successfully parsed xml", true, filePath);
                slf4jLogger.info("Successfully parsed XML");
            } catch (final Exception e) {
                tempResult = new FrameWorkResult("Could not parse xml" + e.getMessage(), false, filePath);
                slf4jLogger.error("Could not parse XML" + e.getMessage());
            }
        }

        resultsToWrite.add(tempResult);
        if (tempResult.isPassed()) {
            countUniqueTests = 0;

            boolean metFixedTimeTests = false;
            metFixedTimeTests = false;
            for (final int time : times) {

                final List<TestCase> clonedSuite = cloneSuite(allTestsFromXml);
                setUpRanOneInstanceOfTestChecker(clonedSuite, time);
                suiteWithTime = enrichTests(time, clonedSuite);
                resultsToWrite.addAll(runTests(suiteWithTime, metFixedTimeTests));
                metFixedTimeTests = true;
            }

            if (!busyHourTests.isEmpty()) {
                setUpRanOneInstanceOfTestCheckerFixedTime(busyHourTests, 1440);
                slf4jLogger.info("Started busy hour tests");
                suiteWithTime = enrichTests(1440, busyHourTests);
                resultsToWrite.addAll(runTests(suiteWithTime, metFixedTimeTests));
            }

            if (!busyDayTests.isEmpty()) {
                setUpRanOneInstanceOfTestCheckerFixedTime(busyDayTests, 10080);
                slf4jLogger.info("Started busy day tests");
                suiteWithTime = enrichTests(10080, busyDayTests);
                resultsToWrite.addAll(runTests(suiteWithTime, metFixedTimeTests));
            }

            resultsToWrite.add(checkIfAllTestsHaveBeenMet());
            resultsToWrite.add(new FrameWorkResult(testTotals));
        }
        writeAllResults(resultsToWrite);
        shutDownModules();
    }

    private void setUpModules(final String filePath) throws ArrestItException {

        resultTextWriter = new ResultTextWriter(filePath, SHOULDVALIDATECSV);
        resultTextWriter.start();

        final ManagerFactory managerFactory = ManagerFactory.getInstance();
        dbManager = (DBManager) managerFactory.getManager(Constants.DB_MANAGER);
        timeManager = (TimeManager) managerFactory.getManager(Constants.TIME_MANAGER);
        timeManager.setMainTime();

        queryManager = (QueryManager) managerFactory.getManager(Constants.QUERY_MANAGER);
        validationManager = (ValidationManager) managerFactory.getManager(Constants.VALIDATION_MANAGER);
        jsonValidatonManager = (JsonValidationManager) managerFactory.getManager(Constants.JSON_VALIDATION_MANAGER);
        testCaseExpander = new TestCaseExpander(queryManager, this);

        httpEngine = new HttpEngine(uiServerAddress, uiUsername, uiPassword, String.valueOf(System.currentTimeMillis() % 1000));
        resultSerializer = new ResultSerializer(filePath);
        resultSerializer.startSerialization();

        final Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(new ShutDownExecutor(dbManager, httpEngine, resultSerializer));
    }

    private void writeAllResults(final List<TestResult> results) {
        resultTextWriter.writeFrameWorkTests(results);
        resultSerializer.serializeFrameWorkResults(filePath, results);
        resultSerializer.stopSerialization();

    }

    private void shutDownModules() {
        httpEngine.stop();
        dbManager.closeConnectionOnShutDown();
        resultTextWriter.stop();
    }

    private List<TestCase> enrichTests(final int time, final List<TestCase> clonedSuite) {
        final List<TestCase> testSuite = timeManager.enrich(clonedSuite, time);
        return testSuite;
    }

    private TestResult checkIfAllTestsHaveBeenMet() {
        final List<String> testsNotMet = new ArrayList<String>();
        for (final String keyString : metTests.keySet()) {
            if (metTests.get(keyString) == false) {
                testsNotMet.add(keyString);
            }
        }
        if (testsNotMet.size() > 0) {
            return new FrameWorkResult(filePath, countUniqueTests, testsNotMet);
        } else {
            return new FrameWorkResult(filePath, countUniqueTests);
        }
    }

    List<TestResult> runTests(final List<TestCase> templateTests, final boolean metFixedTimeTests) {
        final List<TestResult> frameWorkResults = new ArrayList<TestResult>();
        FrameWorkResult tempResult;
        long queryStartTime = 0;
        long queryEndTime = 0;

        List<TestCase> testsToRun = new ArrayList<TestCase>();
        int excessTrimCounter = 0;
        for (int i = 0; i < templateTests.size();) {
            final TestCase tempTest = templateTests.get(i);
            if (tempTest.getTestType() != null && !metFixedTimeTests) {
                handleFixedTimeTests(tempTest);
                templateTests.remove(i);
            } else if (tempTest.getTestType() != null && metFixedTimeTests) {
                templateTests.remove(i);
            } else {
                i++;
            }
        }

        if (templateTests.size() > 0) {
            testsToRun.add(templateTests.get(0));

            while (testsToRun.size() > 0) {
                TestCase testCase = testsToRun.get(0);
                addTestToMet(testCase.getTitle());
                incrementTestCounter(TOTAL_TESTS, 1);
                slf4jLogger.info("Parent Values for Test Case " + testCase.getDirection() + ":" + testCase.getParentValues());
                System.out.println("Test Case Direction Without Row info: " + testCase.getDirectionWithOutRowInfo());
                initializeTestResultHolder(testCase);
                try {
                    queryManager.urlEnrichment(testCase);
                    tempResult = new FrameWorkResult("Successfully enriched TestCase Url", true);
                } catch (final ArrestItException aie) {
                    tempResult = new FrameWorkResult("Could not enrich TestCase Url: " + aie.getMessage(), false);

                }

                testCase.getTestResultHolder().addTestResult(tempResult);

                if (tempResult.isPassed()) {
                    queryStartTime = System.currentTimeMillis();
                    setJSON(testCase);
                    // this will set the JSON Variables and JSON data Array
                    // which can be cleared once children are created
                    metTests.put(testCase.getTime() + ":" + testCase.getDirectionWithOutRowInfo(), true);
                }

                if (testCase.getData() != null && testCase.getData().length() > 0) {
                    final List<Integer> repeatValidationRowNumbers = testCaseExpander.getRepeatRowNumbers(testCase, false);
                    // expands sqltests and enriches with parent values and
                    // values from json data Array

                    queryManager.expandAndRunSql(testCase, repeatValidationRowNumbers);

                    // validates the results for this window;
                    validationManager.validateTestResults(testCase, repeatValidationRowNumbers);
                    resultTextWriter.writeTestCaseResults(testCase);
                    resultSerializer.serializeResults(testCase.getTestResultHolder());

                    if (!testCase.getRepeatDrillDown().isEmpty()) {
                        testsToRun = testCaseExpander.attemptToCreateChildTests(templateTests, testsToRun, testCase, repeatDrillRowNumbers);
                    }
                } else {
                    incrementTestCounter(NO_OF_EMPTY_GRIDS, 1);
                    resultTextWriter.writeTestCaseResults(testCase);
                    resultSerializer.serializeResults(testCase.getTestResultHolder());
                }

                checkIfTestsHavePassed(testCase);
                testCase = null;
                testsToRun.remove(0);

                if (excessTrimCounter == 4) {
                    testsToRun = removeExcessTestCases(testsToRun);
                    excessTrimCounter = 0;
                }
                excessTrimCounter++;
                queryEndTime = System.currentTimeMillis();

                if (queryEndTime - queryStartTime > 1140000) {
                    httpEngine.stop();
                    frameWorkResults.add(httpEngine.start(filePath));
                }
            }
        }
        return frameWorkResults;
    }

    void setUpRanOneInstanceOfTestChecker(final List<TestCase> suite, final int testTime) {

        for (final TestCase tc : suite) {
            if (tc.getTestType() == null) {
                metTests.put(testTime + ":" + tc.getDirectionWithOutRowInfo(), false);
                countUniqueTests++;
            }
        }
    }

    void setUpRanOneInstanceOfTestCheckerFixedTime(final List<TestCase> suite, final int testTime) {
        countUniqueTests = suite.size();
        for (final TestCase tc : suite) {
            metTests.put(testTime + ":" + tc.getDirectionWithOutRowInfo(), false);
            countUniqueTests++;
        }
    }

    public void handleFixedTimeTests(final TestCase testCase) {

        if (StringUtils.containsIgnoreCase(testCase.getTestType(), "busyhour")) {
            testCase.setTestType(null);
            busyHourTests.add(testCase);
        } else if (StringUtils.containsIgnoreCase(testCase.getTestType(), "busyday")) {
            testCase.setTestType(null);
            busyDayTests.add(testCase);
        }
    }

    private static List<TestCase> cloneSuite(final List<TestCase> baseSuite) {
        final List<TestCase> clonedSuite = new ArrayList<TestCase>();
        for (final TestCase oldTc : baseSuite) {
            final TestCase newTc = new TestCase(oldTc);
            clonedSuite.add(newTc);
        }
        return clonedSuite;
    }

    void setJSON(final TestCase testCase) {

        JSONArray csvData = new JSONArray();

        final JSONArray data = httpEngine.sendUrlQueries(testCase, testCase.getUrl());
        testCase.setData(data);

        if (testCase.getCsvUrl() != null && SHOULDVALIDATECSV) {
            csvData = httpEngine.sendCsvUrl(testCase);
            testCase.setCsvData(csvData);
        }

        jsonValidatonManager.compareCsvAndGrid(testCase);

        if (testCase.getData().length() > 0) {
            repeatDrillRowNumbers = testCaseExpander.getRepeatRowNumbers(testCase, true);
        }
    }

    List<TestCase> removeExcessTestCases(final List<TestCase> testsToRun) {
        final Map<String, Integer> uniqueTestIdsAndCount = new HashMap<String, Integer>();
        String tempTestId = "";
        final List<String> testsWhichExceedLimit = new ArrayList<String>();
        int previousTestCaseCount, limitCount;

        for (final TestCase testCase : testsToRun) {
            tempTestId = testCase.getTitle().substring(0, testCase.getTitle().indexOf(":"));

            if (!uniqueTestIdsAndCount.containsKey(tempTestId)) {
                uniqueTestIdsAndCount.put(tempTestId, 1);
            } else {
                previousTestCaseCount = uniqueTestIdsAndCount.get(tempTestId);
                uniqueTestIdsAndCount.put(tempTestId, previousTestCaseCount + 1);
            }
        }

        for (final String testKey : uniqueTestIdsAndCount.keySet()) {
            if (uniqueTestIdsAndCount.get(testKey) > excessTestCaseLimit) {
                testsWhichExceedLimit.add(testKey);
            }
        }

        if (testsWhichExceedLimit.size() > 0) {
            slf4jLogger.info("Excess testcases found, removing excess");

            final List<TestCase> newTestsToRun = new ArrayList<TestCase>();

            Collections.shuffle(testsToRun);

            for (final TestCase testCase : testsToRun) {
                tempTestId = testCase.getTitle().substring(0, testCase.getTitle().indexOf(":"));
                if (!testsWhichExceedLimit.contains(tempTestId)) {
                    newTestsToRun.add(testCase);
                }
            }

            for (final String testId : testsWhichExceedLimit) {
                limitCount = 0;
                for (final TestCase testCase : testsToRun) {
                    tempTestId = testCase.getTitle().substring(0, testCase.getTitle().indexOf(":"));
                    if (tempTestId.equals(testId)) {
                        limitCount++;
                        if (limitCount < excessTestCaseLimit) {
                            newTestsToRun.add(testCase);
                        }
                    }

                }
                slf4jLogger.info("Removed " + (limitCount - excessTestCaseLimit) + " instances of " + testId);
            }
            return newTestsToRun;
        } else {
            return testsToRun;
        }
    }

    void initializeTestResultHolder(final TestCase testCase) {
        final TestResultHolder testResults = new TestResultHolder(testCase);
        testCase.setTestResultHolder(testResults);
    }

    void initializeTestCounter() {
        this.testTotals = new HashMap<String, Integer>();
        this.testTotals.put(TOTAL_TESTS, 0);
        this.testTotals.put(PASSED_TESTS, 0);
        this.testTotals.put(FAILED_TESTS, 0);
        this.testTotals.put(EXCESS_TESTS, 0);
        this.testTotals.put(DRILLS_ATTEMPTED, 0);
        this.testTotals.put(DRILLS_NOT_COMPLETED, 0);
        this.testTotals.put(NO_OF_EMPTY_GRIDS, 0);
    }

    void incrementTestCounter(final String key, final int noToAdd) {
        testTotals.put(key, testTotals.get(key) + noToAdd);
    }

    void checkIfTestsHavePassed(final TestCase testCase) {
        if (testCase.getTestResultHolder().haveAllTestsPassed()) {
            incrementTestCounter(PASSED_TESTS, 1);
        } else {
            incrementTestCounter(FAILED_TESTS, 1);
        }
    }

    void addTestToMet(String title) {
        final int colonIndex = title.indexOf(":");
        final int secondColonIndex = title.indexOf(":", colonIndex + 1);
        if (secondColonIndex >= 0) {
            title = title.substring(0, secondColonIndex);
        }
        metTests.put(title, true);
    }
}