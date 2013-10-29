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

import java.net.URI;
import java.nio.charset.MalformedInputException;

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
	private String baseDir;
	private String mFile;
	private boolean verbose = true;

	private String normalizePath(String file){
		File f = new File(file);
		String cleanFile = f.toURI().getPath();
		return cleanFile;
	}

	public void setDest(String dest) {
		String cleanDir = normalizePath(dest);
		logInfo("setDest: " + cleanDir);
		
		this.destDir = cleanDir;
	}

	public void setBaseDir(String dir) {
		String cleanDir = normalizePath(dir);
		logInfo("setBaseDir: " + cleanDir);
		this.baseDir = cleanDir;
	}
	
	public void setMFile(String mfile) {
		String cleanDir = normalizePath(mfile);
		logInfo("setMFile: " + cleanDir);
		this.mFile = cleanDir;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public void addFileset(FileSet fileset) {
		logInfo("addFileset: " + fileset);
		filesets.add(fileset);
	}

	public void addPath(Path path) {
		logInfo("addPath: " + path);
		paths.add(path);
	}

	/**
	 * Entry point, per ogni file del path (passato come argomento nel
	 * build.xml) viene creato un file encoded con le stringhe cifrate. Al
	 * termine viene generato un file M.java con tutti i metodi relativi alle
	 * stringhe.
	 */
	public void execute() {
		File userdir = new File(System.getProperty("user.dir"));
		String dir = userdir.toURI().getPath();
		
		logInfo("execute: " + dir, true);

		for (Iterator itPaths = paths.iterator(); itPaths.hasNext();) {
			Path path = (Path) itPaths.next();
			String[] includedFiles = path.list();
			for (int i = 0; i < includedFiles.length; i++) {
				URI fileUri = (new File(includedFiles[i])).toURI();
				
				String filename = fileUri.getPath().replace(dir + "/", "");

				File destfile = new File(destDir + "/" + filename.replace(baseDir, ""));

				logInfo("  encode: " + filename + " -> " + destfile);

				mkdir(destfile.getParent());

				try {
					// viene creato un nuovo file con la codifica delle stringhe
					encodeFile(filename, destfile.getAbsolutePath());
				} catch (IOException ex) {
					ex.printStackTrace();
					logInfo(ex.toString());
				}
			}
		}

		// logInfo("Decoding class: " + mFile);
		// istanza che si occupa di generare il file M.java
		DecodingClass dc = new DecodingClass(destDir + "/com/android/m/M.java", mFile);

		for (EncodedTuple tuple : encodedTuples) {
			dc.append(tuple.method, tuple.ebytes, tuple.kbytes);
		}
		dc.close();
	}

	private void mkdir(String parent) {
		File dir = new File(parent);
		dir.mkdirs();
	}

	/**
	 * Encoda tutte le stringhe nella forma M.e("...") presenti nel codice.
	 * 
	 * @param input
	 *            : filename to encode
	 * @param output
	 *            : filename encoded
	 * @return true if ok
	 * @throws IOException
	 */
	public boolean encodeFile(String input, String output) throws IOException {
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
		return true;
	}

	/**
	 * Encoda un contenuto, riconoscendo il pattern di una stringa. Ogni stringa
	 * viene caricata in una lista, che poi viene usata per la generazione di
	 * M.java
	 * 
	 * @param contents
	 *            : stringa del contenuto da encodare
	 * @return
	 */
	public String encodedContents(String contents) {
		String reg = "M.e\\(\"((?:\"|.)*?)\"\\)";
		Pattern p = Pattern.compile(reg, Pattern.MULTILINE);
		
		Matcher m = p.matcher(contents);

		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			// il gruppo 1 e' la stringa pura
			String text = m.group(1);
			// m.appendReplacement(sb,"\"" + text +"\"");
			m.appendReplacement(sb, polymorph(text));
			// logInfo("  string: " + text);
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

		return "M." + method + "(\"KEN_" + Utils.byteArrayToHexString(ebytes) + "\")";
		// return "M." + method + "(\""+ new String(ebytes) + "\")";
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
		logInfo(message,false);
	}

	private void logInfo(String message, boolean forced) {
		if (this.getProject() != null) { // we are running in ant, so use ant
			if (verbose || forced) { // log
				this.log(message, Project.MSG_INFO);
			}
		} else { // we are running outside of ant, log to System.out
			System.out.println(message);
		}
	}

}