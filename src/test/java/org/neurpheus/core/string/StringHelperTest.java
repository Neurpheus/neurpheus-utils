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

import net.trajano.commons.testing.UtilityClassTestUtil;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;


/**
 * Test StringHelper class.
 * 
 * @author Jakub Strychowski
 */
public class StringHelperTest {
    
    public StringHelperTest() {
    }

    @Test
    public void testUtilityClass() throws ReflectiveOperationException {
        UtilityClassTestUtil.assertUtilityClassWellDefined(StringHelper.class);
    }
    
    @Test
    public void testReverseString() {
        // GIVEN
        String txt = "abcdef";
        
        // WHEN
        String result = StringHelper.reverseString(txt);
        
        // THEN
        assertEquals("fedcba", result);
    }
    
    @Test
    public void testNullReverseString() {
        // GIVEN
        String txt = null;
        
        // WHEN
        try {
            String result = StringHelper.reverseString(txt);
            fail("NPE should be thrown");
        } catch (NullPointerException ex) {
            // THEN
            assertTrue(ex.getMessage().length() > 0);
          
        }
        
    }
    
    
}
