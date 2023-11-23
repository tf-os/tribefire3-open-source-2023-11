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
import static com.braintribe.swagger.util.SwaggerProcessorUtil.PROPERTIES_DESCRIPTIONS;
import static com.braintribe.swagger.util.SwaggerProcessorUtil.TO_IGNORE;
import static com.braintribe.swagger.util.SwaggerProcessorUtil.getSimpleParameterType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.cfg.Configurable;
import com.braintribe.gwt.utils.genericmodel.GMCoreTools;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.ddra.endpoints.v2.DdraDeleteEntitiesEndpoint;
import com.braintribe.model.ddra.endpoints.v2.DdraGetEntitiesEndpoint;
import com.braintribe.model.ddra.endpoints.v2.DdraManipulateEntitiesEndpoint;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmLinearCollectionType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.data.constraint.Deletable;
import com.braintribe.model.meta.data.constraint.Instantiable;
import com.braintribe.model.meta.data.constraint.Modifiable;
import com.braintribe.model.meta.data.prompt.Description;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.swagger.v2_0.SwaggerApi;
import com.braintribe.model.swagger.v2_0.SwaggerBodyParameter;
import com.braintribe.model.swagger.v2_0.SwaggerOperation;
import com.braintribe.model.swagger.v2_0.SwaggerParameter;
import com.braintribe.model.swagger.v2_0.SwaggerPath;
import com.braintribe.model.swagger.v2_0.SwaggerSimpleParameter;
import com.braintribe.model.swaggerapi.SwaggerEntitiesRequest;
import com.braintribe.swagger.model.ApiCreationContext;
import com.braintribe.swagger.util.SwaggerProcessorUtil;
import com.braintribe.utils.CollectionTools;
import com.braintribe.utils.collection.api.MultiMap;

public class SwaggerEntitiesRequestProcessor extends SwaggerCrudProcessor<SwaggerEntitiesRequest> {

	private static final String PROPERTY_DEFINITION_SUFFIX = "Property of request. <br/>May also be used to query entity with `where.%s`.<br/>";
	private static final Set<String> PATH_ENDPOINT_SUFFIX_PARAMS = CollectionTools.getSet("", "/{id}", "/{id}/{partition}");
	private static final Set<String> TO_IGNORE_IF_NOT_ENTITIES_GET_METHOD = new HashSet<>(
			Arrays.asList("startIndex", "maxResults", "orderBy", "orderDirection", "distinct"));

	private static Predicate<GmProperty> IGNORED_PROPERTIES_PREDICATE = property -> !CollectionTools.getSet("globalId").contains(property.getName());

	protected List<SwaggerParameter> endpointParametersManipulationEntitiesEndpoint;
	protected List<SwaggerParameter> endpointParametersDeleteEntitiesEndpoint;

	@Override
	public SwaggerApi process(ServiceRequestContext requestContext, SwaggerEntitiesRequest request) {
		logger.info("Processing Swagger Service Request for entities..." + request);

		checkAccessId(request);

		IncrementalAccess incrementalAccess = findIncrementalAccess(request, cortexSessionFactory.get());
		GmMetaModel metaModel = resolveGmMetaModel(request, incrementalAccess);

		CmdResolver cmdResolver = createResolverForMetaModel(metaModel);
		ModelOracle oracle = cmdResolver.getModelOracle();
		ModelMdResolver mdResolver = cmdResolver.getMetaData().useCase(USECASE_SWAGGER).lenient(true);

		boolean useFullyQualifiedDefinitionName = request.getUseFullyQualifiedDefinitionName();
		ApiCreationContext creationContext = new ApiCreationContext(request, oracle, mdResolver, incrementalAccess, useFullyQualifiedDefinitionName);

		computeFailureType();
		computeEndpointParameters(request);
		return getSwaggerApi(creationContext);
	}

	private void computeEndpointParameters(SwaggerEntitiesRequest request) {
		// @formatter:off
		endpointParameters = DdraGetEntitiesEndpoint.T.getProperties().stream()
				.filter(property -> !TO_IGNORE.contains(property.getName()))
				.sorted(Comparator.comparing(Property::getName))
				.map(property -> resolveEndpointProperty(request, property))
				.collect(Collectors.toList());
		// @formatter:on

		// @formatter:off
		endpointParametersManipulationEntitiesEndpoint = DdraManipulateEntitiesEndpoint.T.getProperties().stream()
				.filter(property -> !TO_IGNORE.contains(property.getName()))
				.sorted(Comparator.comparing(Property::getName))
				.map(property -> resolveEndpointProperty(request, property))
				.collect(Collectors.toList());
		// @formatter:on

		// @formatter:off
		endpointParametersDeleteEntitiesEndpoint = DdraDeleteEntitiesEndpoint.T.getProperties().stream()
				.filter(property -> !TO_IGNORE.contains(property.getName()))
				.sorted(Comparator.comparing(Property::getName))
				.map(property -> resolveEndpointProperty(request, property))
				.collect(Collectors.toList());
		// @formatter:on
	}
	private SwaggerSimpleParameter resolveEndpointProperty(SwaggerEntitiesRequest request, Property property) {
		SwaggerSimpleParameter parameter = SwaggerSimpleParameter.T.create();

		parameter.setName("endpoint." + property.getName());
		parameter.setIn("query");

		resolveParameterDescTypeAndDefault(request.getSessionId(), property, parameter, false);

		if ("array".equals(parameter.getType())) {
			parameter.setItems(getSimpleItems(((CollectionType) property.getType()).getCollectionElementType()));
			parameter.setCollectionFormat("multi");
		}

		return parameter;
	}

	private SwaggerApi getSwaggerApi(ApiCreationContext creationContext) {
		logger.info("Creating swagger api...");

		SwaggerEntitiesRequest request = (SwaggerEntitiesRequest) creationContext.request;
		MultiMap<GmEntityType, String> entities = getEntities(creationContext.access, creationContext.oracle, request.getResource(),
				creationContext.mdResolver);

		String swaggerBasePath = request.getBasePath();
		if (swaggerBasePath == null)
			swaggerBasePath = getBasePath() + "/" + request.getAccessId();

		SwaggerApi api = createSwaggerApi("Tribefire CRUD API (Entity Level) for " + request.getAccessId(), swaggerBasePath);
		api.setUseFullyQualifiedDefinitionName(creationContext.useFullyQualifiedDefinitionName);

		entities.keySet().forEach(service -> resolveApi(request, api, service, creationContext));

		logger.info("Swagger api has been created.");
		return api;
	}

	private void resolveApi(SwaggerEntitiesRequest request, SwaggerApi api, GmEntityType service, ApiCreationContext creationContext) {
		PATH_ENDPOINT_SUFFIX_PARAMS.forEach(uriPathParam -> {
			if (!request.getEnablePartition() && uriPathParam.contains("partition"))
				return;

			String apiPath = getPathKey("/", service, uriPathParam, creationContext.useFullyQualifiedDefinitionName);
			SwaggerPath path = getPath(api, service, apiPath, creationContext);
			if (path.getGet() != null || path.getPost() != null || path.getPost() != null || path.getPatch() != null || path.getDelete() != null)
				api.getPaths().put(apiPath, path);
		});
	}

	private boolean checkIsInstantiable(GmEntityType service, ModelMdResolver mdResolver) {
		return !mdResolver.entityTypeSignature(service.getTypeSignature()).useCase(USECASE_SWAGGER).is(Instantiable.T);
	}

	private boolean checkIsModifiable(GmEntityType service, ModelMdResolver mdResolver) {
		return !mdResolver.entityTypeSignature(service.getTypeSignature()).useCase(USECASE_SWAGGER).is(Modifiable.T);
	}

	private boolean checkIsDeletable(GmEntityType service, ModelMdResolver mdResolver) {
		return !mdResolver.entityTypeSignature(service.getTypeSignature()).useCase(USECASE_SWAGGER).is(Deletable.T);
	}

	private SwaggerPath getPath(SwaggerApi api, GmEntityType serviceType, String apiPath, ApiCreationContext creationContext) {

		String typeSignature = serviceType.getTypeSignature();
		Collection<String> typeTags = Collections.singleton(GMCoreTools.getSimpleEntityTypeNameFromTypeSignature(typeSignature) + " ("
				+ GMCoreTools.getJavaPackageNameFromEntityTypeSignature(typeSignature) + ")");

		ModelMdResolver mdResolver = creationContext.mdResolver;
		SwaggerPath path = SwaggerPath.T.create();
		if (!checkIsInstantiable(serviceType, mdResolver) && !serviceType.getIsAbstract()) {
			path.setPost(getOperation(api, serviceType, typeTags, true, "post", apiPath, creationContext));
		}
		if (!checkIsModifiable(serviceType, mdResolver) && !serviceType.getIsAbstract()) {
			path.setPut(getOperation(api, serviceType, typeTags, true, "put", apiPath, creationContext));
			path.setPatch(getOperation(api, serviceType, typeTags, true, "patch", apiPath, creationContext));
		}
		if (!checkIsDeletable(serviceType, mdResolver) && !serviceType.getIsAbstract()) {
			path.setDelete(getOperation(api, serviceType, typeTags, false, "delete", apiPath, creationContext));
		}
		if (!serviceType.getIsAbstract()) {
			path.setGet(getOperation(api, serviceType, typeTags, false, "get", apiPath, creationContext));
		}

		return path;
	}

	private Stream<SwaggerSimpleParameter> getSimpleParameters(GmEntityType gmEntityType, String apiPath, ApiCreationContext creationContext) {
		EntityTypeOracle oracle = creationContext.oracle.getEntityTypeOracle(gmEntityType);

		return oracle.getProperties().asGmProperties().filter(IGNORED_PROPERTIES_PREDICATE).filter(SwaggerProcessorUtil::isSimpleProperty)
				.map(gmProperty -> {

					String name = gmProperty.getName();
					if (apiPath.contains("{id}") || name.equals("id"))
						return null;

					SwaggerSimpleParameter parameter = SwaggerSimpleParameter.T.create();
					parameter.setName("where." + name);
					Description propertyDescription = creationContext.mdResolver.entityType(gmEntityType).property(gmProperty).meta(Description.T)
							.exclusive();
					parameter.setDescription(resolvePropertyDescription(String.format(PROPERTY_DEFINITION_SUFFIX, name), propertyDescription));
					parameter.setIn("query");
					parameter.setType(name.equals("id") ? "string" : getSimpleParameterType(gmProperty));
					parameter.setDefault(getInitializerDefault(gmProperty));

					if ("array".equals(parameter.getType())) {
						GmLinearCollectionType collectionType = (GmLinearCollectionType) gmProperty.getType();
						parameter.setItems(getSimpleItems(collectionType.getElementType()));
						parameter.setCollectionFormat("multi");
					}

					return parameter;
				}).filter(param -> (param != null && param.getType() != null));
	}

	private String resolvePropertyDescription(String defDescriptionSuffix, Description propertyDescription) {
		return propertyDescription != null ? propertyDescription.getDescription().getLocalizedValues().get("default") + " " + defDescriptionSuffix
				: defDescriptionSuffix;
	}

	private SwaggerOperation getOperation(SwaggerApi api, GmEntityType serviceType, Collection<String> tags, boolean hasBody, String method,
			String apiPath, ApiCreationContext creationContext) {
		SwaggerOperation operation = SwaggerOperation.T.create();

		operation.setDescription(String.format(METHODS_DESCRIPTIONS.get(method).toString(), serviceType.getTypeSignature()));
		resolveIdPartitionAndDeleteOperationParameters(apiPath, operation);

		operation.getTags().addAll(tags);

		if (hasBody) {
			SwaggerBodyParameter bodyParameter = getBodyParameter(api, serviceType, serviceType.getTypeSignature(), IGNORED_PROPERTIES_PREDICATE);
			if (bodyParameter != null)
				operation.getParameters().add(bodyParameter);
		} else {
			Stream<SwaggerSimpleParameter> simpleParameters = getSimpleParameters(serviceType, apiPath, creationContext);
			simpleParameters.forEach(operation.getParameters()::add);
		}

		boolean pathContainId = apiPath.contains("{id}");
		if (method.equals("get"))
			endpointParameters.forEach(ep -> {
				if (pathContainId && TO_IGNORE_IF_NOT_ENTITIES_GET_METHOD.contains(ep.getName().split(PATTERN_DOT)[1]))
					return;
				operation.getParameters().add(ep);
			});

		if (method.equals("post") || method.equals("put") || method.equals("patch"))
			endpointParametersManipulationEntitiesEndpoint.forEach(ep -> operation.getParameters().add(ep));

		if (method.equals("delete"))
			endpointParametersDeleteEntitiesEndpoint.forEach(ep -> {
				if (pathContainId && "allowMultipleDelete".equals(ep.getName().split(PATTERN_DOT)[1]))
					return;
				operation.getParameters().add(ep);
			});

		addResponsesToOperation(api, serviceType, operation, IGNORED_PROPERTIES_PREDICATE);

		return operation;
	}

	private void resolveIdPartitionAndDeleteOperationParameters(String apiPath, SwaggerOperation operation) {
		if (apiPath.contains("{id}"))
			operation.getParameters().add(getIdPathParameter());
		if (apiPath.contains("{partition}"))
			operation.getParameters().add(getPartitionPathParameter());
	}

	private SwaggerSimpleParameter getIdPathParameter() {
		SwaggerSimpleParameter id = SwaggerSimpleParameter.T.create();
		id.setName("id");
		id.setIn("path");
		id.setDescription(PROPERTIES_DESCRIPTIONS.get("id").toString());
		id.setType("string");
		id.setRequired(true);
		return id;
	}

	@Override
	public String getBasePath() {
		return super.getBasePath() != null ? super.getBasePath() + BASE_PATH : BASE_PATH;
	}

	@Configurable
	public void setDefaultAccessId(String defaultAccessId) {
		this.defaultAccessId = defaultAccessId;
	}
}
