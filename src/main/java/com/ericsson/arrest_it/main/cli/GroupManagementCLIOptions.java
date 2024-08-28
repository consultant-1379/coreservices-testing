package com.ericsson.arrest_it.main.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.ericsson.arrest_it.common.Constants;

public class GroupManagementCLIOptions extends Options {

    private static final long serialVersionUID = 1L;

    public GroupManagementCLIOptions() {
        addOption(create());
        addOption(delete());
    }

    private Option create() {
        final Option option = new Option(Constants.CREATE, "Creates Groups to use for ARREST-IT test cases.");
        option.setRequired(false);
        return option;
    }

    private Option delete() {
        final Option option = new Option(Constants.DELETE, "Deletes Groups that where created by ARREST-IT application..");
        option.setRequired(false);
        return option;
    }
}
