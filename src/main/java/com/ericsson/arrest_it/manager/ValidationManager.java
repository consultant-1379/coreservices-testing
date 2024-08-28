package com.ericsson.arrest_it.manager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.arrest_it.common.*;
import com.ericsson.arrest_it.logging.LogbackFileUtils;
import com.ericsson.arrest_it.results.FrameWorkResult;
import com.ericsson.arrest_it.results.JsonValidationResult;

@SuppressWarnings("restriction")
public class ValidationManager implements Manager {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(LogbackFileUtils.ARREST_IT_LOGGER);

	private final DBValuesManager dbValuesManager;
	private final QueryManager queryManager;
	private final boolean shouldValidateCsv;
	private int noOfDecimalPlaces;

	public ValidationManager(final DBValuesManager dbValuesManager, final QueryManager queryManager, final boolean shouldValidateCsv) {
		this.dbValuesManager = dbValuesManager;
		this.queryManager = queryManager;
		this.shouldValidateCsv = shouldValidateCsv;
	}

	public void validateTestResults(final TestCase testCase, final List<Integer> rowIndices) {

		setMaxRows(testCase);

		for (final Validation validation : testCase.getValidations()) {
			this.noOfDecimalPlaces = validation.getNoOfDecimalPlaces();

			if (validation.isRepeating()) {

				for (final Integer rowIndex : rowIndices) {
					performValidation(validation, testCase, rowIndex);
				}
			} else {
				performValidation(validation, testCase, 0);
			}
		}
	}

	protected void performValidation(Validation validation, final TestCase testCase, final Integer rowIndex) {
		try {
			validation = splitEquation(validation);
			validation = replaceFunctionsWithValues(testCase, validation);
			validation.setRightSideOfEquation(replaceKeyVariablesWithValues(testCase, validation.getRightSideOfEquation(), rowIndex));
			validation.setLeftSideOfEquation(replaceKeyVariablesWithValues(testCase, validation.getLeftSideOfEquation(), rowIndex));

			slf4jLogger.info("Validation Equation After Enrichment: " + validation.getLeftSideOfEquation() + validation.getOperator()
					+ validation.getRightSideOfEquation());

			if (isNonNumericComparison(validation)) {
				final boolean testResult = isAlphabeticTestPassed(validation);
				addTestResult(testCase, rowIndex, validation, testResult);
				slf4jLogger.info("Validation Equation Passed: " + testResult);
			} else {
				validation.setLeftSideNumericalValue(performCalculation(validation.getLeftSideOfEquation()));
				validation.setRightSideNumericalValue(performCalculation(validation.getRightSideOfEquation()));
				final boolean testResult = isNumericTestPassed(validation, testCase);

				addNumericTestResult(testCase, rowIndex, validation, testResult);
				slf4jLogger.info("Validation Equation Passed: " + testResult);
			}
		} catch (final Exception e) {
			slf4jLogger.warn("Error occured during validation: " + validation.getEquation() + e.getMessage());
			testCase.getTestResultHolder().addTestResult(
					new FrameWorkResult("Could not perform validation " + validation.getEquation() + "\t" + e.getMessage(), false));
		}
	}

	boolean isNonNumericComparison(final Validation validation) {
		final String validationText = validation.getLeftSideOfEquation() + " " + validation.getRightSideOfEquation();
		return (validationText.matches(Constants.NON_NUMERIC_PATTERN) || validation.getLeftSideOfEquation().isEmpty() || validation
				.getRightSideOfEquation().isEmpty()) && !isScientificNotation(validationText);
	}

	boolean isScientificNotation(final String validationText) {
		return validationText.matches(Constants.SCIENTIFIC_NOTATION_PATTERN);
	}

	protected Validation splitEquation(final Validation validation) {
		slf4jLogger.info("Validation Equation Before: " + validation.getEquation());
		final String equation = validation.getEquation();

		final String regex = "(!=|>=|<=|>|<|==|=)";
		final String[] values = equation.split(regex);
		final String operator = equation.substring(values[0].length(), equation.length() - values[1].length()).trim();

		values[0] = values[0].trim();
		values[1] = values[1].trim();

		validation.setLeftSideOfEquation(values[0]);
		validation.setOperator(operator);
		validation.setRightSideOfEquation(values[1]);

		return validation;
	}

	protected Validation replaceFunctionsWithValues(final TestCase testCase, final Validation validation) {

		if (validation.getLeftSideOfEquation().matches(Constants.COUNT_REGEX)) {
			validation.setLeftSideOfEquation(replaceCountWithValue(validation.getLeftSideOfEquation(), testCase));
			validation.setLeftCountPresent(true);
		}

		if (validation.getLeftSideOfEquation().matches(Constants.SORT_REGEX)) {
			validation.setLeftSideOfEquation(replaceSortWithValue(validation.getLeftSideOfEquation(), testCase, validation.getRightSideOfEquation()));
		}

		if (validation.getLeftSideOfEquation().matches(Constants.SUM_REGEX)) {
			validation.setLeftSideOfEquation(replaceSumWithValue(validation.getLeftSideOfEquation(), testCase));
			validation.setSumOrAvgPresent(true);
		}

		if (validation.getLeftSideOfEquation().matches(Constants.AVERAGE_REGEX)) {
			validation.setLeftSideOfEquation(replaceAverageWithValue(validation.getLeftSideOfEquation(), testCase));
			validation.setSumOrAvgPresent(true);
		}

		if (validation.getRightSideOfEquation().matches(Constants.COUNT_REGEX)) {
			validation.setRightSideOfEquation(replaceCountWithValue(validation.getRightSideOfEquation(), testCase));
			validation.setRightCountPresent(true);
		}

		if (validation.getRightSideOfEquation().matches(Constants.SUM_REGEX)) {
			validation.setRightSideOfEquation(replaceSumWithValue(validation.getRightSideOfEquation(), testCase));
			validation.setSumOrAvgPresent(true);
		}

		if (validation.getRightSideOfEquation().matches(Constants.AVERAGE_REGEX)) {
			validation.setRightSideOfEquation(replaceAverageWithValue(validation.getRightSideOfEquation(), testCase));
			validation.setSumOrAvgPresent(true);
		}

		if (validation.getRightSideOfEquation().matches(Constants.SORT_REGEX)) {
			validation
			.setRightSideOfEquation(replaceSortWithValue(validation.getRightSideOfEquation(), testCase, validation.getLeftSideOfEquation()));
		}

		return validation;
	}

	private String performCount(final TestCase testCase) {
		if (shouldValidateCsv && testCase.getCsvUrl() != null) {
			return String.valueOf(testCase.getSizeOfCsvFile());
		} else {
			return String.valueOf(testCase.getData().length());
		}
	}

	private void setMaxRows(final TestCase testCase) {
		if (testCase.getMaxRowCount() <= 0) {
			testCase.setMaxRowCount(getMaxRowsFromUrl(testCase.getUrl()));
		}
	}

	public static int getMaxRowsFromUrl(String url) {

		url = url.substring(url.indexOf("maxRows=") + "maxRows=".length());

		if (!NumberUtils.isNumber(url)) {
			final int secondIndex = url.indexOf("&");
			url = url.substring(0, secondIndex);
		}

		if (NumberUtils.isNumber(url)) {
			return Integer.parseInt(url);
		}
		return Constants.DEFAULT_MAX_ROWS;
	}

	private String replaceCountWithValue(String oneSideOfEquation, final TestCase testCase) {
		int beginIndex = oneSideOfEquation.indexOf(Constants.COUNT_SEARCH_PATTERN), endIndex;
		String wordToReplace = "";

		while (beginIndex >= 0) {
			endIndex = oneSideOfEquation.indexOf(Constants.CLOSED_CURLY_BRACE, beginIndex) + 1;
			wordToReplace = oneSideOfEquation.substring(beginIndex, endIndex);
			oneSideOfEquation = oneSideOfEquation.replace(wordToReplace, performCount(testCase));
			beginIndex = oneSideOfEquation.indexOf(Constants.COUNT_SEARCH_PATTERN);
		}

		return oneSideOfEquation;
	}

	private String replaceSortWithValue(final String oneSideOfEquation, final TestCase testCase, final String expectedSortValue) {
		final int beginIndex = oneSideOfEquation.indexOf(Constants.SORT_SEARCH_PATTERN);
		int endIndex;
		endIndex = oneSideOfEquation.indexOf(Constants.CLOSED_CURLY_BRACE, beginIndex) + 1;
		final String wordToReplace = oneSideOfEquation.substring(beginIndex, endIndex);
		return oneSideOfEquation.replace(wordToReplace, obtainSortType(testCase, wordToReplace, expectedSortValue));
	}

	private String replaceSumWithValue(String oneSideOfEquation, final TestCase testCase) {
		int beginIndex = oneSideOfEquation.indexOf(Constants.SUM_SEARCH_PATTERN), endIndex;
		String wordToReplace = "";

		while (beginIndex >= 0) {
			endIndex = oneSideOfEquation.indexOf(Constants.CLOSED_CURLY_BRACE, beginIndex) + 1;
			wordToReplace = oneSideOfEquation.substring(beginIndex, endIndex);
			oneSideOfEquation = oneSideOfEquation.replace(wordToReplace, performSum(testCase, wordToReplace));
			beginIndex = oneSideOfEquation.indexOf(Constants.SUM_SEARCH_PATTERN);
		}

		return oneSideOfEquation;
	}

	private String replaceAverageWithValue(String oneSideOfEquation, final TestCase testCase) {
		int beginIndex = oneSideOfEquation.indexOf(Constants.AVG_SEARCH_PATTERN), endIndex;
		String wordToReplace = "";

		while (beginIndex >= 0) {
			endIndex = oneSideOfEquation.indexOf(Constants.CLOSED_CURLY_BRACE, beginIndex) + 1;
			wordToReplace = oneSideOfEquation.substring(beginIndex, endIndex);
			oneSideOfEquation = oneSideOfEquation.replace(wordToReplace, performAverage(testCase, wordToReplace));
			beginIndex = oneSideOfEquation.indexOf(Constants.AVG_SEARCH_PATTERN);
		}

		return oneSideOfEquation;
	}

	private String obtainSortType(final TestCase testCase, String key, final String expectedSortValue) {
		final JSONArray uiData = testCase.getData();
		key = stripKey(key);
		List<String> jsonColumn = new ArrayList<String>();
		final String columnIndexString = testCase.getJSONVariables().get(key);

		if (isEventTimeColumn(uiData, columnIndexString)) {
			jsonColumn = populateEventTimeColumn(uiData, columnIndexString, jsonColumn);
		} else {
			for (int rowIndex = 0; rowIndex < uiData.length(); rowIndex++) {
				final JSONObject uiRow = uiData.getJSONObject(rowIndex);
				jsonColumn.add(String.valueOf(uiRow.get(columnIndexString)));
			}
		}

		final List<String> columnAscending = new ArrayList<String>(jsonColumn);
		Collections.sort(columnAscending, new UiRankComparator());

		final List<String> columnDescending = new ArrayList<String>(jsonColumn);
		Collections.sort(columnDescending, new UiRankComparator());
		Collections.reverse(columnDescending);

		return compareJsonColumnWithSortedCopies(jsonColumn, columnAscending, columnDescending, expectedSortValue);
	}

	private List<String> populateEventTimeColumn(final JSONArray uiData, final String columnIndex, final List<String> jsonColumn) {
		final JSONObject firstUiRow = uiData.getJSONObject(0);
		final String firstRowValue = String.valueOf(firstUiRow.get(columnIndex));
		final int substringIndex = firstRowValue.lastIndexOf(":");

		for (int rowIndex = 0; rowIndex < uiData.length(); rowIndex++) {
			final JSONObject uiRow = uiData.getJSONObject(rowIndex);
			jsonColumn.add(String.valueOf(uiRow.get(columnIndex)).substring(0, substringIndex));
		}

		return jsonColumn;
	}

	private boolean isEventTimeColumn(final JSONArray uiData, final String columnIndexString) {
		final JSONObject uiRow = uiData.getJSONObject(0);
		final String firstRowValue = String.valueOf(uiRow.get(columnIndexString));

		return firstRowValue.matches(Constants.EVENT_TIME_REGEX);
	}

	private String compareJsonColumnWithSortedCopies(final List<String> jsonColumn, final List<String> columnAscending,
			final List<String> columnDescending, final String expectedSortValue) {

		if (columnAscending.equals(columnDescending) && columnAscending.equals(jsonColumn)) {
			return expectedSortValue;
		} else if (jsonColumn.equals(columnAscending)) {
			return Constants.ASCENDING_MARKER;
		} else if (jsonColumn.equals(columnDescending)) {
			return Constants.DESCENDING_MARKER;
		}

		return Constants.UNSORTED_MARKER;
	}

	private String performSum(final TestCase tc, String key) {
		String value;
		String tempValue;
		Double total = 0.0;
		final JSONArray ja = tc.getData();

		key = stripKey(key);

		final String columnIndexString = tc.getJSONVariables().get(key);

		for (int i = 0; i < ja.length(); i++) {
			final JSONObject jsonRow = ja.getJSONObject(i);
			tempValue = String.valueOf(jsonRow.get(columnIndexString));
			total += Double.parseDouble(tempValue);
		}

		value = String.valueOf(total);
		return value;
	}

	private String stripKey(String key) {
		if (key.contains(Constants.SUM_SEARCH_PATTERN)) {
			key = key.replace(Constants.SUM_SEARCH_PATTERN, Constants.EMPTY_STRING);
		} else if (key.contains(Constants.AVG_SEARCH_PATTERN)) {
			key = key.replace(Constants.AVG_SEARCH_PATTERN, Constants.EMPTY_STRING);
		} else if (key.contains(Constants.SORT_SEARCH_PATTERN)) {
			key = key.replace(Constants.SORT_SEARCH_PATTERN, Constants.EMPTY_STRING);
		}

		key = key.replace("}", "");

		return key;
}

	private String performAverage(final TestCase tc, String key) {
		String average;
		String tempValue;
		Double total = 0.0;
		final JSONArray ja = tc.getData();
		key = stripKey(key);

		final String columnIndexString = tc.getJSONVariables().get(key);

		for (int i = 0; i < ja.length(); i++) {
			final JSONObject jsonRow = ja.getJSONObject(i);
			tempValue = jsonRow.getString(columnIndexString);
			if (tempValue.contains("-")) {
				tempValue = "0";
			}
			total += Double.parseDouble(tempValue);
		}

		final int count = tc.getData().length();

		average = String.valueOf(total / count);
		return average;
	}

	protected String replaceKeyVariablesWithValues(final TestCase tc, String oneSideOfEquation, final Integer rowIndex) throws ArrestItException {

		if (oneSideOfEquation.matches(Constants.MASTER_KEY_REGEX)) {
			oneSideOfEquation = replaceMasterKeyWithValue(tc, oneSideOfEquation);
		}

		if (oneSideOfEquation.matches(Constants.PARENT_KEY_REGEX)) {
			oneSideOfEquation = replaceParentKeyWithValue(tc, oneSideOfEquation);
		}

		if (oneSideOfEquation.matches(Constants.SQL_KEY_REGEX)) {
			oneSideOfEquation = replaceSqlKeyWithValue(tc, oneSideOfEquation, rowIndex);
		}

		if (oneSideOfEquation.matches(Constants.JSON_KEY_REGEX)) {
			oneSideOfEquation = replaceJsonKeyWithValue(tc, oneSideOfEquation, rowIndex);
		}

		return oneSideOfEquation;
	}

	private String replaceMasterKeyWithValue(final TestCase tc, String equation) throws ArrestItException {
		final Pattern pattern = Pattern.compile(Constants.MASTER_REPLACE_REGEX);
		final Matcher matcher = pattern.matcher(equation);
		while (matcher.find()) {
			final String masterKey = matcher.group(0);
			final String sqlKeyValue = dbValuesManager.getSingleDbValue(tc, masterKey);
			equation = equation.replace(masterKey, sqlKeyValue);
		}

		return equation;
	}

	private String replaceSqlKeyWithValue(final TestCase tc, String equation, final Integer rowIndex) throws ArrestItException {
		final Pattern pattern = Pattern.compile(Constants.SQL_REPLACE_KEY);
		final Matcher matcher = pattern.matcher(equation);
		try {

			while (matcher.find()) {
				final String sqlKeyWithRowIndex = rowIndex + ":" + matcher.group(0);
				final String sqlKey = matcher.group(0);
				if (tc.getSqlVariables().containsKey(sqlKeyWithRowIndex)) {
					final String sqlKeyValue = tc.getSqlVariables().get(sqlKeyWithRowIndex);
					equation = equation.replace(sqlKey, sqlKeyValue);
				} else if (tc.getSqlVariables().containsKey(sqlKey)) {
					final String sqlKeyValue = tc.getSqlVariables().get(sqlKey);
					equation = equation.replace(sqlKey, sqlKeyValue);
				}
			}
		} catch (final NullPointerException e) {
			throw new ArrestItException(e.getMessage());
		}
		return equation;
	}

	private String replaceParentKeyWithValue(final TestCase tc, String equation) {
		final Pattern pattern = Pattern.compile(Constants.PARENT_REPLACE_REGEX);
		final Matcher matcher = pattern.matcher(equation);

		while (matcher.find()) {
			final String parenetKey = matcher.group(0);
			if (tc.getParentValues().containsKey(parenetKey)) {
				final String parentKeyValue = tc.getParentValues().get(parenetKey);
				equation = equation.replace(parenetKey, parentKeyValue);
			}
		}

		return equation;
	}

	private String replaceJsonKeyWithValue(final TestCase tc, String equation, final Integer rowIndex) {
		final Pattern pattern = Pattern.compile(Constants.JSON_VAR_REPLACE_REGEX);
		final Matcher matcher = pattern.matcher(equation);

		while (matcher.find()) {
			final String jsonKey = matcher.group(0);
			if (tc.getJSONVariables().containsKey(jsonKey)) {
				final String jsonKeyValue = queryManager.getJSONValue(tc, Integer.toString(rowIndex), jsonKey);
				equation = equation.replace(jsonKey, jsonKeyValue);
			}
		}

		return equation;
	}

	protected double performCalculation(String equation) throws ScriptException {
		final ScriptEngineManager mgr = new ScriptEngineManager();
		final ScriptEngine engine = mgr.getEngineByName("JavaScript");
		if (equation.equals("-")) {
			equation = "0";
		}
		return (Double) engine.eval(equation);
	}

	protected boolean isNumericTestPassed(final Validation validation, final TestCase testCase) {
		final double leftSideValue = round(validation.getLeftSideNumericalValue(), noOfDecimalPlaces);
		final double rightSideValue = round(validation.getRightSideNumericalValue(), noOfDecimalPlaces);
		final String operator = validation.getOperator();

		if (shouldPassForSumOrAvg(validation, testCase)) {
			return true;
		}

		if (shouldUpdateValuesForCount(validation, testCase)) {
			return isTestPassedWithCountConsideration(validation, operator, leftSideValue, rightSideValue, testCase);
		}

		if (operator.equals("=") || operator.equals("==")) {
			if (leftSideValue == rightSideValue) {
				return true;
			}
		} else if (operator.equals("!=")) {
			if (leftSideValue != rightSideValue) {
				return true;
			}
		} else if (operator.equals(">")) {
			if (leftSideValue > rightSideValue) {
				return true;
			}
		} else if (operator.equals("<")) {
			if (leftSideValue < rightSideValue) {
				return true;
			}
		} else if (operator.equals(">=")) {
			if (leftSideValue >= rightSideValue) {
				return true;
			}
		} else if (operator.equals("<=")) {
			if (leftSideValue <= rightSideValue) {
				return true;
			}
		}
		return false;
	}

	protected static double round(final double value, final int places) {
		if (places < 0) {
			throw new IllegalArgumentException();
		}

		if (isAlmost100(value)) {
			return 99.99;
		} else if (isAlmostZero(value)) {
			return 0.01;
		}

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	private static boolean isAlmostZero(final double value) {
		return value > 0 && value < 0.005;
	}

	private static boolean isAlmost100(final double value) {
		return value >= 99.995 && value < 100;
	}

	protected boolean isAlphabeticTestPassed(final Validation validation) {
		String leftSideValue = validation.getLeftSideOfEquation().trim();
		String rightSideValue = validation.getRightSideOfEquation().trim();
		final String operator = validation.getOperator().trim();
		
		if (leftSideValue.equals("EMPTY")  && StringUtils.isEmpty(rightSideValue))
		{
			leftSideValue = "";
		}
		if (rightSideValue.equals("EMPTY")  && StringUtils.isEmpty(leftSideValue))
		{
			rightSideValue = "";
		}
		
		
		if (operator.equals("=") || operator.equals("==")) {

			if (isVendorNameEricsson(leftSideValue, rightSideValue)) {
				return true;
			}

			if (leftSideValue.equals(rightSideValue)) {
				return true;
			}
		} else if (operator.equals("!=")) {
			if (!leftSideValue.equals(rightSideValue)) {
				return true;
			}

		}

		return false;
	}

	private boolean isVendorNameEricsson(final String leftSide, final String rightSide) {

		boolean isVendorEricsson = false;

		if (leftSide.equalsIgnoreCase("ericsson") && rightSide.equalsIgnoreCase("ericsson")) {

			isVendorEricsson = true;
		}

		return isVendorEricsson;
	}

	private void addNumericTestResult(final TestCase tc, final Integer rowIndex, final Validation validation, final boolean testResult) {
		final String resultString = validation.getEquation() + "\t" + String.valueOf(validation.getLeftSideNumericalValue()) + " "
				+ validation.getOperator() + String.valueOf(validation.getRightSideNumericalValue());

		final String rowIndexString = checkRowIndexValue(rowIndex);

		tc.getTestResultHolder().addTestResult(new JsonValidationResult(resultString, rowIndexString, testResult));
	}

	private void addTestResult(final TestCase tc, final Integer rowIndex, final Validation validation, final boolean testResult) {
		final String resultString = validation.getEquation() + "\t" + validation.getLeftSideOfEquation() + " " + validation.getOperator() + " "
				+ validation.getRightSideOfEquation();

		final String rowIndexString = checkRowIndexValue(rowIndex);

		tc.getTestResultHolder().addTestResult(new JsonValidationResult(resultString, rowIndexString, testResult));
	}

	private String checkRowIndexValue(final int rowIndex) {
		if (rowIndex == -1) {
			return Constants.NON_REPEAT_VALIDATION_FLAG;
		} else {
			return String.valueOf(rowIndex);
		}
	}

	private boolean shouldUpdateValuesForCount(final Validation validation, final TestCase testCase) {
		if (!validation.isLeftCountPresent() && !validation.isRightCountPresent()) {
			return false;
		}

		if (shouldValidateCsv && testCase.getCsvUrl() != null) {
			return false;
		}

		if (testCase.getData().length() < testCase.getMaxRowCount()) {
			return false;
		}

		return true;

	}

	private boolean shouldPassForSumOrAvg(final Validation validation, final TestCase testCase) {
		if (validation.isSumOrAvgPresent()) {
			if (testCase.getData().length() >= testCase.getMaxRowCount()) {
				return true;
			}
		}

		return false;
	}

	private boolean isTestPassedWithCountConsideration(final Validation validation, final String operator, final Double leftNumber,
			final Double rightNumber, final TestCase testCase) {

		if (validation.isRightCountPresent() && validation.isLeftCountPresent()) {
			slf4jLogger.warn("User Attempting to compare count values from same window, Validation: " + validation.getEquation() + "\tTestCase: "
					+ testCase.getDirection());
			return true;
		}

		if (validation.isLeftCountPresent()) {
			if (operator.equals(">")) {
				if (rightNumber <= leftNumber) {
					return false;
				}
			} else if (operator.equals(">=") || operator.equals("=") || operator.equals("==") || operator.equals("!=")) {
				if (rightNumber < leftNumber) {
					return false;
				}
			}
		} else if (validation.isRightCountPresent()) {
			if (operator.equals("<")) {
				if (rightNumber <= leftNumber) {
					return false;
				}
			} else if (operator.equals("<=") || operator.equals("=") || operator.equals("==") || operator.equals("!=")) {
				if (rightNumber > leftNumber) {
					return false;
				}
			}
		}
		slf4jLogger.info("Default to true because row count limit met, Validation: " + validation.getEquation() + "\tTestCase: "
				+ testCase.getDirection());
		return true;
	}
}
