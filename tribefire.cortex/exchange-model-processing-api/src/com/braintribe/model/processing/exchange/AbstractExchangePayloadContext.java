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
package com.braintribe.model.processing.exchange;

import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

public abstract class AbstractExchangePayloadContext implements ExchangePayloadContext {
	
	private PersistenceGmSession systemSession;
	private PersistenceGmSession session;

	public AbstractExchangePayloadContext(PersistenceGmSession session, PersistenceGmSession systemSession) {
		this.session = session;
		this.systemSession = systemSession;
	}

	@Override
	public PersistenceGmSession getSession() {
		return session;
	}

	@Override
	public PersistenceGmSession getSystemSession() {
		return systemSession;
	}

}
