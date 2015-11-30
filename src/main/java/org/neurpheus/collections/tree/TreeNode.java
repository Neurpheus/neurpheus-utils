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

import java.util.List;

/**
 * A node of a tree. Each node has a value (any Java object) and collection of child nodes. A leaf
 * is a node without children.
 *
 * @author Jakub Strychowski
 * @param <T> Type of values describing the node.
 */
public interface TreeNode<T> {

    /**
     * Returns a value assigned to this node. The value in most cases distinguish nodes having the
     * same parent.
     *
     * @return Any object stored as value of the node.
     */
    T getValue();

    /**
     * Sets a new value for this node. The value in most cases distinguish nodes having the same
     * parent.
     *
     * @param newValue Any object to store as a value of this node.
     */
    void setValue(T newValue);

    /**
     * Checks if this nodes is a leaf - has no child nodes.
     *
     * @return <strong>true</strong> if this node has no children.
     */
    boolean isLeaf();

    /**
     * Checks if this node holds additional data.
     * <p>
     * Some nodes can hold additional data besides a value. Check {@link TreeNodeWithData} for more
     * information.
     * </p>
     *
     * @return <strong>true</strong> if this node holds additional data.
     */
    boolean hasExtraData();

    /**
     * Returns a list of child nodes.
     *
     * @return children of this node.
     */
    List<TreeNode> getChildren();

    /**
     * Return number of children of this node.
     *
     * @return Number of child nodes.
     */
    int getNumberOfChildren();

    /**
     * Returns a child node represented the given key value.
     *
     * @param key the value of a node.
     *
     * @return Found child node or null if this node doesn't have any node with the given value.
     */
    TreeNode getChild(T key);

    /**
     * Returns a child node represented the given key value (searching from the given node).
     * <p>
     * In some implementations this method can speed up a tree traversal by omitting already checked
     * nodes.
     * </p>
     *
     * @param key      the value of a node.
     * @param fromNode Star searching from the given node on a list of child nodes.
     *
     * @return Found child node or null if this node doesn't have any node with the given value.
     */
    TreeNode getChild(T key, TreeNode fromNode);

    /**
     * Returns a node at the given position on a list of child nodes.
     *
     * @param index the position of a list of child nodes.
     *
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index} argument is
     *                                           negative, or if it is greater than or equal to the
     *                                           number of child nodes.
     *
     * @return node at the given position.
     */
    TreeNode getChildAtPos(int index);

    /**
     * Sets child nodes for this node.
     *
     * @param children A list of nodes for which this node is a parent.
     *
     * @exception NullPointerException If the specified {@code children} argument is null.
     */
    void setChildren(List<TreeNode> children);

    /**
     * Adds a new child node to this node.
     *
     * @param child A new child node - it will be added at the end of a list of children.
     *
     * @exception NullPointerException If the specified {@code child} argument is null.
     */
    void addChild(TreeNode child);

    /**
     * Adds a new child node tho this node at the given position in a list of children nodes.
     *
     * @param index a position on a list where the given node should be added.
     * @param child a new child of this node.
     *
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index} argument is
     *                                           negative, or if it is greater than number of child
     *                                           nodes.
     * @exception NullPointerException           If the specified {@code child} argument is null.
     */
    void addChild(int index, TreeNode child);

    /**
     * Removes the given node from a list of children of this node.
     *
     * @param child The child to remove.
     *
     * @return <code>true</code> if the given node has been found and removed from a list of
     *         children nodes.
     */
    boolean removeChild(TreeNode child);

    /**
     * Removes a child node from the given position at a list child nodes.
     *
     * @param index The position from which this method should remove node.
     *
     * @return removed node
     *
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index} argument is
     *                                           negative, or if it is greater than number of child
     *                                           nodes.
     */
    TreeNode removeChild(int index);

    /**
     * Replaces child nodes of this node.
     *
     * @param fromNode the node which should be removed from the list of child nodes.
     * @param toNode   the node which should be added to the list of child nodes at the place of
     *                 previous node.
     *
     * @return position of the replaced node.
     */
    int replaceChild(TreeNode fromNode, TreeNode toNode);

    /**
     * Removes all children of this node and any data hold by node.
     */
    void clear();

}
