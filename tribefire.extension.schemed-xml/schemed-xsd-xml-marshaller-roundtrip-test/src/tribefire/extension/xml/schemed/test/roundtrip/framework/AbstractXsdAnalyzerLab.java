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
package tribefire.extension.xml.schemed.test.roundtrip.framework;

import java.io.File;
import java.util.List;

import tribefire.extension.xml.schemed.marshaller.commons.ModelPersistenceExpert;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.SchemedXmlXsdAnalyzerRequest;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.SchemedXmlXsdAnalyzerResponse;
import tribefire.extension.xml.schemed.test.commons.commons.SchemedXmlXsdAnalyzerRequestBuilder;
import tribefire.extension.xml.schemed.test.commons.xsd.test.resource.ResourceProvidingSession;
import tribefire.extension.xml.schemed.xsd.analyzer.SchemedXmlXsdAnalyzer;

/**
 * @author pit
 *
 */
public abstract class AbstractXsdAnalyzerLab {
	protected static File contents = new File( "res");
	private ResourceProvidingSession session = new ResourceProvidingSession();
	private SchemedXmlXsdAnalyzer analyzer;
	private SchemedXmlXsdAnalyzerRequestBuilder requestBuilder;
	
	public AbstractXsdAnalyzerLab() {
		analyzer = new SchemedXmlXsdAnalyzer();
	}
	
	public SchemedXmlXsdAnalyzerRequestBuilder getRequestBuilder() {
		if (requestBuilder == null) {
			requestBuilder = new SchemedXmlXsdAnalyzerRequestBuilder();
			requestBuilder.setSession(session);
		}
		return requestBuilder;
	}
	
	/**
	 * @param request
	 * @param output
	 */
	protected ProcessTuple process( SchemedXmlXsdAnalyzerRequest request, File output) {	
		System.out.println("**** Processing [" + request.getSchema().getName() + "] -> [" + output.getAbsolutePath() + "] ****");
		SchemedXmlXsdAnalyzerResponse response = analyzer.process( request);
	
		System.out.println("**** dumping jar for  [" + response.getSkeletonModel().getName() + "] -> [" + output.getAbsolutePath() + "] ****");
		File jar = ModelPersistenceExpert.dumpModelJar(response.getSkeletonModel(), output);
		
		System.out.println("**** dumping xml for  [" + response.getMappingModel().getName() + "] -> [" + output.getAbsolutePath() + "] ****");
		File xml = ModelPersistenceExpert.dumpMappingModel( response.getMappingModel(), output);
		
		return new ProcessTuple( jar, xml);
	}
	
	protected SchemedXmlXsdAnalyzerRequest buildPrimerRequest(File input, String packageName, String xsdName, List<String> referencedXsds, String modelName) {		
		return getRequestBuilder().buildPrimerRequest(input, packageName, xsdName, referencedXsds, modelName);				
	}
	
		
}
