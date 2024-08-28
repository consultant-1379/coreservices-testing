package com.ericsson.arrest_it.io;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.math.NumberUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.ericsson.arrest_it.common.*;
import com.ericsson.arrest_it.main.TestDriver;

public class XMLParser {
    private final String filename;
    private CopyOnWriteArrayList<TestCase> suite;
    private final boolean isSucRawOn;
    public Map<String, String> directionsMap;

    public XMLParser(final String filename, final boolean sucRawOn) {
        this.filename = filename;
        this.isSucRawOn = sucRawOn;
        this.directionsMap = new HashMap<String, String>();
    }

    public CopyOnWriteArrayList<TestCase> parse() throws Exception {

        final SAXParserFactory factory = SAXParserFactory.newInstance();
        final SAXParser saxParser = factory.newSAXParser();

        final DefaultHandler handler = new DefaultHandler() {

            TestCase testCase;
            String passToDrillDownID = "", sqlInstruction = "", noOfDecimalPlaces, preconditionSql = "";
            String tempString = "";

            boolean isTestCaseActive = false, isDirectionActive = false, isUrlActive = false, isRepeatValidationActive = false,
                    isSqlTestActive = false, isValidationActive = false, isJsonVariablesActive = false, isRepeatDrillActive = false,
                    isPreConditionActive = false, isPreConditionSqlActive = false, isTestSuiteActive = false, isPassToDrillActive = false,
                    sqlInstructionPresent = false, isMaxValsActive = false, isMaxRowCountActive = false, isCSVUrlActive = false,
                    isIgnoredInCsvActive = false, isNoOfDecimalsActive = false, shouldAddSql = true;

            @Override
            public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {

                if (!isTagValid(qName)) {
                    createSaxExceptionForInvalidTag(qName);
                }

                if (qName.equalsIgnoreCase("TESTSUITE")) {
                    suite = new CopyOnWriteArrayList<TestCase>();
                }

                if (qName.equalsIgnoreCase("TESTCASE")) {
                    this.testCase = new TestCase();
                    this.testCase.setTitle(attributes.getValue("id").replaceAll(" ", ""));
                    this.testCase.setTestType(attributes.getValue("type"));
                    isTestCaseActive = true;
                }
                if (qName.equalsIgnoreCase("DIRECTION")) {
                    isDirectionActive = true;
                }
                if (qName.equalsIgnoreCase("URL")) {
                    isUrlActive = true;
                }
                if (qName.equalsIgnoreCase("CSVURL") && TestDriver.SHOULDVALIDATECSV) {
                    isCSVUrlActive = true;
                }
                if (qName.equalsIgnoreCase("MAXVALIDATION")) {
                    isMaxValsActive = true;
                }
                if (qName.equalsIgnoreCase("MAXROWCOUNT")) {
                    isMaxRowCountActive = true;
                }

                if (qName.equalsIgnoreCase("REPEATVALIDATION")) {
                    this.testCase.setRepeatValidationInstruction(attributes.getValue("instruction"));
                    isRepeatValidationActive = true;
                }
                if (qName.equalsIgnoreCase("SQLTEST")) {
                    if (attributes.getValue("instruction") != null) {
                        sqlInstruction = attributes.getValue("instruction");
                        sqlInstructionPresent = true;
                    }
                    if (attributes.getValue("success_raw") != null) {
                        shouldAddSql = Boolean.parseBoolean(attributes.getValue("success_raw"));
                        if (shouldAddSql == isSucRawOn) {
                            shouldAddSql = true;
                        } else {
                            shouldAddSql = false;
                        }
                    } else {
                        shouldAddSql = true;
                    }
                    isSqlTestActive = true;
                }
                if (qName.equalsIgnoreCase("VALIDATE")) {
                    if (attributes.getValue("precision") != null) {
                        noOfDecimalPlaces = attributes.getValue("precision");
                        isNoOfDecimalsActive = true;
                    }
                    isValidationActive = true;
                }
                if (qName.equalsIgnoreCase("JSONVARIABLES")) {
                    isJsonVariablesActive = true;
                }
                if (qName.equalsIgnoreCase("IGNOREDINCSV")) {
                    isIgnoredInCsvActive = true;
                }
                if (qName.equalsIgnoreCase("REPEATDRILLDOWN")) {
                    this.testCase.setRepeatDrillDownInstruction(attributes.getValue("instruction"));
                    isRepeatDrillActive = true;
                }
                if (qName.equalsIgnoreCase("PASSTODRILLDOWN")) {
                    this.passToDrillDownID = attributes.getValue("id");
                    isPassToDrillActive = true;
                }
                if (qName.equalsIgnoreCase("PRECONDITION")) {
                    isPreConditionActive = true;
                }
                if (qName.equalsIgnoreCase("PRECONDITIONSQL")) {
                    isPreConditionSqlActive = true;
                }
            }

            @Override
            public void endElement(final String uri, final String localName, final String qName) throws SAXException {
                if (qName.equalsIgnoreCase("TESTCASE")) {
                    suite.add(this.testCase);
                }

                if (qName.equalsIgnoreCase("REPEATVALIDATION")) {
                    isRepeatValidationActive = false;
                }

                if (qName.equalsIgnoreCase("SQLTEST")) {
                    sqlInstruction = "";
                    sqlInstructionPresent = false;
                }
                if (qName.equalsIgnoreCase("VALIDATE")) {
                    noOfDecimalPlaces = "";
                    isNoOfDecimalsActive = false;
                }

            }

            @Override
            public void characters(final char ch[], final int start, final int length) throws SAXException {
                if (isTestSuiteActive) {
                    isTestSuiteActive = false;
                }
                if (isDirectionActive) {
                    tempString = new String(ch, start, length);
                    tempString = tempString.replaceAll("\\r\\n|\\r|\\n", "");
                    this.testCase.setDirection(tempString);
                    this.testCase.setDirectionWithOutRowInfo(tempString);
                    isDirectionActive = false;
                }
                if (isIgnoredInCsvActive) {
                    final List<Integer> ignoredListIds = new ArrayList<Integer>();

                    String ignoredString = new String(ch, start, length);
                    ignoredString = ignoredString.replaceAll("\\r\\n|\\r|\\n", "");
                    final String[] ignoredListElements = ignoredString.split(",");
                    for (final String subElement : ignoredListElements) {
                        final String[] subElementSplit = subElement.split(":");
                        for (final String element : subElementSplit) {
                            if (NumberUtils.isDigits(element)) {
                                ignoredListIds.add(Integer.parseInt(element));
                            }
                        }
                    }
                    this.testCase.setIgnoredInCsv(ignoredListIds);
                    isIgnoredInCsvActive = false;
                }
                if (isTestCaseActive) {
                    isTestCaseActive = false;
                }
                if (isMaxValsActive) {
                    this.testCase.setMaxVals(Integer.parseInt(new String(ch, start, length).replaceAll("\\r\\n|\\r|\\n", "")));
                    isMaxValsActive = false;
                }
                if (isMaxRowCountActive) {
                    this.testCase.setMaxRowCount(Integer.parseInt(new String(ch, start, length).replaceAll("\\r\\n|\\r|\\n", "")));
                    isMaxRowCountActive = false;
                }

                if (isUrlActive) {
                    this.testCase.setUrl(new String(ch, start, length).replaceAll("\\r\\n|\\r|\\n", ""));
                    isUrlActive = false;
                }
                if (isCSVUrlActive) {
                    this.testCase.setCsvUrl(new String(ch, start, length).replaceAll("\\r\\n|\\r|\\n", ""));
                    isCSVUrlActive = false;
                }
                if (isSqlTestActive) {
                    String templateStatement = new String(ch, start, length).replaceAll("\\r\\n|\\r|\\n", "");

                    if (shouldAddSql) {
                        if (sqlInstructionPresent) {
                            this.testCase.addDbQuery(new DBQuery(templateStatement, isRepeatValidationActive, sqlInstruction));
                        } else {
                            this.testCase.addDbQuery(new DBQuery(templateStatement, isRepeatValidationActive));
                        }
                    }

                    isSqlTestActive = false;
                }
                if (isValidationActive) {
                    int numberOfDecimals = 2;

                    if (isNoOfDecimalsActive) {
                        numberOfDecimals = Integer.parseInt(noOfDecimalPlaces);
                    }

                    final String rawValidation = new String(ch, start, length).replaceAll("\\r\\n|\\r|\\n", "");
                    if (rawValidation.contains(",")) {
                        final String[] splitVals = rawValidation.split(",");
                        for (final String v : splitVals) {
                            final Validation newValidation = new Validation(v, isRepeatValidationActive, numberOfDecimals);
                            this.testCase.addValidation(newValidation);
                        }
                    } else {
                        this.testCase.addValidation(new Validation(rawValidation, isRepeatValidationActive, numberOfDecimals));
                    }

                    isValidationActive = false;
                }
                if (isJsonVariablesActive) {
                    final String jsonVarsString = new String(ch, start, length).replaceAll("\\r\\n|\\r|\\n", "");
                    final String[] jsonVars = jsonVarsString.split(",");
                    for (final String jsonVar : jsonVars) {
                        final String[] keyValues = jsonVar.split(":");
                        this.testCase.addJsonVariable(keyValues[1], keyValues[0]);
                    }
                    isJsonVariablesActive = false;
                }
                if (isPassToDrillActive) {
                    String passToDrillVars = new String(ch, start, length).replaceAll("\\r\\n|\\r|\\n", "");
                    passToDrillVars = passToDrillVars.replaceAll(" ", "");
                    final String[] passVars = passToDrillVars.split(",");
                    final List<String> passValues = new ArrayList<String>();
                    Collections.addAll(passValues, passVars);
                    this.testCase.addPassToDrill(passToDrillDownID, passValues);
                    isPassToDrillActive = false;
                }
                if (isRepeatDrillActive) {
                    final String repeatDrill = new String(ch, start, length).replaceAll("\\r\\n|\\r|\\n", "");
                    if (repeatDrill.contains(",")) {
                        final String[] repeats = repeatDrill.split(",");
                        for (final String s : repeats) {
                            this.testCase.addRepeatDrillDown(s);
                        }
                    } else {
                        this.testCase.addRepeatDrillDown(repeatDrill);
                    }

                    isRepeatDrillActive = false;
                }

                if (isPreConditionSqlActive) {
                    preconditionSql = new String(ch, start, length).replaceAll("\\r\\n|\\r|\\n", "");
                    isPreConditionSqlActive = false;
                }

                if (isPreConditionActive) {
                    if (preconditionSql.isEmpty()) {
                        final Precondition precondition = new Precondition(new String(ch, start, length).replaceAll("\\r\\n|\\r|\\n", ""));
                        this.testCase.setPrecondition(precondition);
                    } else {
                        final Precondition precondition = new Precondition(preconditionSql, new String(ch, start, length).replaceAll(
                                "\\r\\n|\\r|\\n", ""));
                        this.testCase.setPrecondition(precondition);
                    }

                    preconditionSql = "";
                    isPreConditionActive = false;
                }
            }
        };
        saxParser.parse(this.filename, handler);
        enrichTestCaseDirectionsB(suite);
        for (final TestCase tc : suite) {
            System.out.println(tc.getTitle() + "\t" + tc.getDirectionWithOutRowInfo());
        }
        return suite;
    }

    public void enrichTestCaseDirectionsB(final List<TestCase> testsToEnrich) {
        initializeDirectionsMap(testsToEnrich);
        String drillToFind = "";
        String newDirection;
        for (final TestCase tc : testsToEnrich) {
            drillToFind = tc.getTitle();
            newDirection = tc.getDirection();
            while (drillToFind != null) {
                if (!drillToFind.equals(tc.getTitle())) {
                    newDirection = directionsMap.get(drillToFind) + " -> " + newDirection;
                }

                drillToFind = lookForDrill(drillToFind, suite);
            }

            tc.setDirectionWithOutRowInfo(newDirection);
        }

    }

    public String lookForDrill(final String drillToFind, final List<TestCase> suite) {

        for (final TestCase tc : suite) {
            if (tc.getRepeatDrillDown().contains(drillToFind)) {
                return tc.getTitle();
            }
        }

        return null;
    }

    public void initializeDirectionsMap(final List<TestCase> suite) {
        for (final TestCase tc : suite) {
            directionsMap.put(tc.getTitle(), tc.getDirection());
        }
    }

    public boolean isTagValid(final String tag) {
        for (String validTag : Constants.VALID_XML_TAGS) {
            if (tag.equalsIgnoreCase(validTag)) {
                return true;
            }
        }
        return false;
    }

    public void createSaxExceptionForInvalidTag(final String invalidTag) throws SAXException {
        String validTags = "";
        for (String validTag : Constants.VALID_XML_TAGS) {
            validTags += validTag + ", ";
        }

        validTags = validTags.substring(0, validTags.length() - 2);

        throw new SAXException(" Invalid Tag: " + invalidTag + " Xml tag should be one of: " + validTags);
    }
}
