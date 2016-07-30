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

import org.neurpheus.collections.tree.TreeNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a single nod in a linked list tree.
 *
 * @author Jakub Strychowski
 */
public class LinkedListTreeNode implements TreeNode<Integer> {

    /** Position in the LLT unit array where this node is defined. */
    private LinkedListPosition pos;

    /**
     * Constructs a new node defined at the specified LLT position.
     *
     * @param pos Information about a position in a LLT unit array and traversal history.
     */
    protected LinkedListTreeNode(LinkedListPosition pos) {
        this.pos = pos;
    }

    /**
     * Returns a unit describing this node.
     *
     * @return a definition of this node in the LLT structure.
     */
    protected LinkedListTreeUnit getUnit() {
        return pos.getUnit();
    }

    @Override
    public Integer getValue() {
        int vc = pos.getUnit().getValueCode();
        return pos.getUnitArray().getValueMapping()[vc];
    }

    @Override
    public void setValue(Integer newValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isLeaf() {
        return getUnit().isWordEnd();
    }

    @Override
    public boolean hasExtraData() {
        return false;
    }

    /**
     * Returns a definition of a position in the LLT structure during traversal.
     *
     * @return Definition of the current position in the LLT structure.
     */
    protected LinkedListPosition getPosition() {
        return pos;
    }

    @Override
    public int getNumberOfChildren() {
        return getChildren().size();
    }

    @Override
    public LinkedListTreeNode getChildAtPos(int index) {
        return getChildren().get(index);
    }

    @Override
    public void clear() {
        pos.dispose();
        pos = null;
    }
    
    /**
     * Returns a readonly list of children nodes of this node.
     *
     * @return List of children nodes or empty list if this node is a leaf.
     */
    @Override
    public List<LinkedListTreeNode> getChildren() {
        List<LinkedListTreeNode> result = Collections.emptyList();
        LinkedListTreeUnit unit = pos.getUnit();
        if (unit.isWordContinued()) {
            result = new ArrayList<>(5);
            LinkedListPosition childPosition = pos.nextLevel();
            while (childPosition != null) {
                LinkedListTreeUnit childUnit = childPosition.getUnit();
                if (childUnit == null) {
                    throw new IllegalStateException("LLT strcuture is incoherent");
                }
                LinkedListTreeNode child = childUnit.isWordEnd() 
                        ? new LinkedListTreeDataNode(childPosition) 
                        : new LinkedListTreeNode(childPosition);
                result.add(child);
                childPosition = childPosition.nextChild();
            }
        }
        return result;
    }

    @Override
    public LinkedListTreeNode getChild(Integer key) {
        if (pos.isWordContinued()) {
            LinkedListPosition childPosition = pos.goToNextLevel();
            if (childPosition != null) {
                int keyValue = key;
                keyValue = pos.getUnitArray().mapToValueCode(keyValue);

                while (childPosition != null) {
                    int valueCode = childPosition.getValueMapped();
                    if (valueCode == keyValue) {
                        return childPosition.isWordEnd()
                                ? new LinkedListTreeDataNode(childPosition)
                                : new LinkedListTreeNode(childPosition);
                    }
                    childPosition = valueCode < keyValue ? childPosition.goToNextChild() : null;
                }
            }
        }
        return null;
    }

    @Override
    public LinkedListTreeNode getChild(final Integer key, final TreeNode fromNode) {
        if (fromNode == null) {
            return getChild(key);
        } else {
            LinkedListPosition childPosition;
            childPosition = new LinkedListPosition(((LinkedListTreeNode) fromNode).pos);
            childPosition = childPosition.goToNextChild();
            int keyValue = key;
            keyValue = pos.getUnitArray().mapToValueCode(keyValue);
            while (childPosition != null) {
                int valueCode = childPosition.getValueMapped();
                if (valueCode == keyValue) {
                    return childPosition.isWordEnd() 
                            ? new LinkedListTreeDataNode(childPosition) 
                            : new LinkedListTreeNode(childPosition);
                }
                childPosition = valueCode < keyValue ? childPosition.goToNextChild() : null;
            }
            return null;
        }
    }


    /**
     * Returns a child which contains specified key.
     *
     * @param key      The transition key to the child.
     * @param stack    Stack used for fast recursion.
     * @param stackPos Current position in the stack
     *
     * @return The child or null if there is no child containing specified key.
     */
    @SuppressWarnings({"squid:MethodCyclomaticComplexity", 
                       "common-java:DuplicatedBlocks", 
                       "squid:S134"})
    public LinkedListTreeNode getChild(final Integer key, final int[] stack, int stackPos) {
        final LinkedListTreeUnitArray units = pos.getUnitArray();
        final int unitsSize = units.size();
        final int startStackPos = stackPos;
        int index = pos.getPos();
        int fastIndex = units.getFastIndex(index);
        int nested = pos.getNested() ? 1 : 0;
        int unitsToRead = pos.getUnitsToRead();
        LinkedListPosition returnPos = pos.getReturnPos();
        if (units.isWordContinuedFast(fastIndex)) {
            int cint = key;
            cint = units.mapToValueCode(cint);
            // go to next level
            if (nested == 1 && unitsToRead <= 1) {
                // return from the absolute pointer
                if (stackPos <= startStackPos) {
                    if (returnPos != null) {
                        unitsToRead = returnPos.getUnitsToRead();
                        nested = returnPos.getNested() ? 1 : 0;
                        index = returnPos.getPos();
                        returnPos = returnPos.getReturnPos();
                    } else {
                        // not matched
                        return null;
                    }
                } else {
                    unitsToRead = stack[stackPos--];
                    nested = stack[stackPos--];
                    index = stack[stackPos--];
                }
            } else {
                ++index;
                --unitsToRead;
            }
            fastIndex = units.getFastIndex(index);
            // traverse all children (getChild)
            boolean found = false;
            while (!found) {
                // process absolute pointer
                while (index < unitsSize && units.isAbsolutePointerFast(fastIndex)) {
                    if (nested == 0 || unitsToRead > 1) {
                        stack[++stackPos] = index + 1;
                        stack[++stackPos] = nested;
                        stack[++stackPos] = unitsToRead - 1;
                    }
                    unitsToRead = units.getValueCodeFast(fastIndex);
                    nested = unitsToRead != 0 ? 1 : 0;
                    index = units.getDistanceFast(fastIndex);
                    fastIndex = units.getFastIndex(index);
                }
                // check key (getChild)
                int vc = units.getValueCodeFast(fastIndex);
                if (vc == cint) {
                    // found = true
                    LinkedListPosition retPos = returnPos == null ? null : new LinkedListPosition(
                            returnPos);
                    LinkedListPosition position = new LinkedListPosition(units, index, retPos,
                                                                         unitsToRead,
                                                                         nested == 1);
                    LinkedListTreeNode child = 
                            units.isWordEndFast(fastIndex) 
                            ? new LinkedListTreeDataNode(position) :
                            new LinkedListTreeNode(position);
                    position.setAbsProcessed(true);
                    // determine return position
                    while (stackPos > startStackPos) {
                        unitsToRead = stack[stackPos--];
                        nested = stack[stackPos--];
                        index = stack[stackPos--];
                        LinkedListPosition tmp = 
                            new LinkedListPosition(units, index, retPos, unitsToRead, nested == 1);
                        position.setReturnPos(tmp);
                        position = tmp;
                    }
                    return child;
                } else if (vc > cint) {
                    return null;
                } else {
                    // go to next child (getChild)
                    int distance = units.getDistanceFast(fastIndex);
                    if (distance > 0) {
                        int target = index + distance;
                        if (nested == 1 && unitsToRead > 0 && target >= index + unitsToRead) {
                            // return from the absolute pointer
                            if (stackPos <= startStackPos) {
                                if (returnPos != null) {
                                    unitsToRead = returnPos.getUnitsToRead();
                                    nested = returnPos.getNested() ? 1 : 0;
                                    index = returnPos.getPos();
                                    returnPos = returnPos.getReturnPos();
                                } else {
                                    // not matched
                                    return null;
                                }
                            } else {
                                unitsToRead = stack[stackPos--];
                                nested = stack[stackPos--];
                                index = stack[stackPos--];
                            }
                        } else {
                            index = target;
                            unitsToRead = unitsToRead - distance;
                        }
                        fastIndex = units.getFastIndex(index);
                    } else {
                        // not matched
                        return null;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns data stored in a tree at the specified location starting from the current node.
     * 
     * @param path      A list of characters describing successive nodes 
     *                  in a tree while traversal. 
     * @param stack     Stack used for fast recursion in the tree.
     * @param stackPos  Current position in the stack.
     * 
     * @return  Integer identifier of an object stored in the tree or null if there is no 
     *          data at the specified path.
     */
    @SuppressWarnings({"squid:MethodCyclomaticComplexity", 
                       "common-java:DuplicatedBlocks", 
                       "squid:S134"})
    public Integer getData(String path, int[] stack, int stackPos) {
        LinkedListTreeUnitArray units = pos.getUnitArray();
        int unitsSize = units.size();
        int index = pos.getPos();
        int fastIndex = units.getFastIndex(index);
        int nested = pos.getNested() ? 1 : 0;
        int unitsToRead = pos.getUnitsToRead();
        for (int i = 0; i < path.length(); i++) {
            int cint = (int) path.charAt(i);
            cint = units.mapToValueCode(cint);
            // go to next level
            if (units.isWordContinuedFast(fastIndex)) {
                if (nested == 1 && unitsToRead <= 1) {
                    // return from the absolute pointer
                    unitsToRead = stack[stackPos--];
                    nested = stack[stackPos--];
                    index = stack[stackPos--];
                } else {
                    ++index;
                    --unitsToRead;
                }
                fastIndex = units.getFastIndex(index);
            } else {
                return null;
            }
            // traverse all children (getData)
            boolean found = false;
            while (!found) {
                // process absolute pointer
                while (index < unitsSize && units.isAbsolutePointerFast(fastIndex)) {
                    if (nested == 0 || unitsToRead > 1) {
                        stack[++stackPos] = index + 1;
                        stack[++stackPos] = nested;
                        stack[++stackPos] = unitsToRead - 1;
                    }
                    unitsToRead = units.getValueCodeFast(fastIndex);
                    nested = unitsToRead != 0 ? 1 : 0;
                    index = units.getDistanceFast(fastIndex);
                    fastIndex = units.getFastIndex(index);
                }
                // check key - getData
                int vc = units.getValueCodeFast(fastIndex);
                if (vc == cint) {
                    found = true;
                } else if (vc > cint) {
                    return null;
                } else {
                    // go to next child (getData)
                    int distance = units.getDistanceFast(fastIndex);
                    if (distance > 0) {
                        index += distance;
                        unitsToRead -= distance;
                        fastIndex = units.getFastIndex(index);
                    } else {
                        // not matched
                        return null;
                    }
                }
            }
        }
        if (units.isWordEndFast(fastIndex)) {
            return units.getDataCodeFast(fastIndex);
        }
        return null;
    }

    /**
     * This method is not supported for this implementation of the {@link TreeNode} interface.
     * <p>
     * A tree should be constructed by the      
     * {@link LinkedListTreeFactory#createTree(org.neurpheus.collections.tree.Tree, 
     * boolean, boolean, boolean)} method using any source/base tree.
     * </p>
     *
     * @param newChildren unused here.
     *
     * @exception UnsupportedOperationException
     */
    @Override
    public void setChildren(List newChildren) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported for this implementation of the {@link TreeNode} interface.
     * <p>
     * A tree should be constructed by the     
     * {@link LinkedListTreeFactory#createTree(org.neurpheus.collections.tree.Tree, 
     * boolean, boolean, boolean)} method using any source/base tree.
     * </p>
     *
     * @param child unused here.
     *
     * @exception UnsupportedOperationException
     */
    @Override
    public void addChild(TreeNode child) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported for this implementation of the {@link TreeNode} interface.
     * <p>
     * A tree should be constructed by the      
     * {@link LinkedListTreeFactory#createTree(org.neurpheus.collections.tree.Tree, 
     * boolean, boolean, boolean)} method using any source/base tree.
     * </p>
     *
     * @param index unused here.
     * @param child unused here.
     *
     * @exception UnsupportedOperationException
     */
    @Override
    public void addChild(int index, TreeNode child) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported for this implementation of the {@link TreeNode} interface.
     * <p>
     * A tree should be constructed by the      
     * {@link LinkedListTreeFactory#createTree(org.neurpheus.collections.tree.Tree, 
     * boolean, boolean, boolean)} method using any source/base tree.
     * </p>
     *
     * @param child unused here.
     *
     * @return no return - exception is thrown.
     *
     * @exception UnsupportedOperationException
     */
    @Override
    public boolean removeChild(TreeNode child) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported for this implementation of the {@link TreeNode} interface.
     * <p>
     * A tree should be constructed by the      
     * {@link LinkedListTreeFactory#createTree(org.neurpheus.collections.tree.Tree, 
     * boolean, boolean, boolean)} method using any source/base tree.
     * </p>
     *
     * @param index unused here.
     *
     * @return no return - exception is thrown.
     *
     * @exception UnsupportedOperationException
     */
    @Override
    public TreeNode removeChild(int index) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported for this implementation of the {@link TreeNode} interface.
     * <p>
     * A tree should be constructed by the      
     * {@link LinkedListTreeFactory#createTree(org.neurpheus.collections.tree.Tree, 
     * boolean, boolean, boolean)} method using any source/base tree.
     * </p>
     *
     * @param fromNode unused here.
     * @param toNode   unused here.
     *
     * @return no return - exception is thrown.
     *
     * @exception UnsupportedOperationException
     */
    @Override
    public int replaceChild(TreeNode fromNode, TreeNode toNode) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof LinkedListTreeNode) {
            return pos.getPos() == ((LinkedListTreeNode) anObject).getPosition().getPos();
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return pos.hashCode();
        
    }

}
