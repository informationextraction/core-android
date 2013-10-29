package com.ant;

public class Utils {
	public static String byteArrayToHexString(final byte[] b) {
		final StringBuffer sb = new StringBuffer(b.length * 2);

		for (final byte element : b) {
			final int v = element & 0xff;
			if (v < 16) {
				sb.append('0');
			}
			sb.append(Integer.toHexString(v));
		}
		return sb.toString().toUpperCase();
	}
}
