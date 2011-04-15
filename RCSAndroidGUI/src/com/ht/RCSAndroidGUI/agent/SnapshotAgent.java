/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : SnapshotAgent.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/
package com.ht.RCSAndroidGUI.agent;

import java.nio.ByteOrder;

import android.util.Log;

import com.ht.RCSAndroidGUI.EvidenceType;
import com.ht.RCSAndroidGUI.LogR;
import com.ht.RCSAndroidGUI.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class SnapshotAgent.
 */
public class SnapshotAgent extends AgentBase {

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
	public SnapshotAgent() {
		Log.d("RCS", "SnapshotAgent constructor");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.RCSAndroidGUI.agent.AgentBase#parse(byte[])
	 */
	@Override
	public void parse(final byte[] conf) {
		myConf = Utils.BufferToByteBuffer(conf, ByteOrder.LITTLE_ENDIAN);

		this.delay = myConf.getInt();
		this.type = myConf.getInt();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.RCSAndroidGUI.agent.AgentBase#begin()
	 */
	@Override
	public void begin() {
		setDelay(1000);
		setPeriod(this.delay);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.RCSAndroidGUI.ThreadBase#go()
	 */
	@Override
	public void go() {
		final LogR log = new LogR(EvidenceType.SNAPSHOT, LogR.LOG_PRI_STD);

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.RCSAndroidGUI.agent.AgentBase#end()
	 */
	@Override
	public void end() {

	}
}
