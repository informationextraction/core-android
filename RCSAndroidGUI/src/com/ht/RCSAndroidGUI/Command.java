package com.ht.RCSAndroidGUI;

import java.util.LinkedList;
import java.util.Queue;

import android.os.Message;
import android.util.Log;

public class Command {
	private Queue<Message> msgQueue;
	
	public Command() { 	
		msgQueue = new LinkedList<Message>();
		msgQueue.clear();
	} 
	
	public synchronized void pushMessage(Message msg) { 
		if (msg == null) {
			Log.d("Que", "Command queue is null");
			return;
		}
		
		msgQueue.add(msg);
	}

	// Returns null is the queue is empty
	public synchronized Message getMessage() {
		Message m = msgQueue.poll();
		
		return m;
	}
}
