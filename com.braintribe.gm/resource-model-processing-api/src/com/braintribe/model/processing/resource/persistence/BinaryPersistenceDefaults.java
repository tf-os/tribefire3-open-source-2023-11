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
package com.braintribe.model.processing.resource.persistence;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.resource.ResourceProcessingDefaults;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.ResourceSource;

/**
 * <p>
 * A collection of default utility methods at disposal of {@link BinaryPersistence} implementations for use and
 * overriding.
 * 
 */
public interface BinaryPersistenceDefaults extends ResourceProcessingDefaults {

	default Resource createResource(Resource source) {
		
		return createResource(null, source);
		
	}
	
	default Resource createResource(PersistenceGmSession session, Resource source) {

		final Resource resource;
		
		resource = createEntity(session, Resource.T);

		transferProperties(source, resource, e -> createEntity(session, e));

		return resource;

	}

	default Resource createResource(PersistenceGmSession session, Resource source, ResourceSource resourceSource) {

		Resource resource = createResource(session, source);
		
		resource.setResourceSource(resourceSource);
		
		return resource;

	}
	
	default <T extends GenericEntity> T createEntity(PersistenceGmSession session, EntityType<T> entityType) {
		if (session == null) {
			return entityType.create();
		} else {
			return session.create(entityType);
		}
	}

}
