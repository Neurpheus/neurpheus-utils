/*
 *  © 2015 Jakub Strychowski
 */

package org.neurpheus.collections.tree.objecttree;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import org.neurpheus.collections.tree.TreeNode;

/**
 *
 * @author Kuba
 */
public class ObjectTreeNodeTest {
    
    public ObjectTreeNodeTest() {
    }

    /**
     * Test of getValue method, of class ObjectTreeNode.
     */
    @Test
    public void testGetValue1() {
        ObjectTreeNode instance = new ObjectTreeNode();
        Object result = instance.getValue();
        assertNull(result);
    }

    /**
     * Test of getValue method, of class ObjectTreeNode.
     */
    @Test
    public void testGetValue2() {
        ObjectTreeNode<String> instance = new ObjectTreeNode<>("a");
        assertEquals("a", instance.getValue());
    }
    
    /**
     * Test of setValue method, of class ObjectTreeNode.
     */
    @Test
    public void testSetValue() {
        ObjectTreeNode instance = new ObjectTreeNode();
        instance.setValue("b");
        assertEquals("b", instance.getValue());
        instance.setValue("c");
        assertEquals("c", instance.getValue());
        instance.setValue(null);
        assertNull(instance.getValue());
    }

    /**
     * Test of isLeaf method, of class ObjectTreeNode.
     */
    @Test
    public void testIsLeaf() {
        ObjectTreeNode instance = new ObjectTreeNode();
        assertTrue(instance.isLeaf());
        instance.addChild(new ObjectTreeNode());
        assertFalse(instance.isLeaf());
        instance.addChild(new ObjectTreeNode());
        assertFalse(instance.isLeaf());
        instance.removeChild(1);
        assertFalse(instance.isLeaf());
        instance.removeChild(0);
        assertTrue(instance.isLeaf());
    }

    /**
     * Test of getChildren method, of class ObjectTreeNode.
     */
    @Test
    public void testGetChildren() {
        ObjectTreeNode instance = new ObjectTreeNode();
        ObjectTreeNode n1 = new ObjectTreeNode();
        ObjectTreeNode n2 = new ObjectTreeNode();
        ObjectTreeNode n3 = new ObjectTreeNode();
        instance.addChild(n1);
        instance.addChild(n2);
        instance.addChild(n3);
        List result = instance.getChildren();
        assertEquals(3, result.size());
        assertEquals(n1, result.get(0));
        assertEquals(n2, result.get(1));
        assertEquals(n3, result.get(2));
    }

    /**
     * Test of setChildren method, of class ObjectTreeNode.
     */
    @Test
    public void testSetChildren1() {
        ObjectTreeNode n1 = new ObjectTreeNode();
        ObjectTreeNode n2 = new ObjectTreeNode();
        ObjectTreeNode n3 = new ObjectTreeNode();
        ArrayList<ObjectTreeNode> list = new ArrayList<>(3);
        list.add(n1);
        list.add(n2);
        list.add(n3);
        ObjectTreeNode instance = new ObjectTreeNode();
        instance.setChildren(list);
        List result = instance.getChildren();
        assertEquals(3, result.size());
        assertEquals(n1, result.get(0));
        assertEquals(n2, result.get(1));
        assertEquals(n3, result.get(2));
    }
    
    /**
     * Test of setChildren method, of class ObjectTreeNode.
     */
    @Test
    public void testSetChildren2() {
        ObjectTreeNode n1 = new ObjectTreeNode();
        ArrayList<ObjectTreeNode> list = new ArrayList<>(3);
        list.add(n1);
        ObjectTreeNode instance = new ObjectTreeNode();
        instance.setChildren(list);
        List result = instance.getChildren();
        assertEquals(1, result.size());
        assertEquals(n1, result.get(0));
    }

    /**
     * Test of setChildren method, of class ObjectTreeNode.
     */
    @Test
    public void testSetChildren3() {
        ArrayList<ObjectTreeNode> list = new ArrayList<>(3);
        ObjectTreeNode instance = new ObjectTreeNode();
        instance.setChildren(list);
        List result = instance.getChildren();
        assertEquals(0, result.size());
    }

    /**
     * Test of setChildren method, of class ObjectTreeNode.
     */
    @Test
    public void testSetChildren4() {
        ObjectTreeNode instance = new ObjectTreeNode();
        instance.setChildren(null);
        List result = instance.getChildren();
        assertEquals(0, result.size());
    }
    

    /**
     * Test of getNumberOfChildren method, of class ObjectTreeNode.
     */
    @Test
    public void testGetNumberOfChildren() {
        ObjectTreeNode instance = new ObjectTreeNode();
        assertEquals(0, instance.getNumberOfChildren());
        instance.addChild(new ObjectTreeNode());
        assertEquals(1, instance.getNumberOfChildren());
        instance.addChild(new ObjectTreeNode());
        assertEquals(2, instance.getNumberOfChildren());
        instance.addChild(new ObjectTreeNode());
        assertEquals(3, instance.getNumberOfChildren());
        instance.removeChild(0);
        assertEquals(2, instance.getNumberOfChildren());
        instance.removeChild(0);
        assertEquals(1, instance.getNumberOfChildren());
        instance.removeChild(0);
        assertEquals(0, instance.getNumberOfChildren());
    }

    /**
     * Test of getChildAtPos method, of class ObjectTreeNode.
     */
    @Test
    public void testGetChildAtPos() {
        ObjectTreeNode instance = new ObjectTreeNode();
        ObjectTreeNode n1 = new ObjectTreeNode();
        ObjectTreeNode n2 = new ObjectTreeNode();
        ObjectTreeNode n3 = new ObjectTreeNode();
        instance.addChild(n1);
        assertEquals(n1, instance.getChildAtPos(0));
        instance.addChild(n2);
        assertEquals(n1, instance.getChildAtPos(0));
        assertEquals(n2, instance.getChildAtPos(1));
        instance.addChild(n3);
        assertEquals(n1, instance.getChildAtPos(0));
        assertEquals(n2, instance.getChildAtPos(1));
        assertEquals(n3, instance.getChildAtPos(2));
        instance.removeChild(0);
        assertEquals(n2, instance.getChildAtPos(0));
        assertEquals(n3, instance.getChildAtPos(1));
        instance.addChild(n1);
        assertEquals(n2, instance.getChildAtPos(0));
        assertEquals(n3, instance.getChildAtPos(1));
        assertEquals(n1, instance.getChildAtPos(2));
        instance.removeChild(1);
        assertEquals(n2, instance.getChildAtPos(0));
        assertEquals(n1, instance.getChildAtPos(1));
    }

    /**
     * Test of getChildAtPos method, of class ObjectTreeNode.
     */
    @Test
    public void testGetChildAtPos2() {
        ObjectTreeNode instance = new ObjectTreeNode();
        ObjectTreeNode n1 = new ObjectTreeNode();
        ObjectTreeNode n2 = new ObjectTreeNode();
        instance.addChild(n1);
        assertEquals(n1, instance.getChildAtPos(0));
        instance.addChild(n2);
        assertEquals(n1, instance.getChildAtPos(0));
        instance.removeChild(1);
        assertEquals(n1, instance.getChildAtPos(0));
        instance.removeChild(0);
        instance.addChild(n1);
        assertEquals(n1, instance.getChildAtPos(0));
    }
    
    /**
     * Test of getChildAtPos method, of class ObjectTreeNode.
     */
    @Test (expected = IndexOutOfBoundsException.class)
    public void testGetChildAtPos3() {
        ObjectTreeNode instance = new ObjectTreeNode();
        ObjectTreeNode n1 = new ObjectTreeNode();
        ObjectTreeNode n2 = new ObjectTreeNode();
        instance.addChild(n1);
        instance.addChild(n2);
        assertEquals(n1, instance.getChildAtPos(0));
        assertEquals(n2, instance.getChildAtPos(1));
        instance.getChildAtPos(2);
    }

    /**
     * Test of getChildAtPos method, of class ObjectTreeNode.
     */
    @Test (expected = IndexOutOfBoundsException.class)
    public void testGetChildAtPos4() {
        ObjectTreeNode instance = new ObjectTreeNode();
        instance.getChildAtPos(0);
    }
    
    
    /**
     * Test of getChildAtPos method, of class ObjectTreeNode.
     */
    @Test (expected = IndexOutOfBoundsException.class)
    public void testGetChildAtPos5() {
        ObjectTreeNode instance = new ObjectTreeNode();
        ObjectTreeNode n1 = new ObjectTreeNode();
        instance.addChild(n1);
        instance.getChildAtPos(1);
    }
    
    /**
     * Test of addChild method, of class ObjectTreeNode.
     */
    @Test
    public void testAddChild_TreeNode() {
        ObjectTreeNode instance = new ObjectTreeNode();
        
        ObjectTreeNode n1 = new ObjectTreeNode();
        instance.addChild(n1);
        List result = instance.getChildren();
        assertEquals(1, result.size());
        assertEquals(n1, result.get(0));
        
        ObjectTreeNode n2 = new ObjectTreeNode();
        instance.addChild(n2);
        result = instance.getChildren();
        assertEquals(2, result.size());
        assertEquals(n1, result.get(0));
        assertEquals(n2, result.get(1));

        ObjectTreeNode n3 = new ObjectTreeNode();
        instance.addChild(n3);
        result = instance.getChildren();
        assertEquals(3, result.size());
        assertEquals(n1, result.get(0));
        assertEquals(n2, result.get(1));
        assertEquals(n3, result.get(2));
        
    }

    /**
     * Test of addChild method, of class ObjectTreeNode.
     */
    @Test
    public void testAddChild_int_TreeNode() {
        ObjectTreeNode instance = new ObjectTreeNode();
        
        ObjectTreeNode n1 = new ObjectTreeNode();
        instance.addChild(n1);
        List result = instance.getChildren();
        assertEquals(1, result.size());
        assertEquals(n1, result.get(0));
        
        ObjectTreeNode n2 = new ObjectTreeNode();
        instance.addChild(0, n2);
        result = instance.getChildren();
        assertEquals(2, result.size());
        assertEquals(n2, result.get(0));
        assertEquals(n1, result.get(1));

        ObjectTreeNode n3 = new ObjectTreeNode();
        instance.addChild(1, n3);
        result = instance.getChildren();
        assertEquals(3, result.size());
        assertEquals(n2, result.get(0));
        assertEquals(n3, result.get(1));
        assertEquals(n1, result.get(2));
    }

    /**
     * Test of addChild method, of class ObjectTreeNode.
     */
    @Test (expected = IndexOutOfBoundsException.class)
    public void testAddChild_int_TreeNode2() {
        ObjectTreeNode instance = new ObjectTreeNode();
        ObjectTreeNode n1 = new ObjectTreeNode();
        ObjectTreeNode n2 = new ObjectTreeNode();
        instance.addChild(0, n2);
        ObjectTreeNode n3 = new ObjectTreeNode();
        instance.addChild(2, n3);
        instance.addChild(4, n3);
    }

    /**
     * Test of addChild method, of class ObjectTreeNode.
     */
    @Test (expected = IndexOutOfBoundsException.class)
    public void testAddChild_int_TreeNode3() {
        ObjectTreeNode instance = new ObjectTreeNode();
        ObjectTreeNode n2 = new ObjectTreeNode();
        instance.addChild(1, n2);
    }
    
    /**
     * Test of removeChild method, of class ObjectTreeNode.
     */
    @Test
    public void testRemoveChild_TreeNode() {
        ObjectTreeNode instance = new ObjectTreeNode();
        ObjectTreeNode n1 = new ObjectTreeNode("n1");
        ObjectTreeNode n2 = new ObjectTreeNode("n2");
        ObjectTreeNode n3 = new ObjectTreeNode("n3");
        instance.addChild(n1);
        instance.addChild(n2);
        instance.addChild(n3);
        assertTrue(instance.removeChild(n2));
        List result = instance.getChildren();
        assertEquals(2, result.size());
        assertEquals(n1, result.get(0));
        assertEquals(n3, result.get(1));
        assertFalse(instance.removeChild(n2));
        assertTrue(instance.removeChild(n1));
        assertFalse(instance.removeChild(n1));
        result = instance.getChildren();
        assertEquals(1, result.size());
        assertEquals(n3, result.get(0));
        instance.addChild(n2);
        result = instance.getChildren();
        assertEquals(2, result.size());
        assertTrue(instance.removeChild(n3));
        assertFalse(instance.removeChild(n3));
        result = instance.getChildren();
        assertEquals(1, result.size());
        assertEquals(n2, result.get(0));
        assertTrue(instance.removeChild(n2));
        assertFalse(instance.removeChild(n2));
        result = instance.getChildren();
        assertEquals(0, result.size());
    }

    /**
     * Test of removeChild method, of class ObjectTreeNode.
     */
    @Test
    public void testRemoveChild_int() {
        ObjectTreeNode instance = new ObjectTreeNode();
        ObjectTreeNode n1 = new ObjectTreeNode();
        ObjectTreeNode n2 = new ObjectTreeNode();
        ObjectTreeNode n3 = new ObjectTreeNode();
        instance.addChild(n1);
        instance.addChild(n2);
        instance.addChild(n3);
        assertEquals(n2, instance.removeChild(1));
        List result = instance.getChildren();
        assertEquals(2, result.size());
        assertEquals(n1, result.get(0));
        assertEquals(n3, result.get(1));
        assertEquals(n1, instance.removeChild(0));
        result = instance.getChildren();
        assertEquals(1, result.size());
        assertEquals(n3, result.get(0));
        instance.addChild(n2);
        result = instance.getChildren();
        assertEquals(2, result.size());
        assertEquals(n3, instance.removeChild(0));
        result = instance.getChildren();
        assertEquals(1, result.size());
        assertEquals(n2, result.get(0));
        assertEquals(n2, instance.removeChild(0));
        result = instance.getChildren();
        assertEquals(0, result.size());
    }

    /**
     * Test of removeChild method, of class ObjectTreeNode.
     */
    @Test (expected = IndexOutOfBoundsException.class)
    public void testRemoveChild_int2() {
        ObjectTreeNode instance = new ObjectTreeNode();
        instance.removeChild(0);
    }
    
    
    /**
     * Test of removeChild method, of class ObjectTreeNode.
     */
    @Test (expected = IndexOutOfBoundsException.class)
    public void testRemoveChild_int3() {
        ObjectTreeNode instance = new ObjectTreeNode();
        ObjectTreeNode n1 = new ObjectTreeNode();
        instance.addChild(n1);
        instance.removeChild(1);
    }
    
    /**
     * Test of removeChild method, of class ObjectTreeNode.
     */
    @Test (expected = IndexOutOfBoundsException.class)
    public void testRemoveChild_int4() {
        ObjectTreeNode instance = new ObjectTreeNode();
        ObjectTreeNode n1 = new ObjectTreeNode();
        ObjectTreeNode n2 = new ObjectTreeNode();
        ObjectTreeNode n3 = new ObjectTreeNode();
        instance.addChild(n1);
        instance.addChild(n2);
        instance.addChild(n3);
        instance.removeChild(3);
    }
    
    /**
     * Test of replaceChild method, of class ObjectTreeNode.
     */
    @Test
    public void testReplaceChild() {
        ObjectTreeNode instance = new ObjectTreeNode();
        ObjectTreeNode n1 = new ObjectTreeNode();
        ObjectTreeNode n2 = new ObjectTreeNode();
        ObjectTreeNode n3 = new ObjectTreeNode();
        ObjectTreeNode n4 = new ObjectTreeNode();
        instance.addChild(n1);
        instance.addChild(n2);
        instance.addChild(n3);
        
        assertEquals(1, instance.replaceChild(n2, n4));
        List result = instance.getChildren();
        assertEquals(3, result.size());
        assertEquals(n1, result.get(0));
        assertEquals(n4, result.get(1));
        assertEquals(n3, result.get(2));
        
        
        assertEquals(0, instance.replaceChild(n1, n2));
        result = instance.getChildren();
        assertEquals(3, result.size());
        assertEquals(n2, result.get(0));
        assertEquals(n4, result.get(1));
        assertEquals(n3, result.get(2));

        assertEquals(2, instance.replaceChild(n3, n1));
        result = instance.getChildren();
        assertEquals(3, result.size());
        assertEquals(n2, result.get(0));
        assertEquals(n4, result.get(1));
        assertEquals(n1, result.get(2));

        assertEquals(2, instance.replaceChild(n1, n3));
        result = instance.getChildren();
        assertEquals(3, result.size());
        assertEquals(n2, result.get(0));
        assertEquals(n4, result.get(1));
        assertEquals(n3, result.get(2));

        assertEquals(0, instance.replaceChild(n2, n1));
        result = instance.getChildren();
        assertEquals(3, result.size());
        assertEquals(n1, result.get(0));
        assertEquals(n4, result.get(1));
        assertEquals(n3, result.get(2));

        assertEquals(1, instance.replaceChild(n4, n2));
        result = instance.getChildren();
        assertEquals(3, result.size());
        assertEquals(n1, result.get(0));
        assertEquals(n2, result.get(1));
        assertEquals(n3, result.get(2));
        
    }
    
    /**
     * Test of replaceChild method, of class ObjectTreeNode.
     */
    @Test
    public void testReplaceChild2() {
        ObjectTreeNode instance = new ObjectTreeNode();
        ObjectTreeNode n1 = new ObjectTreeNode();
        ObjectTreeNode n2 = new ObjectTreeNode();
        instance.addChild(n1);
        
        assertEquals(0, instance.replaceChild(n1, n2));
        List result = instance.getChildren();
        assertEquals(1, result.size());
        assertEquals(n2, result.get(0));

        assertEquals(0, instance.replaceChild(n2, n1));
        result = instance.getChildren();
        assertEquals(1, result.size());
        assertEquals(n1, result.get(0));
        
    }

    /**
     * Test of replaceChild method, of class ObjectTreeNode.
     */
    @Test
    public void testReplaceChild3() {
        ObjectTreeNode instance = new ObjectTreeNode();
        ObjectTreeNode n1 = new ObjectTreeNode();
        ObjectTreeNode n2 = new ObjectTreeNode();
        
        assertEquals(-1, instance.replaceChild(n1, n2));
        List result = instance.getChildren();
        assertEquals(0, result.size());
    }

    /**
     * Test of replaceChild method, of class ObjectTreeNode.
     */
    @Test
    public void testReplaceChild4() {
        ObjectTreeNode instance = new ObjectTreeNode();
        ObjectTreeNode n1 = new ObjectTreeNode();
        ObjectTreeNode n2 = new ObjectTreeNode();
        
        instance.addChild(n1);
        assertEquals(0, instance.replaceChild(n1, n2));
        
        List result = instance.getChildren();
        assertEquals(1, result.size());
        assertEquals(n2, result.get(0));
        
    }
    

    /**
     * Test of clear method, of class ObjectTreeNode.
     */
    @Test
    public void testClear() {
        ObjectTreeNode instance = new ObjectTreeNode();
        ObjectTreeNode n1 = new ObjectTreeNode();
        ObjectTreeNode n2 = new ObjectTreeNode();
        ObjectTreeNode n3 = new ObjectTreeNode();
        ObjectTreeNode n4 = new ObjectTreeNode();
        instance.addChild(n1);
        instance.addChild(n2);
        instance.addChild(n3);
        instance.addChild(n4);
        instance.clear();
        assertTrue(instance.isLeaf());
        assertEquals(0, instance.getNumberOfChildren());
        assertEquals(0, instance.getChildren().size());
    }

    /**
     * Test of clear method, of class ObjectTreeNode.
     */
    @Test
    public void testClear2() {
        ObjectTreeNode instance = new ObjectTreeNode();
        ObjectTreeNode n1 = new ObjectTreeNode();
        instance.addChild(n1);
        instance.clear();
        assertTrue(instance.isLeaf());
        assertEquals(0, instance.getNumberOfChildren());
        assertEquals(0, instance.getChildren().size());
    }
    
    /**
     * Test of clear method, of class ObjectTreeNode.
     */
    @Test
    public void testClear3() {
        ObjectTreeNode instance = new ObjectTreeNode();
        instance.clear();
        assertTrue(instance.isLeaf());
        assertEquals(0, instance.getNumberOfChildren());
        assertEquals(0, instance.getChildren().size());
    }
    
    
    /**
     * Test of hasExtraData method, of class ObjectTreeNode.
     */
    @Test
    public void testHasExtraData() {
        ObjectTreeNode instance = new ObjectTreeNode();
        assertFalse(instance.hasExtraData());
    }

    /**
     * Test of sort method, of class ObjectTreeNode.
     */
    @Test
    public void testSort() {
        ObjectTreeNode instance = new ObjectTreeNode();
        ObjectTreeNode nodeA = new ObjectTreeNode("a");
        ObjectTreeNode nodeB = new ObjectTreeNode("b");
        ObjectTreeNode nodeBA = new ObjectTreeNode("ba");
        ObjectTreeNode nodeBB = new ObjectTreeNode("bb");
        ObjectTreeNode nodeBC = new ObjectTreeNode("bc");
        ObjectTreeNode nodeBCA = new ObjectTreeNode("bca");
        ObjectTreeNode nodeBCB = new ObjectTreeNode("bcb");
        ObjectTreeNode nodeBCC = new ObjectTreeNode("bcc");
        ObjectTreeNode nodeBCD = new ObjectTreeNode("bcd");
        ObjectTreeNode nodeC = new ObjectTreeNode("c");
        ObjectTreeNode nodeD = new ObjectTreeNode("d");
        
        instance.addChild(nodeD);
        instance.addChild(nodeB);
        instance.addChild(nodeC);
        instance.addChild(nodeA);
        
        nodeB.addChild(nodeBB);
        nodeB.addChild(nodeBA);
        nodeB.addChild(nodeBC);
        
        nodeBC.addChild(nodeBCD);
        nodeBC.addChild(nodeBCA);
        nodeBC.addChild(nodeBCB);
        nodeBC.addChild(nodeBCC);
        
        instance.sort();
        
        assertEquals(nodeA, instance.getChildAtPos(0));
        assertEquals(nodeB, instance.getChildAtPos(1));
        assertEquals(nodeC, instance.getChildAtPos(2));
        assertEquals(nodeD, instance.getChildAtPos(3));
        
        assertEquals(nodeBA, nodeB.getChildAtPos(0));
        assertEquals(nodeBB, nodeB.getChildAtPos(1));
        assertEquals(nodeBC, nodeB.getChildAtPos(2));

        assertEquals(nodeBCA, nodeBC.getChildAtPos(0));
        assertEquals(nodeBCB, nodeBC.getChildAtPos(1));
        assertEquals(nodeBCC, nodeBC.getChildAtPos(2));
        assertEquals(nodeBCD, nodeBC.getChildAtPos(3));
    }

    /**
     * Test of sort method, of class ObjectTreeNode.
     */
    @Test
    public void testSort2() {
        ObjectTreeNode instance = new ObjectTreeNode();
        ObjectTreeNode nodeB = new ObjectTreeNode("b");
        ObjectTreeNode nodeBC = new ObjectTreeNode("bc");
        ObjectTreeNode nodeBCA = new ObjectTreeNode("bca");
        ObjectTreeNode nodeBCB = new ObjectTreeNode("bcb");
        ObjectTreeNode nodeBCC = new ObjectTreeNode("bcc");
        ObjectTreeNode nodeBCD = new ObjectTreeNode("bcd");
        
        instance.addChild(nodeB);
        
        nodeB.addChild(nodeBC);
        
        nodeBC.addChild(nodeBCD);
        nodeBC.addChild(nodeBCA);
        nodeBC.addChild(nodeBCB);
        nodeBC.addChild(nodeBCC);
        
        instance.sort();
        
        assertEquals(nodeB, instance.getChildAtPos(0));
        
        assertEquals(nodeBC, nodeB.getChildAtPos(0));

        assertEquals(nodeBCA, nodeBC.getChildAtPos(0));
        assertEquals(nodeBCB, nodeBC.getChildAtPos(1));
        assertEquals(nodeBCC, nodeBC.getChildAtPos(2));
        assertEquals(nodeBCD, nodeBC.getChildAtPos(3));
    }

    
    /**
     * Test of getChild method, of class ObjectTreeNode.
     */
    @Test
    public void testGetChild_Object() {
        ObjectTreeNode instance = new ObjectTreeNode();
        ObjectTreeNode n1 = new ObjectTreeNode("n1");
        ObjectTreeNode n2 = new ObjectTreeNode("n2");
        ObjectTreeNode n3 = new ObjectTreeNode("n3");
        ObjectTreeNode n4 = new ObjectTreeNode("n4");
        instance.addChild(n1);
        instance.addChild(n2);
        instance.addChild(n3);
        instance.addChild(n4);
        assertEquals(n1, instance.getChild("n1"));
        assertEquals(n2, instance.getChild("n2"));
        assertEquals(n3, instance.getChild("n3"));
        assertEquals(n4, instance.getChild("n4"));
        assertNull(instance.getChild("n5"));
    }

    /**
     * Test of getChild method, of class ObjectTreeNode.
     */
    @Test
    public void testGetChild_Object2() {
        ObjectTreeNode instance = new ObjectTreeNode();
        assertNull(instance.getChild("n1"));
        ObjectTreeNode n1 = new ObjectTreeNode("n1");
        instance.addChild(n1);
        assertEquals(n1, instance.getChild("n1"));
        assertNull(instance.getChild("n5"));
        
        assertNull(instance.getChild(null));
    }
    
    /**
     * Test of getChild method, of class ObjectTreeNode.
     */
    @Test
    public void testGetChild_Object_TreeNode() {
        ObjectTreeNode instance = new ObjectTreeNode();
        ObjectTreeNode n1 = new ObjectTreeNode("n1");
        ObjectTreeNode n2 = new ObjectTreeNode("n2");
        ObjectTreeNode n3 = new ObjectTreeNode("n3");
        ObjectTreeNode n4 = new ObjectTreeNode("n4");
        ObjectTreeNode n5 = new ObjectTreeNode("n5");
        instance.addChild(n1);
        instance.addChild(n2);
        instance.addChild(n3);
        instance.addChild(n4);
        assertEquals(n1, instance.getChild("n1", null));
        assertEquals(n1, instance.getChild("n1", n1));
        assertNull(instance.getChild("n1", n2));
        assertNull(instance.getChild("n1", n3));
        assertNull(instance.getChild("n1", n4));
        assertNull(instance.getChild("n1", n5));
        
        assertEquals(n2, instance.getChild("n2", null));
        assertEquals(n2, instance.getChild("n2", n1));
        assertEquals(n2, instance.getChild("n2", n2));
        assertNull(instance.getChild("n2", n3));
        assertNull(instance.getChild("n2", n4));
        assertNull(instance.getChild("n2", n5));

        assertEquals(n3, instance.getChild("n3", null));
        assertEquals(n3, instance.getChild("n3", n1));
        assertEquals(n3, instance.getChild("n3", n2));
        assertEquals(n3, instance.getChild("n3", n3));
        assertNull(instance.getChild("n3", n4));
        assertNull(instance.getChild("n3", n5));

        assertEquals(n4, instance.getChild("n4", null));
        assertEquals(n4, instance.getChild("n4", n1));
        assertEquals(n4, instance.getChild("n4", n2));
        assertEquals(n4, instance.getChild("n4", n3));
        assertEquals(n4, instance.getChild("n4", n4));
        assertNull(instance.getChild("n4", n5));

        assertNull(instance.getChild("n5", null));
        assertNull(instance.getChild("n5", n1));
        assertNull(instance.getChild("n5", n2));
        assertNull(instance.getChild("n5", n3));
        assertNull(instance.getChild("n5", n4));
        assertNull(instance.getChild("n5", n5));
        
    }

    /**
     * Test of getChild method, of class ObjectTreeNode.
     */
    @Test
    public void testGetChild_Object_TreeNode2() {
        ObjectTreeNode instance = new ObjectTreeNode();
        ObjectTreeNode n1 = new ObjectTreeNode("n1");
        ObjectTreeNode n2 = new ObjectTreeNode("n2");
        
        assertNull(instance.getChild(null, n1));
        assertNull(instance.getChild("n1", n1));
        assertNull(instance.getChild("n1", null));
        
        
        instance.addChild(n1);
        assertEquals(n1, instance.getChild("n1", null));
        assertEquals(n1, instance.getChild("n1", n1));
        assertNull(instance.getChild("n1", n2));
        
    }
    
    /**
     * Test of compareTo method, of class ObjectTreeNode.
     */
    @Test
    public void testCompareTo() {
        ObjectTreeNode n1 = new ObjectTreeNode("n1");
        ObjectTreeNode n2 = new ObjectTreeNode("n2");
        assertEquals(-1, n1.compareTo(n2));
        assertEquals(1, n2.compareTo(n1));
        assertEquals(0, n1.compareTo(n1));
        
        ObjectTreeNode n1bis = new ObjectTreeNode("n1");
        assertEquals(0, n1.compareTo(n1bis));
        assertEquals(0, n1bis.compareTo(n1));
        
        assertEquals(1, n1.compareTo(null));
        
        ObjectTreeNode null1 = new ObjectTreeNode();
        ObjectTreeNode null2 = new ObjectTreeNode();
        assertEquals(0, null1.compareTo(null2));
        
        ObjectTreeNode n3 = new ObjectTreeNode();
        ObjectTreeNode n4 = new ObjectTreeNode();
        assertEquals(0, n3.compareTo(n4));
        assertEquals(1, n1.compareTo(n3));
        assertEquals(-1, n3.compareTo(n1));
        
    }

    /**
     * Test of writeTreeNode method, of class ObjectTreeNode.
     */
    @Test
    public void testWriteReadTreeNode() throws Exception {
        
        ObjectTreeNode instance = new ObjectTreeNode();
        TreeNode nodeA = new ObjectTreeNode("a");
        TreeNode nodeB = new ObjectTreeNode("b");
        TreeNode nodeBA = new ObjectTreeNode("ba");
        TreeNode nodeBB = new ObjectTreeNode("bb");
        TreeNode nodeBC = new ObjectTreeNode("bc");
        TreeNode nodeBCA = new ObjectTreeNode("bca");
        TreeNode nodeBCB = new ObjectTreeNode("bcb");
        TreeNode nodeBCC = new ObjectTreeNode("bcc");
        TreeNode nodeBCD = new ObjectTreeNode("bcd");
        TreeNode nodeBCDA = new ObjectTreeNode("bcda");
        TreeNode nodeC = new ObjectTreeNode("c");
        TreeNode nodeD = new ObjectTreeNode("d");
        
        instance.addChild(nodeD);
        instance.addChild(nodeB);
        instance.addChild(nodeC);
        instance.addChild(nodeA);
        
        nodeB.addChild(nodeBB);
        nodeB.addChild(nodeBA);
        nodeB.addChild(nodeBC);
        
        nodeBC.addChild(nodeBCD);
        nodeBC.addChild(nodeBCA);
        nodeBC.addChild(nodeBCB);
        nodeBC.addChild(nodeBCC);
        
        nodeBCD.addChild(nodeBCDA);
        
        
        instance.sort();
        
        
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(); 
                ObjectOutputStream oout = new ObjectOutputStream(out)) {
            instance.writeTreeNode(oout);
            
            oout.flush();
            
            byte[] data = out.toByteArray();
            
            try (ByteArrayInputStream in = new ByteArrayInputStream(data); ObjectInputStream oin = new ObjectInputStream(in)) {
                
                ObjectTreeNode instance2 = new ObjectTreeNode();
                instance2.readTreeNode(oin);
                
                assertEquals(instance, instance2);

                assertEquals(nodeA, instance2.getChildAtPos(0));
                assertEquals(nodeB, instance2.getChildAtPos(1));
                assertEquals(nodeC, instance2.getChildAtPos(2));
                assertEquals(nodeD, instance2.getChildAtPos(3));
                
                nodeB = instance2.getChildAtPos(1);

                assertEquals(nodeBA, nodeB.getChildAtPos(0));
                assertEquals(nodeBB, nodeB.getChildAtPos(1));
                assertEquals(nodeBC, nodeB.getChildAtPos(2));

                nodeBC = nodeB.getChildAtPos(2);
                
                assertEquals(nodeBCA, nodeBC.getChildAtPos(0));
                assertEquals(nodeBCB, nodeBC.getChildAtPos(1));
                assertEquals(nodeBCC, nodeBC.getChildAtPos(2));
                assertEquals(nodeBCD, nodeBC.getChildAtPos(3));
                
                nodeBCD = nodeBC.getChildAtPos(3);
                assertEquals(nodeBCDA, nodeBCD.getChildAtPos(0));
                
            }
            
            
        } 
        
    }
    
    
    @Test
    public void testEquals() {
        ObjectTreeNode n1 = new ObjectTreeNode("n1");
        ObjectTreeNode n2 = new ObjectTreeNode("n2");
        ObjectTreeNode n3 = new ObjectTreeNode();
        assertTrue(n1.equals(n1));
        assertFalse(n1.equals(n2));
        
        assertFalse(n1.equals(null));
        assertFalse(n1.equals(n3));
        assertTrue(n3.equals(n3));
        
        assertFalse(n1.equals("other type"));
        
    }

    @Test
    public void testHashCode() {
        ObjectTreeNode n1 = new ObjectTreeNode("n1");
        ObjectTreeNode n2 = new ObjectTreeNode("n2");
        ObjectTreeNode n3 = new ObjectTreeNode();
        assertEquals(n1.hashCode(), n1.hashCode());
        assertEquals(n2.hashCode(), n2.hashCode());
        assertEquals(n3.hashCode(), n3.hashCode());
        
        assertNotEquals(n1.hashCode(), n2.hashCode());
        assertNotEquals(n1.hashCode(), n3.hashCode());
        assertNotEquals(n2.hashCode(), n3.hashCode());
    }
    
}
