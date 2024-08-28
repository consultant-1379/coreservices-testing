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

import static org.junit.Assert.*;

import org.junit.Test;

import com.ericsson.arrest_it.common.ArrestItException;
import com.ericsson.arrest_it.common.Constants;

public class ManagerFactoryTest {

    @Test
    public void test_getInstance() throws ArrestItException {
        final ManagerFactory managerFactory = ManagerFactory.getInstance();
        assertNotNull(managerFactory);
        assertTrue(managerFactory instanceof ManagerFactory);
    }

    @Test
    public void test_getManager_dbManager() throws ArrestItException {
        final Manager manager = ManagerFactory.getInstance().getManager(Constants.DB_MANAGER);
        assertNotNull(manager);
        assertTrue(manager instanceof DBManager);
    }

    @Test
    public void test_getManager_dbValuesManager() throws ArrestItException {
        final Manager manager = ManagerFactory.getInstance().getManager(Constants.DB_VALUES_MANAGER);
        assertNotNull(manager);
        assertTrue(manager instanceof DBValuesManager);
    }

    @Test
    public void test_getManager_jsonValidationManager() throws ArrestItException {
        final Manager manager = ManagerFactory.getInstance().getManager(Constants.JSON_VALIDATION_MANAGER);
        assertNotNull(manager);
        assertTrue(manager instanceof JsonValidationManager);
    }

    @Test
    public void test_getManager_preconditionManager() throws ArrestItException {
        final Manager manager = ManagerFactory.getInstance().getManager(Constants.PRECONDITION_MANAGER);
        assertNotNull(manager);
        assertTrue(manager instanceof PreconditionManager);
    }

    @Test
    public void test_getManager_queryManager() throws ArrestItException {
        final Manager manager = ManagerFactory.getInstance().getManager(Constants.QUERY_MANAGER);
        assertNotNull(manager);
        assertTrue(manager instanceof QueryManager);
    }

    @Test
    public void test_getManager_sqlManager() throws ArrestItException {
        final Manager manager = ManagerFactory.getInstance().getManager(Constants.SQL_MANAGER);
        assertNotNull(manager);
        assertTrue(manager instanceof SQLManager);
    }

    @Test
    public void test_getManager_timeManager() throws ArrestItException {
        final Manager manager = ManagerFactory.getInstance().getManager(Constants.TIME_MANAGER);
        assertNotNull(manager);
        assertTrue(manager instanceof TimeManager);
    }

    @Test
    public void test_getManager_validationManager() throws ArrestItException {
        final Manager manager = ManagerFactory.getInstance().getManager(Constants.VALIDATION_MANAGER);
        assertNotNull(manager);
        assertTrue(manager instanceof ValidationManager);
    }

    @Test
    public void test_getManager_unknown() throws ArrestItException {
        final Manager manager = ManagerFactory.getInstance().getManager("Unknown");
        assertNull(manager);
    }

    @Test
    public void test_getManager_null() throws ArrestItException {
        final Manager manager = ManagerFactory.getInstance().getManager(null);
        assertNull(manager);
    }
}
