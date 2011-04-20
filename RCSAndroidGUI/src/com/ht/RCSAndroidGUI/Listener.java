/***********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 20-apr-2011
 **********************************************/

package com.ht.RCSAndroidGUI;

import java.util.Stack;

public abstract class Listener {
	Stack<Object> observers;
	
	public Listener() {
		observers = new Stack<Object>();
	}
	
	public boolean attach(Object o) {
		// Object already in the stack
		if (observers.search(o) != -1)
			return false;
		
		observers.push(o);
		return true;
	}
	
	public void detach(Object o) {
		if (observers.empty())
			return;
		
		observers.remove(o);
	}
	
	protected abstract void run(Object o);
}
