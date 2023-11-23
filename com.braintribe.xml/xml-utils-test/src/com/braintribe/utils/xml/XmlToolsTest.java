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
package com.braintribe.utils.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.Test;
import org.w3c.dom.Document;

import com.braintribe.exception.Exceptions;
import com.braintribe.utils.FileTools;

public class XmlToolsTest {

	@Test
	public void testXXEAttack() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("<?gm-xml version=\"3\"?>\n");
		sb.append("<!DOCTYPE lolz [\n");
		sb.append("<!ENTITY lol SYSTEM \"file:///\">\n");
		sb.append("]>\n");
		sb.append("<gm-data><string>&lol;</string></gm-data>\n");
		ByteArrayInputStream bais = new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));

		@SuppressWarnings("unused")
		Document xml = null;
		try { 
			xml = XmlTools.loadXML(bais);
			throw new AssertionError("XXE attack was successful.");
		} catch(Exception e) {
			//This is expected
			return;
		}

		/* This was for testing... this point should not be reached anymore. Keeping this for future purposes. 
		BtAssertions.assertThat(xml).isNotNull();

		Element documentElement = xml.getDocumentElement();

		BtAssertions.assertThat(documentElement).isNotNull();

		Node stringChild = documentElement.getFirstChild();

		BtAssertions.assertThat(stringChild).isNotNull();
		BtAssertions.assertThat(stringChild.getNodeName()).isEqualTo("string");

		String textContent = stringChild.getTextContent();

		BtAssertions.assertThat(textContent).isNotNull();
		BtAssertions.assertThat(textContent).isNotEmpty();

		System.out.println(textContent);
		*/
	}

	@Test
	public void testLocalXInclude() throws Exception {
		Document document = XmlTools.loadXML(new File("res/Folder1/importing2.xml"));
		XmlTools.xinclude(document, null);

		String name = XmlTools.evaluateXPathToString(document, "/ContentHierarchy/node[@id='10000000']/node[@id='11000000']/@name", null);
		assertThat(name).isEqualTo("HL");
	}
	
	@Test
	public void testForeignXInclude() throws Exception {
		Document document = XmlTools.loadXML(new File("res/Folder1/importing1.xml"));
		XmlTools.xinclude(document, id -> {
			String filename = FileTools.getFilenameFromFullPath(id);
			File inputFile = new File("res/Folder2/"+filename);
			try {
				return new FileInputStream(inputFile);
			} catch (FileNotFoundException e) {
				throw Exceptions.unchecked(e, "Could not open file "+inputFile.getAbsolutePath());
			}
		});

		String name = XmlTools.evaluateXPathToString(document, "/ContentHierarchy/node[@id='10000000']/node[@id='11000000']/@name", null);
		assertThat(name).isEqualTo("HL");
	}
	
}
