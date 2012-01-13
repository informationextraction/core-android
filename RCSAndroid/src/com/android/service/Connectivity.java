/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : Connectivity.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service;

public class Connectivity {
	private final boolean connected;

	public Connectivity(boolean c) {
		connected = c;
	}

	public boolean isConnected() {
		return connected;
	}
}
