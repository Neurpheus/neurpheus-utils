/*		 
 * Neurpheus - Utilities Package
 *
 * Copyright (C) 2002, 2003, 2004 Paolo Boldi and Sebastiano Vigna 
 * Copyright (C) 2006-2015 Jakub Strychowski
 *
 *  This class is a derivated work based on the MG4J project version 0.9.2.
 *  This class is a modification of the BytesString class.
 *  Modifications mainly can be presented in the the following points:
 *  - string are stored as arrays of bytes instead of characters,
 *  - this class uses the Char2ByteConverter to encode and decode strings to bytes,
 *  - dependencies from other libraries used by the BytesString are removed.
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
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.neurpheus.core.string;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.neurpheus.core.charset.DynamicCharset;

/** Fast, compact, optimised &amp; versatile mutable strings.
 * <p>
 * This class is a modification of the MutableString class derivated from the MG4J project version
 * 0.9.2. Modifications can be mainly expressed in the the following points:
 * <ul>
 * <li>string are stored as arrays of bytes instead of characters,</li>
 * <li>this class uses the Char2ByteConverter to encode and decode strings,</li>
 * <li>dependencies from other libraries are removed.</li>
 * </ul>
 * <p>
 * The following text is a modified documentation of the orginal MutableString. Added paragraphs are
 * written in the italic format.
 * <p>
 *
 * <h3>Motivation</h3>
 *
 * <P>
 * The classical Java string classes, {@link java.lang.String} and {@link
 * java.lang.StringBuffer}, lie at the extreme of a spectrum (immutable and mutable).
 *
 * <P>
 * However, large-scale text opperations requires some features that are not provided by these
 * classes: in particular, the possibility of using a mutable string, once frozen, in the same
 * optimised way of an immutable string. Moreover, usually we do not need synchronisation (which
 * makes {@link java.lang.StringBuffer} slow).
 *
 * <P>
 * In a typical scenario you are dividing text into words (so you use a
 * <em>mutable</em> string to accumulate characters). Once you've got your word, you would like to
 * check whether this word is in a dictionary
 * <em>without creating a new object</em>. However, equality of <code>StringBuffer</code>s is not
 * defined on their content, and storing words after a conversion to <code>String</code> will not
 * help either, as then you would need to convert the current mutable string into an immutable one
 * (thus creating a new object) <em>before deciding whether you need to store it</em>.
 *
 * <P>
 * This class tries to make the best of both worlds, and thus aims at being a Better
 * Mousetrap&trade;.
 *
 * <p>
 * <i>
 * In most cases you are processing texts written in a particular language. This language uses a
 * particular alphabet which consist of signs. Each sign (a letter) is coded as a number. In the
 * Java language a single character is represented by the <code>char</code> primitive type. This
 * type consumes 2 bytes in most JVMs. Because, the number of signs in a given alphabet is probably
 * less then 127. it is possible to store characters on single bytes and reduce th size of a
 * consumed memory twice. This advantage is the main motivation for the modifications of the base
 * MutableString class.
 * </i>
 *
 *
 * <P>
 * You can read more details about the design of <code>MutableString</code> in
 * <a href="http://vigna.dsi.unimi.it/papers.php#BoVMSJ"><i>Mutable strings in Java: Design,
 * implementation and lightweight text-search algorithms</i></a>, by Paolo Boldi and Sebastiano
 * Vigna, to appear in <i>Science of Computer Programming</i>.
 *
 * <h3>Features</h3>
 *
 * Mutable strings come in two flavours: <em>compact</em> and
 * <em>loose</em>. A mutable string created by the empty constructor or the constructor specifying a
 * capacity is loose. All other constructors create compact mutable strings. In most cases, you can
 * completely forget whether your mutable strings are loose or compact and get good performance.
 *
 * <P>
 * <ul>
 *
 * <li>Mutable strings occupy little space&mdash; their only attributes are a backing byte array and
 * an integer (they are smaller of both <code>String</code>s and <code>StringBuffer</code>s);
 *
 * <li>they are not synchronised;
 *
 * <li>they let you access directly the backing array (at your own risk);
 *
 * <li><code>null</code>is not accepted as a string argument;
 *
 * <li>compact mutable strings have a slow growth; loose mutable strings have a fast growth;
 *
 * <li>hash codes of compact mutable strings are cached (for faster equality checks);
 *
 * <li>typical conversions such as trimming, upper/lower casing and replacements are made in place,
 * with minimal reallocations;
 *
 * <li>all methods try, whenever it is possible, to return <code>this</code>, so you can chain
 * methods as in <code>s.length(0).append("foo").append("bar")</code>;
 *
 * <li>you can write or print a mutable string without creating a <code>String</code> by using {@link #write(Writer, Char2ByteConverter)}, {@link
 * #print(PrintWriter, Char2ByteConverter)} and {@link #println(PrintWriter, Char2ByteConverter)};
 * you can read it back using {@link #read(Reader,int, Char2ByteConverter)}.
 *
 * <li>you can {@link #wrap(byte[]) wrap} any byte array into a mutable string;
 *
 * <li>this class is not final: thus, you can add your own methods to specialised versions.
 *
 * </ul>
 *
 * <h3>The Reallocation Heuristic</h3>
 *
 * <P>
 * Backing array reallocations use a heuristic based on looseness. Whenever an operation changes the
 * length, compact strings are resized to fit
 * <em>exactly</em> the new content, whereas the capacity of a loose string is never shortened, and
 * enlargements maximise the new length required with the double of the current capacity.
 *
 * <P>
 * The effect of this policy is that loose strings will get large buffers quickly, but compact
 * strings will occupy little space and perform very well in data structures using hash codes.
 *
 * <P>
 * For instance, you can easily reuse a loose mutable string calling {@link
 * #length(int) length(0)} (which does <em>not</em> reallocate the backing array).
 *
 * <P>
 * In any case, you can call {@link #compact()} and {@link #loose()} to force the respective
 * condition.
 *
 * <h3>Disadvantages</h3>
 *
 * <P>
 * The main disadvantage of mutable strings is that their substrings cannot share their backing
 * arrays, so if you need to generate many substrings you may want to use <code>String</code>.
 * However, {@link
 * #subSequence(int,int) subSequence()} returns a {@link CharSequence} that shares the backing
 * array.
 *
 * <h3>Warnings</h3>
 *
 * There are a few differences with standard string classes you should be aware of.
 *
 * <ol>
 *
 * <li><STRONG>This class is not synchronised</STRONG>. If multiple threads access an object of this
 * class concurrently, and at least one of the threads modifies it, it must be synchronised
 * externally.
 *
 * <li>This class implements polymorphic versions of the {@link #equals(Object)
 * equals} method that compare the <em>content</em> of <code>String</code>s and
 * <code>CharSequence</code>s, so that you can easily do checks like
 *
 * <PRE>
 *         MutableString.equals("Hello")
 * </PRE>
 *
 * Thus, you must <em>not</em> mix mutable strings with <code>CharSequence</code>s in collections as
 * equality between objects of those types is not symmetric. The same holds for the {@link #compareTo(Object)
 * compareTo} method.
 *
 * <li>When the length of a string or char array argument is zero, some methods may just do nothing
 * even if other parameters are out of bounds.
 *
 * <li>Even if this class is not final, most <em>methods</em> are declared final for efficiency, so
 * you cannot override them (why should you ever want to override {@link #array()}?).
 *
 * </ol>
 *
 * <h3>Benchmarking of MutableString class</h3>
 *
 * <P>
 * Benchmarking a string class is an almost impossible task, as patterns of usage may vary wildly
 * from application to application. Here we give some simple data about text scanning: we want to
 * count the number of occurrences of each word (a maximal subsequence of characters satisfying {@link
 * Character#isLetterOrDigit(char) isLetterOrDigit()}) in a file of about 200 Mbytes.
 * <pre>
 * $ java it.unimi.dsi.mg4j.test.StringSpeedTest &lt;/tmp/test
 * Read 30090870 words in 33328 ms (902843.4696510546 words/s)
 * $ java it.unimi.dsi.mg4j.test.MutableStringSpeedTest &lt;/tmp/test
 * Read 30090870 words in 12745 ms (2360994.1153393486 words/s)
 * </pre>
 * <P>
 * Note that 30% of the running time is spent in I/O latency and in {@link
 * Character#isLetterOrDigit(char) isLetterOrDigit()}, so the actual speedup is even greater (of
 * course, to get this performance you need a map from <a
 * href="http://fastutil.dsi.unimi.it/">fastutil</A>).
 *
 * @author Sebastiano Vigna - orginal MutableString class
 * @author Paolo Boldi - orginal MutableString class
 * @author Jakub Strychowski - array of bytes version
 */
public class MutableString implements Serializable, Comparable, Cloneable, CharSequence {

    /**
     * Identifies version of a mutable string in the serialized data.
     */
    static final long serialVersionUID = -318929984008928417L;

    /**
     * Holds empty array of bytes. This static field may be used for the representation of an empty
     * string.
     */
    private static final byte[] EMPTY_BYTES_ARRAY = new byte[0];

    /**
     * Holds name of the default charset which is used during mutable strings encoding and decoding.
     */
    private static String defaultCharsetName = "x-dynamic-charset";

    /**
     * The backing array which stores data of this mutable string.
     */
    private transient byte[] array;

    /**
     * This field holds eighter the length of the string or the hash code of the string.
     * <p>
     * if this attribute is negative, this mutable string is compact and the value is a hash code of
     * this string (<b>-1</b> denotes the invalid hash code). Positive attribute donotes a loose
     * string, and the value is the number of characters actually stored in the backing array.
     */
    private transient int hashLength;

    static {
        // check if dynamic charset can be used as defualt charset for
        // mutable strings
        try {
            DynamicCharset dchs = (DynamicCharset) Charset.forName("x-dynamic-charset");
            dchs.setInternational();
        } catch (Exception e) { //NOSONAR
            // use iso8859-1 charset as default mutable string charset
            MutableString.setDefaultCharsetName("iso8859-1");
        }
    }

    /**
     * Creates a new loose empty mutable string with capacity 2.
     */
    public MutableString() {
        this(2);
    }

    /**
     * Creates a new loose empty mutable string with given capacity.
     *
     * @param capacity The required capacity.
     */
    public MutableString(final int capacity) {
        array = capacity != 0 ? new byte[capacity] : EMPTY_BYTES_ARRAY;
    }

    /**
     * Returns the number of characters in this mutable string.
     *
     * @return the length of this mutable string.
     */
    @Override
    public final int length() {
        return hashLength >= 0 ? hashLength : array.length;
    }

    /**
     * Creates a new compact mutable string with given length.
     *
     * @param length the desired length of the new string.
     */
    private void makeCompactString(final int length) {
        if (length != 0) {
            if (array == null || array.length != length) {
                array = new byte[length];
            }
        } else {
            array = EMPTY_BYTES_ARRAY;
        }
        hashLength = -1;
    }

    /**
     * Ensures that at least the given number of characters can be stored in this mutable string.
     *
     * <P>
     * The new capacity of this string will be <em>exactly</em> equal to the provided argument if
     * this mutable string is compact (this differs markedly from {@link java.lang.StringBuffer#ensureCapacity(int)
     * StringBuffer}). If this mutable string is loose, the provided argument is maximised with the
     * current capacity doubled.</p>
     *
     * <P>
     * Note that if the given argument is greater than the current length, you will make this string
     * loose.</p>
     *
     * @param minimumCapacity We want at least this number of characters, but no more.
     *
     * @return This mutable string.
     */
    public MutableString ensureCapacity(final int minimumCapacity) {
        final int length = length();
        expand(minimumCapacity);
        if (length < minimumCapacity) {
            hashLength = length;
        }
        return this;
    }

    /**
     * Makes this mutable string loose.
     *
     * @return this mutable string.
     */
    public MutableString loose() {
        if (hashLength < 0) {
            hashLength = array.length;
        }
        return this;
    }

    /**
     * Returns whether this mutable string is compact.
     *
     * @return <code>true</code> if this mutable string is compact.
     */
    public boolean isCompact() {
        return hashLength < 0;
    }

    /**
     * Returns whether this mutable string is loose.
     *
     * @return <code>true</code> if this mutable string is loose.
     */
    public boolean isLoose() {
        return hashLength >= 0;
    }

    /**
     * Invalidates the current cached hash code if this mutable string is compact.
     *
     * <p>
     * You will need to call this method only if you change the backing array of a compact mutable
     * string directly.</p>
     *
     * @return This mutable string.
     */
    public MutableString changed() {
        if (hashLength < 0) {
            hashLength = -1;
        }
        return this;
    }

    /**
     * Wraps a given byte array in a compact mutable string.
     *
     * <p>
     * The returned mutable string will be compact and backed by the given byte array.</p>
     *
     * @param a A byte array to wrap.
     *
     * @return A compact mutable string backed by the given array.
     */
    public static MutableString wrap(final byte[] a) {
        MutableString s = new MutableString(0);
        s.array = a;
        s.hashLength = -1;
        return s;
    }

    /**
     * Wraps a given byte array for a given length in a loose mutable string.
     *
     * <p>
     * The returned mutable string will be loose and backed by the given byte array.</p>
     *
     * @param a      A byte array to wrap.
     * @param length A length of new mutable string.
     *
     * @return A loose mutable string backed by the given array with the given length.
     */
    public static MutableString wrap(final byte[] a, final int length) {
        MutableString s = new MutableString(0);
        s.array = a;
        s.hashLength = length;
        return s;
    }

    /**
     * Sets the length of this mutable string.
     *
     * <p>
     * If the provided length is greater than that of the current string, the string is padded with
     * zeros. If it is shorter, the string is truncated to the given length. We do <em>not</em>
     * reallocate the backing array, to increase object reuse. Use rather {@link #compact()} for
     * that purpose.</p>
     *
     * <p>
     * Note that shortening a string will make it loose.</p>
     *
     * @param newLength The new length for this mutable string.
     *
     * @return This mutable string.
     */
    public MutableString setLength(final int newLength) {
        if (newLength < 0) {
            throw new IllegalArgumentException("Negative length (" + newLength + ")");
        }
        if (hashLength < 0) {
            if (array.length == newLength) {
                return this;
            }
            hashLength = -1;
            // For compact strings, length and capacity coincide.
            setCapacity(newLength);
        } else {
            final int length = hashLength;
            if (newLength == length) {
                return this;
            }
            if (newLength > array.length) {
                // In this case, the array is already filled with zeroes.
                expand(newLength);
            } else if (newLength > length) {
                java.util.Arrays.fill(array, length, newLength, (byte) 0);
            }
            hashLength = newLength;
        }
        return this;
    }

    /**
     * Gets name of the default charset which is used during mutable strings encoding and decoding.
     * <p>
     * Initially, this method returns <b><i>x-dynamic-charset</i></b> value. This is the default
     * name of a dynamic charset registered in the JVM. Dynamic charsets ensures storing single
     * characters as single bytes if there is <b>no more then 255 signs</b> in a used alphabet.
     * Please, see {@link org.neurpheus.nlp.util.charset.DynamicCharset} to get more information
     * about dynamic charsets.
     *
     * @return Name of the charset used by all mutable strings to encode and decode strings.
     */
    public static String getDefaultCharsetName() {
        return MutableString.defaultCharsetName;
    }

    /**
     * Sets name of the default charset which is used during mutable strings encoding and decoding.
     *
     * @param charsetName New default name of a charset for mutable strings.
     */
    public static void setDefaultCharsetName(final String charsetName) {
        try {
            Charset cs = Charset.forName(charsetName);
            if (cs.newEncoder().averageBytesPerChar() > 1.0) {
                throw new IllegalArgumentException(
                        "Invalid charset name : " + charsetName
                        + "! You cannot use charset which consumes more"
                        + " then 1 byte for any character.");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        MutableString.defaultCharsetName = charsetName;
    }

    /**
     * Ensures that at least the given number of characters can be stored in this string.
     *
     * <p>
     * If necessary, enlarges the backing array. If the string is compact, we expand it exactly to
     * the given capacity; otherwise, expand to the maximum between the given capacity and the
     * double of the current capacity.</p>
     *
     * <p>
     * This method works even with a <code>null</code> backing array (which will be considered of
     * length 0).</p>
     *
     * <p>
     * After a call to this method, we may be in an inconsistent state: if you expand a compact
     * string, {@link #hashLength} will be negative, but there will be spurious characters in the
     * string. Be sure to fill them suitably.</p>
     *
     * @param minimumCapacity We want at least this number of characters.
     */
    private void expand(final int minimumCapacity) {
        // Array can be null during deserialization.
        final int c = array == null ? 0 : array.length;
        if (minimumCapacity <= c && array != null) {
            return;
        }
        final int length = hashLength >= 0 ? hashLength : c;
        final byte[] newArray
                = new byte[hashLength >= 0 && c * 2 > minimumCapacity ? c * 2 : minimumCapacity];
        if (length != 0) {
            System.arraycopy(array, 0, newArray, 0, length);
        }
        array = newArray;
    }

    /**
     * Ensures that exactly the given number of characters can be stored in this string.
     *
     * <p>
     * If necessary, reallocates the backing array. If the new capacity is smaller than the string
     * length, the string will be truncated.</p>
     *
     * <p>
     * After a call to this method, we may be in an inconsistent state: if you expand a compact
     * string, {@link #hashLength} will be negative, but there will be additional NUL characters in
     * the string. Be sure to substitute them suitably.</p>
     *
     * @param capacity We want exactly this number of characters.
     */
    private void setCapacity(final int capacity) {
        final int c = array.length;
        if (capacity == c) {
            return;
        }
        final int length = hashLength >= 0 ? hashLength : c;
        final byte[] newArray = capacity != 0 ? new byte[capacity] : EMPTY_BYTES_ARRAY;
        System.arraycopy(array, 0, newArray, 0, length < capacity ? length : capacity);
        array = newArray;
    }

    /**
     * Makes this mutable string compact.
     *
     * Note that this operation may require reallocating the backing array (of course, with a
     * shorter length).
     *
     * @return This mutable string.
     */
    public MutableString compact() {
        if (hashLength >= 0) {
            setCapacity(hashLength);
            hashLength = -1;
        }
        return this;
    }

    /**
     * Creates a new compact mutable string copying a given mutable string.
     *
     * @param s the initial contents of the string.
     */
    public MutableString(final MutableString s) {
        makeCompactString(s.length());
        System.arraycopy(s.array, 0, array, 0, array.length);
    }

    /**
     * Creates a new compact mutable string copying a given string.
     *
     * @param s the initial contents of the string.
     */
    public MutableString(final String s) {
        makeCompactString(s.length());
        try {
            System.arraycopy(
                    s.getBytes(MutableString.getDefaultCharsetName()),
                    0, array, 0, array.length);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Compares this mutable string to another object. If the argument is a character sequence, this
     * method performs a lexicographical comparison; otherwise, it throws a
     * <code>ClassCastException</code>.
     *
     * <P>
     * A potentially nasty consequence is that comparisons are not symmetric. See the discussion in
     * the {@linkplain MutableString class description}.
     *
     * @param o an object.
     *
     * @return if the argument is a character sequence, a negative integer, zero, or a positive
     *         integer as this mutable string is less than, equal to, or greater than the argument.
     *
     * @see #equals(Object)
     * @see java.lang.String#compareTo(Object)
     */
    @Override
    public final int compareTo(final Object o) {
        if (o instanceof MutableString) {
            return compareTo((MutableString) o);
        }
        // In the worst case, this will throw a ClassCastException.
        return compareTo((CharSequence) o);
    }

    /**
     * Type-specific version of {@link #compareTo(Object) compareTo()}.
     *
     * @param s a mutable string.
     *
     * @return a negative integer, zero, or a positive integer as this mutable string is less than,
     *         equal to, or greater than the specified mutable string.
     *
     * @see #compareTo(Object)
     */
    public final int compareTo(final MutableString s) {
        final int l1 = length();
        final int l2 = s.length();
        final int n = l1 < l2 ? l1 : l2;
        final byte[] a1 = array;
        final byte[] a2 = s.array;
        for (int i = 0; i < n; i++) {
            if (a1[i] != a2[i]) {
                return a1[i] - a2[i];
            }
        }
        return l1 - l2;
    }

    /**
     * Type-specific version of {@link #compareTo(Object) compareTo()}.
     *
     * @param s a string.
     *
     * @return a negative integer, zero, or a positive integer as this mutable string is less than,
     *         equal to, or greater than the specified character sequence.
     *
     * @see #compareTo(Object)
     */
    public final int compareTo(final String s) {
        return toString().compareTo(s);
    }

    /** Type-specific version of {@link #compareTo(Object) compareTo()}.
     *
     * @param s a character sequence.
     *
     * @return a negative integer, zero, or a positive integer as this mutable string is less than,
     *         equal to, or greater than the specified character sequence.
     *
     * @see #compareTo(Object)
     */
    public final int compareTo(final CharSequence s) {
        return toString().compareTo(s.toString());
    }

    /**
     * Defines the value by which hashcode is multipled before adding succeding character to the
     * hash code value.
     */
    private static final int HASH_CODE_MULTIPLER = 31;

    /**
     * Dentotes index of a bit which must be set for hash codes in the hashLength field.
     */
    private static final int HASH_CODE_SHIFT = 31;

    /**
     * Returns a hash code for this mutable string.
     *
     * <P>
     * The hash code of a mutable string is the same as that of a <code>String</code> with the same
     * content, but with the leftmost bit set.
     *
     * <P>
     * A compact mutable string caches its hash code, so it is very efficient on data structures
     * that check hash codes before invoking {@link java.lang.Object#equals(Object) equals()}.
     *
     * @return a hash code array for this object.
     *
     * @see java.lang.String#hashCode()
     */
    @Override
    public final int hashCode() {
        int h = hashLength;
        if (h >= -1) {
            final byte[] a = array;
            final int l = length();
            for (int i = h = 0; i < l; i++) {
                h = HASH_CODE_MULTIPLER * h + a[i];
            }
            h |= (1 << HASH_CODE_SHIFT);
            if (hashLength == -1) {
                hashLength = h;
            }
        }
        return h;
    }

    /**
     * Converts this mutable string to string using default mutable string charset.
     *
     * @return String representation of this mutable string.
     */
    @Override
    public final String toString() {
        if (array == null || length() == 0) {
            return "";
        }
        try {
            return new String(array, 0, length(), defaultCharsetName);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /** Writes a mutable string in serialised form.
     *
     * <P>
     * The serialised version of a mutable string is made of its length followed by its characters
     * (in UTF-16 format). Note that the compactness state is forgotten.
     *
     * <P>
     * Because of limitations of {@link ObjectOutputStream}, this method must write one character at
     * a time, and does not try to do any caching (in particular, it does not create any object). On
     * non-buffered data outputs it might be very slow.
     *
     * @param s a data output.
     *
     * @throws IOException if any error occurred while mutable string writing.
     */
    private void writeObject(final ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeInt(length());
        s.write(array);
    }

    /** Reads a mutable string in serialised form.
     *
     * <P>
     * Mutable strings produced by this method are always compact; this seems reasonable, as stored
     * strings are unlikely going to be changed.
     *
     * <P>
     * Because of limitations of {@link ObjectInputStream}, this method must read one character at a
     * time, and does not try to do any read-ahead (in particular, it does not create any object).
     * On non-buffered data inputs it might be very slow.
     *
     * @param s a data input.
     *
     * @throws IOException            if any error occurred while mutable string writing.
     * @throws ClassNotFoundException if the class of serialized object cannot be found.
     */
    private void readObject(final ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        final int length = s.readInt();
        // The new string will be compact.
        hashLength = -1;
        expand(length);
        s.readFully(array, 0, length);
    }

    /**
     * Gets backing array of this mutable string.
     *
     * @return backing array of this mutable string.
     */
    public byte[] getBackingArray() {
        return this.array;
    }

    /**
     * Returns a new mutable string.
     *
     * @return Empty mutable string.
     */
    public static MutableString newEmptyString() {
        return new MutableString(0);
    }

    /**
     * Compares this mutable string to the specified object. The result is <code>true</code> if and
     * only if the argument is not <code>null</code> and is a <code>MutableString</code> or
     * <code>String</code> or <code>CharSequence</code> object that represents the same sequence of
     * characters as this object.
     *
     * @param o The object to compare this <code>MutableString</code> against.
     *
     * @return <code>true</code> if the object is equal to this mutable string, <code>false</code>
     *         otherwise.
     */
    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (o == null) {
            return false;
        } else if (o instanceof MutableString) {
            return equalsMutableString((MutableString) o);
        } else if (o instanceof String) {
            return equalsString((String) o);
        } else if (o instanceof CharSequence) {
            return equalsCharSequence((CharSequence) o);
        } else {
            return false;
        }
    }

    /**
     * Compares this mutable string to the specified mutable string. The result is <code>true</code>
     * if and only if the argument is not <code>null</code> and the given mutable string represents
     * the same sequence of characters as this object.
     *
     * @param s The mutable string to compare this mutable string against.
     *
     * @return <code>true</code> if the given mutable string is equal to this mutable string,
     *         <code>false</code> otherwise.
     */
    public boolean equalsMutableString(final MutableString s) {
        if (s == this) {
            return true;
        } else if (s == null) {
            return false;
        } else if (length() != s.length()) {
            return false;
        } else {
            byte[] sa = s.getBackingArray();
            byte[] a = getBackingArray();
            for (int i = length() - 1; i >= 0; i--) {
                if (a[i] != sa[i]) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Compares this mutable string to the specified characters sequence. The result is
     * <code>true</code> if and only if the argument is not <code>null</code> and the given
     * characters sequence represents the same sequence of characters as this object.
     *
     * @param s The characters sequence to compare this mutable string against.
     *
     * @return <code>true</code> if the given characters sequence is equal to this mutable string,
     *         <code>false</code> otherwise.
     */
    public boolean equalsCharSequence(final CharSequence s) {
        if (s == null) {
            return false;
        } else if (length() != s.length()) {
            return false;
        } else {
            return toString().equals(s);
        }
    }

    /**
     * Compares this mutable string to the specified string. The result is <code>true</code> if and
     * only if the argument is not <code>null</code> and the given string represents the same
     * sequence of characters as this object.
     *
     * @param s The string to compare this mutable string against.
     *
     * @return <code>true</code> if the given string is equal to this mutable string,
     *         <code>false</code> otherwise.
     */
    public boolean equalsString(final String s) {
        if (s == null) {
            return false;
        } else if (length() != s.length()) {
            return false;
        } else {
            return toString().equals(s);
        }
    }

    /**
     * Return default charset used by all mutable strings.
     *
     * @return Default charset for all mutable strings.
     */
    public static Charset getDefaultCharset() {
        return Charset.forName(getDefaultCharsetName());
    }

    /**
     * Encodes given character to byte using default charset of mutable strings.
     *
     * @param c Character to encode.
     *
     * @return Single byte which represents given character.
     */
    public static byte getByte(final char c) {
        Charset charset = getDefaultCharset();
        if (charset instanceof DynamicCharset) {
            return ((DynamicCharset) charset).fastEncode(c);
        } else {
            CharBuffer cbuf = CharBuffer.wrap(new char[]{c});
            return charset.encode(cbuf).array()[0];
        }
    }

    /**
     * Decodes given character representation to character using default charset of mutable strings.
     *
     * @param b Character represnetation to decode.
     *
     * @return Decoded character.
     */
    public static char getChar(final byte b) {
        Charset charset = getDefaultCharset();
        if (charset instanceof DynamicCharset) {
            return ((DynamicCharset) charset).fastDecode(b);
        } else {
            ByteBuffer bbuf = ByteBuffer.wrap(new byte[]{b});
            return charset.decode(bbuf).array()[0];
        }
    }

    /**
     * Converts this mutable string to a new character array.
     *
     * @return Array of characters which are stored in this object.
     */
    public char[] toCharArray() {
        final Charset charset = getDefaultCharset();
        if (charset instanceof DynamicCharset) {
            return ((DynamicCharset) charset).fastDecodeToCharArray(array, 0, length());
        } else {
            final ByteBuffer bbuf = ByteBuffer.wrap(array, 0, length());
            return charset.decode(bbuf).array();
        }
    }

    /**
     * Gets a character representation from the given position.
     *
     * <p>
     * If you end up calling repeatedly this method, you should consider using
     * {@link #getBasckingArray()} and {@link #length()} instead.</p>
     *
     * @param index The index of a character.
     *
     * @return The chracter representation at that index.
     */
    public byte byteAt(final int index) {
        if (index >= length()) {
            throw new StringIndexOutOfBoundsException(index);
        }
        return array[index];
    }

    /**
     * Sets a character representation at the given position.
     *
     * @param index The index of a character.
     * @param c     The new character representation.
     *
     * @return This mutable string.
     */
    public MutableString setByteAt(final int index, final byte c) {
        if (index >= length()) {
            throw new StringIndexOutOfBoundsException(index);
        }
        array[index] = c;
        changed();
        return this;
    }

    /**
     * Returns the representaion of a first character of this mutable string. This method throws
     * StringIndexOutOfBoundsException when called on the empty string.
     *
     * @return The first character.
     */
    public byte firstByte() {
        if (length() == 0) {
            throw new StringIndexOutOfBoundsException(0);
        }
        return array[0];
    }

    /**
     * Returns the representation of a last character of this mutable string. This method throws
     * ArrayIndexOutOfBoundsException when called on the empty string.
     *
     * @return The last character.
     */
    public byte lastByte() {
        return array[length() - 1];
    }

    /**
     * Converts this string to a new byte array.
     *
     * @return A newly allocated byte array with the same length and content of this mutable string.
     */
    public byte[] toByteArray() {
        byte[] res = new byte[length()];
        System.arraycopy(array, 0, res, 0, length());
        return res;
    }

    /**
     * Returns a new mutable string that is a substring of this mutable string.
     *
     * <p>
     * The substring begins at the specified <code>beginIndex</code> and extends to the character at
     * index <code>endIndex - 1</code>. Thus the length of the substring is
     * <code>endIndex-beginIndex</code>. This method can throw StringIndexOutOfBoundsException if
     * the <code>beginIndex</code> is negative, or <code>endIndex</code> is larger than the length
     * of this object, or <code>beginIndex</code> is larger than <code>endIndex</code>.</p>
     *
     * @param beginIndex The beginning index, inclusive.
     * @param endIndex   The ending index, exclusive.
     *
     * @return The specified substring.
     */
    public MutableString substring(final int beginIndex, final int endIndex) {
        if (beginIndex < 0) {
            throw new StringIndexOutOfBoundsException(beginIndex);
        }
        if (endIndex > length()) {
            throw new StringIndexOutOfBoundsException(endIndex);
        }
        if (beginIndex > endIndex) {
            throw new StringIndexOutOfBoundsException(endIndex - beginIndex);
        }
        if ((beginIndex == 0) && (endIndex == length())) {
            return new MutableString(this);
        } else {
            byte[] tab = new byte[endIndex - beginIndex];
            System.arraycopy(array, beginIndex, tab, 0, endIndex - beginIndex);
            return MutableString.wrap(tab);
        }
    }

    /** Returns a substring of this mutable string.
     *
     * @param start first character of the substring (inclusive).
     *
     * @return a substring ranging from the given position to the end of this string.
     *
     * @see #substring(int,int)
     */
    public MutableString substring(final int start) {
        return substring(start, length());
    }

    /**
     * Splits this mutable string around whitespaces.
     *
     * <p>
     * Following characters are whitespaces for this method : ' ', '\t', '\n', '\r'.</p>
     *
     * <p>
     * Trailing empty strings are not included in the resulting array.</p>
     *
     * <p>
     * The string <tt>"boo and foo"</tt>, for example, yields the following result :
     * <tt>{ "boo", "and", foo" }</tt>.</p>
     *
     * @return The array of mutable strings computed by splitting this string around whitespaces.
     */
    public MutableString[] splitAtWhitespace() {
        byte space = MutableString.getByte(' ');
        byte newLine = MutableString.getByte('\n');
        byte tab = MutableString.getByte('\t');
        byte carretReturn = MutableString.getByte('\r');
        List<MutableString> res = new ArrayList<>();
        int length = length();
        int l = 0;
        int p = 0;
        while (p < length) {
            byte b = array[p];
            if (b == space || b == newLine || b == tab || b == carretReturn) {
                if (l < p) {
                    byte[] a = new byte[p - l];
                    System.arraycopy(array, l, a, 0, p - l);
                    res.add(MutableString.wrap(a));
                }
                l = p + 1;
            }
            ++p;
        }
        if (l < p) {
            byte[] a = new byte[p - l];
            System.arraycopy(array, l, a, 0, p - l);
            res.add(MutableString.wrap(a));
        }
        return (MutableString[]) res.toArray(new MutableString[0]);
    }

    /**
     * Returns whether this mutable string starts with the given mutable string.
     *
     * @param prefix A prefix of this mutable string.
     *
     * @return <code>true</code> if this mutable string starts with the <code>prefix</code>.
     */
    public boolean startsWith(final MutableString prefix) {
        final int l = prefix.length();
        if (l > length()) {
            return false;
        }
        int i = l;
        final byte[] a1 = prefix.array;
        final byte[] a2 = array;
        while (i-- != 0) {
            if (a1[i] != a2[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether this mutable string ends with the given mutable string.
     *
     * @param suffix A suffix of this mutable string.
     *
     * @return <code>true</code> if this mutable string ends with the <code>suffix</code>.
     */
    public boolean endsWith(final MutableString suffix) {
        final int l = suffix.length();
        int length = length();
        if (l > length) {
            return false;
        }
        int i = l;
        final byte[] a1 = suffix.array;
        final byte[] a2 = array;
        while (i-- != 0) {
            if (a1[i] != a2[--length]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the <code>char</code> value at the specified index.
     *
     * <p>
     * An index ranges from zero to <tt>length() - 1</tt>. The first <code>char</code> value of the
     * sequence is at index zero, the next at index one, and so on, as for array indexing. </p>
     *
     * <p>
     * This method throws IndexOutOfBoundsException if the <tt>index</tt> argument is negative or
     * not less than <tt>length()</tt>.</p>
     *
     * @param index The index of the <code>char</code> value to be returned.
     *
     * @return The specified <code>char</code> value.
     */
    @Override
    public char charAt(final int index) {
        return MutableString.getChar(byteAt(index));
    }

    /**
     * Returns a new <code>CharSequence</code> that is a subsequence of this sequence.
     *
     * <p>
     * The subsequence starts with the <code>char</code> value at the specified index and ends with
     * the <code>char</code> value at index <tt>end - 1</tt>. The length (in <code>char</code>s) of
     * the returned sequence is <tt>end - start</tt>, so if <tt>start == end</tt>
     * then an empty sequence is returned. </p>
     *
     * <p>
     * This method throws IndexOutOfBoundsException if <tt>start</tt> or <tt>end</tt> are negative,
     * if <tt>end</tt> is greater than <tt>length()</tt>, or if <tt>start</tt> is greater than
     * <tt>end</tt></p>
     *
     * @param beginIndex The start index, inclusive.
     * @param endIndex   The end index, exclusive.
     *
     * @return The specified subsequence.
     */
    @Override
    public CharSequence subSequence(final int beginIndex, final int endIndex) {
        return substring(beginIndex, endIndex);
    }

    /**
     * Tells whether or not this string matches the given regular expression.
     *
     * <p>
     * An invocation of this method of the form <i>str</i><tt>.matches(</tt><i>regex</i><tt>)</tt>
     * yields exactly the same result as the expression
     * <blockquote><tt> {@link java.util.regex.Pattern}.
     * {@link java.util.regex.Pattern#matches(String,CharSequence) matches}(</tt><i>regex</i><tt>,</tt>
     * <i>str</i><tt>)</tt></blockquote> </p>
     *
     * <p>
     * This method throws PatternSyntaxException if the regular expression's syntax is invalid</p>
     *
     * @param regex the regular expression to which this string is to be matched
     *
     * @return <tt>true</tt> if, and only if, this string matches the given regular expression
     */
    public boolean matches(final MutableString regex) {
        return Pattern.matches(regex.toString(), this);
    }

    /**
     * Tells whether or not this string matches the given regular expression.
     *
     * <p>
     * An invocation of this method of the form <i>str</i><tt>.matches(</tt><i>regex</i><tt>)</tt>
     * yields exactly the same result as the expression
     * <blockquote><tt> {@link java.util.regex.Pattern}.
     * {@link java.util.regex.Pattern#matches(String,CharSequence) matches}(</tt><i>regex</i><tt>,</tt>
     * <i>str</i><tt>)</tt></blockquote> </p>
     *
     * <p>
     * This method throws PatternSyntaxException if the regular expression's syntax is invalid</p>
     *
     * @param regex the regular expression to which this string is to be matched
     *
     * @return <tt>true</tt> if, and only if, this string matches the given regular expression
     */
    public boolean matches(final String regex) {
        return Pattern.matches(regex, this);
    }

    /**
     * Appends the given mutable string to this mutable string.
     *
     * @param s The mutable string to append.
     *
     * @return this mutable string.
     */
    public MutableString append(final MutableString s) {
        final int l = s.length();
        if (l == 0) {
            return this;
        }
        final int newLength = length() + l;
        expand(newLength);
        System.arraycopy(s.array, 0, array, newLength - l, l);
        hashLength = hashLength < 0 ? -1 : newLength;
        return this;
    }

    /**
     * Parses this mutable string as a signed decimal integer. The characters in the string must all
     * be decimal digits, except that the first character may be an ASCII minus sign
     * <code>'-'</code> (<code>'&#92;u002D'</code>) to indicate a negative value.
     *
     * This method throws NumberFormatException if the string does not contain a parsable integer.
     *
     * @return The integer value represented by this mutable string in decimal.
     */
    public int parseInt() {
        return Integer.parseInt(this.toString());
    }

    /**
     * Returns the first position of a character in this mutable string.
     *
     * @param c The character to find.
     *
     * @return Index of first occurrence of the given character or <code>-1</code> if character
     *         dosen't exist in this mutable string.
     */
    public int indexOf(final char c) {
        return indexOf(MutableString.getByte(c));
    }

    /**
     * Returns the first position of a character representation in this mutable string.
     *
     * @param b The character representation to find.
     *
     * @return Index of first occurrence of the given character representation or <code>-1</code> if
     *         character dosen't exist in this mutable string.
     */
    public int indexOf(final byte b) {
        final int l = length();
        for (int i = 0; i < l; i++) {
            if (array[i] == b) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the first position of the given mutable string in this mutable string.
     *
     * @param s The mutable string to find.
     *
     * @return Index of first occurrence of the given mutable string or <code>-1</code> if given
     *         string dosen't exist in this mutable string.
     */
    public int indexOf(final MutableString s) {
        final int maxpos = length() - s.length();
        if (maxpos >= 0 && s.length() > 0) {
            final byte[] a = array;
            final byte[] sa = s.array;
            final byte firstChar = sa[0];
            for (int i = 0; i <= maxpos; i++) {
                if (a[i] == firstChar) {
                    int j = s.length() - 1;
                    for (; j > 0; j--) {
                        if (sa[j] != a[i + j]) {
                            j = -1;
                        }
                    }
                    if (j == 0) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Appends the given character code to this mutable string.
     *
     * @param b The character code to append.
     *
     * @return this mutable string.
     */
    public MutableString append(final byte b) {
        final int newLength = length() + 1;
        expand(newLength);
        array[newLength - 1] = b;
        hashLength = hashLength < 0 ? -1 : newLength;
        return this;
    }

    /**
     * Appends the given character to this mutable string.
     *
     * @param c The character to append.
     *
     * @return this mutable string.
     */
    public MutableString append(final char c) {
        return append(getByte(c));
    }

    /**
     * Inserts the given character code to this mutable string at the given position.
     *
     * @param pos Position where character code should be inserted.
     * @param b   The character code to insert.
     *
     * @return this mutable string.
     */
    public MutableString insert(final int pos, final byte b) {
        final int l = length();
        expand(l + 1);
        System.arraycopy(array, pos, array, pos + 1, l - pos);
        array[pos] = b;
        hashLength = hashLength < 0 ? -1 : l + 1;
        return this;
    }

    /**
     * Inserts given character to this mutable string at the given position.
     *
     * @param pos Position where character should be inserted.
     * @param c   The character to insert.
     *
     * @return this mutable string.
     */
    public MutableString insert(final int pos, final char c) {
        return insert(pos, getByte(c));
    }

    /** Writes this mutable string into data stream.
     *
     * @param out The data output.
     *
     * @throws IOException if any error occurred while mutable string writing.
     */
    public void write(final DataOutputStream out) throws IOException {
        out.writeInt(length());
        out.write(array, 0, length());
    }

    /** Reads a mutable string from data stream.
     *
     * @param in The data input.
     *
     * @return The readed mutable string.
     *
     * @throws IOException if any error occurred while mutable string reading.
     */
    public static MutableString read(final DataInputStream in) throws IOException {
        final int length = in.readInt();
        byte[] a = new byte[length];
        in.readFully(a);
        return MutableString.wrap(a);
    }

    /**
     * Prints this mutable string to the given output.
     *
     * @param out The print output.
     */
    public void print(final PrintStream out) {
        out.print(toCharArray());
    }

    /**
     * Prints this mutable string to the given output and prints new line.
     *
     * @param out The print output.
     */
    public void println(final PrintStream out) {
        out.println(toCharArray());
    }

    /**
     * Read a line of text. A line is considered to be terminated by any one of a line feed ('\n'),
     * a carriage return ('\r'), or a carriage return followed immediately by a linefeed.
     *
     * @param reader The input stream reader.
     *
     * @return A MutableString containing the contents of the line, not including any
     *         line-termination characters, or null if the end of the stream has been reached
     *
     * @exception IOException If an I/O error occurs
     */
    public static MutableString readLine(final BufferedReader reader) throws IOException {
        String l = reader.readLine();
        if (l == null) {
            return null;
        } else {
            return new MutableString(l);
        }
    }

    /*
     * Returns a copy of the string, with leading and trailing whitespace omitted.
     *
     * @return The mutable string without whitespaces at its begining and at its end.
     */
    public MutableString trim() {
        int l = 0;
        int p = length() - 1;
        while (l <= p && Character.isWhitespace(getChar(array[l]))) {
            ++l;
        }
        while (p >= l && Character.isWhitespace(getChar(array[p]))) {
            --p;
        }
        if (l > p) {
            return MutableString.newEmptyString();
        } else {
            return substring(l, p + 1);
        }
    }

    /**
     * Reverses the order of characters in this string.
     *
     * @return this string.
     */
    public MutableString reverse() {
        int l = 0;
        int p = length() - 1;
        while (l < p) {
            byte b = array[l];
            array[l++] = array[p];
            array[p--] = b;
        }
        return this;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        MutableString result = (MutableString) super.clone();
        if ((result != null) && (result.array != EMPTY_BYTES_ARRAY)) {
            result.array = result.array.clone();
        }
        return result;
    }

}
