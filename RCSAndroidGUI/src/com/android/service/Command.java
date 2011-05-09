/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : Command.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/


package com.android.service;
import java.util.LinkedList;
import java.util.Queue;

import android.os.Message;
import android.util.Log;

/**
 * The Class Command.
 */
public class Command {

	private static final String TAG = "Command";
	
	/** The msg queue. */
	private final Queue<Message> msgQueue;

	/**
	 * Instantiates a new command.
	 */
	public Command() {
		msgQueue = new LinkedList<Message>();
		msgQueue.clear();
	}

	/**
	 * Push message.
	 * 
	 * @param msg
	 *            the msg
	 */
	public synchronized void pushMessage(final Message msg) {
		if (msg == null) {
			Log.d("QZ", TAG + " Command queue is null");
			return;
		}

		msgQueue.add(msg);
	}

	// Returns null if the queue is empty
	/**
	 * Gets the message.
	 * 
	 * @return the message
	 */
	public synchronized Message getMessage() {
		final Message m = msgQueue.poll();

		return m;
	}
}
