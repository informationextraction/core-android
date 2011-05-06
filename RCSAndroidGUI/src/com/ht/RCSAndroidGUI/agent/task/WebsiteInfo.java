/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : WebsiteInfo.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.ht.RCSAndroidGUI.agent.task;

public class WebsiteInfo {
	private static final String TAG = "WebsiteInfo";
	
	private long userId;
	private String websiteName;

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
