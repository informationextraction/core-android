/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : EventBattery.java
 * Created      : 6-mag-2011
 * Author		: zeno -> ladro :D
 * *******************************************/

package com.android.service.event;

import java.io.IOException;

import com.android.service.Battery;
import com.android.service.auto.Cfg;
import com.android.service.conf.ConfEvent;
import com.android.service.conf.ConfigurationException;
import com.android.service.interfaces.Observer;
import com.android.service.listener.ListenerBattery;
import com.android.service.util.Check;
import com.android.service.util.DataBuffer;

public class EventBattery extends BaseEvent implements Observer<Battery> {
	/** The Constant TAG. */
	private static final String TAG = "EventBattery"; //$NON-NLS-1$

	private int actionOnExit, actionOnEnter, minLevel, maxLevel;
	private boolean inRange = false;

	@Override
	public void actualStart() {
		ListenerBattery.self().attach(this);
	}

	@Override
	public void actualStop() {
		ListenerBattery.self().detach(this);
		onExit(); // di sicurezza
	}

	@Override
	public boolean parse(ConfEvent conf) {
		try {
			minLevel = conf.getInt("min");
			maxLevel = conf.getInt("max");

			if (Cfg.DEBUG) {
				Check.log(TAG + " exitAction: " + actionOnExit + " minLevel:" + minLevel + " maxLevel:" + maxLevel);//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		} catch (final ConfigurationException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: params FAILED");//$NON-NLS-1$
			}

			return false;
		}

		return true;
	}

	@Override
	public void actualGo() {
		// TODO Auto-generated method stub
	}

	public int notification(Battery b) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " Got battery notification: " + b.getBatteryLevel() + "%");//$NON-NLS-1$ //$NON-NLS-2$
		}

		if (minLevel > maxLevel) {
			return 0;
		}

		// Nel range
		if ((b.getBatteryLevel() >= minLevel && b.getBatteryLevel() <= maxLevel) && inRange == false) {
			inRange = true;

			if (Cfg.DEBUG) {
				Check.log(TAG + " Battery IN");//$NON-NLS-1$
			}

			onEnter();
		} else if // Fuori dal range
		((b.getBatteryLevel() < minLevel || b.getBatteryLevel() > maxLevel) && inRange == true) {
			inRange = false;

			if (Cfg.DEBUG) {
				Check.log(TAG + " Battery OUT");//$NON-NLS-1$
			}

			onExit();
		}

		return 0;
	}
}
