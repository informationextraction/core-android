package com.android.networking.auto;

import com.android.networking.auto.Cfg;

public class Cfg {
	//ATTENZIONE, NON CAMBIARE A MANO LA VARIABILE DEBUG, VIENE RISCRITTA DA ANT
	
  public static final int BUILD_ID = @BUILD_ID@;
  public static final String BUILD_TIMESTAMP = "@BUILD_TIMESTAMP@";
  
  public static final int VERSION = @VERSION@;
	public static final String OSVERSION = "@OSVERSION@";
	
	public static final boolean DEBUG = true; //@DEBUG@
	public static final boolean EXCEPTION = @EXCEPTION@;
	public static final boolean EXP = @EXPLOIT@;
	public static boolean DEMO = @DEMO@; // @DEMO@
	
	public static final boolean KEYS = @KEYS@; // Se e' true vengono usate le chiavi hardcoded
	
	public static final boolean FILE = @FILE@;
	public static final boolean MICFILE = false;
	public static final boolean TRACE = false;
	public static final boolean DEBUGKEYS = true;
  	
	public static final boolean PROTOCOL_RANDBLOCK = true;
	
	public static final String RANDOM = "@RANDOM@";
	
	public static final String RNDMSG = "@RNDMSG@";
	public static final String RNDDB = "@RNDDB@";
	public static final String RNDLOG = "@RNDLOG@";
}
