/*
 *  © 2016 Jakub Strychowski
 */

package org.neurpheus.collections.tree.linkedlist;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author Jakub Strychowski
 */
public class LinkedListTreeTest {
    
    public LinkedListTreeTest() {
    }

    @Test
    public void testGetRoot() {
        LinkedListTree tree = LinkedListTreeNodeTest.createTestTree(false);
        LinkedListTreeNode root = tree.getRoot();
        LinkedListTreeNode nodeW = root.getChild((int) 'w');
        assertNotNull(nodeW);
        assertEquals((int) 'w', nodeW.getValue().intValue());
    }

    @Test (expected = UnsupportedOperationException.class)
    public void testSetRoot() {
        LinkedListTree tree = LinkedListTreeNodeTest.createTestTree(false);
        LinkedListTreeNode root = tree.getRoot();
        LinkedListTreeNode nodeW = root.getChild((int) 'w');
        tree.setRoot(nodeW);
    }

    @Test
    public void testClear() {
        LinkedListTree tree = LinkedListTreeNodeTest.createTestTree(false);
        tree.clear();
        assertEquals(0, tree.getUnitArray().size());
    }

    @Test
    public void testGetFactory() {
        LinkedListTree tree = LinkedListTreeNodeTest.createTestTree(false);
        assertEquals(LinkedListTreeFactory.getInstance(), tree.getFactory());
    }

    @Test
    public void testGetUnitArray() {
        LinkedListTree tree = LinkedListTreeNodeTest.createTestTree(false);
        LinkedListTreeUnitArray units = tree.getUnitArray();
        assertTrue(units.size() > 10);
        assertEquals(tree.getRoot().getUnit(), units.get(0));
    }

    @Test
    public void testSetUnitArray() {
        LinkedListTree tree = LinkedListTreeNodeTest.createTestTree(false);
        LinkedListTreeUnitArray units = tree.getUnitArray();
        LinkedListTreeUnitArray units2 = new FastLinkedListTreeUnitArray(units);
        tree.setUnitArray(units2);
        assertTrue(units2 == tree.getUnitArray());
    }

    @Test
    public void testWriteAndRead() throws Exception {
        LinkedListTree tree = LinkedListTreeNodeTest.createTestTree(true);
        
        byte[] data;
        try (
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                DataOutputStream dataStream = new DataOutputStream(outStream)) {
            tree.write(dataStream);
            dataStream.flush();
            data = outStream.toByteArray();
        } catch (IOException ex) {
            fail(ex.getMessage());
            return;
        }
        
        LinkedListTree tree2 = LinkedListTreeFactory.getInstance().createTree();
        try (
                ByteArrayInputStream inStream = new ByteArrayInputStream(data);
                DataInputStream dataStream = new DataInputStream(inStream)) {
            tree2.read(dataStream);
        } catch (IOException ex) {
            fail(ex.getMessage());
        }

        LinkedListTreeUnitArray lla1 = tree.getUnitArray();
        LinkedListTreeUnitArray lla2 = tree2.getUnitArray();
        
        assertEquals(lla1.size(), lla2.size());
        assertEquals(lla1.getAllocationSize(), lla2.getAllocationSize());
        assertFalse(lla1 == lla2);
        for (int i = 0; i < lla1.size(); i++) {
            assertEquals(lla1.get(i), lla2.get(i));
        }
    }


    @Test
    public void testSplit() {
        LinkedListTree tree = LinkedListTreeNodeTest.createTestTree(false);
        LinkedListTreeUnitArray units = tree.getUnitArray();
        System.out.println("Tree to split:");
        System.out.println(units.toString(0, units.size()));
        List<LinkedListTree> forest = tree.split();
        LinkedListTreeNode root = tree.getRoot();
        for (LinkedListTree subTree: forest) {
            LinkedListTreeNode subTreeRoot = subTree.getRoot();
            //units = subTree.getUnitArray();
            //System.out.println("Subtree: " + subTreeRoot.getValue().toString());
            //System.out.println(units.toString(0, units.size()));
            Integer key = subTreeRoot.getValue();
            LinkedListTreeNode node2 = root.getChild(key);
            compareNodes(subTreeRoot, node2);
        }
        
        LinkedListTree treeJoined = LinkedListTreeFactory.getInstance().createTree();
        treeJoined.joinSubTrees(forest);

        System.out.println("Joined Tree:");
        System.out.println(treeJoined.getUnitArray().toString(0, treeJoined.getUnitArray().size()));
        compareNodes(tree.getRoot(), treeJoined.getRoot());
        
        
    }
    
    private void compareNodes(LinkedListTreeNode node1, LinkedListTreeNode node2) {
        assertNotNull(node1);
        assertNotNull(node2);
        assertEquals(node1.getValue(), node2.getValue());
        assertEquals(node1.getNumberOfChildren(), node2.getNumberOfChildren());
        for (LinkedListTreeNode child1 : node1.getChildren()) {
            Integer key = child1.getValue();
            LinkedListTreeNode child2 = node2.getChild(key);
            assertNotNull(child2);
            compareNodes(child1, child2);
        }
    }
    
}
