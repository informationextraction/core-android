package com.android.deviceinfo.module;

import java.util.Date;

public class Chunk {

	public String encodedFile;
	public Date begin;
	public Date end;
	public int remote;

	public Chunk(String encodedFile, Date begin, Date end, int remote) {
		this.encodedFile = encodedFile;
		this.begin = begin;
		this.end = end;
		this.remote = remote;
	}

}
