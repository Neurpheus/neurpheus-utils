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

import org.neurpheus.collections.tree.TreeNode;
import org.neurpheus.collections.tree.TreeNodeWithData;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;

/**
 * A tree node which holds additional data.
 * 
 * @param <T> Type of values describing nodes.
 * @param <D> Type of data stored in the node.
 *
 * @author Jakub Strychowski
 */
public class ObjectTreeNodeWithData<T, D> 
    extends ObjectTreeNode<T> 
    implements TreeNodeWithData<T, D>, Serializable {

    /**
     * Unique version of this class.
     */
    private static final long serialVersionUID = 770608070910113945L;

    private transient D data;

    /**
     * Constructs empty node with no value assigned and no children.
     */
    protected ObjectTreeNodeWithData() {
        super();
    }

    /**
     * Constructs a new node and marks it with the given value and sets the given additional data.
     * Created node has no children.
     *
     * @param key  The value assigned to the node.
     * @param data Additional data to store by this node.
     */
    protected ObjectTreeNodeWithData(T key, D data) {
        super(key);
        this.data = data;
    }
    
    /**
     * Returns additional data hold by this node.
     *
     * @return Any object hold by this node.
     */
    @Override
    public D getData() {
        return this.data;
    }

    /**
     * Set a new data which should be held by this node.
     *
     * @param newData additional data held by a tree structure in this node.
     */
    @Override
    public void setData(D newData) {
        this.data = newData;
    }

    /**
     * Checks if this node holds additional data.
     * <p>
     * Some nodes can hold additional data besides a value. Check {@link TreeNodeWithData} for more
     * information.
     * </p>
     *
     * @return <strong>true</strong> if this node holds additional data.
     */
    @Override
    public boolean hasExtraData() {
        return true;
    }


    /**
     * Removes all children of this node and any data hold by node.
     */
    @Override
    public void clear() {
        super.clear();
        this.data = null;
    }

    /**
     * Serializes this node to the objects stream.
     *
     * @param out Output stream.
     *
     * @throws IOException if data cannot be written.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        writeTreeNode(out);
        out.writeObject(this.data);
    }

    /**
     * Deserializes state of this node from the input stream.
     *
     * @param in Input stream
     *
     * @throws IOException            If data cannot be read.
     * @throws ClassNotFoundException If object cannot be created.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        readTreeNode(in);
        this.data = (D) in.readObject();
    }

    /**
     * Returns a hash code value for the object calculate from 
     * the value assigned to this node and additional data stored in the node.
     * 
     * @return a hash code for this object.
     */
    @Override
    public int hashCode() {
        return 5 + 37 * Objects.hashCode(this.value) + 47 * Objects.hashCode(this.data); 
    }

    /**
     * Return true if a compared object is a node with the same value and additional data assigned.
     * 
     * @param obj Node with which to compare.
     * @return true if this node has the same value and additional data assigned.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else {
            if (obj instanceof TreeNodeWithData) {
                TreeNodeWithData node = (TreeNodeWithData) obj;
                return Objects.equals(this.value, node.getValue()) 
                        && Objects.equals(this.data, node.getData());
            } else {
                TreeNode node = (TreeNode) obj;
                return Objects.equals(this.value, node.getValue());
            }
        }
    }
    
    

}
