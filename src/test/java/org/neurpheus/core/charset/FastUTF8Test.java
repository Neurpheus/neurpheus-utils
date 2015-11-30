/*
 *  © 2015 Jakub Strychowski
 */
package org.neurpheus.core.charset;

import java.io.UnsupportedEncodingException;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;


/**
 *
 * @author Kuba
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
            String val = "absadbsajhbjhbasdBABBHABXBèØ—•Ø#%$≥π≥øÛ≥ÍÒÒ" + (char) 0x93 + (char) 0x94;
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
            String val = "absadbsajhbjhbasdBABBHABXBèØ—•Ø#%$≥π≥øÛ≥ÍÒÒ" + (char) 0x93 + (char) 0x94;
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
