package com.ht.RCSAndroidGUI.mock;

import com.ht.RCSAndroidGUI.event.EventBase;
import com.ht.RCSAndroidGUI.event.EventFactory;
import com.ht.RCSAndroidGUI.event.EventType;
import com.ht.RCSAndroidGUI.interfaces.AbstractFactory;

public class EventMockFactory implements AbstractFactory<EventBase,EventType>{

	@Override
	public EventBase create(EventType params) {
		// TODO Auto-generated method stub
		return new MockEvent();
	}

}
