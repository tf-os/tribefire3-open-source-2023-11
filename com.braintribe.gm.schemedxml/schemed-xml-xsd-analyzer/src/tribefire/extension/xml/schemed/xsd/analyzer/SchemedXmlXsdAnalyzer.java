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
package tribefire.extension.xml.schemed.xsd.analyzer;

import com.braintribe.model.resource.Resource;

import tribefire.extension.xml.schemed.marshaller.xsd.resolver.BasicSchemaReferenceResolverMk2;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.SchemedXmlXsdAnalyzerRequest;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.SchemedXmlXsdAnalyzerResponse;
import tribefire.extension.xml.schemed.model.xsd.Schema;
import tribefire.extension.xml.schemed.xsd.analyzer.modelbuilder.ModelBuilder;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.analyzer.BasicAnalyzerRegistry;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.analyzer.BasicNamespaceGenerator;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.schema.BasicSchemaRegistry;
import tribefire.extension.xml.schemed.xsd.api.analyzer.naming.NamespaceGenerator;

public class SchemedXmlXsdAnalyzer  {

	private BasicSchemaReferenceResolverMk2 schemaReferenceResolver = new BasicSchemaReferenceResolverMk2();
	private NamespaceGenerator namespaceGenerator = new BasicNamespaceGenerator();
	private BasicAnalyzerRegistry analyzerRegistry = new BasicAnalyzerRegistry();
	private boolean verbose;
	
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	
	
	public SchemedXmlXsdAnalyzerResponse process( SchemedXmlXsdAnalyzerRequest request) {
		Schema schema = null;
		Resource containerResource = request.getContainerResource();
		if (containerResource == null) {			
			schemaReferenceResolver.setReferencedSchemata( request.getReferencedSchemata());
			String uri = request.getSkeletonModelName(); // we have no URI here, so we use the skeleton as URI 
			schema = schemaReferenceResolver.resolve( uri, request.getSchema());
		}
		else {
			schemaReferenceResolver.setContainerResource( containerResource);
			schema = schemaReferenceResolver.resolve( null, request.getContainerTerminalSchemaUri());
			
		}
		BasicSchemaRegistry schemaRegistry = BasicSchemaRegistry.createFromSchema(schema, schemaReferenceResolver, namespaceGenerator, analyzerRegistry, analyzerRegistry);
		schemaRegistry.setVerbose(verbose);
		
		analyzerRegistry.setVerbose( verbose);
		analyzerRegistry.parametrize(request);
		analyzerRegistry.analyze(schemaRegistry);
		
		
		SchemedXmlXsdAnalyzerResponse response = ModelBuilder.buildPrimerResponse(request.getSkeletonModelName(), analyzerRegistry);		
		return response;			
	}
		
	
}
