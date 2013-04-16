package com.android.networking.module.chat;

public class Contact {
	public Contact(String id) {
		this.id = id;
		this.number = id;
	}
	
	public Contact(String id, String number, String name, String extra) {
		this.id = id;
		this.number = number;
		this.name = name;
		this.extra = extra;
	}
	
	public String id;
	public String number;
	public String name;
	public String extra;
}
