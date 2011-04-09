package com.ht.RCSAndroidGUI.test;

import java.util.Arrays;

import com.ht.RCSAndroidGUI.utils.WChar;

import junit.framework.TestCase;

public class WCharTest extends TestCase {

	String orig = "hello world";
	byte[] wchar = new byte[]{0x68, 0x0, 0x65, 0x0, 0x6c, 0x0, 0x6c, 0x0, 0x6f, 0x0, 0x20, 0x0, 0x77, 0x0, 0x6f, 0x0, 0x72, 0x0, 0x6c, 0x0, 0x64, 0x0};
	byte[] wcharzero = new byte[]{0x68, 0x0, 0x65, 0x0, 0x6c, 0x0, 0x6c, 0x0, 0x6f, 0x0, 0x20, 0x0, 0x77, 0x0, 0x6f, 0x0, 0x72, 0x0, 0x6c, 0x0, 0x64, 0x0, 0x0, 0x0};
	
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

	public final void testPascalizeByteArray() {
		fail("Not yet implemented"); // TODO
	}

	public final void testGetStringByteArrayBoolean() {
		fail("Not yet implemented"); // TODO
	}

	public final void testGetStringByteArrayIntIntBoolean() {
		fail("Not yet implemented"); // TODO
	}

	public final void testReadPascal() {
		fail("Not yet implemented"); // TODO
	}

	public final void testPascalizeString() {
		fail("Not yet implemented"); // TODO
	}

}
