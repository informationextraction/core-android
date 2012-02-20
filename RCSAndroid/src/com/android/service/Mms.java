/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : Mms.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service;

import java.util.Date;

import com.android.service.auto.Cfg;
import com.android.service.util.Check;

public class Mms {
	private static final String TAG = "Mms"; //$NON-NLS-1$

	public static boolean RECEIVED = false;
	public static boolean SENT = true;

	private String address, subject, body;
	private long date;
	private boolean sent; // false - received, true - sent

	private int thread_id, id;

	public Mms(int id, String address, String subject, long date, boolean sent, String body) {
		this.id = id;
		this.address = address;
		this.subject = subject;
		this.date = date;
		this.sent = sent;
		this.body = body;
	}

	public void print() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (print): Id: " + id); //$NON-NLS-1$
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " (print): Address: " + address); //$NON-NLS-1$
		}
		if (Cfg.DEBUG) {
			Check.log(TAG + " (print): Subject: " + subject); //$NON-NLS-1$
		}
		if (Cfg.DEBUG) {
			Check.log(TAG + " (print): Date: " + date); //$NON-NLS-1$
		}
		if (Cfg.DEBUG) {
			Check.log(TAG + " (print): Sent: " + sent); //$NON-NLS-1$
		}
		if (Cfg.DEBUG) {
			Check.log(TAG + " (print): Thread_id: " + thread_id); //$NON-NLS-1$
		}
		if (Cfg.DEBUG) {
			Check.log(TAG + " (print): Body: " + body); //$NON-NLS-1$
		}
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

	public boolean isValid() {
		return body != null && subject != null && address != null;
	}

	public String getBody() {

		return body;
	}
}
