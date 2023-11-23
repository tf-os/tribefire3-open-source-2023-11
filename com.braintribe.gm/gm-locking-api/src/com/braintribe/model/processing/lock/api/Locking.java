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
package com.braintribe.model.processing.lock.api;

import java.util.concurrent.locks.ReadWriteLock;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.processing.lock.impl.SemaphoreBasedLocking;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

/**
 * Represents a registry where @link{ReadWriteLock}s can be retrieved from, based on an identifier or an entity.
 * <p>
 * For sample implementation (which can be used in tests for example) see {@link SemaphoreBasedLocking}.
 */
public interface Locking {

	ReadWriteLock forIdentifier(String id);

	/**
	 * Equivalent to {@code forIdentifier(namespace + ":" + id)}
	 */
	default ReadWriteLock forIdentifier(String namespace, String id) {
		return forIdentifier(namespace + ":" + id);
	}

	/**
	 * Creates a String s representing given entity and calls {@code forIdentifier("entity", s)}
	 */
	default ReadWriteLock forEntity(GenericEntity entity) {
		StringBuilder builder = new StringBuilder();
		EntityType<?> entityType = entity.entityType();
		builder.append(entityType.getTypeSignature());

		Object id = entity.getId();
		if (id != null) {
			builder.append('[');
			builder.append(id.toString());
			builder.append(']');

			GmSession session = entity.session();
			if (session instanceof PersistenceGmSession) {
				PersistenceGmSession persistenceGmSession = (PersistenceGmSession) session;
				String accessId = persistenceGmSession.getAccessId();
				if (accessId != null) {
					builder.append('@');
					builder.append(accessId);
				}
			}
		}

		String identifier = builder.toString();
		return forIdentifier("entity", identifier);
	}

	/**
	 * Equivalent to {@code forIdentifier("entity-type", entityType.getTypeSignature())}
	 */
	default ReadWriteLock forType(EntityType<?> entityType) {
		return forIdentifier("entity-type", entityType.getTypeSignature());
	}

}
