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
package com.braintribe.model.artifact.processing.service.data;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * a marker and container for the name of the associated repository configuration.
 * @author pit
 *
 */
@Abstract
public interface HasRepositoryConfigurationName extends GenericEntity {
 
	EntityType<HasRepositoryConfigurationName> T = EntityTypes.T(HasRepositoryConfigurationName.class);
	
	/**
	 * @return - the name of the configuration (by which it is identified in the associated access)
	 */
	String getRepositoryConfigurationName();
	/**
	 * @param id - the name of the configuration (by which it is identified in the associated access)
	 */
	void setRepositoryConfigurationName( String id);
}
