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

import org.neurpheus.logging.LoggerService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * An abstract implementation of the linked list tree unit array providing some basic structures and
 * functionalities.
 *
 * @author Jakub Strychowski
 */
public abstract class AbstractLinkedListTreeUnitArray
        implements Serializable, LinkedListTreeUnitArray {

    /** private logger for this class. */
    private static final Logger LOGGER = LoggerService.getLogger(
            AbstractLinkedListTreeUnitArray.class);

    /** Unique serialization identifier of this class. */
    static final long serialVersionUID = 770608151114104256L;

    /** Version number which can be used to detect formating while reading data from the stream. */
    static final byte FORMAT_VERSION = 3;

    /** Number of units in the array. */
    protected int size;

    /** Mapping between value codes and real values describing nodes in a tree. */
    protected int[] valueMapping;

    /** Reverse mapping used to speed up processing while a tree compression process. */
    protected transient Map<Integer, Integer> reverseMapping;

    @Override
    public int size() {
        return size;
    }

    @Override
    public void dispose() {
        valueMapping = null;
        reverseMapping = null;
        size = 0;
    }

    @Override
    public long getAllocationSize() {
        long result = 4L + 4 + 4; // fields
        result += 16; // this object header
        if (valueMapping != null) {
            result += 16 + valueMapping.length * 4L;
        }
        if (reverseMapping != null) {
            result += 16 + 32;
            result += reverseMapping.size() * (32 + 4);
        }
        return result;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeByte(FORMAT_VERSION);
        out.writeInt(size);
        if (valueMapping == null) {
            out.writeInt(0);
        } else {
            out.writeInt(valueMapping.length);
            for (int v : valueMapping) {
                out.writeInt(v);
            }
        }
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        if (FORMAT_VERSION != in.readByte()) {
            throw new IOException("Invalid file format");
        }
        int newSize = in.readInt();
        clear(newSize);
        size = newSize;
        int len = in.readInt();
        reverseMapping = null;
        valueMapping = null;
        if (len != 0) {
            valueMapping = new int[len];
            for (int i = 0; i < len; i++) {
                valueMapping[i] = in.readInt();
            }
            reverseMapping = createReverseMapping(valueMapping);
        }
    }

    @Override
    public void logStatistics(String name) {
        LOGGER.info(String.format("  Linked list '%s' - number of units: %d ", name, size()));
        int differentUnits = LinkedListTreeUnit.getDifferentUnits(this).size();
        LOGGER.info(String.format("  Linked list '%s' - number of unique units: %d ", name,
                                  differentUnits));
        LOGGER.info(String.format("  Linked list '%s' - Estimated mememory consumption: %d kB",
                                  name, this.getAllocationSize() / 1024));
    }

    @Override
    public int[] getValueMapping() {
        return this.valueMapping;
    }

    @Override
    public Map<Integer, Integer> getReverseValueMapping() {
        return this.reverseMapping;
    }

    @Override
    public void setValueMapping(int[] mapping) {
        this.valueMapping = mapping;
        this.reverseMapping = null;
        if (mapping != null) {
            this.reverseMapping = createReverseMapping(mapping);
        }
    }

    /**
     * Creates a mapping between values describing nodes and value codes.
     *
     * @param mapping Mapping from value codes to corresponding values.
     *
     * @return A new mapping from values describing nodes and value codes.
     */
    protected static Map<Integer, Integer> createReverseMapping(int[] mapping) {
        Map<Integer, Integer> result = new HashMap<>(mapping.length + 1, 0.99f);
        for (int i = 0; i < mapping.length; i++) {
            result.put(mapping[i], i);
        }
        return result;
    }

    @Override
    public int mapToValueCode(int value) {
        Integer result = reverseMapping.get(value);
        return result == null ? 0 : result;
    }

    /**
     * Returns a new array created from the specified fragment of this unit array.
     * <p>
     * This method in only checks input arguments and calls abstract method
     * {@link subArrayArgumentsVerified} which should by implemented by subclasses of this abstarct
     * class.
     * </p>
     *
     * @param startIndex the index of the first element of a sub-array.
     * @param endIndex   the final index of the range to be copied, exclusive.
     *
     * @return Copied fragment of this array.
     */
    @Override
    public LinkedListTreeUnitArray subArray(int startIndex, int endIndex) {
        if (startIndex < 0 || startIndex > this.size) {
            throw new IndexOutOfBoundsException("Invalid startIndex = " + startIndex);
        }
        if (endIndex < 0 || endIndex > this.size || endIndex <= startIndex) {
            throw new IndexOutOfBoundsException("Invalid endIndex = " + endIndex);
        }
        AbstractLinkedListTreeUnitArray result = subArrayArgumentsVerified(startIndex, endIndex);
        result.valueMapping = valueMapping;
        result.reverseMapping = reverseMapping;
        return result;
    }

    /**
     * Returns a new array created from the specified fragment of this unit array.
     * <p>
     * This method should be called with valid input arguments which are checked by the
     * {@link subArray(int, int)} method.
     * </p>
     *
     * @param startIndex the index of the first element of a sub-array.
     * @param endIndex   the final index of the range to be copied, exclusive.
     *
     * @return Copied fragment of this array.
     */
    protected abstract AbstractLinkedListTreeUnitArray subArrayArgumentsVerified(
            int startIndex, int endIndex);

    @Override
    public void moveAbsolutePointers(int offset) {
        for (int i = 0; i < this.size; i++) {
            if (isAbsolutePointer(i)) {
                set(i, getDistance(i) + offset, isWordEnd(i),
                    isWordContinued(i), getValueCode(i), getDataCode(i));
            }
        }
    }

    @Override
    public void addAll(LinkedListTreeUnitArray subArray) {
        for (int i = 0; i < subArray.size(); i++) {
            add(subArray.get(i));
        }
    }
    
    @Override
    public String toString(int startIndex, int endIndex) {
        if (startIndex < 0 || startIndex > this.size) {
            throw new IndexOutOfBoundsException("Invalid startIndex = " + startIndex);
        }
        if (endIndex < 0 || endIndex > this.size || endIndex <= startIndex) {
            throw new IndexOutOfBoundsException("Invalid endIndex = " + endIndex);
        }
        StringBuilder builder = new StringBuilder((endIndex - startIndex) * 100);
        for (int i = startIndex; i < endIndex; i++) {
            LinkedListTreeUnit unit = get(i);
            builder.append(unit.toString());
            builder.append("\n");
        }
        return builder.toString();
    }
    

    @Override
    public abstract void set(final int index, final LinkedListTreeUnit unit);

    @Override
    public abstract void set(final int index, final int distance,
                             final boolean wordEnd, final boolean wordContinued,
                             final int valueCode, final int dataCode);

    @Override
    public abstract void add(final LinkedListTreeUnit unit);

    @Override
    public abstract int getValueCode(final int index);

    @Override
    public abstract int getValue(final int index);

    @Override
    public abstract boolean isWordContinued(final int index);

    @Override
    public abstract boolean isWordEnd(final int index);

    @Override
    public abstract boolean isAbsolutePointer(final int index);

    @Override
    public abstract int getDistance(final int index);

    @Override
    public abstract int getDataCode(final int index);

    @Override
    public abstract boolean equalsUnits(final int index1, final int index2);

    @Override
    public abstract LinkedListTreeUnit get(final int index);

    @Override
    public abstract int getFastIndex(int index);

    @Override
    public abstract boolean isAbsolutePointerFast(int index);

    @Override
    public abstract boolean isWordContinuedFast(int index);

    @Override
    public abstract boolean isWordEndFast(int index);

    @Override
    public abstract int getDataCodeFast(int index);

    @Override
    public abstract int getDistanceFast(int index);

    @Override
    public abstract int compareUnits(int index1, int index2);
    
    @Override
    public abstract boolean isNull(int index);
    

}
