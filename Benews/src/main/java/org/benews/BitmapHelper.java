package org.benews;

import android.graphics.Bitmap;

/**
 * Created by zad on 31/10/14.
 */
public class BitmapHelper {
	// Scale and keep aspect ratio
	static public Bitmap scaleToFitWidth(Bitmap b, int width) {
		float factor = width / (float) b.getWidth();
		return Bitmap.createScaledBitmap(b, width, (int) (b.getHeight() * factor), false);
	}

	// Scale and keep aspect ratio
	static public Bitmap scaleToFitHeight(Bitmap b, int height) {
		float factor = height / (float) b.getHeight();
		return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factor), height, false);
	}

	// Scale and keep aspect ratio
	static public Bitmap scaleToFill(Bitmap b, int width, int height) {
		float factorH = height / (float) b.getWidth();
		float factorW = width / (float) b.getWidth();
		float factorToUse = (factorH > factorW) ? factorW : factorH;
		return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factorToUse), (int) (b.getHeight() * factorToUse), false);
	}

	// Scale and dont keep aspect ratio
	static public Bitmap strechToFill(Bitmap b, int width, int height) {
		float factorH = height / (float) b.getHeight();
		float factorW = width / (float) b.getWidth();
		return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factorW), (int) (b.getHeight() * factorH), false);
	}
}