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

import org.neurpheus.collections.array.BitsArray;
import org.neurpheus.collections.array.CompactArray;
import org.neurpheus.logging.LoggerService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents an array of linked list units in a compact form.
 * 
 * <p>
 * In this implementation all properties of LLT units are stored as bit-aligned values.
 * <br>
 * Compact LLT unit array works in two modes:
 * <ul>
 * <li>creation mode - in this mode you can add or set units in the array.</li>
 * <li>compact mode - readonly mode in which the structure holds only unique units and references to
 * these units. This approach can reduce memory consumption several times.</li>
 * </ul>
 * In both modes integer values are stored in {@link CompactArray} or {link BitArray} collections.
 * </p>
 *
 * @author Jakub Strychowski
 */
public final class CompactLinkedListTreeUnitArray extends AbstractLinkedListTreeUnitArray implements
        Serializable, LinkedListTreeUnitArray {

    
    /** Logger for this class. */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    private static final Logger LOGGER = LoggerService.getLogger(
            CompactLinkedListTreeUnitArray.class);

    /** Message of exception thrown when somebody tries to modify array i compact mode. */
    private static final String ILLEGAL_STATE_EXCEPTION_MESSAGE
            = "The unit array is compact now and cannot be modified";

    /** Unique serialization identifier of this class. */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    static final long serialVersionUID = 770608060919195404L;

    static final byte COMPACT_FORMAT_VERSION = 3;

    /** Estimated memory occupied by internal objects of this objects. */
    public static final int BASE_ALLOCATION_SIZE = 6 * 4 + 7;

    /** Holds flags which denote if a word is continued at the given position. */
    private BitsArray wordContinued;

    /** Holds flags which denote if a word ends at the given position. */
    private BitsArray wordEnd;

    /** Holds pointers (absolute and relative). */
    private CompactArray distance;

    /** Holds codes of the values. */
    private CompactArray valueCode;

    /** Holds codes of data. */
    private CompactArray dataCode;

    private CompactArray items;

    private boolean compact;

    /**
     * Creates a new instances of the compact LLT unit array.
     *
     * @param capacity The initial storage size.
     */
    public CompactLinkedListTreeUnitArray(final int capacity) {
        ensureCapacity(capacity);
    }

    /**
     * Creates a new instance of the compact LLT unit array.
     */
    public CompactLinkedListTreeUnitArray() {
        ensureCapacity(100);
    }

    /**
     * Creates a new instance of the compact LLT unit array coping content of the specified base
     * structure.
     *
     * @param baseArray An array from which all units will be added to the newly created array.
     */
    public CompactLinkedListTreeUnitArray(final LinkedListTreeUnitArray baseArray) {
        this.valueMapping = baseArray.getValueMapping();
        this.reverseMapping = baseArray.getReverseValueMapping();
        ensureCapacity(baseArray.size());
        this.size = baseArray.size();
        for (int i = 0; i < baseArray.size(); i++) {
            set(i,
                baseArray.getDistance(i),
                baseArray.isWordEnd(i),
                baseArray.isWordContinued(i),
                baseArray.getValueCode(i),
                baseArray.getDataCode(i)
            );
        }
        compact();
    }

    /**
     * Prepares internal structures to store the specified number of units.
     *
     * @param capacity Estimated number of units which will be stored in this structure.
     */
    protected void ensureCapacity(final int capacity) {
        if (compact) {
            throw new IllegalStateException(ILLEGAL_STATE_EXCEPTION_MESSAGE);
        }
        wordContinued = new BitsArray(capacity);
        wordEnd = new BitsArray(capacity);
        distance = new CompactArray(capacity, 0);
        valueCode = new CompactArray(capacity,
                                     valueMapping == null ? 0 : getValueMapping().length - 1);
        dataCode = new CompactArray(capacity, 0);
        items = null;
        compact = false;
    }

    @Override
    public void clear(int capacity) {
        ensureCapacity(capacity);
        compact = false;
        this.size = 0;
    }

    @Override
    public void set(final int index, final LinkedListTreeUnit unit) {
        if (compact) {
            throw new IllegalStateException(ILLEGAL_STATE_EXCEPTION_MESSAGE);
        }
        if (unit == null) {
            distance.setIntValue(index, index);
            wordEnd.set(index, false);
            wordContinued.set(index, false);
            valueCode.setIntValue(index, 0);
            dataCode.setIntValue(index, 0);
        } else {
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
            throw new IllegalStateException(ILLEGAL_STATE_EXCEPTION_MESSAGE);
        }
        this.dataCode.setIntValue(index, dataCode);
        this.distance.setIntValue(index, distance);
        this.wordEnd.set(index, wordEnd);
        this.wordContinued.set(index, wordContinued);
        this.valueCode.setIntValue(index, valueCode);
    }

    @Override
    public void add(final LinkedListTreeUnit unit) {
        if (compact) {
            throw new IllegalStateException(ILLEGAL_STATE_EXCEPTION_MESSAGE);
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

    @Override
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

    @Override
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

    @Override
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

    @Override
    public boolean isAbsolutePointerFast(final int index) {
        return !wordContinued.get(index) && !wordEnd.get(index);
    }

    @Override
    public int getDistance(final int index) {
        return distance.getIntValue(compact ? items.getIntValue(index) : index);
    }

    @Override
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

    @Override
    public int getDataCodeFast(final int index) {
        return dataCode.getIntValue(index);
    }

    @Override
    public int getFastIndex(final int index) {
        if (compact) {
            return items.getIntValue(index);
        } else {
            return index;
        }
    }

    @Override
    @SuppressWarnings("squid:S1067")
    public boolean equalsUnits(final int index1, final int index2) {
        int pos1 = compact ? items.getIntValue(index1) : index1;
        int pos2 = compact ? items.getIntValue(index2) : index2;
        return wordEnd.get(pos1) == wordEnd.get(pos2)
                && wordContinued.get(pos1) == wordContinued.get(pos2)
                && distance.getIntValue(pos1) == distance.getIntValue(pos2)
                && valueCode.getIntValue(pos1) == valueCode.getIntValue(pos2)
                && dataCode.getIntValue(pos1) == dataCode.getIntValue(pos2);
    }

    @Override
    public LinkedListTreeUnit get(final int index) {
        int pos = compact ? items.getIntValue(index) : index;
        int dist = distance.getIntValue(pos);
        boolean we = wordEnd.get(pos);
        boolean wc = wordContinued.get(pos);
        if (!we && !wc && dist == pos) {
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

    @Override
    public boolean isNull(int index) {
        int pos = compact ? items.getIntValue(index) : index;
        int dist = distance.getIntValue(pos);
        boolean we = wordEnd.get(pos);
        boolean wc = wordContinued.get(pos);
        return !we && !wc && dist == pos;
    }
    

    /**
     * Returns number of different units stored in this array.
     * <p>
     * In compact mode this structure holds only unique definitions of units and array of references
     * to these unique definitions.
     * </p>
     *
     * @return Number of unique units.
     */
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

    /**
     * Reduces memory usage as much as possible. This method finds unique units and all duplicates
     * will be stored ones. This method also converts all internal arrays into a compact form.
     */
    public void compact() {
        if (compact) {
            return;
        }
        Set<LinkedListTreeUnit> differentUnits = LinkedListTreeUnit.getDifferentUnits(this);
        List<LinkedListTreeUnit> list = new ArrayList<>(differentUnits.size());
        list.addAll(differentUnits);
        differentUnits.clear();
        Map map = new HashMap();
        int index = 0;
        for (LinkedListTreeUnit unit : list) {
            map.put(unit, index);
            index++;
        }
        CompactArray unitsMapping = new CompactArray(size(), list.size());
        for (int i = 0; i < size(); i++) {
            LinkedListTreeUnit unit = get(i);
            index = (Integer) map.get(unit);
            unitsMapping.addIntValue(index);
        }
        int oldSize = size();
        clear(list.size());
        this.size = oldSize;
        index = 0;
        for (LinkedListTreeUnit unit : (ArrayList<LinkedListTreeUnit>) list) {
            if (unit == null) {
                set(index, index, false, false, 0, 0);
            } else {
                set(index, unit);
            }
            index++;
        }
        items = unitsMapping;
        trimToSize();
        compact = true;
    }

    @Override
    public void trimToSize() {
        if (items != null) {
            items.compact();
        }
        if (wordEnd != null) {
            wordEnd.compact();
        }
        if (wordContinued != null) {
            wordContinued.compact();
        }
        if (distance != null) {
            distance.compact();
        }
        if (valueCode != null) {
            valueCode.compact();
        }
        if (dataCode != null) {
            dataCode.compact();
        }
    }

    @Override
    public long getAllocationSize() {
        trimToSize();
        long result = super.getAllocationSize() + BASE_ALLOCATION_SIZE;
        result += wordContinued != null ? wordContinued.getAllocationSize() : 0;
        result += wordEnd != null ? wordEnd.getAllocationSize() : 0;
        result += distance != null ? distance.getAllocationSize() : 0;
        result += valueCode != null ? valueCode.getAllocationSize() : 0;
        result += dataCode != null ? dataCode.getAllocationSize() : 0;
        result += items != null ? items.getAllocationSize() : 0;
        return result;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        super.write(out);
        compact();
        out.writeByte(COMPACT_FORMAT_VERSION);
        out.writeBoolean(compact);
        wordContinued.write(out);
        wordEnd.write(out);
        distance.write(out);
        valueCode.write(out);
        dataCode.write(out);
        items.write(out);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        super.read(in);
        if (COMPACT_FORMAT_VERSION != in.readByte()) {
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

    @Override
    public int compareUnits(int index1, int index2) {
        int res = (getValueCode(index1) << 2) + (isWordEnd(index1) ? 2 : 0) + (isWordContinued(
                index1) ? 1 : 0);
        res -= (getValueCode(index2) << 2) 
                + (isWordEnd(index2) ? 2 : 0) 
                + (isWordContinued(index2) ? 1 : 0);
        if (res == 0) {
            res = getDistance(index1) - getDistance(index2);
            if (res == 0 && isWordEnd(index1)) {
                res = getDataCode(index1) - getDataCode(index2);
            }
        }
        return res;
    }

    @Override
    public AbstractLinkedListTreeUnitArray subArrayArgumentsVerified(int startIndex, int endIndex) {
        FastLinkedListTreeUnitArray result = new FastLinkedListTreeUnitArray(endIndex - startIndex);

        int resultIndex = 0;
        for (int i = startIndex; i < endIndex; i++, resultIndex++) {
            int fastIndex = getFastIndex(i);
            result.set(resultIndex,
                       getDistanceFast(fastIndex),
                       isWordEndFast(fastIndex),
                       isWordContinuedFast(fastIndex),
                       getValueCodeFast(fastIndex),
                       getDataCodeFast(fastIndex)
            );
        }

        return result;
    }

}
