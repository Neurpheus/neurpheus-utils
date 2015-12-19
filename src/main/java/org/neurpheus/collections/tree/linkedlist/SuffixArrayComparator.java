/*
 *  © 2015 Jakub Strychowski
 */

package org.neurpheus.collections.tree.linkedlist;

import java.util.Comparator;

//    @Override

//    public LinkedListTree call() throws Exception {
//        lztrieCompression();
//        clear();
//        return processedTree;
//    }
/**
 * Represents a comparator used for a suffix array sorting.
 */
class SuffixArrayComparator implements Comparator {
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
    public SuffixArrayComparator(final LinkedListTreeUnitArray units, final boolean compPos) {
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
                return (res != 0) || (!comparePositions) ? res : (posA < posB ? -1 : 1);
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
