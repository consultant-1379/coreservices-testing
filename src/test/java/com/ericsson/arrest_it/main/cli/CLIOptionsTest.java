package com.ericsson.arrest_it.main.cli;

import static org.junit.Assert.*;

import org.junit.Test;

public class CLIOptionsTest {

    @Test
    public void testCLINotNull() {
        final CLIOptions cli = new CLIOptions();
        assertNotNull(cli);
    }
}
