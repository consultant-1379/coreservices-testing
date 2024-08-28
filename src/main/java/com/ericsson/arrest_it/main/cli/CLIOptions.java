package com.ericsson.arrest_it.main.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.ericsson.arrest_it.common.Constants;

public class CLIOptions extends Options {

    private static final long serialVersionUID = 1L;

    public CLIOptions() {
        addOption(createAllTestOption());
        addOption(createFileTestOption());
        addOption(addTimeStampsOption());
    }

    private Option createAllTestOption() {
        final Option option = new Option(Constants.ALL_TESTS, "Run all tests");
        option.setRequired(false);
        return option;
    }

    private Option createFileTestOption() {
        final Option option = new Option(Constants.FILE, true,
                "File to run test on. Must provide full path (Core_PS_Network/Core_Ranking/Subscriber.xml)");
        option.setRequired(false);
        return option;
    }

    private Option addTimeStampsOption() {
        final Option option = new Option(Constants.TIMESTAMP, false, "Flag to add timestamps to interim file names (Use for multi-user runs)");
        option.setRequired(false);
        return option;
    }
}
