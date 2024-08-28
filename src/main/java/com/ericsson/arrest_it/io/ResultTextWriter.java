package com.ericsson.arrest_it.io;

import static com.ericsson.arrest_it.common.Constants.*;

import java.io.*;
import java.util.List;

import com.ericsson.arrest_it.common.TestCase;
import com.ericsson.arrest_it.results.TestResult;
import com.ericsson.arrest_it.results.TestResultHolder;

public class ResultTextWriter {
    private final String failedFilename;
    private final String allTestsFilename;
    private BufferedWriter bwAllTests;
    private BufferedWriter bwFailedTests;
    private boolean isCsv;

    public ResultTextWriter(final String filePath, final boolean isCsv) {
        failedFilename = splitFileName(filePath, FAILED_FILE);
        allTestsFilename = splitFileName(filePath, ALL_TESTS_FILE);
        this.isCsv = isCsv;
    }

    public String splitFileName(String filePath, final String fileType) {
        filePath = filePath.substring(filePath.indexOf(TEST_FOLDER) + TEST_FOLDER.length() + 1);

        if (SEPARATOR.equals("\\")) {
            filePath = filePath.replaceAll("\\" + SEPARATOR, "_");
        } else {
            filePath = filePath.replaceAll("/", "_");
        }
        filePath = filePath.replace(".xml", "");
        return RELATIVE_PATH + RESULTS_FOLDER + SEPARATOR + filePath + fileType + ".txt";
    }

    public void start() {
        final File failedFile = new File(failedFilename);
        final File allTestsFile = new File(allTestsFilename);
        failedFile.getParentFile().mkdirs();
        allTestsFile.getParentFile().mkdirs();
        FileWriter fwFailed = null;
        FileWriter fwAllTests = null;
        try {
            failedFile.createNewFile();
            allTestsFile.createNewFile();
            fwFailed = new FileWriter(failedFile.getAbsoluteFile());
            fwAllTests = new FileWriter(allTestsFile.getAbsoluteFile());
        } catch (final IOException e) {
            e.printStackTrace();
        }
        bwFailedTests = new BufferedWriter(fwFailed);
        bwAllTests = new BufferedWriter(fwAllTests);
    }

    public void writeFrameWorkTests(final List<TestResult> testResults) {

        final String frameWorkMarker = "\n\n=========FrameWorkResult==========\n\n";
        boolean hasWrittenMarker = false;
        boolean hasWrittenFailedMarker = false;

        for (final TestResult tr : testResults) {
            if (!tr.isPassed()) {
                if (!hasWrittenFailedMarker) {
                    writeFailedLine(frameWorkMarker);
                    hasWrittenFailedMarker = true;
                }
                writeFailedLine(tr.toString());
            }
            if (!hasWrittenMarker) {
                writeAllTestsLine(frameWorkMarker);
                hasWrittenMarker = true;
            }
            writeAllTestsLine(tr.toString());
        }
    }

    public void writeFailedLine(final String line) {
        try {
            bwFailedTests.write(line);
            bwFailedTests.newLine();
        } catch (final IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void writeAllTestsLine(final String line) {
        try {
            bwAllTests.write(line);
            bwAllTests.newLine();
        } catch (final IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void writeTestCaseResults(final TestCase testCase) {
        final TestResultHolder testResultHolder = testCase.getTestResultHolder();
        final boolean allTestsPassed = testResultHolder.haveAllTestsPassed();
        if (!allTestsPassed) {
            writeAllFailedTestCaseResultsHeaders(testResultHolder);
        }
        writeAllTestCaseResultsHeaders(testResultHolder);

        writeResultDetails(testResultHolder, allTestsPassed);
    }

    public void writeAllFailedTestCaseResultsHeaders(final TestResultHolder testResultHolder) {
        writeFailedLine(getTestResultHeader(testResultHolder));
    }

    public void writeAllTestCaseResultsHeaders(final TestResultHolder testResultHolder) {
        writeAllTestsLine(getTestResultHeader(testResultHolder));
    }

    public void writeResultDetails(final TestResultHolder testResultHolder, final boolean allTestsPassed) {
        final String[] resultTypesToWrite = getArrayOfResultTypes();
        for (final String resultTypeToWrite : resultTypesToWrite) {
            if (!allTestsPassed) {
                writeFailedLine(resultTypeToWrite + " results:");
            }
            writeAllTestsLine(resultTypeToWrite + " results:");
            final List<TestResult> oneTypeOfResultToWrite = testResultHolder.returnOneTypeOfResults(resultTypeToWrite);
            for (final TestResult tr : oneTypeOfResultToWrite) {
                if (!allTestsPassed) {
                    writeFailedLine(tr.toString());
                }
                writeAllTestsLine(tr.toString());
            }
        }
    }

    public void writeToBothOrOneTestResultFile(final String lineToWrite, final boolean isPassed) {
        if (!isPassed) {
            writeFailedLine(lineToWrite);

        }
        writeAllTestsLine(lineToWrite);
    }

    public String getTestResultHeader(final TestResultHolder testResultHolder) {
        String header = "";
        header += "\n-------------------TEST CASE----------------------\n\n";
        header += "Test Id: " + testResultHolder.getTestTitle() + "\n";
        header += "Test Direction: " + testResultHolder.getDirection() + "\n";
        header += "Test Time: " + testResultHolder.getTimeRange() + "mins\n";
        header += "Test Url: " + testResultHolder.getUrl() + "\n";
        header += "Parent Grid Values " + testResultHolder.getParentValues() + "\n";
        if (isCsv) {
            header += "Test CSV Url" + testResultHolder.getCsvUrl() + "\n";
        }
        header += "Total Tests Performed: " + testResultHolder.getTotalStatistics().get(TOTAL_TESTS) + "\t";
        header += "Total Tests Passed: " + testResultHolder.getTotalStatistics().get(PASSED_TESTS) + "\t";
        header += "Total Tests Failed: " + testResultHolder.getTotalStatistics().get(FAILED_TESTS) + "\t";
        header += "Percentage Tests Passed: " + testResultHolder.getTotalStatistics().get(PERCENTAGE_PASSED) + "\n";
        return header;
    }

    public String[] getArrayOfResultTypes() {
        final String[] withCsv = { CSV_NETWORK, CSV_PARSING, CSV_VALIDATION, JSON_NETWORK, JSON_PARSING, JSON_VALIDATION, FRAMEWORK };
        final String[] withOutCsv = { JSON_NETWORK, JSON_PARSING, JSON_VALIDATION, FRAMEWORK };

        if (isCsv) {
            return withCsv;
        } else {
            return withOutCsv;
        }
    }

    public boolean isCsv() {
        return isCsv;
    }

    public void setCsv(final boolean isCsv) {
        this.isCsv = isCsv;
    }

    public void stop() {
        try {
            bwAllTests.close();
            bwFailedTests.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }

    }

}
