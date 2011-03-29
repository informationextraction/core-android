package com.ht.RCSAndroidGUI;

import java.nio.ByteOrder;

import android.util.Log;

public class DeviceAgent extends AgentBase {
	private int processList;

	public DeviceAgent() {
		Log.d("Que", "DeviceAgent constructor");
	}
	
	public void begin() {
		setDelay(250);
	}
	
	public void go() {
		LogR log = new LogR(Agent.AGENT_DEVICE, LogR.LOG_PRI_STD);
		
		// OS Version etc...
		Log.d("Que", "Device Agent: logging something");
		
		if (processList == 1) {
			
		}
		
		log.close();
	}
	
	public void end() {
		
	}
	
	public void parse(byte[] conf) {
		myConf = Utils.BufferToByteBuffer(conf, ByteOrder.LITTLE_ENDIAN);
		
		this.processList = myConf.getInt();
	}
}
