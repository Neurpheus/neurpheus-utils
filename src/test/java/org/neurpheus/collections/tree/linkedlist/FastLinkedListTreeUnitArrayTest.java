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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests fast implementation of LLT unit array.
 * 
 * @author Jakub Strychowski
 */
public class FastLinkedListTreeUnitArrayTest extends AbstractLinkedListTreeUnitArrayTest {

    public FastLinkedListTreeUnitArrayTest() {
    }

    @Override
    public AbstractLinkedListTreeUnitArray newInstance() {
        return new FastLinkedListTreeUnitArray();
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
        LinkedListTreeUnitArray lla = newInstance();
        lla.clear(200);
        for (int i = 0; i < 200; i++) {
            lla.add(new LinkedListTreeUnit(i & 10, i % 5, (i % 3 == 0), (i % 7 == 0), i));
        }
        LinkedListTreeUnitArray lla2 = new FastLinkedListTreeUnitArray(lla);

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
        CompactLinkedListTreeUnitArray lla = new CompactLinkedListTreeUnitArray();
        lla.clear(200);
        for (int i = 0; i < 200; i++) {
            lla.add(new LinkedListTreeUnit(i & 10, i % 5, (i % 3 == 0), (i % 7 == 0), i));
        }
        lla.compact();
        LinkedListTreeUnitArray lla2 = new FastLinkedListTreeUnitArray(lla);

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
    
}
