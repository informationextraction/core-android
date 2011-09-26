package com.android.service.action;

import org.json.JSONObject;

import com.android.service.auto.Cfg;
import com.android.service.conf.ConfigurationException;
import com.android.service.conf.JSONConf;
import com.android.service.util.Check;

public class ActionConf extends JSONConf {
	private static final String TAG = "ActionConf";
	private int subActionId;
	private int actionId;
	
	public ActionConf(int actionId, int subActionId, String type, JSONObject params) {		
		super(type, params);
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
