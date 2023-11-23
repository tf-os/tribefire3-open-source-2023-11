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
package tribefire.extension.xml.schemed.test.marshaller.xsd.swift;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

import tribefire.extension.xml.schemed.model.xsd.Schema;
import tribefire.extension.xml.schemed.test.marshaller.xsd.AbstractXsdMarshallerTest;

public class SwiftXsdTest extends AbstractXsdMarshallerTest {
	private static final String SWIFT_MESSAGE_XSD = "SwiftMessage.xsd";
	private static final String PAIN_001_001_03_ISO_XSD = "pain.001.001.03.ISO.xsd";
	private static final String PACS_008_001_01_ISO_XSD = "pacs.008.001.01.ISO.xsd";
	private static File simple = new File( contents, "swift");
	private static File input = new File( simple, "input");
	private static File output = new File( simple, "output");
	
	@BeforeClass
	public static void before() {
		ensure( output);		
	}
	
	
	@Test
	public void roundtripPacs() {
		File in = new File( input, PACS_008_001_01_ISO_XSD);
		Schema schema = readFile( in);
		System.out.println( schema);
		File out = new File( output, PACS_008_001_01_ISO_XSD);
		writeFile( out, schema);
		compare( in, out);
	}
	
	@Test
	public void roundtripPain() {
		File in = new File( input, PAIN_001_001_03_ISO_XSD);
		Schema schema = readFile( in);
		System.out.println( schema);	
		File out = new File( output, PAIN_001_001_03_ISO_XSD);
		writeFile( out, schema);
		compare( in, out);
	}
	
	@Test
	public void roundtripMsg() {
		File in = new File( input, SWIFT_MESSAGE_XSD);
		Schema schema = readFile( in);
		System.out.println( schema);		
		File out = new File( output, SWIFT_MESSAGE_XSD);
		writeFile( out, schema);
		compare( in, out);
	}


 
	public void failTest()  {
		File in = new File( input, PACS_008_001_01_ISO_XSD);
		File out = new File( input, PAIN_001_001_03_ISO_XSD);
		compare( in, out);
	}
}
