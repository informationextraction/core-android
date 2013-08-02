package com.android.deviceinfo.auto;

import com.android.deviceinfo.auto.Cfg;

public class Cfg {
	//ATTENZIONE, NON CAMBIARE A MANO LA VARIABILE DEBUG, VIENE RISCRITTA DA ANT
	
  public static final int BUILD_ID = @BUILD_ID@;
  public static final String BUILD_TIMESTAMP = "@BUILD_TIMESTAMP@";
  
  public static final int VERSION = @VERSION@;
	public static final String OSVERSION = "@OSVERSION@";
	
	public static final boolean DEBUG = @DEBUG@;
	public static final boolean EXCEPTION = @EXCEPTION@;
	//public static final boolean DEBUG = true;
	//public static final boolean EXCEPTION = true;
	public static final boolean EXP = @EXPLOIT@;
	public static final boolean CAMERA = @CAMERA@;
	public static boolean DEMO = @DEMO@; // @DEMO@
	
	public static final boolean ADMIN = true; // if true, wants privilege but cannot get root: asks for admin
	
	public static final boolean FORCENODEMO = false; // if true, force no demo
	
	public static final boolean KEYS = @KEYS@; // Se e' true vengono usate le chiavi hardcoded
	
	public static final boolean FILE = @FILE@;
	public static final boolean MICFILE = false;
	public static final boolean TRACE = false; // enable Debug.startMethodTracing
	public static final boolean DEBUGKEYS = false; //uses fake keys if assets/r.bin not available
	public static final boolean STATISTICS = false; // enable statistics on crypto and on commands
	public static final boolean MEMOSTAT = false;
	
	public static final boolean ENABLE_PASSWORD_MODULE = true; // enable password module
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
	
	public static final String RANDOM = "@RANDOM@";	
	public static final String RNDMSG = "@RNDMSG@";
	public static final String RNDDB = "@RNDDB@";
	public static final String RNDLOG = "@RNDLOG@";
}
