/*
 *  © 2015 Jakub Strychowski
 */

package org.neurpheus.core.charset;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.UnsupportedCharsetException;
import java.util.UUID;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jakub Strychowski
 */
public class DynamicCharsetTest {
    
    public DynamicCharsetTest() {
    }

    private DynamicCharset createCharset(String name) {
        DynamicCharset result = new DynamicCharset(name, null);
        result.setInternational();
        return result;
    }
    
    @Test
    public void testNewEncoder() {
        //  GIVEN
        DynamicCharset dc = createCharset(UUID.randomUUID().toString());
        
        // WHEN
        CharsetEncoder ce = dc.newEncoder();
        
        // THEN
        assertTrue(ce.canEncode("abcde23434%#"));
    }

    @Test
    public void testNewDecoder() {
        //  GIVEN
        DynamicCharset dc = createCharset(UUID.randomUUID().toString());
        
        // WHEN
        CharsetDecoder cd = dc.newDecoder();
        
        // THEN
        assertTrue(cd.averageCharsPerByte() == 1.0f);
    }

    @Test
    public void testContains() {
        //  GIVEN
        DynamicCharset dc1 = createCharset(UUID.randomUUID().toString());
        DynamicCharset dc2 = createCharset(UUID.randomUUID().toString());
        
        // WHEN
        Charset defaultCharset = Charset.defaultCharset();
        
        // THEN
        assertFalse(dc1.contains(defaultCharset));
        assertFalse(dc2.contains(defaultCharset));
        assertTrue(dc1.contains(dc1));
        assertFalse(dc1.contains(dc2));
        assertFalse(dc2.contains(dc1));
    }

    @Test
    public void testAddCharacter() {
        // GIVEN
        DynamicCharset dcs = new DynamicCharset(UUID.randomUUID().toString(), null);
        
        // WHEN
        dcs.addCharacter('a');
        dcs.addCharacter('ą');
        dcs.addCharacter('A');
        dcs.addCharacter('Ą');
        dcs.addCharacter('B');
        

        // THEN
        assertEquals(1, dcs.addCharacter('a')); 
        assertEquals(2, dcs.addCharacter('ą'));
        assertEquals(3, dcs.addCharacter('A'));
        assertEquals(4, dcs.addCharacter('Ą'));
        assertEquals(5, dcs.addCharacter('B'));
        assertEquals(5, dcs.addCharacter('B'));
        assertEquals(6, dcs.addCharacter('b'));
        
    }

    @Test
    public void testAddCharacterToLimit() {
        // GIVEN
        DynamicCharset dcs = new DynamicCharset(UUID.randomUUID().toString(), null);
        
        // WHEN
        for (int index = 1; index < 300; index++) {
            char ch = (char) index;
            dcs.addCharacter(ch);
        }

        // THEN
        assertEquals(DynamicCharset.UNKNOWN_CHARACTER_CODE, dcs.addCharacter('Ą'));
        assertEquals(DynamicCharset.UNKNOWN_CHARACTER_CODE, dcs.fastEncode('Ą'));
    }
    
    @Test
    public void testFastEncode() {
        // GIVEN
        DynamicCharset dcs = new DynamicCharset(UUID.randomUUID().toString(), null);
        
        // WHEN
        dcs.addCharacter('a');
        dcs.addCharacter('ą');
        dcs.addCharacter('A');
        dcs.addCharacter('Ą');
        dcs.addCharacter('B');

        // THEN
        assertEquals(1, dcs.fastEncode('a'));
        assertEquals(2, dcs.fastEncode('ą'));
        assertEquals(3, dcs.fastEncode('A'));
        assertEquals(4, dcs.fastEncode('Ą'));
        assertEquals(5, dcs.fastEncode('B'));
        assertEquals(6, dcs.fastEncode('b'));
        dcs.setChangable(false);
        assertEquals(DynamicCharset.UNKNOWN_CHARACTER_CODE, dcs.fastEncode('c'));
    }

    @Test
    public void testFastDecode_byte() {
        // GIVEN
        DynamicCharset dcs = new DynamicCharset(UUID.randomUUID().toString(), null);
        
        // WHEN
        dcs.addCharacter('a');
        dcs.addCharacter('ą');
        dcs.addCharacter('A');
        dcs.addCharacter('Ą');
        dcs.addCharacter('B');

        // THEN
        assertEquals('a', dcs.fastDecode((byte) 1));
        assertEquals('ą', dcs.fastDecode((byte) 2));
        assertEquals('A', dcs.fastDecode((byte) 3));
        assertEquals('Ą', dcs.fastDecode((byte) 4));
        assertEquals('B', dcs.fastDecode((byte) 5));
        assertEquals(DynamicCharset.UNKNOWN_CHARACTER, dcs.fastDecode((byte) 6));
    }

    @Test
    public void testFastDecode_byteArr() {
        // GIVEN
        DynamicCharset dcs = new DynamicCharset(UUID.randomUUID().toString(), null);
        byte[] encodedString = new byte[] {1, 2, 3, 4 , 5, 1, 2};
        
        // WHEN
        dcs.addCharacter('a');
        dcs.addCharacter('ą');
        dcs.addCharacter('A');
        dcs.addCharacter('Ą');
        dcs.addCharacter('B');

        // THEN
        assertEquals("aąAĄBaą", dcs.fastDecode(encodedString));
        assertEquals("aąA", dcs.fastDecode(encodedString, 0, 3));
        assertEquals("AĄ", dcs.fastDecode(encodedString,2, 2));
        assertEquals("aąAĄBaą", dcs.fastDecode(encodedString, 0, 7));
    }

    @Test
    public void testFastDecodeToCharArray() {
        // GIVEN
        DynamicCharset dcs = new DynamicCharset(UUID.randomUUID().toString(), null);
        byte[] encodedString = new byte[] {1, 2, 3, 4 , 5, 1, 2};
        
        // WHEN
        dcs.addCharacter('a');
        dcs.addCharacter('ą');
        dcs.addCharacter('A');
        dcs.addCharacter('Ą');
        dcs.addCharacter('B');

        // THEN
        assertArrayEquals("aąAĄBaą".toCharArray(), dcs.fastDecodeToCharArray(encodedString,0, 7));
        assertArrayEquals("aąA".toCharArray(), dcs.fastDecodeToCharArray(encodedString,0, 3));
        assertArrayEquals("AĄ".toCharArray(), dcs.fastDecodeToCharArray(encodedString,2, 2));
    }

    @Test
    public void testEncode() throws Exception {
        // GIVEN
        DynamicCharset dcs = new DynamicCharset(UUID.randomUUID().toString(), null);
        String alphabet = "aąAĄbBcdef";
        
        // WHEN
        dcs.encode(alphabet);

        assertEquals(3, dcs.fastEncode('A'));
        assertEquals(1, dcs.fastEncode('a'));
        assertEquals(2, dcs.fastEncode('ą'));
        assertEquals(4, dcs.fastEncode('Ą'));
        assertEquals(6, dcs.fastEncode('B'));
        assertEquals(5, dcs.fastEncode('b'));
    }

    @Test
    public void testGetBytesFromCharSeq1() {
        // GIVEN
        DynamicCharset dcs = new DynamicCharset(UUID.randomUUID().toString(), null);
        byte[] bytes = new byte[15];
        String src = "aąAąBabcdefgh";
        
        // WHEN
        dcs.encode(src);
        dcs.getBytes(src, 0, src.length(), bytes, 0);
        
        // THEN
        assertArrayEquals(new byte[] {1, 2, 3, 2, 4, 1, 5, 6, 7, 8, 9, 10, 11, 0, 0}, bytes);
    }

    @Test
    public void testGetBytesFromCharSeq2() {
        // GIVEN
        DynamicCharset dcs = new DynamicCharset(UUID.randomUUID().toString(), null);
        byte[] bytes = new byte[15];
        String src = "aąAąBabcdefgh";
        
        // WHEN
        dcs.encode(src);
        dcs.getBytes(src, 2, src.length(), bytes, 2);
        
        // THEN
        assertArrayEquals(new byte[] {0, 0, 3, 2, 4, 1, 5, 6, 7, 8, 9, 10, 11, 0, 0}, bytes);
    }

    @Test
    public void testGetBytesFromCharSeq3() {
        // GIVEN
        DynamicCharset dcs = new DynamicCharset(UUID.randomUUID().toString(), null);
        byte[] bytes = new byte[15];
        String src = "aąAąBabcdefgh";
        
        // WHEN
        dcs.encode(src);
        dcs.getBytes(src, 2, 4, bytes, 13);
        
        // THEN
        assertArrayEquals(new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 2}, bytes);
    }

    @Test
    public void testGetBytesFromCharSeq4() {
        // GIVEN
        DynamicCharset dcs = new DynamicCharset(UUID.randomUUID().toString(), null);
        byte[] bytes = new byte[15];
        String src = "aąAąBabcdefgh";
        String src2 = "xyza";
        
        // WHEN
        dcs.encode(src);
        dcs.getBytes(src + src2, 5, src.length() + src2.length(), bytes, 0);
        
        // THEN
        assertArrayEquals(new byte[] {1, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 1, 0, 0, 0}, bytes);
    }
    
    @Test
    public void testGetBytesFromCharArray1() {
        // GIVEN
        DynamicCharset dcs = new DynamicCharset(UUID.randomUUID().toString(), null);
        byte[] bytes = new byte[15];
        String src = "aąAąBabcdefgh";
        
        // WHEN
        dcs.encode(src);
        dcs.getBytes(src.toCharArray(), 0, src.length(), bytes, 0);
        
        // THEN
        assertArrayEquals(new byte[] {1, 2, 3, 2, 4, 1, 5, 6, 7, 8, 9, 10, 11, 0, 0}, bytes);
    }

    @Test
    public void testGetBytesFromCharArray2() {
        // GIVEN
        DynamicCharset dcs = new DynamicCharset(UUID.randomUUID().toString(), null);
        byte[] bytes = new byte[15];
        String src = "aąAąBabcdefgh";
        
        // WHEN
        dcs.encode(src);
        dcs.getBytes(src.toCharArray(), 2, src.length(), bytes, 2);
        
        // THEN
        assertArrayEquals(new byte[] {0, 0, 3, 2, 4, 1, 5, 6, 7, 8, 9, 10, 11, 0, 0}, bytes);
    }

    @Test
    public void testGetBytesFromCharArray3() {
        // GIVEN
        DynamicCharset dcs = new DynamicCharset(UUID.randomUUID().toString(), null);
        byte[] bytes = new byte[15];
        String src = "aąAąBabcdefgh";
        
        // WHEN
        dcs.encode(src);
        dcs.getBytes(src.toCharArray(), 2, 4, bytes, 13);
        
        // THEN
        assertArrayEquals(new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 2}, bytes);
    }

    @Test
    public void testGetBytesFromCharArray4() {
        // GIVEN
        DynamicCharset dcs = new DynamicCharset(UUID.randomUUID().toString(), null);
        byte[] bytes = new byte[15];
        String src = "aąAąBabcdefgh";
        String src2 = "xyza";
        
        // WHEN
        dcs.encode(src);
        dcs.getBytes((src + src2).toCharArray(), 5, src.length() + src2.length(), bytes, 0);
        
        // THEN
        assertArrayEquals(new byte[] {1, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 1, 0, 0, 0}, bytes);
    }


    @Test
    public void testGetChars() {
        // GIVEN
        DynamicCharset dcs = new DynamicCharset(UUID.randomUUID().toString(), null);
        byte[] src = new byte[] {1, 5, 7, 8, 9, 2};
        String alphabet = "aąAĄbBcdef";
        
        // WHEN
        dcs.encode(alphabet);
        char[] dst = new char[10];
        dcs.getChars(src, 0, src.length, dst, 0);
        
        // THEN
        assertArrayEquals(new char[] {'a', 'b', 'c', 'd', 'e', 'ą', 0, 0, 0, 0}, dst);
    }

    @Test
    public void testGetChars2() {
        // GIVEN
        DynamicCharset dcs = new DynamicCharset(UUID.randomUUID().toString(), null);
        byte[] src = new byte[] {1, 5, 7, 8, 9, 2};
        String alphabet = "aąAĄbBcdef";
        
        // WHEN
        dcs.encode(alphabet);
        char[] dst = new char[10];
        dcs.getChars(src, 2, src.length, dst, 2);
        
        // THEN
        assertArrayEquals(new char[] {0, 0, 'c', 'd', 'e', 'ą', 0, 0, 0, 0}, dst);
    }
    
    @Test
    public void testWriteAndRead() throws Exception {
        // GIVEN
        DynamicCharset dcs = new DynamicCharset(UUID.randomUUID().toString(), null);
        String alphabet = "aąAĄbBcdef";
        
        // WHEN
        dcs.encode(alphabet);

        byte[] bytes;
        try (
                ByteArrayOutputStream baout = new ByteArrayOutputStream();
                DataOutputStream dout = new DataOutputStream(baout);
                ) {
            dcs.write(dout);
            dout.flush();
            bytes = baout.toByteArray();
        } 

        DynamicCharset dcs2 = new DynamicCharset(UUID.randomUUID().toString(), null);
        try (
                ByteArrayInputStream bain = new ByteArrayInputStream(bytes);
                DataInputStream din = new DataInputStream(bain);
                ) {
            dcs2.read(din);
        }
        dcs2.setChangable(false);
        
        // THEN 
        assertEquals(3, dcs2.fastEncode('A'));
        assertEquals(1, dcs.fastEncode('a'));
        assertEquals(2, dcs.fastEncode('ą'));
        assertEquals(4, dcs.fastEncode('Ą'));
        assertEquals(6, dcs.fastEncode('B'));
        assertEquals(5, dcs.fastEncode('b'));
    }


    @Test
    public void testGetChar2byte() {
        // GIVEN
        DynamicCharset dcs = new DynamicCharset(UUID.randomUUID().toString(), null);
        String alphabet = "aąAĄbBcdef";
        
        // WHEN
        dcs.encode(alphabet);
        byte[] result = dcs.getChar2byte();
        
        // THEN
        assertEquals(3, result['A']);
        assertEquals(4, result['Ą']);
        assertEquals(5, result['b']);
        assertEquals(1, result['a']);
        assertEquals(2, result['ą']);
        assertEquals(6, result['B']);
        assertEquals(7, result['c']);
        assertEquals(8, result['d']);
        assertEquals(9, result['e']);
        assertEquals(10, result['f']);
    }

    @Test
    public void testGetByte2char() {
        // GIVEN
        DynamicCharset dcs = new DynamicCharset(UUID.randomUUID().toString(), null);
        String alphabet = "aąAĄbBcdef";
        
        // WHEN
        dcs.encode(alphabet);
        char[] result = dcs.getByte2char();
        
        // THEN
        assertEquals('A', result[3]);
        assertEquals('Ą', result[4]);
        assertEquals('b', result[5]);
        assertEquals('a', result[1]);
        assertEquals('ą', result[2]);
        assertEquals('B', result[6]);
        assertEquals('c', result[7]);
        assertEquals('d', result[8]);
        assertEquals('e', result[9]);
        assertEquals('f', result[10]);
    }

    @Test
    public void testGetMaxByte() {
        // GIVEN
        DynamicCharset dcs = new DynamicCharset(UUID.randomUUID().toString(), null);
        String alphabet = "aąAĄbBcdef";
        
        // WHEN
        dcs.encode(alphabet);
        int result = dcs.getMaxByte();
        
        // THEN
        assertEquals(10, result);
    }

    @Test
    public void testGetMaxChar() {
        // GIVEN
        DynamicCharset dcs = new DynamicCharset(UUID.randomUUID().toString(), null);
        String alphabet = "aąAĄbBcdef";
        
        // WHEN
        dcs.encode(alphabet);
        char result = dcs.getMaxChar();
        
        // THEN
        assertEquals('ą', result);
    }

    @Test
    public void testIsChangable() {
        // GIVEN
        DynamicCharset dcs = new DynamicCharset(UUID.randomUUID().toString(), null);
        String alphabet = "aąAĄbBcdef";
        
        // WHEN
        dcs.encode(alphabet);
        
        // THEN
        assertTrue(dcs.isChangable());
        dcs.encode("x");
        assertEquals(11, dcs.fastEncode('x'));
        dcs.setChangable(false);
        assertFalse(dcs.isChangable());
        dcs.encode("yz");
        assertEquals(DynamicCharset.UNKNOWN_CHARACTER_CODE, dcs.fastEncode('y'));
        assertEquals(DynamicCharset.UNKNOWN_CHARACTER_CODE, dcs.fastEncode('z'));
    }


    @Test
    public void testRegisterCharset() {
        // GIVEN
        DynamicCharset dcs = new DynamicCharset(UUID.randomUUID().toString(), null);

        // WHEN
        dcs.registerCharset();
        
        Charset result = Charset.forName(dcs.name());
        
        // THEN
        assertEquals(dcs, result);
    }

    @Test
    public void testUnregisterCharset() {
        // GIVEN
        DynamicCharset dcs = new DynamicCharset(UUID.randomUUID().toString(), null);

        // WHEN
        dcs.registerCharset();
        dcs.unregisterCharset();
        
        // THEN
        try {
            Charset.forName(dcs.name());
            fail("Exception should be thrown");
        } catch (UnsupportedCharsetException ex) {
        }
    }

    @Test
    public void testSetInternational() {
        // GIVEN
        DynamicCharset dcs = new DynamicCharset(UUID.randomUUID().toString(), null);
        String polish = "ęóąśłżźćńĘÓĄŚŁŻŹĆŃ";
        
        // WHEN
        dcs.setInternational();
        dcs.setChangable(false);
        
        // THEN
        for (char ch = 0x20; ch < 0x80; ch++) {
            assertTrue(dcs.fastEncode(ch) != DynamicCharset.UNKNOWN_CHARACTER_CODE);
        }
        
        assertEquals(1, dcs.fastEncode(' '));
        assertEquals(2, dcs.fastEncode('\t'));
        assertEquals(3, dcs.fastEncode('\n'));
        assertEquals(4, dcs.fastEncode('\r'));
        
        for (int pos = 0; pos < polish.length(); pos++) {
            assertTrue(dcs.fastEncode(polish.charAt(pos)) != DynamicCharset.UNKNOWN_CHARACTER_CODE);
        }
        
        
        
    }
    
}
