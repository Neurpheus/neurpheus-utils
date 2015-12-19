/*
 *  © 2015 Jakub Strychowski
 */

package org.neurpheus.collections.tree.linkedlist;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author Kuba
 */
public interface LinkedListTreeUnitArray {

    void logStatistics(String name);

    int getValueCodeFast(int fastp);

    void addAll(LinkedListTreeUnitArray subArray);

    void moveAbsolutePointers(int i);

    void add(final LinkedListTreeUnit unit);

    void clear(int capacity);

    void dispose();

    boolean equalsUnits(final int index1, final int index2);

    LinkedListTreeUnit get(final int index);

    long getAllocationSize();

    int getFastIndex(final int index);
    
    int getDataCode(final int index);

    int getDataCodeFast(final int index);
    
    int getDistance(final int index);

    int getDistanceFast(final int index);
    
    int getValueCode(final int index);
    
    int getValue(final int index);

    boolean isAbsolutePointer(final int index);

    boolean isAbsolutePointerFast(final int index);
    
    //boolean isNull(final int index);

    boolean isWordContinued(final int index);

    boolean isWordContinuedFast(final int index);
    
    boolean isWordEnd(final int index);

    boolean isWordEndFast(final int index);
    
    void set(final int index, final LinkedListTreeUnit unit);

    void set(final int index, final int distance, final boolean wordEnd, final boolean wordContinued,
             final int valueCode, final int dataCode);

    int size();
    
    void write(DataOutputStream out) throws IOException;
    
    public void read(DataInputStream in) throws IOException;
    

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
    int compareUnits(int index1, int index2);
    
    int[] getValueMapping();
    
    Map<Integer, Integer> getReverseValueMapping();
    
    void setValueMapping(int[] mapping);

    public int mapToValueCode(int value);
    
    
    LinkedListTreeUnitArray subArray(int startIndex, int endIndex);
    
}
