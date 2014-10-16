package org.benews;

import android.util.Log;

/**
 * Created by zad on 15/10/14.
 * to refresh the c header fot the library libson:
 * 1) cd in src directory of the .java : cd src/main/java/
 * 2) build the single java : javac -d tmp .org/benews/BsonBridge.java
 * 3) cd tmp
 * 4) generate c header : javah -jni org.benews.BsonBridge
 * 5) resulting header file in our case is /tmp/org_benews_BsonBridge.h
 * 6) it can be copied in src/libbson/include/bsonBridge.h
 * */
public class BsonBridge {
	static {
		System.loadLibrary("bson");
	}


	public static final String TAG = "BsonBridge";
	public static final int BSON_TYPE_TEXT = 0;

	public static native int serialize2( String filename, int type,  int size, byte[] payload);

	public static native String getGreatings( String filename, int type,  int size, byte[] payload);


	public static String serializeBson (String filename, int type, int size, String payload){
		int i = serialize2(filename,type,size,payload.getBytes());
		Log.d(TAG,"serialize called\n");
		return getGreatings(filename,type,size,payload.getBytes()) + payload;
		//return String.format("returned %s",i);
	}

}
