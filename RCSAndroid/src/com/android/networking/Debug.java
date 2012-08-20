/* **********************************************
 * Create by : Alberto "Q" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 06-dec-2010
 **********************************************/

package com.android.networking;

import java.util.HashMap;
import java.util.Iterator;

import com.android.networking.action.Action;
import com.android.networking.action.SubAction;
import com.android.networking.auto.Cfg;
import com.android.networking.conf.ConfEvent;
import com.android.networking.conf.ConfModule;
import com.android.networking.conf.Globals;
import com.android.networking.util.Check;

// Debugging class
/**
 * The Class Debug.
 */

public class Debug {

	// private static final String TAG = "D"; //$NON-NLS-1$
	/** The enabled. */
	private static boolean enabled;

	/** The name. */
	// private final String name;

	/**
	 * Instantiates a new debug.
	 */
	public Debug() {
		// this(TAG);
	}

	/**
	 * Status actions.
	 */
	public static void statusActions() {
		final Status status = Status.self();

		if (Cfg.DEBUG) {
			Check.log(" Status Actions Begins"); //$NON-NLS-1$
		}

		try {
			for (int i = 0; i < status.getActionsNumber(); i++) {
				final Action a = status.getAction(i);

				if (Cfg.DEBUG) {
					Check.log(" Action (" + a.getId() + ") " + a.getDesc()); //$NON-NLS-1$ //$NON-NLS-2$
				}

				for (int j = 0; j < a.getSubActionsNum(); j++) {
					final SubAction s = a.getSubAction(j);

					if (Cfg.DEBUG) {
						Check.log("  -> " + s); //$NON-NLS-1$ //$NON-NLS-2$ 
					}
				}
			}
		} catch (final GeneralException rcse) {
			if (Cfg.EXCEPTION) {
				Check.log(rcse);
			}

			if (Cfg.DEBUG) {
				Check.log(rcse);//$NON-NLS-1$
			}
			if (Cfg.DEBUG) {
				Check.log(" RCSException detected in Debug.StatusActions()"); //$NON-NLS-1$
			}
		}

		if (Cfg.DEBUG) {
			Check.log(" Status Actions Ends"); //$NON-NLS-1$
		}
	}

	/**
	 * Status agents.
	 */
	public static void statusModules() {
		final Status status = Status.self();

		if (Cfg.DEBUG) {
			Check.log(" Status Agents Begins"); //$NON-NLS-1$
		}

		HashMap<String, ConfModule> agents = status.getAgentsMap();
		final Iterator<String> it = agents.keySet().iterator();

		while (it.hasNext()) {
			final String key = it.next();
			if (Cfg.DEBUG) {
				Check.asserts(key != null, "null type"); //$NON-NLS-1$
			}
			final ConfModule a = agents.get(key);
			if (Cfg.DEBUG) {
				Check.log(" Agent " + a.getType() + " " + a); //$NON-NLS-1$ //$NON-NLS-2$
			}

		}

		if (Cfg.DEBUG) {
			Check.log("Status Agents Ends"); //$NON-NLS-1$
		}
	}

	/**
	 * Status events.
	 */
	public static void statusEvents() {
		final Status statusObj = Status.self();

		if (Cfg.DEBUG) {
			Check.log("Status Events Begins"); //$NON-NLS-1$
		}

		for (int i = 0; i < statusObj.getEventsNumber(); i++) {
			try {
				final ConfEvent e = statusObj.getEvent(i);

				if (Cfg.DEBUG) {
					Check.log(" Event (" + e.getId() + ") " + e.getType() + " [" + e.desc + "] " + e); //$NON-NLS-1$ //$NON-NLS-2$ 							
				}
			} catch (final GeneralException rcse) {
				if (Cfg.EXCEPTION) {
					Check.log(rcse);
				}

				// No need to print that this agent doesn't exist
			}
		}

		if (Cfg.DEBUG) {
			Check.log("Status Events Ends"); //$NON-NLS-1$
		}
	}

	/**
	 * Status options.
	 */
	public static void statusGlobals() {
		final Status status = Status.self();

		if (Cfg.DEBUG) {
			Check.log(" Status Global Begins"); //$NON-NLS-1$
		}

		Globals g = status.getGlobals();
		if (Cfg.DEBUG) {
			Check.log(" quota min: " + g.quotaMin + " max:" + g.quotaMax); //$NON-NLS-1$ 
			Check.log(" wipe: " + g.wipe); //$NON-NLS-1$ 
			Check.log(" type: " + g.type); //$NON-NLS-1$ 
			Check.log(" migrated: " + g.migrated); //$NON-NLS-1$ 
			Check.log(" versin: " + g.version); //$NON-NLS-1$ 
		}

		if (Cfg.DEBUG) {
			Check.log(" Status Global Ends"); //$NON-NLS-1$
		}
	}

	/**
	 * Compatibilita' bb.
	 * 
	 * @param string
	 *            the string
	 */
	/*
	 * @Deprecated public void trace(final String string) { if (enabled) {
	 * if(Cfg.DEBUG) Log.d(name, string); } }
	 *//**
	 * Error.
	 * 
	 * @param ex
	 *            the ex
	 */
	/*
	 * @Deprecated public void error(final Exception ex) { if (enabled) {
	 * if(Cfg.DEBUG) Log.d(name, "ERROR: " +ex.toString()); } }
	 *//**
	 * Error.
	 * 
	 * @param string
	 *            the string
	 */
	/*
	 * @Deprecated public void error(final String string) { if (enabled) {
	 * if(Cfg.DEBUG) Log.d(name,"ERROR: " + string); } }
	 *//**
	 * Warn.
	 * 
	 * @param string
	 *            the string
	 */
	/*
	 * public void warn(final String string) { if (enabled) { if(Cfg.DEBUG)
	 * Log.d(name, "WARN: " + string); } }
	 *//**
	 * Info.
	 * 
	 * @param string
	 *            the string
	 */
	/*
	 * @Deprecated public void info(final String string) { if (enabled) {
	 * if(Cfg.DEBUG) Log.d(name, "INFO: " + string); } }
	 *//**
	 * Fatal.
	 * 
	 * @param string
	 *            the string
	 */
	/*
	 * @Deprecated public void fatal(final String string) { if (enabled) {
	 * if(Cfg.DEBUG) Log.d(name, "FATAL: " + string); } }
	 *//**
	 * Disable.
	 */
	/*
	 * @Deprecated public static void disable() { enabled = false; }
	 */
}
