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

import org.neurpheus.collections.tree.Tree;
import org.neurpheus.collections.tree.TreeNode;
import org.neurpheus.logging.LoggerService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neurpheus.collections.tree.objecttree.ObjectTree;

/**
 * Compact, memory efficient implementation of a tree structure.
 * <p>
 * A linked list tree is a structure designed to store the tree in a compact form in a memory.
 * Additionally you can use LZTrie compression algorithm on this structure to eliminate 
 * repeating fragments of a tree reducing memory consumption several times. 
 * For example a searchable index of all Polish words' forms can be stored using 
 * about 275kB instead of 19MB. 
 * </p>
 * <p>
 * This class does NOT implements a tree using the Java's LinkedList class.
 * Instead, nodes and edges are represented by an array of structured items called units.
 * Each units contains some basic informations about a single node-edge pair and contains
 * links to other units (thats why this structure is called linked list tree). 
 * </p>
 * <p>
 * There are some limitations in this implementation of the flexible {@link Tree} interface:
 * <ul>
 * <li>A tree must be constructed from another tree (e.g. {@link ObjectTree}) and cannot be
 * modified after built.</li>
 * <li>Transitions between nodes can be described only by integer values. However you can create
 * a mapping between more complex objects and integers if you need. In reals cases, most 
 * programmers use characters or identifiers of objects as value assigned to edges.</li>
 * <li>An object stored on a leaf in the tree should be an integer value. However you can apply
 * the same approach as described above</li>
 * </ul>
 * </p>
 *
 * @author Jakub Strychowski
 */
public class LinkedListTree implements Tree<Integer, Integer>, Serializable {

    /** Logger for this class. */
    private static final Logger LOGGER = LoggerService.getLogger(LinkedListTree.class);

    /** Unique serialization identifier of this class. */
    static final long serialVersionUID = 770608151111104412L;

    /** Supported version of a data format for serialized LLTree object. */
    static final byte FORMAT_VERSION = 2;

    /** Array of elements of linked list tree structure. */
    protected LinkedListTreeUnitArray unitArray;

    /**
     * Creates a new tree structure with only a root node.
     */
    protected LinkedListTree() {
        unitArray = new FastLinkedListTreeUnitArray(1);
        LinkedListTreeUnit rootUnit = new LinkedListTreeUnit();
        rootUnit.setWordContinued(true);
        unitArray.add(rootUnit);
    }

    @Override
    public LinkedListTreeNode getRoot() {
        return new LinkedListTreeNode(new LinkedListPosition(unitArray, 0, null, 0, false));
    }

    /**
     * This method is not supported for this implementation of the {@link Tree} interface.
     * <p>
     * A tree should be constructed by the 
     * {@link LinkedListTreeFactory#createTree(org.neurpheus.collections.tree.Tree, 
     * boolean, boolean, boolean)} method using any source/base tree.
     * </p>
     *
     * @param root not used here
     *
     * @exception UnsupportedOperationException
     */
    @Override
    public void setRoot(TreeNode root) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        unitArray.dispose();
        unitArray = new CompactLinkedListTreeUnitArray(1);
    }

    @Override
    public LinkedListTreeFactory getFactory() {
        return LinkedListTreeFactory.getInstance();
    }

    /**
     * Return an internal linked list structure.
     *
     * @return An array of units representing nodes and edges.
     */
    public LinkedListTreeUnitArray getUnitArray() {
        return unitArray;
    }

    /**
     * Changes an internal linked list structure to the specified array of units describing nodes
     * and edges.
     *
     * @param unitArray An array of units representing nodes and edges.
     */
    public void setUnitArray(LinkedListTreeUnitArray unitArray) {
        this.unitArray = unitArray;
    }

    /**
     * Writes this tree and all its elements to the specified data stream.
     *
     * @param out Data output stream.
     *
     * @throws IOException if an I/O error occurs.
     */
    public void write(DataOutputStream out) throws IOException {
        out.writeByte(FORMAT_VERSION);
        CompactLinkedListTreeUnitArray units;
        if (unitArray instanceof CompactLinkedListTreeUnitArray) {
            units =  (CompactLinkedListTreeUnitArray) unitArray;
        } else {
            units = new CompactLinkedListTreeUnitArray(unitArray);
        }
        units.compact();
        units.write(out);
        unitArray = units;
    }

    /**
     * Reads a content of a tree from the specified data stream.
     *
     * @param in Data input stream
     *
     * @throws IOException if an I/O error occurs.
     */
    public void read(DataInputStream in) throws IOException {
        if (FORMAT_VERSION != in.readByte()) {
            throw new IOException("Invalid file format");
        }
        unitArray = new CompactLinkedListTreeUnitArray();
        unitArray.read(in);
    }

    /**
     * Splits this tree to a forest of trees.
     * <p>
     * Each child node of a root of this tree will became a root of a new tree.
     * </p>
     *
     * @return A list of trees created by cutting child nodes from a root of this tree.
     */
    public List<LinkedListTree> split() {
        LinkedListTreeFactory factory = LinkedListTreeFactory.getInstance();
        LinkedListTreeNode root = new LinkedListTreeNode(
                new LinkedListPosition(unitArray, 0, null, 0, false));
        List<LinkedListTreeNode> children = root.getChildren();
        LOGGER.log(Level.FINE, "Split tree into {0} sub trees", children.size());
        List<LinkedListTree> result = new ArrayList<>(children.size());
        for (LinkedListTreeNode childNode : children) {
            LinkedListPosition childPos = childNode.getPosition();
            int startIndex = childPos.getPos();
            int offset = childPos.getDistance();
            int endIndex = offset == 0 ? unitArray.size() : startIndex + offset;
            LinkedListTreeUnitArray subArray = unitArray.subArray(startIndex, endIndex);
            subArray.moveAbsolutePointers(-startIndex);
            LinkedListTreeUnit rootUnit = subArray.get(0);
            rootUnit.setDistance(0);
            subArray.set(0, rootUnit);
            subArray = new CompactLinkedListTreeUnitArray(subArray);
            LinkedListTree childTree = factory.createTree();
            childTree.setUnitArray(subArray);
            result.add(childTree);
        }
        return result;
    }

    /**
     * Joins the specified trees to this tree.
     * <p>
     * This method adds roots of the specified trees to a root of this tree.
     * </p>
     * 
     * @param forest A list of trees to join to this tree.
     */
    public void joinSubTrees(List<LinkedListTree> forest) {
        LinkedListTreeUnitArray units = getUnitArray();
        int lastChildPos = 0;
        boolean valueMappingSet = false;
        for (LinkedListTree subTree : forest) {
            if (!valueMappingSet) {
                units.setValueMapping(subTree.getUnitArray().getValueMapping());
                valueMappingSet = true;
            }
            lastChildPos = units.size();
            LinkedListTreeUnitArray subArray = subTree.getUnitArray();
            subArray.moveAbsolutePointers(lastChildPos);
            units.addAll(subArray);
            LinkedListTreeUnit unit = units.get(lastChildPos);
            unit.setDistance(subArray.size());
            units.set(lastChildPos, unit);
        }
        if (lastChildPos > 0) {
            LinkedListTreeUnit unit = units.get(lastChildPos);
            unit.setDistance(0);
            units.set(lastChildPos, unit);
        }
    }

}
