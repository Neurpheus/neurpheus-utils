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
 * A tree data structure can be defined recursively (locally) as a collection of nodes (starting at
 * a root node), where each node is a data structure consisting of a value, together with a list of
 * references to nodes (the "children"), with the constraints that no reference is duplicated, and
 * none points to the root.
 * <p>
 * The tree with no nodes is called the null or empty tree. A tree that is not empty consists of a
 * root node and potentially many levels of additional nodes that form a hierarchy.
 * </p>
 * <p>
 * <h2>Terminologies used in Trees</h2>
 * <ul>
 * <li><strong>Root</strong> – The top node in a tree.</li>
 * <li><strong>Parent</strong> – The converse notion of a child.</li>
 * <li><strong>Siblings</strong> – Nodes with the same parent.</li>
 * <li><strong>Descendant</strong> – a node reachable by repeated proceeding from parent to
 * child.</li>
 * <li><strong>Ancestor</strong> – a node reachable by repeated proceeding from child to
 * parent.</li>
 * <li><strong>Leaf</strong> – a node without child nodes.</li>
 * <li><strong>Internal node</strong> – a node with at least one child.</li>
 * <li><strong>External node</strong> – a node with no children.</li>
 * <li><strong>Degree</strong> – number of sub trees of a node.</li>
 * <li><strong>Edge</strong> – connection between one node to another.</li>
 * <li><strong>Path</strong> – a sequence of nodes and edges connecting a node with a
 * descendant.</li>
 * <li><strong>Level</strong> – The level of a node is defined by 1 + (the number of connections
 * between the node and the root).</li>
 * <li><strong>Height of tree</strong> –The height of a tree is the number of edges on the longest
 * downward path between the root and a leaf.</li>
 * <li><strong>Height of node</strong> –The height of a node is the number of edges on the longest
 * downward path between that node and a leaf.</li>
 * <li><strong>Depth</strong> – The depth of a node is the number of edges from the node to the
 * tree's root node.</li>
 * <li><strong>Forest</strong> – A forest is a set of n ? 0 disjoint trees.</li>
 * </ul>
 * </p>
 *
 * @author Jakub Strychowski
 */
public interface Tree {

    /**
     * Returns the root node of the tree.
     *
     * @return The root node.
     */
    TreeNode getRoot();

    /**
     * Sets the root for this tree.
     *
     * @param root New root node.
     */
    void setRoot(TreeNode root);

    /**
     * Removes all nodes from the tree.
     */
    void clear();

    /**
     * Returns a factory which can create nodes for this tree.
     *
     * @return An implementation of a factory which creates nodes for this tree.
     */
    TreeFactory getFactory();

}
