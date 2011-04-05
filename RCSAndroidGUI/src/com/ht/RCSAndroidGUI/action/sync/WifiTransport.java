package com.ht.RCSAndroidGUI.action.sync;

public class WifiTransport extends HttpTransport {

	public WifiTransport(String host) {
		super(host);

	}

	public WifiTransport(String host, boolean wifiForced) {
		super(host);
	}

	@Override
	public boolean isAvailable() {
		// TODO
		return true;
	}

	@Override
	protected String getSuffix() {
		// TODO
		return "";
	}

}
