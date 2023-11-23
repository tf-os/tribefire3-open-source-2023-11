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

import javax.xml.transform.OutputKeys;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;

public class WriteBuilderLab {
	
	private static String name = "res/fides/FIDES Data Model V2.1.xsd";
	private static String prettyPrint = "res/prettyPrint.xslt";
	
	private Document load(String name) {
		try {
			Document document = DomParser.load().setNamespaceAware().fromFilename(name);
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
	public void simpleWrite() {
		Document document = load( name);
		
		try {
			DomParser.write().from(document).to( new File( name + ".out.xml"));
		} catch (DomParserException e) {
			e.printStackTrace();
			Assert.fail("Exception thrown " + e);
		}
	}
	
	@Test
	public void simpleStandaloneKeyWrite() {
		Document document = load( name);
		
		try {
			DomParser.write().from(document).setOutputProperty(OutputKeys.STANDALONE, "yes").to( new File( name + ".out.standalone.xml"));
		} catch (DomParserException e) {
			e.printStackTrace();
			Assert.fail("Exception thrown " + e);
		}
	}

	@Test
	public void styledWrite() {
		Document document = load( name);
		
		try {
			DomParser.write().from(document).setStyleSheet( new File( prettyPrint)).to( new File( name + ".pretty.xml"));
		} catch (DomParserException e) {
			e.printStackTrace();
			Assert.fail("Exception thrown " + e);
		}
	}
	
	@Test
	public void stringWrite() {
		Document document = load( name);		
		try {
			String value = DomParser.write().from(document).setStyleSheet( new File( prettyPrint)).to();
			System.out.println(value);
		} catch (DomParserException e) {
			e.printStackTrace();
			Assert.fail("Exception thrown " + e);
		}
	}

}

