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
package tribefire.extension.xml.schemed.xml.marshaller.test.issues;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.utils.IOTools;

import tribefire.extension.xml.schemed.model.api.xml.marshaller.api.model.SchemedXmlMarshallerMarshallRequest;
import tribefire.extension.xml.schemed.model.api.xml.marshaller.api.model.SchemedXmlMarshallerMarshallResponse;
import tribefire.extension.xml.schemed.model.api.xml.marshaller.api.model.SchemedXmlMarshallerUnmarshallRequest;
import tribefire.extension.xml.schemed.model.api.xml.marshaller.api.model.SchemedXmlMarshallerUnmarshallResponse;
import tribefire.extension.xml.schemed.test.commons.xsd.test.util.TestUtil;
import tribefire.extension.xml.schemed.xml.marshaller.test.AbstractXmlMarshallerLab;

public class RedirectDateConversionIssueLab extends AbstractXmlMarshallerLab {
	private static final String TEST_XML = "datesRedirect.xml";
	private static final String TEST_XSD = "datesRedirect.xsd";
	private static final String TEST_MAPPING_MODEL_XML = "com.braintribe.xsd.generics.issue.redirected.dates.DatesIssueRedirectFlatModel-mapping.model.xml";
	private File contents = new File(res, "issues");
	private File simple = new File( contents, "redirectDateIssue");
	private File input = new File( simple, "input");
	private File output = new File( simple, "output");
	
	@Before
	public void before() {
		TestUtil.ensure(output);
	}
	
	@Test
	public void test() {
		try {
			SchemedXmlMarshallerUnmarshallRequest umRequest = buildRequest(input, TEST_XML, TEST_MAPPING_MODEL_XML);
			SchemedXmlMarshallerUnmarshallResponse umResponse = process( umRequest);
			GenericEntity result = umResponse.getAssembly();
			System.out.println(result);
			
			SchemedXmlMarshallerMarshallRequest mRequest = buildRequest(input, result, TEST_MAPPING_MODEL_XML);
			SchemedXmlMarshallerMarshallResponse mResponse = process( mRequest);

			File xmlOutputFile = new File( output, TEST_XML);
			IOTools.spit( xmlOutputFile, mResponse.getExpression(), "UTF-8", false);
			
			File xsdInputFile = new File( input, TEST_XSD);
			
			boolean validationResult = validate(xsdInputFile, xmlOutputFile);
			Assert.assertTrue( "output doesn't validate", validationResult);;
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail( "Exception [" + e.getMessage() + "] thrown");
		}
	}

}
