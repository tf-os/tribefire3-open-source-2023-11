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
package com.braintribe.model.processing.ddra.endpoints.rest.v2.handlers;

import java.io.IOException;
import java.util.List;

import com.braintribe.ddra.endpoints.api.rest.v2.RestV2EndpointContext;
import com.braintribe.model.accessapi.QueryEntities;
import com.braintribe.model.ddra.endpoints.v2.DdraGetEntitiesEndpoint;
import com.braintribe.model.ddra.endpoints.v2.DdraGetEntitiesProjection;
import com.braintribe.model.ddra.endpoints.v2.DdraUrlPathParameters;
import com.braintribe.model.ddra.endpoints.v2.HasGetEntitiesProjection;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.query.fluent.AbstractQueryBuilder;
import com.braintribe.model.processing.query.fluent.CascadedOrderingBuilder;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.web.rest.HttpExceptions;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.OrderingDirection;

public class RestV2GetEntitiesHandler extends AbstractEntityQueryingHandler<DdraGetEntitiesEndpoint> {

	@Override
	public void handle(RestV2EndpointContext<DdraGetEntitiesEndpoint> context) throws IOException {
		DdraUrlPathParameters parameters = context.getParameters();
		
		if(parameters.getEntityId() != null) {
			handleGetEntityById(context);
			return;
		}

		DdraGetEntitiesEndpoint endpoint = context.getEndpoint();
		AbstractQueryBuilder<EntityQuery> builder = decodeEntityQueryBuilder(context);

		QueryEntities request = QueryEntities.T.create();
		request.setSessionId(endpoint.getSessionId());
		request.setServiceId(parameters.getAccessId());

		builder.tc(traversingCriteriasMap.getCriterion(endpoint.getComputedDepth()));

		computeOrderBy(context.getEntityType(), endpoint, builder);
		computePaging(endpoint, builder);

		EntityQuery query = builder.done();
		request.setQuery(query);
		query.setNoAbsenceInformation(endpoint.getNoAbsenceInformation());
		
		EntityQueryResult result = evaluateQueryRequest(request, endpoint, false);
		Object projectedResult = project(result, getProjection(endpoint, DdraGetEntitiesProjection.results));
		boolean full = query.getTraversingCriterion() != null;
		writeResponse(context, projectedResult, full);
	}

	private void handleGetEntityById(RestV2EndpointContext<DdraGetEntitiesEndpoint> context) throws IOException {
		DdraUrlPathParameters parameters = context.getParameters();

		DdraGetEntitiesEndpoint endpoint = decode(context);
		
		computeOutMarshallerFor(context);

		EntityType<?> entityType = context.getEntityType();
		EntityQueryBuilder builder = EntityQueryBuilder.from(entityType);
		builder.tc(traversingCriteriasMap.getCriterion(endpoint.getComputedDepth()));
		String partition = parameters.getEntityPartition();
		if(partition != null) {
			builder.where().conjunction().property("id").eq(parameters.getEntityId()).property("partition").eq(parameters.getEntityPartition()).close();
		} else {
			builder.where().property("id").eq(parameters.getEntityId());
		}
		EntityQuery query = builder.done();
		query.setNoAbsenceInformation(endpoint.getNoAbsenceInformation());

		QueryEntities request = QueryEntities.T.create();
		request.setSessionId(endpoint.getSessionId());
		request.setServiceId(parameters.getAccessId());
		request.setQuery(query);

		EntityQueryResult result = evaluateQueryRequest(request, endpoint, true);

		if(result.getEntities().isEmpty()) {
			HttpExceptions.notFound("Cannot find entity with type %s with ID %s %s in access %s.", context.getEntityType().getTypeSignature(), 
					parameters.getEntityId(), partition != null ? " and partition " + partition : "", parameters.getAccessId());
		}

		if(result.getEntities().size() > 1) {
			HttpExceptions.badRequest("%d entities found with type %s with ID %s %s in access %s.", result.getEntities().size(), 
					context.getEntityType().getTypeSignature(), parameters.getEntityId(), partition != null ? " and partition " + partition : "", parameters.getAccessId());
		}

		Object projectedResult = project(result, getProjection(endpoint, DdraGetEntitiesProjection.firstResult));
		boolean full = query.getTraversingCriterion() != null;
		writeResponse(context, projectedResult, full);
	}

	private void computeOrderBy(EntityType<?> entityType, DdraGetEntitiesEndpoint endpoint, AbstractQueryBuilder<EntityQuery> builder) {
		if(endpoint.getOrderBy().size() < endpoint.getOrderDirection().size()) {
			HttpExceptions.badRequest("Expected at least as many orderBy (got %d) as there are orderDirection (got %d).", 
					endpoint.getOrderBy().size(), endpoint.getOrderDirection().size());
		}
		if(endpoint.getOrderBy().size() == 1) {
			builder.orderBy(getOrderByProperty(entityType, endpoint, 0), getOrderingDirection(endpoint, 0));
			return;
		}
		
		CascadedOrderingBuilder<? extends AbstractQueryBuilder<EntityQuery>> ordering = builder.orderByCascade();
		for(int i = 0; i < endpoint.getOrderBy().size(); i++) {
			ordering = ordering.dir(getOrderingDirection(endpoint, i)).property(getOrderByProperty(entityType, endpoint, i));
		}
		ordering.close();
	}

	private String getOrderByProperty(EntityType<?> entityType, DdraGetEntitiesEndpoint endpoint, int index) {
		String propertyName = endpoint.getOrderBy().get(index);
		Property property = entityType.findProperty(propertyName);
		if(property == null) {
			HttpExceptions.badRequest("Invalid orderBy: Cannot find property %s in entityType %s", propertyName, entityType.getTypeSignature());
		}
		return propertyName;
	}

	private OrderingDirection getOrderingDirection(DdraGetEntitiesEndpoint endpoint, int index) {
		if(index >= endpoint.getOrderDirection().size()) {
			return OrderingDirection.ascending;
		}
		return endpoint.getOrderDirection().get(index);
	}

	private void computePaging(DdraGetEntitiesEndpoint endpoint, AbstractQueryBuilder<EntityQuery> builder) {
		if(endpoint.getMaxResults() != null) {
			Integer startIndex = endpoint.getStartIndex() == null ? 0 : endpoint.getStartIndex();
			builder.paging(endpoint.getMaxResults(), startIndex);
		}
	}

	private DdraGetEntitiesProjection getProjection(HasGetEntitiesProjection endpoint, DdraGetEntitiesProjection defaultProjecition) {
		return endpoint.getProjection() == null ? defaultProjecition : endpoint.getProjection();
	}

	private Object project(EntityQueryResult result, DdraGetEntitiesProjection projection) {
		switch(projection) {
			case envelope:
				return result;
			case firstResult:
				List<?> entities = result.getEntities();
				if(entities.isEmpty()) {
					return null;
				}
				return entities.get(0);
			case results:
				return result.getEntities();
			default:
				HttpExceptions.internalServerError("Unexpected projection %s", projection);
				return null;
		}
	}

	@Override
	protected DdraGetEntitiesEndpoint createEndpoint() {
		return DdraGetEntitiesEndpoint.T.create();
	}
}
