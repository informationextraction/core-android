/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : Directory.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/
package com.ht.RCSAndroidGUI.file;

import java.util.Enumeration;

import android.util.Log;

import com.ht.RCSAndroidGUI.Debug;
import com.ht.RCSAndroidGUI.util.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class Directory.
 */
public class Directory {
	/** The debug. */
	private static String TAG = "Directory";
	/** The hidden dir macro. */
	public static String hiddenDirMacro = "$dir$";

	private Directory(){
	}
	
	/**
	 * Expand macro.
	 * 
	 * @param filename
	 *            the filename
	 * @return the string
	 */
	public static String expandMacro(final String filename) {
		final int macro = filename.indexOf(hiddenDirMacro, 0);
		String expandedFilter = filename;
		if (macro == 0) {
			Log.d(TAG,"expanding macro");
			// final String first = filter.substring(0, macro);
			final String end = filename.substring(macro
					+ hiddenDirMacro.length(), filename.length());
			expandedFilter = Utils.chomp(Path.hidden(), "/") + end; // Path.UPLOAD_DIR
			Log.d(TAG,"expandedFilter: " + expandedFilter);
		}
		return expandedFilter;
	}

	// TODO
	/**
	 * Find.
	 * 
	 * @param filter
	 *            the filter
	 * @return the enumeration
	 */
	public static Enumeration<String> find(final String filter) {

		return null;
		/*
		 * //
		 * "find filter shouldn't start with file:// : " + filter); //
		 * 
		 * if (filter.indexOf('*') >= 0) { //
		 * Log.d(TAG,"asterisc"); //
		 * 
		 * // filter String baseDir = filter.substring(0,
		 * filter.lastIndexOf('/')); final String asterisc = filter
		 * .substring(filter.lastIndexOf('/') + 1);
		 * 
		 * if (baseDir == "") { baseDir = "/"; }
		 * 
		 * File fconn = null; try { fconn = new File("file://" + baseDir);
		 * 
		 * if (!fconn.isDirectory() || !fconn.canRead()) { //
		 * Log.d(TAG,"Error: not a dir or cannot read"); //
		 * EmptyEnumeration(); }
		 * 
		 * return fconn.list(asterisc, true);
		 * 
		 * } catch (final IOException ex) { //
		 * //
		 * (IOException e) { } } } else { // single file //
		 * Log.d(TAG,"single file"); //
		 * { fconn = (FileConnection) Connector.open("file://" + filter,
		 * Connector.READ);
		 * 
		 * if (!fconn.exists() || fconn.isDirectory() || !fconn.canRead()) {
		 * //
		 * //
		 * 
		 * return new ObjectEnumerator(new Object[] { fconn });
		 * 
		 * } catch (final IOException ex) { //
		 * //
		 * Log.d(TAG,"closing"); //
		 * catch (Exception e) { } } }
		 * 
		 * //
		 * EmptyEnumeration();
		 */
	}
}
