package com.ericsson.arrest_it.common;

/**
 * Each validation object maps to a single validation provided in the test xml. Stores elements of the validation which are used as the validation is
 * processed.
 * 
 */
public class Validation {
    /**
     * Indicates whether the validation is performed just once per TestCase instance or on a row by row basis.
     */
    private boolean isRepeating;
    private boolean isLeftCountPresent;
    private String equation;
    private int noOfDecimalPlaces;
    private String leftSideOfEquation;
    private String rightSideOfEquation;
    private String operator;

    private boolean isRightCountPresent;
    private boolean isSumOrAvgPresent;
    private double leftSideNumericalValue;
    private double rightSideNumericalValue;

    public Validation(final String equation, final boolean repeating, final int noOfDecimals) {
        setEquation(equation);
        setRepeating(repeating);
        setNoOfDecimalPlaces(noOfDecimals);
        isLeftCountPresent = false;
        isRightCountPresent = false;
        isSumOrAvgPresent = false;
    }

    public Validation(final Validation another) {
        this.equation = another.getEquation();
        this.setRepeating(another.isRepeating());
        this.noOfDecimalPlaces = another.getNoOfDecimalPlaces();
        this.isLeftCountPresent = another.isLeftCountPresent();
        this.isRightCountPresent = another.isRightCountPresent();
        this.isSumOrAvgPresent = another.isSumOrAvgPresent();
    }

    public String getEquation() {
        return equation;
    }

    public void setEquation(final String equation) {
        this.equation = equation;
    }

    public int getNoOfDecimalPlaces() {
        return noOfDecimalPlaces;
    }

    public void setNoOfDecimalPlaces(final int noOfDecimalPlaces) {
        this.noOfDecimalPlaces = noOfDecimalPlaces;
    }

    public boolean isLeftCountPresent() {
        return isLeftCountPresent;
    }

    public void setLeftCountPresent(final boolean isLeftCountPresent) {
        this.isLeftCountPresent = isLeftCountPresent;
    }

    public boolean isRightCountPresent() {
        return isRightCountPresent;
    }

    public void setRightCountPresent(final boolean isRightCountPresent) {
        this.isRightCountPresent = isRightCountPresent;
    }

    public String getLeftSideOfEquation() {
        return leftSideOfEquation;
    }

    public void setLeftSideOfEquation(final String leftSideOfEquation) {
        this.leftSideOfEquation = leftSideOfEquation;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(final String operator) {
        this.operator = operator;
    }

    public String getRightSideOfEquation() {
        return rightSideOfEquation;
    }

    public void setRightSideOfEquation(final String rightSideOfEquation) {
        this.rightSideOfEquation = rightSideOfEquation;
    }

    public double getLeftSideNumericalValue() {
        return leftSideNumericalValue;
    }

    public void setLeftSideNumericalValue(final double leftSideNumericalValue) {
        this.leftSideNumericalValue = leftSideNumericalValue;
    }

    public double getRightSideNumericalValue() {
        return rightSideNumericalValue;
    }

    public void setRightSideNumericalValue(final double rightSideNumericalValue) {
        this.rightSideNumericalValue = rightSideNumericalValue;
    }

    public boolean isSumOrAvgPresent() {
        return isSumOrAvgPresent;
    }

    public void setSumOrAvgPresent(final boolean isSumOrAvgPresent) {
        this.isSumOrAvgPresent = isSumOrAvgPresent;
    }

    /**
     * @return {@link Validation#isRepeating}
     */
    public boolean isRepeating() {
        return isRepeating;
    }

    /**
     * @param isRepeating
     *        {@link Validation#isRepeating}
     */
    public void setRepeating(final boolean isRepeating) {
        this.isRepeating = isRepeating;
    }

}