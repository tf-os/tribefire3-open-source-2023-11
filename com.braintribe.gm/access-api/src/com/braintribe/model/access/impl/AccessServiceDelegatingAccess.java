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

import com.braintribe.model.access.AccessService;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.accessapi.ReferencesRequest;
import com.braintribe.model.accessapi.ReferencesResponse;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;

/**
 * An implementation of {@link IncrementalAccess} that wraps an instance of {@link AccessService} and delegates all calls to the service with the
 * access ID configured via {@link #setAccessId(String)}.
 * 
 */
public class AccessServiceDelegatingAccess implements IncrementalAccess {

	private AccessService accessService;
	private String accessId;

	public AccessService getAccessService() {
		return accessService;
	}

	public void setAccessService(AccessService accessService) {
		this.accessService = accessService;
	}

	public void setAccessId(String accessId) {
		this.accessId = accessId;
	}

	@Override
	public String getAccessId() {
		return accessId;
	}

	@Override
	public GmMetaModel getMetaModel() {
		return accessService.getMetaModel(accessId);
	}

	@Override
	public SelectQueryResult query(SelectQuery query) {
		return accessService.query(accessId, query);
	}

	@Override
	public EntityQueryResult queryEntities(EntityQuery request) {
		return accessService.queryEntities(accessId, request);
	}

	@Override
	public PropertyQueryResult queryProperty(PropertyQuery request) {
		return accessService.queryProperty(accessId, request);
	}

	@Override
	public ManipulationResponse applyManipulation(ManipulationRequest manipulationRequest) {
		return accessService.applyManipulation(accessId, manipulationRequest);
	}

	@Override
	public ReferencesResponse getReferences(ReferencesRequest referencesRequest) {
		return accessService.getReferences(accessId, referencesRequest);
	}

	@Override
	public Set<String> getPartitions() {
		return accessService.getPartitions(accessId);
	}

}
