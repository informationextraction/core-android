/* **********************************************
 * Create by : Alberto "Q" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 01-dec-2010
 **********************************************/

package com.android.networking.conf;

import org.json.JSONObject;


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
