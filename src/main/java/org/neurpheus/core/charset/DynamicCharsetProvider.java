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

import java.nio.charset.Charset;
import java.nio.charset.spi.CharsetProvider;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Allows for registering and unregistering any charset in the environment of a JVM instance.
 *
 * @author Jakub Strychowski
 */
public class DynamicCharsetProvider extends CharsetProvider {

    /** Holds all registered charsets. */
    private static Map charsets;

    // create default dynamic charset
    static {
        charsets = new ConcurrentHashMap();
        DynamicCharset xcs = new DynamicCharset("x-dynamic-charset", null);
        charsets.put(xcs.name(), xcs);
    }

    /**
     * Creates a new instance of DynamicCharsetProvider.
     */
    public DynamicCharsetProvider() {
    }

    /**
     * Retrieves a charset for the given charset name.
     *
     * @param charsetName The name of the requested charset; may be either a canonical name or an
     *                    alias.
     *
     * @return A charset object for the named charset, or <tt>null</tt> if the named charset is not
     *         supported by this provider.
     */
    @Override
    public Charset charsetForName(final String charsetName) {
        return (Charset) charsets.get(charsetName);
    }

    /**
     * Creates an iterator that iterates over the charsets supported by this provider. This method
     * is used in the implementation of the {@link
     * java.nio.charset.Charset#availableCharsets Charset.availableCharsets} method.
     *
     * @return The new iterator.
     */
    @Override
    public Iterator charsets() {
        List tmp = new ArrayList();
        tmp.addAll(charsets.values());
        return tmp.iterator();
    }

    /**
     * Registers the given charset in this charset provider. Registered charset can be used as
     * standard encodings by its cannonical name or aliases.
     *
     * @param cs The charset to register.
     */
    public static void registerCharset(final Charset cs) {
        charsets.put(cs.name(), cs);
        for (Iterator it = cs.aliases().iterator(); it.hasNext();) {
            charsets.put(it.next(), cs);
        }
    }

    /**
     * Unregisters the given charset from this charset provider.
     *
     * @param cs The charset to unregister.
     */
    public static void unregisterCharset(final Charset cs) {
        charsets.remove(cs.name());
        for (Iterator it = cs.aliases().iterator(); it.hasNext();) {
            charsets.remove(it.next());
        }
    }

    /**
     * Unregisters a charset having the given name from this charset provider.
     *
     * @param charsetName The name of a charset to unregister.
     */
    public static void unregisterCharset(final String charsetName) {
        final Charset cs = (Charset) charsets.get(charsetName);
        if (cs != null) {
            unregisterCharset(cs);
        }
    }

}
