/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : Connectivity.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.ht.RCSAndroidGUI;

public class Connectivity {
	private boolean connected;
	
	public Connectivity(boolean c) {
		connected = c;
	}
	
	public boolean isConnected() {
		return connected;
	}
}
