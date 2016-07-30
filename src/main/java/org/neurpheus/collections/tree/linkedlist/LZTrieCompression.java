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

import org.neurpheus.collections.array.CompactArray;
import org.neurpheus.logging.LoggerService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Compresses linked list tree using the LZ-based compression algorithm.
 * <p>
 * The LZTire compression algorithm search for duplicated fragments in a tree structure, and
 * replaces these fragments with pointers to their first occurrences.
 * </p>
 *
 * <p>
 * This algorithm has time complexity O(N^2) where N is number of units in the array. To speed up
 * compression this algorithm splits input units array into partitions. A partition is a list of
 * equal sequences of two units in the array. Each partition is compressed separately. Therefore
 * real complexity is O(P * S^2) where P is number of partitions and S is the size of a larger
 * partition.
 * </p>
 *
 * <p>
 * The space complexity of this algorithm is O(N).
 * </p>
 *
 * <p>
 * <b>Usage:</b>
 * <br>
 * <code><pre>
 * LinkedListTree tree = ...;
 * LinkedListTree compressedTree = LZTrieCompression.compress(tree, false);
 * </pre></code>
 * </p>
 *
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
 * <li>
 * Jakub Strychowski : "Analiza morfologiczna języka naturalnego z wykorzystaniem sztucznych 
 * sieci neuronowych", PhD thesis, Poznan University of Technlogy, 02.07.2009.
 * </li>
 * </ul>
 * </p>
 *
 * <p>
 * <strong>Acknowledgments:</strong><br>
 * The author of this implementation is great full to Stasa Ristov for the help with understanding
 * some difficult aspects of the algorithm.
 * </p>
 *
 * @author Jakub Strychowski
 */
public class LZTrieCompression {

    /** Holds the logger for this class. */
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
    private static final int MAX_REPLACEMENT_LENGTH = 127;

    /** Experimental constant to limit the algorithm complexity using partition size limit. */
    private static final int MAX_PARTITION_SIZE = Integer.MAX_VALUE;

    /** Size of a synchronized fragment of a unit array. */
    private static int SYNCHRONIZATION_PAGE_SIZE = MAX_REPLACEMENT_LENGTH * 5;

    /** Info message for logger. */
    private static final String SEARCHING_MESSAGE = "   searching for duplicated tree fragments";

    /** Positions of alphabetically orderer suffixes of unit sequences. * */
    private int[] suffixArray;

    /** Array of units to compress. */
    private LinkedListTreeUnitArray units;

    /** A comparator used for units sorting. */
    private SuffixArrayComparator comparator;

    /** Array of units used for compression. */
    private LinkedListTreeUnitArray work;

    /** Array of flags marking positions in unit array as empty (replaced by pointers). */
    private boolean[] isWorkNull;

    /** Distance to next not null element in the work array - used for speed up. */
    private int[] nextNotNull;

    /** Each element contains a back pointers to a previous child (unit) or 0. */
    private int[] localPointers;

    /**
     * A bit at position x decides if a unit at position x points absolutely to different area in
     * the structure.
     */
    private BitSet absolutePointers;

    /**
     * A bit at position x decides if a unit at position x is pointed by any unit in the structure.
     */
    private BitSet absolutePointersEnds;

    /** Number of elements in a unit array before compression. */
    private int sizeBefore;

    /** Number of units in a unit array. */
    private int unitsLength;

    /** Compressed tree. */
    private LinkedListTree processedTree;

    private int maxSuffixArrayPos;

    /** System time when compression started. */
    private long startTime;

    /** System time when progress was logged out. */
    private long lastTime;

    /** Compression progress (number of operations on units). */
    private int progress;

    /** Maximum progress value. */
    private long maxProgress;

    /** Marks already processed units. */
    private boolean[] processed;

    /**
     * If {@code true} use parallel compression.
     */
    private boolean parallel;

    /**
     * Parallel compression splits a units array to sub arrays called pages. Each page may be
     * separately locked to protect concurrent modification by threads.
     */
    AtomicLong[] synchroniationPages;

    private LZTrieCompression(LinkedListTree tree, boolean parallelMode) {
        this.processedTree = tree;
        this.parallel = parallelMode;
    }

    /**
     * Compresses the specified linked list tree using the LZ-based compression algorithm.
     * <p>
     * Note: Parallel compression is experimental solution (sometimes producing an incoherent tree
     * structure). It can compress a big tree much faster but with lower compression ratio. You can
     * use this compression for trees with very large units arrays (for example for compressing
     * trees represented by tens of millions of units). In this mode each partition is compressed by
     * a separate thread but subsequences from different partitions overlap each other causing some
     * difficulties witch ensuring coherency of a linked list tree internal structure.
     * </p>
     *
     * @param tree         The tree to compress.
     * @param parallelMode if {@code true} use parallel compression.
     *
     * @return The compressed tree.
     */
    public static LinkedListTree compress(final LinkedListTree tree, boolean parallelMode) {
        LZTrieCompression compr = new LZTrieCompression(tree, parallelMode);
        compr.lztrieCompression();
        LinkedListTree result = compr.processedTree;
        compr.clear();
        return result;
    }

    /**
     * Returns the length of a substring at position j with may be replaced by a substring at
     * position i.
     *
     * @param iPos The position of a substring which is replacement.
     * @param jPos The position of a substring which should be replaced.
     *
     * @return The number of units which can be replaced.
     */
    @SuppressWarnings({"squid:MethodCyclomaticComplexity", "squid:S1067"})
    private int getReplacementLength(int iPos, int jPos) {
        int res = 0;
        int i = iPos;
        int j = jPos;
        boolean matched;
        LinkedListTreeUnitArray localWork = work;
        int maxRes = MAX_REPLACEMENT_LENGTH;
        boolean absolutePointerEnd = false;
        do {

            // move j to not null position
            while (j < unitsLength && isWorkNull[j]) {
                j += nextNotNull[j];
            }

            // move i to not null position
            while (i < j && i < jPos && i < unitsLength && isWorkNull[i]) {
                i += nextNotNull[i];
            }

            matched
                    = // checks RULE 4:  the substring with is a part of a replacement for 
                    // other sbstrings cannot be replaced if a new replacement contains
                    //  characters which occurrs after the substring
                    !absolutePointerEnd
                    // RULE 0: the length of a replacement is limited because the length is coded 
                    // on a limited number of bits; RULE 1: prevent substrings overlaping. 
                    && res < maxRes
                    // general conditions - units equals
                    && i < j && i < jPos && j < unitsLength
                    && localWork.equalsUnits(i, j)
                    // checks RULE 3: no l-pointers pointing into the replaced substring
                    //                from outside of this subtring. Only first unit 
                    //                in the substring can be pointed from outside unit.
                    && (j == jPos
                    || (!absolutePointers.get(j)
                    && (localPointers[j] == 0 || localPointers[j] >= jPos)));

            if (matched) {
                ++res;
                absolutePointerEnd = absolutePointersEnds.get(j);
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
                int fastIndex = work.getFastIndex(i);
                numberOfUnits++;
                --tmp;
                if (work.isAbsolutePointerFast(fastIndex)) {
                    int vc = work.getValueCode(fastIndex);
                    if (vc > 0) {
                        numberOfUnits += vc - 1;
                    }
                }
                lastUnitPos = i;
            }
        }
        absolutePointersEnds.set(lastUnitPos, true);
        return numberOfUnits;
    }

    private void createSortedSuffixArray() {
        // create suffx array
        LOGGER.finer("    creating suffix array....");
        Integer[] tmpArray = new Integer[unitsLength];
        for (int i = tmpArray.length - 1; i >= 0; i--) {
            tmpArray[i] = i;
        }

        // sort suffix array
        LOGGER.finer("    sorting suffix array...");
        comparator = new SuffixArrayComparator(units, true);
        Arrays.sort(tmpArray, comparator);
        suffixArray = new int[unitsLength];
        for (int i = tmpArray.length - 1; i >= 0; i--) {
            suffixArray[i] = tmpArray[i].intValue();
        }

        tmpArray = null;
        maxSuffixArrayPos = suffixArray.length - 1;
    }

    private void createBackPointersArray() {
        LOGGER.finer("   creating back pointer arrays...");
        localPointers = new int[unitsLength];
        absolutePointers = new BitSet(unitsLength);
        absolutePointersEnds = new BitSet(unitsLength);
        for (int i = 0; i < unitsLength; i++) {
            if (units.getDistance(i) > 0) {
                if (units.isAbsolutePointer(i)) {
                    absolutePointers.set(units.getDistance(i), true);
                    if (units.getValueCode(i) > 0) {
                        absolutePointersEnds.set(units.getDistance(i) + units.getValueCode(1) - 1,
                                                 true);
                    }
                } else {
                    localPointers[i + units.getDistance(i)] = i;
                }
            }
        }
    }

    private void createTemporaryResultArray() {
        LOGGER.finer("   creating temorary result array...");
        if (units instanceof CompactLinkedListTreeUnitArray) {
            work = new FastLinkedListTreeUnitArray(units);
        } else {
            work = units;
            units = new CompactLinkedListTreeUnitArray(units);
        }
        processedTree.setUnitArray(units);
    }

    /**
     *
     * @param tree
     *
     * @return
     */
    private double lztrieCompression() {
        LOGGER.fine("LZTrie compression started");
        sizeBefore = processedTree.getUnitArray().size();

        units = processedTree.getUnitArray();
        unitsLength = units.size();

        createSortedSuffixArray();
        createBackPointersArray();
        createTemporaryResultArray();

        prepareLoop();

        if (parallel) {
            mainLoopParallel();
        } else {
            mainLoopFirst();
        }

        eliminateEmptySpace();
        updateTwoWayPointers();

        LOGGER.info("LZTrie compression finished");

        final int sizeAfter = processedTree.getUnitArray().size();
        return sizeBefore == 0 ? 100.0 : 100.0 * sizeAfter / sizeBefore;
    }

    protected void prepareLoop() {
        // Analyses units from the begining to the end of a suffix array and
        // replaces each repeted substring with the substring at the current
        // analysed position.
        comparator = new SuffixArrayComparator(units, false);
        processed = new boolean[maxSuffixArrayPos + 1];
        isWorkNull = new boolean[maxSuffixArrayPos + 1];
        nextNotNull = new int[maxSuffixArrayPos + 1];
        synchroniationPages = new AtomicLong[2 + ((maxSuffixArrayPos + 1) / SYNCHRONIZATION_PAGE_SIZE)];
        for (int i = 0; i < synchroniationPages.length; i++) {
            synchroniationPages[i] = new AtomicLong(1L);
        }
        Arrays.fill(nextNotNull, 1);

        initializeProgressMonitoring();

    }

    protected void mainLoopSecond() {
        LOGGER.finer(SEARCHING_MESSAGE + "...");
        // search partitions
        List<Integer> partitionPoints = findPartitions();
        int partitionStart = 0;
        for (int partitionPoint : partitionPoints) {
            processPartition(partitionStart, partitionPoint);
            partitionStart = partitionPoint;
        }
        LOGGER.finer(SEARCHING_MESSAGE + ": 100%. Preparing final LZTrie data.");
    }

    protected void mainLoopParallel() {
        LOGGER.finer(SEARCHING_MESSAGE + "...");
        int cores = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
        ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(cores);
        List<Integer> partitionPoints = findPartitions();
        List<Callable<Integer>> tasks = new ArrayList<>(partitionPoints.size());
        int partitionStart = 0;
        for (int partitionPoint : partitionPoints) {
            tasks.add(new PartitionCompression(this, partitionStart, partitionPoint));
            partitionStart = partitionPoint;
        }
        try {
            pool.invokeAll(tasks);
        } catch (InterruptedException ex) {
            Logger.getLogger(LZTrieCompression.class.getName()).log(Level.SEVERE, null, ex);
        }
        pool.shutdown();
        LOGGER.finer(SEARCHING_MESSAGE + ": 100%. Preparing final LZTrie data.");
    }

    protected void processPartition(int partitionStart, int partitionEnd) {
        for (int i = partitionStart; i < partitionEnd; i++) {
            logProgress();
            final int iPos = suffixArray[i];
            processed[iPos] = true;
            if (!isWorkNull[iPos]) {
                progress++;
                findDuplicates(i, iPos, partitionEnd);
            }
        }
    }

    protected List<Integer> findPartitions() {
        List<Integer> partitionPoints = new ArrayList<>();
        int partitionEnd;
        for (int i = 0; i < maxSuffixArrayPos; i = partitionEnd) {
            int iPos = suffixArray[i];
            partitionEnd = i + 1;
            int posB = suffixArray[partitionEnd];
            while ((partitionEnd < maxSuffixArrayPos)
                    && ((partitionEnd - i) < MAX_PARTITION_SIZE)
                    && units.equalsUnits(iPos, posB)
                    && units.equalsUnits(iPos + 1, posB + 1)) {
                ++partitionEnd;
                posB = suffixArray[partitionEnd];
            }
            partitionPoints.add(partitionEnd);
        }
        return partitionPoints;
    }

    protected void mainLoopFirst() {
        LOGGER.finer("   searching for duplicated tree fragments...");
        int partitionEnd = -1;
        for (int i = 1; i < maxSuffixArrayPos; i++) {
            logProgress();
            final int iPos = suffixArray[i];
            processed[iPos] = true;
            if (!isWorkNull[iPos]) {
                progress++;
                if (partitionEnd <= i) {
                    partitionEnd = findPartitionEnd(i, iPos);
                }
                findDuplicates(i, iPos, partitionEnd);
            }
        }
        LOGGER.finer(
                "    searching for duplicated tree fragments: 100%. Preparing final LZTrie data.");
    }

    protected int findPartitionEnd(final int i, final int iPos) {
        int partitionEnd = i + 1;
        int posB = suffixArray[partitionEnd];
        while ((partitionEnd < maxSuffixArrayPos)
                && ((partitionEnd - i) < MAX_PARTITION_SIZE)
                && units.equalsUnits(iPos, posB)
                && units.equalsUnits(iPos + 1, posB + 1)) {
            ++partitionEnd;
            posB = suffixArray[partitionEnd];
        }
        return partitionEnd;
    }

    protected void findDuplicates(final int i, final int iPos, final int partitionEnd) {
        for (int j = i + 1; j < partitionEnd; j++) {
            final int jPos = suffixArray[j];
            if (!isWorkNull[jPos]) {
                int replacementLength = getReplacementLength(iPos, jPos);
                if (replacementLength > 1) {
                    int page = jPos / SYNCHRONIZATION_PAGE_SIZE;
                    AtomicLong syncObj1 = synchroniationPages[page];
                    AtomicLong syncObj2 = synchroniationPages[page + 1];
                    if (((jPos + replacementLength) / SYNCHRONIZATION_PAGE_SIZE) > page + 1) {
                        throw new IllegalStateException("Unsycnhronized page hit");
                    }
                    synchronized (syncObj1) {
                        synchronized (syncObj2) {
                            int replacementLength2 = getReplacementLength(iPos, jPos);
                            if (replacementLength2 > 1) {
                                replaceDuplicate(iPos, jPos, replacementLength2);
                            }
                        }
                    }
                }
            }
        }
    }

    protected void replaceDuplicate(int iPos, int jPos, int replacementLength) {
        // determines number of units available on the target position
        // returns 0 if replacement is closed and this number is not
        // important
        final int nofu = getNumberOfUnits(iPos, replacementLength);
        // check if replacement is open or closed
        absolutePointers.set(iPos, true);
        work.set(jPos, iPos, false, false, nofu, 0);
        isWorkNull[jPos] = false;
        int len = replacementLength;
        --len;
        boolean isReplacementEnd = false;
        for (int k = 1; len > 0; k++) { // NOSONAR
            if (!isWorkNull[jPos + k]) {
                if (!processed[jPos + k]) {
                    progress++;
                }
                isReplacementEnd = absolutePointersEnds.get(jPos + k);
                work.set(jPos + k, null);
                isWorkNull[jPos + k] = true;
                --len;
            }
        }
        updateNotNullDistance(jPos);
        if (isReplacementEnd) {
            absolutePointersEnds.set(jPos, true);
        }
    }

    protected synchronized void logProgress() {
        if (LOGGER.isLoggable(Level.FINER)) {
            long duration = System.currentTimeMillis() - lastTime;
            if (duration > 1_000) {
                float percent = 100.0f * progress / maxProgress;
                float speed = progress / (System.currentTimeMillis() - startTime);
                long toTheEnd = (long) ((maxProgress - progress) / speed);

                LOGGER.finer(String.format("    searching for duplicated tree fragments : %7.4f %%",
                                           percent));
                lastTime = System.currentTimeMillis();
                LOGGER.finer(String.format("Estimated finish in %d s. (about %tT)", toTheEnd / 1000,
                                           new Date(lastTime + toTheEnd)));
            }
        }
    }

    protected void initializeProgressMonitoring() {
        startTime = System.currentTimeMillis();
        lastTime = startTime;
        progress = 1;
        maxProgress = maxSuffixArrayPos;
    }

    protected void eliminateEmptySpace() {
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

        processedTree.getUnitArray().dispose();
        processedTree.setUnitArray(work);

        FastLinkedListTreeUnitArray result = new FastLinkedListTreeUnitArray(work.size() - counter);
        result.valueMapping = work.getValueMapping();
        result.reverseMapping = work.getReverseValueMapping();
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

        processedTree.setUnitArray(result);

    }

    protected void updateTwoWayPointers() {
        LOGGER.fine("   updating two-way pointers lengths....");
        LinkedListTreeUnitArray tmpUnitArray = processedTree.getUnitArray();
        for (int i = tmpUnitArray.size() - 1; i >= 0; i--) {
            final LinkedListTreeUnit unit = tmpUnitArray.get(i);
            if (unit.isAbsolutePointer()) {
                int len = unit.getValueCode();
                if (len > 0) {
                    for (int j = unit.getDistance(); j < unit.getDistance() + len; j++) {
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
        comparator = null;
        work = null;
        localPointers = null;
        absolutePointers = null;
        absolutePointersEnds = null;
        isWorkNull = null;
        synchroniationPages = null;
    }

}
