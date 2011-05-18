/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : KeysFake.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.crypto;

// ID 179 su rcs-prod
/**
 * The Class KeysFake.
 */
public class KeysFake extends Keys {

	// RCS 179
	/** The Aes key. */
	byte[] AesKey = new byte[] { (byte) 0x9e, (byte) 0xdb, (byte) 0x16,
			(byte) 0x47, (byte) 0x55, (byte) 0x17, (byte) 0x77, (byte) 0x72,
			(byte) 0xaf, (byte) 0x6b, (byte) 0xfd, (byte) 0x6f, (byte) 0xc9,
			(byte) 0xd5, (byte) 0x6f, (byte) 0xfd };

	/** The Conf key. */
	byte[] ConfKey = new byte[] { (byte) 0xa9, (byte) 0x98, (byte) 0x76,
			(byte) 0x7f, (byte) 0x8c, (byte) 0x31, (byte) 0x99, (byte) 0xb0,
			(byte) 0x33, (byte) 0x8c, (byte) 0xb2, (byte) 0xd9, (byte) 0x98,
			(byte) 0x08, (byte) 0x42, (byte) 0x58 };

	/** The Challenge key. */
	byte[] ChallengeKey = new byte[] { (byte) 0x57, (byte) 0x2e, (byte) 0xbc,
			(byte) 0x94, (byte) 0x39, (byte) 0x12, (byte) 0x81, (byte) 0xcc,
			(byte) 0xf5, (byte) 0x3a, (byte) 0x85, (byte) 0x13, (byte) 0x30,
			(byte) 0xbb, (byte) 0x0d, (byte) 0x99 };

	/** The Build id. */
	String BuildId = "RCS_0000000179";

	public KeysFake(){
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
