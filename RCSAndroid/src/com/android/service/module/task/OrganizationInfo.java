/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : OrganizationInfo.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.module.task;

public class OrganizationInfo {
	private static final String TAG = "OrganizationInfo"; //$NON-NLS-1$

	private final int userId;
	private final int type;
	private final String companyName;
	private final String companyTitle;

	public OrganizationInfo(int userId, int type, String companyName, String companyTitle) {
		this.userId = userId;
		this.type = type;
		this.companyName = companyName;
		this.companyTitle = companyTitle;
	}

	public int getUserId() {
		return userId;
	}

	public int getType() {
		return type;
	}

	public String getCompanyName() {
		return companyName;
	}

	public String getCompanyTitle() {
		return companyTitle;
	}
}
