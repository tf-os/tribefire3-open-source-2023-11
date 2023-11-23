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
package com.braintribe.model.processing.ddra.endpoints.rest.v2;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.ddra.endpoints.api.rest.v2.CrudRequestTarget;
import com.braintribe.ddra.endpoints.api.rest.v2.RestV2EndpointContext;
import com.braintribe.logging.Logger;
import com.braintribe.model.ddra.endpoints.v2.DdraUrlPathParameters;
import com.braintribe.model.ddra.endpoints.v2.RestV2Endpoint;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.ScalarType;
import com.braintribe.model.processing.ddra.endpoints.AbstractDdraRestServlet;
import com.braintribe.model.processing.ddra.endpoints.rest.v2.handlers.RestV2Handler;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.web.rest.HttpExceptions;
import com.braintribe.model.processing.web.rest.UrlPathCodec;
import com.braintribe.model.processing.web.rest.impl.HttpRequestEntityDecoderUtils;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.utils.StringTools;

public class RestV2Server extends AbstractDdraRestServlet<RestV2EndpointContext<RestV2Endpoint>> {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(RestV2Server.class);

	private static final String PROPERTIES_BASE_PATH = "properties";
	private static final String ENTITIES_BASE_PATH = "entities";
	private static final String SWAGGER_BASE_PATH = "swaggerentities";
	private static final String EXPORT_SWAGGER_BASE_PATH = "exportswaggerentities";
	private static final String SWAGGER_PROPERTIES_BASE_PATH = "swaggerproperties";
	private static final String EXPORT_SWAGGER_PROPERTIES_BASE_PATH = "exportswaggerproperties";

	public static final UrlPathCodec<GenericEntity> ENTITIES_URL_CODEC = UrlPathCodec.create() //
			.constantSegment(ENTITIES_BASE_PATH) //
			.mappedSegment("accessId") //
			.mappedSegment("typeSignature") //
			.mappedSegment("entityIdStringValue") //
			.mappedSegment("entityPartition");

	public static final UrlPathCodec<GenericEntity> PROPERTIES_URL_CODEC = UrlPathCodec.create() //
			.constantSegment(PROPERTIES_BASE_PATH) //
			.mappedSegment("accessId") //
			.mappedSegment("typeSignature") //
			.mappedSegment("entityIdStringValue") //
			.mappedSegment("entityPartition", true) //
			.mappedSegment("property");

	public static final UrlPathCodec<GenericEntity> SWAGGER_URL_CODEC = UrlPathCodec.create() //
			.constantSegment(SWAGGER_BASE_PATH) //
			.mappedSegment("accessId");

	public static final UrlPathCodec<GenericEntity> EXPORT_SWAGGER_URL_CODEC = UrlPathCodec.create() //
			.constantSegment(EXPORT_SWAGGER_BASE_PATH) //
			.mappedSegment("accessId");

	public static final UrlPathCodec<GenericEntity> SWAGGER_PROP_URL_CODEC = UrlPathCodec.create() //
			.constantSegment(SWAGGER_PROPERTIES_BASE_PATH) //
			.mappedSegment("accessId");

	public static final UrlPathCodec<GenericEntity> EXPORT_SWAGGER_PROP_URL_CODEC = UrlPathCodec.create() //
			.constantSegment(EXPORT_SWAGGER_PROPERTIES_BASE_PATH) //
			.mappedSegment("accessId");

	public static String getUrlFor(HttpServletRequest request, DdraUrlPathParameters parameters) {
		String encoded = parameters.getProperty() != null ? PROPERTIES_URL_CODEC.encode(parameters) : ENTITIES_URL_CODEC.encode(parameters);

		StringBuffer url = request.getRequestURL();
		int length = url.length();
		// +1 for the additional "/"
		url.replace(length - request.getPathInfo().length() + 1, length, encoded);

		return url.toString();
	}

	private ModelAccessoryFactory modelAccessoryFactory;

	private Predicate<String> accessAvailability = a -> true;

	private Map<String, RestV2Handler<RestV2Endpoint>> handlers;

	@Override
	protected void handle(RestV2EndpointContext<RestV2Endpoint> context) throws IOException {
		String method = context.getRequest().getMethod();
		String url = context.getTarget().getUrl();
		RestV2Handler<RestV2Endpoint> handler = getHandler(url, method);
		if (handler == null)
			HttpExceptions.methodNotAllowed("Unsupported method %s", method);
		handler.handle(context);
	}

	private RestV2Handler<RestV2Endpoint> getHandler(String url, String method) {
		return handlers.get(method.concat(":").concat(url));
	}

	@Override
	protected RestV2EndpointContext<RestV2Endpoint> createContext(HttpServletRequest request, HttpServletResponse response) {
		String url = getRequestTarget(request).getUrl();
		RestV2Handler<RestV2Endpoint> handler = getHandler(url, request.getMethod());
		RestV2EndpointContext<RestV2Endpoint> context = handler.createContext(request, response);
		context.setEvaluator(evaluator);
		return context;
	}

	@Override
	protected boolean fillContext(RestV2EndpointContext<RestV2Endpoint> context) {
		context.setTarget(getRequestTarget(context.getRequest()));
		computeUrlParameters(context);

		if (isSwaggerTarget(context))
			return true;

		checkAccess(context);
		computeEntityType(context);

		if (isSwaggerTarget(context))
			return true;

		computeEntityIdIfNecessary(context);
		computePropertyIfNecessary(context);
		
		return true;
	}

	private boolean isSwaggerTarget(RestV2EndpointContext<RestV2Endpoint> context) {
		return context.getTarget() == CrudRequestTarget.SWAGGER || context.getTarget() == CrudRequestTarget.EXPORT_SWAGGER
				|| context.getTarget() == CrudRequestTarget.EXPORT_SWAGGER_PROPERTIES || context.getTarget() == CrudRequestTarget.SWAGGER_PROPERTIES;
	}

	private void computeUrlParameters(RestV2EndpointContext<RestV2Endpoint> context) {
		DdraUrlPathParameters parameters = DdraUrlPathParameters.T.create();
		context.setParameters(parameters);
		String pathInfo = getPathInfo(context.getRequest());
		if (pathInfo == null) {
			return;
		}
		switch (context.getTarget()) {
			case ENTITY:
				ENTITIES_URL_CODEC.decode(() -> parameters, pathInfo);
				break;
			case PROPERTY:
				PROPERTIES_URL_CODEC.decode(() -> parameters, pathInfo);
				break;
			case SWAGGER:
				SWAGGER_URL_CODEC.decode(() -> parameters, pathInfo);
				break;
			case EXPORT_SWAGGER:
				EXPORT_SWAGGER_URL_CODEC.decode(() -> parameters, pathInfo);
				break;
			case SWAGGER_PROPERTIES:
				SWAGGER_PROP_URL_CODEC.decode(() -> parameters, pathInfo);
				break;
			case EXPORT_SWAGGER_PROPERTIES:
				EXPORT_SWAGGER_PROP_URL_CODEC.decode(() -> parameters, pathInfo);
				break;
		}
	}

	private void checkAccess(RestV2EndpointContext<RestV2Endpoint> context) {
		DdraUrlPathParameters parameters = context.getParameters();
		if (parameters.getAccessId() == null) {
			resolveTargetForSwagger(context);
			return;
		}

		if (!accessAvailability.test(parameters.getAccessId())) {
			EntityQuery query = EntityQueryBuilder.from(com.braintribe.model.accessdeployment.IncrementalAccess.class).where().property("externalId")
					.eq(parameters.getAccessId()).done();
			PersistenceGmSession session = systemSessionFactory.newSession(cortexAccessId);
			GenericEntity access = session.query().entities(query).first();
			if (access == null)
				HttpExceptions.notFound("No access with externalId " + parameters.getAccessId() + " found in the " + cortexAccessId + " database.");
			else
				HttpExceptions.expectationFailed("The access " + parameters.getAccessId() + " exists but is not deployed.");
		}
	}

	private void resolveTargetForSwagger(RestV2EndpointContext<RestV2Endpoint> context) {
		if (context.getTarget() == CrudRequestTarget.ENTITY)
			context.setTarget(CrudRequestTarget.SWAGGER);
		else if (context.getTarget() == CrudRequestTarget.PROPERTY)
			context.setTarget(CrudRequestTarget.SWAGGER_PROPERTIES);
	}

	private void computeEntityType(RestV2EndpointContext<RestV2Endpoint> context) {
		DdraUrlPathParameters parameters = context.getParameters();
		String typeSignature = parameters.getTypeSignature();

		if (typeSignature == null) {
			resolveTargetForSwagger(context);
			return;
		}

		if (typeSignature.contains(".")) {
			try {
				context.setEntityType(EntityTypes.get(typeSignature));
			} catch (GenericModelException e) {
				HttpExceptions.notFound("Entity type %s not found.", typeSignature);
			}
		} else {
			context.setEntityType(getBySimpleName(parameters));
		}
	}

	private EntityType<?> getBySimpleName(DdraUrlPathParameters parameters) {
		ModelAccessory accessory = modelAccessoryFactory.getForAccess(parameters.getAccessId());
		String suffix = "." + parameters.getTypeSignature();
		List<EntityType<?>> types = accessory.getOracle().getTypes().onlyEntities().filter(type -> type.getTypeSignature().endsWith(suffix))
				.<EntityType<?>> asTypes().collect(Collectors.toList());

		if (types.isEmpty()) {
			HttpExceptions.notFound("Cannot find entity type with simple name %s in model %s", parameters.getTypeSignature(),
					accessory.getModel().getName());
		}
		if (types.size() > 1) {
			HttpExceptions.badRequest("Found multiple (at least 2) entities with simple name %s in access %s: %s and %s",
					parameters.getTypeSignature(), parameters.getAccessId(), types.get(0).getTypeSignature(), types.get(1).getTypeSignature());
		}

		return types.get(0);
	}

	private void computeEntityIdIfNecessary(RestV2EndpointContext<RestV2Endpoint> context) {
		DdraUrlPathParameters parameters = context.getParameters();

		if (context.getTarget() == CrudRequestTarget.PROPERTY && parameters.getEntityIdStringValue() == null) {
			HttpExceptions.badRequest(
					"Expected URL of the form /properties/accessId/entity.TypeSignature/id(/partition)/propertyName but the id was not specified.");
		}

		if (parameters.getEntityIdStringValue() != null) {
			ScalarType idType = getIdType(context);
			parameters.setEntityId(parse(idType, parameters.getEntityIdStringValue()));
		}
	}

	private ScalarType getIdType(RestV2EndpointContext<RestV2Endpoint> context) {
		ModelAccessory accessory = modelAccessoryFactory.getForAccess(context.getParameters().getAccessId());
		return accessory.getCmdResolver().getIdType(context.getEntityType().getTypeSignature());
	}

	private void computePropertyIfNecessary(RestV2EndpointContext<RestV2Endpoint> context) {
		if (context.getTarget() == CrudRequestTarget.ENTITY) {
			return;
		}

		DdraUrlPathParameters parameters = context.getParameters();
		if (parameters.getProperty() == null) {
			HttpExceptions.badRequest(
					"Expected URL of the form /properties/accessId/entity.TypeSignature/id(/partition)/propertyName but the propertyName was not specified.");
		}

		Property property = context.getEntityType().findProperty(parameters.getProperty());
		if (property == null) {
			HttpExceptions.notFound("No property with name %s found in entityType %s.", parameters.getProperty(),
					context.getEntityType().getTypeSignature());
		}

		context.setProperty(property);
	}

	private CrudRequestTarget getRequestTarget(HttpServletRequest request) {
		String path = getPathInfo(request);
		
		if (path == null) {
			path = ENTITIES_BASE_PATH;
		}
		
		// TODO: Get rid of this as soon as swagger is extracted to webapp
		if (StringTools.countOccurrences(path, "/") == 1){
			if (path.startsWith(ENTITIES_BASE_PATH)) 
				return CrudRequestTarget.SWAGGER;
			if (path.startsWith(PROPERTIES_BASE_PATH))
				return CrudRequestTarget.SWAGGER_PROPERTIES;
		}
			
		if (path.startsWith(PROPERTIES_BASE_PATH)) {
			return CrudRequestTarget.PROPERTY;
		}
		if (path.startsWith(ENTITIES_BASE_PATH)) {
			return CrudRequestTarget.ENTITY;
		}
		if (path.startsWith(SWAGGER_BASE_PATH)) {
			return CrudRequestTarget.SWAGGER;
		}
		if (path.startsWith(EXPORT_SWAGGER_BASE_PATH)) {
			return CrudRequestTarget.EXPORT_SWAGGER;
		}
		if (path.startsWith(SWAGGER_PROPERTIES_BASE_PATH)) {
			return CrudRequestTarget.SWAGGER_PROPERTIES;
		}
		if (path.startsWith(EXPORT_SWAGGER_PROPERTIES_BASE_PATH)) {
			return CrudRequestTarget.EXPORT_SWAGGER_PROPERTIES;
		}
	

		HttpExceptions.notFound("Expected URL of the form entities/... or properties/... but got: %s", path);
		return null;
	}

	private String getPathInfo(HttpServletRequest request) {
		String pathInfo = request.getPathInfo();
		if (pathInfo == null) {
			return null;
		}
		return pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
	}

	private Object parse(ScalarType type, String encodedValue) {
		switch (type.getTypeCode()) {
			case booleanType:
				return Boolean.parseBoolean(encodedValue);
			case dateType:
				return HttpRequestEntityDecoderUtils.parseDate(encodedValue);
			case decimalType:
				return new BigDecimal(encodedValue);
			case doubleType:
				return Double.parseDouble(encodedValue);
			case stringType:
				return encodedValue;
			case floatType:
				return Float.parseFloat(encodedValue);
			case integerType:
				return Integer.parseInt(encodedValue);
			case longType:
				return Long.parseLong(encodedValue);
			case enumType:
				return ((EnumType) type).getInstance(encodedValue);
			default:
				HttpExceptions.badRequest("Unsupported ID type %s", type.getTypeName());
				return null;
		}
	}

	@Required
	@Configurable
	public void setModelAccessoryFactory(ModelAccessoryFactory modelAccessoryFactory) {
		this.modelAccessoryFactory = modelAccessoryFactory;
	}

	@Required
	@Configurable
	public void setHandlers(Map<String, RestV2Handler<RestV2Endpoint>> handlers) {
		this.handlers = handlers;
	}

	public Map<String, RestV2Handler<RestV2Endpoint>> getHandlers() {
		return handlers;
	}

	@Configurable
	public void setAccessAvailability(Predicate<String> accessAvailability) {
		this.accessAvailability = accessAvailability;
	}

	@Configurable
	public void setCortexAccessId(String cortexAccessId) {
		this.cortexAccessId = cortexAccessId;
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}

}
