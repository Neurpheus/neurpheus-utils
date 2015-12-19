/*
 *  © 2015 Jakub Strychowski
 */

package org.neurpheus.collections.tree.linkedlist;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.neurpheus.collections.tree.Tree;
import org.neurpheus.collections.tree.TreeNode;
import org.neurpheus.logging.LoggerService;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 *
 * @author Kuba
 */
public class LinkedListTreeFactoryTest {

    private static final char[] SUFFIX_ALPHABET = new char[] { 'a', 'e', 'z', 'x', 'c' ,'v' ,'b', 'n', 'm'};
    private static final char[] THEME_ALPHABET = new char[] { 'a', 'e', 's', 'd' ,'f' ,'g', 'h', 'j', 'k', 'l'};
    private static final char[] PREFIX_ALPHABET = new char[] { 'w', 'e', 'r', 't', 'y', 'u'};
    
    private static final int NUMBER_OF_SUFFIXES = 200;
    private static final int NUMBER_OF_THEMES = 20_000;
    private static final int NUMBER_OF_PREFIXES = 10;
    
    private static final int NUMBER_OF_RANDOM_WORDS = 300_000;
    private static final int NUMBER_OF_WORDS = 10_000_000;
    
    private static final boolean REVERSE = false;
    
    private static final String[] EXAMPLES = new String[] {
      "wysoki",
      "wysokiego",
      "wysokiemu",
      "najwyzszemu",
      "wysoka",
      "wysocy",
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
    };
    
    public LinkedListTreeFactoryTest() {
    }

    /**
     * Test of getInstance method, of class LinkedListTreeFactory.
     */
    //@Test
    public void testCompressSimpleExamples() {
        LoggerService.setLogLevelForConsole(Level.FINER);
        List<String> examples = Arrays.asList(EXAMPLES);
        testExamples(examples, false);
        testExamples(examples, true);
    }
    
    /**
     * Test of getInstance method, of class LinkedListTreeFactory.
     */
    @Test
    public void testCompressRandomExamples() {
        LoggerService.setLogLevelForConsole(Level.FINER);
        List<String> examples = generateExamples();
        testExamples(examples, false);
    }
    
    
    /**
     * Test of getInstance method, of class LinkedListTreeFactory.
     */
    //@Test
    public void testCompress3() {
        LoggerService.setLogLevelForConsole(Level.FINER);
        
        
        URL url = ClassLoader.getSystemResource("");
        File folder = null;
        try {
            folder = new File(url.toURI());
        } catch (URISyntaxException ex) {
            fail(ex.getMessage());
        }
        String resourcePath = folder.getAbsolutePath() + File.separator;

        String path = resourcePath + "english.txt";
        
        //path = "c:/projekty/neurpheus/neurpheus-utils/data/full_pl_PL.all";
        //path = "c:/projekty/neurpheus/neurpheus-utils/data/en_GB.all";
        //path = "c:/projekty/neurpheus/neurpheus-utils/data/data-sets/weiss/wikipedia.txt";
        //path = "c:/projekty/neurpheus/neurpheus-utils/data/data-sets/weiss/polish.txt";
        //path = "c:/projekty/neurpheus/neurpheus-utils/data/data-sets/weiss/random.txt";
        
         

        try {
            String result = LinkedListTreeTools.testTreeCreation(path, REVERSE, true);
            if (result != null) {
                fail(result);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            fail("IOException: " + ex.getMessage());
        }
        
    }
    
    
    private void testExamples(List<String> examples, boolean parallelMode) {
        System.out.println("--------------------------------------------");
        System.out.printf("Test %d words%n", examples.size());
        System.out.println("--------------------------------------------");
        Tree baseTree = LinkedListTreeTools.createBaseTree(examples, REVERSE);
        Tree compressedTree = LinkedListTreeFactory.getInstance().createTree(baseTree, true, true, parallelMode);
        
        for (String example : examples) {
            TreeNode node = LinkedListTreeTools.findNode(example, compressedTree, REVERSE);
            assertTrue("Cannot find string: " + example, node != null);
        }
    }
    
    
    private List<String> generateExamples() {
        
        List<String> suffixes = generateStrings(SUFFIX_ALPHABET, 0, 4, NUMBER_OF_SUFFIXES);
        List<String> themes = generateStrings(THEME_ALPHABET, 1, 8, NUMBER_OF_THEMES);
        List<String> prefixes = generateStrings(PREFIX_ALPHABET, 1, 8, NUMBER_OF_PREFIXES);
        
        Set<String> result = new HashSet<>();
        
        int i = NUMBER_OF_RANDOM_WORDS;
        while (i > 0) {
            String theme = themes.get((int) Math.floor(Math.random() * themes.size()));
            String suffix = suffixes.get((int) Math.floor(Math.random() * suffixes.size()));
            String prefix = prefixes.get((int) Math.floor(Math.random() * prefixes.size()));
            String str = prefix + theme + suffix;
            if (result.add(str)) {
                i--;
            }
        }
        
        return new ArrayList(result);
        
    }
    
    private List<String> generateStrings(char[] alphabet, int minLength, int  maxLength, 
                                                                        int numberOfStrings) {
        Set<String> result = new HashSet<String>();
        double lengthRange = maxLength - minLength;
        int alphabetSize = alphabet.length;
        StringBuilder builder = new StringBuilder();
        int i = numberOfStrings;
        while (i > 0) {
            int len = minLength + (int) Math.floor(Math.random() * lengthRange);
            builder.setLength(0);
            for (int j = 0; j < len; j++) {
                char ch = alphabet[(int) Math.floor(Math.random() * alphabetSize)];
                builder.append(ch);
            }
            String str = builder.toString();
            if (result.add(str)) {
                i--;
            }
        }
        return new ArrayList(result);
    }

    
}
