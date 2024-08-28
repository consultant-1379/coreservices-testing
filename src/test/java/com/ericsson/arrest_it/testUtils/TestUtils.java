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
package com.ericsson.arrest_it.testUtils;

import static com.ericsson.arrest_it.common.Constants.*;
import junit.extensions.PA;

import org.joda.time.DateTime;

import com.ericsson.arrest_it.common.ArrestItException;
import com.ericsson.arrest_it.common.Constants;
import com.ericsson.arrest_it.manager.ManagerFactory;
import com.ericsson.arrest_it.manager.TimeManager;

public class TestUtils {
    public static TimeManager initializeTestTimeManager(final String tzOffset, final String endTimeString) throws ArrestItException {
        final TimeManager timeManager = (TimeManager) ManagerFactory.getInstance().getManager(Constants.TIME_MANAGER);

        PA.setValue(timeManager, "timezone", tzOffset);
        DateTime endTime = DateTime.parse(endTimeString);

        int offHours = 0;
        int offMinutes = 0;

        offHours = Integer.parseInt(tzOffset.substring(1, 3));
        offMinutes = Integer.parseInt(tzOffset.substring(3));

        offMinutes = offMinutes + (offHours * 60);

        if (tzOffset.substring(0, 1).equals(DASH)) {
            offMinutes = (offMinutes * -1);
        }
        endTime = endTime.plusMinutes(offMinutes);
        PA.setValue(timeManager, "OFFSETMINUTES", offMinutes);
        PA.setValue(timeManager, "BASETIME", endTime);
        return timeManager;
    }
}