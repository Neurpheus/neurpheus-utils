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

package org.neurpheus.core.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.neurpheus.core.charset.FastUTF8;

/**
 * Helps to write to and read from a stream data values in compact form.
 *
 * @author Jakub Strychowski
 */
public class DataOutputStreamPacker {

    public static void writeInt(int id, DataOutputStream out) throws IOException {
        if (id < Byte.MAX_VALUE - 1) {
            out.writeByte(id);
        } else if (id < Short.MAX_VALUE) {
            out.writeByte(Byte.MAX_VALUE - 1);
            out.writeShort(id);
        } else {
            out.writeByte(Byte.MAX_VALUE);
            out.writeInt(id);
        }
    }

    public static void writeString(final String str, DataOutputStream out) throws IOException {
        if (str == null) {
            writeInt(-1, out);
        } else {
            byte[] bytes = str.getBytes("utf-8");
            writeInt(bytes.length, out);
            out.write(bytes);
        }
    }

    public static String readString(DataInputStream in) throws IOException {
        int v = readInt(in);
        if (v == -1) {
            return null;
        } else {
            byte[] bytes = new byte[v];
            in.readFully(bytes);
            return FastUTF8.decode(bytes);
        }
    }

    public static int readInt(DataInputStream in) throws IOException {
        byte b = in.readByte();
        if (b < Byte.MAX_VALUE - 1) {
            return (int) b;
        } else if (b == Byte.MAX_VALUE) {
            return in.readInt();
        } else {
            return in.readShort();
        }
    }

    public static void writeStringsMap(final Map map, final DataOutputStream out) throws IOException {
        int size = map == null ? 0 : map.size();
        DataOutputStreamPacker.writeInt(size, out);
        if (size > 0) {
            for (final Iterator it = map.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                DataOutputStreamPacker.writeString(entry.getKey().toString(), out);
                DataOutputStreamPacker.writeString(entry.getValue().toString(), out);
            }
        }
    }

    public static Map readStringsMap(final DataInputStream in) throws IOException {
        int size = DataOutputStreamPacker.readInt(in);
        Map result = new HashMap();
        for (int i = 0; i < size; i++) {
            String key = DataOutputStreamPacker.readString(in);
            String value = DataOutputStreamPacker.readString(in);
            result.put(key, value);
        }
        return result;
    }

    public static void writeStringArrayMap(final Map map, final DataOutputStream out) throws
            IOException {
        int size = map == null ? 0 : map.size();
        DataOutputStreamPacker.writeInt(size, out);
        if (size > 0) {
            for (final Iterator it = map.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                DataOutputStreamPacker.writeString(entry.getKey().toString(), out);
                String[] a = (String[]) entry.getValue();
                int len = a == null ? 0 : a.length;
                DataOutputStreamPacker.writeInt(len, out);
                for (int i = 0; i < len; i++) {
                    DataOutputStreamPacker.writeString(a[i], out);
                }
            }
        }
    }

    public static Map readStringArrayMap(final DataInputStream in) throws IOException {
        int size = DataOutputStreamPacker.readInt(in);
        Map result = new HashMap();
        for (int i = 0; i < size; i++) {
            String key = DataOutputStreamPacker.readString(in);
            int len = DataOutputStreamPacker.readInt(in);
            String[] a = new String[len];
            for (int j = 0; j < len; j++) {
                a[j] = DataOutputStreamPacker.readString(in);
            }
            result.put(key, a);
        }
        return result;
    }

    public static void writeArrayOfLongs(long[] data, final DataOutputStream out) throws IOException {
        int len = data.length;
        writeInt(len, out);
        byte[] buffer = new byte[len * 8];
        int j = 0;
        long v;
        for (int i = 0; i < len; i++) {
            v = data[i];
            buffer[j++] = (byte) (v & 0xff);
            buffer[j++] = (byte) ((v >> 8) & 0xff);
            buffer[j++] = (byte) ((v >> 16) & 0xff);
            buffer[j++] = (byte) ((v >> 24) & 0xff);
            buffer[j++] = (byte) ((v >> 32) & 0xff);
            buffer[j++] = (byte) ((v >> 40) & 0xff);
            buffer[j++] = (byte) ((v >> 48) & 0xff);
            buffer[j++] = (byte) ((v >> 56) & 0xff);
        }
        out.write(buffer);
        buffer = null;
    }

    public static long[] readArrayOfLongs(final DataInputStream in) throws IOException {
        int len = readInt(in);
        byte[] buffer = new byte[len * 8];
        in.readFully(buffer);
        long[] data = new long[len];
        int j = buffer.length - 1;
        for (int i = len - 1; i >= 0; i--) {
            data[i] = ((((long) buffer[j--]) & 0xff) << 56)
                    | ((((long) buffer[j--]) & 0xff) << 48)
                    | ((((long) buffer[j--]) & 0xff) << 40)
                    | ((((long) buffer[j--]) & 0xff) << 32)
                    | ((((long) buffer[j--]) & 0xff) << 24)
                    | ((((long) buffer[j--]) & 0xff) << 16)
                    | ((((long) buffer[j--]) & 0xff) << 8)
                    | (((long) buffer[j--]) & 0xff);
        }
        buffer = null;
        return data;
    }

    public static void writeArrayOfIntegers(int[] data, final DataOutputStream out) throws
            IOException {
        int len = data.length;
        writeInt(len, out);
        byte[] buffer = new byte[len * 4];
        int j = 0;
        int v;
        for (int i = 0; i < len; i++) {
            v = data[i];
            buffer[j++] = (byte) (v & 0xff);
            buffer[j++] = (byte) ((v >> 8) & 0xff);
            buffer[j++] = (byte) ((v >> 16) & 0xff);
            buffer[j++] = (byte) ((v >> 24) & 0xff);
        }
        out.write(buffer);
        buffer = null;
    }

    public static int[] readArrayOfIntegers(final DataInputStream in) throws IOException {
        int len = readInt(in);
        byte[] buffer = new byte[len * 4];
        in.readFully(buffer);
        int[] data = new int[len];
        int j = buffer.length - 1;
        for (int i = len - 1; i >= 0; i--) {
            data[i] = ((((int) buffer[j--]) & 0xff) << 24)
                    | ((((int) buffer[j--]) & 0xff) << 16)
                    | ((((int) buffer[j--]) & 0xff) << 8)
                    | (((int) buffer[j--]) & 0xff);
        }
        buffer = null;
        return data;
    }

}
