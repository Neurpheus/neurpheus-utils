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

import org.neurpheus.collections.array.BitsArray;
import org.neurpheus.collections.array.CompactArray;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neurpheus.logging.LoggerService;

/**
 * Represents an array of linked list units.
 *
 * @author Jakub Strychowski
 */
public class CompactLinkedListTreeUnitArray extends AbstractLinkedListTreeUnitArray implements Serializable, LinkedListTreeUnitArray {

    
    private static final Logger LOGGER = LoggerService.getLogger(FastLinkedListTreeUnitArray.class);
    
    /** Unique serialization identifier of this class. */
    static final long serialVersionUID = 770608060919195404L;

    static final byte FORMAT_VERSION = 3;

    /** Holds flags which denote if a word is continued at the given position. */
    private BitsArray wordContinued;

    /** Holds flags which denote if a word ends at the given position. */
    private BitsArray wordEnd;

    /** Holds pointers (absolute and relative) */
    private CompactArray distance;

    /** Holds codes of the values. */
    private CompactArray valueCode;

    /** Holds codes of data. */
    private CompactArray dataCode;

    private CompactArray items;

    private boolean compact;
    
    
    
    //private transient boolean[] isNullArray;

    /** Creates a new instance of LinkedListTreeUnitArray */
    public CompactLinkedListTreeUnitArray(int capacity) {
        super(capacity);
        clear(capacity);
    }

    public CompactLinkedListTreeUnitArray() {
        super();
    }
    
    /** Creates a new instance of LinkedListTreeUnitArray */
    public CompactLinkedListTreeUnitArray(LinkedListTreeUnitArray baseArray) {
        super(baseArray.size());
        this.valueMapping = baseArray.getValueMapping();
        this.reverseMapping = baseArray.getReverseValueMapping();
        clear(baseArray.size());
        this.size = baseArray.size();
        for (int i = 0; i < baseArray.size(); i++) {
            set(i, 
                baseArray.getDistance(i),
                baseArray.isWordEnd(i),
                baseArray.isWordContinued(i),
                baseArray.getValueCode(i),
                baseArray.getDataCode(i)
            );
            //add(unit);
        }
        compact();
    }

    @Override
    public void clear(int capacity) {
        wordContinued = new BitsArray(capacity);
        wordEnd = new BitsArray(capacity);
        distance = new CompactArray(capacity, 0);
        valueCode = new CompactArray(capacity, getValueMapping().length - 1);
        dataCode = new CompactArray(capacity, 0);
        items = new CompactArray();
        //isNullArray = new boolean[capacity];
        compact = false;
    }

    @Override
    public void set(final int index, final LinkedListTreeUnit unit) {
        if (compact) {
            throw new IllegalStateException("The unit array is compact now and cannot be modified");
        }
//        if (index >= isNullArray.length) {
//            isNullArray = Arrays.copyOf(isNullArray, isNullArray.length * 2);
//        }
        if (unit == null) {
            //isNullArray[index] = true;
            distance.setIntValue(index, index);
            wordEnd.set(index, false);
            wordContinued.set(index, false);
            valueCode.setIntValue(index, 0);
            dataCode.setIntValue(index, 0);
        } else {
            //isNullArray[index] = false;
            dataCode.setIntValue(index, unit.getDataCode());
            distance.setIntValue(index, unit.getDistance());
            wordEnd.set(index, unit.isWordEnd());
            wordContinued.set(index, unit.isWordContinued());
            if (unit.getValueCode() >= 0) {
                valueCode.setIntValue(index, unit.getValueCode());
            } else {
                throw new NullPointerException("index: " + index + " unit: " + unit.toString());
            }
        }
    }
    
    @Override
    public void set(final int index, final int distance,
                    final boolean wordEnd, final boolean wordContinued,
                    final int valueCode, final int dataCode) {
        if (compact) {
            throw new IllegalStateException("The unit array is compact now and cannot be modified");
        }
        //this.isNullArray[index] = false;
        this.dataCode.setIntValue(index, dataCode);
        this.distance.setIntValue(index, distance);
        this.wordEnd.set(index, wordEnd);
        this.wordContinued.set(index, wordContinued);
        this.valueCode.setIntValue(index, valueCode);
    }
    

    @Override
    public void add(final LinkedListTreeUnit unit) {
        if (compact) {
            throw new IllegalStateException("The unit array is compact now and cannot be modified");
        }
        int index = this.size;
        set(index, unit);
        this.size = index + 1;
    }

    @Override
    public int getValueCode(final int index) {
        if (compact) {
            return valueCode.getIntValue(items.getIntValue(index));
        } else {
            return valueCode.getIntValue(index);
        }
    }

    @Override
    public final int getValue(final int index) {
        if (compact) {
            return valueMapping[valueCode.getIntValue(items.getIntValue(index))];
        } else {
            return valueMapping[valueCode.getIntValue(index)];
        }
    }
    
    
//    public int getValueCodeCompact(final int index) {
//        return valueCode.getIntValue(items.getIntValue(index));
//    }

    public int getValueCodeFast(final int index) {
        return valueCode.getIntValue(index);
    }

    @Override
    public boolean isWordContinued(final int index) {
        if (compact) {
            return wordContinued.get(items.getIntValue(index));
        } else {
            return wordContinued.get(index);
        }
    }

    public boolean isWordContinuedFast(final int index) {
        return wordContinued.get(index);
    }

    @Override
    public boolean isWordEnd(final int index) {
        if (compact) {
            return wordEnd.get(items.getIntValue(index));
        } else {
            return wordEnd.get(index);
        }
    }

    public boolean isWordEndFast(final int index) {
        return wordEnd.get(index);
    }

    @Override
    public boolean isAbsolutePointer(final int index) {
        if (compact) {
            int pos = items.getIntValue(index);
            return !(wordContinued.get(pos) || wordEnd.get(pos));
        } else {
            return !(wordContinued.get(index) || wordEnd.get(index));
        }
    }

    public boolean isAbsolutePointerFast(final int index) {
        return !wordContinued.get(index) && !wordEnd.get(index);
    }

    @Override
    public int getDistance(final int index) {
        if (compact) {
            return distance.getIntValue(items.getIntValue(index));
        } else {
            return distance.getIntValue(index);
        }
    }

    public int getDistanceFast(final int index) {
        return distance.getIntValue(index);
    }

    @Override
    public int getDataCode(final int index) {
        if (compact) {
            return dataCode.getIntValue(items.getIntValue(index));
        } else {
            return dataCode.getIntValue(index);
        }
    }

    public int getDataCodeFast(final int index) {
        return dataCode.getIntValue(index);
    }

//    @Override
//    public final boolean isNull(final int index) {
//        int pos = compact ? (int) items.getLongValue(index) : index;
//        return isNullArray[pos];
//        //return !wordEnd.get(pos) && !wordContinued.get(pos) && index == (int) distance.getLongValue(pos);
//    }

    public int getFastIndex(final int index) {
        if (compact) {
            return items.getIntValue(index);
        } else {
            return index;
        }
    }
    
    @Override
    public boolean equalsUnits(final int index1, final int index2) {
        int pos1 = compact ? items.getIntValue(index1) : index1;
        int pos2 = compact ? items.getIntValue(index2) : index2;
        //return (isNullArray[pos1] && isNullArray[pos2]) ||
        return 
                (wordEnd.get(pos1) == wordEnd.get(pos2)
               && wordContinued.get(pos1) == wordContinued.get(pos2)
               && distance.getIntValue(pos1) == distance.getIntValue(pos2)
               && valueCode.getIntValue(pos1) == valueCode.getIntValue(pos2)
               && dataCode.getIntValue(pos1) == dataCode.getIntValue(pos2));
    }

    @Override
    public LinkedListTreeUnit get(final int index) {
        int pos = compact ? items.getIntValue(index) : index;
        int dist = distance.getIntValue(pos);
        boolean we = wordEnd.get(pos);
        boolean wc = wordContinued.get(pos);
        if (!we && !wc && dist == index) {
            return null;
        }
        return new LinkedListTreeUnit(
                valueCode.getIntValue(pos),
                dist,
                we,
                wc,
                dataCode.getIntValue(pos)
        );
    }
    
    public int getNumberOfDifferentUnits() {
        if (compact) {
            return valueCode.size();
        } else {
            Set set = new HashSet();
            for (int i = size() - 1; i >= 0; i--) {
                set.add(get(i));
            }
            return set.size();
        }
    }

    @Override
    public void dispose() {
        wordContinued = null;
        wordEnd = null;
        distance.dispose();
        valueCode.dispose();
        dataCode.dispose();
        distance = null;
        valueCode = null;
        dataCode = null;
    }

    public void compact() {
        if (compact) {
            return;
        }
        Set<LinkedListTreeUnit> differentUnits = LinkedListTreeUnit.getDifferentUnits(this);
        List<LinkedListTreeUnit> list = new ArrayList<>(differentUnits.size());
        list.addAll(differentUnits);
        differentUnits.clear();
        int index = 0;
        Map map = new HashMap();
        for (LinkedListTreeUnit unit : list) {
            map.put(unit, new Integer(index));
            index++;
        }
        CompactArray unitsMapping = new CompactArray(size(), list.size());
        for (int i = 0; i < size(); i++) {
            LinkedListTreeUnit unit = get(i);
            index = ((Integer) map.get(unit)).intValue();
            unitsMapping.addIntValue(index);
        }
        clear(list.size());
        index = 0;
        for (LinkedListTreeUnit unit : (ArrayList<LinkedListTreeUnit>) list) {
            set(index, unit);
            index++;
        }
        items = unitsMapping;
        items.compact();
        wordContinued.compact();
        wordEnd.compact();
        distance.compact();
        valueCode.compact();
        dataCode.compact();
        //isNullArray = null;
        compact = true;
    }

    @Override
    public long getAllocationSize() {
        return super.getAllocationSize() 
                + wordContinued.getAllocationSize()
                + wordEnd.getAllocationSize()
                + distance.getAllocationSize()
                + valueCode.getAllocationSize()
                + dataCode.getAllocationSize()
                + items.getAllocationSize();
    }

    /**
     * Reads this object data from the given input stream.
     *
     * @param in The input stream where this IPB is stored.
     *
     * @throws IOException            if any read error occurred.
     * @throws ClassNotFoundException if this object cannot be instantied.
     */
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }

    public void write(DataOutputStream out) throws IOException {
        super.write(out);
        compact();
        out.writeByte(FORMAT_VERSION);
        out.writeBoolean(compact);
        wordContinued.write(out);
        wordEnd.write(out);
        distance.write(out);
        valueCode.write(out);
        dataCode.write(out);
        items.write(out);
    }

    public void read(DataInputStream in) throws IOException {
        super.read(in);
        if (FORMAT_VERSION != in.readByte()) {
            throw new IOException("Invalid file format");
        }
        compact = in.readBoolean();
        wordContinued = BitsArray.readInstance(in);
        wordEnd = BitsArray.readInstance(in);
        distance = CompactArray.readInstance(in);
        valueCode = CompactArray.readInstance(in);
        dataCode = CompactArray.readInstance(in);
        items = CompactArray.readInstance(in);
    }

    public static CompactLinkedListTreeUnitArray readInstance(DataInputStream in) throws IOException {
        CompactLinkedListTreeUnitArray result = new CompactLinkedListTreeUnitArray();
        result.read(in);
        return result;
    }
    
    @Override
    public void logStatistics(String name) {
        super.logStatistics(name);
        if (LOGGER.isLoggable(Level.FINER)) {
            this.distance.logStatistics("llt distances");
            this.valueCode.logStatistics("llt values");
            this.dataCode.logStatistics("llt data");
            this.items.logStatistics("llt items");
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
        int res = (getValueCode(index1) << 2) + (isWordEnd(index1) ? 2 : 0) + (isWordContinued(index1) ? 1 : 0);
        res -= (getValueCode(index2) << 2) + (isWordEnd(index2) ? 2 : 0) + (isWordContinued(index2) ? 1 : 0);
        if (res == 0) {
            res = getDistance(index1) - getDistance(index2);
            if (res == 0 && isWordEnd(index1)) {
                res = getDataCode(index1) - getDataCode(index2);
            }
        }
        return res;
    }    

    @Override
    public LinkedListTreeUnitArray subArray(int startIndex, int endIndex) {
        super.subArray(startIndex, endIndex);
        FastLinkedListTreeUnitArray result = new FastLinkedListTreeUnitArray(endIndex - startIndex);
        
        int resultIndex = 0;
        for (int i = startIndex; i < endIndex; i++, resultIndex++) {
            int fastIndex = getFastIndex(i);
            result.set(resultIndex, 
                       getDistanceFast(fastIndex),
                       isWordEndFast(fastIndex),
                       isWordContinuedFast(fastIndex),
                       getValueCode(fastIndex),
                       getDataCodeFast(fastIndex)
                       );
        }
        
        return result;
    }
    

}
