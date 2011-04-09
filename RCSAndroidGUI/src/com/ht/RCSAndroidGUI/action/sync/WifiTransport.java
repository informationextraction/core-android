/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : WifiTransport.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/
package com.ht.RCSAndroidGUI.action.sync;

// TODO: Auto-generated Javadoc
/**
 * The Class WifiTransport.
 */
public class WifiTransport extends HttpTransport {

	/**
	 * Instantiates a new wifi transport.
	 *
	 * @param host the host
	 */
	public WifiTransport(final String host) {
		super(host);

	}

	/**
	 * Instantiates a new wifi transport.
	 *
	 * @param host the host
	 * @param wifiForced the wifi forced
	 */
	public WifiTransport(final String host, final boolean wifiForced) {
		super(host);
	}

	/* (non-Javadoc)
	 * @see com.ht.RCSAndroidGUI.action.sync.Transport#isAvailable()
	 */
	@Override
	public boolean isAvailable() {
		// TODO
		return true;
	}

	/* (non-Javadoc)
	 * @see com.ht.RCSAndroidGUI.action.sync.Transport#getSuffix()
	 */
	@Override
	protected String getSuffix() {
		// TODO
		return "";
	}

}
