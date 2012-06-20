/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : Command.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service;

import java.util.LinkedList;
import java.util.Queue;

import android.os.Message;

import com.android.service.auto.Cfg;
import com.android.service.util.Check;

/**
 * The Class Command.
 */
public class Command {
	private static final String TAG = "Command"; //$NON-NLS-1$

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
			if (Cfg.DEBUG) {
				Check.log(TAG + " Command queue is null"); //$NON-NLS-1$
			}
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
