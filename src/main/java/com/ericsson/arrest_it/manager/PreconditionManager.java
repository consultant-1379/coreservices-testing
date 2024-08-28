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

import java.util.List;

import javax.script.ScriptException;

import com.ericsson.arrest_it.common.*;

@SuppressWarnings("restriction")
public class PreconditionManager implements Manager {
    private final QueryManager queryManager;
    private final ValidationManager validationManager;
    private final TimeManager timeManager;

    public PreconditionManager(final QueryManager queryManager, final ValidationManager validationManager, final TimeManager timeManager)
            throws ArrestItException {
        this.queryManager = queryManager;
        this.validationManager = validationManager;
        this.timeManager = timeManager;
    }

    public boolean isPreconditionPassed(final TestCase parentTestCase, Precondition precondition, final int drillIndex) throws ArrestItException {

        if (precondition.isSqlPresent()) {
            precondition = getUpdatedSqlPrecondition(parentTestCase, precondition, drillIndex);
        } else {
            precondition = getUpdatedNonSqlPrecondition(parentTestCase, precondition, drillIndex);
        }

        return isEnrichedPreconditionPassed(parentTestCase, precondition);
    }

    private Precondition getUpdatedSqlPrecondition(TestCase parentTestCase, final Precondition precondition, final int drillIndex)
            throws ArrestItException {
        String sql = precondition.getSql();
        String preconditionString = precondition.getCondition();

        sql = updateParentKeyToMatchJsonKeyInParentTestCase(sql, parentTestCase);
        sql = queryManager.enrichSqlWithMasterValues(sql, parentTestCase);
        sql = queryManager.enrichSqlWithParentValues(sql, parentTestCase);
        sql = queryManager.enrichSqlWithJsonValues(sql, drillIndex, parentTestCase);
        sql = timeManager.updateSqlWithRelevantTestTimes(sql, parentTestCase.getTime());

        DBQuery dbQuery = new DBQuery(sql);

        parentTestCase = queryManager.runSql(parentTestCase, dbQuery, String.valueOf(drillIndex));

        preconditionString = updateParentKeyToMatchJsonKeyInParentTestCase(preconditionString, parentTestCase);

        preconditionString = validationManager.replaceKeyVariablesWithValues(parentTestCase, preconditionString, drillIndex);
        preconditionString = preconditionString.replaceAll("\\{", "");
        preconditionString = preconditionString.replaceAll("\\}", "");

        return new Precondition(preconditionString);
    }

    private Precondition getUpdatedNonSqlPrecondition(final TestCase parentTestCase, final Precondition precondition, final int drillIndex)
            throws ArrestItException {
        String preconditionString = precondition.getCondition();
        preconditionString = updateParentKeyToMatchJsonKeyInParentTestCase(preconditionString, parentTestCase);

        preconditionString = validationManager.replaceKeyVariablesWithValues(parentTestCase, preconditionString, drillIndex);
        preconditionString = preconditionString.replaceAll("\\{", "");
        preconditionString = preconditionString.replaceAll("\\}", "");

        return new Precondition(preconditionString);
    }

    private boolean isEnrichedPreconditionPassed(final TestCase parentTestCase, final Precondition precondition) throws ArrestItException {
        boolean isPassed = false;

        if (precondition.getCondition().isEmpty()) {
            isPassed = true;
        } else {
            Validation validation = new Validation(precondition.getCondition(), false, 4);
            validation = validationManager.splitEquation(validation);

            if (validationManager.isNonNumericComparison(validation)) {
                isPassed = validationManager.isAlphabeticTestPassed(validation);
            } else {
                try {
                    validation.setLeftSideNumericalValue(validationManager.performCalculation(validation.getLeftSideOfEquation()));
                    validation.setRightSideNumericalValue(validationManager.performCalculation(validation.getRightSideOfEquation()));
                } catch (final ScriptException e) {
                    throw new ArrestItException(e);
                }
                isPassed = validationManager.isNumericTestPassed(validation, parentTestCase);
            }
        }

        return isPassed;
    }

    private String updateParentKeyToMatchJsonKeyInParentTestCase(String preconditionText, final TestCase parentTest) {
        final String parentId = parentTest.getId();
        final List<String> parentKeys = queryManager.getWordsOfInterest(preconditionText, Constants.PARENT_REPLACE_REGEX);

        for (final String parentKey : parentKeys) {
            final String testCaseReference = parentKey.substring(0, parentKey.indexOf("."));
            if (testCaseReference.equals(parentId)) {
                preconditionText = preconditionText.replaceAll(parentKey, parentKey.substring(parentKey.indexOf(".") + 1));
            }
        }

        return preconditionText;

    }
}