package com.ht.RCSAndroidGUI.agent.task;

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
