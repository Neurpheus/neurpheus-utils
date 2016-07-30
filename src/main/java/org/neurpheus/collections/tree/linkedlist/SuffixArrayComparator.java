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

import java.util.Comparator;

/**
 * A comparator used for sorting {@link LinkedListTreeUnitArray} 
 * by common endings of unit sequences in the array (suffixes). 
 * <p>
 * A sorted index of suffixes may be used for a faster compression with the
 * LZTrie algorithm.
 * </p>
 */
public class SuffixArrayComparator implements Comparator {

    /** An array of units describing nodes and edges in a LLTree. */
    private final LinkedListTreeUnitArray unitsArray;

    /** Holds the maximum index in a unit array. */
    private final int maxPos;

    /**
     * If <code>true</code> substrings should be compared also according to their positions in a
     * unit array.
     */
    private final boolean comparePositions;

    /**
     * Constructs a new instance of this comparator.
     *
     * @param units       An array of units constructing a linked list tree.
     * @param byPositions If <code>true</code> substrings should be compared also according to their
     *                    positions in the unit array.
     */
    public SuffixArrayComparator(final LinkedListTreeUnitArray units, final boolean byPositions) {
        this.unitsArray = units;
        this.maxPos = units.size() - 1;
        this.comparePositions = byPositions;
    }

    /**
     * Compares two unit sequences pointed by the specified two positions in a unit array.
     *
     * @param sequence1 the position of a first unit sequence.
     * @param sequence2 the position of a second unit sequence.
     *
     * @return a negative integer, zero, or a positive integer as the first sequence of units is
     *         less than, equal to, or greater than the second sequence of units.
     */
    @Override
    public int compare(Object sequence1, Object sequence2) {
        int position1 = (Integer) sequence1;
        int position2 = (Integer) sequence2;
        return compare(position1, position2);
    }

    /**
     * Compares two unit sequences pointed by the specified two positions in a unit array.
     *
     * @param position1 The position of a first unit sequence.
     * @param position2 The position of a second unit sequence.
     *
     * @return a negative integer, zero, or a positive integer as the first argument is less than,
     *         equal to, or greater than the second.
     */
    public int compare(int position1, int position2) {
        if (position1 >= maxPos) {
            // Last unit goes to the end
            return 1;
        } else if (position2 >= maxPos) {
            // Last unit goes to the end
            return -1;
        } else {
            int res = unitsArray.compareUnits(position1, position2);
            if (res == 0) {
                // sequences are equals at the first position
                res = unitsArray.compareUnits(position1 + 1, position2 + 1);

                // If units are equal and the comparePosition flag is on, compare positions 
                // of units, otherwise return the result of comparision of units at second position.
                if (res == 0 && comparePositions) {
                    res = position1 < position2 ? -1 : 1;
                }
            }
            return res;
        }
    }

}
