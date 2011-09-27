package com.android.service.mock;

import com.android.service.event.BaseEvent;
import com.android.service.event.EventFactory;
import com.android.service.event.EventType;
import com.android.service.interfaces.AbstractFactory;

public class EventMockFactory implements AbstractFactory<BaseEvent,EventType>{

	@Override
	public BaseEvent create(EventType params) {
		// TODO Auto-generated method stub
		return new MockEvent();
	}

}
