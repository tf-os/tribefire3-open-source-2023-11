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
package com.braintribe.product.rat.imp;

import java.util.HashSet;
import java.util.Set;

import com.braintribe.logging.Logger;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.product.rat.imp.impl.utils.QueryHelper;
import com.braintribe.utils.lcd.Arguments;

/**
 * Base class for any class in this artifact that has a session (so almost every one).
 */
public abstract class AbstractHasSession implements HasSession {
	private final static Set<PersistenceGmSession> invalidatedSessions = new HashSet<>();

	// Makes all log output look like it came from ImpApi class to hide implementation details from user in log output
	protected final static Logger logger = Logger.getLogger(ImpApi.class);

	private final PersistenceGmSession session;
	protected final QueryHelper queryHelper;

	public AbstractHasSession(PersistenceGmSession session) {
		Arguments.notNullWithName("session", session);
		this.session = session;
		queryHelper = new QueryHelper(session);
	}

	protected void invalidateSession() {
		invalidatedSessions.add(session);
	}

	@Override
	public PersistenceGmSession session() {
		if (invalidatedSessions.contains(session)) {
			throw new ImpException("This imp's session is no longer valid. Please create a new imp, i.e. using ImpApiFactory");
		}

		return session;
	}

	@Override
	public void commit() {
		session().commit();
	}
}
