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

package org.neurpheus.collections.array;

import org.neurpheus.core.io.DataOutputStreamPacker;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;



/**
 * Array holding binary elements in a compact form to reduce memory consumption.
 * <p>
 * Java provides a boolean[] structure to hold an array of logical elements.
 * Unfortunately this structure consumes about 1 byte per each bit of information.
 * This class reduces memory consumption holding logical values on a single bits 
 * backed by a long[] array. You can easy set and get a single binary value providing its
 * index in this binary array.
 * </p>
 * <p>
 * This class is another implementation of a standard java.util.BitSet class adding some
 * features:
 * <ul>
 * <li>You can optimize memory consumed by the array calling {@see compact()} method 
 * after setting up values in the array.</li>
 * <li>Object can be serialized to a stream in a compacted form</li>
 * </ul>
 * </p>
 * 
 * @author Jakub Strychowski
 */
public class BitsArray implements Serializable {

    /** Unique serialization identifier of this class. */
    static final long serialVersionUID = 770608061016143242L;

    /** Backing array - each element holds 64 bits. **/
    private long[] data;

    /** Size of the array - number of bits actually stored in the array. */
    private int size;

    /**
     * Creates a new, empty array of bits.
     */
    public BitsArray() {
        data = new long[1];
        size = 0;
    }

    /**
     * Create a new array holding given number of bits defined in the given backing array.
     * 
     * @param size Number of bits in the array.
     * @param backingArray Initial values of the array.
     */
    public BitsArray(final int size, final long[] backingArray) {
        data = backingArray;
        this.size = size;
        if ((size >> 6) > data.length) {
            int allocsize = 1 + ((size - 1) >> 6);
            long[] newData = new long[allocsize];
            System.arraycopy(data, 0, newData, 0, data.length);
            data = newData;
        }
    }

    /** 
     * Creates a new instance of BitsArray with the given capacity.
     * 
     * @param capacity number of bits for which this structure will reserve memory. 
     */
    public BitsArray(final int capacity) {
        int allocSize = 1 + ((capacity - 1) >> 6);
        data = new long[allocSize];
        size = 0;
    }

    /**
     * Returns size/length of the array.
     * 
     * @return number of bits stored in the array.
     */
    public int size() {
        return size;
    }

    /**
     * Sets a single bit value at the given position in the array.
     * This method automatically expands the size of a backing array if needed to store value 
     * at the given position. To trim the backing array for efficient storage you should 
     * call the {@see compact()} method after setting up all values in the array.
     * 
     * @param index position in the array where a bit of information should be stored.
     * @param value binary value to store
     * 
     */
    public void set(final int index, boolean value) {
        if (index < 0) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        final int backingIndex = index >> 6;
        if (backingIndex >= data.length) {
            long[] newData = new long[2 * backingIndex];
            System.arraycopy(data, 0, newData, 0, data.length);
            data = newData;
        }
        if (index >= size) {
            size = index + 1;
        }
        if (value) {
            data[backingIndex] |= (1L << (index & 0x3F));
        } else {
            data[backingIndex] &= ~(1L << (index & 0x3F));
        }
    }
    
    /**
     * Reduces memory occupied by this array eliminating 
     * unused area at the end of the backing array.
     */
    public void compact() {
        int allocsize = 1 + ((size - 1) >> 6);
        if (data.length != allocsize) {
            long[] newData = new long[allocsize];
            System.arraycopy(data, 0, newData, 0, Math.min(newData.length, data.length));
            data = newData;
        }
    }

    /**
     * Returns a binary value stored at the given position in the array.
     * 
     * @param index the position in the array.
     * 
     * @return binary value stored in the array. 
     */
    public final boolean get(final int index) {
        if (index < 0 || index >= size) {
            throw new ArrayIndexOutOfBoundsException(index);
        } 
        return (index < size) && ((data[index >> 6] & (1L << (index & 0x3F))) != 0);
    }

    /**
     * Returns backing array where all bits are actually stored.
     * 
     * @return the backing array - bits are stored in long primitive values.
     */
    protected long[] getBackingArray() {
        return data;
    }

    /**
     * Returns estimated size of a memory occupied by this structure.
     * 
     * @return Number of bytes occupied by this structure.
     */
    public long getAllocationSize() {
        return 8 + (data == null ? 0 : 8 + data.length * 8);
    }

    /**
     * Stores this array in the given data output stream.
     * 
     * @param out the stream where this array should be stored.
     * 
     * @throws IOException if writing isn't possible.
     */
    
    public void write(DataOutputStream out) throws IOException {
        compact();
        DataOutputStreamPacker.writeInt(size, out);
        DataOutputStreamPacker.writeArrayOfLongs(data, out);
    }

    /**
     * Reads data of this array from the given input stream holding data values.
     * 
     * @param in the input stream where an array of bits has been serialized.
     * 
     * @throws IOException if reading isn't possible.
     */
    public void read(DataInputStream in) throws IOException {
        size = DataOutputStreamPacker.readInt(in);
        data = DataOutputStreamPacker.readArrayOfLongs(in);
    }

    /**
     * Creates a new instance of this class reading data from the given input stream.
     * 
     * @param in the input stream were an array of bits has been serialized.
     * 
     * @return New, compact bits array.
     * 
     * @throws IOException if reading isn't possible.
     */
    public static BitsArray readInstance(DataInputStream in) throws IOException {
        int newSize = DataOutputStreamPacker.readInt(in);
        long[] backingArray = DataOutputStreamPacker.readArrayOfLongs(in);
        return new BitsArray(newSize, backingArray);
    }

}
