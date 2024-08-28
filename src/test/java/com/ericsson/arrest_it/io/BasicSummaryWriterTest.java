package com.ericsson.arrest_it.io;

import static com.ericsson.arrest_it.common.Constants.*;
import static org.junit.Assert.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import junit.extensions.PA;

import org.junit.*;

import com.ericsson.arrest_it.common.TestCase;
import com.ericsson.arrest_it.common.TestConstants;
import com.ericsson.arrest_it.results.JsonValidationResult;
import com.ericsson.arrest_it.results.TestResultHolder;

public class BasicSummaryWriterTest {
    private static final int[] times = { 30, 60, 180 };
    private BasicSummaryWriter basicSummaryWriter;

    @Before
    public void testCreation() {
        basicSummaryWriter = new BasicSummaryWriter(false, times, "-8000");
        PA.setValue(basicSummaryWriter, "resultFileName", TestConstants.RESULT_FILE);
    }

    @Test
    public void test_writerConstruction() {
        final boolean isCsv = false;
        final String timeZone = "+4000";
        final BasicSummaryWriter basicSummWriter = new BasicSummaryWriter(false, times, timeZone);
        final int[] timesFromTest = (int[]) PA.getValue(basicSummWriter, "times");

        assertEquals(times, timesFromTest);

        final Boolean isCsvFromTest = (Boolean) PA.getValue(basicSummWriter, "isCsv");
        assertEquals(isCsv, isCsvFromTest);
    }

    @Test
    public void test_sortLexigraphically() {

        final String firstDirection = "Launch -> Controller";
        final String secondDirection = "Launch -> Controller -> Drill on Failures";
        final String thirdDirection = "Launch -> Apn -> Drill on Failures -> Drill on Busy Hour";

        final Object[] testDirections = { firstDirection, secondDirection, thirdDirection };

        final String[] sortedDirections = BasicSummaryWriter.sortKeysLexigraphically(testDirections);

        assertEquals(sortedDirections[0], thirdDirection);
        assertEquals(sortedDirections[1], firstDirection);
        assertEquals(sortedDirections[2], secondDirection);
    }

    @Test
    public void test_writeATestResultToFile() {
        final String testDirection = "Launch -> Apn -> Drill on Failures";
        final String testFileName = "interimA_Workspace_Apn.ser";
        final TestCase testCase = new TestCase();
        testCase.setDirectionWithOutRowInfo(testDirection);
        testCase.setTime(times[0]);
        testCase.setTitle("1");
        final TestResultHolder tResHolder = new TestResultHolder(testCase);
        final JsonValidationResult tRes = new JsonValidationResult("x != y", false);
        tResHolder.addTestResult(tRes);

        final List<TestResultHolder> testListOfResults = new ArrayList<TestResultHolder>();

        testListOfResults.add(tResHolder);
        basicSummaryWriter.initializeResultFile();
        basicSummaryWriter.writeResults(testListOfResults, testFileName);
        basicSummaryWriter.closeResultFile();
        final ArrayList<String> linesInFile = new ArrayList<String>();
        String currentLine;
        try {

            final BufferedReader testFileReader = new BufferedReader(new FileReader(TestConstants.RESULT_FILE));
            while ((currentLine = testFileReader.readLine()) != null) {
                linesInFile.add(currentLine);
            }
            testFileReader.close();
        } catch (final FileNotFoundException e) {
            assertTrue(false);
        } catch (final IOException e) {
            e.printStackTrace();
        }

        basicSummaryWriter.closeResultFile();

        assertTrue(linesInFile.contains("Test Case Name: " + testDirection));
        assertTrue(linesInFile.contains("Test Result: Failed	Failure Reason: " + times[0] + ": " + UI_MISMATCH));
    }

    @Test
    public void test_writeResultSubHeading() {
        final String testFileName = "interimA_Workspace_Apn.ser";
        final String expectedFileName = "Workspace_Apn" + ALL_TESTS_FILE + TXT_EXTENSION;

        final String testFrameWorkFailure = "Could not parse XML";

        basicSummaryWriter.initializeResultFile();
        basicSummaryWriter.writeResultSubHeading(testFileName, testFrameWorkFailure);
        basicSummaryWriter.closeResultFile();

        final ArrayList<String> linesInFile = new ArrayList<String>();
        String currentLine;
        try {

            final BufferedReader testFileReader = new BufferedReader(new FileReader(TestConstants.RESULT_FILE));
            while ((currentLine = testFileReader.readLine()) != null) {
                linesInFile.add(currentLine);
            }
            testFileReader.close();
        } catch (final FileNotFoundException e) {
            assertTrue(false);
        } catch (final IOException e) {
            e.printStackTrace();
        }

        basicSummaryWriter.closeResultFile();

        assertTrue(linesInFile.contains(expectedFileName));
        assertTrue(linesInFile.contains(testFrameWorkFailure));
    }

    @Test
    public void test_updateSimplifiedSummary() {
        final String testExistingDetails = "a,b,c";
        final String testProposedAddition = "b";
        final String testSeparator = ",";

        String testOutput = BasicSummaryWriter.updateSimplifiedSummary(testExistingDetails, testProposedAddition, testSeparator);

        assertEquals(testExistingDetails, testOutput);

        final String testProposedAdditionB = "d,e";

        testOutput = BasicSummaryWriter.updateSimplifiedSummary(testExistingDetails, testProposedAdditionB, testSeparator);

        assertEquals(testOutput, testExistingDetails + "," + testProposedAdditionB);
    }

    @Test
    public void test_writeResultLinesToFile() {
        final String testLine = "this is a test line";
        basicSummaryWriter.initializeResultFile();
        basicSummaryWriter.writeResultLine(testLine);
        basicSummaryWriter.closeResultFile();

        final ArrayList<String> linesInFile = new ArrayList<String>();
        String currentLine;
        try {

            final BufferedReader testFileReader = new BufferedReader(new FileReader(TestConstants.RESULT_FILE));
            while ((currentLine = testFileReader.readLine()) != null) {
                linesInFile.add(currentLine);
            }
            testFileReader.close();
        } catch (final FileNotFoundException e) {
            assertTrue(false);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        basicSummaryWriter.closeResultFile();
        assertTrue(linesInFile.contains(testLine));
    }

    @Test
    public void test_createResultsFile() {
        basicSummaryWriter.initializeResultFile();
        try {
            final BufferedWriter testBw = (BufferedWriter) PA.getValue(basicSummaryWriter, "resultFileWriter");
            try {
                testBw.flush();
                assertTrue(true);
            } catch (final IOException ie) {
                assertTrue(false);
            }

        } finally {
            basicSummaryWriter.closeResultFile();
        }
    }

    @After
    public void testTearDown() {
        final File resultsFile = new File(TestConstants.RESULT_FILE);
        if (resultsFile.exists()) {
            resultsFile.delete();
        }
    }

}
