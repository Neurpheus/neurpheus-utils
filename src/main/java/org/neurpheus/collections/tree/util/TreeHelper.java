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

package org.neurpheus.collections.tree.util;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import org.neurpheus.collections.tree.Tree;
import org.neurpheus.collections.tree.TreeNodeWithData;
import org.neurpheus.collections.tree.TreeNode;
import org.neurpheus.collections.tree.linkedlist.LinkedListTree;
import org.neurpheus.collections.tree.linkedlist.LinkedListTreeUnit;
import org.neurpheus.collections.tree.linkedlist.LinkedListTreeUnitArray;

/**
 *
 * @author szkoleniowy
 */
public class TreeHelper {

    public static void printTree(Tree tree, PrintStream out) throws IOException {
        printTree(tree, out, new DefaultNodePrinter());
    }

    public static void printTreeUnits(LinkedListTree tree, PrintStream out) throws IOException {
        StringBuffer tmp = new StringBuffer();
        LinkedListTreeUnitArray unitArray = tree.getUnitArray();
        for (int i = 0; i < unitArray.size(); i++) {
            LinkedListTreeUnit unit = unitArray.get(i);
            if (i >= 0) {
                tmp.append('|');
                tmp.append(i);
                tmp.append(':');
                if (unit.isAbsolutePointer()) {
                    tmp.append('[');
                    tmp.append(unit.getDistance());
                    tmp.append(',');
                    tmp.append(unit.getValueCode());
                    tmp.append(']');
                } else {
                    tmp.append((byte) unit.getValueCode());
                    if (unit.getDistance() > 0) {
                        tmp.append('(');
                        tmp.append(unit.getDistance());
                        tmp.append(')');
                    }
                    if (unit.isWordEnd()) {
                        tmp.append('*');
                    }
                    if (unit.isWordContinued()) {
                        tmp.append('>');
                    }
                }

            }
        }
        out.println(tmp.toString());
    }

    public static void printTree(Tree tree, PrintStream out, TreeNodePrinter nodePrinter) throws
            IOException {
        for (Iterator it = tree.getRoot().getChildren().iterator(); it.hasNext();) {
            printNode((TreeNode) it.next(), out, 0, nodePrinter);
        }
        out.println(
                "---------------------------------------------------------------------------------------------");
        printTreeWords(tree, out, nodePrinter);
    }

    public static void printTree(Tree tree, String outPath, TreeNodePrinter nodePrinter) throws
            IOException {
        PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(outPath)),
                                          false, "UTF-8");
        try {
            printTree(tree, out, nodePrinter);
        } finally {
            out.close();
        }
    }

    public static void printTreeWords(Tree tree, PrintStream out, TreeNodePrinter nodePrinter)
            throws IOException {
        for (Iterator it = tree.getRoot().getChildren().iterator(); it.hasNext();) {
            printWords((TreeNode) it.next(), out, "", nodePrinter);
        }
    }

    private static void printWords(TreeNode node, PrintStream out, String prefix,
                                   TreeNodePrinter nodePrinter) {
        String tmp = prefix + nodePrinter.getValueString(node.getValue());
        if (node.isLeaf() || node.hasExtraData()) {
            out.println(tmp);
        }
        for (Iterator it = node.getChildren().iterator(); it.hasNext();) {
            printWords((TreeNode) it.next(), out, tmp, nodePrinter);
        }
    }

    private static void printNode(TreeNode node, PrintStream out, int ident,
                                  TreeNodePrinter nodePrinter) throws IOException {
        out.println();
        for (int i = 0; i <= ident; i++) {
            out.print("\t");
        }
        out.print(nodePrinter.getValueString(node.getValue()));
        if (node.hasExtraData()) {
            out.print(" : ");
            out.print(nodePrinter.getDataString(((TreeNodeWithData) node).getData()));
        }
        for (Iterator it = node.getChildren().iterator(); it.hasNext();) {
            printNode((TreeNode) it.next(), out, ident + 1, nodePrinter);
        }
    }

    public static Collection getTreeWords(Tree tree, TreeNodePrinter nodePrinter) {
        Collection result = new HashSet();
        for (Iterator it = tree.getRoot().getChildren().iterator(); it.hasNext();) {
            getWords((TreeNode) it.next(), result, "", nodePrinter);
        }
        return result;
    }

    private static void getWords(TreeNode node, Collection result, String prefix,
                                 TreeNodePrinter nodePrinter) {
        String tmp = prefix + nodePrinter.getValueString(node.getValue());
        if (node.hasExtraData() || node.isLeaf()) {
            result.add(tmp);
        }
        for (Iterator it = node.getChildren().iterator(); it.hasNext();) {
            getWords((TreeNode) it.next(), result, tmp, nodePrinter);
        }
    }

}
