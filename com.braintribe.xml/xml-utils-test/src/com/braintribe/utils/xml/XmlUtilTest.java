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

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;

public class XmlUtilTest {

	@Test
	public void testWriteSave() throws Exception {

		Document doc = XmlTools.createDocument();
		Element element = doc.createElement("Hello");
		doc.appendChild(element);
		File tempDir = FileTools.createNewTempDir("testWriteSave");
		File tmpFile = new File(tempDir, "testWriteSave.xml");

		try {
			XmlTools.writeXml(doc, tmpFile, "UTF-8", true);
			String content = IOTools.slurp(tmpFile, "UTF-8");
			Assert.assertTrue(content.contains("<Hello"));
			
			File[] files = tempDir.listFiles();
			Assert.assertEquals(1, files.length);
			
		} finally {
			tmpFile.delete();
			tempDir.delete();
		}

	}

	@Test
	public void testWriteUnsave() throws Exception {

		Document doc = XmlTools.createDocument();
		Element element = doc.createElement("Hello");
		doc.appendChild(element);

		File tmpFile = File.createTempFile("testWriteSave", ".xml");

		try {
			XmlTools.writeXml(doc, tmpFile, "UTF-8", false);

			String content = IOTools.slurp(tmpFile, "UTF-8");

			Assert.assertTrue(content.contains("<Hello"));
		} finally {
			tmpFile.delete();
		}

	}

}
