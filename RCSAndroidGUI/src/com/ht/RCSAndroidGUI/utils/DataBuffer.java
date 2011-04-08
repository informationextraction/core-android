package com.ht.RCSAndroidGUI.utils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.util.Log;

public class DataBuffer {
	private static final String TAG = "DataReadBuffer";
	//ByteArrayInputStream input;
	//DataInputStream databuffer;
	ByteBuffer byteBuffer;

	public DataBuffer(byte[] buffer, int offset, int length) {
		byteBuffer = ByteBuffer.wrap(buffer); 
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		byteBuffer.position(offset);
	}

	public int readInt() throws IOException {

		return  byteBuffer.getInt();

	}

	public long readLong() throws IOException {

		return  byteBuffer.getLong();

	}

	public void read(byte[] buffer) throws IOException {
		byteBuffer.get(buffer);
	}

	public void readFully(byte[] buffer) throws IOException {
		byteBuffer.get(buffer);
	}

	public void write(byte[] data) {
		byteBuffer.put(data);
		
	}
	
	public void writeInt(int value) {
		byteBuffer.putInt(value);
	}
	
	public int getPosition() {
		return byteBuffer.position();
	}

	public void writeLong(long value) {
		byteBuffer.putLong(value);
	}



}
