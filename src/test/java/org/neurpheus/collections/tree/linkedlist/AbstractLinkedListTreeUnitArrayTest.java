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

package org.neurpheus.collections.tree.linkedlist;

import com.carrotsearch.sizeof.RamUsageEstimator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests LLT unit arrays.
 * 
 * @author Jakub Strychowski
 */
public abstract class AbstractLinkedListTreeUnitArrayTest {
    
    public AbstractLinkedListTreeUnitArrayTest() {
    }

    public abstract AbstractLinkedListTreeUnitArray newInstance();
    
    @Test
    public void testEqualsUnits() {
        LinkedListTreeUnitArray lla = newInstance();
        int distance = 10;
        boolean wordEnd = true;
        boolean wordContinued = true;
        int valueCode = 5;
        int dataCode = 5;
        for (int index = 0; index < 10; index++) {
            LinkedListTreeUnit newUnit = new LinkedListTreeUnit(
                    valueCode, distance, wordEnd, wordContinued, dataCode);
            lla.add(newUnit);
            lla.add(newUnit);

            distance += 20;
            wordEnd = index % 2 == 0;
            wordContinued = index % 3 == 0;
            valueCode += 5;
            dataCode += 5;
            
            
            assertTrue(lla.equalsUnits(index * 2, index * 2 + 1));
            if (index > 0) {
                assertFalse(lla.equalsUnits(index * 2, index * 2 - 1));
            }
        }
    }
    
    @Test
    public void testCompareUnits() {
        LinkedListTreeUnitArray lla = newInstance();
        int distance = 10;
        boolean wordEnd = true;
        boolean wordContinued = true;
        int valueCode = 5;
        int dataCode = 5;
        for (int index = 0; index < 10; index++) {
            LinkedListTreeUnit newUnit = new LinkedListTreeUnit(
                    valueCode, distance, wordEnd, wordContinued, dataCode);
            lla.add(newUnit);
            lla.add(newUnit);

            distance += 20;
            wordEnd = !wordEnd;
            wordContinued = !wordContinued;
            valueCode += 5;
            dataCode += 5;
            
            
            assertEquals(0, lla.compareUnits(index * 2, index * 2 + 1));
            if (index > 0) {
                assertTrue(lla.compareUnits(index * 2, index * 2 - 1) > 0);
                assertTrue(lla.compareUnits(index * 2 - 1, index * 2) < 0);
            }
        }
    }
    
    @Test
    public void testAddSetAndGetters() {
        LinkedListTreeUnitArray lla = newInstance();
        lla.add(new LinkedListTreeUnit(1, 0, false, true, 0));
        lla.add(new LinkedListTreeUnit(2, 1, true, false, 1));
        lla.add(new LinkedListTreeUnit(3, 1, true, false, 2));
        int distance = 10;
        boolean wordEnd = true;
        boolean wordContinued = true;
        int valueCode = 5;
        int dataCode = 5;
        for (int index = 0; index < 10; index++) {
            lla.set(index, distance, wordEnd, wordContinued, valueCode, dataCode);
            assertEquals(distance, lla.getDistance(index));
            assertTrue(lla.isWordEnd(index) == wordEnd);
            assertTrue(lla.isWordContinued(index) == wordContinued);
            assertEquals(valueCode, lla.getValueCode(index));
            assertEquals(dataCode, lla.getDataCode(index));
            assertTrue(lla.isAbsolutePointer(index) == (!wordContinued && !wordEnd));

            lla.set(index, new LinkedListTreeUnit(valueCode, distance, wordEnd, wordContinued,
                                                  dataCode));

            int fastIndex = lla.getFastIndex(index);

            assertEquals(distance, lla.getDistanceFast(fastIndex));
            assertTrue(lla.isWordEndFast(fastIndex) == wordEnd);
            assertTrue(lla.isWordContinuedFast(fastIndex) == wordContinued);
            assertEquals(valueCode, lla.getValueCodeFast(fastIndex));
            assertEquals(dataCode, lla.getDataCodeFast(fastIndex));
            assertTrue(lla.isAbsolutePointerFast(fastIndex) == (!wordContinued && !wordEnd));

            distance += 20;
            wordEnd = index % 2 == 0;
            wordContinued = index % 3 == 0;
            valueCode += 5;
            dataCode += 5;

        }
    }

    @Test
    public void setNull() {
        LinkedListTreeUnitArray lla = newInstance();
        lla.add(new LinkedListTreeUnit(1, 0, false, true, 0));
        lla.add(new LinkedListTreeUnit(2, 1, true, false, 1));
        lla.add(new LinkedListTreeUnit(3, 1, true, false, 2));
        lla.set(1, null);
        assertTrue(lla.get(1) == null);
    }
    
    @Test
    public void testAddAndGet() {
        LinkedListTreeUnitArray lla = newInstance();
        int distance = 10;
        boolean wordEnd = true;
        boolean wordContinued = true;
        int valueCode = 5;
        int dataCode = 5;
        LinkedListTreeUnit lastUnit = null;
        for (int index = 0; index < 10; index++) {
            LinkedListTreeUnit newUnit = new LinkedListTreeUnit(
                    valueCode, distance, wordEnd, wordContinued, dataCode);
            lla.add(newUnit);
            assertEquals(distance, lla.getDistance(index));
            assertTrue(lla.isWordEnd(index) == wordEnd);
            assertTrue(lla.isWordContinued(index) == wordContinued);
            assertEquals(valueCode, lla.getValueCode(index));
            assertEquals(dataCode, lla.getDataCode(index));
            assertTrue(lla.isAbsolutePointer(index) == (!wordContinued && !wordEnd));

            if (lastUnit != null) {
                assertTrue(lastUnit.equals(lla.get(index - 1)));
            }

            lastUnit = newUnit;

            distance += 20;
            wordEnd = !wordEnd;
            wordContinued = !wordContinued;
            valueCode += 5;
            dataCode += 5;

        }
    }

    @Test
    public void testGetAllocationSizeAndDispose() {
        LinkedListTreeUnitArray lla = newInstance();
        lla.clear(200);
        for (int i = 0; i < 200; i++) {
            lla.add(new LinkedListTreeUnit(i & 10, 0, false, true, 0));
        }
        lla.trimToSize();
        assertEquals("Unexpected allocation size.", RamUsageEstimator.sizeOf(lla), lla.
                     getAllocationSize(), 30);
        lla.dispose();
        assertEquals("Unexpected allocation size.", RamUsageEstimator.sizeOf(lla), lla.
                     getAllocationSize(), 10);
    }
    
    @Test
    public void testSize() {
        LinkedListTreeUnitArray lla = newInstance();
        for (int i = 0; i < 200; i++) {
            lla.add(new LinkedListTreeUnit(i & 10, 0, false, true, 0));
            assertEquals(i + 1, lla.size());
        }
    }

    

    @Test
    public void testWriteAndRead() {
        LinkedListTreeUnitArray lla = newInstance();
        lla.clear(200);
        for (int i = 0; i < 200; i++) {
            lla.add(new LinkedListTreeUnit(i & 10, i % 5, (i % 3 == 0), (i % 7 == 0), i));
        }
        byte[] data;
        try (
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                DataOutputStream dataStream = new DataOutputStream(outStream)) {
            lla.write(dataStream);
            dataStream.flush();
            data = outStream.toByteArray();
        } catch (IOException ex) {
            fail(ex.getMessage());
            return;
        }
        
        LinkedListTreeUnitArray lla2 = newInstance();
        try (
                ByteArrayInputStream inStream = new ByteArrayInputStream(data);
                DataInputStream dataStream = new DataInputStream(inStream)) {
            lla2.read(dataStream);
        } catch (IOException ex) {
            fail(ex.getMessage());
        }

        assertEquals(lla.size(), lla2.size());
        assertEquals(lla.getAllocationSize(), lla2.getAllocationSize());
        assertFalse(lla == lla2);
        for (int i = 0; i < 200; i++) {
            assertEquals(lla.get(i), lla2.get(i));
        }
    }

    @Test
    public void testWriteAndReadFormatError() {
        LinkedListTreeUnitArray lla = newInstance();
        lla.clear(200);
        for (int i = 0; i < 200; i++) {
            lla.add(new LinkedListTreeUnit(i & 10, i % 5, (i % 3 == 0), (i % 7 == 0), i));
        }
        byte[] data;
        try (
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                DataOutputStream dataStream = new DataOutputStream(outStream)) {
            dataStream.writeByte(2);
            lla.write(dataStream);
            dataStream.flush();
            data = outStream.toByteArray();
        } catch (IOException ex) {
            fail(ex.getMessage());
            return;
        }
        
        LinkedListTreeUnitArray lla2 = newInstance();
        try (
                ByteArrayInputStream inStream = new ByteArrayInputStream(data);
                DataInputStream dataStream = new DataInputStream(inStream)) {
            lla2.read(dataStream);
            fail("Exception should be thrown");
        } catch (IOException ex) {
            // ok
        }
    }
    
    @Test
    public void testLogStatistics() {
        String name = "test-statistics";
        AbstractLinkedListTreeUnitArray instance = newInstance();
        instance.logStatistics(name);
    }


    @Test
    public void testSubArray() {
        LinkedListTreeUnitArray lla = newInstance();
        lla.clear(200);
        for (int i = 0; i < 200; i++) {
            lla.add(new LinkedListTreeUnit(i & 10, i % 5, (i % 3 == 0), (i % 7 == 0), i));
        }
        String message = "Invalid index - methd should throw IndexOutOfBoundsException";
        try {
            lla.subArray(-1, 200);
            fail(message);
        } catch (IndexOutOfBoundsException ex) {
            // ok
        }
        try {
            lla.subArray(0, 202);
            fail(message);
        } catch (IndexOutOfBoundsException ex) {
            // ok
        }
        lla.subArray(0, 200);
        try {
            lla.subArray(300, 201);
            fail(message);
        } catch (IndexOutOfBoundsException ex) {
            // ok
        }
        try {
            lla.subArray(120, 100);
            fail(message);
        } catch (IndexOutOfBoundsException ex) {
            // ok
        }
    }

    @Test
    public void testSubArrayArgumentsVerified() {
        LinkedListTreeUnitArray lla = newInstance();
        lla.clear(200);
        for (int i = 0; i < 200; i++) {
            lla.add(new LinkedListTreeUnit(i & 10, 301 - i, (i % 3 == 0), (i % 7 == 0), i));
        }
        int subArrayLength = 11;
        for (int offset = 0; offset < 200 - subArrayLength; offset += subArrayLength) {
            LinkedListTreeUnitArray lla2 = lla.subArray(offset, offset + subArrayLength);
            for (int i = 0; i < subArrayLength; i++) {
                assertEquals(lla.get(offset + i), lla2.get(i));
            }
        }
    }

    @Test
    public void testMoveAbsolutePointers() {
        LinkedListTreeUnitArray lla = newInstance();
        lla.clear(200);
        for (int i = 0; i < 200; i++) {
            lla.add(new LinkedListTreeUnit(i, 300 - i, false, false, i));
        }
        for (int offset = -100; offset < 100; offset += 20) {
            lla.moveAbsolutePointers(offset);
            for (int i = 0; i < 200; i++) {
                assertTrue(lla.isAbsolutePointer(i));
                assertEquals(offset + 300 - i, lla.getDistance(i));
            }
            lla.moveAbsolutePointers(-offset);
        }
    }

    @Test
    public void testAddAll() {
        LinkedListTreeUnitArray lla = newInstance();
        lla.clear(200);
        for (int i = 0; i < 200; i++) {
            lla.add(new LinkedListTreeUnit(i, 201 - i, false, false, i));
        }
        LinkedListTreeUnitArray lla2 = lla.subArray(0, 10);
        lla.addAll(lla2);
        assertEquals(210, lla.size());
        for (int i = 0; i < 10; i++) {
            assertEquals(lla2.get(i), lla.get(200 + i));
        }
    }

    
    
    @Test
    public void testValueMapping() {
        AbstractLinkedListTreeUnitArray lla = newInstance();
        assertTrue(lla.getValueMapping() == null);
        assertTrue(lla.getReverseValueMapping() == null);
        for (int i = 0; i < 200; i++) {
            lla.add(new LinkedListTreeUnit(i, 200 - i, false, false, i));
        }
        assertTrue(lla.getValueMapping() == null);
        assertTrue(lla.getReverseValueMapping() == null);
        lla.setValueMapping(null);
        assertTrue(lla.getValueMapping() == null);
        assertTrue(lla.getReverseValueMapping() == null);
        int[] mapping = new int[] {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'ą', 'ę', 'ł', 'ź'};
        lla.setValueMapping(mapping);
        assertArrayEquals(mapping, lla.getValueMapping());
        Map<Integer, Integer> reverseMap = lla.getReverseValueMapping();
        assertTrue(reverseMap != null);
        for (int i = 0; i < mapping.length; i++) {
            assertEquals(i, reverseMap.get(mapping[i]).intValue());
            assertEquals(i, lla.mapToValueCode(mapping[i]));
        }
        for (int i = 0; i < mapping.length; i++) {
            lla.set(i, 0, true, true, i, 0);
            assertEquals(mapping[i], lla.getValue(i));
        }
        
        
    }

    @Test
    public void testToString() {
        LinkedListTreeUnitArray lla = newInstance();
        lla.clear(200);
        for (int i = 0; i < 200; i++) {
            lla.add(new LinkedListTreeUnit(i, 201 - i, false, false, i));
        }
        String result = lla.toString(0, 5);
        assertTrue(result.length() > 100);
        System.out.println(result);
    }
    
    
}
