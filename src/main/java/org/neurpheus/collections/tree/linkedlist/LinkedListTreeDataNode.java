/*
 * Neurpheus - Utilities Package
 *
 * Copyright (C) 2006-2016 Jakub Strychowski
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
 * Represents a single node holding data in a linked list tree.
 *
 * @author Jakub Strychowski
 */
public class LinkedListTreeDataNode
        extends LinkedListTreeNode
        implements TreeNodeWithData<Integer, Integer> {

    /**
     * Constructs a new data node defined at the specified LLT position.
     *
     * @param pos Information about a position in a LLT unit array and traversal history.
     */
    LinkedListTreeDataNode(LinkedListPosition pos) {
        super(pos);
    }

    @Override
    public Integer getData() {
        return getUnit().getDataCode();
    }

    /**
     * This method is not supported for this implementation of the {@link TreeNode} interface.
     * <p>
     * A tree should be constructed by the      
     * {@link LinkedListTreeFactory#createTree(org.neurpheus.collections.tree.Tree, 
     * boolean, boolean, boolean)} method using any source/base tree.
     * </p>
     *
     * @exception UnsupportedOperationException
     */
    @Override
    public void setData(Integer newData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasExtraData() {
        return true;
    }

}
