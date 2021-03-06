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

package org.neurpheus.collections.tree.objecttree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.neurpheus.collections.tree.Tree;
import org.neurpheus.collections.tree.TreeNode;

/**
 * Tests a simple implementation of a {link Tree} interface.
 *
 * @author Jakub Strychowski
 */
public class ObjectTreeTest {
    
    public ObjectTreeTest() {
    }

    /**
     * Test of getRoot method, of class ObjectTree.
     */
    @Test
    public void testGetRoot() {
        ObjectTree instance = new ObjectTree();
        TreeNode root = instance.getRoot();
        TreeNode root2 = instance.getRoot();
        assertTrue(root == root2);
    }

    /**
     * Test of setRoot method, of class ObjectTree.
     */
    @Test
    public void testSetRoot() {
        TreeNode<String> root = new ObjectTreeNode<>("root");
        ObjectTree tree = new ObjectTree();
        TreeNode oldRoot = tree.getRoot();
        assertTrue(root != oldRoot);
        tree.setRoot(root);
        TreeNode rootTest = tree.getRoot();
        assertTrue(root == rootTest);
    }

    /**
     * Test of setRoot method, of class ObjectTree.
     */
    @Test (expected = NullPointerException.class)
    public void testSetRoot2() {
        Tree tree = ObjectTreeFactory.getInstance().createTree();
        tree.setRoot(null);
    }
    
    /**
     * Test of clear method, of class ObjectTreeNode.
     */
    @Test
    public void testClear() {
        Tree tree = new ObjectTree();
        TreeNode root = tree.getRoot();
        ObjectTreeNode n1 = new ObjectTreeNode();
        ObjectTreeNode n2 = new ObjectTreeNode();
        ObjectTreeNode n3 = new ObjectTreeNode();
        ObjectTreeNode n4 = new ObjectTreeNode();
        root.addChild(n1);
        root.addChild(n2);
        root.addChild(n3);
        root.addChild(n4);
        tree.clear();
        root = tree.getRoot();
        assertTrue(root.isLeaf());
        assertEquals(0, root.getNumberOfChildren());
        assertEquals(0, root.getChildren().size());
    }

    /**
     * Test of getFactory method, of class ObjectTree.
     */
    @Test
    public void testGetFactory() {
        Tree tree = new ObjectTree();
        assertTrue(tree.getFactory() == ObjectTreeFactory.getInstance());
    }
    
}
