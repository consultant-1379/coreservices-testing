package com.ericsson.arrest_it.common;

import static com.ericsson.arrest_it.common.Constants.*;
import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

public class UiRankComparatorTest {
    static UiRankComparator UI_RANK_COMPARATOR;
    final static String EQUAL_FAIL_MESSAGE = "Test with Equal values comparison not returning 0 as expected. Values: ";
    final static String GREATER_THAN_FAIL_MESSAGE = "Test with First Value Greater not returning 1 as expected. Values: ";
    final static String LESS_THAN_FAIL_MESSAGE = "Test with First Value Less not returning -1 as expected. Values: ";
    final static int EXPECTED_EQUAL_VALUE = 0;
    final static int EXPECTED_GREATER_THAN_VALUE = 1;
    final static int EXPECTED_LESS_THAN_VALUE = -1;

    @BeforeClass
    public static void setUp() {
        UI_RANK_COMPARATOR = new UiRankComparator();
    }

    @Test
    public void test_compareEqualTimes() {
        String timeA = "2015-03-19 09:44";
        String timeB = "2015-03-19 09:44";
        int actualValue = UI_RANK_COMPARATOR.compare(timeA, timeB);
        String failMessage = EQUAL_FAIL_MESSAGE + timeA + TAB + timeB;

        assertEquals(failMessage, EXPECTED_EQUAL_VALUE, actualValue);
    }

    @Test
    public void test_compareTimesFirstGreater() {
        String timeA = "2015-03-19 09:45";
        String timeB = "2015-03-19 09:44";
        int actualValue = UI_RANK_COMPARATOR.compare(timeA, timeB);
        String failMessage = GREATER_THAN_FAIL_MESSAGE + timeA + TAB + timeB;

        assertEquals(failMessage, EXPECTED_GREATER_THAN_VALUE, actualValue);
    }

    @Test
    public void test_compareTimesFirstLess() {
        String timeA = "2015-03-19 08:44";
        String timeB = "2015-03-19 10:44";
        int actualValue = UI_RANK_COMPARATOR.compare(timeA, timeB);
        String failMessage = LESS_THAN_FAIL_MESSAGE + timeA + TAB + timeB;

        assertEquals(failMessage, EXPECTED_LESS_THAN_VALUE, actualValue);
    }

    @Test
    public void test_compareLargeNumbersEquals() {
        String firstNumber = "1234567890123456789012345";
        String secondNumber = "1234567890123456789012345";
        int actualValue = UI_RANK_COMPARATOR.compare(firstNumber, secondNumber);
        String failMessage = EQUAL_FAIL_MESSAGE + firstNumber + TAB + secondNumber;

        assertEquals(failMessage, EXPECTED_EQUAL_VALUE, actualValue);
    }

    @Test
    public void test_compareLargeNumbersFirstGreater() {
        String firstNumber = "1234567890123456789012348";
        String secondNumber = "1234567890123456789012345";
        int actualValue = UI_RANK_COMPARATOR.compare(firstNumber, secondNumber);
        String failMessage = GREATER_THAN_FAIL_MESSAGE + firstNumber + TAB + secondNumber;

        assertEquals(failMessage, EXPECTED_GREATER_THAN_VALUE, actualValue);
    }

    @Test
    public void test_compareLargeNumbersFirstLess() {
        String firstNumber = "1234567890123456789012342";
        String secondNumber = "1234567890123456789012345";
        int actualValue = UI_RANK_COMPARATOR.compare(firstNumber, secondNumber);
        String failMessage = LESS_THAN_FAIL_MESSAGE + firstNumber + TAB + secondNumber;

        assertEquals(failMessage, EXPECTED_LESS_THAN_VALUE, actualValue);
    }

    @Test
    public void test_compareDecimalsEquals() {
        String firstNumber = "2.3456789";
        String secondNumber = "2.3456789";
        int actualValue = UI_RANK_COMPARATOR.compare(firstNumber, secondNumber);
        String failMessage = EQUAL_FAIL_MESSAGE + firstNumber + TAB + secondNumber;

        assertEquals(failMessage, EXPECTED_EQUAL_VALUE, actualValue);
    }

    @Test
    public void test_compareStringsEquals() {
        String firstWord = "apple";
        String secondWord = "APPLE";
        int actualValue = UI_RANK_COMPARATOR.compare(firstWord, secondWord);
        String failMessage = EQUAL_FAIL_MESSAGE + firstWord + TAB + secondWord;

        assertEquals(failMessage, EXPECTED_EQUAL_VALUE, actualValue);
    }
}
