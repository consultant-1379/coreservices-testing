package com.ericsson.arrest_it.manager;

import static org.junit.Assert.*;

import javax.script.ScriptException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.*;

import com.ericsson.arrest_it.common.*;
import com.ericsson.arrest_it.io.PropertyReader;
import com.ericsson.arrest_it.results.TestResultHolder;

@SuppressWarnings("restriction")
public class ValidationManagerTest {
    private TestCase testCase;
    private DBValuesManager dbValuesManager;
    private QueryManager queryManager;
    private ValidationManager validationManager;
    private static String testServerAddress;

    @BeforeClass
    public static void setupClass() {
        testServerAddress = PropertyReader.getInstance().getDbServerURL();
    }

    @Before
    public void setUp() {
        final DBManager db = new DBManager(testServerAddress, "dc", "dc", "2640");
        final TimeManager timeManager = new TimeManager();
        timeManager.setMainTime();
        testCase = new TestCase();
        dbValuesManager = new DBValuesManager(db, "username", timeManager);
        queryManager = new QueryManager(db, dbValuesManager, timeManager);

        validationManager = new ValidationManager(dbValuesManager, queryManager, true);
    }

    @Test
    public void test_splitEquation() {
        Validation validation = new Validation("a + b = x", true, 2);
        validation = validationManager.splitEquation(validation);
        assertEquals("a + b", validation.getLeftSideOfEquation());
        assertEquals("=", validation.getOperator());
        assertEquals("x", validation.getRightSideOfEquation());
    }

    @Test
    public void test_splitEquation_withJsonVariable() {
        Validation validation = new Validation("2 + 1.errors = 6", true, 2);
        validation = validationManager.splitEquation(validation);
        assertEquals("2 + 1.errors", validation.getLeftSideOfEquation());
        assertEquals("=", validation.getOperator());
        assertEquals("6", validation.getRightSideOfEquation());
    }

    @Test
    public void test_splitEquation_withMoreThanOneOpertaion() {
        Validation validation = new Validation("(2+1.errors)*2=12", true, 2);
        validation = validationManager.splitEquation(validation);
        assertEquals("(2+1.errors)*2", validation.getLeftSideOfEquation());
        assertEquals("=", validation.getOperator());
        assertEquals("12", validation.getRightSideOfEquation());
    }

    @Test
    public void test_splitEquation_lessThanOperation() {
        Validation validation = new Validation("(2+1.errors)*2<12", true, 2);
        validation = validationManager.splitEquation(validation);
        assertEquals("(2+1.errors)*2", validation.getLeftSideOfEquation());
        assertEquals("<", validation.getOperator());
        assertEquals("12", validation.getRightSideOfEquation());
    }

    @Test
    public void test_splitEquation_greaterThanOperation() {
        Validation validation = new Validation("(2+1.errors)*2>12", true, 2);
        validation = validationManager.splitEquation(validation);
        assertEquals("(2+1.errors)*2", validation.getLeftSideOfEquation());
        assertEquals(">", validation.getOperator());
        assertEquals("12", validation.getRightSideOfEquation());
    }

    @Test
    public void test_splitEquation_notEqualOperation() {
        Validation validation = new Validation("(2+1.errors)*2!=12", true, 2);
        validation = validationManager.splitEquation(validation);
        assertEquals("(2+1.errors)*2", validation.getLeftSideOfEquation());
        assertEquals("!=", validation.getOperator());
        assertEquals("12", validation.getRightSideOfEquation());
    }

    @Test
    public void test_splitEquation_lessThanOrEqualOperation() {
        Validation validation = new Validation("(2+1.errors)*2<=12", true, 2);
        validation = validationManager.splitEquation(validation);
        assertEquals("(2+1.errors)*2", validation.getLeftSideOfEquation());
        assertEquals("<=", validation.getOperator());
        assertEquals("12", validation.getRightSideOfEquation());
    }

    @Test
    public void test_splitEquation_greaterThanOrEqualOperation() {
        Validation validation = new Validation("(2+1.errors)*2>=12", true, 2);
        validation = validationManager.splitEquation(validation);
        assertEquals("(2+1.errors)*2", validation.getLeftSideOfEquation());
        assertEquals(">=", validation.getOperator());
        assertEquals("12", validation.getRightSideOfEquation());
    }

    @Test
    public void test_performCalculation() throws ScriptException {
        final double expected = 8;
        final double actual = validationManager.performCalculation("(2*2)+4");

        assertEquals(expected, actual, 0);
    }

    @Test
    public void test_performCalculation_dash() throws ScriptException {
        final double expected = 0;
        final double actual = validationManager.performCalculation("-");

        assertEquals(expected, actual, 0);
    }

    @Test
    public void test_performCalculation_scientificNotation() throws ScriptException {
        final double expected = 14258010;
        final double actual = validationManager.performCalculation("1.425801E7");

        assertEquals(expected, actual, 0);
    }

    @Test
    public void test_performCalculation_scientificNotationSum() throws ScriptException {
        final double expected = 14179935;
        final double actual = validationManager.performCalculation("24014.0 + 1.4155921E7");

        assertEquals(expected, actual, 0);
    }

    @Test
    public void test_compareNumericValues_equals_true_scientificNotation() {
        final Validation validation = new Validation("", true, 2);
        validation.setLeftSideNumericalValue(14258010);
        validation.setRightSideNumericalValue(1.425801E7);
        validation.setOperator("=");
        final boolean actual = validationManager.isNumericTestPassed(validation, testCase);
        assertTrue(actual);
    }

    @Test
    public void test_compareNumericValues_equals_true() {
        final Validation validation = new Validation("", true, 2);
        validation.setLeftSideNumericalValue(6);
        validation.setRightSideNumericalValue(6);
        validation.setOperator("=");
        final boolean actual = validationManager.isNumericTestPassed(validation, testCase);
        assertTrue(actual);
    }

    @Test
    public void test_compareNumericValues_doubleEquals_true() {
        final Validation validation = new Validation("", true, 2);
        validation.setLeftSideNumericalValue(6);
        validation.setRightSideNumericalValue(6);
        validation.setOperator("==");
        final boolean actual = validationManager.isNumericTestPassed(validation, testCase);
        assertTrue(actual);
    }

    @Test
    public void test_compareNumericValues_greaterThanAndEquals_true() {
        final Validation validation = new Validation("", true, 2);
        validation.setLeftSideNumericalValue(8);
        validation.setRightSideNumericalValue(6);
        validation.setOperator(">=");
        final boolean actual = validationManager.isNumericTestPassed(validation, testCase);
        assertTrue(actual);
    }

    @Test
    public void test_compareNumericValues_lessThanAndEquals_true() {
        final Validation validation = new Validation("", true, 2);
        validation.setLeftSideNumericalValue(4);
        validation.setRightSideNumericalValue(6);
        validation.setOperator("<=");
        final boolean actual = validationManager.isNumericTestPassed(validation, testCase);
        assertTrue(actual);
    }

    @Test
    public void test_compareNumericValues_greaterThan_true() {
        final Validation validation = new Validation("", true, 2);
        validation.setLeftSideNumericalValue(8);
        validation.setRightSideNumericalValue(6);
        validation.setOperator(">");
        final boolean actual = validationManager.isNumericTestPassed(validation, testCase);
        assertTrue(actual);
    }

    @Test
    public void test_compareNumericValues_lessThan_true() {
        final Validation validation = new Validation("", true, 2);
        validation.setLeftSideNumericalValue(1);
        validation.setRightSideNumericalValue(5);
        validation.setOperator("<");
        final boolean actual = validationManager.isNumericTestPassed(validation, testCase);
        assertTrue(actual);
    }

    @Test
    public void test_compareNumericValues_notEquals_true() {
        final Validation validation = new Validation("", true, 2);
        validation.setLeftSideNumericalValue(1);
        validation.setRightSideNumericalValue(5);
        validation.setOperator("!=");
        final boolean actual = validationManager.isNumericTestPassed(validation, testCase);
        assertTrue(actual);
    }

    @Test
    public void test_compareNumericValues_equals_false() {
        final Validation validation = new Validation("", true, 2);
        validation.setLeftSideNumericalValue(1);
        validation.setRightSideNumericalValue(5);
        validation.setOperator("=");
        final boolean actual = validationManager.isNumericTestPassed(validation, testCase);
        assertFalse(actual);
    }

    @Test
    public void test_compareNumericValues_doubleEquals_false() {
        final Validation validation = new Validation("", true, 2);
        validation.setLeftSideNumericalValue(9);
        validation.setRightSideNumericalValue(6);
        validation.setOperator("==");
        final boolean actual = validationManager.isNumericTestPassed(validation, testCase);
        assertFalse(actual);
    }

    @Test
    public void test_compareNumericValues_greaterThanAndEquals_false() {
        final Validation validation = new Validation("", true, 2);
        validation.setLeftSideNumericalValue(4);
        validation.setRightSideNumericalValue(6);
        validation.setOperator(">=");
        final boolean actual = validationManager.isNumericTestPassed(validation, testCase);
        assertFalse(actual);
    }

    @Test
    public void test_compareNumericValues_lessThanAndEquals_false() {
        final Validation validation = new Validation("", true, 2);
        validation.setLeftSideNumericalValue(9);
        validation.setRightSideNumericalValue(5);
        validation.setOperator("<=");
        final boolean actual = validationManager.isNumericTestPassed(validation, testCase);
        assertFalse(actual);
    }

    @Test
    public void test_compareNumericValues_greaterThan_false() {
        final Validation validation = new Validation("", true, 2);
        validation.setLeftSideNumericalValue(8);
        validation.setRightSideNumericalValue(8);
        validation.setOperator(">");
        final boolean actual = validationManager.isNumericTestPassed(validation, testCase);
        assertFalse(actual);
    }

    @Test
    public void test_compareNumericValues_lessThan_false() {
        final Validation validation = new Validation("", true, 2);
        validation.setLeftSideNumericalValue(5);
        validation.setRightSideNumericalValue(5);
        validation.setOperator("<");
        final boolean actual = validationManager.isNumericTestPassed(validation, testCase);
        assertFalse(actual);
    }

    @Test
    public void test_compareNumericValues_notEquals_false() {
        final Validation validation = new Validation("", true, 2);
        validation.setLeftSideNumericalValue(5);
        validation.setRightSideNumericalValue(5);
        validation.setOperator("!=");
        final boolean actual = validationManager.isNumericTestPassed(validation, testCase);
        assertFalse(actual);
    }

    @Test
    public void test_compareAlphabeticValues_notEquals_true() {
        final Validation validation = new Validation("", true, 2);
        validation.setLeftSideOfEquation("Nokia");
        validation.setRightSideOfEquation("Samsung");
        validation.setOperator("!=");
        final boolean actual = validationManager.isAlphabeticTestPassed(validation);
        assertTrue(actual);
    }

    @Test
    public void test_compareAlphabeticValues_equals_true() {
        final Validation validation = new Validation("", true, 2);
        validation.setLeftSideOfEquation("Nokia");
        validation.setRightSideOfEquation("Nokia");
        validation.setOperator("=");
        final boolean actual = validationManager.isAlphabeticTestPassed(validation);
        assertTrue(actual);
    }

    @Test
    public void test_compareAlphabeticValues_doubleEquals_true() {
        final Validation validation = new Validation("", true, 2);
        validation.setLeftSideOfEquation("Nokia");
        validation.setRightSideOfEquation("Nokia");
        validation.setOperator("==");
        final boolean actual = validationManager.isAlphabeticTestPassed(validation);
        assertTrue(actual);
    }

    @Test
    public void test_compareAlphabeticValues_notEquals_false() {
        final Validation validation = new Validation("", true, 2);
        validation.setLeftSideOfEquation("Nokia");
        validation.setRightSideOfEquation("Nokia");
        validation.setOperator("!=");
        final boolean actual = validationManager.isAlphabeticTestPassed(validation);
        assertFalse(actual);
    }

    @Test
    public void test_compareAlphabeticValues_equals_false() {
        final Validation validation = new Validation("", true, 2);
        validation.setLeftSideOfEquation("Nokia");
        validation.setRightSideOfEquation("Samsung");
        validation.setOperator("=");
        final boolean actual = validationManager.isAlphabeticTestPassed(validation);
        assertFalse(actual);
    }

    @Test
    public void test_compareAlphabeticValues_doubleEquals_false() {
        final Validation validation = new Validation("", true, 2);
        validation.setLeftSideOfEquation("Nokia");
        validation.setRightSideOfEquation("Samsung");
        validation.setOperator("==");
        final boolean actual = validationManager.isAlphabeticTestPassed(validation);
        assertFalse(actual);
    }

    @Test
    public void test_compareAlphabeticValues_ignoreVendorValueCase_equals_true() {
        final Validation validation = new Validation("", true, 2);
        validation.setLeftSideOfEquation("ERICSSON");
        validation.setRightSideOfEquation("ericsson");
        validation.setOperator("=");
        final boolean actual = validationManager.isAlphabeticTestPassed(validation);
        assertTrue(actual);
    }

    @Test
    public void test_compareAlphabeticValues_ignoreVendorValueCase_doubleEquals_true() {
        final Validation validation = new Validation("", true, 2);
        validation.setLeftSideOfEquation("ericsson");
        validation.setRightSideOfEquation("ERICSSON");
        validation.setOperator("==");
        final boolean actual = validationManager.isAlphabeticTestPassed(validation);
        assertTrue(actual);
    }

    @Test
    public void test_compareAlphabeticValues_vendorCaseSensitive_notEquals_true() {
        final Validation validation = new Validation("", true, 2);
        validation.setLeftSideOfEquation("ericsson");
        validation.setRightSideOfEquation("ERICSSON");
        validation.setOperator("!=");
        final boolean actual = validationManager.isAlphabeticTestPassed(validation);
        assertTrue(actual);
    }

    @Test
    public void test_compareNumericValues_scientificNotation_equals_true1() {
        final Validation validation = new Validation("", true, 2);
        validation.setLeftSideNumericalValue(6.0E-04);
        validation.setRightSideNumericalValue(0.0006);
        validation.setOperator("=");
        final boolean actual = validationManager.isNumericTestPassed(validation, testCase);
        assertTrue(actual);
    }

    @Test
    public void test_compareNumericValues_scientificNotation_equals_true2() {
        final Validation validation = new Validation("", true, 2);
        validation.setLeftSideNumericalValue(0.0006);
        validation.setRightSideNumericalValue(6.0E-04);
        validation.setOperator("=");
        final boolean actual = validationManager.isNumericTestPassed(validation, testCase);
        assertTrue(actual);
    }

    @Test
    public void test_compareNumericValues_scientificNotation_equals_true3() {
        final Validation validation = new Validation("", true, 2);
        validation.setLeftSideNumericalValue(6.0E-04);
        validation.setRightSideNumericalValue(6.0E-04);
        validation.setOperator("=");
        final boolean actual = validationManager.isNumericTestPassed(validation, testCase);
        assertTrue(actual);
    }

    @Test
    public void test_isNonNumericComparison_alphaNumerics1_true() {
        final Validation validation = new Validation("", true, 2);
        validation.setLeftSideOfEquation("0.0004");
        validation.setRightSideOfEquation("Sony Ericsson");
        final boolean actual = validationManager.isNonNumericComparison(validation);
        assertTrue(actual);
    }

    @Test
    public void test_isNonNumericComparison_alphaNumerics2_true() {
        final Validation validation = new Validation("", true, 2);
        validation.setLeftSideOfEquation("Sony");
        validation.setRightSideOfEquation("1.245");
        final boolean actual = validationManager.isNonNumericComparison(validation);
        assertTrue(actual);
    }

    @Test
    public void test_isNonNumericComparison_blank1_true() {
        final Validation validation = new Validation("", true, 2);
        validation.setLeftSideOfEquation("");
        validation.setRightSideOfEquation("0.23");
        final boolean actual = validationManager.isNonNumericComparison(validation);
        assertTrue(actual);
    }

    @Test
    public void test_isNonNumericComparison_blank2_true() {
        final Validation validation = new Validation("", true, 2);
        validation.setLeftSideOfEquation("");
        validation.setRightSideOfEquation("");
        final boolean actual = validationManager.isNonNumericComparison(validation);
        assertTrue(actual);
    }

    @Test
    public void test_isNonNumericComparison_numericsOnly_false() {

        final Validation validation = new Validation("", true, 2);
        validation.setLeftSideOfEquation("0.0004");
        validation.setRightSideOfEquation("1.02");
        final boolean actual = validationManager.isNonNumericComparison(validation);
        assertFalse(actual);
    }

    @Test
    public void test_isNonNumericComparison_scientificNotation_false() {
        final Validation validation = new Validation("", true, 2);
        validation.setLeftSideOfEquation("0.4");
        validation.setRightSideOfEquation("4.0E-4");
        final boolean actual = validationManager.isNonNumericComparison(validation);
        assertFalse(actual);
    }

    @Test
    public void test_isNonNumericComparison_scientificNotation_false2() {
        final Validation validation = new Validation("", true, 2);
        validation.setLeftSideOfEquation("24014.0 + 1.4155921E7");
        validation.setRightSideOfEquation("14179935");
        final boolean actual = validationManager.isNonNumericComparison(validation);
        assertFalse(actual);
    }

    @Test
    public void test_isScientificNotation_alphaNumerics_false() {
        final String equation = "1Avfv234 = 1Acfcd234";
        final boolean actual = validationManager.isScientificNotation(equation);
        assertFalse(actual);
    }

    @Test
    public void test_isScientificNotation_false() {
        final String equation = "Not.Applicable.for.Event.Type = Not.Applicable.for.Event.Type";
        final boolean actual = validationManager.isScientificNotation(equation);
        assertFalse(actual);
    }

    @Test
    public void test_isScientificNotation_scientificNotation_true() {
        final String equation = "1.0E-4 = 0.0001";
        final boolean actual = validationManager.isScientificNotation(equation);
        assertTrue(actual);
    }

    @Test
    public void test_replaceKeyVarablesWithValues_parentKeys() throws ArrestItException {
        testCase.addParentValue("2.errors", "6");

        final String oneSideOfEquation = "2.errors + 2";

        final String expected = "6 + 2";

        final String actual = validationManager.replaceKeyVariablesWithValues(testCase, oneSideOfEquation, 0);

        assertEquals(expected, actual);
    }

    @Test
    public void test_obtainDescendingSortValue() {
        final JSONArray data = new JSONArray();
        data.put(new JSONObject().put("1", "a"));
        data.put(new JSONObject().put("1", "b"));
        data.put(new JSONObject().put("1", "c"));

        testCase.setData(data);
        testCase.addJsonVariable("letters", "1");

        final String sortOrderToValidate = Constants.ASCENDING_MARKER;
        final String leftSideOfEquation = "sort{letters}";
        final Validation validation = new Validation("", true, 2);

        validation.setLeftSideOfEquation(leftSideOfEquation);
        validation.setRightSideOfEquation(sortOrderToValidate);
        validationManager.replaceFunctionsWithValues(testCase, validation);

        assertEquals(validation.getRightSideOfEquation(), sortOrderToValidate);
        assertEquals(validation.getLeftSideOfEquation(), sortOrderToValidate);
    }

    @Test
    public void test_obtainAscendingSortValue() {
        final JSONArray data = new JSONArray();
        data.put(new JSONObject().put("1", "c"));
        data.put(new JSONObject().put("1", "b"));
        data.put(new JSONObject().put("1", "a"));

        testCase.setData(data);
        testCase.addJsonVariable("letters", "1");

        final String sortOrderToValidate = Constants.DESCENDING_MARKER;
        final String leftSideOfEquation = "sort{letters}";
        final Validation validation = new Validation("", true, 2);

        validation.setLeftSideOfEquation(leftSideOfEquation);
        validation.setRightSideOfEquation(sortOrderToValidate);
        validationManager.replaceFunctionsWithValues(testCase, validation);

        assertEquals(validation.getRightSideOfEquation(), sortOrderToValidate);
        assertEquals(validation.getLeftSideOfEquation(), sortOrderToValidate);
    }

    @Test
    public void test_obtainUnsortedSortValue() {
        final JSONArray data = new JSONArray();
        data.put(new JSONObject().put("1", "b"));
        data.put(new JSONObject().put("1", "a"));
        data.put(new JSONObject().put("1", "c"));

        testCase.setData(data);
        testCase.addJsonVariable("letters", "1");

        final String sortOrderToValidate = Constants.UNSORTED_MARKER;
        final String leftSideOfEquation = "sort{letters}";
        final Validation validation = new Validation("", true, 2);

        validation.setLeftSideOfEquation(leftSideOfEquation);
        validation.setRightSideOfEquation(sortOrderToValidate);
        validationManager.replaceFunctionsWithValues(testCase, validation);

        assertEquals(validation.getRightSideOfEquation(), sortOrderToValidate);
        assertEquals(validation.getLeftSideOfEquation(), sortOrderToValidate);
    }

    @Test
    public void test_replaceKeyVarablesWithValues_jsonKeys() throws ArrestItException {
        final JSONArray data = new JSONArray();
        data.put(new JSONObject().put("1", "2").put("2", "40"));

        testCase.setData(data);
        testCase.addJsonVariable("successes", "1");
        testCase.addJsonVariable("errors", "2");

        final String oneSideOfEquation = "errors + successes";

        final String expected = "40 + 2";

        final String actual = validationManager.replaceKeyVariablesWithValues(testCase, oneSideOfEquation, 0);

        assertEquals(expected, actual);
    }

    @Test
    public void test_replaceKeyVarablesWithValues_sqlKeys() throws ArrestItException {
        final JSONArray data = new JSONArray();
        data.put(new JSONObject().put("1", "2"));

        testCase.setData(data);
        testCase.addJsonVariable("failures", "1");
        testCase.addSQLVariable("0:sqlFailures", "23");

        final String oneSideOfEquation = "failures + sqlFailures";

        final String expected = "2 + 23";

        final String actual = validationManager.replaceKeyVariablesWithValues(testCase, oneSideOfEquation, 0);

        assertEquals(expected, actual);
    }

    @Test
    public void test_replaceKeyVarablesWithValues_masterKeys() throws ArrestItException {
        final JSONArray data = new JSONArray();
        data.put(new JSONObject().put("1", "23456"));

        testCase.setData(data);
        testCase.setTime(30);
        testCase.addJsonVariable("tac", "1");
        dbValuesManager.addDBValue("-master-terminal-:30", "32564");

        final String oneSideOfEquation = "tac + -master-terminal-";

        final String expected = "23456 + 32564";

        final String actual = validationManager.replaceKeyVariablesWithValues(testCase, oneSideOfEquation, 0);

        assertEquals(expected, actual);
    }

    @Test
    public void test_replaceKeyVarablesWithValues_masterKeys_twoDashes() throws ArrestItException {
        final JSONArray data = new JSONArray();
        data.put(new JSONObject().put("1", "23456"));

        testCase.setData(data);
        testCase.setTime(30);
        testCase.addJsonVariable("tac", "1");
        dbValuesManager.addDBValue("-master-qos-terminal-:30", "32564");

        final String oneSideOfEquation = "tac + -master-qos-terminal-";

        final String expected = "23456 + 32564";

        final String actual = validationManager.replaceKeyVariablesWithValues(testCase, oneSideOfEquation, 0);

        assertEquals(expected, actual);
    }

    @Test
    public void test_replaceKeyVarablesWithValues_masterKeys_noTrailingDash() throws ArrestItException {
        final JSONArray data = new JSONArray();
        data.put(new JSONObject().put("1", "23456"));

        testCase.setData(data);
        testCase.setTime(30);
        testCase.addJsonVariable("tac", "1");
        dbValuesManager.addDBValue("-master-qos-terminal:30", "32564");

        final String oneSideOfEquation = "tac + -master-qos-terminal";

        final String expected = "23456 + 32564";

        final String actual = validationManager.replaceKeyVariablesWithValues(testCase, oneSideOfEquation, 0);

        assertEquals(expected, actual);
    }

    @Test
    public void test_replaceFunctionsWithValues_count_csvSetToTrue() throws ScriptException {
        testCase.setSizeOfCsvFile(12);
        testCase.setCsvUrl("EniqEventsCSV.jsp?display=grid&tzOffset=+0000&key=ERR&type=CELL&TAC={1.TAC}"
                + "&eventID={1.eventId}&dataTimeFrom=1415440800000&dataTimeTo=1415442600000" + "&userName=admin&url=NETWORK/EVENT_ANALYSIS&maxRows=0");

        final String leftSideOfEquation = "23456 + count{eventTime}";

        Validation validation = new Validation("", true, 2);

        validation.setLeftSideOfEquation(leftSideOfEquation);
        validation.setRightSideOfEquation("2 - 1");
        final String expected = "23456 + 12";

        validation = validationManager.replaceFunctionsWithValues(testCase, validation);

        assertEquals(validation.getLeftSideOfEquation(), expected);
        assertTrue(validation.isLeftCountPresent());
    }

    @Test
    public void test_replaceFunctionsWithValues_count_csvSetToFalse() throws ScriptException {
        validationManager = new ValidationManager(dbValuesManager, queryManager, false);

        final JSONArray data = new JSONArray();
        data.put(new JSONObject().put("1", "2014-12-02 00:00"));
        data.put(new JSONObject().put("1", "2014-12-02 00:10"));
        data.put(new JSONObject().put("1", "2014-12-02 00:20"));

        testCase.setData(data);
        testCase.addJsonVariable("eventTime", "1");
        testCase.setUrl("NETWORK/RANKING_ANALYSIS?time=30&display=grid&type=APN&tzOffset=+0100&maxRows=50");

        final String rightSideOfEquation = "23456 + count{eventTime}";

        final String expected = "23456 + 3";

        Validation validation = new Validation("", true, 2);
        validation.setRightSideOfEquation(rightSideOfEquation);
        validation.setLeftSideOfEquation("2 - 1");

        validation = validationManager.replaceFunctionsWithValues(testCase, validation);

        assertEquals(expected, validation.getRightSideOfEquation());
        assertTrue(validation.isRightCountPresent());
    }

    @Test
    public void test_replaceFunctionsWithValues_count_csvSetToFalse_countGreaterThanMaxRows() throws ScriptException {
        validationManager = new ValidationManager(dbValuesManager, queryManager, false);

        final JSONArray data = new JSONArray();
        data.put(new JSONObject().put("1", "2014-12-02 00:00"));
        data.put(new JSONObject().put("1", "2014-12-02 00:10"));
        data.put(new JSONObject().put("1", "2014-12-02 00:20"));

        testCase.setData(data);
        testCase.addJsonVariable("eventTime", "1");
        testCase.setUrl("NETWORK/RANKING_ANALYSIS?time=30&display=grid&type=APN&tzOffset=+0100&maxRows=1");

        final String rightSideOfEquation = "23456 + count{eventTime}";
        Validation validation = new Validation("", false, 2);
        validation.setRightSideOfEquation(rightSideOfEquation);
        validation.setLeftSideOfEquation("2 - 1");
        final String expected = "23456 + 3";

        validation = validationManager.replaceFunctionsWithValues(testCase, validation);

        assertEquals(expected, validation.getRightSideOfEquation());
        assertTrue(validation.isRightCountPresent());
    }

    @Test
    public void test_replaceFunctionsWithValues_sum() throws ScriptException {
        final JSONArray data = new JSONArray();
        data.put(new JSONObject().put("1", "2"));
        data.put(new JSONObject().put("1", "10"));

        testCase.setData(data);
        testCase.addJsonVariable("failures", "1");
        testCase.setUrl("NETWORK/RANKING_ANALYSIS?time=30&display=grid&type=APN&tzOffset=+0100&maxRows=1");

        final String rightSideOfEquation = "10 + sum{failures}";
        Validation validation = new Validation("", false, 2);
        validation.setRightSideOfEquation(rightSideOfEquation);
        validation.setLeftSideOfEquation("2 - 1");

        final String expected = "10 + 12.0";

        validation = validationManager.replaceFunctionsWithValues(testCase, validation);

        assertEquals(expected, validation.getRightSideOfEquation());
        assertTrue(validation.isSumOrAvgPresent());
    }

    @Test
    public void test_replaceFunctionsWithValues_mulitpleSum() throws ScriptException {
        final JSONArray data = new JSONArray();
        data.put(new JSONObject().put("1", "100").put("2", "5"));
        data.put(new JSONObject().put("1", "200").put("2", "10"));

        testCase.setData(data);
        testCase.addJsonVariable("failure", "1");
        testCase.addJsonVariable("success", "2");

        final String leftSideOfEquation = "sum{failure} + sum{success}";
        Validation validation = new Validation("", false, 2);
        validation.setLeftSideOfEquation(leftSideOfEquation);
        validation.setRightSideOfEquation("2 - 1");

        final String expected = "300.0 + 15.0";

        validation = validationManager.replaceFunctionsWithValues(testCase, validation);

        assertEquals(expected, validation.getLeftSideOfEquation());
        assertTrue(validation.isSumOrAvgPresent());
    }

    @Test
    public void test_replaceFunctionsWithValues_average() throws ScriptException {
        final JSONArray data = new JSONArray();
        data.put(new JSONObject().put("1", "2"));
        data.put(new JSONObject().put("1", "10"));
        data.put(new JSONObject().put("1", "18"));

        testCase.setData(data);
        testCase.addJsonVariable("kpi", "1");

        final String leftSideOfEquation = "10 + average{kpi}";
        Validation validation = new Validation("", false, 2);
        validation.setLeftSideOfEquation(leftSideOfEquation);
        validation.setRightSideOfEquation("2 - 1");

        final String expected = "10 + 10.0";

        validation = validationManager.replaceFunctionsWithValues(testCase, validation);

        assertEquals(expected, validation.getLeftSideOfEquation());
        assertTrue(validation.isSumOrAvgPresent());
    }

    @Test
    public void test_replaceFunctionsWithValues_multipleAverage() throws ScriptException {
        final JSONArray data = new JSONArray();
        data.put(new JSONObject().put("1", "100").put("2", "5"));
        data.put(new JSONObject().put("1", "200").put("2", "10"));

        testCase.setData(data);
        testCase.addJsonVariable("failure", "1");
        testCase.addJsonVariable("success", "2");

        final String leftSideOfEquation = "average{failure} + average{success}";
        Validation validation = new Validation("", false, 2);
        validation.setLeftSideOfEquation(leftSideOfEquation);
        validation.setRightSideOfEquation("2 - 1");

        final String expected = "150.0 + 7.5";

        validation = validationManager.replaceFunctionsWithValues(testCase, validation);

        assertEquals(expected, validation.getLeftSideOfEquation());
        assertTrue(validation.isSumOrAvgPresent());
    }

    @Test
    public void test_replaceFunctionsWithValues_multipleAvgSumCount() throws ScriptException {
        final JSONArray data = new JSONArray();
        data.put(new JSONObject().put("1", "100").put("2", "5"));
        data.put(new JSONObject().put("1", "200").put("2", "10"));

        testCase.setData(data);
        testCase.addJsonVariable("failure", "1");
        testCase.addJsonVariable("success", "2");

        final String leftSideOfEquation = "average{failure} + sum{success} + count{failure}";
        Validation validation = new Validation("", false, 2);
        validation.setLeftSideOfEquation(leftSideOfEquation);
        validation.setRightSideOfEquation("2 - 1");

        final String expected = "150.0 + 15.0 + 2";

        validation = validationManager.replaceFunctionsWithValues(testCase, validation);

        assertEquals(expected, validation.getLeftSideOfEquation());
        assertTrue(validation.isSumOrAvgPresent());
        assertTrue(validation.isLeftCountPresent());
    }

    @Test
    public void test_performValidation_withCountGreaterThanMaxRows() throws ScriptException, ArrestItException {

        testCase = setUpTestCaseWithFiveRows("57", 4);
        final Validation validation = setUpNumericalValidation("2.failures = count{failure}");

        assertTrue(validationManager.isNumericTestPassed(validation, testCase));
    }

    @Test
    public void test_performValidation_withScientificNotation_true() throws ScriptException, ArrestItException {

        final Validation validation = setUpNumericalValidation("1.0E-4 = 0.0001");

        assertTrue(validationManager.isNumericTestPassed(validation, testCase));
    }

    @Test
    public void test_performValidation_withScientificNotation_false() throws ScriptException, ArrestItException {

        final Validation validation = setUpNumericalValidation("1.0E-4 = 0.0000");

        assertFalse(validationManager.isNumericTestPassed(validation, testCase));
    }

    @Test
    public void test_performValidation_withCountGreaterThanMaxRows_false() throws ScriptException, ArrestItException {

        testCase = setUpTestCaseWithFiveRows("2", 5);
        final Validation validation = setUpNumericalValidation("2.failures = count{failure}");

        assertFalse(validationManager.isNumericTestPassed(validation, testCase));
    }

    @Test
    public void test_performValidation_withCountLessThanMaxRows_false() throws ScriptException, ArrestItException {
        testCase = setUpTestCaseWithFiveRows("2", 5);
        final Validation validation = setUpNumericalValidation("2.failures = count{failure}");

        assertFalse(validationManager.isNumericTestPassed(validation, testCase));
    }

    @Test
    public void test_performValidation_withCountLessThanMaxRows() throws ScriptException, ArrestItException {
        testCase = setUpTestCaseWithFiveRows("5", 8);
        final Validation validation = setUpNumericalValidation("2.failures = count{failure}");

        assertTrue(validationManager.isNumericTestPassed(validation, testCase));
    }

    @Test
    public void test_performValidation_withMultipleSums() throws ScriptException, ArrestItException {
        testCase = setUpTestCaseWithFiveRows("1575", 8);
        final Validation validation = setUpNumericalValidation("2.failures = sum{failure} + sum{success}");

        assertTrue(validationManager.isNumericTestPassed(validation, testCase));
    }

    @Test
    public void test_performValidation_withSumGreaterThanMaxRows() throws ScriptException, ArrestItException {
        testCase = setUpTestCaseWithFiveRows("2", 3);
        final Validation validation = setUpNumericalValidation("2.failures = sum{failure} + sum{success}");

        assertTrue(validationManager.isNumericTestPassed(validation, testCase));
    }

    @Test
    public void test_performValidation_withAverageGreaterThanMaxRows() throws ArrestItException, ScriptException {
        testCase = setUpTestCaseWithFiveRows("2", 3);
        final Validation validation = setUpNumericalValidation("2.failures = average{failure} + average{success}");

        assertTrue(validationManager.isNumericTestPassed(validation, testCase));
    }

    @Test
    public void test_performValidation_withAverageLessThanMaxRows() throws ScriptException, ArrestItException {
        testCase = setUpTestCaseWithFiveRows("300", 8);
        final Validation validation = setUpNumericalValidation("2.failures = average{failure}");

        assertTrue(validationManager.isNumericTestPassed(validation, testCase));
    }

    @Test
    public void test_performValidation_withSortAscAlphabetic_PassExpected() throws ScriptException {
        testCase = setUpTestCaseForSortTests("a", "B", "c", "name");
        final Validation validation = new Validation("sort{name} = asc", false, 2);

        validationManager.performValidation(validation, testCase, 0);

        assertTrue(testCase.getTestResultHolder().haveAllTestsPassed());
    }

    @Test
    public void test_performValidation_withSortAscAlphabetic_FailExpected() throws ScriptException {
        testCase = setUpTestCaseForSortTests("carrot", "DUCK", "apple", "name");
        final Validation validation = new Validation("sort{name} = asc", false, 2);

        validationManager.performValidation(validation, testCase, 0);

        assertFalse(testCase.getTestResultHolder().haveAllTestsPassed());
    }

    @Test
    public void test_performValidation_withSortDescAlphabetic_PassExpected() throws ScriptException {
        testCase = setUpTestCaseForSortTests("Carrot", "bEar", "Baby", "name");
        final Validation validation = new Validation("sort{name} = desc", false, 2);

        validationManager.performValidation(validation, testCase, 0);

        assertTrue(testCase.getTestResultHolder().haveAllTestsPassed());
    }

    @Test
    public void test_performValidation_withSortDescAlphaNumeric_PassExpected() throws ScriptException {
        testCase = setUpTestCaseForSortTests("Carrot", "bEar", "1zebra", "name");
        final Validation validation = new Validation("sort{name} = desc", false, 2);

        validationManager.performValidation(validation, testCase, 0);

        assertTrue(testCase.getTestResultHolder().haveAllTestsPassed());
    }

    @Test
    public void test_performValidation_withSortDescNumeric_FailExpected() throws ScriptException {
        testCase = setUpTestCaseForSortTests("3", "1", "2", "name");
        final Validation validation = new Validation("sort{name} = desc", false, 2);

        validationManager.performValidation(validation, testCase, 0);

        assertFalse(testCase.getTestResultHolder().haveAllTestsPassed());
    }

    @Test
    public void test_performValidation_withSortDescNumeric_PassExpected() throws ScriptException {
        testCase = setUpTestCaseForSortTests("3", "2", "2", "name");
        final Validation validation = new Validation("sort{name} = desc", false, 2);

        validationManager.performValidation(validation, testCase, 0);

        assertTrue(testCase.getTestResultHolder().haveAllTestsPassed());
    }

    @Test
    public void test_performValidation_withSortAscTimes_PassExpected() throws ScriptException {
        testCase = setUpTestCaseForSortTests("2015-03-19 09:44:01.576", "2015-03-19 09:44:00.963", "2015-03-20 09:45:01.13", "time");
        final Validation validation = new Validation("sort{time} = asc", false, 2);

        validationManager.performValidation(validation, testCase, 0);

        assertTrue(testCase.getTestResultHolder().haveAllTestsPassed());
    }

    @Test
    public void test_performValidation_withSortDescTimes_PassExpected() throws ScriptException {
        testCase = setUpTestCaseForSortTests("2015-03-19 09:45:01.13", "2015-03-19 09:44:00.963", "2015-03-19 09:43:00.576", "time");
        final Validation validation = new Validation("sort{time} = desc", false, 2);

        validationManager.performValidation(validation, testCase, 0);

        assertTrue(testCase.getTestResultHolder().haveAllTestsPassed());
    }

    private TestCase setUpTestCaseForSortTests(final String firstRowValue, final String secondRowValue, final String thirdRowValue,
                                               final String columnName) {
        final JSONArray data = new JSONArray();
        data.put(new JSONObject().put("1", firstRowValue));
        data.put(new JSONObject().put("1", secondRowValue));
        data.put(new JSONObject().put("1", thirdRowValue));

        testCase.setData(data);
        testCase.addJsonVariable(columnName, "1");

        final TestResultHolder testResults = new TestResultHolder(testCase);
        testCase.setTestResultHolder(testResults);

        return testCase;
    }

    private Validation setUpNumericalValidation(final String equation) throws ArrestItException, ScriptException {
        Validation validation = new Validation(equation, false, 2);
        validation = validationManager.splitEquation(validation);
        validation = validationManager.replaceFunctionsWithValues(testCase, validation);
        validation.setRightSideOfEquation(validationManager.replaceKeyVariablesWithValues(testCase, validation.getRightSideOfEquation(), 0));
        validation.setLeftSideOfEquation(validationManager.replaceKeyVariablesWithValues(testCase, validation.getLeftSideOfEquation(), 0));
        validation.setLeftSideNumericalValue(validationManager.performCalculation(validation.getLeftSideOfEquation()));
        validation.setRightSideNumericalValue(validationManager.performCalculation(validation.getRightSideOfEquation()));
        return validation;
    }

    private TestCase setUpTestCaseWithFiveRows(final String parentValue, final int maxRowCount) {
        final JSONArray data = new JSONArray();
        data.put(new JSONObject().put("1", "100").put("2", "5"));
        data.put(new JSONObject().put("1", "200").put("2", "10"));
        data.put(new JSONObject().put("1", "300").put("2", "15"));
        data.put(new JSONObject().put("1", "400").put("2", "20"));
        data.put(new JSONObject().put("1", "500").put("2", "25"));

        testCase.setData(data);
        testCase.addJsonVariable("failure", "1");
        testCase.addJsonVariable("success", "2");

        testCase.setMaxRowCount(maxRowCount);
        testCase.addParentValue("2.failures", parentValue);

        return testCase;
    }

    @Test
    public void test_getMaxRowsFromUrl() throws ScriptException {
        final String url = "NETWORK/RANKING_ANALYSIS?time=30&display=grid&type=APN&tzOffset=+0100&maxRows=10";
        final int expected = 10;
        final int actual = ValidationManager.getMaxRowsFromUrl(url);

        assertEquals(expected, actual);
    }

    @Test
    public void test_getMaxRowsFromUrl_maxRowsNotAtEndOfUrl() throws ScriptException {
        final String url = "NETWORK/RANKING_ANALYSIS?time=30&display=grid&type=APN&maxRows=10&tzOffset=+0100";
        final int expected = 10;
        final int actual = ValidationManager.getMaxRowsFromUrl(url);

        assertEquals(expected, actual);
    }

    @Test
    public void test_getMaxRowsFromUrl_noMaxRows_return500() throws ScriptException {
        final String url = "NETWORK/RANKING_ANALYSIS?time=30&display=grid&type=APN&tzOffset=+0100";
        final int expected = 500;
        final int actual = ValidationManager.getMaxRowsFromUrl(url);

        assertEquals(expected, actual);
    }

    @Test
    public void test_round() {
        final double expected = 222.16;
        final double actual = ValidationManager.round(222.15543643, 2);

        assertEquals(expected, actual, 0.01);
    }

    @Test
    public void test_round_99percent() {
        final double expected = 99.99;
        final double actual = ValidationManager.round(99.996657, 2);

        assertEquals(expected, actual, 0.01);
    }

    @Test
    public void test_round_01percent() {
        final double expected = 00.01;
        final double actual = ValidationManager.round(00.0012356, 2);

        assertEquals(expected, actual, 0.01);
    }
}
