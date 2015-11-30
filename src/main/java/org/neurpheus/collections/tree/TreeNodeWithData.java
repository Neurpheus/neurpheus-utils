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

package org.neurpheus.collections.tree;

/**
 * A tree node which holds additional data.
 *
 * @param <T> Type of values describing nodes.
 * @param <D> Type of data stored in the node.
 * 
 * @author Jakub Strychowski
 */
public interface TreeNodeWithData<T, D> extends TreeNode<T> {

    /**
     * Returns additional data hold by this node.
     *
     * @return Any object hold by this node.
     */
    D getData();

    /**
     * Set a new data which should be held by this node.
     *
     * @param newData additional data held by a tree structure in this node.
     */
    void setData(D newData);

}
