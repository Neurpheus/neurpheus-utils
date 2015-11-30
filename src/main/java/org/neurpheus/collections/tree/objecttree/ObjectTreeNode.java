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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Implementation of the TreeNode interface using collections of objects.
 *
 * @param <T> Type of values describing the node.
 * 
 * @author Jakub Strychowski
 */
public class ObjectTreeNode<T> implements TreeNode<T>, Serializable, Comparable {

    /**
     * Unique version of this class.
     */
    private static final long serialVersionUID = 770608070910113713L;

    // Holds value assigned to this node
    protected transient T value;

    /**
     * An object holding children nodes of this node. This field can contains different types of
     * values (reduced memory consumption):
     * <ul>
     * <li>null - for nodes without children</li>
     * <li>single TreeNode object - for nodes with have only one child</li>
     * <li>ArrayList - for nodes having many children</li>
     * </ul>
     */
    private transient Object children;

    /**
     * Constructs empty node with no value assigned and no children.
     */
    protected ObjectTreeNode() {
    }

    /**
     * Constructs a new node and marks it with the given value. Created node has no children.
     *
     * @param value The value assigned to the node.
     */
    protected ObjectTreeNode(final T value) {
        this.value = value;
    }

    /**
     * Returns a value assigned to this node. The value in most cases distinguish nodes having the
     * same parent.
     *
     * @return Any object stored as value of the node.
     */
    @Override
    public T getValue() {
        return value;
    }

    /**
     * Sets a new value for this node. The value in most cases distinguish nodes having the same
     * parent.
     *
     * @param newValue Any object to store as a value of this node.
     */
    @Override
    public void setValue(T newValue) {
        this.value = newValue;
    }

    /**
     * Checks if this nodes is a leaf - has no child nodes.
     *
     * @return <strong>true</strong> if this node has no children.
     */
    @Override
    public boolean isLeaf() {
        if (children == null) {
            return true;
        } else if (children instanceof ObjectTreeNode) {
            return false;
        } else {
            return ((List) children).isEmpty();
        }
    }

    /**
     * Returns a readonly list of children nodes of this node.
     *
     * @return List of children nodes or empty list if this node is a leaf.
     */
    @Override
    public List<TreeNode> getChildren() {
        if (children == null) {
            return Collections.emptyList();
        } else if (children instanceof ObjectTreeNode) {
            return Collections.singletonList((TreeNode) children);
        } else {
            return (List<TreeNode>) children;
        }
    }

    /**
     * Sets child nodes for this node.
     *
     * @param newChildren A list of nodes for which this node is a parent.
     *
     * @exception NullPointerException If the specified {@code children} argument is null.
     */
    @Override
    public void setChildren(final List newChildren) {
        if (null == newChildren) {
            children = null;
        } else if (newChildren.isEmpty()) {
            children = null;
        } else if (newChildren.size() == 1) {
            children = newChildren.get(0);
        } else {
            children = newChildren;
        }
    }

    /**
     * Return number of children of this node.
     *
     * @return Number of child nodes.
     */
    @Override
    public int getNumberOfChildren() {
        if (children == null) {
            return 0;
        } else if (children instanceof ObjectTreeNode) {
            return 1;
        } else {
            return ((List) children).size();
        }
    }

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
    @Override
    public TreeNode getChildAtPos(int index) {
        if (children == null) {
            throw new IndexOutOfBoundsException(
                    "This node dosn't contain any child node.");
        } else if (children instanceof ObjectTreeNode) {
            if (index > 0) {
                throw new IndexOutOfBoundsException(
                        "This node contains only one child. Invalid index parameter = " + index);
            } else {
                return (TreeNode) children;
            }
        } else {
            return (TreeNode) ((List) children).get(index);
        }
    }

    /**
     * Adds a new child node to this node.
     *
     * @param child A new child node - it will be added at the end of a list of children.
     *
     * @exception NullPointerException If the specified {@code child} argument is null.
     */
    @Override
    public void addChild(TreeNode child) {
        if (children == null) {
            children = child;
        } else if (children instanceof ObjectTreeNode) {
            List tmp = new ArrayList(2);
            tmp.add(children);
            tmp.add(child);
            children = tmp;
        } else {
            ((List) children).add(child);
        }
    }

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
    @Override
    public void addChild(int index, TreeNode child) {
        if (children == null) {
            if (index != 0) {
                throw new IndexOutOfBoundsException("Wrong position: " + index);
            }
            children = child;
        } else if (children instanceof ObjectTreeNode) {
            ArrayList tmp = new ArrayList(2);
            tmp.add(children);
            tmp.add(index, child);
            children = tmp;
        } else {
            ((List) children).add(index, child);
        }
    }

    /**
     * Removes the given node from a list of children of this node.
     *
     * @param child The child to remove.
     *
     * @return <code>true</code> if the given node has been found and removed from a list of
     *         children nodes.
     */
    @Override
    public boolean removeChild(TreeNode child) {
        if (children == null) {
            return false;
        } else if (children instanceof ObjectTreeNode) {
            if (child.equals(children)) {
                children = null;
                return true;
            }
        } else {
            boolean result = ((List) children).remove(child);
            if (((List) children).size() == 1) {
                children = ((List) children).get(0);
            }
            return result;
        }
        return false;
    }

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
    @Override
    public TreeNode removeChild(int index) {
        TreeNode result = null;
        if (children == null) {
            throw new IndexOutOfBoundsException("Wrong index: " + index);
        } else if (children instanceof ObjectTreeNode) {
            if (index != 0) {
                throw new IndexOutOfBoundsException("Wrong index: " + index);
            }
            result = (TreeNode) children;
            children = null;
        } else {
            List nodes = (List) children;
            result = (TreeNode) nodes.remove(index);
            if (nodes.size() == 1) {
                children = nodes.get(0);
            }
        }
        return result;
    }

    /**
     * Replaces child nodes of this node.
     *
     * @param fromNode the node which should be removed from the list of child nodes.
     * @param toNode   the node which should be added to the list of child nodes at the place of
     *                 previous node.
     *
     * @return position of the replaced node.
     */
    @Override
    public int replaceChild(TreeNode fromNode, TreeNode toNode) {
        int result = -1;
        if (children != null) {
            if (children instanceof ObjectTreeNode) {
                if (children == fromNode) {
                    children = toNode;
                    result = 0;
                }
            } else {
                int index = 0;
                for (TreeNode tn : (List<TreeNode>) children) {
                    if (tn == fromNode) {
                        ((List) children).set(index, toNode);
                        result = index;
                    }
                    index++;
                }
            }
        }
        return result;
    }

    /**
     * Removes all children of this node.
     */
    @Override
    public void clear() {
        if (children != null) {
            if (children instanceof ObjectTreeNode) {
                ((ObjectTreeNode) children).clear();
            } else {
                for (Iterator it = ((List) children).iterator(); it.hasNext();) {
                    TreeNode tn = (TreeNode) it.next();
                    tn.clear();
                }
                ((List) children).clear();
            }
            children = null;
        }
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
        return false;
    }

    /**
     * Sorts children of this node by values assigned to child nodes.
     */
    public void sort() {
        if (children != null) {
            if (children instanceof ObjectTreeNode) {
                ((ObjectTreeNode) children).sort();
            } else {
                Collections.sort((List) children);
                for (ObjectTreeNode node : (List<ObjectTreeNode>) children) {
                    node.sort();
                }
            }
        }
    }

    /**
     * Returns a child node represented the given key value.
     *
     * @param key the value of a node.
     *
     * @return Found child node or null if this node doesn't have any node with the given value.
     */
    @Override
    public TreeNode getChild(T key) {
        if (children == null || key == null) {
            return null;
        }
        if (children instanceof ObjectTreeNode) {
            return key.equals(((ObjectTreeNode) children).getValue()) ? (TreeNode) children : null;
        } else {
            for (Iterator it = ((List) children).iterator(); it.hasNext();) {
                TreeNode tn = (TreeNode) it.next();
                if (key.equals(tn.getValue())) {
                    return tn;
                }
            }
        }
        return null;
    }

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
    @Override
    public TreeNode getChild(T key, TreeNode fromNode) {
        if (fromNode == null) {
            return getChild(key);
        } else if (children != null && key != null) {
            if (children instanceof TreeNode) {
                if (fromNode == children && key.equals(fromNode.getValue())) {
                    return (TreeNode) children;
                }
            } else {
                return fastGetChild(key, fromNode);
            }
        }
        return null;
    }
    
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
    private TreeNode fastGetChild(T key, TreeNode fromNode) {
        boolean check = false;
        for (TreeNode tn : (List<TreeNode>) children) {
            check |= tn == fromNode;
            if (check && key.equals(tn.getValue())) {
                return tn;
            }
        }
        return null;
    }

    /**
     * Compares two nodes by values assigned to these nodes. Returns a negative integer, zero, or a
     * positive integer as value assigned to this nodes is less than, equal to, or greater than the
     * value of the specified node.
     *
     * @param obj Second node
     *
     * @return Result of comparison.
     */
    @Override
    public int compareTo(Object obj) {
        if (obj == null) {
            return 1;
        }
        Object val = ((TreeNode) obj).getValue();
        if (this.value == null) {
            return val == null ? 0 : -1;
        } else {
            return val == null ? 1 : ((Comparable) this.value).compareTo(val);
        }
    }
    
    
    /**
     * Return true if a compared object is a node with the same value assigned.
     * 
     * @param obj Node with which to compare.
     * @return true if this node has the same value assigned.
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj instanceof TreeNode) {
            return Objects.equals(this.value, ((TreeNode) obj).getValue());
        }
        return false;
    }

    /**
     * Returns a hash code value for the object calculate from the value assigned to this node.
     * 
     * @return a hash code for this object.
     */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.value);
        return hash;
    }

    /**
     * Writes this node to the objects stream.
     *
     * @param out Output stream.
     *
     * @throws IOException if data cannot be written.
     */
    protected void writeTreeNode(ObjectOutputStream out) throws IOException {
        out.writeObject((Serializable) value);
        if (children == null) {
            out.writeInt(0);
        } else if (children instanceof ObjectTreeNode) {
            out.writeInt(1);
            out.writeObject((ObjectTreeNode) children);
        } else {
            List<ObjectTreeNode> nodeList = (List<ObjectTreeNode>) children;
            out.writeInt(nodeList.size());
            for (ObjectTreeNode node : nodeList) {
                out.writeObject(node);
            }
        }
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
    }

    /**
     * Reads state of this node from the input stream.
     *
     * @param in Input stream.
     *
     * @throws IOException            if data cannot be read from the stream.
     * @throws ClassNotFoundException if object cannot be created.
     */
    protected void readTreeNode(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.value = (T) in.readObject();
        int size = in.readInt();
        switch (size) {
            case 0:
                this.children = null;
                break;
            case 1:
                this.children = in.readObject();
                break;
            default:
                ArrayList<ObjectTreeNode> array = new ArrayList<>(size);
                for (int i = size; i > 0; i--) {
                    array.add((ObjectTreeNode) in.readObject());
                }
                this.children = array;
        }
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
    }

}
