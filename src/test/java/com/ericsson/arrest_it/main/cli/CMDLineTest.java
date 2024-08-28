package com.ericsson.arrest_it.main.cli;

import static org.junit.Assert.*;

import org.apache.commons.cli.ParseException;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.arrest_it.common.Constants;

public class CMDLineTest {
    private String[] args;

    @Before
    public void setup() {
        this.args = new String[1];
        args[0] = "applicationname";
    }

    @Test
    public void testGetRunAllTests_NO() {
        CMDLine cmdLine = null;
        try {
            cmdLine = new CMDLine(this.args);
            assertNotNull(cmdLine);
            final Boolean isRunAllTests = cmdLine.isRunAllTests();
            assertFalse(isRunAllTests);
        } catch (final ParseException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetRunAllTests_YES() {
        final String[] arguments = new String[2];
        arguments[0] = "applicationname";
        arguments[1] = "-all";

        CMDLine cmdLine = null;
        try {
            cmdLine = new CMDLine(arguments);
            assertNotNull(cmdLine);
            final Boolean isRunAllTests = cmdLine.isRunAllTests();
            assertTrue(isRunAllTests);
        } catch (final ParseException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testIsFile() {
        final String[] arguments = new String[1];
        arguments[0] = "applicationname";

        CMDLine cmdLine = null;
        try {
            cmdLine = new CMDLine(arguments);
            assertNotNull(cmdLine);
            final Boolean isFile = cmdLine.isTestFile();
            assertFalse(isFile);
        } catch (final ParseException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetFile() {
        final String[] arguments = new String[3];
        arguments[0] = "applicationname";
        arguments[1] = "-f";
        arguments[2] = "Core_PS_Network\\Core_Ranking\\Subscriber.xml";

        final StringBuilder sb = new StringBuilder();
        sb.append("Core_PS_Network");
        sb.append(Constants.SEPARATOR);
        sb.append("Core_Ranking");
        sb.append(Constants.SEPARATOR);
        sb.append("Subscriber.xml");
        final String expected = sb.toString();

        CMDLine cmdLine = null;
        try {
            cmdLine = new CMDLine(arguments);
            assertNotNull(cmdLine);
            final String fileName = cmdLine.getTestFile();
            assertEquals("The file name is not a match", expected, fileName);
        } catch (final ParseException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetFileUnixSeparator() {
        final String[] arguments = new String[3];
        arguments[0] = "applicationname";
        arguments[1] = "-f";
        arguments[2] = "Core_PS_Network" + Constants.SEPARATOR + "Core_Ranking" + Constants.SEPARATOR + "Subscriber.xml";

        final StringBuilder sb = new StringBuilder();
        sb.append("Core_PS_Network");
        sb.append(Constants.SEPARATOR);
        sb.append("Core_Ranking");
        sb.append(Constants.SEPARATOR);
        sb.append("Subscriber.xml");
        final String expected = sb.toString();

        CMDLine cmdLine = null;
        try {
            cmdLine = new CMDLine(arguments);
            assertNotNull(cmdLine);
            final String fileName = cmdLine.getTestFile();
            assertEquals("The file name is not a match", expected, fileName);
        } catch (final ParseException e) {
            fail(e.getMessage());
        }
    }
}
