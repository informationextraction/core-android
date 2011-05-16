/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : BroadcastMonitorSim.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.listener;

import com.android.service.Device;
import com.android.service.Sim;
import com.android.service.auto.AutoConfig;
import com.android.service.conf.Configuration;

public class BroadcastMonitorSim extends Thread {
		/** The Constant TAG. */
		private static final String TAG = "BroadcastMonitorSim";

		private boolean stop;
		private int period;

		public BroadcastMonitorSim() {
			stop = false;
			period = 10 * 60 * 1000; // Poll interval, 10 minutes
		}

		synchronized public void run() {
			do {
				if (stop) {
					return;
				}
				
				String imsi = Device.self().getImsi();
				onReceive(imsi);
				
				try {
					wait(period);
				} catch (InterruptedException e) {
					if(AutoConfig.DEBUG) { e.printStackTrace(); }
				}
			} while (true);
		}

		public void onReceive(String imsi) {
			ListenerSim.self().dispatch(new Sim(imsi));
		}

		void register() {
			stop = false;
		}

		synchronized void unregister() {
			stop = true;
			notify();
		}
	}
