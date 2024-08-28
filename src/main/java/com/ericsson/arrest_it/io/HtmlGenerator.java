package com.ericsson.arrest_it.io;

import static com.ericsson.arrest_it.common.Constants.*;

import java.util.*;

public class HtmlGenerator {
    private static final String DIV_OPEN_DETAILED_SUMMARY = "<div class=\"testsummaryB\">";
    private static final String DIV_CLOSE = "</div>";
    private static final String TABLE_OPEN = "<table>";
    private static final String TABLE_CLOSE = "</table>";
    private static final String TABLE_ROW_WITH_STYLE_OPEN = "<tr style=\"background-color: #eaebec\">";
    private static final String TABLE_ROW_OPEN = "<tr>";
    private static final String TABLE_ROW_CLOSE = "</tr>";
    private static final String TABLE_HEADING_OPEN = "<th>";
    private static final String TABLE_HEADING_CLOSE = "</th>";
    private static final String TABLE_DIVISION_OPEN = "<td>";
    private static final String TABLE_DIVISION_CLOSE = "</td>";
    private static final String TYPE_TEXT_OPEN = "<tt>";
    private static final String TYPE_TEXT_CLOSE = "</tt>";
    private static final String PERCENTAGE_PASSED = "Percentage Passed";
    private static final String DIV_OPEN_FAIL_RESULTS = "<div class=\"failtable\">";
    private static final String FAIL_FILES = "Files Which Did Not Run All Tests due to Data Inavailability";
    private static final String INCOMPLETE_FILES = "Files Which Did Not Run All Tests";
    private static final String PARAGRAPH_OPEN_WITH_RESULT_TAG_CLASS = "<p class=\"resultTag\">";
    private static final String PARAGRAPH_CLOSE = "</p>";
    private static final String RESULTID_HOLDER = "result1";
    private static final String A_TAG_WITH_REF_OPEN = "<a href=\"#" + RESULTID_HOLDER + "\" class=\"resultRef\">";
    private static final String A_TAG_CLOSE = "</a>";
    private static final String A_TAG_FOR_CLOSING_OPEN = "<a href=\"#closeResult\" class=\"resultClose\">";
    private static final String IMAGE_TAG = "<img src=\"button.jpg\" alt=\"-\" height=\"12.5\" width=\"12.5\">";
    private static final String DIV_OPEN_WITH_RESULT_ID = "<div id=\"" + RESULTID_HOLDER + "\" class=\"resultTable\">";
    private static final String TABLE_DIVISION_WITH_CLASS = "<td class=\"resultTableHead\">";

    public List<String> generateDetailedSummary(final Map<String, Double> detailedValues, final String[] typesOfTests) {
        final List<String> htmlToReturn = new ArrayList<String>();
        final String[] identifiers = { ALL_TESTS_IDENTIFIER, PASSED_IDENTIFIER, FAILED_IDENTIFIER, PERCENTAGE_PASSED };
        double passed, total, percPassed;

        htmlToReturn.add(DIV_OPEN_DETAILED_SUMMARY);
        htmlToReturn.add(TABLE_OPEN);

        for (final String typeOfTest : typesOfTests) {
            passed = detailedValues.get(typeOfTest + PASSED_IDENTIFIER);
            total = detailedValues.get(typeOfTest + ALL_TESTS_IDENTIFIER);
            percPassed = calculatePercPassed(total, passed);
            detailedValues.put(typeOfTest + PERCENTAGE_PASSED, percPassed);

            htmlToReturn.add(TABLE_ROW_WITH_STYLE_OPEN);

            for (final String identifier : identifiers) {
                htmlToReturn.add(TABLE_HEADING_OPEN);
                htmlToReturn.add(typeOfTest + " " + identifier);
                htmlToReturn.add(TABLE_HEADING_CLOSE);
            }

            htmlToReturn.add(TABLE_ROW_CLOSE);
            htmlToReturn.add(TABLE_ROW_OPEN);

            for (final String identifier : identifiers) {
                htmlToReturn.add(TABLE_DIVISION_OPEN);
                htmlToReturn.add(TYPE_TEXT_OPEN);
                htmlToReturn.add(String.valueOf(detailedValues.get(typeOfTest + identifier)));
                htmlToReturn.add(TYPE_TEXT_CLOSE);
                htmlToReturn.add(TABLE_DIVISION_CLOSE);
            }

            htmlToReturn.add(TABLE_ROW_CLOSE);

        }

        htmlToReturn.add(DIV_CLOSE);
        htmlToReturn.add(TABLE_CLOSE);
        htmlToReturn.add(DIV_CLOSE);

        return htmlToReturn;
    }

    public double calculatePercPassed(final double total, final double passed) {
        final double percPassed = (passed / total) * 100;
        return (double) Math.round(percPassed * 100) / 100;
    }

    public List<String> generateFailedResultsLists(final List<String> failedFiles, final List<String> testsNotMetFiles) {
        final Map<String, List<String>> listHolder = new HashMap<String, List<String>>();
        listHolder.put(FAIL_FILES, failedFiles);
        listHolder.put(INCOMPLETE_FILES, testsNotMetFiles);

        final List<String> htmlToReturn = new ArrayList<String>();
        final String[] typesOfResults = { FAIL_FILES, INCOMPLETE_FILES };
        htmlToReturn.add(DIV_OPEN_FAIL_RESULTS);
        htmlToReturn.add(TABLE_OPEN);
        int htmlResultId = 0;

        for (final String resultType : typesOfResults) {
            htmlToReturn.add(TABLE_ROW_OPEN);
            htmlToReturn.add(TABLE_DIVISION_OPEN);
            htmlToReturn.add(PARAGRAPH_OPEN_WITH_RESULT_TAG_CLASS);
            htmlToReturn.add(addResultIdToTag(A_TAG_WITH_REF_OPEN, htmlResultId));
            htmlToReturn.add(resultType);
            htmlToReturn.add(A_TAG_CLOSE);
            htmlToReturn.add(A_TAG_FOR_CLOSING_OPEN);
            htmlToReturn.add(IMAGE_TAG);
            htmlToReturn.add(A_TAG_CLOSE);
            htmlToReturn.add(PARAGRAPH_CLOSE);
            htmlToReturn.add(addResultIdToTag(DIV_OPEN_WITH_RESULT_ID, htmlResultId));
            htmlResultId++;
            htmlToReturn.add(TABLE_OPEN);
            final List<String> resultIdentifiers = listHolder.get(resultType);

            for (final String resultIdentifier : resultIdentifiers) {
                htmlToReturn.add(TABLE_ROW_OPEN);
                htmlToReturn.add(TABLE_DIVISION_WITH_CLASS);
                htmlToReturn.add("File: ");
                htmlToReturn.add(TABLE_DIVISION_CLOSE);
                htmlToReturn.add(TABLE_DIVISION_OPEN);
                htmlToReturn.add("<a href=\"" + convertSerFileNameToResultFileName(resultIdentifier, true) + "\">");
                htmlToReturn.add(convertSerFileNameToResultFileName(resultIdentifier, true));
                htmlToReturn.add(A_TAG_CLOSE);
                htmlToReturn.add(TABLE_DIVISION_CLOSE);
                htmlToReturn.add(TABLE_ROW_CLOSE);
            }

            htmlToReturn.add(TABLE_CLOSE);
            htmlToReturn.add(DIV_CLOSE);
            htmlToReturn.add(TABLE_DIVISION_CLOSE);
            htmlToReturn.add(TABLE_ROW_CLOSE);

        }

        htmlToReturn.add(TABLE_CLOSE);
        htmlToReturn.add(DIV_CLOSE);

        return htmlToReturn;
    }

    public static String convertSerFileNameToResultFileName(String serFileName, final boolean failedOnly) {
        if (serFileName.contains(INTERIM_FILE_NAME + "_")) {
            serFileName = serFileName.replace(INTERIM_FILE_NAME + "_", "");
        }

        if (serFileName.contains(SERIALIZATION_EXTENSION)) {
            if (failedOnly) {
                serFileName = serFileName.replace(SERIALIZATION_EXTENSION, FAILED_FILE + TXT_EXTENSION);
            } else {
                serFileName = serFileName.replace(SERIALIZATION_EXTENSION, ALL_TESTS_FILE + TXT_EXTENSION);
            }

        }
        return serFileName;
    }

    public String addResultIdToTag(String htmlLine, final int resultId) {
        htmlLine = htmlLine.replace(RESULTID_HOLDER, RESULTID_HOLDER + resultId);
        return htmlLine;
    }

}
