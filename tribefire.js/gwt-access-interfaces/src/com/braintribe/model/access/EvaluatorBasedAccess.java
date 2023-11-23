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
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * @author peter.gazdik
 */
public class EvaluatorBasedAccess implements IncrementalAccess {

	private final String accessId;
	private final Evaluator<ServiceRequest> evaluator;

	public EvaluatorBasedAccess(String accessId, Evaluator<ServiceRequest> evaluator) {
		this.accessId = accessId;
		this.evaluator = evaluator;
	}

	@Override
	public String getAccessId() {
		return accessId;
	}

	@Override
	public GmMetaModel getMetaModel() {
		GetModel request = request(GetModel.T);
		return request.eval(evaluator).get();
	}

	@Override
	public SelectQueryResult query(SelectQuery query) {
		QueryAndSelect request = request(QueryAndSelect.T);
		request.setQuery(query);

		return request.eval(evaluator).get();
	}

	@Override
	public EntityQueryResult queryEntities(EntityQuery query) {
		QueryEntities request = request(QueryEntities.T);
		request.setQuery(query);

		return request.eval(evaluator).get();
	}

	@Override
	public PropertyQueryResult queryProperty(PropertyQuery query) {
		QueryProperty request = request(QueryProperty.T);
		request.setQuery(query);

		return request.eval(evaluator).get();
	}

	@Override
	public ManipulationResponse applyManipulation(ManipulationRequest request) {
		return request.eval(evaluator).get();
	}

	@Override
	public ReferencesResponse getReferences(ReferencesRequest referencesRequest) {
		ReferencesRequest request = request(ReferencesRequest.T);
		return request.eval(evaluator).get();
	}

	@Override
	public Set<String> getPartitions() {
		GetPartitions request = request(GetPartitions.T);
		return request.eval(evaluator).get();
	}

	private <R extends PersistenceRequest> R request(EntityType<R> et) {
		R result = et.create();
		result.setServiceId(accessId);

		return result;
	}

}
