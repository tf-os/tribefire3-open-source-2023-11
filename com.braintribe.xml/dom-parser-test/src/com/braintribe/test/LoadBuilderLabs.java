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


import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;

public class LoadBuilderLabs {

	private static String name = "res/fides/FIDES Data Model V2.1.xsd";
	
	@Test
	public void testSimpleLoad() {
		try {
			Document document = DomParser.load().fromFilename(name);
			if (document == null) {
				Assert.fail("No document returned");
			}
		} catch (DomParserException e) {
			e.printStackTrace();
			Assert.fail("Exception thrown " + e);
		}
	}
	// cannot be reached anymore 2019.01.08, pit 
	public void testUrlLoad() {
		String urlAsString = "http://nvd.nist.gov/schema/scap-core_0.1.xsd";
		try {
			Document doc = DomParser.load().from( new URL( urlAsString));
			if (doc == null) {
				Assert.fail( "No document returned");
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			Assert.fail("Exception thrown " + e);
		} catch (DomParserException e) {
			e.printStackTrace();
			Assert.fail("Exception thrown " + e);
		}
	}

	// pointless : cannot be reached anymore 2019.01.08, pit 
	public void testUnresolvedUrlLoad() {
		String urlAsString = "http://nvd.nist.gov/schema/scap-core_0.1.xsd.x";
		try {
			Document doc = DomParser.load().from( new URL( urlAsString));
			if (doc != null) {
				Assert.fail( "Document returned");
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			Assert.fail("Exception thrown " + e);
		} catch (DomParserException e) {
			return;
		}
		Assert.fail("No Exception thrown ");
	}

}
