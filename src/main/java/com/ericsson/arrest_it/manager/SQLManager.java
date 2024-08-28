package com.ericsson.arrest_it.manager;

import org.apache.commons.lang3.StringUtils;

import com.ericsson.arrest_it.common.TestCase;
import com.ericsson.arrest_it.utils.SQLUtils;

public class SQLManager implements Manager {

    public String getIndividualSql(final TestCase tc, final String key) {
        String sql = null;
        boolean tableSpecific = false;
        final String dateTimeValues = SQLUtils.getDateTimeFromSql(tc.getDbQueries().get(0).getTemplateStatement());
        String tableName = "";
        String attName = "";

        if (StringUtils.containsIgnoreCase(key, "lte")) {
            tableSpecific = true;
            tableName = "lte";
        } else if (StringUtils.containsIgnoreCase(key, "sgeh")) {
            tableSpecific = true;
            tableName = "sgeh";
        }

        if (StringUtils.containsIgnoreCase(key, "rncControllerNode")) {
            sql = SQLUtils.createRNCControllerNodeSql(dateTimeValues);
        } else if (StringUtils.containsIgnoreCase(key, "rncAccessAreaNode")) {
            sql = SQLUtils.createRNCAccessAreaNodeSql(dateTimeValues);
        } else if (StringUtils.containsIgnoreCase(key, "UserPlaneControllerNode")) {
            sql = SQLUtils.createUserPlaneControllerNodeSql(dateTimeValues);
        } else if (StringUtils.containsIgnoreCase(key, "UserPlaneAccessAreaNode")) {
            sql = SQLUtils.createUserPlaneAccessAreaNodeSql(dateTimeValues);
        } else if (StringUtils.containsIgnoreCase(key, "terminalNode")) {
            attName = "tac";
        } else if (StringUtils.containsIgnoreCase(key, "accessAreaNode")) {
            attName = "hierarchy_1";
        } else if (StringUtils.containsIgnoreCase(key, "controllerNode")) {
            attName = "hierarchy_3";
        } else if (StringUtils.containsIgnoreCase(key, "msisdnTau")) {
            sql = SQLUtils.createMsisdnTauSql(dateTimeValues);
        } else if (StringUtils.containsIgnoreCase(key, "msisdnHandover")) {
            sql = SQLUtils.createMsisdnHandoverSql(dateTimeValues);
        } else if (StringUtils.containsIgnoreCase(key, "msisdn")) {
            sql = SQLUtils.createMsisdnSql(dateTimeValues);
        } else if (StringUtils.containsIgnoreCase(key, "terminal")) {
            attName = "tac";
        } else if (StringUtils.containsIgnoreCase(key, "apn")) {
            attName = "apn";
        } else if (StringUtils.containsIgnoreCase(key, "imsiTau")) {
            sql = SQLUtils.createImsiTauSql(dateTimeValues);
        } else if (StringUtils.containsIgnoreCase(key, "imsiHandover")) {
            sql = SQLUtils.createImsiHandoverSql(dateTimeValues);
        } else if (StringUtils.containsIgnoreCase(key, "imsi")) {
            sql = SQLUtils.createIMSISql(dateTimeValues);
        } else if (StringUtils.containsIgnoreCase(key, "controller")) {
            attName = "hierarchy_3";
        } else if (StringUtils.containsIgnoreCase(key, "accessarea")) {
            attName = "hierarchy_1";
        } else if (StringUtils.containsIgnoreCase(key, "sgsn")) {
            attName = "event_source_name";
        } else if (StringUtils.containsIgnoreCase(key, "tracking")) {
            attName = "trac";
            tableName = "lte";
            tableSpecific = true;
        } else if (StringUtils.containsIgnoreCase(key, "ptmsi")) {
            sql = SQLUtils.createPTMSISql(dateTimeValues);
        }

        if (sql == null) {
            if (StringUtils.containsIgnoreCase(key, "qos")) {
                sql = SQLUtils.createIndividualQosSql(attName, dateTimeValues);
            } else if (tableSpecific) {
                sql = SQLUtils.createIndividualTableSpecificSql(attName, tableName, dateTimeValues);
            } else {
                sql = SQLUtils.createIndividualSql(attName, tableName, dateTimeValues);
            }
        }

        return sql;
    }

    public String getGroupSql(final TestCase tc, final String key) {
        String sql = null;
        final String dateTimeValues = SQLUtils.getDateTimeFromSql(tc.getDbQueries().get(0).getTemplateStatement());
        String attName = "";
        String tableName = "";
        String groupTable = "";
        boolean tableSpecific = false;

        if (StringUtils.containsIgnoreCase(key, "lte")) {
            tableSpecific = true;
            tableName = "lte";
        } else if (StringUtils.containsIgnoreCase(key, "sgeh")) {
            tableSpecific = true;
            tableName = "sgeh";
        }

        if (StringUtils.containsIgnoreCase(key, "imsigroupTau")) {
            sql = SQLUtils.createImsiGroupTauSql(dateTimeValues);
        } else if (StringUtils.containsIgnoreCase(key, "imsigroupHandover")) {
            sql = SQLUtils.createImsiGroupHandoverSql(dateTimeValues);
        } else if (StringUtils.containsIgnoreCase(key, "imsi")) {
            attName = "imsi";
            groupTable = "group_type_e_imsi";
        } else if (StringUtils.containsIgnoreCase(key, "apn")) {
            attName = "apn";
            groupTable = "group_type_e_apn";
        } else if (StringUtils.containsIgnoreCase(key, "controller")) {
            attName = "hierarchy_3";
            groupTable = "GROUP_TYPE_E_RAT_VEND_HIER3";
        } else if (StringUtils.containsIgnoreCase(key, "sgsn")) {
            attName = "event_source_name";
            groupTable = "group_type_e_evntsrc";
        } else if (StringUtils.containsIgnoreCase(key, "terminal")) {
            attName = "tac";
            groupTable = "GROUP_TYPE_E_TAC";
        } else if (StringUtils.containsIgnoreCase(key, "tracking")) {
            attName = "trac";
            groupTable = "GROUP_TYPE_E_LTE_TRAC";
        } else if (StringUtils.containsIgnoreCase(key, "access")) {
            attName = "hierarchy_1";
            groupTable = "GROUP_TYPE_E_RAT_VEND_HIER321";
        }

        if (sql == null) {
            if (StringUtils.containsIgnoreCase(key, "qos")) {
                sql = SQLUtils.createGroupQosSql(attName, groupTable, dateTimeValues);
            } else if (tableSpecific) {
                sql = SQLUtils.createGroupTableSpecificSql(attName, tableName, groupTable, dateTimeValues);
            } else {
                sql = SQLUtils.createGroupSql(attName, groupTable, dateTimeValues);
            }
        }

        return sql;
    }

}