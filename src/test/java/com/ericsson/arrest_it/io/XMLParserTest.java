package com.ericsson.arrest_it.io;

import static org.junit.Assert.*;

import java.util.List;

import junit.extensions.PA;

import org.junit.Test;

import com.ericsson.arrest_it.common.TestCase;

public class XMLParserTest {
    XMLParser xm = new XMLParser("src/test/resources/ServiceTestXml.xml", true);

    @Test
    public void test_XMLParserCreation() {
        final XMLParser testXm = new XMLParser("testFileName", true);
        final String fileName = (String) PA.getValue(testXm, "filename");
        assertEquals("testFileName", fileName);
    }

    @Test
    public void test_XMLParsing() throws Exception {

        final List<TestCase> suite = xm.parse();
        System.out.println(suite.size());
        for (final TestCase tc : suite) {
            System.out.println(tc.getTitle());
            System.out.println(tc.getUrl());
            System.out.println(tc.getPrecondition());
        }

        for (final TestCase tc : suite) {
            System.out.println(tc.getDirection());
            assertTrue(tc.getDirection().length() > 0);
            System.out.println(tc.getTitle());
            assertTrue(isNumeric(tc.getTitle()));
            assertTrue(tc.getDbQueries().size() > 1); // True for ServiceTestXML.xml

        }

    }

    @SuppressWarnings("unused")
    public boolean isNumeric(final String str) {
        try {
            final double d = Double.parseDouble(str);
        } catch (final NumberFormatException nfe) {
            return false;
        }
        return true;
    }

}
