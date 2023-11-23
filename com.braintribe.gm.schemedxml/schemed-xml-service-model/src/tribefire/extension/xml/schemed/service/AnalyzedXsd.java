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
package tribefire.extension.xml.schemed.service;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;

import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.SchemedXmlXsdAnalyzerResponse;

/**
 * the response for Jinni
 * 
 * @author pit
 *
 */
public interface AnalyzedXsd extends SchemedXmlXsdAnalyzerResponse {
	
	
	EntityType<AnalyzedXsd> T = EntityTypes.T(AnalyzedXsd.class);


	/**
	 * @return - the resource that can deliver the skeleton model (staxed as xml)
	 */
	Resource getSkeletonResource();
	void setSkeletonResource( Resource skeleton);
	
	/**
	 * @return - the resource that can deliver the constraint model (staxed as xml)
	 */
	Resource getConstraintResource();
	void setConstraintResource( Resource constraints);
	
	/**
	 * @return - the resource that can deliver the mapping model (staxed as xml)
	 */
	Resource getMappingResource();
	void setMappingResource( Resource constraints);
	
	/**
	 * @return - the resource that can deliver the artifact (zipped into a ZIP)
	 */
	Resource getArtifact();
	void setArtifact( Resource artifact);
	
	/**
	 * @return - the resource that can deliver the exchange package (staxed as xml)
	 */
	Resource getExchangePackage();
	void setExchangePackage( Resource exchangePackage);
	
	
	
}
