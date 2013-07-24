package com.example.ant;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Vector;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.security.SecureRandom;

public class StringEncrypt extends Task {

	private String destDir;
	private Vector filesets = new Vector();
	private Vector paths = new Vector();
	
	SecureRandom random = new SecureRandom();

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
				String destfile = destDir + "/" + filename;
				logInfo("  encode: " + filename + " -> " + destfile);

			}
		}
	}

	public static void encodeFile(String input, String output) throws Exception {
		byte[] bytes = new byte[128 * 1024]; // large enough
		FileInputStream in = new FileInputStream(input);
		int bytesRead = in.read(bytes);
		in.close();
		String content = new String(bytes, 0, bytesRead, "ISO8859-1");
		content = encodedContents(content);
		bytes = content.getBytes("ISO8859-1");
		FileOutputStream fout = new FileOutputStream(output);
		fout.write(bytes);
		fout.close();
	}

	public static String encodedContents(String contents) {
		Pattern p = Pattern.compile("M.e(\"([^\"]+)\")");
		Matcher m = p.matcher(contents);
		int delta = 0;
		while (m.find()) {
			System.out.println("encoding string " + m.start() + " " + m.end());
			String start = contents.substring(0, m.start() + delta);
			String text = contents.substring(m.start() + delta + 1, m.end() + delta - 1);
			String end = contents.substring(m.end() + delta);
			
			contents = start + "M.d(\"" + enc(text) + "\")" + end;
			delta += 15;
			
		}
		return contents;
	}
	
	private String enc(String text){
		byte[] ebytes = encoded.getBytes();
		byte[] obytes = new byte[ebytes];
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

}
