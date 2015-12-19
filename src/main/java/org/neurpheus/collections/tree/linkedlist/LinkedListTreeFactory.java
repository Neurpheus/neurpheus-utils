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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neurpheus.collections.tree.Tree;
import org.neurpheus.collections.tree.TreeFactory;
import org.neurpheus.collections.tree.TreeNodeWithData;
import org.neurpheus.collections.tree.TreeNode;
import org.neurpheus.logging.LoggerService;

/**
 *
 * @author szkoleniowy
 */
public class LinkedListTreeFactory implements TreeFactory {

    private static final Logger LOGGER = LoggerService.getLogger(LinkedListTreeFactory.class);

    private static final LinkedListTreeFactory instance = new LinkedListTreeFactory();
    
    /** Creates a new instance of LinkedListTreeFactory */
    private LinkedListTreeFactory() {
    }


    public static LinkedListTreeFactory getInstance() {
        return instance;
    }

    @Override
    public TreeNode createTreeNode(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TreeNodeWithData createTreeNodeWithAdditionalData(Object value, Object data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Tree createTree() {
        return new LinkedListTree();
    }

    public Tree createTree(Tree baseTree, boolean clearBaseTree, boolean compress, boolean parallelMode) {
        long startTime = System.currentTimeMillis();
        LinkedListTree llt = createLinkedListTree(baseTree, clearBaseTree);
        
        if (clearBaseTree) {
            baseTree.clear();
        }
        
        long memoryBefore = llt.getUnitArray().getAllocationSize();
        
        if (compress) {
            llt = LZTrieCompression.compress(llt, parallelMode);
            
//            llt.getUnitArray().logStatistics("compressed form");
//            if (LOGGER.isLoggable(Level.FINE)) {
//                LOGGER.fine("LZTrie compression ratio : " + ratio);
//            }
        }
        


//        if (compress) {
//            double ratio = LZTrieCompression.compress(llt);
//            llt.getUnitArray().logStatistics("compressed form");
//            if (LOGGER.isLoggable(Level.FINE)) {
//                LOGGER.fine("LZTrie compression ratio : " + ratio);
//            }
//        }

        

        compact(llt);
        
        
        long memoryAfter = llt.getUnitArray().getAllocationSize();
        
        
        if (LOGGER.isLoggable(Level.FINE)) {
            long treeCreationTime = System.currentTimeMillis() - startTime;
            LOGGER.fine(String.format("Memory usage: %d kB (%5.2f%% of uncompressed size = %d kB)", 
                        memoryAfter / 1024,
                        (100.0 * memoryAfter / memoryBefore),
                        memoryBefore / 1024));
            LOGGER.fine("Total time: " + treeCreationTime + " ms.");
        }
        
        return llt;
    }

    private void compact(LinkedListTree llt) {
        CompactLinkedListTreeUnitArray compactArray = new CompactLinkedListTreeUnitArray(llt.getUnitArray());
        llt.setUnitArray(compactArray);
        llt.getUnitArray().logStatistics("compact form");
    }
    
    private LinkedListTree createLinkedListTree(Tree baseTree, boolean clearBaseTree) {
        long startTime = System.currentTimeMillis();
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Creating linked list tree.");
        }
        LinkedListTreeUnitArray unitArray = new FastLinkedListTreeUnitArray(1);
        LinkedListTreeUnit unit = new LinkedListTreeUnit();
        unit.setDistance(0);
        unit.setWordContinued(true);
        unitArray.add(unit);
        TreeNode rootNode = baseTree.getRoot();
        Map<Integer, Integer> valueMapping = new HashMap<>();
        valueMapping.put(0, 0);
        createTreeFromNode(rootNode, unitArray, clearBaseTree, valueMapping);
        LinkedListTree llt = new LinkedListTree();
        llt.setUnitArray(unitArray);        
        int[] values = new int[valueMapping.size()];
        int index = 0;
        for (Map.Entry<Integer, Integer> entry : valueMapping.entrySet()) {
            values[entry.getValue()] = entry.getKey();
        }
        unitArray.setValueMapping(values);

        if (LOGGER.isLoggable(Level.FINE)) {
            long duration = System.currentTimeMillis() - startTime;
            LOGGER.fine(String.format("Linked list tree created in %d ms.", duration));
            llt.getUnitArray().logStatistics("not packed");
        }
        
        return llt;
    }
    
    private void createTreeFromNode(TreeNode node, LinkedListTreeUnitArray unitArray, boolean clearBaseTree, Map<Integer, Integer> valueMapping) {
        LinkedListTreeUnit lastUnit = null;
        int lastUnitPos = 0;
        ArrayList<TreeNode> children = new ArrayList<>(node.getChildren());
        Collections.sort(children, new ValueCodeComparator(valueMapping));
        for (TreeNode child : children) {
            if (lastUnit != null) {
                lastUnit.setDistance(unitArray.size() - lastUnitPos);
                unitArray.set(lastUnitPos, lastUnit);
            }
            LinkedListTreeUnit unit = new LinkedListTreeUnit();
            Integer val = ((Integer) child.getValue());
            Integer valMapped = valueMapping.get(val);
            if (valMapped == null) {
                valMapped = valueMapping.size();
                valueMapping.put(val, valMapped);
            }
            unit.setValueCode(valMapped);
//            Object value = child.getValue();
//            if (value instanceof Character) {
//                unit.setValueCode(((Character) value).charValue());
//            } else {
//                unit.setValueCode(Integer.parseInt(value.toString()));
//            }
            if (child.hasExtraData()) {
                Object dataObject = ((TreeNodeWithData) child).getData();
                if (dataObject == null) {
                    unit.setDataCode(0);
                } else if (dataObject instanceof Number) {
                    unit.setDataCode(((Number) dataObject).intValue());
                } else {
                    unit.setDataCode(Integer.parseInt(dataObject.toString()));
                }
                unit.setWordEnd(true);
            }
            unit.setDistance(0);
            boolean hasChildren = !child.getChildren().isEmpty();
            if (!hasChildren) {
                unit.setWordEnd(true);
            }
            lastUnit = unit;
            lastUnitPos = unitArray.size();
            if (hasChildren) {
                unit.setWordContinued(true);
                unitArray.add(unit);
                createTreeFromNode(child, unitArray, clearBaseTree, valueMapping);
            } else {
                unitArray.add(unit);
            }
        }
        if (clearBaseTree) {
            node.clear();
        }
    }
    
    
    public class ValueCodeComparator implements Comparator {

        
        Map<Integer, Integer> valueMapping;
        
        public ValueCodeComparator(Map<Integer, Integer> valueMapping) {
            this.valueMapping = valueMapping;
        }
        
        @Override
        public int compare(Object o1, Object o2) {
            Integer v1 = ((Integer) ((TreeNode) o1).getValue());
            Integer v2 = ((Integer) ((TreeNode) o2).getValue());
            Integer v1mapped = valueMapping.get(v1);
            if (v1mapped == null) {
                v1mapped = valueMapping.size();
                valueMapping.put(v1, v1mapped);
            }
            Integer v2mapped = valueMapping.get(v2);
            if (v2mapped == null) {
                v2mapped = valueMapping.size();
                valueMapping.put(v2, v2mapped);
            }
            return v1mapped.compareTo(v2mapped);
        }
        
        
        
    }

    
    
}
