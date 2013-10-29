package com.android.deviceinfo.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.android.deviceinfo.Status;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.conf.Configuration;
import com.android.m.M;

public class Execute {
	private static final String TAG = "Execute";

	public static ExecuteResult executeRoot(String command) {
		String cmd = command;
		
		if (Status.haveRoot()) {
			cmd = String.format("%s %s %s", Configuration.shellFile, M.e("qzx"), command); // EXPORT
		}
		
		return execute(cmd);
	}

	public static ExecuteResult execute(String cmd) {
		String line = null;
		// ArrayList<String> fullResponse = new ArrayList<String>();

		Process localProcess = null;
		ExecuteResult result = new ExecuteResult(cmd);
		
		if (Cfg.DEBUG) {
			Check.log(TAG + " (execute) executing: " + cmd); //$NON-NLS-1$
		}
		
		try {
			localProcess = Runtime.getRuntime().exec(cmd);
		} catch (Exception e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}
		}

		if (localProcess != null) {
			try {
				// 
				BufferedReader in = new BufferedReader(new InputStreamReader(localProcess.getInputStream()));

				while ((line = in.readLine()) != null) {
					result.stdout.add(line);
				}

				in.close();
			
				result.exitCode = localProcess.waitFor();
				
				BufferedReader err = new BufferedReader(new InputStreamReader(localProcess.getErrorStream()));
				while ((line = err.readLine()) != null) {
					result.stderr.add(line);
				}
				err.close();
				
			} catch (Exception e) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (execute) Error: " + e);
				}
			}
		}

		return result;
	}
}