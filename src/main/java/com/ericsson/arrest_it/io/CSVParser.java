package com.ericsson.arrest_it.io;

import java.util.*;

import org.json.*;

import com.ericsson.arrest_it.common.TestCase;
import com.ericsson.arrest_it.results.CsvParsingResult;

public class CSVParser {

    public JSONArray parseCsvExport(final List<String> csvLines, final TestCase testCase) {
        boolean hasIgnored = false;

        if (csvLines.size() > 1) {
            csvLines.remove(0);
        } else {
            System.out.println("Empty CSV returned");
            return new JSONArray();
        }

        if (!testCase.getIgnoredInCsv().isEmpty()) {
            hasIgnored = true;
        }

        JSONArray dataJson;

        if (csvLines.size() > 0) {
            final StringBuilder jsonStringBuilder = new StringBuilder("[");
            for (final String line : csvLines) {
                final String[] elements = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

                final List<String> elementsList = new ArrayList<String>(Arrays.asList(elements));
                if (hasIgnored) {
                    for (final int elementToIgnore : testCase.getIgnoredInCsv()) {
                        try {
                            elementsList.add(elementToIgnore - 1, "");
                        } catch (final IndexOutOfBoundsException e) {
                            return (handleException(e, "Invalid CSV Row:  " + elementsList.toString(), testCase));
                        }

                    }
                }

                jsonStringBuilder.append("{");
                for (int i = 0; i < elementsList.size(); i++) {

                    if (elementsList.get(i).contains("NULL")) {
                        elementsList.set(i, "");
                    }
                    if (elementsList.get(i).contains("\"")) {
                        jsonStringBuilder.append("\"" + (i + 1) + "\":" + elementsList.get(i));
                    } else {
                        jsonStringBuilder.append("\"" + (i + 1) + "\":\"" + elementsList.get(i) + "\"");
                    }

                    if (i != elementsList.size() - 1) {
                        jsonStringBuilder.append(",");
                    }
                }
                jsonStringBuilder.append("},");
            }
            jsonStringBuilder.deleteCharAt(jsonStringBuilder.length() - 1);

            jsonStringBuilder.append("]");
            final String csvForJson = jsonStringBuilder.toString();

            try {
                dataJson = new JSONArray(csvForJson);
                testCase.getTestResultHolder().addTestResult(new CsvParsingResult("Successfully parsed CSV data", true));
            } catch (final JSONException je) {
                dataJson = handleException(je, csvForJson, testCase);
                je.printStackTrace();
            }

        } else {
            dataJson = new JSONArray();
        }

        return dataJson;
    }

    JSONArray handleException(final Exception genericException, final String csvForJson, final TestCase testCase) {
        final JSONArray errorJsonArray = new JSONArray();
        final JSONObject errorJsonObject = new JSONObject();

        final String stackTrace = genericException.getMessage();
        final String unterminatedStringMessage = "Unterminated string at ";
        String charsInRange = "";
        if (genericException instanceof IndexOutOfBoundsException) {
            errorJsonObject.append("errorDescription", csvForJson);
            errorJsonArray.put(errorJsonObject);
            testCase.getTestResultHolder().addTestResult(
                    new CsvParsingResult("Could not parse CSV, index out of bound exception \n " + csvForJson, false));
            return errorJsonArray;
        }
        if (stackTrace.contains(unterminatedStringMessage)) {
            final int firstIndex = stackTrace.indexOf(unterminatedStringMessage) + unterminatedStringMessage.length();
            final int secondIndex = stackTrace.indexOf("[", firstIndex);
            final String charValue = stackTrace.substring(firstIndex, secondIndex).trim();
            final int charIndex = Integer.parseInt(charValue);

            for (int i = 0; i < csvForJson.length(); i++) {
                if (i > charIndex - 200 && i < charIndex + 200) {
                    charsInRange += Character.toString(csvForJson.charAt(i));
                }
            }
            errorJsonObject.append("errorDescription", "Unterminated String error, could not parse CSV to JSON: " + charsInRange);
            testCase.getTestResultHolder().addTestResult(
                    new CsvParsingResult("Could not parse CSV, Unterminated String exception \n " + charsInRange, false));
            errorJsonArray.put(errorJsonObject);
        } else {
            String subsetOfCsvToPrint;
            if (csvForJson.length() < 201) {
                subsetOfCsvToPrint = csvForJson;
            } else {
                subsetOfCsvToPrint = csvForJson.substring(0, 200);
            }

            errorJsonObject.append("errorDescription", "Could not parse csv to Json: " + subsetOfCsvToPrint);
            testCase.getTestResultHolder().addTestResult(new CsvParsingResult("Could not parse CSV:  \n" + subsetOfCsvToPrint, false));
            errorJsonArray.put(errorJsonObject);
        }

        return errorJsonArray;
    }

}
