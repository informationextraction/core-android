/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : LogAction.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.action;

import java.io.IOException;

import com.android.service.auto.Cfg;
import com.android.service.evidence.Evidence;
import com.android.service.util.Check;
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
	public LogAction(final int type, final byte[] confParams) {
		super(type, confParams);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.action.SubAction#execute()
	 */
	@Override
	public boolean execute() {
		Evidence.info(msg);

		return true;
	}

	@Override
	protected boolean parse(final byte[] params) {
		try {
			final DataBuffer db = new DataBuffer(params);

			// Message length
			final byte buffer[] = new byte[db.readInt()];

			db.read(buffer);

			this.msg = WChar.getString(buffer, true);
		} catch (final IOException io) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "parse() exception");
			}
			if (Cfg.DEBUG) {
				Check.log(io);
			}
		}

		return true;
	}
}
