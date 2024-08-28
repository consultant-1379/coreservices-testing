package com.ericsson.arrest_it.results;

import static com.ericsson.arrest_it.common.Constants.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class FrameWorkResult extends TestResult implements Serializable {

    private static final long serialVersionUID = 2064454011771069149L;

    public FrameWorkResult(final Map<String, Integer> testTotals) {
        super(testTotals);
    }

    public FrameWorkResult(final String textResult, final boolean passed) {
        super(textResult, passed);
    }

    public FrameWorkResult(final String textResult, final boolean hasPassed, final String fileName) {
        super(textResult, hasPassed, fileName);
    }

    public FrameWorkResult(final String textResult, final String rowIdentifier, final boolean passed) {
        super(textResult, rowIdentifier, passed);
    }

    public FrameWorkResult(final String fileName, final int noOfUniqueTests) {
        super(fileName, noOfUniqueTests);
    }

    public FrameWorkResult(final String fileName, final int noOfUniqueTests, final List<String> testsNotRun) {
        super(fileName, noOfUniqueTests, testsNotRun);
    }

    @Override
    public String toString() {
        String toReturn = "";
        final Map<String, Integer> frameWorkTestTotals = getTestTotals();
        final String fileName = getFileName();
        final int noOfUniqueTests = getNoOfUniqueTests();
        final List<String> testsNotRun = getTestsNotRun();
        final String rowIdentifier = getRowIdentifier();
        final String resultText = getResultText();

        if (frameWorkTestTotals != null) {
            toReturn += "Ran " + frameWorkTestTotals.get(TOTAL_TESTS) + " test cases";
            toReturn += "\nPassed " + frameWorkTestTotals.get(PASSED_TESTS) + " test cases";
            toReturn += "\nFailed " + frameWorkTestTotals.get(FAILED_TESTS) + " test cases";
            toReturn += "\nRemoved " + frameWorkTestTotals.get(EXCESS_TESTS) + " excess test cases";
            toReturn += "\nDrills Attempted " + frameWorkTestTotals.get(DRILLS_ATTEMPTED);
            toReturn += "\nDrills Not Completed " + frameWorkTestTotals.get(DRILLS_NOT_COMPLETED);
            toReturn += "\nNumber of Empty Grids " + frameWorkTestTotals.get(NO_OF_EMPTY_GRIDS);

        } else if (fileName != null) {
            toReturn += getResultPassedString() + "\t" + getTestTime() + "\t";
            if (noOfUniqueTests != 0) {
                toReturn += " Ran " + noOfUniqueTests + " unique tests ";

                if (testsNotRun == null) {
                    toReturn += "all Tests were attempted in at least one instance";
                } else {
                    toReturn += "the following tests were not attempted due to data Inavalaibility " + testsNotRun;
                }
            } else {
                toReturn += resultText;
            }
        } else if (rowIdentifier != null) {
            toReturn += getResultPassedString() + "\t" + getTestTime() + "\t";
            if (rowIdentifier.equals(NON_REPEAT_VALIDATION_FLAG)) {
                toReturn += resultText + " relating to entire grid";
            } else {
                toReturn += resultText + " on Row Number " + rowIdentifier;
            }
        } else {
            toReturn += getResultPassedString() + "\t" + getTestTime() + "\t" + resultText;
        }
        return toReturn;
    }
}
