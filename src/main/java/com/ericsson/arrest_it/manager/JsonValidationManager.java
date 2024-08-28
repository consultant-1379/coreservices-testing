package com.ericsson.arrest_it.manager;

import java.util.*;

import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.arrest_it.common.CsvComparisonRow;
import com.ericsson.arrest_it.common.TestCase;
import com.ericsson.arrest_it.logging.LogbackFileUtils;
import com.ericsson.arrest_it.main.TestDriver;
import com.ericsson.arrest_it.results.CsvValidationResult;
import com.ericsson.arrest_it.results.JsonValidationResult;

public class JsonValidationManager implements Manager {

    private boolean validateCsv;
    private static final Logger slf4jLogger = LoggerFactory.getLogger(LogbackFileUtils.ARREST_IT_LOGGER);

    public JsonValidationManager() {
        this.validateCsv = TestDriver.SHOULDVALIDATECSV;
    }

    public void compareCsvAndGrid(final TestCase testCase) {

        if (testCase.getCsvUrl() != null && validateCsv == true) {
            checkCsvAndGridForErrors(testCase);
        } else {
            checkGridDataForErrors(testCase);
        }

        if (testCase.getData().length() == 0) {
            testCase.getTestResultHolder().addTestResult(new JsonValidationResult("No data present in Grid", false));
        }
    }

    private void checkCsvAndGridForErrors(final TestCase testCase) {

        checkCsvDataForErrors(testCase);
        checkGridDataForErrors(testCase);

        if (testCase.getData().length() == 0 && testCase.getCsvData().length() != 0) {
            checkCsvDataForErrors(testCase);
            testCase.getTestResultHolder().addTestResult(new JsonValidationResult("Data present in CSV but none/invalid in Grid", false));
        } else if (testCase.getCsvData().length() == 0 && testCase.getData().length() != 0) {
            checkGridDataForErrors(testCase);
            testCase.getTestResultHolder().addTestResult(new CsvValidationResult("Data present in Grid but none/invalid in CSV", false));
        }

        if (testCase.getCsvData().length() == 0) {
            testCase.getTestResultHolder().addTestResult(new JsonValidationResult("Data Not Present/Invalid in Grid and Csv", false));
        } else {
            testCase.getTestResultHolder().addTestResult(new JsonValidationResult("Data Present in Grid and Csv", true));
            compareDataInCsvAndGrid(testCase);
        }
    }

    private void checkGridDataForErrors(final TestCase testCase) {
        if (testCase.getData().length() > 0 && testCase.getData().getJSONObject(0).has("errorDescription")) {
            testCase.setData(new JSONArray());
        }
    }

    private void checkCsvDataForErrors(final TestCase testCase) {
        if (testCase.getCsvData().length() > 0 && testCase.getCsvData().getJSONObject(0).has("errorDescription")) {
            testCase.setCsvData(new JSONArray());
        }
    }

    private void compareDataInCsvAndGrid(final TestCase testCase) {
        final int urlMaxLength = testCase.getMaxRowCount();

        int gridSize = testCase.getData().length();
        final int csvSize = testCase.getCsvData().length();

        if (urlMaxLength > 0) {
            if (testCase.getData().length() > urlMaxLength) {
                gridSize = urlMaxLength;
            }
        }

        if (gridSize > csvSize) {
            testCase.getTestResultHolder().addTestResult(new CsvValidationResult("More Rows present in Grid than CSV export", false));
        } else if (gridSize < csvSize) {
            testCase.getTestResultHolder().addTestResult(
                    new CsvValidationResult("More Rows present in CSV Export than Grid (allowing for max rows), gridsize: " + gridSize + " csvsize: "
                            + csvSize, false));
        } else {
            testCase.getTestResultHolder().addTestResult(new CsvValidationResult("CSV and Grid size match (allowing for max rows)", true));
            matchLimitedRowsInGridToCsv(testCase);
        }
    }

    private void matchLimitedRowsInGridToCsv(final TestCase testCase) {
        final JSONArray gridData = testCase.getData();

        String gridElement = "";

        final ArrayList<String> timeIdentifiers = new ArrayList<String>();

        int noOfRowsToCheckAgainst = 40;
        int noOfRowsToCheck = 10;

        if (gridData.length() < 40) {
            noOfRowsToCheckAgainst = gridData.length();
        }

        if (gridData.length() < 10) {
            noOfRowsToCheck = gridData.length();
        }

        final JSONObject gridRow = gridData.getJSONObject(0);

        for (final Object objectKey : gridRow.keySet()) {
            gridElement = gridRow.getString(objectKey.toString());
            if (CsvComparisonRow.isTimeElement(gridElement)) {
                timeIdentifiers.add(objectKey.toString());
            }
        }

        performLimitedMatch(testCase, timeIdentifiers, noOfRowsToCheckAgainst, noOfRowsToCheck);
    }

    private void performLimitedMatch(final TestCase testCase, final List<String> timeIdentifiers, final int noOfRowsToCheckAgainst,
                                     final int noOfRowsToCheck) {

        final JSONArray gridData = testCase.getData();
        final JSONArray csvData = testCase.getCsvData();
        final List<CsvComparisonRow> gridRows = new ArrayList<CsvComparisonRow>();
        final List<CsvComparisonRow> csvRows = new ArrayList<CsvComparisonRow>();

        boolean hasPassed = true;

        for (int i = 0; i < noOfRowsToCheckAgainst; i++) {
            gridRows.add(returnRowToStringMinusMillisecondValues(gridData.getJSONObject(i), timeIdentifiers, testCase));
        }

        for (int i = 0; i < noOfRowsToCheck; i++) {
            csvRows.add(returnRowToStringMinusMillisecondValues(csvData.getJSONObject(i), timeIdentifiers, testCase));
        }

        for (final CsvComparisonRow csvRow : csvRows) {
            try {
                if (!gridRows.contains(csvRow)) {

                    testCase.getTestResultHolder().addTestResult(new CsvValidationResult(createRowNotFoundMessage(csvRow, gridRows.get(0)), false));
                    slf4jLogger.warn(createRowNotFoundMessage(csvRow, gridRows.get(0)));
                } else {
                    testCase.getTestResultHolder().addTestResult(
                            new CsvValidationResult("Csv Row Found in UI search : " + csvRow.getCsvGridRowText(), true));
                    slf4jLogger.warn("Csv Row Found in UI search : " + csvRow.getCsvGridRowText());

                }
            } catch (final IllegalArgumentException iae) {
                testCase.getTestResultHolder().addTestResult(
                        new CsvValidationResult("Row by Row Comparison Failure, column in csv or grid contains both time values and other values",
                                false));
                slf4jLogger.warn("Row by Row Comparison Failure, column in csv or grid contains both time values and other values");

            }
        }

    }

    private String createRowNotFoundMessage(final CsvComparisonRow csvRow, final CsvComparisonRow gridRow) {
        return "Searching for Csv row in UI Fail - Csv Row Not Found: " + csvRow.getCsvGridRowText();
    }

    private CsvComparisonRow returnRowToStringMinusMillisecondValues(final JSONObject gridOrCsvRow, final List<String> timeIdentifiers,
                                                                     final TestCase testCase) {
        String gridElement = "";
        String objectKeyString = "";
        boolean hasNoTimeElement = false;
        final List<Double> gridNumbers = new ArrayList<Double>();
        final List<String> gridStrings = new ArrayList<String>();

        if (timeIdentifiers.size() == 0) {
            hasNoTimeElement = true;
        }

        final List<String> rowKeys = new ArrayList<String>();
        for (final Object objectKey : gridOrCsvRow.keySet()) {
            rowKeys.add(objectKey.toString());
        }

        Collections.sort(rowKeys);

        for (final Object objectKey : rowKeys) {
            objectKeyString = objectKey.toString();
            gridElement = gridOrCsvRow.getString(objectKeyString);

            /* Condition has been changed in order to handle, when there are multiple time identifiers and also which are ignored in CSV*/
            if (testCase.getIgnoredInCsv().contains(Integer.parseInt(objectKeyString))) {
                gridElement = "";
             }
            else if (!hasNoTimeElement && timeIdentifiers.contains(objectKeyString)) {
                if (gridElement.contains(".")) {
                    gridElement = gridElement.substring(0, gridElement.indexOf("."));
                }
            }

            if (NumberUtils.isNumber(gridElement) && gridElement.length() < 32 && Double.parseDouble(gridElement) < 10000) {
                final Double gridElementAsNumber = Double.parseDouble(gridElement);
                gridNumbers.add((double) Math.round(gridElementAsNumber * 100) / 100);
                gridStrings.add("");
            } else {
                gridStrings.add(gridElement);
                gridNumbers.add(0.0);
            }
        }

        final CsvComparisonRow rowToReturn = new CsvComparisonRow(gridStrings, gridNumbers, gridOrCsvRow);
        return rowToReturn;
    }

    /**
     * @return the validateCsv whether CSV validation is on(true) or off (false)
     */
    public boolean isValidateCsv() {
        return validateCsv;
    }

    /**
     * @param validateCsv
     *        whether CSV validation is swithed on (true) or off (false)
     */
    public void setValidateCsv(final boolean validateCsv) {
        this.validateCsv = validateCsv;
    }
}
