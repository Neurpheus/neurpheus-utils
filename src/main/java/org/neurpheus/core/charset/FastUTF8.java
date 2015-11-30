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

package org.neurpheus.core.charset;

import java.util.logging.Logger;
import org.neurpheus.logging.LoggerService;

/**
 * Quickly creates a string from an array of bytes using the UTF-8 charset.
 *
 * @author Jakub Strychowski
 */
public class FastUTF8 {

    private final static Logger logger = LoggerService.getLogger(FastUTF8.class);

    private final static String EMPTY_STRING = "";

    public static String decode(byte[] bytes) {
        if (bytes == null) {
            return null;
        } else if (bytes.length == 0) {
            return EMPTY_STRING;
        } else {
            char[] result = new char[bytes.length];
            final int blen = bytes.length;
            int bpos = 0;
            int cpos = 0;
            char c;
            while (bpos < blen) {
                c = (char) (0xFF & bytes[bpos++]);
                if ((c & 0x80) != 0) {
                    if ((c & 0xC0) == 0xC0) {
                        c = (char) (((c & 0x1F) << 6) | (bytes[bpos++] & 0x3F));
                    } else if ((c & 0xE0) == 0xE0) {
                        c = (char) (((c & 0x0F) << 12) | ((bytes[bpos++] & 0x3F) << 6) | (bytes[bpos++] & 0x3F));
                    } else if ((c & 0xF8) == 0xF0) {
                        bpos += 3;
                        logger.warning("Unsupported characted found: " + Integer.
                                toHexString((int) c));
                    }
                }
                result[cpos++] = c;
            }
            return new String(result, 0, cpos);
        }
    }

}
