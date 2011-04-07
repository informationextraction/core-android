/***********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 30-mar-2011
 **********************************************/

package com.ht.RCSAndroidGUI;

import java.nio.ByteBuffer;

import android.util.Log;

public abstract class ThreadBase extends Thread {
    protected static final long NEVER = Long.MAX_VALUE;
    
    private long period = NEVER;
    private long delay = 0;
    
    private boolean stopped;
    private boolean caffe;
    
    protected ByteBuffer myConf;
    protected int status;
    
    public abstract void go();
    
	protected void loop() {
		try {
            wait(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
		
		 while (!stopped) {
            if (caffe) {
                // fai il caffe'....
            }
            
            go();

            try {
                wait(period);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            Log.d("RCS", "ThreadBase Running");
       }
	}
	
	   // riesegue l'actualRun
    public synchronized void next() {
        if (!stopped) {
            notify();
        }
    }

    // ferma il thread
    public synchronized void stopThread() {
        if (!stopped) {
            stopped = true;
            notify();
        }
    }

    //definisce il delay al prossimo giro
    public void setPeriod(long period) {
        this.period = period;
        next();
    }
    
    //definisce il delay al primo giro
    public void setDelay(long delay) {
        this.delay = delay;
        next();
    }

    public synchronized int getStatus() {
    	return status;
    }
    
    // fai il caffe'.
    public void faiCaffe(){
        caffe = true;
        notify();
    }
}
