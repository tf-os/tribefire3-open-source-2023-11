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
package tribefire.extension.xml.schemed.test.marshaller.xsd.generics;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

import tribefire.extension.xml.schemed.model.xsd.Schema;
import tribefire.extension.xml.schemed.test.marshaller.xsd.AbstractXsdMarshallerTest;

public class SimpleContentXsdTest extends AbstractXsdMarshallerTest {
	private static File simple = new File( contents, "simpleContent");
	private static File input = new File( simple, "input");
	private static File output = new File( simple, "output");
	
	
	@BeforeClass
	public static void before() {
		ensure( output);
	}
	@Test
	public void roundtrip() {
		Schema schema = readFile( new File( input, "simpleContent.xsd"));
		System.out.println( schema);		
		writeFile( new File( output, "simpleContent.xsd"), schema);
		
	}
	
	@Test
	public void roundtripDerived() {
		Schema schema = readFile( new File( input, "simpleContentDerived.xsd"));
		System.out.println( schema);
		writeFile( new File( output, "simpleContentDerived.xsd"), schema);
		
	}
	
	@Test
	public void roundtripRedirect() {
		File in = new File( input, "simpleContentRedirect.xsd");
		Schema schema = readFile( in);
		System.out.println( schema);
		
		File out = new File( output, "simpleContentRedirect.xsd");
		writeFile( out, schema);
		compare(in, out);
		
	}

}
