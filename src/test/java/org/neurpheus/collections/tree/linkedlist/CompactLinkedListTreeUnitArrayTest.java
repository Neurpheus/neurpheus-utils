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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Tests {@link CompactLinkedListTreeUnitArray}.
 *
 * @author Jakub Strychowski
 */
public class CompactLinkedListTreeUnitArrayTest  extends AbstractLinkedListTreeUnitArrayTest  {
    
    public CompactLinkedListTreeUnitArrayTest() {
    }

    @Override
    public AbstractLinkedListTreeUnitArray newInstance() {
        return new CompactLinkedListTreeUnitArray(); 
    }

    @Test
    public void testClear() {
        LinkedListTreeUnitArray lla = newInstance();
        lla.add(new LinkedListTreeUnit(1, 0, false, true, 0));
        lla.add(new LinkedListTreeUnit(2, 1, true, false, 1));
        lla.add(new LinkedListTreeUnit(3, 1, true, false, 2));
        lla.clear(1000);
        assertEquals(0, lla.size());
        assertTrue(lla.getAllocationSize() > 100);
    }
    
    @Test
    public void testCopyConstructor1() {
        CompactLinkedListTreeUnitArray lla = new CompactLinkedListTreeUnitArray(200);
        lla.clear(200);
        for (int i = 0; i < 200; i++) {
            lla.add(new LinkedListTreeUnit(i & 10, i % 5, (i % 3 == 0), (i % 7 == 0), i));
        }
        lla.compact();
        LinkedListTreeUnitArray lla2 = new CompactLinkedListTreeUnitArray(lla);

        assertEquals(lla.size(), lla2.size());
        assertEquals(lla.getAllocationSize(), lla2.getAllocationSize());
        assertFalse(lla == lla2);
        for (int i = 0; i < 200; i++) {
            LinkedListTreeUnit unit1 = lla.get(i);
            LinkedListTreeUnit unit2 = lla2.get(i);
            assertEquals(unit1, unit2);
            if (unit1 != null) {
                assertFalse(unit1 == unit2);
            }
        }
    }

    @Test
    public void testCopyConstructor2() {
        FastLinkedListTreeUnitArray lla = new FastLinkedListTreeUnitArray();
        lla.clear(200);
        for (int i = 0; i < 200; i++) {
            lla.add(new LinkedListTreeUnit(i & 10, i % 5, (i % 3 == 0), (i % 7 == 0), i));
        }
        LinkedListTreeUnitArray lla2 = new CompactLinkedListTreeUnitArray(lla);

        assertEquals(lla.size(), lla2.size());
        assertFalse(lla == lla2);
        for (int i = 0; i < 200; i++) {
            LinkedListTreeUnit unit1 = lla.get(i);
            LinkedListTreeUnit unit2 = lla2.get(i);
            assertEquals(unit1, unit2);
            if (unit1 != null) {
                assertFalse(unit1 == unit2);
            }
        }
    }

    @Test
    public void testModificationinCompactMode() {
        CompactLinkedListTreeUnitArray lla = new CompactLinkedListTreeUnitArray(200);
        lla.clear(200);
        for (int i = 0; i < 200; i++) {
            lla.add(new LinkedListTreeUnit(i & 10, i % 5, (i % 3 == 0), (i % 7 == 0), i));
        }
        lla.compact();
        lla.compact();
        try {
            lla.set(1, new LinkedListTreeUnit(2, 2, true, false, 2));
            fail("An exception should be thrown");
        } catch (IllegalStateException ex) {
            // ok
        }
        try {
            lla.add(new LinkedListTreeUnit(2, 2, true, false, 2));
            fail("An exception should be thrown");
        } catch (IllegalStateException ex) {
            // ok
        }
        try {
            lla.set(1, 5, true, true, 2, 3);
            fail("An exception should be thrown");
        } catch (IllegalStateException ex) {
            // ok
        }
        try {
            lla.ensureCapacity(100);
            fail("An exception should be thrown");
        } catch (IllegalStateException ex) {
            // ok
        }
    }
    
    @Test
    public void testAddSetAndGettersCompact() {
        CompactLinkedListTreeUnitArray lla = new CompactLinkedListTreeUnitArray();
        int distance = 10;
        boolean wordEnd = true;
        boolean wordContinued = true;
        int valueCode = 5;
        int dataCode = 5;
        for (int index = 0; index < 10; index++) {
            lla.add(new LinkedListTreeUnit(valueCode, distance, wordEnd, wordContinued, dataCode));
            distance += 20;
            wordEnd = index % 2 == 0;
            wordContinued = index % 3 == 0;
            valueCode += 5;
            dataCode += 5;
        }
        
        lla.compact();

        distance = 10;
        wordEnd = true;
        wordContinued = true;
        valueCode = 5;
        dataCode = 5;
        for (int index = 0; index < 10; index++) {
            assertEquals(distance, lla.getDistance(index));
            assertTrue(lla.isWordEnd(index) == wordEnd);
            assertTrue(lla.isWordContinued(index) == wordContinued);
            assertEquals(valueCode, lla.getValueCode(index));
            assertEquals(dataCode, lla.getDataCode(index));
            assertTrue(lla.isAbsolutePointer(index) == (!wordContinued && !wordEnd));

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
    public void testGetNumberOfDifferentUnits() {
        CompactLinkedListTreeUnitArray lla = new CompactLinkedListTreeUnitArray();
        lla.add(new LinkedListTreeUnit(1, 1, true, true, 1));
        lla.add(new LinkedListTreeUnit(1, 1, true, true, 1));
        lla.add(new LinkedListTreeUnit(1, 1, true, true, 1));
        lla.add(new LinkedListTreeUnit(2, 1, true, true, 1));
        lla.add(new LinkedListTreeUnit(2, 1, true, true, 1));
        lla.add(new LinkedListTreeUnit(3, 1, true, true, 1));
        lla.add(new LinkedListTreeUnit(4, 1, true, true, 1));
        lla.add(new LinkedListTreeUnit(5, 1, true, true, 1));
        lla.add(new LinkedListTreeUnit(6, 1, true, true, 1));
        assertEquals(6, lla.getNumberOfDifferentUnits());
        lla.compact();
        assertEquals(6, lla.getNumberOfDifferentUnits());
    }
    
}
