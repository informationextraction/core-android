package com.android.deviceinfo;

import java.lang.ref.WeakReference;

import com.android.deviceinfo.event.BaseEvent;

public class Trigger {
	private int actionId;
	private WeakReference<BaseEvent> event;

	public Trigger(int actionId, BaseEvent event) {
		this.actionId = actionId;
		this.event = new WeakReference<BaseEvent>(event);
	}

	public int getActionId() {
		return actionId;
	}

	public BaseEvent getEvent() {
		return event.get();
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
