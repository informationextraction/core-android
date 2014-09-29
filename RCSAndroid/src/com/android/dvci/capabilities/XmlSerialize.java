/* *********************************************
 * Create by : Alberto "Q" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 30-jun-2011
 * Comments  : Grazie Google per NON aver creato questa classe... 
 **********************************************/

package com.android.dvci.capabilities;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class XmlSerialize {
	private static final String TAG = "XmlSerialize";

	// Questa funzione fa mille milione di allocazioni...
	private String docToString(NodeList list) {
		StringBuffer returnValue = new StringBuffer(128 * 1024);

		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i).getNodeType() == 3) {
				returnValue.append(list.item(i).getNodeValue());
			} else {
				returnValue.append("\n<").append(list.item(i).getNodeName());

				for (int j = 0; j < list.item(i).getAttributes().getLength(); j++) {
					returnValue.append(" ").append(list.item(i).getAttributes().item(j).getNodeName()).append("=\"")
							.append(list.item(i).getAttributes().item(j).getNodeValue()).append("\"");
				}

				returnValue.append(">");
			}

			if (list.item(i).getChildNodes().getLength() > 0) {
				returnValue.append(docToString(list.item(i).getChildNodes()));
			}

			if (list.item(i).getNodeType() == 3) {
				continue;
			}

			if (returnValue.substring(returnValue.length() - 1).equals(">")) {
				returnValue.append("\n");
			}

			returnValue.append("</").append(list.item(i).getNodeName()).append(">");
		}

		returnValue.trimToSize();
		return returnValue.toString();
	}

	public static final String xmlDocumentToString(Document document) {
		String returnValue = "";

		XmlSerialize util = new XmlSerialize();
		returnValue = util.docToString(document.getChildNodes()).trim();

		return returnValue;
	}
}
