package com.ericsson.arrest_it.main.cli;

import static org.junit.Assert.*;

import org.junit.Test;

public class GroupManagementCLIOptionsTest {

    @Test
    public void testCLINotNull() {
        final GroupManagementCLIOptions cli = new GroupManagementCLIOptions();
        assertNotNull(cli);
    }
}
