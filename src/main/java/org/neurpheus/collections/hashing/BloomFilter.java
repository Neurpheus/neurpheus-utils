/*
 * Neurpheus - Utilities Package
 *
 * Copyright (C) 2004-2006 Sebastiano Vigna (base version implementation)
 * Copyright (C) 2006-2016 Jakub Strychowski (port from the MG4J project)
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

package org.neurpheus.collections.hashing;

import org.neurpheus.core.io.DataOutputStreamPacker;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;

/**
 * A Bloom filter.
 *
 * <P>
 * Instances of this class represent a set of character sequences or primitive-type arrays (with
 * false positives) using a Bloom filter. Because of the way Bloom filters work, you cannot remove
 * elements.
 * </p>
 * <p>
 * The intended usage is that character sequences and arrays should <em>not</em> be mixed (albeit in
 * principle it could work). A Bloom filter is rather agnostic with respect to the data it
 * contains&mdash;all it needs is a sequence of hash functions that can be applied to the data.
 * </p>
 * <P>
 * Bloom filters have an expected error rate, depending on the number of hash functions used, on the
 * filter size and on the number of elements in the filter. This implementation uses a variable
 * optimal number of hash functions, depending on the expected number of elements. More precisely, a
 * Bloom filter for <var>n</var> character sequences with <var>numberOfHashFunctions</var> hash
 * functions will use ln 2
 * <var>numberOfHashFunctions</var><var>n</var> &#8776; 1.44
 * <var>numberOfHashFunctions</var><var>n</var> bits; false positives will happen with probability
 * 2<sup>-<var>numberOfHashFunctions</var></sup>. The maximum number of bits supported is
 * {@link #MAX_BITS}.
 * </p>
 * <P>
 * Hash functions are generated at creation time using a mix of universal hashing and shift-add-xor
 * hashing (for the latter, see
 * <i>Performance in practice of string hashing functions</i>, by M.V. Ramakrishna and Justin Zobel,
 * <i>Proc. of the Fifth International Conference on Database Systems for Advanced Applications</i>,
 * 1997, pages 215&minus;223).
 * </p>
 * <p>
 * Each hash function uses {@link #NUMBER_OF_WEIGHTS} random integers, which are cyclically
 * multiplied by the character codes in a character sequence. The resulting integers are then summed
 * with a shifted hash and XOR-ed together.
 * </p>
 *
 * <P>
 * This class exports access methods that are similar to those of {@link java.util.Set}, but it does
 * not implement that interface, as too many non-optional methods wouldn't be implementable (e.g.,
 * iterators).
 * </p>
 *
 * <P>
 * A main method makes it easy to create serialized Bloom filters starting from a list of terms.
 * </p>
 *
 * @author Sebastiano Vigna
 */
public class BloomFilter implements Serializable {

    /** Unique serialization identifier of this class. */
    static final long serialVersionUID = 77_06_08_06_10_25_23_0515L;

    /** Number of bits used by the Long type. */
    static final int LONG_SIZE = 64;

    /** Version number of a serialized data structure supported by this class. */
    static final byte FORMAT_VERSION = 1;

    /**
     * The number of elements currently in the filter. It may be smaller than the actual number of
     * additions because of false positives.
     */
    private int size;

    /** The maximum number of bits in a filter (limited by array size and bits in a long). */
    public static final long MAX_BITS = (long) LONG_SIZE * Integer.MAX_VALUE;

    private static final long LOG2_LONG_SIZE = 6;

    private static final long BIT_INDEX_MASK = (1L << LOG2_LONG_SIZE) - 1L;

    /** The number of weights used to create hash functions. */
    private static final int NUMBER_OF_WEIGHTS = 16;

    /** The number of bits in this filter. */
    private long numberOfBits;

    /** The number of hash functions used by this filter. */
    private int numberOfHashFunctions;

    /** The underlying bit vector. */
    private long[] bits;
    /** The random integers used to generate the hash functions. */
    private int[][] weight;

    /** The random integers used to initialize the hash functions. */
    private int[] init;

    /** The natural logarithm of 2, used in the computation of the number of bits. */
    private static final double NATURAL_LOG_OF_2 = Math.log(2);

    /**
     * Creates a new high-precision Bloom filter.
     */
    public BloomFilter() {
        this(100);
    }

    /**
     * Creates a new high-precision Bloom filter a given expected number of elements.
     *
     * <p>
     * This constructor uses a number of hash functions that is logarithmic in the number of
     * expected elements. This usually results in no false positives at all.
     * </p>
     *
     * @param capacity the expected number of elements.
     */
    public BloomFilter(final int capacity) {
        this(capacity, calculateNumberOfHashFunctions(capacity));
    }

    /**
     * Creates a new Bloom filter with given number of hash functions and expected number of
     * elements.
     *
     * @param capacity              the expected number of elements.
     * @param numberOfHashFunctions the number of hash functions; under obvious uniformity and
     *                              independence assumptions, if the filter has not more than
     *                              <code>n</code> elements, false positives will happen with
     *                              probability 2<sup>-<var>numberOfHashFunctions</var></sup>.
     */
    public BloomFilter(final int capacity, final int numberOfHashFunctions) {
        this.numberOfHashFunctions = numberOfHashFunctions;
        final long wantedNumberOfBits
                = (long) Math.ceil(capacity * (numberOfHashFunctions / NATURAL_LOG_OF_2));
        if (wantedNumberOfBits > MAX_BITS) {
            throw new IllegalArgumentException(
                    String.format("The wanted number of bits (%d) is larger than %d",
                                  wantedNumberOfBits, MAX_BITS));
        }
        bits = new long[(int) ((wantedNumberOfBits + LONG_SIZE - 1) / LONG_SIZE)];
        numberOfBits = bits.length * (long) LONG_SIZE;

        // The purpose of Random().nextInt() is to generate a different seed at each invocation.
        final Random random = new Random();
        weight = new int[numberOfHashFunctions][];
        init = new int[numberOfHashFunctions];
        for (int i = 0; i < numberOfHashFunctions; i++) {
            weight[i] = new int[NUMBER_OF_WEIGHTS];
            init[i] = random.nextInt();
            for (int j = 0; j < NUMBER_OF_WEIGHTS; j++) {
                weight[i][j] = random.nextInt();
            }
        }
    }

    /**
     * Evaluates number of hash function need for the specified capacity of a structure.
     *
     * @param capacity Expected number of elements.
     *
     * @return number of recommended hash functions.
     */
    private static int calculateNumberOfHashFunctions(final int capacity) {
        int result = 0;
        int tmp = capacity;
        while (tmp > 0) {
            tmp = tmp >> 1;
            ++result;
        }
        return result;
    }

    /**
     * Returns the value of the bit with the specified index in the specified array.
     *
     * <p>
     * This method (and its companion {@link #set(long[], long)}) are static so that the bit array
     * can be cached by the caller in a local variable.
     * </p>
     *
     * @param bits  array of bits stored as an array of long values.
     * @param index the bit index.
     *
     * @return the value of the bit of index <code>index</code>.
     */
    private static boolean get(long[] bits, long index) {
        return (bits[(int) (index >> LOG2_LONG_SIZE)] & (1L << (index & BIT_INDEX_MASK))) != 0;
    }

    /**
     * Sets the bit with specified index in the specified array.
     *
     * @param bits  array of bits stored as an array of long values.
     * @param index the bit index.
     *
     * @see #get(long[], long)
     */
    private boolean set(long[] bits, long index) {
        final int unit = (int) (index >> LOG2_LONG_SIZE);
        final long mask = 1L << (index & BIT_INDEX_MASK);
        final boolean result = (bits[unit] & mask) != 0;
        bits[unit] |= mask;
        return result;
    }

    /**
     * Hashes the given sequence with the given hash function.
     *
     * @param value a character sequence.
     * @param len   the length of <code>s</code>.
     * @param index a hash function index (smaller than {@link #numberOfHashFunctions}).
     *
     * @return the position in the filter corresponding to <code>s</code> for the hash function
     *         <code>k</code>.
     */
    private long hash(final CharSequence value, final int len, final int index) {
        final int[] w = weight[index];
        long hash = init[index];
        int pos = len;
        while (pos-- != 0) {
            hash ^= (hash << 5) + value.charAt(pos) * w[pos % NUMBER_OF_WEIGHTS] + (hash >>> 2);
        }
        return (hash & Long.MAX_VALUE) % numberOfBits;
    }

    /**
     * Hashes the given byte array with the given hash function.
     *
     * @param value a byte array.
     * @param len   the length of <code>s</code>.
     * @param index a hash function index (smaller than {@link #numberOfHashFunctions}).
     *
     * @return the position in the filter corresponding to <code>value</code> for the hash function
     *         <code>k</code>.
     */
    private long hash(final byte[] value, final int len, final int index) {
        final int[] w = weight[index];
        long hash = init[index];
        int pos = len;
        while (pos-- != 0) {
            hash ^= (hash << 5) + value[pos] * w[pos % NUMBER_OF_WEIGHTS] + (hash >>> 2);
        }
        return (hash & Long.MAX_VALUE) % numberOfBits;
    }

    /**
     * Hashes the given short array with the given hash function.
     *
     * @param value a short array.
     * @param len   the length of <code>s</code>.
     * @param index a hash function index (smaller than {@link #numberOfHashFunctions}).
     *
     * @return the position in the filter corresponding to <code>value</code> for the hash function
     *         <code>k</code>.
     */
    private long hash(final short[] value, final int len, final int index) {
        final int[] w = weight[index];
        long hash = init[index];
        int pos = len;
        while (pos-- != 0) {
            hash ^= (hash << 5) + value[pos] * w[pos % NUMBER_OF_WEIGHTS] + (hash >>> 2);
        }
        return (hash & Long.MAX_VALUE) % numberOfBits;
    }

    /**
     * Hashes the given character array with the given hash function.
     *
     * @param value a character array.
     * @param len   the length of <code>s</code>.
     * @param index a hash function index (smaller than {@link #numberOfHashFunctions}).
     *
     * @return the position in the filter corresponding to <code>value</code> for the hash function
     *         <code>k</code>.
     */
    private long hash(final char[] value, final int len, final int index) {
        final int[] w = weight[index];
        long hash = init[index];
        int pos = len;
        while (pos-- != 0) {
            hash ^= (hash << 5) + value[pos] * w[pos % NUMBER_OF_WEIGHTS] + (hash >>> 2);
        }
        return (hash & Long.MAX_VALUE) % numberOfBits;
    }

    /**
     * Hashes the given int array with the given hash function.
     *
     * @param value an int array.
     * @param len   the length of <code>s</code>.
     * @param index a hash function index (smaller than {@link #numberOfHashFunctions}).
     *
     * @return the position in the filter corresponding to <code>value</code> for the hash function
     *         <code>k</code>.
     */
    private long hash(final int[] value, final int len, final int index) {
        final int[] w = weight[index];
        long hash = init[index];
        int pos = len;
        while (pos-- != 0) {
            hash ^= (hash << 5) + value[pos] * w[pos % NUMBER_OF_WEIGHTS] + (hash >>> 2);
        }
        return (hash & Long.MAX_VALUE) % numberOfBits;
    }

    /**
     * Hashes the given long array with the given hash function.
     *
     * @param value a long array.
     * @param len   the length of <code>s</code>.
     * @param index a hash function index (smaller than {@link #numberOfHashFunctions}).
     *
     * @return the position in the filter corresponding to <code>value</code> for the hash function
     *         <code>k</code>.
     */
    private long hash(final long[] value, final int len, final int index) {
        final int[] w = weight[index];
        long hash = init[index];
        int pos = len;
        while (pos-- != 0) {
            hash ^= (hash << 5) + value[pos] * w[pos % NUMBER_OF_WEIGHTS] + (hash >>> 2);
        }
        return (hash & Long.MAX_VALUE) % numberOfBits;
    }

    /** Hashes the given float array with the given hash function.
     *
     * @param value a float array.
     * @param len   the length of <code>s</code>.
     * @param index a hash function index (smaller than {@link #numberOfHashFunctions}).
     *
     * @return the position in the filter corresponding to <code>value</code> for the hash function
     *         <code>k</code>.
     */
    private long hash(final float[] value, final int len, final int index) {
        final int[] w = weight[index];
        long hash = init[index];
        int pos = len;
        while (pos-- != 0) {
            hash ^= (hash << 5)
                    + Float.floatToRawIntBits(value[pos]) * w[pos % NUMBER_OF_WEIGHTS]
                    + (hash >>> 2);
        }
        return (hash & Long.MAX_VALUE) % numberOfBits;
    }

    /**
     * Hashes the given double array with the given hash function.
     *
     * @param value a double array.
     * @param len   the length of <code>s</code>.
     * @param index a hash function index (smaller than {@link #numberOfHashFunctions}).
     *
     * @return the position in the filter corresponding to <code>value</code> for the hash function
     *         <code>k</code>.
     */
    private long hash(final double[] value, final int len, final int index) {
        final int[] w = weight[index];
        long hash = init[index];
        int pos = len;
        while (pos-- != 0) {
            hash ^= (hash << 5)
                    + Double.doubleToRawLongBits(value[pos]) * w[pos % NUMBER_OF_WEIGHTS]
                    + (hash >>> 2);
        }
        return (hash & Long.MAX_VALUE) % numberOfBits;
    }

    /**
     * Checks whether the given character sequence is in this filter.
     *
     * <P>
     * Note that this method may return true on a character sequence that has not been added to the
     * filter. This will happen with probability 2<sup>-<var>numberOfHashFunctions</var></sup>,
     * where <var>numberOfHashFunctions</var> is the number of hash functions specified at creation
     * time, if the number of the elements in the filter is less than <var>n</var>, the number of
     * expected elements specified at creation time.
     *
     * @param value a character sequence.
     *
     * @return true if <code>s</code> (or some element with the same hash sequence as
     *         <code>s</code>) is in the filter.
     */
    public boolean contains(final CharSequence value) {
        int hashFunction = numberOfHashFunctions;
        int len = value.length();
        long[] bitsArray = this.bits;  // getfield opt
        while (hashFunction-- != 0) {
            if (!get(bitsArray, hash(value, len, hashFunction))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether the given byte array is in this filter.
     *
     * @param value a byte array.
     *
     * @return true if <code>value</code> (or some element with the same hash sequence as
     *         <code>value</code>) is in the filter.
     *
     * @see #contains(CharSequence)
     */
    public boolean contains(final byte[] value) {
        int hashFunction = numberOfHashFunctions;
        int len = value.length;
        long[] bitsArray = this.bits; // getfield opt
        while (hashFunction-- != 0) {
            if (!get(bitsArray, hash(value, len, hashFunction))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether the given short array is in this filter.
     *
     * @param value a short array.
     *
     * @return true if <code>value</code> (or some element with the same hash sequence as
     *         <code>value</code>) is in the filter.
     *
     * @see #contains(CharSequence)
     */
    public boolean contains(final short[] value) {
        int hashFunction = numberOfHashFunctions;
        int len = value.length;
        long[] bitsArray = this.bits; // getfield opt
        while (hashFunction-- != 0) {
            if (!get(bitsArray, hash(value, len, hashFunction))) {
                return false;
            }
        }
        return true;
    }

    /** Checks whether the given character array is in this filter.
     *
     * @param value a character array.
     *
     * @return true if <code>value</code> (or some element with the same hash sequence as
     *         <code>value</code>) is in the filter.
     *
     * @see #contains(CharSequence)
     */
    public boolean contains(final char[] value) {
        int hashFunction = numberOfHashFunctions;
        int len = value.length;
        long[] bitsArray = this.bits; // getfield opt
        while (hashFunction-- != 0) {
            if (!get(bitsArray, hash(value, len, hashFunction))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether the given int array is in this filter.
     *
     * @param value an int array.
     *
     * @return true if <code>value</code> (or some element with the same hash sequence as
     *         <code>value</code>) is in the filter.
     *
     * @see #contains(CharSequence)
     */
    public boolean contains(final int[] value) {
        int hashFunction = numberOfHashFunctions;
        int len = value.length;
        long[] bitsArray = this.bits; // getfield opt
        while (hashFunction-- != 0) {
            if (!get(bitsArray, hash(value, len, hashFunction))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether the given long array is in this filter.
     *
     * @param value a long array.
     *
     * @return true if <code>value</code> (or some element with the same hash sequence as
     *         <code>value</code>) is in the filter.
     *
     * @see #contains(CharSequence)
     */
    public boolean contains(final long[] value) {
        int hashFunction = numberOfHashFunctions;
        int len = value.length;
        long[] bitsArray = this.bits; // getfield opt
        while (hashFunction-- != 0) {
            if (!get(bitsArray, hash(value, len, hashFunction))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether the given float array is in this filter.
     *
     * @param value a float array.
     *
     * @return true if <code>value</code> (or some element with the same hash sequence as
     *         <code>value</code>) is in the filter.
     *
     * @see #contains(CharSequence)
     */
    public boolean contains(final float[] value) {
        int hashFunction = numberOfHashFunctions;
        int len = value.length;
        long[] bitsArray = this.bits; // getfield opt
        while (hashFunction-- != 0) {
            if (!get(bitsArray, hash(value, len, hashFunction))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether the given double array is in this filter.
     *
     * @param value a double array.
     *
     * @return true if <code>value</code> (or some element with the same hash sequence as
     *         <code>value</code>) is in the filter.
     *
     * @see #contains(CharSequence)
     */
    public boolean contains(final double[] value) {
        int hashFunction = numberOfHashFunctions;
        int len = value.length;
        long[] bitsArray = this.bits; // getfield opt
        while (hashFunction-- != 0) {
            if (!get(bitsArray, hash(value, len, hashFunction))) {
                return false;
            }
        }
        return true;
    }

    /** Adds a character sequence to the filter.
     *
     * @param value a character sequence.
     *
     * @return true if this filter was modified (i.e., neither <code>s</code> nor any other element
     *         with the same hash sequence as <code>s</code> was already in this filter).
     */
    public boolean add(final CharSequence value) {
        int hashFunction = numberOfHashFunctions;
        int len = value.length();
        long[] bitsArray = this.bits; // getfield opt
        boolean alreadySet = true;
        while (hashFunction-- != 0) {
            alreadySet &= set(bitsArray, hash(value, len, hashFunction));
        }
        if (!alreadySet) {
            size++;
        }
        return !alreadySet;
    }

    /**
     * Adds a byte array to the filter.
     *
     * @param value a byte array.
     *
     * @return true if this filter was modified (i.e., neither <code>value</code> nor any other
     *         element with the same hash sequence as <code>value</code> was already in this
     *         filter).
     */
    public boolean add(final byte[] value) {
        int hashFunction = numberOfHashFunctions;
        int len = value.length;
        long[] bitsArray = this.bits; // getfield opt
        boolean alreadySet = true;
        while (hashFunction-- != 0) {
            alreadySet &= set(bitsArray, hash(value, len, hashFunction));
        }
        if (!alreadySet) {
            size++;
        }
        return !alreadySet;
    }

    /**
     * Adds a short array to the filter.
     *
     * @param value a short array.
     *
     * @return true if this filter was modified (i.e., neither <code>value</code> nor any other
     *         element with the same hash sequence as <code>value</code> was already in this
     *         filter).
     */
    public boolean add(final short[] value) {
        int hashFunction = numberOfHashFunctions;
        int len = value.length;
        long[] bitsArray = this.bits; // getfield opt
        boolean alreadySet = true;
        while (hashFunction-- != 0) {
            alreadySet &= set(bitsArray, hash(value, len, hashFunction));
        }
        if (!alreadySet) {
            size++;
        }
        return !alreadySet;
    }

    /**
     * Adds a character array to the filter.
     *
     * @param value a character array.
     *
     * @return true if this filter was modified (i.e., neither <code>value</code> nor any other
     *         element with the same hash sequence as <code>value</code> was already in this
     *         filter).
     */
    public boolean add(final char[] value) {
        int hashFunction = numberOfHashFunctions;
        int len = value.length;
        long[] bitsArray = this.bits; // getfield opt
        boolean alreadySet = true;
        while (hashFunction-- != 0) {
            alreadySet &= set(bitsArray, hash(value, len, hashFunction));
        }
        if (!alreadySet) {
            size++;
        }
        return !alreadySet;
    }

    /**
     * Adds an int array to the filter.
     *
     * @param value an int array.
     *
     * @return true if this filter was modified (i.e., neither <code>value</code> nor any other
     *         element with the same hash sequence as <code>value</code> was already in this
     *         filter).
     */
    public boolean add(final int[] value) {
        int hashFunction = numberOfHashFunctions;
        int len = value.length;
        long[] bitsArray = this.bits; // getfield opt
        boolean alreadySet = true;
        while (hashFunction-- != 0) {
            alreadySet &= set(bitsArray, hash(value, len, hashFunction));
        }
        if (!alreadySet) {
            size++;
        }
        return !alreadySet;
    }

    /**
     * Adds a long array to the filter.
     *
     * @param value a long array.
     *
     * @return true if this filter was modified (i.e., neither <code>value</code> nor any other
     *         element with the same hash sequence as <code>value</code> was already in this
     *         filter).
     */
    public boolean add(final long[] value) {
        int hashFunction = numberOfHashFunctions;
        int len = value.length;
        long[] bitsArray = this.bits; // getfield opt
        boolean alreadySet = true;
        while (hashFunction-- != 0) {
            alreadySet &= set(bitsArray, hash(value, len, hashFunction));
        }
        if (!alreadySet) {
            size++;
        }
        return !alreadySet;
    }

    /**
     * Adds a float array to the filter.
     *
     * @param value a float array.
     *
     * @return true if this filter was modified (i.e., neither <code>value</code> nor any other
     *         element with the same hash sequence as <code>value</code> was already in this
     *         filter).
     */
    public boolean add(final float[] value) {
        int hashFunction = numberOfHashFunctions;
        int len = value.length;
        long[] bitsArray = this.bits; // getfield opt
        boolean alreadySet = true;
        while (hashFunction-- != 0) {
            alreadySet &= set(bitsArray, hash(value, len, hashFunction));
        }
        if (!alreadySet) {
            size++;
        }
        return !alreadySet;
    }

    /**
     * Adds a double array to the filter.
     *
     * @param value a double array.
     *
     * @return true if this filter was modified (i.e., neither <code>value</code> nor any other
     *         element with the same hash sequence as <code>value</code> was already in this
     *         filter).
     */
    public boolean add(final double[] value) {
        int hashFunction = numberOfHashFunctions;
        int len = value.length;
        long[] bitsArray = this.bits; // getfield opt
        boolean alreadySet = true;
        while (hashFunction-- != 0) {
            alreadySet &= set(bitsArray, hash(value, len, hashFunction));
        }
        if (!alreadySet) {
            size++;
        }
        return !alreadySet;
    }

    /**
     * Clears this filter.
     */
    public void clear() {
        Arrays.fill(bits, 0);
        size = 0;
    }

    /**
     * Returns the size of this filter.
     *
     * <p>
     * Note that the size of a Bloom filter is only a <em>lower bound</em>
     * for the number of distinct elements that have been added to the filter. False positives might
     * make the number returned by this method smaller than it should be.
     * </p>
     *
     * @return the size of this filter.
     */
    public long size() {
        return size;
    }

    /**
     * Returns an estimated size of memory occupied by this bloom filter.
     *
     * @return Number of bytes occupied by internal structure of this object.
     */
    public int getAllocationSize() {
        int result = 56 + this.bits.length * 8 + this.init.length * 4 + this.weight.length * 12;
        for (int[] arr : this.weight) {
            result += 12 + arr.length * 4;
        }
        return result;
    }

    /**
     * Writes this collection into the specified data output stream.
     *
     * @param out The data output stream.
     *
     * @throws IOException if any i/o error occurred.
     */
    public void write(DataOutputStream out) throws IOException {
        out.writeByte(FORMAT_VERSION);
        DataOutputStreamPacker.writeInt(size, out);
        DataOutputStreamPacker.writeInt((int) numberOfBits, out);
        DataOutputStreamPacker.writeInt(numberOfHashFunctions, out);
        DataOutputStreamPacker.writeArrayOfLongs(bits, out);
        DataOutputStreamPacker.writeArrayOfIntegers(init, out);
        DataOutputStreamPacker.writeInt(weight.length, out);
        for (int[] val : weight) {
            DataOutputStreamPacker.writeArrayOfIntegers(val, out);
        }
    }

    /**
     * Reads internal data of this collection from the specified data input stream.
     *
     * @param in The data input stream where bloom filter was serialized.
     *
     * @throws IOException if any i/o error occurred.
     */
    public void read(DataInputStream in) throws IOException {
        if (FORMAT_VERSION != in.readByte()) {
            throw new IOException("Invalid file format");
        }
        size = DataOutputStreamPacker.readInt(in);
        numberOfBits = DataOutputStreamPacker.readInt(in);
        numberOfHashFunctions = DataOutputStreamPacker.readInt(in);
        bits = DataOutputStreamPacker.readArrayOfLongs(in);
        init = DataOutputStreamPacker.readArrayOfIntegers(in);
        int len = DataOutputStreamPacker.readInt(in);
        weight = new int[len][];
        for (int i = 0; i < len; i++) {
            weight[i] = DataOutputStreamPacker.readArrayOfIntegers(in);
        }
    }

}
