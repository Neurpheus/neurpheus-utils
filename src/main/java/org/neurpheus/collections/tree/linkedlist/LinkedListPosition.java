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

/**
 * Represents a single element of a linked list (compressed form of a tree). This class helps in
 * traversing a tree stored in a packed form.
 *
 * @author Jakub Strychowski
 */
class LinkedListPosition {

    /**
     * Position of element on the linked list.
     */
    private int pos;

    /**
     * Position to which algorithms should return after processing current part of the list.
     */
    private LinkedListPosition returnPos;

    /**
     * How many elements can be read from the list before algorithm should return to previous
     * fragment of the list.
     */
    private int unitsToRead;

    /**
     * <code>true</code> if the current position is nested part of other fragment in the list.
     */
    private boolean nested;

    /**
     * Indicates if this position in a units array holds an absolute pointer and if this pointer has
     * been evaluated.
     */
    private boolean absProcessed;

    /**
     * Local reference to the internal representation of a linked list.
     */
    private LinkedListTreeUnitArray unitArray;

    protected LinkedListPosition(LinkedListTreeUnitArray unitArray, int pos,
                                 LinkedListPosition returnPos, int unitsToRead, boolean nested) {
        this.unitArray = unitArray;
        this.pos = pos;
        this.returnPos = returnPos;
        this.unitsToRead = unitsToRead;
        this.nested = nested;
        this.absProcessed = false;
    }

    protected LinkedListPosition(LinkedListPosition basePos) {
        this.unitArray = basePos.unitArray;
        this.pos = basePos.pos;
        this.returnPos = basePos.returnPos;
        this.unitsToRead = basePos.unitsToRead;
        this.nested = basePos.nested;
        this.absProcessed = basePos.absProcessed;
    }

    protected int getDistance() {
        if (!absProcessed) {
            processAbsolutePointer();
        }
        return unitArray.getDistance(pos);
    }

    protected LinkedListPosition nextLevel() {
        if (unitArray.isWordContinued(pos)) {
            if (nested && unitsToRead <= 1) {
                return returnPos;
            } else {
                return new LinkedListPosition(unitArray, pos + 1,
                                              returnPos, unitsToRead - 1, nested);
            }
        } else {
            return null;
        }
    }

    protected  LinkedListPosition goToNextLevel() {
        if (unitArray.isWordContinued(pos)) {
            if (nested && unitsToRead <= 1) {
                return returnPos == null ? null : new LinkedListPosition(returnPos);
            } else {
                return new LinkedListPosition(unitArray, pos + 1, returnPos, unitsToRead - 1, nested);
            }
        }
        return null;
    }

    protected  LinkedListPosition nextChild() {
        int d = unitArray.getDistance(pos);
        if (d > 0) {
            int target = pos + d;
            if (nested && unitsToRead > 0 && target >= pos + unitsToRead) {
                return returnPos;
            } else {
                return new LinkedListPosition(unitArray, target, returnPos, unitsToRead - d, nested);
            }
        } else {
            return null;
        }
    }

    protected  LinkedListPosition goToNextChild() {
        if (!absProcessed) {
            processAbsolutePointer();
        }
        int d = unitArray.getDistance(pos);
        if (d > 0) {
            int target = pos + d;
            if (nested && unitsToRead > 0 && target >= pos + unitsToRead) {
                return returnPos == null ? null : new LinkedListPosition(returnPos);
            } else {
                pos = target;
                unitsToRead = unitsToRead - d;
                absProcessed = false;
                return this;
            }
        } else {
            return null;
        }
    }

    protected  boolean isWordContinued() {
        if (!absProcessed) {
            processAbsolutePointer();
        }
        return unitArray.isWordContinued(pos);
    }

    protected  boolean isWordEnd() {
        if (!absProcessed) {
            processAbsolutePointer();
        }
        return unitArray.isWordEnd(pos);
    }

    protected  int getValueMapped() {
        if (!absProcessed) {
            processAbsolutePointer();
        }
        return unitArray.getValueCode(pos);
    }

    public LinkedListTreeUnit getUnit() {
        if (!absProcessed) {
            processAbsolutePointer();
        }
        return pos < unitArray.size() ? unitArray.get(pos) : null;
    }

    private void processAbsolutePointer() {
        if (!absProcessed) {
            absProcessed = true;
            boolean isPointer = pos < unitArray.size();
            while (isPointer) {
                int fastp = unitArray.getFastIndex(pos);
                isPointer = unitArray.isAbsolutePointerFast(fastp);
                if (isPointer) {
                    if (!nested || unitsToRead > 1) {
                        returnPos = new LinkedListPosition(unitArray, pos + 1, returnPos,
                                                           unitsToRead - 1, nested);
                    }
                    unitsToRead = unitArray.getValueCodeFast(fastp);
                    nested = unitsToRead != 0;
                    pos = unitArray.getDistanceFast(fastp);
                }
            }
        }
    }

    public void dispose() {
        this.unitArray = null;
        this.returnPos = null;
    }

    public int getPos() {
        return pos;
    }

    public LinkedListPosition getReturnPos() {
        return returnPos;
    }

    protected  void setReturnPos(LinkedListPosition pos) {
        returnPos = pos;
    }

    public int getUnitsToRead() {
        return unitsToRead;
    }

    public boolean getNested() {
        return nested;
    }

    protected void setAbsProcessed(boolean newValue) {
        absProcessed = newValue;
    }

    public LinkedListTreeUnitArray getUnitArray() {
        return unitArray;
    }

    @Override
    public int hashCode() {
        return pos
                ^ (returnPos == null ? 0 : returnPos.hashCode())
                ^ unitsToRead
                ^ (nested ? 0 : 0xa3d7)
                ^ (absProcessed ? 0 : 0x34b8);
    }

    @Override
    public boolean equals(Object anObject) {
        if (anObject == this) {
            return true;
        }
        if (anObject instanceof LinkedListPosition) {
            return equalsLinkedListPosition((LinkedListPosition) anObject);
        }
        return false;
    }

    private boolean equalsLinkedListPosition(LinkedListPosition pos2) {
        if ((pos == pos2.pos && nested == pos2.nested)
                && (unitsToRead == pos2.unitsToRead && absProcessed == pos2.absProcessed)) {
            if (returnPos == null || pos2.returnPos == null) {
                return returnPos == null && pos2.returnPos == null;
            } else {
                return returnPos.equalsLinkedListPosition(pos2.returnPos);
            }

        }
        return false;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{pos: ").append(pos);
        result.append(",absProcessed: ").append(absProcessed);
        result.append(",nested: ").append(nested);
        result.append(",unitsToRead: ").append(unitsToRead);
        result.append(",returnPos: ").append(returnPos == null ? "null" : returnPos.toString());
        result.append("}");
        return result.toString();
    }
    
}
