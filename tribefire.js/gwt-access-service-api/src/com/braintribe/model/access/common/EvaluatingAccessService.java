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
package com.braintribe.model.access.common;

import java.util.Set;

import com.braintribe.gm.model.persistence.reflection.api.GetAccessIds;
import com.braintribe.gm.model.persistence.reflection.api.GetMetaModel;
import com.braintribe.gm.model.persistence.reflection.api.GetMetaModelForTypes;
import com.braintribe.gm.model.persistence.reflection.api.GetModelAndWorkbenchEnvironment;
import com.braintribe.gm.model.persistence.reflection.api.GetModelEnvironment;
import com.braintribe.gm.model.persistence.reflection.api.GetModelEnvironmentForDomain;
import com.braintribe.gm.model.persistence.reflection.api.GetModelEnvironmentServices;
import com.braintribe.model.access.AccessService;
import com.braintribe.model.access.AccessServiceException;
import com.braintribe.model.accessapi.AccessDomain;
import com.braintribe.model.accessapi.GetPartitions;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.accessapi.ModelEnvironment;
import com.braintribe.model.accessapi.ModelEnvironmentServices;
import com.braintribe.model.accessapi.QueryAndSelect;
import com.braintribe.model.accessapi.QueryEntities;
import com.braintribe.model.accessapi.QueryProperty;
import com.braintribe.model.accessapi.ReferencesRequest;
import com.braintribe.model.accessapi.ReferencesResponse;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.service.api.ServiceRequest;

public class EvaluatingAccessService implements AccessService {
	
	private final Evaluator<ServiceRequest> evaluator;
	
	public EvaluatingAccessService(Evaluator<ServiceRequest> evaluator) {
		this.evaluator = evaluator;
	}

	@Override
	public ModelEnvironment getModelEnvironment(String accessId) throws AccessServiceException {
		GetModelEnvironment request = GetModelEnvironment.T.create();
		request.setAccessId(accessId);
		return request.eval(evaluator).get();
	}

	@Override
	public ModelEnvironment getModelAndWorkbenchEnvironment(String accessId) throws AccessServiceException {
		GetModelAndWorkbenchEnvironment request = GetModelAndWorkbenchEnvironment.T.create();
		request.setAccessId(accessId);
		return request.eval(evaluator).get();
	}

	@Override
	public ModelEnvironment getModelAndWorkbenchEnvironment(String accessId, Set<String> workbenchPerspectiveNames) throws AccessServiceException {
		GetModelAndWorkbenchEnvironment request = GetModelAndWorkbenchEnvironment.T.create();
		request.setAccessId(accessId);
		request.setFoldersByPerspective(workbenchPerspectiveNames);
		return request.eval(evaluator).get();
	}

	@Override
	public ModelEnvironment getModelEnvironmentForDomain(String accessId, AccessDomain accessDomain) throws AccessServiceException {
		GetModelEnvironmentForDomain request = GetModelEnvironmentForDomain.T.create();
		request.setAccessId(accessId);
		request.setAccessDomain(accessDomain);
		return request.eval(evaluator).get();
	}

	@Override
	public ModelEnvironmentServices getModelEnvironmentServices(String accessId) throws AccessServiceException {
		GetModelEnvironmentServices request = GetModelEnvironmentServices.T.create();
		request.setAccessId(accessId);
		return request.eval(evaluator).get();
	}

	@Override
	public ModelEnvironmentServices getModelEnvironmentServicesForDomain(String accessId, AccessDomain accessDomain) throws AccessServiceException {
		GetModelEnvironmentForDomain request = GetModelEnvironmentForDomain.T.create();
		request.setAccessDomain(accessDomain);
		request.setAccessId(accessId);
		return request.eval(evaluator).get();
	}

	@Override
	public Set<String> getAccessIds() throws AccessServiceException {
		GetAccessIds request = GetAccessIds.T.create();
		return request.eval(evaluator).get();
	}

	@Override
	public GmMetaModel getMetaModel(String accessId) throws AccessServiceException {
		GetMetaModel request = GetMetaModel.T.create();
		request.setAccessId(accessId);
		return request.eval(evaluator).get();
	}

	@Override
	public GmMetaModel getMetaModelForTypes(Set<String> typeSignatures) throws AccessServiceException {
		GetMetaModelForTypes request = GetMetaModelForTypes.T.create();
		request.setTypeSignatures(typeSignatures);
		return request.eval(evaluator).get();
	}

	@Override
	public SelectQueryResult query(String accessId, SelectQuery query) throws AccessServiceException {
		QueryAndSelect request = QueryAndSelect.T.create();
		request.setServiceId(accessId);
		request.setQuery(query);
		return request.eval(evaluator).get();
	}

	@Override
	public PropertyQueryResult queryProperty(String accessId, PropertyQuery query) throws AccessServiceException {
		QueryProperty request = QueryProperty.T.create();
		request.setQuery(query);
		request.setServiceId(accessId);
		return request.eval(evaluator).get();
	}

	@Override
	public EntityQueryResult queryEntities(String accessId, EntityQuery query) throws AccessServiceException {
		QueryEntities request = QueryEntities.T.create();
		request.setServiceId(accessId);
		request.setQuery(query);
		return request.eval(evaluator).get();
	}

	@Override
	public ManipulationResponse applyManipulation(String accessId, ManipulationRequest manipulationRequest) throws AccessServiceException {
		ManipulationRequest request = shallowClone(manipulationRequest);
		request.setServiceId(accessId);
		
		return request.eval(evaluator).get();
	}
	

	@Override
	public ReferencesResponse getReferences(String accessId, ReferencesRequest referencesRequest) throws AccessServiceException {
		ReferencesRequest request = shallowClone(referencesRequest);
		request.setServiceId(accessId);
		
		return request.eval(evaluator).get();
	}

	@Override
	public Set<String> getPartitions(String accessId) throws AccessServiceException {
		GetPartitions request = GetPartitions.T.create();
		request.setServiceId(accessId);
		
		return request.eval(evaluator).get();
	}
	

	private static <S extends GenericEntity> S shallowClone(S source) {
		S clone = (S) source.entityType().create();
		shallowClone(source, clone);
		return clone;
	}
	
	private static <S extends GenericEntity, T extends S> void shallowClone(S source, T target) {
		for (Property property : source.entityType().getProperties()) {
			Object value = property.get(source);
			property.set(target, value);
		}
	}

}
