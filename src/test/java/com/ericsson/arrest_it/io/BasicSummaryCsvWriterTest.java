package com.ericsson.arrest_it.io;

import static com.ericsson.arrest_it.common.Constants.*;
import static org.junit.Assert.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import junit.extensions.PA;

import org.apache.commons.lang3.StringUtils;
import org.junit.*;

import com.ericsson.arrest_it.common.TestCase;
import com.ericsson.arrest_it.common.TestConstants;
import com.ericsson.arrest_it.results.*;

public class BasicSummaryCsvWriterTest {
    private static final int[] times = { 30, 60 };
    private static BasicSummaryCsvWriter BASIC_SUMMARY_CSV_WRITER;

    @BeforeClass
    public static void testCreation() {
        BASIC_SUMMARY_CSV_WRITER = new BasicSummaryCsvWriter(true, times, "-8000");
        PA.setValue(BASIC_SUMMARY_CSV_WRITER, "resultFileName", TestConstants.RESULT_FILE);
    }

    @Test
    public void test_writerConstruction() {
        final boolean isCsv = false;
        final String timeZone = "+4000";
        final BasicSummaryCsvWriter basicSummCsvWriter = new BasicSummaryCsvWriter(false, times, timeZone);
        final int[] timesFromTest = (int[]) PA.getValue(basicSummCsvWriter, "times");

        assertEquals(times, timesFromTest);

        final Boolean isCsvFromTest = (Boolean) PA.getValue(basicSummCsvWriter, "isCsv");
        assertEquals(isCsv, isCsvFromTest);
    }

    @Test
    public void test_writeATestResultToFile() throws IOException {
        final String testDirection = "Launch -> Apn -> Drill on Failures";
        final String testDirectionNotRun = "Launch -> Apn -> Drill on Failures -> Drill on Imsi";
        final String testDirectionNotRunB = "Launch -> Apn -> Drill on Failures -> Drill on TAC";
        final String testFileName = "interimA_Workspace_Apn.ser";
        final TestCase testCase = new TestCase();
        testCase.setDirectionWithOutRowInfo(testDirection);
        testCase.setTime(times[0]);
        testCase.setTitle("1");
        final TestResultHolder tResHolder = new TestResultHolder(testCase);
        final JsonValidationResult tRes = new JsonValidationResult("x != y", false);
        tResHolder.addTestResult(tRes);

        final ArrayList<String> testsNotRun = new ArrayList<String>();
        testsNotRun.add(times[0] + ":" + testDirectionNotRun);
        testsNotRun.add(times[0] + ":" + testDirectionNotRunB);
        final FrameWorkResult frameRes = new FrameWorkResult("Apn.txt", 7, testsNotRun);
        final List<TestResult> frameWorkResults = new ArrayList<TestResult>();
        frameWorkResults.add(frameRes);
        final TestResultHolder tResHolderB = new TestResultHolder("Apn.txt", frameWorkResults);
        final List<TestResultHolder> testListOfResults = new ArrayList<TestResultHolder>();

        testListOfResults.add(tResHolder);
        testListOfResults.add(tResHolderB);

        BASIC_SUMMARY_CSV_WRITER.initializeResultFile();
        BASIC_SUMMARY_CSV_WRITER.writeResults(testListOfResults, testFileName);
        BASIC_SUMMARY_CSV_WRITER.closeResultFile();

        final ArrayList<String> linesInFile = new ArrayList<String>();
        String currentLine;
        BufferedReader testFileReader = null;
        try {

            testFileReader = new BufferedReader(new FileReader(TestConstants.RESULT_FILE));
            while ((currentLine = testFileReader.readLine()) != null) {
                linesInFile.add(currentLine);
            }
            testFileReader.close();
        } catch (final FileNotFoundException e) {
            assertTrue(false);
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            if (testFileReader != null) {
                testFileReader.close();
            }
        }

        boolean hasNotRunTest = false;
        boolean hasSecondNotRunTest = false;
        for (final String lineInFile : linesInFile) {

            if (lineInFile.contains(testDirectionNotRun) && lineInFile.contains(TEST_NOT_RUN_IDENTIFIER)) {
                hasNotRunTest = true;
            }
            if (lineInFile.contains(testDirectionNotRunB) && lineInFile.contains(TEST_NOT_RUN_IDENTIFIER)) {
                hasSecondNotRunTest = true;
            }
        }

        assertTrue(hasNotRunTest);
        assertTrue(hasSecondNotRunTest);
        final String expectedLine = testDirection + COMMA + FAILED_IDENTIFIER + COMMA + NOT_APPLICABLE_IDENTIFIER + COMMA + NOT_APPLICABLE_IDENTIFIER
                + COMMA + NOT_APPLICABLE_IDENTIFIER + COMMA + times[0] + ": " + UI_MISMATCH + COMMA;
        assertTrue(linesInFile.contains(expectedLine));
    }

    @Test
    public void test_initializeResultFile() throws IOException {

        BASIC_SUMMARY_CSV_WRITER.initializeResultFile();
        BASIC_SUMMARY_CSV_WRITER.closeResultFile();

        String heading = "";
        String currentLine;
        BufferedReader testFileReader = null;
        try {
            testFileReader = new BufferedReader(new FileReader(TestConstants.RESULT_FILE));
            while ((currentLine = testFileReader.readLine()) != null) {
                if (currentLine.length() > 0)
                    heading = currentLine;
            }
        } catch (final FileNotFoundException e) {
            assertTrue(false);
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            if (testFileReader != null) {
                testFileReader.close();
            }
        }

        assertTrue(heading.contains(TEST_NAME_HEADING));
        assertTrue(heading.contains(FAILURE_SUMMARY_HEADING));
        assertTrue(StringUtils.countMatches(heading, UI_RESULT) == times.length);
        assertTrue(StringUtils.countMatches(heading, CSV_RESULT) == times.length);
    }

    @AfterClass
    public static void testTearDown() {
        BASIC_SUMMARY_CSV_WRITER.closeResultFile();

        final File resultsFile = new File(TestConstants.RESULT_FILE);
        if (resultsFile.exists()) {
            resultsFile.delete();
        }
    }

}
