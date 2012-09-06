/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : Directory.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.networking.file;

import java.util.Enumeration;

import com.android.networking.Messages;
import com.android.networking.auto.Cfg;
import com.android.networking.util.Check;
import com.android.networking.util.StringUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class Directory.
 */
public class Directory {
	/** The debug. */
	private static final String TAG = "Directory"; //$NON-NLS-1$

	/** The hidden dir macro. */
	public final static String hiddenDirMacro = Messages.getString("23.0"); //$NON-NLS-1$
	public final static String userProfile = Messages.getString("23.1"); //$NON-NLS-1$
	public final static String userDoc = Messages.getString("23.2"); //$NON-NLS-1$
	public final static String userPicture = Messages.getString("23.3"); //$NON-NLS-1$

	public static String expandMacro(String file) {
		// expanding $dir$ && $userprofile$

		file = Directory.expandMacro(file, hiddenDirMacro, Path.hidden());
		file = Directory.expandMacro(file, userProfile, Path.home());
		file = Directory.expandMacro(file, userDoc, Path.doc());
		file = Directory.expandMacro(file, userPicture, Path.picture());
		return file;
	}

	private static String expandMacro(String filename, String expand, String newdir) {
		final int macro = filename.indexOf(expand, 0);
		String expandedFilter = filename;
		if (macro == 0) {

			// final String first = filter.substring(0, macro);
			final String end = filename.substring(macro + expand.length(), filename.length());
			expandedFilter = StringUtils.chomp(newdir, "/") + end; //$NON-NLS-1$

			if (Cfg.DEBUG) {
				Check.log(TAG + " (expandMacro) expandedFilter: " + expandedFilter);
			}

		}
		return expandedFilter;
	}

	/**
	 * Transforms "something $dir$/other/" to "something /path/to/hidden/other/"
	 * 
	 * @param filename
	 *            the filename
	 * @return the string
	 */
	public static String expandHiddenDir(final String filename) {
		final int macro = filename.indexOf(hiddenDirMacro, 0);
		String expandedFilter;

		if (macro == -1) {
			return filename;
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " expanding macro");
		}

		expandedFilter = filename.replaceFirst("\\$dir\\$", StringUtils.chomp(Path.hidden(), "/"));

		if (Cfg.DEBUG) {
			Check.log(TAG + " expandedFilter: " + expandedFilter);
		}

		return expandedFilter;
	}

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
		 * // "find filter shouldn't start with file:// : " + filter); //
		 * 
		 * if (filter.indexOf('*') >= 0) { // if(AutoConfig.DEBUG) Check.log
		 * ;//$NON-NLS-1$ TAG + " asterisc"); //
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
		 * if(AutoConfig.DEBUG) Check.log( TAG ;//$NON-NLS-1$
		 * " Error: not a dir or cannot read"); // EmptyEnumeration(); }
		 * 
		 * return fconn.list(asterisc, true);
		 * 
		 * } catch (final IOException ex) { // // (IOException e){
		 * if(Cfg.EXCEPTION){Check.log(e);}
		 * 
		 * // single file // if(AutoConfig.DEBUG) Check.log( TAG ;//$NON-NLS-1$
		 * " single file"); // { fconn = (FileConnection)
		 * Connector.open("file://" + filter, Connector.READ);
		 * 
		 * if (!fconn.exists() || fconn.isDirectory() || !fconn.canRead()) { //
		 * //
		 * 
		 * return new ObjectEnumerator(new Object[] { fconn });
		 * 
		 * } catch (final IOException ex){ if(Cfg.EXCEPTION){Check.log(ex);} //
		 * // if(AutoConfig.DEBUG) Check.log( TAG + " closing"); // catch
		 * (Exception e){ if(Cfg.EXCEPTION){Check.log(e);} } } ;//$NON-NLS-1$
		 * 
		 * // EmptyEnumeration();
		 */
	}
}
