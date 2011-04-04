package com.ht.RCSAndroidGUI.action.sync;

import com.ht.RCSAndroidGUI.Debug;

public class TransportException extends Exception {
	 private static Debug debug = new Debug("TransportEx");
	 public TransportException(int i) {
	        //#ifdef DEBUG
	        debug.trace("TransportException: " + i);
	        //#endif
	    }
}
