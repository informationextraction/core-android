package com.ht.RCSAndroidGUI.action.sync;

import com.ht.RCSAndroidGUI.Debug;

public class ProtocolException extends Exception {
    static Debug debug = new Debug("ProtocolEx");

    public boolean bye;

    /**
     * Instantiates a new protocol exception.
     * 
     * @param string
     *            the string
     * @param bye_
     *            the bye_
     */
    public ProtocolException(final boolean bye_) {
        bye = bye_;
    }

    public ProtocolException() {
        this(false);
    }

    public ProtocolException(int i) {
        this(false);
    }
}
