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

import org.apache.commons.lang3.StringUtils;

public class SQLUtils {

    public static boolean isSQL(final String value) {
        boolean isSQL = false;
        if (StringUtils.containsIgnoreCase(value, "select ") && StringUtils.containsIgnoreCase(value, " from ")) {
            isSQL = true;
        }
        return isSQL;
    }

    public static String getDateTimeFromSql(final String sql) {
        int firstIndex = StringUtils.indexOfIgnoreCase(sql, "datetime_id");
        if (firstIndex == -1) {
            firstIndex = StringUtils.indexOfIgnoreCase(sql, "FIVE_MIN_AGG_TIME");
        }
        String timeValue = sql.substring(firstIndex);
        final int nextIndex = timeValue.indexOf("<");
        final int lastIndex = timeValue.indexOf(":", nextIndex) + 4;
        timeValue = timeValue.substring(0, lastIndex);

        return timeValue;
    }

    public static String createPTMSISql(final String dateTimeValues) {
        final String sql = "SELECT TOP 1 CAST(PTMSI AS VARCHAR(30)) AS 'PTMSI_ERR' FROM dc.event_e_SGEH_err_raw WHERE " + dateTimeValues
                + " AND PTMSI <> 0 AND PTMSI IS NOT NULL GROUP BY PTMSI_ERR ORDER BY COUNT(PTMSI_ERR) DESC";
        return sql;
    }

    public static String createIMSISql(final String dateTimeValues) {

        final String sql = "SELECT TOP 1 IMSI FROM (SELECT IMSI, COUNT(IMSI) AS TOTAL FROM dc.event_e_lte_err_raw WHERE " + dateTimeValues
                + " AND IMSI <> 0 GROUP BY IMSI UNION ALL SELECT IMSI, COUNT(IMSI) AS TOTAL FROM dc.event_e_sgeh_err_raw WHERE " + dateTimeValues
                + " AND IMSI <> 0 GROUP BY IMSI ) AS temp GROUP BY IMSI, TOTAL ORDER BY TOTAL DESC";
        return sql;
    }

    public static String createMsisdnSql(final String dateTimeValues) {
        final String sql = "SELECT TOP 1 MSISDN_ERR FROM ( SELECT distinct(errTable.MSISDN) AS 'MSISDN_ERR' FROM dc.event_e_lte_err_raw AS errTable, dc.dim_e_imsi_msisdn AS msisdnTable WHERE errTable.msisdn = msisdnTable.msisdn AND "
                + dateTimeValues
                + " AND errTable.msisdn <> 0 union all SELECT distinct(errTable.MSISDN) AS 'MSISDN_ERR' FROM dc.event_e_sgeh_err_raw AS errTable, dc.dim_e_imsi_msisdn AS msisdnTable WHERE errTable.msisdn = msisdnTable.msisdn AND "
                + dateTimeValues + " AND errTable.msisdn <> 0) as tempTable GROUP BY MSISDN_ERR  ORDER BY COUNT(MSISDN_ERR) DESC";
        return sql;
    }

    public static String createMsisdnTauSql(final String dateTimeValues) {
        final String sql = "SELECT TOP 1 errTable.MSISDN AS 'MSISDN_ERR' FROM dc.event_e_lte_err_raw AS errTable, dc.dim_e_imsi_msisdn AS msisdnTable WHERE errTable.msisdn = msisdnTable.msisdn AND "
                + dateTimeValues + " AND errTable.msisdn <> 0 and errTable.event_id =8 GROUP BY MSISDN_ERR ORDER BY COUNT(MSISDN_ERR) DESC";
        return sql;
    }

    public static String createMsisdnHandoverSql(final String dateTimeValues) {
        final String sql = "SELECT TOP 1 errTable.MSISDN AS 'MSISDN_ERR' FROM dc.event_e_lte_err_raw AS errTable, dc.dim_e_imsi_msisdn AS msisdnTable WHERE errTable.msisdn = msisdnTable.msisdn AND "
                + dateTimeValues + " AND errTable.msisdn <> 0 and errTable.event_id =7 GROUP BY MSISDN_ERR ORDER BY COUNT(MSISDN_ERR) DESC";
        return sql;
    }

    public static String createImsiHandoverSql(final String dateTimeValues) {
        final String sql = "SELECT TOP 1 errTable.IMSI AS 'IMSI_ERR' FROM dc.event_e_lte_err_raw AS errTable WHERE " + dateTimeValues
                + " and errTable.event_id =7 GROUP BY IMSI_ERR ORDER BY COUNT(IMSI_ERR) DESC";
        return sql;
    }

    public static String createImsiTauSql(final String dateTimeValues) {
        final String sql = "SELECT TOP 1 errTable.IMSI AS 'IMSI_ERR' FROM dc.event_e_lte_err_raw AS errTable WHERE " + dateTimeValues
                + " and errTable.event_id =8 GROUP BY IMSI_ERR ORDER BY COUNT(IMSI_ERR) DESC";
        return sql;
    }

    public static String createImsiGroupHandoverSql(final String dateTimeValues) {
        final String sql = "select top 1 b.group_name from (select distinct imsi, count(*) as noOfEvents from event_e_lte_err_raw where "
                + dateTimeValues + " and event_id=7 and imsi in (select imsi from GROUP_TYPE_E_IMSI) group by imsi) a, GROUP_TYPE_E_IMSI b "
                + "where a.imsi = b.imsi group by b.group_name order by sum(a.noOfEvents)";
        return sql;
    }

    public static String createImsiGroupTauSql(final String dateTimeValues) {
        final String sql = "select top 1 b.group_name from (select distinct imsi, count(*) as noOfEvents from event_e_lte_err_raw where "
                + dateTimeValues + " and event_id=8 and imsi in (select imsi from GROUP_TYPE_E_IMSI) group by imsi) a, GROUP_TYPE_E_IMSI b "
                + "where a.imsi = b.imsi group by b.group_name order by sum(a.noOfEvents)";
        return sql;
    }

    public static String createIndividualTableSpecificSql(final String attName, final String tableName, final String dateTimeValues) {
        final String exclude = getExcludeWhereClause(attName);

        final String sql = "select top 1 " + attName + " from event_e_" + tableName + "_err_raw where " + attName + " is not null" + exclude + "and "
                + dateTimeValues + " group by " + attName + " order by count(*) desc";
        return sql;
    }

    public static String createIndividualSql(final String attName, final String tableName, final String dateTimeValues) {
        final String exclude = getExcludeWhereClause(attName);

        final String partA = "select top 1 " + attName + ", sum(sumTotal) from(";
        final String partB = "(select " + attName + ", count(*) as 'sumTotal' from event_e_";
        final String partC = " where " + dateTimeValues + exclude + "and " + attName + " is not null group by " + attName + ")";
        final String partD = ") as rawView group by " + attName + " order by sum(sumtotal) desc";

        final String sql = partA + partB + "lte_err_raw" + partC + " union " + partB + "sgeh_err_raw" + partC + partD;

        return sql;
    }

    public static String createIndividualQosSql(final String attName, final String dateTimeValues) {
        final String exclude = getExcludeWhereClause(attName);
        final String sql = "select top 1 "
                + attName
                + " , sum(qci_err_1 + qci_err_2 + qci_err_3 + qci_err_4 + qci_err_5 + qci_err_6 + qci_err_7 + qci_err_8 + qci_err_9 + qci_err_10) as 'sumtotal' from event_e_lte_err_raw where "
                + dateTimeValues + exclude + "and  " + attName + " is not null Group By " + attName + " Order by sumtotal DESC";
        return sql;
    }

    public static String createGroupQosSql(final String attName, final String groupTableName, final String dateTimeValues) {
        final String exclude = getExcludeWhereClause_TableA(attName);

        final String sql = "select top 1 group_name, SUM(sumTotal) as 'Total' from (select group_name, sum(qci_err_1 + qci_err_2 + qci_err_3 + qci_err_4 + qci_err_5 + qci_err_6 + qci_err_7 + qci_err_8 + qci_err_9 + qci_err_10) as 'sumTotal' "
                + "from event_e_lte_err_raw a, "
                + groupTableName
                + " b, where a."
                + attName
                + " = b."
                + attName
                + " and a."
                + attName
                + " is not null" + exclude + "and " + dateTimeValues + " group by group_name ) as tempTable group by group_name order by Total desc";
        return sql;
    }

    public static String createGroupTableSpecificSql(final String attName, final String tableName, final String groupTable,
                                                     final String dateTimeValues) {
        final String exclude = getExcludeWhereClause_TableA(attName);
        String sql;
        final String partA = "select top 1 group_name, sum(sumTotal) from (select count(*) as 'sumTotal', b.group_name from event_e_";
        final String partB = "_err_raw a, " + groupTable + " b, where a." + attName + " = b." + attName + " and a." + attName + " is not null"
                + exclude + "and " + dateTimeValues + " group by a." + attName
                + ",b.group_name) as rawview group by group_name order by sum(sumTotal)";
        sql = partA + tableName + partB;
        return sql;
    }

    public static String createGroupSql(final String attName, final String groupTable, final String dateTimeValues) {
        final String exclude = getExcludeWhereClause_TableA(attName);
        String sql;
        final String partA = "select top 1 group_name, sum(newSumTotal) from (";
        final String partB = "(select group_name, sum(sumTotal) as 'newSumTotal' from (select count(*) as 'sumTotal', b.group_name from event_e_";
        final String partC = " a, " + groupTable + " b, where a." + attName + " = b." + attName + " and a." + attName + " is not null" + exclude
                + "and " + dateTimeValues + " group by a." + attName + ", b.group_name) as rawview group by group_name)";
        final String partD = ") as rawView group by group_name order by sum(newSumTotal) desc";
        sql = partA + partB + "lte_err_raw " + partC + " union " + partB + "sgeh_err_raw " + partC + partD;
        return sql;
    }

    public static String createControllerNodeSql(final String idVal) {
        final String sql = "Select top 1 hierarchy_3 ||','|| vendor ||','|| rat "
                + "from (select rat, vendor, hierarchy_3 from event_e_lte_err_raw where hierarchy_3='" + idVal + "'"
                + "union all select rat, vendor, hierarchy_3 from event_e_sgeh_err_raw where hierarchy_3='" + idVal + "') as tmp";
        return sql;
    }

    public static String createRNCControllerNodeSql(final String dateTimeValues) {
        final String sql = "Select top 1 hierarchy_3 ||','|| vendor ||','|| rat as 'NODE' FROM (select rat, vendor, hierarchy_3 from dc.event_e_sgeh_err_raw WHERE "
                + dateTimeValues
                + " AND RAT=1 AND hierarchy_3 <> 'Unknown' AND Vendor <> 'Unknown' ) AS tmp GROUP BY hierarchy_3,rat,vendor ORDER BY count(*) DESC";
        return sql;
    }

    public static String createUserPlaneControllerNodeSql(final String dateTimeValues) {
        final String sql = "Select top 1 hierarchy_3 ||','|| vendor ||',3G' as 'NODE' FROM (select vendor, hierarchy_3  FROM DIM_E_SGEH_HIER321 WHERE HIER3_ID IN (SELECT HIER3_ID from dc.EVENT_E_USER_PLANE_TCP_RAW WHERE "
                + dateTimeValues + " GROUP BY HIER3_ID ORDER BY COUNT(HIER3_ID) DESC) AND RAT=1 ) AS tmp";
        return sql;
    }

    public static String createAccessAreaNodeSql(final String idVal) {
        final String sql = "Select top 1 hierarchy_1 ||','|| hierarchy_2 ||','|| hierarchy_3 ||','|| vendor ||','|| rat"
                + " from (select rat, vendor, hierarchy_3, hierarchy_1, hierarchy_2 from event_e_lte_err_raw where hierarchy_1='" + idVal
                + "' and hierarchy_3 <> 'unknown' and vendor <> 'unknown'"
                + " union all select rat, vendor, hierarchy_3, hierarchy_1, hierarchy_2 from event_e_sgeh_err_raw where hierarchy_1='" + idVal
                + "' and hierarchy_3 <> 'unknown' and vendor <> 'unknown' ) as tmp";
        return sql;
    }

    public static String createRNCAccessAreaNodeSql(final String dateTimeValues) {
        final String sql = "SELECT hierarchy_1 ||','|| hierarchy_2 ||','|| hierarchy_3 ||','|| vendor ||','|| rat as 'NODE' FROM( SELECT rat,vendor,hierarchy_1,hierarchy_3,hierarchy_2 FROM event_e_sgeh_err_raw err WHERE "
                + dateTimeValues
                + " AND RAT=1 AND hierarchy_3 <> 'Unknown' AND hierarchy_1 <> 'Unknown' AND hierarchy_1 in ( SELECT hierarchy_1 FROM DIM_E_SGEH_HIER321_CELL WHERE Cell_id is not NULL) AND Vendor <> 'Unknown' ) AS tmp";
        return sql;
    }

    public static String createUserPlaneAccessAreaNodeSql(final String dateTimeValues) {
        final String sql = "Select top 1 hierarchy_1 ||','|| hierarchy_2 ||','|| hierarchy_3 ||','|| vendor ||',3G' as 'NODE' FROM (Select vendor, hierarchy_3, hierarchy_1, hierarchy_2 FROM DIM_E_SGEH_HIER321 WHERE HIER321_ID IN (SELECT HIER321_ID from dc.EVENT_E_USER_PLANE_TCP_RAW WHERE "
                + dateTimeValues
                + " GROUP BY HIER321_ID ORDER BY COUNT(HIER321_ID) DESC) AND RAT=1 AND hierarchy_3 <> 'Unknown' AND hierarchy_1 <> 'Unknown' AND hierarchy_1 in ( SELECT hierarchy_1 FROM DIM_E_SGEH_HIER321_CELL WHERE Cell_id is not NULL) AND Vendor <> 'Unknown' ) AS tmp;";
        return sql;
    }

    public static String createTerminalNodeSql(final String idVal) {
        final String sql = "Select model ||','|| '" + idVal + "' from dim_e_sgeh_tac where tac = " + idVal;
        return sql;
    }

    public static String getExcludeWhereClause(final String attName) {
        String exclude = " ";
        if (attName.equalsIgnoreCase("tac")) {
            exclude = " and tac <> 0 ";
        } else if (attName.equalsIgnoreCase("hierarchy_1") || attName.equalsIgnoreCase("hierarchy_3")) {
            exclude = " and vendor <> 'Unknown' ";
        }
        return exclude;
    }

    public static String getExcludeWhereClause_TableA(final String attName) {
        String exclude = " ";
        if (attName.equalsIgnoreCase("tac")) {
            exclude = " and a.tac <> 0 ";
        } else if (attName.equalsIgnoreCase("hierarchy_1") || attName.equalsIgnoreCase("hierarchy_3")) {
            exclude = " and a.vendor <> 'Unknown' ";
        }
        return exclude;
    }
}
