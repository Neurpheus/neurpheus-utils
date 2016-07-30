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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

/**
 * A linked list representation of a tree structure.
 *
 * <p>
 * A linked list tree consists of an array of structural elements - units. Each unit represents a
 * single node, and holds pointer to a sibling node. Successive units in the array represents
 * children of their predecessors.
 * </p>
 *
 * <p>
 * For example the following tree:
 * <pre>
 * a
 * |-b
 * |-c
 * | |-c1
 * | |-c2
 * | | |-c2.1
 * | | |-c2.2
 * | |-c3
 * |-d
 * </pre> ... will be stored by several units in the following order:
 * <pre>
 * a b[1]* c[6] c1[1] c2[3] c2.1[1] c2.2[0] c3[0] d[0]
 * </pre>
 * <i>(*value in brackets is a distances from a node to a next sibling node)</i>
 * </p>
 *
 * <p>
 * An internal structure of each unit is more complex - full information about it can be found in
 * the documentation of the {@link LinkedListTreeUnit} class.
 * </p>
 *
 * @author Jakub Strychowski
 */
public interface LinkedListTreeUnitArray extends Serializable {


    /**
     * Returns a unit at the specified position.
     *
     * @param index The position in the array.
     *
     * @return A unit from the specified position in this array.
     */
    LinkedListTreeUnit get(final int index);

    /**
     * Adds the specified unit to the end of this array.
     *
     * @param unit The unit which should be added.
     */
    void add(final LinkedListTreeUnit unit);

    /**
     * Adds all units from the specified array.
     *
     * <p>
     * This method can be used for fast merging linked list trees.
     * </p>
     *
     * @param subArray The source array which units will be added at the end of this array.
     */
    void addAll(LinkedListTreeUnitArray subArray);

    /**
     * Places the given unit a the specified position in this array.
     *
     * @param index The position in the array.
     * @param unit  The unit which should be stored in the array.
     */
    void set(final int index, final LinkedListTreeUnit unit);

    /**
     * Sets properties of a unit stored in this array.
     *
     * @param index         A unit position in the array.
     * @param distance      A distance to a next sibling node or 0 if there is no sibling nodes.
     * @param wordEnd       Informs if a node represented by the unit holds any data.
     * @param wordContinued Informs if the node has children.
     * @param valueCode     The code of a value assigned to the node.
     * @param dataCode      The code of data held by the node.
     */
    void set(final int index, final int distance, 
             final boolean wordEnd, final boolean wordContinued,
             final int valueCode, final int dataCode);

    /**
     * Returns size of this array - a number of units held by this array.
     *
     * @return Number of units held by this array.
     */
    int size();

    /**
     * Clears all internal structures and prepare theme for storing the specified number of units.
     *
     * @param capacity The number of units which will be held in the array.
     */
    void clear(int capacity);

    /**
     * Remove all references in internal structures to free up memory.
     */
    void dispose();

    /**
     * Adds the given offset to all absolute pointers used by units held by this array.
     *
     * <p>
     * This method can be used to join two or more arrays - all pointers in the added array should
     * be increased by size of previous array. similarly, when you split a unit array into
     * sub-arrays you should update their absolute pointers.
     * </p>
     *
     * @param offset The value by which increase or decrease absolute pointers.
     */
    void moveAbsolutePointers(int offset);

    /**
     * Checks if two units in this array are equal.
     *
     * <p>
     * <strong>Note:</strong> This method can be much faster then an operation of retrieval and
     * comparison of two {@link LinkedListTreeUnit} objects by the equals method.
     * </p>
     *
     * @param index1 The position of a first unit.
     * @param index2 The position of a second unit to compare with.
     *
     * @return true if the both units are equals.
     */
    boolean equalsUnits(final int index1, final int index2);

    /**
     * Compares two units. Units are ordered by their fields in the following order: valueCode,
     * wordEnd, wordContinued, distance, dataCode.
     *
     * @param index1 - position of a first unit to compare with.
     * @param index2 - position of a second unit to compare with.
     *
     * @return 0 if both units are the same, 1 if first unit is greater then second, returns -1
     *         otherwise.
     */
    int compareUnits(int index1, int index2);

    /**
     * Returns a mapping between codes and its values.
     *
     * <p>
     * See {@link mapToValueCode()} for more information.
     * </p>
     *
     * @return Array of integer where index is a code, and an element is a decoded value.
     */
    int[] getValueMapping();

    /**
     * Returns a mapping between values and their codes.
     *
     * <p>
     * See {@link mapToValueCode()} for more information.
     * </p>
     *
     * @return a map where keys are unique values describing nodes and values are codes of these
     *         values.
     */
    Map<Integer, Integer> getReverseValueMapping();

    /**
     * Sets a mapping between values and their codes.
     *
     * <p>
     * See {@link mapToValueCode()} for more information.
     * </p>
     *
     * @param mapping Array of integer where index is a code, and an element is a decoded value.
     */
    void setValueMapping(int[] mapping);

    /**
     * Returns a unique integer code for the specified value.
     *
     * <p>
     * A unit array doesn't store direct values, but each unique value is encoded into a single
     * integer value (from 1 to number of encoded values). Thank to this, the structure requires
     * less bits for a single value storage. For example, independently from a used language and
     * encoding, each character value can be stored in several bits instant of 2 or more bytes.
     * </p>
     *
     * @param value The vale to encode
     *
     * @return Unique code of the specified value.
     */
    int mapToValueCode(int value);

    /**
     * Returns a new array created from the specified fragment of this unit array.
     *
     * @param startIndex the index of the first element of a sub-array.
     * @param endIndex   the final index of the range to be copied, exclusive.
     *
     * @return Copied fragment of this array.
     */
    LinkedListTreeUnitArray subArray(int startIndex, int endIndex);

    /**
     * Returns estimated size of a memory occupied by this structure.
     *
     * <p>
     * This method can return different values from a real memory allocation. Real allocation
     * depends on a version, configuration and implementation of the JVM. Nevertheless this function
     * can be used to compare different structures and in many cases returns quite good
     * approximation.
     * </p>
     *
     * @return Estimated size of a memory allocated by whole unit array.
     */
    long getAllocationSize();

    /**
     * Logs out statistical information about this unit array.
     *
     * @param name Name or function of the unit array. This name will be displayed in the log.
     */
    void logStatistics(String name);

    /**
     * Returns an offset in the array to get a sibling of the node described by a unit stored at the
     * specified index.
     *
     * @param index Position of the unit in this array.
     *
     * @return Distance from the specified index to the sibling or 0 if there is no other sibling
     *         nodes.
     */
    int getDistance(final int index);

    /**
     * Returns an encoded value assigned to a unit at the specified position in the array.
     *
     * <p>
     * You can find more information about value encoding in documentation of the
     * {@link mapToValueCode(int)} method.
     * </p>
     *
     * @param index Position of the unit in this array.
     *
     * @return Encoded value describing a node.
     */
    int getValueCode(final int index);

    /**
     * Returns a value assigned to a unit at the specified position in the array.
     *
     * @param index Position of the unit in this array.
     *
     * @return Value describing a node.
     */
    int getValue(final int index);

    /**
     * Checks if a unit at the specified position in the array contains absolute pointer instead of
     * relative pointer.
     *
     * @param index Position of the unit in this array.
     *
     * @return true if child node is pointed by absolute value on the LLT.
     */
    boolean isAbsolutePointer(final int index);

    /**
     * Returns integer data value or index to this data for a unit at the specified position in the
     * array.
     *
     * @param index Position of the unit in this array.
     *
     * @return Integer value associated with the node or index/id of this value.
     */
    int getDataCode(final int index);

    /**
     * Checks if a unit at the specified position has children.
     *
     * @param index Position of the unit in this array.
     *
     * @return true if the unit has children - described by units at next positions in the array.
     */
    boolean isWordContinued(final int index);

    
    /**
     * Checks if there is a null unit at the specified position.
     *
     * @param index Position of the unit in this array.
     *
     * @return true if there sie an empty unit at the position.
     */
    public boolean isNull(int index);
    
    /**
     * Checks if a unit at the specified position in this array represents the end of a path in a
     * tree.
     *
     * @param index Position of the unit in this array.
     *
     * @return true if a node defined by the unit is the end of a path in a tree.
     */
    boolean isWordEnd(final int index);

    /**
     * Preprocesses index for faster reading of information from the array.
     *
     * <p>
     * Some implementations of this interface can store units in different order in the array. For
     * example {@link CompacLinkedListTreeUnitArray} can store only distinct units reducing memory
     * usage. If you need to read several properties of a single unit it is good to calculate
     * internal index once and use faster and dedicated for internal indexing methods like:
     * </p>
     * <ul>
     * <li>{@link getValueCodeFast(int)}</li>
     * <li>{@link isAbsolutePointerFast(int)}</li>
     * <li>{@link getDataCodeFast(int)}</li>
     * <li>{@link getDistanceFast(int)}</li>
     * <li>{@link isWordContinuedFast(int)}</li>
     * <li>{@link isWordEndFast(int)}</li>
     * </ul>
     *
     *
     * @param index The position of a unit in this linked list unit array.
     *
     * @return Internal position of the unit.
     */
    int getFastIndex(final int index);

    /**
     * Returns integer data value or index to this data for a unit at the specified internal
     * position in the array.
     *
     * @param internalIndex Internal position of the unit in this array.
     *
     * @return Integer value associated with the node or index/id of this value.
     */
    int getDataCodeFast(final int internalIndex);

    /**
     * Returns an encoded value assigned to a unit at the specified internal position in the array.
     *
     * <p>
     * You can find more information about value encoding in documentation of the
     * {@link mapToValueCode(int)} method.
     * </p>
     *
     * @param internalIndex Internal position of the unit in this array.
     *
     * @return Encoded value describing a node.
     */
    int getValueCodeFast(int internalIndex);

    /**
     * Returns an offset in the array to get a sibling of the node described by a unit stored at the
     * specified internal index.
     *
     * @param internalIndex Internal position of the unit in this array.
     *
     * @return Distance from the specified index to the sibling or 0 if there is no other sibling
     *         nodes.
     */
    int getDistanceFast(final int internalIndex);

    /**
     * Checks if a unit at the specified internal position in the array contains absolute pointer
     * instead of relative pointer.
     *
     * @param internalIndex Internal position of the unit in this array.
     *
     * @return true if child node is pointed by absolute value on the LLT.
     */
    boolean isAbsolutePointerFast(final int internalIndex);

    /**
     * Checks if a unit at the specified internal position has children.
     *
     * @param internalIndex Internal position of the unit in this array.
     *
     * @return true if the unit has children - described by units at next positions in the array.
     */
    boolean isWordContinuedFast(final int internalIndex);

    /**
     * Checks if a unit at the specified internal position in this array represents the end of a
     * path in a tree.
     *
     * @param internalIndex Internal position of the unit in this array.
     *
     * @return true if a node defined by the unit is the end of a path in a tree.
     */
    boolean isWordEndFast(final int internalIndex);

    /**
     * Writes this array and all its elements to the specified data stream.
     *
     * @param out Data output stream.
     *
     * @throws IOException if an I/O error occurs.
     */
    void write(DataOutputStream out) throws IOException;

    /**
     * Reads a content of this array from the specified data stream.
     *
     * @param in Data input stream
     *
     * @throws IOException if an I/O error occurs.
     */
    void read(DataInputStream in) throws IOException;

    /**
     * Reduces the size of a backing structure to the minimum possible size.
     */
    void trimToSize();
    
    
    /**
     * Returns a string representation created from the specified fragment 
     * of this unit array.
     *
     * @param startIndex the index of the first element of a sub-array.
     * @param endIndex   the index of the last element of a sub-array, exclusive.
     *
     * @return Fragment of this array as a string.
     */
    String toString(int startIndex, int endIndex);
    
}
