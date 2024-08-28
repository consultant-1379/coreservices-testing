package com.ericsson.arrest_it.io;

import static com.ericsson.arrest_it.common.Constants.*;

import java.io.*;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.ericsson.arrest_it.results.TestResult;
import com.ericsson.arrest_it.results.TestResultHolder;

/**
 * This class summarises all the results which have been serialized in the interim folder. Produces a html results page.
 *
 */
public class SummaryWriter {

    private final String interimFolder;
    private final String resultFileName;
    private final List<String> filesWithFailedTests;
    private final List<String> filesWhichDidntRunAllTests;
    private final boolean csvValidationOn;
    private String times;
    private final String tzOffset;
    Map<String, Double> results;
    private long failedValidations = 0;
    private long passedValidations = 0;
    private long totalTestCases = 0;
    private long passedTestCases = 0;
    private long failedTestCases = 0;
    private long drillsAttempted = 0;
    private long drillsFailed = 0;
    private long uniqueTestsAttempted = 0;
    private long uniqueTestsSuccessful = 0;
    private long noOfEmptyGrids = 0;
    private static final String HTMLTEMPLATE = RELATIVE_PATH + HTML_TEMPLATE;
    private BufferedWriter summaryWriter;
    private List<String> htmlBeginning;
    private List<String> htmlBody;
    private final String docHeading;
    private final HtmlGenerator htmlGenerator;

    /**
     * Main method which can be called from python execution script. In this scenario the basic summary classes are also called.
     * 
     * @param args
     *        [timezone, comma separated time values, isCsvValidation]
     */
    public static void main(final String[] args) {
        SummaryWriter sw = null;
        BasicSummaryWriter bsw = null;

        try {
            final String tzOffset = args[0];
            final String[] times = args[1].split(",");
            final String csv = args[2];

            final int[] timesNumbers = new int[times.length];

            for (int i = 0; i < times.length; i++) {
                timesNumbers[i] = Integer.parseInt(times[i]);
            }
            bsw = new BasicSummaryWriter(Boolean.parseBoolean(csv), timesNumbers, tzOffset);
            sw = new SummaryWriter(Boolean.parseBoolean(csv), timesNumbers, tzOffset);

        } catch (final Exception e) {
            System.out.println("Invalid Arguements given to run Summary Writer, check properties file");
            e.printStackTrace();
        }

        if (sw != null) {
            sw.deserializeResults();
            bsw.deserializeResults();
        } else {
            System.out.println("Could not create Summary Writer object, no summary file will be written");
        }

    }

    public SummaryWriter(final boolean isCsv, final int[] timesArray, final String tzOffset) {
        this.interimFolder = RELATIVE_PATH + SEPARATOR + INTERIM_FILE_FOLDER;
        String now = new DateTime().toString();
        now = now.substring(0, now.indexOf("."));
        now = now.replaceAll(":", "-");
        this.resultFileName = RELATIVE_PATH + SEPARATOR + RESULTS_FOLDER + SEPARATOR + SUMMARY_FILE + now + HTML_EXTENSION;
        this.filesWithFailedTests = new ArrayList<String>();
        this.filesWhichDidntRunAllTests = new ArrayList<String>();
        this.csvValidationOn = isCsv;
        this.tzOffset = tzOffset;
        this.htmlGenerator = new HtmlGenerator();
        initializeTimes(timesArray);
        initializeResultsMap();
        docHeading = initializeSummaryFileHeading();
    }

    public void initializeTimes(final int[] timesArray) {
        String timeString = "";
        for (final int i : timesArray) {
            timeString += String.valueOf(i) + ",";
        }

        times = StringUtils.strip(timeString, ",");
    }

    public static String initializeSummaryFileHeading() {
        final DateTime now = new DateTime();
        // Make sure we use English month names
        final DateTimeFormatter formatter = DateTimeFormat.forPattern("EEEE d MMMM yyyy").withLocale(Locale.US);
        final String dateText = formatter.print(now);

        return "Arrest-IT Results " + dateText;

    }

    public void initializeResultsMap() {
        results = new HashMap<String, Double>();
        final String[] resultTypes = getArrayOfResultTypes();

        for (final String resultType : resultTypes) {
            results.put(resultType + PASSED_IDENTIFIER, 0.0);
            results.put(resultType + FAILED_IDENTIFIER, 0.0);
            results.put(resultType + ALL_TESTS_IDENTIFIER, 0.0);
        }
    }

    public void deserializeResults() {

        final List<File> filesToDeSerialize = getFilesFromInterimFolder(interimFolder);
        for (final File serializedFile : filesToDeSerialize) {
            System.out.println("FileName " + serializedFile.getName());
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
            addResultsToSummary(resultsFromFile, serializedFile.getName());
        }

        writeResults();
    }

    public void addResultsToSummary(final List<TestResultHolder> resultsFromFile, final String fileName) {
        boolean hasReachedAllTests = true;
        boolean countedUniqueTests = false;

        double tempPassed, tempFailed, tempTotal, failedInFile = 0;

        Map<String, Double> tempResults = null;

        final String[] typesOfTestsPresent = getArrayOfResultTypes();

        for (final TestResultHolder trh : resultsFromFile) {
            tempResults = trh.getTotalStatistics();
            passedValidations += tempResults.get(PASSED_TESTS);
            failedValidations += tempResults.get(FAILED_TESTS);

            for (final String typeOfTest : typesOfTestsPresent) {

                tempResults = trh.getIndividualStatistics(typeOfTest);
                tempPassed = results.get(typeOfTest + PASSED_IDENTIFIER);
                tempFailed = results.get(typeOfTest + FAILED_IDENTIFIER);
                tempTotal = results.get(typeOfTest + ALL_TESTS_IDENTIFIER);
                tempPassed += tempResults.get(PASSED_TESTS);
                tempFailed += tempResults.get(FAILED_TESTS);
                tempTotal += tempResults.get(TOTAL_TESTS);
                failedInFile += tempResults.get(FAILED_TESTS);
                results.put(typeOfTest + PASSED_IDENTIFIER, tempPassed);
                results.put(typeOfTest + FAILED_IDENTIFIER, tempFailed);
                results.put(typeOfTest + ALL_TESTS_IDENTIFIER, tempTotal);
            }

            if (trh.isFrameWorkOnly() && countedUniqueTests == false) {
                hasReachedAllTests = checkIfAllUniqueTestsMet(trh);
                countedUniqueTests = true;
            }

            if (trh.isFrameWorkOnly()) {
                incrementTotalTestCounters(trh);
            }
        }

        if (failedInFile > 0) {
            if (!(hasReachedAllTests == false && failedInFile == 1)) {
                filesWithFailedTests.add(fileName);
            }
        }
        if (hasReachedAllTests == false) {
            filesWhichDidntRunAllTests.add(fileName);
        }
    }

    public void incrementTotalTestCounters(final TestResultHolder trh) {
        for (final TestResult tr : trh.getFrameWorkResults()) {
            if (tr.getTestTotals() != null) {
                totalTestCases += tr.getTestTotals().get(TOTAL_TESTS);
                passedTestCases += tr.getTestTotals().get(PASSED_TESTS);
                failedTestCases += tr.getTestTotals().get(FAILED_TESTS);
                drillsAttempted += tr.getTestTotals().get(DRILLS_ATTEMPTED);
                drillsFailed += tr.getTestTotals().get(DRILLS_NOT_COMPLETED);
                noOfEmptyGrids += tr.getTestTotals().get(NO_OF_EMPTY_GRIDS);
            }
        }
    }

    public boolean checkIfAllUniqueTestsMet(final TestResultHolder trh) {
        for (final TestResult tr : trh.getFrameWorkResults()) {
            if (tr.getNoOfUniqueTests() != 0 && tr.isPassed() == false) {
                uniqueTestsAttempted += tr.getNoOfUniqueTests();
                uniqueTestsSuccessful += tr.getNoOfUniqueTests() - tr.getTestsNotRun().size();
                return false;
            } else if (tr.getNoOfUniqueTests() != 0) {
                uniqueTestsAttempted += tr.getNoOfUniqueTests();
                uniqueTestsSuccessful += tr.getNoOfUniqueTests();
                return true;
            }
        }
        return true;

    }

    public String[] getArrayOfResultTypes() {
        final String[] withCsv = { CSV_NETWORK, CSV_PARSING, CSV_VALIDATION, JSON_NETWORK, JSON_PARSING, JSON_VALIDATION, FRAMEWORK };
        final String[] withOutCsv = { JSON_NETWORK, JSON_PARSING, JSON_VALIDATION, FRAMEWORK };

        if (csvValidationOn) {
            return withCsv;
        } else {
            return withOutCsv;
        }
    }

    public static List<File> getFilesFromInterimFolder(final String interimFolder) {
        final File folder = new File(interimFolder);

        final List<File> files = new ArrayList<File>();

        for (final File fileInFolder : folder.listFiles()) {
            if (fileInFolder.getName().contains(SERIALIZATION_EXTENSION)) {
                files.add(fileInFolder);
                System.out.println("Adding file" + fileInFolder.getName());
            }
        }

        return files;
    }

    public void writeResults() {
        setUpOutputFile();
        setUpAndReadTemplateFile();
        addCssInfoToSummaryFile();
        writeResultsToSummaryFile();
        closeSummaryFile();
    }

    public void closeSummaryFile() {
        try {
            summaryWriter.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void writeResultsToSummaryFile() {

        for (final String htmlLine : htmlBody) {
            if (htmlLine.contains(HTML_RESULT_HEADING)) {
                writeLineToSummaryFile(addValuesToHtml(htmlLine, docHeading));
            } else if (htmlLine.contains(HTML_UNIQUE_GRIDS_ATTEMPTED)) {
                writeLineToSummaryFile(addValuesToHtml(htmlLine, String.valueOf(uniqueTestsAttempted)));
            } else if (htmlLine.contains(HTML_UNIQUE_GRIDS_TESTED)) {
                writeLineToSummaryFile(addValuesToHtml(htmlLine, String.valueOf(uniqueTestsSuccessful)));
            } else if (htmlLine.contains(HTML_TEMPORAL_VALUES_TESTED)) {
                writeLineToSummaryFile(addValuesToHtml(htmlLine, times));
            } else if (htmlLine.contains(HTML_TIME_ZONE_OFFSET)) {
                writeLineToSummaryFile(addValuesToHtml(htmlLine, tzOffset));
            } else if (htmlLine.contains(HTML_TOTAL_TEST_CASES)) {
                writeLineToSummaryFile(addValuesToHtml(htmlLine, String.valueOf(totalTestCases)));
            } else if (htmlLine.contains(HTML_TEST_CASES_PASSED)) {
                writeLineToSummaryFile(addValuesToHtml(htmlLine, String.valueOf(passedTestCases)));
            } else if (htmlLine.contains(HTML_TEST_CASES_FAILED)) {
                writeLineToSummaryFile(addValuesToHtml(htmlLine, String.valueOf(failedTestCases)));
            } else if (htmlLine.contains(HTML_PERCENTAGE_TESTS_PASSED)) {
                final double valsPercentage = ((double) passedTestCases / totalTestCases) * 100;
                writeLineToSummaryFile(addValuesToHtml(htmlLine, String.valueOf((double) Math.round(valsPercentage * 100) / 100)));
            } else if (htmlLine.contains(HTML_TOTAL_VALIDATIONS)) {
                writeLineToSummaryFile(addValuesToHtml(htmlLine, String.valueOf(passedValidations + failedValidations)));
            } else if (htmlLine.contains(HTML_TOTAL_VALIDATIONS_PASSED)) {
                writeLineToSummaryFile(addValuesToHtml(htmlLine, String.valueOf(passedValidations)));
            } else if (htmlLine.contains(HTML_TOTAL_VALIDATIONS_FAILED)) {
                writeLineToSummaryFile(addValuesToHtml(htmlLine, String.valueOf(failedValidations)));
            } else if (htmlLine.contains(HTML_PERCENTAGE_VALIDATIONS_PASSED)) {
                final long totalVals = passedValidations + failedValidations;
                System.out.println("These are the total validations " + totalVals);
                final double valsPercentage = ((double) passedValidations / totalVals) * 100;
                System.out.println("Passed Percentage: " + valsPercentage);
                writeLineToSummaryFile(addValuesToHtml(htmlLine, String.valueOf((double) Math.round(valsPercentage * 100) / 100)));
            } else if (htmlLine.contains(HTML_DRILLS_ATTEMPTED)) {
                writeLineToSummaryFile(addValuesToHtml(htmlLine, String.valueOf(drillsAttempted)));
            } else if (htmlLine.contains(HTML_DRILLS_FAILED)) {
                writeLineToSummaryFile(addValuesToHtml(htmlLine, String.valueOf(drillsFailed)));
            } else if (htmlLine.contains(HTML_PERCENTAGE_PRECONDITIONS)) {
                final double valsPercentage = ((double) (drillsAttempted - drillsFailed) / drillsAttempted) * 100;
                writeLineToSummaryFile(addValuesToHtml(htmlLine, String.valueOf((double) Math.round(valsPercentage * 100) / 100)));
            } else if (htmlLine.contains(HTML_EMPTY_URLS)) {
                writeLineToSummaryFile(addValuesToHtml(htmlLine, String.valueOf(noOfEmptyGrids)));
            }

            else
                writeLineToSummaryFile(htmlLine);
            if (htmlLine.contains(HTML_DETAILED_SUMMARY)) {
                for (final String line : htmlGenerator.generateDetailedSummary(results, getArrayOfResultTypes())) {
                    writeLineToSummaryFile(line);
                }
            } else if (htmlLine.contains(HTML_FAILED_RESULTS)) {
                for (final String line : htmlGenerator.generateFailedResultsLists(filesWithFailedTests, filesWhichDidntRunAllTests)) {
                    writeLineToSummaryFile(line);
                }
            }
        }
    }

    public String addValuesToHtml(String htmlLine, final String value) {
        final String htmlLineEnding = htmlLine.substring(htmlLine.indexOf("</") + 2);
        htmlLine = htmlLine.substring(0, htmlLine.indexOf(">") + 1) + value + "</" + htmlLineEnding;

        return htmlLine;
    }

    public void addCssInfoToSummaryFile() {
        for (final String line : htmlBeginning) {
            writeLineToSummaryFile(line);
        }
    }

    public void writeLineToSummaryFile(final String line) {
        try {
            summaryWriter.write(line);
            summaryWriter.newLine();
        } catch (final IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void setUpOutputFile() {

        final File summaryFile = new File(resultFileName);
        summaryFile.getParentFile().mkdirs();
        FileWriter fwSummary = null;
        try {
            summaryFile.createNewFile();
            fwSummary = new FileWriter(summaryFile.getAbsoluteFile());
        } catch (final IOException e) {
            e.printStackTrace();
        }
        summaryWriter = new BufferedWriter(fwSummary);
    }

    public void setUpAndReadTemplateFile() {
        String line = "";

        htmlBeginning = new ArrayList<String>();
        htmlBody = new ArrayList<String>();

        List<String> listToWrite = new ArrayList<String>();

        try {
            final BufferedReader br = new BufferedReader(new FileReader(HTMLTEMPLATE));
            try {
                while (br.ready()) {
                    line = br.readLine();
                    listToWrite.add(line);
                    if (line.contains("<body>")) {
                        htmlBeginning.addAll(listToWrite);
                        listToWrite = new ArrayList<String>();
                    }
                }
            } finally {
                br.close();
            }
            htmlBody = listToWrite;
        } catch (final FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
