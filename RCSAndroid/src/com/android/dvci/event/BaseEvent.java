/* **********************************************
 * Create by : Alberto "Q" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 30-mar-2011
 **********************************************/

package com.android.dvci.event;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;

import com.android.dvci.ProcessInfo;
import com.android.dvci.Status;
import com.android.dvci.ThreadBase;
import com.android.dvci.action.Action;
import com.android.dvci.auto.Cfg;
import com.android.dvci.conf.ConfEvent;
import com.android.dvci.util.Check;

// TODO: Auto-generated Javadoc
/**
 * The Class EventBase.
 */
public abstract class BaseEvent extends ThreadBase {

	/** The Constant TAG. */
	private static final String TAG = "BaseEvent"; //$NON-NLS-1$

	boolean isActive = false;
	private Alarm alarm;
	private String subType;

	// Gli eredi devono implementare i seguenti metodi astratti
	/**
	 * Parses the.
	 * 
	 * @param event
	 *            the event
	 */
	protected abstract boolean parse(ConfEvent event);

	/** The event. */
	protected ConfEvent conf;
	private int iterCounter;

	public class Alarm extends BroadcastReceiver {

		int count = 0;
		
		@Override
		public void onReceive(Context context, Intent intent) {

			if (Cfg.DEBUG) {
				Check.log(TAG + " SCHED (onReceive), intent: %s", intent);
			}

			PowerManager.WakeLock wl;
			if (Cfg.POWER_MANAGEMENT) {
				PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
				wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
				wl.acquire();
			}
			
			// public void run() {
			try {
				// verifica iter, se sono stati eseguiti i giusti repeat
				// esce
 				if (count >= iterCounter) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " SCHED (onReceive): count >= iterCounter");
					}

					stopAlarm();
					return;
				}
				count++;
				
				triggerRepeatAction();

				if (Cfg.DEBUG) {
					Check.log(TAG + " SCHED (onReceive) count: " + count);
				}

				
			} catch (Exception ex) {
				if (Cfg.EXCEPTION) {
					Check.log(ex);
				}

				if (Cfg.DEBUG) {
					Check.log(TAG + " SCHED (onReceive) Error: " + ex);
				}

				//stopAlarm();
			}

			if (Cfg.POWER_MANAGEMENT) {
				wl.release();
			}
		}

		public void SetAlarm(int delay, int period) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (SetAlarm) delay=" + delay + " period=" + period + " intent=BE." + getId());
			}
			Context context = Status.getAppContext();
			AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			// Intent i = new Intent(Status.getAppContext(), Alarm.class);
			Intent i = new Intent("BE." + getId());
			
			PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
			// Millisec * Second * Minute
			am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay * 1000, period * 1000, pi);
			count = 0;
		}

		public void CancelAlarm() {
			Context context = Status.getAppContext();
			//Intent intent = new Intent(context, Alarm.class);
			Intent intent = new Intent("BE." + getId());
			if (Cfg.DEBUG) {
				Check.log(TAG + " (CancelAlarm) intent: %s", intent);
			}
			PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			alarmManager.cancel(sender);
		}
	}

	public int getId() {
		return conf.getId();
	}

	public String getType() {
		return conf.getType();
	}

	/**
	 * Sets the event.
	 * 
	 * @param event
	 *            the new event
	 */
	public boolean setConf(final ConfEvent conf) {
		if (Cfg.DEBUG) {
			Check.requires(conf != null, "null conf");
		}

		this.conf = conf;

		boolean ret = parse(conf);
		iterCounter = conf.iter;

		return ret;

	}

	private final boolean trigger(int actionId) {
		if (actionId != Action.ACTION_NULL) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " event: " + this + " triggering: " + actionId);//$NON-NLS-1$ //$NON-NLS-2$
			}

			Status.self().triggerAction(actionId, this);
			return true;
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (trigger): null action");
			}

			return false;
		}
	}

	protected int getConfDelay() {
		return conf.delay;
	}

	protected synchronized void onEnter() {
		// if (Cfg.DEBUG) Check.asserts(!active,"stopSchedulerFuture");
		if (isActive) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (onEnter): already active, return");
			}
			return;
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " (onEnter): " + this);
		}

		int delay = getConfDelay();
		int period = delay;

		// Se delay e' 0 e' perche' non c'e' repeat, quindi l'esecuzione deve
		// essere semplice.
		if (delay <= 0) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (onEnter): delay <= 0");
			}

			if (Cfg.DEBUG) {
				Check.asserts(iterCounter == Integer.MAX_VALUE, " (onEnter) Assert failed, iterCounter:" + iterCounter);
				Check.asserts(conf.repeatAction == Action.ACTION_NULL, " (onEnter) Assert failed, repeatAction:"
						+ conf.repeatAction);

			}
		}

		triggerStartAction();

		if (Cfg.DEBUG) {
			Check.log(TAG + " (Alarm) delay: " + delay + " period: " + period);
		}

		if (delay > 0) {
			if (Cfg.DEBUG) {
				Check.asserts(period > 0, " (onEnter) Assert failed, period<=0: " + conf);
				Check.log(TAG + " (onEnter) register Reveiverfilter = BE." + getId());
			}

			alarm = new Alarm();
			Status.getAppContext().registerReceiver(alarm, new IntentFilter("BE." + getId()));
			alarm.SetAlarm(delay, period);

		}

		isActive = true;
	}

	private void stopAlarm() {
		if (Cfg.DEBUG)
			Check.asserts(isActive, "stopAlarm");

		if (isActive && alarm != null) {
			alarm.CancelAlarm();
			Status.getAppContext().unregisterReceiver(alarm);
			alarm = null;
		}
	}

	protected boolean isEntered() {
		return isActive;
	}

	protected synchronized void onExit() {
		// if (Cfg.DEBUG) Check.asserts(active,"stopSchedulerFuture");
		if (isActive) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (onExit): Active");
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " (onExit): " + this);
			}

			stopAlarm();
			isActive = false;

			triggerEndAction();
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (onExit): Not active");
			}
		}

	}

	protected synchronized boolean stillIter() {
		iterCounter--;
		return iterCounter >= 0;
	}

	private boolean triggerStartAction() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (triggerStartAction): " + this);
		}

		if (Cfg.DEBUG) {
			Check.requires(conf != null, "null conf");
		}
		return trigger(conf.startAction);
	}

	private boolean triggerEndAction() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (triggerStopAction): " + this);
		}
		if (Cfg.DEBUG) {
			Check.requires(conf != null, "null conf");
		}
		return trigger(conf.endAction);
	}

	private boolean triggerRepeatAction() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (triggerRepeatAction): " + this);
		}

		if (Cfg.DEBUG) {
			Check.requires(conf != null, "null conf");
		}

		return trigger(conf.repeatAction);
	}

	@Override
	public String toString() {
		return "Event (" + conf.getId() + ") <" + conf.getType().toUpperCase() + "> : " + conf.desc + " " + (isEnabled() ? "ENABLED" : "DISABLED"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	public boolean isEnabled() {
		return conf.enabled;
	}

	public String getSubType() {
		return this.subType;
	}

	public void setSubType(String subtype) {
		this.subType = subtype;
	}

	public void notifyProcess(ProcessInfo b) {
		
	}

}