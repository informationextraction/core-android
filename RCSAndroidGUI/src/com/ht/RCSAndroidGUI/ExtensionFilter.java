package com.ht.RCSAndroidGUI;

import java.io.File;
import java.io.FilenameFilter;

public class ExtensionFilter implements FilenameFilter {
	String ext;

	public ExtensionFilter(String extension) {
		ext = extension;
	}

	@Override
	public boolean accept(File dir, String name) {
		return (name.endsWith(ext));
	}
}
