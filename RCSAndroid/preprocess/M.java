package com.android.mm;

public class M {
    public static String d(byte[] ebytes, byte[] kbytes) {

        for (int i = 0; i < ebytes.length - 2; i++) {
            ebytes[i] = (byte) (ebytes[i + 2] ^ kbytes[i + 1] ^ 0x42);
        }

        String value = new String(ebytes, 0, ebytes.length - 2);
        return value;
    }

    public static byte[] h2b(final String s) {
        int offset = 2;
        int len = s.length() - offset;
        final byte[] b = new byte[len / 2];
        for (int i = 0; i < b.length; i++) {
            final int index = offset + i * 2;
            final int v = Integer.parseInt(s.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }

    public static void printMostused() {

    }
}
