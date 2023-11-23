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
package com.braintribe.model.prototyping.impl;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.prototyping.api.QueryPrototyping;
import com.braintribe.model.query.EntityQuery;

public class QueryPrototypingProcessor extends PrototypingProcessor<QueryPrototyping> {
	private PersistenceGmSessionFactory sessionFactory;
	
	@Configurable
	@Required
	public void setSessionFactory(PersistenceGmSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public GenericEntity process(ServiceRequestContext requestContext, QueryPrototyping request) {
		PersistenceGmSession querySession = sessionFactory.newSession(request.getAccessId());
		
		EntityQuery entityQuery = EntityQueryBuilder.from(GenericEntity.T) //
				.tc() //
					.negation() //
					.joker() //
				.where() //
				.property(GenericEntity.globalId) //
				.eq(request.getPrototypeGlobalId()) //
				.done();
		
		GenericEntity prototype = querySession.query().entities(entityQuery).first();
		
		if (prototype == null) {
			throw new IllegalArgumentException("Could not find prototype with globalId: '" + request.getPrototypeGlobalId() + "' in access: '" + request.getAccessId() + "'.");
		}
		
		return prototype;
	}

}
