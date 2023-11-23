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
package com.braintribe.model.processing.query.smart.test.builder.repo;

import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;

/**
 * 
 * @author peter.gazdik
 */
public class IncrementalAccessRepoDriver implements RepositoryDriver {

	private final IncrementalAccess access;
	private final BasicPersistenceGmSession session;

	public IncrementalAccessRepoDriver(IncrementalAccess access) {
		this.access = access;
		this.session = new BasicPersistenceGmSession(access);
	}

	@Override
	public <T extends GenericEntity> T newInstance(Class<T> clazz) {
		EntityType<T> entityType = GMF.getTypeReflection().getEntityType(clazz);
		return session.create(entityType);
	}

	@Override
	public void commit() {
		try {
			session.commit();

		} catch (GmSessionException e) {
			throw new RuntimeException("Test setup failed", e);
		}
	}

	@Override
	public void commitNoId() {
		try {
			session.commit();

		} catch (GmSessionException e) {
			throw new RuntimeException("Test setup failed", e);
		}
	}

	@Override
	public RepositoryDriver newRepoDriver() {
		return new IncrementalAccessRepoDriver(access);
	}
}
