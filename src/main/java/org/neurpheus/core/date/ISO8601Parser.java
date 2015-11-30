/*
 * Neurpheus - Utilities Package
 *
 * Copyright (C) 2006-2015 Jakub Strychowski
 *
 *  This library is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published by the Free
 *  Software Foundation; either version 3.0 of the License, or (at your option)
 *  any later version.
 *
 *  This library is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 *  for more details.
 */

package org.neurpheus.core.date;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Parses date/time strings written in ISO8601 format.
 * <p>
 * See <a href="http://en.wikipedia.org/wiki/ISO_8601">http://en.wikipedia.org/wiki/ISO_8601</a>
 * for more informations about the ISO 8601 standard. See
 * <a href="http://www.w3.org/TR/NOTE-datetime">http://www.w3.org/TR/NOTE-datetime</a>
 * for informations about a reduced subset of standards supported by this class.
 * </p>
 * <br/>
 * <p>
 * Following example shows how this class can be used for parsing :
 * <br/><code><pre>
 *   Date date = ISO8601Parser.parseDate("2006-11-18T03:15:43.6322+01:00");
 *   System.out.println(date);
 *
 *   Calendar calendar = ISO8601Parser.parseCalendar("2006-11-18T03:15:43.6322+01:00");
 *   System.out.println(calendar);
 * </pre></code>
 * </p>
 *
 * @author Jakub Strychowski
 */
public final class ISO8601Parser {

    /** Number of miliseconds in one second. */
    public static final int MILISECONDS_IN_SECOND = 1000;

    /** Number of seconds in one minute. */
    public static final int SECONDS_IN_MINUTE = 60;

    /** Number of minutes in one hour. */
    public static final int MINUTES_IN_HOUR = 60;
    
    private int numberOfDigits;
    private String inputString;
    private boolean zuluTimezone = false;
    private int length;
    private int lastPos;
    private int timezonePos = -1;
    private int dateTimeSeparatorPos = -1;
    private int fractionPos = -1;
    private int numberOfColons = 0;
    private int numberOfDashes = 0;
    private int lastDashPos = -1;
    private boolean isInTimePart = false;
    private Calendar res;
    
    
    private ISO8601Parser() {

    }

    /** Private constructor prevents against instance creation. */
    private void init(final String dateTimeStr, TimeZone timeZone, Locale locale) {
        inputString = dateTimeStr.trim();
        length = inputString.length();
        lastPos = length - 1;
        res = Calendar.getInstance(timeZone, locale);
        res.clear();
    }


    /**
     * Parses the given date/time string and returns its
     * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/Date.html">java.util.Date</a>
     * representation.
     *
     * @param s The string representation of the date/time.
     *
     * @return The
     * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/Date.html">java.util.Date</a>
     * representation of the parsed string.
     */
    public static Date parseDate(final String s) {
        return parseCalendar(s).getTime();
    }

    public static Calendar parseCalendar(final String dateTimeStr) {
        return parseCalendar(dateTimeStr,
                             TimeZone.getDefault(),
                             Locale.getDefault(Locale.Category.FORMAT));
    }

    
    private final boolean hasSixDigitsAndNoDashes() {
        return numberOfDigits == 6 && numberOfDashes == 0;
    }
    
    private final boolean hasNagtiveTimeZonePart() {
        return lastDashPos == 6 && numberOfDashes == 1;
    }
    
    private final boolean hasNegativeTimezoneWithPossibleDatePart() {
        return lastDashPos > 0 && lastDashPos + 2 < lastPos;
    }

    private void parse() {
        processCharacters();
        if (numberOfDashes > 1) {
            isInTimePart = false;
        }
        if (dateTimeSeparatorPos < 0) {
            if (hasSixDigitsAndNoDashes() 
                    || hasNagtiveTimeZonePart()
                    || hasNegativeTimezoneWithPossibleDatePart()) {
                isInTimePart = true;
            }
            if (isInTimePart) {
                processTimeAndTimezone(0);
            } else {
                processDate(0, lastPos);
            }
        } else {
            if (lastPos > dateTimeSeparatorPos) {
                processTimeAndTimezone(dateTimeSeparatorPos + 1);
            }
            if (dateTimeSeparatorPos > 0) {
                processDate(0, dateTimeSeparatorPos - 1);
            }
        }
    }

    private void processCharacters() {
        final String tmp = inputString;
        for (int i = 0; i < length; i++) {
            char ch = tmp.charAt(i);
            switch (ch) {
                case 'Z':
                    processZulu(i);
                    break;
                case '+':
                    processPlus(i);
                    break;
                case '-':
                    processDash(i);
                    break;
                case ':':
                    processColon(i);
                    break;
                case 'T':
                case ' ':
                    processT(i);
                    break;
                case '.':
                case ',':
                    processDotOrComma(i);
                    break;
                default:
                    processDigit(ch, i);
            }
        }
    }
    
    private void processZulu(int pos) {
        zuluTimezone = true;
        isInTimePart = true;
        if (pos != lastPos) {
            reportError("Unexpected character after Z (zulu) sign", inputString.charAt(pos + 1), pos);
        }
    }
    
    private void processPlus(int pos) {
        isInTimePart = true;
        if (timezonePos > 0) {
            reportError("Duplicated timezone separator", inputString.charAt(pos), pos);
        }
        timezonePos = pos;
    }

    private void processT(int pos) {
        if (dateTimeSeparatorPos > 0) {
            reportError("Duplicated date and time separator", inputString.charAt(pos), pos);
        }
        isInTimePart = true;
        dateTimeSeparatorPos = pos;
    }
    
    private void processDotOrComma(int pos) {
        isInTimePart = true;
        if (fractionPos > 0) {
            reportError("Only one value can have fractional part", inputString.charAt(pos), pos);
        }
        fractionPos = pos;
    }
    
    private void processDash(int pos) {
        if (pos == 0) {
            reportError("Date cannot start from dash", '-', pos);
        }
        if (lastDashPos == pos -1) {
            reportError("Two dashes one after another", '-', pos);
        }
        lastDashPos = pos;
        if (isInTimePart) {
            if (timezonePos > 0) {
                reportError("Duplicated timezone separator", '-', pos);
            }
            timezonePos = pos;
        } else {
            numberOfDashes++;
            if (numberOfDashes > 2) {
                isInTimePart = true;
            }
        }
    }

    private void processColon(int pos) {
        isInTimePart = true;
        numberOfColons++;
        if ((numberOfColons > 3) || (numberOfColons == 3 && timezonePos < 0)) {
            reportError("Too many colons", ':', pos);
        }

    }

    private void processDigit(char ch, int pos) {
        if (!Character.isDigit(ch)) {
            reportError("Invalid character", ch, pos);
        }
        numberOfDigits++;
        if (numberOfDigits > 8) {
            isInTimePart = true;
        }
    }

    private void reportError(String reason, char ch, int pos) {
        throw new IllegalArgumentException(String.format(
                "Cannot parse date/time. %s. input string = '%s', position = %d, char = '%c'",
                reason, inputString, pos, ch));
    }

    public static Calendar parseCalendar(final String dateTimeStr, TimeZone timeZone, Locale locale) {
        ISO8601Parser parser = new ISO8601Parser();
        parser.init(dateTimeStr, timeZone, locale);
        parser.parse();
        return parser.res;
    }

    private enum ParsingPhase {

        YEAR(Calendar.YEAR, 0), 
        MONTH(Calendar.MONTH, 12.0), 
        DAY(Calendar.DAY_OF_MONTH, 30.0),
        HOUR(Calendar.HOUR_OF_DAY, 24.0), 
        MINUTE(Calendar.MINUTE, MINUTES_IN_HOUR), 
        SECOND(Calendar.SECOND, SECONDS_IN_MINUTE), 
        MILISECOND(Calendar.MILLISECOND, MILISECONDS_IN_SECOND);
        
        
        private final int calendarField;
        private final double fractionMultipler;
        
        
        private ParsingPhase(int calField, double multipler) {
            this.calendarField = calField;
            this.fractionMultipler = multipler;
        }
        
        public int getCalendarField() {
            return this.calendarField;
        }
        
        public double getFractionMultipler() {
            return this.fractionMultipler;
        }
        
        
        public ParsingPhase next() {
            return this.equals(MILISECOND) ? null : ParsingPhase.values()[this.ordinal() + 1];
        }
        
        
    }
    
    private void processDate(int startPos, int endPos) {
        ParsingPhase phase = ParsingPhase.YEAR;
        int numberOfCharactesInPhase = 0;
        int maxNumberOfCharactesInPhase = 4;
        int value = 0;
        for (int i = startPos; i <= endPos; i++) {
            char ch = inputString.charAt(i);
            boolean isDash = ch == '-';
            if (!isDash) {
                value = value * 10 + (ch - '0');
                numberOfCharactesInPhase++;
            }
            boolean lastChar = i == endPos;
            boolean endOfPhase = (isDash && numberOfCharactesInPhase > 0) 
                    || numberOfCharactesInPhase == maxNumberOfCharactesInPhase;
            if (lastChar || endOfPhase) {
                res.set(phase.getCalendarField(), 
                        phase.equals(ParsingPhase.MONTH) ? value - 1 : value);
                phase = phase.next();
                if (phase.equals(ParsingPhase.HOUR) && !lastChar) {
                    reportError("Unexpected character", inputString.charAt(i + 1), i + 1);
                }
                numberOfCharactesInPhase = 0;
                value = 0;
                maxNumberOfCharactesInPhase = 2;
            }
        }
    }


    private void processTimeAndTimezone(int startPos) {
        int endPos = lastPos;
        if (zuluTimezone) {
            res.set(Calendar.ZONE_OFFSET, 0);
            endPos--;
        } else {
            if ((timezonePos < 0) && (lastDashPos > startPos)) {
                timezonePos = lastDashPos;
            } 

            if (timezonePos >= 0) {
                processTimezone(timezonePos);
                endPos = timezonePos - 1;
            }
        }
        processTime(startPos, endPos);
    }

    private void processTimezone(int startPos) {
        char ch = inputString.charAt(startPos);
        int sign = ch == '+' ? 1 : -1;
        ParsingPhase phase = ParsingPhase.HOUR;
        int numberOfCharactesInPhase = 0;
        int maxNumberOfCharactesInPhase = 2;
        int value = 0;
        int offset = 0;
        for (int i = startPos + 1; i <= lastPos; i++) {
            ch = inputString.charAt(i);
            boolean isColon = ch == ':';
            if (!isColon) {
                value = value * 10 + (ch - '0');
                numberOfCharactesInPhase++;
            }
            if ((isColon && numberOfCharactesInPhase > 0)
                    || (i == lastPos)
                    || (numberOfCharactesInPhase == maxNumberOfCharactesInPhase)) {
                offset += (phase == ParsingPhase.HOUR) ? value * MINUTES_IN_HOUR : value;
                phase = phase.next();
                numberOfCharactesInPhase = 0;
                value = 0;
            }
        }
        res.set(Calendar.ZONE_OFFSET, sign * offset * SECONDS_IN_MINUTE * MILISECONDS_IN_SECOND);
    }

    private void processTime(int startPos, int endPos) {
        int lastIndex = fractionPos > 0 ? fractionPos - 1 : endPos;
        ParsingPhase phase = ParsingPhase.HOUR;
        int numberOfCharactesInPhase = 0;
        int maxNumberOfCharactesInPhase = 2;
        int value = 0;
        for (int i = startPos; i <= lastIndex; i++) {
            char ch = inputString.charAt(i);
            boolean isColon = ch == ':';
            if (!isColon) {
                value = value * 10 + (ch - '0');
                numberOfCharactesInPhase++;
            }
            if ((isColon && numberOfCharactesInPhase > 0)
                    || (i == lastIndex)
                    || (numberOfCharactesInPhase == maxNumberOfCharactesInPhase)) {
                res.set(phase.getCalendarField(), value);
                phase = phase.next();
                if (phase == null) {
                    reportError("Input value to long for time", ch, i);
                }
                numberOfCharactesInPhase = 0;
                value = 0;
            }
        }
        if (fractionPos > startPos) {
            processTimeFraction(fractionPos + 1, endPos, phase);
        }

    }

    private void processTimeFraction(int startPos, int endPos, ParsingPhase phase) {
        int result = 0;
        int divider = 1;
        for (int i = startPos; i <= endPos; i++) {
            char ch = inputString.charAt(i);
            if (!Character.isDigit(ch)) {
                reportError("Invalid character at fraction part", ch, i);
            }
            result = result * 10 + (ch - '0');
            divider *= 10;
        }
        int value = (int) Math.round(phase.getFractionMultipler() * result / divider);
        res.set(phase.getCalendarField(), value);
    }


}
