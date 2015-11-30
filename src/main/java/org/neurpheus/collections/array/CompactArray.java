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
import org.neurpheus.logging.LoggerService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.logging.Logger;

/**
 * Holds an array of positive integers in a compact form.
 * <p>
 * Each value in this array is represented as a sequence of bits. The length of the sequence depends
 * on the maximum value which can be stored in an array. For example, if the maximum value is 15,
 * all values can be stored using sequences of 4 bits. In this situation, a single long integer can
 * hold 16 values. 80 values can be represented in a backing array which consists of 5 items.
 * </p>
 * <p>
 * You can use this array if standard primitive arrays like byte[], short[], int[], long[] are
 * not optimal choice because of memory consumption.
 * </p>
 * 
 * @author Jakub Strychowski
 */
public class CompactArray implements Serializable {

    private static final Logger LOGGER = LoggerService.getLogger(CompactArray.class);
    
    /** Current version of data format used while writing the array to stream. */
    static final byte FORMAT_VERSION = 2;

    /** Unique serialization identifier of this class. */
    static final long serialVersionUID = 770608150905105225L;

    /** Number of bits in a single backing array element (in long value). */
    private static final int BITS_PER_ITEM = 64;

    /**
     * A mask used for a fast calculation of value position in a backing array.
     */
    private static final int INDEX_MASK = 0x003F;

    /**
     * A shift range used for a calculation of value position in a backing array.
     */
    private static final int INDEX_SHIFT = 6;
    
    /**
     * Basic allocation for fields : array object reference, size, numberOfButs, 
     * maxValue, reallocationIncrementation.
     */
    private static final int BASIC_ALLOCATION_SIZE = 44;
    

    /** The backing array. */
    private long[] data;

    /** The number of elements in the array. */
    private int size;

    /** The number of bits which represents single value. */
    private int numberOfBits;

    /** The maximum value which is stored in the array. */
    private long maxValue;

    
    /**
     * Holds the number of items which are reserved in the data table when it is reallocated.
     * This value grows up which each reallocation. 
     */
    private transient int reallocationIncreamentation = 1;



    /**
     * Creates a new instance of CompactArray.
     */
    public CompactArray() {
        data = null;
        size = 0;
        numberOfBits = 0;
        maxValue = -1;
    }

    /**
     * Creates a new instance of CompactArray initiating backing array.
     *
     * @param capacity The number of elements for which allocate space in the array. You can add
     *                 more elements to the array but this requires additional time for backing
     *                 array reallocation.
     * @param maxV     The maximum value which will be stored in this array. You can add greated
     *                 values then this value but this requires additional time for backing array
     *                 reallocation.
     */
    public CompactArray(final int capacity, final long maxV) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity cannot be smaller then 0");
        }
        if (maxV < 0) {
            throw new IllegalArgumentException("Compact array cannot store negative values.");
        }
        if (maxV > (Long.MAX_VALUE >> 2)) {
            throw new IllegalArgumentException(
                    String.format("Max value =%d is too high - better use long[] array", maxValue));
        }
        this.size = 0;
        this.numberOfBits = determineNumberOfBits(maxV);
        this.maxValue = (1L << this.numberOfBits) - 1;
        long dataLen = 1L + (((long) capacity) * (long) this.numberOfBits / (long) BITS_PER_ITEM);
        this.data = new long[(int) dataLen];
    }

    /**
     * Calculates the number of bits required for storing specified value.
     *
     * @param value The value for which calculate the length of bits sequence.
     *
     * @return The length of a sequence which could store specified value.
     */
    public static int determineNumberOfBits(final long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Compact array cannot store negative values.");
        }
        return value == 0 ? 1 : 64 - Long.numberOfLeadingZeros(value);
    }

    /**
     * Returns the number of elements stored in this array.
     *
     * @return The array size.
     */
    public int size() {
        return this.size;
    }

    /**
     * Returns the long integer value stored at the given position in the array.
     *
     * @param index The position in the array.
     *
     * @return The value stored at the given position.
     */
    public long getLongValue(final int index) {
        if (index < 0 || index >= this.size) {
            throw new ArrayIndexOutOfBoundsException(index);
        } else {
            final long bitIndex = ((long) index) * numberOfBits;
            final int bitpos = (int) (bitIndex & INDEX_MASK);
            final int usedBits = BITS_PER_ITEM - bitpos;
            int pos = (int) (bitIndex >> INDEX_SHIFT);
            long result = (data[pos] >>> bitpos) & maxValue;
            if (usedBits < numberOfBits) {
                result |= (data[++pos] & (maxValue >> usedBits)) << usedBits;
            } 
            
            return result;
        }
    }

    /**
     * Rewrites backing array to store the given max value.
     * 
     * @param value New max value
     */
    private void newMaxValue(final long value) {
        int newNumberOfBits = determineNumberOfBits(value);
        long newMaxValue = (1L << newNumberOfBits) - 1;
        long[] newData = new long[1 + (int) (((long) this.size) * newNumberOfBits / BITS_PER_ITEM)];

        int newBitIndex = 0;
        long bitIndex = 0;
        int bitpos;
        int usedBits;
        int pos = 0;
        long posValue = size == 0 ? 0L : data[pos];
        
        int newPos = 0;
        long newValue = 0;
        long tmp;
        
        for (int i = 0; i < size; i++) {
            
            bitpos = (int) (bitIndex & INDEX_MASK);
            usedBits = BITS_PER_ITEM - bitpos;
            tmp = (posValue >>> bitpos) & maxValue;
            if (usedBits <= numberOfBits) {
                pos++;
                posValue = data[pos];
                tmp |= (posValue & (maxValue >> usedBits)) << usedBits;
            } 
            
            bitpos = newBitIndex & INDEX_MASK;
            usedBits = BITS_PER_ITEM - bitpos;
            
            newValue = (newValue & ~(newMaxValue << bitpos)) | (tmp << bitpos);
            if (usedBits <= newNumberOfBits) {
                newData[newPos++] = newValue;
                newValue = tmp >> usedBits;
            }

            newBitIndex += newNumberOfBits;
            bitIndex += numberOfBits;
        }
        newData[newPos++] = newValue;
        
        this.numberOfBits = newNumberOfBits;
        this.data = newData;
        this.maxValue = newMaxValue;
    }
    
    /**
     * Expands the backing array to store more elements.
     * 
     * @param index New index provided by the client of this class.
     */
    private void expand(int index) {
        long[] newData = new long[index + reallocationIncreamentation];
        reallocationIncreamentation *= 2;
        if (reallocationIncreamentation > 256_000) {
            reallocationIncreamentation = 256_000;
        }
        System.arraycopy(data, 0, newData, 0, data.length);
        data = newData;
    }
    
    /**
     * Sets the long integer value at the given position in the array.
     *
     * @param index The position of the element in the array.
     * @param value The value to set.
     */
    public void setLongValue(final int index, final long value) {
        if (index < 0) {
            throw new ArrayIndexOutOfBoundsException(index);
        } 
        if (value < 0) {
            throw new IllegalArgumentException(String.format(
                    "Compact array cannot store negative values. index=%d; value=%d", 
                    index, value));
        }
        if (value > maxValue) {
            newMaxValue(value);
        } 
        final long bitIndex = ((long) index) * numberOfBits;
        final int bitpos = (int) (bitIndex & INDEX_MASK);
        final long usedBits = BITS_PER_ITEM - bitpos;
        int pos = (int) (bitIndex >> INDEX_SHIFT);
        if (pos + 1 >= data.length) {
            expand(pos + 1);
        }
        data[pos] = (data[pos] & ~(maxValue << bitpos)) | (value << bitpos);
        if (usedBits < numberOfBits) {
            data[pos + 1] = (data[pos + 1] & ~(maxValue >> usedBits)) | (value >> usedBits);
        }

        if (index >= size) {
            size = index + 1;
        }
    }

    /**
     * Adds new long integer value at the end of the array.
     *
     * @param value The value to add.
     */
    public void addLongValue(final long value) {
        setLongValue(size, value);
    }

    /**
     * Adds new integer value at the end of the array.
     *
     * @param value The value to add.
     */
    public void addIntValue(final int value) {
        setLongValue(size, value);
    }

    /**
     * Returns the integer value stored at the given position in the array.
     *
     * @param index The position in the array.
     *
     * @return The value stored at the given position.
     */
    public int getIntValue(final int index) {
        return (int) getLongValue(index);
    }

    /**
     * Sets the integer value at the given position in the array.
     *
     * @param index The position of the element in the array.
     * @param value The value to set.
     */
    public void setIntValue(final int index, final int value) {
        setLongValue(index, value);
    }

    /**
     * Reduces the size of a backing array to the minimum possible size.
     */
    public void compact() {
        long bitIndex = (size() - 1) * numberOfBits;
        int pos = (int) (bitIndex >> INDEX_SHIFT);
        pos++;
        if (pos < data.length - 1) {
            long[] newData = new long[pos + 1];
            System.arraycopy(data, 0, newData, 0, pos + 1);
            data = newData;
        }
    }

    /**
     * Frees up resources occupied by this object.
     */
    public void dispose() {
        data = null;
        size = 0;
        numberOfBits = 1;
        maxValue = 1;
        reallocationIncreamentation = 1;
    }

    /**
     * Returns estimated size of memory occupied by this object.
     * 
     * @return number of bytes occupied by this object.
     */
    public long getAllocationSize() {
        long result = BASIC_ALLOCATION_SIZE;
        
        if (data != null) {
            result += data.length * 8;
        }
        
        return result;
    }

    /**
     * Writes this object into the given data output stream.
     *
     * @param out The output stream where this object should be stored.
     *
     * @throws IOException if any write error occurred.
     */
    public void write(final DataOutputStream out) throws IOException {
        compact();
        out.writeByte(FORMAT_VERSION);
        out.writeByte(numberOfBits);
        out.writeLong(maxValue);
        out.writeInt(size);
        DataOutputStreamPacker.writeArrayOfLongs(data, out);
    }

    /**
     * Reads object's data from the given data input stream.
     *
     * @param in The input stream from which this object should be read.
     *
     * @throws IOException if any read error occurred.
     */
    public void read(final DataInputStream in) throws IOException {
        if (FORMAT_VERSION != in.readByte()) {
            throw new IOException("Invalid file format");
        }
        numberOfBits = in.readByte();
        maxValue = in.readLong();
        size = in.readInt();
        data = DataOutputStreamPacker.readArrayOfLongs(in);
        reallocationIncreamentation = 1 + size / 2;
    }

    /**
     * Reads object's data from the given data input stream.
     *
     * @param in The input stream from which this object should be read.
     * @return read compact array
     *
     * @throws IOException if any read error occurred.
     */
    public static CompactArray readInstance(final DataInputStream in) throws IOException {
        CompactArray result = new CompactArray();
        result.read(in);
        return result;
    }

    /**
     * Returns all values stored in the array.
     * 
     * @return Array of values packed in this structure.
     */
    public long[] getAll() {
        long[] result = new long[this.size];
        for (int index = 0; index < size; index++) {
            final long bitIndex = ((long) index) * numberOfBits;
            final int bitpos = (int) (bitIndex & INDEX_MASK);
            final int usedBits = BITS_PER_ITEM - bitpos;
            int pos = (int) (bitIndex >> INDEX_SHIFT);
            long val = (data[pos] >>> bitpos) & maxValue;
            if (usedBits < numberOfBits) {
                val |= (data[++pos] & (maxValue >> usedBits)) << usedBits;
            } 
            result[index] = val;
        }
        return result;
    }

    /**
     * Returns maximum values stored in this array.
     * 
     * @return Maximum value already stored in this structure
     */
    public long getMaxValue() {
        return this.maxValue;
    }
    
    /**
     * Logs out statistical information for this array.
     */
    public void logStatistics(String arrayName) {
        long[] allValues = getAll();
        HashSet<Long> differentValues = new HashSet<>();
        long minValue = Long.MAX_VALUE;
        long max = 0;
        for (long val: allValues) {
            if (val < minValue) {
                minValue = val;
            }
            if (val > max) {
                max = val;
            }
            differentValues.add(val);
        }

        int bitsForDifferentValues = determineNumberOfBits(differentValues.size());
        int memoryReduction = ((this.numberOfBits - bitsForDifferentValues) * this.size 
                - differentValues.size() * determineNumberOfBits(max)) / 8;
        
        LOGGER.info(String.format("Statistics for compact array %s. size: %d; min value: %d; max value: %d; bits per value: %d; allocation: %d",
                arrayName, this.size, minValue, max, this.numberOfBits, getAllocationSize()));
        if (memoryReduction > 0) {
            LOGGER.info(String.format("    different values: %d; bits for different values: %d; possible memory reduction: %d",
                differentValues.size(), bitsForDifferentValues, memoryReduction));
        }
        
        
    }
    
}
