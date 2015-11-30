/*
 *  © 2015 Jakub Strychowski
 */

package org.neurpheus.collections.tree.linkedlist;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.neurpheus.collections.tree.Tree;
import org.neurpheus.collections.tree.TreeNode;
import org.neurpheus.collections.tree.objecttree.ObjectTreeFactory;
import org.neurpheus.logging.LoggerService;

/**
 *
 * @author Kuba
 */
public class LinkedListTreeTools {
    
    private static final Logger LOGGER = LoggerService.getLogger(LinkedListTreeTools.class);
    
    
    public static Tree createBaseTree(String filePath, boolean reverse) throws IOException {
        ObjectTreeFactory factory = ObjectTreeFactory.getInstance();
        Tree result = factory.createTree();
        TreeNode root = result.getRoot();
        
        //String charset = //Charset.defaultCharset().name();
        String charset = "UTF-8"; 
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath), Charset.forName(charset))) {
            String line;
            int lineNumber = 1;
            try {
                while ((line = reader.readLine()) != null) {
                    String[] values = line.split("\\s"); 
                    for (String str : values) {
                        if (str.length() > 0) {
                            addStringToTree(factory, reverse, root, str, reverse ? str.length() - 1 : 0);
                        }
                    }
                    lineNumber++;
                }
            } catch (IOException e) {
                LOGGER.severe("Cannot read line " + lineNumber);
                throw e;
            }
        }
        return result;
    }

    public static String checkTree(Tree tree, String filePath, boolean reverse) throws IOException {
        long startTime = System.currentTimeMillis();
        String charset = "utf-8"; //Charset.defaultCharset().name();
        int numberOfLookups = 0;
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath), Charset.forName(charset))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split("\\s"); 
                for (String str : values) {
                    if (str.length() > 0) {
                        TreeNode node = LinkedListTreeTools.findNode(str, tree, reverse);
                        numberOfLookups++;
                        if (node == null) {
                            return "Cannot find term: " + str;
                        }
                    }
                }
            }
        }
        long duration = System.currentTimeMillis() - startTime;
        int speed = (int) (numberOfLookups * 1000 / duration);
        LOGGER.info(String.format("Found %d terms in %d ms. Speed: %d terms per s.", numberOfLookups, duration, speed));
        return null;
    }
    
    public static Tree createBaseTree(List<String> examples, boolean reverse) {
        LOGGER.fine("Creating base tree from " + examples.size() + " words");
        ObjectTreeFactory factory = ObjectTreeFactory.getInstance();
        Tree result = factory.createTree();
        TreeNode root = result.getRoot();
        
        for (String str : examples) {
            addStringToTree(factory, reverse, root, str, reverse ? str.length() - 1 : 0);
        }
        
        return result;
    }
    
    private static void addStringToTree(ObjectTreeFactory factory, boolean reverse, TreeNode node, String str, int pos) {
        int valueCode = str.charAt(pos);
        TreeNode child = node.getChild(valueCode);
        int endPos = reverse ? 0 : str.length() - 1;
        if (child == null) {
            if (pos == endPos) {
                //child = factory.createTreeNodeWithAdditionalData(valueCode, 0);
                child = factory.createTreeNode(valueCode);
            } else {
                child = factory.createTreeNode(valueCode);
            }
            node.addChild(child);
        }
        if (pos != endPos) {
           addStringToTree(factory, reverse, child, str, pos + (reverse ? -1 : 1));
        }
    }
    
    
    private int[] stack = new int[1024];
    
    public static TreeNode findNode(String example, Tree tree, boolean reverse) {
        TreeNode node = tree.getRoot();
        if (reverse) {
            for (int i = example.length() - 1; node != null && i >= 0; i--) {
                int ch = example.charAt(i);
                node = node.getChild(ch);
            }
        } else {
            for (int i = 0; i < example.length() && node != null; i++) {
                int ch = example.charAt(i);
                node = node.getChild(ch);
            }
        }
        return node;
    }
    
    
    public static void saveTree(Tree tree, String path) throws IOException {
        
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        try (ObjectOutputStream oou = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
            oou.writeObject(tree);
        }
    }
    
    public static String testTreeCreation(String path, boolean REVERSE) throws IOException {
        LOGGER.info("--------------------------------------------");
        LOGGER.info("Test file: " + path);
        LOGGER.info("--------------------------------------------");
        Tree baseTree = LinkedListTreeTools.createBaseTree(path, REVERSE);
        LOGGER.info("Creating base tree");
        
        LinkedListTree compressedTree = (LinkedListTree) LinkedListTreeFactory.getInstance().createTree(baseTree, true, true);
        
        String resultPath;
        if (path.endsWith(".txt")) {
            resultPath = path.substring(0, path.length() - 4) + ".lztrie";
        } else {
            resultPath = path + ".lztrie";
        }
        
        LOGGER.fine("Saving tree to file: " + resultPath);
        saveTree(compressedTree, resultPath);
        
        LOGGER.fine("Checking traversal through the tree.");
        String result = LinkedListTreeTools.checkTree(compressedTree, path, REVERSE);
        return result;
    }

    public static String testTreeCreation2(String path, boolean REVERSE) throws IOException {
        LOGGER.info("--------------------------------------------");
        LOGGER.info("Test file: " + path);
        LOGGER.info("--------------------------------------------");
        Tree baseTree = LinkedListTreeTools.createBaseTree(path, REVERSE);
        LOGGER.info("Creating base tree");
        
        List<TreeNode> children = baseTree.getRoot().getChildren();
        List<LinkedListTree> subTrees = new ArrayList<>(children.size());
        long allocationSize = 0;
        long startTime = System.currentTimeMillis();
        for (TreeNode node : children) {
            baseTree.setRoot(node);
            LinkedListTree compressedTree = (LinkedListTree) LinkedListTreeFactory.getInstance().createTree(baseTree, true, true);
            subTrees.add(compressedTree);
            allocationSize += compressedTree.getUnitArray().getAllocationSize();
        }

        Tree compressedTree = joinTrees(subTrees);
        
        for (LinkedListTree subTree : subTrees) {
            LOGGER.info("allocation size for " + subTree.getRoot().getValue().toString() + " : " + subTree.getUnitArray().getAllocationSize());
            
        }
        LOGGER.info("Summarized allocation size: " + allocationSize);
        LOGGER.info("CompressionTime: " + (System.currentTimeMillis() - startTime));
        
        
        String resultPath;
        if (path.endsWith(".txt")) {
            resultPath = path.substring(0, path.length() - 4) + ".lztrie";
        } else {
            resultPath = path + ".lztrie";
        }
        
        LOGGER.fine("Saving tree to file: " + resultPath);
        saveTree(compressedTree, resultPath);
        
        LOGGER.fine("Checking traversal through the tree.");
        String result = LinkedListTreeTools.checkTree(compressedTree, path, REVERSE);
        return result;
    }
    
    
    public static Tree joinTrees(List<LinkedListTree> subStrees) {
        return null;
    }
    
    public static void main(String[] args) {
        String path = args[0];
        try {
            String result = LinkedListTreeTools.testTreeCreation(path, false);
            if (result != null) {
                LOGGER.severe(result);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            LOGGER.throwing("Main", "testTreeCreation", ex);
        }
    }
    
}
