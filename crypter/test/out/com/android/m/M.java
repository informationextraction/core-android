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
    public static String d_3643598957289446246(String enc, String k){
        return d(h2b(enc),h2b(k));
    }
    public static String d_1376774380473654882(String enc, String k){
        return d(h2b(enc),h2b(k));
    }
    public static String d_2910561011276521538(String enc, String k){
        return d(h2b(enc),h2b(k));
    }
    public static String d_6344730383328632404(String enc, String k){
        return d(h2b(enc),h2b(k));
    }
    public static String d_9012085659950326748(String enc, String k){
        return d(h2b(enc),h2b(k));
    }
}
