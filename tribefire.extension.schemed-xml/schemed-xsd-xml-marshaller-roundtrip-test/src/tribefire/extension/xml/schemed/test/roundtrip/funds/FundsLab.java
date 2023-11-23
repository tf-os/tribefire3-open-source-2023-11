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
package tribefire.extension.xml.schemed.test.roundtrip.funds;

import java.util.Arrays;
import org.junit.experimental.categories.Category;
import com.braintribe.testing.category.KnownIssue;

@Category(KnownIssue.class)
public class FundsLab extends AbstractFundsLab{

	private static final String MODEL_NAME = "com.braintribe.schemedxml.swift.pacs:PacsFlatModel#1.0";
	private static final String MAIN_XSD = "FundsXML_4.1.3.xsd";
	private static final String PACKAGE_NAME = "com.braintribe.schemedxml.funds";
	private static final String MAIN_XML = "FundsXML4_Sample_Document.xml";
	
//
	public void test_sample() {		
		
		runRoundTrip(funds, PACKAGE_NAME, MAIN_XSD, Arrays.asList(MAIN_XML), MODEL_NAME);
	}
	
}
