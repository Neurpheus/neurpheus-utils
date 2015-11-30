/*
 *  © 2015 Jakub Strychowski
 */

package org.neurpheus.collections.tree.objecttree;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static org.junit.Assert.*;

import org.junit.Test;
import org.neurpheus.collections.tree.TreeNode;

/**
 *
 * @author Jakub Strychowski
 */
public class ObjectTreeNodeWithDataTest {
    
    public ObjectTreeNodeWithDataTest() {
    }

    /**
     * Test of getData method, of class ObjectTreeNodeWithData.
     */
    @Test
    public void testGetData() {
        ObjectTreeNodeWithData instance = new ObjectTreeNodeWithData();
        assertNull(instance.getData());
        
        String data = "data";
        instance = new ObjectTreeNodeWithData("n1", data);
        assertEquals(data, instance.getData());
    }

    /**
     * Test of setData method, of class ObjectTreeNodeWithData.
     */
    @Test
    public void testSetData() {
        ObjectTreeNodeWithData instance = new ObjectTreeNodeWithData();
        assertNull(instance.getData());
        String data = "data";
        instance.setData(data);
        assertEquals(data, instance.getData());
        String data2 = "data2";
        instance.setData(data2);
        assertEquals(data2, instance.getData());
        instance.setData(null);
        assertNull(instance.getData());
    }

    /**
     * Test of hasExtraData method, of class ObjectTreeNodeWithData.
     */
    @Test
    public void testHasExtraData() {
        ObjectTreeNodeWithData instance = new ObjectTreeNodeWithData();
        assertTrue(instance.hasExtraData());
    }

    /**
     * Test of clear method, of class ObjectTreeNodeWithData.
     */
    @Test
    public void testClear() {
        String data = "data";
        ObjectTreeNodeWithData instance = new ObjectTreeNodeWithData("n1", data);
        instance.clear();
        assertNull(instance.getData());
    }
    
    /**
     * Test of writeTreeNode method, of class ObjectTreeNode.
     */
    @Test
    public void testWriteReadTreeNode() throws Exception {
        
        ObjectTreeNode instance = new ObjectTreeNode();
        TreeNode nodeA = new ObjectTreeNodeWithData("a", "data_a");
        TreeNode nodeB = new ObjectTreeNodeWithData("b", "data_b");
        TreeNode nodeBA = new ObjectTreeNodeWithData("ba", "data_ba");
        TreeNode nodeBB = new ObjectTreeNodeWithData("bb", "data_bb");
        TreeNode nodeBC = new ObjectTreeNodeWithData("bc", "data_bc");
        TreeNode nodeBCA = new ObjectTreeNodeWithData("bca", "data_bca");
        TreeNode nodeBCB = new ObjectTreeNodeWithData("bcb", "data_bcb");
        TreeNode nodeBCC = new ObjectTreeNodeWithData("bcc", "data_bcc");
        TreeNode nodeBCD = new ObjectTreeNodeWithData("bcd", "data_bcd");
        TreeNode nodeBCDA = new ObjectTreeNodeWithData("bcda", "data_bcda");
        TreeNode nodeC = new ObjectTreeNodeWithData("c", "data_c");
        TreeNode nodeD = new ObjectTreeNodeWithData("d", "data_d");
        
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
        String d1 = "d1";
        String d2 = "d2";
        ObjectTreeNodeWithData n1 = new ObjectTreeNodeWithData("n1", d1);
        ObjectTreeNodeWithData n2 = new ObjectTreeNodeWithData("n2", d2);
        
        ObjectTreeNodeWithData n3 = new ObjectTreeNodeWithData();
        
        ObjectTreeNodeWithData n1a = new ObjectTreeNodeWithData("n1", d1);
        ObjectTreeNodeWithData n1b = new ObjectTreeNodeWithData("n1", d2);
        ObjectTreeNodeWithData n1c = new ObjectTreeNodeWithData("n1c", d1);
        ObjectTreeNodeWithData n1d = new ObjectTreeNodeWithData("n1", null);
        ObjectTreeNode n1e = new ObjectTreeNode("n1");
        
        assertTrue(n1.equals(n1));
        assertTrue(n1.equals(n1a));
        assertTrue(n1.equals(n1e));
        assertFalse(n1.equals(n1b));
        assertFalse(n1.equals(n1c));
        assertFalse(n1.equals(n1d));
        
        assertFalse(n1.equals(n2));
        
        assertFalse(n1.equals(null));
        assertFalse(n1.equals(n3));
        assertTrue(n3.equals(n3));
    }

    @Test
    public void testHashCode() {
        
        String d1 = "d1";
        String d2 = "d2";
        ObjectTreeNodeWithData n1 = new ObjectTreeNodeWithData("n1", d1);
        ObjectTreeNodeWithData n2 = new ObjectTreeNodeWithData("n2", d2);
        
        ObjectTreeNodeWithData n3 = new ObjectTreeNodeWithData();
        
        ObjectTreeNodeWithData n1a = new ObjectTreeNodeWithData("n1", d1);
        ObjectTreeNodeWithData n1b = new ObjectTreeNodeWithData("n1", d2);
        ObjectTreeNodeWithData n1c = new ObjectTreeNodeWithData("n1c", d1);
        ObjectTreeNodeWithData n1d = new ObjectTreeNodeWithData("n1", null);

        
        assertEquals(n1.hashCode(), n1.hashCode());
        assertNotEquals(n1.hashCode(), n2.hashCode());
        assertNotEquals(n1.hashCode(), n3.hashCode());
        assertEquals(n1.hashCode(), n1a.hashCode());
        assertNotEquals(n1.hashCode(), n1b.hashCode());
        assertNotEquals(n1.hashCode(), n1c.hashCode());
        assertNotEquals(n1.hashCode(), n1d.hashCode());
        assertNotEquals(n1.hashCode(), n3.hashCode());

        assertEquals(n2.hashCode(), n2.hashCode());
        assertEquals(n3.hashCode(), n3.hashCode());
        
        assertNotEquals(n2.hashCode(), n3.hashCode());
    }
    
    
}
