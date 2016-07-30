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

package org.neurpheus.core.charset;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.UnsupportedEncodingException;

/**
 * Tests fast UTF8 encoding.
 * 
 * @author Jakub Strychowski
 */
public class FastUTF8Test  {
    

    @Test
    public void decodeNull() {
        Assert.assertNull(FastUTF8.decode(null));
    }
    
    
    @Test
    public void emptyString() {
        String result = FastUTF8.decode(new byte[0]);
        Assert.assertEquals(0, result.length());
    }
    
    @Test
    public void PolishCharacters() {
        try {
            String val = "absadbsajhbjhbasdBABBHABXBŹŻŃĄŻ#%$łąłżółęńń" + (char) 0x93 + (char) 0x94;
            byte[] bytes = val.getBytes("utf-8");
            String val2 = FastUTF8.decode(bytes);
            Assert.assertEquals(val, val2);
        } catch (UnsupportedEncodingException ex) {
            Assert.fail(ex.getMessage());
        }
    }
    
    @Category(org.neurpheus.test.PerformenceTest.class)
    @Test (timeout = 50L)
    public void performance() {
        try {
            String val = "absadbsajhbjhbasdBABBHABXBŹŻŃĄŻ#%$łąłżółęńń" + (char) 0x93 + (char) 0x94;
            byte[] bytes = val.getBytes("utf-8");
            String val2 = "";
            long startTime = System.nanoTime();
            int numberOfTimes = 20_000;
            for (int i = 0; i < numberOfTimes;i++) {
                val2 = FastUTF8.decode(bytes);
            }
            long duration = (System.nanoTime() - startTime) / 1000000L;
            System.out.println("FastUTF8 executed " + numberOfTimes + " in " + duration + " ms.");
            Assert.assertEquals(val, val2);
        } catch (UnsupportedEncodingException ex) {
            Assert.fail(ex.getMessage());
        }
    }
    
}
