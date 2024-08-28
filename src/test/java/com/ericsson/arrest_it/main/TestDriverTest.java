package com.ericsson.arrest_it.main;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.ericsson.arrest_it.common.DBQuery;
import com.ericsson.arrest_it.common.TestCase;
import com.ericsson.arrest_it.io.XMLParser;

public class TestDriverTest {

    @Test
    public void test_TestCaseCloneing() {
        final XMLParser xm = new XMLParser("src/test/resources/ServiceTestXml.xml", true);

        List<TestCase> suite = null;
        try {
            suite = xm.parse();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        final TestCase tc1 = suite.get(0);

        final TestCase tc2 = new TestCase(tc1);
        tc1.setDirection("This is a test");
        assertFalse(tc1.getDirection().equals(tc2.getDirection()));

        tc1.addDbQuery(new DBQuery("This is also a test", true));

        assertFalse(tc1.getDbQueries().size() == tc2.getDbQueries().size());

    }
}
