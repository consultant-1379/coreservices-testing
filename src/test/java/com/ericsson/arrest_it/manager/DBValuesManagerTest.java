package com.ericsson.arrest_it.manager;

import static org.junit.Assert.*;

import org.junit.*;

import com.ericsson.arrest_it.io.PropertyReader;

public class DBValuesManagerTest {

    private static String testServerAddress;
    private static final String USERNAME = "dc";
    private static final String PASSWORD = "dc";
    private static final String PORT = "2640";

    private DBValuesManager dbValuesManager;

    @BeforeClass
    public static void setupClass() {
        testServerAddress = PropertyReader.getInstance().getDbServerURL();
    }

    @Before
    public void setUp() {
        final DBManager dbManager = new DBManager(testServerAddress, USERNAME, PASSWORD, PORT);
        final TimeManager timeManager = new TimeManager();
        timeManager.setMainTime();
        dbValuesManager = new DBValuesManager(dbManager, "username", timeManager);
    }

    @Test
    public void test_removeDashsAndMasterFromKey() {
        final String expected = "imsi";
        final String actual = dbValuesManager.removeDashsAndMasterFromKey("-master-imsi-");
        assertEquals(expected, actual);
    }

    @Test
    public void test_removeDashsAndMasterFromKey_noTrailingDash() {
        final String expected = "imsi";
        final String actual = dbValuesManager.removeDashsAndMasterFromKey("-master-imsi");
        assertEquals(expected, actual);
    }

    @Test
    public void test_removeDashsAndMasterFromKey_twoWords() {
        final String expected = "qos-imsi";
        final String actual = dbValuesManager.removeDashsAndMasterFromKey("-master-qos-imsi-");
        assertEquals(expected, actual);
    }

    @Test
    public void test_removeDashsAndMasterFromKey_twoWords_noTrailingDash() {
        final String expected = "qos-imsi";
        final String actual = dbValuesManager.removeDashsAndMasterFromKey("-master-qos-imsi");
        assertEquals(expected, actual);
    }

    @Test
    public void test_getLastCharacter() {
        final String expected = "i";
        final String actual = dbValuesManager.getLastCharacter("-master-qos-imsi");
        assertEquals(expected, actual);
    }

    @Test
    public void test_isGroup_true() {
        assertTrue(dbValuesManager.isGroup("-master-imsiGroup"));
    }

    @Test
    public void test_isGroup_false() {
        assertFalse(dbValuesManager.isGroup("-master-imsi"));
    }
}