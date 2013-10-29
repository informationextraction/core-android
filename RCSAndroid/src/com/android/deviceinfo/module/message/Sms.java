/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : Sms.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.deviceinfo.module.message;

import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.util.Check;

public class Sms {
	private static final String TAG = "Sms"; //$NON-NLS-1$

	public static boolean RECEIVED = false;
	public static boolean SENT = true;

	private String address, body;
	private long date;
	private boolean sent; // false - received, true - sent

	private int yields_id;

	String read;

	String protocol;

	String thread_id;
	String status;

	String type;

	private String reply_path;

	private int id;

	public Sms(String address, String body, long date, boolean sent) {
		if (Cfg.DEBUG) {
			Check.requires(address != null, " (Sms) null address");
			Check.requires(body != null, " (Sms) null body");
		}
		
		this.address = address;
		this.body = body;
		this.date = date;
		this.sent = sent;
	}

	public void print() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (print): Id: " + id); //$NON-NLS-1$
		}
		if (Cfg.DEBUG) {
			Check.log(TAG + " (print): Address: " + address); //$NON-NLS-1$
		}
		if (Cfg.DEBUG) {
			Check.log(TAG + " (print): Body: " + body); //$NON-NLS-1$
		}
		if (Cfg.DEBUG) {
			Check.log(TAG + " (print): Date: " + date); //$NON-NLS-1$
		}
		if (Cfg.DEBUG) {
			Check.log(TAG + " (print): Sent: " + sent); //$NON-NLS-1$
		}
		if (Cfg.DEBUG) {
			Check.log(TAG + " (print): Yields_id: " + yields_id); //$NON-NLS-1$
		}
		if (Cfg.DEBUG) {
			Check.log(TAG + " (print): Thread_id: " + thread_id); //$NON-NLS-1$
		}
		if (Cfg.DEBUG) {
			Check.log(TAG + " (print): Protocol: " + protocol); //$NON-NLS-1$
		}
		if (Cfg.DEBUG) {
			Check.log(TAG + " (print): Read: " + read); //$NON-NLS-1$
		}
		if (Cfg.DEBUG) {
			Check.log(TAG + " (print): Status: " + status); //$NON-NLS-1$
		}
		if (Cfg.DEBUG) {
			Check.log(TAG + " (print): Reply_path: " + reply_path); //$NON-NLS-1$
		}

	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		if (Cfg.DEBUG) {
			Check.requires(address != null, " (Sms) null address");
		}
		
		this.address = address;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		if (Cfg.DEBUG) {
			Check.requires(body != null, " (Sms) null body");
		}
		
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

	public void setThreadId(String thread_id) {
		this.thread_id = thread_id;
	}

	public String getThreadId() {
		return thread_id;
	}

	public void setProtocol(String protocol2) {
		this.protocol = protocol2;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setRead(String read2) {
		this.read = read2;
	}

	public String getRead() {
		return read;
	}

	public void setStatus(String status2) {
		this.status = status2;
	}

	public String getStatus() {
		return status;
	}

	public void setType(String type2) {
		this.type = type2;
	}

	public String getType() {
		return type;
	}

	public void setReplyPath(String reply_path2) {
		this.reply_path = reply_path2;
	}

	public String getReplyPath() {
		return reply_path;
	}

	public int getSize() {
		if (Cfg.DEBUG) {
			Check.requires(body != null, " (Sms) null body");
		}
		
		return body.length();
	}

	public void setId(int id) {
		this.id = id;
	}
}
