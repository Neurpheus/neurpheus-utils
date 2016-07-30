/*
 *  © 2015 Jakub Strychowski
 */

package org.neurpheus.core.string;

import java.util.Locale;
import net.trajano.commons.testing.UtilityClassTestUtil;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jakub Strychowski
 */
public class LocaleHelperTest {
    
    public LocaleHelperTest() {
    }

    @Test
    public void testUtilityClass() throws ReflectiveOperationException {
        UtilityClassTestUtil.assertUtilityClassWellDefined(LocaleHelper.class);
    }
    
    
    @Test
    public void testPolishLocale() {
        // GIVEN
        String str = "pl_PL";
                
        // WHEN
        Locale result = LocaleHelper.toLocale(str);
        
        // THEN
        Locale expected = new Locale("pl", "PL");
        assertEquals(expected, result);
    }
    
    @Test
    public void testPolishLocale2() {
        // GIVEN
        String str = "pl";
                
        // WHEN
        Locale result = LocaleHelper.toLocale(str);
        
        // THEN
        Locale expected = new Locale("pl");
        assertEquals(expected, result);
    }

    @Test
    public void testEnglishLocale() {
        // GIVEN
        String str = "en_GB";
                
        // WHEN
        Locale result = LocaleHelper.toLocale(str);
        
        // THEN
        Locale expected = new Locale("en", "GB");
        assertEquals(expected, result);
    }

    @Test
    public void testJapaneseImperialLocale() {
        // GIVEN
        String str = "jp_JP_JP";
                
        // WHEN
        Locale result = LocaleHelper.toLocale(str);
        
        // THEN
        Locale expected = new Locale("jp", "JP", "JP");
        assertEquals(expected, result);
    }


    @Test
    public void testNull() {
        // GIVEN
        String str = null;
                
        // WHEN
        Locale result = LocaleHelper.toLocale(str);
        
        // THEN
        assertNull(result);
    }
    
    @Test
    public void testInvalidFormat1() {
        // GIVEN
        String str = "en_GB12345678990";
                
        // WHEN
        try {
            Locale result = LocaleHelper.toLocale(str);
            fail("IllegalArgumentException should be thrown");
        } catch (Exception ex) {
            // THEN
            assertTrue(ex instanceof IllegalArgumentException);
        }
        
    }

    @Test
    public void testInvalidFormat2() {
        // GIVEN
        String str = "e";
                
        // WHEN
        try {
            Locale result = LocaleHelper.toLocale(str);
            fail("IllegalArgumentException should be thrown");
        } catch (Exception ex) {
            // THEN
            assertTrue(ex instanceof IllegalArgumentException);
        }
        
    }
    
    @Test
    public void testInvalidFormat3() {
        // GIVEN
        String str = "en_GB2";
                
        // WHEN
        try {
            Locale result = LocaleHelper.toLocale(str);
            fail("IllegalArgumentException should be thrown");
        } catch (Exception ex) {
            // THEN
            assertTrue(ex instanceof IllegalArgumentException);
        }
    }
    
    @Test
    public void testInvalidFormat4() {
        // GIVEN
        String str = "EN_GB";
                
        // WHEN
        try {
            Locale result = LocaleHelper.toLocale(str);
            fail("IllegalArgumentException should be thrown");
        } catch (Exception ex) {
            // THEN
            assertTrue(ex instanceof IllegalArgumentException);
        }
    }
    
    @Test
    public void testInvalidFormat5() {
        // GIVEN
        String str = "eN_GB";
                
        // WHEN
        try {
            Locale result = LocaleHelper.toLocale(str);
            fail("IllegalArgumentException should be thrown");
        } catch (Exception ex) {
            // THEN
            assertTrue(ex instanceof IllegalArgumentException);
        }
    }
    
    @Test
    public void testInvalidFormat5b() {
        // GIVEN
        String str = "En_GB";
                
        // WHEN
        try {
            Locale result = LocaleHelper.toLocale(str);
            fail("IllegalArgumentException should be thrown");
        } catch (Exception ex) {
            // THEN
            assertTrue(ex instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testInvalidFormat6() {
        // GIVEN
        String str = "en-GB";
                
        // WHEN
        try {
            Locale result = LocaleHelper.toLocale(str);
            fail("IllegalArgumentException should be thrown");
        } catch (Exception ex) {
            // THEN
            assertTrue(ex instanceof IllegalArgumentException);
        }
    }
    
    @Test
    public void testInvalidFormat7() {
        // GIVEN
        String str = "jp_JP-JP";
                
        // WHEN
        try {
            Locale result = LocaleHelper.toLocale(str);
            fail("IllegalArgumentException should be thrown");
        } catch (Exception ex) {
            // THEN
            assertTrue(ex instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testInvalidFormat8() {
        // GIVEN
        String str = "jp_jp";
                
        // WHEN
        try {
            Locale result = LocaleHelper.toLocale(str);
            fail("IllegalArgumentException should be thrown");
        } catch (Exception ex) {
            // THEN
            assertTrue(ex instanceof IllegalArgumentException);
        }
    }
    
    @Test
    public void testInvalidFormat9() {
        // GIVEN
        String str = "jp_Jp";
                
        // WHEN
        try {
            Locale result = LocaleHelper.toLocale(str);
            fail("IllegalArgumentException should be thrown");
        } catch (Exception ex) {
            // THEN
            assertTrue(ex instanceof IllegalArgumentException);
        }
    }
    
    @Test
    public void testInvalidFormat10() {
        // GIVEN
        String str = "jp__JP";
                
        // WHEN
        try {
            Locale result = LocaleHelper.toLocale(str);
            fail("IllegalArgumentException should be thrown");
        } catch (Exception ex) {
            // THEN
            assertTrue(ex instanceof IllegalArgumentException);
        }
    }
    
}
