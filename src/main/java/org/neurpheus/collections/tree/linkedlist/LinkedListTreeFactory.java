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
import org.neurpheus.collections.tree.TreeFactory;
import org.neurpheus.collections.tree.TreeNode;
import org.neurpheus.collections.tree.TreeNodeWithData;
import org.neurpheus.logging.LoggerService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class factory responsible for creating linked list tree.
 *
 * @author Jakub Strychowski
 */
public class LinkedListTreeFactory implements TreeFactory {

    private static final Logger LOGGER = LoggerService.getLogger(LinkedListTreeFactory.class);

    private static final LinkedListTreeFactory INSTANCE = new LinkedListTreeFactory();

    /** Creates a new instance of LinkedListTreeFactory. */
    private LinkedListTreeFactory() {
    }

    /**
     * Returns a singleton instance of this factory.
     *
     * @return The instance of the factory.
     */
    public static LinkedListTreeFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public LinkedListTreeNode createTreeNode(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LinkedListTreeDataNode createTreeNodeWithAdditionalData(Object value, Object data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LinkedListTree createTree() {
        return new LinkedListTree();
    }

    /**
     * Creates a linked list tree from the specified base tree.
     *
     * <p>
     * Linked list trees can be created only as a copy of already created tree structures.
     * Therefore, at the begin you should create a tree using any other tree implementation (for
     * example: {@link ObjectTree}) and then create a compact, linked list tree using this method.
     * </p>
     * <p>
     * Note that all values and data in the base tree should be integer values (char codes, or
     * identifiers of objects).
     * </p>
     *
     * @param baseTree      The base tree which will be represented as a linked list tree.
     * @param clearBaseTree Dispose all objects from the base tree while creating copy.
     * @param compress      Use LZTrie compression algorithm to reduce memory consumed by the final
     *                      tree.
     * @param parallelMode  Use experimental parallel compression algorithm to speed up compression.
     *
     * @return The created linked list tree.
     */
    public LinkedListTree createTree(Tree baseTree, boolean clearBaseTree, boolean compress,
                                     boolean parallelMode) {
        long startTime = System.currentTimeMillis();
        LinkedListTree llt = createLinkedListTree(baseTree, clearBaseTree);

        if (clearBaseTree) {
            baseTree.clear();
        }

        long memoryBefore = llt.getUnitArray().getAllocationSize();

        if (compress) {
            llt = LZTrieCompression.compress(llt, parallelMode);
        }

        compact(llt);

        long memoryAfter = llt.getUnitArray().getAllocationSize();

        if (LOGGER.isLoggable(Level.FINE)) {
            long treeCreationTime = System.currentTimeMillis() - startTime;
            LOGGER.fine(String.format("Memory usage: %d kB (%5.2f%% of uncompressed size = %d kB)",
                                      memoryAfter / 1024,
                                      100.0 * memoryAfter / memoryBefore,
                                      memoryBefore / 1024));
            LOGGER.log(Level.FINE, "Total time: {0} ms.", treeCreationTime);
        }

        return llt;
    }

    private void compact(LinkedListTree llt) {
        CompactLinkedListTreeUnitArray compactArray
                = new CompactLinkedListTreeUnitArray(llt.getUnitArray());
        llt.setUnitArray(compactArray);
        if (LOGGER.isLoggable(Level.FINE)) {
            llt.getUnitArray().logStatistics("compact form");
        }
    }

    /**
     * Creates uncompressed linked list from the specified base tree.
     *
     * @param baseTree      The base tree which will be represented as a linked list tree.
     * @param clearBaseTree Dispose all objects from the base tree while creating copy.
     *
     * @return The created linked list tree.
     */
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

    /**
     * Recurrent method which traverse nodes of the base tree and creates linked list tree.
     *
     * @param node          Current processed node from the base tree.
     * @param unitArray     target linked list representation.
     * @param clearBaseTree Dispose all objects from the base tree while creating copy.
     * @param valueMapping  maps integer values from the base tree to internal values to reduce
     *                      number of bits required for an integer values storage.
     */
    private void createTreeFromNode(TreeNode node, LinkedListTreeUnitArray unitArray,
                                    boolean clearBaseTree, Map<Integer, Integer> valueMapping) {
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
            Integer val = (Integer) child.getValue();
            Integer valMapped = valueMapping.get(val);
            if (valMapped == null) {
                valMapped = valueMapping.size();
                valueMapping.put(val, valMapped);
            }
            unit.setValueCode(valMapped);
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

    /**
     * Sorts nodes by values describing nodes.
     */
    private class ValueCodeComparator implements Comparator {

        Map<Integer, Integer> valueMapping;

        public ValueCodeComparator(Map<Integer, Integer> valueMapping) {
            this.valueMapping = valueMapping;
        }

        @Override
        public int compare(Object o1, Object o2) {
            Integer v1 = (Integer) ((TreeNode) o1).getValue();
            Integer v2 = (Integer) ((TreeNode) o2).getValue();
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
