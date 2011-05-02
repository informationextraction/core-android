package com.ht.RCSAndroidGUI;

public class Sms {
	private String address, body;
	private boolean sent; // false - received, true - sent
	
	public static boolean RECEIVED = false;
	public static boolean SENT = true;
	
	public Sms() {
		this.address = "";
		this.body = "";
		this.sent = false;
	}
	
	public Sms(String address, String body, boolean sent) {
		this.address = address;
		this.body = body;
		this.sent = sent;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getBody() {
		return body;
	}
	
	public void setBody(String body) {
		this.body = body;
	}
	
	public boolean getSent() {
		return sent;
	}
	
	public void setSent(boolean sent) {
		this.sent = sent;
	}
}
