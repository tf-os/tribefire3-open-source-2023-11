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
package com.braintribe.model.artifact.processing.cfg.repository;

import java.util.List;

import com.braintribe.model.artifact.processing.cfg.repository.details.Repository;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * a simplified (aka reduced to the minimum yet still fully capable, modelled) abstraction of a Maven style XML declaration based config
 * 
 * @author pit
 *
 */
public interface SimplifiedRepositoryConfiguration extends RepositoryConfiguration {
		
	final EntityType<SimplifiedRepositoryConfiguration> T = EntityTypes.T(SimplifiedRepositoryConfiguration.class);

	/**
	 * @return - a {@link List} of the {@link Repository} to be processed by the services 
	 */
	@Mandatory
	List<Repository> getRepositories();
	/**
	 * @param repositories - a {@link List} of the {@link Repository} to be processed by the services
	 */
	void setRepositories( List<Repository> repositories);
	
	/**
	 * @return - the expression that points to the local repository within the server's storage
	 */
	@Mandatory
	String getLocalRepositoryExpression();	
	/**
	 * @param expression - the expression that points to the local repository within the server's storage
	 */
	void setLocalRepositoryExpression( String expression);

}
