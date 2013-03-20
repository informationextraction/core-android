/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : WebsiteInfo.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.networking.module.task;

public class WebsiteInfo {
	private static final String TAG = "WebsiteInfo"; //$NON-NLS-1$

	private final long userId;
	private final String websiteName;

	public WebsiteInfo(long userId, String websiteName) {
		this.userId = userId;
		this.websiteName = websiteName;
	}

	public long getUserId() {
		return userId;
	}

	public String getWebsiteName() {
		return websiteName;
	}

}
