package com.android.deviceinfo.conf;

import org.json.JSONException;
import org.json.JSONObject;

public class ConfAction extends JSONConf {
	private static final String TAG = "ActionConf";
	public int subActionId;
	public int actionId;

	public ConfAction(int actionId, int subActionId, String type, JSONObject params) {
		super(type, params);
		this.subActionId = subActionId;
		this.actionId = actionId;
	}

	public ConfAction(int actionId, int subActionId, JSONObject params) throws JSONException {
		super(params.getString("action"), params);
		this.subActionId = subActionId;
		this.actionId = actionId;
	}

	int getId() {
		return subActionId;
	}

	int getActionId() {
		return actionId;
	}



}
