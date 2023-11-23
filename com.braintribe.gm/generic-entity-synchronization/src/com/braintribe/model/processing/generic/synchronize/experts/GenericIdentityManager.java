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
package com.braintribe.model.processing.generic.synchronize.experts;

import java.util.Collection;
import java.util.HashSet;

import com.braintribe.cfg.Configurable;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.generic.synchronize.api.SynchronizationContext;

/**
 * Implementation of {@link ConfigurableIdentityManager} that can be configured with 
 * identityProperties via according setters and add methods.
 *
 */
public class GenericIdentityManager extends ConfigurableIdentityManager {

	private Collection<String> identityProperties = new HashSet<String>();

	
	@Configurable
	public void setIdentityProperties(Collection<String> identityProperties) {
		this.identityProperties = identityProperties;
	}
	
	public void addIdentityProperties(Collection<String> identityProperties) {
		this.identityProperties.addAll(identityProperties);
	}
	
	/**
	 * Returns the collection internally stored with {@link #setIdentityProperties(Collection)} or {@link #addIdentityProperties(Collection)}.
	 */
	public Collection<String> getIdentityProperties(GenericEntity instance, EntityType<? extends GenericEntity> entityType, SynchronizationContext context) {
		return identityProperties;
	}


	
}
