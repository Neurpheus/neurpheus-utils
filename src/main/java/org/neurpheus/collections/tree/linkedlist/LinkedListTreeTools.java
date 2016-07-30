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
import org.neurpheus.collections.tree.TreeNode;
import org.neurpheus.collections.tree.TreeNodeWithData;
import org.neurpheus.collections.tree.objecttree.ObjectTree;
import org.neurpheus.collections.tree.objecttree.ObjectTreeFactory;
import org.neurpheus.collections.tree.objecttree.ObjectTreeNode;
import org.neurpheus.logging.LoggerService;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tools which helps to create and test LLTries from a command line.
 *
 * @author Jakub Strychowski
 */
public class LinkedListTreeTools {

    private static final Logger LOGGER = LoggerService.getLogger(LinkedListTreeTools.class);

    private static final String CANNOT_FIND_TERM = "Cannot find char sequence: ";
    
    private LinkedListTreeTools() {
    }

    /**
     * Creates a tree from a list of words read from the specified file.
     * <p>
     * This method creates a {@link ObjectTree} from a list of words. The input should be a text
     * file where each line can contain many words separated by a whitespace character.
     * </p>
     * <p>
     * Each word from the file creates a path in the tree. Successive characters from the word
     * describe successive nodes on the path in the tree.
     * </p>
     * <p>
     * Trees can be created also in reverse order - from the last character of a word to the first
     * one.
     * </p>
     * <p>
     * This method can also assign an index value to each unique word creating a perfect hashing
     * mapping.
     * </p>
     * <p>
     * You can create a compressed LLTrie from the result of this method using the
     * {@link LinkedListTreeFactory#createTree(Tree, boolean, boolean, boolean)} method.
     * </p>
     *
     *
     * @param filePath Path to text file with words written in any language.
     * @param charset  Character encoding used by the specified file.
     * @param reverse  if <strong>true</strong> a new tree will store characters in the reverse
     *                 order - from last char to the first one.
     * @param withData if <Strong>true</strong> a new tree will contain an index value assigned to
     *                 each word stored in the tree.
     *
     * @return A newly created tree.
     *
     * @throws IOException if any i/o error occurred.
     */
    public static Tree createBaseTree(
            String filePath, Charset charset, boolean reverse, boolean withData)
            throws IOException {
        ObjectTreeFactory factory = ObjectTreeFactory.getInstance();
        Tree result = factory.createTree();
        TreeNode root = result.getRoot();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath), charset)) {
            String line;
            int lineNumber = 1;
            int index = 0;
            try {
                while ((line = reader.readLine()) != null) {
                    String[] values = line.split("\\s");
                    for (String str : values) {
                        if (str.length() > 0) {
                            addStringToTree(factory, reverse, root, str,
                                            reverse ? str.length() - 1 : 0,
                                            withData ? index : -1);
                            index++;
                        }
                    }
                    lineNumber++;
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Cannot read line {0}", lineNumber);
                throw e;
            }
        }
        return result;
    }

    /**
     * Creates a tree from a list of words.
     * <p>
     * This method creates a {@link ObjectTree} from a list of words.
     * </p>
     * <p>
     * Each word from the file creates a path in the tree. Successive characters from the word
     * describe successive nodes on the path in the tree.
     * </p>
     * <p>
     * Trees can be created also in reverse order - from the last character of a word to the first
     * one.
     * </p>
     * <p>
     * This method can also assign an index value to each unique word creating a perfect hashing
     * mapping.
     * </p>
     * <p>
     * You can create a compressed LLTrie from the result of this method using the
     * {@link LinkedListTreeFactory#createTree(Tree, boolean, boolean, boolean)} method.
     * </p>
     *
     *
     * @param examples A list of words to store in the tree.
     * @param reverse  if <strong>true</strong> a new tree will store characters in the reverse
     *                 order - from last char to the first one.
     * @param withData if <Strong>true</strong> a new tree will contain an index value assigned to
     *                 each word stored in the tree.
     *
     * @return A newly created tree.
     */
    public static Tree createBaseTree(List<String> examples, boolean reverse, boolean withData) {
        LOGGER.log(Level.FINE, "Creating base tree from {0} words", examples.size());
        ObjectTreeFactory factory = ObjectTreeFactory.getInstance();
        Tree result = factory.createTree();
        TreeNode root = result.getRoot();

        int index = 0;
        for (String str : examples) {
            addStringToTree(factory, reverse, root, str,
                            reverse ? str.length() - 1 : 0, withData ? index : -1);
            index++;
        }

        ((ObjectTreeNode) result.getRoot()).sort();

        return result;
    }

    /**
     * Adds recursively a word to {@link ObjectTree}.
     *
     * @param factory  the factory used for nodes creation.
     * @param reverse  if {@code true} analyze string in reverse order.
     * @param node     the parent node to which add child nodes representing successive characters.
     * @param str      the word to add to the tree.
     * @param pos      the current position in the word.
     * @param dataCode the data identifier which should be assigned to the last node in a tree path,
     *                 If it is negative integer, do not add any data at the end of the path.
     */
    private static void addStringToTree(ObjectTreeFactory factory, boolean reverse, TreeNode node,
                                        String str, int pos, int dataCode) {
        int valueCode = str.charAt(pos);
        TreeNode child = node.getChild(valueCode);
        int endPos = reverse ? 0 : str.length() - 1;
        if (child == null) {
            if (pos == endPos && dataCode >= 0) {
                child = factory.createTreeNodeWithAdditionalData(valueCode, dataCode);
            } else {
                child = factory.createTreeNode(valueCode);
            }
            node.addChild(child);
        }
        if (pos != endPos) {
            addStringToTree(factory, reverse, child, str, pos + (reverse ? -1 : 1), dataCode);
        } else if (dataCode >= 0 && !(child instanceof TreeNodeWithData)) {
            TreeNode child2 = factory.createTreeNodeWithAdditionalData(valueCode, dataCode);
            child2.setChildren(child.getChildren());
            node.replaceChild(child, child2);
        }
    }

    /**
     * Checks if a tree contains all words from the specified text file.
     *
     * @param tree          The tree to check.
     * @param filePath      The path to a text file with words written in any language.
     * @param charset       The character encoding used by the specified file.
     * @param reverse       if {@code true} the tree stores characters in the reverse order - from
     *                      last char to the first one.
     * @param withData      if {@code true} this method checks values assigned to nodes whether they
     *                      correspond to positions of word in the input file.
     * @param fastTraversal if {@code true} this method uses faster method for traversing LLTrie.
     * @param split         if {@code true} this method splits test words into parts at random
     *                      positions.
     *
     * @return error message or null if there is no error.
     *
     * @throws IOException if any i/o error occurred.
     */
    public static String checkTree(LinkedListTree tree, String filePath, Charset charset,
                                   boolean reverse, boolean withData, boolean fastTraversal,
                                   boolean split)
            throws IOException {
        long startTime = System.currentTimeMillis();
        int[] stack = new int[1000];
        long numberOfLookups = 0;
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath), charset)) {
            String line;
            int index = 0;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split("\\s");
                for (String str : values) {
                    if (str.length() > 0) {
                        String error;
                        if (fastTraversal) {
                            error = checkTreeFast(
                                    tree, str, stack, index,
                                    reverse, withData, split);
                        } else {
                            error = checkTree(tree, str, index, reverse, withData, split);
                        }
                        if (error != null) {
                            return error;
                        }
                        numberOfLookups++;
                        index++;
                    }
                }
            }
        }
        long duration = System.currentTimeMillis() - startTime;
        int speed = (int) (numberOfLookups * 1000 / duration);
        LOGGER.info(String.format("Found %d terms in %d ms. Speed: %d terms per s.",
                                  numberOfLookups, duration, speed));
        return null;
    }

    private static String checkTree(LinkedListTree tree,
                                    String str, int index,
                                    boolean reverse, boolean withData, boolean split) {
        String substr = str;
        int pos = split ? index % str.length() : 0;
        if (pos > 0) {
            substr = str.substring(0, pos);
        }
        LinkedListTreeNode node = findNode(substr, tree, reverse, null);
        if (node != null && pos > 0) {
            substr = str.substring(pos);
            node = findNode(substr, tree, reverse, node);
        }
        if (node == null) {
            return CANNOT_FIND_TERM + substr;
        }
        if (withData) {
            int data = (Integer) ((TreeNodeWithData) node).getData();
            if (index != data) {
                return "Cannot detect data value for term: " + str;
            }
        }
        return null;
    }

    private static String checkTreeFast(LinkedListTree tree,
                                        String str, int[] stack, int index,
                                        boolean reverse, boolean withData, boolean split) {
        if (withData) {
            return checkTreeFastWithData(tree, str, stack, index, reverse, split);
        } else {
            return checkTreeFastWithoutData(tree, str, stack, index, reverse, split);
        }
    }

    private static String checkTreeFastWithoutData(LinkedListTree tree,
                                        String str, int[] stack, int index,
                                        boolean reverse, boolean split) {
        String substr = str;
        int pos = split ? index % str.length() : 0;
        if (pos > 0) {
            substr = str.substring(0, pos);
        }
        LinkedListTreeNode node = findNode(substr, tree, reverse, null, stack);
        if (node != null && pos > 0) {
            substr = str.substring(pos);
            node = findNode(substr, tree, reverse, node, stack);
        }
        if (node == null) {
            return CANNOT_FIND_TERM + substr;
        }
        return null;
    }

    private static String checkTreeFastWithData(LinkedListTree tree,
                                                String str, int[] stack, int index,
                                                boolean reverse, boolean split) {
        String path = reverse ? new StringBuilder(str).reverse().toString() : str;
        String substr = path;
        LinkedListTreeNode node = tree.getRoot();
        int pos = split ? index % path.length() : 0;
        if (pos > 0) {
            substr = path.substring(0, pos);
            node = findNode(substr, tree, false, node, stack);
            if (node == null) {
                return CANNOT_FIND_TERM + substr;
            }
            substr = path.substring(pos);
        }
        int data = node.getData(substr, stack, 0);
        return index != data ? "Cannot find data value for term: " + str : null;
    }

    /**
     * Checks if the specified tree contains the specified string.
     *
     * @param example The string to go through.
     * @param tree    The tree having nodes described by characters.
     * @param reverse if {@code true} the tree stores characters in the reverse order - from last
     *                char to the first one.
     * @param startNode find a substring traversing from the this node in the tree.
     *
     * @return Found node or null if there is no path in the tree matching to the specified string.
     */
    public static LinkedListTreeNode findNode(
            String example, LinkedListTree tree, boolean reverse, LinkedListTreeNode startNode) {
        LinkedListTreeNode node = startNode == null ? tree.getRoot() : startNode;
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

    /**
     * Checks if the specified tree contains the specified string.
     *
     * @param example The string to go through.
     * @param tree    The tree having nodes described by characters.
     * @param reverse if {@code true} the tree stores characters in the reverse order - from last
     *                char to the first one.
     * @param startNode find a substring traversing from the this node in the tree.
     * @param stack   Stack used for fast recursion.
     *
     * @return Found node or null if there is no path in the tree matching to the specified string.
     */
    public static LinkedListTreeNode findNode(String example,
                                    LinkedListTree tree,
                                    boolean reverse,
                                    LinkedListTreeNode startNode,
                                    int[] stack) {
        LinkedListTreeNode node = startNode == null ? tree.getRoot() : startNode;
        if (reverse) {
            for (int i = example.length() - 1; node != null && i >= 0; i--) {
                int ch = example.charAt(i);
                node = node.getChild(ch, stack, 0);
            }
        } else {
            for (int i = 0; i < example.length() && node != null; i++) {
                int ch = example.charAt(i);
                node = node.getChild(ch, stack, 0);
            }
        }
        return node;
    }

    /**
     * Writes the specified tree to a file on the specified location.
     *
     * @param tree the tree which should be written.
     * @param path the location of the file.
     *
     * @throws IOException if any i/o error occurred.
     */
    public static void saveTree(Tree tree, String path) throws IOException {

        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        try (ObjectOutputStream oou = new ObjectOutputStream(new BufferedOutputStream(
                new FileOutputStream(file)))) {
            oou.writeObject(tree);
        }
    }

    /**
     * Test the LZTrie compression creating a linked list tree from a file containing words i any
     * language.
     *
     * @param path         The location of the input file.
     * @param reverse      if {@code true} the tree stores characters in the reverse order - from
     *                     last char to the first one.
     * @param parallelMode if {@code true} this method uses parallel mode (not safe, much faster on
     *                     multi core processors, worse compression).
     * @param withData     if {@code true} this method checks values assigned to nodes whether they
     *                     correspond to positions of word in the input file.
     * @param split   if {@code true} this method splits test words into 
     *                      two parts while checking.
     *
     * @return Error message.
     *
     * @throws IOException if any I/O error occurred.
     */
    public static String testTreeCreation(String path, boolean reverse, boolean parallelMode,
                                          boolean withData, boolean split) throws IOException {
        LOGGER.info("--------------------------------------------");
        LOGGER.log(Level.INFO, "Test file: {0}", path);
        LOGGER.info("--------------------------------------------");
        Tree baseTree = LinkedListTreeTools.createBaseTree(
                path, StandardCharsets.UTF_8, reverse, withData);
        LOGGER.info("Creating base tree");

        LinkedListTree compressedTree;
        compressedTree = LinkedListTreeFactory.getInstance().createTree(baseTree, true, true,
                                                                        parallelMode);

        String resultPath;
        if (path.endsWith(".txt")) {
            resultPath = path.substring(0, path.length() - 4) + ".lztrie";
        } else {
            resultPath = path + ".lztrie";
        }

        LOGGER.log(Level.FINE, "Saving tree to file: {0}", resultPath);
        saveTree(compressedTree, resultPath);

        String result = "";
        for (int i = 1; i < 6; i++) {
            LOGGER.log(Level.FINE, "Checking traversal through the tree. pass {0}", i);
            checkTree(compressedTree, path,
                      StandardCharsets.UTF_8,
                      reverse, withData, false, split);

            LOGGER.log(Level.FINE, "Checking fast traversal through the tree. pass {0}", i);
            result = checkTree(compressedTree, path,
                               StandardCharsets.UTF_8,
                               reverse, withData, true, split);
        }
        return result;
    }

    /**
     * Implements invocation from the command line.
     * <p>
     * You can call LZTrie compression on neurpheus-utils.jar passing the location of a text file
     * containing test words. This methods reads all words and creates a compressed linked list tree
     * from it. This method also check the created tree searching for all words from the input file.
     * </p>
     *
     * <p>
     * Example usage:
     * <br>
     * <br>
     * java -jar neurpheus-utils.jar testfile.txt
     * <br>
     * <br>
     * ...this call will save the created tree in the testfile.lztrie file
     * </p>
     *
     * <p>
     * You can add the following flags changing behavior:
     * <ul>
     * <li>-parallel : use multi core, parallel compression (much faster on big files, worse
     * compression, indeterministic errors in the current implementation (you have to try produce
     * tree several times until validation pass correctly.</li>
     * <li>-withData : creates a tree with indexed words (tree contains integer values assigned to
     * final nodes of unique input words)</li>
     * <li>-reverse : creates a tree from words written in reverse order</li>
     * <li>-splitWords: checks traversal splitting words into two fragments.</li>
     * </ul>
     * </p>
     *
     * @param args Input arguments.
     */
    public static void main(String[] args) {
        try {
            String result = processCommandLine(args);
            if (result != null) {
                LOGGER.severe(result);
                System.exit(1);
            }
        } catch (IOException ex) {
            LOGGER.throwing(LinkedListTreeTools.class.getName(), "main", ex);
            System.exit(2);
        }
    }

    /**
     * Implements invocation from the command line.
     * <p>
     * You can call LZTrie compression on neurpheus-utils.jar passing the location of a text file
     * containing test words. This methods reads all words and creates a compressed linked list tree
     * from it. This method also check the created tree searching for all words from the input file.
     * </p>
     *
     * <p>
     * Example usage:
     * <br>
     * <br>
     * java -jar neurpheus-utils.jar testfile.txt
     * <br>
     * <br>
     * ...this call will save the created tree in the testfile.lztrie file
     * </p>
     *
     * <p>
     * You can add the following flags changing behavior:
     * <ul>
     * <li>-parallel : use multi core, parallel compression (much faster on big files, worse
     * compression, indeterministic errors in the current implementation (you have to try produce
     * tree several times until validation pass correctly.</li>
     * <li>-withData : creates a tree with indexed words (tree contains integer values assigned to
     * final nodes of unique input words)</li>
     * <li>-reverse : creates a tree from words written in reverse order</li>
     * <li>-splitWords: checks traversal splitting words into two fragments.</li>
     * </ul>
     * </p>
     *
     * @param args Input arguments.
     *
     * @return Information about an error.
     *
     * @throws IOException if any error occurred.
     */
    public static String processCommandLine(String[] args) throws IOException {
        String path = args[0];
        boolean parallelMode = false;
        boolean withData = false;
        boolean reverse = false;
        boolean splitWords = false;
        for (String argument : args) {
            switch (argument) {
                case "-parallel":
                    parallelMode = true;
                    break;
                case "-withData":
                    withData = true;
                    break;
                case "-reverse":
                    reverse = true;
                    break;
                case "-splitWords":
                    splitWords = true;
                    break;
                default:
                    break;
            }
        }
        return LinkedListTreeTools.testTreeCreation(
                path, reverse, parallelMode, withData, splitWords);
    }

}
