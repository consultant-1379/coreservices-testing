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
import junit.extensions.PA;

import org.junit.Test;

public class TimeManagerTest {

    @Test
    public void test_replaceURLTimeZone_negativeTimezone() {
        final TimeManager timeManager = new TimeManager();
        PA.setValue(timeManager, "timezone", "-0800");

        final String url = "https://atrcxb1025.athtem.eei.ericsson.se:8181/EniqEventsServices/NETWORK/KPI?time=30&tzOffset=-0000&type=SGSN&groupname=AR_SGSN_MME_GROUP_15&display=chart&maxRows=500";
        final String expected = "https://atrcxb1025.athtem.eei.ericsson.se:8181/EniqEventsServices/NETWORK/KPI?time=30&tzOffset=-0800&type=SGSN&groupname=AR_SGSN_MME_GROUP_15&display=chart&maxRows=500";
        final String actual = (String) PA.invokeMethod(timeManager, "replaceURLTimeZone(String)", url);

        assertEquals(expected, actual);
    }

    @Test
    public void test_replaceURLTimeZone_negativeTimezone2() {
        final TimeManager timeManager = new TimeManager();
        PA.setValue(timeManager, "timezone", "-0800");

        final String url = "https://atrcxb1025.athtem.eei.ericsson.se:8181/EniqEventsServices/NETWORK/KPI?time=30&type=SGSN&groupname=AR_SGSN_MME_GROUP_15&display=chart&tzOffset=%2B0000&maxRows=500";
        final String expected = "https://atrcxb1025.athtem.eei.ericsson.se:8181/EniqEventsServices/NETWORK/KPI?time=30&type=SGSN&groupname=AR_SGSN_MME_GROUP_15&display=chart&tzOffset=-0800&maxRows=500";
        final String actual = (String) PA.invokeMethod(timeManager, "replaceURLTimeZone(String)", url);

        assertEquals(expected, actual);
    }

    @Test
    public void test_replaceURLTimeZone_negativeTimezone3() {
        final TimeManager timeManager = new TimeManager();
        PA.setValue(timeManager, "timezone", "-0800");

        final String url = "https://atrcxb1025.athtem.eei.ericsson.se:8181/EniqEventsServices/NETWORK/KPI?time=30&type=SGSN&groupname=AR_SGSN_MME_GROUP_15&display=chart&maxRows=500&tzOffset=%2B0000";
        final String expected = "https://atrcxb1025.athtem.eei.ericsson.se:8181/EniqEventsServices/NETWORK/KPI?time=30&type=SGSN&groupname=AR_SGSN_MME_GROUP_15&display=chart&maxRows=500&tzOffset=-0800";
        final String actual = (String) PA.invokeMethod(timeManager, "replaceURLTimeZone(String)", url);

        assertEquals(expected, actual);
    }

    @Test
    public void test_replaceURLTimeZone_positiveTimezone() {
        final TimeManager timeManager = new TimeManager();
        PA.setValue(timeManager, "timezone", "+0800");

        final String url = "https://atrcxb1025.athtem.eei.ericsson.se:8181/EniqEventsServices/NETWORK/KPI?time=30&type=SGSN&groupname=AR_SGSN_MME_GROUP_15&display=chart&tzOffset=+0000&maxRows=500";
        final String expected = "https://atrcxb1025.athtem.eei.ericsson.se:8181/EniqEventsServices/NETWORK/KPI?time=30&type=SGSN&groupname=AR_SGSN_MME_GROUP_15&display=chart&tzOffset=%2B0800&maxRows=500";
        final String actual = (String) PA.invokeMethod(timeManager, "replaceURLTimeZone(String)", url);

        assertEquals(expected, actual);
    }

    @Test
    public void test_replaceURLTimeZone_positiveTimezone2() {
        final TimeManager timeManager = new TimeManager();
        PA.setValue(timeManager, "timezone", "+0800");

        final String url = "https://atrcxb1025.athtem.eei.ericsson.se:8181/EniqEventsServices/NETWORK/KPI?time=30&type=SGSN&groupname=AR_SGSN_MME_GROUP_15&display=chart&tzOffset=%2B0000&maxRows=500";
        final String expected = "https://atrcxb1025.athtem.eei.ericsson.se:8181/EniqEventsServices/NETWORK/KPI?time=30&type=SGSN&groupname=AR_SGSN_MME_GROUP_15&display=chart&tzOffset=%2B0800&maxRows=500";
        final String actual = (String) PA.invokeMethod(timeManager, "replaceURLTimeZone(String)", url);

        assertEquals(expected, actual);
    }

    @Test
    public void test_replaceURLTimeZone_positiveTimezone3() {
        final TimeManager timeManager = new TimeManager();
        PA.setValue(timeManager, "timezone", "+0800");

        final String url = "https://atrcxb1025.athtem.eei.ericsson.se:8181/EniqEventsServices/NETWORK/KPI?time=30&type=SGSN&groupname=AR_SGSN_MME_GROUP_15&display=chart&maxRows=500&tzOffset=%2B0000";
        final String expected = "https://atrcxb1025.athtem.eei.ericsson.se:8181/EniqEventsServices/NETWORK/KPI?time=30&type=SGSN&groupname=AR_SGSN_MME_GROUP_15&display=chart&maxRows=500&tzOffset=%2B0800";
        final String actual = (String) PA.invokeMethod(timeManager, "replaceURLTimeZone(String)", url);

        assertEquals(expected, actual);
    }

}
