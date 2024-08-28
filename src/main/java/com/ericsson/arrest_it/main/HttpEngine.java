package com.ericsson.arrest_it.main;

import static com.ericsson.arrest_it.common.Constants.*;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;

import javax.net.ssl.SSLException;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.ssl.*;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.arrest_it.common.TestCase;
import com.ericsson.arrest_it.io.CSVParser;
import com.ericsson.arrest_it.logging.LogbackFileUtils;
import com.ericsson.arrest_it.manager.ValidationManager;
import com.ericsson.arrest_it.results.*;

/**
 *This class provides all the http functions: logging into ui, sending csv and grid url requests, initial error checking and parsing of data returned.
 */
/**
 *
 */
public class HttpEngine {
    private final String serverPath;
    private final String uiPassword;
    private final String uiUsername;
    private final String uiPath;
    private CloseableHttpClient httpClient;
    private String sessionIdValue;
    private String sessionIdSsoValue;
    private String ipAddress;
    private BasicCookieStore cookies;
    private String requestId;
    private static final Logger slf4jLogger = LoggerFactory.getLogger(LogbackFileUtils.ARREST_IT_LOGGER);

    public HttpEngine(final String serverPath, final String uiUsername, final String uiPassword, final String requestId) {
        this.serverPath = serverPath;
        this.uiPassword = uiPassword;
        this.uiUsername = uiUsername;
        this.requestId = requestId;
        this.uiPath = createUiPath(serverPath);
    }

    public String createUiPath(final String serverPath) {
        String uiPath = serverPath.substring(0, serverPath.indexOf("/Eniq"));
        uiPath += ENIQ_UI_PATH;
        return uiPath;
    }

    public TestResult start(final String fileName) {
        createHttpClient();
        if (!login()) {
            System.out.println("Cannot Login to Eniq UI");
            slf4jLogger.error("Cannot Login to Eniq UI");
            return new FrameWorkResult("Cannot Login to Eniq UI", false, fileName);

        }
        slf4jLogger.info("Successfully logged into UI");
        return new FrameWorkResult("Successfully Logged in to Eniq UI", true, fileName);
    }

    protected void createHttpClient() {
        final String hostP = serverPath.substring(serverPath.indexOf("://") + 3, serverPath.indexOf("/Eniq"));
        final int timeOutInSeconds = URL_QUERY_TIMEOUT_IN_MINUTES * 60;
        this.cookies = new BasicCookieStore();
        final SSLContextBuilder builder = new SSLContextBuilder();

        try {
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            builder.useSSL();

            final RequestConfig config = RequestConfig.custom().setConnectTimeout(timeOutInSeconds * 1000)
                    .setConnectionRequestTimeout(timeOutInSeconds * 1000).setSocketTimeout(timeOutInSeconds * 1000)
                    .setStaleConnectionCheckEnabled(true).build();

            final List<Header> headers = new ArrayList<Header>();
            headers.add(new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded; charset=UTF-8"));
            headers.add(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate"));
            headers.add(new BasicHeader(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.5"));
            headers.add(new BasicHeader(HttpHeaders.HOST, hostP));
            headers.add(new BasicHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:28.0) Gecko/20100101 Firefox/28.0"));

            this.httpClient = HttpClients.custom()
                    .setSSLSocketFactory(new SSLConnectionSocketFactory(builder.build(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER))
                    .setDefaultCookieStore(this.cookies).setDefaultHeaders(headers).setDefaultRequestConfig(config).build();

        } catch (final KeyManagementException e) {
            slf4jLogger.error(e.getMessage());
        } catch (final NoSuchAlgorithmException e) {
            slf4jLogger.error(e.getMessage());
        } catch (final KeyStoreException e) {
            slf4jLogger.error(e.getMessage());
        }
    }

    protected boolean login() {
        try {
            String sessionIdValue = "", sessionIdSsoValue = "";

            final HttpUriRequest login = RequestBuilder.post().setUri(new URI(uiPath + "login/ValidateUser.jsp"))
                    .addParameter("password", this.uiPassword).addParameter("username", this.uiUsername).setHeader("Connection", "keep-alive")
                    .setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8").setHeader("Cache-Control", "no-cache")
                    .setHeader("Pragma", "no-cache").setHeader("Referer", uiPath).build();
            final CloseableHttpResponse response2 = this.httpClient.execute(login);

            try {
                System.out.println("Request One: " + response2.getStatusLine());
                EntityUtils.consume(response2.getEntity());
            } finally {
                response2.close();
            }
            final HttpUriRequest loginB = RequestBuilder.post().setUri(new URI(uiPath + "EniqEventsUI/j_security_check"))
                    .addParameter("j_password", this.uiPassword).addParameter("j_username", this.uiUsername).setHeader("Connection", "keep-alive")
                    .setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8").build();
            final CloseableHttpResponse response3 = this.httpClient.execute(loginB);
            try {
                System.out.println("Login form get, state 2: " + response2.getStatusLine());
                EntityUtils.consume(response3.getEntity());
            } finally {
                response3.close();
            }

            final CloseableHttpResponse response1B = this.httpClient.execute(new HttpGet(uiPath));
            try {
                System.out.println("Login form get state 3: " + response1B.getStatusLine());
                EntityUtils.consume(response1B.getEntity());
                System.out.println("Logged In cookies:");
                final List<Cookie> cookies = this.cookies.getCookies();
                if (cookies.isEmpty()) {
                    System.out.println("No cookies returned from login");
                } else {
                    for (final Cookie cookie : cookies) {
                        System.out.println("- " + cookie.toString());
                    }
                    sessionIdValue = cookies.get(0).getValue();
                    try {
                        sessionIdSsoValue = cookies.get(1).getValue();
                    } catch (final IndexOutOfBoundsException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            } catch (final IOException e) {
                e.printStackTrace();
            } finally {
                response1B.close();
            }
            this.sessionIdValue = sessionIdValue;
            this.sessionIdSsoValue = sessionIdSsoValue;
            this.ipAddress = InetAddress.getLocalHost().getHostAddress();

        } catch (final HttpHostConnectException e) {
            slf4jLogger.error("Program Terminated!!! Connection refused to server:\n" + e.getMessage());
            System.err.println("Program Terminated!!! Connection refused to server:\n" + e.getMessage());
            System.exit(1);
        } catch (final URISyntaxException e) {
            slf4jLogger.error(e.getMessage());
        } catch (final ClientProtocolException e) {
            slf4jLogger.error(e.getMessage());
        } catch (final IOException e) {
            slf4jLogger.error(e.getMessage());
        }
        return true;
    }

    /**
     * @param testCase
     *        the testCase where any success/failure information from sending request will be stored.
     * @param url
     *        the url request to be sent.
     * @return JSONArray the data or error message returned from url request.
     */
    public JSONArray sendUrlQueries(final TestCase testCase, String url) {
        String returnedJSON = "";
        url = url.trim();
        String responseCode = "";
        slf4jLogger.info("Sending url request - " + serverPath + url);
        CloseableHttpResponse response;
        try {
            final HttpGet httpget = getHttpGet(url);
            response = this.httpClient.execute(httpget);

            try {
                responseCode = response.getStatusLine().toString();
                System.out.println("Query Send: " + responseCode);

                returnedJSON = EntityUtils.toString(response.getEntity());
                EntityUtils.consume(response.getEntity());

            } catch (final IllegalArgumentException iae) {
                returnedJSON = "<No Json Returned>";
                slf4jLogger.warn("Could not parse Json - " + url + " Possible 204 status returned");
            } finally {
                response.close();
            }

            testCase.getTestResultHolder().addTestResult(new JsonNetworkResult("Grid Url sent: " + serverPath + url, true));

        } catch (final FileNotFoundException e) {
            handleException(e, testCase, serverPath + url, false);
        } catch (final UnknownHostException e) {
            handleException(e, testCase, serverPath + url, false);
        } catch (final IOException e) {
            handleException(e, testCase, serverPath + url, false);
        } catch (final IllegalArgumentException iae) {
            handleException(iae, testCase, serverPath + url, false);
        }

        final JSONArray ja = extractJsonArrayFromJsonResponse(testCase, url, returnedJSON, responseCode);
        return ja;
    }

    private JSONArray extractJsonArrayFromJsonResponse(final TestCase testCase, final String url, final String returnedJSON, final String responseCode) {
        JSONArray ja = new JSONArray();

        slf4jLogger.info("Completed url request - " + serverPath + url);

        if (returnedJSON.startsWith("<")) {
            if (returnedJSON.contains("HTTP Status 404")) {
                ja = new JSONArray();
                final JSONObject errorJsonObject = new JSONObject();
                testCase.getTestResultHolder().addTestResult(new JsonNetworkResult("404 Error. Glassfish may have went down.", false));
                errorJsonObject.append("errorDescription", "404 Error. Glassfish may have went down.");
                ja.put(errorJsonObject);
            } else {
                ja = new JSONArray();
                final JSONObject errorJsonObject = new JSONObject();
                testCase.getTestResultHolder().addTestResult(
                        new JsonNetworkResult("Unknown Error, status: " + responseCode + " json: " + returnedJSON, false));
                errorJsonObject.append("errorDescription", "Unknown Error: " + returnedJSON);
                ja.put(errorJsonObject);
            }
        } else {

            try {

                final JSONObject jo = new JSONObject(returnedJSON.trim());

                final Object test = jo.get("data");
                if (test instanceof JSONArray) {
                    ja = jo.getJSONArray("data");
                } else {
                    final JSONObject tmp = jo.getJSONObject("data");
                    final Iterator<?> keyIterator = tmp.keys();
                    final JSONArray jsonArray = new JSONArray();

                    while (keyIterator.hasNext()) {
                        final String key = (String) keyIterator.next();
                        jsonArray.put(tmp.get(key));
                    }

                    if (!isJsonObjectEmpty(tmp)) {
                        ja.put(tmp);
                    }
                }
                testCase.getTestResultHolder().addTestResult(new JsonParsingResult("Grid response has valid data", true));

            } catch (final Exception e) {
                ja = new JSONArray();
                final JSONObject errorJsonObject = new JSONObject();
                if (responseCode.contains("200")) {
                    errorJsonObject.append("errorDescription", "Unknown error could not parse returned Json: " + returnedJSON.trim());
                    testCase.getTestResultHolder().addTestResult(
                            new JsonParsingResult("Unknown error could not parse returned Json: \n" + returnedJSON.trim(), false));
                } else {
                    errorJsonObject.append("errorDescription", responseCode);
                    testCase.getTestResultHolder().addTestResult(
                            new JsonParsingResult("Unknown error could not parse returned Json: " + responseCode, false));

                }

                ja.put(errorJsonObject);
            }
        }
        return ja;
    }

    private HttpGet getHttpGet(final String url) {
        final HttpGet httpget = new HttpGet(serverPath + url);
        System.out.println(httpget.toString());
        httpget.setHeader("Accept", "application/json");
        httpget.setHeader("Cache-Control", "no-cache, must-revalidate");
        httpget.setHeader("Cookie", "	JSESSIONID=" + this.sessionIdValue + "; JSESSIONIDSSO=" + this.sessionIdSsoValue);
        httpget.setHeader("Expires", "Sat, 29 Oct 1994 19:43:31 GMT");
        httpget.setHeader("Last-Modified", "Sat, 29 Oct 1994 19:43:31 GMT");
        httpget.setHeader("Pragma", "no-cache");
        httpget.setHeader("requestId", requestId);
        httpget.setHeader("srcIpAddress", this.ipAddress);
        httpget.setHeader("userName", this.uiUsername);
        return httpget;
    }

    private boolean isJsonObjectEmpty(final JSONObject tmp) {
        return tmp.toString().equals("{}");
    }

    /**
     * @param testCase
     *        the testCase containing the csv url request to be sent. Will also store any success/failure results relating to sending the Url request.
     * @return JSONArray the data contained in the csv file, once it has been parsed to JSON by the CSVParser class.
     */
    public JSONArray sendCsvUrl(final TestCase testCase) {
        String csvUrl = testCase.getCsvUrl();
        csvUrl = csvUrl.trim();

        JSONArray ja = new JSONArray();
        final JSONObject errorJsonObject = new JSONObject();
        String responseCode = "";

        int noOfCsvRowsToStore = testCase.getMaxRowCount();
        if (noOfCsvRowsToStore == 0) {
            noOfCsvRowsToStore = ValidationManager.getMaxRowsFromUrl(testCase.getUrl());
        }

        CloseableHttpResponse response;
        final List<String> csvLines = new ArrayList<String>();
        int counter = -1;
        final CSVParser csvParser = new CSVParser();
        slf4jLogger.info("Sending CSV request - " + uiPath + csvUrl);
        try {
            final HttpGet httpget = getHttpGetForCSVRequest(csvUrl);
            response = this.httpClient.execute(httpget);
            responseCode = response.getStatusLine().toString();
            slf4jLogger.info("Completed CSV Url request - " + uiPath + csvUrl);
            try {
                final HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStreamReader inputStreamReader = null;
                    BufferedReader br = null;
                    try {
                        inputStreamReader = new InputStreamReader(entity.getContent(), "ISO-8859-1");
                        br = new BufferedReader(inputStreamReader);
                        slf4jLogger.info("Started reading csvUrl stream - " + csvUrl);
                        String line = br.readLine();
                        while (line != null && counter < noOfCsvRowsToStore) {
                            csvLines.add(line);
                            counter++;
                            line = br.readLine();
                        }

                        while (line != null) {
                            counter++;
                            try {
                                line = br.readLine();
                            } catch (final SocketException se) {
                                line = null;
                                handleException(se, testCase, csvUrl, true);
                                errorJsonObject.append("errorDescription", "Socket exception while reading csv stream: " + se.getMessage());
                            } catch (final SSLException sl) {
                                line = null;
                                handleException(sl, testCase, csvUrl, true);
                                errorJsonObject.append("errorDescription", "SSL exception while reading csv stream: " + sl.getMessage());
                            }

                        }
                        slf4jLogger.info("Finished reading csvUrl stream - Length: " + counter);
                    } finally {
                        inputStreamReader.close();
                        br.close();
                    }
                }
                EntityUtils.consume(response.getEntity());

                testCase.getTestResultHolder().addTestResult(new CsvNetworkResult("Successfully sent CSV url request " + csvUrl, true));

            } catch (final Exception e) {
                handleException(e, testCase, csvUrl, true);
                errorJsonObject.append("errorDescription", "Unknown error could not obtain CSV data: " + responseCode);
                ja.put(errorJsonObject);
            } finally {
                response.close();
            }

        } catch (final FileNotFoundException e) {
            handleException(e, testCase, csvUrl, true);
        } catch (final UnknownHostException e) {
            handleException(e, testCase, csvUrl, true);
        } catch (final IOException e) {
            handleException(e, testCase, csvUrl, true);
        } catch (final IllegalArgumentException iae) {
            handleException(iae, testCase, csvUrl, true);
        }
        slf4jLogger.info("Started Parsing CSV Url request - " + csvUrl);
        ja = csvParser.parseCsvExport(csvLines, testCase);
        slf4jLogger.info("Completed Parsing CSV Url request - " + uiPath + csvUrl);
        testCase.setSizeOfCsvFile(counter);
        return ja;
    }

    private HttpGet getHttpGetForCSVRequest(final String csvUrl) {
        final HttpGet httpget = new HttpGet(uiPath + csvUrl);
        httpget.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        httpget.setHeader("Cookie", "	JSESSIONID=" + this.sessionIdValue + "; JSESSIONIDSSO=" + this.sessionIdSsoValue);
        httpget.setHeader("Expires", "Sat, 29 Oct 1994 19:43:31 GMT");
        httpget.setHeader("Last-Modified", "Sat, 29 Oct 1994 19:43:31 GMT");
        httpget.setHeader("Accept-Encoding", "gzip,deflate,sdch");
        httpget.setHeader("Accept-Language", "en-GB,en-US;q=0.8,en;q=0.6");
        return httpget;
    }

    public void logout() {
        // TODO Create method to log out of UI
    }

    public void stop() {
        try {
            // logout();
            try {
                this.httpClient.close();
            } catch (final NullPointerException ne) {
                slf4jLogger.warn("Attempted to shut down http client before client was created");
            }

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void handleException(final Exception exception, final TestCase testCase, final String url, final Boolean isCSV) {
        exception.printStackTrace();
        slf4jLogger.error(testCase.getDirection() + "\t" + url + "\t" + exception.getMessage());

        if (isCSV) {
            testCase.getTestResultHolder().addTestResult(
                    new CsvNetworkResult("Exception occured while obtaining data: " + exception.getMessage() + "\nFor url: " + url, false));
        } else {
            testCase.getTestResultHolder().addTestResult(
                    new JsonNetworkResult("Exception occured while obtaining data: " + exception.getMessage() + "\nFor url: " + url, false));

        }

    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(final String requestId) {
        this.requestId = requestId;
    }

}
