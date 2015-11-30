/*
 *  © 2015 Jakub Strychowski
 */

package org.neurpheus.core.date;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import net.trajano.commons.testing.UtilityClassTestUtil;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runners.Parameterized;

/**
 *
 * @author Kuba
 */
public class ISO8601ParserTest {

    public ISO8601ParserTest() {
    }

    private final Object[][] dateValues = new Object[][]{
        {"2010-01-01", 2010, 1, 1},
        {"20100101", 2010, 1, 1},
        {"2010-01", 2010, 1, 1},
        {"2010", 2010, 1, 1},
        {"100", 100, 1, 1},
        {"10", 10, 1, 1},
        {"1", 1, 1, 1},
        {"1-2-3", 1, 2, 3},
        {"10-2-3", 10, 2, 3},
        {"9999", 9999, 1, 1},
        {"9999-2", 9999, 2, 1},
        {"9999-2-3", 9999, 2, 3},
        {"2012-02-29", 2012, 2, 29},
        {"2016-02-29", 2016, 2, 29},
        {"2013-02-29", 2013, 3, 1, -1},
        {"2014-02-29", 2014, 3, 1, -1},
        {"2015-02-29", 2015, 3, 1, -1},
        {"2016-02-29", 2016, 2, 29, -1},
        {"2010-02-32", 2010, 3, 4, -1},
        {"2010-13-03", 2011, 1, 3, -1}
    };

    private final Object[][] timeValues = new Object[][]{
        // time with colons
        {"12:34:56", 12, 34, 56, 0, -1},
        {"12:34:56Z", 12, 34, 56, 0, 0},
        {"12:34:56+00", 12, 34, 56, 0, 0},
        {"12:34:56-00", 12, 34, 56, 0, 0},
        {"12:34:56+00:00", 12, 34, 56, 0, 0},
        {"12:34:56-00:00", 12, 34, 56, 0, 0},
        {"12:34:56+0000", 12, 34, 56, 0, 0},
        {"12:34:56-0000", 12, 34, 56, 0, 0},
        // time withoutcolons
        {"123456", 12, 34, 56, 0, -1},
        {"123456Z", 12, 34, 56, 0, 0},
        {"123456+00", 12, 34, 56, 0, 0},
        {"123456-00", 12, 34, 56, 0, 0},
        {"123456+00:00", 12, 34, 56, 0, 0},
        {"123456-00:00", 12, 34, 56, 0, 0},
        {"123456+0000", 12, 34, 56, 0, 0},
        {"123456-0000", 12, 34, 56, 0, 0},
        // time without seconds
        {"12:34", 12, 34, 0, 0, -1},
        {"12:34Z", 12, 34, 0, 0, 0},
        {"12:34+00", 12, 34, 0, 0, 0},
        {"12:34-00", 12, 34, 0, 0, 0},
        {"12:34+00:00", 12, 34, 0, 0, 0},
        {"12:34-00:00", 12, 34, 0, 0, 0},
        {"12:34+0000", 12, 34, 0, 0, 0},
        {"12:34-0000", 12, 34, 0, 0, 0},
        {"T1234", 12, 34, 0, 0, -1},
        {"1234Z", 12, 34, 0, 0, 0},
        {"1234+00", 12, 34, 0, 0, 0},
        {"1234+00:00", 12, 34, 0, 0, 0},
        {"1234-00:00", 12, 34, 0, 0, 0},
        {"1234+0000", 12, 34, 0, 0, 0},
        {"1234-0000", 12, 34, 0, 0, 0},
        // time without minutes
        {"T12", 12, 0, 0, 0, -1},
        {"12Z", 12, 0, 0, 0, 0},
        {"12+00", 12, 0, 0, 0, 0},
        {"12+00:00", 12, 0, 0, 0, 0},
        {"12-00:00", 12, 0, 0, 0, 0},
        {"12+0000", 12, 0, 0, 0, 0},
        {"12-0000", 12, 0, 0, 0, 0},
        // timie with milliseconds
        {"12:34:56.2", 12, 34, 56, 200, -1},
        {"12:34:56.251Z", 12, 34, 56, 251, 0},
        {"12:34:56.25+00", 12, 34, 56, 250, 0},
        {"12:34:56.251-00", 12, 34, 56, 251, 0},
        {"12:34:56.2+00:00", 12, 34, 56, 200, 0},
        {"12:34:56.25-00:00", 12, 34, 56, 250, 0},
        {"12:34:56.251+0000", 12, 34, 56, 251, 0},
        {"12:34:56.2-0000", 12, 34, 56, 200, 0},
        {"123456.251", 12, 34, 56, 251, -1},
        {"123456.2Z", 12, 34, 56, 200, 0},
        {"123456.251Z", 12, 34, 56, 251, 0},
        {"123456.25+00", 12, 34, 56, 250, 0},
        {"123456.2-00", 12, 34, 56, 200, 0},
        {"123456.251+00:00", 12, 34, 56, 251, 0},
        {"123456.25-00:00", 12, 34, 56, 250, 0},
        {"123456.2+0000", 12, 34, 56, 200, 0},
        {"123456.251-0000", 12, 34, 56, 251, 0},
        {"123456.25Z", 12, 34, 56, 250, 0},
        {"123456.2+00", 12, 34, 56, 200, 0},
        {"123456.251-00", 12, 34, 56, 251, 0},
        {"123456.25+00:00", 12, 34, 56, 250, 0},
        {"123456.2-00:00", 12, 34, 56, 200, 0},
        {"123456.251+0000", 12, 34, 56, 251, 0},
        {"123456.25-0000", 12, 34, 56, 250, 0},
        // time with fraction of minutes
        {"12:34.25", 12, 34, 15, 0, -1},
        {"12:34.25Z", 12, 34, 15, 0, 0},
        {"12:34.25+00", 12, 34, 15, 0, 0},
        {"12:34.25-00", 12, 34, 15, 0, 0},
        {"12:34.25+00:00", 12, 34, 15, 0, 0},
        {"12:34.25-00:00", 12, 34, 15, 0, 0},
        {"12:34.25+0000", 12, 34, 15, 0, 0},
        {"12:34.25-0000", 12, 34, 15, 0, 0},
        {"1234.25", 12, 34, 15, 0, -1},
        {"1234.25Z", 12, 34, 15, 0, 0},
        {"1234.25+00", 12, 34, 15, 0, 0},
        {"1234.25-00", 12, 34, 15, 0, 0},
        {"1234.25+00:00", 12, 34, 15, 0, 0},
        {"1234.25-00:00", 12, 34, 15, 0, 0},
        {"1234.25+0000", 12, 34, 15, 0, 0},
        {"1234.25-0000", 12, 34, 15, 0, 0},
        // time with fraction of hour {
        {"12.75", 12, 45, 0, 0, -1},
        {"12.75Z", 12, 45, 0, 0, 0},
        {"12.75+00", 12, 45, 0, 0, 0},
        {"12.75-00", 12, 45, 0, 0, 0},
        {"12.75+00:00", 12, 45, 0, 0, 0},
        {"12.75-00:00", 12, 45, 0, 0, 0},
        {"12.75+0000", 12, 45, 0, 0, 0},
        {"12.75-0000", 12, 45, 0, 0, 0},
    };

    @Test
    public void testDate() {
        for (Object[] row : dateValues) {
            String dateString = (String) row[0];
            int year = (int) row[1];
            int month = (int) row[2];
            int day = (int) row[3];
            checkDate(dateString, year, month, day, -1);
            checkDate(dateString + 'T', year, month, day, -1);
        }
    }
    
    
    @Test
    public void testInvalidDate() {
        for (Object[] row : dateValues) {
            String dateString = (String) row[0];
            int year = (int) row[1];
            int month = (int) row[2];
            int day = (int) row[3];
            
            // char inside
            for (int j = 0; j < dateString.length(); j++) {
                try {
                    checkDate(dateString.substring(0, j) + 'x' + dateString.substring(j), year, month, day, -1);
                    Assert.fail("IllegalArgumentException should be thrown.");
                } catch (IllegalArgumentException ex) {  }
            }
            
        }
    }
    
    
    @Test
    public void testTime() {
        for (Object[] row : timeValues) {
            String timeString = (String) row[0];
            int hour = (int) row[1];
            int minute = (int) row[2];
            int second = (int) row[3];
            int millisecond = (int) row[4];
            int timezone = (int) row[5];
            checkTime(timeString, hour, minute, second, millisecond, timezone);
            if (!timeString.startsWith("T")) {
                checkTime("T" + timeString, hour, minute, second, millisecond, timezone);
            }
        }
    }
    
    @Test
    public void testInvalidTime() {
        for (Object[] row : timeValues) {
            String timeString = (String) row[0];
            int hour = (int) row[1];
            int minute = (int) row[2];
            int second = (int) row[3];
            int millisecond = (int) row[4];
            int timezone = (int) row[5];
            for (int j = 0; j < timeString.length(); j++) {
                String testString = timeString.substring(0, j) + 'x' + timeString.substring(j);
                try {
                    checkTime(testString, hour, minute, second, millisecond, timezone);
                    Assert.fail("IllegalArgumentException should be thrown");
                } catch (IllegalArgumentException ex) {}
            }
        }
    }
    
    
    @Test
    public void testDateAndTime() {
        for (Object[] dateRow : dateValues) {
            String dateString = (String) dateRow[0];
            int year = (int) dateRow[1];
            int month = (int) dateRow[2];
            int day = (int) dateRow[3];
            for (Object[] timeRow : timeValues) {
                String timeString = (String) timeRow[0];
                int hour = (int) timeRow[1];
                int minute = (int) timeRow[2];
                int second = (int) timeRow[3];
                int millisecond = (int) timeRow[4];
                int timezone = (int) timeRow[5];
                boolean hasT = timeString.startsWith("T");
                String dateTimeString = dateString + (hasT ? "" : "T") + timeString;
                checkDateAndTime(dateTimeString, year, month, day, 
                                hour, minute, second, millisecond,timezone);
                dateTimeString = dateString + (hasT ? "" : " ") + timeString;
                checkDateAndTime(dateTimeString, year, month, day, 
                                hour, minute, second, millisecond,timezone);
            }
        }
    }

    @Test
    public void testInvalidDateAndTime() {
        for (Object[] dateRow : dateValues) {
            String dateString = (String) dateRow[0];
            int year = (int) dateRow[1];
            int month = (int) dateRow[2];
            int day = (int) dateRow[3];
            for (Object[] timeRow : timeValues) {
                String timeString = (String) timeRow[0];
                int hour = (int) timeRow[1];
                int minute = (int) timeRow[2];
                int second = (int) timeRow[3];
                int millisecond = (int) timeRow[4];
                int timezone = (int) timeRow[5];
                boolean hasT = timeString.startsWith("T");
                String dateTimeString = dateString + (hasT ? "" : "T") + timeString;
                for (int j = 0; j < dateTimeString.length(); j++) {
                    String testString = dateTimeString.substring(0, j) + 'x' + dateTimeString.substring(j);
                    try {
                        checkDateAndTime(testString, year, month, day, 
                                hour, minute, second, millisecond,timezone);
                        Assert.fail("IllegalArgumentException should be thrown");
                    } catch (IllegalArgumentException ex) {}
                }
                dateTimeString = dateString + (hasT ? "" : " ") + timeString;
                for (int j = 0; j < dateTimeString.length(); j++) {
                    String testString = dateTimeString.substring(0, j) + 'x' + dateTimeString.substring(j);
                    try {
                        checkDateAndTime(testString, year, month, day, 
                                hour, minute, second, millisecond,timezone);
                        Assert.fail("IllegalArgumentException should be thrown");
                    } catch (IllegalArgumentException ex) {}
                }
            }
        }
    }
    
    
    @Test(expected = IllegalArgumentException.class)
    public void invalidDate1() {
        checkDate("2010f-02-03", 2010, 2, 3, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidDate2() {
        checkDate("20103-02-03", 20103, 2, 3, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidDate3() {
        checkDate("-2010-02-03", 2010, 2, 3, -1);
    }


    @Test(expected = IllegalArgumentException.class)
    public void invalidDate4() {
        checkDate("-2010-02-03-", 2010, 2, 3, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidDate5() {
        checkDate("2010--03", 2010, 2, 3, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidDate6() {
        checkDate("2010---03", 2010, 2, 3, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidDate7() {
        checkDate("2010-02-03x", 2010, 2, 3, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidDate8() {
        checkDate("2010-02-03-03", 2010, 2, 3, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidDate9() {
        checkDate("-2010-02-03-03", 2010, 2, 3, -1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void invalidTime1() {
        checkTime("2010020303", 20, 10, 02, 303, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidTime2() {
        checkTime("12:34:56++02:00", 12, 34, 56, 0, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidTime3() {
        checkTime("12:34:56+-02:00", 12, 34, 56, 0, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidTime4() {
        checkTime("12:34:56-+02:00", 12, 34, 56, 0, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidTime5() {
        checkTime("12:34:56Z02:00", 12, 34, 56, 0, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidTime6() {
        checkTime("12.5:34", 12, 34, 56, 0, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidTime7() {
        checkTime("12.5:34.4", 12, 34, 56, 0, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidTime8() {
        checkTime("12:34:56:678", 12, 34, 56, 0, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidTime9() {
        checkTime("12:34:56:678:45", 12, 34, 56, 0, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidTime10() {
        checkTime("12:34:56+02:03:04", 12, 34, 56, 0, 2);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void invalidTime11() {
        checkTime("12x:34:56:678", 12, 34, 56, 0, 2);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void invalidDateTime1() {
        checkDateAndTime("2015-02-03  12:34:56", 2015, 02, 03, 12, 34, 56, 0, -1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void invalidDateTime2() {
        checkDateAndTime("2015-02-03TT12:34:56", 2015, 02, 03, 12, 34, 56, 0, -1);
    }
    
    @Test
    public void testDefaultTimeZone() {
        info("Test default timezone");
        Calendar expResult = Calendar.getInstance();
        expResult.set(Calendar.YEAR, 2010);
        expResult.set(Calendar.MONTH, 0);
        expResult.set(Calendar.DAY_OF_MONTH, 1);
        expResult.set(Calendar.HOUR_OF_DAY, 12);
        expResult.set(Calendar.MINUTE, 30);
        expResult.set(Calendar.SECOND, 40);
        expResult.set(Calendar.MILLISECOND, 0);
        info(expResult.toString());

        Calendar result = ISO8601Parser.parseCalendar("2010-01-01T12:30:40.0");
        info(result.toString());
        //assertEquals(expResult, result);
        assertEquals(expResult.getTimeInMillis(), result.getTimeInMillis());
    }

    @Test
    public void testLosAngelesTimeZone() {
        info("Test Los Angeles timezone");
        Calendar expResult = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
        expResult.set(Calendar.YEAR, 2010);
        expResult.set(Calendar.MONTH, 0);
        expResult.set(Calendar.DAY_OF_MONTH, 1);
        expResult.set(Calendar.HOUR_OF_DAY, 12);
        expResult.set(Calendar.MINUTE, 30);
        expResult.set(Calendar.SECOND, 40);
        expResult.set(Calendar.MILLISECOND, 0);
        info(expResult.toString());

        Calendar result = ISO8601Parser.parseCalendar("2010-01-01T12:30:40.0-08:00");
        info(result.toString());
        //assertEquals(expResult, result);
        assertEquals(expResult.getTimeInMillis(), result.getTimeInMillis());
    }

    @Test
    public void testTimeInDifferentTimeZones() {
        compareTimes("2010-01-01T12:30:40Z", "2010-01-01T12:30:40+00:00");
        compareTimes("2010-01-01T12:30:40Z", "2010-01-01T13:30:40+01:00");
        compareTimes("2010-01-01T12:30:40Z", "2010-01-01T14:00:40+01:30");
        compareTimes("2010-01-01T12:30:40Z", "2010-01-01T11:00:40-01:30");
        compareTimes("2010-01-01T14:00:40+01:30", "2010-01-01T11:00:40-01:30");

        compareTimes("T14:00:40+01:30", "T11:00:40-01:30");

    }

    @Test
    public void simpleTest() {
        checkDate("0110-01-25", 110, 1, 25, -1);
        checkDate("0665-04-04", 665, 4, 4, -1);
        checkDate("2110-01-25", 2110, 1, 25, -1);
        checkDate("21100125", 2110, 1, 25, -1);
        checkDate("110-1-25", 110, 1, 25, -1);
        checkTime("12:34:56", 12, 34, 56, 0, -1);
        checkTime("12:34.5", 12, 34, 30, 0, -1);
        checkTime("12.25", 12, 15, 0, 0, -1);
        checkTime("123456", 12, 34, 56, 0, -1);
        checkTime("2:3:6", 2, 3, 6, 0, -1);

        checkDateAndTime("2012-11-05T22:47:10", 2012, 11, 5, 22, 47, 10, 0, -1);
        checkDateAndTime("0110-01-25T12:34:56", 110, 1, 25, 12, 34, 56, 0, -1);
        checkDateAndTime("0110-01-25T12:34:56.854", 110, 1, 25, 12, 34, 56, 854, -1);
        checkTime("050641.890", 5, 6, 41, 890, -1);
        checkDateAndTime("18610810T183628.281", 1861, 8, 10, 18, 36, 28, 281, -1);
        checkDateAndTime("0966-02-20T19:52:32", 966, 2, 20, 19, 52, 32, 0, -1);
        checkDateAndTime("0966-02-20T19:52:32+02:00", 966, 2, 20, 19, 52, 32, 0, 2);
        checkDateAndTime("0966-02-20T19:52:32-02:00", 966, 2, 20, 19, 52, 32, 0, -2);
        checkDateAndTime("0966-02-20T19:52:32Z", 966, 2, 20, 19, 52, 32, 0, 0);
    }

    private void compareTimes(String value1, String value2) {
        Calendar cal1 = ISO8601Parser.parseCalendar(value1);
        Calendar cal2 = ISO8601Parser.parseCalendar(value2);
        assertEquals(cal1.getTimeInMillis(), cal2.getTimeInMillis());
    }

    private void checkDate(String value, int year, int month, int day, double zoneOffset) {
        Calendar expResult = Calendar.getInstance();
        expResult.clear();
        if (zoneOffset != -1) {
            int zoneOffsetMilliseconds = (int) Math.round(zoneOffset * ISO8601Parser.MINUTES_IN_HOUR
                    * ISO8601Parser.SECONDS_IN_MINUTE * ISO8601Parser.MILISECONDS_IN_SECOND);
            expResult.set(Calendar.ZONE_OFFSET, zoneOffsetMilliseconds);
        }
        expResult.set(year, month - 1, day);
        info(value);
        info("exp:" + expResult.toString());
        //DateTime joda = DateTime.parse(value);
        //info("jod:" + joda.toCalendar(Locale.getDefault()));
        Calendar result = ISO8601Parser.parseCalendar(value);
        info("res:" + result.toString());
        //assertEquals(expResult, result);
        assertEquals("Parsing error for date: " + value, expResult.getTimeInMillis(), result.getTimeInMillis());
        //assertEquals(joda.toCalendar(Locale.getDefault()).getTimeInMillis(), result.getTimeInMillis());

        Date date = ISO8601Parser.parseDate(value);
        assertEquals(expResult.getTimeInMillis(), date.getTime());

    }

    private void checkTime(String value, int hour, int minute, int second, int millisecond,
                           double zoneOffset) {
        Calendar expResult = Calendar.getInstance();
        expResult.clear();
        if (zoneOffset != -1) {
            int zoneOffsetMilliseconds = (int) Math.round(zoneOffset * ISO8601Parser.MINUTES_IN_HOUR
                    * ISO8601Parser.SECONDS_IN_MINUTE * ISO8601Parser.MILISECONDS_IN_SECOND);
            expResult.set(Calendar.ZONE_OFFSET, zoneOffsetMilliseconds);
        }
        expResult.set(Calendar.HOUR_OF_DAY, hour);
        expResult.set(Calendar.MINUTE, minute);
        expResult.set(Calendar.SECOND, second);
        if (millisecond >= 0) {
            expResult.set(Calendar.MILLISECOND, millisecond);
        }
        info(value);
        info("exp:" + expResult.toString());

        //DateTime joda = DateTime.parse(value);
        Calendar result = ISO8601Parser.parseCalendar(value);
        info("res:" + result.toString());
        //assertEquals(expResult, result);
        assertEquals("Parsing error for time: " + value, expResult.getTimeInMillis(), result.getTimeInMillis());
        //assertEquals(joda.getMillis(), result.getTimeInMillis());

        Date date = ISO8601Parser.parseDate(value);
        assertEquals(expResult.getTimeInMillis(), date.getTime());

    }

    private void checkDateAndTime(String value, int year, int month, int day, int hour, int minute,
                                  int second, int millisecond, double zoneOffset) {
        Calendar expResult = Calendar.getInstance();
        expResult.clear();
        if (zoneOffset != -1) {
            int zoneOffsetMilliseconds = (int) Math.round(zoneOffset * ISO8601Parser.MINUTES_IN_HOUR
                    * ISO8601Parser.SECONDS_IN_MINUTE * ISO8601Parser.MILISECONDS_IN_SECOND);
            expResult.set(Calendar.ZONE_OFFSET, zoneOffsetMilliseconds);
        }
        expResult.set(year, month - 1, day, hour, minute, second);
        if (millisecond >= 0) {
            expResult.set(Calendar.MILLISECOND, millisecond);
        }
        info(value);
        info("exp:" + expResult.toString());
        
        //DateTime joda = DateTime.parse(value);
        Calendar result = ISO8601Parser.parseCalendar(value);
        info("res:" + result.toString());
        //assertEquals(expResult, result);
        assertEquals("Parsing error for date and time : " + value, expResult.getTimeInMillis(), result.getTimeInMillis());
        //assertEquals(joda.getMillis(), result.getTimeInMillis());

        Date date = ISO8601Parser.parseDate(value);
        assertEquals(expResult.getTimeInMillis(), date.getTime());
    }

    /**
     * Test of parseCalendar method, of class ISO8601Parser.
     */
    @Test
    public void testParseCalendar() {
        int numberOfTries = 1_000;
        for (int counter = 0; counter < numberOfTries; counter++) {
            int year = (int) Math.round(Math.random() * 2500);
            int month = (int) Math.round(Math.random() * 11);
            int day = 1 + (int) Math.round(Math.random() * 27);
            int hour = (int) Math.round(Math.random() * 24);
            int minute = (int) Math.round(Math.random() * 59);
            int second = (int) Math.round(Math.random() * 59);
            int milisecond = (int) Math.round(Math.random() * 999);

            boolean testDatePart = Math.random() > 0.5;
            boolean testTimePart = (Math.random() > 0.5) || !testDatePart;

            boolean delimeters = Math.random() > 0.5;
            boolean testTimezone = Math.random() > 0.5;
            boolean testMiliseconds = testTimePart && (Math.random() > 0.5);

            String dateStr = String.format(delimeters ? "%04d-%02d-%02d" : "%04d%02d%02d",
                                           year, month + 1, day);

            String timeStr = String.format(delimeters ? "%02d:%02d:%02d" : "%02d%02d%02d",
                                           hour, minute, second);

            String milisecondsStr = testMiliseconds ? String.format(".%03d", milisecond) : "";

            StringBuilder builder = new StringBuilder();
            if (testDatePart) {
                builder.append(dateStr);
            }
            if (testTimePart) {
                if (testDatePart || Math.random() > 0.5) {
                    builder.append(testDatePart || Math.random() > 0.5 ? 'T' : ' ');
                }
                builder.append(timeStr);
                if (testMiliseconds) {
                    builder.append(milisecondsStr);
                }
            }

            String str = builder.toString();

            if (testDatePart) {
                if (testTimePart) {
                    checkDateAndTime(str, year, month + 1, day, hour, minute, second,
                                     testMiliseconds ? milisecond : -1, -1);
                } else {
                    checkDate(str, year, month + 1, day, -1);
                }
            } else {
                checkTime(str, hour, minute, second, testMiliseconds ? milisecond : -1, -1);
            }

        }
    }

    @Test
    public void testUtilityClass() throws ReflectiveOperationException {
        UtilityClassTestUtil.assertUtilityClassWellDefined(ISO8601Parser.class);
    }
    
    
    private void info(String msg) {
        //System.out.println(msg);
    }

}
