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
package com.braintribe.model.processing.ddra.endpoints.rest.v2.ioc;

import java.util.HashMap;
import java.util.Map;

import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.session.impl.managed.StaticAccessModelAccessory;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.testing.tools.gm.GmTestTools;

public class TestSessionFactory implements PersistenceGmSessionFactory {

	private final Map<String, PersistenceGmSession> sessions = new HashMap<>();

	private final Map<String, IncrementalAccess> accesses = new HashMap<>();

	private final Evaluator<ServiceRequest> serviceRequestEvaluator;

	public TestSessionFactory() {
		this(new TestRestEvaluator());
	}
	
	public TestSessionFactory(Evaluator<ServiceRequest> serviceRequestEvaluator) {
		this.serviceRequestEvaluator = serviceRequestEvaluator;
	}

	public void reset(IncrementalAccess access) {
		sessions.clear();
		accesses.put(access.getAccessId(), access);
	}

	public void addAccess(IncrementalAccess access){
		accesses.put(access.getAccessId(), access);
	}

	@Override
	public PersistenceGmSession newSession(String accessId) throws GmSessionException {
		if (sessions.containsKey(accessId)) {
			return sessions.get(accessId);
		}

		IncrementalAccess access = accesses.get(accessId);
		if (access == null) {
			throw new NullPointerException("No access found with id " + accessId);
		}
		BasicPersistenceGmSession session = (BasicPersistenceGmSession) GmTestTools.newSession(access);
		session.setRequestEvaluator(serviceRequestEvaluator);
		
		StaticAccessModelAccessory accessory = new StaticAccessModelAccessory(access.getMetaModel(), accessId);
		session.setModelAccessory(accessory);
		
		sessions.put(accessId, session);
		return session;
	}

}
