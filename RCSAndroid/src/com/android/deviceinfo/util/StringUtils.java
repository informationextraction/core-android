package com.android.deviceinfo.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import com.android.deviceinfo.auto.Cfg;

public class StringUtils {
	private static final String TAG = "StringUtils";

	/**
	 * Byte array to hex string.
	 * 
	 * @param b
	 *            the b
	 * @return the string
	 */
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

	/**
	 * Hex string to byte array.
	 * 
	 * @param s
	 *            the s
	 * @return the byte[]
	 */
	public static byte[] hexStringToByteArray(final String s, int offset, int len) {
		final byte[] b = new byte[len / 2];

		for (int i = 0; i < b.length; i++) {
			final int index = offset + i * 2;
			final int v = Integer.parseInt(s.substring(index, index + 2), 16);
			b[i] = (byte) v;
		}
		return b;
	}

	public static byte[] hexStringToByteArray(final String string) {
		return hexStringToByteArray(string, 0, string.length());
	}

	/**
	 * Hex string to byte array2.
	 * 
	 * @param config
	 *            the config
	 * @return the byte[]
	 */
	public static byte[] hexStringToByteArray2(final String config) {

		final int offset = 4;
		final int len = config.length() / 2;
		final byte[] ret = new byte[len + 4];

		for (int i = offset; i < ret.length; i++) {
			final char first = config.charAt((i - offset) * 2);
			final char second = config.charAt((i - offset) * 2 + 1);

			int value = Integer.parseInt(new String(new byte[] { (byte) first }), 16) << 4;
			value += second;

			ret[i] = (byte) Integer.parseInt(new String(new byte[] { (byte) second }), 16);
		}

		final DataBuffer databuffer = new DataBuffer(ret, 0, 4);
		databuffer.writeInt(len);

		return ret;
	}

	public static byte[] hexStringToByteArray2(final String wchar, int offset, int len) {

		final byte[] ret = new byte[len / 2];

		for (int i = 0; i < ret.length; i++) {
			final char first = wchar.charAt(offset + (i * 2));
			final char second = wchar.charAt(offset + (i * 2 + 1));

			// int value = NumberUtilities.hexDigitToInt(first) << 4;
			// value += NumberUtilities.hexDigitToInt(second);
			int value = Integer.parseInt(new String(new byte[] { (byte) first }), 16) << 4;
			value += second;

			// #ifdef DBC
			if (Cfg.DEBUG) {
				Check.asserts(value >= 0 && value < 256, "HexStringToByteArray: wrong value"); //$NON-NLS-1$
				// #endif
			}

			ret[i] = (byte) value;
		}

		return ret;
	}

	public static boolean matchStar(String wildcardProcess, String processName) {

		if (processName == null) {
			return (wildcardProcess == null);
		}

		for (;;) {
			if (wildcardProcess.length() == 0) {
				return (processName.length() == 0);
			}

			if (wildcardProcess.charAt(0) == '*') {
				wildcardProcess = wildcardProcess.substring(1);

				if (wildcardProcess.length() == 0) {
					return true;
				}

				if (wildcardProcess.charAt(0) != '?' && wildcardProcess.charAt(0) != '*') {
					final int len = processName.length();

					for (int i = 0; i < len; i++) {
						final char c = processName.charAt(0);

						processName = processName.substring(1);
						final String tp = wildcardProcess.substring(1);

						if (c == wildcardProcess.charAt(0) && matchStar(tp, processName)) {
							return true;
						}
					}

					return false;
				}

				for (int i = 0; i < processName.length(); i++) {
					processName = processName.substring(1);

					if (matchStar(wildcardProcess, processName)) {
						return true;
					}
				}

				return false;
			}

			if (processName.length() == 0) {
				return false;
			}

			if (wildcardProcess.charAt(0) != '?' && wildcardProcess.charAt(0) != processName.charAt(0)) {
				return false;
			}

			processName = processName.substring(1);
			wildcardProcess = wildcardProcess.substring(1);
		}
		// NOTREACHED
	}

	public static String inputStreamToString(InputStream is) throws IOException {
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}

	/**
	 * Chomp.
	 * 
	 * @param sd
	 *            the sd
	 * @param c
	 *            the c
	 * @return the string
	 */
	public static String chomp(final String sd, final String c) {
		if (sd == null) {
			return null;
		}
		if (sd.length() == 0) {
			return ""; //$NON-NLS-1$
		}
		if (sd.endsWith(c)) {
			return sd.substring(0, sd.length() - c.length());
		}

		return sd;
	}

	/**
	 * Unspace.
	 * 
	 * @param string
	 *            the string
	 * @return the string
	 */
	public static String unspace(final String string) {
		if (Cfg.DEBUG) {
			Check.requires(string != null, "Unspace: null string"); //$NON-NLS-1$
		}
		if (string == null) {
			return null;
		}
		final StringBuffer unspace = new StringBuffer();
		int spaces = 0;
		final int len = string.length();
		for (int i = 0; i < len; i++) {
			final char c = string.charAt(i);
			if (c != ' ') {
				unspace.append(c);
			} else {
				spaces++;
			}
		}
		if (Cfg.DEBUG) {
			Check.ensures(unspace.length() + spaces == string.length(), "Unspace: wrong spaces"); //$NON-NLS-1$
		}
		return unspace.toString();
	}

	public static boolean isEmpty(String string) {
		return string == null || string.length() == 0;
	}

	public static String join(List<String> lines, String delimiter, int start) {
		String listString = "";

		int counter = 0;
		for (String s : lines) {
			if (counter++ < start)
				continue;
			listString += s + delimiter;
		}

		return listString;
	}

	public static String join(List<String> lines) {
		return join(lines, "", 0);
	}

	public static String join(String[] lines, String delimiter, int start) {
		String listString = "";

		int counter = 0;
		for (String s : lines) {
			if (counter++ < start)
				continue;
			listString += s + delimiter;
		}

		return listString;
	}
	
	public static String join(String[] lines){
		return join(lines, "", 0);
	}

	public static String readFile(String filename) {
		String ret = "";
		File file = new File(filename);
		try {
			FileInputStream is = new FileInputStream(file);
			ret = inputStreamToString(is);
		} catch (FileNotFoundException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readFile) Error: " + e);
			}
		} catch (IOException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readFile) Error: " + e);
			}
		}
		return ret;
	}

	public static List<String> readFileLines(String filename) {
		String line = "";
		List<String> lines = new ArrayList<String>();

		File file = new File(filename);
		BufferedReader reader = null;
		try {

			reader = new BufferedReader(new FileReader(file));
			do {
				line = reader.readLine();
				if(line!=null){
					lines.add(line);
				}else{
					break;
				}
			} while (true);

		} catch (FileNotFoundException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readFile) Error: " + e);
			}
		} catch (IOException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readFile) Error: " + e);
			}
		}finally{
			if (reader != null){
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
		return lines;
	}

	public static String[] split(String e) {
		return e.split(",");
	}
}
