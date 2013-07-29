
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
		int len = s.length() - 4;
		final byte[] b = new byte[len / 2];
		for (int i = 0; i < b.length; i++) {
			final int index =  offset + i * 2;
			final int v = Integer.parseInt(s.substring(index, index + 2), 16);
			b[i] = (byte) v;
		}
		return b;
	}

    public static String d_8556885829010529978(String enc, String k){
        return d(h2b(enc),h2b("KEY_8E39A9411BE1FD2B2609E32C" ));
    }
    public static String d_7546344379657951754(String enc, String k){
        return d(h2b(enc),h2b("KEY_D369593B1418" ));
    }
    public static String d_1256853341375437871(String enc, String k){
        return d(h2b(enc),h2b("KEY_C6FC4F86DD65BB01FFEEFF6E1EF153448D" ));
    }
    public static String d_4873860519919559393(String enc, String k){
        return d(h2b(enc),h2b("KEY_4F6018E564F46AD7F959" ));
    }
    public static String d_5088423595234502277(String enc, String k){
        return d(h2b(enc),h2b("KEY_C68AB7487F84F693E1" ));
    }
}
