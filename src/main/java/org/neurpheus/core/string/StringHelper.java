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

package org.neurpheus.core.string;

/**
 * Utilities for the String class.
 *
 * @author Jakub Strychowski
 */
public final class StringHelper {

    /** Creates a new instance of StringHelper */
    private StringHelper() {
    }

    /**
     * Returns a new string created from the input string through the character order reversing.
     *
     * @param txt The input string.
     *
     * @return The string containing characters in reverse order.
     */
    public static String reverseString(final String txt) {
        StringBuffer tmp = new StringBuffer(txt.length());
        for (int i = txt.length() - 1; i >= 0; i--) {
            tmp.append(txt.charAt(i));
        }
        return tmp.toString();
    }

}
