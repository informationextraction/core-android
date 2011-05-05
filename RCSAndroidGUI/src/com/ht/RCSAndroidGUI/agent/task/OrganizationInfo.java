package com.ht.RCSAndroidGUI.agent.task;

public class OrganizationInfo {
	private static final String TAG = "OrganizationInfo";
	
	private int userId;
	private int type;
	private String companyName;
	private String companyTitle;

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
