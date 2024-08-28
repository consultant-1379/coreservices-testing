package com.ericsson.arrest_it.results;

import static com.ericsson.arrest_it.common.Constants.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

public class TestResult implements Serializable {

    private static final long serialVersionUID = 8175593675668712615L;
    private String resultText;
    private boolean passed;
    private DateTime testTime;

    private int noOfUniqueTests = 0;
    private List<String> testsNotRun;
    private String fileName;
    private String rowIdentifier;
    private Map<String, Integer> testTotals;

    public TestResult(final String resultText, final boolean hasPassed) {
        this.resultText = resultText;
        this.passed = hasPassed;
        this.testTime = new DateTime();
    }

    public TestResult(final String resultText, final String rowIdentifier, final boolean hasPassed) {
        this.resultText = resultText;
        this.passed = hasPassed;
        this.setRowIdentifier(rowIdentifier);
        this.testTime = new DateTime();
    }

    public TestResult(final String resultText, final boolean hasPassed, final String fileName) {
        this.resultText = resultText;
        this.passed = hasPassed;
        this.setFileName(fileName);
        this.testTime = new DateTime();
    }

    public TestResult(final String fileName, final int noOfUniqueTests) {
        this.noOfUniqueTests = noOfUniqueTests;
        this.passed = true;
        this.setFileName(fileName);
        this.testTime = new DateTime();
    }

    public TestResult(final Map<String, Integer> testTotals) {
        this.setTestTotals(testTotals);
        this.testTime = new DateTime();
    }

    public TestResult(final String fileName, final int noOfUniqueTests, final List<String> testsNotRun) {
        this.setNoOfUniqueTests(noOfUniqueTests);
        this.passed = false;
        this.setFileName(fileName);
        this.testsNotRun = testsNotRun;
        this.testTime = new DateTime();
    }

    public String getResultText() {
        return resultText;
    }

    public void setResultText(final String resultText) {
        this.resultText = resultText;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(final boolean passed) {
        this.passed = passed;
    }

    public DateTime getTestTime() {
        return testTime;
    }

    public void setTestTime(final DateTime testTime) {
        this.testTime = testTime;
    }

    public int getNoOfUniqueTests() {
        return noOfUniqueTests;
    }

    public void setNoOfUniqueTests(final int noOfUniqueTests) {
        this.noOfUniqueTests = noOfUniqueTests;
    }

    public List<String> getTestsNotRun() {
        return testsNotRun;
    }

    public void setTestsNotRun(final List<String> testsNotRun) {
        this.testsNotRun = testsNotRun;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public String getRowIdentifier() {
        return rowIdentifier;
    }

    public void setRowIdentifier(final String rowIdentifier) {
        this.rowIdentifier = rowIdentifier;
    }

    public String getResultPassedString() {
        if (passed) {
            return "Passed";
        } else {
            return "**FAILED**";
        }
    }

    @Override
    public String toString() {
        String toReturn = "";

        toReturn += getResultPassedString() + "\t" + testTime + "\t";

        if (rowIdentifier != null) {
            if (rowIdentifier.equals(NON_REPEAT_VALIDATION_FLAG)) {
                toReturn += resultText + " relating to entire grid";
            } else {
                toReturn += resultText + " on Row Number " + rowIdentifier;
            }

        } else {
            toReturn += " " + resultText;
        }

        return toReturn;
    }

    public Map<String, Integer> getTestTotals() {
        return testTotals;
    }

    public void setTestTotals(final Map<String, Integer> testTotals) {
        this.testTotals = testTotals;
    }

}
