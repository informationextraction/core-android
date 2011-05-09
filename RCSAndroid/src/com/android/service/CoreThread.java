package com.android.service;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

// TODO: Auto-generated Javadoc
/**
 * The Class CoreThread.
 */
public class CoreThread extends Activity implements Runnable {
	
	/** The t. */
	private Thread t;
	
	/** The b stop core. */
	private boolean bStopCore = false;
	
	/**
	 * Start.
	 *
	 * @return true, if successful
	 */
	public boolean Start() {
		this.t = new Thread(this);
		
		t.start();
		return true;
	}
	
	/**
	 * Stop.
	 *
	 * @return true, if successful
	 */
	public boolean Stop() {
		bStopCore = true;
		return true;
	}
	
	// Runnable
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		/*while(this.bStopCore == false) {
			//try {
				//Thread.sleep(5000);
				
				// Qui va l'handler
				//messageHandler.sendEmptyMessage(RESULT_OK);
				Toast.makeText(this, "Thread Alive", Toast.LENGTH_LONG).show();
			//} catch (InterruptedException e) {
			//	e.printStackTrace();
			//}
		}*/
	}
	
	/** The message handler. */
	private Handler messageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {  
			switch(msg.what) {
				//handle update
				//.....
				default: 
					break;
			}
		}
	};
}
