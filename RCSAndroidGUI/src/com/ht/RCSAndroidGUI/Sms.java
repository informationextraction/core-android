/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : Sms.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.ht.RCSAndroidGUI;

import android.util.Log;

public class Sms {
	private static final String TAG = "Sms";
	
	public static boolean RECEIVED = false;
	public static boolean SENT = true;
	
	private String address, body;
	private long date;
	private boolean sent; // false - received, true - sent
	
	private int yields_id, thread_id, protocol, read;
	private int status, type, reply_path;
	private String service_center, person;
	
	public Sms(String address, String body, long date, boolean sent) {
		this.address = address;
		this.body = body;
		this.date = date;
		this.sent = sent;
		
		this.service_center = "";
		this.person = "";
	}

	public void print() {
		Log.d("QZ", TAG + " (print): Address: " + address);
		Log.d("QZ", TAG + " (print): Body: " + body);
		Log.d("QZ", TAG + " (print): Date: " + date);
		Log.d("QZ", TAG + " (print): Sent: " + sent);
		Log.d("QZ", TAG + " (print): Yields_id: " + yields_id);
		Log.d("QZ", TAG + " (print): Thread_id: " + thread_id);
		Log.d("QZ", TAG + " (print): Protocol: " + protocol);
		Log.d("QZ", TAG + " (print): Read: " + read);
		Log.d("QZ", TAG + " (print): Status: " + status);
		Log.d("QZ", TAG + " (print): Reply_path: " + reply_path);
		Log.d("QZ", TAG + " (print): Service_center: " + service_center);
		Log.d("QZ", TAG + " (print): Person: " + person);	
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
	
	public void setDate(long date) {
		this.date = date;
	}

	public long getDate() {
		return date;
	}
	
	public boolean getSent() {
		return sent;
	}
	
	public void setSent(boolean sent) {
		this.sent = sent;
	}

	public void setYieldsId(int yields_id) {
		this.yields_id = yields_id;
	}

	public int getYieldsId() {
		return yields_id;
	}

	public void setThreadId(int thread_id) {
		this.thread_id = thread_id;
	}

	public int getThreadId() {
		return thread_id;
	}

	public void setProtocol(int protocol) {
		this.protocol = protocol;
	}

	public int getProtocol() {
		return protocol;
	}

	public void setRead(int read) {
		this.read = read;
	}

	public int getRead() {
		return read;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getStatus() {
		return status;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public void setReplyPath(int reply_path) {
		this.reply_path = reply_path;
	}

	public int getReplyPath() {
		return reply_path;
	}

	public void setServiceCenter(String service_center) {
		this.service_center = service_center;
	}

	public String getServiceCenter() {
		return service_center;
	}

	public void setPerson(String person) {
		this.person = person;
	}

	public String getPerson() {
		return person;
	}
}
