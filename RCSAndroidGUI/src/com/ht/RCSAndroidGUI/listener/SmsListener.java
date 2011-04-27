package com.ht.RCSAndroidGUI.listener;

import com.ht.RCSAndroidGUI.Sms;

public class SmsListener extends Listener<Sms> {
		/** The Constant TAG. */
		private static final String TAG = "SmsListener";

		private SmsBroadcastMonitor smsReceiver;

		/** The singleton. */
		private volatile static SmsListener singleton;

		/**
		 * Self.
		 * 
		 * @return the status
		 */
		public static SmsListener self() {
			if (singleton == null) {
				synchronized (SmsListener.class) {
					if (singleton == null) {
						singleton = new SmsListener();
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
			smsReceiver = new SmsBroadcastMonitor();
		}
}
