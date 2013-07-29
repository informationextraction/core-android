package com.android.m;
import android.content.Context;
public class M { 
	public static String d(byte[] ebytes, byte[] obytes){
        for (int i = 0; i < obytes.length; i++) {
            ebytes[i] = (byte) (ebytes[i] ^ obytes[i]);
        }
    return new String(ebytes);
    }

	public static byte[] h2b(final String s) {
		final byte[] b = new byte[s.length() / 2];
		for (int i = 0; i < b.length; i++) {
			final int index =  i * 2;
			final int v = Integer.parseInt(s.substring(index, index + 2), 16);
			b[i] = (byte) v;
		}
		return b;
	}
    public static String d_7505203514998674524(String enc, String k){
        return d(h2b(enc),h2b(k));
    }
    public static String d_5018135512133502499(String enc, String k){
        return d(h2b(enc),h2b(k));
    }
    public static String d_2035547379118382189(String enc, String k){
        return d(h2b(enc),h2b(k));
    }
    public static String d_3974313388928130326(String enc, String k){
        return d(h2b(enc),h2b(k));
    }
    public static String d_7798164972276262173(String enc, String k){
        return d(h2b(enc),h2b(k));
    }
}
