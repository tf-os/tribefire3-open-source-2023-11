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
package com.braintribe.model.access;

import java.util.Set;

import com.braintribe.cfg.Configurable;
import com.braintribe.model.accessapi.CustomPersistenceRequest;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.accessapi.ReferencesRequest;
import com.braintribe.model.accessapi.ReferencesResponse;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;

public abstract class AbstractDynamicDelegatingAccess implements IncrementalAccess {

	protected String accessId = null;
	
	@Override
	public GmMetaModel getMetaModel() {
		return getDelegate().getMetaModel();
	}
	
	@Override
	public ManipulationResponse applyManipulation(ManipulationRequest manipulationRequest)
			throws ModelAccessException {
		return getDelegate().applyManipulation(manipulationRequest);
	}

	@Override
	public EntityQueryResult queryEntities(EntityQuery request) throws ModelAccessException {
		return getDelegate().queryEntities(request);
	}

	@Override
	public PropertyQueryResult queryProperty(PropertyQuery request) throws ModelAccessException {
		return getDelegate().queryProperty(request);
	}
	
	@Override
	public SelectQueryResult query(SelectQuery query) throws ModelAccessException {
		return getDelegate().query(query);
	}

	@Override
	public ReferencesResponse getReferences(ReferencesRequest referencesRequest)
			throws ModelAccessException {
		return getDelegate().getReferences(referencesRequest);
	}
	
	@Override
	public String getAccessId() {
		if (this.accessId != null) {
			return this.accessId;
		}
		return getDelegate().getAccessId();
	}
	
	@Override
	public Set<String> getPartitions() throws ModelAccessException {
		return getDelegate().getPartitions();
	}

	@Override
	public Object processCustomRequest(ServiceRequestContext context, CustomPersistenceRequest request) {
		return getDelegate().processCustomRequest(context, request);
	}
	
	abstract protected IncrementalAccess getDelegate();

	@Configurable
	public void setAccessId(String accessId) {
		this.accessId = accessId;
	}

}
