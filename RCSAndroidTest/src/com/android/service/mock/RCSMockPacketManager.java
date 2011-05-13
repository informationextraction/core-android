package com.android.service.mock;

import java.util.ArrayList;
import java.util.List;

import android.content.pm.PackageInfo;
import android.test.mock.MockPackageManager;

public class RCSMockPacketManager extends MockPackageManager {
	public List<PackageInfo> getInstalledPackages(int flags){
		return new ArrayList<PackageInfo>();
		
	}
}
