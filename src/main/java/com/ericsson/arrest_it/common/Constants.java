package com.ericsson.arrest_it.common;

import java.io.File;
import java.net.URISyntaxException;

public class Constants {
    public static final String SEPARATOR = System.getProperty("file.separator");
    public static final File ROOT_DIR = getRootDirectory();
    public static final String RELATIVE_PATH = getRelativePath(ROOT_DIR);
    public static final String INTERIM_FILE_NAME = "interimA";
    public static final String INTERIM_FILE_FOLDER = "interim/";
    public static final String SUMMARY_FILE = "Arrest_It_Summary_";
    public static final String BASIC_SUMMARY_FILE = "Arrest_It_Basic_Summary";
    public static final String BASIC_SUMMARY_CSV_FILE = "Arrest_It_Basic_Csv_Summay";
    public static final String TEST_FOLDER = "AllTests";
    public static final String RESULTS_BACKUP_FOLDER = "Arrest_It_Archive_";
    public static final String SERIALIZATION_EXTENSION = ".ser";
    public static final String HTML_TEMPLATE = "src/main/resources/html-resources/test.html";
    public static final String SERVICES_URL = ".athtem.eei.ericsson.se:8181";
    public static final String SERVICES_VAPP_URL = "eniqe.vts.com:8181";
    public static final String ENIQ_UI_PATH = "/EniqEventsUI/";
    public static final String ENIQ_SERVICES_PATH = "/EniqEventsServices/";
    public static final String[] VALID_XML_TAGS = { "TESTSUITE", "TESTCASE", "DIRECTION", "URL", "CSVURL", "MAXVALIDATION", "MAXROWCOUNT",
            "REPEATVALIDATION", "SQLTEST", "VALIDATE", "JSONVARIABLES", "IGNOREDINCSV", "REPEATDRILLDOWN", "PASSTODRILLDOWN", "PRECONDITION",
            "PRECONDITIONSQL" };

    public static final String COMMA = ",";
    public static final String DASH_WITH_SPACES = " - ";
    public static final String DASH = "-";
    public static final String TAB = "\t";
    public static final String EQUALS = "=";
    public static final String AMPERSAND = "&";
    public static final String URL_SEPARATOR = "/";
    public static final String XML_EXTENSION = ".xml";
    public static final String HTML_EXTENSION = ".html";
    public static final String TXT_EXTENSION = ".txt";
    public static final String CSV_EXTENSION = ".csv";
    public static final String RESULTS_FOLDER = "results";
    public static final String FILE_DATETIME_FORMAT = "_yyyy-MM-dd_HHmm";
    public static final int MINUTES_IN_A_WEEK = 10080;
    public static final String SPLITIDENTIFIER = "COMMASPLIT";
    public static final String EMPTY_STRING = "";
    public static final String SINGLE_QUOTE = "'";
    public static final String COLON = ":";

    public static final String ASCENDING_MARKER = "asc";
    public static final String DESCENDING_MARKER = "desc";
    public static final String UNSORTED_MARKER = "unsorted";

    //Managers
    public static final String DB_MANAGER = "DBManager";
    public static final String DB_VALUES_MANAGER = "DBValuesManager";
    public static final String GROUP_MANAGER = "GroupManager";
    public static final String JSON_VALIDATION_MANAGER = "JsonValidationManager";
    public static final String PRECONDITION_MANAGER = "PreconditionManager";
    public static final String QUERY_MANAGER = "QueryManager";
    public static final String SQL_MANAGER = "SQLManager";
    public static final String TIME_MANAGER = "TimeManager";
    public static final String VALIDATION_MANAGER = "ValidationManager";

    // Numbers
    public static final String ZERO_STRING = "0";

    // Map Keys
    public static final String SQL_DATETIME_FROM_KEY = "sqlDateTimeFromKey";
    public static final String SQL_DATETIME_TO_KEY = "sqlDateTimeToKey";
    public static final String URL_TIME_TO_KEY = "timeToKey";
    public static final String URL_TIME_FROM_KEY = "timeFromKey";
    public static final String URL_DATE_TO_KEY = "dateToKey";
    public static final String URL_DATE_FROM_KEY = "dateFromKey";
    public static final String CSV_DATATIME_FROM_KEY = "csvDataFromKey";
    public static final String CSV_DATATIME_TO_KEY = "csvDataToKey";

    //URL Constants
    public static final String DATATIME_TO = "dataTimeTo";
    public static final String DATATIME_FROM = "dataTimeFrom";
    public static final String TIME_TO = "timeTo";
    public static final String TIME_FROM = "timeFrom";
    public static final String DATE_TO = "dateTo";
    public static final String DATE_FROM = "dateFrom";
    public static final String TIME = "time";

    // DB Constants
    public static final String ARREST_IT_CONNECTION_NAME = "Arrest_it";
    public static final String DB_DRIVER = "com.sybase.jdbc4.jdbc.SybDriver";
    public static final String DB_PORT = "dbPort";
    public static final String GROUP_NAME = "GROUP_NAME";

    // flags
    public static final String REPEAT_VALIDATION_FLAG = "-repeat-";
    public static final String NON_REPEAT_VALIDATION_FLAG = "-norepeat-";
    public static final String MASTER_FLAG = "-master-";
    public static final String GRID_CSV_FAIL = "gridCsvFail";
    public static final String ERROR_DESCRIPTION = "errorText";

    // properties file constants
    public static final String TIMES = "times";
    public static final String TIMEZONE = "timezone";
    public static final String MAX_NO_OF_VALIDATIONS_PER_URL = "maxNoOfValidationsPerUrl";
    public static final String IS_SUC_RAW = "sucRaw";
    public static final String IS_V_APP_USING_VPN = "vAppUsingVPN";
    public static final String SHOULD_VALIDATE_CSV = "csv";
    public static final String GO_BACK_HOURS = "noOfHoursToGoBack";
    public static final String UNIQUE_TESTS = "maxNoOfUniqueTestCasesGeneratedPerSuite";
    public static final String NO_OF_DECIMAL_PLACES = "noOfDecimalPlaces";
    public static final String SERVICES_NAME = "servicesName";
    public static final String END_TIME = "endTime";
    public static final String SYBASE_MILLIS_BUFFER = "sybaseMillisBuffer";

    // CLI constants
    public static final String UI_SERVER_NAME = "uiServerName";
    public static final String DB_SERVER_NAME = "dbServerName";
    public static final String DB_USR = "dbUsr";
    public static final String DB_PASS = "dbPass";
    public static final String UI_USR = "uiUsr";
    public static final String UI_PASS = "uiPass";
    public static final String ALL_TESTS = "all";
    public static final String FILE = "f";
    public static final String TIMESTAMP = "t";
    public static final String CREATE = "create";
    public static final String DELETE = "delete";

    // SQL query constants
    public static final String FROM_DATETIME_ID_WITH_SPACE = "DATETIME_ID >=";
    public static final String FROM_DATETIME_ID_WITHOUT_SPACE = "DATETIME_ID>=";
    public static final String TO_DATETIME_ID_WITH_SPACE = "DATETIME_ID <";
    public static final String TO_DATETIME_ID_WITHOUT_SPACE = "DATETIME_ID<";
    public static final String FROM_EVENT_TIME_WITH_SPACE = "EVENT_TIME >=";
    public static final String FROM_EVENT_TIME_WITHOUT_SPACE = "EVENT_TIME>=";
    public static final String TO_EVENT_TIME_WITH_SPACE = "EVENT_TIME <";
    public static final String TO_EVENT_TIME_WITHOUT_SPACE = "EVENT_TIME<";
    public static final String FROM_FIVE_MIN_AGG_TIME_WITH_SPACE = "FIVE_MIN_AGG_TIME >=";
    public static final String FROM_FIVE_MIN_AGG_TIME_WITHOUT_SPACE = "FIVE_MIN_AGG_TIME>=";
    public static final String TO_FIVE_MIN_AGG_TIME_WITH_SPACE = "FIVE_MIN_AGG_TIME <";
    public static final String TO_FIVE_MIN_AGG_TIME_WITHOUT_SPACE = "FIVE_MIN_AGG_TIME<";

    // Results Identifiers
    public static final String CSV_NETWORK = "csvNetworkResults";
    public static final String CSV_PARSING = "csvParsingResults";
    public static final String CSV_VALIDATION = "csvValidationResults";
    public static final String JSON_NETWORK = "jsonNetworkResults";
    public static final String JSON_PARSING = "jsonParsingResults";
    public static final String JSON_VALIDATION = "jsonValidationResults";
    public static final String FRAMEWORK = "frameWorkResults";
    public static final String FAILED_TESTS = "Failed tests";
    public static final String PASSED_TESTS = "Passed Tests";
    public static final String PERCENTAGE_PASSED = "Perc passed";
    public static final String TOTAL_TESTS = "Total Tests";
    public static final String EXCESS_TESTS = "Excess Tests";
    public static final String DRILLS_ATTEMPTED = "UrlAttempted";
    public static final String DRILLS_NOT_COMPLETED = "drillsUnsuccessful";
    public static final String NO_OF_EMPTY_GRIDS = "noOfEmptyGrids";

    // Results Constants
    public static final String PASSED_IDENTIFIER = "Passed";
    public static final String FAILED_IDENTIFIER = "Failed";
    public static final String ALL_TESTS_IDENTIFIER = "Total";
    public static final String FAILED_FILE = "_Failed";
    public static final String ALL_TESTS_FILE = "_AllTests";
    public static final String NOT_APPLICABLE_IDENTIFIER = "Not Applicable";
    public static final String TEST_NOT_RUN_IDENTIFIER = "Could Not Run Test";
    public static final String UNSPECIFIED_FRAMEWORK_FAILURE = "Unspecified Framework Failure";
    public static final String CANNOT_OBTAIN_CSV = "Could Not Obtain CSV";
    public static final String CANNOT_PARSE_CSV = "Could Not parse CSV";
    public static final String NO_DATA_CSV = "No Data Present in CSV";
    public static final String CSV_DATA_MISMATCH = "CSV/UI DataMismatch";
    public static final String CANNOT_OBTAIN_UI = "Could Not obtain UI Data";
    public static final String CANNOT_PARSE_UI = "Could Not parse UI Json";
    public static final String NO_DATA_UI = "No Data Present in UI";
    public static final String UI_MISMATCH = "UI/DB DataMismatch";
    public static final String UI_RESULT = "UI Result";
    public static final String CSV_RESULT = "CSV Result";
    public static final String TEST_NAME_HEADING = "Test Name";
    public static final String FAILURE_SUMMARY_HEADING = "Failure Reason";

    // Html Template File Identifers, if changes are made to the html template
    // file these must be updated
    public static final String HTML_RESULT_HEADING = "id=\"docHeading\"";
    public static final String HTML_UNIQUE_GRIDS_ATTEMPTED = "id=\"uniqueGridsAttempted\"";
    public static final String HTML_UNIQUE_GRIDS_TESTED = "id=\"uniqueGrids\"";
    public static final String HTML_TEMPORAL_VALUES_TESTED = "id=\"temporalValues\"";
    public static final String HTML_TIME_ZONE_OFFSET = "id=\"tzOffset\"";
    public static final String HTML_TOTAL_TEST_CASES = "id=\"totalTests\"";
    public static final String HTML_TEST_CASES_PASSED = "id=\"testsPassed\"";
    public static final String HTML_TEST_CASES_FAILED = "id=\"testsFailed\"";
    public static final String HTML_PERCENTAGE_TESTS_PASSED = "id=\"percTestsPassed\"";
    public static final String HTML_TOTAL_VALIDATIONS = "id=\"totalValidations\"";
    public static final String HTML_TOTAL_VALIDATIONS_PASSED = "id=\"validationsPassed\"";
    public static final String HTML_TOTAL_VALIDATIONS_FAILED = "id=\"validationsFailed\"";
    public static final String HTML_PERCENTAGE_VALIDATIONS_PASSED = "id=\"percValsPassed\"";
    public static final String HTML_DRILLS_ATTEMPTED = "id=\"drillsAttempted\"";
    public static final String HTML_DRILLS_FAILED = "id=\"drillsFailed\"";
    public static final String HTML_PERCENTAGE_PRECONDITIONS = "id=\"percPreconditions\"";
    public static final String HTML_EMPTY_URLS = "id=\"emptyUrls\"";
    public static final String HTML_DETAILED_SUMMARY = "Detailed Summary:";
    public static final String HTML_FAILED_RESULTS = "Failed Results:";

    // Timeout Constants
    public static final int SQL_QUERY_TIMEOUT_IN_MINUTES = 45;
    public static final int URL_QUERY_TIMEOUT_IN_MINUTES = 45;

    public static final String TEST_RESOURCES = RELATIVE_PATH + "src" + SEPARATOR + "test" + SEPARATOR + "resources" + SEPARATOR;

    // Regex Constants

    public static final String PARENT_ENRICHMENT_REGEX = "";
    public static final String JSON_ENRICHMENT_REGEX = "";
    public static final String SQL_ENRICHMENT_REGEX = "";
    public static final String MASTER_ENRICHMENT_REGEX = "";

    public static final String PARENT_KEY_REGEX = ".*\\d\\.[a-zA-Z].*";
    public static final String JSON_KEY_REGEX = ".*[a-zA-Z].*";
    public static final String SQL_KEY_REGEX = ".*sql[a-zA-Z].*";
    public static final String MASTER_KEY_REGEX = ".*-master-[a-zA-Z-].*";

    public static final String MASTER_REPLACE_REGEX = "-master-[a-zA-Z-]+";
    public static final String SQL_REPLACE_KEY = "sql[a-zA-Z]+";
    public static final String PARENT_REPLACE_REGEX = "\\d+\\.[a-zA-Z]+";
    public static final String JSON_VAR_REPLACE_REGEX = "[a-zA-Z]+";

    public static final String COUNT_REGEX = ".*count\\{.*\\}.*";
    public static final String SUM_REGEX = ".*sum\\{.*\\}.*";
    public static final String AVERAGE_REGEX = ".*average\\{.*\\}.*";
    public static final String SORT_REGEX = ".*sort\\{.*\\}.*";

    public static final String EVENT_TIME_REGEX = "[0-9]{4}-[0-1][0-9]-[0-3][0-9] [0-2][0-9]:[0-5][0-9]:[0-5][0-9].*";

    public static final String COUNT_SEARCH_PATTERN = "count{";
    public static final String SUM_SEARCH_PATTERN = "sum{";
    public static final String AVG_SEARCH_PATTERN = "average{";
    public static final String SORT_SEARCH_PATTERN = "sort{";
    public static final String CLOSED_CURLY_BRACE = "}";

    public static final String NON_NUMERIC_PATTERN = ".*[a-zA-Z]+.*";
    public static final String SCIENTIFIC_NOTATION_PATTERN = ".*\\d+\\.\\d+E.*";

    public static final int DEFAULT_MAX_ROWS = 500;

    public static File getMasterValuesFile() {
        final StringBuilder propertiesFileBuilder = new StringBuilder();
        propertiesFileBuilder.append(Constants.RELATIVE_PATH);
        propertiesFileBuilder.append("config");
        propertiesFileBuilder.append(SEPARATOR);
        propertiesFileBuilder.append("masterValues.txt");

        return new File(propertiesFileBuilder.toString());
    }

    public static File getGroupManagmentFile() {
        final StringBuilder propertiesFileBuilder = new StringBuilder();
        propertiesFileBuilder.append(Constants.RELATIVE_PATH);
        propertiesFileBuilder.append("config");
        propertiesFileBuilder.append(SEPARATOR);
        propertiesFileBuilder.append("group_management.txt");

        return new File(propertiesFileBuilder.toString());
    }

    public static String getRelativePath(final File rootDir) {
        final String absolutePath = rootDir.getAbsolutePath();
        String relativePath = absolutePath;

        if (absolutePath.endsWith(".")) {
            relativePath = removeLastCharacter(absolutePath);
        }

        final String binDir = SEPARATOR + "bin" + SEPARATOR;
        if (relativePath.endsWith(".jar")) {
            relativePath = absolutePath.subSequence(0, relativePath.indexOf(binDir)).toString();
        }

        return relativePath + SEPARATOR;
    }

    public static File getRootDirectory() {
        File rootDir = null;

        try {
            rootDir = new File(Constants.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        } catch (final URISyntaxException e) {
            e.printStackTrace();
        }

        if (!rootDir.getAbsolutePath().endsWith(".jar")) {
            final File relativePath = new File(".");
            final String relativePathName = removeLastCharacter(relativePath.getAbsolutePath());

            rootDir = new File(relativePathName);
        }

        return rootDir;
    }

    public static String removeLastCharacter(final String string) {
        return string.subSequence(0, string.length() - 1).toString();
    }

}
