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
import java.util.HashMap;
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
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.builder.meta.MetaModelBuilder;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.pr.criteria.matching.StandardMatcher;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.typecondition.TypeConditions;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmSetType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.constraint.Mandatory;
import com.braintribe.model.meta.data.prompt.Description;
import com.braintribe.model.meta.data.prompt.Hidden;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.meta.selector.UseCaseSelector;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessors;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.notification.api.builder.Notifications;
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
import io.swagger.models.RefModel;
import io.swagger.models.Scheme;
import io.swagger.models.SecurityRequirement;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.models.auth.ApiKeyAuthDefinition;
import io.swagger.models.auth.BasicAuthDefinition;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.models.auth.SecuritySchemeDefinition;
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

public class ConvertSwaggerModelProcessor implements AccessRequestProcessor<ConvertSwaggerModelRequest,GmMetaModel> {

	private static final Logger logger = Logger.getLogger(ConvertSwaggerModelProcessor.class);
	
	private static final ModelOracle modelOracle = new BasicModelOracle(GMF.getTypeReflection().getModel("com.braintribe.gm:root-model").getMetaModel());
	public GmMetaModel rootModel = modelOracle.getGmMetaModel();

	private static final String GROUP_NAME = "tribefire.extension.swagger:";

	private final AccessRequestProcessor<ConvertSwaggerModelRequest, GmMetaModel> dispatcher = AccessRequestProcessors.dispatcher(config->{
		config.register(ConvertSwaggerFromUrlToModel.T, this::convertSwaggerFromUrlToModel);
	});

	private final Map<String,GmType> typeRegistry = new HashMap<>();
	private Map<String,Map<String, Property>> propertyRegistry = new HashMap<>();
	private String defaultNamespace = "";
	private boolean doNotConvertMetadata = false;
	private boolean disableValidation;
	
	@Override
	public GmMetaModel process(AccessRequestContext<ConvertSwaggerModelRequest> context) {
		return dispatcher.process(context);
	}

	public GmMetaModel convertSwaggerFromUrlToModel(AccessRequestContext<ConvertSwaggerFromUrlToModel> context) {
		ConvertSwaggerFromUrlToModel request = context.getRequest();
		init(request);
		
		String swaggerUrl = request.getSwaggerUrl();
		if (StringUtils.isBlank(swaggerUrl)) {
			logger.error("Swagger URL is blank.");
			return null;
		}
		
		Optional<ImportSwaggerModelResponse> validationResult = disableValidation ? Optional.empty() : validateSwaggerModel(swaggerUrl);
		if (validationResult.isPresent()) {
			logger.error("Validation of swagger failed.");
			return null;
		}

		Swagger swagger = parseSwagger(swaggerUrl);
		if (swagger == null) {
			logger.error("Swagger model is NULL, please use version 2.0 of openapi.");
			return null;
		}

		GmMetaModel metaModel = buildModel(swagger);
		if(doNotConvertMetadata)
			return copyModelSkeleton(metaModel);
		else
			return metaModel;

	}
	
	public static GmMetaModel copyModelSkeleton(GmMetaModel metaModel) {
		
		final TraversingCriterion tc = TC.create()
			    .typeCondition(
			        TypeConditions.isAssignableTo(MetaData.T)
			    )
			.done();
		
		final StandardMatcher matcher = new StandardMatcher();
		matcher.setCriterion(tc);
		matcher.setCheckOnlyProperties(false);

		final StandardCloningContext cloningContext = new StandardCloningContext();
		cloningContext.setMatcher(matcher);
		cloningContext.setStrategyOnCriterionMatch(StrategyOnCriterionMatch.skip);
		
		return metaModel.clone(cloningContext);
	}
	
	private void init(ConvertSwaggerFromUrlToModel request) {
		defaultNamespace = request.getNamespace();
		propertyRegistry = new HashMap<>();
		doNotConvertMetadata = request.getWithoutMetadata();
		disableValidation = request.getDisableValidation();
	}

	private GmMetaModel buildModel(Swagger swagger) {
		GmMetaModel metaModel = getGmMetaModel(swagger);
		
		Info swaggerInfo = getDefaultInfoIfNotPresent(swagger.getInfo());
		
		String typePackage = buildModelName(null, swaggerInfo.getTitle(), "-model").replaceAll("-", "").toLowerCase();

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
	
	private Info getDefaultInfoIfNotPresent(Info swaggerInfo) {
		if (swaggerInfo == null) {
			swaggerInfo = new Info();
			swaggerInfo.setTitle("default_title_" + RandomStringUtils.randomAlphabetic(5));
			swaggerInfo.setVersion("1.0");
		}
		return swaggerInfo;
	}

	private GmMetaModel getGmMetaModel(final Swagger swagger) {
		Info swaggerInfo = getDefaultInfoIfNotPresent(swagger.getInfo());
		String modelName = buildModelName(GROUP_NAME, swaggerInfo.getTitle(), "-model");
		String modelVersion = buildVersion(swaggerInfo.getVersion());

		GmMetaModel metaModel = MetaModelBuilder.metaModel(modelName);
		metaModel.setVersion(modelVersion);
		metaModel.setGlobalId("model:"+modelName);
		metaModel.getDependencies().add(modelOracle.getGmMetaModel());

		addMetaDataIfNecessary(metaModel.getMetaData(), buildNameMd(swaggerInfo.getTitle(), modelName));
		addMetaDataIfNecessary(metaModel.getMetaData(), buildDescriptionMd(swaggerInfo.getDescription(), modelName));
		
		return metaModel;
	}

	private GmEntityType buildGmEntityType(GmMetaModel metaModel, String typePackage, String baseName, Model swaggerType) {
		String typeSignature = buildTypeSiganture(typePackage, baseName);

		GmEntityType entityType = MetaModelBuilder.entityType(typeSignature);
		entityType.setDeclaringModel(metaModel);
		entityType.getSuperTypes().add(modelOracle.getEntityTypeOracle(GenericEntity.T).asGmEntityType());
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
			return modelOracle.getGmBaseType();
		}
		ObjectProperty propertyObject = (ObjectProperty) propertyDetails;
		String propertyTypeSignature = buildTypeSiganture(entityType.getTypeSignature() + "." + "api", propertyName);
		GmEntityType propertyEntityType = MetaModelBuilder.entityType(propertyTypeSignature);
		propertyEntityType.setDeclaringModel(entityType.getDeclaringModel());
		propertyEntityType.getSuperTypes().add(modelOracle.getEntityTypeOracle(GenericEntity.T).asGmEntityType());
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
		if (Objects.nonNull(md) && !doNotConvertMetadata) {
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

	private Swagger parseSwagger(String swaggerUrl) {
		SwaggerParser parser = new SwaggerParser();
		return parser.read(swaggerUrl);
	}

	private GmType getBaseGmType(String type, String format) {
		switch (type) {
			case StringProperty.TYPE:
				switch (format) {
					case "date":
					case "date-time":
						return modelOracle.getGmDateType();
				}
				return modelOracle.getGmStringType();
			case BaseIntegerProperty.TYPE:
				switch (format) {
					case LongProperty.FORMAT:
						return modelOracle.getGmLongType();
				}
				return modelOracle.getGmIntegerType();
			case DecimalProperty.TYPE:
				switch (format) {
					case FloatProperty.FORMAT:
						return modelOracle.getGmFloatType();
					case DoubleProperty.FORMAT:
						return modelOracle.getGmDoubleType();
				}
				return modelOracle.getGmDecimalType();
			case BooleanProperty.TYPE:
				return modelOracle.getGmBooleanType();
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
			return modelOracle.getGmBaseType();
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

				GmSetType setType = (GmSetType) typeRegistry.get(typeSignature);
				if (Objects.isNull(setType)) {
					setType = GmSetType.T.create("type:"+typeSignature);
					setType.setTypeSignature(typeSignature);
					setType.setElementType(elementType);
					typeRegistry.put(typeSignature, setType);
				}
				return setType;

		}
		return modelOracle.getGmBaseType();

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
			if (elementType == null) return modelOracle.getGmBaseType();
			
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

		return modelOracle.getGmBaseType();

	}

	private GmType getReferenceGmType(RefModel refModel) {
		String simpleRef = refModel.getSimpleRef();
		String reference = refModel.get$ref().substring(14);
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
	
}
