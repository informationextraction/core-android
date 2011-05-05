package com.ht.RCSAndroidGUI.agent.task;

public class EmailInfo {
	private static final String TAG = "EmailInfo";

	private long userId;
	private int emailType;
	private String email;
	
	public EmailInfo(long userId, int emailType, String email) {
		this.userId = userId;
		this.emailType = emailType;
		this.email = email;
	}

	public long getUserId() {
		return userId;
	}

	public int getEmailType() {
		return emailType;
	}

	public String getEmail() {
		return email;
	}
}
