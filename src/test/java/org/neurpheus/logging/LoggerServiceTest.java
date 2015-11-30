/*
 *  © 2015 Jakub Strychowski
 */

package org.neurpheus.logging;

import net.trajano.commons.testing.UtilityClassTestUtil;
import org.junit.Test;

import java.util.logging.Logger;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 * @author Kuba
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
