package com.android.dvci.auto;

import com.android.dvci.auto.Cfg;

public class Cfg {
	//ATTENZIONE, NON CAMBIARE A MANO LA VARIABILE DEBUG, VIENE RISCRITTA DA ANT

	public static final int BUILD_ID = @BUILD_ID@;
	public static final String BUILD_TIMESTAMP = "@BUILD_TIMESTAMP@";

	public static final int VERSION = @VERSION@;
	public static final String OSVERSION = "@OSVERSION@";

	public static final boolean DEBUG = @DEBUG@;
	public static final boolean DEBUG_SPECIFIC = @DEBUG@; // @DEBUG@
	public static final boolean CHECK_ANTI_DEBUG = !@DEBUG@;
	public static final boolean EXCEPTION = @EXCEPTION@;
	//public static final boolean DEBUG = true;
	//public static final boolean EXCEPTION = true;

	public static boolean DEMO = @DEMO@; // @DEMO@
	public static boolean DEMO_SILENT = false;

	public static final boolean FORCE_NODEMO = false;
	public static final boolean DEMO_INITSOUND = false;

	public static final boolean KEYS = @KEYS@; // Se e' true vengono usate le chiavi hardcoded

	public static boolean FILE = @FILE@;
	public static final boolean MICFILE = false;
	public static final boolean TRACE = false; // enable Debug.startMethodTracing
	public static final boolean DEBUGKEYS = false; //uses fake keys if assets/rb.data not available
	public static final boolean STATISTICS = false; // enable statistics on crypto and on commands

	public static final boolean ENABLE_EXPERIMENTAL_MODULES = false; // enables viber modules
	public static final boolean ENABLE_WIFI_DISABLE = false;
	public static final boolean DELAY_SKYPE_CALL = false;
	public static final boolean PERSISTENCE = true;

	public static final int PROTOCOL_CHUNK = 256 * 1024; // chunk size fot resume
	public static final int EV_QUEUE_LEN = 8;
	public static final int EV_BLOCK_SIZE = 256 * 1024;
	public static final int MAX_ASKED_SU = 3; // maximum number of su ask
	public static final long FREQUENT_NOTIFICATION_PERIOD = 5;

	public static final boolean USE_SD = true; // try to use sd if available
	public static final boolean FORCE_ROOT = false; // force root request

	public static final boolean ONE_MAIL = false; // retrieve only one mail
	public static final boolean ADJUST_OOM_ONCE = false;

	public static final boolean POWER_MANAGEMENT = true; // if true, tries to acquire power lock only when needed
	public static final boolean DEBUGANTI = false; // true to debug antidebug and antiemu deceptions
    public static final boolean DEBUGANTIEMU = false; // true to debug antiemu deceptions

	public static final String RANDOM = "@RANDOM@";
	public static final String RNDMSG = "@RNDMSG@";
	public static final String RNDDB = "@RNDDB@";
	public static final String RNDLOG = "@RNDLOG@";
}
