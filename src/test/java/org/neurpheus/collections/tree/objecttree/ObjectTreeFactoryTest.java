/*
 *  © 2015 Jakub Strychowski
 */

package org.neurpheus.collections.tree.objecttree;

import java.lang.reflect.Constructor;
import org.junit.Test;
import static org.junit.Assert.*;
import org.neurpheus.collections.tree.Tree;
import org.neurpheus.collections.tree.TreeNode;
import org.neurpheus.collections.tree.TreeNodeWithData;

/**
 *
 * @author Kuba
 */
public class ObjectTreeFactoryTest {
    
    public ObjectTreeFactoryTest() {
    }

    /**
     * Test of getInstance method, of class ObjectTreeFactory.
     */
    @Test
    public void testGetInstance() {
        ObjectTreeFactory result1 = ObjectTreeFactory.getInstance();
        ObjectTreeFactory result2 = ObjectTreeFactory.getInstance();
        assertNotNull(result1);
        assertTrue(result1 == result2);
    }

    /**
     * Test of createTree method, of class ObjectTreeFactory.
     */
    @Test
    public void testCreateTree() {
        ObjectTreeFactory factory = ObjectTreeFactory.getInstance();
        Tree tree1 = factory.createTree();
        Tree tree2 = factory.createTree();
        assertNotNull(tree1);
        assertNotNull(tree2);
        assertTrue(tree1 != tree2);
        assertTrue(tree1.getRoot() != tree2.getRoot());
    }

    /**
     * Test of createTreeNode method, of class ObjectTreeFactory.
     */
    @Test
    public void testCreateTreeNode() {
        ObjectTreeFactory<String, Integer> factory = ObjectTreeFactory.getInstance();
        TreeNode<String> node1 = factory.createTreeNode("n1");
        TreeNode<String> node2 = factory.createTreeNode("n2");
        assertNotNull(node1);
        assertNotNull(node2);
        assertTrue(node1 != node2);
        assertNotEquals(node1.getValue(), node2.getValue());
    }

    /**
     * Test of createTreeNode method, of class ObjectTreeFactory.
     */
    @Test
    public void testCreateTreeNode2() {
        ObjectTreeFactory factory = ObjectTreeFactory.getInstance();
        TreeNode node1 = factory.createTreeNode("n1");
        TreeNode node2 = factory.createTreeNode("n2");
        assertNotNull(node1);
        assertNotNull(node2);
        assertTrue(node1 != node2);
        assertNotEquals(node1.getValue(), node2.getValue());
    }
    
    /**
     * Test of createTreeNodeWithAdditionalData method, of class ObjectTreeFactory.
     */
    @Test
    public void testCreateTreeNodeWithAdditionalData() {
        ObjectTreeFactory<String, Integer> factory = ObjectTreeFactory.getInstance();
        TreeNodeWithData<String, Integer> node1 = factory.createTreeNodeWithAdditionalData("n1", 1);
        TreeNodeWithData<String, Integer> node2 = factory.createTreeNodeWithAdditionalData("n2", 2);
        assertNotNull(node1);
        assertNotNull(node2);
        assertTrue(node1 != node2);
        assertNotEquals(node1.getValue(), node2.getValue());
        assertNotEquals(node1.getData(), node2.getData());
    }

    /**
     * Test of setDataForNode method, of class ObjectTreeFactory.
     */
    @Test
    public void testSetDataForNode() {
        ObjectTreeFactory<String, Integer> factory = ObjectTreeFactory.getInstance();
        TreeNode<String> node1 = factory.createTreeNode("n1");
        TreeNode<String> node2 = factory.createTreeNode("n2");
        TreeNode<String> node2a = factory.createTreeNode("n2a");
        TreeNode<String> node2b = factory.createTreeNode("n2b");
        TreeNode<String> node2c = factory.createTreeNode("n2c");
        TreeNode<String> node3 = factory.createTreeNode("n3");

        node2.addChild(node2a);
        node2.addChild(node2b);
        node2.addChild(node2c);
        
        factory.setDataForNode(node2b, node2, 4);
        
        node2b = node2.getChild("n2b");
        assertNotNull(node2b);
        assertTrue(node2b.hasExtraData());
        assertEquals(new Integer(4), ((TreeNodeWithData<String, Integer>) node2b).getData());
        
        factory.setDataForNode(node2b, node2, 5);
        
        assertTrue(node2b.hasExtraData());
        assertEquals(new Integer(5), ((TreeNodeWithData<String, Integer>) node2b).getData());
        
    }

    /**
     * Test of sortTree method, of class ObjectTreeFactory.
     */
    @Test
    public void testSortTree() {
        ObjectTreeFactory<String, Integer> factory = ObjectTreeFactory.getInstance();
        Tree tree = factory.createTree();
        TreeNode root = tree.getRoot();
        TreeNode nodeA = factory.createTreeNode("a");
        TreeNode nodeB = factory.createTreeNode("b");
        TreeNode nodeBA = factory.createTreeNode("ba");
        TreeNode nodeBB = factory.createTreeNode("bb");
        TreeNode nodeBC = factory.createTreeNode("bc");
        TreeNode nodeBCA = factory.createTreeNode("bca");
        TreeNode nodeBCB = factory.createTreeNode("bcb");
        TreeNode nodeBCC = factory.createTreeNode("bcc");
        TreeNode nodeBCD = factory.createTreeNode("bcd");
        TreeNode nodeC = factory.createTreeNode("c");
        TreeNode nodeD = factory.createTreeNode("d");
        
        root.addChild(nodeD);
        root.addChild(nodeB);
        root.addChild(nodeC);
        root.addChild(nodeA);
        
        nodeB.addChild(nodeBB);
        nodeB.addChild(nodeBA);
        nodeB.addChild(nodeBC);
        
        nodeBC.addChild(nodeBCD);
        nodeBC.addChild(nodeBCA);
        nodeBC.addChild(nodeBCB);
        nodeBC.addChild(nodeBCC);

        factory.sortTree(tree);
        
        assertEquals(nodeA, root.getChildAtPos(0));
        assertEquals(nodeB, root.getChildAtPos(1));
        assertEquals(nodeC, root.getChildAtPos(2));
        assertEquals(nodeD, root.getChildAtPos(3));
        
        assertEquals(nodeBA, nodeB.getChildAtPos(0));
        assertEquals(nodeBB, nodeB.getChildAtPos(1));
        assertEquals(nodeBC, nodeB.getChildAtPos(2));

        assertEquals(nodeBCA, nodeBC.getChildAtPos(0));
        assertEquals(nodeBCB, nodeBC.getChildAtPos(1));
        assertEquals(nodeBCC, nodeBC.getChildAtPos(2));
        assertEquals(nodeBCD, nodeBC.getChildAtPos(3));
    }
    
 
    @Test
    public void testPrivateConstructor() {
        try {
            Constructor<ObjectTreeFactory> con = ObjectTreeFactory.class.getDeclaredConstructor();
            con.setAccessible(true);
            con.newInstance();
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }
}
