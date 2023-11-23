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
package com.braintribe.swagger;

import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import com.braintribe.logging.Logger;
import com.braintribe.model.exchange.GenericExchangePayload;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.builder.meta.MetaModelBuilder;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.generic.reflection.SimpleTypes;
import com.braintribe.model.meta.GmBaseType;
import com.braintribe.model.meta.GmBooleanType;
import com.braintribe.model.meta.GmDateType;
import com.braintribe.model.meta.GmDecimalType;
import com.braintribe.model.meta.GmDoubleType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmFloatType;
import com.braintribe.model.meta.GmIntegerType;
import com.braintribe.model.meta.GmListType;
import com.braintribe.model.meta.GmLongType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmSetType;
import com.braintribe.model.meta.GmStringType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.constraint.Mandatory;
import com.braintribe.model.meta.data.prompt.Description;
import com.braintribe.model.meta.data.prompt.Hidden;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.meta.data.prompt.Visible;
import com.braintribe.model.meta.selector.UseCaseSelector;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessors;
import com.braintribe.model.processing.assembly.sync.impl.AssemblyImporter;
import com.braintribe.model.processing.assembly.sync.impl.GenericImporterContext;
import com.braintribe.model.processing.notification.api.builder.Notifications;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.swagger.v2_0.meta.SwaggerBasePathMd;
import com.braintribe.model.swagger.v2_0.meta.SwaggerConsumesMd;
import com.braintribe.model.swagger.v2_0.meta.SwaggerContact;
import com.braintribe.model.swagger.v2_0.meta.SwaggerExampleMd;
import com.braintribe.model.swagger.v2_0.meta.SwaggerExternalDocsMd;
import com.braintribe.model.swagger.v2_0.meta.SwaggerExternalDocumentationObject;
import com.braintribe.model.swagger.v2_0.meta.SwaggerHostMd;
import com.braintribe.model.swagger.v2_0.meta.SwaggerInfoMd;
import com.braintribe.model.swagger.v2_0.meta.SwaggerLicense;
import com.braintribe.model.swagger.v2_0.meta.SwaggerProducesMd;
import com.braintribe.model.swagger.v2_0.meta.SwaggerSchemesMd;
import com.braintribe.model.swagger.v2_0.meta.SwaggerScopesObject;
import com.braintribe.model.swagger.v2_0.meta.SwaggerSecurityDefinitionsMd;
import com.braintribe.model.swagger.v2_0.meta.SwaggerSecurityMd;
import com.braintribe.model.swagger.v2_0.meta.SwaggerSecurityRequirementObject;
import com.braintribe.model.swagger.v2_0.meta.SwaggerSecurityScheme;
import com.braintribe.model.swagger.v2_0.meta.SwaggerSecurityScopes;
import com.braintribe.model.swagger.v2_0.meta.SwaggerTag;
import com.braintribe.model.swagger.v2_0.meta.SwaggerTagsMd;
import com.braintribe.model.swagger.v2_0.meta.SwaggerVendorExtensionMd;
import com.braintribe.swagger.util.SwaggerValidator;
import com.braintribe.utils.i18n.I18nTools;

import io.swagger.models.ArrayModel;
import io.swagger.models.ComposedModel;
import io.swagger.models.Contact;
import io.swagger.models.ExternalDocs;
import io.swagger.models.Info;
import io.swagger.models.License;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.RefModel;
import io.swagger.models.Response;
import io.swagger.models.Scheme;
import io.swagger.models.SecurityRequirement;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.models.auth.ApiKeyAuthDefinition;
import io.swagger.models.auth.BasicAuthDefinition;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.RefParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BaseIntegerProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.DateTimeProperty;
import io.swagger.models.properties.DecimalProperty;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.FloatProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import io.swagger.parser.SwaggerParser;
import io.swagger.parser.util.ClasspathHelper;
import io.swagger.parser.util.RemoteUrl;



public class ImportSwaggerModelProcessor implements AccessRequestProcessor<ImportSwaggerModelRequest,ImportSwaggerModelResponse> {

	private static final Logger logger = Logger.getLogger(ImportSwaggerModelProcessor.class);


	private static final GmMetaModel rootModel = GmMetaModel.T.create("model:com.braintribe.gm:root-model");
	private static final GmMetaModel serviceModel = GmMetaModel.T.create("model:com.braintribe.gm:access-request-model");
	private static final GmEntityType rootType =  GmEntityType.T.create("type:com.braintribe.model.generic.GenericEntity");
	private static final GmEntityType authRequestType =  GmEntityType.T.create("type:com.braintribe.model.service.api.AuthorizedRequest");
	private static final GmEntityType accessRequestType =  GmEntityType.T.create("type:com.braintribe.model.accessapi.AccessRequest");

	private static final GmType stringType = GmStringType.T.create("type:string");
	private static final GmType integerType = GmIntegerType.T.create("type:integer");
	private static final GmType longType = GmLongType.T.create("type:long");
	private static final GmType doubleType = GmDoubleType.T.create("type:double");
	private static final GmType floatType = GmFloatType.T.create("type:float");
	private static final GmType decimalType = GmDecimalType.T.create("type:decimal");
	private static final GmType booleanType = GmBooleanType.T.create("type:boolean");
	private static final GmType dateType = GmDateType.T.create("type:date");
	private static final GmType baseType = GmBaseType.T.create("type:object");

	private static final String GROUP_NAME = "tribefire.extension.swagger:";

	private final AccessRequestProcessor<ImportSwaggerModelRequest, ImportSwaggerModelResponse> dispatcher = AccessRequestProcessors.dispatcher(config->{
		config.register(ImportSwaggerModelFromResource.T, this::importSwaggerFromResource);
		config.register(ImportSwaggerModelFromUrl.T, this::importSwaggerFromUrl);
	});

	private Set<GenericEntity> externalReferences = new HashSet<>();
	private Map<String,GmType> typeRegistry = new HashMap<>();
	private Map<String,Map<String, Property>> propertyRegistry = new HashMap<>();
	private String defaultNamespace = "";
	private boolean disableValidation;
	
	@Override
	public ImportSwaggerModelResponse process(AccessRequestContext<ImportSwaggerModelRequest> context) {
		return dispatcher.process(context);
	}

	public ImportSwaggerModelResponse importSwaggerFromUrl(AccessRequestContext<ImportSwaggerModelFromUrl> context) {
		ImportSwaggerModelFromUrl request = context.getRequest();
		init(request);
		
		String swaggerUrl = request.getSwaggerUrl();
		if (StringUtils.isBlank(swaggerUrl)) {
			ImportSwaggerModelResponse response = ImportSwaggerModelResponse.T.create();
			response.getNotifications().addAll(
					Notifications.build()
							.add()
							.message().confirmError("Cannot import swagger model from empty or null URL!")
							.close()
							.list()
			);
			return response;
		}
		return importSwagger(context.getSession(), swaggerUrl, request.getImportOnlyDefinitions());

	}
	
	public ImportSwaggerModelResponse importSwaggerFromResource(AccessRequestContext<ImportSwaggerModelFromResource> context) {

		ImportSwaggerModelFromResource request = context.getRequest();
		init(request);
		
		PersistenceGmSession session = context.getSession();
		return importSwagger(session, session.resources().url(request.getSwaggerResource()).asString(), request.getImportOnlyDefinitions());
	}

	private void init(ImportSwaggerModelRequest request) {
		defaultNamespace = request.getNamespace();
		externalReferences = new HashSet<>();
		typeRegistry = new HashMap<>();
		propertyRegistry = new HashMap<>();
		disableValidation = request.getDisableValidation();
	}

	/* package-private */ ImportSwaggerModelResponse importSwagger(PersistenceGmSession session, String swaggerUrl, boolean importOnlyDefinitions) {
		Optional<ImportSwaggerModelResponse> validationResult = disableValidation ? Optional.empty() : validateSwaggerModel(swaggerUrl);
		if (validationResult.isPresent())
			return validationResult.get();

		Swagger swagger = parseSwagger(swaggerUrl);
		if (swagger == null) {
			ImportSwaggerModelResponse response = ImportSwaggerModelResponse.T.create();
			response.getNotifications().addAll(
					Notifications.build()
							.add()
							.message().confirmError("Swagger model is NULL, please use version 2.0 of openapi.")
							.close()
							.list()
			);
			return response;
		}

		List<GmMetaModel> models = new ArrayList<>();
		GmMetaModel metaModel = buildModel(swagger);
		models.add(metaModel);
		
		if (!importOnlyDefinitions) {
			GmMetaModel apiModel = buildApiModel(swagger, metaModel);
			models.add(apiModel);
		}

		GmMetaModel importedModel = getGmMetaModels(session, models).get(0);
		return prepareResponse(importedModel);
	}
	
	private Info getDefaultInfoIfNotPresent(Info swaggerInfo) {
		if (swaggerInfo == null) {
			swaggerInfo = new Info();
			swaggerInfo.setTitle("default_title_" + RandomStringUtils.randomAlphabetic(5));
			swaggerInfo.setVersion("1.0");
		}
		return swaggerInfo;
	}
	
	private GmMetaModel buildModel(Swagger swagger) {
		GmMetaModel metaModel = getGmMetaModel(swagger);
		
		Info swaggerInfo = getDefaultInfoIfNotPresent(swagger.getInfo());
		String typePackage = buildModelName(null, swaggerInfo.getTitle(), "-model").replaceAll("-", "").toLowerCase();

		prepareExternalReferences();
		externalReferences.stream().filter(GmType.class::isInstance).map(GmType.class::cast).forEach(extRef -> typeRegistry.put(extRef.getTypeSignature(), extRef));

		List<GmEntityType> entityTypes = new ArrayList<>();


		// CREATE ENTITIES
		Optional<Map<String, Model>> swaggerDefinitions = Optional.ofNullable(swagger.getDefinitions());

		swaggerDefinitions.ifPresent(swaggerDefinition ->
				swaggerDefinition.forEach((key, value) -> {
							GmEntityType entityType = buildGmEntityType(metaModel, typePackage, key, value);
							metaModel.getTypes().add(entityType);

							typeRegistry.put(key, entityType);
							entityTypes.add(entityType);

							Model swaggerType = value;
							String typeSignature = buildTypeSiganture(typePackage, key);
							Optional<Map<String, Property>> properties = Optional.ofNullable(swaggerType.getProperties());
							if (properties.isPresent()) {
								propertyRegistry.put(typeSignature, properties.get());
							} else if (swaggerType instanceof ComposedModel) {
								ComposedModel composedModel = (ComposedModel) swaggerType;
								Model child = composedModel.getChild();
								if (Objects.nonNull(child)) {
									propertyRegistry.put(typeSignature, child.getProperties());
								}
							}
						}
				)
		);

		// SET ENTITY PROPERTIES
		entityTypes.forEach(entityType -> {
			Map<String, Property> properties = propertyRegistry.get(entityType.getTypeSignature());
			if (Objects.nonNull(properties)) {
				buildProperties(entityType, properties);
			}
		});

		// SET ALLOF REFERENCIES
		swaggerDefinitions.ifPresent(swaggerDefinition ->
				swaggerDefinition.forEach((key, value) -> {
							GmType gmType = typeRegistry.get(key);
							if (value instanceof ComposedModel && gmType instanceof GmEntityType) {
								ComposedModel composedModel = (ComposedModel) value;
								List<Model> allOfs = composedModel.getAllOf();
								if (CollectionUtils.isNotEmpty(allOfs)) {
									List<GmEntityType> gmTypes = allOfs.stream()
											.filter(RefModel.class::isInstance)
											.map(this::getGmType)
											.filter(GmEntityType.class::isInstance)
											.map(GmEntityType.class::cast)
											.collect(Collectors.toList());
									GmEntityType gmEntityType = (GmEntityType) gmType;
									gmEntityType.getSuperTypes().addAll(gmTypes);
								}
							}
						}
				)
		);
		
		// ADDING MORE MODEL DETAILS
		String modelName = buildModelName(null, swaggerInfo.getTitle(), "-model");
		
		SwaggerInfoMd infoMd = buildInfoMd(swaggerInfo, modelName);
		addMetaDataIfNecessary(metaModel.getMetaData(), infoMd);
		
		SwaggerBasePathMd basePathMd = buildBasePathMd(swagger.getBasePath(), modelName);
		addMetaDataIfNecessary(metaModel.getMetaData(), basePathMd);
		
		SwaggerConsumesMd consumesMd = buildConsumesMd(swagger.getConsumes(), modelName);
		addMetaDataIfNecessary(metaModel.getMetaData(), consumesMd);
		
		SwaggerExternalDocsMd externalDocsMd = buildExternalDocsMd(swagger.getExternalDocs(), modelName);
		addMetaDataIfNecessary(metaModel.getMetaData(), externalDocsMd);
		
		SwaggerHostMd hostMd = buildHostMd(swagger.getHost(), modelName);
		addMetaDataIfNecessary(metaModel.getMetaData(), hostMd);
		
		SwaggerProducesMd producesMd = buildProducesMd(swagger.getProduces(), modelName);
		addMetaDataIfNecessary(metaModel.getMetaData(), producesMd);
		
		SwaggerSchemesMd schemesMd = buildSchemesMd(swagger.getSchemes(), modelName);
		addMetaDataIfNecessary(metaModel.getMetaData(), schemesMd);
		
		SwaggerSecurityMd securityMd = buildSecurityMd(swagger.getSecurity(), modelName);
		addMetaDataIfNecessary(metaModel.getMetaData(), securityMd);
		
		SwaggerSecurityDefinitionsMd securityDefinitionsMd = buildSecurityDefinitionsMd(swagger.getSecurityDefinitions(), modelName);
		addMetaDataIfNecessary(metaModel.getMetaData(), securityDefinitionsMd);

		SwaggerTagsMd tagsMd = buildTagsMd(swagger.getTags(), modelName);
		addMetaDataIfNecessary(metaModel.getMetaData(), tagsMd);

		return metaModel;
	}

	private GmMetaModel getGmMetaModel(final Swagger swagger) {
		Info swaggerInfo = getDefaultInfoIfNotPresent(swagger.getInfo());
		String modelName = buildModelName(GROUP_NAME, swaggerInfo.getTitle(), "-model");
		String modelVersion = buildVersion(swaggerInfo.getVersion());

		GmMetaModel metaModel = MetaModelBuilder.metaModel(modelName);
		metaModel.setVersion(modelVersion);
		metaModel.setGlobalId("model:"+modelName);
		metaModel.getDependencies().add(rootModel);

		addMetaDataIfNecessary(metaModel.getMetaData(), buildNameMd(swaggerInfo.getTitle(), modelName));
		addMetaDataIfNecessary(metaModel.getMetaData(), buildDescriptionMd(swaggerInfo.getDescription(), modelName));
		
		return metaModel;
	}

	private GmEntityType buildGmEntityType(GmMetaModel metaModel, String typePackage, String baseName, Model swaggerType) {
		String typeSignature = buildTypeSiganture(typePackage, baseName);

		GmEntityType entityType = MetaModelBuilder.entityType(typeSignature);
		entityType.setDeclaringModel(metaModel);
		entityType.getSuperTypes().add(rootType);
		entityType.setGlobalId("type:"+typeSignature);

		addMetaDataIfNecessary(entityType.getMetaData(), buildNameMd(swaggerType.getTitle(), typeSignature));
		addMetaDataIfNecessary(entityType.getMetaData(), buildDescriptionMd(swaggerType.getDescription(), typeSignature));
		if (Objects.nonNull(swaggerType.getExample())) {
			addMetaDataIfNecessary(entityType.getMetaData(), buildExampleMd(swaggerType.getExample().toString(), typeSignature));
		} else if (swaggerType instanceof ComposedModel) {
			ComposedModel composedModel = (ComposedModel) swaggerType;
			Model child = composedModel.getChild();
			if (Objects.nonNull(child) && Objects.nonNull(child.getExample())) {
				Object example = child.getExample();
				addMetaDataIfNecessary(entityType.getMetaData(), buildExampleMd(example.toString(), typeSignature));
			}
		}


		Map<String, Object> vendorExtensions = swaggerType.getVendorExtensions();
		if (MapUtils.isNotEmpty(vendorExtensions)) {
			vendorExtensions.forEach((key, value) -> addMetaDataIfNecessary(entityType.getMetaData(), buildVendorExtensionMd(value, key, typeSignature)));
		}
		addMetaDataIfNecessary(entityType.getMetaData(), buildHiddenMd());
		return entityType;
	}

	private GmMetaModel buildApiModel(Swagger swagger, GmMetaModel metaModel) {
		Info swaggerInfo = getDefaultInfoIfNotPresent(swagger.getInfo());

		String modelName = buildModelName(null, swaggerInfo.getTitle(), "-api-model");
		String modelVersion = buildVersion(swaggerInfo.getVersion());
		String typePackage = modelName.replaceAll("-", "").toLowerCase();
		String serviceModelName = buildModelName(GROUP_NAME, swaggerInfo.getTitle(), "-api-model");

		GmMetaModel apiModel = MetaModelBuilder.metaModel(serviceModelName);
		apiModel.setVersion(modelVersion);
		apiModel.setGlobalId("model:"+serviceModelName);
		apiModel.getDependencies().add(serviceModel);
		apiModel.getDependencies().add(metaModel);

		// Operation endpoints
		for (Map.Entry<String, Path> pathEntry : swagger.getPaths().entrySet()) {
			Path path = pathEntry.getValue();
			processOperations(typePackage, apiModel, path);
		}
		
		SwaggerInfoMd infoMd = buildInfoMd(swaggerInfo, modelName);
		addMetaDataIfNecessary(apiModel.getMetaData(), infoMd);
		
		SwaggerBasePathMd basePathMd = buildBasePathMd(swagger.getBasePath(), modelName);
		addMetaDataIfNecessary(apiModel.getMetaData(), basePathMd);
		
		SwaggerConsumesMd consumesMd = buildConsumesMd(swagger.getConsumes(), modelName);
		addMetaDataIfNecessary(apiModel.getMetaData(), consumesMd);
		
		SwaggerExternalDocsMd externalDocsMd = buildExternalDocsMd(swagger.getExternalDocs(), modelName);
		addMetaDataIfNecessary(apiModel.getMetaData(), externalDocsMd);
		
		SwaggerHostMd hostMd = buildHostMd(swagger.getHost(), modelName);
		addMetaDataIfNecessary(apiModel.getMetaData(), hostMd);
		
		SwaggerProducesMd producesMd = buildProducesMd(swagger.getProduces(), modelName);
		addMetaDataIfNecessary(apiModel.getMetaData(), producesMd);
		
		SwaggerSchemesMd schemesMd = buildSchemesMd(swagger.getSchemes(), modelName);
		addMetaDataIfNecessary(apiModel.getMetaData(), schemesMd);
		
		SwaggerSecurityMd securityMd = buildSecurityMd(swagger.getSecurity(), modelName);
		addMetaDataIfNecessary(apiModel.getMetaData(), securityMd);
		
		SwaggerSecurityDefinitionsMd securityDefinitionsMd = buildSecurityDefinitionsMd(swagger.getSecurityDefinitions(), modelName);
		addMetaDataIfNecessary(apiModel.getMetaData(), securityDefinitionsMd);

		SwaggerTagsMd tagsMd = buildTagsMd(swagger.getTags(), modelName);
		addMetaDataIfNecessary(apiModel.getMetaData(), tagsMd);
		
		return apiModel;
	}

	private void processOperations(String typePackage, GmMetaModel apiModel, Path path) {
		for (Operation operation : path.getOperations()) {

			String operationTitle = operation.getOperationId();
			if (Objects.isNull(operationTitle)) {
				operationTitle = operation.getSummary();
			}
			if (Objects.isNull(operationTitle)) {
				continue;
			}

			String operationName = camelizeApiTitle(operationTitle, true);
			String requestTypeSignature = buildTypeSiganture(typePackage+"."+"api", operationName+"Request");

			GmEntityType entityType = MetaModelBuilder.entityType(requestTypeSignature);
			entityType.setDeclaringModel(apiModel);
			entityType.getSuperTypes().add(authRequestType);
			entityType.getSuperTypes().add(accessRequestType);
			entityType.setGlobalId("type:"+requestTypeSignature);

			addMetaDataIfNecessary(entityType.getMetaData(), buildNameMd(operation.getSummary(), requestTypeSignature));
			addMetaDataIfNecessary(entityType.getMetaData(), buildDescriptionMd(operation.getDescription(), requestTypeSignature));

			Map<String, Object> pathVendorExtensions = path.getVendorExtensions();
			Map<String, Object> operationVendorExtensions = operation.getVendorExtensions();
			if (MapUtils.isNotEmpty(pathVendorExtensions)) {
				pathVendorExtensions.forEach((key, value) -> addMetaDataIfNecessary(entityType.getMetaData(), buildVendorExtensionMd(value, key, requestTypeSignature)));
			} else if (MapUtils.isNotEmpty(operationVendorExtensions)) {
				operationVendorExtensions.forEach((key, value) -> addMetaDataIfNecessary(entityType.getMetaData(), buildVendorExtensionMd(value, key, requestTypeSignature)));
			}

			entityType.setEvaluatesTo(baseType);
			Optional<Map<String, Response>> responses = Optional.ofNullable(operation.getResponses());
			responses.ifPresent(resp -> {
				Optional<Response> successResponse = Optional.ofNullable(resp.get("200"));
				successResponse.ifPresent(success -> {
					Model schemaAsModel = success.getResponseSchema();
					GmType evaluatesTo = getGmType(schemaAsModel);
					if (evaluatesTo instanceof GmEntityType) {
						setToVisible((GmEntityType) evaluatesTo);
						entityType.setEvaluatesTo(evaluatesTo);
					} else if (evaluatesTo instanceof GmSetType) {
						GmSetType setType = (GmSetType) evaluatesTo;
						entityType.setEvaluatesTo(evaluatesTo);
					} else {
						entityType.setEvaluatesTo(baseType);
					}
				});
			});

			processParameters(typePackage, operation.getParameters(), entityType);

			apiModel.getTypes().add(entityType);

		}
	}

	private void processParameters(String typePackage, List<Parameter> parameters, GmEntityType entityType) {
		for (Parameter parameter : parameters) {
			if (isEnumType(parameter)) {
				setEnumType(entityType, parameter, typePackage);
			} else {
				setPropertyType(entityType, parameter, typePackage);
			}
		}
	}

	private void setPropertyType(GmEntityType entityType, Parameter parameter, String typePackage) {
		String propertyName = parameter.getName();
		if ("id".equalsIgnoreCase(propertyName)) {
			propertyName = "paramId";
		}
		String propertyDescription = parameter.getDescription();
		String key = entityType.getTypeSignature()+"/"+propertyName;

		String requestTypeSignature = buildTypeSiganture(typePackage+"."+"api", propertyName+"Parameter");

		GmType propertyType = getParameterType(parameter, entityType, propertyName);

		if (Objects.nonNull(propertyType)) {
			GmProperty property = MetaModelBuilder.property(entityType, camelize(propertyName, false), propertyType);
			entityType.getProperties().add(property);
			property.setGlobalId("property:"+key);
			addMetaDataIfNecessary(property.getMetaData(),buildDescriptionMd(propertyDescription, key));
			addMetaDataIfNecessary(property.getMetaData(),buildNameMd(propertyName, key));
			if (parameter.getRequired()) addMetaDataIfNecessary(property.getMetaData(),buildMandatoryMd());
			Map<String, Object> propertyVendorExtensions = parameter.getVendorExtensions();
			if (MapUtils.isNotEmpty(propertyVendorExtensions)) {
				propertyVendorExtensions.forEach((k, v) -> addMetaDataIfNecessary(entityType.getMetaData(), buildVendorExtensionMd(v, k, requestTypeSignature)));
			}
		}
	}

	private void setEnumType(GmEntityType entityType, Parameter parameter, String typePackage) {
		String propertyName = parameter.getName();
		String propertyDescription = parameter.getDescription();
		String enumTypeSignature = buildTypeSiganture(typePackage + "." + "api", parameter.getName() + "Enum");
		String key = entityType.getTypeSignature()+"/"+propertyName;

		GmEnumType enumType = MetaModelBuilder.enumType(enumTypeSignature);
		enumType.setDeclaringModel(entityType.getDeclaringModel());
		enumType.setGlobalId("type:" + enumTypeSignature);
		List<GmEnumConstant> enumConstants = new ArrayList<>();
		for (Object o : ((AbstractSerializableParameter<?>) parameter).getEnum()) {
			enumConstants.add(MetaModelBuilder.enumConstant(enumType, normalizeEnumTitle(o.toString())));
		}
		enumType.setConstants(enumConstants);
		GmProperty property = MetaModelBuilder.property(entityType, camelize(propertyName, false), enumType);
		if (parameter.getRequired()) addMetaDataIfNecessary(property.getMetaData(),buildMandatoryMd());
		addMetaDataIfNecessary(property.getMetaData(),buildNameMd(propertyName, key));
		addMetaDataIfNecessary(property.getMetaData(),buildDescriptionMd(propertyDescription, key));
		Map<String, Object> propertyVendorExtensions = parameter.getVendorExtensions();
		if (MapUtils.isNotEmpty(propertyVendorExtensions)) {
			propertyVendorExtensions.forEach((k, v) -> addMetaDataIfNecessary(entityType.getMetaData(), buildVendorExtensionMd(v, k, enumTypeSignature)));
		}
		property.setGlobalId("property:" + key);
		entityType.getProperties().add(property);
		entityType.getDeclaringModel().getTypes().add(enumType);
	}

	private void setToVisible(GmEntityType entityType) {
		entityType.getMetaData().removeIf(md -> md instanceof Hidden);
		addMetaDataIfNecessary(entityType.getMetaData(), buildVisibleMd());
	}

	private  void buildProperties(GmEntityType entityType, Map<String, Property> properties) {
		List<GmProperty> gmProperties = entityType.getProperties();
		if (CollectionUtils.isEmpty(gmProperties)) {
			for (Map.Entry<String, Property> propertyEntry : properties.entrySet()) {
				String propertyName = propertyEntry.getKey();
				Property propertyDetails = propertyEntry.getValue();
				List<?> enums = getEnumTypes(propertyDetails);
				if (Objects.nonNull(enums)) {
					setEnumType(entityType, propertyName, enums, propertyDetails.getTitle(), propertyDetails.getDescription(), propertyDetails.getRequired());
				} else {
					GmType propertyType = getGmType(propertyDetails, entityType, propertyName);
					if (Objects.nonNull(propertyType)) {
						setProperty(entityType, propertyDetails, propertyName, propertyType);
					} else {
						logger.warn("Unsupported swagger type: "+propertyDetails.getType()+". Property: "+propertyName+" of type: "+entityType.getTypeSignature()+" ignored.");
					}
				}
			}
		}
	}

	private void setProperty(GmEntityType entityType, Property propertyDetails, String propertyName, GmType propertyType) {
		String propertyTitle = propertyDetails.getTitle();
		String propertyDescription = propertyDetails.getDescription();

		// TODO: how to identify the id property from swagger?
		if (!GenericEntity.id.equals(propertyName)) {
			String key = entityType.getTypeSignature()+"/"+propertyName;
			GmProperty property = MetaModelBuilder.property(entityType, propertyName, propertyType);
			property.setGlobalId("property:"+key);
			entityType.getProperties().add(property);

			addMetaDataIfNecessary(property.getMetaData(),buildNameMd(propertyTitle, key));
			addMetaDataIfNecessary(property.getMetaData(),buildDescriptionMd(propertyDescription, key));
			if (propertyDetails.getRequired()) addMetaDataIfNecessary(property.getMetaData(),buildMandatoryMd());
			if (propertyDetails.getExample() != null)
				addMetaDataIfNecessary(property.getMetaData(), buildExampleMd(propertyDetails.getExample().toString(), key));
		}
	}

	private void setEnumType(GmType entityType, String propertyName, List<?> enums, String propertyTitle, String propertyDescription, boolean isRequired) {
		String enumTypeSignature = buildTypeSiganture(entityType.getTypeSignature() + "." + "api", propertyName + "Enum");

		GmEnumType enumType = MetaModelBuilder.enumType(enumTypeSignature);
		enumType.setDeclaringModel(entityType.getDeclaringModel());
		enumType.setGlobalId("type:" + enumTypeSignature);
		List<GmEnumConstant> enumConstants = new ArrayList<>();
		for (Object o : enums) {
			enumConstants.add(MetaModelBuilder.enumConstant(enumType, normalizeEnumTitle(o.toString())));
		}
		enumType.setConstants(enumConstants);

		GmProperty property = MetaModelBuilder.property((GmEntityType) entityType, propertyName, enumType);
		if (isRequired) addMetaDataIfNecessary(property.getMetaData(),buildMandatoryMd());
		((GmEntityType) entityType).getProperties().add(property);
		String key = entityType.getTypeSignature() + "/" + propertyName;
		property.setGlobalId("property:" + key);
		addMetaDataIfNecessary(property.getMetaData(),buildDescriptionMd(propertyDescription, key));
		addMetaDataIfNecessary(property.getMetaData(),buildNameMd(propertyTitle, key));

		entityType.getDeclaringModel().getTypes().add(enumType);
	}
	
	private String normalizeEnumTitle(String title) {
		return title.replace("\"", "").replaceAll("[^A-Za-z0-9]", "_").toLowerCase();
	}

	private GmType buildPropertyEntity(GmType entityType, Property propertyDetails, String propertyName) {
		if (!(propertyDetails instanceof ObjectProperty)) {
			return baseType;
		}
		ObjectProperty propertyObject = (ObjectProperty) propertyDetails;
		String propertyTypeSignature = buildTypeSiganture(entityType.getTypeSignature() + "." + "api", propertyName);
		GmEntityType propertyEntityType = MetaModelBuilder.entityType(propertyTypeSignature);
		propertyEntityType.setDeclaringModel(entityType.getDeclaringModel());
		propertyEntityType.getSuperTypes().add(rootType);
		propertyEntityType.setGlobalId("type:"+propertyTypeSignature);

		addMetaDataIfNecessary(propertyEntityType.getMetaData(), buildNameMd(propertyObject.getTitle(), propertyTypeSignature));
		addMetaDataIfNecessary(propertyEntityType.getMetaData(), buildDescriptionMd(propertyObject.getDescription(), propertyTypeSignature));
		if (propertyDetails.getExample() != null)
			addMetaDataIfNecessary(propertyEntityType.getMetaData(), buildExampleMd(propertyObject.getExample().toString(), propertyTypeSignature));

		Map<String, Object> vendorExtensions = propertyObject.getVendorExtensions();
		if (MapUtils.isNotEmpty(vendorExtensions)) {
			vendorExtensions.forEach((key, value) -> addMetaDataIfNecessary(propertyEntityType.getMetaData(), buildVendorExtensionMd(value, key, propertyTypeSignature)));
		}
		entityType.getDeclaringModel().getTypes().add(propertyEntityType);
		Map<String, Property> properties = propertyObject.getProperties();
		if (Objects.nonNull(properties)) {
			buildProperties(propertyEntityType, properties);
		}
		addMetaDataIfNecessary(propertyEntityType.getMetaData(), buildHiddenMd());
		return propertyEntityType;

	}

	private String buildTypeSiganture(String typePackage, String baseName) {
		return typePackage+"."+baseName.substring(0, 1).toUpperCase()+baseName.substring(1);
	}

	private void addMetaDataIfNecessary(Set<MetaData> metaDataSet, MetaData md) {
		if (Objects.nonNull(md)) {
			metaDataSet.add(md);
		}
	}

	private Description buildDescriptionMd(String description, String key) {
		if (Objects.isNull(description)) return null;
		Description md = Description.T.create("desc:"+key);
		md.setDescription(I18nTools.createLsWithGlobalId(description, "descls:"+key));
		return md;
	}

	private Name buildNameMd(String name, String key) {
		if (Objects.isNull(name)) return null;
		Name md = Name.T.create("name:"+key);
		md.setName(I18nTools.createLsWithGlobalId(name, "namels:"+key));
		return md;
	}
	
	private SwaggerBasePathMd buildBasePathMd(String basePath, String key) {
		if (Objects.isNull(basePath)) return null;
		SwaggerBasePathMd basePathMd = SwaggerBasePathMd.T.create("swaggerbasepath:"+key);
		basePathMd.setBasePath(basePath);
		return basePathMd;
	}
	
	private SwaggerHostMd buildHostMd(String host, String key) {
		if (Objects.isNull(host)) return null;
		SwaggerHostMd hostMd = SwaggerHostMd.T.create("swaggerhost:"+key);
		hostMd.setHost(host);
		return hostMd;
	}
	
	private SwaggerConsumesMd buildConsumesMd(List<String> consumes, String key) {
		if (CollectionUtils.isEmpty(consumes)) return null;
		SwaggerConsumesMd consumesMd = SwaggerConsumesMd.T.create("swaggerconsumes:"+key);
		consumesMd.setConsumes(consumes);
		return consumesMd;
	}
	
	private SwaggerProducesMd buildProducesMd(List<String> produces, String key) {
		if (CollectionUtils.isEmpty(produces)) return null;
		SwaggerProducesMd producesMd = SwaggerProducesMd.T.create("swaggerproduces:"+key);
		producesMd.setProduces(produces);
		return producesMd;
	}
	
	private SwaggerSchemesMd buildSchemesMd(List<Scheme> swaggerSchemes, String key) {
		if (CollectionUtils.isEmpty(swaggerSchemes)) return null;
		SwaggerSchemesMd schemesMd = SwaggerSchemesMd.T.create("swaggerschemes:"+key);
		
		List<String> schemes = swaggerSchemes.stream().map(scheme -> scheme.toValue()).collect(Collectors.toList());
		schemesMd.setSchemes(schemes);
		
		return schemesMd;
	}
	
	private SwaggerTagsMd buildTagsMd(List<Tag> swaggerTags, String key) {
		if (CollectionUtils.isEmpty(swaggerTags)) return null;
		SwaggerTagsMd tagsMd = SwaggerTagsMd.T.create("swaggertags:"+key);
		List<SwaggerTag> tags = swaggerTags.stream().map(tag -> buildTag(tag)).filter(Objects::nonNull).collect(Collectors.toList());
		tagsMd.setTags(tags);
		return tagsMd;
	}
	
	private SwaggerTag buildTag(Tag swaggerTag) {
		if (Objects.isNull(swaggerTag)) return null; 
		SwaggerTag tag = SwaggerTag.T.create();
		tag.setName(swaggerTag.getName());
		tag.setDescription(swaggerTag.getDescription());
		ExternalDocs externalDocs = swaggerTag.getExternalDocs();
		if (Objects.nonNull(externalDocs)) {
			SwaggerExternalDocumentationObject externalDocumentation = SwaggerExternalDocumentationObject.T.create();
			externalDocumentation.setDescription(externalDocs.getDescription());
			externalDocumentation.setUrl(externalDocs.getUrl());
			tag.setExternalDocs(externalDocumentation);
		}
		return tag;
	}
	
	private SwaggerSecurityDefinitionsMd buildSecurityDefinitionsMd(Map<String, SecuritySchemeDefinition> swaggerSecurityDefinitions, String key) {
		if (MapUtils.isEmpty(swaggerSecurityDefinitions)) return null;
		SwaggerSecurityDefinitionsMd securityDefinitionsMd = SwaggerSecurityDefinitionsMd.T.create("swaggersecuritydefinitions:"+key);
		Map<String, SwaggerSecurityScheme> securityDefinitions = 
				swaggerSecurityDefinitions.entrySet().stream().filter(Objects::nonNull)
		        .collect(Collectors.toMap(
		            e -> e.getKey(),
		            e -> buildSecurityScheme(e.getValue())
		        ));
		securityDefinitionsMd.setSecuritySchemes(securityDefinitions);
		return securityDefinitionsMd;
	}
	
	private SwaggerSecurityScheme buildSecurityScheme(SecuritySchemeDefinition schemeDefinition) {
		if (Objects.isNull(schemeDefinition)) return null;
		SwaggerSecurityScheme securityScheme = SwaggerSecurityScheme.T.create();
		securityScheme.setType(schemeDefinition.getType());
		securityScheme.setDescription(schemeDefinition.getDescription());
		if (schemeDefinition instanceof ApiKeyAuthDefinition) {
			ApiKeyAuthDefinition apiKey = (ApiKeyAuthDefinition) schemeDefinition;
			securityScheme.setName(apiKey.getName());
			securityScheme.setIn(apiKey.getIn() != null ? apiKey.getIn().name() : "");
		} else if (schemeDefinition instanceof BasicAuthDefinition) {
			BasicAuthDefinition basic = (BasicAuthDefinition) schemeDefinition;
		} else if (schemeDefinition instanceof OAuth2Definition) {
			OAuth2Definition oauth = (OAuth2Definition) schemeDefinition;
			securityScheme.setFlow(oauth.getFlow());
			securityScheme.setAuthorizationUrl(oauth.getAuthorizationUrl());
			securityScheme.setTokenUrl(oauth.getTokenUrl());
			SwaggerScopesObject scopesObject = SwaggerScopesObject.T.create();
			scopesObject.setScopes(oauth.getScopes());
			securityScheme.setScopes(scopesObject);
		}
		
		return securityScheme;
	}
	
	private SwaggerSecurityMd buildSecurityMd(List<SecurityRequirement> swaggerSecurityRequirements, String key) {
		if (CollectionUtils.isEmpty(swaggerSecurityRequirements)) return null;
		SwaggerSecurityMd securityMd = SwaggerSecurityMd.T.create("swaggersecurity:"+key);
		
		List<SwaggerSecurityRequirementObject> securityRequirements = 
				swaggerSecurityRequirements.stream().map(it->buildSecurityRequirementObject(it)).filter(Objects::nonNull).collect(Collectors.toList());
		
		if (CollectionUtils.isEmpty(swaggerSecurityRequirements)) return null;
		
		securityMd.setSecurity(securityRequirements);
		return securityMd;
	}
	
	private SwaggerSecurityRequirementObject buildSecurityRequirementObject(SecurityRequirement securityRequirement) {
		if (Objects.isNull(securityRequirement)) return null;
		SwaggerSecurityRequirementObject securityRequirementObject = SwaggerSecurityRequirementObject.T.create();
		if(Objects.nonNull(securityRequirement.getRequirements())) {
			
			Map<String, SwaggerSecurityScopes> SecurityRequirements = 
					securityRequirement.getRequirements().entrySet().stream().filter(Objects::nonNull)
	        .collect(Collectors.toMap(
	            e -> e.getKey(),
	            e -> buildSecurityScopes(e.getValue())
	        ));
			securityRequirementObject.setSecurityRequirement(SecurityRequirements);
		}
		return securityRequirementObject;
	}
	
	private SwaggerSecurityScopes buildSecurityScopes(List<String> scopes) {
		SwaggerSecurityScopes securityScopes = SwaggerSecurityScopes.T.create();
		securityScopes.setSecurityScopes(scopes);
		return securityScopes;
	}
	
	private SwaggerExternalDocsMd buildExternalDocsMd(ExternalDocs externalDocs, String key) {
		if (Objects.isNull(externalDocs)) return null;
		SwaggerExternalDocsMd externalDocsMd = SwaggerExternalDocsMd.T.create("swaggerexternaldocs:"+key);
		
		SwaggerExternalDocumentationObject externalDocumentation = buildExternalDocs(externalDocs,key);
		if (Objects.nonNull(externalDocumentation)) {
			externalDocsMd.setExternalDocs(externalDocumentation);
		}
		return externalDocsMd;
	}
	
	private SwaggerExternalDocumentationObject buildExternalDocs(ExternalDocs externalDocs, String key) {
		if(Objects.isNull(externalDocs)) return null;
		SwaggerExternalDocumentationObject externalDocumentation = SwaggerExternalDocumentationObject.T.create("swaggerexternaldocumentation:"+key);
		externalDocumentation.setDescription(externalDocs.getDescription());
		externalDocumentation.setUrl(externalDocs.getUrl());
		return externalDocumentation;
	}
	
	private SwaggerInfoMd buildInfoMd(Info swaggerInfo, String key) {
		if (Objects.isNull(swaggerInfo)) return null;
		SwaggerInfoMd infoMd = SwaggerInfoMd.T.create("swaggerinfomd:"+key);
		
		infoMd.setTitle(swaggerInfo.getTitle());
		infoMd.setDescription(swaggerInfo.getDescription());
		infoMd.setTermsOfService(swaggerInfo.getTermsOfService());
		infoMd.setVersion(swaggerInfo.getVersion());
		
		SwaggerContact swaggerContact = buildContactMd(swaggerInfo.getContact(),key);
		if (Objects.nonNull(swaggerContact)) {
			infoMd.setContact(swaggerContact);
		}
		
		SwaggerLicense swaggerLicense = buildLicenseMd(swaggerInfo.getLicense(),key);
		if (Objects.nonNull(swaggerLicense)) {
			infoMd.setLicense(swaggerLicense);
		}
		
		return infoMd;
	}
	
	private SwaggerContact buildContactMd(Contact contact, String key) {
		if(Objects.isNull(contact)) return null;
		SwaggerContact swaggerContact = SwaggerContact.T.create("swaggercontact:"+key);
		swaggerContact.setName(contact.getName());
		swaggerContact.setUrl(contact.getUrl());
		swaggerContact.setEmail(contact.getEmail());
		return swaggerContact;
	}
	
	private SwaggerLicense buildLicenseMd(License license, String key) {
		if(Objects.isNull(license)) return null;
		SwaggerLicense swaggerLicense = SwaggerLicense.T.create("swaggerlicense:"+key);
		swaggerLicense.setName(license.getName());
		swaggerLicense.setUrl(license.getUrl());
		return swaggerLicense;
	}

	private SwaggerExampleMd buildExampleMd(String example, String key) {
		if (Objects.isNull(example)) return null;
		SwaggerExampleMd md = SwaggerExampleMd.T.create("example:"+key);
		md.setExample(example);
		return md;
	}

	private SwaggerVendorExtensionMd buildVendorExtensionMd(Object value, String key, String signature) {
		if (Objects.isNull(value)) return null;
		SwaggerVendorExtensionMd md = SwaggerVendorExtensionMd.T.create("extension:"+signature + ":"+key);
		md.setKey(key);
		md.setValue(value.toString());
		return md;
	}

	private Hidden buildHiddenMd() {
		Hidden md = Hidden.T.create();
		UseCaseSelector useCase = UseCaseSelector.T.create();
		useCase.setUseCase("swagger");
		md.setSelector(useCase);
		return md;
	}

	private Visible buildVisibleMd() {
		return Visible.T.create();
	}

	private Mandatory buildMandatoryMd() {
		return Mandatory.T.create();
	}

	private String buildModelName(String prefix, String baseName, String sufix) {
		prefix = StringUtils.isNoneBlank(prefix) ? prefix : "";
		sufix = StringUtils.isNoneBlank(sufix) ? sufix : "";
		baseName = StringUtils.isNoneBlank(baseName) ? baseName : "default_title" + RandomStringUtils.randomAlphabetic(5);
		if (StringUtils.isNoneBlank(defaultNamespace)) {
			baseName = defaultNamespace;
			prefix = "";
		}
		
		String normalizedTitle = normalizeTitle(prefix + baseName + sufix);
		return normalizedTitle;
	}

	private String buildVersion(String baseVersion) {
		return Objects.isNull(baseVersion) ? "1.0" : baseVersion;
	}

	private String normalizeTitle(String title) {
		return title.replaceAll(" ", "-").replaceAll("\\[", "").replaceAll("\\]", "").toLowerCase();
	}

	private String camelize(String str, boolean capitalizeFirstLetter) {
		str = str.replaceAll("[^A-Za-z0-9]", "_");
		String[] strings = StringUtils.split(str.toLowerCase(), "_");
		for (int i = capitalizeFirstLetter ? 0 : 1; i < strings.length; i++){
			strings[i] = StringUtils.capitalize(strings[i]);
		}
		return StringUtils.join(strings);
	}
	
	private String camelizeApiTitle(String str, boolean capitalizeFirstLetter) {
		str = str.replaceAll("[^A-Za-z0-9]", "_");
		String[] strings = StringUtils.split(str, "_");
		for (int i = capitalizeFirstLetter ? 0 : 1; i < strings.length; i++){
			strings[i] = StringUtils.capitalize(strings[i]);
		}
		return StringUtils.join(strings);
	}

	private Swagger parseSwagger(String swaggerUrl) {
		SwaggerParser parser = new SwaggerParser();
		return parser.read(swaggerUrl); //"http://petstore.swagger.io/v2/swagger.json"
	}

	private void prepareExternalReferences() {
		externalReferences = new HashSet<>(Arrays.asList(rootModel,serviceModel,rootType,authRequestType,accessRequestType,stringType,integerType,longType,doubleType,floatType,decimalType,booleanType,dateType,baseType));
		for (SimpleType st : SimpleTypes.TYPES_SIMPLE) {
			externalReferences.add(GmListType.T.create("list<"+st.getTypeSignature()+">"));
		}
	}

	private GmType getBaseGmType(String type, String format) {
		switch (type) {
			case StringProperty.TYPE:
				switch (format) {
					case "date":
					case "date-time":
						return dateType;
				}
				return stringType;
			case BaseIntegerProperty.TYPE:
				switch (format) {
					case LongProperty.FORMAT:
						return longType;
				}
				return integerType;
			case DecimalProperty.TYPE:
				switch (format) {
					case FloatProperty.FORMAT:
						return floatType;
					case DoubleProperty.FORMAT:
						return doubleType;
				}
				return decimalType;
			case BooleanProperty.TYPE:
				return booleanType;
		}
		return null;
	}

	private GmType getGmType(Property propertyDetails, GmType entityType, String propertyName) {
		if (Objects.isNull(propertyDetails)) return null;
		String format = propertyDetails.getFormat();
		if (Objects.isNull(format)) {
			format = "";
		}
		
		List<?> enums = getEnumTypes(propertyDetails);
		if (Objects.nonNull(enums)) {
			setEnumType(entityType, propertyName, enums, propertyDetails.getTitle(), propertyDetails.getDescription(), propertyDetails.getRequired());
			return null;
		}
		
		String type = propertyDetails.getType();

		if (Objects.isNull(type)) {
			return baseType;
		}

		GmType baseGmType = getBaseGmType(type, format);
		if (Objects.nonNull(baseGmType)) {
			return baseGmType;
		}

		switch (type) {
			case RefProperty.TYPE:
				RefProperty refProperty = (RefProperty) propertyDetails;
				String simpleRef = refProperty.getSimpleRef();
				String reference = refProperty.get$ref().substring(14);
				if (reference.contains("/properties/")) {
					return getReferenceTypeFromProperty(reference, simpleRef);
				}
				GmType refType = typeRegistry.get(simpleRef);
				if (Objects.nonNull(refType)) {
					return refType;
				}
				break;
			case ObjectProperty.TYPE:
				return buildPropertyEntity(entityType, propertyDetails, propertyName);
			case ArrayProperty.TYPE:
				ArrayProperty arrayProperty = (ArrayProperty) propertyDetails;

				GmType elementType = getGmType(arrayProperty.getItems(), entityType, propertyName);
				if (elementType == null) return null;
				String typeSignature = "set<"+elementType.getTypeSignature()+">";

				GmType listType = typeRegistry.get(typeSignature);
				if (Objects.isNull(listType)) {
					listType = GmSetType.T.create("type:"+typeSignature);
					listType.setTypeSignature(typeSignature);
					((GmSetType)listType).setElementType(elementType);
					typeRegistry.put(typeSignature, listType);
				}
				return listType;

		}
		return baseType;

	}
	
	private GmType getParameterType(Parameter parameter, GmType entityType, String parameterName) {
		if (Objects.isNull(parameter)) return null;
		if (parameter instanceof AbstractSerializableParameter) {
			AbstractSerializableParameter<?> abstractParameter = (AbstractSerializableParameter<?>) parameter;
			
			Property items = abstractParameter.getItems();
			if (Objects.nonNull(items)) {
				ArrayProperty arrayProperty = new ArrayProperty(items);
				return getGmType(arrayProperty, entityType, parameterName);
			}
			
			String format = abstractParameter.getFormat();
			if (Objects.isNull(format)) {
				format = "";
			}
			
			List<?> enums = abstractParameter.getEnumValue();
			if (Objects.nonNull(enums)) {
				setEnumType(entityType, parameterName, enums, abstractParameter.getName(), abstractParameter.getDescription(), abstractParameter.getRequired());
				return null;
			}
			
			String type = abstractParameter.getType();

			if (Objects.isNull(type)) {
				return baseType;
			}

			GmType baseGmType = getBaseGmType(type, format);
			if (Objects.nonNull(baseGmType)) {
				return baseGmType;
			}

			switch (type) {
				case ObjectProperty.TYPE:
					return baseType;
			}
			return baseType;
			
		} else if (parameter instanceof BodyParameter) {
			BodyParameter bodyParameter = (BodyParameter) parameter;
			Model schemaAsModel = bodyParameter.getSchema();
			GmType bodyType = getGmType(schemaAsModel);
			return bodyType;
			
		} else if (parameter instanceof RefParameter) {
			RefParameter refParameter = (RefParameter) parameter;
			
			String simpleRef = refParameter.getSimpleRef();
			String reference = refParameter.get$ref().substring(14);
			if (reference.contains("/properties/")) {
				return getReferenceTypeFromProperty(reference, simpleRef);
			}
			GmType refType = typeRegistry.get(simpleRef);
			if (Objects.nonNull(refType)) {
				return refType;
			}
			
		}
		logger.info("not supported parameter: [" + parameterName + " ; " + "parameterType:" + parameter.getClass().getCanonicalName());
		return null;
	}

	private GmType getGmType(Model model) {
		if (Objects.isNull(model)) return null;
		if (model instanceof ComposedModel) {
			ComposedModel composedModel = (ComposedModel) model;
			List<Model> allOf = composedModel.getAllOf();
			if (CollectionUtils.isNotEmpty(allOf)) {
				return getGmType(allOf.get(0));
			}
		}
		if (model instanceof RefModel) {
			return getReferenceGmType((RefModel) model);
		}
		if (model instanceof ArrayModel) {
			ArrayModel arrayModel = (ArrayModel) model;
			GmType elementType = getGmType(arrayModel.getItems(),null,null);
			if (elementType == null) return baseType;
			
			String typeSignature = "set<"+elementType.getTypeSignature()+">";

			GmType listType = typeRegistry.get(typeSignature);
			if (Objects.isNull(listType)) {
				listType = GmSetType.T.create("type:"+typeSignature);
				listType.setTypeSignature(typeSignature);
				((GmSetType)listType).setElementType(elementType);
				typeRegistry.put(typeSignature, listType);
			}
			return listType;
		}

		return baseType;

	}

	private GmType getReferenceGmType(RefModel refModel) {
		String simpleRef = refModel.getSimpleRef();
		String reference = refModel.get$ref().substring(14);
		// TODO @vbankovic how to handle multiple level of properties? check is there better parser
		if (reference.contains("/properties/")) {
			return getReferenceTypeFromProperty(reference, simpleRef);
		}
		GmType refType = typeRegistry.get(simpleRef);
		if (Objects.nonNull(refType)) {
			return refType;
		}
		return null;
	}

	private GmType getReferenceTypeFromProperty (String reference, String simpleRef) {
		String refModelKey = reference.substring(0, reference.indexOf("/"));
		GmType refType = typeRegistry.get(refModelKey);
		if (refType instanceof GmEntityType) {
			GmEntityType entityRefType = (GmEntityType) refType;
			List<GmProperty> gmProperties = entityRefType.getProperties();
			if (CollectionUtils.isNotEmpty(gmProperties)) {
				Optional<GmProperty >prop = gmProperties.stream().filter(p-> simpleRef.equals(p.getName())).findFirst();
				if (prop.isPresent())
					return prop.get().getType();
			} else {
				buildProperties(entityRefType, propertyRegistry.get(entityRefType.getTypeSignature()));
				return getReferenceTypeFromProperty(reference, simpleRef);
			}
		}
		return null;
	}

	private List<?> getEnumTypes(Property property) {
		if (property instanceof StringProperty && ((StringProperty) property).getEnum() != null) {
			return ((StringProperty) property).getEnum();
		}
		if (property instanceof DateProperty && ((DateProperty) property).getEnum() != null) {
			return ((DateProperty) property).getEnum();
		}
		if (property instanceof DateTimeProperty && ((DateTimeProperty) property).getEnum() != null) {
			return ((DateTimeProperty) property).getEnum();
		}
		if (property instanceof IntegerProperty && ((IntegerProperty) property).getEnum() != null) {
			return ((IntegerProperty) property).getEnum();
		}
		if (property instanceof LongProperty && ((LongProperty) property).getEnum() != null) {
			return ((LongProperty) property).getEnum();
		}
		if (property instanceof FloatProperty && ((FloatProperty) property).getEnum() != null) {
			return ((FloatProperty) property).getEnum();
		}
		if (property instanceof DoubleProperty && ((DoubleProperty) property).getEnum() != null) {
			return ((DoubleProperty) property).getEnum();
		}
		return null;
	}
	
	private boolean isEnumType(Parameter parameter) {
		boolean result = (parameter instanceof AbstractSerializableParameter && ((AbstractSerializableParameter<?>) parameter).getEnum() != null);
		
		return result;
	}

	private String getContentFromUrl(String location) {
		logger.info("reading from " + location);
		try {
			String data;
			location = location.replaceAll("\\\\","/");
			if (location.toLowerCase().startsWith("http")) {
				data = RemoteUrl.urlToString(location, null);
			} else {
				final String fileScheme = "file:";
				java.nio.file.Path path;
				if (location.toLowerCase().startsWith(fileScheme)) {
					path = Paths.get(URI.create(location));
				} else {
					path = Paths.get(location);
				}
				if(path.toFile().exists()) {
					data = FileUtils.readFileToString(path.toFile(), "UTF-8");
				} else {
					data = ClasspathHelper.loadFileFromClasspath(location);
				}
			}

			return data;
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}

	private Optional<ImportSwaggerModelResponse> validateSwaggerModel(final String swaggerUrl) {
		ImportSwaggerModelResponse response = ImportSwaggerModelResponse.T.create();
		try {
			String content = getContentFromUrl(swaggerUrl);
			String result = SwaggerValidator.validateSwaggerContent(content);
			if (Objects.nonNull(result)) {
				response.getNotifications().addAll(
						Notifications.build()
								.add()
								.message().confirmError(result)
								.close()
								.list()
				);
				return Optional.of(response);
			}
		} catch(Exception e) {
			logger.error(e);
			response.getNotifications().addAll(
					Notifications.build()
							.add()
							.message().confirmError("Cannot import swagger model", e)
							.close()
							.list()
			);
			return Optional.of(response);
		}
		return Optional.empty();
	}

	private ImportSwaggerModelResponse prepareResponse(GmMetaModel importedModel) {
		ImportSwaggerModelResponse response = ImportSwaggerModelResponse.T.create();
		response.setModel(importedModel);

		response.getNotifications().addAll(
				Notifications.build()
						.add()
						.message().confirmInfo("Imported Model from Swagger definition as: "+importedModel.getName())
						.command().gotoModelPath("Imported Model")
						.addElement(importedModel)
						.close()
						.close()
						.list()
		);
		return response;
	}

	private List<GmMetaModel> getGmMetaModels(PersistenceGmSession session, List<GmMetaModel> models) {
		GenericExchangePayload payload = GenericExchangePayload.T.create();
		payload.setAssembly(models);

		GenericImporterContext<GenericExchangePayload> importContext = new GenericImporterContext<>();
		importContext.setAssembly(payload);
		importContext.setDefaultPartition("cortex");
		importContext.setSession(session);
		importContext.setExternalPredicate(e -> externalReferences.contains(e));
		importContext.setEnvelopePredicate(e -> e == payload);

		GenericExchangePayload importedPayload = AssemblyImporter.importAssembly(importContext);


		return (List<GmMetaModel>) importedPayload.getAssembly();
	}

}
