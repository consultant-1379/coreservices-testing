package com.ericsson.arrest_it.results;


public class CsvValidationResult extends TestResult {

    private static final long serialVersionUID = 134051099284749265L;

    public CsvValidationResult(final String textResult, final boolean passed) {
        super(textResult, passed);
    }
}
