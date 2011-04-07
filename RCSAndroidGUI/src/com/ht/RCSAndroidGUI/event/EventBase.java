/***********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 30-mar-2011
 **********************************************/

package com.ht.RCSAndroidGUI.event;

import com.ht.RCSAndroidGUI.Status;
import com.ht.RCSAndroidGUI.ThreadBase;
import com.ht.RCSAndroidGUI.action.Action;

import android.util.Log;

public abstract class EventBase extends ThreadBase implements Runnable {
    private static final String TAG = "EventBase";

    
	// Gli eredi devono implementare i seguenti metodi astratti
    public abstract void begin();
    public abstract void end();
    public abstract void parse(Event event);
    
    protected Event event;
    
    public synchronized void run() {
    	status = Event.EVENT_RUNNING;
    	
    	begin();
    	loop();
        end();
        
        status = Event.EVENT_STOPPED;
        Log.d("RCS", "EventBase stopped");
    }
    
	public void setEvent(Event event) {
		this.event = event;
	}
	
    /**
     * Trigger.
     */
    protected final void trigger() {
    	int actionId = event.getAction();
        if (actionId != Action.ACTION_NULL) {
           Log.d(TAG, "event: " + this + " triggering: " + actionId);
            //#endif

            Status.self().triggerAction(actionId);
        }
    }
}