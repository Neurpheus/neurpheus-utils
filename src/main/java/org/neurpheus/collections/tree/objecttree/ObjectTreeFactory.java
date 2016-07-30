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
import org.neurpheus.collections.tree.TreeNodeWithData;

/**
 * Creates elements of a tree structure.
 * <p>
 * This object is returned by the {@see ObjectTree} class and should be used for 
 * constructing tree elements which are compatible with the object tree.
 * </p>
 *
 * @param <T> Type of values describing nodes.
 * @param <D> Type of additional data stored in some nodes.
 *
 * @author Jakub Strychowski
 */
public final class ObjectTreeFactory<T, D> implements TreeFactory<T, D> {

    /**
     * Single instance of this factory.
     */
    private static final ObjectTreeFactory INSTANCE = new ObjectTreeFactory();
    
    /**
     * Returns a single instance of this factory.
     *
     * @return Factory object.
     */
    public static ObjectTreeFactory getInstance() {
        return INSTANCE;
    }

    /**
     * Creates a new tree compatible with this factory.
     *
     * @return A new tree objects.
     */
    @Override
    public Tree<T, D> createTree() {
        return new ObjectTree<>();
    }

    /**
     * Creates a new node for a tree.
     *
     * @param value any object which describes a node in the tree.
     *
     * @return constructed node.
     */
    @Override
    public ObjectTreeNode<T> createTreeNode(T value) {
        return new ObjectTreeNode<>(value);
    }

    /**
     * Creates a new node holding additional data in a tree.
     *
     * @param value any object which describes a node in the tree.
     * @param data  additional data to hold in the node.
     *
     * @return constructed node.
     */
    @Override
    public TreeNodeWithData<T, D> createTreeNodeWithAdditionalData(T value, D data) {
        return new ObjectTreeNodeWithData<>(value, data);
    }

    /**
     * Sets additional data for the node replacing it with proper type if needed. Only
     * ObjectTreeNodeWithData can hold additional data, therefore this method replaces a node in a
     * tree to nod of this type if needed for setting data.
     *
     * @param node       Node which should hold additional data.
     * @param nodeParent Parent of node.
     * @param data       Additional data to store.
     */
    public void setDataForNode(TreeNode<T> node, TreeNode<T> nodeParent, D data) {
        if (node instanceof TreeNodeWithData) {
            ((TreeNodeWithData) node).setData(data);
        } else {
            TreeNodeWithData newNode = createTreeNodeWithAdditionalData(node.getValue(), data);
            newNode.setChildren(node.getChildren());
            nodeParent.replaceChild(node, newNode);
            node.clear();
        }
    }

    /**
     * Sorts all nodes in the given tree according to values describing nodes.
     *
     * @param tree Tree which nodes should be sort.
     */
    public void sortTree(Tree<T, D> tree) {
        ((ObjectTreeNode<T>) tree.getRoot()).sort();
    }

}
