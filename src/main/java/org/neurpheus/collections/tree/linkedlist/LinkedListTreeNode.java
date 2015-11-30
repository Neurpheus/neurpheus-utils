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

import java.util.ArrayList;
import java.util.List;
import org.neurpheus.collections.tree.TreeNode;

/**
 *
 * @author szkoleniowy
 */
public class LinkedListTreeNode implements TreeNode {

    private LinkedListPosition pos;

    public LinkedListTreeNode() {
        this.pos = null;
    }

    public LinkedListTreeNode(LinkedListPosition pos) {
        this.pos = pos;
    }

    protected LinkedListTreeUnit getUnit() {
        return pos.getUnit();
    }

    @Override
    public Object getValue() {
        int vc = pos.getUnit().getValueCode();
        return pos.getUnitArray().getValueMapping()[vc];
    }

    @Override
    public void setValue(Object newValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isLeaf() {
        return getUnit().isWordEnd();
    }

    /**
     * Returns a readonly list of children nodes of this node.
     *
     * @return List of children nodes or empty list if this node is a leaf.
     */
    @Override
    public List getChildren() {
        List result = new ArrayList();
        LinkedListTreeUnit unit = pos.getUnit();
        if (unit.isWordContinued()) {
            LinkedListPosition childPosition = pos.nextLevel();
            while (childPosition != null) {
                LinkedListTreeUnit childUnit = childPosition.getUnit();
                if (childUnit == null) {
                    System.out.println("error");
                }
                LinkedListTreeNode child = childUnit.isWordEnd() ? new LinkedListTreeDataNode(
                        childPosition) : new LinkedListTreeNode(childPosition);
                result.add(child);
                childPosition = childPosition.nextChild();
            }
        }
        return result;
    }

    /**
     * Sets a new list of children nodes for this node.
     *
     * @param children A new list of children nodes of this node or empty list if this node is a
     *                 leaf.
     */
    @Override
    public void setChildren(List newChildren) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a child which contains specified key.
     *
     * @param key The transition key to the child.
     *
     * @return The child or null if there is no child contianing specified key.
     */
    @Override
    public TreeNode getChild(Object key) {
        if (pos.isWordContinued()) {
            LinkedListPosition childPosition = pos.goToNextLevel();
            if (childPosition != null) {
                int keyValue = ((Integer) key).intValue();
                keyValue = pos.getUnitArray().mapToValueCode(keyValue);

                while (childPosition != null) {
                    int valueCode = childPosition.getValueMapped();
                    if (valueCode == keyValue) {
                        return childPosition.isWordEnd()
                                ? new LinkedListTreeDataNode(childPosition)
                                : new LinkedListTreeNode(childPosition);
                    }
                    childPosition = valueCode < keyValue ? childPosition.goToNextChild() : null;
                    //childPosition = childPosition.goToNextChild();
                }
            }
        }
        return null;
    }

    @Override
    public TreeNode getChild(final Object key, final TreeNode fromNode) {
        if (fromNode == null) {
            return getChild(key);
        } else {
            LinkedListPosition childPosition;
            childPosition = new LinkedListPosition(((LinkedListTreeNode) fromNode).pos);
            childPosition = childPosition.goToNextChild();
            int keyValue = ((Integer) key).intValue();
            keyValue = pos.getUnitArray().mapToValueCode(keyValue);
            while (childPosition != null) {
                int valueCode = childPosition.getValueMapped();
                if (valueCode == keyValue) {
                    LinkedListTreeNode child = childPosition.isWordEnd() ? new LinkedListTreeDataNode(
                            childPosition) : new LinkedListTreeNode(childPosition);
                    return child;
                }
                childPosition = valueCode < keyValue ? childPosition.goToNextChild() : null;
            }
            return null;
        }
    }

    /**
     * Returns the number of children nodes of this node.
     *
     * @return The number of children of this node.
     */
    @Override
    public int getNumberOfChildren() {
        return getChildren().size();
    }

    /**
     * Returns a chid available at the given position in the ordered list of children nodes.
     *
     * @param pos The index of child nodes on the nodes list.
     *
     * @return The child from the given position.
     */
    @Override
    public TreeNode getChildAtPos(int index) {
        return (TreeNode) getChildren().get(index);
    }

    /**
     * Adds the given node to the end of a list of children nodes.
     *
     * @param child The child node to add.
     */
    @Override
    public void addChild(TreeNode child) {
        throw new UnsupportedOperationException();
    }

    /**
     * Adds the given node child
     *
     * @param index
     * @param pos
     * @param child
     */
    @Override
    public void addChild(int index, TreeNode child) {
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @param child
     *
     * @return
     */
    @Override
    public boolean removeChild(TreeNode child) {
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @param index
     * @param pos
     *
     * @return
     */
    @Override
    public TreeNode removeChild(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int replaceChild(TreeNode fromNode, TreeNode toNode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        pos.dispose();
        pos = null;
    }

    /**
     * Returns a child which contains specified key.
     *
     * @param key      The transition key to the child.
     * @param stack
     * @param stackPos
     *
     * @return The child or null if there is no child contianing specified key.
     */
    public TreeNode getChild(final Object key, final int[] stack, int stackPos) {
        final LinkedListTreeUnitArray units = pos.getUnitArray();
        final int unitsSize = units.size();
        final int startStackPos = stackPos;
        int p = pos.getPos();
        int fp = units.getFastIndex(p);
        int nested = pos.getNested() ? 1 : 0;
        int unitsToRead = pos.getUnitsToRead();
        LinkedListPosition returnPos = pos.getReturnPos();
        // process absolute pointer
        while (p < unitsSize && units.isAbsolutePointerFast(fp)) {
            if (nested == 0 || unitsToRead > 1) {
                stack[++stackPos] = p + 1;
                stack[++stackPos] = nested;
                stack[++stackPos] = unitsToRead - 1;
            }
            unitsToRead = units.getValueCode(fp);
            nested = unitsToRead != 0 ? 1 : 0;
            p = units.getDistanceFast(fp);
            fp = units.getFastIndex(p);
        }
        if (units.isWordContinuedFast(fp)) {
            int cint = ((Integer) key).intValue();
            cint = units.mapToValueCode(cint);
            // go to next level
            if (nested == 1 && unitsToRead <= 1) {
                // return from the absolute pointer
                if (stackPos <= startStackPos) {
                    if (returnPos != null) {
                        unitsToRead = returnPos.getUnitsToRead();
                        nested = returnPos.getNested() ? 1 : 0;
                        p = returnPos.getPos();
                        returnPos = returnPos.getReturnPos();
                    } else {
                        // not matched
                        return null;
                    }
                } else {
                    unitsToRead = stack[stackPos--];
                    nested = stack[stackPos--];
                    p = stack[stackPos--];
                }
            } else {
                ++p;
                --unitsToRead;
            }
            fp = units.getFastIndex(p);
            // traverse all children (getChild)
            boolean found = false;
            while (!found) {
                // process absolute pointer
                while (p < unitsSize && units.isAbsolutePointerFast(fp)) {
                    if (nested == 0 || unitsToRead > 1) {
                        stack[++stackPos] = p + 1;
                        stack[++stackPos] = nested;
                        stack[++stackPos] = unitsToRead - 1;
                    }
                    unitsToRead = units.getValueCode(fp);
                    nested = unitsToRead != 0 ? 1 : 0;
                    p = units.getDistanceFast(fp);
                    fp = units.getFastIndex(p);
                }
                // check key (getChild)
                //int vc = units.getValueCode(fp);
                int vc = units.getValueCode(fp);
                if (vc == cint) {
                    // found = true
                    LinkedListPosition retPos = returnPos == null ? null : new LinkedListPosition(
                            returnPos);
                    LinkedListPosition position = new LinkedListPosition(units, p, retPos,
                                                                         unitsToRead,
                                                                         nested == 1);
                    LinkedListTreeNode child = units.isWordEndFast(fp) ? new LinkedListTreeDataNode(
                            position) : new LinkedListTreeNode(position);
                    position.setAbsProcessed(true);
                    // determine return position
                    while (stackPos > startStackPos) {
                        unitsToRead = stack[stackPos--];
                        nested = stack[stackPos--];
                        p = stack[stackPos--];
                        LinkedListPosition tmp = new LinkedListPosition(units, p, retPos,
                                                                        unitsToRead, nested == 1);
                        position.setReturnPos(tmp);
                        position = tmp;
                    }
                    //                    if (!child.pos.equals(childTmp.pos)) {
                    //                        return null;
                    //                    }
                    return child;
                } else if (vc > cint) {
                    return null;
                } else {
                    // go to next child (getChild)
                    int d = units.getDistanceFast(fp);
                    if (d > 0) {
                        int target = p + d;
                        if (nested == 1 && unitsToRead > 0 && target >= p + unitsToRead) {
                            // return from the absolute pointer
                            if (stackPos <= startStackPos) {
                                if (returnPos != null) {
                                    unitsToRead = returnPos.getUnitsToRead();
                                    nested = returnPos.getNested() ? 1 : 0;
                                    p = returnPos.getPos();
                                    returnPos = returnPos.getReturnPos();
                                } else {
                                    // not matched
                                    return null;
                                }
                            } else {
                                unitsToRead = stack[stackPos--];
                                nested = stack[stackPos--];
                                p = stack[stackPos--];
                            }
                        } else {
                            p = target;
                            unitsToRead = unitsToRead - d;
                        }
                        fp = units.getFastIndex(p);
                    } else {
                        // not matched
                        return null;
                    }
                }
            }
        }
        return null;
    }

    public Object getData(String str, int[] stack, int stackPos) {
        int startStackPos = stackPos;
        LinkedListTreeUnitArray units = pos.getUnitArray();
        int unitsSize = units.size();
        int p = pos.getPos();
        int fp = units.getFastIndex(p);
        int nested = pos.getNested() ? 1 : 0;
        int unitsToRead = pos.getUnitsToRead();
        for (int i = 0; i < str.length(); i++) {
            int cint = (int) str.charAt(i);
            cint = units.mapToValueCode(cint);
            // process absolute pointer
            while (p < unitsSize && units.isAbsolutePointerFast(fp)) {
                if (nested == 0 || unitsToRead > 1) {
                    stack[++stackPos] = p + 1;
                    stack[++stackPos] = nested;
                    stack[++stackPos] = unitsToRead - 1;
                }
                unitsToRead = units.getValueCode(fp);
                nested = unitsToRead != 0 ? 1 : 0;
                p = units.getDistanceFast(fp);
                fp = units.getFastIndex(p);
            }
            // go to next level
            if (units.isWordContinuedFast(fp)) {
                if (nested == 1 && unitsToRead <= 1) {
                    // return from the absolute pointer
                    if (stackPos <= startStackPos) {
                        LinkedListPosition returnPos = pos.getReturnPos();
                        if (returnPos != null) {
                            unitsToRead = returnPos.getUnitsToRead();
                            nested = returnPos.getNested() ? 1 : 0;
                            p = returnPos.getPos();
                        } else {
                            // not matched
                            return null;
                        }
                    } else {
                        unitsToRead = stack[stackPos--];
                        nested = stack[stackPos--];
                        p = stack[stackPos--];
                    }
                } else {
                    ++p;
                    --unitsToRead;
                }
                fp = units.getFastIndex(p);
            } else {
                return null;
            }
            // traverse all children (getData)
            boolean found = false;
            while (!found) {
                // process absolute pointer
                while (p < unitsSize && units.isAbsolutePointerFast(fp)) {
                    if (nested == 0 || unitsToRead > 1) {
                        stack[++stackPos] = p + 1;
                        stack[++stackPos] = nested;
                        stack[++stackPos] = unitsToRead - 1;
                    }
                    unitsToRead = units.getValueCode(fp);
                    nested = unitsToRead != 0 ? 1 : 0;
                    p = units.getDistanceFast(fp);
                    fp = units.getFastIndex(p);
                }
                // check key (getData)
                int vc = units.getValueCode(fp);
                if (vc == cint) {
                    found = true;
                } else if (vc > cint) {
                    return null;
                } else {
                    // go to next child (getData)
                    int d = units.getDistanceFast(fp);
                    if (d > 0) {
                        int target = p + d;
                        if (nested == 1 && unitsToRead > 0 && target >= p + unitsToRead) {
                            // return from the absolute pointer
                            if (stackPos <= startStackPos) {
                                LinkedListPosition returnPos = pos.getReturnPos();
                                if (returnPos != null) {
                                    unitsToRead = returnPos.getUnitsToRead();
                                    nested = returnPos.getNested() ? 1 : 0;
                                    p = returnPos.getPos();
                                    returnPos = returnPos.getReturnPos();
                                } else {
                                    // not matched
                                    return null;
                                }
                            } else {
                                unitsToRead = stack[stackPos--];
                                nested = stack[stackPos--];
                                p = stack[stackPos--];
                            }
                        } else {
                            p = target;
                            unitsToRead = unitsToRead - d;
                        }
                        fp = units.getFastIndex(p);
                    } else {
                        // not matched
                        return null;
                    }
                }
            }
        }
        if (units.isWordEndFast(fp)) {
            int dataCode = units.getDataCodeFast(fp);
            return new Integer(dataCode);
        }
        return null;
    }

    @Override
    public boolean hasExtraData() {
        return false;
    }

}
