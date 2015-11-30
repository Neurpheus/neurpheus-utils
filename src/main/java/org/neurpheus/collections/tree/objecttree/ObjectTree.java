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
import org.neurpheus.collections.tree.TreeFactory;
import org.neurpheus.collections.tree.TreeNode;

import java.io.Serializable;

/**
 * Implementation of the Tree interface using collections of objects.
 *
 * @author Jakub Strychowski
 */
public class ObjectTree implements Tree, Serializable {

    /**
     * Unique version of this class.
     */
    static final long serialVersionUID = 770608070910114037L;

    /**
     * Root node of the tree.
     */
    protected ObjectTreeNode root;

    /**
     * Creates a tree with only root node.
     */
    public ObjectTree() {
        root = ObjectTreeFactory.getInstance().createTreeNode(null);
    }

    /**
     * Returns the root node of the tree.
     *
     * @return The root node.
     */
    @Override
    public TreeNode getRoot() {
        return root;
    }

    /**
     * Sets the root for this tree.
     *
     * @param root New root node.
     */
    @Override
    public void setRoot(TreeNode root) {
        if (root == null) {
            throw new NullPointerException("Root cannot be null");
        }
        this.root = (ObjectTreeNode) root;
    }

    /**
     * Removes all nodes from the tree.
     */
    @Override
    public void clear() {
        root.clear();
        root.setValue(null);
    }

    /**
     * Returns a factory which can be used for creation of nodes for this tree.
     * 
     * @return Returns an instance of a factory. 
     */
    @Override
    public TreeFactory getFactory() {
        return ObjectTreeFactory.getInstance();
    }

}
