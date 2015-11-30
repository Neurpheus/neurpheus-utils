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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.zip.GZIPOutputStream;
import java.util.logging.Logger;
import org.neurpheus.logging.LoggerService;

/**
 * Writes an object or objects to a file located at a specified location.
 *
 * @author Jakub Strychowski
 */
public final class Object2FilePath {

    /** Holds the logger of this class. */
    private static final Logger logger = LoggerService.getLogger(Object2FilePath.class);

    /** Creates a new instance of Object2FilePath. */
    private Object2FilePath() {
    }

    /**
     * Writes the given object to a file located at the specified location.
     *
     * @param path     The location of a file.
     * @param obj      The object which should be written.
     * @param compress If <code>true</code>, compress data while writting.
     *
     * @throws java.io.IOException if a write error occurred.
     */
    public static void writeObject(
            final String path,
            final Object obj,
            final boolean compress)
            throws IOException {

        writeObjects(path, Collections.singletonList(obj), compress);

    }

    /**
     * Writes the given objects to a file located at the specified location.
     *
     * @param path     The location of a file.
     * @param objects  The collection of objects which should be written.
     * @param compress If <code>true</code>, compress data while writting.
     *
     * @throws java.io.IOException if a write error occurred.
     */
    public static void writeObjects(
            final String path,
            final Collection objects,
            final boolean compress)
            throws IOException {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Writing " + objects.size() + " objects to file " + path);
            logger.fine(" use compression = " + compress);
        }
        File f = new File(path);
        if (f.exists()) {
            if (f.exists() && !f.isFile()) {
                throw new IOException("Specified location is not a file. location = " + path);
            }
            if (!f.canWrite()) {
                throw new IOException("Cannot write to file " + path);
            }
        } else {
            if (!f.createNewFile()) {
                throw new IOException("Cannot create file " + path);
            }
        }
        ObjectOutputStream out = null;
        try {
            if (compress) {
                out = new ObjectOutputStream(new GZIPOutputStream(
                        new BufferedOutputStream(new FileOutputStream(f))));
            } else {
                out = new ObjectOutputStream(
                        new BufferedOutputStream(new FileOutputStream(f)));
            }
            out.writeInt(objects.size());
            for (Iterator it = objects.iterator(); it.hasNext();) {
                Object obj = it.next();
                if (obj == null) {
                    throw new IOException("Cannot write null to file " + path);
                } else if (obj instanceof Serializable) {
                    try {
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("Writing object [" + obj.toString() + "].");
                        }
                        out.writeObject(obj);
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Cannot write object", e);
                        throw new IOException("Cannot write object to file " + path);
                    }
                } else {
                    throw new IOException(
                            "Cannot write an object which is not Serializable to file " + path);
                }
            }
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Cannot close output stream.", e);
                }
            }
        }

    }

}
