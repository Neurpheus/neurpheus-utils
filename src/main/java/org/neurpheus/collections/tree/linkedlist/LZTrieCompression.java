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
    private static int maxReplacementLength = 127;

    public int maxPartitionSize = Integer.MAX_VALUE;

    private static int defaultDivideAndRuleTreshold = 10_000000;
    //private static int defaultDivideAndRuleTreshold = 100_000_000;

    // page should be greater then maxReplacementLength
    private static int SYNCHRONIZATION_PAGE_SIZE = maxReplacementLength * 5;

    private int[] suffixArray;
    private LinkedListTreeUnitArray units;
    private SuffixArrayComparator comparator;
    private LinkedListTreeUnitArray work;
    private boolean[] isWorkNull;
    private int[] nextNotNull;
    private int[] localPointers;
    private BitSet absolutePointers;
    private BitSet absolutePointersEnds;
    private int sizeBefore;
    private int unitsLength;

    private LinkedListTree processedTree;

    int maxSuffixArrayPos;
    //int partitionEnd;
    long startTime;
    long lastTime;
    int progress;
    long maxProgress;
    boolean[] processed;
    boolean parallel;

    AtomicLong[] synchroniationPages;

    /** Creates a new instance of LZTrieCompression. */
    private LZTrieCompression() {
    }

    private LZTrieCompression(LinkedListTree tree, boolean parallelMode) {
        this.processedTree = tree;
        this.parallel = parallelMode;

    }

    /**
     * Compresses linked list tree using the LZ-based compression algorithm.
     *
     * @param tree The tree to compress.
     *
     * @return The compression ratio as a percentage size of the source tree.
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
     * @return The number of units which can be repleaced.
     */
    private int getReplacementLength(int iPos, int jPos) {
        int res = 0;
        int i = iPos;
        int j = jPos;
        boolean matched;
        LinkedListTreeUnitArray localWork = work;
        int maxRes = maxReplacementLength;
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
            matched
                    = // checks RULE 4:  the substring with is a part of a replacement for other sbstrings cannot be replaced 
                    //                 if a new replacement contains characters which occurrs after the substring 
                    !aPointerEnd
                    // RULE 0: the length of a replacement is limited because the length is coded on a
                    // limited number of bits; RULE 1: prevent substrings overlaping
                    && res < maxRes
                    // general conditions - units equals
                    && i < j && i < jPos && j < unitsLength
                    && localWork.equalsUnits(i, j)
                    // checks RULE 3: no l-pointers pointing into the replaced substring
                    //                from outside of this subtring. Only first unit 
                    //                in the substring can be pointed from outside unit.
                    && (j == jPos || (!absolutePointers.get(j) && (localPointers[j] == 0 || localPointers[j] >= jPos)));
            ;
            if (matched) {
                ++res;
                aPointerEnd = absolutePointersEnds.get(j);
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
            }
            ++curPos;
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
        // create back pointers array
        LOGGER.finer("   creating back pointer arrays...");
        localPointers = new int[unitsLength];
        absolutePointers = new BitSet(unitsLength);
        absolutePointersEnds = new BitSet(unitsLength);
        for (int i = 0; i < unitsLength; i++) {
            //LinkedListTreeUnit unit = units.get(i);
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

        // create temporary result array
        LOGGER.finer("   creating temorary result array...");
        //work = new FastLinkedListTreeUnitArray(units);
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
        LOGGER.finer("   searching for duplicated tree fragments...");
        // search partitions
        List<Integer> partitionPoints = findPartitions();
        int partitionStart = 0;
        for (int partitionPoint : partitionPoints) {
            processPartition(partitionStart, partitionPoint);
            partitionStart = partitionPoint;
        }
        LOGGER.finer(
                "    searching for duplicated tree fragments: 100%. Preparing final LZTrie data.");
    }

    protected void mainLoopParallel() {
        LOGGER.finer("   searching for duplicated tree fragments...");
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
        LOGGER.finer(
                "    searching for duplicated tree fragments: 100%. Preparing final LZTrie data.");
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
        for (int i = 0; i < maxSuffixArrayPos;) {
            int iPos = suffixArray[i];
            int partitionEnd = i + 1;
            int posB = suffixArray[partitionEnd];
            while ((partitionEnd < maxSuffixArrayPos)
                    && ((partitionEnd - i) < maxPartitionSize)
                    && units.equalsUnits(iPos, posB)
                    && units.equalsUnits(iPos + 1, posB + 1)) {
                ++partitionEnd;
                posB = suffixArray[partitionEnd];
            }
            i = partitionEnd;
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
        int partitionSize = 0;
        int posB = suffixArray[partitionEnd];
        while ((partitionEnd < maxSuffixArrayPos)
                && ((partitionEnd - i) < maxPartitionSize)
                && units.equalsUnits(iPos, posB)
                && units.equalsUnits(iPos + 1, posB + 1)) {
            ++partitionEnd;
            partitionSize++;
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
        //aPointersEnds[iPos.intValue() + replacementLength - 1] = true;                                
        --replacementLength;
        boolean isReplacementEnd = false;
        for (int k = 1; replacementLength > 0; k++) { // NOSONAR
            if (!isWorkNull[jPos + k]) {
                if (!processed[jPos + k]) {
                    progress++;
                }
                isReplacementEnd = absolutePointersEnds.get(jPos + k);
                work.set(jPos + k, null);
                isWorkNull[jPos + k] = true;
                --replacementLength;
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
        // update two-way pointers lengths
        LOGGER.fine("   updating two-way pointers lengths....");
        LinkedListTreeUnitArray tmpUnitArray = processedTree.getUnitArray();
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
        localPointers = null;
        absolutePointers = null;
        absolutePointersEnds = null;
        isWorkNull = null;
        synchroniationPages = null;
    }

}
