package com.ericsson.arrest_it.url;

import static com.ericsson.arrest_it.common.Constants.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.*;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class CheckXMLsForCommonURL {
    private static final Map<String, String> filesWithCommonURLs = new HashMap<String, String>();

    public static void main(final String[] args) throws ParserConfigurationException, SAXException, IOException {
        System.out.println("URL: " + args[0] + "\n");

        final File testFolder = findTestDirectory();
        findCommonURLs(testFolder, args[0]);
        for (final String key : filesWithCommonURLs.keySet()) {
            System.out.println("---------------------------------------  Found URL!!!  ---------------------------------------------------");
            System.out.println("File: " + key);
            System.out.println("URL: " + filesWithCommonURLs.get(key) + "\n");
        }
    }

    private static File findTestDirectory() {
        final File rootDirectory = new File(".");
        final StringBuilder testDirBuilder = new StringBuilder();
        try {
            testDirBuilder.append(rootDirectory.getCanonicalPath());
            testDirBuilder.append(SEPARATOR);
            testDirBuilder.append("src");
            testDirBuilder.append(SEPARATOR);
            testDirBuilder.append("main");
            testDirBuilder.append(SEPARATOR);
            testDirBuilder.append("resources");
            testDirBuilder.append(SEPARATOR);
            testDirBuilder.append("TestCases");
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return new File(testDirBuilder.toString());
    }

    private static void findCommonURLs(final File folder, final String url) throws ParserConfigurationException, SAXException, IOException {
        for (final File file : folder.listFiles()) {
            if (file.isDirectory()) {
                findCommonURLs(file, url);
            } else if (file.isFile()) {
                checkFileForCommonURL(file, url);
            }
        }
    }

    private static void checkFileForCommonURL(final File file, final String url) throws ParserConfigurationException, SAXException, IOException {

        SAXParserFactory factory;
        factory = SAXParserFactory.newInstance();
        final SAXParser saxParser = factory.newSAXParser();

        final DefaultHandler handler = new DefaultHandler() {

            String parsedUrl = "";
            boolean foundUrl = false;

            @Override
            public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
                if (qName.equalsIgnoreCase("URL")) {
                    foundUrl = true;
                }
            }

            @Override
            public void endElement(final String uri, final String localName, final String qName) throws SAXException {
            }

            @Override
            public void characters(final char ch[], final int start, final int length) throws SAXException {

                if (foundUrl) {
                    parsedUrl = new String(ch, start, length);
                    final boolean common = checkIfURLsAreCommon(parsedUrl, url);
                    if (common) {
                        try {
                            filesWithCommonURLs.put(file.getCanonicalPath(), parsedUrl);
                        } catch (final IOException e) {
                            e.printStackTrace();
                        }
                    }
                    foundUrl = false;
                }
            }
        };
        saxParser.parse(file, handler);
    }

    private static boolean checkIfURLsAreCommon(final String parsedUrl, final String url) {
        boolean common = true;

        final String[] splitUrl = url.split("\\?")[1].split("&");
        final String[] splitParsedUrl = parsedUrl.split("\\?")[1].split("&");

        if (!arePathsTheSame(parsedUrl, url) || splitUrl.length != splitParsedUrl.length) {
            return false;
        }

        for (final String aSplitUrl : splitUrl) {
            final String param = aSplitUrl.split(EQUALS)[0];
            boolean foundParam = false;

            for (String parsedParam : splitParsedUrl) {
                parsedParam = parsedParam.split(EQUALS)[0];
                if (parsedParam.equals(param)) {
                    foundParam = true;
                    break;
                }
            }

            if (!foundParam) {
                common = false;
            }
        }

        return common;
    }

    private static boolean arePathsTheSame(final String parsedUrl, final String url) {
        return url.split("\\?")[0].equals(parsedUrl.split("\\?")[0]);
    }
}
