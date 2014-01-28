package com.android.deviceinfo.module.chat;

import java.util.Date;

public class CallInfo {

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
	
	public String getCaller() {
		if(!incoming){
			return account;
		}
		return peer;
	}
	public String getCallee() {
		if(incoming){
			return account;
		}
		return peer;
	}

	

}
