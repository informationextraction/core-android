/**********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 01-dec-2010
 **********************************************/

package com.ht.RCSAndroidGUI;

// This class should only be read by Device
public class Keys {
	// Subversion
	public static final byte[] g_Subtype = {'A', 'N', 'D', 'R', 'O', 'I', 'D'};
	
	// AES key used to encrypt logs
	public static final byte[] g_AesKey = {'3', 'j', '9', 'W', 'm', 'm', 'D', 'g', 'B', 'q', 'y', 'U', 
										   '2', '7', '0', 'F', 'T', 'i', 'd', '3', '7', '1', '9', 'g', 
										   '6', '4', 'b', 'P', '4', 's', '5', '2'};

	// AES key used to decrypt configuration
	public static final byte[] g_ConfKey = {'A', 'd', 'f', '5', 'V', '5', '7', 'g', 'Q', 't', 'y', 'i', 
											'9', '0', 'w', 'U', 'h', 'p', 'b', '8', 'N', 'e', 'g', '5', 
											'6', '7', '5', '6', 'j', '8', '7', 'R'};

	// 20 bytes that uniquely identifies the device (non-static on purpose)
	public final byte[] g_InstanceId = {'b', 'g', '5', 'e', 't', 'G', '8', '7', 'q', '2', 
										'0', 'K', 'g', '5', '2', 'W', '5', 'F', 'g', '1'};
	
	// 16 bytes that uniquely identifies the backdoor, NULL-terminated
	public static final byte[] g_BackdoorID = {'a', 'v', '3', 'p', 'V', 'c', 'k', '1', 'g', 'b', '4', 
											   'e', 'R', '2', 'd', '8', 0};
	
	// Challenge key
	public static final byte[] g_Challenge = {'f', '7', 'H', 'k', '0', 'f', '5', 'u', 's', 'd', '0', 
											  '4', 'a', 'p', 'd', 'v', 'q', 'w', '1', '3', 'F', '5', 
											  'e', 'd', '2', '5', 's', 'o', 'V', '5', 'e', 'D'};
	
	// Configuration name, scrambled using the first byte of g_Challenge[]
	public static final String g_ConfName = "c3mdX053du1YJ541vqWILrc4Ff71pViL";
}
