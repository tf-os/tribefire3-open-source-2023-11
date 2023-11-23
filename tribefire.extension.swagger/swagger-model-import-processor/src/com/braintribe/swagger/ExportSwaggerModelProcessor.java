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

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.ddra.endpoints.api.DdraEndpointsUtils;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmListType;
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmSetType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.constraint.Mandatory;
import com.braintribe.model.meta.data.prompt.Description;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessors;
import com.braintribe.model.processing.notification.api.builder.Notifications;
import com.braintribe.model.processing.resource.streaming.access.BasicResourceAccessFactory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.session.api.resource.ResourceAccess;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.FileSystemSource;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.swagger.v2_0.SwaggerApi;
import com.braintribe.model.swagger.v2_0.meta.SwaggerBasePathMd;
import com.braintribe.model.swagger.v2_0.meta.SwaggerContact;
import com.braintribe.model.swagger.v2_0.meta.SwaggerExampleMd;
import com.braintribe.model.swagger.v2_0.meta.SwaggerExternalDocsMd;
import com.braintribe.model.swagger.v2_0.meta.SwaggerExternalDocumentationObject;
import com.braintribe.model.swagger.v2_0.meta.SwaggerHostMd;
import com.braintribe.model.swagger.v2_0.meta.SwaggerInfoMd;
import com.braintribe.model.swagger.v2_0.meta.SwaggerLicense;
import com.braintribe.model.swagger.v2_0.meta.SwaggerSchemesMd;
import com.braintribe.model.swagger.v2_0.meta.SwaggerScopesObject;
import com.braintribe.model.swagger.v2_0.meta.SwaggerSecurityDefinitionsMd;
import com.braintribe.model.swagger.v2_0.meta.SwaggerSecurityScheme;
import com.braintribe.model.swagger.v2_0.meta.SwaggerTagsMd;
import com.braintribe.model.swaggerapi.SwaggerEntitiesRequest;
import com.braintribe.model.swaggerapi.SwaggerPropertiesRequest;
import com.braintribe.model.swaggerapi.SwaggerRequest;
import com.braintribe.model.swaggerapi.SwaggerServicesRequest;
import com.braintribe.swagger.util.ExchangePackageResourceToModel;
import com.braintribe.swagger.writter.SwaggerJsonWriter;
import com.braintribe.utils.FileTools;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import io.swagger.models.Contact;
import io.swagger.models.ExternalDocs;
import io.swagger.models.Info;
import io.swagger.models.License;
import io.swagger.models.ModelImpl;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.models.auth.ApiKeyAuthDefinition;
import io.swagger.models.auth.BasicAuthDefinition;
import io.swagger.models.auth.In;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.FloatProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ExportSwaggerModelProcessor implements AccessRequestProcessor<ExportSwaggerModelRequest, ExportSwaggerModelResponse> {

	private static final Logger logger = Logger.getLogger(ExportSwaggerModelProcessor.class);

	protected Evaluator<ServiceRequest> evaluator;
	private PersistenceGmSessionFactory sessionFactory;
	private Map<String, Property> propertyRegister = new HashMap<>();

	private AccessRequestProcessor<ExportSwaggerModelRequest, ExportSwaggerModelResponse> dispatcher = AccessRequestProcessors.dispatcher(config -> {
		config.register(ExportSwaggerFromModelName.T, this::exportSwaggerFromModelName);
		config.register(ExportSwaggerFromGmMetaModel.T, this::exportSwaggerFromModel);
		config.register(ExportSwaggerApiMetaModel.T, this::exportSwaggerApiMetaModel);
		config.register(ExportSwaggerApiServiceModel.T, this::exportSwaggerApiServiceModel);
		config.register(ExportSwaggerFromExchangePackageResource.T, this::exportSwaggerFromExchangePackage);
		config.register(ExportSwaggerFromExchangePackageUrl.T, this::exportSwaggerFromExchangePackageUrl);
	});

	@Override
	public ExportSwaggerModelResponse process(AccessRequestContext<ExportSwaggerModelRequest> context) {
		return dispatcher.process(context);
	}

	public ExportSwaggerModelResponse exportSwaggerFromExchangePackage(AccessRequestContext<ExportSwaggerFromExchangePackageResource> context) {
		logger.info(".............. ExportSwaggerFromExchangePackage ................ process");

		ExportSwaggerFromExchangePackageResource request = context.getRequest();
		Resource exchangePackageResource = request.getExchangePackageResource();

		if (exchangePackageResource == null)
			return modelNullResponse("Cannot export swagger from empty (null) exchange package.");

		PersistenceGmSession localSession = context.getSession();

		GmMetaModel model = new ExchangePackageResourceToModel().transform(exchangePackageResource, localSession);

		return prepareResponse(prepareSwaggerModel(model, new HashMap<>(), request.getUseFullyQualifiedDefinitionName()), localSession,
				request.getUseJSONForExport());
	}

	public ExportSwaggerModelResponse exportSwaggerFromExchangePackageUrl(AccessRequestContext<ExportSwaggerFromExchangePackageUrl> context) {
		logger.info(".............. ExportSwaggerFromExchangePackageUrl ................ process");

		ExportSwaggerFromExchangePackageUrl request = context.getRequest();
		String exchangePackageUrl = request.getExchangePackageUrl();

		if (exchangePackageUrl == null)
			return modelNullResponse("Cannot export swagger from empty (null) exchange package url.");

		PersistenceGmSession localSession = context.getSession();
		try {
			Resource exchangePackageResource = getExchangePackageResource(exchangePackageUrl, localSession);
			GmMetaModel model = new ExchangePackageResourceToModel().transform(exchangePackageResource, localSession);
			return prepareResponse(prepareSwaggerModel(model, new HashMap<>(), request.getUseFullyQualifiedDefinitionName()), localSession,
					request.getUseJSONForExport());
		} catch (Throwable t) {
			logger.error(t);
			ExportSwaggerModelResponse response = ExportSwaggerModelResponse.T.create();
			response.getNotifications().addAll(Notifications.build().add().message().confirmError("Cannot export swagger.", t).close().list());
			return response;
		}

	}

	public ExportSwaggerModelResponse exportSwaggerFromModel(AccessRequestContext<ExportSwaggerFromGmMetaModel> context) {
		logger.info(".............. ExportSwaggerFromGmMetaModel ................ process");

		ExportSwaggerFromGmMetaModel request = context.getRequest();

		GmMetaModel model = request.getModel();

		if (model == null)
			return modelNullResponse("Cannot export swagger from empty (null) model.");

		return prepareResponse(prepareSwaggerModel(model, new HashMap<>(), request.getUseFullyQualifiedDefinitionName()), context.getSession(),
				request.getUseJSONForExport());
	}

	public ExportSwaggerModelResponse exportSwaggerFromModelName(AccessRequestContext<ExportSwaggerFromModelName> context) {
		logger.info(".............. ExportSwaggerFromModelName ................ process");

		ExportSwaggerFromModelName request = context.getRequest();
		Map<String, Swagger> models = new HashMap<>();

		if (StringUtils.isBlank(request.getModelName()))
			return modelNullResponse("Cannot export swagger model! Please enter valid name of model.");

		PersistenceGmSession cortexSession = createCortexSession();
		String modelGlobalId = "model:tribefire.extension.swagger:" + request.getModelName().trim();

		GenericEntity entityModel = cortexSession.findEntityByGlobalId(modelGlobalId.trim());
		if (entityModel == null)
			return modelNullResponse("Cannot find model with global id: " + modelGlobalId);

		GmMetaModel model = (GmMetaModel) entityModel;

		return prepareResponse(prepareSwaggerModel(model, models, request.getUseFullyQualifiedDefinitionName()), context.getSession(),
				request.getUseJSONForExport());

	}

	public ExportSwaggerModelResponse exportSwaggerApiMetaModel(AccessRequestContext<ExportSwaggerApiMetaModel> context) {
		logger.info(".............. ExportSwaggerApiMetaModel ................ process");

		ExportSwaggerApiMetaModel request = context.getRequest();
		SwaggerApi swaggerApi = DdraEndpointsUtils.evaluateServiceRequest(evaluator, getSwaggerEntitiesOrPropertiesRequest(request));

		return prepareResponse(swaggerApi, context.getSession(), request.getUseJSONForExport());
	}

	public ExportSwaggerModelResponse exportSwaggerApiServiceModel(AccessRequestContext<ExportSwaggerApiServiceModel> context) {
		logger.info(".............. ExportSwaggerApiServiceModel ................ process");

		ExportSwaggerApiServiceModel request = context.getRequest();
		SwaggerServicesRequest service = SwaggerServicesRequest.T.create();
		setUpService(service, request);
		service.setServiceDomain(request.getServiceDomain());

		SwaggerApi swaggerApi = DdraEndpointsUtils.evaluateServiceRequest(evaluator, service);

		return prepareResponse(swaggerApi, context.getSession(), request.getUseJSONForExport());
	}

	private SwaggerRequest getSwaggerEntitiesOrPropertiesRequest(ExportSwaggerApiMetaModel request) {
		if (request.getPropertiesApi()) {
			SwaggerPropertiesRequest service = SwaggerPropertiesRequest.T.create();
			setUpService(service, request);
			service.setAccessId(request.getAccessId());
			service.setEnablePartition(request.getEnablePartition());
			return service;
		} else {
			SwaggerEntitiesRequest service = SwaggerEntitiesRequest.T.create();
			setUpService(service, request);
			service.setAccessId(request.getAccessId());
			service.setEnablePartition(request.getEnablePartition());
			return service;
		}

	}

	private ServiceRequest setUpService(SwaggerRequest service, ExportSwaggerApi request) {
		service.setSessionId(request.getSessionId());
		service.setResource(request.getResource());
		service.setModel(request.getModel());
		service.setUseJSONForExport(request.getUseJSONForExport());
		service.setUseFullyQualifiedDefinitionName(request.getUseFullyQualifiedDefinitionName());
		return service;
	}

	private Resource getExchangePackageResource(String exchangePackageUrl, PersistenceGmSession localSession) throws Exception {
		URL url = new URL(exchangePackageUrl);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		InputStream in = connection.getInputStream();
		File temp = new File("exchangePackage.tfx.zip");
		FileOutputStream out = new FileOutputStream(temp);
		copy(in, out, 1024);
		out.close();

		ResourceAccess resourceAccess = new BasicResourceAccessFactory().newInstance(localSession);
		InputStream fis = FileTools.newInputStream(temp);
		Resource fileResource = resourceAccess.create().name(temp.getName()).mimeType("application/zip").sourceType(FileSystemSource.T).store(fis);
		return fileResource;
	}

	private void copy(InputStream input, OutputStream output, int bufferSize) throws IOException {
		byte[] buf = new byte[bufferSize];
		int n = input.read(buf);
		while (n >= 0) {
			output.write(buf, 0, n);
			n = input.read(buf);
		}
		output.flush();
	}

	private Swagger prepareSwaggerModel(GmMetaModel model, Map<String, Swagger> models, boolean useFullyQualifiedDefinitionName) {
		Set<MetaData> metaData = model.getMetaData();

		Swagger swaggerModel = new Swagger();

		setInfo(swaggerModel, metaData);
		setTags(swaggerModel, metaData);
		setSchemes(swaggerModel, metaData);
		setSecurityDefinitions(swaggerModel, metaData);
		setBasePath(swaggerModel, metaData);
		setHost(swaggerModel, metaData);
		setExternalDocs(swaggerModel, metaData);

		setDefinitions(swaggerModel, model.getTypes(), models, useFullyQualifiedDefinitionName);

		return swaggerModel;
	}

	private ExportSwaggerModelResponse prepareResponse(Object swaggerModel, PersistenceGmSession localSession, boolean useJsonForExport) {
		ExportSwaggerModelResponse response = ExportSwaggerModelResponse.T.create();
		try {
			File temp;
			if (swaggerModel instanceof SwaggerApi) {
				File tempJson = getTempFile(true);
				Writer writer = new OutputStreamWriter(new FileOutputStream(tempJson));
				new SwaggerJsonWriter(writer, true).write((SwaggerApi) swaggerModel);
				writer.close();

				if (!useJsonForExport) {
					temp = getTempFile(false);
					ObjectMapper mapper = new ObjectMapper(new YAMLFactory().configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true)
							.configure(YAMLGenerator.Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS, true));

					mapper.setSerializationInclusion(Include.NON_NULL);
					mapper.writerWithDefaultPrettyPrinter().writeValue(temp, mapper.readTree(tempJson));
				} else
					temp = tempJson;
			} else {
				temp = getTempFile(useJsonForExport);
				ObjectMapper mapper = new ObjectMapper();
				if (!useJsonForExport)
					mapper = new ObjectMapper(new YAMLFactory().configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true)
							.configure(YAMLGenerator.Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS, true));

				mapper.setSerializationInclusion(Include.NON_NULL);
				mapper.writerWithDefaultPrettyPrinter().writeValue(temp, swaggerModel);
			}

			return resolveResponse(localSession, useJsonForExport, response, temp);

		} catch (Exception ex) {
			logger.error(ex);
			response.getNotifications()
					.addAll(Notifications.build().add().message().confirmError("Cannot export model to swagger. Please check logs for more details.")
							.close().list());
			return response;
		}
	}

	private ExportSwaggerModelResponse resolveResponse(PersistenceGmSession localSession, boolean useJsonForExport,
			ExportSwaggerModelResponse response, File temp) {
		ResourceAccess resourceAccess = new BasicResourceAccessFactory().newInstance(localSession);
		InputStream fis = FileTools.newInputStream(temp);
		Resource fileResource = resourceAccess.create().name(temp.getName()).mimeType(getMimeType(useJsonForExport)).sourceType(FileSystemSource.T)
				.store(fis);
		response.setResource(fileResource);

		response.getNotifications()
				.addAll(Notifications.build().add().message().confirmInfo("Exported swagger model as resource: " + fileResource.getName()).close()
						.list());

		return response;
	}

	private String getMimeType(boolean useJsonForExport) {
		return useJsonForExport ? "application/json" : "application/yaml";
	}

	private File getTempFile(boolean useJsonForExport) throws IOException {
		return useJsonForExport ? File.createTempFile("swaggerModel", ".json") : File.createTempFile("swaggerModel", ".yaml");
	}

	private void setInfo(Swagger swaggerModel, Set<MetaData> metaData) {
		metaData.stream().filter(SwaggerInfoMd.class::isInstance).map(SwaggerInfoMd.class::cast).findFirst().ifPresent(swaggerInfo -> {
			Info info = new Info();
			SwaggerContact swagerContact = swaggerInfo.getContact();
			if (swagerContact != null) {
				Contact contact = new Contact();
				contact.setEmail(swagerContact.getEmail());
				contact.setName(swagerContact.getName());
				contact.setUrl(swagerContact.getUrl());
				info.setContact(contact);
			}
			info.setDescription(swaggerInfo.getDescription());
			info.setVersion(swaggerInfo.getVersion());

			info.setTermsOfService(swaggerInfo.getTermsOfService());

			SwaggerLicense swaggerLicense = swaggerInfo.getLicense();
			if (swaggerLicense != null) {
				License license = new License();
				license.setName(swaggerLicense.getName());
				license.setUrl(swaggerLicense.getUrl());
				info.setLicense(license);
			}
			info.setTitle(swaggerInfo.getTitle());

			swaggerModel.setInfo(info);
		});

	}

	private void setTags(Swagger swaggerModel, Set<MetaData> metaData) {
		metaData.stream().filter(SwaggerTagsMd.class::isInstance).map(SwaggerTagsMd.class::cast).findFirst().ifPresent(swaggerTags -> {
			if (swaggerTags.getTags() != null) {
				swaggerTags.getTags().stream().forEach(swaggerTag -> {
					Tag tag = new Tag();

					tag.setDescription(swaggerTag.getDescription());
					tag.setName(swaggerTag.getName());
					SwaggerExternalDocumentationObject externalDocs = swaggerTag.getExternalDocs();
					if (externalDocs != null) {
						ExternalDocs exDocs = new ExternalDocs();
						exDocs.setDescription(externalDocs.getDescription());
						exDocs.setUrl(externalDocs.getUrl());
						tag.setExternalDocs(exDocs);
					}

					swaggerModel.tag(tag);
				});
			}
		});
	}

	private void setSchemes(Swagger swaggerModel, Set<MetaData> metaData) {
		metaData.stream().filter(SwaggerSchemesMd.class::isInstance).map(SwaggerSchemesMd.class::cast).findFirst().ifPresent(swaggerSchemesMd -> {
			if (swaggerSchemesMd.getSchemes() != null) {
				swaggerSchemesMd.getSchemes().stream().forEach(swaggerScheme -> {
					swaggerModel.scheme(Scheme.forValue(swaggerScheme));
				});
			}
		});
	}

	private void setSecurityDefinitions(Swagger swaggerModel, Set<MetaData> metaData) {
		metaData.stream().filter(SwaggerSecurityDefinitionsMd.class::isInstance).map(SwaggerSecurityDefinitionsMd.class::cast).findFirst()
				.ifPresent(swaggerSecurityDefinitionsMd -> {
					if (swaggerSecurityDefinitionsMd.getSecuritySchemes() != null) {
						swaggerSecurityDefinitionsMd.getSecuritySchemes().forEach((key, value) -> {
							SecuritySchemeDefinition ssd = buildSecuritySchemeDefinition(value);
							swaggerModel.securityDefinition(key, ssd);
						});
					}
				});
	}

	private void setExternalDocs(Swagger swaggerModel, Set<MetaData> metaData) {
		metaData.stream().filter(SwaggerExternalDocsMd.class::isInstance).map(SwaggerExternalDocsMd.class::cast).findFirst()
				.ifPresent(swaggerExternalDocs -> {
					ExternalDocs externalDocs = new ExternalDocs();
					SwaggerExternalDocumentationObject externalDocsObject = swaggerExternalDocs.getExternalDocs();
					if (externalDocsObject != null) {
						externalDocs.setDescription(externalDocsObject.getDescription());
						externalDocs.setUrl(externalDocsObject.getUrl());
						swaggerModel.setExternalDocs(externalDocs);
					}
				});
	}

	private void setBasePath(Swagger swaggerModel, Set<MetaData> metaData) {
		metaData.stream().filter(SwaggerBasePathMd.class::isInstance).map(SwaggerBasePathMd.class::cast).findFirst().ifPresent(swaggerBasePath -> {
			swaggerModel.setBasePath(swaggerBasePath.getBasePath());
		});
	}

	private void setHost(Swagger swaggerModel, Set<MetaData> metaData) {
		metaData.stream().filter(SwaggerHostMd.class::isInstance).map(SwaggerHostMd.class::cast).findFirst().ifPresent(swaggerHost -> {
			swaggerModel.setHost(swaggerHost.getHost());
		});
	}

	private void setDefinitions(Swagger swaggerModel, Set<GmType> gmTypes, Map<String, Swagger> models, boolean useFullyQualifiedDefinitionName) {
		gmTypes.forEach(gmType -> {
			buildModel(swaggerModel, gmType, models, useFullyQualifiedDefinitionName);
		});
	}

	private void buildModel(Swagger swaggerModel, GmType gmType, Map<String, Swagger> models, boolean useFullyQualifiedDefinitionName) {
		String typeSignature = gmType.getTypeSignature();
		if (models.containsKey(typeSignature)) {
			return;
		} else {
			models.put(typeSignature, swaggerModel);
		}
		switch (gmType.typeKind()) {
			case ENTITY:
				ensureEntityTypeDefinitions(swaggerModel, (GmEntityType) gmType, models, useFullyQualifiedDefinitionName);
				break;
			default:
				logger.warn("Cannot create definition from gmType: " + gmType.getTypeSignature() + ", it's not entity");
				break;
		}
	}

	private void ensureEntityTypeDefinitions(Swagger swaggerModel, GmEntityType entityType, Map<String, Swagger> models,
			boolean useFullyQualifiedDefinitionName) {
		if (swaggerModel.getDefinitions() != null && swaggerModel.getDefinitions().containsKey(entityType.getTypeSignature())) {
			return;
		}
		ModelImpl definition = new ModelImpl();
		setDefinitionMetadata(definition, entityType);
		fillEntityTypeDefinition(swaggerModel, entityType, definition, models, useFullyQualifiedDefinitionName);
		if (useFullyQualifiedDefinitionName) {
			swaggerModel.addDefinition(entityType.getTypeSignature(), definition);
		} else {
			String typeSignature = entityType.getTypeSignature();
			int idx = typeSignature.lastIndexOf(".");
			String definitionName = idx >= 0 ? typeSignature.substring(idx + 1, typeSignature.length()) : typeSignature;
			swaggerModel.addDefinition(definitionName, definition);
		}
	}

	private void fillEntityTypeDefinition(Swagger swaggerModel, GmEntityType entityType, ModelImpl definition, Map<String, Swagger> models,
			boolean useFullyQualifiedDefinitionName) {
		definition.setTitle(getDefinitionTitle(entityType));
		definition.setType("object");

		if (entityType.getDeclaringModel() == null) {
			logger.warn("EntityType: " + entityType.entityType() + " has no declared model!");
			return;
		}

		entityType.getProperties().stream().forEach(property -> {
			try {
				Property definitionProperty = buildProperty(swaggerModel, property.getType(), models, useFullyQualifiedDefinitionName);
				setPropertyMetadata(definitionProperty, property);
				definition.property(property.getName(), definitionProperty);
				if (isMandatory(property)) {
					definition.required(property.getName());
				}
			} catch (Throwable e) {
				logger.warn("Exception handling property: " + property.getName());
				logger.warn("Exception message: " + e.getMessage());
				logger.error("Exception handling property: " + property.getName(), e);
				e.printStackTrace();
			}
		});
	}

	private void setDefinitionMetadata(ModelImpl definition, GmEntityType gmEntityType) {
		Set<MetaData> metaData = gmEntityType.getMetaData();
		setDefinitionExample(definition, metaData);
	}

	private void setDefinitionExample(ModelImpl definition, Set<MetaData> metaData) {
		metaData.stream().filter(SwaggerExampleMd.class::isInstance).map(SwaggerExampleMd.class::cast).findFirst().ifPresent(exqmple -> {
			definition.setExample((Object) exqmple.getExample());
		});
	}

	private void setPropertyMetadata(Property definitionProperty, GmProperty property) {
		Set<MetaData> metaData = property.getMetaData();
		setPropertyDescription(definitionProperty, metaData);
		setPropertyExample(definitionProperty, metaData);
	}

	private void setPropertyDescription(Property definitionProperty, Set<MetaData> metaData) {
		metaData.stream().filter(Description.class::isInstance).map(Description.class::cast).findFirst().ifPresent(description -> {
			definitionProperty.setDescription(description.getDescription().value());
		});
	}

	private void setPropertyExample(Property definitionProperty, Set<MetaData> metaData) {
		metaData.stream().filter(SwaggerExampleMd.class::isInstance).map(SwaggerExampleMd.class::cast).findFirst().ifPresent(exqmple -> {
			definitionProperty.setExample((Object) exqmple.getExample());
		});
	}

	private boolean isMandatory(GmProperty property) {
		Set<MetaData> metaData = property.getMetaData();
		boolean result = false;
		result = metaData.stream().filter(Mandatory.class::isInstance).map(Mandatory.class::cast).findFirst().map(mandatory -> {
			return mandatory != null;
		}).isPresent();

		return result;
	}

	private String getDefinitionTitle(GmEntityType entityType) {
		String entityName = getEntityName(entityType);
		if (StringUtils.isNoneBlank(entityName)) {
			return entityName;
		}
		String typeSignature = entityType.getTypeSignature();
		int idx = typeSignature.lastIndexOf(".");
		return idx >= 0 ? typeSignature.substring(idx + 1, typeSignature.length()) : typeSignature;
	}

	private String getEntityName(GmEntityType entityType) {
		Set<MetaData> metaData = entityType.getMetaData();
		Optional<String> posibleName = metaData.stream().filter(Name.class::isInstance).map(Name.class::cast).findFirst().map(name -> {
			return name.getName().value();
		});
		String result = null;
		if (posibleName.isPresent()) {
			result = posibleName.get();
		}
		return result;
	}

	private Property buildProperty(Swagger swaggerModel, GmType gmType, Map<String, Swagger> models, boolean useFullyQualifiedDefinitionName) {

		Property property = null;

		switch (gmType.typeKind()) {
			case BASE:
				property = new ObjectProperty();
				break;
			case BOOLEAN:
				property = new BooleanProperty();
				break;
			case DOUBLE:
				property = new DoubleProperty();
				break;
			case ENTITY:
				RefProperty refProperty = new RefProperty();
				buildModel(swaggerModel, gmType, models, useFullyQualifiedDefinitionName);
				refProperty.set$ref(getRef((GmEntityType) gmType));
				property = refProperty;
				break;
			case FLOAT:
				property = new FloatProperty();
				break;
			case INTEGER:
				property = new IntegerProperty();
				break;
			case LIST:
				ArrayProperty arrayProperty = new ArrayProperty();
				GmListType listType = (GmListType) gmType;
				arrayProperty.items(buildProperty(swaggerModel, listType.getElementType(), models, useFullyQualifiedDefinitionName));
				property = arrayProperty;
				break;
			case STRING:
				property = new StringProperty();
				break;
			case ENUM:
				StringProperty enumProperty = new StringProperty();
				GmEnumType enumType = (GmEnumType) gmType;
				enumType.getConstants().stream().map(constant -> constant.getName()).forEach(value -> enumProperty._enum(value));
				property = enumProperty;
				break;
			case DATE:
				property = new DateProperty();
				break;
			case DECIMAL:
				property = new DoubleProperty();
				break;
			case LONG:
				property = new LongProperty();
				break;
			case MAP:
				MapProperty mapProperty = new MapProperty();
				GmMapType mapType = (GmMapType) gmType;
				if (mapType.getKeyType().isGmSimple()) {
					mapProperty.setAdditionalProperties(buildProperty(swaggerModel, mapType.getValueType(), models, useFullyQualifiedDefinitionName));
				} else {
					ObjectProperty temp = new ObjectProperty();
					HashMap<String, Property> tempMap = new HashMap<>();
					tempMap.put("key", buildProperty(swaggerModel, mapType.getKeyType(), models, useFullyQualifiedDefinitionName));
					tempMap.put("value", buildProperty(swaggerModel, mapType.getValueType(), models, useFullyQualifiedDefinitionName));
					temp.properties(tempMap);
					mapProperty.additionalProperties(temp);
				}
				property = mapProperty;
				break;
			case SET:
				if (propertyRegister.containsKey(gmType.getTypeSignature())) {
					return propertyRegister.get(gmType.getTypeSignature());
				} else {
					arrayProperty = new ArrayProperty();
					arrayProperty.setType("array");
					propertyRegister.put(gmType.getTypeSignature(), arrayProperty);
					GmSetType setType = (GmSetType) gmType;
					arrayProperty.items(buildProperty(swaggerModel, setType.getElementType(), models, useFullyQualifiedDefinitionName));
					property = arrayProperty;
				}
				break;
			default:
				break;
		}

		return property;
	}

	private String getRef(GmEntityType type) {
		return "#/definitions/" + type.getTypeSignature();
	}

	private SecuritySchemeDefinition buildSecuritySchemeDefinition(SwaggerSecurityScheme swaggerSecurityScheme) {
		switch (swaggerSecurityScheme.getType()) {
			case "apiKey":
				ApiKeyAuthDefinition apiKeyAuthDefinition = new ApiKeyAuthDefinition();
				apiKeyAuthDefinition.setDescription(swaggerSecurityScheme.getDescription());
				apiKeyAuthDefinition.setName(swaggerSecurityScheme.getName());
				if (In.HEADER.toValue().equalsIgnoreCase(swaggerSecurityScheme.getIn()) || In.QUERY.toValue()
						.equalsIgnoreCase(swaggerSecurityScheme.getIn())) {
					apiKeyAuthDefinition.setIn(In.forValue(swaggerSecurityScheme.getIn()));
				}
				return apiKeyAuthDefinition;
			case "basic":
				BasicAuthDefinition basicAuthDefinition = new BasicAuthDefinition();
				basicAuthDefinition.setDescription(swaggerSecurityScheme.getDescription());
				return basicAuthDefinition;
			case "oauth2":
				OAuth2Definition oAuth2Definition = new OAuth2Definition();
				oAuth2Definition.setAuthorizationUrl(swaggerSecurityScheme.getAuthorizationUrl());
				oAuth2Definition.setDescription(swaggerSecurityScheme.getDescription());
				oAuth2Definition.setFlow(swaggerSecurityScheme.getFlow());
				oAuth2Definition.setTokenUrl(swaggerSecurityScheme.getTokenUrl());
				SwaggerScopesObject scopes = swaggerSecurityScheme.getScopes();
				if (scopes != null && scopes.getScopes() != null) {
					oAuth2Definition.setScopes(scopes.getScopes());
				}
				return oAuth2Definition;
			default:
				return null;
		}
	}

	private ExportSwaggerModelResponse modelNullResponse(String message) {
		ExportSwaggerModelResponse response = ExportSwaggerModelResponse.T.create();
		response.getNotifications().addAll(Notifications.build().add().message().confirmError(message).close().list());
		return response;
	}

	private PersistenceGmSession createCortexSession() {
		try {
			return sessionFactory.newSession("cortex");
		} catch (Exception ex) {
			logger.error("Cannot create cortex sesssion", ex);
			return null;
		}

	}

	@Required
	public void setSessionFactory(PersistenceGmSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Required
	@Configurable
	public void setEvaluator(Evaluator<ServiceRequest> evaluator) {
		this.evaluator = evaluator;
	}

}
