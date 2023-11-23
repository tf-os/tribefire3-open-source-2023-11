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
package tribefire.extension.xml.schemed.test.xsd.analyzer.example;

import java.io.File;

import tribefire.extension.xml.schemed.marshaller.commons.ModelPersistenceExpert;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.SchemedXmlXsdAnalyzerRequest;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.SchemedXmlXsdAnalyzerResponse;
import tribefire.extension.xml.schemed.requestbuilder.builder.AnalyzerRequestBuilder;
import tribefire.extension.xml.schemed.xsd.analyzer.SchemedXmlXsdAnalyzer;


/**
 * a functional example of how to call the {@link SchemedXmlXsdAnalyzer} locally (file system)
 * 
 * @author pit
 *
 */
public class Example {
		
	/**
	 * @param args
	 */
	public static void main( String [] args) {
		
		// parameterize the request
		String inputDirectory = args[0]; // the directory where the input files reside		
		String packageName = args[1]; // the name of the package the generated types should be in
		String modelName = args[2]; // the fully qualified name of the model, i.e. <groupId>:<artifactId>#<version>
		String xsdName = args[3]; // either the file name of the main XSD or the name of the entry in the zip file, if resourceName's set
		String resourceName = null; // the name of the zip file if multiple XSDs are required
			
		// building the request
		SchemedXmlXsdAnalyzerRequest analyzerRequest;
		File input = new File( inputDirectory);
		if (args.length > 4) {
			resourceName = args[4];
			analyzerRequest = AnalyzerRequestBuilder.request()
								.xsd()
									.archive( new File( input, resourceName), xsdName)
								.close()
								.packageName(packageName)
								.modelName(modelName)
								.build();																	 			
		}
		else {
			analyzerRequest = AnalyzerRequestBuilder.request()
					.xsd()
						.file( new File( input, xsdName))
					.close()
					.packageName(packageName)
					.modelName(modelName)
					.build();			
		}
		// running the analyzer
		SchemedXmlXsdAnalyzer analyzer = new SchemedXmlXsdAnalyzer();
		SchemedXmlXsdAnalyzerResponse analyzerResponse = analyzer.process(analyzerRequest);		
		
		// generating some output
		File output = new File( input, "output");
		// the skeleton as artifact, i.e. jar, pom, sources.jar
		ModelPersistenceExpert.dumpModelJar( analyzerResponse.getSkeletonModel(), output);
		// dump the mapping model
		ModelPersistenceExpert.dumpMappingModel( analyzerResponse.getMappingModel(), output);
		 
	}

}
