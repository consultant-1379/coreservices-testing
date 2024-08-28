package com.ericsson.arrest_it.main.cli;

import org.apache.commons.cli.*;

import com.ericsson.arrest_it.common.Constants;

public class GroupManagementCMDLine {
    private final CommandLine cmd;

    public GroupManagementCMDLine(final String[] args) throws ParseException {
        final CommandLineParser cmdLineParser = new BasicParser();
        this.cmd = cmdLineParser.parse(new GroupManagementCLIOptions(), args);
    }

    public Boolean isCreateGroups() {
        return this.cmd.hasOption(Constants.CREATE);
    }

    public Boolean isDeleteGroups() {
        return this.cmd.hasOption(Constants.DELETE);
    }
}
