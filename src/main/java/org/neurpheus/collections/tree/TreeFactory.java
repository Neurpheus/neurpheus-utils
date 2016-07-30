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
 * Creates elements of a tree structure.
 * <p>
 * This object is returned by an implementation of the {@see Tree} interface}
 * and should be used for constructing tree elements which are compatible with the tree.
 * </p>
 * 
 * @param <T> Type of values describing nodes.
 * @param <D> Type of additional data stored in some nodes.
 * 
 * @author Jakub Strychowski
 */
public interface TreeFactory<T, D> {

    /**
     * Creates a new tree compatible with this factory.
     * 
     * @return A new tree objects.
     */
    Tree<T, D> createTree();
    
    /**
     * Creates a new node for a tree.
     * 
     * @param value any object which describes a node in the tree.
     * 
     * @return constructed node.
     */
    TreeNode<T> createTreeNode(T value);

    /**
     * Creates a new node holding additional data in a tree.
     * 
     * @param value any object which describes a node in the tree.
     * @param data additional data to hold in the node.
     * 
     * @return constructed node.
     */
    TreeNodeWithData<T, D> createTreeNodeWithAdditionalData(T value, D data);


}
