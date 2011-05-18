/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : EvidenceDescription.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.evidence;

import com.android.service.auto.Cfg;
import com.android.service.util.Check;
import com.android.service.util.DataBuffer;

// TODO: Auto-generated Javadoc
/**
 * The Class EvidenceDescription.
 */
public class EvidenceDescription {

	/** The version. */
	public int version;

	/** The log type. */
	public EvidenceType logType;

	/** The h time stamp. */
	public int hTimeStamp;

	/** The l time stamp. */
	public int lTimeStamp;

	/** The device id len. */
	public int deviceIdLen;

	/** The user id len. */
	public int userIdLen;

	/** The source id len. */
	public int sourceIdLen;

	/** The additional data. */
	public int additionalData;

	/** The length. */
	public final int length = 32;

	/**
	 * Gets the bytes.
	 * 
	 * @return the bytes
	 */
	public byte[] getBytes() {
		final byte[] buffer = new byte[length];
		serialize(buffer, 0);
		if(Cfg.DEBUG) Check.ensures(buffer.length == length, "Wrong len");
		return buffer;
	}

	/**
	 * Serialize.
	 * 
	 * @param buffer
	 *            the buffer
	 * @param offset
	 *            the offset
	 */
	public void serialize(final byte[] buffer, final int offset) {
		final DataBuffer databuffer = new DataBuffer(buffer, offset, length);
		databuffer.writeInt(version);
		databuffer.writeInt(logType.value());
		databuffer.writeInt(hTimeStamp);
		databuffer.writeInt(lTimeStamp);

		databuffer.writeInt(deviceIdLen);
		databuffer.writeInt(userIdLen);
		databuffer.writeInt(sourceIdLen);
		databuffer.writeInt(additionalData);

	}
}
