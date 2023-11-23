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
// ============================================================================

// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

import static com.braintribe.ddra.MetadataUtils.description;
import static com.braintribe.ddra.MetadataUtils.isMandatory;
import static com.braintribe.ddra.MetadataUtils.isVisible;
import static com.braintribe.ddra.MetadataUtils.propertyComparator;
import static com.braintribe.model.openapi.v3_0.export.OpenapiMimeType.ALL;
import static com.braintribe.model.openapi.v3_0.export.OpenapiMimeType.APPLICATION_JSON;
import static com.braintribe.model.openapi.v3_0.export.OpenapiUtils.USECASE_OPENAPI_SIMPLE;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.cfg.Required;
import com.braintribe.common.attribute.AttributeContextBuilder;
import com.braintribe.common.lcd.NotImplementedException;
import com.braintribe.common.lcd.function.TriFunction;
import com.braintribe.ddra.MetadataUtils;
import com.braintribe.ddra.TypeTraversal;
import com.braintribe.ddra.TypeTraversalResult;
import com.braintribe.exception.AuthorizationException;
import com.braintribe.gm.model.reason.meta.HttpStatusCode;
import com.braintribe.logging.Logger;
import com.braintribe.model.ddra.DdraMapping;
import com.braintribe.model.ddra.endpoints.v2.RestV2Endpoint;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.CollectionType.CollectionKind;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.LinearCollectionType;
import com.braintribe.model.generic.reflection.ListType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SetType;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.generic.value.EnumReference;
import com.braintribe.model.generic.value.ValueDescriptor;
import com.braintribe.model.meta.data.constraint.Max;
import com.braintribe.model.meta.data.constraint.MaxLength;
import com.braintribe.model.meta.data.constraint.Min;
import com.braintribe.model.meta.data.constraint.MinLength;
import com.braintribe.model.meta.data.prompt.Confidential;
import com.braintribe.model.meta.data.prompt.Description;
import com.braintribe.model.openapi.v3_0.OpenApi;
import com.braintribe.model.openapi.v3_0.OpenapiComponents;
import com.braintribe.model.openapi.v3_0.OpenapiFormat;
import com.braintribe.model.openapi.v3_0.OpenapiInfo;
import com.braintribe.model.openapi.v3_0.OpenapiMediaType;
import com.braintribe.model.openapi.v3_0.OpenapiOperation;
import com.braintribe.model.openapi.v3_0.OpenapiParameter;
import com.braintribe.model.openapi.v3_0.OpenapiResponse;
import com.braintribe.model.openapi.v3_0.OpenapiSchema;
import com.braintribe.model.openapi.v3_0.OpenapiServer;
import com.braintribe.model.openapi.v3_0.OpenapiType;
import com.braintribe.model.openapi.v3_0.api.OpenapiRequest;
import com.braintribe.model.openapi.v3_0.export.attributes.CurrentSessionIdAttribute;
import com.braintribe.model.openapi.v3_0.export.attributes.ReflectSubtypesAttribute;
import com.braintribe.model.openapi.v3_0.export.attributes.ReflectSupertypesAttribute;
import com.braintribe.model.openapi.v3_0.reference.CantBuildReferenceException;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.EnumMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.meta.oracle.EntityTypeProperties;
import com.braintribe.model.processing.meta.oracle.ModelTypes;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.tools.PreparedTcs;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.common.context.UserSessionAspect;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.vde.evaluator.VDE;
import com.braintribe.model.processing.web.rest.impl.HttpRequestEntityDecoderImpl;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.AuthorizableRequest;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.model.user.User;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.utils.DateTools;

public abstract class AbstractOpenapiProcessor<R extends OpenapiRequest> implements ServiceProcessor<R, OpenApi> {

	protected static final OpenapiMimeType[] ALL_MIME_TYPES = new OpenapiMimeType[] { APPLICATION_JSON, ALL };

	protected Logger logger = Logger.getLogger(getClass());

	public static final String ALL_MEDIA_TYPES_RANGE = "*/*";
	protected static final String PATTERN_DOT = Pattern.quote(".");
	private static final String OPENAPI_VERSION = "3.0.1";

	protected final EntityType<?> failureType = Failure.T;
	private boolean wasInitialized;
	protected OpenapiContext standardComponentsContext;

	protected Supplier<PersistenceGmSession> cortexSessionFactory;
	protected ModelAccessoryFactory modelAccessoryFactory;

	private OpenapiResponse standardResponse400;
	private OpenapiResponse standardResponse401;
	private OpenapiResponse standardResponse404;
	private OpenapiResponse standardResponse500;

	private OpenApi createNewApi(String title, String basePath, String documentVersion) {
		OpenApi api = OpenApi.T.create();
		api.setInfo(createInfoObject(title, documentVersion));
		api.setPaths(new LinkedHashMap<>());
		api.setOpenapi(OPENAPI_VERSION);
		api.setServers(Arrays.asList(createServerObject(basePath)));
		api.setComponents(OpenapiComponents.T.create());

		return api;
	}

	protected abstract void init();
	protected abstract ModelAccessory getModelAccessory(ServiceRequestContext requestContext, R request);
	protected abstract void process(OpenapiContext sessionScopedContext, R request, OpenApi api);
	protected abstract String getTitle(ServiceRequestContext requestContext, R request);
	protected abstract String getBasePath(ServiceRequestContext requestContext, R request);

	@Override
	public OpenApi process(ServiceRequestContext requestContext, R request) {
		try {
			if (!wasInitialized) {
				synchronized (this) {
					if (!wasInitialized) {
						createStandardComponents();
						init();
						standardComponentsContext.seal();
						wasInitialized = true;
					}
				}
			}

			String title = getTitle(requestContext, request);

			String loggedInUserName = requestContext.getAspect(UserSessionAspect.class) //
					.map(UserSession::getUser) //
					.map(User::getName) //
					.orElse(null);

			if (loggedInUserName != null)
				title += " for User '" + loggedInUserName + "'";

			String basePath = getBasePath(requestContext, request);

			ModelAccessory modelAccessory = getModelAccessory(requestContext, request);

			if (!isVisible(modelAccessory.getCmdResolver().getMetaData()).atModel()) {
				throw new AuthorizationException("Current user is not authorized to retrieve information about '" + title + "'");
			}

			String version = modelAccessory.getModel().getVersion();
			OpenApi api = createNewApi(title, basePath, version);
			OpenapiContext sessionScopedContext = createSessionScopedContext(request, modelAccessory, api);

			process(sessionScopedContext, request, api);

			return api;
		} catch (CantBuildReferenceException e) {
			throw new IllegalStateException("Could not generate a valid OpenAPI document", e);
		}
	}

	private OpenapiContext createSessionScopedContext(R request, ModelAccessory modelAccessory, OpenApi api) {
		OpenapiComponents components = api.getComponents();

		ComponentScope sessionScope = new ComponentScope(components, modelAccessory.getCmdResolver());
		sessionScope.transferFrom(standardComponentsContext.getComponentScope());

		Set<String> useCases = request.getUseCases();

		AttributeContextBuilder attributes = standardComponentsContext.getAttributes().derive() //
				.set(ReflectSubtypesAttribute.class, request.getReflectSubtypes())
				.set(ReflectSupertypesAttribute.class, request.getReflectSupertypes());

		if (request.getIncludeSessionId()) {
			attributes.set(CurrentSessionIdAttribute.class, request.getSessionId());
			useCases.add(ComponentScope.USECASE_INCLUDE_SESSION_ID);
		}

		OpenapiContext sessionScopedContext = standardComponentsContext.childContext(null, sessionScope, attributes.build());
		sessionScopedContext.addUseCases(useCases);
		return sessionScopedContext;
	}

	private void createStandardComponents() {
		OpenapiComponents standardComponents = OpenapiComponents.T.create();
		ComponentScope standardComponentsScope = new ComponentScope(standardComponents,
				modelAccessoryFactory.getForAccess("cortex").getCmdResolver());

		Set<String> useCases = new HashSet<>();
		useCases.add(ComponentScope.USECASE_DDRA);
		useCases.add(ComponentScope.USECASE_OPENAPI);

		standardComponentsContext = OpenapiContext.create("BASE", standardComponentsScope, OpenapiMimeType.ALL);
		standardComponentsContext.addUseCases(useCases);

		standardResponse400 = registerStandardResponse("400", failureType, "Invalid input was given.");
		standardResponse401 = registerStandardResponse("401", failureType, "sessionId parameter was missing or invalid.");
		standardResponse404 = registerStandardResponse("404", failureType, "Resource not found.");
		standardResponse500 = registerStandardResponse("500", failureType, "An error occurred when processing the request.");
	}

	protected OpenapiServer createServerObject(String basePath) {
		OpenapiServer server = OpenapiServer.T.create();
		server.setUrl(basePath);

		return server;
	}

	protected String getPathKey(String uriService, EntityType<?> serviceType, String pathParams, boolean useFullyQualifiedDefinitionName) {
		String typeSignature = useFullyQualifiedDefinitionName ? serviceType.getTypeSignature() : serviceType.getShortName();

		return "/" + uriService + typeSignature + pathParams;
	}

	private OpenapiInfo createInfoObject(String title, String documentVersion) {
		OpenapiInfo info = OpenapiInfo.T.create();
		info.setDescription("Learn about and try out the APIs for " + title
				+ ". <br/> <a target=\'_blank' href=\'https://documentation.tribefire.com/tribefire.cortex.documentation/api-doc/quick_start_rest.html'>Click here to learn about our REST API in general</a>.");
		info.setTitle(title);
		info.setVersion(documentVersion);

		return info;
	}

	protected <T> T fetchByGlobalId(String globalId) {
		PersistenceGmSession cortexSession = cortexSessionFactory.get();
		EntityQuery query = EntityQueryBuilder.from(GenericEntity.T).where().property(GenericEntity.globalId).eq(globalId).done();

		T fetched = cortexSession.query().entities(query).setTraversingCriterion(PreparedTcs.everythingTc).unique();

		return fetched;
	}

	protected static Object getInitializerDefault(Property property) {
		if (property.getInitializer() == null) {
			if (property.getDefaultRawValue() != null) {
				return property.getDefaultRawValue();
			}

			return null;
		}

		Object initializer = property.getInitializer();
		if (initializer instanceof ValueDescriptor) {
			// A VD is configured as initializer. Resolve it first.
			Object evaluatedInitValue = VDE.evaluate(initializer);
			initializer = evaluatedInitValue;
		}

		// Special handling in case initializer value is an enumReference or date.
		if (property.getType().isEnum()) {// initializer instanceof EnumReference) {
			EnumReference ref = (EnumReference) property.getInitializer();
			initializer = ref.getConstant();
		} else if (initializer instanceof Date) {
			DateTimeFormatter dateFormatter = DateTools.ISO8601_DATE_WITH_MS_FORMAT; // Standard Date Format for OpenAPI
			Date date = (Date) initializer;
			ZonedDateTime dateTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC);
			initializer = dateFormatter.format(dateTime);
		}
		return initializer;
	}

	protected OpenapiResponse createResponse(GenericModelType type, String description, OpenapiContext context) {
		OpenapiResponse response = OpenapiResponse.T.create();
		response.setDescription(description);

		response.setContent(createContent(type, context));
		return response;
	}

	protected Map<String, OpenapiMediaType> createContent(GenericModelType type, OpenapiContext context, OpenapiMimeType... mimes) {
		Map<String, OpenapiMediaType> content = new LinkedHashMap<>();

		for (OpenapiMimeType mime : mimes) {
			OpenapiContext mimeTypeContext = context.childContext(mime);
			OpenapiMediaType mediaType = OpenapiMediaType.T.create();
			mediaType.setSchema(getSchema(type, mimeTypeContext));

			content.put(mime.getMimeString(), mediaType);
		}

		return content;
	}

	protected Map<String, OpenapiMediaType> createContent(GenericModelType type, OpenapiContext context) {
		return createContent(type, context, ALL_MIME_TYPES);
	}

	protected OpenapiSchema getPropertySchema(Property property, OpenapiContext context, String description, EntityMdResolver entityMdResolver) {
		OpenapiSchema valueTypeSchema = getSchema(property.getType(), context);
		PropertyMdResolver propertyMdResolver = entityMdResolver.property(property);
		if (valueTypeSchema.getFormat() == null) {
			boolean isConfidential = propertyMdResolver.is(Confidential.T);
			if (isConfidential) {
				valueTypeSchema.setFormat(OpenapiFormat.PASSWORD);
			}
		}

		if (valueTypeSchema.getType() == OpenapiType.INTEGER || valueTypeSchema.getType() == OpenapiType.NUMBER) {
			Min minimumMd = propertyMdResolver.meta(Min.T).exclusive();
			if (minimumMd != null) {
				Object minimumValue = minimumMd.getLimit();
				if (minimumValue instanceof Number) {
					valueTypeSchema.setMinimum(minimumValue);
					valueTypeSchema.setExclusivMaximum(minimumMd.getExclusive());
				}
			}

			Max maximumMd = propertyMdResolver.meta(Max.T).exclusive();
			if (maximumMd != null) {
				Object maximumValue = maximumMd.getLimit();
				if (maximumValue instanceof Number) {
					valueTypeSchema.setMaximum(maximumValue);
					valueTypeSchema.setExclusivMaximum(maximumMd.getExclusive());
				}
			}

		} else if (valueTypeSchema.getType() == OpenapiType.STRING) {
			MinLength minLength = propertyMdResolver.meta(MinLength.T).exclusive();
			if (minLength != null) {
				valueTypeSchema.setMinLength(minLength.getLength());
			}
			MaxLength maxLength = propertyMdResolver.meta(MaxLength.T).exclusive();
			if (maxLength != null) {
				valueTypeSchema.setMaxLength(maxLength.getLength());
			}

		} else if (valueTypeSchema.getType() == OpenapiType.ARRAY) {
			MinLength minLength = propertyMdResolver.meta(MinLength.T).exclusive();
			if (minLength != null) {
				valueTypeSchema.setMinItems(minLength.getLength());
			}
			MaxLength maxLength = propertyMdResolver.meta(MaxLength.T).exclusive();
			if (maxLength != null) {
				valueTypeSchema.setMaxItems(maxLength.getLength());
			}

		}

		Object defaultValue = getDefaultValue(property, context);
		if (defaultValue == null && description == null) {
			return valueTypeSchema;
		}

		OpenapiSchema propertySchema = valueTypeSchema;

		if (property.getType().isEntity()) {
			propertySchema = OpenapiSchema.T.create();
			propertySchema.getAllOf().add(valueTypeSchema);
		} else if (property.getType().isEnum()) {
			propertySchema = createEnumTypeSchema((EnumType) property.getType());
		}

		if (propertySchema.get$ref() != null) {
			throw new NotImplementedException(
					"Referencing non-enum or -entity-type in property type. There are other types of referencable schemas?");
		}

		propertySchema.setDefault(defaultValue);
		propertySchema.setDescription(description);

		return propertySchema;
	}

	private Object getDefaultValue(Property property, OpenapiContext context) {
		if (!property.getType().isScalar())
			return null;

		if (isSessionId(property)) {
			return context.getAttributes().findOrNull(CurrentSessionIdAttribute.class);
		}

		return getInitializerDefault(property);
	}

	private static boolean isSessionId(Property property) {
		return (property.getDeclaringType() == AuthorizableRequest.T || property.getDeclaringType() == RestV2Endpoint.T) //
				&& property.getName().equals(AuthorizableRequest.sessionId);
	}

	protected OpenapiSchema getSchema(GenericModelType type, OpenapiContext context) {
		OpenapiSchema schema = OpenapiSchema.T.create();

		switch (type.getTypeCode()) {
			case objectType:
				schema.setTitle(type.getTypeSignature());
				schema.setType(OpenapiType.OBJECT);
				break;
			case booleanType:
				schema.setType(OpenapiType.BOOLEAN);
				break;
			case doubleType:
				schema.setType(OpenapiType.NUMBER);
				schema.setFormat(OpenapiFormat.DOUBLE);
				break;
			case entityType:
				schema = ensureEntityTypeDefinitions((EntityType<?>) type, context);
				break;
			case floatType:
				schema.setType(OpenapiType.NUMBER);
				schema.setFormat(OpenapiFormat.FLOAT);
				break;
			case integerType:
				schema.setType(OpenapiType.INTEGER);
				schema.setFormat(OpenapiFormat.INT32);
				break;
			case longType:
				schema.setType(OpenapiType.INTEGER);
				schema.setFormat(OpenapiFormat.INT64);
				break;
			case listType:
				schema.setTitle("list");
				schema.setType(OpenapiType.ARRAY);
				ListType listType = (ListType) type;
				schema.setItems(getSchema(listType.getCollectionElementType(), context));
				break;
			case setType:
				schema.setTitle("set");
				schema.setType(OpenapiType.ARRAY);
				schema.setUniqueItems(true);
				SetType setType = (SetType) type;
				schema.setItems(getSchema(setType.getCollectionElementType(), context));
				break;
			case stringType:
				schema.setType(OpenapiType.STRING);
				break;
			case enumType:
				EnumType enumType = (EnumType) type;
				schema = ensureEnumTypeDefinition(context, enumType);
				break;
			case dateType:
				schema.setTitle("date");
				schema.setType(OpenapiType.STRING);
				schema.setFormat(OpenapiFormat.DATE_TIME);
				break;
			case decimalType:
				schema.setTitle("decimal");
				schema.setType(OpenapiType.NUMBER); // TODO: Check if it shouldn't be String
				break;
			case mapType:
				schema.setTitle("map");
				schema.setType(OpenapiType.OBJECT);
				MapType mapType = (MapType) type;
				if (mapType.getKeyType().isSimple()) {
					schema.setAdditionalProperties(getSchema(mapType.getValueType(), context));
				} else {
					OpenapiSchema valueSchema = OpenapiSchema.T.create();
					schema.getProperties().put("value", valueSchema);

					valueSchema.setType(OpenapiType.ARRAY);

					OpenapiSchema entriesSchema = OpenapiSchema.T.create();
					valueSchema.setItems(entriesSchema);

					entriesSchema.setType(OpenapiType.OBJECT);
					entriesSchema.getProperties().put("key", getSchema(mapType.getKeyType(), context));
					entriesSchema.getProperties().put("value", getSchema(mapType.getValueType(), context));
				}
				break;

			default:
				break;
		}

		return schema;
	}

	private OpenapiSchema ensureEnumTypeDefinition(OpenapiContext context, EnumType enumType) {
		return context.components().schema(enumType) //
				.ensure(currentContext -> {
					EnumMdResolver enumMdResolver = currentContext.getMetaData().enumType(enumType);

					OpenapiSchema schema = createEnumTypeSchema(enumType);
					schema.setDescription(description(enumMdResolver).atEnum());

					return schema;
				}) //
				.getRef();
	}

	private OpenapiSchema createEnumTypeSchema(EnumType enumType) {
		OpenapiSchema schema = OpenapiSchema.T.create();
		schema.setType(OpenapiType.STRING);
		schema.setTitle(enumType.getTypeSignature());

		Stream.of(enumType.getEnumValues()).map(constant -> constant.name()).forEach(schema.getEnum()::add);

		return schema;
	}

	public OpenapiSchema ensureEntityTypeDefinitions(EntityType<?> entityType, OpenapiContext context) {
		return context.components().schema(entityType) //
				.ensure(currentContext -> {
					OpenapiSchema entityTypeSchema = OpenapiSchema.T.create();
					fillEntityTypeSchema(entityTypeSchema, entityType, currentContext);

					if (!reflectPolymorphy(context))
						return entityTypeSchema;

					OpenapiSchema polymorphicSchema = OpenapiSchema.T.create();

					currentContext.getEntityTypeOracle(entityType) //
							.getSubTypes() //
							.asEntityTypeOracles() //
							.stream() //
							.flatMap(AbstractOpenapiProcessor::firstInstantiableSubtypes) //
							.map(e -> ensureEntityTypeDefinitions(e, currentContext)) //
							.forEach(polymorphicSchema.getOneOf()::add);

					if (polymorphicSchema.getOneOf().isEmpty())
						return entityTypeSchema;

					polymorphicSchema.setDescription(entityTypeSchema.getDescription());
					polymorphicSchema.setType(entityTypeSchema.getType());
					polymorphicSchema.setTitle(entityTypeSchema.getTitle());
					polymorphicSchema.getOneOf().add(entityTypeSchema);

					return polymorphicSchema;
				}) //
				.getRef();
	}

	private static Stream<EntityType<?>> firstInstantiableSubtypes(EntityTypeOracle entityTypeOracle) {
		if (!entityTypeOracle.asGmEntityType().getIsAbstract())
			return Stream.of(entityTypeOracle.asType());

		return entityTypeOracle.getSubTypes().asEntityTypeOracles().stream().flatMap(AbstractOpenapiProcessor::firstInstantiableSubtypes);
	}

	private boolean reflectPolymorphy(OpenapiContext context) {
		return context.getAttributes().findOrDefault(ReflectSubtypesAttribute.class, false);
	}

	private boolean reflectTypeHierarchy(OpenapiContext context) {
		return context.getAttributes().findOrDefault(ReflectSupertypesAttribute.class, false);
	}

	private void fillEntityTypeSchema(OpenapiSchema schema, EntityType<?> entityType, OpenapiContext context) {
		String title = entityType.getTypeSignature();
		EntityMdResolver entityMdResolver = context.getMetaData().entityType(entityType);

		schema.setTitle(title);
		schema.setType(OpenapiType.OBJECT);
		schema.setDescription(description(entityMdResolver).atEntity());

		if (reflectTypeHierarchy(context))
			context.getEntityTypeOracle(entityType) //
					.getSuperTypes().asTypes() //
					.stream() //
					.map(e -> ensureEntityTypeDefinitions((EntityType<?>) e, context)) //
					.forEach(schema.getAllOf()::add);

		if (context.getMimeType() == OpenapiMimeType.MULTIPART_FORMDATA) {
			fillEntityTypeSchemaForMultipart(entityType, schema, context);
		} else if (context.getMimeType() == OpenapiMimeType.URLENCODED) {
			fillEntityTypeSchemaForUrlencoded(entityType, schema, context);
		} else {
			fillEntityTypeSchemaGeneral(entityType, schema, context);
		}

	}

	private void fillPropertySchema(EntityMdResolver entityMdResolver, Property property, OpenapiSchema schema, OpenapiContext context) {
		fillPropertySchema(entityMdResolver, property, property.getName(), schema, context);
	}

	private void fillPropertySchema(EntityMdResolver entityMdResolver, Property property, String propertyName, OpenapiSchema schema,
			OpenapiContext context) {
		String description = description(entityMdResolver).atProperty(property);
		OpenapiSchema basicPropertySchema = getPropertySchema(property, context, description, entityMdResolver);

		schema.getProperties().put(propertyName, basicPropertySchema);
		if (isMandatory(entityMdResolver).atProperty(property)) {
			schema.getRequired().add(propertyName);
		}
	}

	private void fillEntityTypeSchemaGeneral(EntityType<?> entityType, OpenapiSchema schema, OpenapiContext context) {
		EntityMdResolver entityMdResolver = context.getMetaData().entityType(entityType);

		entityTypeProperties(entityType, context) //
				.filter(p -> isVisible(entityMdResolver).atProperty(p)) //
				.sorted(propertyComparator(entityMdResolver)) //
				.forEach(property -> fillPropertySchema(entityMdResolver, property, schema, context));
	}

	private Stream<Property> entityTypeProperties(EntityType<?> entityType, OpenapiContext context) {
		EntityTypeOracle entityTypeOracle = context.getEntityTypeOracle(entityType);
		EntityTypeProperties properties = entityTypeOracle.getProperties();

		if (reflectTypeHierarchy(context))
			properties = properties.onlyDeclared();

		return properties.asProperties();
	}

	private void fillEntityTypeSchemaForUrlencoded(EntityType<?> entityType, OpenapiSchema schema, OpenapiContext context) {
		EntityMdResolver entityMdResolver = context.getMetaData().entityType(entityType);

		entityTypeProperties(entityType, context) //
				.filter(p -> isVisible(entityMdResolver).atProperty(p)) //
				.filter(p -> urlencodedPropertyTypeFilter(p.getType())) //
				.sorted(propertyComparator(entityMdResolver)) //
				.forEach(property -> fillPropertySchema(entityMdResolver, property, schema, context));
	}

	private void fillEntityTypeSchemaForMultipart(EntityType<?> entityType, OpenapiSchema schema, OpenapiContext context) {
		if (entityType == Resource.T) {
			schema.setType(OpenapiType.STRING);
			schema.setFormat(OpenapiFormat.BINARY);
			return;
		}

		DdraMapping mapping = context.getMapping();
		if (!isSimpleView(context) || (mapping != null && !mapping.getHideSerializedRequest())) {
			OpenapiSchema serializedRequestSchema = OpenapiSchema.T.create();
			serializedRequestSchema.setType(OpenapiType.OBJECT);
			serializedRequestSchema.setTitle("object");
			schema.getProperties().put(HttpRequestEntityDecoderImpl.SERIALIZED_REQUEST, serializedRequestSchema);
		}

		TypeTraversal.traverseType(context.getMetaData(), entityType).stream()
				.filter(r -> (!reflectTypeHierarchy(context) || r.getProperty().getDeclaringType() == entityType)
						&& multipartPropertyTypeFilter(r.getProperty().getType())) //
				.forEach(r -> fillPropertySchema(context.getMetaData().entityType(r.getEntityType()), r.getProperty(), r.prefixedPropertyName(),
						schema, context));

	}

	private static boolean multipartPropertyTypeFilter(GenericModelType propertyType) {
		if (propertyType.isScalar() || propertyType.isBase()) {
			return true;
		}

		if (propertyType.getTypeCode() == TypeCode.setType || propertyType.getTypeCode() == TypeCode.listType) {
			GenericModelType collectionElementType = ((LinearCollectionType) propertyType).getCollectionElementType();

			return multipartPropertyTypeFilter(collectionElementType);
		}

		return Resource.T.isAssignableFrom(propertyType);
	}

	private static boolean urlencodedPropertyTypeFilter(GenericModelType propertyType) {
		if (propertyType.isScalar() || propertyType.isBase()) {
			return true;
		}

		if (propertyType.getTypeCode() == TypeCode.setType || propertyType.getTypeCode() == TypeCode.listType) {
			GenericModelType collectionElementType = ((LinearCollectionType) propertyType).getCollectionElementType();

			return urlencodedPropertyTypeFilter(collectionElementType);
		}

		return false;
	}

	private boolean isValidQueryParam(Property p) {
		if (p.getType().isCollection()) {
			CollectionType collectionType = (CollectionType) p.getType();

			if (collectionType.getCollectionKind() == CollectionKind.map || !collectionType.hasSimpleOrEnumContent()) {
				return false;
			}
		} else if (!p.getType().isScalar() && !p.getType().isBase()) {
			return false;
		}

		return true;
	}

	public List<OpenapiParameter> getQueryParameterRefs(EntityType<?> entityType, OpenapiContext context, String prefix) {
		TriFunction<TypeTraversalResult, Property, EntityType<?>, TypeTraversalResult> resultFactory = (parent, property, e) -> //
		parent == null //
				? new TypeTraversalResult(property, e, prefix) //
				: new TypeTraversalResult(parent, property, e);

		return TypeTraversal.traverseType(context.getMetaData(), entityType, resultFactory).stream() //
				.filter(r -> isValidQueryParam(r.getProperty())) //
				.map(r -> getParameterRef(r.getProperty(), r.getEntityType(), context, "query", r.getPrefix())) //
				.collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
	}

	private OpenapiParameter getParameterRef(Property property, EntityType<?> entityType, OpenapiContext context, String in, String prefix) {
		return context.components().parameter(entityType, property, prefix) //
				.ensure(currentContext -> createParameter(property, entityType, currentContext, in, prefix)) //
				.getRef();
	}

	private OpenapiParameter createParameter(Property property, EntityType<?> entityType, OpenapiContext context, String in, String prefix) {
		OpenapiParameter parameter = OpenapiParameter.T.create();
		String name = property.getName();

		if (prefix != null) {
			name = prefix + "." + name;
		}

		EntityMdResolver entityMdResolver = context.getMetaData().entityType(entityType);

		String description = description(entityMdResolver).atProperty(property);

		parameter.setDescription(description);
		parameter.setRequired(isMandatory(entityMdResolver).atProperty(property));

		parameter.setName(name);
		parameter.setSchema(getPropertySchema(property, context, description, entityMdResolver));
		parameter.setIn(in);

		return parameter;
	}

	protected Stream<EntityType<?>> modelEntitiesSorted(OpenapiContext context) {
		return modelEntities(context) //
				.sorted(Comparator.comparing(EntityType::getTypeSignature));
	}

	protected Stream<EntityType<?>> modelEntities(OpenapiContext context) {
		ModelTypes entities = context.getComponentScope().getModelOracle().getTypes().onlyEntities();

		ModelMdResolver mdResolver = context.getMetaData();

		return entities.<EntityType<?>> asTypes() //
				.filter(t -> MetadataUtils.isVisible(mdResolver.entityType(t)).atEntity()) //
				.sorted(Comparator.comparing(EntityType::getTypeSignature));
	}

	public OpenapiResponse registerStandardResponse(String status, GenericModelType responseType, String description) {
		return standardComponentsContext.components().response(responseType, status) //
				.ensure(currentContext -> createResponse(responseType, description, currentContext)) //
				.getRef();
	}

	protected void addResponsesToOperation(GenericModelType successfulResponseType, OpenapiOperation operation, OpenapiContext context,
			boolean isAuthorizedRequest) {
		addResponsesToOperation(successfulResponseType, operation, context, isAuthorizedRequest, null);
	}

	protected void addResponsesToOperation(GenericModelType successfulResponseType, OpenapiOperation operation, OpenapiContext context,
			boolean isAuthorizedRequest, List<EntityType<?>> reasonTypes) {
		OpenapiResponse successResponse = context.components().response(successfulResponseType, "200") //
				.ensure(currentContext -> createResponse(successfulResponseType, "Request has been evaluated successfully.", currentContext)) //
				.getRef();

		operation.getResponses().put("200", successResponse);

		if (reasonTypes != null) {
			// Declared Reason responses
			//@formatter:off
			reasonTypes
				.stream()
				.forEach(rt -> {
					Optional<HttpStatusCode> statusOptional = Optional.ofNullable(context.getMetaData().entityType(rt).meta(HttpStatusCode.T).exclusive());
					Description descriptionMd = context.getMetaData().entityType(rt).meta(Description.T).exclusive();
					final String description;
					if (descriptionMd != null) {
						description = descriptionMd.getDescription().value();
					} else {
						description = rt.getShortName(); 
					}
					OpenapiResponse reasoneResponse = createResponse(rt, description, context);
					operation.getResponses().put(""+statusOptional.map(HttpStatusCode::getCode).orElse(500), reasoneResponse);
				});
			//@formatter:on
		} else {
			// Default Failure responses

			operation.getResponses().put("400", standardResponse400);

			// Cannot have a 401 if this is not an authorized request
			if (isAuthorizedRequest) {
				operation.getResponses().put("401", standardResponse401);
			}

			operation.getResponses().put("404", standardResponse404);
			operation.getResponses().put("500", standardResponse500);

		}

	}

	protected boolean isSimpleView(OpenapiContext context) {
		return context.getUseCases().contains(USECASE_OPENAPI_SIMPLE);
	}

	@Required
	public void setCortexSessionFactory(Supplier<PersistenceGmSession> cortexSessionFactory) {
		this.cortexSessionFactory = cortexSessionFactory;
	}

	@Required
	public void setModelAccessoryFactory(ModelAccessoryFactory modelAccessoryFactory) {
		this.modelAccessoryFactory = modelAccessoryFactory;
	}

}
