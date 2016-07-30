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

package org.neurpheus.collections.array;

import com.carrotsearch.sizeof.RamUsageEstimator;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.BitSet;
import org.junit.experimental.categories.Category;


/**
 * Test methods of PaginationSorting class.
 *
 * @author Jakub Strychowski
 */
public class BitsArrayTest {

    public BitsArrayTest() {
    }

    /**
     * Checks performance of the BitsArray which should be no slower then java.util.BitSet.
     */
    @Category(org.neurpheus.test.PerformenceTest.class)
    @Test
    public void testPerformance() {

        final int arrayLength = 1_000_000;
        boolean bit;
        long startTime;

        for (int x = 0; x < 10; x++) {

            bit = true;
            startTime = System.nanoTime();
            boolean[] boola = new boolean[arrayLength];
            for (int i = 0; i < arrayLength; i++) {
                boola[i] = bit;
                bit = !boola[i];
            }
            long boolaSetDuration = (System.nanoTime() - startTime);
            System.out.printf("Time for boolean[]: %d mics.%n", boolaSetDuration / 1_000);

            bit = true;
            startTime = System.nanoTime();
            BitsArray ba = new BitsArray(arrayLength);
            for (int i = 0; i < arrayLength; i++) {
                ba.set(i, bit);
                bit = !ba.get(i);
            }
            ba.compact();
            long bitsArrayDuration = (System.nanoTime() - startTime);
            System.out.printf("Time for BitsArray: %d mics.%n", bitsArrayDuration / 1_000);

            bit = true;
            startTime = System.nanoTime();
            BitSet bs = new BitSet(arrayLength);
            for (int i = 0; i < arrayLength; i++) {
                bs.set(i, bit);
                bit = !bs.get(i);
            }
            long bitSetDuration = (System.nanoTime() - startTime);
            System.out.printf("Time for BitSet: %d mics.%n", bitSetDuration / 1_000);

            if (x > 20) {
                // after warming up for JIT
                assertTrue("BitsArray should be no slower then BitSet", 
                         bitsArrayDuration < 1.3 * bitSetDuration);
            }
        }
    }

    /**
     * Test of size method, of class BitsArray.
     */
    @Test
    public void testSize() {
        BitsArray array = new BitsArray();
        array.set(10, true);
        assertEquals(11, array.size());
        array.set(5, true);
        assertEquals(11, array.size());
        array.set(10, true);
        assertEquals(11, array.size());
        array.set(11, true);
        assertEquals(12, array.size());
        array.set(20, true);
        assertEquals(21, array.size());

    }

    private static final int[] INDEXES = 
            new int[]{0, 1, 2, 7, 8, 9, 10, 14, 15, 16, 17, 63, 64, 65, 5000, 8192, 100000};

    /**
     * Test of set method, of class BitsArray.
     */
    @Test
    public void testSetGet() {
        BitsArray array = new BitsArray();

        for (int index : INDEXES) {
            array.set(index, true);
            assertTrue(array.get(index));
            array.compact();
            assertTrue(array.get(index));
            array.set(index, false);
            assertTrue(!array.get(index));
            array.compact();
            assertTrue(!array.get(index));
            array.set(index, true);
            assertTrue(array.get(index));
            array.compact();
            assertTrue(array.get(index));
        }
    }

    /**
     * Test of set method, of class BitsArray.
     */
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testSetIndexOutOfBounds() {
        BitsArray array = new BitsArray();
        array.set(-1, true);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetIndexOutOfBounds1() {
        BitsArray instance = new BitsArray();
        instance.get(0);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetIndexOutOfBounds2() {
        BitsArray instance = new BitsArray();
        instance.get(-1);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetIndexOutOfBounds3() {
        BitsArray instance = new BitsArray();
        instance.get(1);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetIndexOutOfBounds4() {
        BitsArray instance = new BitsArray();
        instance.set(0, true);
        instance.get(0);
        instance.get(1);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetIndexOutOfBounds5() {
        BitsArray instance = new BitsArray();
        instance.set(4, true);
        instance.get(0);
        instance.get(1);
        instance.get(2);
        instance.get(3);
        instance.get(4);
        instance.get(5);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetIndexOutOfBounds6() {
        BitsArray instance = new BitsArray(10);
        instance.set(4, true);
        instance.get(0);
        instance.get(1);
        instance.get(2);
        instance.get(3);
        instance.get(4);
        instance.get(5);
    }

    /**
     * Test of getBackingArray method, of class BitsArray.
     */
    @Test
    public void testGetBackingArray() {
        System.out.println("getBackingArray");
        BitsArray instance = new BitsArray(128);
        instance.set(0, true);
        long[] result = instance.getBackingArray();
        assertEquals(2, result.length);
        assertEquals(1L, result[0]);
        assertEquals(0L, result[1]);

        instance.set(64, true);
        assertEquals(1L, result[1]);

        instance.set(0, false);
        instance.set(64, false);
        instance.set(63, true);
        assertEquals(Long.MIN_VALUE, result[0]);
    }

    /**
     * Test of getAllocationSize method, of class BitsArray.
     */
    @Test
    public void testGetAllocationSize() {
        BitsArray instance = new BitsArray(128);
        assertEquals(RamUsageEstimator.sizeOf(instance), instance.getAllocationSize());

        instance.set(4, true);
        instance.compact();
        assertEquals(RamUsageEstimator.sizeOf(instance), instance.getAllocationSize());

        instance.set(127, true);
        instance.compact();
        assertEquals(RamUsageEstimator.sizeOf(instance), instance.getAllocationSize());

        instance.set(128, true);
        instance.compact();
        assertEquals(RamUsageEstimator.sizeOf(instance), instance.getAllocationSize());

        instance.set(4354, true);
        instance.compact();
        assertEquals(RamUsageEstimator.sizeOf(instance), instance.getAllocationSize());
    }

    /**
     * Test of write method, of class BitsArray.
     * 
     */
    @Test
    public void testStreaming() {
        try {
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(ba);
            BitsArray instance = new BitsArray();

            for (int index : INDEXES) {
                instance.set(index, true);
            }

            instance.write(out);

            out.flush();
            byte[] streamData = ba.toByteArray();

            ByteArrayInputStream bain = new ByteArrayInputStream(streamData);
            DataInputStream in = new DataInputStream(bain);
            instance = BitsArray.readInstance(in);
            int counter = 0;
            for (int i = INDEXES[INDEXES.length - 1]; i >= 0; i--) {
                boolean bit = instance.get(i);
                if (bit) {
                    counter++;
                }
            }
            assertEquals(INDEXES.length, counter);

            bain.reset();
            in = new DataInputStream(bain);
            instance = new BitsArray(10);
            instance.read(in);
            counter = 0;
            for (int i = INDEXES[INDEXES.length - 1]; i >= 0; i--) {
                boolean bit = instance.get(i);
                if (bit) {
                    counter++;
                }
            }
            assertEquals(INDEXES.length, counter);
        } catch (IOException ex) {
            fail(ex.getMessage());
        }

    }
    
    
    @Test
    public void testConstructor() {

        BitsArray ba1 = new BitsArray();
        for (int index : INDEXES) {
            ba1.set(index, true);
        }
        long[] ba = ba1.getBackingArray();
        
        int size = ba1.size();
                
        BitsArray ba2 = new BitsArray(size, ba);
        for (int i = 0; i < size; i++) {
            assertEquals(ba1.get(i), ba2.get(i));
        }
        
        BitsArray ba3 = new BitsArray(size * 2, ba);
        for (int i = 0; i < size; i++) {
            assertEquals(ba1.get(i), ba3.get(i));
        }
        for (int i = size; i < size * 2; i++) {
            assertFalse(ba3.get(i));
        }
        
        BitsArray ba4 = new BitsArray(size / 2, ba);
        assertEquals(size /2 , ba4.size());
        for (int i = 0; i < size / 2; i++) {
            assertEquals(ba1.get(i), ba3.get(i));
        }
        
        
    }

}
