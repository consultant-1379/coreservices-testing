package com.ericsson.arrest_it.main.cli;

import static org.junit.Assert.*;

import org.apache.commons.cli.ParseException;
import org.junit.Test;

public class GroupManagementCMDLineTest {

    @Test
    public void test_isCreateGroups_true() {
        final String[] args = new String[2];
        args[0] = "applicationname";
        args[1] = "-create";

        GroupManagementCMDLine cmdLine = null;
        try {
            cmdLine = new GroupManagementCMDLine(args);
            assertNotNull(cmdLine);
            final Boolean isCreateGroups = cmdLine.isCreateGroups();
            assertTrue(isCreateGroups);
        } catch (final ParseException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void test_isCreateGroups_false() {
        final String[] args = new String[1];
        args[0] = "applicationname";

        GroupManagementCMDLine cmdLine = null;
        try {
            cmdLine = new GroupManagementCMDLine(args);
            assertNotNull(cmdLine);
            final Boolean isCreateGroups = cmdLine.isCreateGroups();
            assertFalse(isCreateGroups);
        } catch (final ParseException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void test_isDeleteGroups_true() {
        final String[] args = new String[2];
        args[0] = "applicationname";
        args[1] = "-delete";

        GroupManagementCMDLine cmdLine = null;
        try {
            cmdLine = new GroupManagementCMDLine(args);
            assertNotNull(cmdLine);
            final Boolean isDeleteGroups = cmdLine.isDeleteGroups();
            assertTrue(isDeleteGroups);
        } catch (final ParseException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void test_isDeleteGroups_false() {
        final String[] args = new String[1];
        args[0] = "applicationname";

        GroupManagementCMDLine cmdLine = null;
        try {
            cmdLine = new GroupManagementCMDLine(args);
            assertNotNull(cmdLine);
            final Boolean isDeleteGroups = cmdLine.isDeleteGroups();
            assertFalse(isDeleteGroups);
        } catch (final ParseException e) {
            fail(e.getMessage());
        }
    }
}
