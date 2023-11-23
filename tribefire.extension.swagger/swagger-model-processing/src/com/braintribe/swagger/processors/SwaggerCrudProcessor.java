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
package com.braintribe.swagger.processors;

import static com.braintribe.swagger.util.SwaggerProcessorUtil.CRITERION;
import static com.braintribe.swagger.util.SwaggerProcessorUtil.PROPERTIES_DESCRIPTIONS;
import static com.braintribe.swagger.util.SwaggerProcessorUtil.getSimpleParameterType;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.web.rest.HttpExceptions;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.swagger.v2_0.SwaggerParameter;
import com.braintribe.model.swagger.v2_0.SwaggerSimpleParameter;
import com.braintribe.model.swaggerapi.SwaggerCrudRequest;
import com.braintribe.model.swaggerapi.SwaggerRequest;
import com.braintribe.utils.collection.api.MultiMap;
import com.braintribe.utils.collection.impl.HashMultiMap;

public abstract class SwaggerCrudProcessor<R extends SwaggerCrudRequest> extends SwaggerProcessor<R> {

	protected static final String BASE_PATH = "/rest/v2";
	protected String defaultAccessId = "cortex";

	protected List<SwaggerParameter> endpointParameters;

	protected void checkAccessId(SwaggerCrudRequest request) {
		if (StringUtils.isBlank(request.getAccessId()))
			request.setAccessId(defaultAccessId);
	}

	protected GmMetaModel resolveGmMetaModel(SwaggerRequest request, IncrementalAccess incrementalAccess) {
		if (request.getModel() == null)
			return getGmMetaModelFromIncrementalAccess(incrementalAccess);
		else
			return request.getModel();
	}

	protected static IncrementalAccess findIncrementalAccess(SwaggerCrudRequest request, PersistenceGmSession cortexSession) {
		EntityQuery query = EntityQueryBuilder.from(IncrementalAccess.T).where().conjunction().property("metaModel").ne(null).property("externalId")
				.eq(request.getAccessId()).close().tc(CRITERION).done();

		IncrementalAccess incrementalAccess = cortexSession.query().entities(query).unique();
		if (incrementalAccess == null)
			throw new IllegalArgumentException("No access could be determined for request type: " + request);
		return incrementalAccess;
	}

	protected static GmMetaModel getGmMetaModelFromIncrementalAccess(IncrementalAccess incrementalAccess) {
		GmMetaModel metaModel = incrementalAccess.getMetaModel();
		if (metaModel == null)
			throw new IllegalArgumentException("Requested access: " + incrementalAccess + " does not have a meta model attached.");
		return metaModel;
	}

	protected MultiMap<GmEntityType, String> getEntities(IncrementalAccess access, ModelOracle oracle, String resources, ModelMdResolver mdResolver) {
		MultiMap<GmEntityType, String> entities = new HashMultiMap<>();
		logger.debug("Processing request entities...");
		oracle.getTypes().onlyEntities().<EntityTypeOracle> asTypeOracles().filter(t -> !t.isEvaluable()).forEach(entityTypeOracle -> {
			try {
				GmEntityType type = entityTypeOracle.asGmEntityType();
				if (isNotVisibleAndDoNotMatchResource(type, resources, mdResolver))
					return;

				entities.put2(type, access.getExternalId());

			} catch (Exception e) {
				logger.error("Error occurred while handling increment access: " + access.getExternalId(), e);
			}
		});
		logger.debug("Processing requests entities has been done.");
		return entities;
	}

	protected static void resolveParameterDescTypeAndDefault(String sessionId, Property property, SwaggerSimpleParameter parameter, boolean isId) {
		String description = (String) PROPERTIES_DESCRIPTIONS.get(property.getName());
		if (description == null)
			HttpExceptions.internalServerError("No description found for endpoint property %s", property.getName());

		parameter.setDescription(description);
		parameter.setType(isId ? "string" : getSimpleParameterType(property.getType()));
		parameter.setDefault(property.getName().equals("sessionId") ? sessionId : getInitializerDefault(property));
	}

	protected static SwaggerSimpleParameter getPartitionPathParameter() {
		SwaggerSimpleParameter partition = SwaggerSimpleParameter.T.create();
		partition.setName("partition");
		partition.setIn("path");
		partition.setDescription(PROPERTIES_DESCRIPTIONS.get("partition").toString());
		partition.setType("string");
		partition.setRequired(true);
		return partition;
	}

}
