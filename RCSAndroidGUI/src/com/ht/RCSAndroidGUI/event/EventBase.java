/***********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 30-mar-2011
 **********************************************/

package com.ht.RCSAndroidGUI.event;

import com.ht.RCSAndroidGUI.ThreadBase;

import android.util.Log;

public abstract class EventBase extends ThreadBase implements Runnable {
    // Gli eredi devono implementare i seguenti metodi astratti
    public abstract void begin();
    public abstract void end();
    public abstract void parse(byte[] conf);
    
    public synchronized void run() {
    	status = Event.EVENT_RUNNING;
    	
    	begin();
    	loop();
        end();
        
        status = Event.EVENT_STOPPED;
        Log.d("RCS", "EventBase stopped");
    }
}