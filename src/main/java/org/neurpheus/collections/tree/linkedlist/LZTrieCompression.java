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

import org.neurpheus.collections.array.CompactArray;
import org.neurpheus.logging.LoggerService;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neurpheus.collections.tree.TreeNode;

/**
 * Compresses linked list tree using the LZ-based compression algorithm.
 *
 * <p>
 * <b>Usage:</b>
 * <br>
 * <code><pre>
 * LinkedListTree tree = ...;
 * double ratio = LZTrieCompression.compress(tree);
 * System.out.println("The tree occuppies " + ratio + "% of initial size now");
 * </pre></code>
 * </p>
 * <p>
 * You can find more informations about the compression algorithm in the following publications:
 * <ul>
 * <li>
 * Stasa Ristov, Eric Laporte : "Ziv Lempel compression of huge natural language data tries using
 * suffix arrays".
 * </li>
 * <li>
 * Strahil Ristov : "LZ trie and dictionary compression".
 * </li>
 * </ul>
 * </p>
 * <p>
 * <b>Acknowledgements:</b> The author of this implementation is great full to Stasa Ristov for the
 * help with understanding some difficult aspects of the algorithm.
 * </p>
 *
 * @author Jakub Strychowski
 */
public class LZTrieCompression {

    /** Holds the logger for this class */
    private static final Logger LOGGER = LoggerService.getLogger(LZTrieCompression.class);

    /**
     * Holds the maximum length of a replacement.
     * <p>
     * A replacement length have to be encoded on a limited number of bits, therefore the length of
     * a replacement is limited.
     * </p>
     * <p>
     * The default value (127) requires 7 bits for encoding. You can decrease this value before
     * compression, if a unit structure should be packed more.
     * </p>
     */
    private int maxReplacementLength = 127;
    
    public int maxPartitionSize = Integer.MAX_VALUE;

    private int[] suffixArray;
    private LinkedListTreeUnitArray units;
    private SuffixArrayComparator comparator;
    private LinkedListTreeUnitArray work;
    private boolean[] isWorkNull;
    private int[] nextNotNull;
    private int[] lPointers;
    private BitSet aPointers;
    private BitSet aPointersEnds;
    private int sizeBefore;
    private int unitsLength;

    /** Creates a new instance of LZTrieCompression */
    private LZTrieCompression() {
    }

    /**
     * Compresses linked list tree using the LZ-based compression algorithm.
     *
     * @param tree The tree to compress.
     *
     * @return The compression ratio as a percentage size of the source tree.
     */
    public static double compress(final LinkedListTree tree) {
        LZTrieCompression compr = new LZTrieCompression();
        double result = compr.doCompress(tree);
        compr.updateTwoWayPointers(tree);
        compr.clear();
        return result;
    }
    

    /**
     * @return the maxReplacementLength
     */
    public final int getMaxReplacementLength() {
        return maxReplacementLength;
    }

    /**
     * @param maxReplacementLength the maxReplacementLength to set
     */
    public void setMaxReplacementLength(int maxReplacementLength) {
        this.maxReplacementLength = maxReplacementLength;
    }

    /**
     * Represents a comparator used for a suffix array sorting.
     */
    private class SuffixArrayComparator implements Comparator {

        /** Holds unis pointed by items in a suffix array. */
        private LinkedListTreeUnitArray unitsArray;

        /** Holds the maximum index in a units array. */
        private int maxPos;

        /**
         * If <code>true</code> substrings should be compared also according to their positions in a
         * units array.
         */
        private boolean comparePositions;

        /**
         * Constructs new comparator.
         *
         * @param units   The array of units constructing a linked list tree.
         * @param compPos If <code>true</code> substrings should becomparaed also according to their
         *                positions in a units array.
         */
        public SuffixArrayComparator(
                final LinkedListTreeUnitArray units,
                final boolean compPos) {
            this.unitsArray = units;
            this.maxPos = units.size() - 1;
            this.comparePositions = compPos;
        }

        /**
         * Compares two substrings in a units aray pointed by two items from a suffix array.
         *
         * @param objA The position of first substring.
         * @param objB The position of second substring.
         *
         * @return a negative integer, zero, or a positive integer as the first argument is less
         *         than, equal to, or greater than the second.
         */
        @Override
        public int compare(Object objA, Object objB) {
            int posA = ((Integer) objA).intValue();
            int posB = ((Integer) objB).intValue();
            return compare(posA, posB);
        }
                
        /**
         * Compares two substrings in a units aray pointed by two items from a suffix array.
         *
         * @param objA The position of first substring.
         * @param objB The position of second substring.
         *
         * @return a negative integer, zero, or a positive integer as the first argument is less
         *         than, equal to, or greater than the second.
         */
        public int compare(int posA, int posB) {
            if (posA >= maxPos) {
                // Last unit goes to the end
                return 1;
            } else if (posB >= maxPos) {
                // Last unit goes to the end
                return -1;
            } else {
                // Compare two units pointed by the suffix array items.
                //int res = unitsArray.get(posA).compareTo(unitsArray.get(posB));
                int res = unitsArray.compareUnits(posA, posB);

                if (res == 0) {
                    // Substrings are equals at the first position.
                    // Compare units at the second position.
                    //res = unitsArray.get(posA + 1).compareTo(unitsArray.get(posB + 1));
                    res = unitsArray.compareUnits(posA + 1, posB + 1);
                    
//                    if ((res == 0) && comparePositions && (posA + 2 <= maxPos) && (posB + 2 <= maxPos)) {
//                        res = unitsArray.compareUnits(posA + 2, posB + 2);
//                    }
                    
                    // If units are equal and the comparePosition flag is on,
                    // compare positions of units, otherwise return the result
                    // of comparision of units at second position.
                    return (res != 0) || (!comparePositions)
                            ? res : (posA < posB ? -1 : 1);
                } else {
                    // substrings differs at the first position
                    return res;
                }
            }
        }

        /**
         * Relases resources consumed by this comparator.
         */
        public void clear() {
            this.unitsArray = null;
        }
    }
    
    /**
     * Returns the length of a substring at position j with may be replaced by a substring at
     * position i.
     *
     * @param iPos The position of a substring which is replacement.
     * @param jPos The position of a substring which should be replaced.
     *
     * @return The number of units which can be repleaced.
     */
    private int getReplacementLengthOld(int iPos, int jPos) {
        int res = 0;
        int i = iPos;
        int j = jPos;
        boolean matched;
        LinkedListTreeUnitArray localWork = work;
        int maxRes = getMaxReplacementLength();
        do {
            
            // move j to not null position
            while (j < unitsLength && isWorkNull[j]) {
                //++j;
                j += nextNotNull[j];
            }
            
            // move i to not null position
            while (i < j && i < jPos && i < unitsLength && isWorkNull[i]) {
                //++i;
                i += nextNotNull[i];
            }
            /*
             * RULE 0: the length of a replacement is limited because the length is coded on a
             * limited number of bits; RULE 1: prevent substrings overlaping
             */
            matched = i < j && j < unitsLength && res < maxRes && i < jPos && localWork.equalsUnits(i, j);
            if (matched) {
                ++i;
                ++j;
                ++res;
            }
        } while (matched);

        // checks RULE 2, 3 and 4
        res = 0;
        if (j > jPos + 1) {
            boolean aPointerEnd = false;
            for (int x = jPos; x < j && !aPointerEnd; x++) {
                if (!isWorkNull[x]) {
                    // checks RULE 2: no l-pointers pointing after the first unit behind the
                    //                replaced substring. Note that j variable holds the position
                    //                of a first unit after the substring.
                    if (!localWork.isAbsolutePointer(x) && (x + localWork.getDistance(x) > j)) {
                        j = x;
                    } 
                    // checks RULE 3: no l-pointers pointing into the replaced substring
                    //                from outside of this subtring. Only first unit 
                    //                in the substring can be pointed from outside unit.
                    else if (x > jPos && (aPointers.get(x)
                            || (x < lPointers.length && lPointers[x] > 0 && lPointers[x] < jPos))) {
                        j = x;
                        // checks RULE 4:  the substring with is a part of a replacement for other sbstrings cannot be replaced 
                        //                 if a new replacement contains characters which occurrs after the substring 
                    } else if (aPointerEnd) {
                        j = x;
                    } else {
                        ++res;
                    }
                    aPointerEnd = aPointersEnds.get(x);
                }
            }
        }

        return res;
    }

    /**
     * Returns the length of a substring at position j with may be replaced by a substring at
     * position i.
     *
     * @param iPos The position of a substring which is replacement.
     * @param jPos The position of a substring which should be replaced.
     *
     * @return The number of units which can be repleaced.
     */
    private int getReplacementLength(int iPos, int jPos) {
        int res = 0;
        int i = iPos;
        int j = jPos;
        boolean matched;
        LinkedListTreeUnitArray localWork = work;
        int maxRes = getMaxReplacementLength();
        boolean aPointerEnd = false;
        do {
            
            // move j to not null position
            while (j < unitsLength && isWorkNull[j]) {
                //++j;
                j += nextNotNull[j];
            }
            
            // move i to not null position
            while (i < j && i < jPos && i < unitsLength && isWorkNull[i]) {
                //++i;
                i += nextNotNull[i];
            }
            /*
             */
            matched = 
                    // checks RULE 4:  the substring with is a part of a replacement for other sbstrings cannot be replaced 
                    //                 if a new replacement contains characters which occurrs after the substring 
                    !aPointerEnd

                    // RULE 0: the length of a replacement is limited because the length is coded on a
                    // limited number of bits; RULE 1: prevent substrings overlaping
                    && res < maxRes 
                    
                    // general conditions - unita equals
                    && i < j && i < jPos && j < unitsLength 
                    && localWork.equalsUnits(i, j)
                    
                    
                    // checks RULE 3: no l-pointers pointing into the replaced substring
                    //                from outside of this subtring. Only first unit 
                    //                in the substring can be pointed from outside unit.
                    && (j == jPos || (!aPointers.get(j) && (lPointers[j] == 0 || lPointers[j] >= jPos)));
                    ;
            if (matched) {
                ++res;
                aPointerEnd = aPointersEnds.get(j);
                i += nextNotNull[i];
                j += nextNotNull[j];
            }
        } while (matched);

        // checks RULE 2, 3 and 4
        res = 0;
        if (j > jPos + 1) {
            for (int x = jPos; x < j; x += nextNotNull[x]) {
                if (!isWorkNull[x]) {
                    // checks RULE 2: no l-pointers pointing after the first unit behind the
                    //                replaced substring. Note that j variable holds the position
                    //                of a first unit after the substring.
                    if (!localWork.isAbsolutePointer(x) && (x + localWork.getDistance(x) > j)) {
                        j = x;
                    } else {
                        ++res;
                    }
                }
            }
        }

        return res;
    }
    
    /**
     * Compares two substrings in a units aray pointed by two items from a suffix array.
     *
     * @param objA The position of first substring.
     * @param objB The position of second substring.
     *
     * @return a negative integer, zero, or a positive integer as the first argument is less
     *         than, equal to, or greater than the second.
     */
    public boolean inPacket(int posA, int posB) {
        if (units.equalsUnits(posA, posB)) {
            return units.equalsUnits(posA + 1, posB +1);
        }
        return false;
    }
    
    /**
     *
     * @param pos
     *
     * @return
     */
    private int determineNumberOfUnits(int pos) {
        int numberOfUnits = 0;
        int curPos = pos;
        boolean stop = false;
        int maxtarget = 0;
        do {
            if (!isWorkNull[curPos]) {
                //LinkedListTreeUnit unit = work.get(curPos);
                stop = true;
                int fastIndex = work.getFastIndex(curPos);
                if (work.isAbsolutePointerFast(fastIndex)) {
                    int vc = work.getValueCode(fastIndex);
                    if (vc > 0) {
                        numberOfUnits += vc;
                        stop = false;
                    } else {
                        numberOfUnits += determineNumberOfUnits(work.getDistanceFast(fastIndex));
                    }
                } else {
                    numberOfUnits++;
                    int dis = work.getDistanceFast(fastIndex);
                    if (dis > 0) {
                        int target = curPos + dis;
                        if (maxtarget < target) {
                            maxtarget = target;
                        }
                    }
                    if (work.isWordContinuedFast(fastIndex) || curPos <= maxtarget) {
                        stop = false;
                    }
                }
                ++curPos;
            }
        } while (!stop);
        return numberOfUnits;
    }

    /**
     *
     * @param pos
     * @param length
     *
     * @return
     */
    private int getNumberOfUnits(int pos, int length) {
        int tmp = length;
        int numberOfUnits = 0;
        int lastUnitPos = 0;
        for (int i = pos; tmp > 0; i++) { // NOSONAR
            if (!isWorkNull[i]) {
                //LinkedListTreeUnit unit = work.get(i);
                int fastIndex = work.getFastIndex(i);
                numberOfUnits++;
                --tmp;
                if (work.isAbsolutePointerFast(fastIndex)) {
                    int vc = work.getValueCode(fastIndex);
                    if (vc > 0) {
                        numberOfUnits += vc - 1;
                    } else {
                        numberOfUnits += determineNumberOfUnits(work.getDistanceFast(fastIndex)) - 1;
                    }
                }
                lastUnitPos = i;
            }
        }
        aPointersEnds.set(lastUnitPos, true);
        return numberOfUnits;
    }

    /**
     *
     * @param tree
     *
     * @return
     */
    private double doCompress(final LinkedListTree tree) {
        sizeBefore = tree.getUnitArray().size();

        LOGGER.info("LZTrie compression started");
        
        units = tree.getUnitArray();
        unitsLength = units.size();

        // create suffx array
        LOGGER.fine("    creating suffix array....");
        Integer[] tmpArray = new Integer[unitsLength];
        for (int i = tmpArray.length - 1; i >= 0; i--) {
            tmpArray[i] = i;
        }

        // sort suffix array
        LOGGER.fine("    sorting suffix array...");
        comparator = new SuffixArrayComparator(units, true);
        Arrays.sort(tmpArray, comparator);
        suffixArray = new int[unitsLength];
        for (int i = tmpArray.length - 1; i >= 0; i--) {
            suffixArray[i] = tmpArray[i].intValue();
        }

        tmpArray = null;
        
        // create back pointers array
        LOGGER.fine("   creating back pointer arrays...");
        lPointers = new int[unitsLength];
        aPointers = new BitSet(unitsLength);
        aPointersEnds = new BitSet(unitsLength);
        for (int i = 0; i < unitsLength; i++) {
            //LinkedListTreeUnit unit = units.get(i);
            if (units.getDistance(i) > 0) {
                if (units.isAbsolutePointer(i)) {
                    aPointers.set(units.getDistance(i), true);
                    if (units.getValueCode(i) > 0) {
                        aPointersEnds.set(units.getDistance(i) + units.getValueCode(1) - 1, true);
                    }
                } else {
                    lPointers[i + units.getDistance(i)] = i;
                }
            }
        }

        // create temporary result array
        LOGGER.fine("   creating temorary result array...");
        //work = new FastLinkedListTreeUnitArray(units);
        work = units;
        units = new CompactLinkedListTreeUnitArray(units);
        tree.setUnitArray(units);
        units.logStatistics("compact form");
        
        // Analyses units from the begining to the end of a suffix array and
        // replaces each repeted substring with the substring at the current
        // analysed position.
        comparator = new SuffixArrayComparator(units, false);
        final int maxSuffixArrayPos = suffixArray.length - 1;
        int partitionEnd = -1;
        long startTime = System.currentTimeMillis();
        long lastTime = startTime;
        int progress = 1;
        long maxProgress = maxSuffixArrayPos;
        boolean[] processed = new boolean[maxSuffixArrayPos + 1];
        isWorkNull = new boolean[maxSuffixArrayPos + 1];
        nextNotNull = new int[maxSuffixArrayPos + 1];
        Arrays.fill(nextNotNull, 1);
        
        
        LOGGER.fine("   searching for duplicated tree fragments...");
        for (int i = 1; i < maxSuffixArrayPos; i++) {
            if (LOGGER.isLoggable(Level.FINER)) {
                long duration = System.currentTimeMillis() - lastTime;
                if (duration > 1_000) {
                    float percent = 100.0f * progress / maxProgress;
                    float speed = progress / (System.currentTimeMillis() - startTime);
                    long toTheEnd = (long) ( (maxProgress - progress) / speed);

                    LOGGER.finer(String.format("    searching for duplicated tree fragments : %7.4f %%", percent));
                    lastTime = System.currentTimeMillis();
                    LOGGER.finer(String.format("Estimated finish in %d s. (about %tT)", toTheEnd /1000, new Date(lastTime + toTheEnd)));
                }
            }
            final int iPos = suffixArray[i];
            processed[iPos] = true;
            //if (!work.isNull(iPos)) {
            if (!isWorkNull[iPos]) {
                progress++;
                // determine the end of a partition
                if (partitionEnd <= i) {
                    partitionEnd = i + 1;
                    int partitionSize = 0;
                    int posB = suffixArray[partitionEnd];
                    while ((partitionEnd < maxSuffixArrayPos) 
                            && ((partitionEnd - i) < maxPartitionSize) 
                            && units.equalsUnits(iPos, posB) 
                            && units.equalsUnits(iPos + 1, posB + 1)) 
                    {
                        ++partitionEnd;
                        partitionSize++;
                        posB = suffixArray[partitionEnd];
                    }
//                    if (partitionSize > 5000) {
//                        LOGGER.info("*** BIG PARTITION : " + partitionSize);
//                        LOGGER.info(units.get(iPos).toString());
//                        LOGGER.info(units.get(iPos+1).toString());
//                        LOGGER.info("*******************************************");
//                    }
                }
                for (int j = i + 1; j < partitionEnd; j++) {
                    final int jPos = suffixArray[j];
                    if (!isWorkNull[jPos]) {
                        int replacementLength = getReplacementLength(iPos, jPos);
                        if (replacementLength > 1) {
                            // determines number of units available on the target position
                            // returns 0 if replacement is closed and this number is not
                            // important
                            final int nofu = getNumberOfUnits(iPos, replacementLength);
                            // check if replacement is open or closed
                            aPointers.set(iPos, true);
                            work.set(jPos, iPos, false, false, nofu, 0);
                            isWorkNull[jPos] = false;
                            //aPointersEnds[iPos.intValue() + replacementLength - 1] = true;                                
                            --replacementLength;
                            boolean isReplacementEnd = false;
                            for (int k = 1; replacementLength > 0; k++) { // NOSONAR
                                if (!isWorkNull[jPos + k]) {
                                    if (!processed[jPos+k]){
                                        progress++;
                                    }
                                    isReplacementEnd = aPointersEnds.get(jPos + k);
                                    work.set(jPos + k, null);
                                    isWorkNull[jPos + k] = true;
                                    --replacementLength;
                                }
                            }
                            updateNotNullDistance(jPos);
                            if (isReplacementEnd) {
                                aPointersEnds.set(jPos, true);
                            }
                        }
                    }
                }
            }
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("    searching for duplicated tree fragments: 100%. Preparing final LZTrie data.");
        }

        // removes empty elements and upgrades pointers
        LOGGER.fine("   removing empty elements and update pointers....");
        CompactArray emptyElementCounters = new CompactArray(work.size(), 15);
        int counter = 0;
        for (int i = 0; i < work.size(); i++) {
            emptyElementCounters.setIntValue(i, counter);
            if (isWorkNull[i]) {
                ++counter;
            }
        }
        
        tree.getUnitArray().dispose();
        tree.setUnitArray(work);
        
        FastLinkedListTreeUnitArray result = new FastLinkedListTreeUnitArray(work.size() - counter);
        result.setValueMapping(work.getValueMapping());
        int pos = 0;
        for (int i = 0; i < work.size(); i++) {
            if (!isWorkNull[i]) {            
                final LinkedListTreeUnit unit = work.get(i);
                if (unit.isAbsolutePointer()) {
                    unit.setDistance(unit.getDistance() - emptyElementCounters.getIntValue(unit.
                            getDistance()));
                } else if (unit.getDistance() > 0) {
                    unit.setDistance(unit.getDistance() - (emptyElementCounters.getIntValue(
                            i + unit.getDistance()) - emptyElementCounters.getIntValue(i)));
                }
                result.set(pos++, unit);
            }
        }
        work.dispose();
        work = null;
        
        tree.setUnitArray(result);

        
        //making compact array
        //tree.getUnitArray().compact();
        
        LOGGER.info("LZTrie compression finished");

        final int sizeAfter = tree.getUnitArray().size();
        return sizeBefore == 0 ? 100.0 : 100.0 * sizeAfter / sizeBefore;
    }

    protected void updateTwoWayPointers(LinkedListTree tree) {
        // update two-way pointers lengths
        LOGGER.fine("   updating two-way pointers lengths....");
        LinkedListTreeUnitArray tmpUnitArray = tree.getUnitArray();
        for (int i = tmpUnitArray.size() - 1; i >= 0; i--) {
            final LinkedListTreeUnit unit = tmpUnitArray.get(i);
            if (unit.isAbsolutePointer()) {
                int len = unit.getValueCode();
                if (len > 0) {
                    for (int j = unit.getDistance(); j < unit.getDistance() + len; j++) {
                        //final LinkedListTreeUnit u = tmpUnitArray.get(j);
                        if (tmpUnitArray.isAbsolutePointer(j)) {
                            len -= tmpUnitArray.getValueCode(j) - 1;
                        }
                    }
                }
                unit.setValueCode(len < 0 ? 0 : len);
                tmpUnitArray.set(i, unit);
            }
        }
    }
    
    private void updateNotNullDistance(int pos) {

        // go forward
        int next = pos + 1;
        while (next < (unitsLength - 1) && isWorkNull[next]) {
            next++;
        }
        int distance = next - pos;
        for (int i = pos; i < next; i++) {
            nextNotNull[i] = distance;
            --distance;
        }
        
        // go back
        distance = 1;
        int prev = pos - 1;
        while (prev > 0 && isWorkNull[prev]) {
            prev--;
        }
        distance = pos - prev;
        while (prev < pos) {
            nextNotNull[prev++] = distance;
            --distance;
        }
    }
    
    
    private void clear() {
        suffixArray = null;
        units = null;
        if (comparator != null) {
            comparator.clear();
        }
        comparator = null;
        work = null;
        lPointers = null;
        aPointers = null;
        aPointersEnds = null;
        isWorkNull = null;
    }

}
