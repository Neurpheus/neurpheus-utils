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

import java.util.HashSet;
import java.util.Set;

/**
 * A single element of a linked list structure representing a tree.
 * <p>
 * The Linked List Tree (LLT) consists of structural units. Symbols associated with the units
 * describe edges between nodes on different level of the tree. 
 * Each unit represents a single node, and holds pointer to a sibling node.
 * A unit contains the following information:
 * <ul>
 * <li>valueCode - a symbol describing an edge</li>
 * <li>distance - a relative pointer to a next unit describing a node having the same parent as the
 * current node; 0 - means last child of this parent</li>
 * <li>wordEnd - <code>true</code> if this unit represents an end of a path in the tree.</li>
 * <li>wordContinued - <code>true</code> if next unit in the LLT represents child node of current
 * node.</li>
 * <li>dataCode - id of data element assigned to current path in the tree.</li>
 * </ul>
 * </p>
 *
 * @author Jakub Strychowski
 */
public class LinkedListTreeUnit implements Comparable {

    
    /**
     * A symbol describing an edge (or index to this symbol).
     */
    private int valueCode;

    /**
     * Relative pointer from the current position on the LLT to a unit (node) representing a next
     * sibling node in the tree. 0 - means that a parent of current node/unit ha no more children.
     */
    private int distance;

    /**
     * <code>true</code> if this unit represents an end of a path in the tree.
     */
    private boolean wordEnd;

    /**
     * <code>true</code> if next unit in the LLT represents child node of current node.
     */
    private boolean wordContinued;

    /**
     * Identifier of a data element assigned to current path in the tree. If you need to store
     * complex objects on the tree, you should crate mapping between integer identifiers and these
     * objects. To reduce memory consumption ensure to use as small values as possible for all
     * codes.
     */
    private int dataCode;

    /**
     * Creates a new instance of LinkedListTreeUnit.
     */
    public LinkedListTreeUnit() {
        // default constructor
    }

    /**
     * Creates a new unit with the given properties.
     *
     * @param valueCode     A symbol describing an edge (or index to this symbol).
     * @param distance      Relative pointer from the current unit/node to a next sibling unit.
     * @param wordEnd       Flag - end of a path in the tree.
     * @param wordContinued Flag - next unit LLT represents child node of this unit.
     * @param dataCode      - Identifier of a data element assigned to current path in the tree.
     */
    public LinkedListTreeUnit(final int valueCode, final int distance, boolean wordEnd,
                              boolean wordContinued, int dataCode) {
        this.valueCode = valueCode;
        this.distance = distance;
        this.wordContinued = wordContinued;
        this.wordEnd = wordEnd;
        this.dataCode = dataCode;
    }

    /**
     * Returns a symbol describing an edge.
     *
     * @return Symbol in the form of an integer or index to a symbol in external mapping array.
     */
    public int getValueCode() {
        return valueCode;
    }

    /**
     * Sets a symbol describing an edge to this node/unit.
     *
     * @param newValueCode Symbol in the form of an integer or index to a symbol in external mapping
     *                     array.
     */
    public void setValueCode(int newValueCode) {
        this.valueCode = newValueCode;
    }

    /**
     * Returns relative pointer from the current unit/node to a next sibling unit.
     *
     * @return distance on the LLT from the current unit to a unit describing sibling of this node,
     *         or 0 if this node is last of its parent.
     */
    public int getDistance() {
        return distance;
    }

    /**
     * Sets relative pointer from the current unit/node to a next sibling unit.
     *
     * @param distance distance on the LLT from the current unit to a unit describing sibling of
     *                 this node, or 0 if this node is last of its parent.
     */
    public void setDistance(final int distance) {
        this.distance = distance;
    }

    /**
     * Checks if this unit contains absolute pointer instead of relative pointer.
     *
     * @return <code>true</code> if child node is pointed by absolute value on the LLT.
     */
    public boolean isAbsolutePointer() {
        return !this.wordEnd && !this.wordContinued;
    }

    /**
     * Checks if two units are the same.
     *
     * @param obj Other LLT unit.
     *
     * @return <code>true</code> if a compared unit is the same as this unit.
     */
    @Override
    public boolean equals(final Object obj) {
        return this.compareTo(obj) == 0;
    }

    /**
     * Calculates hash code of this unit.
     *
     * @return hashing value.
     */
    @Override
    public int hashCode() {
        return this.distance
                + this.valueCode
                + this.dataCode
                + (this.wordContinued ? 0x00010000 : 0)
                + (this.wordEnd ? 0x00020000 : 0);
    }

    /**
     * Returns data hold by the current node/unit or index to this data.
     *
     * @return Integer value associated with the node or index/id of this value.
     */
    public int getDataCode() {
        return this.dataCode;
    }

    /**
     * Sets data value for the current node/unit.
     *
     * @param newDataCode Integer value associated with the node or index/id of this value.
     */
    public void setDataCode(final int newDataCode) {
        this.dataCode = newDataCode;
    }

    /**
     * Checks if this unit represents the end of path in the tree.
     *
     * @return <code>true</code> if this node is one of ends of the tree - contains value.
     */
    public boolean isWordEnd() {
        return wordEnd;
    }

    /**
     * Marks this node/unit as the end of a path in the tree or not.
     *
     * @param wordEnd if <code>true</code> this node is one of ending in the tree.
     */
    public void setWordEnd(final boolean wordEnd) {
        this.wordEnd = wordEnd;
    }

    /**
     * Checks if next unit in the LLT represents child of this node.
     *
     * @return <code>true</code> if next unit in the LLT represents a child of this node.
     */
    public boolean isWordContinued() {
        return wordContinued;
    }

    /**
     * Marks this node as a node with children described by next units in the LLT.
     *
     * @param wordContinued if <code>true</code> next unit in the LLT represents a child of this
     *                      node.
     */
    public void setWordContinued(final boolean wordContinued) {
        this.wordContinued = wordContinued;
    }

    /**
     * Compares two units.
     * Units are ordered by their fields in the following order:
     * valueCode, wordEnd, wordContinued, distance, dataCode.
     *
     * @param obj LLT unit to compare with.
     *
     * @return 0 if both units are the same, 1 if current node is greater then obj, else returns -1.
     */
    @Override
    @SuppressWarnings({"squid:MethodCyclomaticComplexity"})
    public int compareTo(final Object obj) {
        if (obj != null && obj instanceof LinkedListTreeUnit) {
            final LinkedListTreeUnit unit2 = (LinkedListTreeUnit) obj;
            int res = (this.valueCode << 2) + (this.wordEnd ? 2 : 0) + (this.wordContinued ? 1 : 0);
            res -= (unit2.valueCode << 2) + (unit2.wordEnd ? 2 : 0) + (unit2.wordContinued ? 1 : 0);
            if (res == 0) {
                res = this.distance - unit2.distance;
                if (res == 0 && this.wordEnd) {
                    res = this.dataCode - unit2.dataCode;
                }
            }
            return res;
        } else {
            return 1;
        }
    }
    
    /**
     * Returns a set of unique units use by the specified unit array.
     *
     * @param unitArray The source unit array.
     * 
     * @return Collection of different units used by the specified input structure.
     */
    public static Set<LinkedListTreeUnit> getDifferentUnits(LinkedListTreeUnitArray unitArray) {
        Set<LinkedListTreeUnit> result = new HashSet<>();
        
        for (int i = unitArray.size() - 1; i >= 0; i--) {
            LinkedListTreeUnit unit = unitArray.get(i);
            result.add(unit);
        }
        return result;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{valueCode: ").append(this.valueCode).append(',');
        builder.append("wordContinued: ").append(this.wordContinued).append(',');
        builder.append("wordEnd: ").append(this.wordEnd).append(',');
        builder.append("distance: ").append(this.distance).append(',');
        builder.append("dataCode: ").append(this.dataCode).append('}');
        return builder.toString();
    }

}
