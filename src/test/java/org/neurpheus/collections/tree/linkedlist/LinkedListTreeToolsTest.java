/*
 *  © 2015 Jakub Strychowski
 */

package org.neurpheus.collections.tree.linkedlist;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jakub Strychowski
 */
public class LinkedListTreeToolsTest {
    
    public LinkedListTreeToolsTest() {
    }
    
    private static String filePath;
    private final static Charset charset = StandardCharsets.UTF_8;
    
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
    
    @BeforeClass
    public static void setUpClass() {
        URL url = ClassLoader.getSystemResource("");
        File folder = null;
        try {
            folder = new File(url.toURI());
        } catch (URISyntaxException ex) {
            fail(ex.getMessage());
        }
        String resourcePath = folder.getAbsolutePath() + File.separator;

        filePath = resourcePath + "english.txt";
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    @Test
    public void testMainMethod() {
        testMain(false, false, false, false);
        testMain(false, false, true, false);
        testMain(false, true, false, false);
        testMain(false, true, true, false);
        testMain(false, false, false, true);
        testMain(false, false, true, true);
        testMain(false, true, false, true);
        testMain(false, true, true, true);
        testMain(true, false, true, true);
    }
    
    private void testMain(boolean parallel, boolean reverse, boolean withData, boolean splitWord) {
        try {
            List<String> argsList = new ArrayList<>();
            argsList.add(filePath);
            if (parallel) {
                argsList.add("-parallel");
            }
            if (reverse) {
                argsList.add("-reverse");
            }
            if (withData) {
                argsList.add("-withData");
            }
            if (splitWord) {
                argsList.add("-splitWord");
            }
            String[] args = argsList.toArray(new String[argsList.size()]);
            assertNull(LinkedListTreeTools.processCommandLine(args));
            LinkedListTreeTools.main(args);
        } catch (IOException ex) {
            fail(ex.getMessage());
        }
    }
    

    /*
    @Test
    public void testFindNode_3args() {
        System.out.println("findNode");
        String example = "";
        Tree tree = null;
        boolean reverse = false;
        TreeNode expResult = null;
        TreeNode result = LinkedListTreeTools.findNode(example, tree, reverse);
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    @Test
    public void testFindNode_4args() {
        System.out.println("findNode");
        String example = "";
        LinkedListTree tree = null;
        boolean reverse = false;
        int[] stack = null;
        TreeNode expResult = null;
        TreeNode result = LinkedListTreeTools.findNode(example, tree, reverse, stack);
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    @Test
    public void testSaveTree() throws Exception {
        System.out.println("saveTree");
        Tree tree = null;
        String path = "";
        LinkedListTreeTools.saveTree(tree, path);
        fail("The test case is a prototype.");
    }

    @Test
    public void testTestTreeCreation() throws Exception {
        System.out.println("testTreeCreation");
        String path = "";
        boolean reverse = false;
        boolean parallelMode = false;
        boolean withData = false;
        String expResult = "";
        String result = LinkedListTreeTools.testTreeCreation(path, reverse, parallelMode, withData);
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    @Test
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        LinkedListTreeTools.main(args);
        fail("The test case is a prototype.");
    }
    */
    
}
