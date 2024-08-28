package com.ericsson.arrest_it.common;

import java.util.Map;

public class Row {
    private Map<String, String> columnValue;

    public Row(final Map<String, String> columnValue) {
        this.columnValue = columnValue;
    }

    public Map<String, String> getColumnValue() {
        return columnValue;
    }

    public void setColumnValue(final Map<String, String> columnValue) {
        this.columnValue = columnValue;
    }

    public void addColumnValue(final String columnName, final String value) {
        this.columnValue.put(columnName, value);
    }
}
