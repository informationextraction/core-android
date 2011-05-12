/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : ExtensionFilter.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service;

import java.io.File;
import java.io.FilenameFilter;

// TODO: Auto-generated Javadoc
/**
 * The Class ExtensionFilter.
 */
public class ExtensionFilter implements FilenameFilter {

	/** The ext. */
	String ext;

	/**
	 * Instantiates a new extension filter.
	 * 
	 * @param extension
	 *            the extension
	 */
	public ExtensionFilter(final String extension) {
		ext = extension;
	}

	// @Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
	 */
	public boolean accept(final File dir, final String name) {
		return (name.endsWith(ext));
	}
}
