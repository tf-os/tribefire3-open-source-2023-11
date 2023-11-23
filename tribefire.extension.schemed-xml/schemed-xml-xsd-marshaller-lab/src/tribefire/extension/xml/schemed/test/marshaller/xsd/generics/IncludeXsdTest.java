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
import java.io.IOException;
import java.nio.file.Files;

import org.junit.BeforeClass;
import org.junit.Test;

import tribefire.extension.xml.schemed.model.xsd.Schema;
import tribefire.extension.xml.schemed.test.marshaller.xsd.AbstractXsdMarshallerTest;

public class IncludeXsdTest extends AbstractXsdMarshallerTest {
	private static File include = new File( contents, "include");
	private static File input = new File( include, "input");
	private static File output = new File( include, "output");
	
	
	private File inBase = new File( input, "base.xsd");
	private File outBase = new File( output, "base.xsd");

	@BeforeClass
	public static void before() {
		ensure( output);
	}

	private void roundtrip(File in, File out) {
		Schema schema = readFile( in);
		System.out.println( schema);
		
		
		// copy base 
		try {
			Files.copy( inBase.toPath(), outBase.toPath());
		} catch (IOException e) {		
		}
		writeFile( out, schema);
		compare( in, out);
	}

	@Test
	public void roundTrip1() {
		roundtrip( new File( input, "include.1.xsd"), new File( output, "include.1.xsd"));
	}
	@Test
	public void roundTrip2() {
		roundtrip( new File( input, "include.2.xsd"), new File( output, "include.2.xsd"));
	}
	@Test
	public void roundTrip3() {
		roundtrip( new File( input, "include.3.xsd"), new File( output, "include.3.xsd"));
	}
}
