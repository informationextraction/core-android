package com.ht.RCSAndroidGUI;

public class Sms {
	private String address, body;
	private boolean sent;
	
	public Sms(String address, String body, boolean sent) {
		this.address = address;
		this.body = body;
		this.sent = sent;
	}

	public String getAddress() {
		return address;
	}

	public String getBody() {
		return body;
	}
	
	public boolean getSent() {
		return sent;
	}
}
