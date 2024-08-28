package com.ericsson.arrest_it.main;

import static com.ericsson.arrest_it.common.Constants.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.arrest_it.common.*;
import com.ericsson.arrest_it.io.PropertyReader;
import com.ericsson.arrest_it.logging.LogbackFileUtils;
import com.ericsson.arrest_it.manager.*;
import com.ericsson.arrest_it.results.FrameWorkResult;

class TestCaseExpander {
    private int maxNoOfValidationsPerUrl;

    private final QueryManager queryManager;
    private final TestDriver testDriver;
    private static final Logger slf4jLogger = LoggerFactory.getLogger(LogbackFileUtils.ARREST_IT_LOGGER);

    public TestCaseExpander(final QueryManager queryManager, final TestDriver testDriver) {
        this.queryManager = queryManager;
        this.testDriver = testDriver;
        maxNoOfValidationsPerUrl = PropertyReader.getInstance().getMaxNoOfValidationsPerUrl();
    }

    public List<Integer> getRepeatRowNumbers(final TestCase testCase, final boolean isDrillDown) {

        int maxNoOfValidations = maxNoOfValidationsPerUrl;

        if (!isDrillDown && testCase.getMaxVals() != 0) {
            maxNoOfValidations = testCase.getMaxVals();
        }
        String repeatInstruction;

        final JSONArray data = testCase.getData();
        if (isDrillDown) {
            repeatInstruction = testCase.getRepeatDrillDownInstruction();
        } else {
            repeatInstruction = testCase.getRepeatValidationInstruction();
        }
        final List<Integer> repeatRowNumbers = new ArrayList<Integer>();

        int noOfRows = 0;
        if (data != null) {
            noOfRows = data.length();
        }

        if (repeatInstruction == null || repeatInstruction.isEmpty()) {
            repeatInstruction = "all";
        }
        if (repeatInstruction.equalsIgnoreCase("all")) {
            if (noOfRows > maxNoOfValidations) {
                noOfRows = maxNoOfValidations;
            }

            for (int i = 0; i < noOfRows; i++) {
                repeatRowNumbers.add(i);
            }
        } else if (StringUtils.containsIgnoreCase(repeatInstruction, "random")) {
            final double randomSize = Double.parseDouble(repeatInstruction.substring("random".length()));
            int randomVal = (int) (Math.random() * randomSize);

            for (int i = 0; i < noOfRows; i++) {
                if (i % (randomVal + 1) == 0) {
                    repeatRowNumbers.add(i);
                    if (repeatRowNumbers.size() == maxNoOfValidations) {
                        break;
                    }
                    randomVal = (int) (Math.random() * randomSize);
                }
            }
        }
        // TODO add if statements for top/max and fix random
        maxNoOfValidations = 0;
        return repeatRowNumbers;
    }

    /**
     * @param templateTests
     *        tests which have been updated for datetime but hold none of the values passed from a parent test or 'master values'.
     * @param testsToRun
     *        the collection of tests which are waiting to be performed.
     * @param parentTest
     *        the test from which will pass values to the new tests created.
     * @param drillIndices
     *        the row indices which define the row in the parent test data from where values for child tests will be obtained.
     * @return the updated collection of tests to run which now contains the new test cases created.
     */

    public List<TestCase> attemptToCreateChildTests(final List<TestCase> templateTests, final List<TestCase> testsToRun, final TestCase parentTest,
                                                    final List<Integer> drillIndices) {
        final List<String> newTestIds = parentTest.getRepeatDrillDown();

        try {
            for (final String newTestId : newTestIds) {
            	
            	TestCase futureTestTemplate = null;
            	
            	try{
                futureTestTemplate = getTemplateTestById(newTestId, templateTests);
                
            	}catch (final ArrestItException aie)
            	{
            		slf4jLogger.warn(aie.getMessage());continue;
            	}
                
                createChildTestsFromTemplate(futureTestTemplate, parentTest, drillIndices, testsToRun);
            }
        } catch (final ArrestItException aie) {
            parentTest.getTestResultHolder().addFrameWorkResult(new FrameWorkResult(aie.getMessage(), false));
        }

        return testsToRun;
    }

    private void createChildTestsFromTemplate(final TestCase futureTestTemplate, final TestCase parentTest, List<Integer> drillIndices,
                                              final List<TestCase> testsToRun) throws ArrestItException {
        boolean shouldContinueCreationAttempts = true;
        final Precondition precondition = futureTestTemplate.getPrecondition();
        int childTestsCreated = 0;

        if (precondition == null) {
            createTestsWithoutPrecondition(drillIndices, testsToRun, futureTestTemplate, parentTest);
        } else {

            final List<Integer> drillsAttempted = new ArrayList<Integer>();

            while (shouldContinueCreationAttempts) {
                childTestsCreated += createTestsWithPrecondition(precondition, drillIndices, testsToRun, parentTest, futureTestTemplate,
                        childTestsCreated);
                drillsAttempted.addAll(drillIndices);
                if (areFurtherDrillAttemptsRequired(childTestsCreated, drillsAttempted, parentTest)) {
                    drillIndices = obtainNewDrillIndices(parentTest, drillsAttempted);
                } else {
                    shouldContinueCreationAttempts = false;
                }
            }
        }
    }

    private List<Integer> obtainNewDrillIndices(final TestCase parentTest, final List<Integer> drillsAttempted) {
        final List<Integer> newDrillIndices = new ArrayList<Integer>();
        int dataIndex = 0;
        while ((newDrillIndices.size() < maxNoOfValidationsPerUrl) && dataIndex < parentTest.getData().length()) {
            if (!drillsAttempted.contains(dataIndex)) {
                newDrillIndices.add(dataIndex);
            }
            dataIndex++;
        }
        return newDrillIndices;
    }

    private boolean areFurtherDrillAttemptsRequired(final int childTestsCreated, final List<Integer> drillsAttempted, final TestCase parentTest) {
        if (childTestsCreated >= maxNoOfValidationsPerUrl) {
            return false;
        }
        if (drillsAttempted.size() >= parentTest.getData().length()) {
            return false;
        }
        return true;
    }

    private int createTestsWithPrecondition(final Precondition precondition, final List<Integer> drillIndices, final List<TestCase> testsToRun,
                                            final TestCase parentTest, final TestCase futureTestTemplate, final int childTestsCreated)
            throws ArrestItException {
        final PreconditionManager preconditionManager = (PreconditionManager) ManagerFactory.getInstance().getManager(PRECONDITION_MANAGER);
        int testsCreated = 0;
        final int testsRequired = maxNoOfValidationsPerUrl - childTestsCreated;
        for (final int drillIndex : drillIndices) {
            testDriver.incrementTestCounter(DRILLS_ATTEMPTED, 1);
            if (preconditionManager.isPreconditionPassed(parentTest, precondition, drillIndex)) {
                final TestCase childTest = cloneWithParams(parentTest, futureTestTemplate, drillIndex);
                testsToRun.add(childTest);
                testsCreated++;
                slf4jLogger.info("Precondition met. Created test: " + futureTestTemplate.getTitle() + " From drill on row: " + drillIndex);
            } else {
                testDriver.incrementTestCounter(DRILLS_NOT_COMPLETED, 1);
                slf4jLogger.info("Precondition not met. Did not create test: " + futureTestTemplate.getTitle() + " From drill on row: " + drillIndex);
            }

            if (testsCreated == testsRequired) {
                break;
            }
        }

        return testsCreated;
    }

    private void createTestsWithoutPrecondition(final List<Integer> drillIndices, final List<TestCase> testsToRun, final TestCase futureTestTemplate,
                                                final TestCase parentTest) {
        for (final int drillIndex : drillIndices) {
            final TestCase childTest = cloneWithParams(parentTest, futureTestTemplate, drillIndex);
            testsToRun.add(childTest);
            testDriver.incrementTestCounter(DRILLS_ATTEMPTED, 1);
            slf4jLogger.info("Created test: " + futureTestTemplate.getTitle() + " From drill on row: " + drillIndex);
        }
    }

    private TestCase getTemplateTestById(final String newTestId, final List<TestCase> templateTests) throws ArrestItException {

        for (final TestCase templateTest : templateTests) {
            String templateTestId = templateTest.getTitle();
            templateTestId = templateTestId.substring(0, templateTestId.indexOf(COLON));
            if (templateTestId.equals(newTestId)) {
                return templateTest;
            }
        }
        
        throw new ArrestItException("Test Id referenced for drill down not found");
    }

    TestCase cloneWithParams(final TestCase parentTest, final TestCase templateTest, final int drillIndex) {

        final TestCase newTest = new TestCase(templateTest);
        newTest.setTitle(templateTest.getTitle() + COLON + drillIndex);
        String key = templateTest.getTitle();
        if (key.contains(COLON)) {
            key = key.substring(0, key.indexOf(COLON));
        }

        String parentId = parentTest.getTitle();

        if (parentId.contains(COLON)) {
            parentId = parentId.substring(0, parentId.indexOf(COLON));
        }

        final List<String> parentKeys = parentTest.getPassToDrill().get(key);
        if (parentKeys != null) {

            for (final String parentKey : parentKeys) {
                if (parentKey.contains(".")) {
                    final String parentValue = parentTest.getParentValues().get(parentKey);
                    newTest.addParentValue(parentKey, parentValue);
                } else {
                    final String parentValue = queryManager.getJSONValue(parentTest, String.valueOf(drillIndex), parentKey);
                    newTest.addParentValue(parentId + "." + parentKey, parentValue);
                }
            }
        }
        newTest.setTitle(newTest.getTitle() + COLON + drillIndex);
        newTest.setDirection(parentTest.getDirection() + " -> " + newTest.getDirection() + " Row(" + drillIndex + ")");
        return newTest;
    }

    /**
     * @return the maxNoOfValidationsPerUrl
     */
    public int getMaxNoOfValidationsPerUrl() {
        return maxNoOfValidationsPerUrl;
    }

    /**
     * @param maxNoOfValidationsPerUrl
     *        the maxNoOfValidationsPerUrl to set
     */
    public void setMaxNoOfValidationsPerUrl(final int maxNoOfValidationsPerUrl) {
        this.maxNoOfValidationsPerUrl = maxNoOfValidationsPerUrl;
    }
}