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

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

/**
 * An engine that can transform a sequence of sixteen-bit Unicode characters into a sequence of
 * bytes in a specific charset. See {@link java.nio.charset.CharsetEncoder}.
 *
 * @author Jakub Strychowski
 */
public class DynamicCharsetEncoder extends CharsetEncoder {

    /** Holds replacment for unknow characters. */
    private static final byte[] REPLACEMENT = {DynamicCharset.UNKNOWN_CHARACTER_CODE};

    /**
     * Creates a new instance of DynamicCharsetEncoder.
     *
     * @param cs The charset which creates this decoder.
     */
    public DynamicCharsetEncoder(final Charset cs) {
        super(cs, 1, 1, REPLACEMENT);
    }

    /**
     * Encodes one or more characters into one or more bytes.
     *
     * <p>
     * This method encapsulates the basic encoding loop, encoding as many characters as possible
     * until it either runs out of input, runs out of room in the output buffer, or encounters an
     * encoding error. This method is invoked by the {@link #encode encode} method, which handles
     * result interpretation and error recovery.
     *
     * <p>
     * The buffers are read from, and written to, starting at their current positions. At most
     * {@link Buffer#remaining in.remaining()} characters will be read, and at most
     * {@link Buffer#remaining out.remaining()} bytes will be written. The buffers' positions will
     * be advanced to reflect the characters read and the bytes written, but their marks and limits
     * will not be modified.
     *
     * <p>
     * This method returns a {@link CoderResult} object to describe its reason for termination, in
     * the same manner as the {@link #encode encode} method. Most implementations of this method
     * will handle encoding errors by returning an appropriate result object for interpretation by
     * the {@link #encode encode} method. An optimized implementation may instead examine the
     * relevant error action and implement that action itself.
     *
     * <p>
     * An implementation of this method may perform arbitrary lookahead by returning
     * {@link CoderResult#UNDERFLOW} until it receives sufficient input.  </p>
     *
     * @param in  The input character buffer.
     * @param out The output byte buffer.
     *
     * @return A coder-result object describing the reason for termination.
     */
    @Override
    protected CoderResult encodeLoop(final CharBuffer in, final ByteBuffer out) {
        DynamicCharset cs = (DynamicCharset) charset();
        while (in.remaining() > 0) {
            if (out.remaining() == 0) {
                return CoderResult.OVERFLOW;
            } else {
                out.put(cs.fastEncode(in.get()));
            }
        }
        return CoderResult.UNDERFLOW;
    }

    /**
     * Tells whether or not the given byte array is a legal replacement value for this encoder.
     *
     * @param repl The byte array to be tested
     *
     * @return <tt>true</tt> if, and only if, the given byte array is a legal replacement value for
     *         this encoder
     */
    @Override
    public boolean isLegalReplacement(final byte[] repl) {
        return (repl == REPLACEMENT) || (repl.length == 1 && repl[0] == DynamicCharset.UNKNOWN_CHARACTER_CODE);
    }

    /**
     * Tells whether or not this encoder can encode the given character.
     *
     * @return <tt>true</tt> if, and only if, this encoder can encode the given character
     */
    @Override
    public boolean canEncode(char c) {
        DynamicCharset dcs = (DynamicCharset) charset();
        return dcs.isChangable() ? true : (c <= dcs.getMaxChar()) && (dcs.fastEncode(c) != dcs.UNKNOWN_CHARACTER_CODE);
    }

    /**
     * Tells whether or not this encoder can encode the given character sequence.
     *
     * @return <tt>true</tt> if, and only if, this encoder can encode the given character without
     *         throwing any exceptions and without performing any replacements
     */
    @Override
    public boolean canEncode(CharSequence s) {
        DynamicCharset dcs = (DynamicCharset) charset();
        for (int i = s.length() - 1; i >= 0; i--) {
            if (dcs.fastEncode(s.charAt(i)) == dcs.UNKNOWN_CHARACTER_CODE) {
                return false;
            }
        }
        return true;
    }

}
