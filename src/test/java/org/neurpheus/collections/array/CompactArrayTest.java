/*
 *  © 2015 Jakub Strychowski
 */

package org.neurpheus.collections.array;

import com.carrotsearch.sizeof.RamUsageEstimator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Kuba
 */
public class CompactArrayTest {
    
    public CompactArrayTest() {
    }


    @Test
    public void testDetermineNumberOfBits() {
        long value = 1;
        for (int i = 1; i < 64; i++) {
            assertEquals(i, CompactArray.determineNumberOfBits(value));
            value <<= 1;
        }
        assertEquals(1, CompactArray.determineNumberOfBits(0));
    }
    
    int numberOfElements = 1024 * 50;
    long[] expected = new long[numberOfElements];
    
    public long createAndCheckArrayLong(long maxValue) {

        
        for (int i = 0; i < numberOfElements; i++) {
            long value = Math.round(Math.random() * maxValue);
            expected[i] = value;
        }

        long startTime = System.nanoTime();
        
        //CompactArray ca = new CompactArray(numberOfElements, maxValue);
        CompactArray ca = new CompactArray();
        
        for (int i = 0; i < numberOfElements; i++) {
            ca.addLongValue(expected[i]);
        }

        ca.compact();

        int log2MaxValue = 64 - Long.numberOfLeadingZeros(maxValue - 1);

        for (int i = 0; i < numberOfElements; i++) {
            assertEquals(expected[i], ca.getLongValue(i));
        }

        ca.setLongValue(numberOfElements / 2, maxValue);

        assertEquals(maxValue, ca.getLongValue(numberOfElements / 2));

        long duration = (System.nanoTime() - startTime) / 1000;

        long allocation = RamUsageEstimator.sizeOf(ca);


        System.out.printf(
            "compact array test (numberOfElements = %d; maxValue = %d) :%n"
            + "        duration = %d microsencods; allocated RAM = %d B; allocation per element = %f B %n", 
            numberOfElements, maxValue, duration, allocation, ((float) allocation) / numberOfElements);
        
        
        return duration;
    }
    
    @Test
    public void testStorageLong() {

        // warm up
        System.out.println("---- warming up -----");
        createAndCheckArrayLong(1);
        createAndCheckArrayLong(2);
        createAndCheckArrayLong(4);
        createAndCheckArrayLong(1);

        System.out.println("---- ready -----");
        
        long sumDuration = 0;
        for (long maxValue = 1; maxValue < Long.MAX_VALUE / 10; maxValue = maxValue << 1) {
            long duration = createAndCheckArrayLong(maxValue);
            sumDuration += duration;
        }
            
        System.out.printf("Summarized reading time = %d %n", sumDuration);
        
        //assertEquals(40 + ca.size() * log2MaxValue / 8, ca.getAllocationSize(), 40);
        
             
    }
    
    public long createAndCheckArrayInt(int maxValue) {

        
        for (int i = 0; i < numberOfElements; i++) {
            long value = Math.round(Math.random() * maxValue);
            expected[i] = value;
        }

        long startTime = System.nanoTime();
        
        CompactArray ca = new CompactArray(numberOfElements, maxValue);
        
        for (int i = 0; i < numberOfElements; i++) {
            ca.addIntValue((int) expected[i]);
        }

        ca.compact();

        int log2MaxValue = 64 - Long.numberOfLeadingZeros(maxValue - 1);

        for (int i = 0; i < numberOfElements; i++) {
            assertEquals((int) expected[i], (int) ca.getIntValue(i));
        }

        ca.setIntValue(numberOfElements / 2, maxValue);

        assertEquals(maxValue, ca.getIntValue(numberOfElements / 2));

        long duration = (System.nanoTime() - startTime) / 1000;

        long allocation = RamUsageEstimator.sizeOf(ca);


        System.out.printf(
            "compact array test (numberOfElements = %d; maxValue = %d) :%n"
            + "        duration = %d microsencods; allocated RAM = %d B; allocation per element = %f B %n", 
            numberOfElements, maxValue, duration, allocation, ((float) allocation) / numberOfElements);
        
        return duration;
    }
    
    
    @Test
    public void testStorageInt() {

        // warm up
        System.out.println("---- warming up -----");
        createAndCheckArrayInt(1);
        createAndCheckArrayInt(2);
        createAndCheckArrayInt(4);
        createAndCheckArrayInt(1);

        System.out.println("---- ready -----");
        
        long sumDuration = 0;
        for (int maxValue = 1; maxValue < Integer.MAX_VALUE / 10; maxValue = maxValue << 1) {
            long duration = createAndCheckArrayInt(maxValue);
            sumDuration += duration;
        }
            
        System.out.printf("Summarized reading time = %d %n", sumDuration);
        
        //assertEquals(40 + ca.size() * log2MaxValue / 8, ca.getAllocationSize(), 40);
        
    }
    
    
    /**
     * Test of size method, of class CompactArray.
     */
    @Test
    public void testSize() {
        CompactArray instance = new CompactArray();
        assertEquals(0, instance.size());
        instance.addLongValue(3342);
        assertEquals(1, instance.size());
        instance.addLongValue(3342);
        assertEquals(2, instance.size());
        instance.setLongValue(10, 3342);
        assertEquals(11, instance.size());
        instance.compact();
        assertEquals(11, instance.size());
        instance.addLongValue(3342);
        assertEquals(12, instance.size());
        instance.setLongValue(20, 3342);
        assertEquals(21, instance.size());
        
        
        instance = new CompactArray(100, 443);
        assertEquals(0, instance.size());
        instance.addLongValue(3342);
        assertEquals(1, instance.size());
        instance.addLongValue(3342);
        assertEquals(2, instance.size());
        instance.setLongValue(10, 3342);
        assertEquals(11, instance.size());
        instance.compact();
        assertEquals(11, instance.size());
        instance.addLongValue(3342);
        assertEquals(12, instance.size());
        instance.setLongValue(20, 3342);
        assertEquals(21, instance.size());
        
    }
    

    @Test
    public void testAllocation() {
        for (long maxValue = 1; maxValue < Long.MAX_VALUE / 10; maxValue = maxValue << 1) {
            CompactArray instance = new CompactArray();
            long testSize = 50 + Math.round(Math.random() * numberOfElements);
            instance.addLongValue(maxValue);
            for (int i = 1; i < testSize; i++) {
                long value = Math.round(Math.random() * maxValue);
                instance.setLongValue(i, value);
            }
            instance.compact();
            long allocation = instance.getAllocationSize();
            long sizeOf = RamUsageEstimator.sizeOf(instance);
            assertEquals(sizeOf, allocation, 40);
            assertEquals(CompactArray.determineNumberOfBits(maxValue), 
                         8f * (sizeOf - 44) / (testSize + 1), 2);
        }
        
    }
    
    @Test
    public void testDispose() {
        CompactArray instance = new CompactArray();
        long testSize = numberOfElements;
        long maxValue = 63;
        for (int i = 0; i < testSize; i++) {
            long value = Math.round(Math.random() * maxValue);
            instance.setLongValue(i, value);
        }
        instance.dispose();
        long sizeOf = RamUsageEstimator.sizeOf(instance);
        assertEquals(40, sizeOf, 20);
        assertEquals(40, instance.getAllocationSize(), 20);
    }
    
    @Test
    public void testDataStream() throws IOException {
        for (long maxValue = 1; maxValue < Long.MAX_VALUE / 10; maxValue = maxValue << 1) {
            CompactArray instance = new CompactArray();
            long testSize = 10 + Math.round(Math.random() * numberOfElements);
            for (int i = 0; i < testSize; i++) {
                long value = Math.round(Math.random() * maxValue);
                instance.setLongValue(i, value);
            }
            instance.compact();
            long sizeOfBefore = RamUsageEstimator.sizeOf(instance);
            
            try (ByteArrayOutputStream out = new ByteArrayOutputStream(); DataOutputStream dout = new DataOutputStream(out)) {
                instance.write(dout);
                
                dout.flush();
                byte[] ba = out.toByteArray();
                
                try (ByteArrayInputStream in = new ByteArrayInputStream(ba); DataInputStream din = new DataInputStream(in)) {
                    
                    CompactArray instance2 = CompactArray.readInstance(din);
                    assertEquals(instance.size(), instance2.size());
                    for (int i = 0; i < instance.size(); i++) {
                        assertEquals(instance.getLongValue(i), instance2.getLongValue(i));
                    }
                    assertEquals(sizeOfBefore, RamUsageEstimator.sizeOf(instance2));
                }
                
            }
            
        }
        
        
    }
    
    @Test
    public void testObjectStream() throws IOException, ClassNotFoundException {
        for (long maxValue = 1; maxValue < Long.MAX_VALUE / 10; maxValue = maxValue << 1) {
            CompactArray instance = new CompactArray();
            long testSize = 10 + Math.round(Math.random() * numberOfElements);
            for (int i = 0; i < testSize; i++) {
                long value = Math.round(Math.random() * maxValue);
                instance.setLongValue(i, value);
            }
            instance.compact();
            long sizeOfBefore = RamUsageEstimator.sizeOf(instance);
            
            try (ByteArrayOutputStream out = new ByteArrayOutputStream(); ObjectOutputStream oout = new ObjectOutputStream(out)) {
                oout.writeObject(instance);
                oout.flush();
                byte[] ba = out.toByteArray();
                
                try (ByteArrayInputStream in = new ByteArrayInputStream(ba); ObjectInputStream oin = new ObjectInputStream(in)) {
                    
                    CompactArray instance2 = (CompactArray) oin.readObject();

                    assertEquals(instance.size(), instance2.size());
                    for (int i = 0; i < instance.size(); i++) {
                        assertEquals(instance.getLongValue(i), instance2.getLongValue(i));
                    }
                    assertEquals(sizeOfBefore, RamUsageEstimator.sizeOf(instance2));
                }
                
            }
            
        }
        
        
    }

    @Test (expected = IllegalArgumentException.class)
    public void testConstructionParameterChecking1() {
        CompactArray ca = new CompactArray(-1, 2);
        ca.compact();
    }

    @Test (expected = IllegalArgumentException.class)
    public void testConstructionParameterChecking2() {
        CompactArray ca = new CompactArray(10, -2);
        ca.compact();
    }

    @Test (expected = IllegalArgumentException.class)
    public void testConstructionParameterChecking3() {
        CompactArray ca = new CompactArray(1, Long.MAX_VALUE);
        ca.compact();
    }
    
    
    @Test (expected = IllegalArgumentException.class)
    public void testDetermineNumberOfButsParameters() {
        assertEquals(1, CompactArray.determineNumberOfBits(-1));
    }
    
    @Test (expected = ArrayIndexOutOfBoundsException.class)
    public void testGetIntValueParameters1() {
        CompactArray ca = new CompactArray(10, 15);
        ca.addIntValue(3);
        ca.getIntValue(0);
        ca.getIntValue(-1);
    }
    
    @Test (expected = ArrayIndexOutOfBoundsException.class)
    public void testGetIntValueParameters2() {
        CompactArray ca = new CompactArray(10, 15);
        ca.getIntValue(0);
    }

    @Test (expected = ArrayIndexOutOfBoundsException.class)
    public void testGetIntValueParameters3() {
        CompactArray ca = new CompactArray(10, 15);
        ca.addIntValue(3);
        ca.getIntValue(0);
        ca.getIntValue(1);
    }

    @Test (expected = ArrayIndexOutOfBoundsException.class)
    public void testGetLongValueParameters1() {
        CompactArray ca = new CompactArray(10, 15);
        ca.addIntValue(3);
        ca.getLongValue(0);
        ca.getLongValue(-1);
    }
    
    @Test (expected = ArrayIndexOutOfBoundsException.class)
    public void testGetLongValueParameters2() {
        CompactArray ca = new CompactArray(10, 15);
        ca.getLongValue(0);
    }

    @Test (expected = ArrayIndexOutOfBoundsException.class)
    public void testGetLongValueParameters3() {
        CompactArray ca = new CompactArray(10, 15);
        ca.addIntValue(3);
        ca.getLongValue(0);
        ca.getLongValue(1);
    }

    
    @Test (expected = ArrayIndexOutOfBoundsException.class)
    public void testSetLongValueParameters1() {
        CompactArray ca = new CompactArray(10, 15);
        ca.setLongValue(-1, 10);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testSetLongValueParameters2() {
        CompactArray ca = new CompactArray(10, 15);
        ca.setLongValue(0, -10);
    }

    @Test (expected = ArrayIndexOutOfBoundsException.class)
    public void testSetIntValueParameters1() {
        CompactArray ca = new CompactArray(10, 15);
        ca.setIntValue(-1, 10);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testSetIntValueParameters2() {
        CompactArray ca = new CompactArray(10, 15);
        ca.setIntValue(0, -10);
    }
    

    @Test (expected = IOException.class)
    public void testDataStreamFormat() throws IOException {
        long maxValue = 15; 
        CompactArray instance = new CompactArray();
        long testSize = 10 + Math.round(Math.random() * numberOfElements);
        for (int i = 0; i < testSize; i++) {
            long value = Math.round(Math.random() * maxValue);
            instance.setLongValue(i, value);
        }
        instance.compact();
        long sizeOfBefore = RamUsageEstimator.sizeOf(instance);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream(); DataOutputStream dout = new DataOutputStream(out)) {
            dout.writeInt(-342);
            instance.write(dout);

            dout.flush();
            byte[] ba = out.toByteArray();

            try (ByteArrayInputStream in = new ByteArrayInputStream(ba); DataInputStream din = new DataInputStream(in)) {

                CompactArray instance2 = CompactArray.readInstance(din);
            }

        }
    }
    
    
}


