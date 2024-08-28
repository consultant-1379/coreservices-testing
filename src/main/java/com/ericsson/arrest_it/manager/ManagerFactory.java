/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.arrest_it.manager;

import com.ericsson.arrest_it.common.ArrestItException;
import com.ericsson.arrest_it.common.Constants;
import com.ericsson.arrest_it.io.PropertyReader;

public class ManagerFactory {

    private static ManagerFactory instance = null;
    private final DBManager dbManager;
    private final DBValuesManager dbValuesManager;
    private final JsonValidationManager jsonValidationManager;
    private final PreconditionManager preconditionManager;
    private final QueryManager queryManager;
    private final SQLManager sqlManager;
    private final TimeManager timeManager;
    private final ValidationManager validationManager;

    private ManagerFactory() throws ArrestItException {
        final boolean shouldValidateCsv = PropertyReader.getInstance().isValidateCsv();
        final String dbServerAddress = PropertyReader.getInstance().getDbServerURL();
        final String uiUsername = PropertyReader.getInstance().getUIUserName();
        final String dbUsername = PropertyReader.getInstance().getDBUserName();
        final String dbPassword = PropertyReader.getInstance().getDBPassword();
        final String dbPort = PropertyReader.getInstance().getDBPort();

        timeManager = new TimeManager();
        jsonValidationManager = new JsonValidationManager();
        dbManager = new DBManager(dbServerAddress, dbUsername, dbPassword, dbPort);
        dbValuesManager = new DBValuesManager(dbManager, uiUsername, timeManager);
        sqlManager = new SQLManager();
        queryManager = new QueryManager(dbManager, dbValuesManager, timeManager);
        validationManager = new ValidationManager(dbValuesManager, queryManager, shouldValidateCsv);
        preconditionManager = new PreconditionManager(queryManager, validationManager, timeManager);
    }

    public static ManagerFactory getInstance() throws ArrestItException {
        if (instance == null) {
            instance = new ManagerFactory();
        }

        return instance;
    }

    public Manager getManager(final String managerType) {
        Manager manager = null;

        if (managerType != null) {
            if (managerType.equalsIgnoreCase(Constants.DB_MANAGER)) {
                manager = dbManager;
            } else if (managerType.equalsIgnoreCase(Constants.DB_VALUES_MANAGER)) {
                manager = dbValuesManager;
            } else if (managerType.equalsIgnoreCase(Constants.JSON_VALIDATION_MANAGER)) {
                manager = jsonValidationManager;
            } else if (managerType.equalsIgnoreCase(Constants.PRECONDITION_MANAGER)) {
                manager = preconditionManager;
            } else if (managerType.equalsIgnoreCase(Constants.QUERY_MANAGER)) {
                manager = queryManager;
            } else if (managerType.equalsIgnoreCase(Constants.SQL_MANAGER)) {
                manager = sqlManager;
            } else if (managerType.equalsIgnoreCase(Constants.TIME_MANAGER)) {
                manager = timeManager;
            } else if (managerType.equalsIgnoreCase(Constants.VALIDATION_MANAGER)) {
                manager = validationManager;
            }
        }

        return manager;
    }
}
