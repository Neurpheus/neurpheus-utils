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

/**
 * Represents a single element of a linked list (compressed form of a tree).
 * This class helps in traversing a tree stored in a packed form.
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
     * How many elements can be read from the list before algorithm 
     * should return to previous fragment of the list.
     */
    private int unitsToRead;
    
    /**
     * <code>true</code> if the current position is nested part of other fragment in the list.
     */
    private boolean nested;
    
    
    /**
     * Flaga informuj¹ca czy sprawdzono czy dana pozycja jest
     * wskaŸnikiem i czy przetworzono ten wskaŸnik zmieniaj¹c
     * pozycjê na pozycjê odpowiadaj¹c¹ wskazywanemu
     * podci¹gowi.
     */
    private boolean absProcessed;
    
    /**
     * Local reference to the internal representation of a linked list.
     */
    private LinkedListTreeUnitArray unitArray;

    public LinkedListPosition(LinkedListTreeUnitArray unitArray, int pos,
                              LinkedListPosition returnPos, int unitsToRead, boolean nested) {
        this.unitArray = unitArray;
        this.pos = pos;
        this.returnPos = returnPos;
        this.unitsToRead = unitsToRead;
        this.nested = nested;
        this.absProcessed = false;
    }

    public LinkedListPosition(LinkedListTreeUnitArray unitArray, int pos,
                              LinkedListPosition returnPos, int unitsToRead, boolean nested,
                              boolean absProcessed) {
        this.unitArray = unitArray;
        this.pos = pos;
        this.returnPos = returnPos;
        this.unitsToRead = unitsToRead;
        this.nested = nested;
        this.absProcessed = absProcessed;
    }

    public LinkedListPosition(LinkedListPosition basePos) {
        this.unitArray = basePos.unitArray;
        this.pos = basePos.pos;
        this.returnPos = basePos.returnPos;
        this.unitsToRead = basePos.unitsToRead;
        this.nested = basePos.nested;
        this.absProcessed = basePos.absProcessed;
    }

    public int getDistance() {
        if (!absProcessed) {
            processAbsolutePointer();
        }
        return unitArray.getDistance(pos);
    }

    public LinkedListPosition nextLevel() {
        if (!absProcessed) {
            processAbsolutePointer();
        }
        if (unitArray.isWordContinued(pos)) {
            if (nested && unitsToRead <= 1) {
                return returnPos;
            } else {
                return new LinkedListPosition(unitArray, pos + 1, returnPos, unitsToRead - 1, nested);
            }
        } else {
            return null;
        }
    }

    public LinkedListPosition goToNextLevel() {
        if (!absProcessed) {
            processAbsolutePointer();
        }
        if (unitArray.isWordContinued(pos)) {
            if (nested && unitsToRead <= 1) {
                return returnPos == null ? null : new LinkedListPosition(returnPos);
            } else {
                return new LinkedListPosition(unitArray, pos + 1, returnPos, unitsToRead - 1, nested);
            }
        }
        return null;
    }

    public LinkedListPosition nextChild() {
        if (!absProcessed) {
            processAbsolutePointer();
        }
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

    public LinkedListPosition goToNextChild() {
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

    public boolean isWordContinued() {
        if (!absProcessed) {
            processAbsolutePointer();
        }
        return unitArray.isWordContinued(pos);
    }

    public boolean isWordEnd() {
        if (!absProcessed) {
            processAbsolutePointer();
        }
        return unitArray.isWordEnd(pos);
    }

    public int getValueMapped() {
        if (!absProcessed) {
            processAbsolutePointer();
        }
        return unitArray.getValueCode(pos);
    }

    public int getValue() {
        if (!absProcessed) {
            processAbsolutePointer();
        }
        return unitArray.getValueMapping()[unitArray.getValueCode(pos)];
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

    public LinkedListPosition getCopy() {
        return new LinkedListPosition(this);
    }

    public int getPos() {
        return pos;
    }

    public LinkedListPosition getReturnPos() {
        return returnPos;
    }

    public void setReturnPos(LinkedListPosition pos) {
        returnPos = pos;
    }

    public int getUnitsToRead() {
        return unitsToRead;
    }

    public boolean getNested() {
        return nested;
    }

    public void setAbsProcessed(boolean v) {
        absProcessed = v;
    }

    public LinkedListTreeUnitArray getUnitArray() {
        return unitArray;
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj == null) || !(obj instanceof LinkedListPosition)) {
            return false;
        }
        return equalsLinkedListPosition((LinkedListPosition) obj);
    }

    @Override
    public int hashCode() {
        return pos ^ (returnPos == null ? 0 : returnPos.hashCode()) ^ unitsToRead ^ (nested ? 0 : 0xa3d7) ^ (absProcessed ? 0 : 0x34b8);
    }

    public boolean equalsLinkedListPosition(LinkedListPosition b) {
        if ((pos == b.pos && nested == b.nested)
                && (unitsToRead == b.unitsToRead && absProcessed == b.absProcessed)) {
            if (returnPos == null || b.returnPos == null) {
                return returnPos == null && b.returnPos == null;
            } else {
                return returnPos.equalsLinkedListPosition(b.returnPos);
            }

        }
        return false;
    }

}
