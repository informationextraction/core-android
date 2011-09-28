package com.android.service.conf;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.service.auto.Cfg;
import com.android.service.util.Check;

public class ConfAction extends JSONConf {
	private static final String TAG = "ActionConf";
	private int subActionId;
	private int actionId;
	
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

	int getId(){
		return subActionId;
	}
	
	int getActionId(){
		return actionId;
	}



}
