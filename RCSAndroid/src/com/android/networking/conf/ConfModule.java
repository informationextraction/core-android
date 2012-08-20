/* **********************************************
 * Create by : Alberto "Q" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 01-dec-2010
 **********************************************/

package com.android.networking.conf;

import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.networking.auto.Cfg;
import com.android.networking.util.Check;


// TODO: Auto-generated Javadoc
/**
 * The Class Agent.
 */
public class ConfModule extends JSONConf {

	private static final String TAG = "AgentConf";

	/**
	 * Instantiates a new agent.
	 * 
	 * @param moduleId
	 *            the id
	 * @param jmodule
	 *            the params
	 */
	public ConfModule(final String moduleType, final JSONObject jmodule) {
		super(moduleType, jmodule);
	}
}
