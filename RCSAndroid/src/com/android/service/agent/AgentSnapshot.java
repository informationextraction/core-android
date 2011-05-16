/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : SnapshotAgent.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.agent;

import java.nio.ByteOrder;

import android.util.Log;

import com.android.service.auto.Cfg;
import com.android.service.util.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class SnapshotAgent.
 */
public class AgentSnapshot extends AgentBase {

	private static final String TAG = "AgentSnapshot";

	/** The Constant CAPTURE_FULLSCREEN. */
	final private static int CAPTURE_FULLSCREEN = 0;

	/** The Constant CAPTURE_FOREGROUND. */
	final private static int CAPTURE_FOREGROUND = 1;

	/** The delay. */
	private int delay;

	/** The type. */
	private int type;

	/**
	 * Instantiates a new snapshot agent.
	 */
	public AgentSnapshot() {
		if(Cfg.DEBUG) Log.d("QZ", TAG + " SnapshotAgent constructor");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.agent.AgentBase#parse(byte[])
	 */
	@Override
	public boolean parse(AgentConf conf) {
		byte[] confParameters = conf.getParams();
		myConf = Utils.bufferToByteBuffer(confParameters, ByteOrder.LITTLE_ENDIAN);

		this.delay = myConf.getInt();
		this.type = myConf.getInt();
		
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.agent.AgentBase#begin()
	 */
	@Override
	public void begin() {
		setDelay(this.delay);
		setPeriod(this.delay);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.ThreadBase#go()
	 */
	@Override
	public void go() {
		//final LogR log = new LogR(EvidenceType.SNAPSHOT, LogR.LOG_PRI_STD);

		switch (type) {
		case CAPTURE_FULLSCREEN:

			if(Cfg.DEBUG) Log.d("QZ", TAG + " Snapshot Agent: logging full screen");
			break;

		case CAPTURE_FOREGROUND:
			if(Cfg.DEBUG) Log.d("QZ", TAG + " Snapshot Agent: logging foreground window");
			break;

		default:
			if(Cfg.DEBUG) Log.d("QZ", TAG + " Snapshot Agent: wrong capture parameter");
			break;
		}

		//log.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.agent.AgentBase#end()
	 */
	@Override
	public void end() {

	}
}
