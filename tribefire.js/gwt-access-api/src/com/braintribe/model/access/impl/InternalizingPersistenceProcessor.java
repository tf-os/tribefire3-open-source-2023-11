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
package com.braintribe.model.access.impl;

import java.util.Set;

import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.accessapi.CustomPersistenceRequest;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.accessapi.PersistenceRequest;
import com.braintribe.model.accessapi.QueryAndSelect;
import com.braintribe.model.accessapi.QueryEntities;
import com.braintribe.model.accessapi.QueryProperty;
import com.braintribe.model.accessapi.ReferencesRequest;
import com.braintribe.model.accessapi.ReferencesResponse;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.api.UnsupportedRequestTypeException;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;

public class InternalizingPersistenceProcessor implements ServiceProcessor<PersistenceRequest, Object>, IncrementalAccess {

	protected IncrementalAccess delegate;
	
	public InternalizingPersistenceProcessor(IncrementalAccess delegate) {
		this.delegate = delegate;
	}
	
	protected InternalizingPersistenceProcessor() {
	}
	
	@Override
	public Object process(ServiceRequestContext requestContext, PersistenceRequest request) throws ModelAccessException {
		switch (request.persistenceRequestType()) {
		case ManipulationRequest: return delegate.applyManipulation((ManipulationRequest) request);
		case QueryAndSelect: return delegate.query(((QueryAndSelect) request).getQuery());
		case QueryEntities: return delegate.queryEntities(((QueryEntities) request).getQuery());
		case QueryProperty: return delegate.queryProperty(((QueryProperty) request).getQuery());
		case ReferencesRequest: return delegate.getReferences((ReferencesRequest) request);
		case GetPartitions: return delegate.getPartitions();
		case GetModel: return delegate.getMetaModel();
		case Custom: return delegate.processCustomRequest(requestContext, (CustomPersistenceRequest) request);
		default:
			throw new UnsupportedRequestTypeException("unsupported type " + request.entityType().getTypeSignature() + " with enum code " + request.persistenceRequestType());
		}
	}

	@Override
	public GmMetaModel getMetaModel() {
		return delegate.getMetaModel();
	}

	@Override
	public String getAccessId() {
		return delegate.getAccessId();
	}

	@Override
	public SelectQueryResult query(SelectQuery query) throws ModelAccessException {
		return delegate.query(query);
	}

	@Override
	public EntityQueryResult queryEntities(EntityQuery request) throws ModelAccessException {
		return delegate.queryEntities(request);
	}

	@Override
	public PropertyQueryResult queryProperty(PropertyQuery request) throws ModelAccessException {
		return delegate.queryProperty(request);
	}

	@Override
	public ManipulationResponse applyManipulation(ManipulationRequest manipulationRequest) throws ModelAccessException {
		return delegate.applyManipulation(manipulationRequest);
	}

	@Override
	public ReferencesResponse getReferences(ReferencesRequest referencesRequest) throws ModelAccessException {
		return delegate.getReferences(referencesRequest);
	}

	@Override
	public Set<String> getPartitions() throws ModelAccessException {
		return delegate.getPartitions();
	}

}
