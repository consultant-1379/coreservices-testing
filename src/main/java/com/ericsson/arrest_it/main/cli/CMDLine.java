package com.ericsson.arrest_it.main.cli;

import org.apache.commons.cli.*;

import com.ericsson.arrest_it.common.Constants;

public class CMDLine {
    private final CommandLine cmd;

    public CMDLine(final String[] args) throws ParseException {

        final CommandLineParser cmdLineParser = new BasicParser();
        this.cmd = cmdLineParser.parse(new CLIOptions(), args);
    }

    /**
     * Determine if the "-all" argument is entered in the program arguments.
     *
     * @return true if -all is present otherwise false.
     */
    public Boolean isRunAllTests() {
        return this.cmd.hasOption(Constants.ALL_TESTS);
    }

    /**
     * Get the test file to run
     *
     * @return Test file to run
     */
    public String getTestFile() {
        return this.processFilePath(this.cmd.getOptionValue(Constants.FILE));
    }

    /**
     * Is there a test file specified?
     *
     * @return true|false.
     */
    public Boolean isTestFile() {
        return this.cmd.hasOption(Constants.FILE);
    }

    private String processFilePath(String filePath) {
        if (!filePath.contains(Constants.SEPARATOR)) {
            final String[] fileParts = filePath.split("\\\\");

            final StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < fileParts.length; i++) {
                stringBuilder.append(fileParts[i]);
                if (i < fileParts.length - 1) {
                    stringBuilder.append(Constants.SEPARATOR);
                }
            }
            filePath = stringBuilder.toString();
        }

        return filePath;
    }

    public Boolean isTimeStamp() {
        return this.cmd.hasOption(Constants.TIMESTAMP);
    }
}
