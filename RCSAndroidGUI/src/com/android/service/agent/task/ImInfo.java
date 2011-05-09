/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : ImInfo.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.agent.task;

public class ImInfo {
	private static final String TAG = "ImInfo";
	
	private long userId;
	private int imType;
	private String im;
	
	public ImInfo(long userId, int imType, String im) {
		this.userId = userId;
		this.imType = imType;
		this.im = im;
	}

	public long getUserId() {
		return userId;
	}

	public int getImType() {
		return imType;
	}

	public String getIm() {
		return im;
	}

}
