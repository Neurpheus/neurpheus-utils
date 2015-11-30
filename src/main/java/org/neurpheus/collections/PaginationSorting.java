/*
 * Neurpheus - Utilities Package
 *
 * Copyright (C) 2009-2015 Jakub Strychowski
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

package org.neurpheus.collections;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Thanks to this class you don't need to sort a whole array to get only a small range of sorted
 * elements (a page).
 * <p>
 * For example, you can use methods from this class to speed up your web application. If the
 * application displays sorted data on a list, grid or table, then thanks to this class, you don't
 * need to sort the whole array to display only a single page. In many cases this can speed up
 * processing 10 times especially if data are stored in a memory and an user can change a sorting
 * column from the interface.
 * </p>
 * <p>
 * It is also possible to use this class to get k-lowest elements from an array or collection. In
 * fact, an algorithm used in this implementation is a variation of
 * <a href="https://en.wikipedia.org/wiki/Quickselect">the Quickselect algorithm</a>.
 * </p>
 *
 * @author Jakub Strychowski
 */
public final class PaginationSorting {

    // Suppresses default constructor, ensuring non-instantiability.
    private PaginationSorting() {

    }

    /**
     * Sorts a fragment of the given array in such a way, that the the specified fragment will
     * contain the same elements as in case of sorting the whole array.
     *
     * @param intArray       The array to be sorted
     * @param pageStartIndex The position of first element (inclusive) in the sorted page.
     * @param pageEndIndex   The position of last element (exclusive) in the sorted page.
     */
    public static void sortPage(final int[] intArray, final int pageStartIndex,
                                final int pageEndIndex) {
        checkArguments(intArray, pageStartIndex, pageEndIndex);
        paginationSortInIntArray(intArray, pageStartIndex, pageEndIndex, 0, intArray.length - 1);
    }

    /**
     * Sorts a fragment of the given array in such a way, that the the specified fragment will
     * contain the same elements as in case of sorting the whole array.
     *
     * @param floatArray     The array to be sorted
     * @param pageStartIndex The position of first element (inclusive) in the sorted page.
     * @param pageEndIndex   The position of last element (exclusive) in the sorted page.
     */
    public static void sortPage(final float[] floatArray, final int pageStartIndex,
                                final int pageEndIndex) {
        checkArguments(floatArray, pageStartIndex, pageEndIndex);
        paginationSortInFloatArray(floatArray, pageStartIndex, pageEndIndex, 0,
                                   floatArray.length - 1);
    }

    /**
     * Sorts a fragment of the given array in such a way, that the the specified fragment will
     * contain the same elements as in case of sorting the whole array.
     *
     * @param shortArray     The array to be sorted
     * @param pageStartIndex The position of first element (inclusive) in the sorted page.
     * @param pageEndIndex   The position of last element (exclusive) in the sorted page.
     */
    public static void sortPage(final short[] shortArray, final int pageStartIndex,
                                final int pageEndIndex) {
        checkArguments(shortArray, pageStartIndex, pageEndIndex);
        paginationSortInShortArray(shortArray, pageStartIndex, pageEndIndex, 0,
                                   shortArray.length - 1);
    }

    /**
     * Sorts a fragment of the given array in such a way, that the the specified fragment will
     * contain the same elements as in case of sorting the whole array.
     *
     * @param longArray      The array to be sorted
     * @param pageStartIndex The position of first element (inclusive) in the sorted page.
     * @param pageEndIndex   The position of last element (exclusive) in the sorted page.
     */
    public static void sortPage(final long[] longArray, final int pageStartIndex,
                                final int pageEndIndex) {
        checkArguments(longArray, pageStartIndex, pageEndIndex);
        paginationSortInLongArray(longArray, pageStartIndex, pageEndIndex, 0, longArray.length - 1);
    }

    /**
     * Sorts a fragment of the given array in such a way, that the the specified fragment will
     * contain the same elements as in case of sorting the whole array.
     *
     * @param charArray      The array to be sorted
     * @param pageStartIndex The position of first element (inclusive) in the sorted page.
     * @param pageEndIndex   The position of last element (exclusive) in the sorted page.
     */
    public static void sortPage(final char[] charArray, final int pageStartIndex,
                                final int pageEndIndex) {
        checkArguments(charArray, pageStartIndex, pageEndIndex);
        paginationSortInCharArray(charArray, pageStartIndex, pageEndIndex, 0, charArray.length - 1);
    }

    /**
     * Sorts a fragment of the given array in such a way, that the the specified fragment will
     * contain the same elements as in case of sorting the whole array.
     *
     * @param doubleArray    The array to be sorted
     * @param pageStartIndex The position of first element (inclusive) in the sorted page.
     * @param pageEndIndex   The position of last element (exclusive) in the sorted page.
     */
    public static void sortPage(final double[] doubleArray, final int pageStartIndex,
                                final int pageEndIndex) {
        checkArguments(doubleArray, pageStartIndex, pageEndIndex);
        paginationSortInDoubleArray(doubleArray, pageStartIndex, pageEndIndex, 0,
                                    doubleArray.length - 1);
    }

    /**
     * Sorts a fragment of the given array in such a way, that the the specified fragment will
     * contain the same elements as in case of sorting the whole array.
     * <p>
     * Sorts the specified array into ascending order, according to the
     * {@linkplain Comparable natural ordering} of its elements. All elements in the array must
     * implement the {@link Comparable} interface. Furthermore, all elements in the array must be
     * <i>mutually comparable</i> (that is,
     * <tt>e1.compareTo(e2)</tt> must not throw a <tt>ClassCastException</tt>
     * for any elements <tt>e1</tt> and <tt>e2</tt> in the array).
     * </p>
     *
     * @param objectsArray   The array to be sorted
     * @param pageStartIndex The position of first element (inclusive) in the sorted page.
     * @param pageEndIndex   The position of last element (exclusive) in the sorted page.
     */
    public static void sortPage(final Object[] objectsArray, final int pageStartIndex,
                                final int pageEndIndex) {
        checkArguments(objectsArray, pageStartIndex, pageEndIndex);
        paginationSortInObjectArray(objectsArray, pageStartIndex, pageEndIndex, 0,
                                    objectsArray.length - 1);
    }

    /**
     * Sorts a fragment of the given array in such a way, that the the specified fragment will
     * contain the same elements as in case of sorting the whole array.
     *
     * @param <T>            Any type
     * @param genericArray   The array to be sorted
     * @param comparator     The comparator used for sorting
     * @param pageStartIndex The position of first element (inclusive) in the sorted page.
     * @param pageEndIndex   The position of last element (exclusive) in the sorted page.
     */
    public static <T> void sortPage(final T[] genericArray,
                                    Comparator<? super T> comparator,
                                    final int pageStartIndex,
                                    final int pageEndIndex) {
        checkArguments(genericArray, pageStartIndex, pageEndIndex);
        paginationSort(genericArray, comparator, pageStartIndex, pageEndIndex, 0,
                       genericArray.length - 1);
    }

    /**
     * Sorts a fragment of the given list in such a way, that the the specified fragment will
     * contain the same elements as in case of sorting the whole list.
     *
     * @param <T>            Any type
     * @param dataList       The collection to be sorted
     * @param pageStartIndex The position of first element (inclusive) in the sorted page.
     * @param pageEndIndex   The position of last element (exclusive) in the sorted page.
     */
    public static <T extends Comparable<? super T>> void sortPage(List<T> dataList,
                                                                  int pageStartIndex,
                                                                  int pageEndIndex) {
        Object[] tmp = dataList.toArray();
        sortPage(tmp, pageStartIndex, pageEndIndex);
        for (int i = 0; i < tmp.length; i++) {
            dataList.set(i, (T) tmp[i]);
        }
    }

    /**
     * Sorts a fragment of the given list in such a way, that the the specified fragment will
     * contain the same elements as in case of sorting the whole list.
     *
     * @param <T>            Any type
     * @param dataList       The collection to be sorted
     * @param comparator     The comparator used for sorting
     * @param pageStartIndex The position of first element (inclusive) in the sorted page.
     * @param pageEndIndex   The position of last element (exclusive) in the sorted page.
     */
    public static <T> void sortPage(List<T> dataList,
                                    Comparator<? super T> comparator,
                                    int pageStartIndex, int pageEndIndex) {
        @SuppressWarnings("unchecked")
        T[] tmp = (T[]) dataList.toArray();
        PaginationSorting.sortPage(tmp, comparator, pageStartIndex, pageEndIndex);
        for (int i = 0; i < tmp.length; i++) {
            dataList.set(i, tmp[i]);
        }
    }

    private static void checkArguments(final Object data, final int pageStartIndex,
                                       final int pageEndIndex) {
        if (data == null) {
            throw new NullPointerException("The 'data' argument cannot be null");
        }
        if (pageStartIndex < 0) {
            throw new ArrayIndexOutOfBoundsException(pageStartIndex);
        }
        if (pageEndIndex >= Array.getLength(data)) {
            throw new ArrayIndexOutOfBoundsException(pageEndIndex);
        }
        if (pageStartIndex >= pageEndIndex) {
            throw new ArrayIndexOutOfBoundsException(pageEndIndex);
        }
    }

    /**
     * Performs pagination sorting in the specified area of an integers' array.
     *
     * @param intArray       The array to be sorted
     * @param pageStartIndex The position of first element (inclusive) in the sorted page.
     * @param pageEndIndex   The position of last element (exclusive) in the sorted page.
     * @param lo             The position of first element (inclusive) in the processed area.
     * @param hi             The position of last element (inclusive) in the processed area.
     *
     */
    @SuppressWarnings({"squid:MethodCyclomaticComplexity", "common-java:DuplicatedBlocks"})
    private static void paginationSortInIntArray(final int[] intArray,
                                                 final int pageStartIndex,
                                                 final int pageEndIndex,
                                                 final int lo, final int hi) {
        if (hi - lo < pageEndIndex - pageStartIndex) {
            Arrays.sort(intArray, lo, hi + 1);
            return;
        }
        int left = lo;
        int right = hi;
        int pivotIndex = (lo + hi) / 2;
        int pivot = intArray[pivotIndex];
        int tmp;
        //  partition
        do {
            while (intArray[left] < pivot) {
                left++;
            }
            while (intArray[right] > pivot) {
                right--;
            }
            if (left <= right) {
                tmp = intArray[left];
                intArray[left] = intArray[right];
                intArray[right] = tmp;
                left++;
                right--;
            }
        } while (left <= right);
        //  recursion
        if (lo < right && right >= pageStartIndex) {
            paginationSortInIntArray(intArray, pageStartIndex, pageEndIndex, lo, right);
        }
        if (left < hi && left <= pageEndIndex) {
            paginationSortInIntArray(intArray, pageStartIndex, pageEndIndex, left, hi);
        }
    }

    /**
     * Performs pagination sorting in the specified area of a floats' array.
     *
     * @param floatArray     The array to be sorted
     * @param pageStartIndex The position of first element (inclusive) in the sorted page.
     * @param pageEndIndex   The position of last element (exclusive) in the sorted page.
     * @param lo             The position of first element (inclusive) in the processed area.
     * @param hi             The position of last element (inclusive) in the processed area.
     */
    @SuppressWarnings({"squid:MethodCyclomaticComplexity", "common-java:DuplicatedBlocks"})
    private static void paginationSortInFloatArray(final float[] floatArray,
                                                   final int pageStartIndex,
                                                   final int pageEndIndex,
                                                   final int lo, final int hi) {
        if (hi - lo < pageEndIndex - pageStartIndex) {
            Arrays.sort(floatArray, lo, hi + 1);
            return;
        }
        int left = lo;
        int right = hi;
        float tmp;
        float pivot = floatArray[(lo + hi) / 2];
        //  partition
        do {
            while (floatArray[left] < pivot) {
                left++;
            }
            while (floatArray[right] > pivot) {
                right--;
            }
            if (left <= right) {
                tmp = floatArray[left];
                floatArray[left] = floatArray[right];
                floatArray[right] = tmp;
                left++;
                right--;
            }
        } while (left <= right);
        //  recursion
        if (lo < right && right >= pageStartIndex) {
            paginationSortInFloatArray(floatArray, pageStartIndex, pageEndIndex, lo, right);
        }
        if (left < hi && left <= pageEndIndex) {
            paginationSortInFloatArray(floatArray, pageStartIndex, pageEndIndex, left, hi);
        }
    }

    /**
     * Performs pagination sorting in the specified area of a doubles' array.
     *
     * @param doubleArray    The array to be sorted
     * @param pageStartIndex The position of first element (inclusive) in the sorted page.
     * @param pageEndIndex   The position of last element (exclusive) in the sorted page.
     * @param lo             The position of first element (inclusive) in the processed area.
     * @param hi             The position of last element (inclusive) in the processed area.
     */
    @SuppressWarnings({"squid:MethodCyclomaticComplexity", "common-java:DuplicatedBlocks"})
    private static void paginationSortInDoubleArray(final double[] doubleArray,
                                                    final int pageStartIndex,
                                                    final int pageEndIndex,
                                                    final int lo, final int hi) {
        if (hi - lo < pageEndIndex - pageStartIndex) {
            Arrays.sort(doubleArray, lo, hi + 1);
            return;
        }
        int left = lo;
        int right = hi;
        double tmp;
        double pivot = doubleArray[(lo + hi) / 2];
        //  partition
        do {
            while (doubleArray[left] < pivot) {
                left++;
            }
            while (doubleArray[right] > pivot) {
                right--;
            }
            if (left <= right) {
                tmp = doubleArray[left];
                doubleArray[left] = doubleArray[right];
                doubleArray[right] = tmp;
                left++;
                right--;
            }
        } while (left <= right);
        //  recursion
        if (lo < right && right >= pageStartIndex) {
            paginationSortInDoubleArray(doubleArray, pageStartIndex, pageEndIndex, lo, right);
        }
        if (left < hi && left <= pageEndIndex) {
            paginationSortInDoubleArray(doubleArray, pageStartIndex, pageEndIndex, left, hi);
        }
    }

    /**
     * Performs pagination sorting in the specified area of a shorts' array.
     *
     * @param shortArray     The array to be sorted
     * @param pageStartIndex The position of first element (inclusive) in the sorted page.
     * @param pageEndIndex   The position of last element (exclusive) in the sorted page.
     * @param lo             The position of first element (inclusive) in the processed area.
     * @param hi             The position of last element (inclusive) in the processed area.
     */
    @SuppressWarnings({"squid:MethodCyclomaticComplexity", "common-java:DuplicatedBlocks"})
    private static void paginationSortInShortArray(final short[] shortArray,
                                                   final int pageStartIndex,
                                                   final int pageEndIndex,
                                                   final int lo, final int hi) {
        if (hi - lo < pageEndIndex - pageStartIndex) {
            Arrays.sort(shortArray, lo, hi + 1);
            return;
        }
        int left = lo;
        int right = hi;
        short tmp;
        short pivot = shortArray[(lo + hi) / 2];
        //  partition
        do {
            while (shortArray[left] < pivot) {
                left++;
            }
            while (shortArray[right] > pivot) {
                right--;
            }
            if (left <= right) {
                tmp = shortArray[left];
                shortArray[left] = shortArray[right];
                shortArray[right] = tmp;
                left++;
                right--;
            }
        } while (left <= right);
        //  recursion
        if (lo < right && right >= pageStartIndex) {
            paginationSortInShortArray(shortArray, pageStartIndex, pageEndIndex, lo, right);
        }
        if (left < hi && left <= pageEndIndex) {
            paginationSortInShortArray(shortArray, pageStartIndex, pageEndIndex, left, hi);
        }
    }

    /**
     * Performs pagination sorting in the specified area of a longs' array.
     *
     * @param longArray      The array to be sorted
     * @param pageStartIndex The position of first element (inclusive) in the sorted page.
     * @param pageEndIndex   The position of last element (exclusive) in the sorted page.
     * @param lo             The position of first element (inclusive) in the processed area.
     * @param hi             The position of last element (inclusive) in the processed area.
     */
    @SuppressWarnings({"squid:MethodCyclomaticComplexity", "common-java:DuplicatedBlocks"})
    private static void paginationSortInLongArray(final long[] longArray,
                                                  final int pageStartIndex,
                                                  final int pageEndIndex,
                                                  final int lo, final int hi) {
        if (hi - lo < pageEndIndex - pageStartIndex) {
            Arrays.sort(longArray, lo, hi + 1);
            return;
        }
        int left = lo;
        int right = hi;
        long tmp;
        long pivot = longArray[(lo + hi) / 2];
        //  partition
        do {
            while (longArray[left] < pivot) {
                left++;
            }
            while (longArray[right] > pivot) {
                right--;
            }
            if (left <= right) {
                tmp = longArray[left];
                longArray[left] = longArray[right];
                longArray[right] = tmp;
                left++;
                right--;
            }
        } while (left <= right);
        //  recursion
        if (lo < right && right >= pageStartIndex) {
            paginationSortInLongArray(longArray, pageStartIndex, pageEndIndex, lo, right);
        }
        if (left < hi && left <= pageEndIndex) {
            paginationSortInLongArray(longArray, pageStartIndex, pageEndIndex, left, hi);
        }
    }

    /**
     * Performs pagination sorting in the specified area of a chars' array.
     *
     * @param charArray      The array to be sorted
     * @param pageStartIndex The position of first element (inclusive) in the sorted page.
     * @param pageEndIndex   The position of last element (exclusive) in the sorted page.
     * @param lo             The position of first element (inclusive) in the processed area.
     * @param hi             The position of last element (inclusive) in the processed area.
     */
    @SuppressWarnings({"squid:MethodCyclomaticComplexity", "common-java:DuplicatedBlocks"})
    private static void paginationSortInCharArray(final char[] charArray,
                                                  final int pageStartIndex,
                                                  final int pageEndIndex,
                                                  final int lo, final int hi) {
        if (hi - lo < pageEndIndex - pageStartIndex) {
            Arrays.sort(charArray, lo, hi + 1);
            return;
        }
        int left = lo;
        int right = hi;
        char tmp;
        char pivot = charArray[(lo + hi) / 2];
        //  partition
        do {
            while (charArray[left] < pivot) {
                left++;
            }
            while (charArray[right] > pivot) {
                right--;
            }
            if (left <= right) {
                tmp = charArray[left];
                charArray[left] = charArray[right];
                charArray[right] = tmp;
                left++;
                right--;
            }
        } while (left <= right);
        //  recursion
        if (lo < right && right >= pageStartIndex) {
            paginationSortInCharArray(charArray, pageStartIndex, pageEndIndex, lo, right);
        }
        if (left < hi && left <= pageEndIndex) {
            paginationSortInCharArray(charArray, pageStartIndex, pageEndIndex, left, hi);
        }
    }

    /**
     * Performs pagination sorting in the specified area of a objects' array.
     *
     * @param objectsArray   The array to be sorted
     * @param pageStartIndex The position of first element (inclusive) in the sorted page.
     * @param pageEndIndex   The position of last element (exclusive) in the sorted page.
     * @param lo             The position of first element (inclusive) in the processed area.
     * @param hi             The position of last element (inclusive) in the processed area.
     */
    @SuppressWarnings({"squid:MethodCyclomaticComplexity", "common-java:DuplicatedBlocks"})
    private static void paginationSortInObjectArray(final Object[] objectsArray,
                                                    final int pageStartIndex,
                                                    final int pageEndIndex,
                                                    final int lo, final int hi) {
        if (hi - lo < pageEndIndex - pageStartIndex) {
            Arrays.sort(objectsArray, lo, hi + 1);
            return;
        }
        int left = lo;
        int right = hi;
        Object tmp;
        Object pivot = objectsArray[(lo + hi) / 2];
        //  partition
        do {
            while (((Comparable) objectsArray[left]).compareTo(pivot) < 0) {
                left++;
            }
            while (((Comparable) objectsArray[right]).compareTo(pivot) > 0) {
                right--;
            }
            if (left <= right) {
                tmp = objectsArray[left];
                objectsArray[left] = objectsArray[right];
                objectsArray[right] = tmp;
                left++;
                right--;
            }
        } while (left <= right);
        //  recursion
        if (lo < right && right >= pageStartIndex) {
            paginationSortInObjectArray(objectsArray, pageStartIndex, pageEndIndex, lo, right);
        }
        if (left < hi && left <= pageEndIndex) {
            paginationSortInObjectArray(objectsArray, pageStartIndex, pageEndIndex, left, hi);
        }
    }

    /**
     * Performs pagination sorting in the specified area of a doubles' array.
     *
     * @param genericArray   The array to be sorted
     * @param pageStartIndex The position of first element (inclusive) in the sorted page.
     * @param pageEndIndex   The position of last element (exclusive) in the sorted page.
     * @param lo             The position of first element (inclusive) in the processed area.
     * @param hi             The position of last element (inclusive) in the processed area.
     */
    @SuppressWarnings({"squid:MethodCyclomaticComplexity", "common-java:DuplicatedBlocks"})
    private static <T> void paginationSort(final T[] genericArray,
                                           final Comparator<? super T> comparator,
                                           final int pageStartIndex,
                                           final int pageEndIndex,
                                           final int lo, final int hi) {
        if (hi - lo < pageEndIndex - pageStartIndex) {
            Arrays.sort(genericArray, lo, hi + 1, comparator);
            return;
        }
        int left = lo;
        int right = hi;
        T tmp;
        T pivot = genericArray[(lo + hi) / 2];
        //  partition
        do {
            while (comparator.compare(genericArray[left], pivot) < 0) {
                left++;
            }
            while (comparator.compare(genericArray[right], pivot) > 0) {
                right--;
            }
            if (left <= right) {
                tmp = genericArray[left];
                genericArray[left] = genericArray[right];
                genericArray[right] = tmp;
                left++;
                right--;
            }
        } while (left <= right);
        //  recursion
        if (lo < right && right >= pageStartIndex) {
            paginationSort(genericArray, comparator, pageStartIndex, pageEndIndex, lo, right);
        }
        if (left < hi && left <= pageEndIndex) {
            paginationSort(genericArray, comparator, pageStartIndex, pageEndIndex, left, hi);
        }
    }

}
