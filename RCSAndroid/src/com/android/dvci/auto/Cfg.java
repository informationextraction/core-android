package com.android.dvci.auto;

import com.android.dvci.auto.Cfg;

public class Cfg {
	//ATTENZIONE, NON CAMBIARE A MANO LA VARIABILE DEBUG, VIENE RISCRITTA DA ANT

	public static final int BUILD_ID = 285;
	public static final String BUILD_TIMESTAMP = "20140626-155817";

	public static final int VERSION = 2014063001;
	public static final String OSVERSION = "default";

	public static final boolean DEBUG = true;
	public static final boolean EXCEPTION = true;
	//public static final boolean DEBUG = true;
	//public static final boolean EXCEPTION = true;

	public static final boolean CAMERA = false;
	public static boolean DEMO = false; // false
	public static final boolean DEMO_INITSOUND = false;

	public static final boolean KEYS = false; // Se e' true vengono usate le chiavi hardcoded

	public static boolean FILE = true;
	public static final boolean MICFILE = false;
	public static final boolean TRACE = false; // enable Debug.startMethodTracing
	public static final boolean DEBUGKEYS = false; //uses fake keys if assets/r.bin not available
	public static final boolean STATISTICS = false; // enable statistics on crypto and on commands

	public static final boolean ENABLE_EXPERIMENTAL_MODULES = false; // enables viber modules
	public static final boolean ENABLE_WIFI_DISABLE = false;
	public static boolean SUPPORT_CYANOGENMOD = false;
	public static final boolean DELAY_SKYPE_CALL = false;

	public static final int PROTOCOL_CHUNK = 256 * 1024; // chunk size fot resume
	public static final int EV_QUEUE_LEN = 8;
	public static final int EV_BLOCK_SIZE = 256 * 1024;
	public static final int MAX_ASKED_SU = 3; // maximum number of su ask

	public static final boolean USE_SD = true; // try to use sd if available
	public static final boolean FORCE_ROOT = false; // force root request

	public static final boolean ONE_MAIL = false; // retrieve only one mail
	public static final boolean ADJUST_OOM_ONCE = false;

	public static final boolean POWER_MANAGEMENT = true; // if true, tries to acquire power lock only when needed
	public static final boolean DEBUGANTI = false; // true to debug antidebug and antiemu deceptions

	public static final String RANDOM = "B684F4B09371ED29";
	public static final String RNDMSG = "C7CBE59B5EED0CFE";
	public static final String RNDDB = "E31859D2F4FC257B";
	public static final String RNDLOG = "C87E55A869702981";
}
