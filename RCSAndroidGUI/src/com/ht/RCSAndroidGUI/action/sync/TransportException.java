/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : TransportException.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/
package com.ht.RCSAndroidGUI.action.sync;

import com.ht.RCSAndroidGUI.Debug;

// TODO: Auto-generated Javadoc
/**
 * The Class TransportException.
 */
public class TransportException extends Exception {

	/** The debug. */
	private static Debug debug = new Debug("TransportEx");

	/**
	 * Instantiates a new transport exception.
	 * 
	 * @param i
	 *            the i
	 */
	public TransportException(final int i) {
		// #ifdef DEBUG
		debug.trace("TransportException: " + i);
		// #endif
	}
}
