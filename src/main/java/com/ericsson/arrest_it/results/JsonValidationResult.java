package com.ericsson.arrest_it.results;


public class JsonValidationResult extends TestResult {

    private static final long serialVersionUID = -7695314450524615382L;

    public JsonValidationResult(final String textResult, final boolean passed) {
        super(textResult, passed);
    }

    public JsonValidationResult(final String textResult, final String rowIdentifier, final boolean passed) {
        super(textResult, rowIdentifier, passed);
    }
}
