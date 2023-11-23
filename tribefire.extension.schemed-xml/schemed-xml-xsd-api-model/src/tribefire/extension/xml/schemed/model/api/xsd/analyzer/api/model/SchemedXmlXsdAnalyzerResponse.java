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
package tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;

/**
 * the return type of the analyzer 
 * @author pit
 *
 */

public interface SchemedXmlXsdAnalyzerResponse extends GenericEntity {
	
	final EntityType<SchemedXmlXsdAnalyzerResponse> T = EntityTypes.T(SchemedXmlXsdAnalyzerResponse.class);
	
	/**	 
	 * @return - the skeleton {@link GmMetaModel} , only types
	 */
	GmMetaModel getSkeletonModel();
	void setSkeletonModel(GmMetaModel model);
	
	/**
	 * @return - the constraints {@link GmMetaModel}, only MD that are well known
	 */
	GmMetaModel getConstraintModel();
	void setConstraintModel( GmMetaModel model);
	
	/**
	 * @return - the mapping {@link GmMetaModel}, only MD used for the marshaller
	 */
	GmMetaModel getMappingModel();
	void setMappingModel( GmMetaModel model);
	
	/**
	 * @return - a {@link GmMetaModel} that binds them all 
	 */
	GmMetaModel getVirtualModel();
	void setVirtualModel( GmMetaModel model);
	
	/**
	 * @param shallowTypes - a list of external types?? 
	 */
	void setShallowTypes(List<GmType> shallowTypes);
	List<GmType> getShallowTypes();
		
}
