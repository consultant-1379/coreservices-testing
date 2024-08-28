package com.ericsson.arrest_it.io;

import static com.ericsson.arrest_it.common.Constants.*;

import java.io.*;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import com.ericsson.arrest_it.results.TestResultHolder;

public class BasicSummaryWriter {

    private final int[] times;
    private final String interimFolder;
    private final String resultFileName;
    private BufferedWriter resultFileWriter;
    private final Boolean isCsv;
    private final BasicSummaryCsvWriter basicCsv;

    public BasicSummaryWriter(final Boolean isCsv, final int[] times, final String tzOffset) {
        this.times = times;
        this.isCsv = isCsv;
        this.interimFolder = RELATIVE_PATH + SEPARATOR + INTERIM_FILE_FOLDER;
        String now = new DateTime().toString();
        now = now.substring(0, now.indexOf("."));
        now = now.replaceAll(":", "-");
        this.resultFileName = RELATIVE_PATH + SEPARATOR + RESULTS_FOLDER + SEPARATOR + BASIC_SUMMARY_FILE + now + TXT_EXTENSION;
        this.basicCsv = new BasicSummaryCsvWriter(isCsv, times, tzOffset);
    }

    public void deserializeResults() {
        final List<File> filesToDeSerialize = SummaryWriter.getFilesFromInterimFolder(interimFolder);
        initializeResultFile();
        basicCsv.initializeResultFile();
        for (final File serializedFile : filesToDeSerialize) {

            final List<TestResultHolder> resultsFromFile = new ArrayList<TestResultHolder>();
            FileInputStream fis = null;

            try {
                fis = new FileInputStream(serializedFile);
                final InputStream buffer = new BufferedInputStream(fis);
                final ObjectInputStream ois = new ObjectInputStream(buffer);
                while (true) {
                    resultsFromFile.add((TestResultHolder) ois.readObject());
                }
            } catch (final EOFException ignored) {
                // as expected
            } catch (final IOException e) {
                e.printStackTrace();
            } catch (final ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (fis != null)
                    try {
                        fis.close();
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
            }
            writeResults(resultsFromFile, serializedFile.getName());
            basicCsv.writeResults(resultsFromFile, serializedFile.getName());
        }
        closeResultFile();
        basicCsv.closeResultFile();
    }

    public void writeResults(final List<TestResultHolder> resultsFromFile, final String serializedFileName) {
        final Map<String, Boolean> testsStatus = new HashMap<String, Boolean>();
        final Map<String, String> failedTestDetails = new HashMap<String, String>();
        String testIdentifier, frameWorkFailure = "";

        boolean hasPassed;

        for (final TestResultHolder trh : resultsFromFile) {
            hasPassed = trh.haveAllTestsPassed();
            if (!trh.isFrameWorkOnly()) {
                testIdentifier = trh.getDirectionWithoutRows();
                if (!testsStatus.containsKey(testIdentifier)) {
                    testsStatus.put(testIdentifier, hasPassed);
                    if (!hasPassed) {
                        failedTestDetails.put(testIdentifier, trh.getSimplifiedResultSummary(COMMA));
                    }
                } else if (!trh.haveAllTestsPassed()) {
                    testsStatus.put(testIdentifier, false);
                    if (!failedTestDetails.containsKey(testIdentifier)) {
                        failedTestDetails.put(testIdentifier, trh.getSimplifiedResultSummary(COMMA));
                    } else {
                        failedTestDetails.put(testIdentifier,
                                updateSimplifiedSummary(failedTestDetails.get(testIdentifier), trh.getSimplifiedResultSummary(COMMA), COMMA));
                    }
                }
            } else {
                if (!hasPassed) {
                    frameWorkFailure = trh.getAllFailedFrameWorkDescriptions();
                }
            }
        }

        writeResultLine("------------------------------------------------------------------------------------");

        writeResultSubHeading(serializedFileName, frameWorkFailure);

        final Object[] keySet = testsStatus.keySet().toArray();

        final String[] sortedKeysByLength = sortKeysLexigraphically(keySet);

        for (final String tcIdentifier : sortedKeysByLength) {
            if (testsStatus.get(tcIdentifier)) {
                writeResultLine("Test Case Name: " + tcIdentifier);
                writeResultLine("Test Result: Passed");
                writeResultLine("");
            } else {
                writeResultLine("Test Case Name: " + tcIdentifier);
                writeResultLine("Test Result: Failed" + "\tFailure Reason: " + failedTestDetails.get(tcIdentifier));
                writeResultLine("");
            }
        }

    }

    public static String[] sortKeysLexigraphically(final Object[] keySet) {
        final String[] sortingArray = new String[keySet.length];
        for (int i = 0; i < keySet.length; i++) {
            sortingArray[i] = keySet[i].toString();
        }

        Arrays.sort(sortingArray);

        return sortingArray;
    }

    public static String updateSimplifiedSummary(String existingTestDetails, final String proposedAdditions, final String textSeparator) {
        final String[] proposedResults = proposedAdditions.split(textSeparator);
        for (final String proposedResult : proposedResults) {
            if (!existingTestDetails.contains(proposedResult)) {
                existingTestDetails += textSeparator + proposedResult;
            }
        }

        return existingTestDetails;
    }

    public void writeResultSubHeading(final String serializedFileName, final String frameWorkFailure) {
        writeResultLine("");
        writeResultLine(HtmlGenerator.convertSerFileNameToResultFileName(serializedFileName, false));
        if (frameWorkFailure.length() > 0) {
            writeResultLine(frameWorkFailure);
        }
        writeResultLine("");
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

    public void writeHeading() {
        String timeString = "Times Run: ";
        writeResultLine(SummaryWriter.initializeSummaryFileHeading());
        for (final int time : times) {
            timeString += time + ",";
        }
        timeString = StringUtils.strip(timeString, ",");
        writeResultLine(timeString);
        writeResultLine("CSV Tests On: " + isCsv.toString());
        writeResultLine("");
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
