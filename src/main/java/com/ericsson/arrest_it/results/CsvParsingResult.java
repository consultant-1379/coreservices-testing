package com.ericsson.arrest_it.results;

import static com.ericsson.arrest_it.common.Constants.*;

import org.json.JSONArray;

public class CsvParsingResult extends TestResult {

    private static final long serialVersionUID = -8965312036364831263L;

    public CsvParsingResult(final String textResult, final boolean passed) {
        super(textResult, passed);
    }

    public void getResultFromJsonArray(final JSONArray resultJson) {
        if (resultJson.length() > 0 && resultJson.getJSONObject(0).has(ERROR_DESCRIPTION)) {
            this.setResultText(resultJson.getJSONObject(0).getString(ERROR_DESCRIPTION));
        } else {
            this.setResultText("Unknown CSV parsing error");
        }
    }
}
