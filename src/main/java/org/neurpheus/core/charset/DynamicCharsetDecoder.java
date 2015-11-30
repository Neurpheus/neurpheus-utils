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
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

/**
 * An engine that can transform a sequence of bytes in a specific charset into a sequence of
 * sixteen-bit Unicode characters. See {@link java.nio.charset.CharsetDecoder}.
 *
 * @author Jakub Strychowski
 */
public class DynamicCharsetDecoder extends CharsetDecoder {

    /**
     * Creates a new instance of DynamicCharsetDecoder.
     *
     * @param cs The charset which creates this decoder.
     */
    public DynamicCharsetDecoder(final Charset cs) {
        super(cs, 1, 1);
    }

    /**
     * Decodes one or more bytes into one or more characters.
     *
     * <p>
     * This method encapsulates the basic decoding loop, decoding as many bytes as possible until it
     * either runs out of input, runs out of room in the output buffer, or encounters a decoding
     * error. This method is invoked by the {@link #decode decode} method, which handles result
     * interpretation and error recovery.
     *
     * <p>
     * The buffers are read from, and written to, starting at their current positions. At most
     * {@link Buffer#remaining in.remaining()} bytes will be read, and at most
     * {@link Buffer#remaining out.remaining()} characters will be written. The buffers' positions
     * will be advanced to reflect the bytes read and the characters written, but their marks and
     * limits will not be modified.
     *
     * <p>
     * This method returns a {@link CoderResult} object to describe its reason for termination, in
     * the same manner as the {@link #decode decode} method. Most implementations of this method
     * will handle decoding errors by returning an appropriate result object for interpretation by
     * the {@link #decode decode} method. An optimized implementation may instead examine the
     * relevant error action and implement that action itself.
     *
     * <p>
     * An implementation of this method may perform arbitrary lookahead by returning
     * {@link CoderResult#UNDERFLOW} until it receives sufficient input.  </p>
     *
     * @param in  The input byte buffer.
     * @param out The output character buffer.
     *
     * @return A coder-result object describing the reason for termination.
     */
    @Override
    protected CoderResult decodeLoop(final ByteBuffer in, final CharBuffer out) {
        DynamicCharset cs = (DynamicCharset) charset();
        while (in.remaining() > 0) {
            if (out.remaining() == 0) {
                return CoderResult.OVERFLOW;
            } else {
                out.put(cs.fastDecode(in.get()));
            }
        }
        return CoderResult.UNDERFLOW;
    }

}
