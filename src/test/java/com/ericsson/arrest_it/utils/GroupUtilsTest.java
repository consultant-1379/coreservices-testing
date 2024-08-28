package com.ericsson.arrest_it.utils;

import static com.ericsson.arrest_it.common.Constants.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.*;

import org.junit.*;

import com.ericsson.arrest_it.common.ArrestItException;
import com.ericsson.arrest_it.common.Row;
import com.ericsson.arrest_it.io.PropertyReader;
import com.ericsson.arrest_it.manager.DBManager;
import com.ericsson.arrest_it.utils.GroupUtils;

public class GroupUtilsTest {
    private static final String RAT = "RAT";
    private static final String GROUP_TYPE_E_RAT = "GROUP_TYPE_E_RAT";
    private static final String ARREST_IT_GROUP_NAME = "AR_RAT_GROUP";
    private static final String TEST_GROUP_MANAGEMENT_FILE = TEST_RESOURCES + "config" + SEPARATOR + "test_group_management.txt";
    private static String testServerAddress;
    private DBManager dbManager;

    @BeforeClass
    public static void setupClass() {
        testServerAddress = PropertyReader.getInstance().getDbServerURL();
    }

    @Before
    public void setUp() {
        dbManager = new DBManager(testServerAddress, "dc", "dc", "2640");
    }
    
   

    @Test
    public void test_getGroupNameTableNameFromConfigFile() throws ArrestItException {
        final Map<String, String> actual = GroupUtils.getGroupNameTableNameFromConfigFile(new File(TEST_GROUP_MANAGEMENT_FILE));
        final Map<String, String> expected = new HashMap<String, String>();
        expected.put(ARREST_IT_GROUP_NAME, GROUP_TYPE_E_RAT);

        assertEquals(expected.size(), actual.size());
        assertEquals(expected.get(ARREST_IT_GROUP_NAME), actual.get(ARREST_IT_GROUP_NAME));
    }
    
   

    @Test
    public void test_createDeleteSQL() throws ArrestItException {
        final String expected = "DELETE FROM GROUP_TYPE_E_IMSI WHERE GROUP_NAME LIKE 'AR_IMSI_GROUP%';";
        final String actual = GroupUtils.createDeleteGroupSQL("GROUP_TYPE_E_IMSI", "AR_IMSI_GROUP");

        assertEquals(expected, actual);
    }

    @Test
    public void test_createDataTypeQuery() throws ArrestItException {
        final String expected = "select distinct datatype.domain_name as 'DATA_TYPE' from sys.sysdomain datatype, sys.systabcol col, sys.systab tab "
                + "where tab.table_id = col.table_id and col.domain_id = datatype.domain_id "
                + "and col.column_name = 'GROUP_NAME' and tab.table_name = 'GROUP_TYPE_E_IMSI'";
        final String actual = GroupUtils.createDataTypeQuery("GROUP_NAME", "GROUP_TYPE_E_IMSI");

        assertEquals(expected, actual);
    }

    private Row getTestRow() {
        final Map<String, String> columnValues = new HashMap<String, String>();

        columnValues.put(GROUP_NAME, "AR_IMSI_GROUP_10080");
        columnValues.put("IMSI", "1234864867548");
        return new Row(columnValues);
    }

    private Map<String, List<Row>> getExpectedTableColumnValues() {
        final Map<String, List<Row>> expected = new HashMap<String, List<Row>>();
        final List<Row> rows = new ArrayList<Row>();
        final Map<String, String> columnValues1 = new HashMap<String, String>();

        columnValues1.put(GROUP_NAME, "AR_RAT_GROUP_10080");
        columnValues1.put(RAT, "0");
        rows.add(new Row(columnValues1));

        final Map<String, String> columnValues2 = new HashMap<String, String>();

        columnValues2.put(GROUP_NAME, "AR_RAT_GROUP_10080");
        columnValues2.put(RAT, "1");
        rows.add(new Row(columnValues2));

        expected.put(GROUP_TYPE_E_RAT, rows);

        return expected;
    }
}
