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
package com.braintribe.cartridge.common.processing.externalizer;

import java.util.Objects;
import java.util.Set;

import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.accessapi.CustomPersistenceRequest;
import com.braintribe.model.accessapi.GetModel;
import com.braintribe.model.accessapi.GetPartitions;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.accessapi.PersistenceRequest;
import com.braintribe.model.accessapi.QueryAndSelect;
import com.braintribe.model.accessapi.QueryEntities;
import com.braintribe.model.accessapi.QueryProperty;
import com.braintribe.model.accessapi.ReferencesRequest;
import com.braintribe.model.accessapi.ReferencesResponse;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * <p>
 * {@link ServiceRequest}-driven externalizing {@link IncrementalAccess} implementation.
 * 
 */
public class ExternalizingIncrementalAccess extends AbstractExternalizingDispatchableComponent<PersistenceRequest> implements IncrementalAccess {

	private final String accessId;

	public ExternalizingIncrementalAccess(Evaluator<ServiceRequest> remoteEvaluator, String externalId) {
		super(remoteEvaluator, externalId);
		this.accessId = externalId;
	}

	@Override
	public GmMetaModel getMetaModel() {
		try {
			return evaluate(GetModel.T.create());

		} catch (ModelAccessException e) {
			throw new GenericModelException("Error or while evauating new GmMetaModel.", e);
		}
	}

	@Override
	public String getAccessId() {
		return accessId;
	}

	@Override
	public SelectQueryResult query(SelectQuery query) throws ModelAccessException {
		Objects.requireNonNull(query, "query must not be null");
		QueryAndSelect request = QueryAndSelect.T.create();
		request.setQuery(query);
		SelectQueryResult response = evaluate(request);
		return response;
	}

	@Override
	public EntityQueryResult queryEntities(EntityQuery query) throws ModelAccessException {
		Objects.requireNonNull(query, "query must not be null");
		QueryEntities request = QueryEntities.T.create();
		request.setQuery(query);
		EntityQueryResult response = evaluate(request);
		return response;
	}

	@Override
	public PropertyQueryResult queryProperty(PropertyQuery query) throws ModelAccessException {
		Objects.requireNonNull(query, "query must not be null");
		QueryProperty request = QueryProperty.T.create();
		request.setQuery(query);
		PropertyQueryResult response = evaluate(request);
		return response;
	}

	@Override
	public ManipulationResponse applyManipulation(ManipulationRequest manipulationRequest) throws ModelAccessException {
		Objects.requireNonNull(manipulationRequest, "manipulationRequest must not be null");
		ManipulationResponse response = evaluate(manipulationRequest);
		return response;
	}

	@Override
	public ReferencesResponse getReferences(ReferencesRequest referencesRequest) throws ModelAccessException {
		Objects.requireNonNull(referencesRequest, "referencesRequest must not be null");
		ReferencesResponse result = evaluate(referencesRequest);
		return result;
	}

	@Override
	public Set<String> getPartitions() throws ModelAccessException {
		Set<String> result = evaluate(GetPartitions.T.create());
		return result;
	}

	@Override
	public Object processCustomRequest(ServiceRequestContext context, CustomPersistenceRequest request) {
		Objects.requireNonNull(request, "customPersitenceRequest must not be null");
		Object result = evaluate(request);
		return result;
	}
	
	protected <T> T evaluate(PersistenceRequest request) throws ModelAccessException {
		T response = (T) request.eval(evaluator()).get();
		return response;
	}

}
