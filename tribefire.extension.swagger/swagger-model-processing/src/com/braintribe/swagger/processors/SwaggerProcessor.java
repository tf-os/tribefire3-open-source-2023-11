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

import static com.braintribe.swagger.util.SwaggerProcessorUtil.getSimpleParameterType;

import java.util.LinkedHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.value.EnumReference;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmListType;
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmSetType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.constraint.Mandatory;
import com.braintribe.model.meta.data.prompt.Priority;
import com.braintribe.model.meta.data.prompt.Visible;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.meta.cmd.result.PropertyMdResult;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.service.api.AuthorizedRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.model.swagger.v2_0.SwaggerApi;
import com.braintribe.model.swagger.v2_0.SwaggerBodyParameter;
import com.braintribe.model.swagger.v2_0.SwaggerInfo;
import com.braintribe.model.swagger.v2_0.SwaggerItems;
import com.braintribe.model.swagger.v2_0.SwaggerOperation;
import com.braintribe.model.swagger.v2_0.SwaggerResponse;
import com.braintribe.model.swagger.v2_0.SwaggerSchema;
import com.braintribe.model.swaggerapi.SwaggerRequest;
import com.braintribe.utils.genericmodel.GmPropertyComparator;

public abstract class SwaggerProcessor<R extends SwaggerRequest> implements ServiceProcessor<R, SwaggerApi> {

	protected Logger logger = Logger.getLogger(getClass());

	// TODO: Move to some commons class
	public static final String USECASE_SWAGGER = "swagger";
	public static final String USECASE_DDRA = "ddra";

	public static String mappingSpecificUsecase(String path) {
		return USECASE_DDRA + ":" + path;
	}

	protected static final String PATTERN_DOT = Pattern.quote(".");

	protected Evaluator<ServiceRequest> evaluator;

	protected GmEntityType failureType;

	protected String swaggerApiVersion;

	private String basePath = "/tribefire-services";

	protected Supplier<PersistenceGmSession> cortexSessionFactory;

	protected boolean isNotVisibleAndDoNotMatchResource(GmEntityType type, String resources, ModelMdResolver resolver) {
		return !resolver.entityType(type).is(Visible.T) || (StringUtils.isNotBlank(resources) && !type.getTypeSignature().startsWith(resources));
	}

	protected SwaggerApi createSwaggerApi(String title, String swaggerBasePath) {
		SwaggerApi api = SwaggerApi.T.create();
		api.setInfo(createSwaggerInfo(title));
		api.setBasePath(swaggerBasePath);
		api.setPaths(new LinkedHashMap<>());
		return api;
	}

	protected static String getPathKey(String uriService, String serviceTypeSignature, String pathParams, boolean useFullyQualifiedDefinitionName) {
		if (!useFullyQualifiedDefinitionName)
			serviceTypeSignature = getShortTypeName(serviceTypeSignature);

		return uriService + serviceTypeSignature + pathParams;
	}

	protected String getPathKey(String uriService, GmEntityType serviceType, String pathParams, boolean useFullyQualifiedDefinitionName) {
		String typeSignature = serviceType.getTypeSignature();
		return getPathKey(uriService, typeSignature, pathParams, useFullyQualifiedDefinitionName);
	}

	private static String getShortTypeName(String typeSignature) {
		int idx = typeSignature.lastIndexOf(".");
		return idx >= 0 ? typeSignature.substring(idx + 1, typeSignature.length()) : typeSignature;
	}

	private SwaggerInfo createSwaggerInfo(String title) {
		SwaggerInfo info = SwaggerInfo.T.create();
		info.setDescription("Learn about and try out the APIs for " + title
				+ ". <br/> <a target=\'_blank' href=\'https://documentation.tribefire.com/tribefire.cortex.documentation/api-doc/quick_start_rest.html'>Click here to learn about our REST API in general.</a>.");
		info.setTitle(title);
		info.setVersion(swaggerApiVersion);

		return info;
	}

	protected void computeFailureType() {
		logger.debug("Computing failure type...");
		PersistenceGmSession cortexSession = cortexSessionFactory.get();
		failureType = cortexSession.findEntityByGlobalId("type:" + Failure.T.getTypeSignature());
		logger.debug("Computed failure type.");
	}

	protected static String getInitializerDefault(Property property) {
		if (property.getInitializer() == null) {
			if (property.getDefaultRawValue() != null)
				return property.getDefaultRawValue().toString().replaceAll("\"", "'");

			return null;
		}

		if (property.getType().isEnum()) {
			EnumReference ref = (EnumReference) property.getInitializer();
			return ref.getConstant();
		}

		return property.getInitializer().toString().replaceAll("\"", "'");
	}

	protected static String getInitializerDefault(GmProperty gmProperty) {
		Object initializer = gmProperty.getInitializer();

		if (initializer == null) {
			if (gmProperty.getType().isGmSimple() && !gmProperty.getNullable()) {
				GenericModelType type = gmProperty.getType().reflectionType();
				if (type.getDefaultValue() != null)
					return type.getDefaultValue().toString().replaceAll("\"", "'");
			}

			return null;
		}

		if (gmProperty.getType().isGmEnum()) {
			EnumReference ref = (EnumReference) initializer;
			return ref.getConstant();
		}

		return initializer.toString().replaceAll("\"", "'");
	}

	protected static SwaggerItems getSimpleItems(GenericModelType type) {
		SwaggerItems items = SwaggerItems.T.create();
		items.setType(getSimpleParameterType(type));
		return items;
	}

	protected static SwaggerItems getSimpleItems(GmType type) {
		SwaggerItems items = SwaggerItems.T.create();
		items.setType(getSimpleParameterType(type));
		return items;
	}

	protected SwaggerResponse get200Response(SwaggerApi api, GmType serviceType, Predicate<GmProperty> ignoredPropertiesPredict) {
		SwaggerResponse response = SwaggerResponse.T.create();
		response.setDescription("Request has been evaluated successfully.");
		response.setSchema(getSchema(api, serviceType, true, ignoredPropertiesPredict));
		return response;
	}

	protected SwaggerResponse get400Response(SwaggerApi api, Predicate<GmProperty> ignoredPropertiesPredict) {
		SwaggerResponse response = SwaggerResponse.T.create();
		response.setDescription("Invalid input was given.");
		response.setSchema(getSchema(api, failureType, true, ignoredPropertiesPredict));
		return response;
	}

	protected SwaggerResponse get401Response(SwaggerApi api, Predicate<GmProperty> ignoredPropertiesPredict) {
		SwaggerResponse response = SwaggerResponse.T.create();
		response.setDescription("sessionId parameter was missing or invalid.");
		response.setSchema(getSchema(api, failureType, true, ignoredPropertiesPredict));
		return response;
	}

	protected SwaggerResponse get404Response(SwaggerApi api, Predicate<GmProperty> ignoredPropertiesPredict) {
		SwaggerResponse response = SwaggerResponse.T.create();
		response.setDescription("Resource not found.");
		response.setSchema(getSchema(api, failureType, true, ignoredPropertiesPredict));
		return response;
	}

	protected SwaggerResponse get412Response(SwaggerApi api, Predicate<GmProperty> ignoredPropertiesPredict) {
		SwaggerResponse response = SwaggerResponse.T.create();
		response.setDescription("Precondition fails.");
		response.setSchema(getSchema(api, failureType, true, ignoredPropertiesPredict));
		return response;
	}

	protected SwaggerResponse get500Response(SwaggerApi api, Predicate<GmProperty> ignoredPropertiesPredict) {
		SwaggerResponse response = SwaggerResponse.T.create();
		response.setDescription("An error occurred when processing the request.");
		response.setSchema(getSchema(api, failureType, true, ignoredPropertiesPredict));
		return response;
	}

	protected void addResponsesToOperation(SwaggerApi api, GmType serviceType, SwaggerOperation operation,
			Predicate<GmProperty> ignoredPropertiesPredict) {
		operation.getResponses().put("200", get200Response(api, serviceType, ignoredPropertiesPredict));
		operation.getResponses().put("400", get400Response(api, ignoredPropertiesPredict));
		// Cannot have a 401 if this is not an authorized request
		if (AuthorizedRequest.T.isAssignableFrom(serviceType.entityType()))
			operation.getResponses().put("401", get401Response(api, ignoredPropertiesPredict));
		operation.getResponses().put("404", get404Response(api, ignoredPropertiesPredict));
		operation.getResponses().put("500", get500Response(api, ignoredPropertiesPredict));
	}

	protected SwaggerSchema getSchema(SwaggerApi api, GmType type, boolean setInDefinitions, Predicate<GmProperty> ignoredPropertiesPredict) {
		SwaggerSchema schema = SwaggerSchema.T.create();
		schema.setProperties(new LinkedHashMap<>());
		switch (type.typeKind()) {
			case BASE:
				schema.setTitle(getTypeName(api, type.getTypeSignature()));
				schema.setType("object");
				break;
			case BOOLEAN:
				schema.setType("boolean");
				break;
			case DOUBLE:
				schema.setType("number");
				schema.setFormat("double");
				break;
			case ENTITY:
				if (setInDefinitions) {
					schema.set$ref(getRef(api, type));
					ensureEntityTypeDefinitions(api, (GmEntityType) type, ignoredPropertiesPredict);
				} else
					fillEntityTypeSchema(api, (GmEntityType) type, schema, ignoredPropertiesPredict);
				break;
			case FLOAT:
				schema.setType("number");
				schema.setFormat("float");
				break;
			case INTEGER:
				schema.setType("integer");
				schema.setFormat("int32");
				break;
			case LIST:
				schema.setTitle("list");
				schema.setType("array");
				GmListType listType = (GmListType) type;
				schema.setItems(getSchema(api, listType.getElementType(), true, ignoredPropertiesPredict));
				break;
			case STRING:
				schema.setType("string");
				break;
			case ENUM:
				schema.setTitle(getTypeName(api, type.getTypeSignature()));
				schema.setType("string");
				GmEnumType enumType = (GmEnumType) type;
				enumType.getConstants().stream().map(constant -> constant.getName()).forEach(schema.getEnum()::add);
				break;
			case DATE:
				schema.setTitle("date");
				schema.setType("string");
				schema.setFormat("dateTime");
				break;
			case DECIMAL:
				schema.setTitle("decimal");
				schema.setType("string");
				break;
			case LONG:
				schema.setTitle("long");
				schema.setType("string");
				break;
			case MAP:
				schema.setTitle("map");
				schema.setType("object");
				GmMapType mapType = (GmMapType) type;
				if (mapType.getKeyType().isGmSimple())
					schema.setAdditionalProperties(getSchema(api, mapType.getValueType(), true, ignoredPropertiesPredict));
				else {
					SwaggerSchema valueSchema = SwaggerSchema.T.create();
					schema.getProperties().put("value", valueSchema);

					valueSchema.setType("array");

					SwaggerSchema entriesSchema = SwaggerSchema.T.create();
					valueSchema.setItems(entriesSchema);

					entriesSchema.setType("object");
					entriesSchema.getProperties().put("key", getSchema(api, mapType.getKeyType(), true, ignoredPropertiesPredict));
					entriesSchema.getProperties().put("value", getSchema(api, mapType.getValueType(), true, ignoredPropertiesPredict));
				}
				break;
			case SET:
				schema.setTitle("set");
				schema.setType("array");
				GmSetType setType = (GmSetType) type;
				schema.setItems(getSchema(api, setType.getElementType(), true, ignoredPropertiesPredict));
				break;
			default:
				break;
		}

		return schema;
	}
	private static String getTypeName(SwaggerApi api, String typeSignature) {
		return api.getUseFullyQualifiedDefinitionName() ? typeSignature : getShortTypeName(typeSignature);
	}
	private static String getRef(SwaggerApi api, GmType type) {
		return "#/definitions/" + (getTypeName(api, type.getTypeSignature()));
	}

	private void ensureEntityTypeDefinitions(SwaggerApi api, GmEntityType entityType, Predicate<GmProperty> ignoredPropertiesPredict) {
		String typeSignature = getTypeName(api, entityType.getTypeSignature());
		if (api.getDefinitions().containsKey(typeSignature))
			return;

		SwaggerSchema schema = SwaggerSchema.T.create();
		schema.setProperties(new LinkedHashMap<>());
		api.getDefinitions().put(typeSignature, schema);
		fillEntityTypeSchema(api, entityType, schema, ignoredPropertiesPredict);
	}

	private void fillEntityTypeSchema(SwaggerApi api, GmEntityType entityType, SwaggerSchema schema, Predicate<GmProperty> ignoredPropertiesPredict) {
		schema.setTitle(getTypeName(api, entityType.getTypeSignature()));
		schema.setType("object");

		if (entityType.getDeclaringModel() == null) {
			logger.warn("EntityType: " + entityType.entityType() + " has no declared model!");
			return;
		}

		GmMetaModel declaringModel = entityType.getDeclaringModel();

		CmdResolver resolver = createResolverForMetaModel(declaringModel);
		if (resolver == null) {
			logger.warn("Resolver for EntityType: " + entityType.entityType() + " not created!");
			return;
		}

		ModelMdResolver metaData = resolver.getMetaData();
		if (metaData == null) {
			logger.warn("ModelMdResolver is NULL for EntityType: " + entityType.entityType() + "!");
			return;
		}
		ModelMdResolver mdResolver = metaData.useCase(USECASE_SWAGGER);
		if (mdResolver == null) {
			logger.warn("ModelMdResolver for use case USECASE_SWAGGER is NULL for EntityType: " + entityType.entityType() + "!");
			return;
		}

		EntityTypeOracle oracle = resolver.getModelOracle().getEntityTypeOracle(entityType);

		GmPropertyComparator propertyComparator = new GmPropertyComparator((p) -> {
			PropertyMdResolver property = mdResolver.property(p);
			PropertyMdResult<Priority> meta = property.meta(Priority.T);
			Priority prio = meta.exclusive();
			if (prio != null)
				return prio.getPriority();
			return 0d;
		});

		oracle.getProperties().asGmProperties().sorted(propertyComparator).filter(ignoredPropertiesPredict).forEach(property -> {
			try {
				if (mdResolver.entityType(entityType).property(property).is(Visible.T)) {
					schema.getProperties().put(property.getName(), getSchema(api, property.getType(), true, ignoredPropertiesPredict));
					boolean required = mdResolver.entityType(entityType).property(property).is(Mandatory.T);
					if (required)
						schema.getRequired().add(property.getName());
				}
			} catch (Exception e) {
				logger.warn("Exceprion handling property: " + property.getName());
				logger.warn("Exceprion message: " + e.getMessage());
			}
		});
	}

	protected SwaggerBodyParameter getBodyParameter(SwaggerApi api, GmType entityType, String name, Predicate<GmProperty> ignoredPropertiesPredict) {
		SwaggerBodyParameter parameter = SwaggerBodyParameter.T.create();
		parameter.setName(getTypeName(api, name));
		parameter.setRequired(true);

		SwaggerSchema schema = getSchema(api, entityType, false, ignoredPropertiesPredict);
		if (entityType.isGmEntity() && schema.getProperties().isEmpty())
			return null;// Do not expose empty body.

		parameter.setSchema(schema);
		return parameter;
	}

	protected static CmdResolver createResolverForMetaModel(GmMetaModel model) {
		return new CmdResolverImpl(new BasicModelOracle(model));
	}

	public String getBasePath() {
		return this.basePath;
	}

	@Required
	@Configurable
	public void setEvaluator(Evaluator<ServiceRequest> evaluator) {
		this.evaluator = evaluator;
	}

	@Configurable
	public void setSwaggerApiVersion(String swaggerApiVersion) {
		this.swaggerApiVersion = swaggerApiVersion;
	}

	@Configurable
	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	@Configurable
	@Required
	public void setCortexSessionFactory(Supplier<PersistenceGmSession> cortexSessionFactory) {
		this.cortexSessionFactory = cortexSessionFactory;
	}

}
