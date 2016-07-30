/*
 *  © 2015 Jakub Strychowski
 */

package org.neurpheus.collections.hashing;

import com.carrotsearch.sizeof.RamUsageEstimator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.DataOutputStream;

/**
 * Test Bloom filter.
 *
 * @author Jakub Strychowski
 */
public class BloomFilterTest {

    @Test
    public void testContains_CharSequence() {
        BloomFilter bloom = new BloomFilter();
        String testValue1 = "abc1";
        String testValue2 = "abc2";
        String testValue3 = "abc3";
        String testValue4 = "abc4";
        assertFalse(bloom.contains(testValue1));
        assertFalse(bloom.contains(testValue2));
        assertFalse(bloom.contains(testValue3));
        assertFalse(bloom.contains(testValue4));
        bloom.add(testValue1);
        assertTrue(bloom.contains(testValue1));
        assertFalse(bloom.contains(testValue2));
        assertFalse(bloom.contains(testValue3));
        assertFalse(bloom.contains(testValue4));
        bloom.add(testValue2);
        assertTrue(bloom.contains(testValue1));
        assertTrue(bloom.contains(testValue2));
        assertFalse(bloom.contains(testValue3));
        assertFalse(bloom.contains(testValue4));
        bloom.add(testValue3);
        assertTrue(bloom.contains(testValue1));
        assertTrue(bloom.contains(testValue2));
        assertTrue(bloom.contains(testValue3));
        assertFalse(bloom.contains(testValue4));
    }

    @Test
    public void testContains_byteArr() {
        BloomFilter bloom = new BloomFilter();
        byte[] testValue1 = {16, 32, 63, Byte.MAX_VALUE, Byte.MIN_VALUE, 0};
        byte[] testValue2 = {16, 32, 64, Byte.MAX_VALUE, Byte.MIN_VALUE, 0};
        byte[] testValue3 = {16, 32, 65, Byte.MAX_VALUE, Byte.MIN_VALUE, 0};
        byte[] testValue4 = {16, 32, 66, Byte.MAX_VALUE, Byte.MIN_VALUE, 0};
        assertFalse(bloom.contains(testValue1));
        assertFalse(bloom.contains(testValue2));
        assertFalse(bloom.contains(testValue3));
        assertFalse(bloom.contains(testValue4));
        bloom.add(testValue1);
        assertTrue(bloom.contains(testValue1));
        assertFalse(bloom.contains(testValue2));
        assertFalse(bloom.contains(testValue3));
        assertFalse(bloom.contains(testValue4));
        bloom.add(testValue2);
        assertTrue(bloom.contains(testValue1));
        assertTrue(bloom.contains(testValue2));
        assertFalse(bloom.contains(testValue3));
        assertFalse(bloom.contains(testValue4));
        bloom.add(testValue3);
        assertTrue(bloom.contains(testValue1));
        assertTrue(bloom.contains(testValue2));
        assertTrue(bloom.contains(testValue3));
        assertFalse(bloom.contains(testValue4));
    }

    @Test
    public void testContains_shortArr() {
        BloomFilter bloom = new BloomFilter();
        short[] testValue1 = {16, 32, 63, Short.MAX_VALUE, Short.MIN_VALUE, 0};
        short[] testValue2 = {16, 32, 64, Short.MAX_VALUE, Short.MIN_VALUE, 0};
        short[] testValue3 = {16, 32, 65, Short.MAX_VALUE, Short.MIN_VALUE, 0};
        short[] testValue4 = {16, 32, 66, Short.MAX_VALUE, Short.MIN_VALUE, 0};
        assertFalse(bloom.contains(testValue1));
        assertFalse(bloom.contains(testValue2));
        assertFalse(bloom.contains(testValue3));
        assertFalse(bloom.contains(testValue4));
        bloom.add(testValue1);
        assertTrue(bloom.contains(testValue1));
        assertFalse(bloom.contains(testValue2));
        assertFalse(bloom.contains(testValue3));
        assertFalse(bloom.contains(testValue4));
        bloom.add(testValue2);
        assertTrue(bloom.contains(testValue1));
        assertTrue(bloom.contains(testValue2));
        assertFalse(bloom.contains(testValue3));
        assertFalse(bloom.contains(testValue4));
        bloom.add(testValue3);
        assertTrue(bloom.contains(testValue1));
        assertTrue(bloom.contains(testValue2));
        assertTrue(bloom.contains(testValue3));
        assertFalse(bloom.contains(testValue4));
    }

    @Test
    public void testContains_charArr() {
        BloomFilter bloom = new BloomFilter();
        char[] testValue1 = {'a', 'b', 'c', Character.MAX_VALUE, Character.MIN_VALUE, 0};
        char[] testValue2 = {'a', 'b', 'd', Character.MAX_VALUE, Character.MIN_VALUE, 0};
        char[] testValue3 = {'a', 'b', 'e', Character.MAX_VALUE, Character.MIN_VALUE, 0};
        char[] testValue4 = {'a', 'b', 'f', Character.MAX_VALUE, Character.MIN_VALUE, 0};
        assertFalse(bloom.contains(testValue1));
        assertFalse(bloom.contains(testValue2));
        assertFalse(bloom.contains(testValue3));
        assertFalse(bloom.contains(testValue4));
        bloom.add(testValue1);
        assertTrue(bloom.contains(testValue1));
        assertFalse(bloom.contains(testValue2));
        assertFalse(bloom.contains(testValue3));
        assertFalse(bloom.contains(testValue4));
        bloom.add(testValue2);
        assertTrue(bloom.contains(testValue1));
        assertTrue(bloom.contains(testValue2));
        assertFalse(bloom.contains(testValue3));
        assertFalse(bloom.contains(testValue4));
        bloom.add(testValue3);
        assertTrue(bloom.contains(testValue1));
        assertTrue(bloom.contains(testValue2));
        assertTrue(bloom.contains(testValue3));
        assertFalse(bloom.contains(testValue4));
    }

    @Test
    public void testContains_intArr() {
        BloomFilter bloom = new BloomFilter();
        int[] testValue1 = {16, 32, 63, Integer.MAX_VALUE, Integer.MIN_VALUE, 0};
        int[] testValue2 = {16, 32, 64, Integer.MAX_VALUE, Integer.MIN_VALUE, 0};
        int[] testValue3 = {16, 32, 65, Integer.MAX_VALUE, Integer.MIN_VALUE, 0};
        int[] testValue4 = {16, 32, 66, Integer.MAX_VALUE, Integer.MIN_VALUE, 0};
        assertFalse(bloom.contains(testValue1));
        assertFalse(bloom.contains(testValue2));
        assertFalse(bloom.contains(testValue3));
        assertFalse(bloom.contains(testValue4));
        bloom.add(testValue1);
        assertTrue(bloom.contains(testValue1));
        assertFalse(bloom.contains(testValue2));
        assertFalse(bloom.contains(testValue3));
        assertFalse(bloom.contains(testValue4));
        bloom.add(testValue2);
        assertTrue(bloom.contains(testValue1));
        assertTrue(bloom.contains(testValue2));
        assertFalse(bloom.contains(testValue3));
        assertFalse(bloom.contains(testValue4));
        bloom.add(testValue3);
        assertTrue(bloom.contains(testValue1));
        assertTrue(bloom.contains(testValue2));
        assertTrue(bloom.contains(testValue3));
        assertFalse(bloom.contains(testValue4));
    }

    @Test
    public void testContains_longArr() {
        BloomFilter bloom = new BloomFilter();
        long[] testValue1 = {16L, 32L, 63L, Long.MAX_VALUE, Long.MIN_VALUE, 0L};
        long[] testValue2 = {16L, 32L, 64L, Long.MAX_VALUE, Long.MIN_VALUE, 0L};
        long[] testValue3 = {16L, 32L, 65L, Long.MAX_VALUE, Long.MIN_VALUE, 0L};
        long[] testValue4 = {16L, 32L, 66L, Long.MAX_VALUE, Long.MIN_VALUE, 0L};
        assertFalse(bloom.contains(testValue1));
        assertFalse(bloom.contains(testValue2));
        assertFalse(bloom.contains(testValue3));
        assertFalse(bloom.contains(testValue4));
        bloom.add(testValue1);
        assertTrue(bloom.contains(testValue1));
        assertFalse(bloom.contains(testValue2));
        assertFalse(bloom.contains(testValue3));
        assertFalse(bloom.contains(testValue4));
        bloom.add(testValue2);
        assertTrue(bloom.contains(testValue1));
        assertTrue(bloom.contains(testValue2));
        assertFalse(bloom.contains(testValue3));
        assertFalse(bloom.contains(testValue4));
        bloom.add(testValue3);
        assertTrue(bloom.contains(testValue1));
        assertTrue(bloom.contains(testValue2));
        assertTrue(bloom.contains(testValue3));
        assertFalse(bloom.contains(testValue4));
    }

    @Test
    public void testContains_floatArr() {
        BloomFilter bloom = new BloomFilter();
        float[] testValue1 = {16.1f, 32.2f, 63.3f, Float.MAX_VALUE, Float.MIN_VALUE, 0.0f};
        float[] testValue2 = {16.1f, 32.2f, 64.4f, Float.MAX_VALUE, Float.MIN_VALUE, 0.0f};
        float[] testValue3 = {16.1f, 32.2f, 65.5f, Float.MAX_VALUE, Float.MIN_VALUE, 0.0f};
        float[] testValue4 = {16.1f, 32.2f, 166.6f, Float.MAX_VALUE, Float.MIN_VALUE, 0.0f};
        assertFalse(bloom.contains(testValue1));
        assertFalse(bloom.contains(testValue2));
        assertFalse(bloom.contains(testValue3));
        assertFalse(bloom.contains(testValue4));
        bloom.add(testValue1);
        assertTrue(bloom.contains(testValue1));
        assertFalse(bloom.contains(testValue2));
        assertFalse(bloom.contains(testValue3));
        assertFalse(bloom.contains(testValue4));
        bloom.add(testValue2);
        assertTrue(bloom.contains(testValue1));
        assertTrue(bloom.contains(testValue2));
        assertFalse(bloom.contains(testValue3));
        assertFalse(bloom.contains(testValue4));
        bloom.add(testValue3);
        assertTrue(bloom.contains(testValue1));
        assertTrue(bloom.contains(testValue2));
        assertTrue(bloom.contains(testValue3));
        assertFalse(bloom.contains(testValue4));
    }

    @Test
    public void testContains_doubleArr() {
        BloomFilter bloom = new BloomFilter();
        double[] testValue1 = {16.1d, 32.2d, 63.3d, Double.MAX_VALUE, Double.MIN_VALUE, 0.0d};
        double[] testValue2 = {16.1d, 32.2d, 64.4d, Double.MAX_VALUE, Double.MIN_VALUE, 0.0d};
        double[] testValue3 = {16.1d, 32.2d, 65.5d, Double.MAX_VALUE, Double.MIN_VALUE, 0.0d};
        double[] testValue4 = {16.1d, 32.2d, 166.6d, Double.MAX_VALUE, Double.MIN_VALUE, 0.0d};
        assertFalse(bloom.contains(testValue1));
        assertFalse(bloom.contains(testValue2));
        assertFalse(bloom.contains(testValue3));
        assertFalse(bloom.contains(testValue4));
        bloom.add(testValue1);
        assertTrue(bloom.contains(testValue1));
        assertFalse(bloom.contains(testValue2));
        assertFalse(bloom.contains(testValue3));
        assertFalse(bloom.contains(testValue4));
        bloom.add(testValue2);
        assertTrue(bloom.contains(testValue1));
        assertTrue(bloom.contains(testValue2));
        assertFalse(bloom.contains(testValue3));
        assertFalse(bloom.contains(testValue4));
        bloom.add(testValue3);
        assertTrue(bloom.contains(testValue1));
        assertTrue(bloom.contains(testValue2));
        assertTrue(bloom.contains(testValue3));
        assertFalse(bloom.contains(testValue4));
    }

    @Test
    public void testClearAndSize() {
        BloomFilter bloom = new BloomFilter();
        char[] testValue1 = {'a', 'b', 'c', Character.MAX_VALUE, Character.MIN_VALUE, 0};
        char[] testValue2 = {'a', 'b', 'd', Character.MAX_VALUE, Character.MIN_VALUE, 0};
        char[] testValue3 = {'a', 'b', 'e', Character.MAX_VALUE, Character.MIN_VALUE, 0};
        char[] testValue4 = {'a', 'b', 'f', Character.MAX_VALUE, Character.MIN_VALUE, 0};
        for (int counter = 0; counter < 5; counter++) {
            bloom.clear();
            assertEquals(0, bloom.size());
            assertFalse(bloom.contains(testValue1));
            assertFalse(bloom.contains(testValue2));
            assertFalse(bloom.contains(testValue3));
            assertFalse(bloom.contains(testValue4));
            bloom.add(testValue1);
            assertEquals(1, bloom.size());
            assertTrue(bloom.contains(testValue1));
            assertFalse(bloom.contains(testValue2));
            assertFalse(bloom.contains(testValue3));
            assertFalse(bloom.contains(testValue4));
            bloom.add(testValue2);
            assertEquals(2, bloom.size());
            assertTrue(bloom.contains(testValue1));
            assertTrue(bloom.contains(testValue2));
            assertFalse(bloom.contains(testValue3));
            assertFalse(bloom.contains(testValue4));
            bloom.add(testValue3);
            assertEquals(3, bloom.size());
            assertTrue(bloom.contains(testValue1));
            assertTrue(bloom.contains(testValue2));
            assertTrue(bloom.contains(testValue3));
            assertFalse(bloom.contains(testValue4));
            bloom.add(testValue4);
            assertEquals(4, bloom.size());
        }
    }

    @Test
    public void testGetAllocationSize() {
        BloomFilter bloom;
        bloom = new BloomFilter(10);
        assertEquals(RamUsageEstimator.sizeOf(bloom), bloom.getAllocationSize(), 20);

        bloom = new BloomFilter(100, 8);
        assertEquals(RamUsageEstimator.sizeOf(bloom), bloom.getAllocationSize(), 50);
        
        bloom = new BloomFilter(1024, 8);
        assertEquals(RamUsageEstimator.sizeOf(bloom), bloom.getAllocationSize(), 50);

        bloom = new BloomFilter();
        assertEquals(RamUsageEstimator.sizeOf(bloom), bloom.getAllocationSize(), 50);
    }

    @Test
    public void testWriteAndRead() throws Exception {
        BloomFilter bloom = new BloomFilter();
        String testValue1 = "abc1";
        String testValue2 = "abc2";
        String testValue3 = "abc3";
        String testValue4 = "abc4";
        char[] chartestValue1 = {'a', 'b', 'c', Character.MAX_VALUE, Character.MIN_VALUE, 0};
        char[] chartestValue2 = {'a', 'b', 'd', Character.MAX_VALUE, Character.MIN_VALUE, 0};
        char[] chartestValue3 = {'a', 'b', 'e', Character.MAX_VALUE, Character.MIN_VALUE, 0};
        char[] chartestValue4 = {'a', 'b', 'f', Character.MAX_VALUE, Character.MIN_VALUE, 0};
        byte[] bytetestValue1 = {16, 32, 63, Byte.MAX_VALUE, Byte.MIN_VALUE, 0};
        byte[] bytetestValue2 = {16, 32, 64, Byte.MAX_VALUE, Byte.MIN_VALUE, 0};
        byte[] bytetestValue3 = {16, 32, 65, Byte.MAX_VALUE, Byte.MIN_VALUE, 0};
        byte[] bytetestValue4 = {16, 32, 66, Byte.MAX_VALUE, Byte.MIN_VALUE, 0};
        short[] shorttestValue1 = {16, 32, 63, Short.MAX_VALUE, Short.MIN_VALUE, 0};
        short[] shorttestValue2 = {16, 32, 64, Short.MAX_VALUE, Short.MIN_VALUE, 0};
        short[] shorttestValue3 = {16, 32, 65, Short.MAX_VALUE, Short.MIN_VALUE, 0};
        short[] shorttestValue4 = {16, 32, 66, Short.MAX_VALUE, Short.MIN_VALUE, 0};
        int[] inttestValue1 = {16, 32, 63, Integer.MAX_VALUE, Integer.MIN_VALUE, 0};
        int[] inttestValue2 = {16, 32, 64, Integer.MAX_VALUE, Integer.MIN_VALUE, 0};
        int[] inttestValue3 = {16, 32, 65, Integer.MAX_VALUE, Integer.MIN_VALUE, 0};
        int[] inttestValue4 = {16, 32, 66, Integer.MAX_VALUE, Integer.MIN_VALUE, 0};
        long[] longtestValue1 = {16L, 32L, 63L, Long.MAX_VALUE, Long.MIN_VALUE, 0L};
        long[] longtestValue2 = {16L, 32L, 64L, Long.MAX_VALUE, Long.MIN_VALUE, 0L};
        long[] longtestValue3 = {16L, 32L, 65L, Long.MAX_VALUE, Long.MIN_VALUE, 0L};
        long[] longtestValue4 = {16L, 32L, 66L, Long.MAX_VALUE, Long.MIN_VALUE, 0L};
        float[] floattestValue1 = {16.1f, 32.2f, 63.3f, Float.MAX_VALUE, Float.MIN_VALUE, 0.0f};
        float[] floattestValue2 = {16.1f, 32.2f, 64.3f, Float.MAX_VALUE, Float.MIN_VALUE, 0.0f};
        float[] floattestValue3 = {16.1f, 32.2f, 65.5f, Float.MAX_VALUE, Float.MIN_VALUE, 0.0f};
        float[] floattestValue4 = {16.1f, 32.2f, 166.6f, Float.MAX_VALUE, Float.MIN_VALUE, 0.0f};
        double[] doubletestValue1 = {16.1d, 32.2d, 63.3d, Double.MAX_VALUE, Double.MIN_VALUE, 0.0d};
        double[] doubletestValue2 = {16.1d, 32.2d, 64.3d, Double.MAX_VALUE, Double.MIN_VALUE, 0.0d};
        double[] doubletestValue3 = {16.1d, 32.2d, 65.5d, Double.MAX_VALUE, Double.MIN_VALUE, 0.0d};
        double[] doubletestValue4 = {16.1d, 32.2d, 166.6d, Double.MAX_VALUE, Double.MIN_VALUE, 0.0d};
        BloomFilter bloom1 = new BloomFilter();
        bloom1.add(chartestValue1);
        bloom1.add(chartestValue2);
        bloom1.add(chartestValue3);
        bloom1.add(chartestValue4);
        bloom1.add(bytetestValue1);
        bloom1.add(bytetestValue2);
        bloom1.add(bytetestValue3);
        bloom1.add(bytetestValue4);
        bloom1.add(shorttestValue1);
        bloom1.add(shorttestValue2);
        bloom1.add(shorttestValue3);
        bloom1.add(shorttestValue4);
        bloom1.add(inttestValue1);
        bloom1.add(inttestValue2);
        bloom1.add(inttestValue3);
        bloom1.add(inttestValue4);
        bloom1.add(longtestValue1);
        bloom1.add(longtestValue2);
        bloom1.add(longtestValue3);
        bloom1.add(longtestValue4);
        bloom1.add(floattestValue1);
        bloom1.add(floattestValue2);
        bloom1.add(floattestValue3);
        bloom1.add(floattestValue4);
        bloom1.add(doubletestValue1);
        bloom1.add(doubletestValue2);
        bloom1.add(doubletestValue3);
        bloom1.add(doubletestValue4);

        byte[] bytes;
        try (
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                DataOutputStream dout = new DataOutputStream(bout);) {
            bloom1.write(dout);
            dout.flush();
            bytes = bout.toByteArray(); 
        }

        try (
                ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
                DataInputStream din = new DataInputStream(bin);) {
            BloomFilter bloom2 = new BloomFilter();
            bloom2.read(din);
            
            assertEquals(bloom1.size(), bloom2.size());
            assertEquals(bloom1.getAllocationSize(), bloom2.getAllocationSize());
            
            assertEquals(bloom1.contains(chartestValue1), bloom2.contains(chartestValue1));
            assertEquals(bloom1.contains(chartestValue2), bloom2.contains(chartestValue2));
            assertEquals(bloom1.contains(chartestValue3), bloom2.contains(chartestValue3));
            assertEquals(bloom1.contains(chartestValue4), bloom2.contains(chartestValue4));

            assertEquals(bloom1.contains(bytetestValue1), bloom2.contains(bytetestValue1));
            assertEquals(bloom1.contains(bytetestValue2), bloom2.contains(bytetestValue2));
            assertEquals(bloom1.contains(bytetestValue3), bloom2.contains(bytetestValue3));
            assertEquals(bloom1.contains(bytetestValue4), bloom2.contains(bytetestValue4));

            assertEquals(bloom1.contains(shorttestValue1), bloom2.contains(shorttestValue1));
            assertEquals(bloom1.contains(shorttestValue2), bloom2.contains(shorttestValue2));
            assertEquals(bloom1.contains(shorttestValue3), bloom2.contains(shorttestValue3));
            assertEquals(bloom1.contains(shorttestValue4), bloom2.contains(shorttestValue4));

            assertEquals(bloom1.contains(inttestValue1), bloom2.contains(inttestValue1));
            assertEquals(bloom1.contains(inttestValue2), bloom2.contains(inttestValue2));
            assertEquals(bloom1.contains(inttestValue3), bloom2.contains(inttestValue3));
            assertEquals(bloom1.contains(inttestValue4), bloom2.contains(inttestValue4));

            assertEquals(bloom1.contains(longtestValue1), bloom2.contains(longtestValue1));
            assertEquals(bloom1.contains(longtestValue2), bloom2.contains(longtestValue2));
            assertEquals(bloom1.contains(longtestValue3), bloom2.contains(longtestValue3));
            assertEquals(bloom1.contains(longtestValue4), bloom2.contains(longtestValue4));

            assertEquals(bloom1.contains(floattestValue1), bloom2.contains(floattestValue1));
            assertEquals(bloom1.contains(floattestValue2), bloom2.contains(floattestValue2));
            assertEquals(bloom1.contains(floattestValue3), bloom2.contains(floattestValue3));
            assertEquals(bloom1.contains(floattestValue4), bloom2.contains(floattestValue4));

            assertEquals(bloom1.contains(doubletestValue1), bloom2.contains(doubletestValue1));
            assertEquals(bloom1.contains(doubletestValue2), bloom2.contains(doubletestValue2));
            assertEquals(bloom1.contains(doubletestValue3), bloom2.contains(doubletestValue3));
            assertEquals(bloom1.contains(doubletestValue4), bloom2.contains(doubletestValue4));
            
        }

    }

}
