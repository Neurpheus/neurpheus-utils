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

package org.neurpheus.core.charset;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Arrays;

/**
 * Encodes and decodes Unicode characters using dynamically created mapping between characters and
 * bytes.
 * 
 * <p>
 * This class ensures that characters from a particular alphabet, which contains no more than 255
 * signs, can be encoded as single bytes. A mapping between characters and bytes changes dynamically
 * during strings encoding. All previously visible characters are encoded using available mappings.
 * If an encoded string contains unknown character, a new mapping for this character is created.
 * <p>
 * A particular byte code of an character depends on an order of characters presentation. Succeeding
 * unknown characters receive codes from 1 to 255.
 * </p>
 * <p>
 * After creation of 255 mappings or after setting <code>changeable</code> flag to
 * <code>false</code> no more unknown characters can be encoded. In such situation unknown
 * characters are encoded as 0. This code is decoded to the <code>'?'</code> character.
 * </p>
 * <p>
 * The final mapping can be saved to an output stream, and an instance of this class can be created
 * through the mapping reading from an input stream.
 * </p>
 * <p>
 * <b><i>
 * Each instance of the dynamic charset should have a unique canonical name because the SUN
 * implementation of the Charset.encode method uses cached encoders. This causes that new dynamic
 * charset instances, which have the same canonical name like the instance previously created, use
 * invalid encoder (encoder created for the first instance). For dynamic charsets it is important to
 * use an encoder created for a particular instance and not for all instances having the same
 * canonical names.
 * </i></b></p>
 *
 * @author Jakub Strychowski
 */
public class DynamicCharset extends Charset implements Serializable {

    /** The byte code of a character which dose not have mapping. */
    public static final byte UNKNOWN_CHARACTER_CODE = 0;

    /** The sign representing unknown character code. */
    public static final char UNKNOWN_CHARACTER = '?';

    /**
     * Maximum number of mappings which can be created. You can decrease this value if you want for
     * example to encode characters on few bits.
     */
    public static final int BYTE_MAX = 255;

    /** Value used for a conversion from bytes to unsigned bytes. */
    private static final int TO_BYTE = 0xFF;

    /**
     * Characters available on keyboard, Polish characters, whitespaces, numbers.
     */
    private static final String POLISH_ALPHABET = "ęóąśłżźćńĘÓĄŚŁŻŹĆŃ";
    
    /** Characters to bytes mapping array. */
    private byte[] char2byte;

    /** Bytes to characters mapping array. */
    private char[] byte2char;

    /** Maximal code of mapped byte. */
    private int maxByte;

    /** Maximal code of mapped character. */
    private char maxChar;

    /** A flag denoting if new mappings can be added. */
    private boolean changeable;

    /** Holds the encoder returned by this object. */
    private transient DynamicCharsetEncoder encoder;

    /** Holds the decoder returned by this object. */
    private transient DynamicCharsetDecoder decoder;

    /**
     * Initializes a new charset with the given canonical name and alias set.
     *
     * @param canonicalName The canonical name of this charset.
     * @param aliases       An array of this charset's aliases, or null if it has no aliases.
     */
    public DynamicCharset(final String canonicalName, final String[] aliases) {
        super(canonicalName, aliases);
        this.maxByte = 0;
        this.maxChar = UNKNOWN_CHARACTER;
        this.changeable = true;
        this.char2byte = new byte[(int) this.maxChar + 1];
        Arrays.fill(this.char2byte, UNKNOWN_CHARACTER_CODE);
        this.byte2char = new char[BYTE_MAX + 1];
        Arrays.fill(this.byte2char, UNKNOWN_CHARACTER);
        this.encoder = new DynamicCharsetEncoder(this);
        this.decoder = new DynamicCharsetDecoder(this);
        this.decoder.replaceWith(Character.toString(UNKNOWN_CHARACTER));
    }

    /**
     * Initializes a new charset with the given canonical name and alias set, and reads mapping
     * between chars and bytes from the input stream.
     *
     * @param canonicalName The canonical name of this charset.
     * @param aliases       An array of this charset's aliases, or null if it has no aliases.
     * @param in            The input stream from which a mapping and other charset data should be
     *                      read.
     *
     * @throws IOException if input error occurred.
     */
    public DynamicCharset(final String canonicalName, final String[] aliases,
                          final DataInputStream in)
            throws IOException {
        super(canonicalName, aliases);
        read(in);
        this.encoder = new DynamicCharsetEncoder(this);
        this.decoder = new DynamicCharsetDecoder(this);
    }

    /**
     * Constructs a new encoder for this charset.
     *
     * @return the encoder for this charset.
     */
    @Override
    public CharsetEncoder newEncoder() {
        return this.encoder;
    }

    /**
     * Constructs a new decoder for this charset.
     *
     * @return the decoder for this charset.
     */
    @Override
    public CharsetDecoder newDecoder() {
        return this.decoder;
    }

    /**
     * Tells whether or not this charset contains the given charset. A dynamic charset contains only
     * itself.
     *
     * @param cs Charset to check.
     *
     * @return <code>true</code> if, and only if, the given charset is contained in this charset.
     */
    @Override
    public boolean contains(final Charset cs) {
        return cs == this;
    }

    /**
     * Adds mapping for the given character. If the given character is unknown for this charset a
     * new mapping for the character is created. This method is synchronized to prevent creation of
     * many mappings for a single character what can occur if many threads encodes strings using
     * this object.
     *
     * @param ch A character which should be encoded by this this charset
     *
     * @return A byte code representing the character or {@link UNKNOWN_CHARACTER_CODE} if no more
     *         characters can be mapped by this charset.
     */
    public synchronized byte addCharacter(final char ch) {
        // make sure if another thread didn't add character
        if (ch <= this.maxChar && this.char2byte[ch] != UNKNOWN_CHARACTER_CODE) {
            return this.char2byte[ch];
        }
        if (this.maxByte < BYTE_MAX) {
            ++this.maxByte;
            this.byte2char[this.maxByte] = ch;
            if (ch > this.maxChar) {
                byte[] tmp = new byte[ch + 1];
                System.arraycopy(this.char2byte, 0, tmp, 0, this.char2byte.length);
                Arrays.fill(tmp, this.char2byte.length, tmp.length, UNKNOWN_CHARACTER_CODE);
                this.maxChar = ch;
                this.char2byte = tmp;
            }
            this.char2byte[ch] = (byte) this.maxByte;
            return (byte) this.maxByte;
        } else {
            this.changeable = false;
        }
        return UNKNOWN_CHARACTER_CODE;
    }

    /**
     * Encodes the given character to its byte code.
     *
     * @param ch A character to encode.
     *
     * @return Byte code representation of the character.
     */
    public byte fastEncode(final char ch) {
        byte res = (ch <= this.maxChar) ? this.char2byte[ch] : UNKNOWN_CHARACTER_CODE;
        return (this.changeable && res == UNKNOWN_CHARACTER_CODE) ? this.addCharacter(ch) : res;
    }

    /**
     * Decodes the given byte code to an encoded character.
     *
     * @param byteCode A byte code of a character.
     *
     * @return Encoded character.
     */
    public char fastDecode(final byte byteCode) {
        return byte2char[byteCode & TO_BYTE];
    }

    /**
     * Decodes given array of bytes to string.
     *
     * @param tab String in the encoded representation.
     *
     * @return Decoded string.
     */
    public String fastDecode(final byte[] tab) {
        return new String(fastDecodeToCharArray(tab, 0, tab.length));
    }

    /**
     * Decodes given array of bytes to string.
     *
     * @param tab   String in the encoded representation.
     * @param start The index in the tab array from which start to decode.
     * @param len   Number of characters to decode.
     *
     * @return Decoded string.
     */
    public String fastDecode(final byte[] tab, final int start, final int len) {
        return new String(fastDecodeToCharArray(tab, start, len));
    }

    /**
     * Decodes given array of bytes to array of characters.
     *
     * @param tab   String in the encoded representation.
     * @param start Index in the tab array from which start to decode.
     * @param len   Number of characters to decode.
     *
     * @return Decoded string.
     */
    public char[] fastDecodeToCharArray(final byte[] tab, final int start, final int len) {
        char[] tmp = new char[len];
        for (int i = 0; i < len; i++) {
            tmp[i] = byte2char[tab[i + start] & TO_BYTE];
        }
        return tmp;
    }

    /**
     * Encodes characters from the given char sequence into the destination byte array.
     * 
     * <p>
     * The first character to be encoded is at index <code>srcBegin</code>; the last character to be
     * encoded is at index <code>srcEnd-1</code> (thus the total number of characters to be encoded
     * is <code>srcEnd-srcBegin</code>). The characters are encoded into the subarray of
     * <code>dst</code> starting at index <code>dstBegin</code> and ending at index:
     * <p>
     * <blockquote><pre>
     *     dstbegin + (srcEnd-srcBegin) - 1
     * </pre></blockquote>
     *
     * @param src        Characters sequence to encode.
     * @param srcBegin Index of the first character in the string to encode.
     * @param srcEnd   Index after the last character in the string to encode.
     * @param dst      The destination array.
     * @param dstBegin The start offset in the destination array.
     */
    public void getBytes(final CharSequence src,
                         final int srcBegin, final int srcEnd,
                         final byte[] dst, final int dstBegin) {
        int dstPos = dstBegin;
        for (int i = srcBegin; i < srcEnd; i++, dstPos++) {
            char c = src.charAt(i);
            byte b = (c <= this.maxChar) ? this.char2byte[c] : UNKNOWN_CHARACTER_CODE;
            dst[dstPos] = (this.changeable && b == UNKNOWN_CHARACTER_CODE) ? addCharacter(c) : b;
        }
    }

    /**
     * Encodes characters from the given char array into the destination byte array.
     * <p>
     * The first character to be encoded is at index <code>srcBegin</code>; the last character to be
     * encoded is at index <code>srcEnd-1</code> (thus the total number of characters to be encoded
     * is <code>srcEnd-srcBegin</code>). The characters are encoded into the subarray of
     * <code>dst</code> starting at index <code>dstBegin</code> and ending at index:
     * <p>
     * <blockquote><pre>
     *     dstbegin + (srcEnd-srcBegin) - 1
     * </pre></blockquote>
     *
     * @param a        Array of characters to encode.
     * @param srcBegin Index of the first character in the array to encode.
     * @param srcEnd   Index after the last character in the array to encode.
     * @param dst      The destination array.
     * @param dstBegin The start offset in the destination array.
     */
    public void getBytes(final char[] a, final int srcBegin, final int srcEnd, final byte[] dst,
                         final int dstBegin) {
        int dstPos = dstBegin;
        for (int i = srcBegin; i < srcEnd; i++, dstPos++) {
            char c = a[i];
            byte b = (c <= this.maxChar) ? this.char2byte[c] : UNKNOWN_CHARACTER_CODE;
            dst[dstPos] = (this.changeable && b == UNKNOWN_CHARACTER_CODE) ? addCharacter(c) : b;
        }
    }

    /**
     * Decodes bytes from the given bytes array into the destination characters array.
     * <p>
     * The first byte to be decoded is at index <code>srcBegin</code>; the last byte to be decoded
     * is at index <code>srcEnd-1</code> (thus the total number of bytes to be decoded is
     * <code>srcEnd-srcBegin</code>). The characters are decoded into the subarray of
     * <code>dst</code> starting at index <code>dstBegin</code> and ending at index:
     * <p>
     * <blockquote><pre>
     *     dstbegin + (srcEnd-srcBegin) - 1
     * </pre></blockquote>
     *
     * @param a        Array of bytes to decode from.
     * @param srcBegin Index of the first byte in the array to decode.
     * @param srcEnd   Index after the last byte in the array to decode.
     * @param dst      The destination array.
     * @param dstBegin The start offset in the destination array.
     */
    public void getChars(final byte[] a, final int srcBegin, final int srcEnd, final char[] dst,
                         final int dstBegin) {
        int dstPos = dstBegin;
        for (int i = srcBegin; i < srcEnd; i++, dstPos++) {
            dst[dstPos] = this.byte2char[a[i] & TO_BYTE];
        }
    }

    /**
     * Writes a configuration of this charset to the output stream.
     *
     * @param out The output stream.
     *
     * @throws IOException if output error occurred.
     */
    public void write(final DataOutputStream out) throws IOException {
        out.writeInt(maxByte);
        out.writeChar(maxChar);
        out.writeBoolean(changeable);
        out.writeInt(char2byte.length);
        for (int i = 0; i < char2byte.length; i++) {
            out.writeByte(char2byte[i]);
        }
        out.writeInt(byte2char.length);
        for (int i = 0; i < byte2char.length; i++) {
            out.writeChar(byte2char[i]);
        }
    }

    /**
     * Reads a configuration of this charset from the input stream.
     *
     * @param in The input stream.
     *
     * @throws IOException if input error occurred.
     */
    public void read(final DataInputStream in) throws IOException {
        maxByte = in.readInt();
        maxChar = in.readChar();
        changeable = in.readBoolean();
        int count = in.readInt();
        char2byte = new byte[count];
        for (int i = 0; i < count; i++) {
            char2byte[i] = in.readByte();
        }
        count = in.readInt();
        byte2char = new char[count];
        for (int i = 0; i < count; i++) {
            byte2char[i] = in.readChar();
        }
    }

    /**
     * Gets array which maps characters to bytes.
     *
     * @return The array which indexes are characters and values are corresponding bytes.
     */
    public byte[] getChar2byte() {
        return char2byte;
    }

    /**
     * Gets array which maps bytes to characters.
     *
     * @return The array which indexes are bytes and values are corresponding characters.
     */
    public char[] getByte2char() {
        return byte2char;
    }

    /**
     * Gets the maximum value of a byte which encodes a character.
     *
     * @return The maximum value of bytes representing characters.
     */
    public int getMaxByte() {
        return maxByte;
    }

    /**
     * Gets the maximum value of a character which can be encoded.
     *
     * @return The maximum value of characters capable for encoding.
     */
    public char getMaxChar() {
        return maxChar;
    }

    /**
     * Checks if charset can add mapping for a new unknown character.
     *
     * @return <code>true</code> if the charset can encode any additional unknown character.
     */
    public boolean isChangable() {
        return changeable;
    }

    /**
     * Informs the charset if it should add new mappings for unknown characters.
     *
     * @param isChangeable If <code>false</code> no more character mappings can be created.
     */
    public void setChangable(final boolean isChangeable) {
        this.changeable = isChangeable;
    }

    /**
     * Registers this charset in the Dynamic Charset Provider. Registered charset can be used as
     * standard encodings by its cannonical name or aliases.
     */
    public void registerCharset() {
        DynamicCharsetProvider.registerCharset(this);
        // we should call these to clear the fast cache of the buggy Charset implementation
        Charset.forName("US-ASCII");
        Charset.forName("ISO-8859-1");
        Charset.forName("UTF-8");
    }

    /**
     * Unregisters this charset from the Dynamic Charset Provider.
     */
    public void unregisterCharset() {
        DynamicCharsetProvider.unregisterCharset(this);
        // we should call these to clear the fast cache of the buggy Charset implementation
        Charset.forName("US-ASCII");
        Charset.forName("ISO-8859-1");
        Charset.forName("UTF-8");
    }


    /**
     * Sets the international alphabet for this charset. The international alphabet is suitable for
     * an european languages.
     */
    public void setInternational() {
        addCharacter(' ');
        addCharacter('\t');
        addCharacter('\n');
        addCharacter('\r');
        for (char ch = 0x20; ch < 0x80; ch++) {
            addCharacter(ch);
        }
        for (int i = 0; i < POLISH_ALPHABET.length(); i++) {
            addCharacter(POLISH_ALPHABET.charAt(i));
        }
        
        
        
        
    }

}
