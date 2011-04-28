package com.ht.RCSAndroidGUI;

public class Call {
	private static final String TAG = "Call";
	
	private String number;
	private boolean incoming, status;
	
	public Call(String number, boolean incoming, boolean status) {
		this.number = number;
		this.incoming = incoming;
		this.status = status;
	}

	public String getNumber() {
		return number;
	}

	public boolean isIncoming() {
		return incoming;
	}
	
	public boolean isOngoing() {
		return status;
	}
}
