package com.android.dvci.interfaces;

import android.hardware.Camera;

public interface SnapshotManager extends Camera.PictureCallback{

	void cameraReady();
	
}
