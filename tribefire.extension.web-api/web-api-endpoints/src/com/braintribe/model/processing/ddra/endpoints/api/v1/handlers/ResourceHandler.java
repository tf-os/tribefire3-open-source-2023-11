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
package com.braintribe.model.processing.ddra.endpoints.api.v1.handlers;

import com.braintribe.exception.AuthorizationException;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.meta.data.prompt.Visible;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;
import com.braintribe.model.processing.session.api.managed.NotFoundException;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.resource.Resource;

import java.util.Objects;

public abstract class ResourceHandler {

	protected PersistenceGmSessionFactory sessionFactory;
	protected ModelAccessoryFactory modelAccessoryFactory;

	protected PersistenceGmSession openSession(String accessId) {
		try {
			return sessionFactory.newSession(accessId);
		} catch (GmSessionException e) {
			throw new RuntimeException("Error while creating session for access with id: " + accessId, e);
		}
	}

	protected Resource retrieveResource(PersistenceGmSession gmSession, String resourceId, String accessId) {

		Objects.requireNonNull(gmSession, "gmSession");
		Objects.requireNonNull(resourceId, "resourceId");

		EntityQuery query = EntityQueryBuilder.from(Resource.T).where().property(Resource.id).eq(resourceId).done();

		Resource resource = null;

		try {
			resource = gmSession.query().entities(query).unique();
		} catch (NotFoundException e) {
			throw e;
		} catch (GmSessionException e) {
			throw new RuntimeException(
					"Failed to obtain resource: [ " + resourceId + " ] from access [ " + gmSession.getAccessId() + " ]: " + e.getMessage(), e);
		}

		if (resource == null)
			throw new NotFoundException("Resource [ " + resourceId + " ] not found");

		if (this.modelAccessoryFactory != null) {
			ModelAccessory modelAccessory = this.modelAccessoryFactory.getForAccess(accessId);
			boolean visible = modelAccessory.getMetaData().entity(resource).is(Visible.T);
			if (!visible)
				throw new AuthorizationException("Insufficient privileges to retrieve resource.");
		}

		return resource;

	}

}
