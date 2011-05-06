/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : Standby.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.ht.RCSAndroidGUI;

public class Standby {
	private boolean status;

	public Standby(boolean s) {
		status = s;
	}

	public boolean getStatus() {
		return status;
	}
}
