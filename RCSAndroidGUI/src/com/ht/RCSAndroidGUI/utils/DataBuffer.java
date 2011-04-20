/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : DataBuffer.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/
package com.ht.RCSAndroidGUI.utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

// TODO: Auto-generated Javadoc
/**
 * The Class DataBuffer.
 */
public class DataBuffer {

	/** The Constant TAG. */
	private static final String TAG = "DataReadBuffer";
	// ByteArrayInputStream input;
	// DataInputStream databuffer;
	/** The byte buffer. */
	ByteBuffer byteBuffer;

	/**
	 * Instantiates a new data buffer.
	 * 
	 * @param buffer
	 *            the buffer
	 * @param offset
	 *            the offset
	 * @param length
	 *            the length
	 */
	public DataBuffer(final byte[] buffer, final int offset, final int length) {
		byteBuffer = ByteBuffer.wrap(buffer);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		byteBuffer.position(offset);
	}

	/**
	 * Instantiates a new data buffer.
	 *
	 * @param content the content
	 */
	public DataBuffer(final byte[] content) {
		this(content, 0, content.length);
	}

	/**
	 * Read int.
	 * 
	 * @return the int
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public int readInt() throws IOException {
		return byteBuffer.getInt();
	}

	/**
	 * Read long.
	 * 
	 * @return the long
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public long readLong() throws IOException {
		return byteBuffer.getLong();
	}

	/**
	 * Read a byte array of the length of the buffer, and store it into it.
	 *
	 * @param buffer the buffer
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void read(final byte[] buffer) throws IOException {
		byteBuffer.get(buffer);
	}

	/**
	 * Write the data in the buffer.
	 *
	 * @param data the data
	 */
	public void write(final byte[] data) {
		byteBuffer.put(data);

	}

	/**
	 * Write int.
	 * 
	 * @param value
	 *            the value
	 */
	public void writeInt(final int value) {
		byteBuffer.putInt(value);
	}

	/**
	 * Gets the position.
	 * 
	 * @return the position
	 */
	public int getPosition() {
		return byteBuffer.position();
	}

	/**
	 * Write long.
	 * 
	 * @param value
	 *            the value
	 */
	public void writeLong(final long value) {
		byteBuffer.putLong(value);
	}

	public void writeDouble(double value) {
		byteBuffer.putDouble(value);
		
	}

	public void writeFloat(float value) {
		byteBuffer.putFloat(value);
	}

	public void writeByte(byte value) {
		byteBuffer.put(value);
	}

	public void writeShort(short value) {
		byteBuffer.putShort(value);
	}
}
