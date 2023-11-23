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
package tribefire.extension.xml.schemed.test.xsd.analyzer.test.generics;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.MappingOverride;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.SchemaAddress;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.SchemedXmlXsdAnalyzerRequest;
import tribefire.extension.xml.schemed.requestbuilder.xsd.test.util.TestUtil;
import tribefire.extension.xml.schemed.test.xsd.analyzer.test.AbstractXsdAnalyzerLab;

public class OverrideNameAnalyzerLab extends AbstractXsdAnalyzerLab {
	private static File simple = new File( contents, "overrideName");
	private static File input = new File( simple, "input");
	private static File output = new File( simple, "output");
	

	@BeforeClass
	public static void beforeClass() {
		TestUtil.ensure(output);
	}

	@Test
	public void overrideNamesTest() {
		List<String> refs = new ArrayList<>();
		refs.add( "import.xsd");
		SchemedXmlXsdAnalyzerRequest request = buildPrimerRequest( input, "com.braintribe.override.name", "overrideName.xsd", refs, "com.braintribe.xsd:OverrideNameFlatModel#1.0");
		
		MappingOverride override1 = MappingOverride.T.create();
		override1.setNameOverride("overridenA");
		SchemaAddress schemaAddress1 = SchemaAddress.T.create();
		schemaAddress1.setElement("a");
		schemaAddress1.setParent("Root");
		override1.setSchemaAddress(schemaAddress1);
		
		request.getMappingOverrides().add( override1);
		
		MappingOverride override2 = MappingOverride.T.create();
		override2.setNameOverride("overridenB");
		SchemaAddress schemaAddress2 = SchemaAddress.T.create();
		schemaAddress2.setElement("b");
		schemaAddress2.setParent("Child");
		override2.setSchemaAddress(schemaAddress2);
		
		request.getMappingOverrides().add( override2);

		MappingOverride override3 = MappingOverride.T.create();
		override3.setNameOverride("Kind");
		SchemaAddress schemaAddress3 = SchemaAddress.T.create();
		schemaAddress3.setParent("Child");
		override3.setSchemaAddress(schemaAddress3);
		
		request.getMappingOverrides().add( override3);
		process( request, output);
	}
}
