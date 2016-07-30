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

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.neurpheus.collections.tree.Tree;
import org.neurpheus.collections.tree.TreeNode;
import org.neurpheus.logging.LoggerService;

/**
 *
 * @author Jakub Strychowski
 */
public class LinkedListTreeNodeTest {

    private static final List<String> examples = Arrays.asList(
            new String[]{
                "wysoki",
                "wysokiego",
                "wysokiemu",
                "najwyzszemu",
                "wysoka",
                "wysocy",
                "wysockich",
                "wysocki",
                "wysokim",
                "wysokimi",
                "niewysoki",
                "niewysokiego",
                "niewysokiemu",
                "nienajwyzszemu",
                "niewysoka",
                "niewysocy",
                "niewysocki",
                "niewysokim",
                "niewysokimi"
            });

    //77 w 119
    //79 y 121
    //73 s 115
    //6F o 111 
    //6B k 107
    //69 i 105
    public LinkedListTreeNodeTest() {
    }

    static LinkedListTree tree = null;
    static LinkedListTreeNode root = null;
    static LinkedListTreeUnitArray units = null;
    static int[] valueMapping = null;
    static int[] stack;
    static boolean REVERSE = false;

    public static LinkedListTree createTestTree(boolean compress) {
        Tree baseTree = LinkedListTreeTools.createBaseTree(examples, REVERSE, true);
        tree = (LinkedListTree) LinkedListTreeFactory.getInstance().createTree(
                baseTree, true, compress, false);
        return tree;
    }

    @BeforeClass
    public static void setUpClass() {
        LoggerService.setLogLevelForConsole(Level.FINER);
        createTestTree(true);
        root = tree.getRoot();
        units = tree.getUnitArray();
        valueMapping = units.getValueMapping();
        stack = new int[1000];
    }

    @Test
    public void testTraversal1() {
        for (String example : examples) {
            TreeNode node = LinkedListTreeTools.findNode(example, tree, REVERSE, null);
            assertTrue("Cannot find string: " + example, node != null);
        }
    }

    @AfterClass
    public static void tearDownClass() {
        tree = null;
        root = null;
        units = null;
        valueMapping = null;
        stack = null;
    }

    @Test
    public void testGetUnit() {
        LinkedListTreeUnitArray units = tree.getUnitArray();
        for (LinkedListTreeNode child : root.getChildren()) {
            LinkedListTreeUnit unit = child.getUnit();
            assertEquals(unit, units.get(child.getPosition().getPos()));
            int value1 = (Integer) child.getValue();
            int value2 = valueMapping[unit.getValueCode()];
            assertEquals(value1, value2);
        }
    }

    @Test
    public void testGetValue() {
        LinkedListTreeNode nodeW = root.getChild((int) 'w');
        assertNotNull(nodeW);
        assertEquals((int) 'w', nodeW.getValue().intValue());

        LinkedListTreeNode nodeN = root.getChild((int) 'n');
        assertNotNull(nodeN);
        assertEquals((int) 'n', nodeN.getValue().intValue());

        LinkedListTreeNode nodeNa = nodeN.getChild((int) 'a');
        assertNotNull(nodeNa);
        assertEquals((int) 'a', nodeNa.getValue().intValue());

        LinkedListTreeNode nodeNi = nodeN.getChild((int) 'i');
        assertNotNull(nodeNi);
        assertEquals((int) 'i', nodeNi.getValue().intValue());

    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetValue() {
        LinkedListTreeNode nodeW = root.getChild((int) 'w');
        assertNotNull(nodeW);
        assertEquals((int) 'w', nodeW.getValue().intValue());
        nodeW.setValue((int) 'x');
    }

    private LinkedListTreeNode getLastNode(String examples) {
        LinkedListTreeNode node = root;
        for (char c : examples.toCharArray()) {
            node = node.getChild((int) c);
        }
        return node;
    }

    private Integer getLastNodeData(String example) {
        return root.getData(example, stack, 0);
    }

    @Test
    public void testIsLeaf() {
        assertFalse(root.isLeaf());
        assertFalse(root.getChild((int) 'w').isLeaf());
        assertFalse(root.getChild((int) 'n').isLeaf());
        LinkedListTreeNode node1 = getLastNode("wysocy");
        assertTrue(node1.isLeaf());
        LinkedListTreeNode node2 = getLastNode("niewysoka");
        assertTrue(node2.isLeaf());
        LinkedListTreeNode node3 = getLastNode("wysoki");
        assertTrue(node3.isLeaf());
        LinkedListTreeNode node4 = getLastNode("wysockich");
        assertTrue(node4.isLeaf());

        LinkedListTreeNode nodeWrong1 = getLastNode("wyso");
        assertFalse(nodeWrong1.isLeaf());
    }

    @Test
    public void testHasExtraData() {
        assertFalse(root.isLeaf());
        assertFalse(root.getChild((int) 'w').hasExtraData());
        assertFalse(root.getChild((int) 'n').hasExtraData());
        LinkedListTreeNode node1 = getLastNode("wysocy");
        assertTrue(node1.hasExtraData());
        LinkedListTreeNode node2 = getLastNode("niewysoka");
        assertTrue(node2.hasExtraData());
        LinkedListTreeNode node3 = getLastNode("wysoki");
        assertTrue(node3.hasExtraData());
        LinkedListTreeNode node4 = getLastNode("wysockich");
        assertTrue(node4.hasExtraData());

        LinkedListTreeNode nodeWrong1 = getLastNode("wyso");
        assertFalse(nodeWrong1.hasExtraData());
    }

    @Test
    public void testGetData1() {
        int index = 0;
        for (String example : examples) {
            LinkedListTreeNode node = getLastNode(example);
            assertTrue("No data for example " + example, node.hasExtraData());
            assertEquals(index, ((LinkedListTreeDataNode) node).getData().intValue());
            index++;
        }
    }

    @Test
    public void testGetData2() {
        int index = 0;
        for (String example : examples) {
            int data = getLastNodeData(example);
            assertEquals(index, data);
            index++;
        }
        assertNull(getLastNodeData("wys"));
        assertNull(getLastNodeData("wysokiemuw"));
    }

    @Test
    public void testGetNumberOfChildren() {
        LinkedListTreeNode node = root;
        assertEquals(2, node.getNumberOfChildren());

        node = node.getChild((int) 'w');
        assertEquals(1, node.getNumberOfChildren());

        node = node.getChild((int) 'y');
        assertEquals(1, node.getNumberOfChildren());

        node = node.getChild((int) 's');
        assertEquals(1, node.getNumberOfChildren());

        node = node.getChild((int) 'o');
        assertEquals(2, node.getNumberOfChildren());

        node = node.getChild((int) 'c');
        assertEquals(2, node.getNumberOfChildren());

        node = node.getChild((int) 'k');
        assertEquals(1, node.getNumberOfChildren());

        node = node.getChild((int) 'i');
        assertEquals(1, node.getNumberOfChildren());

        node = node.getChild((int) 'c');
        assertEquals(1, node.getNumberOfChildren());

        node = node.getChild((int) 'h');
        assertEquals(0, node.getNumberOfChildren());

        node = node.getChild((int) 'v');
        assertNull(node);

    }

    @Test
    public void testGetChildAtPos() {
        ArrayDeque<TreeNode> nodes = new ArrayDeque<>();
        nodes.push(root);
        while (!nodes.isEmpty()) {
            TreeNode node = nodes.pop();
            int nofc = node.getNumberOfChildren();
            for (int i = 0; i < nofc; i++) {
                TreeNode child = node.getChildAtPos(i);
                nodes.push(child);
                assertEquals(child, node.getChild(child.getValue()));
            }
        }
    }

    @Test
    public void testClear() {
        Tree baseTree = LinkedListTreeTools.createBaseTree(examples, REVERSE, true);
        LinkedListTree tmpTree = (LinkedListTree) LinkedListTreeFactory.getInstance().createTree(
                baseTree, true, true, false);
        LinkedListTreeNode tmpRoot = (LinkedListTreeNode) tmpTree.getRoot();
        tmpRoot.clear();
        assertNull(tmpRoot.getPosition());
    }

    @Test
    public void testGetChildren() {
        LinkedListTreeNode node = root;
        assertEquals(2, node.getChildren().size());

        node = node.getChild((int) 'w');
        assertEquals(1, node.getChildren().size());

        node = node.getChild((int) 'y');
        assertEquals(1, node.getChildren().size());

        node = node.getChild((int) 's');
        assertEquals(1, node.getChildren().size());

        node = node.getChild((int) 'o');
        assertEquals(2, node.getChildren().size());

        node = node.getChild((int) 'c');
        assertEquals(2, node.getChildren().size());

        node = node.getChild((int) 'k');
        assertEquals(1, node.getChildren().size());

        node = node.getChild((int) 'i');
        assertEquals(1, node.getChildren().size());

        node = node.getChild((int) 'c');
        assertEquals(1, node.getChildren().size());

        node = node.getChild((int) 'h');
        assertEquals(0, node.getChildren().size());
    }

    @Test
    public void testGetChild_Object_TreeNode() {
        ArrayDeque<LinkedListTreeNode> nodes = new ArrayDeque<>();
        nodes.push(root);
        while (!nodes.isEmpty()) {
            LinkedListTreeNode node = nodes.pop();
            List<LinkedListTreeNode> children = node.getChildren();
            LinkedListTreeNode lastChild = null;
            for (LinkedListTreeNode child : children) {
                nodes.push(child);
                if (lastChild != null) {
                    LinkedListTreeNode childBis = node.getChild(child.getValue(), lastChild);
                    assertEquals(child, childBis);
                    childBis = node.getChild(child.getValue(), null);
                    assertEquals(child, childBis);
                    childBis = node.getChild((int) 'v', lastChild);
                    assertNull(childBis);
                }
                lastChild = child;
            }
        }
    }

    @Test
    public void testGetChild_3args() {
        int[] stack = new int[1000];
        ArrayDeque<LinkedListTreeNode> nodes = new ArrayDeque<>();
        nodes.push(root);
        while (!nodes.isEmpty()) {
            LinkedListTreeNode node = nodes.pop();
            List<LinkedListTreeNode> children = node.getChildren();
            for (LinkedListTreeNode child : children) {
                nodes.push(child);
                LinkedListTreeNode childBis = node.getChild(child.getValue(), stack, 0);
                assertEquals(child, childBis);
            }
        }
    }

    @Test
    public void testEquals() {
        assertEquals(root, root);
        assertEquals(root.getChild((int) 'w'), root.getChild((int) 'w'));
        assertNotEquals(root, root.getChild((int) 'w'));
        assertNotEquals(root, "www");
        assertNotEquals(root, null);
    }

    @Test
    public void testHashCode() {
        assertEquals(root.hashCode(), root.hashCode());
        assertEquals(root.getChild((int) 'w').hashCode(), root.getChild((int) 'w').hashCode());
        assertNotEquals(root.hashCode(), root.getChild((int) 'w').hashCode());
        
        assertTrue(root.getPosition().equals(root.getPosition()));
        assertTrue(root.getChild((int) 'w').getPosition()
                .equals(root.getChild((int) 'w').getPosition()));
        assertFalse(root.getPosition().equals(root.getChild((int) 'w').getPosition()));
        
        assertFalse(root.getPosition().equals(null));
        assertFalse(root.getPosition().equals("w"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetChildren() {
        root.setChildren(Collections.emptyList());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddChild_TreeNode() {
        root.addChild(root);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddChild_int_TreeNode() {
        root.addChild(0, root);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveChild_TreeNode() {
        root.removeChild(root);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveChild_int() {
        root.removeChild(0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testReplaceChild() {
        root.replaceChild(root, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetData() {
        LinkedListTreeNode node = root;
        String example = "wysoka";
        for (int i = 0; i < example.length(); i++) {
            node = node.getChild((int) example.charAt(i));
        }
        assertTrue(node.isLeaf());
        assertTrue(node.hasExtraData());
        
        
        LinkedListTreeDataNode dataNode = (LinkedListTreeDataNode) node;
        assertTrue(dataNode.getData() > 0);
        
        dataNode.setData(3);
    }
    
}
