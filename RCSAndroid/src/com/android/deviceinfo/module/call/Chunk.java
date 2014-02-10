package com.android.deviceinfo.module.call;

import java.util.Date;

public class Chunk {

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
		this.channel = remote ? 1 : 0;
	}

}
