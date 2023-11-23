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
package tribefire.extension.xml.schemed.marshaller.xsd.resolver;

import com.braintribe.model.resource.Resource;

import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.ReferencedSchema;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.ReferencedSchemata;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.SchemedXmlXsdAnalyzerRequest;

/**
 * the {@link SchemaReferenceResolver} needs to be configured if it needs to 
 * resolve URI without location.
 * @author pit
 *
 */
public interface ConfigurableSchemaReferenceResolver {

	/**
	 * direct from the request, a set of {@link ReferencedSchema}
	 * @param schemata - the {@link ReferencedSchemata} as from the {@link SchemedXmlXsdAnalyzerRequest}
	 */
	void setReferencedSchemata( ReferencedSchemata schemata);	
	/**
	 * a zip file that contains the XSDs to process
	 * @param resource
	 */
	void setContainerResource( Resource resource);	
}
