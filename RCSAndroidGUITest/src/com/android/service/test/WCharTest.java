package com.android.service.test;

import java.io.IOException;
import java.util.Arrays;

import com.android.service.util.DataBuffer;
import com.android.service.util.WChar;

import junit.framework.TestCase;

public class WCharTest extends TestCase {

	String orig = "hello world";
	byte[] wchar = new byte[]{0x68, 0x0, 0x65, 0x0, 0x6c, 0x0, 0x6c, 0x0, 0x6f, 0x0, 0x20, 0x0, 0x77, 0x0, 0x6f, 0x0, 0x72, 0x0, 0x6c, 0x0, 0x64, 0x0};
	byte[] wcharzero = new byte[]{0x68, 0x0, 0x65, 0x0, 0x6c, 0x0, 0x6c, 0x0, 0x6f, 0x0, 0x20, 0x0, 0x77, 0x0, 0x6f, 0x0, 0x72, 0x0, 0x6c, 0x0, 0x64, 0x0, 0x0, 0x0};
	byte[] paswchar = new byte[]{24,0x0,0x0,0x0, 0x68, 0x0, 0x65, 0x0, 0x6c, 0x0, 0x6c, 0x0, 0x6f, 0x0, 0x20, 0x0, 0x77, 0x0, 0x6f, 0x0, 0x72, 0x0, 0x6c, 0x0, 0x64, 0x0, 0x0, 0x0};
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	public final void testGetBytesString() {
		byte[] res = WChar.getBytes(orig);
		assertTrue( Arrays.equals(res, wchar) );
	}

	public final void testGetBytesStringBoolean() {
		byte[] res = WChar.getBytes(orig, true); 
		assertTrue( Arrays.equals(res, wcharzero) );
	}


	public final void testReadPascal() {
		DataBuffer dataBuffer = new DataBuffer(paswchar);
		String r1=null;
		try {
			r1 = WChar.readPascal(dataBuffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertEquals(r1, orig);
	}

	public final void testPascalizeString() {
		byte[] r1 = WChar.pascalize(orig);
		byte[] r2 = WChar.pascalize(wchar);
		byte[] r3 = WChar.pascalize(wcharzero);
		
		assertTrue( Arrays.equals(r1, r2) );
		assertTrue( Arrays.equals(r2, r3) );
		assertTrue( Arrays.equals(r1, paswchar) );
	}

}
