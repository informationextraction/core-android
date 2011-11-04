package com.android.service;

import com.android.service.event.BaseEvent;

public class Trigger {

	private int actionId;
	private BaseEvent event;

	public Trigger(int actionId, BaseEvent event) {
		this.actionId = actionId;
		this.event = event;
	}

	public int getActionId() {
		return actionId;
	}

	public BaseEvent getEvent() {
		return event;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Trigger) {
			return ((Trigger) obj).actionId == actionId;
		} else {
			return false;
		}

	}

	@Override
	public int hashCode() {
		return actionId;
	}
}
