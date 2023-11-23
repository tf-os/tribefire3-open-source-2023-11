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
public class PainLab extends AbstractSwiftLab{

	private static final String COM_BRAINTRIBE_SCHEMEDXML_SWIFT_PAIN_PAIN_FLAT_MODEL_1_0 = "com.braintribe.schemedxml.swift.pain:PainFlatModel#1.0";
	private static final String PAIN_001_001_03_ISO_XSD = "pain.001.001.03.ISO.xsd";
	private static final String PAIN_001_001_02_ISO_XSD = "pain.001.001.02.ISO.xsd";
	private static final String COM_BRAINTRIBE_SCHEMEDXML_SWIFT_PAIN = "com.braintribe.schemedxml.swift.pain";
	private File workingDirectory = new File( swift, "pain");
	
	@Test
	public void test_Pain_1() {		
		runRoundTrip(workingDirectory, COM_BRAINTRIBE_SCHEMEDXML_SWIFT_PAIN, PAIN_001_001_03_ISO_XSD, Arrays.asList("pain.1.xml"), COM_BRAINTRIBE_SCHEMEDXML_SWIFT_PAIN_PAIN_FLAT_MODEL_1_0);
	}
	@Test
	public void test_Pain_2() {		
		runRoundTrip(workingDirectory, COM_BRAINTRIBE_SCHEMEDXML_SWIFT_PAIN, PAIN_001_001_03_ISO_XSD, Arrays.asList("pain.2.xml"), COM_BRAINTRIBE_SCHEMEDXML_SWIFT_PAIN_PAIN_FLAT_MODEL_1_0);
	}
	@Test
	public void test_Pain_3() {		
		runRoundTrip(workingDirectory, COM_BRAINTRIBE_SCHEMEDXML_SWIFT_PAIN, PAIN_001_001_03_ISO_XSD, Arrays.asList("pain.3.xml"), COM_BRAINTRIBE_SCHEMEDXML_SWIFT_PAIN_PAIN_FLAT_MODEL_1_0);
	}
	
	//@Test
	public void test_Pain_parallel() {		
		runRoundTrip(workingDirectory, COM_BRAINTRIBE_SCHEMEDXML_SWIFT_PAIN, PAIN_001_001_03_ISO_XSD, Arrays.asList("pain.1.xml", "pain.2.xml", "pain.3.xml"), COM_BRAINTRIBE_SCHEMEDXML_SWIFT_PAIN_PAIN_FLAT_MODEL_1_0, 1, 0);
	}
	
	@Test
	public void test_Pain_bulk_1000() {		
		runRoundTrip(workingDirectory, COM_BRAINTRIBE_SCHEMEDXML_SWIFT_PAIN, PAIN_001_001_03_ISO_XSD, Arrays.asList("pain.bulk.1000.xml"), COM_BRAINTRIBE_SCHEMEDXML_SWIFT_PAIN_PAIN_FLAT_MODEL_1_0, 110,10);
	}
	//@Test
	public void test_Pain_bulk_1000_p() {		
		runRoundTrip(workingDirectory, COM_BRAINTRIBE_SCHEMEDXML_SWIFT_PAIN, PAIN_001_001_03_ISO_XSD, Arrays.asList("pain.bulk.1000.xml", "pain.bulk.1000.xml"), COM_BRAINTRIBE_SCHEMEDXML_SWIFT_PAIN_PAIN_FLAT_MODEL_1_0);
	}
	
	//@Test
	public void test_Pain_bulk_400000() {		
		runRoundTrip(workingDirectory, COM_BRAINTRIBE_SCHEMEDXML_SWIFT_PAIN, PAIN_001_001_02_ISO_XSD, Arrays.asList("pain.bulk.400000.xml"), COM_BRAINTRIBE_SCHEMEDXML_SWIFT_PAIN_PAIN_FLAT_MODEL_1_0);
	}
}
