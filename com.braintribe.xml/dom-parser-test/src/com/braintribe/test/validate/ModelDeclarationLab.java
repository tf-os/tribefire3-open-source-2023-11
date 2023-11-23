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
package com.braintribe.test.validate;

import java.io.File;
import java.io.StringWriter;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;

public class ModelDeclarationLab {

	private static File contents = new File("res/model-declaration");
	
	private static File xsdModelDeclaration = new File( contents, "model-declaration.xsd");
	private static File xmlModelDeclaration = new File( contents, "model-declaration.xml");

	
	
	
	private Document load(File file) {
		try {
			Document document = DomParser.load().setNamespaceAware().from( file);
			if (document == null) {
				Assert.fail("No document returned");
			}
			return document;
		} catch (DomParserException e) {
			e.printStackTrace();
			Assert.fail("Exception thrown " + e);
		}
		return null;
	}
	
	@Test
	public void testBooleanOutput() {
		Document xmlPayorderDocument = load( xmlModelDeclaration);
		Document xsdDocument = load( xsdModelDeclaration);
		try {
			boolean validate = DomParser.validate().from( xmlPayorderDocument).schema( xsdDocument).makeItSo();
			Assert.assertTrue( "file is not valid", validate);
		} catch (DomParserException e) {
			e.printStackTrace();
			Assert.fail("Exception thrown " + e);
		}	
	}
	
	@Test
	public void testStringOutput() {
		Document xsdDocument = load( xsdModelDeclaration);
		try {		
			String validate = DomParser.validate().from( xmlModelDeclaration).schema( xsdDocument).makeItToString();
			System.out.println("validates [" + validate + "]");
		} catch (DomParserException e) {
			e.printStackTrace();
			Assert.fail("Exception thrown " + e);
		}	
	}
	
	@Test
	public void testWriterOutput() {
		Document xsdDocument = load( xsdModelDeclaration);
		try {
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			boolean validate = DomParser.validate().from( xmlModelDeclaration).schema( xsdDocument).makeItSo(result);
			Assert.assertTrue( "file is not valid", validate);			
		} catch (DomParserException e) {
			e.printStackTrace();
			Assert.fail("Exception thrown " + e);
		}	
	}
	
	

}
