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
package com.braintribe.model.processing.session.impl.managed;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;

public class BasicIdentityCompetence extends AbstractIdentityCompetence {
	protected ManagedGmSession managedSession;
	
	public BasicIdentityCompetence(ManagedGmSession managedSession) {
		super(managedSession);
		this.managedSession = managedSession;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends GenericEntity> T findExistingEntity(EntityReference entityReference) throws IdentityCompetenceException {
		try {
			return (T)managedSession.query().entity(entityReference).find();
		} catch (GmSessionException e) {
			throw new IdentityCompetenceException("error while finding potentially existing entity", e);
		}
	}
	
	@Override
	public boolean wasPropertyManipulated(EntityProperty entityProperty) throws IdentityCompetenceException {
		return false;
	}

	@Override
	public boolean isPreliminarilyDeleted(EntityReference entityReference) throws IdentityCompetenceException {
		return false;
	}
	
}
