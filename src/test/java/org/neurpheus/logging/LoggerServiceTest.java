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

package org.neurpheus.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import net.trajano.commons.testing.UtilityClassTestUtil;
import org.junit.Test;

import java.util.logging.Logger;

/**
 * Tests logging service features.
 * 
 * @author Jakub Strychowski
 */
public class LoggerServiceTest {
    
    public LoggerServiceTest() {
    }

    /**
     * Test of getLogger method, of class LoggerService.
     */
    @Test
    public void testGetLogger() {
        Logger result = LoggerService.getLogger(LoggerServiceTest.class);
        assertNotNull(result);
        assertEquals("org.neurpheus.logging.LoggerServiceTest", result.getName());
    }
    
    @Test 
    public void testNull() {
        Logger result = LoggerService.getLogger(null);
        assertNotNull(result);
        assertEquals(Logger.GLOBAL_LOGGER_NAME, result.getName());
    }

    @Test
    public void testUtilityClass() throws ReflectiveOperationException {
        UtilityClassTestUtil.assertUtilityClassWellDefined(LoggerService.class);
    }
    
}
