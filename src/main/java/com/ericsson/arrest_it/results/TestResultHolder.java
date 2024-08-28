package com.ericsson.arrest_it.results;

import static com.ericsson.arrest_it.common.Constants.*;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.lang3.StringUtils;

import com.ericsson.arrest_it.common.TestCase;

public class TestResultHolder implements Serializable {

    private static final long serialVersionUID = 8698016431780894810L;

    private String testTitle;
    private String direction;
    private String directionWithoutRows;
    private int timeRange;
    private Map<String, String> parentValues;
    private String testType;
    private String url;
    private String csvUrl;
    private boolean frameWorkOnly = false;
    private List<TestResult> frameWorkResults;
    private List<TestResult> jsonNetworkResults;
    private List<TestResult> jsonParsingResults;
    private List<TestResult> jsonValidationResults;
    private List<TestResult> csvNetworkResults;
    private List<TestResult> csvParsingResults;
    private List<TestResult> csvValidationResults;

    public TestResultHolder(final TestCase testCase) {
        this.frameWorkOnly = false;
        this.setDirection(testCase.getDirection());
        this.setDirectionWithoutRows(testCase.getDirectionWithOutRowInfo());
        this.setTimeRange(testCase.getTime());
        this.setTestTitle(testCase.getTitle());
        this.setTestType(testCase.getTestType());
        this.setParentValues(testCase.getParentValues());
        this.setUrl(testCase.getUrl());
        this.setCsvUrl(testCase.getCsvUrl());
        setFrameWorkResults(new ArrayList<TestResult>());
        setJsonNetworkResults(new ArrayList<TestResult>());
        setJsonParsingResults(new ArrayList<TestResult>());
        setJsonValidationResults(new ArrayList<TestResult>());
        setCsvNetworkResults(new ArrayList<TestResult>());
        setCsvParsingResults(new ArrayList<TestResult>());
        setCsvValidationResults(new ArrayList<TestResult>());
    }

    public TestResultHolder(final String fileName, final List<TestResult> frameWorkResults) {
        this.frameWorkOnly = true;
        this.testTitle = fileName;
        this.frameWorkResults = frameWorkResults;
    }

    public boolean isFrameWorkOnly() {
        return frameWorkOnly;
    }

    public void setFrameWorkOnly(final boolean frameWorkOnly) {
        this.frameWorkOnly = frameWorkOnly;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(final String direction) {
        this.direction = direction;
    }

    public int getTimeRange() {
        return timeRange;
    }

    public void setTimeRange(final int timeRange) {
        this.timeRange = timeRange;
    }

    public List<TestResult> getFrameWorkResults() {
        return frameWorkResults;
    }

    public void setFrameWorkResults(final List<TestResult> frameWorkResults) {
        this.frameWorkResults = frameWorkResults;
    }

    public void addFrameWorkResult(final FrameWorkResult fwr) {
        this.frameWorkResults.add(fwr);
    }

    public List<TestResult> getJsonNetworkResults() {
        return jsonNetworkResults;
    }

    public void setJsonNetworkResults(final List<TestResult> jsonNetworkResults) {
        this.jsonNetworkResults = jsonNetworkResults;
    }

    public void addJsonNetworkResult(final JsonNetworkResult jnr) {
        this.jsonNetworkResults.add(jnr);
    }

    public List<TestResult> getJsonParsingResults() {
        return jsonParsingResults;
    }

    public void setJsonParsingResults(final List<TestResult> jsonParsingResults) {
        this.jsonParsingResults = jsonParsingResults;
    }

    public void addJsonParsingResult(final JsonParsingResult jpr) {
        this.jsonParsingResults.add(jpr);
    }

    public List<TestResult> getJsonValidationResults() {
        return jsonValidationResults;
    }

    public void setJsonValidationResults(final List<TestResult> jsonValidationResults) {
        this.jsonValidationResults = jsonValidationResults;
    }

    public void addJsonValidationResult(final JsonValidationResult jvr) {
        this.jsonValidationResults.add(jvr);
    }

    public List<TestResult> getCsvNetworkResults() {
        return csvNetworkResults;
    }

    public void setCsvNetworkResults(final List<TestResult> csvNetworkResults) {
        this.csvNetworkResults = csvNetworkResults;
    }

    public void addCsvNetworkResult(final CsvNetworkResult cnr) {
        this.csvNetworkResults.add(cnr);
    }

    public List<TestResult> getCsvParsingResults() {
        return csvParsingResults;
    }

    public void setCsvParsingResults(final List<TestResult> csvParsingResults) {
        this.csvParsingResults = csvParsingResults;
    }

    public void addCsvParsingResult(final CsvParsingResult cpr) {
        this.csvParsingResults.add(cpr);
    }

    public List<TestResult> getCsvValidationResults() {
        return csvValidationResults;
    }

    public void setCsvValidationResults(final List<TestResult> csvValidationResults) {
        this.csvValidationResults = csvValidationResults;
    }

    public void addCsvValidationResult(final CsvValidationResult cvr) {
        this.csvValidationResults.add(cvr);
    }

    public String getTestTitle() {
        if (testTitle.contains(":")) {
            return testTitle.substring(0, testTitle.indexOf(":"));
        }

        return testTitle;
    }

    public void setTestTitle(final String testTitle) {
        this.testTitle = testTitle;
    }

    public Map<String, String> getParentValues() {
        return parentValues;
    }

    public void setParentValues(final Map<String, String> parentValues) {
        this.parentValues = parentValues;
    }

    public String getTestType() {
        return testType;
    }

    public void setTestType(final String testType) {
        this.testType = testType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getCsvUrl() {
        return csvUrl;
    }

    public void setCsvUrl(final String csvUrl) {
        this.csvUrl = csvUrl;
    }

    public void addTestResult(final TestResult testRes) {
        if (testRes instanceof CsvNetworkResult) {
            this.csvNetworkResults.add(testRes);
        } else if (testRes instanceof CsvParsingResult) {
            this.csvParsingResults.add(testRes);
        } else if (testRes instanceof CsvValidationResult) {
            this.csvValidationResults.add(testRes);
        } else if (testRes instanceof JsonNetworkResult) {
            this.jsonNetworkResults.add(testRes);
        } else if (testRes instanceof JsonParsingResult) {
            this.jsonParsingResults.add(testRes);
        } else if (testRes instanceof JsonValidationResult) {
            this.jsonValidationResults.add(testRes);
        } else if (testRes instanceof FrameWorkResult) {
            this.frameWorkResults.add(testRes);
        }
    }

    public boolean haveAllTestsPassed() {

        for (final TestResult tr : frameWorkResults) {
            if (!tr.isPassed()) {
                return false;
            }
        }
        for (final TestResult tr : csvNetworkResults) {
            if (!tr.isPassed()) {
                return false;
            }
        }
        for (final TestResult tr : csvParsingResults) {
            if (!tr.isPassed()) {
                return false;
            }
        }
        for (final TestResult tr : csvValidationResults) {
            if (!tr.isPassed()) {
                return false;
            }
        }
        for (final TestResult tr : jsonNetworkResults) {
            if (!tr.isPassed()) {
                return false;
            }
        }
        for (final TestResult tr : jsonParsingResults) {
            if (!tr.isPassed()) {
                return false;
            }
        }
        for (final TestResult tr : jsonValidationResults) {
            if (!tr.isPassed()) {
                return false;
            }
        }

        return true;
    }

    public String getSimplifiedResultSummary(final String textSeparator) {
        String simplifiedSummary = "";
        for (final TestResult tr : frameWorkResults) {
            if (!tr.isPassed()) {
                simplifiedSummary = updateSimplifiedSummary(simplifiedSummary, timeRange + ": " + UNSPECIFIED_FRAMEWORK_FAILURE, textSeparator);
            }
        }
        for (final TestResult tr : csvNetworkResults) {
            if (!tr.isPassed()) {
                simplifiedSummary = updateSimplifiedSummary(simplifiedSummary, timeRange + ": " + CANNOT_OBTAIN_CSV, textSeparator);
            }
        }
        for (final TestResult tr : csvParsingResults) {
            if (!tr.isPassed()) {
                simplifiedSummary = updateSimplifiedSummary(simplifiedSummary, timeRange + ": " + CANNOT_PARSE_CSV, textSeparator);
            }
        }
        for (final TestResult tr : csvValidationResults) {
            if (!tr.isPassed()) {
                if (tr.getResultText() != null && tr.getResultText().contains("No data present")) {
                    simplifiedSummary = updateSimplifiedSummary(simplifiedSummary, timeRange + ": " + NO_DATA_CSV, textSeparator);
                } else {
                    simplifiedSummary = updateSimplifiedSummary(simplifiedSummary, timeRange + ": " + CSV_DATA_MISMATCH, textSeparator);
                }
            }
        }
        for (final TestResult tr : jsonNetworkResults) {
            if (!tr.isPassed()) {
                simplifiedSummary = updateSimplifiedSummary(simplifiedSummary, timeRange + ": " + CANNOT_OBTAIN_UI, textSeparator);
            }
        }
        for (final TestResult tr : jsonParsingResults) {
            if (!tr.isPassed()) {
                simplifiedSummary = updateSimplifiedSummary(simplifiedSummary, timeRange + ": " + CANNOT_PARSE_UI, textSeparator);
            }
        }
        for (final TestResult tr : jsonValidationResults) {
            if (!tr.isPassed()) {
                if (tr.getResultText() != null && tr.getResultText().contains("No data present")) {
                    simplifiedSummary = updateSimplifiedSummary(simplifiedSummary, timeRange + ": " + NO_DATA_UI, textSeparator);
                } else {
                    simplifiedSummary = updateSimplifiedSummary(simplifiedSummary, timeRange + ": " + UI_MISMATCH, textSeparator);
                }
            }
        }
        if (simplifiedSummary.endsWith(textSeparator)) {
            simplifiedSummary = StringUtils.strip(simplifiedSummary, textSeparator);
        }
        return simplifiedSummary;
    }

    public String updateSimplifiedSummary(String simplifiedSummary, final String resultToAdd, final String textSeparator) {
        if (!simplifiedSummary.contains(resultToAdd)) {
            simplifiedSummary += resultToAdd + textSeparator;
        }

        return simplifiedSummary;
    }

    public Map<String, Double> getIndividualStatistics(final String identifier) {
        final Map<String, Double> stats = new HashMap<String, Double>();
        double passed = 0, failed = 0, total = 0, percPassed = 0;
        List<TestResult> testsOfInterest = new ArrayList<TestResult>();
        if (identifier.equals(CSV_NETWORK)) {
            testsOfInterest = csvNetworkResults;
        } else if (identifier.equals(CSV_PARSING)) {
            testsOfInterest = csvParsingResults;
        } else if (identifier.equals(CSV_VALIDATION)) {
            testsOfInterest = csvValidationResults;
        } else if (identifier.equals(JSON_NETWORK)) {
            testsOfInterest = jsonNetworkResults;
        } else if (identifier.equals(JSON_PARSING)) {
            testsOfInterest = jsonParsingResults;
        } else if (identifier.equals(JSON_VALIDATION)) {
            testsOfInterest = jsonValidationResults;
        } else if (identifier.equals(FRAMEWORK)) {
            testsOfInterest = frameWorkResults;
        }
        if (testsOfInterest != null) {
            for (final TestResult tr : testsOfInterest) {

                if (tr.isPassed() && tr.getTestTotals() == null) {
                    passed++;
                    total++;
                } else if (!tr.isPassed() && tr.getTestTotals() == null) {
                    failed++;
                    total++;
                }
            }
            stats.put(TOTAL_TESTS, total);
            stats.put(PASSED_TESTS, passed);
            stats.put(FAILED_TESTS, failed);
            percPassed = passed / total * 100;
            stats.put(PERCENTAGE_PASSED, (double) Math.round(percPassed * 100) / 100);
        } else {
            stats.put(TOTAL_TESTS, 0.0);
            stats.put(PASSED_TESTS, 0.0);
            stats.put(FAILED_TESTS, 0.0);
        }
        return stats;
    }

    public Map<String, Double> getTotalStatistics() {
        final Map<String, Double> stats = new HashMap<String, Double>();
        Map<String, Double> tempStats = new HashMap<String, Double>();
        final String[] listsOfResults = { CSV_NETWORK, CSV_PARSING, CSV_VALIDATION, JSON_NETWORK, JSON_PARSING, JSON_VALIDATION, FRAMEWORK };
        double allTotal = 0, allFailed = 0, allPercentage = 0, allPassed = 0;

        for (final String listName : listsOfResults) {
            tempStats = getIndividualStatistics(listName);

            allTotal += tempStats.get(TOTAL_TESTS);
            allFailed += tempStats.get(FAILED_TESTS);
            allPassed += tempStats.get(PASSED_TESTS);

        }
        stats.put(TOTAL_TESTS, allTotal);
        stats.put(FAILED_TESTS, allFailed);
        stats.put(PASSED_TESTS, allPassed);
        allPercentage = allPassed / allTotal * 100;
        stats.put(PERCENTAGE_PASSED, (double) Math.round(allPercentage * 100) / 100);
        return stats;
    }

    public List<TestResult> returnOneTypeOfResults(final String identifier) {

        if (identifier.equals(CSV_NETWORK)) {
            return csvNetworkResults;
        } else if (identifier.equals(CSV_PARSING)) {
            return csvParsingResults;
        } else if (identifier.equals(CSV_VALIDATION)) {
            return csvValidationResults;
        } else if (identifier.equals(JSON_NETWORK)) {
            return jsonNetworkResults;
        } else if (identifier.equals(JSON_PARSING)) {
            return jsonParsingResults;
        } else if (identifier.equals(JSON_VALIDATION)) {
            return jsonValidationResults;
        } else {
            return frameWorkResults;
        }
    }

    public String getAllFailedFrameWorkDescriptions() {
        String allResults = "";

        for (final TestResult tr : frameWorkResults) {
            if (!tr.isPassed()) {
                if (tr.getResultText() != null) {
                    allResults += tr.getResultText() + ",";
                } else if (tr.getTestsNotRun() != null) {
                    allResults += "Could not run the following tests(Time:Testcase Direction): ";
                    for (final String testNotRun : tr.getTestsNotRun()) {
                        allResults += "\n" + testNotRun;
                    }
                }
            }
        }

        allResults = StringUtils.strip(allResults, ",");

        return allResults;
    }

    public List<String> getAllTestsNotRun() {

        for (final TestResult tr : frameWorkResults) {
            if (!tr.isPassed()) {
                if (tr.getTestsNotRun() != null) {
                    return tr.getTestsNotRun();
                }
            }

        }
        return null;
    }

    public Map<String, String> getFrameWorkOnlyFailedResultsTexts() {
        final Map<String, String> results = new HashMap<String, String>();

        for (final TestResult tr : frameWorkResults) {
            if (!tr.isPassed()) {
                if (tr.getResultText() != null && tr.getFileName() != null) {
                    results.put(tr.getFileName(), tr.getResultText());
                }
            }

        }
        return results;
    }

    public String haveAllCSVResultsPassed() {

        if (csvNetworkResults.isEmpty() && csvParsingResults.isEmpty() && csvValidationResults.isEmpty()) {
            return NOT_APPLICABLE_IDENTIFIER;
        }

        for (final TestResult tr : csvNetworkResults) {
            if (!tr.isPassed()) {
                return FAILED_IDENTIFIER;
            }
        }

        for (final TestResult tr : csvParsingResults) {
            if (!tr.isPassed()) {
                return FAILED_IDENTIFIER;
            }
        }

        for (final TestResult tr : csvValidationResults) {
            if (!tr.isPassed()) {
                return FAILED_IDENTIFIER;
            }
        }
        return PASSED_IDENTIFIER;
    }

    public String haveAllUiTestsPassed() {

        for (final TestResult tr : jsonNetworkResults) {
            if (!tr.isPassed()) {
                return FAILED_IDENTIFIER;
            }
        }

        for (final TestResult tr : jsonParsingResults) {
            if (!tr.isPassed()) {
                return FAILED_IDENTIFIER;
            }
        }

        for (final TestResult tr : jsonValidationResults) {
            if (!tr.isPassed()) {
                return FAILED_IDENTIFIER;
            }
        }

        for (final TestResult tr : frameWorkResults) {
            if (!tr.isPassed()) {
                return FAILED_IDENTIFIER;
            }
        }
        return PASSED_IDENTIFIER;
    }

    public String getDirectionWithoutRows() {
        return directionWithoutRows;
    }

    public void setDirectionWithoutRows(final String directionWithoutRows) {
        this.directionWithoutRows = directionWithoutRows;
    }
}
