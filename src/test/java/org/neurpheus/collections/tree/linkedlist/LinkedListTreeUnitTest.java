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
 * Tests Linked List Tree unit.
 *
 * @author Jakub Strychowski
 */
public class LinkedListTreeUnitTest {
    
    public LinkedListTreeUnitTest() {
    }

    /**
     * Test of getValueCode method, of class LinkedListTreeUnit.
     */
    @Test
    public void testGetValueCode() {
        LinkedListTreeUnit unit = new LinkedListTreeUnit();
        assertEquals(0, unit.getValueCode());
        
        unit = new LinkedListTreeUnit(1, 1, true, true, 0);
        assertEquals(1, unit.getValueCode());
        
        unit = new LinkedListTreeUnit(0, 1, true, true, 0);
        assertEquals(0, unit.getValueCode());
        
        unit = new LinkedListTreeUnit(-1, 1, true, true, 0);
        assertEquals(-1, unit.getValueCode());
        
        unit = new LinkedListTreeUnit(Integer.MIN_VALUE, 1, true, true, 0);
        assertEquals(Integer.MIN_VALUE, unit.getValueCode());
        
        unit = new LinkedListTreeUnit(Integer.MAX_VALUE, 1, true, true, 0);
        assertEquals(Integer.MAX_VALUE, unit.getValueCode());
    }

    /**
     * Test of setValueCode method, of class LinkedListTreeUnit.
     */
    @Test
    public void testSetValueCode() {
        LinkedListTreeUnit unit = new LinkedListTreeUnit();
        assertEquals(0, unit.getValueCode());
        
        unit.setValueCode(1);
        assertEquals(1, unit.getValueCode());
        
        unit.setValueCode(0);
        assertEquals(0, unit.getValueCode());
        
        unit.setValueCode(-1);
        assertEquals(-1, unit.getValueCode());
        
        unit.setValueCode(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, unit.getValueCode());
        
        unit.setValueCode(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, unit.getValueCode());
    }

    /**
     * Test of getDistance method, of class LinkedListTreeUnit.
     */
    @Test
    public void testGetDistance() {
        LinkedListTreeUnit unit = new LinkedListTreeUnit();
        assertEquals(0, unit.getDistance());
        
        unit = new LinkedListTreeUnit(2, 1, true, true, 0);
        assertEquals(1, unit.getDistance());
        
        unit = new LinkedListTreeUnit(2, 0, true, true, 0);
        assertEquals(0, unit.getDistance());
        
        unit = new LinkedListTreeUnit(2, -1, true, true, 0);
        assertEquals(-1, unit.getDistance());
        
        unit = new LinkedListTreeUnit(2, Integer.MIN_VALUE, true, true, 0);
        assertEquals(Integer.MIN_VALUE, unit.getDistance());
        
        unit = new LinkedListTreeUnit(2, Integer.MAX_VALUE, true, true, 0);
        assertEquals(Integer.MAX_VALUE, unit.getDistance());
    }

    /**
     * Test of setDistance method, of class LinkedListTreeUnit.
     */
    @Test
    public void testSetDistance() {
        LinkedListTreeUnit unit = new LinkedListTreeUnit();
        assertEquals(0, unit.getDistance());
        
        unit.setDistance(1);
        assertEquals(1, unit.getDistance());
        
        unit.setDistance(0);
        assertEquals(0, unit.getDistance());
        
        unit.setDistance(-1);
        assertEquals(-1, unit.getDistance());
        
        unit.setDistance(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, unit.getDistance());
        
        unit.setDistance(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, unit.getDistance());
    }

    /**
     * Test of getDataCode method, of class LinkedListTreeUnit.
     */
    @Test
    public void testGetDataCode() {
        LinkedListTreeUnit unit = new LinkedListTreeUnit();
        assertEquals(0, unit.getDataCode());
        
        unit = new LinkedListTreeUnit(2, 3, true, true, 1);
        assertEquals(1, unit.getDataCode());
        
        unit = new LinkedListTreeUnit(2, 3, true, true, 0);
        assertEquals(0, unit.getDataCode());
        
        unit = new LinkedListTreeUnit(2, 3, true, true, -1);
        assertEquals(-1, unit.getDataCode());
        
        unit = new LinkedListTreeUnit(2, 3, true, true, Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, unit.getDataCode());
        
        unit = new LinkedListTreeUnit(2, 3, true, true, Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, unit.getDataCode());
    }

    /**
     * Test of setDataCode method, of class LinkedListTreeUnit.
     */
    @Test
    public void testSetDataCode() {
        LinkedListTreeUnit unit = new LinkedListTreeUnit();
        assertEquals(0, unit.getDataCode());
        
        unit.setDataCode(1);
        assertEquals(1, unit.getDataCode());
        
        unit.setDataCode(0);
        assertEquals(0, unit.getDataCode());
        
        unit.setDataCode(-1);
        assertEquals(-1, unit.getDataCode());
        
        unit.setDataCode(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, unit.getDataCode());
        
        unit.setDataCode(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, unit.getDataCode());
    }

    /**
     * Test of isWordEnd method, of class LinkedListTreeUnit.
     */
    @Test
    public void testIsWordEnd() {
        LinkedListTreeUnit unit = new LinkedListTreeUnit();
        assertFalse(unit.isWordEnd());
        
        unit = new LinkedListTreeUnit(2,2, false, false, 3);
        assertFalse(unit.isWordEnd());
        
        unit = new LinkedListTreeUnit(2,2, true, false, 3);
        assertTrue(unit.isWordEnd());
    }

    /**
     * Test of setWordEnd method, of class LinkedListTreeUnit.
     */
    @Test
    public void testSetWordEnd() {
        LinkedListTreeUnit unit = new LinkedListTreeUnit();
        assertFalse(unit.isWordEnd());
        
        unit.setWordEnd(true);
        assertTrue(unit.isWordEnd());

        unit.setWordEnd(true);
        assertTrue(unit.isWordEnd());

        unit.setWordEnd(false);
        assertFalse(unit.isWordEnd());

        unit.setWordEnd(false);
        assertFalse(unit.isWordEnd());

        unit.setWordEnd(true);
        assertTrue(unit.isWordEnd());
    }

    /**
     * Test of isWordContinued method, of class LinkedListTreeUnit.
     */
    @Test
    public void testIsWordContinued() {
        LinkedListTreeUnit unit = new LinkedListTreeUnit();
        assertFalse(unit.isWordContinued());
        
        unit = new LinkedListTreeUnit(2,2, false, false, 3);
        assertFalse(unit.isWordContinued());
        
        unit = new LinkedListTreeUnit(2,2, true, true, 3);
        assertTrue(unit.isWordContinued());
    }

    /**
     * Test of setWordContinued method, of class LinkedListTreeUnit.
     */
    @Test
    public void testSetWordContinued() {
        LinkedListTreeUnit unit = new LinkedListTreeUnit();
        assertFalse(unit.isWordContinued());
        
        unit.setWordContinued(true);
        assertTrue(unit.isWordContinued());

        unit.setWordContinued(true);
        assertTrue(unit.isWordContinued());

        unit.setWordContinued(false);
        assertFalse(unit.isWordContinued());

        unit.setWordContinued(false);
        assertFalse(unit.isWordContinued());

        unit.setWordContinued(true);
        assertTrue(unit.isWordContinued());
    }

    /**
     * Test of isAbsolutePointer method, of class LinkedListTreeUnit.
     */
    @Test
    public void testIsAbsolutePointer() {
        LinkedListTreeUnit unit = new LinkedListTreeUnit();
        assertTrue(unit.isAbsolutePointer());

        unit = new LinkedListTreeUnit(2,2, false, false, 3);
        assertTrue(unit.isAbsolutePointer());
        
        unit = new LinkedListTreeUnit(2,2, true, false, 3);
        assertFalse(unit.isAbsolutePointer());
        
        unit = new LinkedListTreeUnit(2,2, false, true, 3);
        assertFalse(unit.isAbsolutePointer());

        unit = new LinkedListTreeUnit(2,2, true, true, 3);
        assertFalse(unit.isAbsolutePointer());
    }

    /**
     * Test of equals method, of class LinkedListTreeUnit.
     */
    @Test
    public void testEquals() {
        LinkedListTreeUnit unit1 = new LinkedListTreeUnit();
        LinkedListTreeUnit unit2 = new LinkedListTreeUnit();
        assertTrue(unit1.equals(unit2));

        unit1 = new LinkedListTreeUnit();
        unit2 = new LinkedListTreeUnit(0, 0, false, false, 0);
        assertTrue(unit1.equals(unit2));
        
        unit1 = new LinkedListTreeUnit();
        unit2 = new LinkedListTreeUnit(1, 0, false, false, 0);
        assertFalse(unit1.equals(unit2));

        unit1 = new LinkedListTreeUnit(0, 0, false, false, 0);
        unit2 = new LinkedListTreeUnit(1, 0, false, false, 0);
        assertFalse(unit1.equals(unit2));

        unit1 = new LinkedListTreeUnit(0, 0, false, false, 0);
        unit2 = new LinkedListTreeUnit(0, 1, false, false, 0);
        assertFalse(unit1.equals(unit2));

        unit1 = new LinkedListTreeUnit(0, 0, false, false, 0);
        unit2 = new LinkedListTreeUnit(0, 0, true, false, 0);
        assertFalse(unit1.equals(unit2));

        unit1 = new LinkedListTreeUnit(0, 0, false, false, 0);
        unit2 = new LinkedListTreeUnit(0, 0, false, true, 0);
        assertFalse(unit1.equals(unit2));

        unit1 = new LinkedListTreeUnit(0, 0, false, false, 0);
        unit2 = new LinkedListTreeUnit(0, 0, false, false, 1);
        assertTrue(unit1.equals(unit2));

        unit1 = new LinkedListTreeUnit(0, 0, false, false, 1);
        unit2 = new LinkedListTreeUnit(0, 0, false, false, 0);
        assertTrue(unit1.equals(unit2));
        
        unit1 = new LinkedListTreeUnit(0, 0, false, false, 0);
        unit2 = new LinkedListTreeUnit(1, 1, false, false, 0);
        assertFalse(unit1.equals(unit2));

        unit1 = new LinkedListTreeUnit(0, 0, true, true, 0);
        unit2 = new LinkedListTreeUnit(0, 0, true, true, 1);
        assertFalse(unit1.equals(unit2));

        unit1 = new LinkedListTreeUnit(23, 340, true, false, -2);
        unit2 = new LinkedListTreeUnit(23, 340, true, false, -2);
        assertTrue(unit1.equals(unit2));

        assertFalse(unit1.equals(null));
        assertFalse(unit1.equals("String"));
        
    }

    /**
     * Test of hashCode method, of class LinkedListTreeUnit.
     */
    @Test
    public void testHashCode() {
        LinkedListTreeUnit unit1 = new LinkedListTreeUnit();
        LinkedListTreeUnit unit2 = new LinkedListTreeUnit();
        assertEquals(unit1.hashCode(), unit2.hashCode());

        unit1 = new LinkedListTreeUnit();
        unit2 = new LinkedListTreeUnit(0, 0, false, false, 0);
        assertEquals(unit1.hashCode(), unit2.hashCode());
        
        unit1 = new LinkedListTreeUnit();
        unit2 = new LinkedListTreeUnit(1, 0, false, false, 0);
        assertNotEquals(unit1.hashCode(), unit2.hashCode());

        unit1 = new LinkedListTreeUnit(0, 0, false, false, 0);
        unit2 = new LinkedListTreeUnit(1, 0, false, false, 0);
        assertNotEquals(unit1.hashCode(), unit2.hashCode());

        unit1 = new LinkedListTreeUnit(0, 0, false, false, 0);
        unit2 = new LinkedListTreeUnit(0, 1, false, false, 0);
        assertNotEquals(unit1.hashCode(), unit2.hashCode());

        unit1 = new LinkedListTreeUnit(0, 0, false, false, 0);
        unit2 = new LinkedListTreeUnit(0, 0, true, false, 0);
        assertNotEquals(unit1.hashCode(), unit2.hashCode());

        unit1 = new LinkedListTreeUnit(0, 0, false, false, 0);
        unit2 = new LinkedListTreeUnit(0, 0, false, true, 0);
        assertNotEquals(unit1.hashCode(), unit2.hashCode());

        unit1 = new LinkedListTreeUnit(0, 0, false, false, 0);
        unit2 = new LinkedListTreeUnit(0, 0, false, false, 1);
        assertNotEquals(unit1.hashCode(), unit2.hashCode());

        unit1 = new LinkedListTreeUnit(0, 0, false, false, 0);
        unit2 = new LinkedListTreeUnit(1, 1, false, false, 0);
        assertNotEquals(unit1.hashCode(), unit2.hashCode());

        unit1 = new LinkedListTreeUnit(0, 0, true, true, 0);
        unit2 = new LinkedListTreeUnit(0, 0, true, true, 1);
        assertNotEquals(unit1.hashCode(), unit2.hashCode());

        unit1 = new LinkedListTreeUnit(23, 340, true, false, -2);
        unit2 = new LinkedListTreeUnit(23, 340, true, false, -2);
        assertEquals(unit1.hashCode(), unit2.hashCode());
    }

    /**
     * Test of compareTo method, of class LinkedListTreeUnit.
     */
    @Test
    public void testCompareTo() {
        LinkedListTreeUnit unit1 = new LinkedListTreeUnit();
        LinkedListTreeUnit unit2 = new LinkedListTreeUnit();
        assertEquals(0, unit1.compareTo(unit2));
        assertEquals(0, unit2.compareTo(unit1));
        
        unit1 = new LinkedListTreeUnit(0, 0, false, false, 0);
        unit2 = new LinkedListTreeUnit(0, 0, false, false, 0);
        assertEquals(0, unit1.compareTo(unit2));
        assertEquals(0, unit2.compareTo(unit1));

        unit1 = new LinkedListTreeUnit(0, 0, false, false, 0);
        unit2 = new LinkedListTreeUnit(1, 0, false, false, 0);
        assertTrue(unit1.compareTo(unit2) < 0);
        assertTrue(unit2.compareTo(unit1) > 0);

        unit1 = new LinkedListTreeUnit(1, 0, false, false, 0);
        unit2 = new LinkedListTreeUnit(0, 0, false, false, 0);
        assertTrue(unit1.compareTo(unit2) > 0);
        assertTrue(unit2.compareTo(unit1) < 0);

        unit1 = new LinkedListTreeUnit(0, 3, false, false, 0);
        unit2 = new LinkedListTreeUnit(1, 0, false, false, 0);
        assertTrue(unit1.compareTo(unit2) < 0);
        assertTrue(unit2.compareTo(unit1) > 0);


        unit1 = new LinkedListTreeUnit(0, 3, true, false, 0);
        unit2 = new LinkedListTreeUnit(1, 0, false, false, 0);
        assertTrue(unit1.compareTo(unit2) < 0);
        assertTrue(unit2.compareTo(unit1) > 0);
        
        unit1 = new LinkedListTreeUnit(0, 3, true, true, 0);
        unit2 = new LinkedListTreeUnit(1, 0, false, false, 0);
        assertTrue(unit1.compareTo(unit2) < 0);
        assertTrue(unit2.compareTo(unit1) > 0);

        unit1 = new LinkedListTreeUnit(0, 3, true, false, 5);
        unit2 = new LinkedListTreeUnit(1, 0, false, false, 0);
        assertTrue(unit1.compareTo(unit2) < 0);
        assertTrue(unit2.compareTo(unit1) > 0);


        unit1 = new LinkedListTreeUnit(1, 0, false, false, 0);
        unit2 = new LinkedListTreeUnit(1, 3, false, false, 0);
        assertTrue(unit1.compareTo(unit2) < 0);
        assertTrue(unit2.compareTo(unit1) > 0);

        unit1 = new LinkedListTreeUnit(1, 0, true, false, 0);
        unit2 = new LinkedListTreeUnit(1, 3, false, false, 0);
        assertTrue(unit1.compareTo(unit2) > 0);
        assertTrue(unit2.compareTo(unit1) < 0);

        unit1 = new LinkedListTreeUnit(1, 0, true, true, 0);
        unit2 = new LinkedListTreeUnit(1, 3, false, false, 0);
        assertTrue(unit1.compareTo(unit2) > 0);
        assertTrue(unit2.compareTo(unit1) < 0);

        unit1 = new LinkedListTreeUnit(1, 0, true, true, 10);
        unit2 = new LinkedListTreeUnit(1, 3, false, false, 0);
        assertTrue(unit1.compareTo(unit2) > 0);
        assertTrue(unit2.compareTo(unit1) < 0);

        unit1 = new LinkedListTreeUnit(1, 1, false, false, 0);
        unit2 = new LinkedListTreeUnit(1, 1, true, false, 0);
        assertTrue(unit1.compareTo(unit2) < 0);
        assertTrue(unit2.compareTo(unit1) > 0);

        unit1 = new LinkedListTreeUnit(1, 1, false, true, 0);
        unit2 = new LinkedListTreeUnit(1, 1, true, false, 0);
        assertTrue(unit1.compareTo(unit2) < 0);
        assertTrue(unit2.compareTo(unit1) > 0);

        unit1 = new LinkedListTreeUnit(1, 1, false, true, 10);
        unit2 = new LinkedListTreeUnit(1, 1, true, false, 0);
        assertTrue(unit1.compareTo(unit2) < 0);
        assertTrue(unit2.compareTo(unit1) > 0);
        
        unit1 = new LinkedListTreeUnit(1, 1, true, false, 0);
        unit2 = new LinkedListTreeUnit(1, 1, true, false, 0);
        assertTrue(unit1.compareTo(unit2) == 0);
        assertTrue(unit2.compareTo(unit1) == 0);

        unit1 = new LinkedListTreeUnit(1, 1, true, false, 0);
        unit2 = new LinkedListTreeUnit(1, 1, true, true, 0);
        assertTrue(unit1.compareTo(unit2) < 0);
        assertTrue(unit2.compareTo(unit1) > 0);

        unit1 = new LinkedListTreeUnit(1, 1, true, false, 10);
        unit2 = new LinkedListTreeUnit(1, 1, true, true, 0);
        assertTrue(unit1.compareTo(unit2) < 0);
        assertTrue(unit2.compareTo(unit1) > 0);

        unit1 = new LinkedListTreeUnit(1, 1, true, true, 0);
        unit2 = new LinkedListTreeUnit(1, 1, true, true, 0);
        assertTrue(unit1.compareTo(unit2) == 0);
        assertTrue(unit2.compareTo(unit1) == 0);


        unit1 = new LinkedListTreeUnit(1, 1, true, true, 0);
        unit2 = new LinkedListTreeUnit(1, 1, true, true, 10);
        assertTrue(unit1.compareTo(unit2) < 0);
        assertTrue(unit2.compareTo(unit1) > 0);

        unit1 = new LinkedListTreeUnit(1, 1, true, true, 5);
        unit2 = new LinkedListTreeUnit(1, 1, true, true, 10);
        assertTrue(unit1.compareTo(unit2) < 0);
        assertTrue(unit2.compareTo(unit1) > 0);

        unit1 = new LinkedListTreeUnit(1, 1, true, true, 20);
        unit2 = new LinkedListTreeUnit(1, 1, true, true, 10);
        assertTrue(unit1.compareTo(unit2) > 0);
        assertTrue(unit2.compareTo(unit1) < 0);

        unit1 = new LinkedListTreeUnit(1, 1, true, true, 0);
        unit2 = new LinkedListTreeUnit(1, 10, true, true, 0);
        assertTrue(unit1.compareTo(unit2) < 0);
        assertTrue(unit2.compareTo(unit1) > 0);

        unit1 = new LinkedListTreeUnit(1, 20, true, true, 0);
        unit2 = new LinkedListTreeUnit(1, 10, true, true, 0);
        assertTrue(unit1.compareTo(unit2) > 0);
        assertTrue(unit2.compareTo(unit1) < 0);
        
        assertTrue(unit1.compareTo(null) > 0);
        assertTrue(unit1.compareTo("test") > 0);
    }
    
}
