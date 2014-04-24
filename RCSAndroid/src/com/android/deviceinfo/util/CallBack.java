package com.android.deviceinfo.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CallBack {
	private static final String TAG = "CallBack";
	
	private List<ICallBack> callbacks = new ArrayList<ICallBack>();
	
	synchronized public void register(ICallBack c) {
		callbacks.add(c);
	}
	
	synchronized public <O> void trigger(O o) {
		Iterator<ICallBack> iterator = callbacks.iterator();
		
		while (iterator.hasNext()) {
			iterator.next().run(o);
		}
	}
	
	synchronized public void deregister(ICallBack c) {
		callbacks.remove(c);
	}
}

	
