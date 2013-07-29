package com.android.m;
import android.content.Context;
public class M { 
	public static String d(String encoded, String otp){
		byte[] ebytes = encoded.getBytes();byte[] obytes = otp.getBytes();
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
    public String d_5809637788610185088(String enc, String k){
        return d(h2b(enc),h2b(k));
    }
    public String d_9127839932680584584(String enc, String k){
        return d(h2b(enc),h2b(k));
    }
    public String d_7762178137826358237(String enc, String k){
        return d(h2b(enc),h2b(k));
    }
    public String d_8628444064454036387(String enc, String k){
        return d(h2b(enc),h2b(k));
    }
    public String d_2667424404935350625(String enc, String k){
        return d(h2b(enc),h2b(k));
    }
}
