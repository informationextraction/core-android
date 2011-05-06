/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : LogAction.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.ht.RCSAndroidGUI.action;

import java.io.IOException;

import android.util.Log;

import com.ht.RCSAndroidGUI.LogR;
import com.ht.RCSAndroidGUI.evidence.Evidence;
import com.ht.RCSAndroidGUI.evidence.EvidenceType;
import com.ht.RCSAndroidGUI.util.DataBuffer;
import com.ht.RCSAndroidGUI.util.WChar;

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
			io.printStackTrace();
		}

		return true;
	}
}
