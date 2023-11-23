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
package tribefire.extension.xml.schemed.test.marshaller.xsd.nvd;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

import tribefire.extension.xml.schemed.model.xsd.Schema;
import tribefire.extension.xml.schemed.test.marshaller.xsd.AbstractXsdMarshallerTest;

public class NvdXsdTest extends AbstractXsdMarshallerTest {
	private static File simple = new File( contents, "nvd");
	private static File input = new File( simple, "input");
	private static File output = new File( simple, "output");
	
	private File vuln_in = new File( input, "vulnerability_0.4.xsd");
	private File vuln_out = new File( output, "vulnerability_0.4.xsd");
	
	private File nvd_in = new File( input, "nvd-cve-feed_2.0.xsd");
	private File nvd_out = new File( output, "nvd-cve-feed_2.0.xsd");
	
	
	@BeforeClass
	public static void before() {
		ensure( output);		
	}

	private void roundtrip(File in, File out) {
		Schema schema = readFile( in);
		System.out.println( schema);
		writeFile( out, schema);
		compare( in, out);
	}
	
	
	@Test
	public void roundtripVuln() {
		roundtrip( vuln_in, vuln_out);
	}
	
	@Test
	public void roundtripNvd() {
		roundtrip( nvd_in, nvd_out);
	}


}
