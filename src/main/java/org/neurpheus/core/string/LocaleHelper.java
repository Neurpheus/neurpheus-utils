/*
 * Neurpheus - Utilities Package
 *
 * Copyright (C) 2006-2016 Jakub Strychowski
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

package org.neurpheus.core.string;

import java.util.Locale;

/**
 * Converts string representation of internationalization settings to {@see java.util.Locale}.
 *
 * @author Apache Software Foundation, Jakub Strychowski
 */
public final class LocaleHelper {
    
    private static final String INVALID_LOCALE_FORMAT = "Invalid locale format: ";
    
    /**
     * Instances of this class cannot be created.
     */
    private LocaleHelper() {     
    }

    /**
     * <p>
     * Converts a String to a Locale.</p>
     *
     * <p>
     * This method takes the string format of a locale and creates the locale object from it.</p>
     *
     * <pre>
     *   LocaleHelper.toLocale("en")         = new Locale("en", "")
     *   LocaleHelper.toLocale("en_GB")      = new Locale("en", "GB")
     *   LocaleHelper.toLocale("en_GB_xxx")  = new Locale("en", "GB", "xxx")   (#)
     * </pre>
     *
     * <p>
     * (#) The behavior of the JDK variant constructor changed between JDK1.3 and JDK1.4. In
     * JDK1.3, the constructor upper cases the variant, in JDK1.4, it doesn't. Thus, the result from
     * getVariant() may vary depending on your JDK.</p>
     *
     * <p>
     * This method validates the input strictly. The language code must be lowercase. The country
     * code must be uppercase. The separator must be an underscore. The length must be correct.
     * </p>
     *
     * @param str the locale String to convert, null returns null
     *
     * @return a Locale, null if null input
     *
     * @throws IllegalArgumentException if the string is an invalid format
     */
    public static Locale toLocale(String str) {
        if (str == null) {
            return null;
        }
        int len = str.length();
        if (len != 2 && len != 5 && len < 7) {
            throw new IllegalArgumentException(INVALID_LOCALE_FORMAT + str);
        }
        checkFirstTwoChars(str);
        if (len == 2) {
            return new Locale(str, "");
        } else {
            return toLocaleMoreThen2(str, len);
        }
    }

    private static void checkFirstTwoChars(String str) {
        char ch0 = str.charAt(0);
        char ch1 = str.charAt(1);
        if (ch0 < 'a' || ch0 > 'z' || ch1 < 'a' || ch1 > 'z') {
            throw new IllegalArgumentException(INVALID_LOCALE_FORMAT + str);
        }
    }
    
    private static Locale toLocaleMoreThen2(String str, int len) {
        if (str.charAt(2) != '_') {
            throw new IllegalArgumentException(INVALID_LOCALE_FORMAT + str);
        }
        char ch3 = str.charAt(3);
        char ch4 = str.charAt(4);
        if (ch3 < 'A' || ch3 > 'Z' || ch4 < 'A' || ch4 > 'Z') {
            throw new IllegalArgumentException(INVALID_LOCALE_FORMAT + str);
        }
        return toLocaleMoreThen4(str, len);
    }

    private static Locale toLocaleMoreThen4(String str, int len) {
        if (len == 5) {
            return new Locale(str.substring(0, 2), str.substring(3, 5));
        } else {
            if (str.charAt(5) != '_') {
                throw new IllegalArgumentException(INVALID_LOCALE_FORMAT + str);
            }
            return new Locale(str.substring(0, 2), str.substring(3, 5), str.substring(6));
        }
    }
    
}
