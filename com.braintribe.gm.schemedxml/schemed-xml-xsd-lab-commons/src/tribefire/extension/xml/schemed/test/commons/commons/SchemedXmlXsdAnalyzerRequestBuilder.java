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
package tribefire.extension.xml.schemed.test.commons.commons;

import java.io.File;
import java.util.List;

import com.braintribe.cfg.Configurable;
import com.braintribe.model.resource.Resource;

import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.ReferencedSchema;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.ReferencedSchemata;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.SchemedXmlXsdAnalyzerRequest;
import tribefire.extension.xml.schemed.test.commons.xsd.test.resource.ResourceGenerator;
import tribefire.extension.xml.schemed.test.commons.xsd.test.resource.ResourceProvidingSession;

/**
 * @author pit
 *
 */
public class SchemedXmlXsdAnalyzerRequestBuilder {
	protected static File contents = new File( "res");
	private ResourceProvidingSession session = new ResourceProvidingSession();
	
	@Configurable
	public void setSession(ResourceProvidingSession session) {
		this.session = session;
	}
	
	private ResourceProvidingSession getSession() {
		if (session == null) {
			session = new ResourceProvidingSession();
		}
		return session;
	}
	
	
	public SchemedXmlXsdAnalyzerRequest buildPrimerRequest(File input, String packageName, String xsdName, List<String> referencedXsds, String modelName) {
		SchemedXmlXsdAnalyzerRequest request = SchemedXmlXsdAnalyzerRequest.T.create();
				
		request.setSchema( ResourceGenerator.filesystemResourceFromFile( getSession(), new File( input, xsdName)));
		
		if (referencedXsds != null && !referencedXsds.isEmpty()) {
			ReferencedSchemata referencedSchemata = ReferencedSchemata.T.create();		

			for (String referencedXsd : referencedXsds) {
				ReferencedSchema referencedSchema = ReferencedSchema.T.create();		
				Resource schema = ResourceGenerator.filesystemResourceFromFile( getSession(), new File( input, referencedXsd));
				referencedSchema.setSchema(schema);		
				referencedSchema.setUri( referencedXsd);
				referencedSchemata.getReferencedSchemata().add(referencedSchema);
			}						
			request.setReferencedSchemata( referencedSchemata);
		}
		
		request.setSkeletonModelName(modelName);
		request.setTopPackageName(packageName);
		return request;
	}
	
	public SchemedXmlXsdAnalyzerRequest buildPrimerRequest(File input, String packageName, String containerResourceName, String schemaName, String modelName) {
		SchemedXmlXsdAnalyzerRequest request = SchemedXmlXsdAnalyzerRequest.T.create();
				
		request.setContainerResource( ResourceGenerator.filesystemResourceFromFile( getSession(), new File( input, containerResourceName)));
		request.setContainerTerminalSchemaUri(schemaName);
		request.setSkeletonModelName(modelName);
		request.setTopPackageName(packageName);
		return request;
	}
	

}
	
