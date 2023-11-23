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
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.session.api.managed.EntityAccessBuilder;
import com.braintribe.model.processing.session.api.managed.SessionQueryBuilder;

@SuppressWarnings("unusable-by-js")
public abstract class AbstractSessionQueryBuilder implements SessionQueryBuilder {
	@Override
	public <T extends GenericEntity> EntityAccessBuilder<T> entity(String typeSignature, Object id) {
		return entity(typeSignature, id, EntityReference.ANY_PARTITION);
	}
	
	@Override
	public <T extends GenericEntity> EntityAccessBuilder<T> entity(String typeSignature, Object id, String partition) {
		PersistentEntityReference entityReference = PersistentEntityReference.T.create();
		entityReference.setRefId(id);
		entityReference.setTypeSignature(typeSignature);
		entityReference.setRefPartition(partition);
		return entity(entityReference);
	}

	@Override
	public <T extends GenericEntity> EntityAccessBuilder<T> entity(EntityType<T> entityType, Object id) {
		return entity(entityType, id, EntityReference.ANY_PARTITION);
	}

	@Override
	public <T extends GenericEntity> EntityAccessBuilder<T> entity(EntityType<T> entityType, Object id, String partition) {
		PersistentEntityReference entityReference = PersistentEntityReference.T.create();
		entityReference.setTypeSignature(entityType.getTypeSignature());
		entityReference.setRefId(id);
		entityReference.setRefPartition(partition);
		return entity(entityReference);
	}

}
