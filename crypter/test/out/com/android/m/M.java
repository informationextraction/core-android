
package com.android.m;

public class M { 
	public static String d(byte[] ebytes, byte[] obytes){
        for (int i = 0; i < obytes.length; i++) {
            ebytes[i] = (byte) (ebytes[i] ^ obytes[i]);
        }
    return new String(ebytes);
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
	
	public static String e(String text){
		return text;
	}

    public static String d_5568204159545330181(String enc){
        return d(h2b(enc),h2b("KEY_29AEA2AC65F22DA775C48B07" ));
    }
    public static String d_2754041101130777629(String enc){
        return d(h2b(enc),h2b("KEY_E313A9D3FFACE18DE0CE1794C867AAD494" ));
    }
    public static String d_1325326430353196588(String enc){
        return d(h2b(enc),h2b("KEY_28601A10CB3D" ));
    }
    public static String d_8929138350623224208(String enc){
        return d(h2b(enc),h2b("KEY_ABF12FCA175870A9BBEF6269797D849AD5" ));
    }
    public static String d_6399948454043164002(String enc){
        return d(h2b(enc),h2b("KEY_C2BB4EFD3F18D8D78653" ));
    }
    public static String d_4705656050497689215(String enc){
        return d(h2b(enc),h2b("KEY_EFA8521DF6F8B616C5" ));
    }
}
