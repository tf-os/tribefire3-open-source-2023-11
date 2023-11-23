// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.model.smood.tools.xml;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.braintribe.utils.xml.XmlTools;

// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

/**
 * @author peter.gazdik
 */
public class RenamePropertiesInXmlMain {

	static String oldFileName = "res/old.current.xml";
	static String newFileName = "res/current.xml";

	public static void main(String[] args) throws Exception {
		new RenamePropertiesInXmlMain().run();
	}

	private void run() throws Exception {
		Document document = readXmlFile(oldFileName);

		Element element = document.getDocumentElement();

		// attribute 'p' represents the name of the property
		// so we find @p='id' and rename it to 'refId', then 'descriptorId' -> 'id'
		// NOTE that this works because all our original id's were longs, but they could have been anything
		set(element, "/gm-data/pool/E[contains(@id, 'PersistentEntityReference^TDg8y')]/l[@p='id']", "p", "refId");
		set(element, "/gm-data/pool/E[contains(@id, 'PersistentEntityReference^TDg8y')]/l[@p='descriptorId']", "p", "id");

		XmlTools.writeXml(document, new File(newFileName), "UTF-8");

		System.out.println("Xml processing finished!");
	}

	/**
	 * Finds nodes for given path and set's value for given attribute name.
	 */
	private void set(Element element, String xpath, String attributeName, String attributeValue) throws Exception {
		NodeList elements = XmlTools.getElements(element, xpath);

		System.out.println("Setting " + attributeName + " = " + attributeValue + " for " + elements.getLength() + " elements.");

		for (int i = 0; i < elements.getLength(); i++) {
			Element idValueElement = (Element) elements.item(i);
			idValueElement.setAttribute(attributeName, attributeValue);
		}
	}

	private Document readXmlFile(String fileName) {
		File file = new File(fileName);

		try {
			return XmlTools.loadXML(file);
		} catch (Exception e) {
			throw new RuntimeException("Error while reading file: " + file.getAbsolutePath(), e);
		}
	}

}
