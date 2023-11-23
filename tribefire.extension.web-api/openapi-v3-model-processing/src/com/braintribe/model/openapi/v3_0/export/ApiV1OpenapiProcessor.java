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
package com.braintribe.model.openapi.v3_0.export;

import static com.braintribe.ddra.MetadataUtils.description;
import static com.braintribe.ddra.MetadataUtils.name;
import static com.braintribe.model.openapi.v3_0.export.OpenapiMimeType.APPLICATION_JSON;
import static com.braintribe.model.openapi.v3_0.export.OpenapiMimeType.MULTIPART_FORMDATA;
import static com.braintribe.model.openapi.v3_0.export.OpenapiMimeType.URLENCODED;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.cfg.Configurable;
import com.braintribe.ddra.MetadataUtils;
import com.braintribe.model.ddra.DdraConfiguration;
import com.braintribe.model.ddra.DdraMapping;
import com.braintribe.model.ddra.DdraUrlMethod;
import com.braintribe.model.ddra.endpoints.api.v1.ApiV1DdraEndpoint;
import com.braintribe.model.generic.mdec.ModelDeclaration;
import com.braintribe.model.generic.reflection.CustomType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.data.mapping.UnsatisfiedBy;
import com.braintribe.model.meta.selector.UseCaseSelector;
import com.braintribe.model.openapi.v3_0.OpenApi;
import com.braintribe.model.openapi.v3_0.OpenapiOperation;
import com.braintribe.model.openapi.v3_0.OpenapiParameter;
import com.braintribe.model.openapi.v3_0.OpenapiPath;
import com.braintribe.model.openapi.v3_0.OpenapiRequestBody;
import com.braintribe.model.openapi.v3_0.OpenapiTag;
import com.braintribe.model.openapi.v3_0.api.OpenapiServicesRequest;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.tools.PreparedTcs;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.service.api.AuthorizedRequest;
import com.braintribe.model.service.api.ServiceRequest;

public class ApiV1OpenapiProcessor extends AbstractOpenapiProcessor<OpenapiServicesRequest> {

	// TODO: enable endpointParameters field again to cache them as they wont change in most cases
	// private List<OpenapiParameter> endpointParameters;
	private DdraConfiguration ddraConfiguration;
	private Set<String> allUseCases;

	private boolean autoUpdateDdraMappings = true;

	@Override
	protected void init() {
		if (ddraConfiguration == null) {
			ddraConfiguration = fetchByGlobalId("ddra:config");
		}

		PersistenceGmSession session = cortexSessionFactory.get();
		EntityQuery query = EntityQueryBuilder.from(UseCaseSelector.T).done();
		List<UseCaseSelector> useCaseSelectors = session.query() //
				.entities(query) //
				.setTraversingCriterion(PreparedTcs.everythingTc) //
				.list();
		allUseCases = useCaseSelectors //
				.stream() //
				.map(UseCaseSelector::getUseCase) //
				.collect(Collectors.toSet());
	}

	@Override
	protected void process(OpenapiContext sessionScopedContext, OpenapiServicesRequest request, OpenApi openApi) {
		Map<String, OpenapiPath> paths = openApi.getPaths();
		Set<String> ddraMapedTypes = newSet();
		Map<String, OpenapiPath> pathStringToPath = new HashMap<>();
		Set<String> encounteredTags = newSet();
		Map<String, String> modelNameToTag = newMap();

		// TODO: optimize this and only refresh if necessary (use DdraMappings)
		if (autoUpdateDdraMappings)
			ddraConfiguration = fetchByGlobalId("ddra:config");

		String modelDescription = description(sessionScopedContext.getMetaData()).atModel();
		if (modelDescription != null) {
			openApi.getInfo().setDescription(modelDescription);
		}

		String modelName = name(sessionScopedContext.getMetaData()).atModel();
		if (modelName != null) {
			openApi.getInfo().setTitle(modelName);
		}

		ComponentScope sessionEndpointScope = new ComponentScope(sessionScopedContext.getComponentScope(),
				standardComponentsContext.getComponentScope().getCmdResolver());
		OpenapiContext endpointParametersResolvingContext = standardComponentsContext.childContext("-ENDPOINTS-SESSION", sessionEndpointScope,
				URLENCODED);
		endpointParametersResolvingContext.transferRequestDataFrom(sessionScopedContext);

		ddraConfiguration.getMappings().stream() //
				.filter(m -> m.getDefaultServiceDomain() != null && m.getDefaultServiceDomain().equals(request.getServiceDomain())) //
				.sorted(Comparator.comparing(DdraMapping::getPath).thenComparing(DdraMapping::getMethod)).forEach(m -> {
					OpenapiPath path = pathStringToPath.computeIfAbsent(m.getPath(), k -> {
						OpenapiPath p = OpenapiPath.T.create();
						return p;
					});

					OpenapiContext pathRequestResolvingContext = pathContext(sessionScopedContext, m.getPath(), m);
					OpenapiContext pathEndpointParametersResolvingContext = pathContext(endpointParametersResolvingContext, m.getPath(), m);

					createAnyOperation(pathRequestResolvingContext, pathEndpointParametersResolvingContext, m, path);

					m.getTags().forEach(encounteredTags::add);

					ddraMapedTypes.add(m.getRequestType().getTypeSignature());

					paths.put(m.getPath(), path);
				});

		// in simple usecase generic endpoints are not shown if there are any explicitly mapped ones
		if (paths.isEmpty() || !isSimpleView(sessionScopedContext)) {
			List<EntityType<?>> requestTypes = modelEntities(sessionScopedContext) //
					.filter(et -> !et.isAbstract()) //
					.filter(et -> ServiceRequest.T.isAssignableFrom(et)) //
					.filter(et -> !ddraMapedTypes.contains(et.getTypeSignature())) //
					.collect(Collectors.toList());
					
			Map<String, List<EntityType<?>>> typesByShortName = requestTypes.stream().collect(Collectors.groupingBy( //
					EntityType::getShortName, //
					Collectors.toList()) //
			);

			requestTypes.stream() //
					.sorted(Comparator //
							.comparing((EntityType<?> t) -> MetadataUtils.priority(sessionScopedContext.getMetaData().entityType(t)).atEntity()) //
							.thenComparing(et -> shortNameIfUniqueOrFullSignature(typesByShortName, et))) //
					.forEach(t -> {
						boolean needsFullName = !shortNameIsUnique(typesByShortName, t);

						String fullKey = "/" + request.getServiceDomain() + getPathKey("", t, "", true);
						String maybeShortKey = "/" + request.getServiceDomain() + getPathKey("", t, "", needsFullName);

						OpenapiContext pathRequestResolvingContext = sessionScopedContext;
						OpenapiContext pathEndpointParametersResolvingContext = endpointParametersResolvingContext;

						if (needsPathSpecificContext(fullKey)) {
							pathRequestResolvingContext = pathContext(sessionScopedContext, fullKey);
							pathEndpointParametersResolvingContext = pathContext(pathEndpointParametersResolvingContext, fullKey);
						}

						String tag = acquireTagName(modelNameToTag, t);
						paths.put(maybeShortKey, createGenericPath(pathRequestResolvingContext, pathEndpointParametersResolvingContext, t, maybeShortKey, tag));
					});
		}

		encounteredTags.stream() //
				.sorted() //
				.forEach(tag -> addNewTag(openApi, tag));

		// We use models from ModelOracle as it lists them in a depender-first order
		ModelOracle modelOracle = sessionScopedContext.getComponentScope().getModelOracle();
		modelOracle.getDependencies() //
				.transitive() //
				.includeSelf() //
				.asGmMetaModels() //
				.map(gmModel -> modelNameToTag.get(gmModel.getName())) //
				.filter(tag -> tag != null) //
				.forEach(tag -> addNewTag(openApi, tag));
	}

	private String shortNameIfUniqueOrFullSignature(Map<String, List<EntityType<?>>> typesBySimpleName, EntityType<?> et) {
		return shortNameIsUnique(typesBySimpleName, et) ? et.getShortName() : et.getTypeSignature();
	}

	private boolean shortNameIsUnique(Map<String, List<EntityType<?>>> typesBySimpleName, EntityType<?> et) {
		List<EntityType<?>> types = typesBySimpleName.get(et.getShortName());
		return types.size() == 1;
	}

	private String acquireTagName(Map<String, String> modelNameToTag, EntityType<?> t) {
		Model m = t.getModel();
		return modelNameToTag.computeIfAbsent(m.name(), n -> {
			ModelDeclaration d = m.getModelArtifactDeclaration();
			return d.getArtifactId() + " ("+d.getGroupId()+")";
		});
	}

	private void addNewTag(OpenApi openApi, String name) {
		OpenapiTag tag = OpenapiTag.T.create();
		tag.setName(name);

		openApi.getTags().add(tag);
	}

	private boolean useCaseExistsFor(String openapiUseCase) {
		return allUseCases.contains("ddra:" + openapiUseCase) || allUseCases.contains("openapi:" + openapiUseCase);
	}

	private boolean needsPathSpecificContext(String path) {
		return useCaseExistsFor(path);
	}

	private OpenapiContext pathContext(OpenapiContext context, String path) {
		return pathContext(context, path, null);
	}

	private OpenapiContext pathContext(OpenapiContext context, String path, DdraMapping mapping) {
		String keySuffix = path.replaceAll("['\"/~]", "_");
		OpenapiContext pathContext = context.childContext(keySuffix);
		pathContext.setUseCasesForDdraAndOpenapi(path);
		pathContext.setMapping(mapping);
		return pathContext;
	}

	private String summaryFromTypeSignature(CustomType requestType) {
		String shortTypeName = requestType.getShortName();

		return shortTypeName.replaceAll("([A-Z])", " $1").substring(1);
	}

	private void createAnyOperation(OpenapiContext requestResolvingContext, OpenapiContext endpointParametersResolvingContext, DdraMapping mapping,
			OpenapiPath path) {
		EntityType<?> requestType = mapping.getRequestType().reflectionType();
		String keySuffix = "-" + mapping.getMethod();
		OpenapiContext context = requestResolvingContext.childContext(keySuffix);
		GenericModelType responseType = context.getEntityTypeOracle(requestType).getEvaluatesTo().get().reflectionType();
		boolean isAuthorizedRequest = AuthorizedRequest.T.isAssignableFrom(requestType);

		List<EntityType<?>> potentialReasonTypes = collectPotentialReasons(requestType, context);
		OpenapiOperation operation = createOperation(context, responseType, isAuthorizedRequest, potentialReasonTypes);

		EntityMdResolver requestTypeMdResolver = requestResolvingContext.getMetaData().entityType(requestType);
		String requestEntityDescription = description(requestTypeMdResolver).atEntity();
		String requestEntityName = MetadataUtils.name(requestTypeMdResolver).atEntity();

		String description = "Mapped endpoint for <b>" + requestType.getTypeSignature() + "</b><br>";

		if (requestEntityDescription != null)
			description += requestEntityDescription;

		String summary = requestEntityName;

		if (summary == null) {
			summary = summaryFromTypeSignature(requestType);
		}

		operation.setDescription(description);
		operation.setSummary(summary);

		switch (mapping.getMethod()) {
			case GET:
				createGetOperation(operation, requestResolvingContext, mapping);
				path.setGet(operation);
				break;
			case DELETE:
				createDeleteOperation(operation, requestResolvingContext, mapping);
				path.setDelete(operation);
				break;
			case POST:
				createPostOperation(operation, requestResolvingContext, mapping);
				path.setPost(operation);
				break;
			case PUT:
				createPutOperation(operation, requestResolvingContext, mapping);
				path.setPut(operation);
				break;
			case PATCH:
				createPatchOperation(operation, requestResolvingContext, mapping);
				path.setPatch(operation);
				break;

			default:
				throw new IllegalArgumentException("Method not supported: " + mapping.getMethod());
		}

		List<OpenapiParameter> endpointParameters = getQueryParameterRefs(ApiV1DdraEndpoint.T, endpointParametersResolvingContext, "endpoint");
		operation.getParameters().addAll(endpointParameters);
	}

	private List<EntityType<?>> collectPotentialReasons(EntityType<?> requestType, OpenapiContext context) {
		//@formatter:off
		List<UnsatisfiedBy> unsatisfiedBys = 
				context
				.getMetaData()
				.entityType(requestType)
				.meta(UnsatisfiedBy.T)
				.list();
		return unsatisfiedBys
			.stream()
			.map(UnsatisfiedBy::getReasonType)
			.map(GmEntityType::getTypeSignature)
			.map(EntityTypes::get)
			.collect(Collectors.toList());
		//@formatter:on

	}

	private void createPostOperation(OpenapiOperation operation, OpenapiContext context, DdraMapping mapping) {
		boolean isMultipart = mapping.getAnnounceAsMultipart() == null ? true : mapping.getAnnounceAsMultipart();

		createOperationWithBody(operation, context, mapping, isMultipart);
	}

	private void createPutOperation(OpenapiOperation operation, OpenapiContext context, DdraMapping mapping) {
		boolean isMultipart = mapping.getAnnounceAsMultipart() == null ? false : mapping.getAnnounceAsMultipart();

		createOperationWithBody(operation, context, mapping, isMultipart);
	}

	private void createPatchOperation(OpenapiOperation operation, OpenapiContext context, DdraMapping mapping) {
		boolean isMultipart = mapping.getAnnounceAsMultipart() == null ? false : mapping.getAnnounceAsMultipart();

		createOperationWithBody(operation, context, mapping, isMultipart);
	}

	private void createOperationWithBody(OpenapiOperation operation, OpenapiContext context, DdraMapping mapping, boolean isMultipart) {
		EntityType<?> requestType = mapping.getRequestType().reflectionType();

		OpenapiMimeType[] mimeTypes;

		if (isMultipart) {
			mimeTypes = new OpenapiMimeType[] { MULTIPART_FORMDATA, URLENCODED, APPLICATION_JSON };
		} else {
			mimeTypes = ALL_MIME_TYPES;
		}

		OpenapiRequestBody reqestBody = context.components().requestBody(requestType) //
				.ensure(currentContext -> {
					OpenapiRequestBody b = OpenapiRequestBody.T.create();
					b.setContent(createContent(requestType, currentContext, mimeTypes));
					b.setDescription("Serialized " + requestType.getTypeSignature());
					return b;
				}) //
				.getRef();

		operation.setRequestBody(reqestBody);

		operation.getTags().addAll(mapping.getTags());

	}

	private void createGetOperation(OpenapiOperation operation, OpenapiContext context, DdraMapping mapping) {
		createOperationWithoutBody(operation, context, mapping);
	}

	private void createDeleteOperation(OpenapiOperation operation, OpenapiContext context, DdraMapping mapping) {
		createOperationWithoutBody(operation, context, mapping);
	}

	private void createOperationWithoutBody(OpenapiOperation operation, OpenapiContext context, DdraMapping mapping) {
		EntityType<?> requestType = mapping.getRequestType().reflectionType();

		OpenapiContext urlencodedContext = context.childContext(OpenapiMimeType.URLENCODED);

		List<OpenapiParameter> requestParameters = getQueryParameterRefs(requestType, urlencodedContext, null);

		operation.getParameters().addAll(requestParameters);

		operation.getTags().addAll(mapping.getTags());
	}

	private void createGenericEndpointOperation(DdraUrlMethod method, OpenapiPath path, OpenapiContext context,
			OpenapiContext endpointParametersResolvingContext, EntityType<?> requestType, String pathString, String tag) {

		DdraMapping mapping = createGenericMapping(context, requestType);
		mapping.setMethod(method);
		mapping.setPath(pathString);
		mapping.getTags().add(tag);

		createAnyOperation(context, endpointParametersResolvingContext, mapping, path);
	}

	private DdraMapping createGenericMapping(OpenapiContext context, EntityType<?> requestType) {
		DdraMapping mapping = DdraMapping.T.create();

		GmEntityType asGmEntityType = context.getEntityTypeOracle(requestType).asGmEntityType();
		mapping.setRequestType(asGmEntityType);

		context.setMapping(mapping);
		return mapping;
	}

	private OpenapiPath createGenericPath(OpenapiContext requestResolvingContext, OpenapiContext endpointParametersResolvingContext, EntityType<?> t,
			String pathString, String tag) {
		OpenapiPath path = OpenapiPath.T.create();
		createGenericEndpointOperation(DdraUrlMethod.GET, path, requestResolvingContext, endpointParametersResolvingContext, t, pathString, tag);
		createGenericEndpointOperation(DdraUrlMethod.POST, path, requestResolvingContext, endpointParametersResolvingContext, t, pathString, tag);

		return path;
	}

	private OpenapiOperation createOperation(OpenapiContext context, GenericModelType responseType, boolean isAuthorizedRequest,
			List<EntityType<?>> potentialReasonTypes) {
		OpenapiOperation operation = OpenapiOperation.T.create();
		addResponsesToOperation(responseType, operation, context, isAuthorizedRequest, potentialReasonTypes);
		return operation;
	}

	@Override
	protected String getTitle(ServiceRequestContext requestContext, OpenapiServicesRequest request) {
		return "Service Requests in " + request.getServiceDomain();
	}

	@Override
	protected ModelAccessory getModelAccessory(ServiceRequestContext requestContext, OpenapiServicesRequest request) {
		return modelAccessoryFactory.getForServiceDomain(request.getServiceDomain());
	}

	@Override
	protected String getBasePath(ServiceRequestContext requestContext, OpenapiServicesRequest request) {
		String tribefireServicesUrl = request.getTribefireServicesUrl();

		if (tribefireServicesUrl == null) {
			tribefireServicesUrl = TribefireRuntime.getPublicServicesUrl();
		}

		return tribefireServicesUrl + "/api/v1/";
	}

	@Configurable
	public void setDdraConfiguration(DdraConfiguration ddraConfiguration) {
		this.ddraConfiguration = ddraConfiguration;
	}

	// TODO: Remove ASAP and implement a clean solution
	@Deprecated
	public void setAutoUpdateDdraMappings(boolean autoUpdateDdraMappings) {
		this.autoUpdateDdraMappings = autoUpdateDdraMappings;
	}
}
