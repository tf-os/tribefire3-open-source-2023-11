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

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;

public class FidesLab {

	private static File contents = new File( "res/fides");
	private static File xsdFides = new File( contents, "FIDES Data Model V2.1.xsd");
	private static File xmlFidesPayorder = new File( contents, "res/payorder.xml");
	private static File xmlFidesStatement = new File( contents, "statementgroup.xml");
	
	
	
	private Document load(File name) {
		try {
			Document document = DomParser.load().setNamespaceAware().from(name);
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
	
	//@Test
	public void testPayorder() {
		Document xmlPayorderDocument = load( xmlFidesPayorder);
		Document xsdDocument = load( xsdFides);
		try {
			boolean validate = DomParser.validate().from( xmlPayorderDocument).schema( xsdDocument).makeItSo();
			System.out.println("validates [" + validate + "]");
		} catch (DomParserException e) {
			e.printStackTrace();
			Assert.fail("Exception thrown " + e);
		}	
	}
	@Test
	public void testStatementGroup() {
		Document xmlPayorderDocument = load( xmlFidesStatement);
		Document xsdDocument = load( xsdFides);
		try {
			boolean validate = DomParser.validate().from( xmlPayorderDocument).schema( xsdDocument).makeItSo();
			System.out.println("validates [" + validate + "]");
		} catch (DomParserException e) {
			e.printStackTrace();
			Assert.fail("Exception thrown " + e);
		}	
	}
	
	

}
