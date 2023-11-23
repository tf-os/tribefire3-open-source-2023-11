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

import java.util.List;
import java.util.Map;

import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.ddra.endpoints.v2.DdraDeletePropertiesEndpoint;
import com.braintribe.model.ddra.endpoints.v2.DdraGetPropertiesEndpoint;
import com.braintribe.model.ddra.endpoints.v2.DdraPutPropertiesEndpoint;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SimpleTypes;
import com.braintribe.model.openapi.v3_0.OpenApi;
import com.braintribe.model.openapi.v3_0.OpenapiOperation;
import com.braintribe.model.openapi.v3_0.OpenapiParameter;
import com.braintribe.model.openapi.v3_0.OpenapiPath;
import com.braintribe.model.openapi.v3_0.OpenapiRequestBody;
import com.braintribe.model.openapi.v3_0.api.OpenapiPropertiesRequest;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;

/**
 * This processor generically creates an {@link OpenApi} document for REST property queries for a given access.
 * 
 * @author Neidhart.Orlich
 *
 */
public class PropertyOpenapiProcessor extends AbstractOpenapiProcessor<OpenapiPropertiesRequest> {

	private static final EntityType<?> manipulationResponseType = ManipulationResponse.T;

	@Override
	protected void init() {
		// create responses in the standardComponentsContext
		registerStandardResponse("200", manipulationResponseType, "Successful manipulation result.");
	}

	@Override
	protected void process(OpenapiContext sessionScopedContext, OpenapiPropertiesRequest request, OpenApi openApi) {
		Map<String, OpenapiPath> paths = openApi.getPaths();
		
		ComponentScope sessionEndpointScope = new ComponentScope(sessionScopedContext.getComponentScope(), standardComponentsContext.getComponentScope().getCmdResolver());
		OpenapiContext endpointParametersResolvingContext = standardComponentsContext.childContext("-ENDPOINTS-SESSION", sessionEndpointScope, OpenapiMimeType.URLENCODED); 
		endpointParametersResolvingContext.transferRequestDataFrom(sessionScopedContext);
		
		List<OpenapiParameter> endpointParametersGet = getQueryParameterRefs(DdraGetPropertiesEndpoint.T, endpointParametersResolvingContext, "endpoint");
		List<OpenapiParameter> endpointParametersDelete = getQueryParameterRefs(DdraDeletePropertiesEndpoint.T, endpointParametersResolvingContext, "endpoint");
		List<OpenapiParameter> endpointParametersPut = getQueryParameterRefs(DdraPutPropertiesEndpoint.T, endpointParametersResolvingContext, "endpoint");

		modelEntitiesSorted(sessionScopedContext).forEach(e -> {
			e.getProperties().stream()
				.filter(p -> p.getDeclaringType() != GenericEntity.T)
				.forEach(p -> {
					String key = getPathKey("", e, "", true) + "/{id}/" + p.getName();
					paths.put(key, createPath(sessionScopedContext, p, e, endpointParametersGet, endpointParametersDelete, endpointParametersPut));
				});
		});
	}

	private OpenapiPath createPath(OpenapiContext context, Property property, EntityType<?> entityType, List<OpenapiParameter> endpointParametersGet, List<OpenapiParameter> endpointParametersDelete, List<OpenapiParameter> endpointParametersPut) {

		OpenapiOperation get = createOperation(context, property.getType());
		OpenapiOperation delete = createOperation(context, SimpleTypes.TYPE_BOOLEAN);
		OpenapiOperation put = createOperation(context, manipulationResponseType);

		get.getParameters().add(getIdParamRef(context));
		delete.getParameters().add(getIdParamRef(context));
		put.getParameters().add(getIdParamRef(context));

		get.getParameters().addAll(endpointParametersGet);
		delete.getParameters().addAll(endpointParametersDelete);
		put.getParameters().addAll(endpointParametersPut);
		
		GenericModelType propertyType = property.getType();
		OpenapiRequestBody reqestBody = context.components().requestBody(propertyType).ensure(currentContext -> {
			OpenapiRequestBody b = OpenapiRequestBody.T.create();
			b.setContent(createContent(propertyType, currentContext));
			b.setRequired(true);
			b.setDescription("Serialized " + propertyType.getTypeSignature());
			return b;
		}).getRef();

		put.setRequestBody(reqestBody);

		String typeSignature = entityType.getTypeSignature();
		int lastDotIndex = typeSignature.lastIndexOf('.');
		String simpleName = typeSignature.substring(lastDotIndex + 1);
		String packageName = typeSignature.substring(0, lastDotIndex);
		String tag = simpleName + " (" + packageName + ")";

		get.getTags().add(tag);
		delete.getTags().add(tag);
		put.getTags().add(tag);
		
		get.setDescription("Get the value of the *" + property.getName() + "* property of the *" + tag + "* with a certain id.");
		delete.setDescription("Delete the value of the *" + property.getName() + "* property of the *" + tag + "* with a certain id.");
		put.setDescription("Update the value of the *" + property.getName() + "* property of the *" + tag + "* with a certain id.");

		OpenapiPath path = OpenapiPath.T.create();
		path.setGet(get);
		path.setDelete(delete);
		path.setPut(put);

		return path;
	}

	private OpenapiParameter getIdParamRef(OpenapiContext context) {
		OpenapiParameter idParameter = context.components().parameter("PATH_ID").ensure(currentContext -> {
			OpenapiParameter param = OpenapiParameter.T.create();
			param.setName("id");
			param.setIn("path");
			param.setRequired(true);
			param.setSchema(getSchema(SimpleTypes.TYPE_STRING, currentContext));
			param.setDescription("The id of the entity to be adressed.");
			return param;
		}).getRef();
		return idParameter;
	}

	private OpenapiOperation createOperation(OpenapiContext context, GenericModelType responseType) {
		OpenapiOperation operation = OpenapiOperation.T.create();
		addResponsesToOperation(responseType, operation, context, true);
		return operation;
	}

	@Override
	protected String getTitle(ServiceRequestContext requestContext, OpenapiPropertiesRequest request) {
		return "CRUD properties in " + request.getAccessId();
	}

	@Override
	protected ModelAccessory getModelAccessory(ServiceRequestContext requestContext, OpenapiPropertiesRequest request) {
		return modelAccessoryFactory.getForAccess(request.getAccessId());
	}
	
	@Override
	protected String getBasePath(ServiceRequestContext requestContext, OpenapiPropertiesRequest request) {
		String tribefireServicesUrl = request.getTribefireServicesUrl();

		if (tribefireServicesUrl == null) {
			tribefireServicesUrl = TribefireRuntime.getPublicServicesUrl();
		}

		return tribefireServicesUrl + "/rest/v2/properties/" + request.getAccessId();
	}

}
