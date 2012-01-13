/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : CellInfo.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service;


public class CellInfo {
	public int mcc = -1;
	public int mnc = -1; // sid
	public int lac = -1; // nid
	public int cid = -1; // bid

	public int sid;
	public int nid;
	public int bid;

	public int rssi;
	public boolean valid;
	public boolean gsm;
	public boolean cdma;
	
	public void setGsm(int mcc, int mnc, int lac, int cid, int rssi) {
		gsm = true;
		cdma = false;
		valid = true;

		this.rssi = rssi;
		
		this.mcc = mcc;
		this.mnc = mnc;
		this.lac = lac;
		this.cid = cid;
	}

	public void setCdma(int sid, int nid, int bid, int rssi) {
		gsm = false;
		cdma = true;
		valid = true;
		
		this.rssi = rssi;

		this.sid = sid;
		this.nid = nid;
		this.bid = bid;
		
		this.mnc=sid;
		this.lac=nid;
		this.cid=bid;

	}

	public String toString() {
	
		final StringBuffer mb = new StringBuffer();
	
		if (gsm) {
			mb.append("MCC: " + mcc);
			mb.append(" MNC: " + mnc);
			mb.append(" LAC: " + lac);
			mb.append(" CID: " + cid);
		}
	
		if (cdma) {
			mb.append("SID: " + sid);
			mb.append(" NID: " + nid);
			mb.append(" BID: " + bid);
		}
	
		return mb.toString();
	}
}
