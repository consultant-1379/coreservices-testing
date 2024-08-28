package com.ericsson.arrest_it.main;

import static com.ericsson.arrest_it.common.Constants.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import junit.extensions.PA;

import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.*;

import com.ericsson.arrest_it.common.TestCase;
import com.ericsson.arrest_it.results.TestResultHolder;
import com.github.tomakehurst.wiremock.WireMockServer;

public class HttpEngineTest {
    private static HttpEngine httpEngine;
    private static final int PORT = 8080;
    private static TestCase testCase;
    private static final String SERVER_PATH = "http://localhost:" + PORT + ENIQ_SERVICES_PATH;
    private static final String TEST_UI_PASSWORD = "admin";
    private static final String TEST_UI_USERNAME = "admin";
    private static final String REQUEST_ID = "23456";
    private static final String UI_RESPONSE_VALID = "{\"success\":\"true\",\"errorDescription\":\"\",\"dataTimeFrom\":\"1427324400000\",\"dataTimeTo\":\"1427929200000\",\"timeZone\":\"+0100\",\"data\":[{\"1\":\"2\",\"2\":\"Unknown\",\"3\":\"10049\",\"4\":\"13\",\"5\":\"L_SERVICE_REQUEST\",\"6\":\"0\",\"7\":\"127698\",\"8\":\"127698\",\"9\":\"100.00\",\"10\":\"0\"}]}";
    private static JSONArray validData;
    private static final String UI_RESPONSE_500_ERROR = "{\"success\" : \"false\", \"errorDescription\" : \"HTTP 500 status returned. Please refer to server.log for more details.\"}";
    private static final String UI_500_TEXT = "HTTP/1.1 500 Internal Server Error";
    private static final String UI_404_TEXT = "HTTP/1.1 404 Not Found";
    private static final String UI_RESPONSE_404_ERROR = "{\"success\" : \"false\", \"errorDescription\" : \"HTTP 404 status returned. Please refer to server.log for more details.\"}";
    private static final String REQUEST_URL = "NETWORK/RANKING_ANALYSIS?time=10080&display=grid&type=TAC&tzOffset=+0100&maxRows=50";
    private static final String CSV_REQUEST_URL = "EniqEventsCSV.jsp?display=grid&type=TAC&tzOffset=%2B0100&dataTimeFrom=1428486300000&dataTimeTo=1428488100000&userName=admin&url=NETWORK/RANKING_ANALYSIS&maxRows=0";
    private static final String CSV_JSON = "[{\"3\":\"3\",\"2\":\"2\",\"1\":\"1\",\"5\":\"5\",\"4\":\"4\"},{\"3\":\"3\",\"2\":\"2\",\"1\":\"1\",\"5\":\"5\",\"4\":\"4\"}]";
    private static final String CSV_JSON_WITH_IGNORED = "[{\"3\":\"3\",\"2\":\"2\",\"1\":\"1\",\"6\":\"\",\"5\":\"5\",\"4\":\"4\"},{\"3\":\"3\",\"2\":\"2\",\"1\":\"1\",\"6\":\"\",\"5\":\"5\",\"4\":\"4\"}]";
    private static final String CSV_JSON_ONE_ROW = "[{\"3\":\"3\",\"2\":\"2\",\"1\":\"1\",\"5\":\"5\",\"4\":\"4\"}]";
    private static final int NO_DATA_LINES_IN_TEST_CSV_FILE = 2;

    private WireMockServer wireMockServer;

    @BeforeClass
    public static void classSetup() {
        httpEngine = new HttpEngine(SERVER_PATH, TEST_UI_USERNAME, TEST_UI_PASSWORD, REQUEST_ID);
        testCase = new TestCase();
        JSONObject responseData = new JSONObject(UI_RESPONSE_VALID);
        validData = responseData.getJSONArray("data");
    }

    @Before
    public void init() {
        wireMockServer = new WireMockServer(PORT);
        wireMockServer.start();
        testCase.setTestResultHolder(new TestResultHolder(testCase));
        PA.setValue(httpEngine, "httpClient", null);
    }

    @After
    public void stopServers() {
        wireMockServer.stop();
    }

    @Test
    public void test_httpEngineSetup() {
        String actualServerPath = (String) PA.getValue(httpEngine, "serverPath");
        String actualRequestId = (String) PA.getValue(httpEngine, "requestId");
        String actualUserName = (String) PA.getValue(httpEngine, "uiUsername");
        String actualPassword = (String) PA.getValue(httpEngine, "uiPassword");
        String actualUiPath = (String) PA.getValue(httpEngine, "uiPath");

        assertEquals("Server Path set incorrectly", SERVER_PATH, actualServerPath);
        assertEquals("Request Id set incorrectly", REQUEST_ID, actualRequestId);
        assertEquals("Ui Username set incorrectly", TEST_UI_USERNAME, actualUserName);
        assertEquals("Ui Password set incorrectly", TEST_UI_PASSWORD, actualPassword);

        String expectedUiPath = "http://localhost:" + PORT + "/EniqEventsUI/";

        assertEquals("Ui path set incorrectly", expectedUiPath, actualUiPath);
    }

    @Test
    public void test_httpClientCreated() {
        CloseableHttpClient actualClientBeforeCreation = (CloseableHttpClient) PA.getValue(httpEngine, "httpClient");

        assertNull("Invalid Test http client is not null before creation", actualClientBeforeCreation);
        httpEngine.createHttpClient();

        CloseableHttpClient actualHttpClient = (CloseableHttpClient) PA.getValue(httpEngine, "httpClient");

        assertNotNull("Http client is null after creation", actualHttpClient);

    }

    @Test
    public void test_httpClientSendingQueries() {
        stubFor(get(urlEqualTo(ENIQ_SERVICES_PATH + REQUEST_URL)).withHeader("Content-Type",
                equalTo("application/x-www-form-urlencoded; charset=UTF-8")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(UI_RESPONSE_VALID)));
        httpEngine.createHttpClient();
        JSONArray returnedData = httpEngine.sendUrlQueries(testCase, REQUEST_URL);

        String expectedResult = validData.toString();
        String actualResult = returnedData.toString();

        assertEquals("Http Engine not returning data correctly from url request", expectedResult, actualResult);
    }

    @Test
    public void test_httpClientReceiving500Error() {
        boolean areArrestItTestsPassed;

        stubFor(get(urlEqualTo(ENIQ_SERVICES_PATH + REQUEST_URL)).withHeader("Content-Type",
                equalTo("application/x-www-form-urlencoded; charset=UTF-8")).willReturn(
                aResponse().withStatus(500).withHeader("Content-Type", "application/json").withBody(UI_RESPONSE_500_ERROR)));
        httpEngine.createHttpClient();
        JSONArray returnedData = httpEngine.sendUrlQueries(testCase, REQUEST_URL);

        areArrestItTestsPassed = testCase.getTestResultHolder().haveAllTestsPassed();

        assertTrue("HttpEngine not returning 500 error information in json", returnedData.toString().contains(UI_500_TEXT));
        assertFalse("HttpEngine passing 500 error tests which should fail", areArrestItTestsPassed);
    }

    @Test
    public void test_httpClientReceiving404Error() {
        boolean areArrestItTestsPassed;

        stubFor(get(urlEqualTo(ENIQ_SERVICES_PATH + REQUEST_URL)).withHeader("Content-Type",
                equalTo("application/x-www-form-urlencoded; charset=UTF-8")).willReturn(
                aResponse().withStatus(404).withHeader("Content-Type", "application/json").withBody(UI_RESPONSE_404_ERROR)));
        httpEngine.createHttpClient();
        JSONArray returnedData = httpEngine.sendUrlQueries(testCase, REQUEST_URL);

        areArrestItTestsPassed = testCase.getTestResultHolder().haveAllTestsPassed();

        assertTrue("HttpEngine not returning 404 error information in json", returnedData.toString().contains(UI_404_TEXT));
        assertFalse("Http Engine passing 404 error tests which should fail", areArrestItTestsPassed);
    }

    @Test
    public void test_csvRequestReturnsCorrectData() {
        stubFor(get(urlEqualTo(ENIQ_UI_PATH + CSV_REQUEST_URL)).withHeader("Accept",
                equalTo("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/csv;charset=ISO-8859-1")
                        .withHeader("Content-disposition", "attachment; filename=export.csv").withBodyFile("testCsvToReturn.csv")));
        httpEngine.createHttpClient();
        testCase.setUrl(REQUEST_URL);
        testCase.setCsvUrl(CSV_REQUEST_URL);
        JSONArray returnedData = httpEngine.sendCsvUrl(testCase);

        String actualResult = returnedData.toString();

        assertEquals("HttpEngine returning incorrect csv data", CSV_JSON, actualResult);
    }

    @Test
    public void test_csvRequestWithIgnoredColumns() {
        stubFor(get(urlEqualTo(ENIQ_UI_PATH + CSV_REQUEST_URL)).withHeader("Accept",
                equalTo("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/csv;charset=ISO-8859-1")
                        .withHeader("Content-disposition", "attachment; filename=export.csv").withBodyFile("testCsvToReturn.csv")));
        httpEngine.createHttpClient();
        testCase.setUrl(REQUEST_URL);
        testCase.setCsvUrl(CSV_REQUEST_URL);
        List<Integer> ignoredInCsv = new ArrayList<Integer>();
        ignoredInCsv.add(6);
        testCase.setIgnoredInCsv(ignoredInCsv);
        JSONArray returnedData = httpEngine.sendCsvUrl(testCase);

        String actualResult = returnedData.toString();

        assertEquals("HttpEngine not ignoring columns in Csv file as expected", CSV_JSON_WITH_IGNORED, actualResult);

        testCase.setIgnoredInCsv(new ArrayList<Integer>());
    }

    @Test
    public void test_csvRequestWithMaxRows() {
        stubFor(get(urlEqualTo(ENIQ_UI_PATH + CSV_REQUEST_URL)).withHeader("Accept",
                equalTo("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/csv;charset=ISO-8859-1")
                        .withHeader("Content-disposition", "attachment; filename=export.csv").withBodyFile("testCsvToReturn.csv")));
        httpEngine.createHttpClient();
        String urlWithOneRow = REQUEST_URL.replace("50", "1");
        testCase.setUrl(urlWithOneRow);
        testCase.setCsvUrl(CSV_REQUEST_URL);
        JSONArray returnedData = httpEngine.sendCsvUrl(testCase);

        String actualResult = returnedData.toString();

        assertEquals("HttpEngine returning values from csv file when max rows is set", CSV_JSON_ONE_ROW, actualResult);

        int actualFileLength = testCase.getSizeOfCsvFile();
        assertEquals("HttpEngine incorrectly calculating size of csv file", NO_DATA_LINES_IN_TEST_CSV_FILE, actualFileLength);
    }

}
