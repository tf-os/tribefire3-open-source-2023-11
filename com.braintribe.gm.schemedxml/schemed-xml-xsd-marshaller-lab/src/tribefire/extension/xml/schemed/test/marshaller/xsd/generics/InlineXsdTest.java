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

import org.junit.Test;

import tribefire.extension.xml.schemed.model.xsd.Schema;
import tribefire.extension.xml.schemed.test.marshaller.xsd.AbstractXsdMarshallerTest;

public class InlineXsdTest extends AbstractXsdMarshallerTest {
	private File simple = new File( contents, "inline");
	private File input = new File( simple, "input");
	private File output = new File( simple, "output");
	
	private File in = new File( input, "inlineTypes.xsd");
	private File out = new File( output, "inlineTypes.xsd");
	
	@Test
	public void roundtrip() {
		Schema schema = readFile( in);
		System.out.println( schema);
		ensure( output);
		writeFile( out, schema);
		compare( in, out);
	}

}
