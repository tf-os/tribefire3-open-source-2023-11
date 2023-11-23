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
package com.braintribe.cartridge.common.processing.session;

import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;


public class AccessIdBasedPersistenceGmSessionFactory implements Supplier<PersistenceGmSession> {

	private String accessId;
	private PersistenceGmSessionFactory sessionFactory;
	
	
	@Required @Configurable
	public void setAccessId(String accessId) {
		this.accessId = accessId;
	}
	@Required @Configurable
	public void setSessionFactory(PersistenceGmSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public PersistenceGmSession get() throws RuntimeException {
		try {
			return sessionFactory.newSession(accessId);	
		} catch (Exception e) {
			throw new RuntimeException("Could not create new session for access: "+accessId,e);
		}
	}
	
}
