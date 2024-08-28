package com.ericsson.arrest_it.io;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import com.ericsson.arrest_it.common.ArrestItException;
import com.ericsson.arrest_it.common.Constants;

public class MasterValuesReaderTest {
    private static final String UNKOWN_FILE_PATH = "/unknown/path/file.txt";
    private static final String VALID_FILE_PATH = Constants.TEST_RESOURCES + "masterValues" + Constants.SEPARATOR + "masterValuesTest.txt";
    private static final String FILE_WITH_SQL = Constants.TEST_RESOURCES + "masterValues" + Constants.SEPARATOR + "masterValuesWithSQL.txt";

    @Test(expected = ArrestItException.class)
    public void test_unknownFile() throws ArrestItException {
        final File file = new File(UNKOWN_FILE_PATH);
        new MasterValuesReader(file);
    }

    @Test
    public void test_validFile() throws ArrestItException {
        final File file = new File(VALID_FILE_PATH);
        final MasterValuesReader masterValuesReader = new MasterValuesReader(file);
        assertEquals("12345678912345", masterValuesReader.getValue("IMSI"));
        assertEquals("Nokia", masterValuesReader.getValue("Make"));
    }

    @Test
    public void test_getUnknownValue_returnNull() throws ArrestItException {
        final File file = new File(VALID_FILE_PATH);
        final MasterValuesReader masterValuesReader = new MasterValuesReader(file);
        assertEquals(null, masterValuesReader.getValue("UNKNOWN"));
    }

    @Test
    public void test_getSQLValue() throws ArrestItException {
        final File file = new File(FILE_WITH_SQL);
        final MasterValuesReader masterValuesReader = new MasterValuesReader(file);
        assertEquals("select top 1 tac from event_e_lte_err_raw where tac != 0", masterValuesReader.getValue("tac"));
    }
}