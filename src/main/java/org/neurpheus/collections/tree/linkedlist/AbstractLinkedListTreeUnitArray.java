/*
 * Neurpheus - Utilities Package
 *
 * Copyright (C) 2006-2015 Jakub Strychowski
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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neurpheus.logging.LoggerService;

/**
 * Represents an array of linked list units.
 *
 * @author Jakub Strychowski
 */
public abstract class AbstractLinkedListTreeUnitArray implements Serializable, LinkedListTreeUnitArray {

    private static final Logger LOGGER = LoggerService.getLogger(AbstractLinkedListTreeUnitArray.class);
    
    /** Unique serialization identifier of this class. */
    static final long serialVersionUID = 770608151114104256L;

    static final byte FORMAT_VERSION = 3;

    protected int[] valueMapping;
    
    transient protected Map<Integer, Integer> reverseMapping;

    protected int size;

    /** Creates a new instance of LinkedListTreeUnitArray */
    public AbstractLinkedListTreeUnitArray(int capacity) {
    }

    /** Creates a new instance of LinkedListTreeUnitArray */
    public AbstractLinkedListTreeUnitArray() {
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
        return valueMapping.length * 20 + 4;
    }

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


    public void write(DataOutputStream out) throws IOException {
        out.writeByte(FORMAT_VERSION);
        out.writeInt(size);
        out.writeInt(valueMapping.length);
        for (int v : valueMapping) {
            out.write(v);
        }
    }

    public void read(DataInputStream in) throws IOException {
        if (FORMAT_VERSION != in.readByte()) {
            throw new IOException("Invalid file format");
        }
        int newSize = in.readInt();
        clear(newSize);
        size = newSize;
        int len = in.readInt();
        valueMapping = new int[len];
        for (int i = 0; i < len; i++) {
            valueMapping[i] = in.readInt();
        }
    }

    @Override
    public void logStatistics(String name) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("  Linked list '%s' - number of units: %d ", name, size()));
            int differentUnits = LinkedListTreeUnit.getDifferentUnits(this).size();
            LOGGER.fine(String.format("  Linked list '%s' - number of unique units: %d ", name, differentUnits));
            LOGGER.fine(String.format("  Linked list '%s' - Estimated mememory consumption: %d kB", 
                name, this.getAllocationSize() / 1024));
        }
    }
    
   /**
     * Compares two units.
     * Units are ordered by their fields in the following order:
     * valueCode, wordEnd, wordContinued, distance, dataCode.
     *
     * @param index1 - position of a first unit to compare with.
     * @param index2 - position of a second unit to compare with.
     *
     * @return 0 if both units are the same, 1 if first unit is greater then second, returns -1 otherwise.
     */
    @Override
    public abstract int compareUnits(int index1, int index2);

    @Override
    public int[] getValueMapping() {
        return this.valueMapping;
    }
    
    @Override
    public void setValueMapping(int[] mapping ) {
        this.valueMapping = mapping;
        this.reverseMapping = createReverseMapping(mapping);
    }
    
    public static Map<Integer, Integer> createReverseMapping(int[] mapping) {
        Map<Integer, Integer> result = new HashMap<>();
        for (int i = 0; i < mapping.length; i++) {
            result.put(mapping[i], i);
        }
        return result;
    }

    @Override
    public int mapToValueCode(int value) {
        return reverseMapping.get(value);
    }

    
}
