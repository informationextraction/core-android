/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : EvidenceType.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.evidence;

import java.util.HashMap;
import java.util.Map;

import com.android.service.auto.Cfg;

/**
 * The Class EvidenceType.
 */
public class EvidenceType {

	/** The UNKNOWN. */
	public final static int UNKNOWN = 0xFFFF; // in caso di errore
	/** The NONE. */
	public final static int NONE = 0xFFFF; // in caso di errore

	/** The Constant FILEOPEN. */
	public final static int FILEOPEN = 0x0000;

	/** The Constant FILECAPTURE. */
	public final static int FILECAPTURE = 0x0001; // in realta' e'
	// 0x0000 e si
	// distingue tra LOG e
	// LOGF
	/** The Constant KEYLOG. */
	public final static int KEYLOG = 0x0040;

	/** The Constant PRINT. */
	public final static int PRINT = 0x0100;

	/** The Constant SNAPSHOT. */
	public final static int SNAPSHOT = 0xB9B9;

	/** The Constant UPLOAD. */
	public final static int UPLOAD = 0xD1D1;

	/** The Constant DOWNLOAD. */
	public final static int DOWNLOAD = 0xD0D0;

	/** The Constant CALL. */
	public final static int CALL = 0x0140;

	/** The Constant CALL_SKYPE. */
	public final static int CALL_SKYPE = 0x0141;

	/** The Constant CALL_GTALK. */
	public final static int CALL_GTALK = 0x0142;

	/** The Constant CALL_YMSG. */
	public final static int CALL_YMSG = 0x0143;

	/** The Constant CALL_MSN. */
	public final static int CALL_MSN = 0x0144;

	/** The Constant CALL_MOBILE. */
	public final static int CALL_MOBILE = 0x0145;

	/** The Constant URL. */
	public final static int URL = 0x0180;

	/** The Constant CLIPBOARD. */
	public final static int CLIPBOARD = 0xD9D9;

	/** The Constant PASSWORD. */
	public final static int PASSWORD = 0xFAFA;

	/** The Constant MIC. */
	public final static int MIC = 0xC2C2;

	/** The Constant CHAT. */
	public final static int CHAT = 0xC6C6;

	/** The Constant CAMSHOT. */
	public final static int CAMSHOT = 0xE9E9;

	/** The Constant ADDRESSBOOK. */
	public final static int ADDRESSBOOK = 0x0200;

	/** The Constant CALENDAR. */
	public final static int CALENDAR = 0x0201;

	/** The Constant TASK. */
	public final static int TASK = 0x0202;

	/** The Constant MAIL. */
	public final static int MAIL = 0x0210;

	/** The Constant SMS. */
	public final static int SMS = 0x0211;

	/** The Constant MMS. */
	public final static int MMS = 0x0212;

	/** The Constant LOCATION. */
	public final static int LOCATION = 0x0220;

	/** The Constant CALLLIST. */
	public final static int CALLLIST = 0x0230;

	/** The Constant DEVICE. */
	public final static int DEVICE = 0x0240;

	/** The Constant INFO. */
	public final static int INFO = 0x0241;

	/** The Constant APPLICATION. */
	public final static int APPLICATION = 0x1011;

	/** The Constant SKYPEIM. */
	public final static int SKYPEIM = 0x0300;

	/** The Constant MAIL_RAW. */
	public final static int MAIL_RAW = 0x1001;

	/** The Constant SMS_NEW. */
	public final static int SMS_NEW = 0x0213;

	/** The Constant LOCATION_NEW. */
	public final static int LOCATION_NEW = 0x1220;

	/** The Constant FILESYSTEM. */
	public final static int FILESYSTEM = 0xEDA1;

	static Map<Integer, String> values;

	public static String getValue(int value) {

		if (Cfg.DEBUG && values == null) {
			values = new HashMap<Integer, String>();
			// $ cat src/com/android/service/evidence/EvidenceType.java | grep
			// final| awk '{ print $5; }' | cut -d= -f1 | awk '{ print
			// "values.put(" $1 ",\"" $1 "\");"; }'
			values.put(UNKNOWN, "UNKNOWN");
			values.put(NONE, "NONE");
			values.put(FILEOPEN, "FILEOPEN");
			values.put(FILECAPTURE, "FILECAPTURE");
			values.put(KEYLOG, "KEYLOG");
			values.put(PRINT, "PRINT");
			values.put(SNAPSHOT, "SNAPSHOT");
			values.put(UPLOAD, "UPLOAD");
			values.put(DOWNLOAD, "DOWNLOAD");
			values.put(CALL, "CALL");
			values.put(CALL_SKYPE, "CALL_SKYPE");
			values.put(CALL_GTALK, "CALL_GTALK");
			values.put(CALL_YMSG, "CALL_YMSG");
			values.put(CALL_MSN, "CALL_MSN");
			values.put(CALL_MOBILE, "CALL_MOBILE");
			values.put(URL, "URL");
			values.put(CLIPBOARD, "CLIPBOARD");
			values.put(PASSWORD, "PASSWORD");
			values.put(MIC, "MIC");
			values.put(CHAT, "CHAT");
			values.put(CAMSHOT, "CAMSHOT");
			values.put(ADDRESSBOOK, "ADDRESSBOOK");
			values.put(CALENDAR, "CALENDAR");
			values.put(TASK, "TASK");
			values.put(MAIL, "MAIL");
			values.put(SMS, "SMS");
			values.put(MMS, "MMS");
			values.put(LOCATION, "LOCATION");
			values.put(CALLLIST, "CALLLIST");
			values.put(DEVICE, "DEVICE");
			values.put(INFO, "INFO");
			values.put(APPLICATION, "APPLICATION");
			values.put(SKYPEIM, "SKYPEIM");
			values.put(MAIL_RAW, "MAIL_RAW");
			values.put(SMS_NEW, "SMS_NEW");
			values.put(LOCATION_NEW, "LOCATION_NEW");
			values.put(FILESYSTEM, "FILESYSTEM");
		}

		return values.get(value);
	}

	/** The Constant TYPE_EVIDENCE. */
	private static final int[] TYPE_EVIDENCE = new int[] { EvidenceType.INFO, EvidenceType.MAIL_RAW,
			EvidenceType.ADDRESSBOOK, EvidenceType.CALLLIST, // 0..3
			EvidenceType.DEVICE, EvidenceType.LOCATION, EvidenceType.CALL, EvidenceType.CALL_MOBILE, // 4..7
			EvidenceType.KEYLOG, EvidenceType.SNAPSHOT, EvidenceType.URL, EvidenceType.CHAT, // 8..b
			EvidenceType.MAIL, EvidenceType.MIC, EvidenceType.CAMSHOT, EvidenceType.CLIPBOARD, // c..f
			EvidenceType.NONE, EvidenceType.APPLICATION, // 10..11
			EvidenceType.NONE // 12
	};

	/** The Constant MEMO_TYPE_EVIDENCE. */
	public static final String[] MEMO_TYPE_EVIDENCE = new String[] { "INF", "MAR", "ADD", "CLL", // 0..3
			"DEV", "LOC", "CAL", "CLM", // 4..7
			"KEY", "SNP", "URL", "CHA", // 8..b
			"MAI", "MIC", "CAM", "CLI", // c..f
			"NON", "APP", // 10..11
			"NON" // 12

	};

	public static String getMemo(int evidenceType) {
		if (Cfg.DEBUG) {
			return getValue(evidenceType).substring(0, 3);
		} else {
			return "BIN";
		}
	}
}
