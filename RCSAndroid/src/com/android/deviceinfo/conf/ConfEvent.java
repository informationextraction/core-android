/* *********************************************
 * Create by : Alberto "Q" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 01-dec-2010
 **********************************************/

package com.android.deviceinfo.conf;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.deviceinfo.action.Action;

// TODO: Auto-generated Javadoc
/**
 * The Class Event.
 */
public class ConfEvent extends JSONConf {

	/** Event unique ID. */
	private final int eventId;

	public int startAction = Action.ACTION_NULL;
	public int endAction = Action.ACTION_NULL;
	public int repeatAction = Action.ACTION_NULL;
	public int iter = Integer.MAX_VALUE;
	/** delay in seconds */
	public int delay = 0;

	final public String desc;

	public boolean enabled;

	public ConfEvent(int eventId, String eventType, JSONObject params) throws JSONException {
		super(eventType, params);

		this.eventId = eventId;

		if (params.has("start")) {
			startAction = params.getInt("start");
		}
		if (params.has("end")) {
			endAction = params.getInt("end");
		}
		if (params.has("repeat")) {
			repeatAction = params.getInt("repeat");
		}
		if (params.has("iter")) {
			iter = params.getInt("iter");
		}
		if (params.has("delay")) {
			delay = params.getInt("delay");
		}

		desc = params.getString("desc");
		enabled = params.getBoolean("enabled");
	}

	public ConfEvent(int id, JSONObject conf) throws JSONException {
		this(id, conf.getString("event"), conf);
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public int getId() {
		return this.eventId;
	}

}
