package com.ericsson.arrest_it.manager;

import static org.junit.Assert.*;

import java.util.List;

import junit.extensions.PA;

import org.junit.*;

import com.ericsson.arrest_it.common.ArrestItException;
import com.ericsson.arrest_it.common.Row;
import com.ericsson.arrest_it.io.PropertyReader;

public class DBManagerTest {

    private DBManager db;
    private static String testServerAddress;

    @BeforeClass
    public static void setupClass() {
        testServerAddress = PropertyReader.getInstance().getDbServerURL();
    }

    @Before
    public void setUp() {
        db = new DBManager(testServerAddress, "dc", "dc", "2640");

    }

    @Test
    public void test_DBObjCreation() {

        final DBManager dbm = new DBManager("http://testServer:18080", "testUserName", "testPassWord", "2640");

        final String serv = (String) PA.getValue(dbm, "server");
        final String un = (String) PA.getValue(dbm, "username");
        final String pass = (String) PA.getValue(dbm, "password");

        assertEquals("testServer", serv);
        assertEquals("testUserName", un);
        assertEquals("testPassWord", pass);
    }   
}
