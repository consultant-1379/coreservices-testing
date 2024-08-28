package com.ericsson.arrest_it.main;

import java.io.File;

import org.apache.commons.cli.ParseException;

import com.ericsson.arrest_it.common.ArrestItException;
import com.ericsson.arrest_it.common.Constants;
import com.ericsson.arrest_it.main.cli.GroupManagementCMDLine;
import com.ericsson.arrest_it.manager.*;
import com.ericsson.arrest_it.utils.GroupUtils;

public class GroupManagementMain {

    public static void main(final String[] args) throws ParseException, ArrestItException {
        final GroupManagementCMDLine cmdLine = new GroupManagementCMDLine(args);

        final DBManager dbManager = (DBManager) ManagerFactory.getInstance().getManager(Constants.DB_MANAGER);
        final File config = Constants.getGroupManagmentFile();

        try {
            if (cmdLine.isCreateGroups()) {
                GroupUtils.insertGroupsIntoTables(config, dbManager);
            } else if (cmdLine.isDeleteGroups()) {
                GroupUtils.deleteGroupsFromTables(config, dbManager);
            }
        } catch (final ArrestItException aie) {
            System.out.println(aie.getMessage());
        }
    }
}
