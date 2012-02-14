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
	
	// RCS 7
	byte[] AesKey = new byte[]{ (byte)0xaa, (byte)0x55, (byte)0x01, (byte)0xfb, (byte)0xaa, (byte)0xc3, (byte)0x35, (byte)0x66, (byte)0x1a, (byte)0x00, (byte)0x50, (byte)0x1f, (byte)0x9e, (byte)0x1a, (byte)0x48, (byte)0xfe };
	byte[] ConfKey = new byte[] { (byte)0x62, (byte)0x48, (byte)0xf4, (byte)0x84, (byte)0x5f, (byte)0x2d, (byte)0x80, (byte)0x9c, (byte)0x15, (byte)0x30, (byte)0x53, (byte)0x4d, (byte)0xc1, (byte)0x2b, (byte)0xe4, (byte)0x34 };
	byte[] ChallengeKey = new byte[]{ (byte)0x0e, (byte)0xb7, (byte)0xdc, (byte)0xf3, (byte)0x41, (byte)0x9e, (byte)0x6d, (byte)0xec, (byte)0x26, (byte)0x04, (byte)0x5e, (byte)0x1a, (byte)0xa9, (byte)0x14, (byte)0xa4, (byte)0x2f };
	String BuildId = "RCS_0000000007";

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
