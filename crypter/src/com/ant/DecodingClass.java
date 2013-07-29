package com.ant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class DecodingClass {

	String header = "package com.android.m;\nimport android.content.Context;\npublic class M { \n";
	String dec = "	public static String d(byte[] ebytes, byte[] obytes){\n        for (int i = 0; i < obytes.length; i++) {\n            ebytes[i] = (byte) (ebytes[i] ^ obytes[i]);\n        }\n    return new String(ebytes);\n    }\n\n";
	
	String h2b = "	public static byte[] h2b(final String s) {\n		final byte[] b = new byte[s.length() / 2];\n		for (int i = 0; i < b.length; i++) {\n			final int index =  i * 2;\n			final int v = Integer.parseInt(s.substring(index, index + 2), 16);\n			b[i] = (byte) v;\n		}\n		return b;\n	}\n";
	FileOutputStream fout;

	public DecodingClass(String filename) {

		File classFile = new File(filename);

		classFile.getParentFile().mkdirs();

		try {
			fout = new FileOutputStream(classFile);
			fout.write(header.getBytes());
			fout.write(dec.getBytes());
			fout.write(h2b.getBytes());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void append(String method, byte[] ebytes, byte[] kbytes) {

		try {

			String m1 = "    public static String " + method +"(String enc, String k){\n";
			String m2 = "        return d(h2b(enc),h2b(k));\n    }\n";
			fout.write(m1.getBytes());
			fout.write(m2.getBytes());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void close() {
		try {
			fout.write("}\n".getBytes());
			fout.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
