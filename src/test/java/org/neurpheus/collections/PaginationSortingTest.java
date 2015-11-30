/*
 *  © 2015 Jakub Strychowski
 */

package org.neurpheus.collections;

import net.trajano.commons.testing.UtilityClassTestUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Test methods of PaginationSorting class.
 * Unfortunately  we cannot define generic methods for primitives in Java 8, that's why
 * we have here huge number of test cases.
 * 
 * @author Jakub Strychowski
 */
public class PaginationSortingTest {
    
    
    private static final String TIME_MESSAGE = 
        "Pagination sorting algorithm sorted page containing %d elements in %d microseconds. %n";

    private static final String QUICKSORT_MESSAGE = 
        "Quicksort algorithm sorted array containing %d elements in %d microseconds. %n";

    public PaginationSortingTest() {
    }

    private static final int TEST_ARRAY_LENGTH = 5_000;

    private static final int NUMBER_OF_PAGES = 30;
    
    @Test(expected = NullPointerException.class)
    public void intArraySortPageArgumentsNullData() {
        int[] data = null;
        PaginationSorting.sortPage(data, 0, 10);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void intArraySortPageArgumentsIllegalRange1() {
        int[] data = new int[10];
        PaginationSorting.sortPage(data, -10, 10);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void intArraySortPageArgumentsIllegalRange2() {
        int[] data = new int[10];
        PaginationSorting.sortPage(data, 0, 0);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void intArraySortPageArgumentsIllegalRange3() {
        int[] data = new int[10];
        PaginationSorting.sortPage(data, 10, 0);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void intArraySortPageArgumentsIllegalRange4() {
        int[] data = new int[10];
        PaginationSorting.sortPage(data, 5, 5);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void intArraySortPageArgumentsIllegalRange5() {
        int[] data = new int[10];
        PaginationSorting.sortPage(data, 0, 10);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void intArraySortPageArgumentsIllegalRange6() {
        int[] data = new int[10];
        PaginationSorting.sortPage(data, 20, 30);
    }

    @Test
    public void intArraySortPageAlgorithmVerification() {

        int[] testArray = new int[TEST_ARRAY_LENGTH];
        for (int i = 0; i < TEST_ARRAY_LENGTH; i++) {
            int value = (int) Math.round((Math.random() * 2.0d - 1.0d) * Integer.MAX_VALUE);
            testArray[i] = value;
        }

        int[] testArrayCopy = Arrays.copyOf(testArray, TEST_ARRAY_LENGTH);

        long startTime = System.nanoTime();
        Arrays.sort(testArray);

        System.out.printf(
                QUICKSORT_MESSAGE,
                testArray.length, ((System.nanoTime() - startTime) / 1000));

        for (int j = NUMBER_OF_PAGES; j > 0; j--) {
            int left = (int) Math.round(Math.random() * TEST_ARRAY_LENGTH);
            if (left > TEST_ARRAY_LENGTH - 10) {
                left = TEST_ARRAY_LENGTH - 10;
            }
            int len = 10 + (int) Math.round(Math.random() * 50);
            int right = left + len;
            if (right >= TEST_ARRAY_LENGTH) {
                right = TEST_ARRAY_LENGTH - 1;
            }

            startTime = System.nanoTime();
            PaginationSorting.sortPage(testArrayCopy, left, right);
            System.out.printf(
                    TIME_MESSAGE,
                    right - left, ((System.nanoTime() - startTime) / 1000));

            for (int x = left; x < right; x++) {
                Assert.assertEquals(testArray[x], testArrayCopy[x]);
            }

        }

    }

    @Test(expected = NullPointerException.class)
    public void floatArraySortPageArgumentsNullData() {
        float[] data = null;
        PaginationSorting.sortPage(data, 0, 10);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void floatArraySortPageArgumentsIllegalRange1() {
        float[] data = new float[10];
        PaginationSorting.sortPage(data, -10, 10);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void floatArraySortPageArgumentsIllegalRange2() {
        float[] data = new float[10];
        PaginationSorting.sortPage(data, 0, 0);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void floatArraySortPageArgumentsIllegalRange3() {
        float[] data = new float[10];
        PaginationSorting.sortPage(data, 10, 0);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void floatArraySortPageArgumentsIllegalRange4() {
        float[] data = new float[10];
        PaginationSorting.sortPage(data, 5, 5);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void floatArraySortPageArgumentsIllegalRange5() {
        float[] data = new float[10];
        PaginationSorting.sortPage(data, 0, 10);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void floatArraySortPageArgumentsIllegalRange6() {
        float[] data = new float[10];
        PaginationSorting.sortPage(data, 20, 30);
    }

    @Test
    public void floatArraySortPageAlgorithmVerification() {

        float[] testArray = new float[TEST_ARRAY_LENGTH];
        for (int i = 0; i < TEST_ARRAY_LENGTH; i++) {
            float value = (float) (Math.random() * 2.0 - 1.0);
            testArray[i] = value;
        }

        float[] testArrayCopy = Arrays.copyOf(testArray, TEST_ARRAY_LENGTH);

        long startTime = System.nanoTime();
        Arrays.sort(testArray);

        System.out.printf(
                QUICKSORT_MESSAGE,
                testArray.length, ((System.nanoTime() - startTime) / 1000));

        for (int j = NUMBER_OF_PAGES; j > 0; j--) {
            int left = (int) Math.round(Math.random() * TEST_ARRAY_LENGTH);
            if (left > TEST_ARRAY_LENGTH - 10) {
                left = TEST_ARRAY_LENGTH - 10;
            }
            int len = 10 + (int) Math.round(Math.random() * 50);
            int right = left + len;
            if (right >= TEST_ARRAY_LENGTH) {
                right = TEST_ARRAY_LENGTH - 1;
            }

            startTime = System.nanoTime();
            PaginationSorting.sortPage(testArrayCopy, left, right);
            System.out.printf(
                    TIME_MESSAGE,
                    right - left, ((System.nanoTime() - startTime) / 1000));

            for (int x = left; x < right; x++) {
                Assert.assertEquals(testArray[x], testArrayCopy[x], 0);
            }

        }

    }

    @Test(expected = NullPointerException.class)
    public void doubleArraySortPageArgumentsNullData() {
        double[] data = null;
        PaginationSorting.sortPage(data, 0, 10);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void doubleArraySortPageArgumentsIllegalRange1() {
        double[] data = new double[10];
        PaginationSorting.sortPage(data, -10, 10);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void doubleArraySortPageArgumentsIllegalRange2() {
        double[] data = new double[10];
        PaginationSorting.sortPage(data, 0, 0);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void doubleArraySortPageArgumentsIllegalRange3() {
        double[] data = new double[10];
        PaginationSorting.sortPage(data, 10, 0);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void doubleArraySortPageArgumentsIllegalRange4() {
        double[] data = new double[10];
        PaginationSorting.sortPage(data, 5, 5);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void doubleArraySortPageArgumentsIllegalRange5() {
        double[] data = new double[10];
        PaginationSorting.sortPage(data, 0, 10);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void doubleArraySortPageArgumentsIllegalRange6() {
        double[] data = new double[10];
        PaginationSorting.sortPage(data, 20, 30);
    }

    @Test
    public void doubleArraySortPageAlgorithmVerification() {

        double[] testArray = new double[TEST_ARRAY_LENGTH];
        for (int i = 0; i < TEST_ARRAY_LENGTH; i++) {
            double value = (double) (Math.random() * 2.0 - 1.0);
            testArray[i] = value;
        }

        double[] testArrayCopy = Arrays.copyOf(testArray, TEST_ARRAY_LENGTH);

        long startTime = System.nanoTime();
        Arrays.sort(testArray);

        System.out.printf(
                QUICKSORT_MESSAGE,
                testArray.length, ((System.nanoTime() - startTime) / 1000));

        for (int j = NUMBER_OF_PAGES; j > 0; j--) {
            int left = (int) Math.round(Math.random() * TEST_ARRAY_LENGTH);
            if (left > TEST_ARRAY_LENGTH - 10) {
                left = TEST_ARRAY_LENGTH - 10;
            }
            int len = 10 + (int) Math.round(Math.random() * 50);
            int right = left + len;
            if (right >= TEST_ARRAY_LENGTH) {
                right = TEST_ARRAY_LENGTH - 1;
            }

            startTime = System.nanoTime();
            PaginationSorting.sortPage(testArrayCopy, left, right);
            System.out.printf(
                    TIME_MESSAGE,
                    right - left, ((System.nanoTime() - startTime) / 1000));

            for (int x = left; x < right; x++) {
                Assert.assertEquals(testArray[x], testArrayCopy[x], 0);
            }

        }

    }

    @Test(expected = NullPointerException.class)
    public void shortArraySortPageArgumentsNullData() {
        short[] data = null;
        PaginationSorting.sortPage(data, 0, 10);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void shortArraySortPageArgumentsIllegalRange1() {
        short[] data = new short[10];
        PaginationSorting.sortPage(data, -10, 10);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void shortArraySortPageArgumentsIllegalRange2() {
        short[] data = new short[10];
        PaginationSorting.sortPage(data, 0, 0);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void shortArraySortPageArgumentsIllegalRange3() {
        short[] data = new short[10];
        PaginationSorting.sortPage(data, 10, 0);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void shortArraySortPageArgumentsIllegalRange4() {
        short[] data = new short[10];
        PaginationSorting.sortPage(data, 5, 5);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void shortArraySortPageArgumentsIllegalRange5() {
        short[] data = new short[10];
        PaginationSorting.sortPage(data, 0, 10);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void shortArraySortPageArgumentsIllegalRange6() {
        short[] data = new short[10];
        PaginationSorting.sortPage(data, 20, 30);
    }

    @Test
    public void shortArraySortPageAlgorithmVerification() {

        short[] testArray = new short[TEST_ARRAY_LENGTH];
        for (int i = 0; i < TEST_ARRAY_LENGTH; i++) {
            short value = (short) Math.round((Math.random() * 2.0d - 1.0d) * Short.MAX_VALUE);
            testArray[i] = value;
        }

        short[] testArrayCopy = Arrays.copyOf(testArray, TEST_ARRAY_LENGTH);

        long startTime = System.nanoTime();
        Arrays.sort(testArray);

        System.out.printf(
                QUICKSORT_MESSAGE,
                testArray.length, ((System.nanoTime() - startTime) / 1000));

        for (int j = NUMBER_OF_PAGES; j > 0; j--) {
            int left = (int) Math.round(Math.random() * TEST_ARRAY_LENGTH);
            if (left > TEST_ARRAY_LENGTH - 10) {
                left = TEST_ARRAY_LENGTH - 10;
            }
            int len = 10 + (int) Math.round(Math.random() * 50);
            int right = left + len;
            if (right >= TEST_ARRAY_LENGTH) {
                right = TEST_ARRAY_LENGTH - 1;
            }

            startTime = System.nanoTime();
            PaginationSorting.sortPage(testArrayCopy, left, right);
            System.out.printf(
                    TIME_MESSAGE,
                    right - left, ((System.nanoTime() - startTime) / 1000));

            for (int x = left; x < right; x++) {
                Assert.assertEquals(testArray[x], testArrayCopy[x]);
            }

        }

    }

    @Test(expected = NullPointerException.class)
    public void longArraySortPageArgumentsNullData() {
        long[] data = null;
        PaginationSorting.sortPage(data, 0, 10);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void longArraySortPageArgumentsIllegalRange1() {
        long[] data = new long[10];
        PaginationSorting.sortPage(data, -10, 10);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void longArraySortPageArgumentsIllegalRange2() {
        long[] data = new long[10];
        PaginationSorting.sortPage(data, 0, 0);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void longArraySortPageArgumentsIllegalRange3() {
        long[] data = new long[10];
        PaginationSorting.sortPage(data, 10, 0);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void longArraySortPageArgumentsIllegalRange4() {
        long[] data = new long[10];
        PaginationSorting.sortPage(data, 5, 5);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void longArraySortPageArgumentsIllegalRange5() {
        long[] data = new long[10];
        PaginationSorting.sortPage(data, 0, 10);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void longArraySortPageArgumentsIllegalRange6() {
        long[] data = new long[10];
        PaginationSorting.sortPage(data, 20, 30);
    }

    @Test
    public void longArraySortPageAlgorithmVerification() {

        long[] testArray = new long[TEST_ARRAY_LENGTH];
        for (int i = 0; i < TEST_ARRAY_LENGTH; i++) {
            long value = (long) Math.round((Math.random() * 2.0d - 1.0d) * Long.MAX_VALUE);
            testArray[i] = value;
        }

        long[] testArrayCopy = Arrays.copyOf(testArray, TEST_ARRAY_LENGTH);

        long startTime = System.nanoTime();
        Arrays.sort(testArray);

        System.out.printf(
                QUICKSORT_MESSAGE,
                testArray.length, ((System.nanoTime() - startTime) / 1000));

        for (int j = NUMBER_OF_PAGES; j > 0; j--) {
            int left = (int) Math.round(Math.random() * TEST_ARRAY_LENGTH);
            if (left > TEST_ARRAY_LENGTH - 10) {
                left = TEST_ARRAY_LENGTH - 10;
            }
            int len = 10 + (int) Math.round(Math.random() * 50);
            int right = left + len;
            if (right >= TEST_ARRAY_LENGTH) {
                right = TEST_ARRAY_LENGTH - 1;
            }

            startTime = System.nanoTime();
            PaginationSorting.sortPage(testArrayCopy, left, right);
            System.out.printf(
                    TIME_MESSAGE,
                    right - left, ((System.nanoTime() - startTime) / 1000));

            for (int x = left; x < right; x++) {
                Assert.assertEquals(testArray[x], testArrayCopy[x]);
            }

        }

    }

    
    
    @Test(expected = NullPointerException.class)
    public void charArraySortPageArgumentsNullData() {
        char[] data = null;
        PaginationSorting.sortPage(data, 0, 10);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void charArraySortPageArgumentsIllegalRange1() {
        char[] data = new char[10];
        PaginationSorting.sortPage(data, -10, 10);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void charArraySortPageArgumentsIllegalRange2() {
        char[] data = new char[10];
        PaginationSorting.sortPage(data, 0, 0);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void charArraySortPageArgumentsIllegalRange3() {
        char[] data = new char[10];
        PaginationSorting.sortPage(data, 10, 0);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void charArraySortPageArgumentsIllegalRange4() {
        char[] data = new char[10];
        PaginationSorting.sortPage(data, 5, 5);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void charArraySortPageArgumentsIllegalRange5() {
        char[] data = new char[10];
        PaginationSorting.sortPage(data, 0, 10);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void charArraySortPageArgumentsIllegalRange6() {
        char[] data = new char[10];
        PaginationSorting.sortPage(data, 20, 30);
    }

    @Test
    public void charArraySortPageAlgorithmVerification() {

        char[] testArray = new char[TEST_ARRAY_LENGTH];
        for (int i = 0; i < TEST_ARRAY_LENGTH; i++) {
            char value = (char) Math.round(Math.random() * Character.MAX_VALUE);
            testArray[i] = value;
        }

        char[] testArrayCopy = Arrays.copyOf(testArray, TEST_ARRAY_LENGTH);

        long startTime = System.nanoTime();
        Arrays.sort(testArray);

        System.out.printf(
                QUICKSORT_MESSAGE,
                testArray.length, ((System.nanoTime() - startTime) / 1000));

        for (int j = NUMBER_OF_PAGES; j > 0; j--) {
            int left = (int) Math.round(Math.random() * TEST_ARRAY_LENGTH);
            if (left > TEST_ARRAY_LENGTH - 10) {
                left = TEST_ARRAY_LENGTH - 10;
            }
            int len = 10 + (int) Math.round(Math.random() * 50);
            int right = left + len;
            if (right >= TEST_ARRAY_LENGTH) {
                right = TEST_ARRAY_LENGTH - 1;
            }

            startTime = System.nanoTime();
            PaginationSorting.sortPage(testArrayCopy, left, right);
            System.out.printf(
                    TIME_MESSAGE,
                    right - left, ((System.nanoTime() - startTime) / 1000));

            for (int x = left; x < right; x++) {
                Assert.assertEquals(testArray[x], testArrayCopy[x]);
            }

        }

    }
    
    @Test(expected = NullPointerException.class)
    public void stringArraySortPageArgumentsNullData() {
        String[] data = null;
        PaginationSorting.sortPage(data, 0, 10);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void stringArraySortPageArgumentsIllegalRange1() {
        String[] data = new String[10];
        PaginationSorting.sortPage(data, -10, 10);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void stringArraySortPageArgumentsIllegalRange2() {
        String[] data = new String[10];
        PaginationSorting.sortPage(data, 0, 0);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void stringArraySortPageArgumentsIllegalRange3() {
        String[] data = new String[10];
        PaginationSorting.sortPage(data, 10, 0);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void stringArraySortPageArgumentsIllegalRange4() {
        String[] data = new String[10];
        PaginationSorting.sortPage(data, 5, 5);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void stringArraySortPageArgumentsIllegalRange5() {
        String[] data = new String[10];
        PaginationSorting.sortPage(data, 0, 10);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void stringArraySortPageArgumentsIllegalRange6() {
        String[] data = new String[10];
        PaginationSorting.sortPage(data, 20, 30);
    }

    @Test
    public void stringArraySortPageAlgorithmVerification() {

        String[] testArray = new String[TEST_ARRAY_LENGTH];
        StringBuilder builder = new StringBuilder(31);
        for (int i = 0; i < TEST_ARRAY_LENGTH; i++) {
            builder.setLength(0);
            int strLen = (int) Math.round((Math.random() * 1.0d) * 30);
            for (int j = 0; j < strLen; j++) {
                builder.append((char) (Math.random() * 20 + 'a'));
            }
            testArray[i] = builder.toString();
        }

        String[] testArrayCopy = Arrays.copyOf(testArray, TEST_ARRAY_LENGTH);

        long startTime = System.nanoTime();
        Arrays.sort(testArray);

        System.out.printf(
                QUICKSORT_MESSAGE,
                testArray.length, ((System.nanoTime() - startTime) / 1000));

        for (int j = NUMBER_OF_PAGES; j > 0; j--) {
            int left = (int) Math.round(Math.random() * TEST_ARRAY_LENGTH);
            if (left > TEST_ARRAY_LENGTH - 10) {
                left = TEST_ARRAY_LENGTH - 10;
            }
            int len = 10 + (int) Math.round(Math.random() * 50);
            int right = left + len;
            if (right >= TEST_ARRAY_LENGTH) {
                right = TEST_ARRAY_LENGTH - 1;
            }

            startTime = System.nanoTime();
            PaginationSorting.sortPage(testArrayCopy, left, right);
            System.out.printf(
                TIME_MESSAGE,
                right - left, ((System.nanoTime() - startTime) / 1000));

            for (int x = left; x < right; x++) {
                Assert.assertEquals(testArray[x], testArrayCopy[x]);
            }

        }

    }
    
    protected class TestComparator implements Comparator {
        
        @Override
        public int compare(Object objA, Object objB) {
            String strA = (String) objA;
            String strB = (String) objB;
            int result = -1 * strA.compareTo(strB);
            return result;
        }
        
    }

    
    @Test(expected = NullPointerException.class)
    public void stringArraySortPageArgumentsNullDataWitchComparator() {
        String[] data = null;
        Comparator comp = new TestComparator();
        PaginationSorting.sortPage(data, comp, 0, 10);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void stringArraySortPageArgumentsIllegalRange1WitchComparator() {
        String[] data = new String[10];
        Comparator comp = new TestComparator();
        PaginationSorting.sortPage(data, comp, -10, 10);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void stringArraySortPageArgumentsIllegalRange2WitchComparator() {
        String[] data = new String[10];
        Comparator comp = new TestComparator();
        PaginationSorting.sortPage(data, comp, 0, 0);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void stringArraySortPageArgumentsIllegalRange3WitchComparator() {
        String[] data = new String[10];
        Comparator comp = new TestComparator();
        PaginationSorting.sortPage(data, comp, 10, 0);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void stringArraySortPageArgumentsIllegalRange4WitchComparator() {
        String[] data = new String[10];
        Comparator comp = new TestComparator();
        PaginationSorting.sortPage(data, comp, 5, 5);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void stringArraySortPageArgumentsIllegalRange5WitchComparator() {
        String[] data = new String[10];
        Comparator comp = new TestComparator();
        PaginationSorting.sortPage(data, comp, 0, 10);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void stringArraySortPageArgumentsIllegalRange6WitchComparator() {
        String[] data = new String[10];
        Comparator comp = new TestComparator();
        PaginationSorting.sortPage(data, comp, 20, 30);
    }

    @Test
    public void stringArraySortPageAlgorithmVerificationWitchComparator() {

        String[] testArray = new String[TEST_ARRAY_LENGTH];
        StringBuilder builder = new StringBuilder(31);
        for (int i = 0; i < TEST_ARRAY_LENGTH; i++) {
            builder.setLength(0);
            int strLen = (int) Math.round((Math.random() * 1.0d) * 30);
            for (int j = 0; j < strLen; j++) {
                builder.append((char) (Math.random() * 20 + 'a'));
            }
            testArray[i] = builder.toString();
        }

        String[] testArrayCopy = Arrays.copyOf(testArray, TEST_ARRAY_LENGTH);

        Comparator comp = new TestComparator();
        
        long startTime = System.nanoTime();
        Arrays.sort(testArray, comp);

        System.out.printf(
                QUICKSORT_MESSAGE,
                testArray.length, ((System.nanoTime() - startTime) / 1000));

        for (int j = NUMBER_OF_PAGES; j > 0; j--) {
            int left = (int) Math.round(Math.random() * TEST_ARRAY_LENGTH);
            if (left > TEST_ARRAY_LENGTH - 10) {
                left = TEST_ARRAY_LENGTH - 10;
            }
            int len = 10 + (int) Math.round(Math.random() * 50);
            int right = left + len;
            if (right >= TEST_ARRAY_LENGTH) {
                right = TEST_ARRAY_LENGTH - 1;
            }

            startTime = System.nanoTime();
            PaginationSorting.sortPage(testArrayCopy, comp, left, right);
            System.out.printf(
                TIME_MESSAGE,
                right - left, ((System.nanoTime() - startTime) / 1000));

            for (int x = left; x < right; x++) {
                Assert.assertEquals(testArray[x], testArrayCopy[x]);
            }

        }

    }

    
    
    @Test(expected = NullPointerException.class)
    public void listSortPageArgumentsNullData() {
        List<String> data = null;
        PaginationSorting.sortPage(data, 0, 10);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void listSortPageArgumentsIllegalRange1() {
        List<String> data = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            data.add("test" + i);
        }
        PaginationSorting.sortPage(data, -10, 10);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void listSortPageArgumentsIllegalRange2() {
        List<String> data = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            data.add("test" + i);
        }
        PaginationSorting.sortPage(data, 0, 0);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void listSortPageArgumentsIllegalRange3() {
        List<String> data = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            data.add("test" + i);
        }
        PaginationSorting.sortPage(data, 10, 0);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void listSortPageArgumentsIllegalRange4() {
        List<String> data = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            data.add("test" + i);
        }
        PaginationSorting.sortPage(data, 5, 5);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void listSortPageArgumentsIllegalRange5() {
        List<String> data = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            data.add("test" + i);
        }
        PaginationSorting.sortPage(data, 0, 10);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void listSortPageArgumentsIllegalRange6() {
        List<String> data = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            data.add("test" + i);
        }
        PaginationSorting.sortPage(data, 20, 30);
    }

    @Test
    public void listSortPageAlgorithmVerification() {

        List<String> testList = new ArrayList<>(TEST_ARRAY_LENGTH);
        StringBuilder builder = new StringBuilder(31);
        for (int i = 0; i < TEST_ARRAY_LENGTH; i++) {
            builder.setLength(0);
            int strLen = (int) Math.round((Math.random() * 1.0d) * 30);
            for (int j = 0; j < strLen; j++) {
                builder.append((char) (Math.random() * 20 + 'a'));
            }
            testList.add(builder.toString());
        }

        List<String> testListCopy = new ArrayList<>(testList);

        long startTime = System.nanoTime();
        Collections.sort(testList);

        System.out.printf(
                QUICKSORT_MESSAGE,
                testList.size(), ((System.nanoTime() - startTime) / 1000));

        for (int j = NUMBER_OF_PAGES; j > 0; j--) {
            int left = (int) Math.round(Math.random() * TEST_ARRAY_LENGTH);
            if (left > TEST_ARRAY_LENGTH - 10) {
                left = TEST_ARRAY_LENGTH - 10;
            }
            int len = 10 + (int) Math.round(Math.random() * 50);
            int right = left + len;
            if (right >= TEST_ARRAY_LENGTH) {
                right = TEST_ARRAY_LENGTH - 1;
            }

            startTime = System.nanoTime();
            PaginationSorting.sortPage(testListCopy, left, right);
            System.out.printf(
                TIME_MESSAGE,
                right - left, ((System.nanoTime() - startTime) / 1000));

            for (int x = left; x < right; x++) {
                Assert.assertEquals(testList.get(x), testListCopy.get(x));
            }

        }

    }

    @Test(expected = NullPointerException.class)
    public void listSortPageArgumentsNullDataWithComparator() {
        List<String> data = null;
        PaginationSorting.sortPage(data, new TestComparator(), 0, 10);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void listSortPageArgumentsIllegalRange1WithComparator() {
        List<String> data = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            data.add("test" + i);
        }
        PaginationSorting.sortPage(data, new TestComparator(), -10, 10);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void listSortPageArgumentsIllegalRange2WithComparator() {
        List<String> data = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            data.add("test" + i);
        }
        PaginationSorting.sortPage(data, new TestComparator(), 0, 0);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void listSortPageArgumentsIllegalRange3WithComparator() {
        List<String> data = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            data.add("test" + i);
        }
        PaginationSorting.sortPage(data, 10, 0);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void listSortPageArgumentsIllegalRange4WithComparator() {
        List<String> data = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            data.add("test" + i);
        }
        PaginationSorting.sortPage(data, new TestComparator(), 5, 5);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void listSortPageArgumentsIllegalRange5WithComparator() {
        List<String> data = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            data.add("test" + i);
        }
        PaginationSorting.sortPage(data, new TestComparator(), 0, 10);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void listSortPageArgumentsIllegalRange6WithComparator() {
        List<String> data = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            data.add("test" + i);
        }
        PaginationSorting.sortPage(data, 20, 30);
    }

    @Test
    public void listSortPageAlgorithmVerificationWithComparator() {

        List<String> testList = new ArrayList<>(TEST_ARRAY_LENGTH);
        StringBuilder builder = new StringBuilder(31);
        for (int i = 0; i < TEST_ARRAY_LENGTH; i++) {
            builder.setLength(0);
            int strLen = (int) Math.round((Math.random() * 1.0d) * 30);
            for (int j = 0; j < strLen; j++) {
                builder.append((char) (Math.random() * 20 + 'a'));
            }
            testList.add(builder.toString());
        }

        List<String> testListCopy = new ArrayList<>(testList);

        long startTime = System.nanoTime();
        Collections.sort(testList, new TestComparator());

        System.out.printf(
                QUICKSORT_MESSAGE,
                testList.size(), ((System.nanoTime() - startTime) / 1000));

        for (int j = NUMBER_OF_PAGES; j > 0; j--) {
            int left = (int) Math.round(Math.random() * TEST_ARRAY_LENGTH);
            if (left > TEST_ARRAY_LENGTH - 10) {
                left = TEST_ARRAY_LENGTH - 10;
            }
            int len = 10 + (int) Math.round(Math.random() * 50);
            int right = left + len;
            if (right >= TEST_ARRAY_LENGTH) {
                right = TEST_ARRAY_LENGTH - 1;
            }

            startTime = System.nanoTime();
            PaginationSorting.sortPage(testListCopy, new TestComparator(), left, right);
            System.out.printf(
                TIME_MESSAGE,
                right - left, ((System.nanoTime() - startTime) / 1000));

            for (int x = left; x < right; x++) {
                Assert.assertEquals(testList.get(x), testListCopy.get(x));
            }

        }

    }
    
    
    @Test
    public void testUtilityClass() throws ReflectiveOperationException {
        UtilityClassTestUtil.assertUtilityClassWellDefined(PaginationSorting.class);
    }
    
}
