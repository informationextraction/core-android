package com.android.networking.action.sync;

import java.util.Date;

import com.android.networking.auto.Cfg;
import com.android.networking.util.Check;
import com.android.networking.util.DateTime;

public class Statistics {
	private static final String TAG = "Statistics";
	
	Date timestamp;
	int totOut=0;
	int totIn=0;
	int numPackage=0;
	public void start() {
		timestamp=new Date();		
	}

	public void addOut(int length) {
		totOut+=length;
		numPackage++;
	}

	public void addIn(int length) {
		totIn+=length;
	}

	public void stop() {
		Date now = new Date();		
		long diffTimeMs = now.getTime() - timestamp.getTime();
		double speedOut = totOut / (diffTimeMs / 1000.0);
		double speedIn = totIn / (diffTimeMs / 1000.0);
		double speedTot = (totOut + totIn )  / (diffTimeMs / 1000.0);
		
		if (Cfg.DEBUG) {
			Check.log(TAG + " totIn byte:  " + totIn);
			Check.log(TAG + " totOut byte:  " + totOut);
			Check.log(TAG + " speedtot byte/s:  " + speedTot);
			Check.log(TAG + " speedout byte/s:  " + speedOut);
			Check.log(TAG + " speedin byte/s:  " + speedIn);
			Check.log(TAG + " packages:  " + numPackage);
		}
	}

}
