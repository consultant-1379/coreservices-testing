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
package com.ericsson.arrest_it.utils;

import static org.junit.Assert.*;

import org.junit.Test;

public class SQLUtilsTest {

    @Test
    public void test_isSQL_false() {
        final boolean actual = SQLUtils.isSQL("-master-qos-imsi");
        assertFalse(actual);
    }

    @Test
    public void test_isSQL_false_partialSQL() {
        final boolean actual = SQLUtils.isSQL("select imsi");
        assertFalse(actual);
    }

    @Test
    public void test_isSQL_true() {
        final boolean actual = SQLUtils.isSQL("select imsi from event_e_lte_err_raw");
        assertTrue(actual);
    }

    @Test
    public void test_getDateTimeFromSql() {
        final String sql = "select top 1 imsi from event_e_lte_err_raw where datetime_id >= '2014-11-12 00:00' and datetime_id < '2014-11-12 01:00'";

        final String expected = "datetime_id >= '2014-11-12 00:00' and datetime_id < '2014-11-12 01:00'";
        final String actual = SQLUtils.getDateTimeFromSql(sql);
        assertEquals(expected, actual);
    }

    @Test
    public void test_createIMSISql() {
        final String expected = "SELECT TOP 1 IMSI FROM (SELECT IMSI, COUNT(IMSI) AS TOTAL FROM dc.event_e_lte_err_raw WHERE datetime_id >= '2014-11-12 00:00' and datetime_id < '2014-11-12 01:00' "
                + "AND IMSI <> 0 GROUP BY IMSI UNION ALL SELECT IMSI, COUNT(IMSI) AS TOTAL FROM dc.event_e_sgeh_err_raw WHERE datetime_id >= '2014-11-12 00:00' and datetime_id < '2014-11-12 01:00' "
                + "AND IMSI <> 0 GROUP BY IMSI ) AS temp GROUP BY IMSI, TOTAL ORDER BY TOTAL DESC";

        final String actual = SQLUtils.createIMSISql("datetime_id >= '2014-11-12 00:00' and datetime_id < '2014-11-12 01:00'");
        assertEquals(expected, actual);
    }

    @Test
    public void test_createMsisdnSql() {
        final String expected = "SELECT TOP 1 MSISDN_ERR FROM ( SELECT distinct(errTable.MSISDN) AS 'MSISDN_ERR' "
                + "FROM dc.event_e_lte_err_raw AS errTable, dc.dim_e_imsi_msisdn AS msisdnTable " + "WHERE errTable.msisdn = msisdnTable.msisdn "
                + "AND datetime_id >= '2014-11-12 00:00' and datetime_id < '2014-11-12 01:00' "
                + "AND errTable.msisdn <> 0 union all SELECT distinct(errTable.MSISDN) AS 'MSISDN_ERR' "
                + "FROM dc.event_e_sgeh_err_raw AS errTable, dc.dim_e_imsi_msisdn AS msisdnTable " + "WHERE errTable.msisdn = msisdnTable.msisdn "
                + "AND datetime_id >= '2014-11-12 00:00' and datetime_id < '2014-11-12 01:00' " + "AND errTable.msisdn <> 0) as tempTable "
                + "GROUP BY MSISDN_ERR  ORDER BY COUNT(MSISDN_ERR) DESC";

        final String actual = SQLUtils.createMsisdnSql("datetime_id >= '2014-11-12 00:00' and datetime_id < '2014-11-12 01:00'");
        assertEquals(expected, actual);
    }

    @Test
    public void test_createMsisdnTauSql() {
        final String expected = "SELECT TOP 1 errTable.MSISDN AS 'MSISDN_ERR' "
                + "FROM dc.event_e_lte_err_raw AS errTable, dc.dim_e_imsi_msisdn AS msisdnTable " + "WHERE errTable.msisdn = msisdnTable.msisdn "
                + "AND datetime_id >= '2014-11-12 00:00' and datetime_id < '2014-11-12 01:00' "
                + "AND errTable.msisdn <> 0 and errTable.event_id =8 " + "GROUP BY MSISDN_ERR ORDER BY COUNT(MSISDN_ERR) DESC";

        final String actual = SQLUtils.createMsisdnTauSql("datetime_id >= '2014-11-12 00:00' and datetime_id < '2014-11-12 01:00'");
        assertEquals(expected, actual);
    }

    @Test
    public void test_createMsisdnHandoverSql() {
        final String expected = "SELECT TOP 1 errTable.MSISDN AS 'MSISDN_ERR' "
                + "FROM dc.event_e_lte_err_raw AS errTable, dc.dim_e_imsi_msisdn AS msisdnTable " + "WHERE errTable.msisdn = msisdnTable.msisdn "
                + "AND datetime_id >= '2014-11-12 00:00' and datetime_id < '2014-11-12 01:00' "
                + "AND errTable.msisdn <> 0 and errTable.event_id =7 " + "GROUP BY MSISDN_ERR ORDER BY COUNT(MSISDN_ERR) DESC";

        final String actual = SQLUtils.createMsisdnHandoverSql("datetime_id >= '2014-11-12 00:00' and datetime_id < '2014-11-12 01:00'");
        assertEquals(expected, actual);
    }

    @Test
    public void test_createImsiHandoverSql() {
        final String expected = "SELECT TOP 1 errTable.IMSI AS 'IMSI_ERR' FROM dc.event_e_lte_err_raw AS errTable "
                + "WHERE datetime_id >= '2014-11-12 00:00' and datetime_id < '2014-11-12 01:00' "
                + "and errTable.event_id =7 GROUP BY IMSI_ERR ORDER BY COUNT(IMSI_ERR) DESC";

        final String actual = SQLUtils.createImsiHandoverSql("datetime_id >= '2014-11-12 00:00' and datetime_id < '2014-11-12 01:00'");
        assertEquals(expected, actual);
    }

    @Test
    public void test_createImsiTauSql() {
        final String expected = "SELECT TOP 1 errTable.IMSI AS 'IMSI_ERR' FROM dc.event_e_lte_err_raw AS errTable "
                + "WHERE datetime_id >= '2014-11-12 00:00' and datetime_id < '2014-11-12 01:00' "
                + "and errTable.event_id =8 GROUP BY IMSI_ERR ORDER BY COUNT(IMSI_ERR) DESC";

        final String actual = SQLUtils.createImsiTauSql("datetime_id >= '2014-11-12 00:00' and datetime_id < '2014-11-12 01:00'");
        assertEquals(expected, actual);
    }

    @Test
    public void test_createImsiGroupHandoverSql() {
        final String expected = "select top 1 b.group_name from (select distinct imsi, count(*) as noOfEvents from event_e_lte_err_raw "
                + "where datetime_id >= '2014-11-12 00:00' and datetime_id < '2014-11-12 01:00' and event_id=7 "
                + "and imsi in (select imsi from GROUP_TYPE_E_IMSI) group by imsi) a, GROUP_TYPE_E_IMSI b "
                + "where a.imsi = b.imsi group by b.group_name order by sum(a.noOfEvents)";

        final String actual = SQLUtils.createImsiGroupHandoverSql("datetime_id >= '2014-11-12 00:00' and datetime_id < '2014-11-12 01:00'");
        assertEquals(expected, actual);
    }

    @Test
    public void test_createImsiGroupTauSql() {
        final String expected = "select top 1 b.group_name from (select distinct imsi, count(*) as noOfEvents from event_e_lte_err_raw "
                + "where datetime_id >= '2014-11-12 00:00' and datetime_id < '2014-11-12 01:00' and event_id=8 "
                + "and imsi in (select imsi from GROUP_TYPE_E_IMSI) group by imsi) a, GROUP_TYPE_E_IMSI b "
                + "where a.imsi = b.imsi group by b.group_name order by sum(a.noOfEvents)";

        final String actual = SQLUtils.createImsiGroupTauSql("datetime_id >= '2014-11-12 00:00' and datetime_id < '2014-11-12 01:00'");
        assertEquals(expected, actual);
    }

    @Test
    public void test_getExcludeWhereClause_tac() {
        final String expected = " and tac <> 0 ";
        final String actual = SQLUtils.getExcludeWhereClause("tac");
        assertEquals(expected, actual);
    }

    @Test
    public void test_getExcludeWhereClause_hierarchy_1() {
        final String expected = " and vendor <> 'Unknown' ";
        final String actual = SQLUtils.getExcludeWhereClause("hierarchy_1");
        assertEquals(expected, actual);
    }

    @Test
    public void test_getExcludeWhereClause_hierarchy_3() {
        final String expected = " and vendor <> 'Unknown' ";
        final String actual = SQLUtils.getExcludeWhereClause("hierarchy_3");
        assertEquals(expected, actual);
    }

    @Test
    public void test_getExcludeWhereClause_noExcludeClase() {
        final String expected = " ";
        final String actual = SQLUtils.getExcludeWhereClause("imsi");
        assertEquals(expected, actual);
    }

    @Test
    public void test_createTerminalNodeSql() {
        final String expected = "Select model ||','|| '3532352' from dim_e_sgeh_tac where tac = 3532352";
        final String actual = SQLUtils.createTerminalNodeSql("3532352");
        assertEquals(expected, actual);
    }

    @Test
    public void test_createAccessAreaNodeSql() {
        final String expected = "Select top 1 hierarchy_1 ||','|| hierarchy_2 ||','|| hierarchy_3 ||','|| vendor ||','|| rat from "
                + "(select rat, vendor, hierarchy_3, hierarchy_1, hierarchy_2 from event_e_lte_err_raw where hierarchy_1='LTE01ERBS00001-2' and hierarchy_3 <> 'unknown' and vendor <> 'unknown' "
                + "union all select rat, vendor, hierarchy_3, hierarchy_1, hierarchy_2 from event_e_sgeh_err_raw where hierarchy_1='LTE01ERBS00001-2' and hierarchy_3 <> 'unknown' and vendor <> 'unknown' ) as tmp";

        final String actual = SQLUtils.createAccessAreaNodeSql("LTE01ERBS00001-2");
        assertEquals(expected, actual);
    }

    @Test
    public void test_createControllerNodeSql() {
        final String expected = "Select top 1 hierarchy_3 ||','|| vendor ||','|| rat "
                + "from (select rat, vendor, hierarchy_3 from event_e_lte_err_raw " + "where hierarchy_3='ONRM_RootMo_R:LTE01ERBS00001'"
                + "union all select rat, vendor, hierarchy_3 from event_e_sgeh_err_raw " + "where hierarchy_3='ONRM_RootMo_R:LTE01ERBS00001') as tmp";

        final String actual = SQLUtils.createControllerNodeSql("ONRM_RootMo_R:LTE01ERBS00001");
        assertEquals(expected, actual);
    }

    @Test
    public void test_createIndividualTableSpecificSql() {
        final String expected = "select top 1 IMSI from event_e_LTE_err_raw where IMSI is not null "
                + "and datetime_id >= '2014-11-12 00:00' and datetime_id < '2014-11-12 01:00' " + "group by IMSI order by count(*) desc";

        final String actual = SQLUtils.createIndividualTableSpecificSql("IMSI", "LTE",
                "datetime_id >= '2014-11-12 00:00' and datetime_id < '2014-11-12 01:00'");
        assertEquals(expected, actual);
    }

    @Test
    public void test_createIndividualTableSpecificSql_excludeCauseUsed() {
        final String expected = "select top 1 TAC from event_e_LTE_err_raw where TAC is not null and tac <> 0 "
                + "and datetime_id >= '2014-11-12 00:00' and datetime_id < '2014-11-12 01:00' " + "group by TAC order by count(*) desc";

        final String actual = SQLUtils.createIndividualTableSpecificSql("TAC", "LTE",
                "datetime_id >= '2014-11-12 00:00' and datetime_id < '2014-11-12 01:00'");
        assertEquals(expected, actual);
    }
}
