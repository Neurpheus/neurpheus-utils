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

package org.neurpheus.collections.tree.linkedlist;

import org.neurpheus.collections.tree.TreeNodeWithData;

/**
 *
 * @author szkoleniowy
 */
public class LinkedListTreeDataNode extends LinkedListTreeNode implements TreeNodeWithData {

    public LinkedListTreeDataNode(LinkedListPosition pos) {
        super(pos);
    }

    @Override
    public Object getData() {
        return new Integer(getUnit().getDataCode());
    }

    @Override
    public void setData(Object newData) {
        getUnit().setDataCode(((Integer) newData).intValue());
    }

    @Override
    public boolean hasExtraData() {
        return true;
    }

}
