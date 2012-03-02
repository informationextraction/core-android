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

	// RCS 751
	byte[] AesKey = new byte[]{ (byte)0x8e, (byte)0x5c, (byte)0x99, (byte)0x7a, (byte)0x67, (byte)0xd8, (byte)0xce, (byte)0x44, (byte)0x19, (byte)0xb0, (byte)0x19, (byte)0xef, (byte)0xc0, (byte)0x13, (byte)0x16, (byte)0xc7 };
	byte[] ConfKey = new byte[] { (byte)0x80, (byte)0x87, (byte)0x59, (byte)0x58, (byte)0xfb, (byte)0x3f, (byte)0xa9, (byte)0x80, (byte)0x5c, (byte)0x90, (byte)0x5d, (byte)0x48, (byte)0x1f, (byte)0xeb, (byte)0xae, (byte)0x80 };
	byte[] ChallengeKey = new byte[]{ (byte)0x57, (byte)0x2e, (byte)0xbc, (byte)0x94, (byte)0x39, (byte)0x12, (byte)0x81, (byte)0xcc, (byte)0xf5, (byte)0x3a, (byte)0x85, (byte)0x13, (byte)0x30, (byte)0xbb, (byte)0x0d, (byte)0x99 };
	String BuildId = "RCS_0000000751";

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
