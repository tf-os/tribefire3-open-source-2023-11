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
package tribefire.extension.xml.schemed.test.roundtrip.swift;

import java.io.File;
import java.util.Arrays;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.testing.category.KnownIssue;
@Category(KnownIssue.class)
public class PacsLab extends AbstractSwiftLab{

	private static final String COM_BRAINTRIBE_SCHEMEDXML_SWIFT_PACS_PACS_FLAT_MODEL_1_0 = "com.braintribe.schemedxml.swift.pacs:PacsFlatModel#1.0";
	private static final String PACS_008_001_01_ISO_XSD = "pacs.008.001.01.ISO.xsd";
	private static final String COM_BRAINTRIBE_SCHEMEDXML_SWIFT_PACS = "com.braintribe.schemedxml.swift.pacs";
	private File workingDirectory = new File( swift, "pacs");
	
	@Test
	public void test_Pacs_a() {		
		runRoundTrip(workingDirectory, COM_BRAINTRIBE_SCHEMEDXML_SWIFT_PACS, PACS_008_001_01_ISO_XSD, Arrays.asList("pacs.008.01.01.a.xml", "pacs.008.01.01.a.xml"), COM_BRAINTRIBE_SCHEMEDXML_SWIFT_PACS_PACS_FLAT_MODEL_1_0);
	}
	@Test
	public void test_Pacs_b() {		
		runRoundTrip(workingDirectory, COM_BRAINTRIBE_SCHEMEDXML_SWIFT_PACS, PACS_008_001_01_ISO_XSD, Arrays.asList("pacs.008.01.01.b.xml", "pacs.008.01.01.b.xml"), COM_BRAINTRIBE_SCHEMEDXML_SWIFT_PACS_PACS_FLAT_MODEL_1_0);
	}
}
