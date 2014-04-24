/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : AutoFile.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.deviceinfo.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.evidence.EvidenceCollector;
import com.android.deviceinfo.util.ByteArray;
import com.android.deviceinfo.util.Check;
import com.android.deviceinfo.util.Execute;
import com.android.deviceinfo.util.ExecuteResult;

/**
 * The Class AutoFlashFile.
 */
public final class AutoFile {

	/** The Constant TAG. */
	private static final String TAG = "AutoFile"; //$NON-NLS-1$
	/** The file. */
	File file;
	private String filename;

	/**
	 * Instantiates a new auto flash file.
	 * http://developer.android.com/guide/topics
	 * /data/data-storage.html#filesInternal
	 * 
	 * @param filename
	 *            the filename
	 */
	public AutoFile(final String filename) {
		file = new File(filename);
		this.filename = filename;
	}

	public AutoFile(String dir, String filename) {
		file = new File(dir, filename);
		this.filename = filename;
	}

	public AutoFile(File filesDir, String file) {
		this(filesDir.getAbsolutePath(), file);
	}

	/**
	 * Reads the content of the file.
	 * 
	 * @return the byte[]
	 */
	public byte[] read() {
		return read(0);
	}

	/**
	 * Read the file starting from the offset specified.
	 * 
	 * @param offset
	 *            the offset
	 * @return the byte[]
	 */
	public byte[] read(final int offset, int length) {
		length = Math.min(length, (int) file.length() - offset);
		InputStream in = null;
		if (Cfg.DEBUG) {
			Check.asserts(file != null, " (read) Assert failed, null file");
		}

		if (length == 0) {
			return null;
		}
		try {
			in = new BufferedInputStream(new FileInputStream(file), length);
			final byte[] buffer = new byte[length];
			in.skip(offset);
			in.read(buffer, 0, length);
			return buffer;
		} catch (final IOException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(e);//$NON-NLS-1$
			}

		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (final IOException e) {
					if (Cfg.EXCEPTION) {
						Check.log(e);
					}

					if (Cfg.DEBUG) {
						Check.log(e);//$NON-NLS-1$
					}
				}
			}
		}

		return null;
	}

	/**
	 * Read the file starting from the offset specified.
	 * 
	 * @param offset
	 *            the offset
	 * @return the byte[]
	 */
	public byte[] read(final int offset) {
		final int length = (int) file.length() - offset;
		InputStream in = null;
		if (Cfg.DEBUG) {
			Check.asserts(file != null, " (read) Assert failed, null file");
		}

		if (length == 0) {
			return null;
		}
		try {
			in = new BufferedInputStream(new FileInputStream(file), length);
			final byte[] buffer = new byte[length];
			in.skip(offset);
			in.read(buffer, 0, length);
			return buffer;
		} catch (final IOException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(e);//$NON-NLS-1$
			}

		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (final IOException e) {
					if (Cfg.EXCEPTION) {
						Check.log(e);
					}

					if (Cfg.DEBUG) {
						Check.log(e);//$NON-NLS-1$
					}
				}
			}
		}

		return null;
	}

	/**
	 * Write some data to the file.
	 * 
	 * @param data
	 *            the data
	 */
	public void write(final byte[] data) {
		write(data, 0, false);
	}

	/**
	 * Write a data buffer in the file, at a specific offset. If append is false
	 * the content of the file is overwritten.
	 * 
	 * @param data
	 *            the data
	 * @param offset
	 *            the offset
	 * @param append
	 *            the append
	 * @return true, if successful
	 */
	public boolean write(final byte[] data, final int offset, final boolean append) {
		OutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(file, append), data.length - offset);
			out.write(data, offset, data.length - offset);
			out.flush();
			return true;
		} catch (final Exception ex) {
			if (Cfg.EXCEPTION) {
				Check.log(ex);
			}

			return false;
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (final IOException e) {
					if (Cfg.EXCEPTION) {
						Check.log(e);
					}

					if (Cfg.DEBUG) {
						Check.log(TAG + " Error: " + e.toString());//$NON-NLS-1$
					}
				}
			}
		}
	}

	public void write(int value) {
		write(ByteArray.intToByteArray(value));
	}

	/**
	 * Append some data to the file.
	 * 
	 * @param data
	 *            the data
	 */
	public boolean append(final byte[] data) {
		if(data==null){
			if (Cfg.DEBUG) {
				Check.log(TAG + " (append) null data");
			}
			return true;
		}
		FileOutputStream fout = null;
		OutputStream out = null;
		try {
			fout = new FileOutputStream(file, true);
			out = new BufferedOutputStream(fout, data.length);
			out.write(data);
			out.flush();
		} catch (final Exception ex) {
			if (Cfg.EXCEPTION) {
				Check.log(ex);
			}

			return false;
		} finally {
			if (fout != null) {
				try {
					fout.close();
				} catch (final IOException e) {
					if (Cfg.EXCEPTION) {
						Check.log(e);
					}

					if (Cfg.DEBUG) {
						Check.log(TAG + " Error: " + e.toString());//$NON-NLS-1$
					}
				}
			}
			
			if (out != null) {
				try {
					out.close();
				} catch (final IOException e) {
					if (Cfg.EXCEPTION) {
						Check.log(e);
					}

					if (Cfg.DEBUG) {
						Check.log(TAG + " Error: " + e.toString());//$NON-NLS-1$
					}
				}
			}
		}

		
		return true;
	}

	/**
	 * Tells if the file exists.
	 * 
	 * @return true, if successful
	 */
	public boolean exists() {
		return file.exists();
	}

	/**
	 * The file can be read.
	 * 
	 * @return true if readable
	 */
	public boolean canRead() {
		return file.canRead();
	}

	/**
	 * Check. if the file is a directory. //$NON-NLS-1$
	 * 
	 * @return true, if is directory
	 */
	public boolean isDirectory() {
		return file.isDirectory();
	}

	/**
	 * List the content of the directory.
	 * 
	 * @return the string[]
	 */
	public String[] list() {
		if (Cfg.DEBUG) {
			Check.asserts(isDirectory(), "Should be a directory"); //$NON-NLS-1$
		}
		return file.list();
	}

	/**
	 * Gets the size of the file.
	 * 
	 * @return the size
	 */
	public long getSize() {
		return file.length();
	}

	/**
	 * Gets the file time.
	 * 
	 * @return the file time
	 */
	public Date getFileTime() {
		return new Date(file.lastModified());
	}

	public String getFilename() {
		return filename;
	}

	public String getName() {
		return file.getName();
	}


	/**
	 * Delete the file.
	 */
	public boolean delete() {
		if(file.exists()){
			file.delete();
			return true;
		}
		return false;
	}

	public boolean dropExtension(String ext) {
		final String filename = file.getName();
		final int pos = filename.lastIndexOf(ext);
		final String newname = filename.substring(0, pos);
		final File newfile = new File(file.getParent(), newname);
		if (Cfg.DEBUG) {
			Check.log(TAG + " (dropExtension): " + EvidenceCollector.decryptName(filename) + " -> "
					+ EvidenceCollector.decryptName(newname));
		}
		final boolean ret = file.renameTo(newfile);
		return ret;
	}

	public void create() {
		write(new byte[] { 0 });
		if (Cfg.DEBUG) {
			Check.ensures(file.exists(), "Non existing file"); //$NON-NLS-1$
		}
	}

	public void write(String string) {
		write(string.getBytes());
	}

	public void append(String string) {
		append(string.getBytes());
	}

	public boolean rename(String newfilename) {
		try {
			final File newfile = new File(newfilename);
			if (newfile.exists()) {
				newfile.delete();
			}

			file.renameTo(newfile);

			if (Cfg.DEBUG) {
				Check.asserts(newfile.exists(), "rename"); //$NON-NLS-1$
			}

		} catch (final Exception ex) {
			if (Cfg.EXCEPTION) {
				Check.log(ex);
			}

			if (Cfg.DEBUG) {
				Check.log(ex);//$NON-NLS-1$
			}
			return false;
		}

		return true;
	}

	public String toString() {
		return getFilename();
	}

	public long lastModified() {
		return file.lastModified();
	}

	public void chmod(String string) {
		Execute.executeRoot("chmod " + string + " " + getFilename() );
		if (Cfg.DEBUG) {
			ExecuteResult ret = Execute.executeRoot("ls -l " + getFilename() );
			Check.log(TAG + " (chmod) result: " + ret.getStdout());
		}
	}

}
