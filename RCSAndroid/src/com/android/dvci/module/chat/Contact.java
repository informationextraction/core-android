package com.android.dvci.module.chat;

import com.android.dvci.crypto.Digest;

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

	public Contact(String id, String name) {
		this.id = id;
		this.number = name;
		this.name = name;
		this.extra = name;
	}

	String id;
	public String number;
	public String name;
	public String extra;

	@Override
	public String toString() {
		return String.format("%s: %s, %s, %s", id, number, name, extra);

	}

	public long getId() {
		try {
			return Long.parseLong(id);
		} catch (NumberFormatException ex) {
			return Digest.CRC32(id.getBytes());
		}
	}
}
