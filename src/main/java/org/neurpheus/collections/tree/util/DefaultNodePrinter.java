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

package org.neurpheus.collections.tree.util;

public class DefaultNodePrinter implements TreeNodePrinter {

    @Override
    public String getValueString(Object value) {
        return value.toString();
    }

    @Override
    public String getDataString(Object data) {
        return "";
//        if (data instanceof Object[]) {
//            StringBuffer tmp = new StringBuffer();
//            Object[] a = (Object[]) data;
//            for (int i = 0; i < a.length; i++) {
//                tmp.append("[");
//                tmp.append(getDataString(a[i]));
//                tmp.append("]");
//            }
//            return tmp.toString();
//        }  else {
//            return data == null ? "NULL" : data.toString();
//        }
    }

}
