package com.ericsson.arrest_it.manager;

import static com.ericsson.arrest_it.common.Constants.*;

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;

import com.ericsson.arrest_it.common.DBQuery;
import com.ericsson.arrest_it.common.TestCase;
import com.ericsson.arrest_it.io.PropertyReader;

public class TimeManager implements Manager {
    private static int OFFSETMINUTES;
    private static DateTime BASETIME;
    private static String timezone;

    public void setMainTime() {
        timezone = PropertyReader.getInstance().getTimezone();

        final String endTimeString = PropertyReader.getInstance().getEndTime();
        DateTime endTime = DateTime.parse(endTimeString);

        findTimezoneOffset();

        System.out.println("This is the offset Minutes: " + OFFSETMINUTES);

        BASETIME = endTime;
    }

    private static void findTimezoneOffset() {
        int offHours = 0;
        int offMinutes = 0;

        offHours = Integer.parseInt(timezone.substring(1, 3));
        offMinutes = Integer.parseInt(timezone.substring(3));

        offMinutes = offMinutes + (offHours * 60);

        if (timezone.substring(0, 1).equals(DASH)) {
            OFFSETMINUTES = (offMinutes * -1);
        } else {
            OFFSETMINUTES = offMinutes;
        }
    }

    public List<TestCase> enrich(final List<TestCase> suite, final int time) {

        // Create URL Times
        DateTime dateTimeTo = new DateTime(BASETIME);

        if (time == MINUTES_IN_A_WEEK) {
            dateTimeTo = goBackToMidnight(dateTimeTo);
        }

        final DateTime dateTimeFrom = dateTimeTo.minusMinutes(time);

        // Create URL Time Strings
        final Map<String, String> urlTimeStrings = createUrlTimeValues(dateTimeTo, dateTimeFrom);
        final Map<String, String> csvTimeStrings = createCsvTimeValues(dateTimeTo, dateTimeFrom);

        //Create SQL Times
        DateTime sqlTimeTo = new DateTime(dateTimeTo);
        sqlTimeTo = fixSqlTimeWithOffset(sqlTimeTo, time);
        final DateTime sqlTimeFrom = sqlTimeTo.minusMinutes(time);

        //Create SQL Time Strings
        final Map<String, String> sqlTimeStrings = createSqlTimeValues(sqlTimeTo, sqlTimeFrom);

        for (final TestCase tc : suite) {
            tc.setTime(time);
            tc.setUrl(replaceUrlTimeAndTimeZone(tc.getUrl(), urlTimeStrings));

            final List<DBQuery> newDbQueries = new ArrayList<DBQuery>();

            if (tc.getCsvUrl() != null) {
                tc.setCsvUrl(replaceCsvUrlTimeAndTimeZone(tc.getCsvUrl(), csvTimeStrings));
            }

            for (final DBQuery oldDbQuery : tc.getDbQueries()) {
                oldDbQuery.setTemplateStatement(replaceSqlTime(oldDbQuery.getTemplateStatement(), sqlTimeStrings));
                newDbQueries.add(oldDbQuery);
            }
            tc.setDbQueries(newDbQueries);
            tc.setTitle(addTimeToTitle(tc.getTitle(), time));
        }
        return suite;
    }

    public Map<String, String> createUrlTimeValues(final DateTime dateTimeTo, final DateTime dateTimeFrom) {
        final Map<String, String> urlTimes = new HashMap<String, String>();
        urlTimes.put(URL_DATE_TO_KEY, "" + addZeroToTimeValue(dateTimeTo.getDayOfMonth()) + addZeroToTimeValue(dateTimeTo.getMonthOfYear())
                + dateTimeTo.getYear());
        urlTimes.put(URL_DATE_FROM_KEY, "" + addZeroToTimeValue(dateTimeFrom.getDayOfMonth()) + addZeroToTimeValue(dateTimeFrom.getMonthOfYear())
                + dateTimeFrom.getYear());

        urlTimes.put(URL_TIME_TO_KEY, "" + addZeroToTimeValue(dateTimeTo.getHourOfDay()) + addZeroToTimeValue(dateTimeTo.getMinuteOfHour()));
        urlTimes.put(URL_TIME_FROM_KEY, "" + addZeroToTimeValue(dateTimeFrom.getHourOfDay()) + addZeroToTimeValue(dateTimeFrom.getMinuteOfHour()));

        return urlTimes;
    }

    public Map<String, String> createSqlTimeValues(final DateTime sqlTimeTo, final DateTime sqlTimeFrom) {
        final Map<String, String> sqlTimeStrings = new HashMap<String, String>();
        sqlTimeStrings.put(SQL_DATETIME_TO_KEY, SINGLE_QUOTE + sqlTimeTo.getYear() + DASH + addZeroToTimeValue(sqlTimeTo.getMonthOfYear()) + DASH
                + addZeroToTimeValue(sqlTimeTo.getDayOfMonth()) + " " + addZeroToTimeValue(sqlTimeTo.getHourOfDay()) + COLON
                + addZeroToTimeValue(sqlTimeTo.getMinuteOfHour()) + SINGLE_QUOTE);
        sqlTimeStrings.put(SQL_DATETIME_FROM_KEY, SINGLE_QUOTE + sqlTimeFrom.getYear() + DASH + addZeroToTimeValue(sqlTimeFrom.getMonthOfYear())
                + DASH + addZeroToTimeValue(sqlTimeFrom.getDayOfMonth()) + " " + addZeroToTimeValue(sqlTimeFrom.getHourOfDay()) + COLON
                + addZeroToTimeValue(sqlTimeFrom.getMinuteOfHour()) + SINGLE_QUOTE);
        return sqlTimeStrings;
    }

    public DateTime adjustCsvWithLocalTimeZone(final DateTime csvTime) {
        final DateTimeZone localTimezone = csvTime.getZone();
        final long localOffest = localTimezone.getOffset(csvTime.getMillis());
        return csvTime.plus(localOffest);
    }

    public Map<String, String> createCsvTimeValues(DateTime dateTimeTo, DateTime dateTimeFrom) {
        final Map<String, String> csvTimes = new HashMap<String, String>();
        dateTimeTo = adjustCsvWithLocalTimeZone(dateTimeTo);
        dateTimeFrom = adjustCsvWithLocalTimeZone(dateTimeFrom);
        dateTimeTo = dateTimeTo.minusMinutes(OFFSETMINUTES);
        dateTimeFrom = dateTimeFrom.minusMinutes(OFFSETMINUTES);

        csvTimes.put(CSV_DATATIME_TO_KEY, String.valueOf(dateTimeTo.getMillis()));
        csvTimes.put(CSV_DATATIME_FROM_KEY, String.valueOf(dateTimeFrom.getMillis()));

        return csvTimes;
    }

    String addZeroToTimeValue(final int timeValue) {
        String timeValWithZero;
        final int absoluteTimeValue = Math.abs(timeValue);

        if (absoluteTimeValue < 10) {
            timeValWithZero = ZERO_STRING + absoluteTimeValue;
        } else {
            timeValWithZero = "" + absoluteTimeValue;
        }
        return timeValWithZero;
    }

    String addZeroToMillisTimeValue(final int timeValue) {
        String timeValueWithZero;
        if (timeValue < 100) {
            timeValueWithZero = ZERO_STRING + timeValue;
        } else {
            timeValueWithZero = String.valueOf(timeValue);
        }

        return timeValueWithZero;
    }

    DateTime goBackToMidnight(final DateTime baseT) {
        DateTime newMidnight = new DateTime(baseT);
        newMidnight = newMidnight.minusHours(newMidnight.getHourOfDay());
        newMidnight = newMidnight.minusMinutes(newMidnight.getMinuteOfDay());
        return newMidnight;
    }

    String replaceUrlTimeAndTimeZone(String url, final Map<String, String> urlTimes) {
        url = replaceURLTime(url, urlTimes);
        url = replaceURLTimeZone(url);
        return url;
    }

    String replaceCsvUrlTimeAndTimeZone(String csvUrl, final Map<String, String> csvUrlTimes) {
        csvUrl = replaceCsvUrlTime(csvUrl, csvUrlTimes);
        csvUrl = replaceURLTimeZone(csvUrl);
        return csvUrl;
    }

    String replaceCsvUrlTime(String url, final Map<String, String> csvUrlTimeValues) {
        final String newTimeString = DATATIME_FROM + EQUALS + csvUrlTimeValues.get(CSV_DATATIME_FROM_KEY) + AMPERSAND + DATATIME_TO + EQUALS
                + csvUrlTimeValues.get(CSV_DATATIME_TO_KEY);
        final int urlFirstTimeIndex = url.indexOf(DATATIME_FROM + EQUALS);
        final int urlSecondTimeIndex = url.indexOf(DATATIME_TO + EQUALS);
        final int urlThirdTimeIndex = url.indexOf(AMPERSAND, urlSecondTimeIndex);
        final String oldUrlTime = url.substring(urlFirstTimeIndex, urlThirdTimeIndex);
        url = url.replace(oldUrlTime, newTimeString);
        return url;

    }

    private String replaceURLTime(String url, final Map<String, String> urlValues) {
        final String newTimeString = DATE_FROM + EQUALS + urlValues.get(URL_DATE_FROM_KEY) + AMPERSAND + DATE_TO + EQUALS
                + urlValues.get(URL_DATE_TO_KEY) + AMPERSAND + TIME_FROM + EQUALS + urlValues.get(URL_TIME_FROM_KEY) + AMPERSAND + TIME_TO + EQUALS
                + urlValues.get(URL_TIME_TO_KEY);
        if (url.contains(TIME + EQUALS)) {
            final int urlFirstTimeIndex = url.indexOf(TIME + EQUALS);
            final int urlSecondTimeIndex = url.indexOf(AMPERSAND, urlFirstTimeIndex);
            final String oldUrlTime = url.substring(urlFirstTimeIndex, urlSecondTimeIndex);
            url = url.replace(oldUrlTime, newTimeString);
        } else if (url.contains(DATE_FROM + EQUALS)) {
            final int urlFirstTimeIndex = url.indexOf(DATE_FROM + EQUALS);
            final int urlSecondTimeIndex = url.indexOf(TIME_TO + EQUALS);
            final int urlThirdTimeIndex = url.indexOf(AMPERSAND, urlSecondTimeIndex);
            final String oldUrlTime = url.substring(urlFirstTimeIndex, urlThirdTimeIndex);
            url = url.replace(oldUrlTime, newTimeString);
        }
        return url;
    }

    private String replaceURLTimeZone(String url) {
        if (timezone.startsWith("+")) {
            timezone = timezone.replace("+", "%2B");
        }

        final String timeZoneUrl = "tzOffset=" + timezone;
        final String decodedTimeZonePattern = "tzOffset=%2B\\d{4}";
        final String alternateTimeZonePattern = "tzOffset=[+-]\\d{4}";

        if (StringUtils.containsIgnoreCase(url, "tzOffset=%2B")) {
            url = url.replaceAll(decodedTimeZonePattern, timeZoneUrl);
        } else {
            url = url.replaceAll(alternateTimeZonePattern, timeZoneUrl);
        }

        return url;
    }

    DateTime fixSqlTimeWithOffset(DateTime sqlTime, final int time) {
        sqlTime = sqlTime.minusMinutes(OFFSETMINUTES);
        return sqlTime;
    }

    String replaceSqlTime(String sql, final Map<String, String> sqlTimeStrings) {
        int startIndex = 0, endIndex = 0, searchIndex = 0;
        String oldTime = "";
        if (StringUtils.containsIgnoreCase(sql, "datetime_id") || StringUtils.containsIgnoreCase(sql, "FIVE_MIN_AGG_TIME")) {

            do {
                startIndex = findFromTimeIndex(sql, searchIndex);

                if (startIndex > -1) {
                    endIndex = StringUtils.indexOfIgnoreCase(sql, SINGLE_QUOTE, startIndex + 1);
                    oldTime = sql.substring(startIndex, endIndex + 1);
                    sql = sql.replace(oldTime, sqlTimeStrings.get(SQL_DATETIME_FROM_KEY));

                    startIndex = findToTimeIndex(sql, startIndex);
                    endIndex = StringUtils.indexOfIgnoreCase(sql, SINGLE_QUOTE, startIndex + 1);
                    searchIndex = endIndex;

                    oldTime = sql.substring(startIndex, endIndex + 1);
                    sql = sql.replace(oldTime, sqlTimeStrings.get(SQL_DATETIME_TO_KEY));
                }
            } while (startIndex > -1);

        }
        return sql;
    }

    String addTimeToTitle(String title, final int time) {
        if (title.contains(COLON)) {
            title = title.substring(0, title.indexOf(COLON));
        }
        title += COLON + time;
        return title;
    }

    public String manageEventTime(String sql) {
        final int beginIndex = StringUtils.indexOfIgnoreCase(sql, "event_time =");
        final int tempEndIndex = sql.indexOf(SINGLE_QUOTE, beginIndex);
        final int endIndex = sql.indexOf(SINGLE_QUOTE, tempEndIndex + 1);
        final String eventTimeQuery = sql.substring(beginIndex, endIndex + 1);
        final String oldEventTimeQuery = eventTimeQuery;
        final int sybaseBuffer = PropertyReader.getInstance().getSybaseMillisBuffer();
        
        String etEdit = eventTimeQuery.substring(eventTimeQuery.indexOf(SINGLE_QUOTE) + 1, eventTimeQuery.lastIndexOf(SINGLE_QUOTE));
        final String year = etEdit.substring(0, etEdit.indexOf(DASH));
        final int yearInt = Integer.parseInt(year);
        etEdit = etEdit.substring(year.length() + 1);
        final String month = etEdit.substring(0, etEdit.indexOf(DASH));
        final int monthInt = Integer.parseInt(month);
        etEdit = etEdit.substring(month.length() + 1);
        final String day = etEdit.substring(0, etEdit.indexOf(" "));
        final int dayInt = Integer.parseInt(day);
        etEdit = etEdit.substring(day.length() + 1);
        final String hour = etEdit.substring(0, etEdit.indexOf(COLON));
        final int hourInt = Integer.parseInt(hour);
        etEdit = etEdit.substring(hour.length() + 1);
        final String minute = etEdit.substring(0, etEdit.indexOf(COLON));
        final int minuteInt = Integer.parseInt(minute);
        etEdit = etEdit.substring(minute.length() + 1);
        final String second = etEdit.substring(0, etEdit.indexOf("."));
        final int secondInt = Integer.parseInt(second);
        etEdit = etEdit.substring(second.length() + 1);
        final String millis = etEdit;

        int millisInt = Integer.parseInt(millis);
        if (millis.length() < 3) {
            millisInt = millisInt * 10;
        }
        if (millis.length() < 2) {
            millisInt = millisInt * 10;
        }
        DateTime eventTimeDateTime = new DateTime(yearInt, monthInt, dayInt, hourInt, minuteInt, secondInt, millisInt);

        eventTimeDateTime = eventTimeDateTime.minusMinutes(OFFSETMINUTES);
        String finalTimeQuery = "";
        
        if(sybaseBuffer == 0){
        	finalTimeQuery = getEventTimeWithoutBuffer(eventTimeDateTime);
        }else{
        	finalTimeQuery = getEventTimeWithBuffer(eventTimeDateTime, sybaseBuffer);
        }
        sql = sql.replace(oldEventTimeQuery, finalTimeQuery);

        return sql;
    }
private String parseDateTimeToSybaseString(DateTime dateTime)
{
	final String sqlDate= "" + dateTime.getYear() + DASH + addZeroToTimeValue(dateTime.getMonthOfYear()) + DASH
            + addZeroToTimeValue(dateTime.getDayOfMonth()) + " " + addZeroToTimeValue(dateTime.getHourOfDay()) + COLON
            + addZeroToTimeValue(dateTime.getMinuteOfHour()) + COLON + addZeroToTimeValue(dateTime.getSecondOfMinute()) + "."
            + addZeroToMillisTimeValue(dateTime.getMillisOfSecond());
	return sqlDate;
	}

private String getEventTimeWithBuffer(DateTime eventTimeDateTime, int sybaseBuffer)
{	

    final DateTime dtPlusMillis = eventTimeDateTime.plusMillis(sybaseBuffer);
    final DateTime dtMinusMillis = eventTimeDateTime.minusMillis(sybaseBuffer);
    
    String eventTimeMinus = parseDateTimeToSybaseString(dtMinusMillis);
    String eventTimePlus = parseDateTimeToSybaseString(dtPlusMillis);
    
    final String finalTimeQuery = "event_time >= '" + eventTimeMinus + "' and event_time < '" + eventTimePlus + SINGLE_QUOTE;
	return finalTimeQuery;
	}



private String getEventTimeWithoutBuffer(DateTime eventTimeDateTime){
	String eventTimeWithoutBuffer = parseDateTimeToSybaseString(eventTimeDateTime);
	
	final String eventTimeQuery = "event_time = '" + eventTimeWithoutBuffer + SINGLE_QUOTE;
	
	return eventTimeQuery;
}
    /**
     * Locates the first position to replace the "from time", weather this is "datetime_id" or "event_time".
     *
     * @param sql
     *        The SQL query to examine for "from time" indices.
     * @return The location to start the replacement for the "from time".
     */
    private int findFromTimeIndex(final String sql, final int searchStartIndex) {

        if (StringUtils.containsIgnoreCase(sql, FROM_DATETIME_ID_WITH_SPACE)) {
            return getIndexOfSingleQuoteAfterTimeValue(sql, FROM_DATETIME_ID_WITH_SPACE, searchStartIndex);
        } else if (StringUtils.containsIgnoreCase(sql, FROM_DATETIME_ID_WITHOUT_SPACE)) {
            return getIndexOfSingleQuoteAfterTimeValue(sql, FROM_DATETIME_ID_WITHOUT_SPACE, searchStartIndex);
        } else if (StringUtils.containsIgnoreCase(sql, FROM_EVENT_TIME_WITH_SPACE)) {
            return getIndexOfSingleQuoteAfterTimeValue(sql, FROM_EVENT_TIME_WITH_SPACE, searchStartIndex);
        } else if (StringUtils.containsIgnoreCase(sql, FROM_EVENT_TIME_WITHOUT_SPACE)) {
            return getIndexOfSingleQuoteAfterTimeValue(sql, FROM_EVENT_TIME_WITHOUT_SPACE, searchStartIndex);
        } else if (StringUtils.containsIgnoreCase(sql, FROM_FIVE_MIN_AGG_TIME_WITH_SPACE)) {
            return getIndexOfSingleQuoteAfterTimeValue(sql, FROM_FIVE_MIN_AGG_TIME_WITH_SPACE, searchStartIndex);
        } else if (StringUtils.containsIgnoreCase(sql, FROM_FIVE_MIN_AGG_TIME_WITHOUT_SPACE)) {
            return getIndexOfSingleQuoteAfterTimeValue(sql, FROM_FIVE_MIN_AGG_TIME_WITHOUT_SPACE, searchStartIndex);
        }
        return -1;
    }

    private int getIndexOfSingleQuoteAfterTimeValue(final String sql, final String searchTerm, final int searchStartIndex) {
        final int dateTimeIndex = StringUtils.indexOfIgnoreCase(sql, searchTerm, searchStartIndex);

        if (dateTimeIndex != -1) {
            return StringUtils.indexOfIgnoreCase(sql, SINGLE_QUOTE, dateTimeIndex);
        }

        return -1;
    }

    /**
     * Locates the first position to replace the "to time", weather this is "datetime_id" or "event_time".
     *
     * @param sql
     *        The SQL query to examine for "to time" indices.
     * @return The location to start the replacement for the "to time".
     */
    private int findToTimeIndex(final String sql, final int searchStartIndex) {

        if (StringUtils.containsIgnoreCase(sql, TO_DATETIME_ID_WITH_SPACE)) {
            return getIndexOfSingleQuoteAfterTimeValue(sql, TO_DATETIME_ID_WITH_SPACE, searchStartIndex);
        } else if (StringUtils.containsIgnoreCase(sql, TO_DATETIME_ID_WITHOUT_SPACE)) {
            return getIndexOfSingleQuoteAfterTimeValue(sql, TO_DATETIME_ID_WITHOUT_SPACE, searchStartIndex);
        } else if (StringUtils.containsIgnoreCase(sql, TO_EVENT_TIME_WITH_SPACE)) {
            return getIndexOfSingleQuoteAfterTimeValue(sql, TO_EVENT_TIME_WITH_SPACE, searchStartIndex);
        } else if (StringUtils.containsIgnoreCase(sql, TO_EVENT_TIME_WITHOUT_SPACE)) {
            return getIndexOfSingleQuoteAfterTimeValue(sql, TO_EVENT_TIME_WITH_SPACE, searchStartIndex);
        } else if (StringUtils.containsIgnoreCase(sql, TO_FIVE_MIN_AGG_TIME_WITH_SPACE)) {
            return getIndexOfSingleQuoteAfterTimeValue(sql, TO_FIVE_MIN_AGG_TIME_WITH_SPACE, searchStartIndex);
        } else if (StringUtils.containsIgnoreCase(sql, TO_FIVE_MIN_AGG_TIME_WITHOUT_SPACE)) {
            return getIndexOfSingleQuoteAfterTimeValue(sql, TO_FIVE_MIN_AGG_TIME_WITHOUT_SPACE, searchStartIndex);
        }
        return -1;
    }

    public String getBusyHourString(final String hourValue) {
        DateTime hourToFind;

        final DateTime lastDay = BASETIME;
        final DateTime firstDay = lastDay.minusDays(1);

        final int hourOfInterest = Integer.parseInt(hourValue);
        final int currentHour = lastDay.getHourOfDay();

        if (hourOfInterest < currentHour) {
            hourToFind = lastDay.minusHours(currentHour - hourOfInterest);
        } else {
            hourToFind = firstDay.plusHours(hourOfInterest - currentHour);
        }

        hourToFind = fixSqlTimeWithOffset(hourToFind, 0);

        final Map<String, String> timeStrings = createSqlTimeValues(hourToFind.plusHours(1), hourToFind);

        return "datetime_id >= " + timeStrings.get(SQL_DATETIME_FROM_KEY) + " and datetime_id < " + timeStrings.get(SQL_DATETIME_TO_KEY);
    }

    public String getBusyDayString(final String dayValue) {
        final int searchDay = getNumericalDayOfWeek(dayValue);
        DateTime lastMidnight = goBackToMidnight(BASETIME);

        int daysToSubtract = 0;
        final int currentDay = lastMidnight.getDayOfWeek();

        lastMidnight = fixSqlTimeWithOffset(lastMidnight, 0);

        if (searchDay < currentDay) {
            daysToSubtract = currentDay - searchDay;
        } else if (searchDay >= currentDay) {
            daysToSubtract = 7 - (searchDay - currentDay);
        }

        lastMidnight = lastMidnight.minusDays(daysToSubtract);
        final Map<String, String> timeStrings = createSqlTimeValues(lastMidnight.plusDays(1), lastMidnight);

        return "datetime_id >= " + timeStrings.get(SQL_DATETIME_FROM_KEY) + " and datetime_id < " + timeStrings.get(SQL_DATETIME_TO_KEY);
    }

    public String getKpiTime(final String startTime, final int testCaseTime) {

        DateTime startDateTime, endDateTime;
        startDateTime = parseStringToDateTime(startTime);
        

        endDateTime = startDateTime.plusMinutes(getNoOfMinutesToAdd_KPI(testCaseTime));

        final Map<String, String> timeStrings = createSqlTimeValues(endDateTime, startDateTime);

        return "datetime_id >= " + timeStrings.get(SQL_DATETIME_FROM_KEY) + " and datetime_id < " + timeStrings.get(SQL_DATETIME_TO_KEY);
    }

    public String getFiveMinAggTime(final String startTime) {

        DateTime fiveMinAggTime = parseStringToDateTime(startTime);
      

        final String sqlFiveMinAggTime = SINGLE_QUOTE + fiveMinAggTime.getYear() + DASH + addZeroToTimeValue(fiveMinAggTime.getMonthOfYear()) + DASH
                + addZeroToTimeValue(fiveMinAggTime.getDayOfMonth()) + " " + addZeroToTimeValue(fiveMinAggTime.getHourOfDay()) + COLON
                + addZeroToTimeValue(fiveMinAggTime.getMinuteOfHour()) + SINGLE_QUOTE;

        return "FIVE_MIN_AGG_TIME = " + sqlFiveMinAggTime;
    }

    public String getFiveMinDateTime(final String startTime) {

        DateTime fiveMinDateTime = parseStringToDateTime(startTime);
       

        final String sqlFiveMinDateTime = SINGLE_QUOTE + fiveMinDateTime.getYear() + DASH + addZeroToTimeValue(fiveMinDateTime.getMonthOfYear())
                + DASH + addZeroToTimeValue(fiveMinDateTime.getDayOfMonth()) + " " + addZeroToTimeValue(fiveMinDateTime.getHourOfDay()) + COLON
                + addZeroToTimeValue(fiveMinDateTime.getMinuteOfHour()) + SINGLE_QUOTE;

        return "DATETIME_ID = " + sqlFiveMinDateTime;
    }

    /**
     * @param startTime
     * @return
     * @throws NumberFormatException
     */
    private DateTime parseStringToDateTime(final String startTime) throws NumberFormatException {
        DateTime startDateTime;
        if (startTime.matches("\\b\\d+\\b")) {
            
        	startDateTime = new DateTime(Long.parseLong(startTime)).toDateTime(DateTimeZone.UTC);
        } else {
            startDateTime = DateTime.parse(startTime, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.S"));
            startDateTime = fixSqlTimeWithOffset(startDateTime, 0);
        }
        return startDateTime;
    }

    public String getEventVolumeTime(final String eventVolumeTime, final int testCaseTime) {

        final int noOfMinutesToAdd = getNoOfMinutesToAdd_EventVolume(testCaseTime);
        DateTime startTime = DateTime.parse(eventVolumeTime, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.S"));
        startTime = fixSqlTimeWithOffset(startTime, 0);
        final DateTime endTime = startTime.plusMinutes(noOfMinutesToAdd);

        final Map<String, String> timeStrings = createSqlTimeValues(endTime, startTime);

        return "datetime_id >= " + timeStrings.get(SQL_DATETIME_FROM_KEY) + " and datetime_id < " + timeStrings.get(SQL_DATETIME_TO_KEY);

    }

    /**
     * @param testCaseTime
     * @return
     */
    private int getNoOfMinutesToAdd_EventVolume(final int testCaseTime) {
        int noOfMinutesToAdd;
        if (testCaseTime <= 120) {
            noOfMinutesToAdd = 1;
        } else if (testCaseTime < 10080) {
            noOfMinutesToAdd = 15;
        } else {
            noOfMinutesToAdd = 1440;
        }
        return noOfMinutesToAdd;
    }

    /**
     * @param testCaseTime
     * @return
     */
    private int getNoOfMinutesToAdd_KPI(final int testCaseTime) {
        int noOfMinutesToAdd;
        if (testCaseTime < 10080) {
            noOfMinutesToAdd = 15;
        } else {
            noOfMinutesToAdd = 1440;
        }
        return noOfMinutesToAdd;
    }

    public int getNumericalDayOfWeek(final String dayString) {
        int numericalDay = 0;

        if (dayString.equalsIgnoreCase("Monday")) {
            numericalDay = 1;
        } else if (dayString.equalsIgnoreCase("Tuesday")) {
            numericalDay = 2;
        } else if (dayString.equalsIgnoreCase("Wednesday")) {
            numericalDay = 3;
        } else if (dayString.equalsIgnoreCase("Thursday")) {
            numericalDay = 4;
        } else if (dayString.equalsIgnoreCase("Friday")) {
            numericalDay = 5;
        } else if (dayString.equalsIgnoreCase("Saturday")) {
            numericalDay = 6;
        } else if (dayString.equalsIgnoreCase("Sunday")) {
            numericalDay = 7;
        }

        return numericalDay;
    }

    public Map<Integer, String> getSqlDateTimes(final int[] times) {
        final Map<Integer, String> timesToReturn = new HashMap<Integer, String>();

        DateTime fromTime = null;
        DateTime toTime = null;

        for (final int timeValue : times) {
            toTime = BASETIME;
            if (timeValue >= 10080) {
                toTime = goBackToMidnight(toTime);
                fromTime = toTime.minusMinutes(timeValue);
            } else {
                fromTime = toTime.minusMinutes(timeValue);
            }
            toTime = fixSqlTimeWithOffset(toTime, timeValue);
            fromTime = fixSqlTimeWithOffset(fromTime, timeValue);
            final Map<String, String> sqlTimeStrings = createSqlTimeValues(toTime, fromTime);
            timesToReturn.put(timeValue,
                    "datetime_id >= " + sqlTimeStrings.get(SQL_DATETIME_FROM_KEY) + " and datetime_id < " + sqlTimeStrings.get(SQL_DATETIME_TO_KEY));
        }

        return timesToReturn;
    }

    public String updateSqlWithRelevantTestTimes(final String sql, final int time) {
        DateTime sqlDateTimeTo = new DateTime(BASETIME);

        if (time == MINUTES_IN_A_WEEK) {
            sqlDateTimeTo = goBackToMidnight(sqlDateTimeTo);
        }

        sqlDateTimeTo = fixSqlTimeWithOffset(sqlDateTimeTo, time);
        final DateTime sqlDateTimeFrom = sqlDateTimeTo.minusMinutes(time);

        final Map<String, String> sqlTimeStrings = createSqlTimeValues(sqlDateTimeTo, sqlDateTimeFrom);

        return replaceSqlTime(sql, sqlTimeStrings);
    }

}
