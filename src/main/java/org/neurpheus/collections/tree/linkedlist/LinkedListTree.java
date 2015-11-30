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

import org.neurpheus.collections.tree.Tree;
import org.neurpheus.collections.tree.TreeFactory;
import org.neurpheus.collections.tree.TreeNode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * LZTrie implementation of tree.
 *
 * @author Jakub Strychowski
 */
public class LinkedListTree implements Tree, Serializable {

    /** Unique serialization identifier of this class. */
    static final long serialVersionUID = 770608151111104412L;

    static final byte FORMAT_VERSION = 2;

    protected LinkedListTreeUnitArray unitArray;
    
    public LinkedListTree() {
    }

    /**
     * Returns the root node of the tree.
     *
     * @return The root node.
     */
    @Override
    public TreeNode getRoot() {
        return new LinkedListTreeNode(new LinkedListPosition(unitArray, 0, null, 0, false));
    }

    /**
     * Sets the root for this tree.
     *
     * @param root New root node.
     */
    @Override
    public void setRoot(TreeNode root) {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes all nodes from the tree.
     */
    @Override
    public void clear() {
        unitArray.dispose();
        unitArray = new CompactLinkedListTreeUnitArray(1);
    }

    @Override
    public TreeFactory getFactory() {
        return LinkedListTreeFactory.getInstance();
    }

    public LinkedListTreeUnitArray getUnitArray() {
        return unitArray;
    }

    public void setUnitArray(LinkedListTreeUnitArray unitArray) {
        this.unitArray = unitArray;
    }

    public void write(DataOutputStream out) throws IOException {
        out.writeByte(FORMAT_VERSION);
        unitArray.write(out);
    }

    public void read(DataInputStream in) throws IOException {
        if (FORMAT_VERSION != in.readByte()) {
            throw new IOException("Invalid file format");
        }
        unitArray = CompactLinkedListTreeUnitArray.readInstance(in);
    }

    public static LinkedListTree readInstance(DataInputStream in) throws IOException {
        LinkedListTree result = new LinkedListTree();
        result.read(in);
        return result;
    }

}
