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
package tribefire.extension.xml.schemed.test.requestbuilder;

import java.io.File;

import org.junit.Test;

import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.SchemedXmlXsdAnalyzerRequest;
import tribefire.extension.xml.schemed.requestbuilder.builder.AnalyzerRequestBuilder;
import tribefire.extension.xml.schemed.test.requestbuilder.validator.Validator;

/**
 * not only (not yet) a lab, but a place to show examples of the builder for {@link SchemedXmlXsdAnalyzerRequest}
 * 
 * @author pit
 *
 */
public class AnalyzerRequestBuilderLab {

	
	/**
	 * the simplest form of a request: one XSD file into one model 
	 */
	@Test
	public void singleXSD() {
		SchemedXmlXsdAnalyzerRequest request = AnalyzerRequestBuilder.request()
				.xsd()
					.file( new File( "main.xsd"))
				.close()
				.modelName("my-group:my-artifact#1.0")
				.packageName("com.braintribe.xsd")
			.build();
		
		Validator.validate(request);
	}
	
	/**
	 * multiple XSD into one model, XSD are packaged into ZIP
	 */
	@Test
	public void multipleXSDinZip() {
		SchemedXmlXsdAnalyzerRequest request = AnalyzerRequestBuilder.request()
				.xsd()
					.archive( new File( "archive.zip"), "Schemas/gs1/ecom/main.xsd")
				.close()
				.modelName("my-group:my-artifact#1.0")
				.packageName("com.braintribe.xsd")
			.build();
		Validator.validate(request);
	}
	
	/**
	 * multiple XSD into one model, XSD are single files
	 */
	@Test
	public void multipleXSD() {	
		SchemedXmlXsdAnalyzerRequest request = AnalyzerRequestBuilder.request()
				.xsd()
				.file( new File( "main.xsd"))
				.close()
				.modelName("my-group:my-artifact#1.0")
				.packageName("com.braintribe.xsd")
				.references()
				.file( new File( "import1.xsd"), "import1.xsd")
				.file( new File( "import2.xsd"), "import2.xsd")
				.close()
				
				.build();
		
		Validator.validate( request);		
	}
	
	/**
	 * overriding the names of types and properties
	 */
	@Test
	public void nameOverrides() {
		SchemedXmlXsdAnalyzerRequest request = AnalyzerRequestBuilder.request()
				.xsd()
					.file( new File( "main.xsd"))
				.close()
				.modelName("my-group:my-artifact#1.0")
				.packageName("com.braintribe.xsd")
				
				.nameOverride()
					.overridingName("myFirstName")
			
					.schemaAddress()
						.type("first-xsd-type")
						.property("first-xsd-property")
					.close()
			
				.close()
				
				.nameOverride()
					.overridingName("mySecondName")
			
					.schemaAddress()
						.type("second-xsd-type")
						.property("second-xsd-property")
					.close()		
				.close()				
			.build();
		Validator.validate(request);
	}
	
	/**
	 * overriding the types of collections (from List to Set)
	 */
	@Test
	public void collectionOverrides() {
		SchemedXmlXsdAnalyzerRequest request = AnalyzerRequestBuilder.request()
				.xsd()
					.file( new File( "main.xsd"))
				.close()
				.modelName("my-group:my-artifact#1.0")
				.packageName("com.braintribe.xsd")
				
				.collectionOverride()
					.asSet()			
					.schemaAddress()
						.type("xsd-type")
						.property("collection-property")
					.close()
			
				.close()
											
			.build();
		Validator.validate(request);
	}

	
	/**
	 * wire bidirectional properties 
	 */
	@Test
	public void bidirectionals() {
		SchemedXmlXsdAnalyzerRequest request = AnalyzerRequestBuilder.request()
				.xsd()
					.file( new File( "main.xsd"))
				.close()
				.modelName("my-group:my-artifact#1.0")
				.packageName("com.braintribe.xsd")
				.bidirectional()
					.schemaAddress()
						.type( "first-xsd-type")
						.property( "first-xsd-collection")
					.close()
					.property( "first-backlink-property")
				.close()
				.bidirectional()
				.schemaAddress()
					.type( "second-xsd-type")
					.property( "second-xsd-collection")
				.close()
				.property( "second-backlink-property")
			.close()
		.build();
		
		Validator.validate( request);
	}
	
	
	/*
	 * substitution 
	 */
	@Test
	public void substitution() {
		SchemedXmlXsdAnalyzerRequest request = AnalyzerRequestBuilder.request()
				.xsd()
					.file( new File( "main.xsd"))
				.close()
				.modelName("my-group:my-artifact#1.0")
				.packageName("com.braintribe.xsd")
				.substitutionModel()
					.modelName("my-substitution:model")
					.substitution()
						.replacementSignature("my.injected.package.one.SubstitutionOne")					
						.schemaAddress()
							.type( "first-xsd-type")							
						.close()
					.close()
					.substitution()
						.replacementSignature("my.injected.package.one.SubstitutionTwo")						
						.schemaAddress()
							.type( "second-xsd-type")
							.property( "second-xsd-collection")
						.close()
					.close()
				.close()				
			.build();
		
		Validator.validate( request);
	}

}
