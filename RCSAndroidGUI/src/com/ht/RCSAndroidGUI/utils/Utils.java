/*******************************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 01-dec-2010
 *******************************************************/

package com.ht.RCSAndroidGUI.utils;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.SecureRandom;
import java.util.Random;

public final class Utils {
	/**
	 * Converts a Buffer to a DataInputStream.
	 * 
	 * @param buffer : Input buffer
	 * @return DataInputStream, must be closed by the caller.
	 */
	static final DataInputStream BufferToDataInputStream(byte[] buffer) {
		ByteArrayInputStream bufferByteStream = new ByteArrayInputStream(buffer);
		DataInputStream bufferDataStream = new DataInputStream(bufferByteStream);
		
		try {
			bufferByteStream.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			Log.d("RCS", "IOException() caught in Utils.BufferToDataInputStream()");
		}
		
		return bufferDataStream;
	}
	
	/**
	 * Converts a Buffer to a ByteBuffer.
	 * 
	 * @param buffer : Input buffer
	 * @param order : ByteOrder, LITTLE_ENDIAN or BIG_ENDIAN
	 * @return ByteBuffer.
	 */
	public static final ByteBuffer BufferToByteBuffer(byte[] buffer, ByteOrder order) {
		ByteBuffer retBuff = ByteBuffer.wrap(buffer);
		retBuff.order(order);
		
		return retBuff;
	}
	
	/**
	 * Converts a Buffer to a ByteBuffer with boundary constraints.
	 * 
	 * @param buffer : Input buffer
	 * @param order : ByteOrder, LITTLE_ENDIAN or BIG_ENDIAN
	 * @param start : Offset from which to start the buffer creation
	 * @param len : Length at which the conversion will stop
	 * @return ByteBuffer.
	 */
	static final ByteBuffer BufferToByteBuffer(byte[] buffer, ByteOrder order, int start, int len) {
		ByteBuffer retBuff = ByteBuffer.wrap(buffer, start, len);
		retBuff.order(order);
		
		return retBuff;
	}
	
	/**
	 * Converts an InputStream into a buffer.
	 * 
	 * @param iStream : InputStream that will be converted
	 * @param offset: Used to discard _offset_ bytes from the resource
	 * @return byte[], an array filled with data from InpustrStream.
	 */
	public static final byte[] InputStreamToBuffer(InputStream iStream, int offset) {
		try {
			int i, count = 0;

			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
		    
	        i = iStream.read();
	        count++;
	        
	        while (i != -1) {
	        	if (count > offset)
	        		byteArrayOutputStream.write(i);
	        	
	            i = iStream.read();
	            count++;
	        }
	        
	        iStream.close();
	        
	        return byteArrayOutputStream.toByteArray();
		} catch (IOException e) {
	        e.printStackTrace();
	        Log.d("RCS", "IOException() caught in Utils.RawResourceToBuffer()");
	        return null;
	    }
	}
	
	/**
	 * Compare BufferA with BufferB and return the resul
	 * 
	 * @param bufferA : first buffer
	 * @param offsetA : index from which to start
	 * @param bufferB : second buffer
	 * @param offsetB : index from which to start
	 * @param len : number of bytes to compare
	 * @return false when the buffers are different, true if they're the sam
	 */
	public static boolean equals(byte[] bufferA, int offsetA, byte[] bufferB, int offsetB, int len) {
		if (len < 0) 
			return false;
		
		if (offsetA < 0 || offsetA > bufferA.length) 
			return false;
		
		if (offsetB < 0 || offsetB > bufferB.length) 
			return false;
		
		if (offsetA + len > bufferA.length)
			return false;
		
		if (offsetB + len > bufferB.length)
			return false;
		
		for (int i = 0; i < len; i++) {
			if (bufferA[offsetA + i] != bufferB[offsetB + i])
				return false;
		}
		
		return true;
	}
	
    /**
     * Search a buffer looking for a token, returns token's index.
     * 
     * @param buffer : buffer to search
     * @param token : token to look for
     * @return start position of token into the buffer, -1 if token is not found
     */
    public static int getIndex(final byte[] buffer, final byte[] token) {
        for (int i = 0; i < buffer.length; i++) {
        	if (equals(buffer, i, token, 0, token.length))
	           	return i;	            
        }

        return -1;
    }
    
    public static void sleep(int t) {
    	try {
			Thread.sleep(t);
		} catch (InterruptedException e) {
			Log.d("RCS", "sleep() throwed an exception");
			e.printStackTrace();
		}
    }
    
    public static long getUniqueId() {   	
    	SecureRandom rand = new SecureRandom();
    	
    	return rand.nextLong();
    }
    
    public static long getTimeStamp() {
		return System.currentTimeMillis();
    }
}
