package com.ht.RCSAndroidGUI.utils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class DataBuffer extends DataInputStream
{

	public DataBuffer(byte[] content, int offset, int length) {
		super(new ByteArrayInputStream(content,offset,length));
	}


}
