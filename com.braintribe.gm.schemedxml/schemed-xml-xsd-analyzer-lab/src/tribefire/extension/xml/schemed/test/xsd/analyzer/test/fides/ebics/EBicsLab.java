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
package tribefire.extension.xml.schemed.test.xsd.analyzer.test.fides.ebics;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.SchemedXmlXsdAnalyzerRequest;
import tribefire.extension.xml.schemed.requestbuilder.builder.AnalyzerRequestBuilder;
import tribefire.extension.xml.schemed.requestbuilder.xsd.test.util.TestUtil;
import tribefire.extension.xml.schemed.test.xsd.analyzer.test.AbstractXsdAnalyzerLab;
import tribefire.extension.xml.schemed.test.xsd.analyzer.test.fides.HasFidesTokens;

public class EBicsLab extends AbstractXsdAnalyzerLab implements HasFidesTokens {
	
	private static final String TEST_MODEL = GRP_CREDIT_SUISSE_FOX + ":fox-ebics-light-envelope-model#" + VERSION_CREDIT_SUISSE_FOX;
	private static final String TEST_XSD = "EbicsLightEnvelopeModel-3.0.xsd";
	private static String TEST_PACKAGE = "com.braintribe.custom.cs.fox.model.ebics.envelope";	
	private static File fides = new File( contents, "ebics");
	private static File input = new File( fides, "input");
	private static File output = new File( fides, "output");
	

	@BeforeClass
	public static void beforeClass() {
		TestUtil.ensure(output);
	}

	@Test
	public void flat_Full() {
		SchemedXmlXsdAnalyzerRequest request = AnalyzerRequestBuilder.request()
				.xsd()
				.file(new File( input, TEST_XSD))
			.close()
			.packageName( TEST_PACKAGE)
			.modelName( TEST_MODEL)
			
			.substitutionModel()
			  
				.modelName(GRP_CREDIT_SUISSE_FOX + ":fox-format-model#" + VERSION_CREDIT_SUISSE_FOX)				
				
				.substitution()
					.schemaAddress()
						.type("Agreement_CF")
						.property("EodStatementExportSchedules")
					.close()
					.replacementSignature("com.braintribe.model.time.schedule.Schedule") 
				.close()
				
				.substitution()
					.schemaAddress()
						.type("Agreement_CF")
						.property("IntradayStatementExportSchedules")
					.close()
					.replacementSignature("com.braintribe.model.time.schedule.Schedule") 
				.close()
			
				.substitution()
					.schemaAddress()
						.type("FormatVersion_CF")
						.property("Schemas")
					.close()
					.replacementSignature("com.braintribe.custom.cs.fox.model.format.ResourceBundle") 
				.close()
				
				.substitution()
					.schemaAddress()
						.type("FormatVersion_CF")
						.property("Schematrons")
					.close()
					.replacementSignature("com.braintribe.custom.cs.fox.model.format.ResourceBundle") 
				.close()
				
				.substitution()
					.schemaAddress()
						.type("FormatVersion_CF")
						.property("XsltImport")
					.close()
					.replacementSignature("com.braintribe.custom.cs.fox.model.format.ResourceBundle") 
				.close()
				
				.substitution()
					.schemaAddress()
						.type("FormatVersion_CF")
						.property("XsltExport")
					.close()
					.replacementSignature("com.braintribe.custom.cs.fox.model.format.ResourceBundle") 
				.close()
				
				.substitution()
					.schemaAddress()
						.type("OutboundStatus_CF")
						.property("Resource")
					.close()
					.replacementSignature("com.braintribe.custom.cs.fox.model.format.ResourceBundle") 
				.close()

				.substitution()
					.schemaAddress()
						.type("Rule_CF")
						.property("RuleScript")
					.close()
					.replacementSignature("com.braintribe.model.rule.RuleScript") 
				.close()

			
			.close()
					
		.build();
		
		process( request, output);
	}
	
	
}
