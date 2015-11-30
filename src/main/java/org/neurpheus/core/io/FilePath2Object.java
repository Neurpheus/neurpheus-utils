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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import org.neurpheus.logging.LoggerService;

/**
 * Reads an object or objects from a file located at specified location.
 *
 * @author Jakub Strychowski
 */
public final class FilePath2Object {

    /** Holds the logger of this class. */
    private static Logger logger = LoggerService.getLogger(FilePath2Object.class);

    /** Creates a new instance of FilePath2Object. */
    private FilePath2Object() {
    }

    /**
     * Reads an object from a file stored at the specified location. If the file contains more then
     * one object, this method returns only first object. If the file is empty, this method return
     * <code>null</code>.
     *
     * @param path     The location of a file.
     * @param compress If <code>true</code>, decompress data while reading.
     *
     * @return The read object.
     *
     * @throws java.io.IOException if a read error occurred.
     */
    public static Object readObject(
            final String path,
            final boolean compress)
            throws IOException {

        Collection tmp = readObjects(path, compress);
        if (tmp.size() == 0) {
            return null;
        } else {
            return tmp.iterator().next();
        }
    }

    /**
     * Reads objects from a file stored at the specified location.
     *
     * @param path     The location of a file.
     * @param compress If <code>true</code>, decompress data while reading.
     *
     * @return The list of read objects.
     *
     * @throws java.io.IOException if a read error occurred.
     */
    public static List readObjects(
            final String path,
            final boolean compress)
            throws IOException {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Reading objects from the file " + path);
            logger.fine(" use compression = " + compress);
        }

        File f = new File(path);
        if (!f.isFile()) {
            throw new IOException("Specified location is not a file. location = " + path);
        }
        if (!f.canRead()) {
            throw new IOException("Cannot read from file " + path);
        }

        ObjectInputStream in = null;
        try {
            if (compress) {
                in = new ObjectInputStream(new GZIPInputStream(
                        new BufferedInputStream(new FileInputStream(f))));
            } else {
                in = new ObjectInputStream(
                        new BufferedInputStream(new FileInputStream(f)));
            }

            int count = in.readInt();
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(" number of object to read: " + count);
            }

            List res = new ArrayList();
            while (count > 0) {
                --count;
                try {
                    Object obj = in.readObject();
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("Object [" + obj.toString() + "] readed.");
                    }
                    res.add(obj);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Cannot read object", e);
                    throw new IOException("Cannot read object from file " + path);
                }

            }

            return res;

        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Cannot close input stream.", e);
                }
            }
        }

    }

}
