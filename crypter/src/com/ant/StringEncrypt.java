package com.ant;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;

import sun.io.MalformedInputException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.security.SecureRandom;
import java.io.File;

public class StringEncrypt extends Task {

	private String destDir;
	private Vector filesets = new Vector();
	private Vector paths = new Vector();

	static SecureRandom random = new SecureRandom();

	class EncodedTuple {
		String method;
		byte[] ebytes;
		byte[] kbytes;
	}

	ArrayList<EncodedTuple> encodedTuples = new ArrayList<EncodedTuple>();

	public void setDest(String dest) {
		logInfo("setDest: " + dest);
		this.destDir = dest;
	}

	public void addFileset(FileSet fileset) {
		logInfo("addFileset: " + fileset);
		filesets.add(fileset);
	}

	public void addPath(Path path) {
		logInfo("addPath: " + path);
		paths.add(path);
	}

	public void execute() {
		String dir = System.getProperty("user.dir");
		logInfo("execute: " + dir);

		for (Iterator itPaths = paths.iterator(); itPaths.hasNext();) {
			Path path = (Path) itPaths.next();
			String[] includedFiles = path.list();
			for (int i = 0; i < includedFiles.length; i++) {
				String filename = includedFiles[i].replaceFirst(dir + "/", "");
				File destfile = new File(destDir + "/" + filename);

				logInfo("  encode: " + filename + " -> " + destfile);

				mkdir(destfile.getParent());

				try {
					encodeFile(filename, destfile.getAbsolutePath());
				} catch (IOException ex) {
					ex.printStackTrace();
					logInfo(ex.toString());
				}
			}
		}

		DecodingClass dc = new DecodingClass(destDir + "/com/android/m/M.java");

		for (EncodedTuple tuple : encodedTuples) {
			dc.append(tuple.method, tuple.ebytes, tuple.kbytes);
		}
		dc.close();
	}

	private void mkdir(String parent) {
		File dir = new File(parent);
		dir.mkdirs();
	}

	public boolean encodeFile(String input, String output) throws IOException {
		byte[] bytes = new byte[128 * 1024]; // large enough
		FileInputStream in = new FileInputStream(input);
		int bytesRead = in.read(bytes);
		in.close();
		String content = new String(bytes, 0, bytesRead, "ISO8859-1");
		if (malformedContent(content)) {
			throw new MalformedInputException();
		}
		content = encodedContents(content);
		bytes = content.getBytes("ISO8859-1");
		FileOutputStream fout = new FileOutputStream(output);
		fout.write(bytes);
		fout.close();
		return true;
	}

	@SuppressWarnings("deprecation")
	private boolean malformedContent(String contents) throws MalformedInputException {
		logInfo("encoded: " + contents);
		Pattern p = Pattern.compile("M.e\\(\"[^\"]+^");
		Matcher m = p.matcher(contents);

		while (m.find()) {
			logInfo("found malformed " + m.groupCount() + " :" + m.group());
			throw new MalformedInputException();
		}
		return false;
	}

	public String encodedContents(String contents) {
		logInfo("encoded: " + contents);
		Pattern p = Pattern.compile("M.e\\(\"([^\"]+)\"\\)", Pattern.MULTILINE);
		Matcher m = p.matcher(contents);

		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			logInfo("found " + m.groupCount() + " :" + m.group());
			for (int i = 0; i <= m.groupCount(); i++) {
				String g = m.group(i);
				logInfo(" group " + i + ": " + g);
			}
			String text = m.group(1);
			// m.appendReplacement(sb,"\"" + text +"\"");
			m.appendReplacement(sb, polymorph(text));
		}
		m.appendTail(sb);

		return sb.toString();
	}

	private String polymorph(String text) {
		byte[] ebytes = text.getBytes();
		byte[] kbytes = new byte[ebytes.length];
		random.nextBytes(kbytes);

		enc(ebytes, kbytes);

		String method = "d_" + Math.abs(random.nextLong());

		// createDecodingClass(method, ebytes, kbytes);
		appendStringDecode(method, ebytes, kbytes);

		return "M." + method + "(\""+ byteArrayToHexString(ebytes) + "\",\"" + byteArrayToHexString(kbytes) + "\")";
		//return "M." + method + "(\""+ new String(ebytes) + "\")";
	}

	private void appendStringDecode(String method, byte[] ebytes, byte[] kbytes) {
		EncodedTuple tuple = new EncodedTuple();
		tuple.method = method;
		tuple.ebytes = ebytes;
		tuple.kbytes = kbytes;

		encodedTuples.add(tuple);
	}

	
	private void enc(byte[] ebytes, byte[] kbytes) {
		for (int i = 0; i < kbytes.length; i++) {
			ebytes[i] = (byte) (ebytes[i] ^ kbytes[i]);
		}
	}

	private static String enc(String text) {
		byte[] ebytes = text.getBytes();
		byte[] obytes = new byte[ebytes.length];
		random.nextBytes(obytes);

		for (int i = 0; i < obytes.length; i++) {
			ebytes[i] = (byte) (ebytes[i] ^ obytes[i]);
		}
		return new String(ebytes);
	}

	private void logInfo(String message) {
		if (this.getProject() != null) { // we are running in ant, so use ant
											// log
			this.log(message, Project.MSG_INFO);
		} else { // we are running outside of ant, log to System.out
			System.out.println(message);
		}
	}

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
