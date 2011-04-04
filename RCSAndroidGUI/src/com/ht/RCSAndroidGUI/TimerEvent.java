/***********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 30-mar-2011
 **********************************************/

package com.ht.RCSAndroidGUI;

import java.nio.ByteOrder;

import android.util.Log;

public class TimerEvent extends EventBase {
	final private static int CONF_TIMER_SINGLE = 0;
	final private static int CONF_TIMER_REPEAT = 1;
	final private static int CONF_TIMER_DATE = 2;
		
	private int type;
	
	public TimerEvent() {
		Log.d("RCS", "TimerEvent constructor");
	}
	
	public void parse(byte[] conf) {
		myConf = Utils.BufferToByteBuffer(conf, ByteOrder.LITTLE_ENDIAN);
		
		this.type = myConf.getInt();
	}
	
	public void begin() {
		setDelay(2000);
	}
	
	public void go() {
		// Checka i timer
		switch (type) {
			case CONF_TIMER_SINGLE:
				break;
				
			case CONF_TIMER_REPEAT:
				break;
				
			case CONF_TIMER_DATE:
				break;
				
			default:
				break;
		}
	}
	
	public void end() {
		
	}
}
