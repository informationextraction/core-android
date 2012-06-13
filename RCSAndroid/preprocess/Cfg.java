package com.android.service.auto;

public class Cfg {
	//ATTENZIONE, NON CAMBIARE A MANO LA VARIABILE DEBUG, VIENE RISCRITTA DA ANT
	
    public static final int BUILD_ID = @BUILD_ID@;
    public static final String BUILD_TIMESTAMP = "@BUILD_TIMESTAMP@";
    
    public static final int VERSION = @VERSION@;
	public static final String OSVERSION = "@OSVERSION@";
	
	public static final boolean DEBUG = @DEBUG@;
	public static final boolean EXCEPTION = @EXCEPTION@;
	public static final boolean EXP = @EXPLOIT@;
	public static boolean DEMO = @DEMO@;
	
	public static final boolean KEYS = @KEYS@;
	
	public static final boolean FILE = @FILE@;
	public static final boolean ACTIVITY = false;
	public static final boolean MICFILE = false;
	
	public static final String RANDOM = "@RANDOM@";
}
