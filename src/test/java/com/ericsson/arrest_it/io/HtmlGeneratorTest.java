package com.ericsson.arrest_it.io;

import static org.junit.Assert.*;

import org.junit.Test;

import com.ericsson.arrest_it.common.Constants;

public class HtmlGeneratorTest {
    private static final String SERIALIZATION_FILE_NAME = "interimA_KPI_Analysis_Network_Control_Plane_KPIs_Attach_Success_Rate.ser";
    private static final String NOT_SERIALIZATION_FILE = "TestFile.txt";
    private static final String EXPECTED_FILE_NAME = "KPI_Analysis_Network_Control_Plane_KPIs_Attach_Success_Rate" + Constants.FAILED_FILE
            + Constants.TXT_EXTENSION;

    @Test
    public void test_convertSerFileNameToResultFileName() {
        assertEquals(EXPECTED_FILE_NAME, HtmlGenerator.convertSerFileNameToResultFileName(SERIALIZATION_FILE_NAME, true));
        assertEquals(NOT_SERIALIZATION_FILE, HtmlGenerator.convertSerFileNameToResultFileName(NOT_SERIALIZATION_FILE, true));
    }
}
