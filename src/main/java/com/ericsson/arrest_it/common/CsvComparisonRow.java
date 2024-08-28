package com.ericsson.arrest_it.common;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONObject;

/**
 * This object is used for comparing UI rows to CSV rows. Because CSV rows might not contain all columns of UI rows, each is processed as a text array
 * and numerical array to allow comparison.
 */
public class CsvComparisonRow {
    private List<String> stringsInRow;
    private List<Double> numbersInRow;
    private String csvGridRowText;

    public CsvComparisonRow(final List<String> stringsInRow, final List<Double> numbersInRow, final JSONObject gridOrCsvRow) {
        this.stringsInRow = stringsInRow;
        this.numbersInRow = numbersInRow;
        this.csvGridRowText = getRowText(gridOrCsvRow);
    }

    public List<String> getStringsInRow() {
        return stringsInRow;
    }

    public void setStringsInRow(final List<String> stringsInRow) {
        this.stringsInRow = stringsInRow;
    }

    public List<Double> getNumbersInRow() {
        return numbersInRow;
    }

    public void setNumbersInRow(final List<Double> numbersInRow) {
        this.numbersInRow = numbersInRow;
    }

    public String getCsvGridRowText() {
        return csvGridRowText;
    }

    public void setCsvRowText(final String csvGridRowText) {
        this.csvGridRowText = csvGridRowText;
    }

    public String getRowText(final JSONObject csvOrGridRow) {
        String rowText = "";

        final List<String> rowKeys = new ArrayList<String>();
        for (final Object objectKey : csvOrGridRow.keySet()) {
            rowKeys.add(objectKey.toString());
        }

        Collections.sort(rowKeys);
        for (String key : rowKeys) {
            rowText += csvOrGridRow.getString(key.toString()) + ",";
        }
        return rowText;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((numbersInRow == null) ? 0 : numbersInRow.hashCode());
        result = prime * result + ((stringsInRow == null) ? 0 : stringsInRow.hashCode());
        return result;
    }

    /**
     * Allows comparison of RowForCSV objects when text and numerical arrays are compared. Differences in UI and CSV null/empty references are
     * accounted for.
     * 
     */
    @Override
    public boolean equals(final Object obj) throws IllegalStateException {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final CsvComparisonRow other = (CsvComparisonRow) obj;
        boolean isEquals = true;

        if (numbersInRow == null) {
            if (other.numbersInRow != null) {
                isEquals = false;
            }
        } else if (!numbersInRow.equals(other.numbersInRow)) {
            isEquals = false;
        }

        if (stringsInRow == null) {
            if (other.stringsInRow != null) {
                isEquals = false;
            }
        } else if (stringsInRow.size() != other.stringsInRow.size()) {
            isEquals = false;
        } else {
            String myValue = "", otherValue = "";
            for (int i = 0; i < stringsInRow.size(); i++) {
                myValue = stringsInRow.get(i);
                otherValue = other.stringsInRow.get(i);
                if (!myValue.equals(otherValue)) {
                    if (!((myValue.equals("") || myValue.equals("null")) && (otherValue.equals("") || otherValue.equals("null")))) {
                        isEquals = false;
                    }
                }
                if (isTimeElement(myValue)) {
                    if (!compareEventTimes(myValue, otherValue)) {
                        isEquals = false;
                    }
                }
            }
        }

        return isEquals;
    }

    public boolean compareEventTimes(final String timeA, final String timeB) throws IllegalArgumentException {
        DateTime dateTimeA = null, dateTimeB = null;
        final int sybaseBuffer = 1000;

        dateTimeA = DateTime.parse(timeA, DateTimeFormat.forPattern("dd-MM-yyyy HH:mm:ss"));
        dateTimeB = DateTime.parse(timeB, DateTimeFormat.forPattern("dd-MM-yyyy HH:mm:ss"));

        final long result = dateTimeA.getMillis() - dateTimeB.getMillis();

        if (Math.abs(result) > sybaseBuffer) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean isTimeElement(final String gridElement) {
        final String patternString = "([0-9]{4}[-][0-9]{2}[-][0-9]{2}[ ][0-9]{2}[:][0-9]{2}[:][0-9]{2}[.][0-9]+)";
        final Pattern p = Pattern.compile(patternString);
        final Matcher m = p.matcher(gridElement);

        return m.matches();
    }
}
