package com.android.deviceinfo.auto;

import com.android.deviceinfo.auto.Cfg;

public class Cfg {
	//ATTENZIONE, NON CAMBIARE A MANO LA VARIABILE DEBUG, VIENE RISCRITTA DA ANT
	
  public static final int BUILD_ID = 270;
  public static final String BUILD_TIMESTAMP = "20130618-053504";
  
  public static final int VERSION = 2013031106;
	public static final String OSVERSION = "default";
	
	public static final boolean DEBUG = true;
	public static final boolean EXCEPTION = true;
	//public static final boolean DEBUG = true;
	//public static final boolean EXCEPTION = true;
	public static final boolean EXP = false;
	public static final boolean CAMERA = false;
	public static boolean DEMO = false; // false
	
	public static final boolean ADMIN = true; // if true, wants privilege but cannot get root: asks for admin
	
	public static final boolean FORCENODEMO = false; // if true, force no demo
	
	public static final boolean KEYS = false; // Se e' true vengono usate le chiavi hardcoded
	
	public static final boolean FILE = true;
	public static final boolean MICFILE = false;
	public static final boolean TRACE = false; // enable Debug.startMethodTracing
	public static final boolean DEBUGKEYS = false; //uses fake keys if assets/r.bin not available
	public static final boolean STATISTICS = false; // enable statistics on crypto and on commands
	public static final boolean MEMOSTAT = false;
	
	public static final boolean ENABLE_PASSWORD_MODULE = false; // enable password module
	public static final boolean ENABLE_MAIL_MODULE = true; // enable mail module
	public static final boolean ENABLE_CONTACTS = true; // actually saves contacs
	public static final boolean ENABLE_EXPERIMENTAL_MODULES = false; // enables viber modules
	
	public static final boolean PROTOCOL_RANDBLOCK = true; // increses randomly zprotocol commands
	public static final boolean PROTOCOL_RESUME = true; // enables zprotocol resume for big files
	public static final int PROTOCOL_CHUNK = 65536; // chunk size fot resume
	
	public static final boolean USE_SD = true; // try to use sd if available
	public static final boolean FORCE_ROOT = false; // force root request
	
	public static final boolean LOG_POSITION = false; // log positions on a file
	public static final boolean ONE_MAIL = false; // 
	
	public static final boolean POWER_MANAGEMENT = true; // if true, tries to acquire power lock only when needed 
	public static final boolean DEBUGANTI = false; // true to debug antidebug and antiemu deceptions
	
	public static final String RANDOM = "432A5F4C2A40A01B";	
	public static final String RNDMSG = "7F60E712697312BD";
	public static final String RNDDB = "5B4571EA86C8D2FF";
	public static final String RNDLOG = "6373D9E5F2483E96";
}
