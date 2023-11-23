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
package tribefire.extension.xml.schemed.test.xsd.analyzer.test.fides.client;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.SchemedXmlXsdAnalyzerRequest;
import tribefire.extension.xml.schemed.requestbuilder.builder.AnalyzerRequestBuilder;
import tribefire.extension.xml.schemed.requestbuilder.xsd.test.util.TestUtil;
import tribefire.extension.xml.schemed.test.xsd.analyzer.test.AbstractXsdAnalyzerLab;
import tribefire.extension.xml.schemed.test.xsd.analyzer.test.fides.HasFidesTokens;

public class ClientModelLab extends AbstractXsdAnalyzerLab implements HasFidesTokens {
	private static final String TEST_MODEL = GRP_CREDIT_SUISSE_FOX + ":fox-client-model#" + VERSION_CREDIT_SUISSE_FOX;
	private static final String TEST_XSD = "FIDES-ICC-DataModel-7.0.xsd";
	private static String TEST_PACKAGE = "com.braintribe.model.fidesv";	
	private static File fides = new File( contents, "fides");
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
	
			//
			// collection overrides as sets
			//
			.collectionOverride()
				.asSet()
				.schemaAddress()
					.type( "Agreement_CF")
					.property("AgreementAccount")
				.close()
			.close()
			
			.collectionOverride()
				.asSet()
				.schemaAddress()
					.type( "CustomiseOutput_CF")
					.property("AgreementAccount")
				.close()			
			.close()
			
			.collectionOverride()
				.asSet()
				.schemaAddress()
					.type( "AgreementFormatChannel_CF")
					.property("AgreementAccount")
				.close()			
			.close()
			
			.collectionOverride()
				.asSet()
				.schemaAddress()
					.type( "Scheduling_CF")
					.property("AgreementAccount")
				.close()			
			.close()
			
			.collectionOverride()
				.asSet()
				.schemaAddress()
					.type( "Statement_CF")
					.property("RelatedReceiverAgreement")
				.close()			
			.close()
		
			.collectionOverride()
				.asSet()
				.schemaAddress()
					.type( "V11Unit_CF")
					.property("RelatedReceiverAgreement")
				.close()			
			.close()
				
			.collectionOverride()
				.asSet()
				.schemaAddress()
					.type( "EDocCIFGroup_CF")
					.property("RelatedReceiverAgreement")
				.close()			
			.close()
			
			.collectionOverride()
				.asSet()
				.schemaAddress()
					.type( "PayOrder_CF")
					.property("EodTransaction")
				.close()			
			.close()
		
			.collectionOverride()
				.asSet()
				.schemaAddress()
					.type( "Payment_CF")
					.property("EodTransaction")
				.close()			
			.close()
	
			.collectionOverride()
				.asSet()
				.schemaAddress()
					.type( "EODFile_CF")
					.property("EODTransaction")
				.close()			
			.close()
		
			.collectionOverride()
				.asSet()
				.schemaAddress()
					.type( "EODFile_CF")
					.property("EODReconFindings")
				.close()			
			.close()
	
		
			//
			// substitutions
			//
			.substitutionModel()
				// check group id of this model..  
				.modelName(GRP_CREDIT_SUISSE_FOX + ":fox-format-model#" + VERSION_CREDIT_SUISSE_FOX)
				
				.substitution()
					.schemaAddress()
						.type("XMLResourceBundle_CF")
						.property("ResourceBundleType")
					.close()
					.replacementSignature("com.braintribe.custom.cs.fox.model.format.ResourceBundle") // not core gm
				.close()
				
				.substitution()
					.schemaAddress()
						.type("OutboundStatus_CF")
						.property("Resource")
					.close()
					.replacementSignature("com.braintribe.custom.cs.fox.model.format.ResourceBundle") // not core gm
				.close()
								
			.close()
			
			.substitutionModel()
				// check group id of this model..  
				.modelName("com.braintribe.gm:resource-model#1.0.10")
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
						.type("Rule_CF")
						.property("RuleScript")
					.close()
					.replacementSignature("com.braintribe.model.rule.RuleScript")
				.close()
			
				.substitution()
					.schemaAddress()
						.type("V11Unit_CF")
						.property("PassThroughData")
					.close()
					.replacementSignature("com.braintribe.model.resource.Resource")
				.close()			
				
				.substitution()
					.schemaAddress()
						.type("V11Set_CF")
						.property("FileContent")
					.close()
					.replacementSignature("com.braintribe.model.resource.Resource")
				.close()	
				
				.substitution()
					.schemaAddress()
						.type("EosDiexFiles_CF")
						.property("FileContent")
					.close()
					.replacementSignature("com.braintribe.model.resource.Resource")
				.close()	
			
				.substitution()
					.schemaAddress()
						.type("EDocument_CF")
						.property("pdfContent")
					.close()
					.replacementSignature("com.braintribe.model.resource.Resource")
				.close()	
		
				.substitution()
					.schemaAddress()
						.type("EDocEnclosure_CF")
						.property("pdfEnclosureContent")
					.close()
					.replacementSignature("com.braintribe.model.resource.Resource")
				.close()	

			.close()
			
		.build();
		
		process( request, output);
	}
	
	//@Test
	public void flat_NoSubstitute() {
		SchemedXmlXsdAnalyzerRequest request = AnalyzerRequestBuilder.request()
				.xsd()
				.file(new File( input, TEST_XSD))
			.close()
			.packageName( TEST_PACKAGE)
			.modelName( TEST_MODEL)
	
			//
			// collection overrides as sets
			//
			.collectionOverride()
				.asSet()
				.schemaAddress()
					.type( "Agreement_CF")
					.property("AgreementAccount")
				.close()
			.close()
			
			.collectionOverride()
				.asSet()
				.schemaAddress()
					.type( "CustomiseOutput_CF")
					.property("AgreementAccount")
				.close()			
			.close()
			
			.collectionOverride()
				.asSet()
				.schemaAddress()
					.type( "AgreementFormatChannel_CF")
					.property("AgreementAccount")
				.close()			
			.close()
			
			.collectionOverride()
				.asSet()
				.schemaAddress()
					.type( "Scheduling_CF")
					.property("AgreementAccount")
				.close()			
			.close()
			
			.collectionOverride()
				.asSet()
				.schemaAddress()
					.type( "Statement_CF")
					.property("RelatedReceiverAgreement")
				.close()			
			.close()
		
			.collectionOverride()
				.asSet()
				.schemaAddress()
					.type( "V11Unit_CF")
					.property("RelatedReceiverAgreement")
				.close()			
			.close()
				
			.collectionOverride()
				.asSet()
				.schemaAddress()
					.type( "EDocCIFGroup_CF")
					.property("RelatedReceiverAgreement")
				.close()			
			.close()
			
			.collectionOverride()
				.asSet()
				.schemaAddress()
					.type( "PayOrder_CF")
					.property("EodTransaction")
				.close()			
			.close()
		
			.collectionOverride()
				.asSet()
				.schemaAddress()
					.type( "Payment_CF")
					.property("EodTransaction")
				.close()			
			.close()
	
			.collectionOverride()
				.asSet()
				.schemaAddress()
					.type( "EODFile_CF")
					.property("EODTransaction")
				.close()			
			.close()
		
			.collectionOverride()
				.asSet()
				.schemaAddress()
					.type( "EODFile_CF")
					.property("EODReconFindings")
				.close()			
			.close()
					
		.build();
		
		process( request, output);
	}
	
	
}
