package com.android.service.mock;

import com.android.service.event.EventBase;
import com.android.service.event.EventFactory;
import com.android.service.event.EventType;
import com.android.service.interfaces.AbstractFactory;

public class EventMockFactory implements AbstractFactory<EventBase,EventType>{

	@Override
	public EventBase create(EventType params) {
		// TODO Auto-generated method stub
		return new MockEvent();
	}

}
