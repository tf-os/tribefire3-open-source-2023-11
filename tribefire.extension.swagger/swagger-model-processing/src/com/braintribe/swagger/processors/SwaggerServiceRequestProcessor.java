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
import static com.braintribe.swagger.util.SwaggerProcessorUtil.TO_IGNORE;
import static com.braintribe.swagger.util.SwaggerProcessorUtil.getSimpleParameterType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.ddra.endpoints.api.api.v1.DdraMappings;
import com.braintribe.ddra.endpoints.api.api.v1.SingleDdraMapping;
import com.braintribe.model.ddra.DdraMapping;
import com.braintribe.model.ddra.endpoints.api.v1.ApiV1DdraEndpoint;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.EntityCriterion;
import com.braintribe.model.generic.pr.criteria.matching.StandardMatcher;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityVisitor;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmLinearCollectionType;
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.constraint.Mandatory;
import com.braintribe.model.meta.data.prompt.Description;
import com.braintribe.model.meta.data.prompt.Embedded;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.meta.data.prompt.Priority;
import com.braintribe.model.meta.data.prompt.Visible;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.meta.oracle.PropertyOracle;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.web.rest.HttpExceptions;
import com.braintribe.model.processing.web.rest.HttpRequestEntityDecoder;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resourceapi.request.ResourceDeleteRequest;
import com.braintribe.model.resourceapi.request.ResourceDownloadRequest;
import com.braintribe.model.resourceapi.request.ResourceUploadRequest;
import com.braintribe.model.service.api.AuthorizableRequest;
import com.braintribe.model.service.api.AuthorizedRequest;
import com.braintribe.model.service.domain.ServiceDomain;
import com.braintribe.model.swagger.v2_0.SwaggerApi;
import com.braintribe.model.swagger.v2_0.SwaggerBodyParameter;
import com.braintribe.model.swagger.v2_0.SwaggerOperation;
import com.braintribe.model.swagger.v2_0.SwaggerParameter;
import com.braintribe.model.swagger.v2_0.SwaggerPath;
import com.braintribe.model.swagger.v2_0.SwaggerResponse;
import com.braintribe.model.swagger.v2_0.SwaggerSimpleParameter;
import com.braintribe.model.swagger.v2_0.SwaggerTag;
import com.braintribe.model.swaggerapi.SwaggerServicesRequest;
import com.braintribe.swagger.util.SwaggerProcessorUtil;
import com.braintribe.utils.CollectionTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.collection.api.MultiMap;
import com.braintribe.utils.collection.impl.HashMultiMap;
import com.braintribe.utils.genericmodel.GMCoreTools;

public class SwaggerServiceRequestProcessor extends SwaggerProcessor<SwaggerServicesRequest> {

	private DdraMappings mappings;
	private String mappedPathTag = "Mapped Endpoints";
	private String genericPathTag = "Generic Endpoints";

	private static final String BASE_PATH = "/api/v1";

	private static Predicate<GmProperty> IGNORED_PROPERTIES_PREDICATE = SwaggerServiceRequestProcessor::defaultIgnoredPropertiesPredicate;

	private static boolean defaultIgnoredPropertiesPredicate(GmProperty property) {
		if (CollectionTools.getSet("id", "globalId", "partition").contains(property.getName()))
			return false;
		
		if (AuthorizableRequest.T.getTypeSignature().equals(property.getDeclaringType().getTypeSignature())
				&& AuthorizedRequest.sessionId.equals(property.getName())) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public SwaggerApi process(ServiceRequestContext requestContext, SwaggerServicesRequest request) {
		logger.info("Processing Service Request:" + request);

		PersistenceGmSession cortexSession = this.cortexSessionFactory.get();

		ServiceDomain domain = findServiceDomain(request, cortexSession);
		if (domain == null)
			throw new IllegalArgumentException("No service domain could be determined for request type: " + request);

		GmMetaModel serviceModel;
		if (request.getModel() == null) {
			serviceModel = domain.getServiceModel();
			if (serviceModel == null)
				throw new IllegalArgumentException("Requested service domain: " + domain + " does not have a service model attached.");
		} else
			serviceModel = request.getModel();

		CmdResolver cmdResolver = createResolverForMetaModel(serviceModel);
		ModelOracle oracle = cmdResolver.getModelOracle();
		ModelMdResolver mdResolver = cmdResolver.getMetaData().useCases(USECASE_SWAGGER, USECASE_DDRA);


		ApiCreationContext creationContext = new ApiCreationContext(cmdResolver, request, oracle, mdResolver, domain, cortexSession,
				request.getUseFullyQualifiedDefinitionName());
		computeFailureType();
		return getSwaggerApi(creationContext);
	}

	private static class ApiCreationContext {

		private final CmdResolver cmdResolver;
		private final ModelOracle oracle;
		private final ModelMdResolver mdResolver;
		private final ServiceDomain domain;
		private final SwaggerServicesRequest request;
		private final PersistenceGmSession cortexSession;
		private final boolean useFullyQualifiedDefinitionName;

		public ApiCreationContext(CmdResolver cmdResolver, SwaggerServicesRequest request, ModelOracle oracle, ModelMdResolver mdResolver, ServiceDomain domain,
				PersistenceGmSession cortexSession, boolean useFullyQualifiedDefinitionName) {
			this.cmdResolver = cmdResolver;
			this.oracle = oracle;
			this.mdResolver = mdResolver;
			this.domain = domain;
			this.request = request;
			this.cortexSession = cortexSession;
			this.useFullyQualifiedDefinitionName = useFullyQualifiedDefinitionName;
		}
	}
	
	private static class RequestCreationContext {
		private final EntityMdResolver mdResolver;
		private final SingleDdraMapping ddraMapping;
		private final String requestTypeSignature;
		private final ApiCreationContext apiCreationContext;
		private final EntityTypeOracle entityTypeOracle;
		private final GmEntityType requestGmEntityType;
		private final ModelMdResolver cortexMdResolver;
		
		public RequestCreationContext(EntityTypeOracle entityTypeOracle, ApiCreationContext apiCreationContext, SingleDdraMapping ddraMapping) {
			this.ddraMapping = ddraMapping;
			this.requestGmEntityType = entityTypeOracle.asGmEntityType();
			this.requestTypeSignature =  requestGmEntityType.getTypeSignature();
			this.mdResolver = apiCreationContext.cmdResolver.getMetaData().entityTypeSignature(requestTypeSignature);
			this.cortexMdResolver = apiCreationContext.cortexSession.getModelAccessory().getMetaData();
			this.apiCreationContext = apiCreationContext;
			this.entityTypeOracle = entityTypeOracle;
			
			String path = (ddraMapping == null) ? getPathKey(apiCreationContext, requestTypeSignature) : ddraMapping.getPath();
			String[] usecases = new String[] {USECASE_DDRA, USECASE_SWAGGER, mappingSpecificUsecase(path )};
			
			cortexMdResolver.useCases(usecases);
			mdResolver.useCases(usecases);
		}
	}

	private ServiceDomain findServiceDomain(SwaggerServicesRequest request, PersistenceGmSession cortexSession) {

		logger.debug("Getting service requests...");
		String serviceDomainId = request.getServiceDomain();
		if (serviceDomainId == null) {
			serviceDomainId = "serviceDomain:default";
		}
		EntityQuery query = EntityQueryBuilder.from(ServiceDomain.T).where().property(ServiceDomain.externalId).eq(serviceDomainId).tc(CRITERION)
				.done();

		ServiceDomain domain = cortexSession.query().entities(query).first();
		return domain;
	}

	private MultiMap<RequestCreationContext, String> getServiceRequests(ApiCreationContext creationContext) {

		ServiceDomain domain = creationContext.domain;
		ModelOracle oracle = creationContext.oracle;
		SwaggerServicesRequest request = creationContext.request;
		ModelMdResolver mdResolver = creationContext.mdResolver;

		MultiMap<RequestCreationContext, String> entities = new HashMultiMap<>();

		logger.debug("Processing service requests entities...");

		oracle.getTypes().onlyEntities().<EntityTypeOracle>asTypeOracles().filter(EntityTypeOracle::isEvaluable).forEach(entityTypeOracle -> {
			try {
				RequestCreationContext requestCreationContext = new RequestCreationContext(entityTypeOracle, creationContext, null);
				GmEntityType type = requestCreationContext.requestGmEntityType;
				if (type.getIsAbstract() || isNotVisibleAndDoNotMatchResource(type, request.getResource(), mdResolver))
					return;

				entities.put2(requestCreationContext, domain.getExternalId());

			} catch (Exception e) {
				logger.error("Error occurred while handling service domain: " + domain.getExternalId(), e);
			}

		});

		logger.debug("Processing service requests entities has been done.");
		return entities;
	}

	private List<SwaggerParameter> computeEndpointParameters(RequestCreationContext context) {
		EntityMdResolver entityMdResolver = context.cortexMdResolver.entityType(ApiV1DdraEndpoint.T);
		
		//@formatter:off
		return ApiV1DdraEndpoint.T.getProperties().stream()
				.filter(property ->	!TO_IGNORE.contains(property.getName()))
				.filter(property -> resolveVisibility(entityMdResolver, property.getName()))
				.sorted(Comparator.comparing(Property::getName)).map(property -> {

					SwaggerSimpleParameter parameter = SwaggerSimpleParameter.T.create();
					parameter.setName("endpoint." + property.getName());
					parameter.setIn("query");

					String description = (String) PROPERTIES_DESCRIPTIONS.get(property.getName());
					if(description == null) {
						HttpExceptions.internalServerError("No description found for endpoint property %s", property.getName());
					}
					parameter.setDescription(description);
					parameter.setType(getSimpleParameterType(property.getType()));
					parameter.setDefault(getInitializerDefault(property));

					if ("array".equals(parameter.getType())){
						parameter.setItems(getSimpleItems(((CollectionType) property.getType()).getCollectionElementType()));
						parameter.setCollectionFormat("multi");
					}

					return parameter;
				}).filter(param -> param.getType() != null).collect(Collectors.toList());
		//@formatter:on
		
	}

	private SwaggerApi getSwaggerApi(ApiCreationContext creationContext) {
		SwaggerServicesRequest request = creationContext.request;

		String swaggerBasePath = request.getBasePath();
		if (swaggerBasePath == null)
			swaggerBasePath = getBasePath();

		logger.info("Creating Swagger Api...");
		SwaggerApi api = createSwaggerApi("Tribefire Service API for: " + creationContext.domain.getName(), swaggerBasePath);
		api.setUseFullyQualifiedDefinitionName(creationContext.useFullyQualifiedDefinitionName);

		if (!resolveExplicitMappings(api, creationContext)) {
			MultiMap<RequestCreationContext, String> services = getServiceRequests(creationContext);
			services.keySet().forEach(service -> {
				SwaggerPath swaggerPath = getPath(api, service);
				api.getPaths().put(swaggerPath.getPath(), swaggerPath);
				api.getPathList().add(swaggerPath);
			});
		}

		logger.info("Swagger api has been created.");

		Set<String> uniqueTags = new HashSet<>();
		SwaggerApi.T.traverse(api, new StandardMatcher(), new EntityVisitor() {

			@Override
			protected void visitEntity(GenericEntity entity, EntityCriterion criterion, TraversingContext traversingContext) {
				if (entity instanceof SwaggerOperation) {
					SwaggerOperation operation = (SwaggerOperation) entity;
					uniqueTags.addAll(operation.getTags());
				}
			}

		});

		List<SwaggerTag> sortedTags = uniqueTags.stream().sorted().map((t) -> {
			SwaggerTag swaggerTag = SwaggerTag.T.create();
			swaggerTag.setName(t);
			return swaggerTag;
		}).collect(Collectors.toList());

		api.setTags(sortedTags);
		return api;
	}

	private boolean resolveExplicitMappings(SwaggerApi api, ApiCreationContext creationContext) {
		SwaggerServicesRequest request = creationContext.request;
		if (request.getServiceDomain() == null)
			request.setServiceDomain("serviceDomain:default");

		PersistenceGmSession cortexSession = creationContext.cortexSession;
		
		// I want actually just a boolean but the variable needs to be final because of
		// the lambda in the forEach so I abuse an AtomicBoolean to contain the value
		AtomicBoolean explicitMappingResolved = new AtomicBoolean(); 
		
		mappings.getAll().stream().sorted((m1, m2) -> {

			String path1 = m1.getPath();
			String path2 = m2.getPath();

			return path1.compareTo(path2);
		}).forEach((mapping) -> {

			String mappedServiceDomain = mapping.getDefaultServiceDomain();
			if (mappedServiceDomain == null)
				mappedServiceDomain = "serviceDomain:default";

			if (!mappedServiceDomain.equals(request.getServiceDomain()))
				return;

			GmEntityType service = cortexSession.findEntityByGlobalId("type:" + mapping.getRequestType().getTypeSignature());
			if (service == null) {
				EntityQuery q = EntityQueryBuilder.from(GmEntityType.T).where().property("typeSignature").eq(mapping.getRequestType().getTypeSignature()).done();
				service = cortexSession.query().entities(q).first();
				
				if (service == null) {
					logger.warn("Cannot find service type " + mapping.getRequestType().getTypeSignature() + " for mapped path " + mapping.getPath());
					return;
				}
			}

			Set<String> tags = new HashSet<>();
			EntityQuery mappingQuery = EntityQueryBuilder.from(DdraMapping.T).where().property("path").eq(mapping.getPath()).done();
			DdraMapping configuredMapping = cortexSession.query().entities(mappingQuery).first();

			if (configuredMapping != null && !configuredMapping.getTags().isEmpty())
				tags.addAll(configuredMapping.getTags());
			else
				tags.add(this.mappedPathTag);

			SwaggerPath foundPath = api.getPaths().get(mapping.getPath());
			EntityTypeOracle entityTypeOracle = creationContext.oracle.getEntityTypeOracle(service);
			RequestCreationContext requestCreationContext = new RequestCreationContext(entityTypeOracle, creationContext, mapping);
			SwaggerPath swaggerPath = getPath(api, mapping, requestCreationContext, tags, foundPath);
			if (swaggerPath != null) {
				api.getPaths().put(mapping.getPath(), swaggerPath);
				api.getPathList().add(swaggerPath);
			}

			explicitMappingResolved.set(true);
		});
		
		return explicitMappingResolved.get();

	}

	private SwaggerPath getPath(SwaggerApi api, SingleDdraMapping mapping, RequestCreationContext serviceContext, Collection<String> tags,
			SwaggerPath existingPath) {

		SwaggerPath path = existingPath != null ? existingPath : SwaggerPath.T.create();
		if (existingPath == null)
			path.setPath(mapping.getPath());

		/* TODO: Do a similar check earlier
		ModelOracle oracle = creationContext.oracle;
		if (oracle.findEntityTypeOracle(serviceType) == null) {
			logger.warn("ServiceRequest: " + serviceType.getTypeSignature() + " (mapped for path: " + mapping.getPath()
					+ " could not be found on registered service domain: " + mapping.getDefaultServiceDomain()
					+ ". Please check the associated ServiceModel. ");
			return null;
		}*/
		
		Boolean announceAsMultipart = mapping.getAnnounceAsMultipart();
		final boolean forceMultipart;
		final boolean postMultipart;
		
		if (announceAsMultipart == null) {
			SwaggerServicesRequest swaggerRequest = serviceContext.apiCreationContext.request;
			// For POST requests it makes sense to enable multipart by default
			postMultipart = swaggerRequest.getDefaultToMultipart();
			// For PUT and PATCH multipart requests are usually considered incorrect so we will never default to that 
			forceMultipart = false;
		} else {
			forceMultipart = announceAsMultipart; // If explicitly mapped, PUT and PATCH requests may be multipart
			postMultipart = announceAsMultipart;
		}
		
		switch (mapping.getMethod()) {
			case DELETE:
				path.setDelete(getOperation(api, serviceContext, tags, false, false, mapping));
				break;
			case GET:
				path.setGet(getOperation(api, serviceContext, tags, false, false, mapping));
				break;
			case GET_POST:
				path.setGet(getOperation(api, serviceContext, tags, false, false, mapping));
				path.setPost(getOperation(api, serviceContext, tags, true, postMultipart, mapping));
				break;
			case POST:
				path.setPost(getOperation(api, serviceContext, tags, true, postMultipart, mapping));
				break;
			case PUT:
				path.setPut(getOperation(api, serviceContext, tags, true, forceMultipart, mapping));
				break;
			case PATCH:
				path.setPatch(getOperation(api, serviceContext, tags, true, forceMultipart, mapping));
				break;
			default:
				HttpExceptions.internalServerError("Unexpected MappingMethod %s", mapping.getMethod());
		}

		return path;
	}

	private static String getPathKey(ApiCreationContext creationContext, String typeSignature) {
		String domainId = creationContext.domain.getExternalId();
		return getPathKey("/" + domainId + "/", typeSignature, "", creationContext.useFullyQualifiedDefinitionName);
	}
	
	private SwaggerPath getPath(SwaggerApi api, RequestCreationContext serviceContext) {
		String typeSignature = serviceContext.requestTypeSignature;
		ApiCreationContext creationContext = serviceContext.apiCreationContext;
		
		Collection<String> tags = Collections.singleton(this.genericPathTag);
		String path = getPathKey(creationContext, typeSignature);
		
		SwaggerServicesRequest swaggerRequest = creationContext.request;
		// For POST requests it makes sense to enable multipart by default
		boolean postMultipart = swaggerRequest.getDefaultToMultipart();

		SwaggerPath swaggerPath = SwaggerPath.T.create();
		swaggerPath.setPath(path);
		swaggerPath.setGet(getOperation(api, serviceContext, tags, false, false, null));
		swaggerPath.setPost(getOperation(api, serviceContext, tags, true, postMultipart, null));

		return swaggerPath;
	}

	private String findName(EntityMdResolver mdResolver) {
		Name nameMd = mdResolver.meta(Name.T).exclusive();
		if (nameMd != null)
			return nameMd.getName().value();

		return null;
	}

	private String findDescription(EntityMdResolver mdResolver) {
		Description descriptionMd = mdResolver.meta(Description.T).exclusive();
		if (descriptionMd != null)
			return descriptionMd.getDescription().value();

		return null;
	}
	
//	private void addSimpleParameter(SwaggerSimpleParameter parameter, SwaggerOperation operation, ModelMdResolver mdResolver) {
//		operation.getParameters().add(parameter);
//	}

	private SwaggerOperation getOperation(SwaggerApi api, RequestCreationContext serviceContext, Collection<String> tags, boolean hasBody,
			boolean isMultipartFormData, SingleDdraMapping mapping) {
		
		String serviceRequestTypeSignature = serviceContext.requestTypeSignature;
		
		EntityMdResolver mdResolver = serviceContext.mdResolver;
		SwaggerOperation operation = SwaggerOperation.T.create();

		String name = findName(mdResolver);
		if (name != null)
			operation.setSummary(name);

		boolean hideSerializedRequest = false;
		if (mapping != null) {
			if (operation.getSummary() == null)
				operation.setSummary(StringTools.prettifyCamelCase(GMCoreTools.getSimpleEntityTypeNameFromTypeSignature(serviceRequestTypeSignature)));

			operation.setDescription("Mapped endpoint for service request: <b>" + serviceRequestTypeSignature + "</b>.");
			
			hideSerializedRequest = mapping.getHideSerializedRequest();
		} else
			operation.setDescription("Generic endpoint for service request: <b>" + serviceRequestTypeSignature + "</b>.");

		String description = findDescription(mdResolver);
		if (description != null)
			operation.setDescription(operation.getDescription() + "<br/>" + description);

		operation.getTags().addAll(tags);

//		EntityTypeOracle o = creationContext.oracle.getEntityTypeOracle(serviceType);
//		boolean isMultipartFormData = o.getProperties().asGmProperties().anyMatch(st -> Resource.T.getTypeSignature().equals(st.getType().getTypeSignature()));
		
		if (hasBody) {
			
			resolveBody(api, serviceContext, operation, isMultipartFormData, hideSerializedRequest);
		} else {
			getSimpleParameters(serviceContext).forEach(operation.getParameters()::add);
		}

		if (!checkIsResourceStreamingRequest(serviceContext.requestGmEntityType))
			handleEndpointParameters(mapping, operation, serviceContext);

		GmType gmType = serviceContext.entityTypeOracle.getEvaluatesTo().get();

		addResponsesToOperation(api, gmType, operation, IGNORED_PROPERTIES_PREDICATE);
		operation.getResponses().put("200",
				get200Response(api, gmType, serviceContext.apiCreationContext.oracle, mapping != null ? mapping.getDefaultProjection() : null, IGNORED_PROPERTIES_PREDICATE));

		return operation;
	}

	private void handleEndpointParameters(SingleDdraMapping mapping, SwaggerOperation operation, RequestCreationContext serviceContext) {
		for (SwaggerParameter param : computeEndpointParameters(serviceContext)) {
			if (mapping != null) {
				String paramName = param.getName().substring(param.getName().lastIndexOf(".") + 1);
				String value = mapping.getDefaultEndpointParameter(paramName);
				if (value != null) {
					SwaggerSimpleParameter p = param.clone(new StandardCloningContext());
					p.setDefault(value);
					operation.getParameters().add(p);
					continue;
				}

				operation.getParameters().add(param);
			} else
				operation.getParameters().add(param);
		}
	}
	private void resolveBody(SwaggerApi api, RequestCreationContext serviceContext, SwaggerOperation operation,
			boolean isMultiPartFormData, boolean hideSerializedRequest) {
		if (isMultiPartFormData || isResourceUploadRequest(serviceContext.requestGmEntityType))
			resolveMultipartFormDataOperation(operation, serviceContext, hideSerializedRequest);
		else {
			SwaggerBodyParameter bodyParameter = getBodyParameter(api, serviceContext.requestGmEntityType, "body", IGNORED_PROPERTIES_PREDICATE);
			if (bodyParameter != null)
				operation.getParameters().add(bodyParameter);
		}
	}

	private boolean checkIsResourceStreamingRequest(GmEntityType serviceType) {
		return isResourceUploadRequest(serviceType) || ResourceDownloadRequest.T.getTypeSignature().equalsIgnoreCase(serviceType.getTypeSignature())
				|| ResourceDeleteRequest.T.getTypeSignature().equalsIgnoreCase(serviceType.getTypeSignature());
	}

	private boolean isResourceUploadRequest(GmEntityType serviceType) {
		return ResourceUploadRequest.T.getTypeSignature().equalsIgnoreCase(serviceType.getTypeSignature());
	}

	private void resolveMultipartFormDataOperation(SwaggerOperation operation, RequestCreationContext serviceContext, boolean hideSerializedRequest) {
		operation.setConsumes(Arrays.asList("multipart/form-data"));

		boolean foundNestedProperty = traverseType(operation, serviceContext, new HashSet<>(), "");
		
		if (foundNestedProperty  && !hideSerializedRequest) {
			SwaggerSimpleParameter rpcRequestParam = getRpcRequestParam();
			operation.getParameters().add(0, rpcRequestParam);
		}
	}
	
	private boolean traverseType(SwaggerOperation operation, RequestCreationContext serviceContext, Set<String> traversedTypeSignatures, String prefix) {
		EntityTypeOracle entityTypeOracle = serviceContext.entityTypeOracle;
		EntityMdResolver entityMdResolver = serviceContext.mdResolver;
		
		Iterable<GmProperty> properties = entityTypeOracle.getProperties()
				.asGmProperties()
				.filter(IGNORED_PROPERTIES_PREDICATE)
				.filter(p -> resolveVisibility(entityMdResolver, p))
				.sorted(propertyComparator(entityMdResolver))
				.collect(Collectors.toList());
		
		boolean foundComplexProperty = false;
		for (GmProperty property: properties) {
			PropertyMdResolver propertyMdResolver = entityMdResolver.property(property);
			GmType propertyType = property.getType();
			
			SwaggerSimpleParameter swaggerParameter = null;
			if (propertyType.isGmScalar()) {
				swaggerParameter = resolveParamForScalarProperty(property, propertyMdResolver);
			}
			else if (!(propertyType instanceof GmMapType) && propertyType.isGmCollection() && ((GmLinearCollectionType)propertyType).getElementType().isGmScalar()) {
				swaggerParameter = resolveParamForCollectionProperty(property, propertyMdResolver);
			} else {
				foundComplexProperty = true;

				String propertyTypeSignature = propertyType.getTypeSignature();
				
				if (propertyTypeSignature.equals(Resource.T.getTypeSignature())) {
					swaggerParameter = resolveFileParamForResource(property, propertyMdResolver);
				} else if (propertyType.isGmEntity() && !traversedTypeSignatures.contains(propertyTypeSignature)) {
					traversedTypeSignatures.add(propertyTypeSignature);
					if (propertyMdResolver.meta(Embedded.T).exclusive() != null) {
						
						EntityTypeOracle embeddedTypeOracle = serviceContext.apiCreationContext.oracle.findEntityTypeOracle(propertyTypeSignature);
						RequestCreationContext embeddedContext = new RequestCreationContext(embeddedTypeOracle, serviceContext.apiCreationContext, serviceContext.ddraMapping);
						
						traverseType(
								operation,
								embeddedContext,
								traversedTypeSignatures,
								prefix + property.getName() + ".");
					}
				}
			}
			
			if (swaggerParameter != null) {
				String prefixedPropertyName = prefix + swaggerParameter.getName();
				swaggerParameter.setName(prefixedPropertyName);
				operation.getParameters().add(swaggerParameter);
			}
		}
		
		return foundComplexProperty;
	}

	private static Comparator<GmProperty> propertyComparator(EntityMdResolver entityMdResolver) {
		Comparator<GmProperty> propertyComparator = Comparator //
				// reversing sign because high numbers should be first
				.comparing((GmProperty p) -> (-1) * resolvePriority(entityMdResolver, p))
				// reversing boolean because true should be < false
				.thenComparing(p -> ! resolveMandatory(entityMdResolver, p)) //
				.thenComparing(GmProperty::getName);
		return propertyComparator;
	}
	
	private SwaggerSimpleParameter getRpcRequestParam() {
		SwaggerSimpleParameter rpcRequestParam = SwaggerSimpleParameter.T.create();
		rpcRequestParam.setIn("formData");
		rpcRequestParam.setRequired(false);
		rpcRequestParam.setName(HttpRequestEntityDecoder.SERIALIZED_REQUEST);
		rpcRequestParam.setType("string");
		rpcRequestParam.setDescription("Service request asssembly.");
		return rpcRequestParam;
	}

	private SwaggerSimpleParameter generateMultipartParameterFor(GmProperty property, PropertyMdResolver propertyMdResolver) {
		SwaggerSimpleParameter param = SwaggerSimpleParameter.T.create();
		param.setIn("formData");
		param.setRequired(propertyMdResolver.is(Mandatory.T));
		param.setName(property.getName());
		
		Description propertyDescription = propertyMdResolver.meta(Description.T).exclusive();
		if (propertyDescription != null)
			param.setDescription(propertyDescription.getDescription().getLocalizedValues().get("default"));
		
		return param;
	}
	
	private SwaggerSimpleParameter resolveFileParamForResource(GmProperty property, PropertyMdResolver propertyMdResolver) {
		SwaggerSimpleParameter fdParam = generateMultipartParameterFor(property, propertyMdResolver);
		fdParam.setType("file");
		
		return fdParam;
	}
	
	private SwaggerSimpleParameter resolveParamForScalarProperty(GmProperty property, PropertyMdResolver propertyMdResolver) {
		SwaggerSimpleParameter fdParam = generateMultipartParameterFor(property, propertyMdResolver);
		fdParam.setType(SwaggerProcessorUtil.getSimpleParameterType(property));
		fdParam.setDefault(getInitializerDefault(property));
	
		return fdParam;
	}
	
	private SwaggerSimpleParameter resolveParamForCollectionProperty(GmProperty property, PropertyMdResolver propertyMdResolver) {
		SwaggerSimpleParameter fdParam = generateMultipartParameterFor(property, propertyMdResolver);
		fdParam.setType("array");
		fdParam.setDefault(getInitializerDefault(property));
		
		GmLinearCollectionType collectionType = (GmLinearCollectionType) property.getType();
		fdParam.setItems(getSimpleItems(collectionType.getElementType()));
		fdParam.setCollectionFormat("multi");
		
		return fdParam;
	}
	
	
	private static double resolvePriority(EntityMdResolver mdResolver, GmProperty property) {
		return resolvePriority(mdResolver, property.getName());
	}
	private static double resolvePriority(EntityMdResolver mdResolver, String propertyName) {
		//@formatter:off
		Priority priorityMd = 
			mdResolver
			.property(propertyName)
			.meta(Priority.T)
			.exclusive();
		//@formatter:on
		
		return (priorityMd != null) ? priorityMd.getPriority() : 0d;
	}

	private static boolean resolveMandatory(EntityMdResolver mdResolver, GmProperty property) {
		return resolveMandatory(mdResolver, property.getName());
	}
	private static boolean resolveMandatory(EntityMdResolver mdResolver, String propertyName) {
		//@formatter:off
		return mdResolver
			.property(propertyName)
			.is(Mandatory.T);
		//@formatter:on
	}

	private boolean resolveVisibility(EntityMdResolver mdResolver, GmProperty property) {
		return resolveVisibility(mdResolver, property.getName());
	}
	private boolean resolveVisibility(EntityMdResolver mdResolver, String propertyName) {
		//@formatter:off
		return mdResolver
			.property(propertyName)
			.is(Visible.T);
		//@formatter:on
	}
	
	private Stream<SwaggerSimpleParameter> getSimpleParameters(RequestCreationContext serviceContext) {
		SwaggerServicesRequest request = serviceContext.apiCreationContext.request;
		EntityMdResolver entityMdResolver = serviceContext.mdResolver;
		Comparator<GmProperty> propertyComparator = propertyComparator(entityMdResolver);
		
		return serviceContext.entityTypeOracle.getProperties().asGmProperties()
				.filter(IGNORED_PROPERTIES_PREDICATE)
				.filter(SwaggerProcessorUtil::isSimpleProperty)
				.filter(p -> resolveVisibility(entityMdResolver, p))
				.sorted(propertyComparator)
				.map(gmProperty -> {
					String name = gmProperty.getName();

					SwaggerSimpleParameter parameter = SwaggerSimpleParameter.T.create();
					parameter.setName(name);

					parameter.setIn("query");

					Description propertyDescription = entityMdResolver.property(gmProperty).meta(Description.T).exclusive();
					if (propertyDescription != null)
						parameter.setDescription(propertyDescription.getDescription().getLocalizedValues().get("default"));

					boolean required = entityMdResolver.property(gmProperty).is(Mandatory.T);
					parameter.setRequired(required);
					parameter.setType(getSimpleParameterType(gmProperty));
					if (name.equals("sessionId")) {
						parameter.setDefault(request.getSessionId());
						parameter.setDescription(PROPERTIES_DESCRIPTIONS.get(name).toString());
					} else {
						parameter.setDefault(getInitializerDefault(gmProperty));
					}
					
					if ("array".equals(parameter.getType())) {
						GmLinearCollectionType collectionType = (GmLinearCollectionType)gmProperty.getType();
						parameter.setItems(getSimpleItems(collectionType.getElementType()));
						parameter.setCollectionFormat("multi");
					}

					return parameter;
				})
				.filter(Objects::nonNull)
				.filter(param -> param.getType() != null);
		//@formatter:on
	}

	private SwaggerResponse get200Response(SwaggerApi api, GmType type, ModelOracle oracle, String projection, Predicate<GmProperty> ignoredPropertiesPredict) {
		SwaggerResponse response = SwaggerResponse.T.create();
		response.setDescription("Service request has been evaluated successfully.");
		response.setSchema(getSchema(api, project(type, oracle, projection), true, ignoredPropertiesPredict));
		return response;
	}

	private GmType project(GmType type, ModelOracle oracle, String projection) {
		if (StringTools.isEmpty(projection) || !GmEntityType.T.isInstance(type))
			return type;

		String[] properties = projection.split(PATTERN_DOT);
		GmType result = type;
		PropertyOracle property;

		for (String propertyName : properties) {
			if (!GmEntityType.T.isInstance(type)) {
				// do not fail the call here, just ignore the projection
				logger.warn("Could not compute projection " + projection + " for result type " + type);
				return type;
			}

			property = oracle.findEntityTypeOracle((GmEntityType)type).findProperty(propertyName);

			if (property == null) {
				// do not fail the call here, just ignore the projection
				logger.warn("Could not compute projection " + projection + " for result type " + type);
				return type;
			}

			result = property.asGmProperty().getType();
		}

		return result;
	}

	@Required
	@Configurable
	public void setMappings(DdraMappings mappings) {
		this.mappings = mappings;
	}

	@Override
	public String getBasePath() {
		return super.getBasePath() != null ? super.getBasePath() : BASE_PATH;
	}

	@Configurable
	public void setMappedPathTag(String mappedPathTag) {
		this.mappedPathTag = mappedPathTag;
	}

	@Configurable
	public void setGenericPathTag(String genericPathTag) {
		this.genericPathTag = genericPathTag;
	}
}
