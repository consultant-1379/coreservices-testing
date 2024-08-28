package com.ericsson.arrest_it.common;

import java.math.BigDecimal;
import java.util.Comparator;

import org.apache.commons.lang3.math.NumberUtils;

public class UiRankComparator implements Comparator<Object> {

    public int compare(final Object o1, final Object o2) {
        String s1 = (String) o1;
        String s2 = (String) o2;
        if (NumberUtils.isNumber(s1)) {
            BigDecimal bgd1 = new BigDecimal(s1);
            BigDecimal bgd2 = new BigDecimal(s2);
            return bgd1.compareTo(bgd2);
        }
        return s1.toLowerCase().compareTo(s2.toLowerCase());
    }

}
