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

package org.neurpheus.collections.tree.objecttree;

import org.neurpheus.collections.tree.Tree;
import org.neurpheus.collections.tree.TreeNode;

import java.io.Serializable;

/**
 * Implementation of the Tree interface using collections of objects.
 *
 * @param <T> Type of values describing nodes.
 * @param <D> Type of additional data stored in some nodes.
 * 
 * @author Jakub Strychowski
 */
public class ObjectTree<T, D> implements Tree<T, D>, Serializable {

    /**
     * Unique version of this class.
     */
    static final long serialVersionUID = 770608070910114037L;

    /**
     * Root node of the tree.
     */
    protected ObjectTreeNode<T> root;

    /**
     * Creates a tree with only root node.
     */
    public ObjectTree() {
        root = ObjectTreeFactory.getInstance().createTreeNode(null);
    }

    @Override
    public ObjectTreeNode<T> getRoot() {
        return root;
    }

    @Override
    public void setRoot(TreeNode<T> root) {
        if (root == null) {
            throw new NullPointerException("Root cannot be null");
        }
        this.root = (ObjectTreeNode) root;
    }

    @Override
    public void clear() {
        root.clear();
        root.setValue(null);
    }

    @Override
    public ObjectTreeFactory<T, D> getFactory() {
        return ObjectTreeFactory.getInstance();
    }

}
