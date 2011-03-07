package com.ht.RCSAndroidGUI;

public class Packet {
	private int type, command, priority;
	private long id;
	private byte[] data;
	
	public Packet(long unique) {
		type = 0;
		command = 0;
		id = unique;
		data = null;
	}
	
	public long getId() {
		return id;
	}
	
	public void setCommand(int c) {
		command = c;
	}
	
	public int getCommand() {
		return command;
	}

	public void setPriority(int p) {
		priority = p;
	}
	
	public int getPriority() {
		return priority;
	}

	// Needed only when sending LOG_CREATE
	public void setType(int t) {
		type = t;
	}
	
	public int getType() {
		return type;
	}
	
	public void fill(byte[] d) {
		data = d;
	}
	
	public byte[] peek() {
		return data;
	}
}
