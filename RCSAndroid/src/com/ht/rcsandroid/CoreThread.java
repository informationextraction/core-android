package com.ht.rcsandroid;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class CoreThread extends Activity implements Runnable {
	private Thread t;
	private boolean bStopCore = false;
	
	public boolean Start() {
		this.t = new Thread(this);
		
		t.start();
		return true;
	}
	
	public boolean Stop() {
		bStopCore = true;
		return true;
	}
	
	// Runnable
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
