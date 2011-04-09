/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : Packet.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/
package com.ht.RCSAndroidGUI;

// TODO: Auto-generated Javadoc
/**
 * The Class Packet.
 */
public class Packet {
	
	/** The priority. */
	private int type, command, priority;
	
	/** The id. */
	private final long id;
	
	/** The data. */
	private byte[] data;

	/**
	 * Instantiates a new packet.
	 *
	 * @param unique the unique
	 */
	public Packet(final long unique) {
		type = 0;
		command = 0;
		id = unique;
		data = null;
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * Sets the command.
	 *
	 * @param c the new command
	 */
	public void setCommand(final int c) {
		command = c;
	}

	/**
	 * Gets the command.
	 *
	 * @return the command
	 */
	public int getCommand() {
		return command;
	}

	/**
	 * Sets the priority.
	 *
	 * @param p the new priority
	 */
	public void setPriority(final int p) {
		priority = p;
	}

	/**
	 * Gets the priority.
	 *
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}

	// Needed only when sending LOG_CREATE
	/**
	 * Sets the type.
	 *
	 * @param t the new type
	 */
	public void setType(final int t) {
		type = t;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * Fill.
	 *
	 * @param d the d
	 */
	public void fill(final byte[] d) {
		data = d;
	}

	/**
	 * Peek.
	 *
	 * @return the byte[]
	 */
	public byte[] peek() {
		return data;
	}
}
