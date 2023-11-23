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

import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;

/**
 * a Maven style configuration, supplied via an resource containing a XML declaration (as in settings.xml)
 * 
 * @author pit
 *
 */
public interface MavenRepositoryConfiguration extends RepositoryConfiguration {
	
	final EntityType<MavenRepositoryConfiguration> T = EntityTypes.T(MavenRepositoryConfiguration.class);

	/**
	 * @return - the {@link Resource} that contains the XML declaration 
	 */
	@Mandatory
	Resource getSettingsAsResource();
	/**
	 * @param settings - the {@link Resource} that contains the XML declaration
	 */
	void setSettingsAsResource( Resource settings);
	

}
