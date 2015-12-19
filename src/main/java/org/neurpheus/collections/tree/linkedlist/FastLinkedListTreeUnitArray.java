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
import java.util.Arrays;
import java.util.logging.Logger;
import org.neurpheus.logging.LoggerService;

/**
 * Represents an array of linked list units.
 *
 * @author Jakub Strychowski
 */
public class FastLinkedListTreeUnitArray extends AbstractLinkedListTreeUnitArray implements Serializable, LinkedListTreeUnitArray {

    private static final Logger LOGGER = LoggerService.getLogger(FastLinkedListTreeUnitArray.class);
    
    /** Unique serialization identifier of this class. */
    static final long serialVersionUID = 770608151108131632L;

    static final byte FORMAT_VERSION = 3;

    /** Holds flags which denote if a word is continued at the given position. */
    private boolean[] wordContinued;

    /** Holds flags which denote if a word ends at the given position. */
    private boolean[] wordEnd;

    /** Holds pointers (absolute and relative) */
    private int[] distance;

    /** Holds codes of the values. */
    private int[] valueCode;

    /** Holds codes of data. */
    private int[] dataCode;
    


    /** Creates a new instance of LinkedListTreeUnitArray */
    public FastLinkedListTreeUnitArray(int capacity) {
        super(capacity);
        clear(capacity);
    }

    /** Creates a new instance of LinkedListTreeUnitArray */
    public FastLinkedListTreeUnitArray() {
        super();
    }

    /** Creates a new instance of LinkedListTreeUnitArray */
    public FastLinkedListTreeUnitArray(LinkedListTreeUnitArray baseArray) {
        if (baseArray instanceof FastLinkedListTreeUnitArray) {
            FastLinkedListTreeUnitArray source = (FastLinkedListTreeUnitArray) baseArray;
            this.size = source.size();
            this.valueCode = Arrays.copyOf(source.valueCode, size);
            this.distance = Arrays.copyOf(source.distance, size);
            this.dataCode = Arrays.copyOf(source.dataCode, size);
            this.wordEnd = Arrays.copyOf(source.wordEnd, size);
            this.wordContinued = Arrays.copyOf(source.wordContinued, size);
        } else {
            this.size = baseArray.size();
            this.valueCode = new int[size];
            this.distance = new int[size];
            this.dataCode = new int[size];
            this.wordEnd = new boolean[size];
            this.wordContinued = new boolean[size];
            for (int i = 0; i < size; i++) {
                this.valueCode[i] = baseArray.getValueCode(i);
                this.distance[i] = baseArray.getDistance(i);
                this.dataCode[i] = baseArray.getDataCode(i);
                this.wordEnd[i] = baseArray.isWordEnd(i);
                this.wordContinued[i] = baseArray.isWordContinued(i);
            }
        }
        this.valueMapping = baseArray.getValueMapping();
        this.reverseMapping = baseArray.getReverseValueMapping();
        
    }
    
    @Override
    public void clear(int capacity) {
        wordContinued = new boolean[capacity];
        wordEnd = new boolean[capacity];
        distance = new int[capacity];
        valueCode = new int[capacity];
        dataCode = new int[capacity];
        //isNullArray = new boolean[capacity];
        size = 0;
    }

    @Override
    public void set(final int index, final LinkedListTreeUnit unit) {
        if (index >= this.valueCode.length) {
            int newSize = 100 + (int) (index * 1.3f);
            LOGGER.finest("Expanding capacity of linked list unit array to " + newSize);
            //isNullArray = Arrays.copyOf(isNullArray, newSize);
            wordContinued = Arrays.copyOf(wordContinued, newSize);
            wordEnd = Arrays.copyOf(wordEnd, newSize);
            distance = Arrays.copyOf(distance, newSize);
            valueCode = Arrays.copyOf(valueCode, newSize);
            dataCode = Arrays.copyOf(dataCode, newSize);
        }
        if (unit == null) {
            //isNullArray[index] = true;
            distance[index] = index;
            wordEnd[index] = false;
            wordContinued[index] = false;
            valueCode[index] = 0;
            dataCode[index] = 0;
        } else {
            //isNullArray[index] = false;
            dataCode[index] = unit.getDataCode();
            distance[index] = unit.getDistance();
            wordEnd[index] = unit.isWordEnd();
            wordContinued[index] = unit.isWordContinued();
            valueCode[index] = unit.getValueCode();
        }
        if (index >= size) {
            size = index + 1;
        }
    }
    
    @Override
    public void set(final int index, final int distance,
                    final boolean wordEnd, final boolean wordContinued,
                    final int valueCode, final int dataCode) {
        this.dataCode[index] = dataCode;
        this.distance[index] = distance;
        this.wordEnd[index] = wordEnd;
        this.wordContinued[index] = wordContinued;
        this.valueCode[index] = valueCode;
        //this.isNullArray[index] = index == distance && !wordContinued && !wordEnd;
    }
    

    @Override
    public final void add(final LinkedListTreeUnit unit) {
        set(size, unit);
    }

    @Override
    public final int getValueCode(final int index) {
        return valueCode[index];
    }

    @Override
    public final int getValueCodeFast(final int index) {
        return valueCode[index];
    }
    
    @Override
    public final int getValue(final int index) {
        return valueMapping[valueCode[index]];
    }

    
    @Override
    public final boolean isWordContinued(final int index) {
        return wordContinued[index];
    }

    @Override
    public final boolean isWordEnd(final int index) {
        return wordEnd[index];
    }

    @Override
    public final boolean isAbsolutePointer(final int index) {
        return !wordContinued[index] && !wordEnd[index];
    }

    @Override
    public final int getDistance(final int index) {
        return distance[index];
    }

    @Override
    public final int getDataCode(final int index) {
        return dataCode[index];
    }

//    @Override
//    public final boolean isNull(final int index) {
//        return isNullArray[index];
//    }

    @Override
    public final boolean equalsUnits(final int index1, final int index2) {
//        return (isNullArray[index1] && isNullArray[index2]) ||
        return 
                (wordEnd[index1] == wordEnd[index2]
               && wordContinued[index1] == wordContinued[index2]
               && distance[index1] == distance[index2]
               && valueCode[index1] == valueCode[index2]
               && dataCode[index1] == dataCode[index2]);
    }

    @Override
    public final LinkedListTreeUnit get(final int index) {
//        if (isNullArray[index]) {
//            return null;
//        }
        return new LinkedListTreeUnit(valueCode[index], distance[index], wordEnd[index], 
                wordContinued[index],dataCode[index]
        );
    }
    
    

    @Override
    public void dispose() {
        super.dispose();
        wordContinued = null;
        wordEnd = null;
        distance = null;
        valueCode = null;
        dataCode = null;
    }


    @Override
    public long getAllocationSize() {
        long result = super.getAllocationSize();
        wordContinued = Arrays.copyOf(wordContinued, size);
        wordEnd = Arrays.copyOf(wordEnd, size);
        distance = Arrays.copyOf(distance, size);
        valueCode = Arrays.copyOf(valueCode, size);
        dataCode = Arrays.copyOf(dataCode, size);
        
        result += 4 + 5 * 20 + wordContinued.length + wordEnd.length
                + distance.length * 4
                + valueCode.length * 4
                + dataCode.length * 4;

        return result;
    }

    @Override
    public final int getFastIndex(int index) {
        return index;
    }

    @Override
    public final boolean isAbsolutePointerFast(int index) {
        return !wordContinued[index] && !wordEnd[index];
    }

    @Override
    public final boolean isWordContinuedFast(int index) {
        return wordContinued[index];
    }

    @Override
    public final boolean isWordEndFast(int index) {
        return wordEnd[index];
    }

    @Override
    public final int getDataCodeFast(int index) {
        return dataCode[index];
    }

    @Override
    public final int getDistanceFast(int index) {
        return distance[index];
    }

//    @Override
//    public final int getValueCodeFast(int index) {
//        return valueCode[index];
//    }

    public void write(DataOutputStream out) throws IOException {
        super.write(out);
        out.writeByte(FORMAT_VERSION);
        out.writeInt(size);
        for (int i = 0; i < size; i++) {
            out.writeBoolean(wordContinued[i]);
            out.writeBoolean(wordEnd[i]);
            out.writeInt(distance[i]);
            out.writeInt(valueCode[i]);
            out.writeInt(dataCode[i]);
        }
    }

    public void read(DataInputStream in) throws IOException {
        super.read(in);
        if (FORMAT_VERSION != in.readByte()) {
            throw new IOException("Invalid file format");
        }
        int newSize = in.readInt();
        clear(newSize);
        size = newSize;
        for (int i = 0; i < size; i++) {
            wordContinued[i] = in.readBoolean();
            wordEnd[i] = in.readBoolean();
            distance[i] = in.readInt();
            valueCode[i] = in.readInt();
            dataCode[i] = in.readInt();
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
    public int compareUnits(int index1, int index2) {
        int res = (valueCode[index1] << 2) + (wordEnd[index1] ? 2 : 0) + (wordContinued[index1] ? 1 : 0);
        res -= (valueCode[index2] << 2) + (wordEnd[index2] ? 2 : 0) + (wordContinued[index2] ? 1 : 0);
        if (res == 0) {
            res = distance[index1] - distance[index2];
            if (res == 0 && wordEnd[index1]) {
                res = dataCode[index1] - dataCode[index2];
            }
        }
        return res;
    }    
    
    @Override
    public LinkedListTreeUnitArray subArray(int startIndex, int endIndex) {
        super.subArray(startIndex, endIndex);
        FastLinkedListTreeUnitArray result = new FastLinkedListTreeUnitArray(endIndex - startIndex);
        result.valueMapping = this.valueMapping;
        result.reverseMapping = this.reverseMapping;
        result.wordContinued = Arrays.copyOfRange(wordContinued, startIndex, endIndex);
        result.wordEnd = Arrays.copyOfRange(wordEnd, startIndex, endIndex);
        result.distance = Arrays.copyOfRange(distance, startIndex, endIndex);
        result.valueCode = Arrays.copyOfRange(valueCode, startIndex, endIndex);
        result.dataCode = Arrays.copyOfRange(dataCode, startIndex, endIndex);
        result.size = endIndex - startIndex;
        return result;
    }

    
    

    
}
