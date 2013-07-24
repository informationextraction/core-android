package com.android.deviceinfo.interfaces;

import android.hardware.Camera;

public interface SnapshotManager extends Camera.PictureCallback{

	void cameraReady();
	
}
