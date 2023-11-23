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
package com.braintribe.xml.parser.sax.test;

import java.io.File;

import org.junit.Test;
import org.xml.sax.SAXParseException;

import com.braintribe.xml.parser.sax.test.framework.TestRunner;

public class CsParseLab {

	@Test
	public void testPain1AutoXsd() {
		String name = "res/parse/cs/pain.001.001.03.a.xml";
		TestRunner.runTest( 5, 55, new File(name), null);				
	}	
	@Test
	public void testPain1SpecifiedXsd() {
		String name = "res/parse/cs/pain.001.001.03.a.xml";
		String schema = "res/parse/cs/pain.001.001.03.ISO.xsd";		
		TestRunner.runTest( new File(name), new File( schema), null);
	}	
		
	@Test
	public void testPacs() {
		String name = "res/parse/cs/pacs.008.001.01.a.xml";
		String schema = "res/parse/cs/pacs.008.001.01.ISO.xsd";		
		TestRunner.runTest( 5, 105, new File(name), new File( schema));				
	}
	
	@Test
	public void testDumpedPacs() {
		String name = "res/parse/cs/dump/pacs.008.01.01.a.out.xml";
		String schema = "res/parse/cs/pacs.008.001.01.ISO.xsd";		
		TestRunner.runTest( 5, 105, new File(name), new File( schema));				
	}
	
	@Test
	public void testMissingXsdPacs() {
		String name = "res/parse/cs/pacs.008.001.01.missing.xml";
		String schema = "res/parse/cs/pacs.008.001.01.ISO.xsd";
		TestRunner.runTest( new File(name), new File( schema), null);
	}

	
	@Test
	public void testCrookedPacsSpecifiedXsd() {
		String name = "res/parse/cs/pacs.008.001.01.crooked.xml";
		String schema = "res/parse/cs/pacs.008.001.01.ISO.xsd";		
		TestRunner.runTest( new File(name), new File( schema), com.braintribe.utils.xml.parser.sax.SaxParserException.class);		
	}
	@Test
	public void testCrookedPacsAutoXsd() {
		String name = "res/parse/cs/pacs.008.001.01.crooked.xml";			
		TestRunner.runTest( new File(name), null, com.braintribe.utils.xml.parser.sax.SaxParserException.class);		
	}
	@Test
	public void testCrookedPainSpecifiedXsd() {
		String name = "res/parse/cs/pain.001.001.03.crooked.xml";
		String schema = "res/parse/cs/pain.001.001.03.ISO.xsd";
		TestRunner.runTest( new File(name), new File( schema), com.braintribe.utils.xml.parser.sax.SaxParserException.class);				
	}
	
	@Test
	public void testCrookedPainAutoXsd() {
		String name = "res/parse/cs/pain.001.001.03.crooked.xml";		
		TestRunner.runTest( new File(name), null, com.braintribe.utils.xml.parser.sax.SaxParserException.class);				
	}

}
