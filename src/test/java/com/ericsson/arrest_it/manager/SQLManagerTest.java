package com.ericsson.arrest_it.manager;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.ericsson.arrest_it.common.DBQuery;
import com.ericsson.arrest_it.common.TestCase;
import com.ericsson.arrest_it.utils.SQLUtils;

public class SQLManagerTest {
    static SQLManager sqlManager;
    static String dateTimeValues;
    private static TestCase tc;

    @BeforeClass
    public static void setUp() {
        tc = new TestCase();
        tc.addDbQuery(new DBQuery(" datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' ", true));
        dateTimeValues = SQLUtils.getDateTimeFromSql(tc.getDbQueries().get(0).getTemplateStatement());
        sqlManager = new SQLManager();
    }

    //KPI ANALYSIS UNIT TESTS
    @Test
    public void test_rncControllerNode() {
        final String key = "-master-rncControllerNode-";
        final String expected = "Select top 1 hierarchy_3 ||','|| vendor ||','|| rat as 'NODE' FROM (select rat, vendor, hierarchy_3 from dc.event_e_sgeh_err_raw WHERE datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' AND RAT=1 AND hierarchy_3 <> 'Unknown' AND Vendor <> 'Unknown' ) AS tmp GROUP BY hierarchy_3,rat,vendor ORDER BY count(*) DESC";
        final String actual = sqlManager.getIndividualSql(tc, key);
        assertEquals(expected, actual);
    }

    @Test
    public void test_rncAccessAreaNode() {
        final String key = "-master-rncAccessAreaNode-";
        final String expected = "SELECT hierarchy_1 ||','|| hierarchy_2 ||','|| hierarchy_3 ||','|| vendor ||','|| rat as 'NODE' FROM( SELECT rat,vendor,hierarchy_1,hierarchy_3,hierarchy_2 FROM event_e_sgeh_err_raw err WHERE datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' AND RAT=1 AND hierarchy_3 <> 'Unknown' AND hierarchy_1 <> 'Unknown' AND hierarchy_1 in ( SELECT hierarchy_1 FROM DIM_E_SGEH_HIER321_CELL WHERE Cell_id is not NULL) AND Vendor <> 'Unknown' ) AS tmp";
        final String actual = sqlManager.getIndividualSql(tc, key);
        assertEquals(expected, actual);
    }

    @Test
    public void test_userPlaneControllerNode() {
        final String key = "-master-UserPlaneControllerNode-";
        final String expected = "Select top 1 hierarchy_3 ||','|| vendor ||',3G' as 'NODE' FROM (select vendor, hierarchy_3  FROM DIM_E_SGEH_HIER321 WHERE HIER3_ID IN (SELECT HIER3_ID from dc.EVENT_E_USER_PLANE_TCP_RAW WHERE datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' GROUP BY HIER3_ID ORDER BY COUNT(HIER3_ID) DESC) AND RAT=1 ) AS tmp";
        final String actual = sqlManager.getIndividualSql(tc, key);
        assertEquals(expected, actual);
    }

    @Test
    public void test_userPlaneAccessAreaNode() {
        final String key = "-master-UserPlaneAccessAreaNode-";
        final String expected = "Select top 1 hierarchy_1 ||','|| hierarchy_2 ||','|| hierarchy_3 ||','|| vendor ||',3G' as 'NODE' FROM (Select vendor, hierarchy_3, hierarchy_1, hierarchy_2 FROM DIM_E_SGEH_HIER321 WHERE HIER321_ID IN (SELECT HIER321_ID from dc.EVENT_E_USER_PLANE_TCP_RAW WHERE datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' GROUP BY HIER321_ID ORDER BY COUNT(HIER321_ID) DESC) AND RAT=1 AND hierarchy_3 <> 'Unknown' AND hierarchy_1 <> 'Unknown' AND hierarchy_1 in ( SELECT hierarchy_1 FROM DIM_E_SGEH_HIER321_CELL WHERE Cell_id is not NULL) AND Vendor <> 'Unknown' ) AS tmp;";
        final String actual = sqlManager.getIndividualSql(tc, key);
        assertEquals(expected, actual);
    }

    //2G3G4G CORE UNIT TESTS    

    @Test
    public void test_apn() {
        final String key = "-master-apn-";
        final String expected = "select top 1 apn, sum(sumTotal) from((select apn, count(*) as 'sumTotal' from event_e_lte_err_raw where datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' and apn is not null group by apn) union (select apn, count(*) as 'sumTotal' from event_e_sgeh_err_raw where datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' and apn is not null group by apn)) as rawView group by apn order by sum(sumtotal) desc";
        final String actual = sqlManager.getIndividualSql(tc, key);
        assertEquals(expected, actual);
    }

    @Test
    public void test_terminal() {
        final String key = "-master-terminal-";
        final String expected = "select top 1 tac, sum(sumTotal) from((select tac, count(*) as 'sumTotal' from event_e_lte_err_raw where datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' and tac <> 0 and tac is not null group by tac) union (select tac, count(*) as 'sumTotal' from event_e_sgeh_err_raw where datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' and tac <> 0 and tac is not null group by tac)) as rawView group by tac order by sum(sumtotal) desc";
        final String actual = sqlManager.getIndividualSql(tc, key);
        assertEquals(expected, actual);
    }

    @Test
    public void test_controller() {
        final String key = "-master-controller-";
        final String expected = "select top 1 hierarchy_3, sum(sumTotal) from((select hierarchy_3, count(*) as 'sumTotal' from event_e_lte_err_raw where datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' and vendor <> 'Unknown' and hierarchy_3 is not null group by hierarchy_3) union (select hierarchy_3, count(*) as 'sumTotal' from event_e_sgeh_err_raw where datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' and vendor <> 'Unknown' and hierarchy_3 is not null group by hierarchy_3)) as rawView group by hierarchy_3 order by sum(sumtotal) desc";
        final String actual = sqlManager.getIndividualSql(tc, key);
        assertEquals(expected, actual);
    }

    @Test
    public void test_accessArea() {
        final String key = "-master-AccessArea-";
        final String expected = "select top 1 hierarchy_1, sum(sumTotal) from((select hierarchy_1, count(*) as 'sumTotal' from event_e_lte_err_raw where datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' and vendor <> 'Unknown' and hierarchy_1 is not null group by hierarchy_1) union (select hierarchy_1, count(*) as 'sumTotal' from event_e_sgeh_err_raw where datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' and vendor <> 'Unknown' and hierarchy_1 is not null group by hierarchy_1)) as rawView group by hierarchy_1 order by sum(sumtotal) desc";
        final String actual = sqlManager.getIndividualSql(tc, key);
        assertEquals(expected, actual);
    }

    @Test
    public void test_sgsn() {
        final String key = "-master-sgsn-";
        final String expected = "select top 1 event_source_name, sum(sumTotal) from((select event_source_name, count(*) as 'sumTotal' from event_e_lte_err_raw where datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' and event_source_name is not null group by event_source_name) union (select event_source_name, count(*) as 'sumTotal' from event_e_sgeh_err_raw where datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' and event_source_name is not null group by event_source_name)) as rawView group by event_source_name order by sum(sumtotal) desc";
        final String actual = sqlManager.getIndividualSql(tc, key);
        assertEquals(expected, actual);
    }

    @Test
    public void test_imsi() {
        final String key = "-master-imsi-";
        final String expected = "SELECT TOP 1 IMSI FROM (SELECT IMSI, COUNT(IMSI) AS TOTAL FROM dc.event_e_lte_err_raw WHERE datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' AND IMSI <> 0 GROUP BY IMSI UNION ALL SELECT IMSI, COUNT(IMSI) AS TOTAL FROM dc.event_e_sgeh_err_raw WHERE datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' AND IMSI <> 0 GROUP BY IMSI ) AS temp GROUP BY IMSI, TOTAL ORDER BY TOTAL DESC";
        final String actual = sqlManager.getIndividualSql(tc, key);
        assertEquals(expected, actual);
    }

    @Test
    public void test_tracking() {
        final String key = "-master-tracking-";
        final String expected = "select top 1 trac from event_e_lte_err_raw where trac is not null and datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' group by trac order by count(*) desc";
        final String actual = sqlManager.getIndividualSql(tc, key);
        assertEquals(expected, actual);
    }

    @Test
    public void test_ptmsi() {
        final String key = "-master-ptmsi-";
        final String expected = "SELECT TOP 1 CAST(PTMSI AS VARCHAR(30)) AS 'PTMSI_ERR' FROM dc.event_e_SGEH_err_raw WHERE datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' AND PTMSI <> 0 AND PTMSI IS NOT NULL GROUP BY PTMSI_ERR ORDER BY COUNT(PTMSI_ERR) DESC";
        final String actual = sqlManager.getIndividualSql(tc, key);
        assertEquals(expected, actual);
    }

    @Test
    public void test_msisdnTau() {
        final String key = "-master-msisdnTau-";
        final String expected = "SELECT TOP 1 errTable.MSISDN AS 'MSISDN_ERR' FROM dc.event_e_lte_err_raw AS errTable, dc.dim_e_imsi_msisdn AS msisdnTable WHERE errTable.msisdn = msisdnTable.msisdn AND datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' AND errTable.msisdn <> 0 and errTable.event_id =8 GROUP BY MSISDN_ERR ORDER BY COUNT(MSISDN_ERR) DESC";
        final String actual = sqlManager.getIndividualSql(tc, key);
        assertEquals(expected, actual);
    }

    @Test
    public void test_msisdnHandover() {
        final String key = "-master-msisdnHandover-";
        final String expected = "SELECT TOP 1 errTable.MSISDN AS 'MSISDN_ERR' FROM dc.event_e_lte_err_raw AS errTable, dc.dim_e_imsi_msisdn AS msisdnTable WHERE errTable.msisdn = msisdnTable.msisdn AND datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' AND errTable.msisdn <> 0 and errTable.event_id =7 GROUP BY MSISDN_ERR ORDER BY COUNT(MSISDN_ERR) DESC";
        final String actual = sqlManager.getIndividualSql(tc, key);
        assertEquals(expected, actual);
    }

    @Test
    public void test_msisdn() {
        final String key = "-master-msisdn-";
        final String expected = "SELECT TOP 1 MSISDN_ERR FROM ( SELECT distinct(errTable.MSISDN) AS 'MSISDN_ERR' FROM dc.event_e_lte_err_raw AS errTable, dc.dim_e_imsi_msisdn AS msisdnTable WHERE errTable.msisdn = msisdnTable.msisdn AND datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' AND errTable.msisdn <> 0 union all SELECT distinct(errTable.MSISDN) AS 'MSISDN_ERR' FROM dc.event_e_sgeh_err_raw AS errTable, dc.dim_e_imsi_msisdn AS msisdnTable WHERE errTable.msisdn = msisdnTable.msisdn AND datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' AND errTable.msisdn <> 0) as tempTable GROUP BY MSISDN_ERR  ORDER BY COUNT(MSISDN_ERR) DESC";
        final String actual = sqlManager.getIndividualSql(tc, key);
        assertEquals(expected, actual);
    }

    @Test
    public void test_imsiTau() {
        final String key = "-master-imsiTau-";
        final String expected = "SELECT TOP 1 errTable.IMSI AS 'IMSI_ERR' FROM dc.event_e_lte_err_raw AS errTable WHERE datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' and errTable.event_id =8 GROUP BY IMSI_ERR ORDER BY COUNT(IMSI_ERR) DESC";
        final String actual = sqlManager.getIndividualSql(tc, key);
        assertEquals(expected, actual);
    }

    @Test
    public void test_imsiHandover() {
        final String key = "-master-imsiHandover-";
        final String expected = "SELECT TOP 1 errTable.IMSI AS 'IMSI_ERR' FROM dc.event_e_lte_err_raw AS errTable WHERE datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' and errTable.event_id =7 GROUP BY IMSI_ERR ORDER BY COUNT(IMSI_ERR) DESC";
        final String actual = sqlManager.getIndividualSql(tc, key);
        assertEquals(expected, actual);
    }

    // QOS

    @Test
    public void test_apnQOS() {
        final String key = "-master-qos-apn-";
        final String expected = "select top 1 apn , sum(qci_err_1 + qci_err_2 + qci_err_3 + qci_err_4 + qci_err_5 + qci_err_6 + qci_err_7 + qci_err_8 + qci_err_9 + qci_err_10) as 'sumtotal' from event_e_lte_err_raw where datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' and  apn is not null Group By apn Order by sumtotal DESC";
        final String actual = sqlManager.getIndividualSql(tc, key);
        assertEquals(expected, actual);
    }

    @Test
    public void test_terminalQOS() {
        final String key = "-master-qos-terminal-";
        final String expected = "select top 1 tac , sum(qci_err_1 + qci_err_2 + qci_err_3 + qci_err_4 + qci_err_5 + qci_err_6 + qci_err_7 + qci_err_8 + qci_err_9 + qci_err_10) as 'sumtotal' from event_e_lte_err_raw where datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' and tac <> 0 and  tac is not null Group By tac Order by sumtotal DESC";
        final String actual = sqlManager.getIndividualSql(tc, key);
        assertEquals(expected, actual);
    }

    @Test
    public void test_controllerQOS() {
        final String key = "-master-qos-controller-";
        final String expected = "select top 1 hierarchy_3 , sum(qci_err_1 + qci_err_2 + qci_err_3 + qci_err_4 + qci_err_5 + qci_err_6 + qci_err_7 + qci_err_8 + qci_err_9 + qci_err_10) as 'sumtotal' from event_e_lte_err_raw where datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' and vendor <> 'Unknown' and  hierarchy_3 is not null Group By hierarchy_3 Order by sumtotal DESC";
        final String actual = sqlManager.getIndividualSql(tc, key);
        assertEquals(expected, actual);
    }

    @Test
    public void test_accessAreaQOS() {
        final String key = "-master-qos-AccessArea-";
        final String expected = "select top 1 hierarchy_1 , sum(qci_err_1 + qci_err_2 + qci_err_3 + qci_err_4 + qci_err_5 + qci_err_6 + qci_err_7 + qci_err_8 + qci_err_9 + qci_err_10) as 'sumtotal' from event_e_lte_err_raw where datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' and vendor <> 'Unknown' and  hierarchy_1 is not null Group By hierarchy_1 Order by sumtotal DESC";
        final String actual = sqlManager.getIndividualSql(tc, key);
        assertEquals(expected, actual);
    }

    @Test
    public void test_sgsnQOS() {
        final String key = "-master-qos-sgsn-";
        final String expected = "select top 1 event_source_name , sum(qci_err_1 + qci_err_2 + qci_err_3 + qci_err_4 + qci_err_5 + qci_err_6 + qci_err_7 + qci_err_8 + qci_err_9 + qci_err_10) as 'sumtotal' from event_e_lte_err_raw where datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' and  event_source_name is not null Group By event_source_name Order by sumtotal DESC";
        final String actual = sqlManager.getIndividualSql(tc, key);
        assertEquals(expected, actual);
    }

    // 2G3G4G CORE Group

    @Test
    public void test_apnGroup() {
        final String key = "-master-apnGroup-";
        final String expected = "select top 1 group_name, sum(newSumTotal) from ((select group_name, sum(sumTotal) as 'newSumTotal' from (select count(*) as 'sumTotal', b.group_name from event_e_lte_err_raw  a, group_type_e_apn b, where a.apn = b.apn and a.apn is not null and datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' group by a.apn, b.group_name) as rawview group by group_name) union (select group_name, sum(sumTotal) as 'newSumTotal' from (select count(*) as 'sumTotal', b.group_name from event_e_sgeh_err_raw  a, group_type_e_apn b, where a.apn = b.apn and a.apn is not null and datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' group by a.apn, b.group_name) as rawview group by group_name)) as rawView group by group_name order by sum(newSumTotal) desc";
        final String actual = sqlManager.getGroupSql(tc, key);
        assertEquals(expected, actual);
    }

    @Test
    public void test_terminalGroup() {
        final String key = "-master-terminalGroup-";
        final String expected = "select top 1 group_name, sum(newSumTotal) from ((select group_name, sum(sumTotal) as 'newSumTotal' from (select count(*) as 'sumTotal', b.group_name from event_e_lte_err_raw  a, GROUP_TYPE_E_TAC b, where a.tac = b.tac and a.tac is not null and a.tac <> 0 and datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' group by a.tac, b.group_name) as rawview group by group_name) union (select group_name, sum(sumTotal) as 'newSumTotal' from (select count(*) as 'sumTotal', b.group_name from event_e_sgeh_err_raw  a, GROUP_TYPE_E_TAC b, where a.tac = b.tac and a.tac is not null and a.tac <> 0 and datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' group by a.tac, b.group_name) as rawview group by group_name)) as rawView group by group_name order by sum(newSumTotal) desc";
        final String actual = sqlManager.getGroupSql(tc, key);
        assertEquals(expected, actual);
    }

    @Test
    public void test_controllerGroup() {
        final String key = "-master-controllerGroup-";
        final String expected = "select top 1 group_name, sum(newSumTotal) from ((select group_name, sum(sumTotal) as 'newSumTotal' from (select count(*) as 'sumTotal', b.group_name from event_e_lte_err_raw  a, GROUP_TYPE_E_RAT_VEND_HIER3 b, where a.hierarchy_3 = b.hierarchy_3 and a.hierarchy_3 is not null and a.vendor <> 'Unknown' and datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' group by a.hierarchy_3, b.group_name) as rawview group by group_name) union (select group_name, sum(sumTotal) as 'newSumTotal' from (select count(*) as 'sumTotal', b.group_name from event_e_sgeh_err_raw  a, GROUP_TYPE_E_RAT_VEND_HIER3 b, where a.hierarchy_3 = b.hierarchy_3 and a.hierarchy_3 is not null and a.vendor <> 'Unknown' and datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' group by a.hierarchy_3, b.group_name) as rawview group by group_name)) as rawView group by group_name order by sum(newSumTotal) desc";
        final String actual = sqlManager.getGroupSql(tc, key);
        assertEquals(expected, actual);
    }

    @Test
    public void test_accessAreaGroup() {
        final String key = "-master-AccessAreaGroup-";
        final String expected = "select top 1 group_name, sum(newSumTotal) from ((select group_name, sum(sumTotal) as 'newSumTotal' from (select count(*) as 'sumTotal', b.group_name from event_e_lte_err_raw  a, GROUP_TYPE_E_RAT_VEND_HIER321 b, where a.hierarchy_1 = b.hierarchy_1 and a.hierarchy_1 is not null and a.vendor <> 'Unknown' and datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' group by a.hierarchy_1, b.group_name) as rawview group by group_name) union (select group_name, sum(sumTotal) as 'newSumTotal' from (select count(*) as 'sumTotal', b.group_name from event_e_sgeh_err_raw  a, GROUP_TYPE_E_RAT_VEND_HIER321 b, where a.hierarchy_1 = b.hierarchy_1 and a.hierarchy_1 is not null and a.vendor <> 'Unknown' and datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' group by a.hierarchy_1, b.group_name) as rawview group by group_name)) as rawView group by group_name order by sum(newSumTotal) desc";
        final String actual = sqlManager.getGroupSql(tc, key);
        assertEquals(expected, actual);
    }

    @Test
    public void test_sgsnGroup() {
        final String key = "-master-sgsnGroup-";
        final String expected = "select top 1 group_name, sum(newSumTotal) from ((select group_name, sum(sumTotal) as 'newSumTotal' from (select count(*) as 'sumTotal', b.group_name from event_e_lte_err_raw  a, group_type_e_evntsrc b, where a.event_source_name = b.event_source_name and a.event_source_name is not null and datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' group by a.event_source_name, b.group_name) as rawview group by group_name) union (select group_name, sum(sumTotal) as 'newSumTotal' from (select count(*) as 'sumTotal', b.group_name from event_e_sgeh_err_raw  a, group_type_e_evntsrc b, where a.event_source_name = b.event_source_name and a.event_source_name is not null and datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' group by a.event_source_name, b.group_name) as rawview group by group_name)) as rawView group by group_name order by sum(newSumTotal) desc";
        final String actual = sqlManager.getGroupSql(tc, key);
        assertEquals(expected, actual);
    }

    @Test
    public void test_imsiGroup() {
        final String key = "-master-imsiGroup-";
        final String expected = "select top 1 group_name, sum(newSumTotal) from ((select group_name, sum(sumTotal) as 'newSumTotal' from (select count(*) as 'sumTotal', b.group_name from event_e_lte_err_raw  a, group_type_e_imsi b, where a.imsi = b.imsi and a.imsi is not null and datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' group by a.imsi, b.group_name) as rawview group by group_name) union (select group_name, sum(sumTotal) as 'newSumTotal' from (select count(*) as 'sumTotal', b.group_name from event_e_sgeh_err_raw  a, group_type_e_imsi b, where a.imsi = b.imsi and a.imsi is not null and datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' group by a.imsi, b.group_name) as rawview group by group_name)) as rawView group by group_name order by sum(newSumTotal) desc";
        final String actual = sqlManager.getGroupSql(tc, key);
        assertEquals(expected, actual);
    }

    // 2G3G4G CORE QOS Group

    @Test
    public void test_apnGroup_QOS() {
        final String key = "-master-qos-apnGroup-";
        final String expected = "select top 1 group_name, SUM(sumTotal) as 'Total' from (select group_name, sum(qci_err_1 + qci_err_2 + qci_err_3 + qci_err_4 + qci_err_5 + qci_err_6 + qci_err_7 + qci_err_8 + qci_err_9 + qci_err_10) as 'sumTotal' from event_e_lte_err_raw a, group_type_e_apn b, where a.apn = b.apn and a.apn is not null and datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' group by group_name ) as tempTable group by group_name order by Total desc";
        final String actual = sqlManager.getGroupSql(tc, key);
        assertEquals(expected, actual);
    }

    @Test
    public void test_terminalGroup_QOS() {
        final String key = "-master-qos-terminalGroup-";
        final String expected = "select top 1 group_name, SUM(sumTotal) as 'Total' from (select group_name, sum(qci_err_1 + qci_err_2 + qci_err_3 + qci_err_4 + qci_err_5 + qci_err_6 + qci_err_7 + qci_err_8 + qci_err_9 + qci_err_10) as 'sumTotal' from event_e_lte_err_raw a, GROUP_TYPE_E_TAC b, where a.tac = b.tac and a.tac is not null and a.tac <> 0 and datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' group by group_name ) as tempTable group by group_name order by Total desc";
        final String actual = sqlManager.getGroupSql(tc, key);
        assertEquals(expected, actual);
    }

    @Test
    public void test_controllerGroup_QOS() {
        final String key = "-master-qos-controllerGroup-";
        final String expected = "select top 1 group_name, SUM(sumTotal) as 'Total' from (select group_name, sum(qci_err_1 + qci_err_2 + qci_err_3 + qci_err_4 + qci_err_5 + qci_err_6 + qci_err_7 + qci_err_8 + qci_err_9 + qci_err_10) as 'sumTotal' from event_e_lte_err_raw a, GROUP_TYPE_E_RAT_VEND_HIER3 b, where a.hierarchy_3 = b.hierarchy_3 and a.hierarchy_3 is not null and a.vendor <> 'Unknown' and datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' group by group_name ) as tempTable group by group_name order by Total desc";
        final String actual = sqlManager.getGroupSql(tc, key);
        assertEquals(expected, actual);
    }

    @Test
    public void test_accessAreaGroup_QOS() {
        final String key = "-master-qos-AccessAreaGroup-";
        final String expected = "select top 1 group_name, SUM(sumTotal) as 'Total' from (select group_name, sum(qci_err_1 + qci_err_2 + qci_err_3 + qci_err_4 + qci_err_5 + qci_err_6 + qci_err_7 + qci_err_8 + qci_err_9 + qci_err_10) as 'sumTotal' from event_e_lte_err_raw a, GROUP_TYPE_E_RAT_VEND_HIER321 b, where a.hierarchy_1 = b.hierarchy_1 and a.hierarchy_1 is not null and a.vendor <> 'Unknown' and datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' group by group_name ) as tempTable group by group_name order by Total desc";
        final String actual = sqlManager.getGroupSql(tc, key);
        assertEquals(expected, actual);
    }

    @Test
    public void test_sgsnGroup_QOS() {
        final String key = "-master-qos-sgsnGroup-";
        final String expected = "select top 1 group_name, SUM(sumTotal) as 'Total' from (select group_name, sum(qci_err_1 + qci_err_2 + qci_err_3 + qci_err_4 + qci_err_5 + qci_err_6 + qci_err_7 + qci_err_8 + qci_err_9 + qci_err_10) as 'sumTotal' from event_e_lte_err_raw a, group_type_e_evntsrc b, where a.event_source_name = b.event_source_name and a.event_source_name is not null and datetime_id >= '2014-12-30 15:00' and datetime_id < '2015-01-06 15:00' group by group_name ) as tempTable group by group_name order by Total desc";
        final String actual = sqlManager.getGroupSql(tc, key);
        assertEquals(expected, actual);
    }

}
