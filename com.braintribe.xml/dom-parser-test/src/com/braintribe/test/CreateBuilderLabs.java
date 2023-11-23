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
package com.braintribe.test;




import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;

public class CreateBuilderLabs {
	private static String xsdFides = "res/fides/FIDES Data Model V2.1";
	
	@Test
	public void testDefaultCreation() {
		try {
			DomParser.create().makeItSo();
		} catch (DomParserException e) {
			e.printStackTrace();
			Assert.fail( "Exception thrown");
		}
	}

	public void testNamespaceAwareCreation(){
		try {
			DomParser.create().setNamespaceAware().makeItSo();
		} catch (DomParserException e) {
			e.printStackTrace();
			Assert.fail( "Exception thrown");
		}
	}
	@Test
	public void testCopy() {
		try {
			Document document = DomParser.load().setNamespaceAware().from( new File( xsdFides + ".xsd"));
			Document copy = DomParser.create().setNamespaceAware().makeItSo(document);		
			DomParser.write().from( copy).to( new File( xsdFides + ".copy.xsd"));
		} catch (DomParserException e) {
			e.printStackTrace();
			Assert.fail( "Exception thrown");
		}
		
	}
}
