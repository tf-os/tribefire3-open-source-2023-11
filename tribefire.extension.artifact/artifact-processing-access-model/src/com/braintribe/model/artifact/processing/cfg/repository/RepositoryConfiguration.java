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

import com.braintribe.model.artifact.processing.cfg.NamedConfiguration;
import com.braintribe.model.artifact.processing.cfg.env.Override;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * the basic {@link RepositoryConfiguration} for both types
 * @author pit
 *
 */
@Abstract
public interface RepositoryConfiguration extends NamedConfiguration {
	
	final EntityType<RepositoryConfiguration> T = EntityTypes.T(RepositoryConfiguration.class);
	
	/**
	 * @return - a {@link List} of {@link Override} to pass to the VE
	 */
	List<Override> getEnvironmentOverrides();
	/**
	 * @param overrides - a {@link List} of {@link Override} to pass to the VE
	 */
	void setEnvironmentOverrides( List<Override> overrides);
	
}
