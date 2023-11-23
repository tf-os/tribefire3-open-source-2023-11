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
package com.braintribe.model.processing.session.api.persistence;

import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.generic.session.exception.GmSessionException;


public class SessionFactoryBasedSessionProvider implements Supplier<PersistenceGmSession>{

	private PersistenceGmSessionFactory persistenceGmSessionFactory;
	private String accessId;

	@Configurable @Required
	public void setAccessId(String accessId) {
		this.accessId = accessId;
	}

	@Configurable @Required
	public void setPersistenceGmSessionFactory(PersistenceGmSessionFactory persistenceGmSessionFactory) {
		this.persistenceGmSessionFactory = persistenceGmSessionFactory;
	}

	@Override
	public PersistenceGmSession get() {
		try {
			return persistenceGmSessionFactory.newSession(accessId);
		} catch (GmSessionException e) {
			throw new RuntimeException(e);
		}
	}

}
