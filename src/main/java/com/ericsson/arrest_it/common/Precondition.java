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
package com.ericsson.arrest_it.common;

public class Precondition {

    private String sql = null;
    private String condition;
    private boolean isSqlPresent = false;

    public Precondition(final String condition) {
        this.condition = condition;
    }

    public Precondition(final String sql, final String condition) {
        this.sql = sql;
        this.condition = condition;
        this.isSqlPresent = true;
    }

    public String getSql() {
        return sql;
    }

    public String getCondition() {
        return condition;
    }

    public void setSql(final String sql) {
        this.sql = sql;
    }

    public void setCondition(final String condition) {
        this.condition = condition;
    }

    public boolean isSqlPresent() {
        return isSqlPresent;
    }

    public void setIsSqlPresent(final boolean isSqlPresent) {
        this.isSqlPresent = isSqlPresent;
    }

}
