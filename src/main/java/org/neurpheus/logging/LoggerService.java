/*
 * Neurpheus - Utilities Package
 *
 * Copyright (C) 2015 Jakub Strychowski
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

package org.neurpheus.logging;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Produces a logger for a given class.
 *
 * @author Jakub Strychowski
 */
public final class LoggerService {

    /**
     * Private constructor to prevent instantiation.
     */
    private LoggerService() {
    }

    /**
     * Returns a configured logger for the given class.
     *
     * @param clazz A client of the logger.
     *
     * @return logger configured for the given class.
     */
    public static Logger getLogger(final Class clazz) {
        if (clazz != null) {
            return Logger.getLogger(clazz.getName());
        }
        return Logger.getGlobal();
    }

    private static Handler consoleHandler = null;
    
    public static synchronized void setLogLevelForConsole(Level level) {
        if (consoleHandler == null) {
            consoleHandler = new ConsoleHandler();
            // Logger.getAnonymousLogger().addHandler(consoleHandler);        
            LogManager.getLogManager().getLogger("").addHandler(consoleHandler);
        }
        consoleHandler.setLevel(level);
        LogManager.getLogManager().getLogger("").setLevel(level);
    }

}
