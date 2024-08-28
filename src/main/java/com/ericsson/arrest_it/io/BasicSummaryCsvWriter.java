package com.ericsson.arrest_it.io;

import static com.ericsson.arrest_it.common.Constants.*;

import java.io.*;
import java.util.*;

import org.joda.time.DateTime;

import com.ericsson.arrest_it.results.TestResultHolder;

public class BasicSummaryCsvWriter {
    private final int[] times;
    private final String resultFileName;
    private BufferedWriter resultFileWriter;
    private final Boolean isCsv;

    private static String UI = "Ui";
    private static String CSV = "Csv";
    private static String FAILED_SUMMARY = "failedSummary";

    public BasicSummaryCsvWriter(final Boolean isCsv, final int[] times, final String tzOffset) {
        this.times = times;
        this.isCsv = isCsv;
        String now = new DateTime().toString();
        now = now.substring(0, now.indexOf("."));
        now = now.replaceAll(":", "-");
        this.resultFileName = RELATIVE_PATH + SEPARATOR + RESULTS_FOLDER + SEPARATOR + BASIC_SUMMARY_CSV_FILE + now + CSV_EXTENSION;
    }

    public void writeResults(final List<TestResultHolder> testResults, final String serializedFileName) {

        final Map<String, HashMap<String, String>> results = new HashMap<String, HashMap<String, String>>();
        boolean hasPassed;
        HashMap<String, String> actualResults = new HashMap<String, String>();
        String testIdentifier;

        for (final TestResultHolder tResHolder : testResults) {
            testIdentifier = tResHolder.getDirectionWithoutRows();

            hasPassed = tResHolder.haveAllTestsPassed();

            if (!tResHolder.isFrameWorkOnly()) {
                if (!results.containsKey(testIdentifier)) {
                    actualResults = initializeResultMap(actualResults);
                } else {
                    actualResults = results.get(testIdentifier);
                }
                if (!hasPassed) {
                    if (actualResults.containsKey(FAILED_SUMMARY)) {
                        actualResults.put(
                                FAILED_SUMMARY,
                                BasicSummaryWriter.updateSimplifiedSummary(actualResults.get(FAILED_SUMMARY),
                                        tResHolder.getSimplifiedResultSummary(DASH_WITH_SPACES), DASH_WITH_SPACES));
                    } else {
                        actualResults.put(FAILED_SUMMARY, tResHolder.getSimplifiedResultSummary(DASH_WITH_SPACES));
                    }
                }

                actualResults = updateResultHoldersWithResults(tResHolder, times, actualResults);

                actualResults.put(UI + tResHolder.getTimeRange(), tResHolder.haveAllUiTestsPassed());
                if (isCsv) {
                    actualResults.put(CSV + tResHolder.getTimeRange(), tResHolder.haveAllCSVResultsPassed());
                }

                results.put(testIdentifier, actualResults);

            } else if (!hasPassed) {

                final List<String> testsNotRun = tResHolder.getAllTestsNotRun();
                final Map<String, String> resultTexts = tResHolder.getFrameWorkOnlyFailedResultsTexts();

                if (testsNotRun != null) {
                    for (final String testNotRun : testsNotRun) {

                        final String[] testDetails = testNotRun.split(":");
                        String testTime = testDetails[0];
                        String resultString = "";

                        final ArrayList<String> testTimes = new ArrayList<String>();
                        for (final int i : times) {
                            testTimes.add(String.valueOf(i));
                        }

                        if (!testTimes.contains(testTime)) {
                            resultString += testTime + ":";
                            testTime = String.valueOf(times[0]);
                        }
                        final String testDescription = testDetails[1];

                        if (results.containsKey(testDescription)) {
                            actualResults = results.get(testDescription);
                            actualResults.put(UI + testTime, resultString + TEST_NOT_RUN_IDENTIFIER);
                            results.put(testDescription, actualResults);
                        } else {
                            actualResults = initializeResultMap(actualResults);
                            actualResults.put(UI + testTime, resultString + TEST_NOT_RUN_IDENTIFIER);
                            results.put(testDescription, actualResults);
                        }
                    }
                }

                if (!resultTexts.isEmpty()) {
                    for (final String key : resultTexts.keySet()) {
                        actualResults = new HashMap<String, String>();
                        actualResults.put(FAILED_SUMMARY, resultTexts.get(key));
                        results.put(key, actualResults);
                    }
                }
            }
        }
        writeResultsToFile(results);
    }

    public HashMap<String, String> updateResultHoldersWithResults(final TestResultHolder tResHolder, final int[] times,
                                                                  final HashMap<String, String> actualResults) {
        String resultString = "";
        int time = tResHolder.getTimeRange();
        final ArrayList<String> testTimes = new ArrayList<String>();
        for (final int i : times) {
            testTimes.add(String.valueOf(i));
        }
        if (!testTimes.contains(String.valueOf(time))) {
            resultString += tResHolder.getTimeRange() + ":";
            time = times[0];
        }

        actualResults.put(UI + time, resultString + tResHolder.haveAllUiTestsPassed());

        if (isCsv) {
            actualResults.put(CSV + time, resultString + tResHolder.haveAllCSVResultsPassed());
        }

        return actualResults;
    }

    public void writeResultsToFile(final Map<String, HashMap<String, String>> results) {
        final Object[] keySet = results.keySet().toArray();
        final String[] sortedKeys = BasicSummaryWriter.sortKeysLexigraphically(keySet);
        String resultString;
        final List<String> columns = new ArrayList<String>();

        for (final int time : times) {
            columns.add(UI + time);
        }

        if (isCsv) {
            for (final int time : times) {
                columns.add(CSV + time);
            }
        }

        columns.add(FAILED_SUMMARY);

        for (final String resultNameKey : sortedKeys) {
            resultString = resultNameKey + ",";
            final HashMap<String, String> resultGroup = results.get(resultNameKey);
            for (final String individualResult : columns) {
                if (resultGroup.containsKey(individualResult)) {
                    resultString += resultGroup.get(individualResult) + ",";
                } else
                    resultString += ",";
            }
            writeResultLine(resultString);
        }
    }

    public HashMap<String, String> initializeResultMap(HashMap<String, String> actualResults) {
        actualResults = new HashMap<String, String>();

        for (final int time : times) {
            actualResults.put(UI + time, NOT_APPLICABLE_IDENTIFIER);
        }

        if (isCsv) {
            for (final int time : times) {
                actualResults.put(CSV + time, NOT_APPLICABLE_IDENTIFIER);
            }
        }

        return actualResults;
    }

    public void writeHeading() {
        String headingString = TEST_NAME_HEADING + ": ,";

        for (final int time : times) {
            headingString += time + " :" + UI_RESULT + ",";
        }

        if (isCsv) {
            for (final int time : times) {
                headingString += time + " :" + CSV_RESULT + ",";
            }
        }

        headingString += FAILURE_SUMMARY_HEADING;
        writeResultLine(headingString);
    }

    public void initializeResultFile() {

        final File basicSummaryFile = new File(resultFileName);
        basicSummaryFile.getParentFile().mkdirs();
        FileWriter fwSummary = null;
        try {
            basicSummaryFile.createNewFile();
            fwSummary = new FileWriter(basicSummaryFile.getAbsoluteFile());
        } catch (final IOException e) {
            e.printStackTrace();
        }
        resultFileWriter = new BufferedWriter(fwSummary);

        writeHeading();
    }

    public void writeResultLine(final String line) {
        try {
            resultFileWriter.write(line);
            resultFileWriter.newLine();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void closeResultFile() {
        try {
            resultFileWriter.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

}
