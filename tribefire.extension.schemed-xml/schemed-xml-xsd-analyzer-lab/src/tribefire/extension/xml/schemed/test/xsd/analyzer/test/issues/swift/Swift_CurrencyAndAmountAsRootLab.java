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
package tribefire.extension.xml.schemed.test.xsd.analyzer.test.issues.swift;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.SchemedXmlXsdAnalyzerRequest;
import tribefire.extension.xml.schemed.requestbuilder.xsd.test.util.TestUtil;
import tribefire.extension.xml.schemed.test.xsd.analyzer.test.AbstractXsdAnalyzerLab;

public class Swift_CurrencyAndAmountAsRootLab extends AbstractXsdAnalyzerLab {
	private static final String TEST_MODEL = "com.braintribe.xsd.swift.issue.one:SwiftIssueOneFlatModel#1.0";
	private static final String TEST_XSD = "swiftIssue.1.xsd";
	private static String TEST_PACKAGE = "com.braintribe.xsd.swift.issue.one";	
	private static File simple = new File( contents, "issues/one");
	private static File input = new File( simple, "input");
	private static File output = new File( simple, "output");

	@BeforeClass
	public static void beforeClass() {
		TestUtil.ensure(output);
	}

	@Test
	public void flat_Simple() {
		SchemedXmlXsdAnalyzerRequest request = buildPrimerRequest( input, TEST_PACKAGE, TEST_XSD, java.util.Collections.emptyList(), TEST_MODEL);		
		process( request, output);
	}
	
	
}
