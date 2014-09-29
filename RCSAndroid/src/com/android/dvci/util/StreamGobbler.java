package com.android.dvci.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import com.android.dvci.auto.Cfg;

public class StreamGobbler extends Thread {
	private static final String TAG = "StreamGobbler";
	InputStream is;
	String type;
	OutputStream os;

	public StreamGobbler(InputStream is, String type) {
		this(is, type, null);
	}

	StreamGobbler(InputStream is, String type, OutputStream redirect) {
		this.is = is;
		this.type = type;
		this.os = redirect;
	}

	public void run() {
		try {
			PrintWriter pw = null;
			if (os != null)
				pw = new PrintWriter(os);

			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			
			while ((line = br.readLine()) != null) {
				if (pw != null)
					pw.println(line);
				if (Cfg.DEBUG) {
					Check.log(TAG + " (run): " + type + ">" + line);
				}

			}
			if (pw != null)
				pw.flush();
		} catch (IOException ioe) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (run) Error: " + ioe);
			}
		}
	}
}
