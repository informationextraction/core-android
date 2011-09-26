/* **********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 03-dec-2010
 **********************************************/

package com.android.service.conf;

import org.json.JSONObject;

/**
 * The Class Option.
 */
public class Global {

	/** Option ID. */
	private final int optionId;

	/** Parameters. */
	private final JSONObject optionParams;

	/**
	 * Instantiates a new option.
	 * 
	 * @param id
	 *            the id
	 * @param jglobal
	 *            the params
	 */
	public Global(final int id, final JSONObject jglobal) {
		this.optionId = id;
		this.optionParams = jglobal;
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public int getId() {
		return this.optionId;
	}


}
