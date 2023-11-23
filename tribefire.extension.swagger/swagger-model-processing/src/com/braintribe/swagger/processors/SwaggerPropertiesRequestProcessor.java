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

import static com.braintribe.swagger.util.SwaggerProcessorUtil.METHODS_DESCRIPTIONS;
import static com.braintribe.swagger.util.SwaggerProcessorUtil.TO_IGNORE;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.gwt.utils.genericmodel.GMCoreTools;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.GmTypeKind;
import com.braintribe.model.meta.data.constraint.Modifiable;
import com.braintribe.model.meta.data.prompt.Visible;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.swagger.v2_0.SwaggerApi;
import com.braintribe.model.swagger.v2_0.SwaggerBodyParameter;
import com.braintribe.model.swagger.v2_0.SwaggerOperation;
import com.braintribe.model.swagger.v2_0.SwaggerPath;
import com.braintribe.model.swagger.v2_0.SwaggerSimpleParameter;
import com.braintribe.model.swaggerapi.SwaggerPropertiesRequest;
import com.braintribe.swagger.model.ApiCreationContext;
import com.braintribe.swagger.model.DdraGetSwaggerPropertiesEndpoint;
import com.braintribe.utils.CollectionTools;
import com.braintribe.utils.collection.api.MultiMap;

public class SwaggerPropertiesRequestProcessor extends SwaggerCrudProcessor<SwaggerPropertiesRequest> {

	private static Predicate<GmProperty> IGNORED_PROPERTIES_PREDICATE = property -> !CollectionTools.getSet("id", "globalId", "partition")
			.contains(property.getName());

	@Override
	public SwaggerApi process(ServiceRequestContext requestContext, SwaggerPropertiesRequest request) {
		logger.info("Processing Swagger Service Request for properties..." + request);

		checkAccessId(request);

		IncrementalAccess incrementalAccess = findIncrementalAccess(request, cortexSessionFactory.get());
		GmMetaModel metaModel;
		metaModel = resolveGmMetaModel(request, incrementalAccess);

		CmdResolver cmdResolver = createResolverForMetaModel(metaModel);
		ModelOracle oracle = cmdResolver.getModelOracle();
		ModelMdResolver mdResolver = cmdResolver.getMetaData().useCase(USECASE_SWAGGER).lenient(true);

		ApiCreationContext creationContext = new ApiCreationContext(request, oracle, mdResolver, incrementalAccess,
				request.getUseFullyQualifiedDefinitionName());
		computeFailureType();
		computeEndpointParameters(request);
		return getSwaggerApi(creationContext);
	}

	private void computeEndpointParameters(SwaggerPropertiesRequest request) {
		// @formatter:off
		endpointParameters = DdraGetSwaggerPropertiesEndpoint.T.getProperties().stream()
				.filter(property -> {
					if(property.getName().equals("id")) return true;
					return !TO_IGNORE.contains(property.getName());
				}).sorted(Comparator.comparing(Property::getName)).map(property -> {

					SwaggerSimpleParameter parameter = SwaggerSimpleParameter.T.create();

					boolean isId = property.getName().equals("id");
					parameter.setName(isId ? property.getName() : "endpoint." + property.getName());
					parameter.setIn(isId ? "path" : "query");

					resolveParameterDescTypeAndDefault(request.getSessionId(), property, parameter, isId);

					if(isId)
						parameter.setRequired(true);
					if ("array".equals(parameter.getType())){
						parameter.setItems(getSimpleItems(((CollectionType) property.getType()).getCollectionElementType()));
						parameter.setCollectionFormat("multi");
					}

					return parameter;
				}).collect(Collectors.toList());
		// @formatter:on
	}

	private SwaggerApi getSwaggerApi(ApiCreationContext creationContext) {
		logger.info("Creating swagger api...");

		SwaggerPropertiesRequest request = (SwaggerPropertiesRequest) creationContext.request;
		MultiMap<GmEntityType, String> entities = getEntities(creationContext.access, creationContext.oracle, request.getResource(),
				creationContext.mdResolver);

		String swaggerBasePath = request.getBasePath();
		if (swaggerBasePath == null)
			swaggerBasePath = getBasePath() + "/" + request.getAccessId();

		SwaggerApi api = createSwaggerApi("Tribefire CRUD API (Property Level) for " + request.getAccessId(), swaggerBasePath);
		api.setUseFullyQualifiedDefinitionName(creationContext.useFullyQualifiedDefinitionName);

		entities.keySet().forEach(service -> resolveApi(creationContext, api, service));

		logger.info("Swagger api has been created.");
		return api;
	}

	private void resolveApi(ApiCreationContext creationContext, SwaggerApi api, GmEntityType service) {

		String typeSignature = service.getTypeSignature();
		Collection<String> typeTags = Collections.singleton(GMCoreTools.getSimpleEntityTypeNameFromTypeSignature(typeSignature) + " ("
				+ GMCoreTools.getJavaPackageNameFromEntityTypeSignature(typeSignature) + ")");

		EntityTypeOracle oracle = creationContext.oracle.getEntityTypeOracle(service);
		ModelMdResolver mdResolver = creationContext.mdResolver;

		Stream<GmProperty> gmProperties = oracle.getProperties().asGmProperties();
		gmProperties.forEach(gmProperty -> {
			GmType type = gmProperty.getType();
			if (!mdResolver.entityType(service).property(gmProperty).useCase(USECASE_SWAGGER).is(Visible.T))
				return;

			boolean omitPut = !mdResolver.entityType(service).property(gmProperty).useCase(USECASE_SWAGGER).is(Modifiable.T);
			SwaggerPath path = getPath(api, type, gmProperty.getName(), typeTags, false, omitPut, service.getIsAbstract());
			if (path == null)
				return;

			api.getPaths().put(getPathKey("/", service, "/{id}/" + gmProperty.getName(), creationContext.useFullyQualifiedDefinitionName), path);
			if (((SwaggerPropertiesRequest) creationContext.request).getEnablePartition())
				api.getPaths().put(
						getPathKey("/", service, "/{id}/{partition}/" + gmProperty.getName(), creationContext.useFullyQualifiedDefinitionName),
						getPath(api, type, gmProperty.getName(), typeTags, true, omitPut, service.getIsAbstract()));

		});
	}

	private boolean isCollection(GmType type) {
		return type.typeKind().equals(GmTypeKind.LIST) || type.typeKind().equals(GmTypeKind.MAP) || type.typeKind().equals(GmTypeKind.SET);
	}

	private SwaggerPath getPath(SwaggerApi api, GmType serviceType, String propertyName, Collection<String> tags, boolean hasPartitionEnabledInPath,
			boolean omitPut, boolean isAbstract) {
		if (isAbstract)
			return null;

		SwaggerPath path = SwaggerPath.T.create();
		path.setGet(getOperation(api, serviceType, propertyName, tags, false, "get", hasPartitionEnabledInPath));
		path.setDelete(getOperation(api, serviceType, propertyName, tags, false, "delete", hasPartitionEnabledInPath));

		if (isCollection(serviceType))
			path.setPost(getOperation(api, serviceType, propertyName, tags, true, "post", hasPartitionEnabledInPath));
		if (!omitPut)
			path.setPut(getOperation(api, serviceType, propertyName, tags, true, "put", hasPartitionEnabledInPath));

		return path;
	}

	private SwaggerOperation getOperation(SwaggerApi api, GmType serviceType, String propertyName, Collection<String> tags, boolean hasBody,
			String method, boolean hasPartitionEnabledInPath) {

		SwaggerOperation operation = SwaggerOperation.T.create();
		operation.setDescription(String.format(METHODS_DESCRIPTIONS.get(method).toString(), serviceType.getTypeSignature()));
		operation.getTags().addAll(tags);

		resolvePartitionAndDeleteQueryProps(method, hasPartitionEnabledInPath, operation);

		if (hasBody) {
			SwaggerBodyParameter bodyParameter = getBodyParameter(api, serviceType, propertyName, IGNORED_PROPERTIES_PREDICATE);
			if (bodyParameter != null)
				operation.getParameters().add(bodyParameter);
		}

		endpointParameters.forEach(operation.getParameters()::add);

		addResponsesToOperation(api, serviceType, operation, IGNORED_PROPERTIES_PREDICATE);

		return operation;
	}

	private void resolvePartitionAndDeleteQueryProps(String method, boolean hasPartitionEnabledInPath, SwaggerOperation operation) {
		if (hasPartitionEnabledInPath)
			operation.getParameters().add(getPartitionPathParameter());
		if (method.equals("post"))
			operation.getParameters().add(getRemoveParameter());
	}

	private SwaggerSimpleParameter getRemoveParameter() {
		SwaggerSimpleParameter removeParameter = SwaggerSimpleParameter.T.create();
		removeParameter.setName("remove");
		removeParameter.setIn("query");
		removeParameter.setDescription("When set to true, removes the values in the body from the collection instead of adding them.");
		removeParameter.setType("boolean");
		removeParameter.setDefault("false");
		removeParameter.setRequired(false);
		return removeParameter;
	}

	@Override
	public String getBasePath() {
		return super.getBasePath() != null ? super.getBasePath() + BASE_PATH : BASE_PATH;
	}

}
