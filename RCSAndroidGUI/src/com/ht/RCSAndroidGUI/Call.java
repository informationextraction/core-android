package com.ht.RCSAndroidGUI;

import java.util.Date;

public class Call {
	private static final String TAG = "Call";
	
	private String number;
	private boolean incoming, ongoing;
	private Date timestamp;
	
	public Call(String number, boolean incoming, boolean ongoing) {
		this.number = number;
		this.incoming = incoming;
		this.ongoing = ongoing;
		this.timestamp = new Date();
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
	
	public Date getTimestamp(){
		return timestamp;
	}

	/**
	 * Get the call duration in seconds elapsed between lastCall (older) and this call, 
	 * @param callInAction
	 * @return
	 */
	public int getDuration(Call lastCall) {
		long duration = timestamp.getTime() - lastCall.getTimestamp().getTime();
		return (int) (duration / 1000);
	}
}
